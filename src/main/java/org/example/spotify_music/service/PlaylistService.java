package org.example.spotify_music.service;

import org.example.spotify_music.entity.Playlist;

public interface PlaylistService {

    /**
     * Ensure the current user has a default "我喜欢" playlist, create one if missing.
     */
    Playlist ensureLikedPlaylist(Long userId);

    /**
     * Add a song to the default "我喜欢" playlist.
     */
    void addSongToLikedPlaylist(Long userId, Long songId);

    /**
     * Remove a song from the default "我喜欢" playlist.
     */
    void removeSongFromLikedPlaylist(Long userId, Long songId);
}
