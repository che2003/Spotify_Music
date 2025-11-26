package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.entity.UserFollow;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.UserFollowMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired private UserFollowMapper followMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ArtistMapper artistMapper;

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    // 切换关注状态 (关注/取关 艺人)
    @PostMapping("/artist")
    public Result<?> toggleFollowArtist(@RequestParam Long artistId) {
        Long currentUserId = getCurrentUserId();

        // 1. 查出这个艺人对应的 UserId
        Artist artist = artistMapper.selectById(artistId);
        if (artist == null) return Result.error("艺人不存在");
        Long targetUserId = artist.getUserId();

        if (targetUserId == null) return Result.error("该艺人未绑定账号，无法关注");
        if (targetUserId.equals(currentUserId)) return Result.error("不能关注自己");

        // 2. 检查是否已关注
        QueryWrapper<UserFollow> query = new QueryWrapper<>();
        query.eq("user_id", currentUserId).eq("followed_user_id", targetUserId);
        UserFollow existing = followMapper.selectOne(query);

        if (existing != null) {
            // 已关注 -> 取消
            followMapper.deleteById(existing.getId());
            return Result.success("已取消关注");
        } else {
            // 未关注 -> 关注
            UserFollow follow = new UserFollow();
            follow.setUserId(currentUserId);
            follow.setFollowedUserId(targetUserId);
            followMapper.insert(follow);
            return Result.success("关注成功");
        }
    }

    // 检查是否关注了某位艺人
    @GetMapping("/check")
    public Result<Boolean> checkFollow(@RequestParam Long artistId) {
        Long currentUserId = getCurrentUserId();
        Artist artist = artistMapper.selectById(artistId);
        if (artist == null || artist.getUserId() == null) return Result.success(false);

        Long count = followMapper.selectCount(new QueryWrapper<UserFollow>()
                .eq("user_id", currentUserId)
                .eq("followed_user_id", artist.getUserId()));

        return Result.success(count > 0);
    }

    // 关注列表（我关注的人）
    @GetMapping("/following")
    public Result<List<User>> followingList() {
        Long currentUserId = getCurrentUserId();
        List<UserFollow> relations = followMapper.selectList(new QueryWrapper<UserFollow>()
                .eq("user_id", currentUserId));
        if (relations.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Long> ids = relations.stream().map(UserFollow::getFollowedUserId).collect(Collectors.toList());
        return Result.success(userMapper.selectBatchIds(ids));
    }

    // 粉丝列表（关注我的人）
    @GetMapping("/fans")
    public Result<List<User>> fansList() {
        Long currentUserId = getCurrentUserId();
        List<UserFollow> relations = followMapper.selectList(new QueryWrapper<UserFollow>()
                .eq("followed_user_id", currentUserId));
        if (relations.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Long> ids = relations.stream().map(UserFollow::getUserId).collect(Collectors.toList());
        return Result.success(userMapper.selectBatchIds(ids));
    }
}