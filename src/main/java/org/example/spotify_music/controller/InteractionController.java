package org.example.spotify_music.controller;

import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Interaction;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.InteractionMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/interaction")
public class InteractionController {

    @Autowired
    private InteractionMapper interactionMapper;
    @Autowired
    private UserMapper userMapper;

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    // 记录行为 (前端播放音乐时调用这个接口)
    // type: 1=播放, 2=点击, 3=喜欢(收藏), 4=跳过
    @PostMapping("/record")
    public Result<?> record(@RequestBody Interaction interaction) {
        interaction.setUserId(getCurrentUserId());

        // 如果没有传评分，默认给个隐式评分
        if (interaction.getRating() == null) {
            if (interaction.getType() == 3) interaction.setRating(new BigDecimal("5.0")); // 喜欢 = 5分
            else if (interaction.getType() == 1) interaction.setRating(new BigDecimal("3.0")); // 播放 = 3分
            else interaction.setRating(new BigDecimal("1.0"));
        }

        interactionMapper.insert(interaction);
        return Result.success("行为已记录");
    }
}