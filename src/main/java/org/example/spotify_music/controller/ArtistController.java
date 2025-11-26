package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/artist")
public class ArtistController {
    @Autowired private ArtistMapper artistMapper;
    @Autowired private UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<Artist>> list() {
        return Result.success(artistMapper.selectList(null));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Artist artist) {
        artistMapper.insert(artist);
        return Result.success("艺人添加成功");
    }

    // 当前音乐人数据看板：返回 totalFans / totalPlays 等统计字段
    @GetMapping("/stats/me")
    public Result<Artist> myStats() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("请先登录");
        }
        Artist artist = artistMapper.selectOne(new QueryWrapper<Artist>().eq("user_id", user.getId()));
        if (artist == null) {
            return Result.error("当前账号未绑定音乐人");
        }
        return Result.success(artist);
    }
}