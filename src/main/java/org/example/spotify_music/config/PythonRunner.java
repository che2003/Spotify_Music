package org.example.spotify_music.config;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Component
public class PythonRunner implements CommandLineRunner {

    private Process pythonProcess;

    // 1. Python 解释器路径 (Anaconda 环境路径)
    // 请确认这个路径是你电脑上真实的 python.exe 路径
    private static final String PYTHON_EXEC_PATH = "D:\\Anaconda\\envs\\Spotify_NCF\\python.exe";

    // 2. 【关键修改】Python 脚本所在目录
    // 根据你刚才发的文件列表，代码在 Spotify_NCF 目录下，而不是 python_scripts
    private static final String SCRIPT_DIR = "D:\\学习\\Spotify_Music\\Spotify_NCF";

    @Override
    public void run(String... args) throws Exception {
        startPythonService();
    }

    private void startPythonService() {
        System.out.println(">>> 正在启动 Python NCF 推荐服务...");

        try {
            // 构建启动命令: python -m uvicorn main_service:app --host 127.0.0.1 --port 5000
            ProcessBuilder builder = new ProcessBuilder(
                    PYTHON_EXEC_PATH,
                    "-m", "uvicorn",
                    "main_service:app",
                    "--host", "127.0.0.1",
                    "--port", "5000"
            );

            // 设置工作目录 (确保能找到 main_service.py 和 ncf_model.pth)
            builder.directory(new File(SCRIPT_DIR));

            // 合并错误流，方便在 Java 控制台看到 Python 的报错
            builder.redirectErrorStream(true);

            // 启动进程
            pythonProcess = builder.start();

            // 开启新线程读取 Python 的输出日志 (防止缓冲区满导致死锁)
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 给 Python 日志加个前缀
                        System.out.println("[Python-AI]: " + line);
                    }
                } catch (IOException e) {
                    // 进程关闭时可能会报流关闭错误，忽略
                }
            }).start();

            System.out.println(">>> Python 服务启动命令已发送！");

        } catch (IOException e) {
            System.err.println("❌ Python 服务启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 当 Spring Boot 关闭时，自动杀死 Python 进程
    @PreDestroy
    public void stopPythonService() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            System.out.println(">>> 正在关闭 Python 服务...");
            pythonProcess.destroy(); // 尝试优雅关闭
            try {
                // 等待 2 秒
                if (!pythonProcess.waitFor(2, TimeUnit.SECONDS)) {
                    pythonProcess.destroyForcibly(); // 强制杀死
                }
            } catch (InterruptedException e) {
                pythonProcess.destroyForcibly();
            }
            System.out.println(">>> Python 服务已关闭。");
        }
    }
}