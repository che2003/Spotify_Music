<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const allSongs = ref<any[]>([])
const loading = ref(false)
const searchKeyword = ref('')
const editDialogVisible = ref(false)

const editForm = reactive({
  id: 0,
  title: '',
  artistId: null as number | null,
  albumId: null as number | null,
  genre: '',
  duration: 0,
  coverUrl: '',
  fileUrl: '',
  description: '',
  genreIds: [] as number[]
})

const genreOptions = ref<any[]>([])
const artistOptions = ref<any[]>([])
const albumOptions = ref<any[]>([])

const fetchMeta = async () => {
  const [genres, artists, albums] = await Promise.all([
    request.get('/genre/list'),
    request.get('/artist/list'),
    request.get('/album/list')
  ])
  genreOptions.value = genres.data
  artistOptions.value = artists.data
  albumOptions.value = albums.data
}

// 管理员获取所有歌
const fetchAllSongs = async () => {
  loading.value = true
  try {
    const url = searchKeyword.value ? `/search?keyword=${searchKeyword.value}` : '/song/list'
    const res = await request.get(url)
    allSongs.value = res.data
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const openEdit = async (row: any) => {
  editForm.id = row.id
  editForm.title = row.title
  editForm.artistId = row.artistId
  editForm.albumId = row.albumId
  editForm.genre = row.genre
  editForm.duration = row.duration
  editForm.coverUrl = row.coverUrl
  editForm.fileUrl = row.fileUrl
  editForm.description = row.lyrics
  try {
    const res = await request.get(`/song/${row.id}/genres`)
    editForm.genreIds = res.data || []
  } catch (e) {
    console.error(e)
  }
  editDialogVisible.value = true
}

const resetEditForm = () => {
  editForm.id = 0
  editForm.title = ''
  editForm.artistId = null
  editForm.albumId = null
  editForm.genre = ''
  editForm.duration = 0
  editForm.coverUrl = ''
  editForm.fileUrl = ''
  editForm.description = ''
  editForm.genreIds = []
}

const submitEdit = async () => {
  if (!editForm.id) return
  await request.post('/song/update', editForm)
  ElMessage.success('保存成功')
  editDialogVisible.value = false
  fetchAllSongs()
  resetEditForm()
}

// 管理员强删
const adminDelete = (id: number) => {
  ElMessageBox.confirm('管理员操作：确定强制删除该歌曲吗？', '高危操作', {
    type: 'error', confirmButtonText: '强制删除', cancelButtonText: '取消'
  }).then(async () => {
    await request.post(`/song/delete?id=${id}`)
    ElMessage.success('已删除')
    fetchAllSongs()
  })
}

onMounted(async () => {
  await Promise.all([fetchMeta(), fetchAllSongs()])
})
</script>

<template>
  <div class="admin-container">
    <div class="header">
      <h2 class="title">全站内容管理 (Admin)</h2>
      <el-input v-model="searchKeyword" placeholder="搜索歌曲..." @keyup.enter="fetchAllSongs" style="width: 300px;" clearable @clear="fetchAllSongs">
        <template #append><el-button @click="fetchAllSongs">搜索</el-button></template>
      </el-input>
    </div>

    <el-table :data="allSongs" v-loading="loading" height="600" style="width: 100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="封面" width="70">
        <template #default="{ row }">
          <img :src="row.coverUrl" style="width:40px; height:40px; border-radius:4px;" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="歌名" />
      <el-table-column prop="artistName" label="歌手" />
      <el-table-column prop="albumTitle" label="所属专辑" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="adminDelete(row.id)" style="margin-left: 8px">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editDialogVisible" title="编辑歌曲" width="640px" @close="resetEditForm">
      <el-form :model="editForm" label-width="96px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="所属艺人">
          <el-select v-model="editForm.artistId" placeholder="选择艺人" filterable>
            <el-option v-for="artist in artistOptions" :key="artist.id" :label="artist.name" :value="artist.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专辑">
          <el-select v-model="editForm.albumId" placeholder="选择专辑" clearable filterable>
            <el-option v-for="album in albumOptions" :key="album.id" :label="album.title" :value="album.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="主流派">
          <el-select v-model="editForm.genre" placeholder="选择流派" filterable>
            <el-option v-for="genre in genreOptions" :key="genre.id" :label="genre.name" :value="genre.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="流派标签">
          <el-select v-model="editForm.genreIds" multiple placeholder="选择标签" filterable>
            <el-option v-for="genre in genreOptions" :key="genre.id" :label="genre.name" :value="genre.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时长 (秒)">
          <el-input-number v-model="editForm.duration" :min="0" />
        </el-form-item>
        <el-form-item label="音频链接">
          <el-input v-model="editForm.fileUrl" placeholder="请输入文件地址" />
        </el-form-item>
        <el-form-item label="封面链接">
          <el-input v-model="editForm.coverUrl" placeholder="请输入封面地址" />
        </el-form-item>
        <el-form-item label="描述 / 歌词">
          <el-input type="textarea" :rows="3" v-model="editForm.description" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-container { padding: 40px; color: white; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.title { font-size: 24px; font-weight: bold; margin: 0; }
:deep(.el-table) { background: transparent; color: #ccc; --el-table-row-hover-bg-color: #282828; --el-table-border-color: #333; }
:deep(.el-table th), :deep(.el-table tr) { background: transparent; border-bottom: 1px solid #333; }
:deep(.el-table td) { border-bottom: 1px solid #333; }
:deep(.el-input__wrapper) { background-color: #242424; }
</style>