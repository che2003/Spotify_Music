<script setup lang="ts">
import { reactive } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: ''
})

const handleRegister = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('用户名和密码必填')
    return
  }

  try {
    await request.post('/auth/register', form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    // 错误已由 request.ts 统一处理
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h2>加入 Spotify</h2>
      <p>注册以开始收听</p>

      <div class="input-group">
        <el-input v-model="form.username" placeholder="用户名" size="large" />
      </div>
      <div class="input-group">
        <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
      </div>
      <div class="input-group">
        <el-input v-model="form.nickname" placeholder="昵称 (选填)" size="large" />
      </div>
      <div class="input-group">
        <el-input v-model="form.email" placeholder="邮箱 (选填)" size="large" />
      </div>

      <el-button type="success" class="login-btn" size="large" round @click="handleRegister">
        注 册
      </el-button>

      <div class="links">
        <span>已有账号？</span>
        <span class="link-text" @click="router.push('/login')">去登录</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex; justify-content: center; align-items: center; height: 100vh;
  background: linear-gradient(to bottom, #1db954 0%, #121212 40%);
}
.login-box {
  background-color: rgba(24, 24, 24, 0.9); padding: 40px; border-radius: 16px; width: 400px; text-align: center;
  border: 1px solid var(--spotify-border);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
}
h2 { color: var(--spotify-white); margin-bottom: 10px; font-weight: bold; }
p { color: var(--spotify-text-sub); margin-bottom: 30px; }
.input-group { margin-bottom: 20px; }
.login-btn { width: 100%; font-weight: bold; letter-spacing: 2px; margin-top: 10px; }
.links { margin-top: 20px; color: var(--spotify-text-sub); font-size: 14px; }
.link-text { color: var(--spotify-white); font-weight: bold; cursor: pointer; margin-left: 5px; }
.link-text:hover { text-decoration: underline; }
</style>