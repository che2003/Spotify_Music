package org.example.spotify_music.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("music_song_genre")
public class SongGenre implements java.io.Serializable {

    private Long songId;
    private Integer genreId;

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }

    public Integer getGenreId() {
        return genreId;
    }

    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }
}
