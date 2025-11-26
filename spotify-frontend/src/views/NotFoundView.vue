<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="not-found">
    <h1>404</h1>
    <p>找不到这个页面</p>
    <p class="sub-text">但这首“空无一物”的歌也许不错？</p>
    <el-button type="success" round size="large" @click="router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #121212;
  color: white;
  text-align: center;
}
h1 { font-size: 120px; margin: 0; color: #1db954; font-weight: 900; }
p { font-size: 24px; margin: 10px 0 30px; }
.sub-text { font-size: 16px; color: #b3b3b3; margin-top: -20px; margin-bottom: 40px; }
</style>