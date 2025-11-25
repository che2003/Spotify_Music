<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const keyword = ref('')
const searchResults = ref<any[]>([])

// 搜索逻辑
const handleSearch = async () => {
  if (!keyword.value) {
    searchResults.value = []
    return
  }

  try {
    // 调用后端 /search 接口
    const res = await request.get(`/search?keyword=${keyword.value}`)
    searchResults.value = res.data
    ElMessage.success(`找到 ${res.data.length} 首歌曲`)
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error(error)
  }
}
</script>

<template>
  <div class="search-container">
    <div class="search-input-wrapper">
      <el-input
          v-model="keyword"
          placeholder="搜索歌曲、专辑、艺人"
          size="large"
          @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><i class="el-icon-search"></i></el-icon>
        </template>
      </el-input>
      <el-button
          type="success"
          size="large"
          @click="handleSearch"
          style="margin-left: 10px;"
      >搜索</el-button>
    </div>

    <div v-if="searchResults.length > 0" class="results-list">
      <h3 class="results-title">搜索结果</h3>
      <el-table :data="searchResults" stripe style="width: 100%; background: #121212;">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="title" label="歌曲名" />
        <el-table-column prop="artistId" label="歌手 ID" width="100" />
        <el-table-column prop="genre" label="流派" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button link type="success" size="small">播放</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-else-if="keyword" style="color: #b3b3b3; margin-top: 20px;">
      未找到相关结果。
    </div>
  </div>
</template>

<style scoped>
.search-input-wrapper {
  display: flex;
  margin-bottom: 30px;
  max-width: 600px;
}

.results-title {
  color: white;
  margin-bottom: 15px;
}

/* 覆盖 Element Plus 的表格默认样式，使其适应深色背景 */
:deep(.el-table) {
  --el-table-row-hover-bg-color: #282828 !important;
  --el-table-bg-color: #121212 !important;
  --el-table-border-color: #333333;
  color: white;
}
:deep(.el-table th) {
  background-color: #181818 !important;
  color: #b3b3b3;
}
</style>