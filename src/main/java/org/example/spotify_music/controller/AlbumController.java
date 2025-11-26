package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Album;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.mapper.AlbumMapper;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.vo.AlbumDetailVo;
import org.example.spotify_music.vo.AlbumSimpleVo;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/album")
public class AlbumController {

    @Autowired private AlbumMapper albumMapper;
    @Autowired private ArtistMapper artistMapper;
    @Autowired private SongMapper songMapper;

    @GetMapping("/new")
    public Result<List<AlbumSimpleVo>> latestAlbums(@RequestParam(defaultValue = "10") Integer limit) {
        List<Album> albums = albumMapper.selectList(new QueryWrapper<Album>()
                .orderByDesc("release_date", "create_time")
                .last("limit " + limit));

        Map<Long, Artist> artistMap = new HashMap<>();
        if (!albums.isEmpty()) {
            List<Long> artistIds = albums.stream().map(Album::getArtistId).collect(Collectors.toList());
            artistMap = artistMapper.selectBatchIds(artistIds).stream()
                    .collect(Collectors.toMap(Artist::getId, a -> a));
        }

        List<AlbumSimpleVo> vos = new ArrayList<>();
        for (Album album : albums) {
            AlbumSimpleVo vo = new AlbumSimpleVo();
            vo.setId(album.getId());
            vo.setTitle(album.getTitle());
            vo.setCoverUrl(album.getCoverUrl());
            vo.setDescription(album.getDescription());
            vo.setReleaseDate(album.getReleaseDate());
            vo.setArtistId(album.getArtistId());
            Artist artist = artistMap.get(album.getArtistId());
            if (artist != null) {
                vo.setArtistName(artist.getName());
            }
            vos.add(vo);
        }

        return Result.success(vos);
    }

    @GetMapping("/{id}")
    public Result<AlbumDetailVo> detail(@PathVariable Long id) {
        Album album = albumMapper.selectById(id);
        if (album == null) {
            return Result.error("专辑不存在");
        }

        AlbumDetailVo vo = new AlbumDetailVo();
        vo.setId(album.getId());
        vo.setTitle(album.getTitle());
        vo.setCoverUrl(album.getCoverUrl());
        vo.setDescription(album.getDescription());
        vo.setReleaseDate(album.getReleaseDate());
        vo.setArtistId(album.getArtistId());

        Artist artist = artistMapper.selectById(album.getArtistId());
        if (artist != null) {
            vo.setArtistName(artist.getName());
        }

        List<SongVo> songs = songMapper.selectSongVoByAlbumId(id);
        vo.setSongs(songs);

        return Result.success(vo);
    }
}
