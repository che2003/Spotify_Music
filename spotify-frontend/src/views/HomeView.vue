<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { usePlayerStore } from '@/stores/player'

interface Banner {
  id: number
  title: string
  imageUrl: string
  targetUrl?: string
}

interface SongVo {
  id: number
  title: string
  artistName: string
  coverUrl: string
  fileUrl: string
  artistId?: number
}

const router = useRouter()
const playerStore = usePlayerStore()

const banners = ref<Banner[]>([])
const bannerLoading = ref(false)
const songList = ref<SongVo[]>([])
const hotSongs = ref<SongVo[]>([])
const latestSongs = ref<SongVo[]>([])
const latestAlbums = ref<any[]>([])
const genres = ref<any[]>([])
const genreSongs = ref<SongVo[]>([])
const activeGenreId = ref<number | null>(null)
const loading = ref(true)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 12) return '早上好'
  if (hour >= 12 && hour < 19) return '下午好'
  return '晚上好'
})

const heroSongs = computed(() => songList.value.slice(0, 6))
const recommendSongs = computed(() => songList.value.slice(6, 12))
const newReleases = computed(() => latestSongs.value.slice(0, 8))

const fetchBanners = async () => {
  bannerLoading.value = true
  try {
    const res = await request.get('/banner/list')
    banners.value = res.data || []
  } catch (error) {
    console.error('加载首页 Banner 失败', error)
  } finally {
    bannerLoading.value = false
  }
}

const fetchRecommendations = async () => {
  loading.value = true
  try {
    const res = await request.get('/recommend/daily')
    songList.value = res.data
  } catch (error) {
    console.error('获取推荐失败', error)
  } finally {
    loading.value = false
  }
}

const fetchHotSongs = async () => {
  try {
    const res = await request.get('/song/hot', { params: { limit: 10 } })
    hotSongs.value = res.data
  } catch (e) { console.error(e) }
}

const fetchLatestSongs = async () => {
  try {
    const res = await request.get('/song/new', { params: { limit: 12 } })
    latestSongs.value = res.data
  } catch (e) { console.error(e) }
}

const fetchLatestAlbums = async () => {
  try {
    const res = await request.get('/album/new', { params: { limit: 6 } })
    latestAlbums.value = res.data
  } catch (e) { console.error(e) }
}

const fetchGenres = async () => {
  try {
    const res = await request.get('/genre/list')
    genres.value = res.data
    if (genres.value.length) {
      activeGenreId.value = genres.value[0].id
      fetchGenreSongs(genres.value[0].id)
    }
  } catch (e) { console.error(e) }
}

const fetchGenreSongs = async (genreId: number) => {
  activeGenreId.value = genreId
  try {
    const res = await request.get(`/song/genre/${genreId}`, { params: { limit: 8 } })
    genreSongs.value = res.data
  } catch (e) { console.error(e) }
}

const playMusic = (song: SongVo) => { playerStore.setSong(song) }
const goToSongDetail = (id: number) => { router.push(`/song/${id}`) }
const handleBannerClick = (banner: Banner) => {
  if (banner.targetUrl) {
    window.open(banner.targetUrl, '_blank')
  }
}

onMounted(() => {
  fetchBanners()
  fetchRecommendations()
  fetchHotSongs()
  fetchLatestSongs()
  fetchLatestAlbums()
  fetchGenres()
})
</script>

