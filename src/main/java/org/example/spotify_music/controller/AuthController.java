package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.JwtUtils;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 注册 (保持不变)
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return Result.error("用户名或密码不能为空");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败: " + e.getMessage());
        }
        return Result.success("注册成功");
    }

    // === 新增：登录接口 ===
    @PostMapping("/login")
    public Result<?> login(@RequestBody User loginUser) {
        // 1. 根据用户名查用户
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", loginUser.getUsername());
        User dbUser = userMapper.selectOne(query);

        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        // 2. 比对密码 (rawPassword, encodedPassword)
        if (!passwordEncoder.matches(loginUser.getPassword(), dbUser.getPassword())) {
            return Result.error("密码错误");
        }

        // 3. 生成 Token
        String token = jwtUtils.generateToken(dbUser.getUsername(), dbUser.getId());

        // 4. 返回 Token 和用户信息
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("username", dbUser.getUsername());
        map.put("avatar", dbUser.getAvatarUrl());

        return Result.success(map);
    }
}