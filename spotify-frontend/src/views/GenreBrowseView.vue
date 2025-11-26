<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { usePlayerStore } from '@/stores/player'

interface GenreItem {
  id: number
  name: string
}

interface SongVo {
  id: number
  title: string
  artistName: string
  artistId?: number
  coverUrl: string
  duration?: number
}

interface GenrePanel {
  id: number
  name: string
  songCount: number
  songs: SongVo[]
}

const keyword = ref('')
const genreList = ref<GenreItem[]>([])
const moodPresets = ['清晨通勤', '轻松小憩', '夜晚助眠', '专注学习', '派对活力', '旅行公路']
const genrePanels = ref<GenrePanel[]>([])
const activeGenreId = ref<number | null>(null)
const activeSongs = ref<SongVo[]>([])
const loadingGenres = ref(false)
const loadingPanels = ref(false)
const loadingSongs = ref(false)

const playerStore = usePlayerStore()
const router = useRouter()

const activeGenreName = computed(() => {
  return genrePanels.value.find((item) => item.id === activeGenreId.value)?.name || '精选歌单'
})

const filteredGenres = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return genreList.value
  return genreList.value.filter((genre) => genre.name.toLowerCase().includes(kw))
})

const formatDuration = (seconds?: number) => {
  if (!seconds && seconds !== 0) return '--:--'
  const mins = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return `${mins}:${secs.toString().padStart(2, '0')}`
}

const fetchGenreList = async () => {
  loadingGenres.value = true
  try {
    const res = await request.get('/genre/list')
    genreList.value = res.data || []
    if (!activeGenreId.value && genreList.value.length) {
      setActiveGenre(genreList.value[0].id)
    }
  } catch (e) {
    console.error(e)
  } finally {
    loadingGenres.value = false
  }
}

