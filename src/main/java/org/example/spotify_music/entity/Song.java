package org.example.spotify_music.entity;

import com.baomidou.mybatisplus.annotation.TableName;

// 1. 删掉 @Data 和 @EqualsAndHashCode
@TableName("music_song")
public class Song extends BaseEntity {

    private Long albumId;
    private Long artistId;
    private String title;
    private String fileUrl;
    private String coverUrl;
    private Integer duration;
    private String genre;
    private String lyrics;
    private String description;
    private Long playCount;

    // 2. 手动补充 Getter / Setter
    public Long getAlbumId() { return albumId; }
    public void setAlbumId(Long albumId) { this.albumId = albumId; }

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLyrics() { return lyrics; }
    public void setLyrics(String lyrics) { this.lyrics = lyrics; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPlayCount() { return playCount; }
    public void setPlayCount(Long playCount) { this.playCount = playCount; }
}