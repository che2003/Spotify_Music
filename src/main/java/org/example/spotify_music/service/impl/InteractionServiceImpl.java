package org.example.spotify_music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.spotify_music.entity.Interaction;
import org.example.spotify_music.mapper.InteractionMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.service.InteractionService;
import org.example.spotify_music.service.PlaylistService;
import org.example.spotify_music.vo.LikeStatusVo;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InteractionServiceImpl extends ServiceImpl<InteractionMapper, Interaction> implements InteractionService {

    @Autowired
    private InteractionMapper interactionMapper;

    @Autowired
    private SongMapper songMapper;

    @Autowired
    private PlaylistService playlistService;

    @Override
    @Transactional
    public LikeStatusVo toggleSongLike(Long songId, Long userId) {
        QueryWrapper<Interaction> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("song_id", songId).eq("type", 3);

        Interaction existing = interactionMapper.selectOne(query);
        boolean liked;
        if (existing != null) {
            interactionMapper.delete(query);
            liked = false;
            playlistService.removeSongFromLikedPlaylist(userId, songId);
        } else {
            Interaction interaction = new Interaction();
            interaction.setUserId(userId);
            interaction.setSongId(songId);
            interaction.setType(3);
            interactionMapper.insert(interaction);
            liked = true;
            playlistService.addSongToLikedPlaylist(userId, songId);
        }

        long likeCount = interactionMapper.countLikesBySong(songId);
        return new LikeStatusVo(liked, likeCount);
    }

    @Override
    public List<Long> getLikedSongIds(Long userId) {
        QueryWrapper<Interaction> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("type", 3).select("song_id");
        List<Interaction> interactions = interactionMapper.selectList(query);
        return interactions.stream()
                .map(Interaction::getSongId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<SongVo> getLikedSongs(Long userId) {
        List<Long> songIds = getLikedSongIds(userId);
        if (songIds.isEmpty()) {
            return Collections.emptyList();
        }
        return songMapper.selectSongVoListByIds(songIds);
    }

    @Override
    public LikeStatusVo getSongLikeStatus(Long songId, Long userId) {
        QueryWrapper<Interaction> query = new QueryWrapper<>();
        query.eq("user_id", userId).eq("song_id", songId).eq("type", 3);
        Interaction existing = interactionMapper.selectOne(query);
        long likeCount = interactionMapper.countLikesBySong(songId);
        return new LikeStatusVo(existing != null, likeCount);
    }
}
