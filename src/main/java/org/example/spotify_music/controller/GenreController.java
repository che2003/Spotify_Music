package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Genre;
import org.example.spotify_music.mapper.GenreMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.vo.GenreBrowseVo;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/browse")
    public Result<List<GenreBrowseVo>> browse(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "songLimit", required = false) Integer songLimit) {

        QueryWrapper<Genre> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.lambda().like(Genre::getName, keyword.trim());
        }

        int limit = songLimit != null ? songLimit : 6;
        List<Genre> genres = genreMapper.selectList(wrapper);

        List<GenreBrowseVo> data = genres.stream().map(genre -> {
            GenreBrowseVo vo = new GenreBrowseVo();
            vo.setId(genre.getId());
            vo.setName(genre.getName());
            vo.setSongCount(songMapper.countSongByGenreId(genre.getId()));
            vo.setSongs(songMapper.selectSongVoByGenreId(genre.getId(), limit));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(data);
    }
}
