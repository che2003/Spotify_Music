package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.dto.SongUploadRequest;
import org.example.spotify_music.entity.Album;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.entity.SongGenre;
import org.example.spotify_music.mapper.AlbumMapper;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.SongGenreMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.service.SongService;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.example.spotify_music.entity.Song;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/song")
public class SongController {

    @Autowired private SongService songService;
    @Autowired private SongMapper songMapper;
    @Autowired private AlbumMapper albumMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ArtistMapper artistMapper;
    @Autowired private SongGenreMapper songGenreMapper;

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private boolean isMusician() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MUSICIAN"));
    }

    // 1. 获取所有歌曲 (公共)
    @GetMapping("/list")
    public Result<List<SongVo>> list() {
        return Result.success(songMapper.selectSongVoList());
    }

    @GetMapping("/hot")
    public Result<List<SongVo>> hot(@RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(songMapper.selectTopSongVos(limit));
    }

    @GetMapping("/new")
    public Result<List<SongVo>> latest(@RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(songMapper.selectLatestSongVos(limit));
    }

    // 2. 【新增】获取当前音乐人发布的歌曲
    @GetMapping("/my")
    public Result<List<SongVo>> myUploads() {
        Long userId = getCurrentUserId();
        // 查找该用户绑定的艺人身份
        Artist artist = artistMapper.selectOne(new QueryWrapper<Artist>().eq("user_id", userId));

        if (artist == null) {
            return Result.success(List.of()); // 不是艺人或没绑定，返回空
        }
        return Result.success(songMapper.selectSongVoByArtistId(artist.getId()));
    }

    // 3. 添加歌曲
    @PostMapping("/add")
    public Result<?> addSong(@RequestBody SongUploadRequest request) {
        if (request.getAlbumId() == null) {
            return Result.error("缺少专辑ID");
        }

        Album album = albumMapper.selectById(request.getAlbumId());
        if (album == null) {
            return Result.error("专辑不存在");
        }

        Song song = new Song();
        BeanUtils.copyProperties(request, song);

        if (song.getArtistId() == null) {
            song.setArtistId(album.getArtistId());
        } else if (!song.getArtistId().equals(album.getArtistId())) {
            return Result.error("专辑与艺人不匹配");
        }

        songService.saveSongWithGenres(song, request.getGenreIds());
        return Result.success("添加成功");
    }

    // 4. 删除歌曲 (带权限控制)
    @PostMapping("/delete")
    public Result<?> delete(@RequestParam Long id) {
        Long currentUserId = getCurrentUserId();

        // 判断是否是管理员
        if (isAdmin()) {
            songService.removeById(id);
            return Result.success("管理员删除成功");
        }

        // 判断是否是音乐人并检查归属
        if (isMusician()) {
            Song song = songService.getById(id);
            if (song == null) return Result.error("歌曲不存在");

            Artist artist = artistMapper.selectById(song.getArtistId());
            if (artist != null && currentUserId.equals(artist.getUserId())) {
                songService.removeById(id);
                return Result.success("删除成功");
            } else {
                return Result.error("无权删除他人的作品");
            }
        }

        return Result.error("权限不足");
    }

    @GetMapping("/{id}")
    public Result<SongVo> getById(@PathVariable Long id) {
        return Result.success(songMapper.selectSongVoById(id));
    }

    @GetMapping("/genre/{id}")
    public Result<List<SongVo>> byGenre(@PathVariable("id") Long genreId,
                                       @RequestParam(value = "limit", required = false) Integer limit) {
        return Result.success(songMapper.selectSongVoByGenreId(genreId, limit));
    }

    @RequestMapping(value = "/update", method = {RequestMethod.POST, RequestMethod.PUT})
    public Result<?> updateSong(@RequestBody SongUploadRequest request) {
        Long songId = request.getSongId() != null ? request.getSongId() : request.getId();
        if (songId == null) {
            return Result.error("缺少歌曲ID");
        }

        Song existing = songService.getById(songId);
        if (existing == null) {
            return Result.error("歌曲不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (!isAdmin()) {
            if (!isMusician()) {
                return Result.error("权限不足");
            }
            Artist owner = artistMapper.selectById(existing.getArtistId());
            if (owner == null || !currentUserId.equals(owner.getUserId())) {
                return Result.error("无权修改他人的作品");
            }
            request.setArtistId(existing.getArtistId());
        }

        if (request.getArtistId() != null) existing.setArtistId(request.getArtistId());
        existing.setAlbumId(request.getAlbumId());
        existing.setTitle(request.getTitle());
        existing.setFileUrl(request.getFileUrl());
        existing.setCoverUrl(request.getCoverUrl());
        existing.setDuration(request.getDuration());
        existing.setGenre(request.getGenre());
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getLyrics() != null) {
            existing.setLyrics(request.getLyrics());
        }

        songService.updateSongWithGenres(existing, request.getGenreIds());
        return Result.success("保存成功");
    }

    @GetMapping("/{id}/genres")
    public Result<List<Integer>> genreIds(@PathVariable Long id) {
        List<Integer> ids = songGenreMapper.selectList(new QueryWrapper<SongGenre>().eq("song_id", id))
                .stream().map(SongGenre::getGenreId).collect(Collectors.toList());
        return Result.success(ids);
    }
}