const fetchPanels = async () => {
  loadingPanels.value = true
  try {
    const res = await request.get('/genre/browse', {
      params: {
        keyword: keyword.value.trim() || undefined,
        songLimit: 4
      }
    })
    genrePanels.value = res.data || []

    if (!activeGenreId.value && genrePanels.value.length > 0) {
      setActiveGenre(genrePanels.value[0].id)
    } else {
      activeGenreId.value = null
      activeSongs.value = []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loadingPanels.value = false
  }
}

const fetchGenreSongs = async (genreId: number) => {
  loadingSongs.value = true
  try {
    const res = await request.get(`/genre/${genreId}/songs`, { params: { limit: 30 } })
    activeSongs.value = res.data || []
  } catch (e) {
    console.error(e)
    activeSongs.value = []
  } finally {
    loadingSongs.value = false
  }
}

const setActiveGenre = (genreId: number) => {
  if (activeGenreId.value === genreId) return
  activeGenreId.value = genreId
  fetchGenreSongs(genreId)
}

const onSearch = () => fetchPanels()
const applyMood = (mood: string) => {
  keyword.value = mood
  fetchPanels()
}

const playSong = (song: SongVo) => playerStore.setSong(song)
const goSongDetail = (id: number) => router.push(`/song/${id}`)
const goArtistDetail = (id?: number) => { if (id) router.push(`/artist/${id}`) }

onMounted(() => {
  fetchGenreList()
  fetchPanels()
})
</script>

<template>
  <div class="genre-page">
    <div class="page-header">
      <div>
        <p class="eyebrow">分类浏览</p>
        <h1>按照流派与心情探索音乐</h1>
        <p class="subtext">使用流派或心情标签快速筛选，发现与你气质相符的歌曲。</p>
      </div>
      <div class="search-area">
        <el-input
          v-model="keyword"
          placeholder="搜索流派 / 心情，例如：摇滚、轻松、古典"
          clearable
          @keyup.enter="onSearch"
          @clear="fetchPanels"
        >
          <template #append>
            <el-button type="success" @click="onSearch">探索</el-button>
          </template>
        </el-input>
        <div class="mood-tags">
          <span class="tag-label">推荐标签：</span>
          <el-tag
            v-for="mood in moodPresets"
            :key="mood"
            effect="dark"
            type="info"
            size="large"
            class="mood-tag"
            @click="applyMood(mood)"
          >
            {{ mood }}
          </el-tag>
        </div>
      </div>
    </div>

    <div class="section-head">
      <div>
        <p class="eyebrow">全部流派</p>
        <h2>选择一个流派开始探索</h2>
      </div>
    </div>

    <div class="genre-grid" v-loading="loadingGenres">
      <div
        v-for="genre in filteredGenres"
        :key="genre.id"
        class="genre-card"
        :class="{ active: genre.id === activeGenreId }"
        @click="setActiveGenre(genre.id)"
      >
        <div class="genre-avatar">{{ genre.name.charAt(0).toUpperCase() }}</div>
        <div class="genre-name">{{ genre.name }}</div>
        <el-tag size="small" effect="dark" type="success">流派</el-tag>
      </div>
      <div v-if="!filteredGenres.length && !loadingGenres" class="empty-state">暂无匹配的流派</div>
    </div>

    <div class="panel-grid" v-loading="loadingPanels">
      <div
        v-for="panel in genrePanels"
        :key="panel.id"
        class="panel-card"
        :class="{ active: panel.id === activeGenreId }"
        @click="setActiveGenre(panel.id)"
      >
        <div class="panel-head">
          <div>
            <p class="panel-label">{{ panel.songCount }} 首歌曲</p>
            <h3>{{ panel.name }}</h3>
          </div>
          <el-tag type="success" effect="dark">精选</el-tag>
        </div>
        <div class="preview-list">
          <div v-for="song in panel.songs" :key="song.id" class="preview-item">
            <img :src="song.coverUrl" class="preview-cover" />
            <div class="preview-meta">
              <div class="preview-title">{{ song.title }}</div>
              <div class="preview-artist">{{ song.artistName }}</div>
            </div>
          </div>
          <div v-if="panel.songs.length === 0" class="empty-text">该流派暂未收录歌曲</div>
        </div>
      </div>
      <div v-if="!genrePanels.length && !loadingPanels" class="empty-state">暂无匹配的流派</div>
    </div>

    <div v-if="activeGenreId" class="song-section">
      <div class="section-head">
        <div>
          <p class="eyebrow">精选歌曲</p>
          <h2>{{ activeGenreName }}</h2>
        </div>
        <el-button plain @click="fetchGenreSongs(activeGenreId)">刷新推荐</el-button>
      </div>

      <el-skeleton v-if="loadingSongs" animated :count="4" class="skeleton-row">
        <template #template>
          <div class="song-row">
            <el-skeleton-item variant="image" style="width:48px; height:48px;" />
            <div class="song-meta">
              <el-skeleton-item variant="text" style="width:60%;" />
              <el-skeleton-item variant="text" style="width:40%;" />
            </div>
            <el-skeleton-item variant="text" style="width:80px;" />
          </div>
        </template>
      </el-skeleton>

      <div v-else class="song-table">
        <div v-for="song in activeSongs" :key="song.id" class="song-row">
          <div class="song-info" @click="goSongDetail(song.id)">
            <img :src="song.coverUrl" class="song-cover" />
            <div class="song-meta">
              <div class="song-title">{{ song.title }}</div>
              <div class="song-artist" @click.stop="goArtistDetail(song.artistId)">{{ song.artistName }}</div>
            </div>
          </div>
          <div class="song-actions">
            <span class="duration">{{ formatDuration(song.duration) }}</span>
            <el-button type="success" size="small" @click.stop="playSong(song)">播放</el-button>
          </div>
        </div>
        <div v-if="!activeSongs.length" class="empty-state">该流派暂无歌曲，试试其他标签吧。</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.genre-page {
  color: #fff;
  padding: 8px 0 48px;
}
.page-header {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: center;
  margin-bottom: 28px;
}
.eyebrow {
  color: #1db954;
  letter-spacing: 0.4px;
  font-size: 13px;
  margin-bottom: 6px;
}
h1 {
  font-size: 30px;
  margin: 0;
  line-height: 1.2;
}
h2 {
  margin: 0;
}
.subtext {
  color: #b3b3b3;
  margin-top: 8px;
}
.search-area {
  background: #161616;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.35);
}
.mood-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
.mood-tag {
  cursor: pointer;
}
.tag-label {
  color: #b3b3b3;
  font-size: 13px;
}
.panel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}
.genre-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}
.genre-card {
  background: linear-gradient(135deg, #1f1f1f 0%, #161616 100%);
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.genre-card:hover {
  transform: translateY(-2px);
  border-color: #2b2b2b;
}
.genre-card.active {
  border-color: #1db954;
  box-shadow: 0 10px 30px rgba(29, 185, 84, 0.18);
}
.genre-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: rgba(29, 185, 84, 0.16);
  color: #1db954;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}
.genre-name {
  font-weight: 700;
  font-size: 16px;
}
.panel-card {
  background: #181818;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.25s ease;
  min-height: 220px;
}
.panel-card:hover {
  border-color: #2b2b2b;
  transform: translateY(-2px);
}
.panel-card.active {
  border-color: #1db954;
  box-shadow: 0 10px 30px rgba(29, 185, 84, 0.18);
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: start;
  margin-bottom: 12px;
}
.panel-label {
  color: #8f8f8f;
  font-size: 13px;
  margin: 0 0 6px;
}
.preview-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.preview-item {
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 10px;
  align-items: center;
}
.preview-cover {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}
.preview-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.preview-title {
  font-weight: 700;
}
.preview-artist {
  color: #9f9f9f;
  font-size: 13px;
}
.song-section {
  background: #111;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.35);
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.song-table {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.song-row {
  display: grid;
  grid-template-columns: 1fr auto;
  align-items: center;
  background: #1a1a1a;
  padding: 10px 12px;
  border-radius: 10px;
  transition: background 0.2s ease;
}
.song-row:hover {
  background: #222;
}
.song-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.song-cover {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.35);
}
.song-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.song-title {
  font-weight: 700;
  cursor: pointer;
}
.song-artist {
  color: #9f9f9f;
  font-size: 13px;
  cursor: pointer;
}
.song-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.duration {
  color: #b3b3b3;
  font-size: 13px;
}
.empty-state {
  color: #8f8f8f;
  text-align: center;
  padding: 16px 0;
}
.empty-text {
  color: #7f7f7f;
  font-size: 13px;
}
.skeleton-row .song-row {
  grid-template-columns: 48px 1fr;
}
@media (max-width: 960px) {
  .page-header {
    grid-template-columns: 1fr;
  }
}
</style>
