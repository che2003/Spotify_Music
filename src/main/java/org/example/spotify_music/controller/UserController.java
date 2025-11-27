package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.dto.PasswordChangeDto;
import org.example.spotify_music.dto.UserUpdateDto;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.Playlist;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.entity.UserArtistLike;
import org.example.spotify_music.entity.UserFollow;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.PlaylistMapper;
import org.example.spotify_music.mapper.UserArtistLikeMapper;
import org.example.spotify_music.mapper.UserFollowMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.vo.PublicProfileVo;
import org.example.spotify_music.vo.PublicUserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired private UserMapper userMapper;
    @Autowired private PlaylistMapper playlistMapper;
    @Autowired private UserFollowMapper userFollowMapper;
    @Autowired private UserArtistLikeMapper userArtistLikeMapper;
    @Autowired private ArtistMapper artistMapper;
    @Autowired private PasswordEncoder passwordEncoder; // 使用 SecurityConfig 暴露的编码器

    // 辅助方法：获取当前登录用户实体
    private User getCurrentUser() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

    private PublicUserVo toPublicUser(User user) {
        if (user == null) return null;
        PublicUserVo vo = new PublicUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        return vo;
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

    // 4. 公开的用户主页信息
    @GetMapping("/public/{userId}")
    public Result<PublicProfileVo> getPublicProfile(@PathVariable Long userId) {
        User targetUser = userMapper.selectById(userId);
        if (targetUser == null || Boolean.TRUE.equals(targetUser.getDeleted())) {
            return Result.error("用户不存在");
        }

        PublicProfileVo profileVo = new PublicProfileVo();
        profileVo.setUser(toPublicUser(targetUser));

        List<Playlist> playlists = playlistMapper.selectList(new QueryWrapper<Playlist>()
                .eq("creator_id", userId)
                .eq("visibility", "public"));
        profileVo.setPlaylists(playlists);

        List<UserArtistLike> artistLikes = userArtistLikeMapper.selectList(new QueryWrapper<UserArtistLike>()
                .eq("user_id", userId));
        if (artistLikes.isEmpty()) {
            profileVo.setLikedArtists(Collections.emptyList());
        } else {
            List<Long> artistIds = artistLikes.stream().map(UserArtistLike::getArtistId).collect(Collectors.toList());
            List<Artist> likedArtists = artistMapper.selectBatchIds(artistIds);
            profileVo.setLikedArtists(likedArtists);
        }

        List<UserFollow> followings = userFollowMapper.selectList(new QueryWrapper<UserFollow>()
                .eq("user_id", userId));
        if (followings.isEmpty()) {
            profileVo.setFollowing(Collections.emptyList());
        } else {
            List<Long> followingIds = followings.stream().map(UserFollow::getFollowedUserId).collect(Collectors.toList());
            List<User> followingUsers = userMapper.selectBatchIds(followingIds);
            profileVo.setFollowing(followingUsers.stream().map(this::toPublicUser).collect(Collectors.toList()));
        }

        List<UserFollow> fans = userFollowMapper.selectList(new QueryWrapper<UserFollow>()
                .eq("followed_user_id", userId));
        if (fans.isEmpty()) {
            profileVo.setFans(Collections.emptyList());
        } else {
            List<Long> fanIds = fans.stream().map(UserFollow::getUserId).collect(Collectors.toList());
            List<User> fanUsers = userMapper.selectBatchIds(fanIds);
            profileVo.setFans(fanUsers.stream().map(this::toPublicUser).collect(Collectors.toList()));
        }

        return Result.success(profileVo);
    }
}