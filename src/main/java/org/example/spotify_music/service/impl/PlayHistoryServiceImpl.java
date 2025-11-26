package org.example.spotify_music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.spotify_music.entity.PlayHistory;
import org.example.spotify_music.mapper.PlayHistoryMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.service.PlayHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PlayHistoryServiceImpl extends ServiceImpl<PlayHistoryMapper, PlayHistory> implements PlayHistoryService {

    @Autowired
    private SongMapper songMapper;

    @Override
    @Transactional
    public void recordPlay(Long userId, Long songId) {
        if (userId == null || songId == null) {
            return;
        }

        this.remove(new QueryWrapper<PlayHistory>()
                .eq("user_id", userId)
                .eq("song_id", songId));

        PlayHistory history = new PlayHistory();
        history.setUserId(userId);
        history.setSongId(songId);
        history.setPlayTime(LocalDateTime.now());
        this.save(history);

        songMapper.incrementPlayCount(songId);
    }
}
