package org.example.spotify_music.loader;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spotify_music.config.MinioProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioBootstrapper {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Value("${minio.auto-start.enabled:false}")
    private boolean autoStartEnabled;

    @Value("${minio.auto-start.command:}")
    private String autoStartCommand;

    @Value("${minio.auto-start.wait-seconds:15}")
    private int waitSeconds;

    private volatile boolean bucketPrepared = false;
    private volatile boolean minioAvailable = true;
    private volatile boolean autoStartAttempted = false;

    public boolean ensureBucketAvailable(String context) {
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
                if (attemptAutoStart(context, ex)) {
                    return ensureBucketAvailable(context);
                }
                minioAvailable = false;
                log.warn("对象存储不可用，无法访问或创建桶 {} ({}): {}", minioProperties.getBucket(), context, ex.getMessage());
                return false;
            }
        }
        return true;
    }

    private boolean attemptAutoStart(String context, Exception trigger) {
        if (!autoStartEnabled || autoStartAttempted) {
            return false;
        }
        autoStartAttempted = true;

        String command = Optional.ofNullable(autoStartCommand)
                .filter(cmd -> !cmd.isBlank())
                .orElseGet(this::buildDefaultCommand);

        try {
            log.warn("检测到 MinIO 不可用，正在尝试自动启动 ({}). 触发原因：{}", command, trigger.getMessage());
            Process process = new ProcessBuilder("/bin/sh", "-c", command)
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(waitSeconds, TimeUnit.SECONDS);
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (output.isBlank()) {
                log.info("MinIO 自动启动进程已返回 ({} 秒{})", waitSeconds, finished ? "" : "，仍在后台运行");
            } else {
                log.info("MinIO 自动启动输出：{}", output.trim());
            }
            if (!finished) {
                log.info("MinIO 启动命令仍在后台运行，继续等待服务可用...");
            }
            // 给 MinIO 一点额外的启动时间
            TimeUnit.SECONDS.sleep(2);
            return true;
        } catch (Exception startEx) {
            log.warn("自动启动 MinIO 失败 ({}): {}", command, startEx.getMessage());
            return false;
        }
    }

    private String buildDefaultCommand() {
        return String.format(
                "docker start minio || docker run -p 9000:9000 -p 9090:9090 --name minio -d --restart=always -e \"MINIO_ROOT_USER=%s\" -e \"MINIO_ROOT_PASSWORD=%s\" minio/minio server /data --console-address \":9090\"",
                minioProperties.getAccessKey(), minioProperties.getSecretKey());
    }
}

