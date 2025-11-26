<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

interface AlbumForm {
  id: number | null
  title: string
  artistId: number | null
  coverUrl: string
  description: string
  releaseDate: string | null
}

const albumList = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const artistOptions = ref<any[]>([])

const albumForm = reactive<AlbumForm>({
  id: null,
  title: '',
  artistId: null,
  coverUrl: '',
  description: '',
  releaseDate: null
})

const resetForm = () => {
  albumForm.id = null
  albumForm.title = ''
  albumForm.artistId = null
  albumForm.coverUrl = ''
  albumForm.description = ''
  albumForm.releaseDate = null
}

const fetchArtists = async () => {
  const res = await request.get('/artist/list')
  artistOptions.value = res.data
}

const fetchAlbums = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/album/crud')
    albumList.value = res.data
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  albumForm.id = row.id
  albumForm.title = row.title
  albumForm.artistId = row.artistId
  albumForm.coverUrl = row.coverUrl
  albumForm.description = row.description
  albumForm.releaseDate = row.releaseDate
  dialogVisible.value = true
}

const submitAlbum = async () => {
  if (albumForm.id) {
    await request.put(`/admin/album/crud/${albumForm.id}`, albumForm)
  } else {
    await request.post('/admin/album/crud', albumForm)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchAlbums()
}

const removeAlbum = (id: number) => {
  ElMessageBox.confirm('确定删除该专辑吗？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/admin/album/crud/${id}`)
    ElMessage.success('已删除')
    fetchAlbums()
  })
}

onMounted(() => {
  fetchArtists()
  fetchAlbums()
})
</script>

<template>
  <div class="admin-container">
    <div class="header">
      <h2 class="title">专辑管理</h2>
      <el-button type="primary" @click="openCreate">新增专辑</el-button>
    </div>

    <el-table :data="albumList" v-loading="loading" style="width: 100%" height="600px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img :src="row.coverUrl" style="width:50px; height:50px; border-radius:4px;" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="description" label="简介" :show-overflow-tooltip="true" />
      <el-table-column prop="releaseDate" label="发行日" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="removeAlbum(row.id)" style="margin-left: 8px">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="albumForm.id ? '编辑专辑' : '新建专辑'" width="560px" @close="resetForm">
      <el-form :model="albumForm" label-width="96px">
        <el-form-item label="标题">
          <el-input v-model="albumForm.title" />
        </el-form-item>
        <el-form-item label="所属艺人">
          <el-select v-model="albumForm.artistId" filterable placeholder="选择艺人">
            <el-option v-for="artist in artistOptions" :key="artist.id" :label="artist.name" :value="artist.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面链接">
          <el-input v-model="albumForm.coverUrl" placeholder="图片 URL" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input type="textarea" :rows="3" v-model="albumForm.description" />
        </el-form-item>
        <el-form-item label="发行日期">
          <el-date-picker
            v-model="albumForm.releaseDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAlbum">保存</el-button>
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
