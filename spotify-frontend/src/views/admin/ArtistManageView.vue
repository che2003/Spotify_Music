<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

interface ArtistForm {
  id: number | null
  name: string
  bio: string
  avatarUrl: string
  userId: number | null
  totalFans: number | null
  totalPlays: number | null
}

const artistList = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)

const artistForm = reactive<ArtistForm>({
  id: null,
  name: '',
  bio: '',
  avatarUrl: '',
  userId: null,
  totalFans: null,
  totalPlays: null
})

const resetForm = () => {
  artistForm.id = null
  artistForm.name = ''
  artistForm.bio = ''
  artistForm.avatarUrl = ''
  artistForm.userId = null
  artistForm.totalFans = null
  artistForm.totalPlays = null
}

const fetchArtists = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/artist/crud')
    artistList.value = res.data
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  artistForm.id = row.id
  artistForm.name = row.name
  artistForm.bio = row.bio
  artistForm.avatarUrl = row.avatarUrl
  artistForm.userId = row.userId
  artistForm.totalFans = row.totalFans
  artistForm.totalPlays = row.totalPlays
  dialogVisible.value = true
}

const submitArtist = async () => {
  if (artistForm.id) {
    await request.put(`/admin/artist/crud/${artistForm.id}`, artistForm)
  } else {
    await request.post('/admin/artist/crud', artistForm)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchArtists()
}

const removeArtist = (id: number) => {
  ElMessageBox.confirm('确定删除该艺人吗？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/admin/artist/crud/${id}`)
    ElMessage.success('已删除')
    fetchArtists()
  })
}

onMounted(fetchArtists)
</script>

<template>
  <div class="admin-container">
    <div class="header">
      <h2 class="title">艺人管理</h2>
      <el-button type="primary" @click="openCreate">新增艺人</el-button>
    </div>

    <el-table :data="artistList" v-loading="loading" style="width: 100%" height="600px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="头像" width="80">
        <template #default="{ row }">
          <el-avatar :size="40" :src="row.avatarUrl" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="bio" label="简介" :show-overflow-tooltip="true" />
      <el-table-column prop="userId" label="关联用户" width="100" />
      <el-table-column prop="totalFans" label="粉丝" width="100" />
      <el-table-column prop="totalPlays" label="播放" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="removeArtist(row.id)" style="margin-left: 8px">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="artistForm.id ? '编辑艺人' : '新建艺人'" width="560px" @close="resetForm">
      <el-form :model="artistForm" label-width="96px">
        <el-form-item label="名称">
          <el-input v-model="artistForm.name" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input type="textarea" :rows="3" v-model="artistForm.bio" />
        </el-form-item>
        <el-form-item label="头像链接">
          <el-input v-model="artistForm.avatarUrl" placeholder="图片 URL" />
        </el-form-item>
        <el-form-item label="关联用户ID">
          <el-input v-model.number="artistForm.userId" placeholder="可选" />
        </el-form-item>
        <el-form-item label="粉丝数">
          <el-input v-model.number="artistForm.totalFans" />
        </el-form-item>
        <el-form-item label="播放量">
          <el-input v-model.number="artistForm.totalPlays" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitArtist">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-container { padding: 40px; color: white; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.title { font-size: 24px; margin: 0; }
:deep(.el-table) { background: transparent; color: #ccc; --el-table-row-hover-bg-color: #282828; --el-table-border-color: #333; }
:deep(.el-table th), :deep(.el-table tr) { background: transparent; border-bottom: 1px solid #333; }
:deep(.el-table td) { border-bottom: 1px solid #333; }
:deep(.el-input__wrapper), :deep(.el-select .el-input__wrapper) { background-color: #242424; }
</style>
