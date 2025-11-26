package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Interaction;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.InteractionMapper;
import org.example.spotify_music.mapper.SongMapper; // 【新增】
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/interaction")
public class InteractionController {

    @Autowired private InteractionMapper interactionMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private SongMapper songMapper; // 【新增注入】

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    // 记录行为
    @PostMapping("/record")
    public Result<?> record(@RequestBody Interaction interaction) {
        interaction.setUserId(getCurrentUserId());

        // 默认评分逻辑
        if (interaction.getRating() == null) {
            if (interaction.getType() == 3) interaction.setRating(new BigDecimal("5.0"));
            else if (interaction.getType() == 1) {
                interaction.setRating(new BigDecimal("3.0"));

                // 【核心逻辑】如果是播放(type=1)，则歌曲热度+1
                songMapper.incrementPlayCount(interaction.getSongId());
            }
            else interaction.setRating(new BigDecimal("1.0"));
        }

        interactionMapper.insert(interaction);
        return Result.success("行为已记录");
    }

    @GetMapping("/liked")
    public Result<List<Long>> getLikedSongs() {
        Long userId = getCurrentUserId();
        QueryWrapper<Interaction> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("type", 3).select("song_id");
        List<Interaction> list = interactionMapper.selectList(query);
        List<Long> songIds = list.stream().map(Interaction::getSongId).distinct().collect(Collectors.toList());
        return Result.success(songIds);
    }
}