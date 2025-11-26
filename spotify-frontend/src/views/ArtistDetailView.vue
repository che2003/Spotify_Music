<script setup lang="ts">

import { computed, ref, watch } from 'vue'
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
  playCount?: number
}

interface Artist {
  id: number
  name: string
  bio?: string
  avatarUrl?: string
}

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()
const loading = ref(true)
const artist = ref<Artist | null>(null)
const songs = ref<SongVo[]>([])
const isFollowing = ref(false)
const followLoading = ref(false)
let fetchToken = 0

const artistSongs = computed(() => songs.value.filter(song => song.artistId === Number(route.params.id)))
const albums = computed(() => {
  const map = new Map<number, { albumId: number; title: string; cover?: string; songs: SongVo[] }>()
  artistSongs.value.forEach(song => {
    if (!song.albumId) return
    if (!map.has(song.albumId)) {
      map.set(song.albumId, {
        albumId: song.albumId,
        title: song.albumTitle || `专辑 #${song.albumId}`,
        cover: song.albumCover || song.coverUrl,
        songs: []
      })
    }
    map.get(song.albumId)!.songs.push(song)
  })
  return Array.from(map.values())
})

const fetchData = async () => {
  const requestId = ++fetchToken
  loading.value = true
  artist.value = null
  songs.value = []
  isFollowing.value = false
  try {
    const [artistRes, songRes] = await Promise.all([
      request.get('/artist/list'),
      request.get('/song/list')
    ])


    if (requestId !== fetchToken) return

    const currentId = Number(route.params.id)
    artist.value = artistRes.data.find((item: Artist) => item.id === currentId) || null
    songs.value = songRes.data

    if (artist.value?.id) {
      await checkFollowStatus(artist.value.id)
    } else {
      ElMessage.error('未找到该艺人')
    }
  } catch (e) {
    if (requestId === fetchToken) {
      ElMessage.error('加载艺人信息失败')
    }
  } finally {
    if (requestId === fetchToken) {
      loading.value = false
    }
  }
}

const playSong = (song: SongVo) => playerStore.setSong(song)
const goAlbum = (albumId?: number) => { if (albumId) router.push(`/album/${albumId}`) }

const checkFollowStatus = async (artistId: number) => {
  try {
    const res = await request.get(`/follow/check?artistId=${artistId}`)
    if (artistId === artist.value?.id) {
      isFollowing.value = res.data
    }
  } catch (e) {}
}

const toggleFollow = async () => {
  if (!artist.value?.id) return
  followLoading.value = true
  try {
    await request.post(`/follow/artist?artistId=${artist.value.id}`)
    isFollowing.value = !isFollowing.value
    ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注')
  } catch (e) {
    ElMessage.error('操作失败 (可能艺人未入驻)')
  } finally {
    followLoading.value = false
  }
}

watch(
  () => route.params.id,
  () => {
    fetchData()
  },
  { immediate: true }
)

</script>

<template>
  <div class="artist-page">
    <div class="hero" v-if="artist">
      <img :src="artist.avatarUrl || 'https://placehold.co/200x200?text=Artist'" class="avatar" />
      <div class="hero-text">
        <p class="type-label">艺人</p>
        <h1 class="name">{{ artist.name }}</h1>
        <div class="actions">
          <el-button
              size="small"
              round
              class="follow-btn"
              :type="isFollowing ? 'default' : 'success'"
              plain
              :loading="followLoading"
              @click="toggleFollow"
          >
            {{ isFollowing ? '已关注' : '关注' }}
          </el-button>
        </div>
        <p class="bio" v-if="artist.bio">{{ artist.bio }}</p>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-text">未找到该艺人</div>

    <el-skeleton v-if="loading" :rows="6" animated />

    <div v-if="artistSongs.length" class="section">
      <div class="section-header">
        <h3>热门歌曲</h3>
      </div>
      <div class="song-grid">
        <div v-for="song in artistSongs" :key="song.id" class="song-card" @click="playSong(song)">
          <img :src="song.coverUrl" class="cover" />
          <div class="song-meta">
            <div class="title">{{ song.title }}</div>
            <div class="plays" v-if="song.playCount">播放 {{ song.playCount }}</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="albums.length" class="section">
      <div class="section-header">
        <h3>专辑</h3>
      </div>
      <div class="album-grid">
        <div v-for="album in albums" :key="album.albumId" class="album-card" @click="goAlbum(album.albumId)">
          <img :src="album.cover || 'https://placehold.co/240x240?text=Album'" class="cover" />
          <div class="album-info">
            <div class="title">{{ album.title }}</div>
            <div class="count">{{ album.songs.length }} 首歌曲</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.artist-page { color: white; padding: 10px 0 40px; }
.hero { display: flex; align-items: flex-end; gap: 24px; margin-bottom: 32px; }
.avatar { width: 200px; height: 200px; object-fit: cover; border-radius: 50%; box-shadow: 0 8px 30px rgba(0,0,0,0.35); }
.hero-text { display: flex; flex-direction: column; gap: 8px; }
.actions { display: flex; gap: 8px; align-items: center; }
.type-label { font-size: 12px; font-weight: 700; letter-spacing: 1px; text-transform: uppercase; opacity: 0.8; }
.name { font-size: 48px; font-weight: 900; margin: 0; letter-spacing: -1px; }
.bio { max-width: 720px; color: #b3b3b3; line-height: 1.6; }
.section { margin-top: 32px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.section-header h3 { margin: 0; font-size: 24px; font-weight: 800; }
.song-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; }
.song-card { background: #181818; padding: 12px; border-radius: 10px; cursor: pointer; transition: transform 0.2s, background 0.2s; }
.song-card:hover { background: #262626; transform: translateY(-3px); }
.song-card .cover { width: 100%; height: 180px; object-fit: cover; border-radius: 8px; box-shadow: 0 8px 16px rgba(0,0,0,0.4); }
.song-meta { margin-top: 8px; }
.song-meta .title { font-weight: 700; }
.song-meta .plays { color: #b3b3b3; font-size: 12px; margin-top: 2px; }
.album-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 18px; margin-top: 12px; }
.album-card { background: #181818; border-radius: 10px; padding: 12px; cursor: pointer; transition: transform 0.2s, background 0.2s; }
.album-card:hover { background: #262626; transform: translateY(-3px); }
.album-card .cover { width: 100%; height: 200px; object-fit: cover; border-radius: 8px; box-shadow: 0 8px 16px rgba(0,0,0,0.35); }
.album-info { margin-top: 8px; }
.album-info .title { font-weight: 800; font-size: 16px; }
.album-info .count { color: #b3b3b3; font-size: 12px; margin-top: 4px; }
.empty-text { color: #b3b3b3; margin-top: 20px; }
</style>
