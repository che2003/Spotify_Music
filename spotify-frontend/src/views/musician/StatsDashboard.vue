<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const loading = ref(true)
const dashboard = ref<any>({
  totalPlays: 0,
  totalLikes: 0,
  uniqueListeners: 0,
  songCount: 0,
  avgRating: 0,
  playTrend: [],
  topSongs: [],
  songMetrics: []
})

const initCharts = (data: any) => {
  const trendDom = document.getElementById('trendChart')
  const topSongDom = document.getElementById('topSongChart')
  if (!trendDom || !topSongDom) return

  const trendChart = echarts.init(trendDom)
  const barChart = echarts.init(topSongDom)

  const dates = data.playTrend.map((p: any) => p.date)
  const plays = data.playTrend.map((p: any) => p.plays)
  const listeners = data.playTrend.map((p: any) => p.listeners)

  trendChart.setOption({
    title: { text: '近 7 日播放 & 听众趋势', textStyle: { color: '#fff' } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['播放量', '独立听众'], textStyle: { color: '#ccc' } },
    grid: { left: '3%', right: '3%', bottom: '8%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { color: '#ccc' } },
    yAxis: { type: 'value', axisLabel: { color: '#ccc' }, splitLine: { lineStyle: { color: '#333' } } },
    series: [
      { name: '播放量', type: 'line', smooth: true, data: plays, itemStyle: { color: '#1db954' }, areaStyle: { color: 'rgba(29,185,84,0.15)' } },
      { name: '独立听众', type: 'line', smooth: true, data: listeners, itemStyle: { color: '#4ecdc4' }, areaStyle: { color: 'rgba(78,205,196,0.15)' } }
    ]
  })

  const topTitles = data.topSongs.map((s: any) => s.title)
  const topPlays = data.topSongs.map((s: any) => s.playCount)

  barChart.setOption({
    title: { text: '近 30 日热播作品', textStyle: { color: '#fff' } },
    tooltip: { trigger: 'axis' },
    grid: { left: '4%', right: '8%', bottom: '8%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: '#ccc' }, splitLine: { lineStyle: { color: '#333' } } },
    yAxis: { type: 'category', data: topTitles, axisLabel: { color: '#ccc', width: 120, overflow: 'truncate' } },
    series: [{ type: 'bar', data: topPlays, itemStyle: { color: '#1db954', borderRadius: [0, 12, 12, 0] }, label: { show: true, position: 'right', color: '#fff' } }]
  })

  window.addEventListener('resize', () => {
    trendChart.resize()
    barChart.resize()
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/musician/stats/dashboard')
    dashboard.value = res.data
    await nextTick()
    initCharts(res.data)
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(fetchData)
</script>

<template>
  <div class="musician-dashboard">
    <h2 class="page-title">作品统计面板</h2>

    <div class="stats-grid" v-if="loading">
      <el-skeleton v-for="i in 4" :key="i" animated :rows="2" class="stat-card" />
    </div>
    <div class="stats-grid" v-else>
      <div class="stat-card">
        <div class="stat-value">{{ dashboard.totalPlays }}</div>
        <div class="stat-label">总播放量</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ dashboard.totalLikes }}</div>
        <div class="stat-label">点赞次数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ dashboard.uniqueListeners }}</div>
        <div class="stat-label">独立听众</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ dashboard.songCount }}</div>
        <div class="stat-label">已发布作品</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ dashboard.avgRating?.toFixed(2) || '0.00' }}</div>
        <div class="stat-label">平均评分</div>
      </div>
    </div>

    <div class="charts-row" v-if="loading">
      <el-skeleton animated :rows="6" class="chart-box" />
      <el-skeleton animated :rows="6" class="chart-box" />
    </div>
    <div class="charts-row" v-else>
      <div class="chart-box" id="trendChart"></div>
      <div class="chart-box" id="topSongChart"></div>
    </div>

    <div class="table-card" v-if="!loading">
      <div class="table-header">
        <h3>作品表现</h3>
        <span class="table-desc">播放量、点赞数与评分的汇总视图</span>
      </div>
      <el-table :data="dashboard.songMetrics" style="width: 100%" height="400">
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <img :src="row.coverUrl" class="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="歌曲" min-width="180" />
        <el-table-column prop="playCount" label="播放量" width="120" />
        <el-table-column prop="likeCount" label="点赞数" width="120" />
        <el-table-column label="平均评分" width="120">
          <template #default="{ row }">
            {{ row.avgRating ? row.avgRating.toFixed(2) : '0.00' }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.musician-dashboard { padding: 32px; color: white; }
.page-title { font-size: 26px; font-weight: 700; margin-bottom: 20px; }

.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 24px; }
.stat-card { background: #181818; padding: 20px; border-radius: 10px; border: 1px solid #202020; box-shadow: 0 6px 20px rgba(0,0,0,0.25); }
.stat-value { font-size: 28px; font-weight: 800; color: #1db954; }
.stat-label { color: #b3b3b3; margin-top: 6px; letter-spacing: 0.5px; font-size: 13px; }

.charts-row { display: flex; flex-wrap: wrap; gap: 16px; margin-bottom: 24px; }
.chart-box { background: #181818; border-radius: 10px; padding: 16px; flex: 1; min-width: 360px; height: 360px; }

.table-card { background: #181818; padding: 16px; border-radius: 10px; border: 1px solid #202020; }
.table-header { display: flex; align-items: baseline; gap: 10px; margin-bottom: 10px; }
.table-header h3 { margin: 0; font-size: 18px; }
.table-desc { color: #8c8c8c; font-size: 12px; }
.cover { width: 48px; height: 48px; object-fit: cover; border-radius: 6px; }

:deep(.el-table) { background: transparent; color: #b3b3b3; --el-table-row-hover-bg-color: #202020; --el-table-border-color: #333; }
:deep(.el-table th) { background: #181818 !important; color: #e0e0e0; }
:deep(.el-table tr) { background: transparent !important; }
:deep(.el-table td) { border-bottom: 1px solid #2a2a2a; }
</style>
