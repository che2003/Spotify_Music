package org.example.spotify_music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("play_history")
public class PlayHistory implements java.io.Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long songId;

    @TableField("play_time")
    private LocalDateTime playTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    public LocalDateTime getPlayTime() { return playTime; }
    public void setPlayTime(LocalDateTime playTime) { this.playTime = playTime; }
}
