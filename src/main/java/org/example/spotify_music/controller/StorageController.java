package org.example.spotify_music.controller;

import org.example.spotify_music.common.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/storage")
public class StorageController {

    // 文件将被存储在项目根目录下的 uploads 文件夹中
    private static final String UPLOAD_PATH = "uploads";

    /**
     * 文件上传接口，支持图片或音频文件
     * @param file 前端上传的文件对象
     * @return 存储后的文件访问URL
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        try {
            // 1. 确定文件存储的绝对路径 (项目根目录 + uploads)
            String rootPath = System.getProperty("user.dir");
            File uploadDir = new File(rootPath, UPLOAD_PATH);

            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // 如果 uploads 目录不存在，则创建
            }

            // 2. 构造新文件名 (UUID + 原始文件后缀名，防止重名)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 3. 将文件写入磁盘
            File dest = new File(uploadDir, newFilename);
            file.transferTo(dest);

            // 4. 返回可访问的 URL (例如: http://localhost:8080/static/abcde123.mp3)
            String baseUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/static/")
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