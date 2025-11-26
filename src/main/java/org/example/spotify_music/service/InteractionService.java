package org.example.spotify_music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.spotify_music.entity.Interaction;
import org.example.spotify_music.vo.LikeStatusVo;
import org.example.spotify_music.vo.SongVo;

import java.util.List;

public interface InteractionService extends IService<Interaction> {

    LikeStatusVo toggleSongLike(Long songId, Long userId);

    List<Long> getLikedSongIds(Long userId);

    List<SongVo> getLikedSongs(Long userId);

    LikeStatusVo getSongLikeStatus(Long songId, Long userId);
}
