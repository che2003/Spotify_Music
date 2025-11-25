package org.example.spotify_music.controller;

import org.example.spotify_music.common.Result;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.vo.SongVo; // 引入 SongVo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SongMapper songMapper; // 用于调用联表查询方法

    /**
     * 模糊搜索歌曲和歌手 (返回 SongVo，包含歌手名)
     */
    @GetMapping
    public Result<List<SongVo>> search(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果关键词为空，返回空列表
            return Result.success(List.of());
        }
        // 调用 SongMapper 中联表查询的方法
        List<SongVo> list = songMapper.selectSongVoByKeyword(keyword);
        return Result.success(list);
    }
}