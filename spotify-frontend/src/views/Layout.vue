<script setup lang="ts">
import { RouterView, useRoute, useRouter } from 'vue-router'
import { ref, watch, computed, onMounted } from 'vue'
import { usePlayerStore } from '@/stores/player'
import SharePopover from '@/components/SharePopover.vue'

const router = useRouter()
const route = useRoute()
const playerStore = usePlayerStore()
const audioRef = ref<HTMLAudioElement | null>(null)
// 获取用户信息
const user = JSON.parse(localStorage.getItem('user') || '{}')
const userRoles = computed(() => (Array.isArray(user.roles) ? user.roles : []))
const displayName = computed(() => user.username || '未命名用户')

// --- 权限控制 ---
const isMusician = computed(() => userRoles.value.includes('musician'))
const isAdmin = computed(() => userRoles.value.includes('admin'))
const roleLabel = computed(() => {
  if (isAdmin.value) return '管理员'
  if (isMusician.value) return '音乐人'
  return '听众'
})

const menuEntries = [
  '/discover',
  '/new',
  '/search',
  '/charts',
  '/genres',
  '/history',
  '/library',
  '/profile',
  '/upload',
  '/musician/stats',
  '/musician/works',
  '/musician/albums',
  '/admin/dashboard',
  '/admin/users',
  '/admin/songs',
  '/admin/albums'
]
const activeMenu = computed(() => {
  const segments = route.path.split('/').filter(Boolean)
  if (segments.length === 0) return '/discover'
  const basePath = `/${segments.slice(0, Math.min(2, segments.length)).join('/')}`
  const matched = menuEntries.find((path) => basePath.startsWith(path) || route.path.startsWith(path))
  return matched || '/discover'
})

// --- 初始化 ---
onMounted(() => {
  // 加载用户喜欢的歌曲列表 (用于点亮爱心)
  playerStore.fetchLikedSongs()
})

// --- 交互逻辑 ---
const toggleLike = () => {
  if (playerStore.currentSong.id) {
    playerStore.toggleLike(playerStore.currentSong.id)
  }
}

const isShareReady = computed(() => Boolean(playerStore.currentSong.id))

