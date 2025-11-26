package org.example.spotify_music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.spotify_music.entity.SongGenre;

@Mapper
public interface SongGenreMapper extends BaseMapper<SongGenre> {
}
