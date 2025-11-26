<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { usePlayerStore } from '@/stores/player'

interface SongVo {
  id: number
  title: string
  artistName?: string
  coverUrl?: string
  fileUrl?: string
  albumTitle?: string
  releaseTime?: string
  duration?: number
}

interface AlbumVo {
  id: number
  title: string
  coverUrl?: string
  artistName?: string
  releaseDate?: string
  description?: string
}

const router = useRouter()
const playerStore = usePlayerStore()
const activeTab = ref<'songs' | 'albums'>('songs')
const songs = ref<SongVo[]>([])
const albums = ref<AlbumVo[]>([])
const loading = ref(true)

const highlightSongs = computed(() => songs.value.slice(0, 4))
const moreSongs = computed(() => songs.value.slice(4))

const formatDate = (value?: string) => {
  if (!value) return '待上线'
  const date = new Date(value)
  return new Intl.DateTimeFormat('zh-CN', { month: 'short', day: 'numeric' }).format(date)
}

const formatDuration = (seconds?: number) => {
  if (!seconds) return '--:--'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const playSong = (song: SongVo) => { playerStore.setSong(song as any) }
const goSongDetail = (id: number) => router.push(`/song/${id}`)
const goAlbumDetail = (id: number) => router.push(`/album/${id}`)

const fetchData = async () => {
  loading.value = true
  try {
    const [songRes, albumRes] = await Promise.all([
      request.get('/song/new', { params: { limit: 30 } }),
      request.get('/album/new', { params: { limit: 20 } })
    ])
    songs.value = songRes.data || []
    albums.value = albumRes.data || []
  } catch (error) {
    console.error('加载新发行失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="new-release-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">新歌速递与预告</p>
        <h1>最新发行</h1>
        <p class="subtitle">集中展示刚刚发布的单曲与专辑，第一时间把握流行脉搏。</p>
      </div>
      <div class="tab-group">
        <button class="tab-btn" :class="{ active: activeTab === 'songs' }" @click="activeTab = 'songs'">最新单曲</button>
        <button class="tab-btn" :class="{ active: activeTab === 'albums' }" @click="activeTab = 'albums'">全新专辑</button>
      </div>
    </div>

    <div v-if="loading" class="skeleton-wrapper">
      <el-skeleton v-for="i in 6" :key="i" animated class="skeleton-card">
        <template #template>
          <el-skeleton-item variant="image" style="width:100%;height:140px;border-radius:12px" />
          <div style="padding: 12px;">
            <el-skeleton-item variant="text" style="width:70%;margin-bottom:8px;" />
            <el-skeleton-item variant="text" style="width:50%;" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <template v-else>
      <section v-if="activeTab === 'songs'">
        <div class="highlight-grid" v-if="highlightSongs.length">
          <div
            v-for="song in highlightSongs"
            :key="song.id"
            class="highlight-card"
            @click="goSongDetail(song.id)"
          >
            <div class="highlight-top">
              <div class="pill">{{ formatDate(song.releaseTime) }}</div>
              <button class="icon" @click.stop="playSong(song)">
                <svg role="img" height="24" width="24" viewBox="0 0 24 24" fill="black">
                  <path d="M7.05 3.606l13.49 7.788a.7.7 0 010 1.212L7.05 20.394A.7.7 0 016 19.788V4.212a.7.7 0 011.05-.606z" />
                </svg>
              </button>
            </div>
            <div class="cover-block">
              <img :src="song.coverUrl || 'https://placehold.co/240x240?text=Song'" class="cover" />
            </div>
            <div class="meta">
              <div class="title">{{ song.title }}</div>
              <div class="artist">{{ song.artistName || '未知艺人' }}</div>
              <div class="album" v-if="song.albumTitle">收录于 {{ song.albumTitle }}</div>
            </div>
          </div>
        </div>

        <div class="list-block" v-if="moreSongs.length">
          <div class="list-header">
            <h3>更多新歌</h3>
            <span class="hint">按发布时间降序排列</span>
          </div>
          <div class="song-list">
            <div
              v-for="song in moreSongs"
              :key="song.id"
              class="song-row"
              @click="goSongDetail(song.id)"
            >
              <div class="song-info">
                <img :src="song.coverUrl || 'https://placehold.co/64x64?text=Song'" class="thumb" />
                <div>
                  <div class="song-title">{{ song.title }}</div>
                  <div class="song-sub">{{ song.artistName || '未知艺人' }} · {{ song.albumTitle || '单曲' }}</div>
                </div>
              </div>
              <div class="song-meta">
                <span class="pill muted">{{ formatDate(song.releaseTime) }}</span>
                <span class="duration">{{ formatDuration(song.duration) }}</span>
                <button class="play" @click.stop="playSong(song)">播放</button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section v-else>
        <div class="album-grid">
          <div
            v-for="album in albums"
            :key="album.id"
            class="album-card"
            @click="goAlbumDetail(album.id)"
          >
            <div class="album-cover-wrap">
              <img :src="album.coverUrl || 'https://placehold.co/240x240?text=Album'" class="album-cover" />
              <div class="pill">{{ formatDate(album.releaseDate) }}</div>
            </div>
            <div class="album-meta">
              <div class="album-title">{{ album.title }}</div>
              <div class="album-artist">{{ album.artistName || '未知艺人' }}</div>
              <div class="album-desc">{{ album.description || '暂无简介' }}</div>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.new-release-page {
  color: white;
  padding-top: 16px;
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.eyebrow {
  color: #1db954;
  letter-spacing: 2px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  margin-bottom: 4px;
}

h1 {
  font-size: 36px;
  margin: 0;
  font-weight: 800;
}

.subtitle {
  color: #b3b3b3;
  margin-top: 8px;
  max-width: 520px;
}

.tab-group {
  display: flex;
  gap: 12px;
}

.tab-btn {
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid #333;
  background: #181818;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background: #1db954;
  color: black;
  border-color: #1db954;
  box-shadow: 0 12px 30px rgba(29, 185, 84, 0.35);
}

.skeleton-wrapper {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.skeleton-card {
  background: #181818;
  border-radius: 12px;
  overflow: hidden;
}

.highlight-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

.highlight-card {
  background: linear-gradient(145deg, #1f1f1f, #121212);
  border-radius: 14px;
  padding: 16px;
  border: 1px solid #2c2c2c;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.highlight-card:hover {
  transform: translateY(-4px);
  border-color: #1db954;
}

.highlight-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pill {
  background: rgba(29, 185, 84, 0.15);
  color: #1db954;
  border: 1px solid rgba(29, 185, 84, 0.3);
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  font-weight: 700;
  width: fit-content;
}

.pill.muted {
  background: rgba(255, 255, 255, 0.06);
  color: #e0e0e0;
  border-color: transparent;
}

.icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: #1db954;
  display: grid;
  place-items: center;
  cursor: pointer;
  box-shadow: 0 8px 16px rgba(29, 185, 84, 0.25);
}

.cover-block {
  margin: 14px 0;
}

.cover {
  width: 100%;
  border-radius: 12px;
  object-fit: cover;
  aspect-ratio: 1;
  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.45);
}

.meta .title {
  font-weight: 800;
  font-size: 18px;
}

.meta .artist {
  color: #b3b3b3;
  margin-top: 4px;
}

.meta .album {
  color: #8f8f8f;
  margin-top: 6px;
  font-size: 13px;
}

.list-block {
  background: #121212;
  border-radius: 14px;
  border: 1px solid #1f1f1f;
  padding: 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.list-header h3 {
  margin: 0;
  font-size: 18px;
}

.hint {
  color: #8f8f8f;
  font-size: 13px;
}

.song-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.song-row {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  background: #181818;
  border: 1px solid transparent;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.song-row:hover {
  border-color: #1db954;
  background: #1f1f1f;
}

.song-info {
  display: flex;
  gap: 12px;
  align-items: center;
}

.thumb {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  object-fit: cover;
}

.song-title {
  font-weight: 700;
}

.song-sub {
  color: #8f8f8f;
  font-size: 13px;
  margin-top: 2px;
}

.song-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.duration {
  color: #b3b3b3;
  font-size: 13px;
}

.play {
  background: #1db954;
  border: none;
  color: black;
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  font-weight: 700;
}

.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}

.album-card {
  background: #181818;
  border-radius: 14px;
  border: 1px solid #222;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.album-card:hover {
  transform: translateY(-4px);
  border-color: #1db954;
}

.album-cover-wrap {
  position: relative;
}

.album-cover {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}

.album-cover-wrap .pill {
  position: absolute;
  left: 12px;
  bottom: 12px;
}

.album-meta {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.album-title {
  font-weight: 800;
}

.album-artist {
  color: #b3b3b3;
}

.album-desc {
  color: #8f8f8f;
  font-size: 13px;
  line-height: 1.4;
}
</style>
