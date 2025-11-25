package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.dto.PasswordChangeDto;
import org.example.spotify_music.dto.UserUpdateDto;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired private UserMapper userMapper;
    @Autowired private PasswordEncoder passwordEncoder; // 使用 SecurityConfig 暴露的编码器

    // 辅助方法：获取当前登录用户实体
    private User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

    // 1. 获取用户个人资料
    @GetMapping("/profile")
    public Result<User> getProfile() {
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录或不存在");
        }
        // 为了安全，查询时不返回密码哈希
        user.setPassword(null);
        return Result.success(user);
    }

    // 2. 更新用户资料 (昵称/邮箱/头像URL)
    @PostMapping("/update")
    public Result<?> updateProfile(@RequestBody UserUpdateDto dto) {
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录或不存在");
        }

        // 使用 BeanUtils 复制属性，避免直接覆盖ID等关键字段
        BeanUtils.copyProperties(dto, user);

        userMapper.updateById(user);
        return Result.success("资料更新成功");
    }

    // 3. 修改密码
    @PostMapping("/changePassword")
    public Result<?> changePassword(@RequestBody PasswordChangeDto dto) {
        User user = getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录或不存在");
        }

        // A. 校验旧密码是否正确
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误，请重新输入");
        }

        // B. 加密新密码并更新
        String newHashedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(newHashedPassword);
        userMapper.updateById(user);

        return Result.success("密码修改成功");
    }
}