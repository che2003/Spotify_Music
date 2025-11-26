package org.example.spotify_music.entity;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("user_follow")
public class UserFollow extends BaseEntity {
    private Long userId;        // 粉丝
    private Long followedUserId;// 被关注的人 (对应 sys_user 的 id)

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getFollowedUserId() { return followedUserId; }
    public void setFollowedUserId(Long followedUserId) { this.followedUserId = followedUserId; }
}