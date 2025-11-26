package org.example.spotify_music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.spotify_music.entity.PlayHistory;
import org.example.spotify_music.vo.PlayHistoryVo;

import java.util.List;

@Mapper
public interface PlayHistoryMapper extends BaseMapper<PlayHistory> {

    List<PlayHistoryVo> selectRecentByUser(@Param("userId") Long userId, @Param("limit") Integer limit);
}
