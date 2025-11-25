package org.example.spotify_music.entity;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_user_role")
public class UserRole {
    private Long userId;
    private Long roleId;

    // Getter/Setter
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}