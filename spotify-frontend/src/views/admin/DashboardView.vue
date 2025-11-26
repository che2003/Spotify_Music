<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const stats = ref({
  userCount: 0,
  songCount: 0,
  artistCount: 0,
  totalPlays: 0
})

// 初始化图表
const initCharts = (data: any) => {
  // 1. 柱状图：热门歌曲 Top 10
  const barChart = echarts.init(document.getElementById('barChart'))
  const songTitles = data.topSongs.map((s: any) => s.title)
  const songPlays = data.topSongs.map((s: any) => s.playCount)

  barChart.setOption({
    title: { text: '热门歌曲 Top 10', textStyle: { color: '#fff' } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value', splitLine: { lineStyle: { color: '#333' } }, axisLabel: { color: '#ccc' } },
    yAxis: { type: 'category', data: songTitles, axisLabel: { color: '#ccc', width: 100, overflow: 'truncate' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    series: [{
      type: 'bar',
      data: songPlays,
      itemStyle: { color: '#1db954', borderRadius: [0, 20, 20, 0] },
      label: { show: true, position: 'right', color: '#fff' }
    }],
    backgroundColor: 'transparent'
  })

  // 2. 饼图：流派分布
  const pieChart = echarts.init(document.getElementById('pieChart'))
  const genreData = data.genreDistribution.map((g: any) => ({ value: g.count, name: g.genre || 'Unknown' }))

  pieChart.setOption({
    title: { text: '曲库流派分布', textStyle: { color: '#fff' }, left: 'center' },
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%', textStyle: { color: '#ccc' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      itemStyle: { borderRadius: 5, borderColor: '#181818', borderWidth: 2 },
      label: { color: '#fff' },
      data: genreData
    }],
    backgroundColor: 'transparent'
  })

  // 窗口缩放自适应
  window.addEventListener('resize', () => {
    barChart.resize()
    pieChart.resize()
  })
}

const fetchData = async () => {
  try {
    const res = await request.get('/admin/stats/dashboard')
    stats.value = res.data
    // 数据加载完后再渲染图表
    setTimeout(() => initCharts(res.data), 100)
  } catch (e) { console.error(e) }
}

onMounted(fetchData)
</script>

<template>
  <div class="dashboard-container">
    <h2 class="page-title">数据看板 (Dashboard)</h2>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.userCount }}</div>
        <div class="stat-label">注册用户</div>
        <i class="el-icon-user stat-icon"></i>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.songCount }}</div>
        <div class="stat-label">曲库总量</div>
        <i class="el-icon-headset stat-icon"></i>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.artistCount }}</div>
        <div class="stat-label">入驻艺人</div>
        <i class="el-icon-mic stat-icon"></i>
      </div>
      <div class="stat-card highlight">
        <div class="stat-value">{{ stats.totalPlays }}</div>
        <div class="stat-label">总播放次数</div>
        <i class="el-icon-data-analysis stat-icon"></i>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-box" id="barChart"></div>
      <div class="chart-box" id="pieChart"></div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container { padding: 30px; color: white; }
.page-title { font-size: 28px; font-weight: 700; margin-bottom: 30px; }

/* 卡片 */
.stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 40px; }
.stat-card { background: #181818; padding: 25px; border-radius: 8px; position: relative; overflow: hidden; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-5px); background: #282828; }
.stat-value { font-size: 36px; font-weight: 900; color: white; }
.stat-label { font-size: 14px; color: #b3b3b3; margin-top: 5px; text-transform: uppercase; letter-spacing: 1px; }
.stat-icon { position: absolute; right: 20px; bottom: 20px; font-size: 48px; opacity: 0.1; color: white; }
.stat-card.highlight .stat-value { color: #1db954; }

/* 图表 */
.charts-row { display: flex; gap: 20px; flex-wrap: wrap; }
.chart-box { background: #181818; border-radius: 8px; padding: 20px; flex: 1; min-width: 400px; height: 400px; }
</style>