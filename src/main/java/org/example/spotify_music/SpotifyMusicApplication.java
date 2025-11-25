package org.example.spotify_music;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@MapperScan("org.example.spotify_music.mapper") // 扫描所有 Mapper 接口
public class SpotifyMusicApplication implements WebMvcConfigurer { // 实现 WebMvcConfigurer 接口

    public static void main(String[] args) {
        SpringApplication.run(SpotifyMusicApplication.class, args);
    }

    /**
     * 配置静态资源路径映射，用于访问用户上传的文件。
     * 当访问 http://localhost:8080/static/filename 时，会去项目根目录下的 uploads 文件夹查找文件。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前项目的根目录 (运行时目录)
        String rootPath = System.getProperty("user.dir");

        registry.addResourceHandler("/static/**") // 浏览器访问的路径，例如 /static/avatar.jpg
                // 映射到文件系统的绝对路径，确保文件协议 file:
                .addResourceLocations("file:" + rootPath + "/uploads/");
    }
}