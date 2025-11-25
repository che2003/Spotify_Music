package org.example.spotify_music.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("music_playlist_song")
public class PlaylistSong extends BaseEntity {
    private Long playlistId;
    private Long songId;
    private Integer sortOrder;

    // 手动 Getter/Setter
    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}