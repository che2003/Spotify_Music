<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router'
import { ref, watch } from 'vue'
import { usePlayerStore } from '@/stores/player'

const router = useRouter()
const playerStore = usePlayerStore()
const audioRef = ref<HTMLAudioElement | null>(null)
const user = JSON.parse(localStorage.getItem('user') || '{}')

// 监听播放状态
watch(() => playerStore.isPlaying, (playing) => {
  if (audioRef.value) {
    if (playing) {
      audioRef.value.play().catch(e => console.error("播放失败:", e))
    } else {
      audioRef.value.pause()
    }
  }
})

// 监听切歌
watch(() => playerStore.currentSong, (newSong) => {
  if (audioRef.value && newSong.fileUrl) {
    audioRef.value.src = newSong.fileUrl
    audioRef.value.play().catch(e => console.error("播放失败:", e))
    playerStore.isPlaying = true
  }
})

// 退出登录函数
const logout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<template>
  <div class="common-layout">
    <el-container class="h-main">
      <el-aside width="240px" class="aside">
        <div class="logo">
          <span style="color:white; font-size: 24px; font-weight: bold;">Spotify</span>
        </div>

        <el-menu
            default-active="/discover"
            class="el-menu-vertical"
            background-color="#000000"
            text-color="#b3b3b3"
            active-text-color="#fff"
            router
        >
          <el-menu-item index="/discover">
            <el-icon><i class="el-icon-house"></i></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/search">
            <el-icon><i class="el-icon-search"></i></el-icon>
            <span>搜索</span>
          </el-menu-item>
          <el-menu-item index="/library"> <el-icon><i class="el-icon-collection"></i></el-icon>
            <span>音乐库</span>
          </el-menu-item>
        </el-menu>

        <div class="logout-btn">
          <el-button type="danger" link @click="logout">退出登录</el-button>
        </div>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="user-info">
            <el-avatar :size="32" :src="user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
            <span class="username">{{ user.username }}</span>
          </div>
        </el-header>

        <el-main class="main-content">
          <RouterView />
        </el-main>
      </el-container>
    </el-container>

    <div class="player-bar">
      <audio ref="audioRef" />

      <div class="track-info" v-if="playerStore.currentSong.title">
        <img :src="playerStore.currentSong.coverUrl" class="track-cover" alt="cover"/>
        <div class="track-text">
          <div class="track-title">{{ playerStore.currentSong.title }}</div>
          <div class="track-artist">Artist #{{ playerStore.currentSong.artistId }}</div>
        </div>
      </div>
      <div class="track-info" v-else>
        <div class="track-text" style="color: #b3b3b3; font-size: 14px;">暂无播放</div>
      </div>

      <div class="player-controls">
        <el-button circle size="large" color="#ffffff" style="color: black" @click="playerStore.togglePlay">
          <span v-if="playerStore.isPlaying" style="font-size: 18px; font-weight: bold;">||</span>
          <span v-else style="font-size: 18px; font-weight: bold;">▶</span>
        </el-button>
      </div>

      <div class="volume-control"></div>
    </div>
  </div>
</template>

<style scoped>
/* 确保全屏和 Flex 布局设置正确 */
.h-screen {
  height: 100vh;
}

.h-main {
  height: calc(100vh - 90px); /* 减去底部播放条的高度 */
}

.aside {
  background-color: black;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.logo {
  padding: 24px;
  color: white;
}

.el-menu-vertical {
  border-right: none;
  flex-grow: 1;
}

.logout-btn {
  margin-top: auto;
  padding: 20px;
  text-align: center;
}

/* Header & Main Content Styles (保持不变) */
.header {
  background-color: #121212;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-bottom: 1px solid #282828;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  background: black;
  padding: 5px 15px;
  border-radius: 20px;
}

.username {
  color: white;
  font-weight: bold;
}

.main-content {
  background-color: #121212;
  padding: 20px;
  overflow-y: auto;
}

/* 底部播放条样式 (保持不变) */
.player-bar {
  position: fixed;
  bottom: 0;
  width: 100%;
  height: 90px;
  background-color: #181818;
  border-top: 1px solid #282828;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 999;
}

.track-info {
  display: flex;
  align-items: center;
  width: 30%;
}

.track-cover {
  width: 56px;
  height: 56px;
  border-radius: 4px;
  margin-right: 15px;
  object-fit: cover;
}

.track-title {
  font-size: 14px;
  color: white;
  font-weight: 500;
}

.track-artist {
  font-size: 12px;
  color: #b3b3b3;
  margin-top: 4px;
}

.player-controls {
  width: 40%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.volume-control {
  width: 30%;
}
</style>