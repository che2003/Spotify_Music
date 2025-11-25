package org.example.spotify_music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.spotify_music.entity.Song;
import org.example.spotify_music.vo.SongVo; // 引入 SongVo

import java.util.List;

@Mapper
public interface SongMapper extends BaseMapper<Song> {

    // 1. 获取所有歌曲的联表查询方法
    List<SongVo> selectSongVoList();

    // 2. 搜索歌曲的联表查询方法
    List<SongVo> selectSongVoByKeyword(@Param("keyword") String keyword);

    // 3. 【解决报错的关键】根据 ID 列表查询 SongVo (供 PlaylistController 使用)
    // 注意：@Param("ids") 必须有，用于在 XML 中循环
    List<SongVo> selectSongVoListByIds(@Param("ids") List<Long> ids);
}