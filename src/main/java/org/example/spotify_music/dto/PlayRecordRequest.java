package org.example.spotify_music.dto;

public class PlayRecordRequest {
    private Long songId;

    public Long getSongId() {
        return songId;
    }

    public void setSongId(Long songId) {
        this.songId = songId;
    }
}
