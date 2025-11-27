package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.JwtUtils;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.service.PlaylistService;
import org.example.spotify_music.service.UserRoleService; // 引入角色服务
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRoleService userRoleService; // 注入角色服务

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PlaylistService playlistService;

    // 注册
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.error("用户名或密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            return Result.error("注册失败，详细错误: " + e.getMessage());
        }

        // 创建默认「我喜欢」歌单
        playlistService.ensureLikedPlaylist(user.getId());
        return Result.success("注册成功");
    }

    // 登录 (修改点：返回角色信息)
    @PostMapping("/login")
    public Result<?> login(@RequestBody User loginUser) {
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", loginUser.getUsername());
        User dbUser = userMapper.selectOne(query);

        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(loginUser.getPassword(), dbUser.getPassword())) {
            return Result.error("密码错误");
        }

        // 1. 获取用户角色列表 (e.g. ["admin", "musician"])
        List<String> roles = userRoleService.getRoleKeysByUserId(dbUser.getId());

        // 2. 生成 Token (把主要角色放入 Token，方便后端鉴权)
        String mainRole = roles.isEmpty() ? "user" : roles.get(0);
        // 注意：如果你之前的 JwtUtils.generateToken 只有2个参数，请用2个参数的版本；如果是3个，请用下面的版本
        // 这里为了兼容性，展示 3 参数版本（假设你上次改了），如果报错请删掉 mainRole 参数
        String token = jwtUtils.generateToken(dbUser.getUsername(), dbUser.getId(), mainRole);

        // 3. 返回前端所需信息
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("username", dbUser.getUsername());
        map.put("avatar", dbUser.getAvatarUrl());
        map.put("roles", roles); // 【关键】把角色列表传给前端

        return Result.success(map);
    }
}