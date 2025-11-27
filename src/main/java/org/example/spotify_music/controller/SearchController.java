package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Album;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.Playlist;
import org.example.spotify_music.mapper.AlbumMapper;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.PlaylistMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.vo.AlbumSimpleVo;
import org.example.spotify_music.vo.SearchResultVo;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired private SongMapper songMapper;
    @Autowired private ArtistMapper artistMapper;
    @Autowired private AlbumMapper albumMapper;
    @Autowired private PlaylistMapper playlistMapper;

    @GetMapping
    public Result<SearchResultVo> search(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(new SearchResultVo());
        }

        String trimmed = keyword.trim();

        List<SongVo> songs = songMapper.selectSongVoByKeyword(trimmed);

        List<Artist> artists = artistMapper.selectList(
                new QueryWrapper<Artist>().like("name", trimmed)
        );

        List<Album> albums = albumMapper.selectList(
                new QueryWrapper<Album>().like("title", trimmed)
        );
        Map<Long, Artist> artistMap = new HashMap<>();
        if (!albums.isEmpty()) {
            List<Long> artistIds = albums.stream().map(Album::getArtistId).collect(Collectors.toList());
            artistMap = artistMapper.selectBatchIds(artistIds).stream()
                    .collect(Collectors.toMap(Artist::getId, a -> a));
        }
        List<AlbumSimpleVo> albumVos = new ArrayList<>();
        for (Album album : albums) {
            AlbumSimpleVo vo = new AlbumSimpleVo();
            vo.setId(album.getId());
            vo.setTitle(album.getTitle());
            vo.setCoverUrl(album.getCoverUrl());
            vo.setDescription(album.getDescription());
            vo.setReleaseDate(album.getReleaseDate());
            vo.setArtistId(album.getArtistId());
            Artist artist = artistMap.get(album.getArtistId());
            if (artist != null) { vo.setArtistName(artist.getName()); }
            albumVos.add(vo);
        }

        List<Playlist> playlists = playlistMapper.selectList(
                new QueryWrapper<Playlist>()
                        .eq("visibility", "public")
                        .like("title", trimmed)
        );

        SearchResultVo result = new SearchResultVo();
        result.setSongs(songs);
        result.setArtists(artists);
        result.setAlbums(albumVos);
        result.setPlaylists(playlists);

        return Result.success(result);
    }
}