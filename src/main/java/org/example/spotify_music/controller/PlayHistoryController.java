package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.PlayHistoryMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.vo.PlayHistoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/history")
public class PlayHistoryController {

    @Autowired private PlayHistoryMapper playHistoryMapper;
    @Autowired private UserMapper userMapper;

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    @GetMapping("/recent")
    public Result<List<PlayHistoryVo>> recent(@RequestParam(defaultValue = "30") Integer limit) {
        return Result.success(playHistoryMapper.selectRecentByUser(getCurrentUserId(), limit));
    }
}
