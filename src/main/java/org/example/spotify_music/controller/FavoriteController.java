package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Album;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.entity.UserAlbumLike;
import org.example.spotify_music.entity.UserArtistLike;
import org.example.spotify_music.mapper.AlbumMapper;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.UserAlbumLikeMapper;
import org.example.spotify_music.mapper.UserArtistLikeMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired private UserArtistLikeMapper userArtistLikeMapper;
    @Autowired private UserAlbumLikeMapper userAlbumLikeMapper;
    @Autowired private ArtistMapper artistMapper;
    @Autowired private AlbumMapper albumMapper;
    @Autowired private UserMapper userMapper;

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    @PostMapping("/artist/toggle")
    public Result<?> toggleArtist(@RequestParam Long artistId) {
        Artist artist = artistMapper.selectById(artistId);
        if (artist == null) {
            return Result.error("艺人不存在");
        }
        Long userId = getCurrentUserId();
        QueryWrapper<UserArtistLike> query = new QueryWrapper<UserArtistLike>()
                .eq("user_id", userId)
                .eq("artist_id", artistId);
        UserArtistLike existing = userArtistLikeMapper.selectOne(query);
        if (existing != null) {
            userArtistLikeMapper.delete(query);
            return Result.success("已取消收藏艺人");
        }
        UserArtistLike like = new UserArtistLike();
        like.setUserId(userId);
        like.setArtistId(artistId);
        userArtistLikeMapper.insert(like);
        return Result.success("已收藏艺人");
    }

    @GetMapping("/artist/check")
    public Result<Boolean> checkArtist(@RequestParam Long artistId) {
        Long userId = getCurrentUserId();
        Long count = userArtistLikeMapper.selectCount(new QueryWrapper<UserArtistLike>()
                .eq("user_id", userId)
                .eq("artist_id", artistId));
        return Result.success(count > 0);
    }

    @GetMapping("/artist/list")
    public Result<List<Artist>> listArtists() {
        Long userId = getCurrentUserId();
        List<UserArtistLike> likes = userArtistLikeMapper.selectList(new QueryWrapper<UserArtistLike>()
                .eq("user_id", userId));
        if (likes.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Long> ids = likes.stream().map(UserArtistLike::getArtistId).collect(Collectors.toList());
        return Result.success(artistMapper.selectBatchIds(ids));
    }

    @PostMapping("/album/toggle")
    public Result<?> toggleAlbum(@RequestParam Long albumId) {
        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            return Result.error("专辑不存在");
        }
        Long userId = getCurrentUserId();
        QueryWrapper<UserAlbumLike> query = new QueryWrapper<UserAlbumLike>()
                .eq("user_id", userId)
                .eq("album_id", albumId);
        UserAlbumLike existing = userAlbumLikeMapper.selectOne(query);
        if (existing != null) {
            userAlbumLikeMapper.delete(query);
            return Result.success("已取消收藏专辑");
        }
        UserAlbumLike like = new UserAlbumLike();
        like.setUserId(userId);
        like.setAlbumId(albumId);
        userAlbumLikeMapper.insert(like);
        return Result.success("已收藏专辑");
    }

    @GetMapping("/album/check")
    public Result<Boolean> checkAlbum(@RequestParam Long albumId) {
        Long userId = getCurrentUserId();
        Long count = userAlbumLikeMapper.selectCount(new QueryWrapper<UserAlbumLike>()
                .eq("user_id", userId)
                .eq("album_id", albumId));
        return Result.success(count > 0);
    }

    @GetMapping("/album/list")
    public Result<List<Album>> listAlbums() {
        Long userId = getCurrentUserId();
        List<UserAlbumLike> likes = userAlbumLikeMapper.selectList(new QueryWrapper<UserAlbumLike>()
                .eq("user_id", userId));
        if (likes.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<Long> ids = likes.stream().map(UserAlbumLike::getAlbumId).collect(Collectors.toList());
        return Result.success(albumMapper.selectBatchIds(ids));
    }
}
