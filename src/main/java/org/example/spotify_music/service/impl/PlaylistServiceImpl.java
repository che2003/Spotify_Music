package org.example.spotify_music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.math.BigDecimal;
import org.example.spotify_music.entity.Playlist;
import org.example.spotify_music.entity.PlaylistSong;
import org.example.spotify_music.mapper.PlaylistMapper;
import org.example.spotify_music.mapper.PlaylistSongMapper;
import org.example.spotify_music.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaylistServiceImpl implements PlaylistService {

    private static final String DEFAULT_LIKED_TITLE = "我喜欢";

    @Autowired private PlaylistMapper playlistMapper;
    @Autowired private PlaylistSongMapper playlistSongMapper;

    @Override
    public Playlist ensureLikedPlaylist(Long userId) {
        Playlist existing = playlistMapper.selectOne(new QueryWrapper<Playlist>()
                .eq("creator_id", userId)
                .eq("title", DEFAULT_LIKED_TITLE));
        if (existing != null) {
            return existing;
        }

        Playlist playlist = new Playlist();
        playlist.setCreatorId(userId);
        playlist.setTitle(DEFAULT_LIKED_TITLE);
        playlist.setDescription("我喜欢的歌曲");
        playlist.setVisibility("private");
        playlistMapper.insert(playlist);
        return playlist;
    }

    @Override
    @Transactional
    public void addSongToLikedPlaylist(Long userId, Long songId) {
        Playlist playlist = ensureLikedPlaylist(userId);
        if (songId == null) return;

        QueryWrapper<PlaylistSong> existsWrapper = new QueryWrapper<PlaylistSong>()
                .eq("playlist_id", playlist.getId())
                .eq("song_id", songId);
        if (playlistSongMapper.selectCount(existsWrapper) > 0) {
            return;
        }

        QueryWrapper<PlaylistSong> sortQuery = new QueryWrapper<>();
        sortQuery.eq("playlist_id", playlist.getId())
                .orderByDesc("position")
                .last("LIMIT 1");
        PlaylistSong last = playlistSongMapper.selectOne(sortQuery);
        BigDecimal nextOrder = (last != null && last.getSortOrder() != null)
                ? last.getSortOrder().add(BigDecimal.ONE)
                : BigDecimal.ONE;

        PlaylistSong link = new PlaylistSong();
        link.setPlaylistId(playlist.getId());
        link.setSongId(songId);
        link.setSortOrder(nextOrder);
        playlistSongMapper.insert(link);
    }

    @Override
    public void removeSongFromLikedPlaylist(Long userId, Long songId) {
        Playlist playlist = playlistMapper.selectOne(new QueryWrapper<Playlist>()
                .eq("creator_id", userId)
                .eq("title", DEFAULT_LIKED_TITLE));
        if (playlist == null || songId == null) return;

        playlistSongMapper.delete(new QueryWrapper<PlaylistSong>()
                .eq("playlist_id", playlist.getId())
                .eq("song_id", songId));
    }
}
