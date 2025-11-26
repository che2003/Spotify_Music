<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const userList = ref<any[]>([])
const loading = ref(false)

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/users')
    userList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 修改角色
const changeRole = (user: any, newRole: string) => {
  ElMessageBox.confirm(`确定将用户 ${user.username} 的角色修改为 ${newRole}?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/admin/user/role', { userId: user.id, role: newRole })
      ElMessage.success('修改成功')
      fetchUsers()
    } catch (e) {
      ElMessage.error('修改失败')
    }
  })
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="admin-container">
    <h2 class="title">用户管理 (Admin Panel)</h2>

    <el-table :data="userList" v-loading="loading" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="头像" width="80">
        <template #default="scope">
          <el-avatar :src="scope.row.avatarUrl" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />

      <el-table-column label="操作 (修改角色)" width="300">
        <template #default="scope">
          <el-button-group>
            <el-button size="small" @click="changeRole(scope.row, 'user')">User</el-button>
            <el-button size="small" type="warning" @click="changeRole(scope.row, 'musician')">Musician</el-button>
            <el-button size="small" type="danger" @click="changeRole(scope.row, 'admin')">Admin</el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.admin-container { padding: 40px; color: white; }
.title { margin-bottom: 30px; font-size: 24px; font-weight: bold; }
/* 表格样式适配深色主题 */
:deep(.el-table) { background-color: transparent; color: #ccc; --el-table-row-hover-bg-color: #282828; }
:deep(.el-table th), :deep(.el-table tr) { background-color: transparent; border-bottom: 1px solid #333; }
:deep(.el-table td) { border-bottom: 1px solid #333; }
</style>