package org.example.spotify_music.vo;

import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.Playlist;

import java.util.ArrayList;
import java.util.List;

public class SearchResultVo {

    private List<SongVo> songs = new ArrayList<>();
    private List<Artist> artists = new ArrayList<>();
    private List<AlbumSimpleVo> albums = new ArrayList<>();
    private List<Playlist> playlists = new ArrayList<>();

    public List<SongVo> getSongs() { return songs; }
    public void setSongs(List<SongVo> songs) { this.songs = songs; }

    public List<Artist> getArtists() { return artists; }
    public void setArtists(List<Artist> artists) { this.artists = artists; }

    public List<AlbumSimpleVo> getAlbums() { return albums; }
    public void setAlbums(List<AlbumSimpleVo> albums) { this.albums = albums; }

    public List<Playlist> getPlaylists() { return playlists; }
    public void setPlaylists(List<Playlist> playlists) { this.playlists = playlists; }
}
