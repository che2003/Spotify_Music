<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/player'

interface SongVo {
  id: number
  title: string
  artistName: string
  coverUrl: string
  fileUrl: string
}

const router = useRouter()
const songList = ref<SongVo[]>([])
const playerStore = usePlayerStore()
const loading = ref(true)

// 中文问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 12) return '早上好'
  if (hour >= 12 && hour < 19) return '下午好'
  return '晚上好'
})

// 数据分层 (模拟栏目)
const heroSongs = computed(() => songList.value.slice(0, 6))       // 顶部 6 个
const recommendSongs = computed(() => songList.value.slice(6, 12)) // 为您推荐
const newReleases = computed(() => songList.value.slice(12, 18))   // 新歌速递

const fetchRecommendations = async () => {
  loading.value = true
  try {
    // NCF 推荐接口
    const res = await request.get('/recommend/daily')
    songList.value = res.data
  } catch (error) {
    console.error("获取推荐失败", error)
  } finally {
    loading.value = false
  }
}

const playMusic = (song: SongVo) => { playerStore.setSong(song) }
const goToSongDetail = (id: number) => { router.push(`/song/${id}`) }

onMounted(() => { fetchRecommendations() })
</script>

<template>
  <div class="discover-container">
    <h2 class="greeting">{{ greeting }}</h2>

    <template v-if="loading">
      <div class="hero-grid">
        <el-skeleton v-for="i in 6" :key="i" animated class="skeleton-card">
          <template #template>
            <el-skeleton-item variant="image" style="width:80px;height:80px;border-radius:4px" />
            <div style="flex:1; padding: 0 12px;">
              <el-skeleton-item variant="text" style="width:70%; height:16px; margin-top: 12px;" />
            </div>
          </template>
        </el-skeleton>
      </div>

      <div class="section">
        <div class="section-header">
          <h2 class="section-title">为您推荐</h2>
        </div>
        <div class="song-grid">
          <el-skeleton v-for="i in 6" :key="'rec-' + i" animated class="song-card">
            <template #template>
              <el-skeleton-item variant="image" style="width:100%;height:160px;border-radius:8px" />
              <el-skeleton-item variant="text" style="width:80%;margin-top:12px;" />
              <el-skeleton-item variant="text" style="width:60%;margin-top:8px;" />
            </template>
          </el-skeleton>
        </div>
      </div>
    </template>

    <template v-else>
      <div class="hero-grid">
        <div v-for="song in heroSongs" :key="'hero-' + song.id" class="hero-card" @click="playMusic(song)">
          <img :src="song.coverUrl" class="hero-img" />
          <span class="hero-text">{{ song.title }}</span>
          <div class="play-btn-hero" @click.stop="playMusic(song)">
            <svg role="img" height="24" width="24" viewBox="0 0 24 24" fill="black"><path d="M7.05 3.606l13.49 7.788a.7.7 0 010 1.212L7.05 20.394A.7.7 0 016 19.788V4.212a.7.7 0 011.05-.606z"></path></svg>
          </div>
        </div>
      </div>

      <div class="section" v-if="recommendSongs.length > 0">
        <div class="section-header">
          <h2 class="section-title">为您推荐</h2>
        </div>
        <div class="song-grid">
          <div v-for="song in recommendSongs" :key="song.id" class="song-card" @click="goToSongDetail(song.id)">
            <div class="cover-wrapper">
              <img :src="song.coverUrl" class="cover-img" />
              <div class="play-btn-overlay" @click.stop="playMusic(song)">
                <svg role="img" height="24" width="24" viewBox="0 0 24 24" fill="black"><path d="M7.05 3.606l13.49 7.788a.7.7 0 010 1.212L7.05 20.394A.7.7 0 016 19.788V4.212a.7.7 0 011.05-.606z"></path></svg>
              </div>
            </div>
            <div class="song-info">
              <div class="song-title">{{ song.title }}</div>
              <div class="song-artist">{{ song.artistName || '未知歌手' }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="section" v-if="newReleases.length > 0">
        <div class="section-header">
          <h2 class="section-title">新歌速递</h2>
        </div>
        <div class="song-grid">
          <div v-for="song in newReleases" :key="song.id" class="song-card" @click="goToSongDetail(song.id)">
            <div class="cover-wrapper">
              <img :src="song.coverUrl" class="cover-img" />
              <div class="play-btn-overlay" @click.stop="playMusic(song)">
                <svg role="img" height="24" width="24" viewBox="0 0 24 24" fill="black"><path d="M7.05 3.606l13.49 7.788a.7.7 0 010 1.212L7.05 20.394A.7.7 0 016 19.788V4.212a.7.7 0 011.05-.606z"></path></svg>
              </div>
            </div>
            <div class="song-info">
              <div class="song-title">{{ song.title }}</div>
              <div class="song-artist">{{ song.artistName || '未知歌手' }}</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.discover-container { padding-top: 24px; padding-bottom: 50px; }
.greeting { color: white; font-size: 32px; font-weight: 700; margin-bottom: 24px; letter-spacing: -0.5px; }
.hero-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 12px 24px; margin-bottom: 40px; }
.hero-card { display: flex; align-items: center; background-color: #2a2a2a; border-radius: 4px; overflow: hidden; cursor: pointer; transition: all 0.3s ease; height: 80px; position: relative; box-shadow: 0 4px 4px rgba(0,0,0,0.2); }
.hero-card:hover { background-color: #404040; }
.hero-img { width: 80px; height: 80px; object-fit: cover; box-shadow: 2px 0 8px rgba(0,0,0,0.3); }
.hero-text { color: white; font-weight: 700; font-size: 15px; padding: 0 16px; flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.play-btn-hero { position: absolute; right: 16px; width: 48px; height: 48px; border-radius: 50%; background-color: #1db954; display: flex; align-items: center; justify-content: center; box-shadow: 0 8px 8px rgba(0,0,0,0.3); opacity: 0; transform: translateY(4px); transition: all 0.3s ease; }
.hero-card:hover .play-btn-hero { opacity: 1; transform: translateY(0); }
.play-btn-hero:hover { transform: scale(1.1) !important; background-color: #1ed760; }

.section { margin-bottom: 40px; }
.section-header { display: flex; justify-content: space-between; align-items: end; margin-bottom: 20px; }
.section-title { color: white; font-size: 24px; font-weight: 700; }
.song-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 24px; }
.song-card { background-color: #181818; padding: 16px; border-radius: 8px; transition: background-color 0.3s ease; cursor: pointer; position: relative; }
.song-card:hover { background-color: #282828; }
.cover-wrapper { position: relative; width: 100%; aspect-ratio: 1; margin-bottom: 16px; box-shadow: 0 8px 24px rgba(0,0,0,0.5); border-radius: 6px; overflow: hidden; }
.cover-img { width: 100%; height: 100%; object-fit: cover; }
.play-btn-overlay { position: absolute; bottom: 8px; right: 8px; width: 48px; height: 48px; border-radius: 50%; background-color: #1db954; display: flex; align-items: center; justify-content: center; opacity: 0; transform: translateY(8px); transition: all 0.3s ease; box-shadow: 0 8px 16px rgba(0,0,0,0.3); }
.song-card:hover .play-btn-overlay { opacity: 1; transform: translateY(0); }
.play-btn-overlay:hover { transform: scale(1.1) !important; background-color: #1ed760; }
.song-title { color: white; font-weight: 700; font-size: 16px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 8px; }
.song-artist { color: #b3b3b3; font-size: 14px; font-weight: 400; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.skeleton-card { display: flex; align-items: center; padding: 12px; background: #181818; border-radius: 8px; }
</style>