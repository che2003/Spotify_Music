package org.example.spotify_music.loader;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spotify_music.entity.Album;
import org.example.spotify_music.mapper.AlbumMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "music.cover", name = "backfill-enabled", havingValue = "true", matchIfMissing = true)
public class AlbumCoverInitializer implements CommandLineRunner {

    private final AlbumMapper albumMapper;
    private final SongMapper songMapper;

    @Override
    public void run(String... args) {
        List<Album> albums = albumMapper.selectList(new QueryWrapper<Album>()
                .nested(wrapper -> wrapper.isNull("cover_url").or().eq("cover_url", "")));

        if (albums.isEmpty()) {
            log.info("专辑封面补全：没有需要补充封面的专辑");
            return;
        }

        int updated = 0;
        for (Album album : albums) {
            String cover = songMapper.selectFirstCoverByAlbum(album.getId());
            if (StringUtils.hasText(cover)) {
                album.setCoverUrl(cover);
                albumMapper.updateById(album);
                updated++;
            }
        }

        log.info("专辑封面补全：本次共更新 {} 条专辑封面", updated);
    }
}
