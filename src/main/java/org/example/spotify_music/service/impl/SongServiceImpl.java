package org.example.spotify_music.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.spotify_music.entity.Song;
import org.example.spotify_music.entity.SongGenre;
import org.example.spotify_music.mapper.SongGenreMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    @Autowired
    private SongGenreMapper songGenreMapper;

    @Override
    @Transactional
    public void saveSongWithGenres(Song song, List<Long> genreIds) {
        this.save(song);

        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }

        List<SongGenre> mappings = genreIds.stream().map(genreId -> {
            SongGenre mapping = new SongGenre();
            mapping.setSongId(song.getId());
            mapping.setGenreId(genreId.intValue());
            return mapping;
        }).collect(Collectors.toList());

        mappings.forEach(songGenreMapper::insert);
    }
}