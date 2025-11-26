<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const allSongs = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')

// 管理员获取所有歌
const fetchAllSongs = async () => {
  loading.value = true
  try {
    const url = searchKeyword.value ? `/search?keyword=${searchKeyword.value}` : '/song/list'
    const res = await request.get(url)
    allSongs.value = res.data
  } catch (e) { console.error(e) } finally { loading.value = false }
}

// 管理员强删
const adminDelete = (id: number) => {
  ElMessageBox.confirm('管理员操作：确定强制删除该歌曲吗？', '高危操作', {
    type: 'error', confirmButtonText: '强制删除', cancelButtonText: '取消'
  }).then(async () => {
    await request.post(`/song/delete?id=${id}`)
    ElMessage.success('已删除')
    fetchAllSongs()
  })
}

onMounted(fetchAllSongs)
</script>

<template>
  <div class="admin-container">
    <div class="header">
      <h2 class="title">全站内容管理 (Admin)</h2>
      <el-input v-model="searchKeyword" placeholder="搜索歌曲..." @keyup.enter="fetchAllSongs" style="width: 300px;" clearable @clear="fetchAllSongs">
        <template #append><el-button @click="fetchAllSongs">搜索</el-button></template>
      </el-input>
    </div>

    <el-table :data="allSongs" v-loading="loading" height="600" style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="70">
        <template #default="{ row }">
          <img :src="row.coverUrl" style="width:40px; height:40px; border-radius:4px;" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="歌名" />
      <el-table-column prop="artistName" label="歌手" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="adminDelete(row.id)">强制删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.admin-container { padding: 40px; color: white; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.title { font-size: 24px; font-weight: bold; margin: 0; }
:deep(.el-table) { background: transparent; color: #ccc; --el-table-row-hover-bg-color: #282828; --el-table-border-color: #333; }
:deep(.el-table th), :deep(.el-table tr) { background: transparent; border-bottom: 1px solid #333; }
:deep(.el-table td) { border-bottom: 1px solid #333; }
:deep(.el-input__wrapper) { background-color: #242424; }
</style>