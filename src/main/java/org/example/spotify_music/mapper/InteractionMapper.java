package org.example.spotify_music.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.spotify_music.entity.Interaction;

@Mapper
public interface InteractionMapper extends BaseMapper<Interaction> {

    Long countLikesByArtist(@Param("artistId") Long artistId);

    Long countLikesBySong(@Param("songId") Long songId);

    Double averageRatingByArtist(@Param("artistId") Long artistId);
}