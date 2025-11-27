<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { usePlayerStore } from '@/stores/player'
import { ElMessage } from 'element-plus'

interface ChartSong {
  id: number
  title: string
  artistName: string
  artistId?: number
  albumId?: number
  albumTitle?: string
  coverUrl: string
  fileUrl: string
  playCount: number
}

const router = useRouter()
const playerStore = usePlayerStore()

const rangeOptions = [
  { label: '飙升周榜', value: 7, description: '过去 7 天热度飙升' },
  { label: '流行月榜', value: 30, description: '近 30 天累计播放' },
  { label: '全站总榜', value: null as number | null, description: '历史累计热度' }
]

const activeRange = ref<number | null>(7)
const songs = ref<ChartSong[]>([])
const loading = ref(false)
const error = ref('')

const subtitle = computed(() => {
  const target = rangeOptions.find((item) => item.value === activeRange.value)
  return target ? target.description : '全站热度趋势'
})

const fetchCharts = async () => {
  loading.value = true
  error.value = ''
  try {
    const params: Record<string, any> = { limit: 50 }
    if (activeRange.value !== null) params.days = activeRange.value
    const res = await request.get('/charts/top-songs', { params })
    songs.value = res.data
  } catch (e) {
    console.error(e)
    error.value = '排行榜数据加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const changeRange = (range: number | null) => {
  if (activeRange.value === range) return
  activeRange.value = range
  fetchCharts()
}

const playSong = (song: ChartSong) => {
  playerStore.setSong(song as any)
}

const playNext = (song: ChartSong) => {
  playerStore.enqueueNext(song as any)
  ElMessage.success('已添加为下一首')
}

const goSongDetail = (id: number) => router.push(`/song/${id}`)
const goArtistDetail = (id?: number) => { if (id) router.push(`/artist/${id}`) }

onMounted(fetchCharts)
</script>

<template>
  <div class="charts-page">
    <header class="page-header">
      <div>
        <div class="eyebrow">Charts · 热门趋势</div>
        <h2 class="title">实时榜单</h2>
        <p class="subtitle">{{ subtitle }}</p>
      </div>
      <div class="range-group">
        <el-button
            v-for="item in rangeOptions"
            :key="item.label"
            size="small"
            :type="item.value === activeRange ? 'success' : 'default'"
            plain
            @click="changeRange(item.value)"
        >
          {{ item.label }}
        </el-button>
      </div>
    </header>

    <section class="chart-card">
      <div class="card-header">
        <div>
          <div class="card-eyebrow">全站播放历史实时聚合</div>
          <h3 class="card-title">热门单曲 Top {{ songs.length || 50 }}</h3>
        </div>
        <div class="card-meta">数据来源：PlayHistory 表 · 自动按播放量排序</div>
      </div>

      <el-skeleton v-if="loading" :rows="6" animated class="skeleton-list"/>
      <div v-else>
        <div v-if="error" class="error-box">{{ error }}</div>
        <div v-else class="chart-list">
          <div
              v-for="(song, index) in songs"
              :key="song.id"
              class="chart-row"
              @click="goSongDetail(song.id)"
          >
            <div class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</div>
            <img :src="song.coverUrl || 'https://placehold.co/64x64'" class="cover" />
            <div class="info">
              <div class="title-line">
                <span class="song-title">{{ song.title }}</span>
                <el-tag v-if="index < 3" type="success" size="small" effect="dark" round>HOT</el-tag>
              </div>
              <div class="meta">
                <span class="artist" @click.stop="goArtistDetail(song.artistId)">{{ song.artistName || '未知歌手' }}</span>
                <span v-if="song.albumTitle" class="album">· {{ song.albumTitle }}</span>
              </div>
            </div>
            <div class="plays">{{ song.playCount?.toLocaleString() }} 播放</div>
            <div class="row-actions">
              <el-button size="small" type="success" plain @click.stop="playSong(song)">播放</el-button>
              <el-button size="small" type="primary" plain @click.stop="playNext(song)">下一首</el-button>
            </div>
          </div>
          <div v-if="!songs.length" class="empty">暂无播放数据</div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.charts-page { padding: 16px; color: white; }
.page-header { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 20px; }
.eyebrow { color: #1db954; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; font-size: 12px; }
.title { font-size: 32px; margin: 4px 0; }
.subtitle { color: #b3b3b3; margin: 0; }
.range-group { display: flex; gap: 8px; }

.chart-card { background: #181818; border-radius: 12px; padding: 20px; box-shadow: 0 8px 24px rgba(0,0,0,0.3); }
.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.card-eyebrow { color: #b3b3b3; text-transform: uppercase; letter-spacing: 1px; font-size: 12px; }
.card-title { margin: 6px 0 0; font-size: 22px; }
.card-meta { color: #7d7d7d; font-size: 13px; }

.chart-list { display: flex; flex-direction: column; gap: 10px; }
.chart-row { display: grid; grid-template-columns: 40px 64px 1fr auto auto; align-items: center; gap: 12px; background: #202020; border-radius: 8px; padding: 12px; cursor: pointer; transition: background-color .2s ease; }
.chart-row:hover { background: #2a2a2a; }
.rank { font-weight: 800; font-size: 18px; color: #b3b3b3; text-align: center; }
.rank.top { color: #1db954; }
.cover { width: 64px; height: 64px; border-radius: 6px; object-fit: cover; }
.info { display: flex; flex-direction: column; gap: 6px; }
.title-line { display: flex; align-items: center; gap: 8px; }
.song-title { font-size: 16px; font-weight: 700; }
.meta { color: #b3b3b3; font-size: 13px; }
.artist { cursor: pointer; }
.artist:hover { color: white; }
.album { margin-left: 4px; }
.plays { color: #b3b3b3; font-size: 13px; }
.row-actions { display: flex; gap: 8px; }

.empty { color: #b3b3b3; text-align: center; padding: 20px 0; }
.error-box { color: #f56c6c; background: #2b1c1c; padding: 12px; border-radius: 8px; }

.skeleton-list { --el-skeleton-color: #2a2a2a; --el-skeleton-to-color: #333; }

@media (max-width: 768px) {
  .chart-row { grid-template-columns: 32px 56px 1fr; grid-template-rows: auto auto; gap: 8px; }
  .plays, .chart-row .el-button { justify-self: flex-end; }
}
</style>
