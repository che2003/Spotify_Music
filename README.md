# 🎵 Spotify-Music：基于 NCF 智能推荐的全栈音乐平台

> 借鉴 Spotify 风格，采用 Spring Boot 3、Vue 3 和 PyTorch (NCF) 构建的现代化音乐流媒体与个性化推荐系统。

<p align="left">
  <img src="https://img.shields.io/badge/Frontend-Vue.js_3-4FC08D?logo=vue.js" alt="Vue 3">
  <img src="https://img.shields.io/badge/Backend-Spring_Boot_3-6DB33F?logo=spring-boot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-21-red?logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/AI-Python_3.10-3776AB?logo=python" alt="Python">
  <img src="https://img.shields.io/badge/Storage-MinIO-C72C48?logo=minio" alt="MinIO">
  <img src="https://img.shields.io/badge/Database-MySQL_8-4479A1?logo=mysql" alt="MySQL">
</p>

---

## 📖 项目简介

**Spotify-Music** 是一个全栈音乐流媒体平台，核心致力于探索 AI 在音乐推荐领域的应用。项目集成了基于 **神经协同过滤 (NCF)** 的推荐算法，能够根据用户的历史行为（播放、收藏、跳过）生成“千人千面”的个性化每日歌单。

### 🔧 核心架构

- **前端**：Vue 3 + TypeScript + Vite + Element Plus（深色沉浸式 UI）
- **后端**：Spring Boot 3 + Spring Security (RBAC/JWT) + MyBatis-Plus
- **存储**：MinIO（对象存储，用于存取歌曲/封面）+ Redis（缓存）
- **算法**：Python FastAPI + PyTorch (NCF 模型)，支持 GPU 加速

---

## 🛠️ 环境准备 (Pre-requisites)

在启动项目前，请确保开发环境满足以下要求：

### 1. 基础软件

- **Java Development Kit (JDK)**：版本 21
- **Node.js**：版本 18+（推荐 LTS）
- **Python**：版本 3.10（建议使用 Anaconda 管理环境）
- **MySQL**：版本 8.0+
- **Redis**：版本 6.0+
- **Docker**：用于快速部署 MinIO 和 Redis

### 2. 硬件要求

- **GPU（可选）**：若需加速 AI 模型训练，建议配置支持 CUDA 12.8 的 NVIDIA 显卡。  
  无 GPU 环境将自动回退至 CPU 模式。

---

## ☁️ 中间件与云平台配置 (Infrastructure Setup)

本项目依赖 **MySQL**、**Redis** 和 **MinIO**。建议使用 Docker 快速搭建。

### 1. 启动 Redis

```bash
docker run --name spotify-redis -p 6379:6379 -d redis
```

### 2. 启动 MinIO 并初始化权限（命令行版）

项目使用 MinIO 存储音乐文件和图片，必须创建名为 `spotifymusic` 的存储桶并设置为 **Public（公开）** 权限。

#### 2.1 启动 MinIO 服务

请务必使用以下命令启动，以匹配后端的默认配置：

```bash
docker run -p 9000:9000 -p 9090:9090   --name minio   -d --restart=always   -e "MINIO_ROOT_USER=admin"   -e "MINIO_ROOT_PASSWORD=password123"   minio/minio server /data --console-address ":9090"
```

#### 2.2 一键初始化 Bucket 与权限（使用 mc CLI）

在 MinIO 启动后，运行以下 Docker 命令。  
该命令会临时启动一个 MinIO 客户端 `mc`，连接到你的 MinIO 服务，创建 `spotifymusic` 桶并将权限策略修改为 Public。

适用于 **Windows PowerShell / CMD / Linux / macOS 终端**：

```bash
docker run --rm --link minio:minio minio/mc sh -c "   mc alias set myminio http://minio:9000 admin password123;   mc mb myminio/spotifymusic;   mc anonymous set public myminio/spotifymusic;   echo '✅ MinIO Bucket configured successfully!'"
```

**命令说明：**

- `mc alias set`：设置连接别名，账号 `admin`，密码 `password123`
- `mc mb`：创建 Bucket（Make Bucket）
- `mc anonymous set public`：关键步骤，将 `spotifymusic` 桶的匿名访问权限设置为 `public`，确保前端能直接加载图片和音频

---

## 🚀 启动流程 (Step-by-Step Guide)

> 请尽量按照以下顺序操作，否则可能导致应用报错。

### 第一步：数据库初始化

