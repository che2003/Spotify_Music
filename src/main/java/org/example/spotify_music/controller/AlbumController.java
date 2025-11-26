package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.dto.AlbumRequest;
import org.example.spotify_music.entity.Album;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.AlbumMapper;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.vo.AlbumDetailVo;
import org.example.spotify_music.vo.AlbumSimpleVo;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.context.SecurityContextHolder;

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
    @Autowired private UserMapper userMapper;

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Artist getCurrentArtist() {
        Long userId = getCurrentUserId();
        return artistMapper.selectOne(new QueryWrapper<Artist>().eq("user_id", userId));
    }

    @GetMapping("/list")
    public Result<List<Album>> listAll(@RequestParam(value = "artistId", required = false) Long artistId) {
        QueryWrapper<Album> wrapper = new QueryWrapper<>();
        if (artistId != null) {
            wrapper.eq("artist_id", artistId);
        }
        wrapper.orderByDesc("create_time");
        return Result.success(albumMapper.selectList(wrapper));
    }

    @GetMapping("/my")
    public Result<List<Album>> myAlbums() {
        Artist artist = getCurrentArtist();
        if (artist == null) {
            return Result.success(List.of());
        }
        return Result.success(albumMapper.selectList(new QueryWrapper<Album>().eq("artist_id", artist.getId())));
    }

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

    @PostMapping("/create")
    public Result<?> create(@RequestBody AlbumRequest request) {
        Artist artist;
        if (isAdmin() && request.getArtistId() != null) {
            artist = artistMapper.selectById(request.getArtistId());
        } else {
            artist = getCurrentArtist();
        }

        if (artist == null) {
            return Result.error("未找到对应的音乐人");
        }

        Album album = new Album();
        album.setArtistId(artist.getId());
        album.setTitle(request.getTitle());
        album.setCoverUrl(request.getCoverUrl());
        album.setDescription(request.getDescription());
        album.setReleaseDate(request.getReleaseDate());
        albumMapper.insert(album);
        return Result.success("创建成功");
    }

    @PostMapping("/update")
    public Result<?> update(@RequestBody AlbumRequest request) {
        if (request.getId() == null) {
            return Result.error("缺少专辑ID");
        }
        Album album = albumMapper.selectById(request.getId());
        if (album == null) {
            return Result.error("专辑不存在");
        }

        boolean admin = isAdmin();
        if (!admin) {
            Artist current = getCurrentArtist();
            if (current == null || !album.getArtistId().equals(current.getId())) {
                return Result.error("无权修改他人的专辑");
            }
        }

        if (request.getArtistId() != null && admin) {
            album.setArtistId(request.getArtistId());
        }
        album.setTitle(request.getTitle());
        album.setCoverUrl(request.getCoverUrl());
        album.setDescription(request.getDescription());
        album.setReleaseDate(request.getReleaseDate());
        albumMapper.updateById(album);
        return Result.success("修改成功");
    }

    @PostMapping("/delete")
    public Result<?> delete(@RequestParam Long id) {
        Album album = albumMapper.selectById(id);
        if (album == null) {
            return Result.success("专辑不存在或已删除");
        }
        if (!isAdmin()) {
            Artist current = getCurrentArtist();
            if (current == null || !album.getArtistId().equals(current.getId())) {
                return Result.error("无权删除他人的专辑");
            }
        }
        albumMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
