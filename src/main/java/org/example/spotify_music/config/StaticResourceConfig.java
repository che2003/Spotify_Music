package org.example.spotify_music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final String RESOURCE_HANDLER = "/files/**";
    private static final String RESOURCE_LOCATION = "file:D:/my-project/music-files/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(RESOURCE_HANDLER)
                .addResourceLocations(RESOURCE_LOCATION);
    }
}
