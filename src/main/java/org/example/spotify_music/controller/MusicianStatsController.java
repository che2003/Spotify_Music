package org.example.spotify_music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.spotify_music.common.Result;
import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.Song;
import org.example.spotify_music.entity.User;
import org.example.spotify_music.mapper.ArtistMapper;
import org.example.spotify_music.mapper.InteractionMapper;
import org.example.spotify_music.mapper.PlayHistoryMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.mapper.UserMapper;
import org.example.spotify_music.vo.MusicianDashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/musician/stats")
public class MusicianStatsController {

    @Autowired private UserMapper userMapper;
    @Autowired private ArtistMapper artistMapper;
    @Autowired private SongMapper songMapper;
    @Autowired private PlayHistoryMapper playHistoryMapper;
    @Autowired private InteractionMapper interactionMapper;

    private Long getCurrentUserId() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        return user.getId();
    }

    @GetMapping("/dashboard")
    public Result<MusicianDashboardVo> dashboard() {
        Long userId = getCurrentUserId();
        Artist artist = artistMapper.selectOne(new QueryWrapper<Artist>().eq("user_id", userId));
        if (artist == null) {
            return Result.error("当前账号未绑定音乐人身份");
        }

        Long artistId = artist.getId();

        MusicianDashboardVo vo = new MusicianDashboardVo();
        vo.setSongCount(songMapper.selectCount(new QueryWrapper<Song>().eq("artist_id", artistId)));
        vo.setTotalPlays(Optional.ofNullable(songMapper.sumPlayCountByArtist(artistId)).orElse(0L));
        vo.setTotalLikes(Optional.ofNullable(interactionMapper.countLikesByArtist(artistId)).orElse(0L));
        vo.setUniqueListeners(Optional.ofNullable(playHistoryMapper.countUniqueListenersByArtist(artistId)).orElse(0L));
        vo.setAvgRating(Optional.ofNullable(interactionMapper.averageRatingByArtist(artistId)).orElse(0.0));

        vo.setPlayTrend(playHistoryMapper.selectPlayTrendByArtist(artistId, 7));
        vo.setTopSongs(playHistoryMapper.selectTopPlayedSongsByArtist(artistId, 30, 5));
        vo.setSongMetrics(songMapper.selectSongMetricsByArtist(artistId));

        return Result.success(vo);
    }
}
