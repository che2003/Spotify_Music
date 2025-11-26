package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Interaction;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.InteractionMapper;
import org.example.spotify_music.mapper.PlayHistoryMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.entity.PlayHistory;
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
    @Autowired private SongMapper songMapper;
    @Autowired private PlayHistoryMapper playHistoryMapper;

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    // 记录行为
    @PostMapping("/record")
    public Result<?> record(@RequestBody Interaction interaction) {
        interaction.setUserId(getCurrentUserId());

        boolean isPlay = interaction.getType() != null && interaction.getType() == 1;

        if (interaction.getRating() == null) {
            if (interaction.getType() == 3) {
                interaction.setRating(new BigDecimal("5.0"));
            } else if (isPlay) {
                interaction.setRating(new BigDecimal("3.0"));
            } else {
                interaction.setRating(new BigDecimal("1.0"));
            }
        }

        interactionMapper.insert(interaction);

        if (isPlay && interaction.getSongId() != null) {
            songMapper.incrementPlayCount(interaction.getSongId());

            PlayHistory history = new PlayHistory();
            history.setSongId(interaction.getSongId());
            history.setUserId(interaction.getUserId());
            history.setPlayTime(java.time.LocalDateTime.now());
            playHistoryMapper.insert(history);
        }

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