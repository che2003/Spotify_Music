<script setup lang="ts">
import { onMounted, ref } from 'vue'
import request from '@/utils/request'
import { usePlayerStore } from '@/stores/player'

const records = ref<any[]>([])
const loading = ref(true)
const playerStore = usePlayerStore()

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await request.get('/history/recent', { params: { limit: 50 } })
    records.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const playSong = (song: any) => { playerStore.setSong(song) }

onMounted(fetchHistory)
</script>

<template>
  <div class="history-page">
    <h2 class="title">播放历史</h2>

    <el-skeleton v-if="loading" :rows="6" animated />

    <div v-else>
      <div v-if="records.length" class="history-list">
        <div v-for="item in records" :key="`${item.id}-${item.playTime}`" class="history-item">
          <div class="time">{{ item.playTime }}</div>
          <div class="info" @click="playSong(item)">
            <img :src="item.coverUrl" class="cover" />
            <div class="text">
              <div class="name">{{ item.title }}</div>
              <div class="artist">{{ item.artistName }}</div>
            </div>
          </div>
          <el-button size="small" type="success" @click="playSong(item)">播放</el-button>
        </div>
      </div>
      <div v-else class="empty">暂无播放记录</div>
    </div>
  </div>
</template>

<style scoped>
.history-page { color: white; padding-bottom: 30px; }
.title { font-size: 24px; font-weight: 800; margin-bottom: 16px; }
.history-list { display: flex; flex-direction: column; gap: 12px; }
.history-item { display: grid; grid-template-columns: 180px 1fr 80px; align-items: center; padding: 12px; background: #181818; border-radius: 10px; }
.time { color: #b3b3b3; font-size: 13px; }
.info { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.cover { width: 64px; height: 64px; object-fit: cover; border-radius: 8px; }
.text { display: flex; flex-direction: column; }
.name { font-weight: 700; }
.artist { color: #b3b3b3; font-size: 13px; }
.empty { color: #b3b3b3; margin-top: 20px; }
</style>
