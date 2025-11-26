package org.example.spotify_music.controller;

import org.example.spotify_music.common.Result;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SongMapper songMapper;

    @GetMapping
    public Result<List<SongVo>> search(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(List.of());
        }
        // 使用新的联表搜索方法
        return Result.success(songMapper.selectSongVoByKeyword(keyword));
    }
}