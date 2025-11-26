<script setup lang="ts">
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 表单数据
const form = reactive({
  username: '',
  password: ''
})

// 登录逻辑
const handleLogin = () => {
  // 发送 POST 请求给后端
  request.post('/auth/login', form).then((res: any) => {
    // 1. 提示成功
    ElMessage.success('登录成功，欢迎回来！')

    // 2. 把 Token 存到浏览器本地 (LocalStorage)
    // 后端返回的数据结构是 { code: 200, data: { token: '...', ... } }
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data))

    // 3. 跳转到首页 (后面再做首页)
    router.push('/')
  })
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <h2>Spotify Music</h2>
      <p>登录以继续聆听</p>

      <div class="input-group">
        <el-input v-model="form.username" placeholder="用户名/邮箱" size="large" />
      </div>

      <div class="input-group">
        <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password />
      </div>

      <el-button type="success" class="login-btn" size="large" round @click="handleLogin">
        登 录
      </el-button>

      <div class="links">
        <span>没有账号？</span>
        <span class="link-text" @click="router.push('/register')">去注册</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(to bottom, #1db954 0%, #121212 40%); /* 仿 Spotify 渐变背景 */
}

.login-box {
  background-color: black;
  padding: 40px;
  border-radius: 8px;
  width: 400px;
  text-align: center;
}

h2 {
  color: white;
  margin-bottom: 10px;
  font-weight: bold;
}

p {
  color: #b3b3b3;
  margin-bottom: 30px;
}

.input-group {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  font-weight: bold;
  letter-spacing: 2px;
  margin-top: 10px;
}

.links {
  margin-top: 20px;
  color: #b3b3b3;
  font-size: 14px;
  cursor: pointer;
}
.links:hover {
  color: white;
  text-decoration: underline;
}
.link-text { color: white; font-weight: bold; cursor: pointer; margin-left: 5px; }
.link-text:hover { text-decoration: underline; }
</style>