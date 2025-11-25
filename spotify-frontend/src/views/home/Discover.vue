<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { usePlayerStore } from '@/stores/player'

// 定义接口类型 (我们现在知道后端返回的是 SongVo)
interface SongVo {
  id: number
  title: string
  artistName: string // ⬅️ 关键：显示歌手名
  coverUrl: string
  fileUrl: string
}

const songList = ref<SongVo[]>([])
const playerStore = usePlayerStore()

// 获取歌曲数据
const fetchSongs = async () => {
  try {
    // 调用 /song/list，现在后端会返回 SongVo (带 artistName)
    const res = await request.get('/song/list')
    songList.value = res.data
  } catch (error) {
    console.error("获取歌单失败", error)
  }
}

// 点击播放逻辑
const playMusic = (song: SongVo) => {
  console.log("点击播放:", song.title)
  playerStore.setSong(song)
}

onMounted(() => {
  fetchSongs()
})
</script>

<template>
  <div class="discover-container">
    <h2 class="title">今日推荐</h2>

    <div class="song-grid">
      <div
          v-for="song in songList"
          :key="song.id"
          class="song-card"
          @click="playMusic(song)"
      >
        <div class="cover-wrapper">
          <img :src="song.coverUrl" alt="cover" class="cover-img" />
          <div class="play-btn-overlay">▶</div>
        </div>
        <div class="song-info">
          <div class="song-title">{{ song.title }}</div>
          <div class="song-artist">{{ song.artistName || '未知歌手' }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.title {
  color: white;
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: bold;
}

.song-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 24px;
}

.song-card {
  background-color: #181818;
  padding: 16px;
  border-radius: 8px;
  transition: background-color 0.3s ease;
  cursor: pointer;
}

.song-card:hover {
  background-color: #282828;
}

.cover-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  margin-bottom: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.5);
  border-radius: 6px;
  overflow: hidden;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-btn-overlay {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #1db954;
  color: black;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  opacity: 0;
  transform: translateY(8px);
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.song-card:hover .play-btn-overlay {
  opacity: 1;
  transform: translateY(0);
}

.song-title {
  color: white;
  font-weight: bold;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.song-artist {
  color: #b3b3b3;
  font-size: 14px;
}
</style>