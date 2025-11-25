package org.example.spotify_music.entity;

import com.baomidou.mybatisplus.annotation.TableName;

// 1. 删掉 @Data 和 @EqualsAndHashCode
@TableName("sys_role")
public class Role extends BaseEntity {
    private String roleKey;
    private String roleName;

    // 2. 手动补充 Getter / Setter
    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}