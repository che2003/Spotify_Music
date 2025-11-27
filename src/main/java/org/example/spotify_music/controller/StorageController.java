package org.example.spotify_music.controller;

import org.example.spotify_music.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Value("${storage.local-path}")
    private String storageLocalPath;

    /**
     * MP3 文件上传接口
     * @param file 前端上传的文件对象
     * @return 存储后的文件访问URL
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 1. 确定文件存储的绝对路径，并确保目录存在
            Path storageDir = Paths.get(storageLocalPath);
            Files.createDirectories(storageDir);

            // 2. 构造新文件名 (UUID + 原始文件后缀名，防止重名)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            if (!".mp3".equalsIgnoreCase(extension)) {
                return Result.error("仅支持上传 MP3 音频文件");
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 3. 将文件写入磁盘
            File dest = storageDir.resolve(newFilename).toFile();
            file.transferTo(dest);

            // 4. 返回可访问的 URL (例如: http://localhost:8080/files/abcde123.mp3)
            String baseUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/files/")
                    .build()
                    .toUriString();
            String fileUrl = baseUrl + newFilename;
            return Result.success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件写入磁盘失败: " + e.getMessage());
        }
    }
}