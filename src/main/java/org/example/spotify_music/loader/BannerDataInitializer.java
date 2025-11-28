package org.example.spotify_music.loader;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spotify_music.config.MinioProperties;
import org.example.spotify_music.entity.SysBanner;
import org.example.spotify_music.mapper.SysBannerMapper;
import org.example.spotify_music.mapper.SongMapper;
import org.example.spotify_music.vo.SongVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "music.banner.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BannerDataInitializer implements CommandLineRunner {

    private final SysBannerMapper sysBannerMapper;
    private final SongMapper songMapper;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    private volatile boolean bucketPrepared = false;
    private volatile boolean minioAvailable = true;

    @Value("${music.banner.seed.limit:4}")
    private int bannerLimit;

    @Override
    public void run(String... args) throws Exception {
        long existingCount = sysBannerMapper.selectCount(new QueryWrapper<>());
        if (existingCount >= bannerLimit) {
            log.info("首页公告已存在 {} 条，跳过自动生成", existingCount);
            return;
        }

        List<SysBanner> existingBanners = sysBannerMapper.selectList(new QueryWrapper<>());
        Set<String> existingTitles = new HashSet<>();
        existingBanners.stream()
                .map(SysBanner::getTitle)
                .filter(StringUtils::hasText)
                .forEach(existingTitles::add);

        int sortOrder = existingBanners.stream()
                .map(SysBanner::getSortOrder)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0) + 1;

        List<SongVo> candidates = songMapper.selectLatestSongVos(bannerLimit * 2);
        if (candidates == null || candidates.isEmpty()) {
            log.info("没有可用歌曲用于生成首页公告，跳过");
            return;
        }

        int seeded = 0;
        for (SongVo song : candidates) {
            if (existingCount + seeded >= bannerLimit) {
                break;
            }

            String imageUrl = resolveImageUrl(song);
            if (!StringUtils.hasText(imageUrl)) {
                continue;
            }

            String title = buildBannerTitle(song);
            if (existingTitles.contains(title)) {
                continue;
            }

            SysBanner banner = new SysBanner();
            banner.setTitle(title);
            banner.setImageUrl(imageUrl);
            banner.setTargetUrl(song.getId() != null ? "/song/" + song.getId() : null);
            banner.setSortOrder(sortOrder++);
            banner.setIsEnabled(true);

            LocalDateTime now = LocalDateTime.now();
            banner.setCreateTime(now);
            banner.setUpdateTime(now);

            sysBannerMapper.insert(banner);
            seeded++;
            existingTitles.add(title);
        }

        if (seeded > 0) {
            log.info("自动生成了 {} 条首页公告", seeded);
        } else {
            log.info("没有生成新的首页公告（可能是缺少封面或标题重复）");
        }
    }

    private String buildBannerTitle(SongVo song) {
        String artist = song.getArtistName();
        String songTitle = song.getTitle();
        if (StringUtils.hasText(artist) && StringUtils.hasText(songTitle)) {
            return String.format("%s · %s 新曲上线", artist, songTitle);
        }
        if (StringUtils.hasText(songTitle)) {
            return songTitle + " 新歌上线";
        }
        return "新曲上线";
    }

    private String resolveImageUrl(SongVo song) {
        String candidate = firstNonBlank(song.getCoverUrl(), song.getAlbumCover());
        if (!StringUtils.hasText(candidate)) {
            return null;
        }
        if (candidate.startsWith("http")) {
            return candidate;
        }

        Path localPath = Paths.get(candidate);
        if (Files.exists(localPath)) {
            return uploadToMinio(localPath);
        }
        return candidate;
    }

    private String uploadToMinio(Path filePath) {
        if (!ensureBucketAvailable()) {
            return null;
        }
        try (InputStream stream = Files.newInputStream(filePath)) {
            String objectName = filePath.getFileName().toString();
            if (!CharSequenceUtil.contains(objectName, '.')) {
                objectName = objectName + ".jpg";
            }
            String contentType = Files.probeContentType(filePath);
            if (!StringUtils.hasText(contentType)) {
                contentType = "image/jpeg";
            }

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object("banners/" + objectName)
                    .stream(stream, Files.size(filePath), -1)
                    .contentType(contentType)
                    .build());

            return String.format("%s/%s/%s", minioProperties.getEndpoint(), minioProperties.getBucket(), "banners/" + objectName);
        } catch (Exception ex) {
            log.warn("上传 Banner 图片到存储失败：{}", filePath, ex);
            return null;
        }
    }

    private boolean ensureBucketAvailable() {
        if (!minioAvailable) {
            return false;
        }
        if (bucketPrepared) {
            return true;
        }
        synchronized (this) {
            if (bucketPrepared) {
                return true;
            }
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .build());
                }
                bucketPrepared = true;
            } catch (Exception ex) {
                minioAvailable = false;
                log.warn("对象存储不可用，无法访问或创建 Banner 桶：{}", minioProperties.getBucket(), ex);
                return false;
            }
        }
        return true;
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        if (StringUtils.hasText(second)) {
            return second;
        }
        return null;
    }
}
