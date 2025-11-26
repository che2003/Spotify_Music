package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Song;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/stats")
public class StatsController {

    @Autowired private UserMapper userMapper;
    @Autowired private SongMapper songMapper;
    @Autowired private ArtistMapper artistMapper;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // 1. 基础统计卡片
        data.put("userCount", userMapper.selectCount(null));
        data.put("songCount", songMapper.selectCount(null));
        data.put("artistCount", artistMapper.selectCount(null));

        List<Artist> allArtists = artistMapper.selectList(null);
        long totalArtistFans = allArtists.stream()
                .map(Artist::getTotalFans)
                .filter(v -> v != null)
                .mapToLong(Integer::longValue)
                .sum();
        long totalArtistPlays = allArtists.stream()
                .map(Artist::getTotalPlays)
                .filter(v -> v != null)
                .mapToLong(Long::longValue)
                .sum();
        data.put("totalArtistFans", totalArtistFans);
        data.put("totalArtistPlays", totalArtistPlays);

        // 计算总播放量 (所有歌曲 playCount 之和)
        // 这里用流处理简单计算，数据量大时建议写 XML SQL: SELECT SUM(play_count) FROM music_song
        List<Song> allSongs = songMapper.selectList(null);
        long totalPlays = allSongs.stream().mapToLong(Song::getPlayCount).sum();
        data.put("totalPlays", totalPlays);

        // 2. 热门歌曲 Top 10 (用于柱状图)
        QueryWrapper<Song> topSongQuery = new QueryWrapper<>();
        topSongQuery.orderByDesc("play_count").last("LIMIT 10");
        topSongQuery.select("title", "play_count"); // 只查需要的字段
        data.put("topSongs", songMapper.selectList(topSongQuery));

        // 3. 流派分布 (用于饼图)
        QueryWrapper<Song> genreQuery = new QueryWrapper<>();
        genreQuery.select("genre", "count(*) as count")
                .groupBy("genre");
        // MyBatis Plus selectMaps 可以返回 List<Map<String, Object>>
        data.put("genreDistribution", songMapper.selectMaps(genreQuery));

        return Result.success(data);
    }
}