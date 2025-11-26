package org.example.spotify_music.vo;

import java.util.List;

public class MusicianDashboardVo {
    private Long totalPlays;
    private Long totalLikes;
    private Long uniqueListeners;
    private Long songCount;
    private Double avgRating;
    private List<TrendPointVo> playTrend;
    private List<SongMetricVo> topSongs;
    private List<SongMetricVo> songMetrics;

    public Long getTotalPlays() {
        return totalPlays;
    }

    public void setTotalPlays(Long totalPlays) {
        this.totalPlays = totalPlays;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Long getUniqueListeners() {
        return uniqueListeners;
    }

    public void setUniqueListeners(Long uniqueListeners) {
        this.uniqueListeners = uniqueListeners;
    }

    public Long getSongCount() {
        return songCount;
    }

    public void setSongCount(Long songCount) {
        this.songCount = songCount;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public List<TrendPointVo> getPlayTrend() {
        return playTrend;
    }

    public void setPlayTrend(List<TrendPointVo> playTrend) {
        this.playTrend = playTrend;
    }

    public List<SongMetricVo> getTopSongs() {
        return topSongs;
    }

    public void setTopSongs(List<SongMetricVo> topSongs) {
        this.topSongs = topSongs;
    }

    public List<SongMetricVo> getSongMetrics() {
        return songMetrics;
    }

    public void setSongMetrics(List<SongMetricVo> songMetrics) {
        this.songMetrics = songMetrics;
    }
}
