<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const mySongs = ref<any[]>([])
const loading = ref(false)

const fetchMySongs = async () => {
  loading.value = true
  try {
    const res = await request.get('/song/my')
    mySongs.value = res.data
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const deleteSong = (id: number) => {
  ElMessageBox.confirm('确定要删除这首作品吗？此操作不可恢复。', '警告', {
    type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
  }).then(async () => {
    try {
      await request.post(`/song/delete?id=${id}`)
      ElMessage.success('删除成功')
      fetchMySongs()
    } catch (e) { ElMessage.error('删除失败') }
  })
}

onMounted(fetchMySongs)
</script>

<template>
  <div class="works-container">
    <h2 class="title">我的作品管理</h2>
    <el-table :data="mySongs" v-loading="loading" style="width: 100%">
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img :src="row.coverUrl" style="width:50px; height:50px; border-radius:4px; object-fit:cover"/>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="歌名" />
      <el-table-column prop="genre" label="流派" width="120" />
      <el-table-column prop="playCount" label="播放量" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="deleteSong(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.works-container { padding: 40px; color: white; }
.title { margin-bottom: 20px; font-size: 24px; font-weight: bold; }
/* 表格深色适配 */
:deep(.el-table) { background: transparent; color: #b3b3b3; --el-table-row-hover-bg-color: #282828; --el-table-border-color: #333; }
:deep(.el-table th), :deep(.el-table tr) { background: transparent !important; border-bottom: 1px solid #333; }
:deep(.el-table td) { border-bottom: 1px solid #333; }
</style>