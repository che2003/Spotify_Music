package org.example.spotify_music.service;

import java.util.List;

public interface UserRoleService {
    /** 获取用户拥有的角色名列表 (e.g., ["admin", "musician"]) */
    List<String> getRoleKeysByUserId(Long userId);
}