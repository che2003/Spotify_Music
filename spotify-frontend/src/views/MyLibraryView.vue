<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const myPlaylists = ref<any[]>([])

const followingUsers = ref<any[]>([])

const likedSongs = ref<any[]>([])

const dialogVisible = ref(false)
const isEditMode = ref(false)
const activeTab = ref('playlist')

const playlistForm = reactive({
  id: null as number | null,
  title: '',
  description: '',
  isPublic: true
})

const fetchMyPlaylists = async () => {
  try {
    const res = await request.get('/playlist/my')
    myPlaylists.value = res.data
  } catch (error) { console.error(error) }
}

const fetchFollowingUsers = async () => {
  try {
    const res = await request.get('/follow/following')
    followingUsers.value = res.data

const fetchLikedSongs = async () => {
  try {
    const res = await request.get('/interaction/song/liked')
    likedSongs.value = res.data || []

  } catch (error) { console.error(error) }
}

// 打开弹窗 (创建/编辑)
const openDialog = (edit: boolean, playlist?: any) => {
  isEditMode.value = edit
  dialogVisible.value = true
  if (edit && playlist) {
    playlistForm.id = playlist.id
    playlistForm.title = playlist.title
    playlistForm.description = playlist.description
    playlistForm.isPublic = playlist.isPublic
  } else {
    playlistForm.id = null
    playlistForm.title = ''
    playlistForm.description = ''
    playlistForm.isPublic = true
  }
}

// 提交
const submitForm = async () => {
  if (!playlistForm.title) { ElMessage.warning('标题不能为空'); return }
  try {
    const url = isEditMode.value ? '/playlist/update' : '/playlist/create'
    await request.post(url, playlistForm)
    ElMessage.success(isEditMode.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchMyPlaylists()
  } catch (error) { ElMessage.error('操作失败') }
}

// 删除
const deletePlaylist = (event: Event, id: number) => {
  event.stopPropagation()
  ElMessageBox.confirm('确定删除该歌单吗?', '警告', { confirmButtonText: '删除', type: 'warning' })
      .then(async () => {
        await request.delete(`/playlist/delete/${id}`)
        ElMessage.success('删除成功')
        fetchMyPlaylists()
      }).catch(()=> {})
}

const goToPlaylist = (id: number) => router.push(`/playlist/${id}`)

const goToUser = (id: number) => router.push(`/user/${id}`)

const toggleFollowUser = async (userId: number) => {
  try {
    const res = await request.post(`/follow/user/toggle?followedUserId=${userId}`)
    ElMessage.success(res.data ? '关注成功' : '已取消关注')
    fetchFollowingUsers()
  } catch (error) { console.error(error) }
}

onMounted(() => {
  fetchMyPlaylists()
  fetchFollowingUsers()

const goToSong = (id: number) => router.push(`/song/${id}`)

onMounted(() => {
  fetchMyPlaylists()
  fetchLikedSongs()

})
</script>

<template>
  <div class="library-container">
    <h2 class="title">我的音乐库</h2>
    <el-tabs v-model="activeTab" class="library-tabs">
      <el-tab-pane label="歌单" name="playlist">
        <div class="playlist-grid">
          <div class="playlist-card create-card" @click="openDialog(false)">
            <div class="create-icon">+</div>
            <div class="playlist-title">创建新歌单</div>
          </div>

          <div v-for="playlist in myPlaylists" :key="playlist.id" class="playlist-card" @click="goToPlaylist(playlist.id)">
            <div class="card-actions">
              <el-icon class="action-icon edit" @click.stop="openDialog(true, playlist)"><i class="el-icon-edit"></i></el-icon>
              <el-icon class="action-icon delete" @click="deletePlaylist($event, playlist.id)"><i class="el-icon-delete"></i></el-icon>
            </div>
            <img :src="playlist.coverUrl || 'https://via.placeholder.com/150/1db954/FFFFFF?text=P'" class="cover-img" />
            <div class="playlist-info">
              <div class="playlist-title">{{ playlist.title }}</div>
              <div class="playlist-description">User #{{ playlist.creatorId }}</div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我喜欢的歌曲" name="liked">
        <div v-if="likedSongs.length" class="liked-grid">
          <div v-for="song in likedSongs" :key="song.id" class="liked-card" @click="goToSong(song.id)">
            <img :src="song.coverUrl || 'https://via.placeholder.com/150/121212/FFFFFF?text=♪'" class="cover-img" />
            <div class="song-info">
              <div class="song-title">{{ song.title }}</div>
              <div class="song-artist">{{ song.artistName || '未知艺人' }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-tip">还没有喜欢的歌曲，去探索一下吧</div>
      </el-tab-pane>
    </el-tabs>

    <div class="follow-section">
      <div class="follow-header">
        <h3>我关注的用户</h3>
        <span class="follow-count">{{ followingUsers.length }} 人</span>
      </div>
      <div class="follow-list" v-if="followingUsers.length">
        <div v-for="user in followingUsers" :key="user.id" class="follow-card">
          <div class="user-info" @click="goToUser(user.id)">
            <el-avatar :size="48" :src="user.avatarUrl || ''" />
            <div>
              <div class="follow-name">{{ user.nickname || user.username }}</div>
              <div class="follow-username">@{{ user.username }}</div>
            </div>
          </div>
          <el-button size="small" type="info" @click="toggleFollowUser(user.id)">取关</el-button>
        </div>
      </div>
      <div v-else class="empty-follow">暂无关注用户</div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEditMode ? '编辑歌单' : '创建新歌单'" width="400px" class="create-dialog">
      <el-form :model="playlistForm" label-position="top">
        <el-form-item label="名称">
          <el-input v-model="playlistForm.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="playlistForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="公开">
          <el-switch v-model="playlistForm.isPublic" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="success" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.title { color: white; font-size: 24px; margin-bottom: 20px; font-weight: bold; }
.playlist-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
.playlist-card { background-color: #181818; padding: 16px; border-radius: 8px; transition: 0.3s; cursor: pointer; position: relative; }
.playlist-card:hover { background-color: #282828; }
.cover-img { width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 4px; margin-bottom: 10px; }
.playlist-title { color: white; font-weight: bold; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.playlist-description { color: #b3b3b3; font-size: 14px; }
.create-card { border: 2px dashed #404040; display: flex; flex-direction: column; justify-content: center; align-items: center; color: #b3b3b3; }
.create-icon { font-size: 48px; margin-bottom: 10px; }
.library-tabs { margin-top: 10px; }
.liked-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; }
.liked-card { background-color: #181818; padding: 12px; border-radius: 8px; display: flex; gap: 12px; align-items: center; cursor: pointer; transition: 0.25s; }
.liked-card:hover { background-color: #242424; transform: translateY(-2px); }
.liked-card .cover-img { width: 72px; height: 72px; margin: 0; }
.song-info { display: flex; flex-direction: column; gap: 4px; }
.song-title { color: #fff; font-weight: 700; }
.song-artist { color: #b3b3b3; font-size: 14px; }
.empty-tip { color: #b3b3b3; text-align: center; padding: 30px 0; }

.card-actions { position: absolute; top: 10px; right: 10px; display: flex; gap: 5px; opacity: 0; transition: opacity 0.2s; }
.playlist-card:hover .card-actions { opacity: 1; }
.action-icon { color: white; background: rgba(0,0,0,0.6); padding: 6px; border-radius: 50%; font-size: 16px; }
.action-icon:hover { background: var(--spotify-green); color: black; }
.action-icon.delete:hover { background: #f56c6c; color: white; }

.follow-section { margin-top: 32px; background: #181818; padding: 20px; border-radius: 8px; }
.follow-header { display: flex; justify-content: space-between; align-items: center; color: white; margin-bottom: 16px; }
.follow-count { color: #b3b3b3; font-size: 14px; }
.follow-list { display: flex; flex-direction: column; gap: 12px; }
.follow-card { display: flex; justify-content: space-between; align-items: center; background: #202020; padding: 12px; border-radius: 8px; }
.user-info { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.follow-name { color: white; font-weight: 600; }
.follow-username { color: #b3b3b3; font-size: 12px; }
.empty-follow { color: #777; }

:global(.create-dialog) { background-color: #282828 !important; }
:global(.create-dialog .el-dialog__title) { color: white; }
:global(.create-dialog .el-form-item__label) { color: #b3b3b3; }
</style>