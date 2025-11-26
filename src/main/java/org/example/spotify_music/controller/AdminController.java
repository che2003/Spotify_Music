package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Album;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.Genre;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.entity.UserRole;
import org.example.spotify_music.mapper.AlbumMapper;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.GenreMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize; // 需要开启鉴权
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private UserMapper userMapper;
    @Autowired private UserRoleMapper userRoleMapper;
    @Autowired private AlbumMapper albumMapper;
    @Autowired private ArtistMapper artistMapper;
    @Autowired private GenreMapper genreMapper;

    // 1. 获取所有用户列表 (仅管理员可用)
    // 简单起见，这里不分页，直接返回所有
    @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')") // 如果你配置了严格鉴权可解开
    public Result<List<User>> listUsers() {
        List<User> users = userMapper.selectList(null);
        // 为了安全，清空密码字段
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    // 2. 修改用户角色
    @PostMapping("/user/role")
    @Transactional
    public Result<?> updateUserRole(@RequestBody Map<String, Object> params) {
        Integer userIdInt = (Integer) params.get("userId");
        Long userId = Long.valueOf(userIdInt);
        String roleType = (String) params.get("role"); // "admin", "musician", "user"

        if (userId == null || roleType == null) return Result.error("参数错误");

        // 1. 先删除旧角色
        userRoleMapper.delete(new QueryWrapper<UserRole>().eq("user_id", userId));

        // 2. 确定新角色 ID (1=admin, 2=user, 3=musician)
        Long newRoleId = 2L; // 默认 user
        if ("admin".equals(roleType)) newRoleId = 1L;
        else if ("musician".equals(roleType)) newRoleId = 3L;

        // 3. 插入新角色
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(newRoleId);
        userRoleMapper.insert(ur);

        return Result.success("角色修改成功");
    }

    // ---------------------------
    // 专辑 CRUD
    // ---------------------------
    @GetMapping("/album/crud")
    public Result<List<Album>> listAlbums() {
        return Result.success(albumMapper.selectList(new QueryWrapper<Album>().orderByDesc("create_time")));
    }

    @GetMapping("/album/crud/{id}")
    public Result<Album> getAlbum(@PathVariable Long id) {
        Album album = albumMapper.selectById(id);
        if (album == null) {
            return Result.error("专辑不存在");
        }
        return Result.success(album);
    }

    @PostMapping("/album/crud")
    public Result<?> createAlbum(@RequestBody Album album) {
        if (album == null || album.getArtistId() == null || album.getTitle() == null) {
            return Result.error("缺少必填字段");
        }
        album.setId(null);
        albumMapper.insert(album);
        return Result.success("专辑创建成功");
    }

    @PutMapping("/album/crud/{id}")
    public Result<?> updateAlbum(@PathVariable Long id, @RequestBody Album payload) {
        Album album = albumMapper.selectById(id);
        if (album == null) {
            return Result.error("专辑不存在");
        }
        if (payload.getArtistId() != null) album.setArtistId(payload.getArtistId());
        if (payload.getTitle() != null) album.setTitle(payload.getTitle());
        if (payload.getCoverUrl() != null) album.setCoverUrl(payload.getCoverUrl());
        if (payload.getDescription() != null) album.setDescription(payload.getDescription());
        if (payload.getReleaseDate() != null) album.setReleaseDate(payload.getReleaseDate());
        albumMapper.updateById(album);
        return Result.success("专辑更新成功");
    }

    @DeleteMapping("/album/crud/{id}")
    public Result<?> deleteAlbum(@PathVariable Long id) {
        albumMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ---------------------------
    // 艺人 CRUD
    // ---------------------------
    @GetMapping("/artist/crud")
    public Result<List<Artist>> listArtists() {
        return Result.success(artistMapper.selectList(new QueryWrapper<Artist>().orderByDesc("create_time")));
    }

    @GetMapping("/artist/crud/{id}")
    public Result<Artist> getArtist(@PathVariable Long id) {
        Artist artist = artistMapper.selectById(id);
        if (artist == null) {
            return Result.error("艺人不存在");
        }
        return Result.success(artist);
    }

    @PostMapping("/artist/crud")
    public Result<?> createArtist(@RequestBody Artist artist) {
        if (artist == null || artist.getName() == null) {
            return Result.error("缺少必填字段");
        }
        artist.setId(null);
        artistMapper.insert(artist);
        return Result.success("艺人创建成功");
    }

    @PutMapping("/artist/crud/{id}")
    public Result<?> updateArtist(@PathVariable Long id, @RequestBody Artist payload) {
        Artist artist = artistMapper.selectById(id);
        if (artist == null) {
            return Result.error("艺人不存在");
        }
        if (payload.getName() != null) artist.setName(payload.getName());
        if (payload.getBio() != null) artist.setBio(payload.getBio());
        if (payload.getAvatarUrl() != null) artist.setAvatarUrl(payload.getAvatarUrl());
        if (payload.getUserId() != null) artist.setUserId(payload.getUserId());
        if (payload.getTotalFans() != null) artist.setTotalFans(payload.getTotalFans());
        if (payload.getTotalPlays() != null) artist.setTotalPlays(payload.getTotalPlays());
        artistMapper.updateById(artist);
        return Result.success("艺人更新成功");
    }

    @DeleteMapping("/artist/crud/{id}")
    public Result<?> deleteArtist(@PathVariable Long id) {
        artistMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ---------------------------
    // 流派 CRUD
    // ---------------------------
    @GetMapping("/genre/crud")
    public Result<List<Genre>> listGenres() {
        return Result.success(genreMapper.selectList(new QueryWrapper<Genre>().orderByAsc("id")));
    }

    @GetMapping("/genre/crud/{id}")
    public Result<Genre> getGenre(@PathVariable Long id) {
        Genre genre = genreMapper.selectById(id);
        if (genre == null) {
            return Result.error("流派不存在");
        }
        return Result.success(genre);
    }

    @PostMapping("/genre/crud")
    public Result<?> createGenre(@RequestBody Genre genre) {
        if (genre == null || genre.getName() == null) {
            return Result.error("缺少必填字段");
        }
        genre.setId(null);
        genreMapper.insert(genre);
        return Result.success("流派创建成功");
    }

    @PutMapping("/genre/crud/{id}")
    public Result<?> updateGenre(@PathVariable Long id, @RequestBody Genre payload) {
        Genre genre = genreMapper.selectById(id);
        if (genre == null) {
            return Result.error("流派不存在");
        }
        if (payload.getName() != null) {
            genre.setName(payload.getName());
        }
        genreMapper.updateById(genre);
        return Result.success("流派更新成功");
    }

    @DeleteMapping("/genre/crud/{id}")
    public Result<?> deleteGenre(@PathVariable Long id) {
        genreMapper.deleteById(id);
        return Result.success("删除成功");
    }
}