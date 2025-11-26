package org.example.spotify_music.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class AlbumSimpleVo {
    private Long id;
    private String title;
    private String coverUrl;
    private String description;
    private Long artistId;
    private String artistName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
}
