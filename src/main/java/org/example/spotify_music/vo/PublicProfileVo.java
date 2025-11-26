package org.example.spotify_music.vo;

import org.example.spotify_music.entity.Artist;
import org.example.spotify_music.entity.Playlist;

import java.util.List;

public class PublicProfileVo {
    private PublicUserVo user;
    private List<Playlist> playlists;
    private List<Artist> likedArtists;
    private List<PublicUserVo> following;
    private List<PublicUserVo> fans;

    public PublicUserVo getUser() {
        return user;
    }

    public void setUser(PublicUserVo user) {
        this.user = user;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }

    public List<Artist> getLikedArtists() {
        return likedArtists;
    }

    public void setLikedArtists(List<Artist> likedArtists) {
        this.likedArtists = likedArtists;
    }

    public List<PublicUserVo> getFollowing() {
        return following;
    }

    public void setFollowing(List<PublicUserVo> following) {
        this.following = following;
    }

    public List<PublicUserVo> getFans() {
        return fans;
    }

    public void setFans(List<PublicUserVo> fans) {
        this.fans = fans;
    }
}
