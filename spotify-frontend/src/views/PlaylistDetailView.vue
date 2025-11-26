<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import SharePopover from '@/components/SharePopover.vue'
import { usePlayerStore } from '@/stores/player'

const route = useRoute()
const playlistId = computed(() => Number(route.params.id))
const playlist = ref<any>({})
const songs = ref<any[]>([])
const playerStore = usePlayerStore()
const draggingId = ref<number | null>(null)
const savingOrder = ref(false)

// ------------------------------------
// 动作 1: 获取歌单详情和歌曲
// ------------------------------------
const fetchPlaylistDetails = async () => {
  try {
    const playlistsRes = await request.get('/playlist/my')
    const currentPlaylist = playlistsRes.data.find((p: any) => p.id === playlistId.value)
    if (currentPlaylist) {
      playlist.value = currentPlaylist
    } else {
      ElMessage.error('歌单不存在')
      return
    }

    // 后端现在返回 SongVo，包含 artistName
    const songsRes = await request.get(`/playlist/${playlistId.value}/songs`)
    songs.value = songsRes.data

  } catch (error) {
    ElMessage.error('加载歌单详情失败')
    console.error(error)
  }
}

// ------------------------------------
// 动作 2: 移除歌曲
// ------------------------------------
const removeSong = (songId: number, title: string) => {
  ElMessageBox.confirm(
      `确定要从歌单《${playlist.value.title}》中移除歌曲【${title}】吗?`,
      '移除确认',
  ).then(async () => {
    try {
      await request.delete(`/playlist/removeSong?playlistId=${playlistId.value}&songId=${songId}`)
      ElMessage.success('歌曲移除成功！')
      fetchPlaylistDetails()
    } catch (error) {
      ElMessage.error('移除失败，可能无权限。')
    }
  }).catch(() => {
    // 取消操作
  })
}

// ------------------------------------
// 动作 3: 播放歌曲 (连接到全局播放器)
// ------------------------------------
const playSong = (song: any) => {
  playerStore.setSong(song)
}

const persistOrder = async () => {
  if (!songs.value.length) return
  savingOrder.value = true
  try {
    await request.post(`/playlist/${playlistId.value}/reorder`, songs.value.map(song => song.id))
    ElMessage.success('排序已更新')
  } catch (error) {
    ElMessage.error('保存排序失败')
    fetchPlaylistDetails()
  } finally {
    savingOrder.value = false
  }
}

const reorderSongs = (targetId: number) => {
  if (draggingId.value === null || draggingId.value === targetId) return

  const currentIndex = songs.value.findIndex(song => song.id === draggingId.value)
  const targetIndex = songs.value.findIndex(song => song.id === targetId)
  if (currentIndex === -1 || targetIndex === -1) return

  const updated = [...songs.value]
  const [moved] = updated.splice(currentIndex, 1)
  updated.splice(targetIndex, 0, moved)
  songs.value = updated
}

const onDragStart = (songId: number) => {
  draggingId.value = songId
}

const onDragEnter = (songId: number) => {
  reorderSongs(songId)
}

const onDragEnd = () => {
  if (draggingId.value !== null) {
    draggingId.value = null
    persistOrder()
  }
}


onMounted(() => {
  fetchPlaylistDetails()
})
</script>

<template>
  <div class="playlist-detail-container">
    <div class="header-section">
      <img :src="playlist.coverUrl || 'https://via.placeholder.com/150/1db954/FFFFFF?text=P'" class="playlist-cover" />
      <div class="info-text">
        <p class="type-label">歌单</p>
        <h1 class="playlist-title">{{ playlist.title }}</h1>
        <p class="playlist-desc">{{ playlist.description || '暂无描述' }}</p>
        <p class="playlist-creator">创建者: User #{{ playlist.creatorId }}</p>
        <div class="playlist-share-actions">
          <SharePopover
              v-if="playlist.id"
              resource-type="playlist"
              :resource-id="playlist.id"
              :title="playlist.title"
          />
        </div>
      </div>
    </div>

    <div class="songs-section">
      <div class="songs-title-row">
        <h3 class="songs-title">歌曲列表 (共 {{ songs.length }} 首)</h3>
        <div class="songs-hint">拖拽左侧手柄进行排序</div>
        <el-tag v-if="savingOrder" type="warning" effect="dark">保存中...</el-tag>
      </div>

      <div class="song-list">
        <div
            v-for="(song, index) in songs"
            :key="song.id"
            class="song-row"
            draggable="true"
            @dragstart="onDragStart(song.id)"
            @dragenter.prevent="onDragEnter(song.id)"
            @dragover.prevent
            @dragend="onDragEnd"
        >
          <div class="drag-handle">⠿</div>
          <div class="song-index">{{ index + 1 }}</div>
          <div class="song-main">
            <div class="title">{{ song.title }}</div>
            <div class="meta">{{ song.artistName || '未知歌手' }}</div>
          </div>
          <div class="song-actions">
            <el-button link type="success" size="small" @click="playSong(song)">播放</el-button>
            <el-button link type="danger" size="small" @click="removeSong(song.id, song.title)">移除</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.header-section {
  display: flex;
  align-items: flex-end;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(to bottom, #1db95430, #121212);
  border-radius: 8px;
}

.playlist-cover {
  width: 200px;
  height: 200px;
  object-fit: cover;
  margin-right: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.5);
  border-radius: 4px;
}

.info-text {
  display: flex;
  flex-direction: column;
  color: white;
}
.type-label { font-size: 14px; margin: 0; }
.playlist-title { font-size: 48px; font-weight: bold; margin: 0; }
.playlist-desc { color: #b3b3b3; margin: 5px 0 10px 0; }
.playlist-creator { font-size: 14px; }
.playlist-share-actions { margin-top: 10px; }
.songs-title { color: white; font-size: 20px; margin: 0; }
.songs-section { margin-top: 24px; }
.songs-title-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.songs-hint { color: #b3b3b3; font-size: 12px; }
.song-list { display: flex; flex-direction: column; gap: 10px; }
.song-row { display: grid; grid-template-columns: 32px 32px 1fr auto; gap: 10px; align-items: center; padding: 12px; background: #181818; border-radius: 8px; }
.song-row:hover { background: #202020; }
.drag-handle { cursor: grab; color: #888; font-size: 18px; text-align: center; user-select: none; }
.song-index { color: #b3b3b3; text-align: center; }
.song-main .title { color: white; font-weight: 700; }
.song-main .meta { color: #b3b3b3; font-size: 12px; margin-top: 4px; }
.song-actions { display: flex; gap: 8px; justify-content: flex-end; }
</style>