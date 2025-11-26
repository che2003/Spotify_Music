package org.example.spotify_music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.spotify_music.entity.Song;

import java.util.List;

public interface SongService extends IService<Song> {

    void saveSongWithGenres(Song song, List<Long> genreIds);

    void updateSongWithGenres(Song song, List<Long> genreIds);
}