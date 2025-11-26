# 🎵 Spotify-Music: 基于神经协同过滤的智能音乐推荐系统

> A Full-Stack Music Streaming & Recommendation Platform based on Spring Boot, Vue 3, and PyTorch (NCF).

![Vue](https://img.shields.io/badge/Frontend-Vue.js_3-4FC08D?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Backend-Spring_Boot_3-6DB33F?logo=spring-boot)
![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)
![Python](https://img.shields.io/badge/AI-Python_3.10-3776AB?logo=python)
![PyTorch](https://img.shields.io/badge/PyTorch-CUDA_12.8-EE4C2C?logo=pytorch)
![MySQL](https://img.shields.io/badge/Database-MySQL_8-4479A1?logo=mysql)

## 📖 项目简介

这是一个模仿 Spotify 风格的全栈音乐流媒体平台。除了基础的音乐播放、歌单管理和搜索功能外，项目核心集成了一个基于 **神经协同过滤 (NCF)** 的 AI 推荐引擎。系统能够根据用户的历史行为（播放、喜欢、跳过），实现“千人千面”的个性化每日歌曲推荐。

项目采用 **前后端分离** + **微服务化算法** 的架构设计，包含三个主要部分：
1.  **前端**：Vue 3 + Element Plus (Spotify 风格深色 UI)。
2.  **后端**：Spring Boot 3 + MyBatis-Plus + Spring Security (RBAC 权限控制)。
3.  **算法服务**：Python + FastAPI + PyTorch (提供实时推荐 API，支持 GPU 加速)。

---

## ✨ 核心功能

### 👤 用户端
* **智能推荐**：每日生成的个性化歌单 (Based on NCF Model)，具备冷启动兜底策略。
* **音乐播放器**：支持播放/暂停、上一首/下一首、循环/随机模式、进度条拖拽、音量控制。
* **歌单管理**：创建、编辑、删除歌单，收藏歌曲到歌单。
* **社交互动**：点赞/取消点赞歌曲，在歌曲详情页发布评论。
* **搜索**：支持模糊搜索歌曲、艺人。
* **个人中心**：修改头像、昵称、密码。

### 🎹 音乐人端 (Musician)
* **作品发布**：上传 MP3 音频文件和封面图片（存储于服务器本地）。
* **作品管理**：管理自己发布的歌曲（删除权限）。

### 🛡️ 管理员端 (Admin)
* **用户管理**：查看所有注册用户，修改用户角色（提权/降权）。
* **内容监管**：拥有删除全站任意歌曲、歌单的最高权限。

---

## 🛠️ 技术栈

### 前端 (Frontend)
* **框架**: Vue 3 (Composition API) + TypeScript
* **构建工具**: Vite
* **UI 库**: Element Plus (深度定制 Spotify 深色主题)
* **状态管理**: Pinia (管理播放器状态、用户 Session)
* **路由**: Vue Router 4
* **网络**: Axios

### 后端 (Backend)
* **语言**: Java 21
* **框架**: Spring Boot 3.3.5
* **ORM**: MyBatis-Plus 3.5.7
* **安全**: Spring Security + JWT (无状态认证)
* **工具**: Hutool, Lombok
* **数据库**: MySQL 8.0

### 算法服务 (AI Service)
* **语言**: Python 3.10
* **加速**: CUDA 12.8 (支持 GPU 训练与推理)
* **框架**: PyTorch (深度学习模型)
* **API**: FastAPI + Uvicorn
* **算法**: Neural Collaborative Filtering (NCF) = GMF + MLP

---

## 🚀 快速启动指南

### 1. 环境准备
请确保本地已安装以下环境：
* **JDK 21**
* **Node.js 18+**
* **MySQL 8.0+**
* **Python 3.10** (建议使用 Anaconda 管理环境)
* **CUDA 12.8** (可选，用于 PyTorch GPU 加速，无 GPU 可自动切换 CPU 模式)

### 2. 数据库初始化
1.  创建数据库 `spotify_music`。
2.  执行项目根目录下的 SQL 脚本（或使用提供的海量数据脚本）建表并导入测试数据。
3.  修改 `src/main/resources/application.yml` 中的数据库密码。

### 3. 启动 Python 算法服务
> ⚠️ 必须先运行 `train.py` 生成模型文件，API 才能正常工作。

```bash
# 建议在 Anaconda 环境下执行
conda create -n Spotify_NCF python=3.10
conda activate Spotify_NCF

cd Spotify_NCF

# 1. 安装依赖
# 注意：请根据你的 CUDA 版本安装对应的 PyTorch，以下为通用安装
pip install pandas sqlalchemy pymysql scikit-learn fastapi uvicorn
# 安装 PyTorch (带 CUDA 12.x 支持)
pip install torch torchvision torchaudio --index-url [https://download.pytorch.org/whl/cu121](https://download.pytorch.org/whl/cu121)

# 2. 训练模型 (生成 ncf_model.pth 和 mappings.json)
python train.py

# 3. 启动推荐 API 服务 (端口 5000)
# 或者由 Java 后端自动唤起 (需配置 PythonRunner)
python -m uvicorn main_service:app --reload --port 5000