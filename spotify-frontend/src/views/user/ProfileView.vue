<script setup lang="ts">
import { reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const form = reactive({
  nickname: '',
  email: '',
  avatarUrl: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: ''
})

// 【修复】在 script 中获取 token
const uploadHeaders = computed(() => {
  return {
    Authorization: 'Bearer ' + localStorage.getItem('token')
  }
})

const fetchProfile = async () => {
  try {
    const res = await request.get('/user/profile')
    Object.assign(form, res.data)
  } catch (e) { console.error(e) }
}

const updateProfile = async () => {
  try {
    await request.post('/user/update', form)
    ElMessage.success('资料已更新')
    // 更新本地存储
    const userStore = JSON.parse(localStorage.getItem('user') || '{}')
    userStore.username = form.nickname || userStore.username
    userStore.avatar = form.avatarUrl
    localStorage.setItem('user', JSON.stringify(userStore))
  } catch (e) { ElMessage.error('更新失败') }
}

const changePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写密码')
    return
  }
  try {
    await request.post('/user/changePassword', pwdForm)
    ElMessage.success('密码修改成功，请重新登录')
  } catch (e) { ElMessage.error('修改失败') }
}

// 头像上传成功回调
const handleAvatarSuccess = (res: any) => {
  if (res.code === 200) {
    form.avatarUrl = res.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

onMounted(() => fetchProfile())
</script>

<template>
  <div class="profile-container">
    <h2 class="title">个人中心</h2>

    <div class="forms-wrapper">
      <div class="form-box">
        <h3>基本资料</h3>
        <el-form label-position="top">
          <el-form-item label="头像">
            <div class="avatar-upload-row">
              <el-avatar :size="80" :src="form.avatarUrl || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
              <el-upload
                  action="/api/storage/upload"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  accept=".jpg,.png,.jpeg"
                  name="file"
                  :headers="uploadHeaders"
              >
                <el-button round>更换头像</el-button>
              </el-upload>
            </div>
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" />
          </el-form-item>
          <div style="text-align: right; margin-top: 20px;">
            <el-button type="success" round @click="updateProfile">保存资料</el-button>
          </div>
        </el-form>
      </div>

      <div class="form-box">
        <h3>安全设置</h3>
        <el-form label-position="top">
          <el-form-item label="旧密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <div style="text-align: right; margin-top: 20px;">
            <el-button type="danger" round @click="changePassword">修改密码</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-container { padding: 40px; max-width: 900px; margin: 0 auto; color: white; }
.title { font-size: 32px; font-weight: 700; margin-bottom: 40px; }
.forms-wrapper { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; }
.form-box { background: #181818; padding: 30px; border-radius: 8px; }
.form-box h3 { font-size: 20px; font-weight: 700; margin-bottom: 20px; border-bottom: 1px solid #333; padding-bottom: 10px; }
.avatar-upload-row { display: flex; align-items: center; gap: 20px; }
:deep(.el-form-item__label) { color: #b3b3b3 !important; font-weight: 700; font-size: 12px; }
</style>