package org.example.spotify_music.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("music_playlist")
public class Playlist extends BaseEntity {
    private Long creatorId; // 谁创建的
    private String title;   // 歌单名
    private String coverUrl;// 封面
    private String description;
    private Boolean isPublic;

    // 手动 Getter/Setter
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
}