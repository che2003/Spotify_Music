<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const myPlaylists = ref<any[]>([])
const createDialogVisible = ref(false) // 控制创建歌单模态框
const newPlaylistForm = reactive({
  title: '',
  description: '',
  isPublic: true
})

const fetchMyPlaylists = async () => {
  try {
    const res = await request.get('/playlist/my')
    myPlaylists.value = res.data
  } catch (error) {
    console.error("获取歌单失败", error)
  }
}

// ------------------------------------
// 动作 1: 创建歌单
// ------------------------------------
const submitCreate = async () => {
  if (!newPlaylistForm.title) {
    ElMessage.warning('歌单标题不能为空')
    return
  }

  try {
    // 调用后端创建接口
    await request.post('/playlist/create', newPlaylistForm)

    ElMessage.success('歌单创建成功！')
    createDialogVisible.value = false
    // 刷新列表
    fetchMyPlaylists()

    // 重置表单
    newPlaylistForm.title = ''
    newPlaylistForm.description = ''
  } catch (error) {
    console.error(error)
  }
}

// ------------------------------------
// 动作 2: 删除歌单
// ------------------------------------
const deletePlaylist = (event: Event, id: number) => {
  // 阻止事件冒泡，防止点击删除时也触发 goToPlaylist
  event.stopPropagation()

  ElMessageBox.confirm(
      '此操作将永久删除该歌单及其包含的所有歌曲关联，确定吗?',
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    try {
      // 调用后端 DELETE 接口
      await request.delete(`/playlist/delete/${id}`)
      ElMessage.success('删除成功！')
      // 刷新列表
      fetchMyPlaylists()
    } catch (error) {
      ElMessage.error('删除失败，请检查权限。')
    }
  }).catch(() => {
    // 取消操作
  })
}

const goToPlaylist = (id: number) => {
  router.push(`/playlist/${id}`)
}

onMounted(() => {
  fetchMyPlaylists()
})
</script>

<template>
  <div class="library-container">
    <h2 class="title">我的音乐库</h2>

    <div class="playlist-grid">
      <div class="playlist-card create-card" @click="createDialogVisible = true">
        <div class="create-icon">+</div>
        <div class="playlist-title">创建新歌单</div>
      </div>

      <div
          v-for="playlist in myPlaylists"
          :key="playlist.id"
          class="playlist-card"
          @click="goToPlaylist(playlist.id)"
      >
        <el-icon class="delete-icon" @click="deletePlaylist($event, playlist.id)">
          <i class="el-icon-delete-filled"></i>
        </el-icon>

        <img :src="playlist.coverUrl || 'https://via.placeholder.com/150/1db954/FFFFFF?text=P'" class="cover-img" alt="cover"/>
        <div class="playlist-info">
          <div class="playlist-title">{{ playlist.title }}</div>
          <div class="playlist-description">由 User #{{ playlist.creatorId }} 创建</div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog
      v-model="createDialogVisible"
      title="创建新的歌单"
      width="400px"
      class="create-dialog"
  >
    <el-form :model="newPlaylistForm" label-position="top">
      <el-form-item label="歌单名称">
        <el-input v-model="newPlaylistForm.title" placeholder="输入歌单名称" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="newPlaylistForm.description" type="textarea" placeholder="描述你的歌单" />
      </el-form-item>
      <el-form-item label="公开">
        <el-switch v-model="newPlaylistForm.isPublic" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createDialogVisible = false">取消</el-button>
      <el-button type="success" @click="submitCreate">创建</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 保持原有样式，并新增删除按钮相关样式 */
.title { color: white; font-size: 24px; margin-bottom: 20px; font-weight: bold; }
.playlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}
.playlist-card {
  background-color: #181818;
  padding: 16px;
  border-radius: 8px;
  transition: background-color 0.3s;
  cursor: pointer;
  position: relative; /* 用于定位删除按钮 */
}
.playlist-card:hover { background-color: #282828; }
.cover-img { width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 4px; margin-bottom: 10px; }
.playlist-title { color: white; font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.playlist-description { color: #b3b3b3; font-size: 14px; }

.create-card {
  border: 2px dashed #404040;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  color: #b3b3b3;
}
.create-icon {
  font-size: 48px;
  color: #b3b3b3;
  margin-bottom: 10px;
}

.delete-icon {
  position: absolute;
  top: 5px;
  right: 5px;
  color: #ccc;
  z-index: 10;
  font-size: 18px;
  padding: 5px;
  border-radius: 4px;
  transition: color 0.2s;
}

.delete-icon:hover {
  color: #f56c6c; /* 红色 */
  background-color: rgba(0, 0, 0, 0.5);
}

/* 覆盖 Element-Plus 弹窗样式 */
:global(.create-dialog) .el-dialog__header,
:global(.create-dialog) .el-dialog__body {
  background-color: #121212;
  color: white;
}
:global(.create-dialog) .el-dialog__title {
  color: white;
}
:global(.create-dialog) .el-form-item__label {
  color: white !important;
}
</style>