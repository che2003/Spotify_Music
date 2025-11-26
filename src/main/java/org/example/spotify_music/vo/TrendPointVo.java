package org.example.spotify_music.vo;

public class TrendPointVo {
    private String date;
    private Long plays;
    private Long listeners;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getPlays() {
        return plays;
    }

    public void setPlays(Long plays) {
        this.plays = plays;
    }

    public Long getListeners() {
        return listeners;
    }

    public void setListeners(Long listeners) {
        this.listeners = listeners;
    }
}
