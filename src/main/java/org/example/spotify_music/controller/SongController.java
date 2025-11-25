package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Song;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.vo.SongVo; // 引入 SongVo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/song")
public class SongController {

    @Autowired
    private SongMapper songMapper; // 用于调用联表查询方法

    /**
     * 获取所有歌曲列表 (返回 SongVo，包含歌手名)
     */
    @GetMapping("/list")
    public Result<List<SongVo>> list() {
        // 调用 SongMapper 中联表查询的方法
        List<SongVo> songs = songMapper.selectSongVoList();
        return Result.success(songs);
    }

    /**
     * 新增歌曲 (仍接收 Song 实体，因为是写入数据库)
     */
    @PostMapping("/add")
    public Result<?> addSong(@RequestBody Song song) {
        songMapper.insert(song);
        return Result.success("歌曲添加成功");
    }

    /**
     * 根据 ID 获取歌曲详情 (可选：可以返回 SongVo)
     */
    @GetMapping("/{id}")
    public Result<Song> getById(@PathVariable Long id) {
        Song song = songMapper.selectById(id);
        return Result.success(song);
    }

    /**
     * 删除歌曲
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        songMapper.deleteById(id);
        return Result.success("歌曲删除成功");
    }
}