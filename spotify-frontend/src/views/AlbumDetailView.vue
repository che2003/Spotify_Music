<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { usePlayerStore } from '@/stores/player'
import { ElMessage } from 'element-plus'

interface SongVo {
  id: number
  title: string
  artistName: string
  artistId: number
  coverUrl: string
  fileUrl: string
  albumId?: number
  albumTitle?: string
  albumCover?: string
  duration?: number
}

const route = useRoute()
const router = useRouter()
const songs = ref<SongVo[]>([])
const loading = ref(true)
const playerStore = usePlayerStore()

const albumSongs = computed(() => songs.value.filter(song => song.albumId === Number(route.params.id)))
const albumMeta = computed(() => {
  const sample = albumSongs.value[0]
  return {
    title: sample?.albumTitle || `专辑 #${route.params.id}`,
    cover: sample?.albumCover || sample?.coverUrl,
    artistName: sample?.artistName,
    artistId: sample?.artistId
  }
})

const fetchSongs = async () => {
  loading.value = true
  try {
    const res = await request.get('/song/list')
    songs.value = res.data
    if (!albumSongs.value.length) {
      ElMessage.warning('该专辑暂无歌曲')
    }
  } catch (e) {
    ElMessage.error('加载专辑失败')
  } finally {
    loading.value = false
  }
}

const playSong = (song: SongVo) => playerStore.setSong(song)
const goArtist = () => { if (albumMeta.value.artistId) router.push(`/artist/${albumMeta.value.artistId}`) }

onMounted(fetchSongs)
</script>

<template>
  <div class="album-page">
    <div class="album-hero" v-if="albumSongs.length">
      <img :src="albumMeta.cover || 'https://placehold.co/240x240?text=Album'" class="cover" />
      <div class="hero-text">
        <p class="type-label">专辑</p>
        <h1 class="title">{{ albumMeta.title }}</h1>
        <p class="artist" v-if="albumMeta.artistName" @click="goArtist">{{ albumMeta.artistName }}</p>
        <p class="subtitle">{{ albumSongs.length }} 首歌曲</p>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="6" animated />

    <div v-if="albumSongs.length" class="tracklist">
      <div class="track" v-for="song in albumSongs" :key="song.id">
        <div class="info">
          <img :src="song.coverUrl" class="thumb" />
          <div class="text">
            <div class="name">{{ song.title }}</div>
            <div class="artist">{{ song.artistName }}</div>
          </div>
        </div>
        <div class="duration">{{ song.duration ? Math.floor(song.duration / 60) + ':' + (song.duration % 60).toString().padStart(2, '0') : '--:--' }}</div>
        <el-button size="small" type="success" @click="playSong(song)">播放</el-button>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-text">暂无曲目</div>
  </div>
</template>

<style scoped>
.album-page { color: white; padding: 10px 0 40px; }
.album-hero { display: flex; gap: 20px; align-items: center; margin-bottom: 24px; }
.cover { width: 240px; height: 240px; object-fit: cover; border-radius: 12px; box-shadow: 0 12px 30px rgba(0,0,0,0.35); }
.hero-text { display: flex; flex-direction: column; gap: 6px; }
.type-label { font-size: 12px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; opacity: 0.8; }
.title { font-size: 42px; font-weight: 900; margin: 0; letter-spacing: -0.5px; }
.artist { color: #b3b3b3; cursor: pointer; }
.artist:hover { color: #fff; text-decoration: underline; }
.subtitle { color: #b3b3b3; }
.tracklist { background: #181818; border-radius: 12px; padding: 10px; }
.track { display: grid; grid-template-columns: 1fr 100px 80px; align-items: center; padding: 10px; border-radius: 8px; transition: background 0.2s; }
.track:hover { background: #262626; }
.info { display: flex; align-items: center; gap: 12px; }
.thumb { width: 56px; height: 56px; object-fit: cover; border-radius: 6px; box-shadow: 0 8px 16px rgba(0,0,0,0.3); }
.text .name { font-weight: 700; }
.text .artist { color: #b3b3b3; font-size: 12px; }
.duration { color: #b3b3b3; }
.empty-text { color: #b3b3b3; margin-top: 12px; }
</style>
