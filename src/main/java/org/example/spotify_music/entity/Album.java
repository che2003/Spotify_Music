package org.example.spotify_music.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;

@TableName("music_album")
public class Album extends BaseEntity {
    private Long artistId;
    private String title;
    private String coverUrl;
    private String description;
    private LocalDate releaseDate;

    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
}