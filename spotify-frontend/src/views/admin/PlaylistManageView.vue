<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const playlists = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')

const fetchPlaylists = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/playlists', { params: { keyword: searchKeyword.value } })
    playlists.value = res.data || []
  } catch (error) {
    console.error('加载歌单失败', error)
  } finally {
    loading.value = false
  }
}

const confirmDelete = (row: any) => {
  ElMessageBox.confirm(`确定强制删除「${row.title}」吗？该歌单及其收录歌曲关系将被清空。`, '高危操作', {
    type: 'error',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.delete(`/admin/playlists/${row.id}`)
    ElMessage.success('歌单已删除')
    fetchPlaylists()
  })
}

onMounted(() => {
  fetchPlaylists()
})
</script>

<template>
  <div class="admin-container">
    <div class="header">
      <div>
        <div class="title">歌单监管</div>
        <div class="subtitle">管理员可直接搜索、查看并强制删除全站歌单</div>
      </div>
      <el-input
          v-model="searchKeyword"
          placeholder="输入歌单标题关键字"
          style="width: 320px"
          clearable
          @keyup.enter="fetchPlaylists"
          @clear="fetchPlaylists"
      >
        <template #append>
          <el-button @click="fetchPlaylists">搜索</el-button>
        </template>
      </el-input>
    </div>

    <el-table :data="playlists" v-loading="loading" height="620" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <img :src="row.coverUrl" alt="" class="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="歌单名称" min-width="160" />
      <el-table-column prop="creatorName" label="创建者" width="140" />
      <el-table-column label="公开" width="90">
        <template #default="{ row }">
          <el-tag :type="row.isPublic ? 'success' : 'info'" effect="dark">{{ row.isPublic ? '公开' : '私密' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="描述" min-width="220">
        <template #default="{ row }">
          <span class="desc" :title="row.description">{{ row.description || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.admin-container { padding: 40px; color: white; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.title { font-size: 24px; font-weight: 800; margin-bottom: 4px; }
.subtitle { color: #9ca3af; font-size: 13px; }
.cover { width: 48px; height: 48px; border-radius: 6px; object-fit: cover; box-shadow: 0 8px 18px rgba(0,0,0,0.35); }
.desc { color: #cfd2d8; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

:deep(.el-table) { background: transparent; color: #d1d5db; --el-table-row-hover-bg-color: #1f2937; --el-table-border-color: #30363d; }
:deep(.el-table th), :deep(.el-table tr) { background: transparent; border-bottom: 1px solid #30363d; }
:deep(.el-table td) { border-bottom: 1px solid #30363d; }
:deep(.el-input__wrapper) { background-color: #1f2937; }
</style>
