package org.example.spotify_music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.spotify_music.entity.Song;
import org.example.spotify_music.vo.SongVo;

import java.util.List;

@Mapper
public interface SongMapper extends BaseMapper<Song> {

    List<SongVo> selectSongVoList();

    List<SongVo> selectSongVoByKeyword(@Param("keyword") String keyword);

    List<SongVo> selectSongVoListByIds(@Param("ids") List<Long> ids);

    List<SongVo> selectSongVoByArtistId(@Param("artistId") Long artistId);

    // 【新增】播放量 +1
    void incrementPlayCount(@Param("id") Long id);
}