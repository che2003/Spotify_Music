package org.example.spotify_music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.spotify_music.entity.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}