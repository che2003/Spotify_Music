package org.example.spotify_music.entity;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("music_artist")
public class Artist extends BaseEntity {
    private String name;
    private String bio; // 简介
    private String avatarUrl;
    private Long userId;
    private Integer totalFans;
    private Long totalPlays;
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Integer getTotalFans() { return totalFans; }
    public void setTotalFans(Integer totalFans) { this.totalFans = totalFans; }
    public Long getTotalPlays() { return totalPlays; }
    public void setTotalPlays(Long totalPlays) { this.totalPlays = totalPlays; }
}