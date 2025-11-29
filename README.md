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

**Spotify-Music** 是一个全栈音乐流媒体平台，核心致力于探索 AI 在音乐推荐领域的应用。项目集成了基于 **神经协同过滤 (NCF)** 的推荐算法，能够根据用户的历史行为生成个性化每日歌单。

### 🔧 核心架构

- **前端**：Vue 3 + TypeScript + Vite + Element Plus
- **后端**：Spring Boot 3 + Spring Security (JWT) + MyBatis-Plus
- **存储**：MinIO（对象存储）+ Redis（缓存）
- **算法**：FastAPI + PyTorch (NCF 模型)

---

## 🛠️ 环境准备

### 1. 基础软件

- JDK 21
- Node.js 18+
- Python 3.10
- MySQL 8
- Redis 6+
- Docker（用于快速部署 MinIO）

### 2. GPU（可选）

若需训练推荐模型，建议使用支持 CUDA 12.8 的 NVIDIA GPU。

---

## ☁️ 中间件部署

## 1️⃣ Redis

```bash
docker run --name spotify-redis -p 6379:6379 -d redis
```

---

## 2️⃣ MinIO（核心：必须配置为 Public）

### 2.1 以 Docker 启动 MinIO

```bash
docker run -p 9000:9000 -p 9090:9090 --name minio -d --restart=always -e "MINIO_ROOT_USER=admin" -e "MINIO_ROOT_PASSWORD=password123" minio/minio server /data --console-address ":9090"
```

### 2.2 一键初始化 Bucket（推荐方式）

```bash
docker run --rm --link minio:minio minio/mc sh -c "mc alias set myminio http://minio:9000 admin password123; mc mb myminio/spotifymusic; mc anonymous set public myminio/spotifymusic; echo 'MinIO Bucket configured successfully!'
"
```

---

## 2.3 在 Windows PowerShell 中使用 mc.exe 手动设置 Bucket（可选）

### 步骤 1：下载 MinIO CLI 工具

```powershell
Invoke-WebRequest -Uri "https://dl.min.io/client/mc/release/windows-amd64/mc.exe" -OutFile "mc.exe"
```

### 步骤 2：配置本地 MinIO 连接

```powershell
.\mc.exe alias set local http://localhost:9000 admin password123
```

显示 `Added 'local' successfully.` 即为成功。

### 步骤 3：将 spotifymusic 桶设置为 Public

```powershell
.\mc.exe anonymous set public local/spotifymusic
```

看到以下即表示成功：

> Access permission for 'local/spotifymusic' is set to 'public'

---

## 🚀 启动流程

---

# 1️⃣ 初始化 MySQL 数据库

创建数据库：

```
spotify_music
```

顺序执行：

- `sql/create_music.sql`（建表）
- `sql/insect.sql`（初始化数据）

---

# 2️⃣ 配置并训练 AI 推荐模型（NCF）

## 创建 Python 环境

```bash
conda create -n Spotify_NCF python=3.10
conda activate Spotify_NCF
```

## 安装依赖

```bash
pip install pandas sqlalchemy pymysql scikit-learn fastapi uvicorn
pip install torch torchvision torchaudio
```

## 训练模型

```bash
python train.py
```

产物：`ncf_model.pth`

---

# 3️⃣ 启动后端（Spring Boot）

## 3.1 配置 application.yml

确保以下与本地一致：

```yaml
spring:
  datasource:
    username: root
    password: 123456

minio:
  endpoint: http://localhost:9000
  access-key: admin
  secret-key: password123
  bucket: spotifymusic
```

## 3.2 配置 PythonRunner 路径（重点）

文件：  
`src/main/java/org/example/spotify_music/config/PythonRunner.java`

```java
private static final String PYTHON_EXEC_PATH = "D:\Anaconda\envs\Spotify_NCF\python.exe";
private static final String SCRIPT_DIR = "D:\YourProject\Spotify_NCF";
```

> 两个路径必须修改为你本地的真实地址。

---

# 4️⃣ 启动前端（Vue 3）

```bash
cd spotify-frontend
npm install
npm run dev
```

访问：  
http://localhost:5173

---

## ❓ 常见问题 FAQ

### ❗ 图片 / 音频无法加载？

请确认：

- MinIO 是否启动
- 桶 `spotifymusic` 是否已设置为 public
- 浏览器 403 → 必定是权限没设置对

---

### ❗ 推荐列表为空？

建议：

- 多播放几首歌
- 点赞收藏几首
- 刷新页面重新请求推荐服务

---

## 📂 目录结构

```txt
├── spotify-frontend/
├── src/
│   ├── main/java/.../config/PythonRunner.java
│   └── main/resources/application.yml
├── Spotify_NCF/
│   ├── main_service.py
│   ├── train.py
│   └── ncf_model.pth
└── sql/
    ├── create_music.sql
    └── insect.sql
```
