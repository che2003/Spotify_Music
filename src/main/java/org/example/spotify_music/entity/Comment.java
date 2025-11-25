package org.example.spotify_music.entity;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("song_comment")
public class Comment extends BaseEntity {
    private Long userId;
    private Long songId;
    private String content;

    // Getter/Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSongId() { return songId; }
    public void setSongId(Long songId) { this.songId = songId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}