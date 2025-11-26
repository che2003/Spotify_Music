<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

interface GenreForm {
  id: number | null
  name: string
}

const genreList = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)

const genreForm = reactive<GenreForm>({
  id: null,
  name: ''
})

const resetForm = () => {
  genreForm.id = null
  genreForm.name = ''
}

const fetchGenres = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/genre/crud')
    genreList.value = res.data
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  genreForm.id = row.id
  genreForm.name = row.name
  dialogVisible.value = true
}

const submitGenre = async () => {
  if (genreForm.id) {
    await request.put(`/admin/genre/crud/${genreForm.id}`, genreForm)
  } else {
    await request.post('/admin/genre/crud', genreForm)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchGenres()
}

const removeGenre = (id: number) => {
  ElMessageBox.confirm('确定删除该流派吗？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/admin/genre/crud/${id}`)
    ElMessage.success('已删除')
    fetchGenres()
  })
}

onMounted(fetchGenres)
</script>

<template>
  <div class="admin-container">
    <div class="header">
      <h2 class="title">流派管理</h2>
      <el-button type="primary" @click="openCreate">新增流派</el-button>
    </div>

    <el-table :data="genreList" v-loading="loading" style="width: 100%" height="500px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="流派名称" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="removeGenre(row.id)" style="margin-left: 8px">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="genreForm.id ? '编辑流派' : '新建流派'" width="420px" @close="resetForm">
      <el-form :model="genreForm" label-width="96px">
        <el-form-item label="流派名称">
          <el-input v-model="genreForm.name" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGenre">保存</el-button>
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
:deep(.el-input__wrapper) { background-color: #242424; }
</style>
