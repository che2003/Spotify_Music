<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

interface Props {
  resourceType: 'song' | 'playlist'
  resourceId: number | string
  title: string
  artistName?: string
}

const props = defineProps<Props>()

const shareUrl = computed(() => {
  const origin = typeof window !== 'undefined' ? window.location.origin : ''
  const typePath = props.resourceType === 'song' ? 'song' : 'playlist'
  return `${origin}/${typePath}/${props.resourceId}`
})

const shareText = computed(() => {
  if (props.resourceType === 'song') {
    return `一起听《${props.title}》${props.artistName ? ' - ' + props.artistName : ''}`
  }
  return `快来收听我整理的歌单《${props.title}》`
})

const embedHeight = computed(() => (props.resourceType === 'song' ? 160 : 260))
const embedCode = computed(() => `<iframe src="${shareUrl.value}?embed=true" width="100%" height="${embedHeight.value}" frameborder="0" allow="autoplay; clipboard-write; encrypted-media"></iframe>`)

const copyText = async (text: string, message: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(message)
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const shareNative = async () => {
  if (navigator.share) {
    try {
      await navigator.share({ title: props.title, text: shareText.value, url: shareUrl.value })
      return
    } catch (error) {
      // 用户取消分享或浏览器不支持
    }
  }
  copyText(shareUrl.value, '已复制分享链接')
}

const openSocial = (platform: 'weibo' | 'twitter' | 'facebook') => {
  const url = encodeURIComponent(shareUrl.value)
  const text = encodeURIComponent(shareText.value)
  let shareLink = ''
  switch (platform) {
    case 'weibo':
      shareLink = `https://service.weibo.com/share/share.php?title=${text}&url=${url}`
      break
    case 'twitter':
      shareLink = `https://twitter.com/intent/tweet?text=${text}&url=${url}`
      break
    case 'facebook':
      shareLink = `https://www.facebook.com/sharer/sharer.php?u=${url}`
      break
  }
  window.open(shareLink, '_blank', 'noopener')
}
</script>

<template>
  <el-popover
      width="420"
      placement="top"
      trigger="click"
      popper-class="share-popover"
  >
    <template #reference>
      <button class="share-trigger" aria-label="分享">
        <svg role="img" height="18" width="18" viewBox="0 0 24 24" fill="currentColor">
          <path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7a2.5 2.5 0 000-1.39l7-4.11A3 3 0 0021 4a3 3 0 10-3 3c.39 0 .76-.08 1.1-.21l-7 4.12a3 3 0 100 4.18l7.12 4.18c-.22.35-.22.35-.22.35A3 3 0 1018 16.08z"/>
        </svg>
        <span>分享</span>
      </button>
    </template>

    <div class="share-panel">
      <div class="share-header">
        <div class="share-title">分享{{ props.resourceType === 'song' ? '歌曲' : '歌单' }}</div>
        <div class="share-sub">{{ shareText }}</div>
      </div>

      <div class="share-row">
        <div class="label">分享链接</div>
        <div class="value-box">
          <span class="mono">{{ shareUrl }}</span>
          <el-button size="small" type="success" plain @click="shareNative">一键分享</el-button>
          <el-button size="small" plain @click="copyText(shareUrl, '已复制分享链接')">复制链接</el-button>
        </div>
      </div>

      <div class="share-row">
        <div class="label">嵌入代码</div>
        <div class="value-box">
          <code class="mono">{{ embedCode }}</code>
          <el-button size="small" plain @click="copyText(embedCode, 'Embed 代码已复制')">复制 Embed</el-button>
        </div>
      </div>

      <div class="share-social">
        <span class="label">社交平台</span>
        <div class="social-actions">
          <el-button size="small" plain @click="openSocial('weibo')">微博</el-button>
          <el-button size="small" plain @click="openSocial('twitter')">Twitter</el-button>
          <el-button size="small" plain @click="openSocial('facebook')">Facebook</el-button>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>
.share-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #2a2a2a;
  border: 1px solid #3a3a3a;
  color: #e5e5e5;
  padding: 8px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, transform 0.1s;
}

.share-trigger:hover {
  background: #333;
  border-color: #4a4a4a;
  transform: translateY(-1px);
}

.share-panel {
  color: #e5e5e5;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.share-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.share-title { font-weight: 800; color: white; }
.share-sub { color: #b3b3b3; font-size: 12px; }

.share-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label { color: #b3b3b3; font-size: 12px; text-transform: uppercase; letter-spacing: 1px; }
.value-box {
  background: #1f1f1f;
  border: 1px solid #2c2c2c;
  border-radius: 8px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mono {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  word-break: break-all;
  color: #d1ffd6;
}

.share-social {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.social-actions { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
