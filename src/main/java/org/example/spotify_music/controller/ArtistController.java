package org.example.spotify_music.controller;

import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.mapper.ArtistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/artist")
public class ArtistController {
    @Autowired private ArtistMapper artistMapper;

    @GetMapping("/list")
    public Result<List<Artist>> list() {
        return Result.success(artistMapper.selectList(null));
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Artist artist) {
        artistMapper.insert(artist);
        return Result.success("艺人添加成功");
    }
}