<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
interface GenreOption {
  id: number
  name: string
}

const uploadUrl = '/api/storage/upload' // 对应后端 StorageController
const fileList = ref([])
const coverList = ref([])
const genreOptions = ref<GenreOption[]>([])

const form = reactive({
  title: '',
  genre: '',
  genreIds: [] as number[],
  description: '',
  artistId: 1, // 暂时硬编码，理想情况应从当前登录的 Musician 信息获取
  albumId: 1,  // 暂时硬编码
  fileUrl: '',
  coverUrl: '',
  duration: 0
})

const loadGenres = async () => {
  try {
    const res = await request.get('/genre/list')
    genreOptions.value = res.data || []
    if (!form.genre && genreOptions.value.length > 0) {
      form.genre = genreOptions.value[0].name
    }
  } catch (error) {
    console.error('加载流派失败', error)
  }
}

onMounted(() => {
  loadGenres()
})

// 上传 MP3 成功回调
const handleAudioSuccess = (response: any) => {
  if (response.code === 200) {
    form.fileUrl = response.data // 后端返回的完整静态资源 URL
    ElMessage.success('音频上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// 上传封面成功回调
const handleCoverSuccess = (response: any) => {
  if (response.code === 200) {
    form.coverUrl = response.data
    ElMessage.success('封面上传成功')
  }
}

// 提交歌曲信息
const submitSong = async () => {
  if (!form.title || !form.fileUrl) {
    ElMessage.warning('请填写歌名并上传音频文件')
    return
  }

  try {
    const payload = {
      ...form,
      description: form.description,
      genreIds: form.genreIds
    }

    // 调用 SongController.add
    await request.post('/song/add', payload)
    ElMessage.success('歌曲发布成功！')
    router.push('/library') // 发布完跳转到资料库
  } catch (error) {
    console.error(error)
    ElMessage.error('发布失败')
  }
}
</script>

<template>
  <div class="upload-container">
    <h2 class="title">发布新歌曲</h2>

    <el-form :model="form" label-position="top" class="upload-form">

      <el-form-item label="歌曲标题">
        <el-input v-model="form.title" placeholder="请输入歌名" size="large" />
      </el-form-item>

      <el-form-item label="主流派 (Genre)">
        <el-select
            v-model="form.genre"
            placeholder="选择流派"
            size="large"
            filterable
            style="width: 100%"
        >
          <el-option
              v-for="item in genreOptions"
              :key="item.id"
              :label="item.name"
              :value="item.name"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="流派标签">
        <el-select
            v-model="form.genreIds"
            placeholder="可多选标签"
            multiple
            filterable
            size="large"
            style="width: 100%"
        >
          <el-option
              v-for="item in genreOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
          />
        </el-select>
        <div class="helper-text">选择多个标签可以帮助听众更快发现你的作品</div>
      </el-form-item>

      <el-form-item label="音频文件 (MP3)">
        <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :limit="1"
            :on-success="handleAudioSuccess"
            :file-list="fileList"
            accept=".mp3,.wav"
        >
          <el-button type="primary">点击上传音频</el-button>
          <template #tip>
            <div class="el-upload__tip">只能上传 mp3/wav 文件，且不超过 50MB</div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="歌曲封面">
        <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :limit="1"
            :on-success="handleCoverSuccess"
            :file-list="coverList"
            list-type="picture"
            accept=".jpg,.png,.jpeg"
        >
          <el-button>点击上传封面</el-button>
        </el-upload>
      </el-form-item>

      <el-form-item label="歌曲描述 / 创作故事">
        <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="介绍歌曲的创作背景、风格或想传达的情绪"
        />
      </el-form-item>

      <div class="submit-btn-wrapper">
        <el-button type="success" size="large" round class="submit-btn" @click="submitSong">
          发布歌曲
        </el-button>
      </div>

    </el-form>
  </div>
</template>

<style scoped>
.upload-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 40px;
  background-color: #181818;
  border-radius: 8px;
}

.title {
  color: white;
  text-align: center;
  margin-bottom: 30px;
  font-weight: 700;
}

/* 覆盖 Element 表单样式以适应深色主题 */
:deep(.el-form-item__label) {
  color: white !important;
}
:deep(.el-upload__tip) {
  color: #b3b3b3;
}
.submit-btn-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
.submit-btn {
  width: 200px;
  font-weight: bold;
  letter-spacing: 1px;
}
.helper-text {
  color: #b3b3b3;
  margin-top: 6px;
  font-size: 13px;
}
</style>