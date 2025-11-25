package org.example.spotify_music.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.spotify_music.entity.Song;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.service.SongService;
import org.springframework.stereotype.Service;

@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {
}