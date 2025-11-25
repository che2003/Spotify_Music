package org.example.spotify_music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.entity.Role;
import org.example.spotify_music.entity.UserRole;
import org.example.spotify_music.mapper.RoleMapper;
import org.example.spotify_music.mapper.UserRoleMapper;
import org.example.spotify_music.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<String> getRoleKeysByUserId(Long userId) {
        // 1. 查出用户拥有的所有 Role ID
        List<UserRole> userRoles = userRoleMapper.selectList(
                new QueryWrapper<UserRole>().eq("user_id", userId)
        );

        // 2. 提取 Role ID 列表
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return List.of(); // 没有角色
        }

        // 3. 根据 Role ID 查出 Role Key (如 admin, user, musician)
        List<Role> roles = roleMapper.selectBatchIds(roleIds);

        return roles.stream()
                .map(Role::getRoleKey)
                .collect(Collectors.toList());
    }
}