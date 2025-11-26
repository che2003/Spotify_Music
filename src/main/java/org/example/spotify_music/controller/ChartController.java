package org.example.spotify_music.controller;

import org.example.spotify_music.common.Result;
import org.example.spotify_music.mapper.PlayHistoryMapper;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/charts")
public class ChartController {

    @Autowired
    private PlayHistoryMapper playHistoryMapper;

    @GetMapping("/top-songs")
    public Result<List<SongVo>> topSongs(@RequestParam(value = "days", required = false) Integer days,
                                         @RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        return Result.success(playHistoryMapper.selectTopPlayedSongs(days, limit));
    }

    @GetMapping("/overview")
    public Result<Map<String, List<SongVo>>> overview(@RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        Map<String, List<SongVo>> data = new HashMap<>();
        data.put("weekly", playHistoryMapper.selectTopPlayedSongs(7, limit));
        data.put("monthly", playHistoryMapper.selectTopPlayedSongs(30, limit));
        data.put("allTime", playHistoryMapper.selectTopPlayedSongs(null, limit));
        return Result.success(data);
    }
}
