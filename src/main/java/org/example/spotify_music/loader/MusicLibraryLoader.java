package org.example.spotify_music.loader;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spotify_music.config.MinioProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.dao.EmptyResultDataAccessException;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "music.loader", name = "enabled", havingValue = "true")
public class MusicLibraryLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Value("${music.loader.base-dir:}")
    private String baseDir;

    @Value("${music.loader.excel-path:music_load/music_information/歌曲信息汇总.xlsx}")
    private String excelPath;

    @Value("${music.loader.audio-root:music_load/music_information/所有歌曲(MP3)}")
    private String audioRoot;

    @Value("${music.loader.cover-root:music_load/music_information/所有封面(Covers)}")
    private String coverRoot;

    @Value("${music.loader.lyrics-root:music_load/music_information/所有歌词(Lyrics)}")
    private String lyricsRoot;

    @Value("${music.loader.default-genre:Imported}")
    private String defaultGenre;

    private volatile boolean bucketPrepared = false;
    private volatile boolean minioAvailable = true;

    private Path loaderBaseDir;
    private Path audioRootPath;
    private Path coverRootPath;
    private Path lyricsRootPath;

    @Override
    public void run(String... args) throws Exception {
        loaderBaseDir = determineBaseDir();
        audioRootPath = resolveConfiguredRoot(audioRoot);
        coverRootPath = resolveConfiguredRoot(coverRoot);
        lyricsRootPath = resolveConfiguredRoot(lyricsRoot);

        File excelFile = resolvePath(excelPath).toFile();
        if (!excelFile.exists()) {
            log.warn("音乐导入跳过，找不到 Excel 源文件: {}", excelFile.getAbsolutePath());
            return;
        }

        log.info("开始读取音乐元数据：{}", excelFile.getAbsolutePath());
        ExcelReader reader = ExcelUtil.getReader(excelFile);
        List<Map<String, Object>> rows = reader.readAll();

        Map<String, Long> artistCache = new HashMap<>();
        Map<String, Map<String, Long>> albumCache = new HashMap<>();
        ensureGenreExists(defaultGenre);
        if (!ensureBucketAvailable()) {
            log.warn("跳过音乐导入：无法连接对象存储 (endpoint: {})", minioProperties.getEndpoint());
            return;
        }

        int inserted = 0;
        for (Map<String, Object> row : rows) {
            MusicLoadRow entry = MusicLoadRow.fromRow(row);
            if (CharSequenceUtil.isBlank(entry.getArtistName())) {
                log.warn("跳过记录，缺少歌手信息：{}", entry.getFileName());
                continue;
            }

            long artistId = ensureArtist(entry.getArtistName(), artistCache);
            long albumId = ensureAlbum(entry.getAlbumTitle(), artistId, albumCache);
            if (songExists(entry.getSongTitle(), artistId)) {
                continue;
            }

            String fileUrl = resolveAudioUrl(entry);
            String coverUrl = resolveCover(entry);
            String lyrics = resolveLyrics(entry);

            inserted += insertSong(entry, artistId, albumId, fileUrl, coverUrl, lyrics);
            updateAlbumCoverIfMissing(albumId, coverUrl);
        }

        log.info("音乐导入完成，本次新增 {} 首歌曲。", inserted);
    }

    private int insertSong(MusicLoadRow entry, long artistId, long albumId, String fileUrl, String coverUrl, String lyrics) {
        String sql = "INSERT INTO music_song (album_id, artist_id, title, file_url, cover_url, genre, lyrics, description) " +
                "VALUES (?,?,?,?,?,?,?,?)";

        return jdbcTemplate.update(sql, albumId, artistId, entry.getSongTitle(), fileUrl, coverUrl,
                defaultGenre, lyrics, buildDescription(entry));
    }

    private String buildDescription(MusicLoadRow entry) {
        return String.format("来自《%s》的本地导入歌曲（封面：%s）", entry.getAlbumTitle(), entry.getCoverStatus());
    }

    private String resolveCover(MusicLoadRow entry) {
        Optional<Path> candidate = findFirstExisting(entry.baseFileName(), coverRootPath, ".jpg", ".png");
        return candidate.map(path -> uploadToMinio(path, "image/jpeg")).orElse(null);
    }

    private String resolveLyrics(MusicLoadRow entry) {
        Optional<Path> candidate = findLyricsPath(entry.baseFileName());
        if (candidate.isEmpty()) {
            return null;
        }
        try {
            return Files.readString(candidate.get(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.warn("读取歌词失败：{}", candidate.get(), ex);
            return null;
        }
    }

    private Optional<Path> findLyricsPath(String baseName) {
        if (lyricsRootPath == null) {
            return Optional.empty();
        }

        List<Path> candidateRoots = List.of(
                lyricsRootPath,
                lyricsRootPath.resolve("所有歌词(Lyrics)"),
                lyricsRootPath.resolve(Paths.get("lrc", "所有歌词(Lyrics)"))
        );

        for (Path root : candidateRoots) {
            Optional<Path> candidate = findFirstExisting(baseName, root, ".lrc", ".txt");
            if (candidate.isPresent()) {
                return candidate;
            }
        }

        return Optional.empty();
    }

    private Optional<Path> findFirstExisting(String baseName, Path root, String... extensions) {
        if (root == null) {
            return Optional.empty();
        }
        for (String ext : extensions) {
            Path candidate = root.resolve(baseName + ext);
            if (Files.exists(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private String resolveAudioUrl(MusicLoadRow entry) {
        String mp3Location = entry.getMp3Location();
        if (CharSequenceUtil.isNotBlank(mp3Location) && mp3Location.startsWith("http")) {
            return mp3Location;
        }

        Optional<Path> audioPath = Optional.empty();
        if (CharSequenceUtil.isNotBlank(mp3Location)) {
            Path manual = resolvePath(mp3Location);
            if (Files.exists(manual)) {
                audioPath = Optional.of(manual);
            } else {
                log.warn("指定的 MP3 路径不存在：{}", manual);
            }
        }

        if (audioPath.isEmpty()) {
            Optional<Path> fallback = findFirstExisting(entry.baseFileName(), audioRootPath, ".mp3");
            if (fallback.isPresent()) {
                audioPath = fallback;
            }
        }

        return audioPath.map(path -> uploadToMinio(path, "audio/mpeg")).orElse(null);
    }

    private Path resolveConfiguredRoot(String configured) {
        if (!CharSequenceUtil.isNotBlank(configured)) {
            return null;
        }
        return resolvePath(configured);
    }

    private Path determineBaseDir() {
        if (CharSequenceUtil.isNotBlank(baseDir)) {
            Path configured = Paths.get(baseDir);
            if (configured.isAbsolute()) {
                return configured.normalize();
            }
            return Paths.get("").toAbsolutePath().resolve(configured).normalize();
        }

        return Paths.get("").toAbsolutePath();
    }

    private Path resolvePath(String rawPath) {
        Path path = Paths.get(rawPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return loaderBaseDir.resolve(path).normalize();
    }

    private String uploadToMinio(Path filePath, String fallbackContentType) {
        if (!ensureBucketAvailable()) {
            return null;
        }

        try (InputStream stream = Files.newInputStream(filePath)) {

            String objectName = buildObjectName(filePath.getFileName().toString());
            long size = Files.size(filePath);
            String contentType = Optional.ofNullable(Files.probeContentType(filePath))
                    .filter(CharSequenceUtil::isNotBlank)
                    .orElse(fallbackContentType);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(stream, size, -1)
                    .contentType(contentType)
                    .build());

            return String.format("%s/%s/%s", minioProperties.getEndpoint(), minioProperties.getBucket(), objectName);
        } catch (Exception ex) {
            log.warn("上传文件到存储失败：{}", filePath, ex);
            return null;
        }
    }

    private String buildObjectName(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }
        return "imports/" + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private boolean ensureBucketAvailable() {
        if (!minioAvailable) {
            return false;
        }
        if (bucketPrepared) {
            return true;
        }
        synchronized (this) {
            if (bucketPrepared) {
                return true;
            }
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .build());
                }
                bucketPrepared = true;
            } catch (Exception ex) {
                minioAvailable = false;
                log.warn("对象存储不可用，无法访问或创建桶：{}", minioProperties.getBucket(), ex);
                return false;
            }
        }
        return true;
    }

    private boolean songExists(String title, long artistId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM music_song WHERE title = ? AND artist_id = ?",
                    Integer.class, title, artistId);
            return count != null && count > 0;
        } catch (Exception ex) {
            log.warn("检查歌曲是否存在时发生异常，继续导入。", ex);
            return false;
        }
    }

    private long ensureArtist(String artistName, Map<String, Long> cache) {
        if (cache.containsKey(artistName)) {
            return cache.get(artistName);
        }

        Long existingId = queryForId("SELECT id FROM music_artist WHERE name = ? LIMIT 1", artistName);
        if (existingId != null) {
            cache.put(artistName, existingId);
            return existingId;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO music_artist (name, bio, total_fans, total_plays) VALUES (?,?,0,0)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, artistName);
            ps.setString(2, "本地导入的音乐人");
            return ps;
        }, keyHolder);

        long id = Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).orElseThrow();
        cache.put(artistName, id);
        return id;
    }

    private long ensureAlbum(String albumTitle, long artistId, Map<String, Map<String, Long>> cache) {
        Map<String, Long> artistAlbums = cache.computeIfAbsent(String.valueOf(artistId), k -> new HashMap<>());
        if (artistAlbums.containsKey(albumTitle)) {
            return artistAlbums.get(albumTitle);
        }

        Long existingId = queryForId(
                "SELECT id FROM music_album WHERE title = ? AND artist_id = ? LIMIT 1",
                albumTitle, artistId);
        if (existingId != null) {
            artistAlbums.put(albumTitle, existingId);
            return existingId;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO music_album (artist_id, title, description) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, artistId);
            ps.setString(2, albumTitle);
            ps.setString(3, "本地导入专辑");
            return ps;
        }, keyHolder);

        long id = Optional.ofNullable(keyHolder.getKey()).map(Number::longValue).orElseThrow();
        artistAlbums.put(albumTitle, id);
        return id;
    }

    private void ensureGenreExists(String genreName) {
        Long id = queryForId("SELECT id FROM music_genre WHERE name = ? LIMIT 1", genreName);
        if (id != null) {
            return;
        }
        jdbcTemplate.update("INSERT INTO music_genre (name) VALUES (?)", genreName);
    }

    private Long queryForId(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, args);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    private void updateAlbumCoverIfMissing(long albumId, String coverUrl) {
        if (CharSequenceUtil.isBlank(coverUrl)) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM music_album WHERE id = ? AND cover_url IS NOT NULL AND cover_url <> ''",
                Integer.class, albumId);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update("UPDATE music_album SET cover_url = ? WHERE id = ?", coverUrl, albumId);
    }
}
