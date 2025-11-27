package org.example.spotify_music.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("music_playlist_song")
public class PlaylistSong extends BaseEntity {
    private Long playlistId;
    private Long songId;
    @TableField("position")
    private BigDecimal sortOrder;

    // 手动 Getter/Setter
    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    public BigDecimal getSortOrder() { return sortOrder; }
    public void setSortOrder(BigDecimal sortOrder) { this.sortOrder = sortOrder; }
}