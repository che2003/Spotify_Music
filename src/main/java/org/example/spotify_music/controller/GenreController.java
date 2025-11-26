package org.example.spotify_music.controller;

import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Genre;
import org.example.spotify_music.mapper.GenreMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/genre")
public class GenreController {

    @Autowired private GenreMapper genreMapper;
    @Autowired private SongMapper songMapper;

    @GetMapping("/list")
    public Result<List<Genre>> listGenres() {
        return Result.success(genreMapper.selectList(null));
    }

    @GetMapping("/{id}/songs")
    public Result<List<SongVo>> songsByGenre(@PathVariable("id") Long id,
                                             @RequestParam(value = "limit", required = false) Integer limit) {
        return Result.success(songMapper.selectSongVoByGenreId(id, limit));
    }
}
