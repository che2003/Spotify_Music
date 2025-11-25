package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Playlist;
import org.example.spotify_music.entity.PlaylistSong;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.PlaylistMapper;
import org.example.spotify_music.mapper.PlaylistSongMapper;
import org.example.spotify_music.mapper.SongMapper; // 引入 SongMapper
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.vo.SongVo; // 引入 SongVo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    @Autowired private PlaylistMapper playlistMapper;
    @Autowired private PlaylistSongMapper playlistSongMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private SongMapper songMapper; // 注入 SongMapper

    // 辅助方法：获取当前登录用户的 ID
    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    // 1. 创建新歌单
    @PostMapping("/create")
    public Result<?> create(@RequestBody Playlist playlist) {
        playlist.setCreatorId(getCurrentUserId());
        playlist.setIsPublic(true);
        playlistMapper.insert(playlist);
        return Result.success(playlist);
    }

    // 2. 获取我的歌单列表
    @GetMapping("/my")
    public Result<List<Playlist>> myPlaylists() {
        Long userId = getCurrentUserId();
        QueryWrapper<Playlist> query = new QueryWrapper<>();
        query.eq("creator_id", userId);
        return Result.success(playlistMapper.selectList(query));
    }

    // 3. 收藏歌曲到歌单
    @PostMapping("/addSong")
    public Result<?> addSong(@RequestBody PlaylistSong playlistSong) {
        // 检查是否已经存在
        QueryWrapper<PlaylistSong> query = new QueryWrapper<>();
        query.eq("playlist_id", playlistSong.getPlaylistId())
                .eq("song_id", playlistSong.getSongId());

        if (playlistSongMapper.selectCount(query) > 0) {
            return Result.error("歌曲已在歌单中");
        }

        playlistSongMapper.insert(playlistSong);
        return Result.success("收藏成功");
    }

    // 4. 删除歌单及其所有关联歌曲
    @DeleteMapping("/delete/{id}")
    @Transactional
    public Result<?> deletePlaylist(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();

        Playlist playlist = playlistMapper.selectById(id);
        if (playlist == null) {
            return Result.error("歌单不存在");
        }
        if (!playlist.getCreatorId().equals(currentUserId)) {
            return Result.error("无权删除他人歌单");
        }

        QueryWrapper<PlaylistSong> wrapper = new QueryWrapper<>();
        wrapper.eq("playlist_id", id);
        playlistSongMapper.delete(wrapper);

        playlistMapper.deleteById(id);

        return Result.success("歌单删除成功");
    }

    // 5. 从歌单中移除一首歌
    @DeleteMapping("/removeSong")
    public Result<?> removeSong(@RequestParam Long playlistId, @RequestParam Long songId) {
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null || !playlist.getCreatorId().equals(getCurrentUserId())) {
            return Result.error("无权操作此歌单");
        }

        QueryWrapper<PlaylistSong> wrapper = new QueryWrapper<>();
        wrapper.eq("playlist_id", playlistId)
                .eq("song_id", songId);

        int deletedRows = playlistSongMapper.delete(wrapper);

        if (deletedRows > 0) {
            return Result.success("歌曲已移除");
        } else {
            return Result.error("歌曲不在歌单中");
        }
    }


    /**
     * 6. 获取歌单中的所有歌曲详情 (返回 SongVo，包含歌手名)
     */
    @GetMapping("/{id}/songs")
    public Result<List<SongVo>> getPlaylistSongs(@PathVariable Long id) {
        // 1. 获取歌单-歌曲关联列表 (保持排序)
        QueryWrapper<PlaylistSong> wrapper = new QueryWrapper<>();
        wrapper.eq("playlist_id", id).orderByAsc("sort_order");
        List<PlaylistSong> associations = playlistSongMapper.selectList(wrapper);

        if (associations.isEmpty()) {
            return Result.success(List.of());
        }

        // 2. 提取所有歌曲 ID
        List<Long> songIds = associations.stream()
                .map(PlaylistSong::getSongId)
                .collect(Collectors.toList());

        // 3. 【关键修改】调用新的 Mapper 方法，联表查询 SongVo 列表
        List<SongVo> songs = songMapper.selectSongVoListByIds(songIds);

        // 4. 重新排序：由于 SQL 查询的结果顺序可能与 songIds 列表不一致，需要根据 associations 重新排序
        Map<Long, SongVo> songMap = songs.stream().collect(Collectors.toMap(SongVo::getId, Function.identity()));

        List<SongVo> orderedSongs = associations.stream()
                .map(association -> songMap.get(association.getSongId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Result.success(orderedSongs);
    }
}