package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Comment;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.CommentMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired private CommentMapper commentMapper;
    @Autowired private UserMapper userMapper;

    // 发布评论
    @PostMapping("/add")
    public Result<?> add(@RequestBody Comment comment) {
        // 从 Security 上下文获取当前登录的用户名
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 查出用户ID
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));

        comment.setUserId(user.getId());
        commentMapper.insert(comment);
        return Result.success("评论成功");
    }

    // 获取某首歌的评论列表
    @GetMapping("/list")
    public Result<List<Comment>> list(@RequestParam Long songId) {
        QueryWrapper<Comment> query = new QueryWrapper<>();
        query.eq("song_id", songId).orderByDesc("create_time");
        return Result.success(commentMapper.selectList(query));
    }
}