const goToDetail = () => {
  if (playerStore.currentSong.id) {
    router.push(`/song/${playerStore.currentSong.id}`)
  }
}
const goToArtistDetail = () => {
  if (playerStore.currentSong.artistId) {
    router.push(`/artist/${playerStore.currentSong.artistId}`)
  }
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

// --- 音频事件 ---
const onTimeUpdate = () => { if (audioRef.value) playerStore.currentTime = audioRef.value.currentTime }
const onLoadedMetadata = () => { if (audioRef.value) playerStore.duration = audioRef.value.duration }
const onEnded = () => {
  if (playerStore.mode === 'loop') {
    if (audioRef.value) { audioRef.value.currentTime = 0; audioRef.value.play() }
  } else {
    playerStore.next()
  }
}

// --- 控件事件 ---
const onSliderChange = (val: number) => { if (audioRef.value) audioRef.value.currentTime = val }
const onVolumeChange = (val: number) => { if (audioRef.value) audioRef.value.volume = val / 100 }

// --- 队列弹窗 ---
const isQueueOpen = ref(false)
const toggleQueue = () => { isQueueOpen.value = !isQueueOpen.value }
const playFromQueue = (index: number) => {
  const target = playerStore.playList[index]
  if (target) playerStore.setSong(target)
}

// --- 状态监听 ---
watch(() => playerStore.isPlaying, (playing) => {
  if (audioRef.value) playing ? audioRef.value.play().catch(()=>{}) : audioRef.value.pause()
})
watch(() => playerStore.currentSong, (newSong) => {
  if (audioRef.value && newSong.fileUrl) {
    audioRef.value.src = newSong.fileUrl
    audioRef.value.play().catch(()=>{})
    playerStore.isPlaying = true
  }
})

// 工具：时间格式化
const formatTime = (seconds: number) => {
  if (!seconds || isNaN(seconds)) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<template>
  <div class="common-layout">
    <el-container class="h-main">
      <el-aside width="240px" class="spotify-aside">
        <div class="logo">
          <span style="color:white; font-size: 24px; font-weight: 700; letter-spacing: -1px;">Spotify</span>
        </div>

        <el-menu :default-active="activeMenu" class="spotify-menu" router>
          <el-menu-item-group>
            <template #title>
              <div class="section-title">浏览</div>
            </template>
            <el-menu-item index="/discover"><el-icon><i class="el-icon-house"></i></el-icon><span>首页</span></el-menu-item>
            <el-menu-item index="/new"><el-icon><i class="el-icon-bell"></i></el-icon><span>新发行</span></el-menu-item>
            <el-menu-item index="/search"><el-icon><i class="el-icon-search"></i></el-icon><span>搜索</span></el-menu-item>
            <el-menu-item index="/charts"><el-icon><i class="el-icon-data-analysis"></i></el-icon><span>排行榜</span></el-menu-item>
            <el-menu-item index="/genres"><el-icon><i class="el-icon-collection-tag"></i></el-icon><span>流派分类</span></el-menu-item>
          </el-menu-item-group>

          <el-menu-item-group>
            <template #title>
              <div class="section-title">我的音乐</div>
            </template>
            <el-menu-item index="/history"><el-icon><i class="el-icon-time"></i></el-icon><span>播放历史</span></el-menu-item>
            <el-menu-item index="/library"><el-icon><i class="el-icon-collection"></i></el-icon><span>音乐库</span></el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><i class="el-icon-user"></i></el-icon><span>个人中心</span>
            </el-menu-item>
          </el-menu-item-group>

          <el-menu-item-group v-if="isMusician">
            <template #title>
              <div class="section-title">音乐人</div>
            </template>
            <el-menu-item index="/upload">
              <el-icon><i class="el-icon-upload"></i></el-icon><span>发布歌曲</span>
            </el-menu-item>
            <el-menu-item index="/musician/stats">
              <el-icon><i class="el-icon-data-board"></i></el-icon><span>作品数据</span>
            </el-menu-item>
          <el-menu-item index="/musician/works">
            <el-icon><i class="el-icon-folder"></i></el-icon><span>我的作品</span>
          </el-menu-item>
          <el-menu-item index="/musician/albums">
            <el-icon><i class="el-icon-collection"></i></el-icon><span>我的专辑</span>
          </el-menu-item>
        </el-menu-item-group>

          <el-menu-item-group v-if="isAdmin">
            <template #title>
              <div class="section-title">管理后台</div>
            </template>
            <el-menu-item index="/admin/dashboard">
              <el-icon><i class="el-icon-data-board"></i></el-icon><span>数据看板</span>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><i class="el-icon-user"></i></el-icon><span>用户管理</span>
            </el-menu-item>
          <el-menu-item index="/admin/songs">
            <el-icon><i class="el-icon-files"></i></el-icon><span>内容管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/albums">
            <el-icon><i class="el-icon-collection"></i></el-icon><span>专辑管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/artists">
            <el-icon><i class="el-icon-user"></i></el-icon><span>艺人管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/genres">
            <el-icon><i class="el-icon-collection-tag"></i></el-icon><span>流派管理</span>
          </el-menu-item>
        </el-menu-item-group>
        </el-menu>

        <div class="logout-area" @click="logout">
          <el-icon><i class="el-icon-switch-button"></i></el-icon>
          <span>退出登录</span>
        </div>
      </el-aside>

      <el-container class="content-container">
        <el-header class="spotify-header">
          <div class="header-actions">
            <div class="user-pill">
              <el-avatar :size="32" :src="user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
              <div class="user-info">
                <span class="username">{{ displayName }}</span>
                <span class="user-role">{{ roleLabel }}</span>
              </div>
            </div>
          </div>
        </el-header>

        <el-main class="spotify-main">
          <RouterView />
        </el-main>
      </el-container>
    </el-container>

    <div class="player-bar">
      <audio ref="audioRef" @timeupdate="onTimeUpdate" @loadedmetadata="onLoadedMetadata" @ended="onEnded" />

      <div class="left-controls">
        <div class="track-info" v-if="playerStore.currentSong.title">
          <div class="cover-container" @click="goToDetail">
            <img :src="playerStore.currentSong.coverUrl" class="track-cover" />
            <div class="cover-mask">
              <svg role="img" height="20" width="20" viewBox="0 0 24 24" fill="white"><path d="M12 8l-6 6 1.41 1.41L12 10.83l4.59 4.58L18 14z"/></svg>
            </div>
          </div>
            <div class="track-text">
              <div class="track-title" @click="goToDetail">{{ playerStore.currentSong.title }}</div>
            <div class="track-artist" @click="goToArtistDetail">{{ playerStore.currentSong.artistName }}</div>
          </div>

          <div class="player-quick-actions">
            <button class="icon-btn like-btn"
                    :class="{ 'is-active': playerStore.likedSongIds.has(playerStore.currentSong.id) }"
                    @click="toggleLike"
            >
              <svg v-if="!playerStore.likedSongIds.has(playerStore.currentSong.id)" role="img" height="16" width="16" viewBox="0 0 16 16" fill="currentColor"><path d="M1.69 2A4.582 4.582 0 018 2.023 4.583 4.583 0 0111.88.817h.002a4.618 4.618 0 013.782 3.65v.003a4.543 4.543 0 01-1.011 3.84L9.35 14.629a1.765 1.765 0 01-2.093.464 1.762 1.762 0 01-.605-.463L1.348 8.309A4.582 4.582 0 011.689 2zm3.158.252c-.896 0-1.747.466-2.201 1.255v.002a3.312 3.312 0 00-.16 2.815l5.185 6.12a.25.25 0 00.386-.002l5.31-6.26a3.315 3.315 0 00.175-2.675V3.5a3.318 3.318 0 00-2.712-2.62 3.324 3.324 0 00-3.332.884l-.45.45-.45-.45a3.313 3.313 0 00-2.07-.91z"></path></svg>
              <svg v-else role="img" height="16" width="16" viewBox="0 0 16 16" fill="#1db954"><path d="M15.724 4.22A4.313 4.313 0 0012.192.814a4.269 4.269 0 00-3.622 1.13.837.837 0 01-1.14 0 4.272 4.272 0 00-3.626-1.13 4.313 4.313 0 00-3.531 3.406c-.253 1.645.577 4.08 4.653 7.913l.063.061.063-.061c4.076-3.833 4.906-6.273 4.653-7.913z"></path></svg>
            </button>
            <SharePopover
                v-if="isShareReady"
                resource-type="song"
                :resource-id="playerStore.currentSong.id"
                :title="playerStore.currentSong.title"
                :artist-name="playerStore.currentSong.artistName"
            />
          </div>
        </div>
        <div class="track-info" v-else>
          <div class="placeholder-box"></div>
          <div class="track-text" style="color:#b3b3b3; font-size: 12px;">暂无播放</div>
        </div>
      </div>

      <div class="center-controls">
        <div class="buttons-row">
          <button class="icon-btn small" :class="{ 'is-active': playerStore.mode === 'random' }" @click="playerStore.toggleMode">
            <svg role="img" height="16" width="16" viewBox="0 0 16 16" fill="currentColor"><path d="M13.151.922a.75.75 0 10-1.06 1.06L13.109 3H11.16a3.75 3.75 0 00-2.873 1.34l-6.173 7.356A2.25 2.25 0 01.39 12.5H0V14h.391a3.75 3.75 0 002.873-1.34l6.173-7.356a2.25 2.25 0 011.724-.804h1.947l-1.017 1.018a.75.75 0 001.06 1.06L15.98 3.75 13.15.922zM.391 3.5H0V2h.391c1.109 0 2.16.49 2.873 1.34L4.89 5.277l-.979 1.167-1.796-2.14A2.25 2.25 0 00.39 3.5z"></path><path d="M7.5 10.723l.98-1.167.957 1.14a2.25 2.25 0 001.724.804h1.947l-1.017-1.018a.75.75 0 111.06-1.06l2.829 2.829-2.829 2.829a.75.75 0 11-1.06-1.06L13.109 13H11.16a3.75 3.75 0 00-2.873-1.34l-.787-.938z"></path></svg>
          </button>
          <button class="icon-btn" @click="playerStore.prev">
            <svg role="img" height="16" width="16" viewBox="0 0 16 16" fill="currentColor"><path d="M3.3 1a.7.7 0 01.7.7v5.15l9.95-5.744a.7.7 0 011.05.606v12.575a.7.7 0 01-1.05.607L4 9.149V14.3a.7.7 0 01-.7.7H1.7a.7.7 0 01-.7-.7V1.7a.7.7 0 01.7-.7h1.6z"></path></svg>
          </button>

          <button class="play-circle-btn" @click="playerStore.togglePlay">
            <svg v-if="playerStore.isPlaying" role="img" height="16" width="16" viewBox="0 0 16 16" fill="black"><path d="M2.7 1a.7.7 0 00-.7.7v12.6a.7.7 0 00.7.7h2.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7H2.7zm8 0a.7.7 0 00-.7.7v12.6a.7.7 0 00.7.7h2.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7h-2.6z"></path></svg>
            <svg v-else role="img" height="16" width="16" viewBox="0 0 16 16" fill="black"><path d="M3 1.713a.7.7 0 011.05-.607l10.89 6.288a.7.7 0 010 1.212L4.05 14.894A.7.7 0 013 14.288V1.713z"></path></svg>
          </button>

          <button class="icon-btn" @click="playerStore.next">
            <svg role="img" height="16" width="16" viewBox="0 0 16 16" fill="currentColor"><path d="M12.7 1a.7.7 0 00-.7.7v5.15L2.05 1.107A.7.7 0 001 1.712v12.575a.7.7 0 001.05.607L12 9.149V14.3a.7.7 0 00.7.7h1.6a.7.7 0 00.7-.7V1.7a.7.7 0 00-.7-.7h-1.6z"></path></svg>
          </button>
          <button class="icon-btn small" :class="{ 'is-active': playerStore.mode === 'loop' }" @click="playerStore.toggleMode">
            <svg role="img" height="16" width="16" viewBox="0 0 16 16" fill="currentColor"><path d="M0 4.75A3.75 3.75 0 013.75 1h8.5A3.75 3.75 0 0116 4.75v5a3.75 3.75 0 01-3.75 3.75h-.36l2.305 2.305a.7.7 0 11-1.06 1.06l-3.586-3.586a.7.7 0 010-1.061l3.586-3.586a.7.7 0 011.06 1.06L11.89 12h.36a2.25 2.25 0 002.25-2.25v-5a2.25 2.25 0 00-2.25-2.25h-8.5A2.25 2.25 0 001.5 4.75v5A2.25 2.25 0 003.75 12h5a.75.75 0 010 1.5h-5A3.75 3.75 0 010 9.75v-5z"></path></svg>
          </button>
        </div>

        <div class="progress-bar-row">
          <span class="time-text">{{ formatTime(playerStore.currentTime) }}</span>
          <div class="slider-wrapper">
            <el-slider
                v-model="playerStore.currentTime"
                :max="playerStore.duration || 100"
                :show-tooltip="false"
                @input="onSliderChange"
                size="small"
                class="spotify-slider"
            />
          </div>
          <span class="time-text">{{ formatTime(playerStore.duration) }}</span>
        </div>
      </div>

      <div class="right-controls">
        <button class="icon-btn small" title="Lyrics"><svg role="img" height="16" width="16" viewBox="0 0 16 16" fill="currentColor"><path d="M13.426 2.574a2.831 2.831 0 00-4.797 1.55l3.247 3.247a2.831 2.831 0 001.55-4.797zM10.5 8.118l-2.619-2.62A63.275 63.275 0 017.883 5.5l-.611-.453C5.405 3.19 1.604.5 1.604.5a.28.28 0 00-.28.28c0 .166.161.275.265.353.224.167.462.34.713.516l.67.467c1.838 1.285 4.54 3.115 5.17 5.387l-2.62 2.62a2.83 2.83 0 000 4.003l.476.476a2.83 2.83 0 004.004 0l2.618-2.619zm-1.414 4.953l-.476-.476a.83.83 0 010-1.174l2.266-2.265 1.65 1.65-2.266 2.265a.83.83 0 01-1.174 0z"></path></svg></button>
        <button class="icon-btn small" title="Queue" @click="toggleQueue"><svg role="img" height="16" width="16" viewBox="0 0 16 16" fill="currentColor"><path d="M15 15H1v-1.5h14V15zm0-4.5H1V9h14v1.5zm-14-7A2.5 2.5 0 013.5 1h9a2.5 2.5 0 010 5h-9A2.5 2.5 0 011 3.5zm2.5-1a1 1 0 000 2h9a1 1 0 100-2h-9z"></path></svg></button>

        <div class="volume-group">
          <button class="icon-btn small"><svg role="img" height="16" width="16" viewBox="0 0 16 16" fill="currentColor"><path d="M9.741.534a.75.75 0 01.735.645C10.746 2.83 11.5 5.117 11.5 7.5s-.754 4.67-1.024 6.321a.75.75 0 01-1.479-.242c.25-1.536.943-3.683.943-6.079s-.693-4.543-.943-6.08a.75.75 0 01.644-.886zM1.75 3.5h2.85l3.613-2.709A1.25 1.25 0 0110.25 1.8v12.401a1.25 1.25 0 01-2.037.99L4.601 12.5H1.75A1.75 1.75 0 010 10.75v-5.5A1.75 1.75 0 011.75 3.5zM13.293 1.843a.75.75 0 01.734.645c.496 3.035.973 5.012.973 5.012s-.477 1.977-.973 5.012a.75.75 0 01-1.479-.242c.48-2.946.912-4.77.912-4.77s-.432-1.824-.912-4.77a.75.75 0 01.645-.886z"></path></svg></button>
          <div class="volume-slider-wrapper">
            <el-slider v-model="playerStore.volume" @input="onVolumeChange" size="small" class="spotify-slider" />
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-drawer
    v-model="isQueueOpen"
    title="播放队列"
    size="360px"
    direction="rtl"
    destroy-on-close
  >
    <div v-if="playerStore.playList.length === 0" class="queue-empty">暂无播放内容</div>
    <ul v-else class="queue-list">
      <li
        v-for="(song, index) in playerStore.playList"
        :key="song.id || index"
        :class="{ active: playerStore.currentSong.id === song.id }"
        @click="playFromQueue(index)"
      >
        <div class="queue-title">{{ song.title || '未命名歌曲' }}</div>
        <div class="queue-artist">{{ song.artistName || '未知歌手' }}</div>
      </li>
    </ul>
  </el-drawer>
</template>

<style scoped>
/* === 布局 === */
.h-main { height: calc(100vh - 90px); display: flex; }
.spotify-aside { background-color: var(--spotify-light-gray); display: flex; flex-direction: column; padding: 24px 12px; gap: 12px; border-right: 1px solid var(--spotify-border); }
.logo { padding-left: 12px; margin-bottom: 8px; }
.spotify-menu { border-right: none; background-color: transparent; flex-grow: 1; }
:deep(.el-menu-item-group__title) { padding-left: 12px; font-size: 12px; letter-spacing: 0.5px; color: var(--spotify-text-sub); }
.section-title { color: var(--spotify-text-sub); font-weight: 700; text-transform: uppercase; letter-spacing: 0.6px; font-size: 12px; }
:deep(.el-menu-item) { color: var(--spotify-text-sub) !important; font-weight: 700; border-radius: 6px; margin-bottom: 4px; height: 40px; font-size: 14px; }
:deep(.el-menu-item:hover) { color: var(--spotify-white) !important; background-color: var(--spotify-hover-gray) !important; }
:deep(.el-menu-item.is-active) { color: var(--spotify-white) !important; background-color: #1d1d1d !important; box-shadow: inset 0 0 0 1px rgba(255,255,255,0.04); }
:deep(.el-icon) { font-size: 20px; margin-right: 14px; }

.logout-area {
  margin-top: auto;
  padding: 12px 12px;
  color: var(--spotify-text-sub);
  cursor: pointer;
  display: flex;
  align-items: center;
  font-weight: 700;
  font-size: 14px;
  transition: color 0.2s, background-color 0.2s;
  border-top: 1px solid var(--spotify-border);
  border-radius: 8px;
}
.logout-area:hover { color: var(--spotify-white); background-color: rgba(255,255,255,0.03); }
.logout-area .el-icon { margin-right: 16px; font-size: 24px; }

.content-container {
  position: relative;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.spotify-header { background: linear-gradient(90deg, rgba(24, 24, 24, 0.92), rgba(12, 12, 12, 0.82)); height: 64px; display: flex; align-items: center; justify-content: flex-end; padding: 0 32px; position: absolute; width: 100%; top: 0; z-index: 100; box-sizing: border-box; border-bottom: 1px solid rgba(255,255,255,0.04); backdrop-filter: blur(6px); }
.header-actions { display: flex; align-items: center; gap: 12px; }
.user-pill { background-color: rgba(255,255,255,0.04); border-radius: 28px; padding: 4px 14px 4px 6px; display: flex; align-items: center; gap: 10px; cursor: pointer; transition: 0.2s; border: 1px solid rgba(255,255,255,0.06); box-shadow: 0 12px 40px rgba(0,0,0,0.35); }
.user-pill:hover { background-color: rgba(255,255,255,0.08); border-color: rgba(255,255,255,0.18); }
.user-info { display: flex; flex-direction: column; line-height: 1.2; }
.username { font-size: 14px; font-weight: 700; margin-right: 4px; }
.user-role { font-size: 12px; color: #9e9e9e; }

.spotify-main {
  background:
    linear-gradient(180deg, rgba(30, 215, 96, 0.08) 0%, rgba(30, 215, 96, 0) 18%),
    linear-gradient(to bottom, #1b1b1b 0%, #111 320px);
  padding: 84px 32px 20px 32px;
  overflow-y: auto;
  flex-grow: 1;
  border-radius: 16px 16px 0 0;
}

/* === 底部播放条 (Fixed Bottom) === */
.player-bar {
  position: fixed; bottom: 0; width: 100%; height: 90px;
  background: linear-gradient(90deg, #0f0f0f, #121212);
  border-top: 1px solid #1f1f1f;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; z-index: 999;
  box-shadow: 0 -10px 30px rgba(0,0,0,0.45);
}

/* 左侧 */
.left-controls { width: 30%; min-width: 180px; display: flex; align-items: center; justify-content: flex-start; }
.track-info { display: flex; align-items: center; }
.cover-container { position: relative; width: 56px; height: 56px; margin-right: 14px; cursor: pointer; flex-shrink: 0; box-shadow: 0 0 10px rgba(0,0,0,0.3); }
.track-cover { width: 100%; height: 100%; border-radius: 4px; object-fit: cover; }
.cover-mask { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); border-radius: 4px; display: none; align-items: center; justify-content: center; }
.cover-container:hover .cover-mask { display: flex; }
.placeholder-box { width: 56px; height: 56px; background: #282828; border-radius: 4px; margin-right: 14px; }

.track-text { display: flex; flex-direction: column; justify-content: center; margin-right: 14px; overflow: hidden; max-width: 150px; }
.track-title { font-size: 14px; color: white; font-weight: 400; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.track-title:hover { text-decoration: underline; }
.track-artist { font-size: 11px; color: #b3b3b3; margin-top: 3px; cursor: pointer; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.track-artist:hover { color: white; text-decoration: underline; }
.player-quick-actions { display: flex; align-items: center; gap: 8px; margin-left: 10px; }

/* 中间 */
.center-controls { width: 40%; max-width: 722px; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.buttons-row { display: flex; align-items: center; gap: 16px; margin-bottom: 8px; }
.progress-bar-row { width: 100%; display: flex; align-items: center; gap: 8px; font-size: 11px; color: #a7a7a7; }
.slider-wrapper { flex-grow: 1; display: flex; align-items: center; }
.time-text { min-width: 40px; text-align: center; font-variant-numeric: tabular-nums; }

/* 右侧 */
.right-controls { width: 30%; min-width: 180px; display: flex; justify-content: flex-end; align-items: center; gap: 8px; }
.volume-group { display: flex; align-items: center; width: 125px; }
.volume-slider-wrapper { flex-grow: 1; margin-left: 8px; }

/* 按钮样式 */
.icon-btn {
  background: transparent; border: none; color: #b3b3b3;
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: color 0.2s; padding: 0;
}
.icon-btn:hover { color: white; }
.icon-btn.is-active { color: var(--spotify-green); position: relative; }
.icon-btn.is-active::after { content: '•'; position: absolute; bottom: 3px; font-size: 18px; line-height: 0; }
.icon-btn.small svg { width: 16px; height: 16px; }
.like-btn { color: #b3b3b3; margin-left: 10px; }
.like-btn.is-active { color: var(--spotify-green); }

.play-circle-btn {
  width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, var(--spotify-green), var(--spotify-green-bright)); border: none; color: #0b0b0b;
  display: flex; align-items: center; justify-content: center; cursor: pointer; transition: transform 0.1s, box-shadow 0.2s;
  box-shadow: 0 14px 30px rgba(29, 185, 84, 0.35);
}
.play-circle-btn:hover { transform: scale(1.06); box-shadow: 0 18px 36px rgba(29, 185, 84, 0.45); }
.play-circle-btn:active { transform: scale(0.95); }

/* Spotify 风格 Slider */
:deep(.spotify-slider) { --el-slider-main-bg-color: #b3b3b3; --el-slider-runway-bg-color: #535353; --el-slider-button-size: 12px; }
:deep(.spotify-slider .el-slider__bar) { background-color: #9bd4aa; transition: background-color 0.2s; }
:deep(.spotify-slider:hover .el-slider__bar) { background-color: var(--spotify-green); } /* 悬停变绿 */
:deep(.spotify-slider .el-slider__runway) { height: 4px; border-radius: 2px; }
:deep(.spotify-slider .el-slider__button) { border: none; background-color: white; display: none; box-shadow: 0 2px 4px rgba(0,0,0,0.3); }
:deep(.spotify-slider:hover .el-slider__button) { display: block; } /* 悬停显示滑块 */
:deep(.spotify-slider .el-slider__button-wrapper) { top: -15px; }

/* 队列抽屉 */
.queue-empty {
  color: #b3b3b3;
  text-align: center;
  padding: 24px 0;
}

.queue-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}


.queue-list li {
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--spotify-surface);
  cursor: pointer;
  transition: background 0.2s, transform 0.2s, border-color 0.2s;
  border: 1px solid var(--spotify-border);
}

.queue-list li:hover {
  background: #282828;
  transform: translateX(-2px);
}

.queue-list li.active {
  border-color: var(--spotify-green);
  background: rgba(29, 185, 84, 0.08);
  box-shadow: 0 10px 24px rgba(0,0,0,0.35);
}

.queue-title { color: white; font-weight: 700; font-size: 14px; }
.queue-artist { color: #a7a7a7; font-size: 12px; margin-top: 2px; }
</style>
