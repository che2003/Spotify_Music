package org.example.spotify_music.loader;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spotify_music.config.MinioProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "music.loader", name = "enabled", havingValue = "true")
public class MusicLibraryLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final TransactionTemplate transactionTemplate;
    private final MinioBootstrapper minioBootstrapper;


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

    private Path loaderBaseDir;
    private Path audioRootPath;
    private Path coverRootPath;
    private Path lyricsRootPath;

    @Override
    public void run(String... args) throws Exception {
        loaderBaseDir = determineBaseDir();
        // 初始化路径配置
        audioRootPath = resolveConfiguredRoot(audioRoot);
        coverRootPath = resolveConfiguredRoot(coverRoot);
        lyricsRootPath = resolveConfiguredRoot(lyricsRoot);

        log.info("音乐导入基准路径：{}", loaderBaseDir);
        log.info("音频目录：{}，封面目录：{}，歌词目录：{}", audioRootPath, coverRootPath, lyricsRootPath);

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
        if (!minioBootstrapper.ensureBucketAvailable("music-loader")) {
            log.warn("对象存储不可用，音频和封面将不上传，仅落库文本及元数据 (endpoint: {})", minioProperties.getEndpoint());
        }

        int processed = 0;
        for (Map<String, Object> row : rows) {
            MusicLoadRow entry = MusicLoadRow.fromRow(row);
            if (CharSequenceUtil.isBlank(entry.getArtistName())) {
                log.warn("跳过记录，缺少歌手信息：{}", entry.getFileName());
                continue;
            }

            processed += processRow(entry, artistCache, albumCache);
        }

        log.info("音乐导入完成，本次处理（新增或更新） {} 首歌曲。", processed);
    }

    private int processRow(MusicLoadRow entry, Map<String, Long> artistCache, Map<String, Map<String, Long>> albumCache) {
        return Optional.ofNullable(transactionTemplate.execute(status -> {
            long artistId = ensureArtist(entry.getArtistName(), artistCache);
            long albumId = ensureAlbum(entry.getAlbumTitle(), artistId, albumCache);

            String fileUrl = resolveAudioUrl(entry);
            String coverUrl = resolveCover(entry);

            // 【新增】如果 MinIO 没启动导致 url 为空，给一个占位符，防止数据库报错
            if (fileUrl == null) {
                // 只有在是“新增”且没有URL时才给占位符；如果是更新，可能不想覆盖为空，但在 processRow 层面不知道是新增还是更新
                // 为了简单起见，如果这里没拿到 URL，我们给个占位符，但在 updateSong 里我们会做非空判断
                // 不过数据库 file_url 是 NOT NULL，如果是 Insert 必须有值。
                fileUrl = "http://localhost:8080/unavailable.mp3";
                // log.warn("MinIO unavailable or file not found, using placeholder for: {}", entry.getSongTitle());
            }

            String lyrics = resolveLyrics(entry);

            // 【修改】检查歌曲是否存在，存在则更新，不存在则插入
            if (songExists(entry.getSongTitle(), artistId)) {
                log.info("更新已存在的歌曲：{} - {}", entry.getArtistName(), entry.getSongTitle());
                // 注意：如果 fileUrl 是占位符，且数据库里已经有真实 URL，我们可能不希望覆盖。
                // 这里的策略是：只要 resolveAudioUrl 返回了（哪怕是占位符），就认为是最新的状态。
                // 如果您希望“如果本地没文件就不更新 URL”，可以在 updateSong 里判断 fileUrl 是否包含 "unavailable"
                updateSong(entry, artistId, albumId, fileUrl, coverUrl, lyrics);
                // 更新后也尝试更新专辑封面（如果专辑还没封面）
                updateAlbumCoverIfMissing(albumId, coverUrl);
                return 1;
            } else {
                int affected = insertSong(entry, artistId, albumId, fileUrl, coverUrl, lyrics);
                updateAlbumCoverIfMissing(albumId, coverUrl);
                return affected;
            }
        })).orElse(0);
    }

    // 【新增】更新歌曲方法
    private void updateSong(MusicLoadRow entry, long artistId, long albumId, String fileUrl, String coverUrl, String lyrics) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE music_song SET album_id = ?, description = ?, genre = ?");
        params.add(albumId);
        params.add(buildDescription(entry));
        params.add(defaultGenre);

        // 只有当 fileUrl 有效（非空且不是我们的占位符，或者是占位符但我们想覆盖）时才更新
        // 这里简化逻辑：只要不为空就更新。如果您不想用占位符覆盖旧的真实链接，可以加判断。
        if (CharSequenceUtil.isNotBlank(fileUrl)) {
            sql.append(", file_url = ?");
            params.add(fileUrl);
        }
        if (CharSequenceUtil.isNotBlank(coverUrl)) {
            sql.append(", cover_url = ?");
            params.add(coverUrl);
        }
        if (CharSequenceUtil.isNotBlank(lyrics)) {
            sql.append(", lyrics = ?");
            params.add(lyrics);
        }

        sql.append(" WHERE title = ? AND artist_id = ?");
        params.add(entry.getSongTitle());
        params.add(artistId);

        jdbcTemplate.update(sql.toString(), params.toArray());
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
        if (candidate.isEmpty()) {
            // log.warn("未找到封面文件：{}", entry.baseFileName());
            return null;
        }
        return uploadToMinio(candidate.get(), "image/jpeg");
    }

    private String resolveLyrics(MusicLoadRow entry) {
        Optional<Path> candidate = findLyricsPath(entry.baseFileName());
        if (candidate.isEmpty()) {
            // log.warn("未找到歌词文件：{}", entry.baseFileName());
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
                // log.warn("指定的 MP3 路径不存在：{}", manual);
            }
        }

        if (audioPath.isEmpty()) {
            Optional<Path> fallback = findFirstExisting(entry.baseFileName(), audioRootPath, ".mp3");
            if (fallback.isPresent()) {
                audioPath = fallback;
            } else {
                log.warn("未找到音频文件：{}", entry.baseFileName());
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
        Path anchorRoot = detectAnchorRoot();

        if (CharSequenceUtil.isNotBlank(baseDir)) {
            Path configured = Paths.get(baseDir);
            return configured.isAbsolute() ? configured.normalize() : anchorRoot.resolve(configured).normalize();
        }

        return anchorRoot;
    }

    private Path detectAnchorRoot() {
        Path codeSourceRoot = locateCodeSourceRoot();
        Path anchored = findProjectRoot(codeSourceRoot);
        if (anchored != null) {
            return anchored;
        }

        Path workingDir = Paths.get("").toAbsolutePath();
        Path workingAnchored = findProjectRoot(workingDir);
        if (workingAnchored != null) {
            return workingAnchored;
        }

        return codeSourceRoot;
    }

    private Path locateCodeSourceRoot() {
        try {
            Path codeSource = Paths.get(MusicLibraryLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path base = Files.isRegularFile(codeSource) ? codeSource.getParent() : codeSource;

            if (base != null && base.getFileName() != null &&
                    ("classes".equals(base.getFileName().toString()) || "test-classes".equals(base.getFileName().toString()))) {
                Path parent = base.getParent();
                if (parent != null && parent.getParent() != null) {
                    return parent.getParent().normalize();
                }
            }

            return base == null ? Paths.get("").toAbsolutePath() : base.normalize();
        } catch (Exception ex) {
            log.warn("自动定位项目根目录失败，使用当前工作目录。", ex);
            return Paths.get("").toAbsolutePath().normalize();

        }
    }


    private Path findProjectRoot(Path start) {
        Path current = start;
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) || Files.exists(current.resolve("mvnw"))) {
                return current.normalize();
            }
            current = current.getParent();
        }
        return null;
    }

    // 【修改】增强的路径解析逻辑：支持自动尝试添加 music_load 前缀
    private Path resolvePath(String rawPath) {
        Path path = Paths.get(rawPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }

        // 1. 先尝试直接解析（即按 Excel/配置 原文找）
        Path directPath = loaderBaseDir.resolve(path).normalize();
        if (Files.exists(directPath)) {
            return directPath;
        }

        // 2. 如果找不到，尝试加上 "music_load" 前缀再找一次
        // (解决 Excel 里少写了 "music_load/" 的问题，以及确保优先使用 music_load 下的资源)
        Path fixedPath = loaderBaseDir.resolve("music_load").resolve(path).normalize();
        if (Files.exists(fixedPath)) {
            return fixedPath;
        }

        // 3. 都找不到，返回原始路径让上层逻辑去处理（比如报错或 fallback）
        return directPath;
    }

    private String uploadToMinio(Path filePath, String fallbackContentType) {
        if (!minioBootstrapper.ensureBucketAvailable("music-loader-upload")) {
            log.warn("对象存储不可用，跳过文件上传：{}", filePath);
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
        // 如果是更新封面，我们不需要检查是否为 NULL，而是可以尝试用最新的封面去更新专辑封面
        // 但为了不频繁更新，这里保留原逻辑：只有当专辑没封面时才更新。
        // 如果您希望歌曲封面变了，专辑封面也跟着变，可以去掉这个 COUNT 检查。
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM music_album WHERE id = ? AND cover_url IS NOT NULL AND cover_url <> ''",
                Integer.class, albumId);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.update("UPDATE music_album SET cover_url = ? WHERE id = ?", coverUrl, albumId);
    }
}