package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.entity.UserRole;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // 需要开启鉴权
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserMapper userMapper;
    @Autowired private UserRoleMapper userRoleMapper;

    // 1. 获取所有用户列表 (仅管理员可用)
    // 简单起见，这里不分页，直接返回所有
    @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')") // 如果你配置了严格鉴权可解开
    public Result<List<User>> listUsers() {
        List<User> users = userMapper.selectList(null);
        // 为了安全，清空密码字段
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    // 2. 修改用户角色
    @PostMapping("/user/role")
    @Transactional
    public Result<?> updateUserRole(@RequestBody Map<String, Object> params) {
        Integer userIdInt = (Integer) params.get("userId");
        Long userId = Long.valueOf(userIdInt);
        String roleType = (String) params.get("role"); // "admin", "musician", "user"

        if (userId == null || roleType == null) return Result.error("参数错误");

        // 1. 先删除旧角色
        userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", userId));

        // 2. 确定新角色 ID (1=admin, 2=user, 3=musician)
        Long newRoleId = 2L; // 默认 user
        if ("admin".equals(roleType)) newRoleId = 1L;
        else if ("musician".equals(roleType)) newRoleId = 3L;

        // 3. 插入新角色
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(newRoleId);
        userRoleMapper.insert(ur);

        return Result.success("角色修改成功");
    }
}