<template>
  <div class="home-container">
    <div class="banner-section" v-if="banners.length">
      <el-carousel height="260px" type="card" trigger="click" indicator-position="outside">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div class="banner-card" @click="handleBannerClick(banner)">
            <img :src="banner.imageUrl" :alt="banner.title" />
            <div class="banner-mask">
              <div class="banner-title">{{ banner.title }}</div>
              <div v-if="banner.targetUrl" class="banner-link">点击了解更多</div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    <div class="banner-skeleton" v-else-if="bannerLoading">
      <el-skeleton animated :rows="3" style="width: 100%;" />
    </div>

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

      <div class="section" v-if="hotSongs.length">
        <div class="section-header">
          <h2 class="section-title">热门榜单</h2>
        </div>
        <div class="chart-list">
          <div v-for="(song, index) in hotSongs" :key="song.id" class="chart-item" @click="goToSongDetail(song.id)">
            <div class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</div>
            <img :src="song.coverUrl" class="chart-cover" />
            <div class="chart-info">
              <div class="chart-title">{{ song.title }}</div>
              <div class="chart-artist">{{ song.artistName }}</div>
            </div>
            <el-button size="small" type="success" @click.stop="playMusic(song)">播放</el-button>
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

      <div class="section" v-if="latestAlbums.length">
        <div class="section-header">
          <h2 class="section-title">全新专辑</h2>
        </div>
        <div class="album-grid">
          <div v-for="album in latestAlbums" :key="album.id" class="album-card" @click="router.push(`/album/${album.id}`)">
            <img :src="album.coverUrl || 'https://placehold.co/180x180?text=Album'" class="album-cover" />
            <div class="album-meta">
              <div class="album-title">{{ album.title }}</div>
              <div class="album-artist">{{ album.artistName || '未知艺人' }}</div>
              <div class="album-date">{{ album.releaseDate || '发行时间未知' }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="section" v-if="genres.length">
        <div class="section-header">
          <h2 class="section-title">流派探索</h2>
          <div class="genre-tags">
            <el-tag
                v-for="genre in genres"
                :key="genre.id"
                :type="genre.id === activeGenreId ? 'success' : 'info'"
                effect="dark"
                class="genre-tag"
                @click="fetchGenreSongs(genre.id)"
            >
              {{ genre.name }}
            </el-tag>
          </div>
        </div>
        <div class="song-grid">
          <div v-for="song in genreSongs" :key="song.id" class="song-card" @click="goToSongDetail(song.id)">
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
.home-container { padding-top: 24px; padding-bottom: 50px; }
.banner-section { margin-bottom: 24px; }
.banner-card { position: relative; width: 100%; height: 260px; border-radius: 16px; overflow: hidden; cursor: pointer; box-shadow: 0 18px 40px rgba(0,0,0,0.35); }
.banner-card img { width: 100%; height: 100%; object-fit: cover; display: block; }
.banner-mask { position: absolute; inset: 0; background: linear-gradient(90deg, rgba(0,0,0,0.55), rgba(0,0,0,0.05)); display: flex; flex-direction: column; justify-content: flex-end; padding: 20px; }
.banner-title { color: #fff; font-size: 22px; font-weight: 800; text-shadow: 0 4px 16px rgba(0,0,0,0.45); }
.banner-link { margin-top: 6px; color: #22c55e; font-weight: 600; }
.banner-skeleton { margin-bottom: 24px; }
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
.chart-list { display: flex; flex-direction: column; gap: 10px; }
.chart-item { display: grid; grid-template-columns: 40px 60px 1fr 80px; align-items: center; padding: 10px; background: #181818; border-radius: 8px; cursor: pointer; transition: background 0.2s; }
.chart-item:hover { background: #262626; }
.rank { font-weight: 900; color: #b3b3b3; font-size: 18px; }
.rank.top { color: #1db954; }
.chart-cover { width: 56px; height: 56px; border-radius: 6px; object-fit: cover; box-shadow: 0 4px 10px rgba(0,0,0,0.3); }
.chart-info { display: flex; flex-direction: column; gap: 4px; }
.chart-title { color: white; font-weight: 700; }
.chart-artist { color: #b3b3b3; font-size: 13px; }
.album-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 20px; }
.album-card { background: #181818; border-radius: 10px; overflow: hidden; transition: transform 0.2s, background 0.2s; cursor: pointer; }
.album-card:hover { transform: translateY(-4px); background: #262626; }
.album-cover { width: 100%; aspect-ratio: 1; object-fit: cover; }
.album-meta { padding: 12px; display: flex; flex-direction: column; gap: 4px; }
.album-title { color: white; font-weight: 700; }
.album-artist { color: #b3b3b3; font-size: 13px; }
.album-date { color: #8f8f8f; font-size: 12px; }
.genre-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.genre-tag { cursor: pointer; }
</style>
