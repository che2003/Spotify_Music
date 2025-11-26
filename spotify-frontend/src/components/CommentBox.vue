<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 1. 定义组件接收的属性：需要知道当前是哪首歌的 ID
const props = defineProps<{
  songId: number | string
}>()

const comments = ref<any[]>([])
const newCommentContent = ref('')

// ------------------------------------
// 动作 1: 获取评论列表
// ------------------------------------
const fetchComments = async () => {
  if (!props.songId) return
  try {
    // 调用后端 /comment/list?songId=xxx 接口
    const res = await request.get(`/comment/list?songId=${props.songId}`)
    comments.value = res.data
  } catch (error) {
    console.error("获取评论失败", error)
  }
}

// ------------------------------------
// 动作 2: 发布新评论
// ------------------------------------
const submitComment = async () => {
  if (!newCommentContent.value.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }

  try {
    await request.post('/comment/add', {
      songId: props.songId,
      content: newCommentContent.value
    })

    ElMessage.success('评论发布成功！')
    newCommentContent.value = ''
    fetchComments() // 刷新列表
  } catch (error) {
    ElMessage.error('评论发布失败')
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<template>
  <div class="comment-box">
    <h3>评论 ({{ comments.length }})</h3>

    <div class="comment-form">
      <el-input
          v-model="newCommentContent"
          type="textarea"
          :rows="3"
          placeholder="留下你的精彩评论..."
          resize="none"
      />
      <el-button type="success" @click="submitComment" :disabled="!newCommentContent.trim()">
        发布评论
      </el-button>
    </div>

    <div class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar size="small" :src="comment.userAvatarUrl" />
        <div class="comment-content-wrapper">
          <div class="comment-header">
            <span class="comment-username">User ID: {{ comment.userId }}</span>
            <span class="comment-time">{{ new Date(comment.createTime).toLocaleString() }}</span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
        </div>
      </div>
      <p v-if="comments.length === 0" class="no-comments">还没有评论，快来抢沙发！</p>
    </div>
  </div>
</template>

<style scoped>
.comment-box {
  margin-top: 40px;
}
h3 {
  color: white;
  margin-bottom: 20px;
  font-size: 20px;
}
.comment-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 30px;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.comment-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border-bottom: 1px solid #282828;
  padding-bottom: 10px;
}
.comment-content-wrapper {
  flex-grow: 1;
  text-align: left;
}
.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #b3b3b3;
}
.comment-username {
  font-weight: bold;
  color: white;
}
.comment-text {
  margin-top: 5px;
  color: #ccc;
}
.no-comments {
  color: #b3b3b3;
  text-align: center;
}
</style>