package org.example.spotify_music.vo;

import java.util.List;

public class GenreBrowseVo {
    private Long id;
    private String name;
    private Integer songCount;
    private List<SongVo> songs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSongCount() {
        return songCount;
    }

    public void setSongCount(Integer songCount) {
        this.songCount = songCount;
    }

    public List<SongVo> getSongs() {
        return songs;
    }

    public void setSongs(List<SongVo> songs) {
        this.songs = songs;
    }
}
