package org.example.spotify_music.loader;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.dao.EmptyResultDataAccessException;

import java.io.File;
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

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "music.loader", name = "enabled", havingValue = "true")
public class MusicLibraryLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

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

    @Override
    public void run(String... args) throws Exception {
        File excelFile = Paths.get(excelPath).toFile();
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

            String fileUrl = resolveAudioPath(entry);
            String coverUrl = resolveCover(entry);
            String lyrics = resolveLyrics(entry);

            inserted += insertSong(entry, artistId, albumId, fileUrl, coverUrl, lyrics);
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

    private String resolveAudioPath(MusicLoadRow entry) {
        if (CharSequenceUtil.isNotBlank(entry.getMp3Location())) {
            return entry.getMp3Location();
        }
        Path resolved = Paths.get(audioRoot, entry.baseFileName() + ".mp3");
        return resolved.toString();
    }

    private String resolveCover(MusicLoadRow entry) {
        Optional<Path> candidate = findFirstExisting(entry.baseFileName(), coverRoot, ".jpg", ".png");
        return candidate.map(Path::toString).orElse(null);
    }

    private String resolveLyrics(MusicLoadRow entry) {
        Optional<Path> candidate = findFirstExisting(entry.baseFileName(), lyricsRoot, ".lrc", ".txt");
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

    private Optional<Path> findFirstExisting(String baseName, String root, String... extensions) {
        if (!CharSequenceUtil.isNotBlank(root)) {
            return Optional.empty();
        }
        for (String ext : extensions) {
            Path candidate = Paths.get(root, baseName + ext);
            if (Files.exists(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
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
}
