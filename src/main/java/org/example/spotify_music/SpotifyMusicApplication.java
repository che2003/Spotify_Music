package org.example.spotify_music;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.spotify_music.mapper") // 扫描所有 Mapper 接口
public class SpotifyMusicApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotifyMusicApplication.class, args);
    }

}