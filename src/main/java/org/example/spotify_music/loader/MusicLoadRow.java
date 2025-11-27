package org.example.spotify_music.loader;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.Map;

/**
 * 一个小型的只读数据载体，用于从 Excel 行中抽取音乐元数据。
 */
public class MusicLoadRow {

    private final String fileName;
    private final String artistName;
    private final String songTitle;
    private final String albumTitle;
    private final String mp3Location;
    private final String coverStatus;

    private MusicLoadRow(String fileName, String artistName, String songTitle, String albumTitle,
                        String mp3Location, String coverStatus) {
        this.fileName = fileName;
        this.artistName = artistName;
        this.songTitle = songTitle;
        this.albumTitle = albumTitle;
        this.mp3Location = mp3Location;
        this.coverStatus = coverStatus;
    }

    public static MusicLoadRow fromRow(Map<String, Object> row) {
        String fileName = getString(row, "文件名");
        String artist = getString(row, "歌手");
        String song = getString(row, "歌名");
        String album = getString(row, "专辑");
        String mp3 = getString(row, "MP3位置");
        String cover = getString(row, "封面");

        if (CharSequenceUtil.isBlank(song)) {
            song = deriveSongTitleFromFileName(fileName);
        }

        if (CharSequenceUtil.isBlank(album)) {
            album = "Single";
        }

        return new MusicLoadRow(fileName, artist, song, album, mp3, cover);
    }

    private static String deriveSongTitleFromFileName(String fileName) {
        if (!CharSequenceUtil.isNotBlank(fileName)) {
            return "Unknown Song";
        }

        String base = fileName.replaceAll("\\.[^.]+$", "");
        String[] parts = base.split(" - ", 2);
        if (parts.length == 2) {
            return CharSequenceUtil.isNotBlank(parts[1]) ? parts[1] : base;
        }
        return base;
    }

    private static String getString(Map<String, Object> row, String key) {
        Object value = row.get(key);
        return value == null ? "" : value.toString().trim();
    }

    public String getFileName() {
        return fileName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public String getMp3Location() {
        return mp3Location;
    }

    public String getCoverStatus() {
        return coverStatus;
    }

    public String baseFileName() {
        return fileName.replaceAll("\\.[^.]+$", "");
    }
}
