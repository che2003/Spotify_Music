<script setup lang="ts">
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { usePlayerStore } from '@/stores/player'
import { ElMessage } from 'element-plus'

type SearchResult = {
  songs: any[]
  artists: any[]
  albums: any[]
  playlists: any[]
}

const defaultResults: SearchResult = { songs: [], artists: [], albums: [], playlists: [] }

const keyword = ref('')
const searchResults = ref<SearchResult>({ ...defaultResults })
const activeTab = ref('all') // all, songs, artists, albums, playlists
const searched = ref(false)
const playerStore = usePlayerStore()

const handleSearch = async () => {
  if (!keyword.value) return
  try {
    const res = await request.get(`/search?keyword=${keyword.value}`)
    const data = res.data || {}
    searchResults.value = {
      songs: data.songs || [],
      artists: data.artists || [],
      albums: data.albums || [],
      playlists: data.playlists || [],
    }
    searched.value = true
  } catch (error) { console.error(error) }
}

const hasResult = computed(() =>
  searchResults.value.songs.length > 0 ||
  searchResults.value.artists.length > 0 ||
  searchResults.value.albums.length > 0 ||
  searchResults.value.playlists.length > 0
)

const playSong = (song: any) => { playerStore.setSong(song) }
const playNext = (song: any) => {
  playerStore.enqueueNext(song)
  ElMessage.success('已加入下一首播放')
}
const formatDuration = (duration?: number) => {
  if (!duration && duration !== 0) return '--:--'
  const minutes = Math.floor(duration / 60)
  const seconds = (duration % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
}
</script>

<template>
  <div class="page-shell search-container">
    <div class="section-card search-header">
      <div class="section-heading">
        <div class="heading-text">
          <span class="heading-eyebrow">Search · 找找看</span>
          <h2 class="heading-title">发现你想听的</h2>
          <p class="heading-subtitle">输入关键词即可在歌曲、艺人、专辑与歌单里极速匹配。</p>
        </div>
        <div class="pill-badge">
          <svg role="img" height="16" width="16" viewBox="0 0 24 24" fill="#1db954"><path d="M15.7 14.3a6 6 0 10-1.4 1.4l4 4a1 1 0 001.4-1.4l-4-4zM14 10a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
          实时结果
        </div>
      </div>

      <div class="search-controls">
        <el-input
            v-model="keyword"
            placeholder="想听什么？"
            size="large"
            class="search-bar"
            @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><i class="el-icon-search"></i></el-icon></template>
        </el-input>
        <div class="filter-tags">
          <span :class="{active: activeTab==='all'}" @click="activeTab='all'">全部</span>
          <span :class="{active: activeTab==='songs'}" @click="activeTab='songs'">歌曲</span>
          <span :class="{active: activeTab==='artists'}" @click="activeTab='artists'">艺人</span>
          <span :class="{active: activeTab==='albums'}" @click="activeTab='albums'">专辑</span>
          <span :class="{active: activeTab==='playlists'}" @click="activeTab='playlists'">歌单</span>
        </div>
      </div>
    </div>

    <div v-if="hasResult" class="section-card results-card">
      <template v-if="activeTab==='all'">
        <div v-if="searchResults.songs.length" class="section-block glass-panel">
          <div class="section-header">
            <div class="title">歌曲</div>
            <div class="count">{{ searchResults.songs.length }} 条结果</div>
          </div>
          <el-table :data="searchResults.songs" stripe style="width: 100%">
            <el-table-column type="index" width="50" />
            <el-table-column label="封面" width="80">
              <template #default="scope">
                <img :src="scope.row.coverUrl" class="cover" />
              </template>
            </el-table-column>
            <el-table-column prop="title" label="歌曲" />
            <el-table-column prop="artistName" label="艺人" />
            <el-table-column prop="duration" label="时长" width="100">
              <template #default="scope">{{ formatDuration(scope.row.duration) }}</template>
            </el-table-column>
            <el-table-column width="150" label="操作">
              <template #default="scope">
                <el-button circle type="success" size="small" @click="playSong(scope.row)">▶</el-button>
                <el-button circle type="primary" size="small" @click="playNext(scope.row)">➜</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div v-if="searchResults.artists.length" class="section-block glass-panel">
          <div class="section-header">
            <div class="title">艺人</div>
            <div class="count">{{ searchResults.artists.length }} 位艺人</div>
          </div>
          <div class="card-grid">
            <div v-for="artist in searchResults.artists" :key="artist.id" class="info-card">
              <img :src="artist.avatarUrl" class="avatar" alt="artist avatar" />
              <div class="card-title">{{ artist.name }}</div>
              <p class="card-desc">{{ artist.bio || '这位艺人很低调，还没有简介~' }}</p>
            </div>
          </div>
        </div>

        <div v-if="searchResults.albums.length" class="section-block glass-panel">
          <div class="section-header">
            <div class="title">专辑</div>
            <div class="count">{{ searchResults.albums.length }} 张专辑</div>
          </div>
          <div class="card-grid">
            <div v-for="album in searchResults.albums" :key="album.id" class="info-card">
              <img :src="album.coverUrl" class="avatar" alt="album cover" />
              <div class="card-title">{{ album.title }}</div>
              <p class="card-meta">{{ album.artistName || '未知艺人' }}</p>
              <p class="card-desc">{{ album.description || '这张专辑还没有描述~' }}</p>
            </div>
          </div>
        </div>

        <div v-if="searchResults.playlists.length" class="section-block glass-panel">
          <div class="section-header">
            <div class="title">歌单</div>
            <div class="count">{{ searchResults.playlists.length }} 个歌单</div>
          </div>
          <div class="card-grid">
            <div v-for="playlist in searchResults.playlists" :key="playlist.id" class="info-card">
              <img :src="playlist.coverUrl" class="avatar" alt="playlist cover" />
              <div class="card-title">{{ playlist.title }}</div>
              <p class="card-desc">{{ playlist.description || '这个歌单还没有描述~' }}</p>
            </div>
          </div>
        </div>
      </template>

      <template v-else-if="activeTab==='songs'">
        <div class="table-surface">
          <el-table :data="searchResults.songs" stripe style="width: 100%">
            <el-table-column type="index" width="50" />
            <el-table-column label="封面" width="80">
              <template #default="scope">
                <img :src="scope.row.coverUrl" class="cover" />
              </template>
            </el-table-column>
            <el-table-column prop="title" label="歌曲" />
            <el-table-column prop="artistName" label="艺人" />
            <el-table-column prop="duration" label="时长" width="100">
              <template #default="scope">{{ formatDuration(scope.row.duration) }}</template>
            </el-table-column>
            <el-table-column width="150" label="操作">
              <template #default="scope">
                <el-button circle type="success" size="small" @click="playSong(scope.row)">▶</el-button>
                <el-button circle type="primary" size="small" @click="playNext(scope.row)">➜</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>

      <template v-else-if="activeTab==='artists'">
        <div class="card-grid">
          <div v-for="artist in searchResults.artists" :key="artist.id" class="info-card">
            <img :src="artist.avatarUrl" class="avatar" alt="artist avatar" />
            <div class="card-title">{{ artist.name }}</div>
            <p class="card-desc">{{ artist.bio || '这位艺人很低调，还没有简介~' }}</p>
          </div>
        </div>
      </template>

      <template v-else-if="activeTab==='albums'">
        <div class="card-grid">
          <div v-for="album in searchResults.albums" :key="album.id" class="info-card">
            <img :src="album.coverUrl" class="avatar" alt="album cover" />
            <div class="card-title">{{ album.title }}</div>
            <p class="card-meta">{{ album.artistName || '未知艺人' }}</p>
            <p class="card-desc">{{ album.description || '这张专辑还没有描述~' }}</p>
          </div>
        </div>
      </template>

      <template v-else-if="activeTab==='playlists'">
        <div class="card-grid">
          <div v-for="playlist in searchResults.playlists" :key="playlist.id" class="info-card">
            <img :src="playlist.coverUrl" class="avatar" alt="playlist cover" />
            <div class="card-title">{{ playlist.title }}</div>
            <p class="card-desc">{{ playlist.description || '这个歌单还没有描述~' }}</p>
          </div>
        </div>
      </template>
    </div>
    <div v-else-if="searched" class="glass-panel empty-hint">没有找到相关结果，试试别的关键词吧~</div>
  </div>
</template>

<style scoped>
.search-container { gap: 16px; }
.search-header { margin-bottom: 4px; }
.search-controls { display: flex; flex-direction: column; gap: 14px; }
.search-bar {
  max-width: 400px;
  --el-input-bg-color: #242424;
  --el-input-border-color: transparent;
  --el-input-text-color: white;
}
.filter-tags { display: flex; gap: 10px; margin-bottom: 20px; }
.filter-tags span {
  padding: 6px 16px;
  background: #1b1b1b;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.2s;
  border: 1px solid #2f2f2f;
  color: #e5e5e5;
}
.filter-tags span.active, .filter-tags span:hover {
  background: linear-gradient(135deg, #1db954, #1ed760);
  color: #0b210f;
  box-shadow: 0 4px 10px rgba(29, 185, 84, 0.25);
  border-color: transparent;
}

.results-card { display: flex; flex-direction: column; gap: 16px; }
.section-block { padding: 16px; border-radius: 12px; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.section-header .title { color: white; font-weight: 600; }
.section-header .count { color: #8c8c8c; font-size: 13px; }

.cover { width: 40px; height: 40px; border-radius: 4px; object-fit: cover; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 14px; }
.info-card {
  background: #1a1a1a;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #262626;
  color: #c9c9c9;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.info-card .avatar {
  width: 100%;
  height: 140px;
  object-fit: cover;
  border-radius: 10px;
}
.info-card .card-title { color: white; font-weight: 600; font-size: 15px; }
.info-card .card-meta { color: #8c8c8c; font-size: 12px; }
.info-card .card-desc { color: #a6a6a6; font-size: 13px; line-height: 1.4; }

.empty-hint { margin-top: 40px; color: #8c8c8c; text-align: center; }

/* Table 适配 */
:deep(.el-table) { background: transparent; --el-table-tr-bg-color: transparent; --el-table-header-bg-color: transparent; color: #b3b3b3; --el-table-row-hover-bg-color: #242424; --el-table-border-color: transparent; }
:deep(.el-table th) { border-bottom: 1px solid #333; color: white; }
:deep(.el-table td) { border-bottom: none; }
:deep(.el-table__body tr td) { background-color: #151515; transition: background-color 0.2s ease; }
:deep(.el-table__body tr.el-table__row--striped td) { background-color: #1d1d1d; }
:deep(.el-table__body tr:hover > td) { background-color: #242424 !important; }
:deep(.el-input__wrapper) { border-radius: 50px; }
</style>