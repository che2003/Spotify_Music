<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

interface AlbumForm {
  id: number | null
  title: string
  coverUrl: string
  description: string
  releaseDate: string | null
}

const albums = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)

const albumForm = reactive<AlbumForm>({
  id: null,
  title: '',
  coverUrl: '',
  description: '',
  releaseDate: null
})

const resetForm = () => {
  albumForm.id = null
  albumForm.title = ''
  albumForm.coverUrl = ''
  albumForm.description = ''
  albumForm.releaseDate = null
}

const fetchAlbums = async () => {
  loading.value = true
  try {
    const res = await request.get('/album/my')
    albums.value = res.data
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
  albumForm.coverUrl = row.coverUrl
  albumForm.description = row.description
  albumForm.releaseDate = row.releaseDate
  dialogVisible.value = true
}

const submitAlbum = async () => {
  const api = albumForm.id ? '/album/update' : '/album/create'
  await request.post(api, albumForm)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchAlbums()
}

const removeAlbum = (id: number) => {
  ElMessageBox.confirm('确定删除该专辑吗？', '提示', { type: 'warning' }).then(async () => {
    await request.post(`/album/delete?id=${id}`)
    ElMessage.success('已删除')
    fetchAlbums()
  })
}

onMounted(fetchAlbums)
</script>

<template>
  <div class="page">
    <div class="header">
      <h2 class="title">我的专辑</h2>
      <el-button type="success" @click="openCreate">新建专辑</el-button>
    </div>

    <el-table :data="albums" v-loading="loading" style="width: 100%" height="520px">
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
          <el-button type="danger" size="small" style="margin-left: 8px" @click="removeAlbum(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="albumForm.id ? '编辑专辑' : '新建专辑'" width="520px" @close="resetForm">
      <el-form :model="albumForm" label-width="96px">
        <el-form-item label="标题">
          <el-input v-model="albumForm.title" />
        </el-form-item>
        <el-form-item label="封面链接">
          <el-input v-model="albumForm.coverUrl" />
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
.page { padding: 30px; color: white; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { margin: 0; font-size: 22px; }
:deep(.el-table) { background: transparent; color: #ccc; --el-table-row-hover-bg-color: #282828; --el-table-border-color: #333; }
:deep(.el-table th), :deep(.el-table tr) { background: transparent; border-bottom: 1px solid #333; }
:deep(.el-table td) { border-bottom: 1px solid #333; }
:deep(.el-input__wrapper) { background-color: #242424; }
</style>
