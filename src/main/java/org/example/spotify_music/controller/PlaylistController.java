package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Playlist;
import org.example.spotify_music.entity.PlaylistSong;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.PlaylistMapper;
import org.example.spotify_music.mapper.PlaylistSongMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    @Autowired private PlaylistMapper playlistMapper;
    @Autowired private PlaylistSongMapper playlistSongMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private SongMapper songMapper;

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

    // 2. 更新歌单信息 (标题/描述)
    @PostMapping("/update")
    public Result<?> update(@RequestBody Playlist playlist) {
        if (playlist.getId() == null) return Result.error("歌单ID不能为空");

        Playlist existing = playlistMapper.selectById(playlist.getId());
        if (existing == null) return Result.error("歌单不存在");
        if (!existing.getCreatorId().equals(getCurrentUserId())) return Result.error("无权修改他人歌单");

        existing.setTitle(playlist.getTitle());
        existing.setDescription(playlist.getDescription());
        existing.setIsPublic(playlist.getIsPublic());

        playlistMapper.updateById(existing);
        return Result.success("歌单更新成功");
    }

    // 3. 获取我的歌单列表
    @GetMapping("/my")
    public Result<List<Playlist>> myPlaylists() {
        return Result.success(playlistMapper.selectList(new QueryWrapper<Playlist>().eq("creator_id", getCurrentUserId())));
    }

    // 4. 收藏歌曲到歌单
    @PostMapping("/addSong")
    public Result<?> addSong(@RequestBody PlaylistSong playlistSong) {
        QueryWrapper<PlaylistSong> query = new QueryWrapper<>();
        query.eq("playlist_id", playlistSong.getPlaylistId())
                .eq("song_id", playlistSong.getSongId());

        if (playlistSongMapper.selectCount(query) > 0) {
            return Result.error("歌曲已在歌单中");
        }

        QueryWrapper<PlaylistSong> sortQuery = new QueryWrapper<>();
        sortQuery.eq("playlist_id", playlistSong.getPlaylistId())
                .orderByDesc("sort_order")
                .last("LIMIT 1");

        PlaylistSong last = playlistSongMapper.selectOne(sortQuery);
        int nextOrder = last != null && last.getSortOrder() != null ? last.getSortOrder() + 1 : 1;
        playlistSong.setSortOrder(nextOrder);

        playlistSongMapper.insert(playlistSong);
        return Result.success("收藏成功");
    }

    // 5. 删除歌单
    @DeleteMapping("/delete/{id}")
    @Transactional
    public Result<?> deletePlaylist(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        Playlist playlist = playlistMapper.selectById(id);

        if (playlist == null) return Result.error("歌单不存在");
        if (!playlist.getCreatorId().equals(currentUserId)) return Result.error("无权删除他人歌单");

        // 删除关联
        playlistSongMapper.delete(new QueryWrapper<PlaylistSong>().eq("playlist_id", id));
        // 删除本体
        playlistMapper.deleteById(id);
        return Result.success("歌单删除成功");
    }

    // 6. 从歌单移除歌曲
    @DeleteMapping("/removeSong")
    public Result<?> removeSong(@RequestParam Long playlistId, @RequestParam Long songId) {
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist == null || !playlist.getCreatorId().equals(getCurrentUserId())) {
            return Result.error("无权操作此歌单");
        }
        playlistSongMapper.delete(new QueryWrapper<PlaylistSong>().eq("playlist_id", playlistId).eq("song_id", songId));
        return Result.success("歌曲已移除");
    }

    // 7. 获取歌单详情 (带歌手名)
    @GetMapping("/{id}/songs")
    public Result<List<SongVo>> getPlaylistSongs(@PathVariable Long id) {
        QueryWrapper<PlaylistSong> wrapper = new QueryWrapper<>();
        wrapper.eq("playlist_id", id).orderByAsc("sort_order");
        List<PlaylistSong> associations = playlistSongMapper.selectList(wrapper);

        if (associations.isEmpty()) return Result.success(List.of());

        List<Long> songIds = associations.stream().map(PlaylistSong::getSongId).collect(Collectors.toList());
        List<SongVo> songs = songMapper.selectSongVoListByIds(songIds);

        Map<Long, SongVo> songMap = songs.stream().collect(Collectors.toMap(SongVo::getId, Function.identity()));
        List<SongVo> orderedSongs = associations.stream()
                .map(a -> songMap.get(a.getSongId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return Result.success(orderedSongs);
    }

    // 8. 调整歌单内歌曲排序
    @PostMapping("/{id}/reorder")
    @Transactional
    public Result<?> reorder(@PathVariable Long id, @RequestBody List<Long> songIds) {
        Playlist playlist = playlistMapper.selectById(id);
        if (playlist == null) return Result.error("歌单不存在");
        if (!playlist.getCreatorId().equals(getCurrentUserId())) return Result.error("无权调整他人歌单");
        if (songIds == null || songIds.isEmpty()) return Result.error("歌曲列表不能为空");

        List<PlaylistSong> associations = playlistSongMapper.selectList(new QueryWrapper<PlaylistSong>().eq("playlist_id", id));
        Set<Long> existingSongIds = associations.stream().map(PlaylistSong::getSongId).collect(Collectors.toSet());
        Set<Long> incoming = new HashSet<>(songIds);

        if (existingSongIds.size() != incoming.size() || !existingSongIds.equals(incoming)) {
            return Result.error("排序列表与歌单歌曲不一致");
        }

        for (int i = 0; i < songIds.size(); i++) {
            PlaylistSong update = new PlaylistSong();
            update.setSortOrder(i + 1);

            QueryWrapper<PlaylistSong> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("playlist_id", id).eq("song_id", songIds.get(i));
            playlistSongMapper.update(update, updateWrapper);
        }

        return Result.success("排序已更新");
    }
}