1. 使用 MySQL 创建数据库，名称为：`spotify_music`
2. 在该数据库中执行项目根目录下的 SQL 脹本文件：
    - 先执行建表脚本：`sql/create_music.sql`
    - 再执行数据插入脚本：`sql/insect.sql`

---

### 第二步：配置并训练 AI 模型（NCF）

后端项目包含 **自动启动 Python 服务** 的逻辑，但首次运行前必须：

1. 手动配置 Python 环境
2. 训练模型（生成权重文件）

#### 2.1 创建 Python 环境

```bash
conda create -n Spotify_NCF python=3.10
conda activate Spotify_NCF
```

#### 2.2 安装依赖

进入 `Spotify_NCF` 目录：

```bash
cd Spotify_NCF
pip install pandas sqlalchemy pymysql scikit-learn fastapi uvicorn
# 安装 PyTorch（下面为 CPU 版本示例，可根据实际 CUDA 版本调整）
pip install torch torchvision torchaudio
```

#### 2.3 训练模型（生成权重文件）

> 必须执行此步骤，否则后端无法加载模型进行推荐。

```bash
python train.py
```

执行成功后，目录下会生成 `ncf_model.pth` 文件。

---

### 第三步：配置后端（Java / Spring Boot）

#### 3.1 修改数据库 & MinIO 配置

打开 `src/main/resources/application.yml`，确认 MySQL、Redis 和 MinIO 的配置与本地环境一致：

```yaml
spring:
  datasource:
    username: root      # 修改为你的数据库账号
    password: 123456    # 修改为你的数据库密码

minio:
  endpoint: http://localhost:9000
  access-key: admin
  secret-key: password123
  bucket: spotifymusic
```

#### 3.2 修改 Python 路径（关键步骤）

打开：

`src/main/java/org/example/spotify_music/config/PythonRunner.java`

由于项目包含自动启动 Python 脚本的功能，需要将以下两个变量修改为你本地的真实路径：

```java
// 修改为你的 conda 环境 python.exe 路径
private static final String PYTHON_EXEC_PATH = "D:\Anaconda\envs\Spotify_NCF\python.exe";

// 修改为项目中的 Spotify_NCF 文件夹绝对路径
private static final String SCRIPT_DIR = "D:\YourProject\Spotify_NCF";
```

设置完成后，运行 `SpotifyMusicApplication.java` 启动后端服务。  
观察控制台日志，确认 `[Python-AI]` 服务已成功拉起且无报错。

---

### 第四步：启动前端（Vue 3）

进入前端目录：

```bash
cd spotify-frontend
```

安装依赖：

```bash
npm install
```

启动开发服务器：

```bash
npm run dev
```

访问地址：

> http://localhost:5173

---

## ❓ 常见问题 (FAQ)

### Q1：后端启动时报错 `Cannot run program ... python.exe`？

**A：**

- 检查 `PythonRunner.java` 中的 `PYTHON_EXEC_PATH` 是否正确指向你的 Python 解释器路径；
- 确保路径中 **不包含中文或空格、特殊字符**（如“学习”、“桌面”等）。

---

### Q2：图片或音乐无法加载？

**A：**

- 确认 MinIO 服务是否已启动；
- 确认是否已经执行 **“2.2 一键初始化 Bucket 与权限”** 中的命令，将 `spotifymusic` 桶设置为 `public`；
- 若权限不是 `Public`，浏览器会因为 `403 Forbidden` 无法加载资源文件。

---

### Q3：推荐页面显示为空？

**A：**

- 检查是否已执行 `python train.py` 生成 `ncf_model.pth`；
- 若是新用户，可能因为缺乏交互数据（冷启动问题），建议：
    - 多播放几首歌；
    - 点赞 / 收藏一些歌曲；  
      然后刷新推荐页面再试。

---

## 📂 目录结构说明

```text
├── spotify-frontend/                         # 前端 Vue 3 项目
├── src/                                      # 后端 Spring Boot 源码
│   ├── main/java/.../config/PythonRunner.java  # Python 进程管理
│   └── main/resources/application.yml           # 全局配置文件
├── Spotify_NCF/                              # Python 推荐算法工程
│   ├── main_service.py                       # FastAPI 服务入口
│   ├── train.py                              # 模型训练脚本
│   └── ncf_model.pth                         # 训练好的模型权重（训练后生成）
└── sql/                                      # 数据库建表 & 初始化脚本
    ├── create_music.sql
    └── insect.sql
```
