<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import CommentBox from '@/components/CommentBox.vue'
import { usePlayerStore } from '@/stores/player'

const route = useRoute()
const song = ref<any>({})
const playerStore = usePlayerStore()
const isFollowing = ref(false)

const fetchSongDetails = async () => {
  const songId = route.params.id as string
  try {
    // 暂时用 list 过滤，推荐改为后端 /song/{id} 接口
    const res = await request.get('/song/list')
    const foundSong = res.data.find((s: any) => s.id === parseInt(songId))
    if (foundSong) song.value = foundSong
    else ElMessage.error('歌曲不存在')
    if (song.value.artistId) {
      checkFollowStatus(song.value.artistId)
    }
  } catch (error) { ElMessage.error('加载失败') }
}
const checkFollowStatus = async (artistId: number) => {
  try {
    const res = await request.get(`/follow/check?artistId=${artistId}`)
    isFollowing.value = res.data
  } catch (e) {}
}
const toggleFollow = async () => {
  if (!song.value.artistId) return
  try {
    await request.post(`/follow/artist?artistId=${song.value.artistId}`)
    isFollowing.value = !isFollowing.value // 切换状态
    ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注')
  } catch (e) {
    ElMessage.error('操作失败 (可能艺人未入驻)')
  }
}
const playSong = () => { if (song.value.id) playerStore.setSong(song.value) }
onMounted(() => { fetchSongDetails() })
</script>

<template>
  <div class="song-detail-container">
    <div v-if="song.id" class="detail-wrapper">
      <div class="header-section">
        <img :src="song.coverUrl" class="song-cover" />
        <div class="info-text">
          <p class="type-label">歌曲</p>
          <h1 class="song-title">{{ song.title }}</h1>

          <div class="artist-row">
            <p class="song-artist">{{ song.artistName || '未知歌手' }}</p>
            <el-button
                size="small"
                round
                class="follow-btn"
                :type="isFollowing ? 'default' : 'success'"
                plain
                @click="toggleFollow"
            >
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.song-detail-container {
  /* 模拟专辑封面色提取的渐变 */
  background: linear-gradient(to bottom, #535353 0%, #121212 500px);
  margin: -20px -30px; padding: 20px 30px; min-height: 100%;
}
.detail-wrapper { max-width: 1400px; margin: 0 auto; }
.header-section { display: flex; align-items: flex-end; margin-top: 20px; margin-bottom: 24px; padding-bottom: 24px; }
.song-cover { width: 232px; height: 232px; object-fit: cover; margin-right: 24px; box-shadow: 0 4px 60px rgba(0,0,0,0.5); }
.info-text { display: flex; flex-direction: column; color: white; }
.type-label { font-size: 12px; font-weight: 700; text-transform: uppercase; margin-bottom: 4px; }
.song-title { font-size: 72px; font-weight: 900; margin: 0 0 8px 0; line-height: 1; letter-spacing: -1px; }
.song-artist { font-size: 24px; font-weight: 700; }
.actions { margin-top: 30px; }
.play-btn-large { width: 56px; height: 56px; border-radius: 50%; background: var(--spotify-green); color: black; font-size: 28px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: transform 0.1s; }
.play-btn-large:hover { transform: scale(1.05); background: #1ed760; }
.content-section { margin-top: 20px; }
.lyrics-box { margin-bottom: 40px; }
.lyrics-text { color: white; font-size: 18px; font-weight: 500; line-height: 1.6; opacity: 0.8; white-space: pre-wrap; }
.artist-row {
  display: flex;
  align-items: center;
  gap: 15px;
}
.follow-btn {
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  background-color: transparent;
  border: 1px solid #878787;
  color: white;
}
.follow-btn:hover {
  border-color: white;
  transform: scale(1.05);
}
/* 如果是未关注状态 (success type)，给绿色边框 */
.el-button--success.is-plain {
  color: #1db954;
  border-color: #1db954;
  background: transparent;
}
.el-button--success.is-plain:hover {
  color: white;
  background-color: #1db954;
  border-color: #1db954;
}
</style>
