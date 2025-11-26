package org.example.spotify_music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.spotify_music.entity.PlayHistory;

public interface PlayHistoryService extends IService<PlayHistory> {

    void recordPlay(Long userId, Long songId);
}
