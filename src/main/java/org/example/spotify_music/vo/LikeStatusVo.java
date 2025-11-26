package org.example.spotify_music.vo;

public class LikeStatusVo {
    private boolean liked;
    private long likeCount;

    public LikeStatusVo() {
    }

    public LikeStatusVo(boolean liked, long likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }
}
