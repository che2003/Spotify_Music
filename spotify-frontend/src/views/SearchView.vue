<script setup lang="ts">
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { usePlayerStore } from '@/stores/player'

const keyword = ref('')
const searchResults = ref<any[]>([])
const activeTab = ref('all') // all, songs, artists
const playerStore = usePlayerStore()

const handleSearch = async () => {
  if (!keyword.value) return
  try {
    const res = await request.get(`/search?keyword=${keyword.value}`)
    searchResults.value = res.data
  } catch (error) { console.error(error) }
}

// 前端过滤逻辑
const filteredResults = computed(() => {
  if (activeTab.value === 'all') return searchResults.value
  // 这里简单通过是否有 fileUrl 区分 (其实 SongVo 都是歌曲，暂无纯艺人搜索接口)
  // 如果要搜艺人，需要在后端加 Artist 搜索接口。这里仅做演示 UI。
  return searchResults.value
})

const playSong = (song: any) => { playerStore.setSong(song) }
</script>

<template>
  <div class="search-container">
    <div class="search-header">
      <el-input
          v-model="keyword"
          placeholder="想听什么？"
          size="large"
          class="search-bar"
          @keyup.enter="handleSearch"
      >
        <template #prefix><el-icon><i class="el-icon-search"></i></el-icon></template>
      </el-input>
    </div>

    <div class="filter-tags">
      <span :class="{active: activeTab==='all'}" @click="activeTab='all'">全部</span>
      <span :class="{active: activeTab==='songs'}" @click="activeTab='songs'">歌曲</span>
      <span :class="{active: activeTab==='artists'}" @click="activeTab='artists'">艺人</span>
    </div>

    <div v-if="searchResults.length > 0" class="results-list">
      <el-table :data="filteredResults" stripe style="width: 100%">
        <el-table-column type="index" width="50" />
        <el-table-column label="封面" width="80">
          <template #default="scope">
            <img :src="scope.row.coverUrl" style="width: 40px; height: 40px; border-radius: 4px;"/>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="歌曲" />
        <el-table-column prop="artistName" label="艺人" />
        <el-table-column prop="duration" label="时长" width="100">
          <template #default="scope">{{ Math.floor(scope.row.duration/60) }}:{{ (scope.row.duration%60).toString().padStart(2,'0') }}</template>
        </el-table-column>
        <el-table-column width="100">
          <template #default="scope">
            <el-button circle type="success" size="small" @click="playSong(scope.row)">▶</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.search-header { margin-bottom: 20px; }
.search-bar { max-width: 400px; --el-input-bg-color: #242424; --el-input-border-color: transparent; --el-input-text-color: white; }
.filter-tags { display: flex; gap: 10px; margin-bottom: 20px; }
.filter-tags span {
  padding: 6px 16px; background: #242424; border-radius: 20px; font-size: 14px; cursor: pointer; transition: 0.2s;
}
.filter-tags span.active, .filter-tags span:hover { background: white; color: black; }

/* Table 适配 */
:deep(.el-table) { background: transparent; --el-table-tr-bg-color: transparent; --el-table-header-bg-color: transparent; color: #b3b3b3; --el-table-row-hover-bg-color: #2a2a2a; --el-table-border-color: transparent; }
:deep(.el-table th) { border-bottom: 1px solid #333; color: white; }
:deep(.el-table td) { border-bottom: none; }
:deep(.el-input__wrapper) { border-radius: 50px; }
</style>