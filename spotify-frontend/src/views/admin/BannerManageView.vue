<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Banner {
  id?: number
  title: string
  imageUrl: string
  targetUrl?: string
  sortOrder: number
  isEnabled: boolean
  updateTime?: string
}

const loading = ref(false)
const banners = ref<Banner[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增 Banner')
const uploadUrl = '/api/storage/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + localStorage.getItem('token')
}))

const form = reactive<Banner>({
  id: undefined,
  title: '',
  imageUrl: '',
  targetUrl: '',
  sortOrder: 0,
  isEnabled: true
})

const resetForm = () => {
  form.id = undefined
  form.title = ''
  form.imageUrl = ''
  form.targetUrl = ''
  form.sortOrder = 0
  form.isEnabled = true
}

const fetchBanners = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/banner/list')
    banners.value = res.data || []
  } catch (error) {
    console.error('加载 Banner 列表失败', error)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  resetForm()
  dialogTitle.value = '新增 Banner'
  dialogVisible.value = true
}

const openEdit = (banner: Banner) => {
  form.id = banner.id
  form.title = banner.title
  form.imageUrl = banner.imageUrl
  form.targetUrl = banner.targetUrl
  form.sortOrder = banner.sortOrder
  form.isEnabled = banner.isEnabled
  dialogTitle.value = '编辑 Banner'
  dialogVisible.value = true
}

const saveBanner = async () => {
  if (!form.title || !form.imageUrl) {
    ElMessage.warning('请填写标题并上传图片')
    return
  }
  try {
    await request.post('/admin/banner/save', form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchBanners()
  } catch (error) {
    console.error('保存 Banner 失败', error)
  }
}

const confirmDelete = (banner: Banner) => {
  ElMessageBox.confirm(`确定删除「${banner.title}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await request.delete(`/admin/banner/delete/${banner.id}`)
      ElMessage.success('删除成功')
      fetchBanners()
    })
    .catch(() => {})
}

const handleUploadSuccess = (response: any) => {
  if (response.code === 200) {
    form.imageUrl = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const handleEnabledChange = async (banner: Banner) => {
  try {
    await request.post('/admin/banner/save', banner)
    ElMessage.success('状态已更新')
  } catch (error) {
    ElMessage.error('更新状态失败')
    banner.isEnabled = !banner.isEnabled
  }
}

const handleSortChange = async (banner: Banner) => {
  try {
    await request.post('/admin/banner/save', banner)
    ElMessage.success('排序已更新')
    fetchBanners()
  } catch (error) {
    ElMessage.error('更新排序失败')
  }
}

onMounted(fetchBanners)
</script>

<template>
  <div class="banner-manage">
    <div class="header">
      <div>
        <h2 class="title">首页 Banner 管理</h2>
        <p class="subtitle">上传图片、配置跳转链接，设置排序与启用状态</p>
      </div>
      <el-button type="primary" @click="openCreate">新增 Banner</el-button>
    </div>

    <el-table :data="banners" v-loading="loading" height="600" style="width: 100%">
      <el-table-column label="预览" width="160">
        <template #default="{ row }">
          <div class="thumb-wrapper">
            <img :src="row.imageUrl" class="thumb" />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="跳转链接" min-width="200">
        <template #default="{ row }">
          <a v-if="row.targetUrl" :href="row.targetUrl" target="_blank" class="link-text">{{ row.targetUrl }}</a>
          <span v-else class="muted">未配置</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" width="140">
        <template #default="{ row }">
          <el-input-number v-model="row.sortOrder" :min="0" size="small" @change="() => handleSortChange(row)" />
        </template>
      </el-table-column>
      <el-table-column label="启用" width="120" align="center">
        <template #default="{ row }">
          <el-switch v-model="row.isEnabled" active-text="启用" inactive-text="停用" @change="() => handleEnabledChange(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="180" />
      <el-table-column label="操作" width="160" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="confirmDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="resetForm">
      <el-form :model="form" label-width="100px" class="banner-form">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入 Banner 标题" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.targetUrl" placeholder="http(s)://" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="form.isEnabled" />
        </el-form-item>
        <el-form-item label="Banner 图片" required>
          <div class="upload-box">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              class="upload-trigger"
            >
              <div v-if="form.imageUrl" class="preview-box">
                <img :src="form.imageUrl" alt="banner" />
                <span class="change-btn">重新上传</span>
              </div>
              <div v-else class="placeholder">点击上传</div>
            </el-upload>
            <p class="tip">推荐尺寸：1200x360，支持 jpg/png，文件将通过后端存储接口保存。</p>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBanner">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.banner-manage { padding: 32px; color: #e5e5e5; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.title { margin: 0; font-size: 22px; font-weight: 700; }
.subtitle { margin: 4px 0 0; color: #9ca3af; }
.thumb-wrapper { width: 140px; height: 80px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.3); }
.thumb { width: 100%; height: 100%; object-fit: cover; display: block; }
.link-text { color: #22c55e; text-decoration: none; word-break: break-all; }
.link-text:hover { text-decoration: underline; }
.muted { color: #9ca3af; }
.banner-form :deep(.el-input__wrapper) { background-color: #1f1f1f; }
.upload-box { display: flex; flex-direction: column; gap: 8px; }
.placeholder, .preview-box { width: 280px; height: 120px; border: 1px dashed #4b5563; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #9ca3af; cursor: pointer; background: #111; overflow: hidden; position: relative; }
.preview-box img { width: 100%; height: 100%; object-fit: cover; }
.change-btn { position: absolute; bottom: 6px; right: 8px; background: rgba(0,0,0,0.6); padding: 4px 8px; border-radius: 6px; font-size: 12px; color: #e5e5e5; }
.tip { color: #9ca3af; font-size: 12px; margin: 0; }
:deep(.el-table) { background: transparent; color: #e5e5e5; --el-table-row-hover-bg-color: #1f2937; --el-table-border-color: #1f2937; }
:deep(.el-table th), :deep(.el-table tr) { background: transparent; border-bottom: 1px solid #1f2937; }
:deep(.el-input-number__increase), :deep(.el-input-number__decrease) { background: #111; }
</style>
