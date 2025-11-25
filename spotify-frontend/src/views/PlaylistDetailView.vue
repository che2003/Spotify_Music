<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { usePlayerStore } from '@/stores/player'

const route = useRoute()
const playlist = ref<any>({})
const songs = ref<any[]>([])
const playerStore = usePlayerStore()

// ------------------------------------
// 动作 1: 获取歌单详情和歌曲
// ------------------------------------
const fetchPlaylistDetails = async () => {
  const playlistId = route.params.id as string

  try {
    const playlistsRes = await request.get('/playlist/my')
    const currentPlaylist = playlistsRes.data.find((p: any) => p.id === parseInt(playlistId))
    if (currentPlaylist) {
      playlist.value = currentPlaylist
    } else {
      ElMessage.error('歌单不存在')
      return
    }

    // 后端现在返回 SongVo，包含 artistName
    const songsRes = await request.get(`/playlist/${playlistId}/songs`)
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
  const playlistId = route.params.id as string

  ElMessageBox.confirm(
      `确定要从歌单《${playlist.value.title}》中移除歌曲【${title}】吗?`,
      '移除确认',
  ).then(async () => {
    try {
      await request.delete(`/playlist/removeSong?playlistId=${playlistId}&songId=${songId}`)
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
      </div>
    </div>

    <div class="songs-section">
      <h3 class="songs-title">歌曲列表 (共 {{ songs.length }} 首)</h3>

      <el-table :data="songs" stripe style="width: 100%">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="title" label="歌曲名" />

        <el-table-column prop="artistName" label="歌手名" width="150" />

        <el-table-column prop="duration" label="时长" width="80" />

        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="success" size="small" @click="playSong(scope.row)">
              ▶ 播放
            </el-button>
            <el-button link type="danger" size="small" @click="removeSong(scope.row.id, scope.row.title)">
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
/* 样式保持不变 */
:deep(.el-table) {
  --el-table-row-hover-bg-color: #282828 !important;
  --el-table-bg-color: #121212 !important;
  --el-table-text-color: white !important;
  color: white;
}
:deep(.el-table th) {
  background-color: #181818 !important;
  color: #b3b3b3;
}

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
.songs-title { color: white; font-size: 20px; margin-bottom: 15px; }
</style>