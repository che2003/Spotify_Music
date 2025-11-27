<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import CommentBox from '@/components/CommentBox.vue'
import SharePopover from '@/components/SharePopover.vue'
import { usePlayerStore } from '@/stores/player'

const route = useRoute()
const router = useRouter()
const song = ref<any>({})
const playerStore = usePlayerStore()
const isFollowing = ref(false)
const isLiked = ref(false)
const likeCount = ref<number | null>(null)
const userRating = ref<number>(0)
const ratingSubmitting = ref(false)
const lyrics = ref<Array<{ time: number; text: string }>>([])
const lyricRefs = ref<HTMLDivElement[]>([])
const loading = ref(true)

const activeLineIndex = computed(() => {
  const current = playerStore.currentTime
  let idx = lyrics.value.findIndex((line, index) => {
    const nextTime = lyrics.value[index + 1]?.time ?? Number.POSITIVE_INFINITY
    return current >= line.time && current < nextTime
  })
  if (idx === -1 && lyrics.value.length) idx = lyrics.value.length - 1
  return idx
})

watch(activeLineIndex, async (newIndex) => {
  await nextTick()
  const target = lyricRefs.value[newIndex]
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
})

const fetchSongDetails = async () => {
  const songId = route.params.id as string
  loading.value = true
  try {
    const res = await request.get(`/song/${songId}`)
    if (!res.data) {
      ElMessage.error('歌曲不存在')
      return
    }
    song.value = res.data
    userRating.value = 0
    if (song.value.artistId) {
      checkFollowStatus(song.value.artistId)
    }
    if (song.value.id) {
      loadLikeStatus(song.value.id)
    }
    parseLyrics(song.value.lyrics || '')
  } catch (error) {
    ElMessage.error('加载失败')
  } finally { loading.value = false }
}

const parseLyrics = (raw: string) => {
  lyricRefs.value = []
  if (!raw) { lyrics.value = []; return }

  const pattern = /\[(\d{1,2}):(\d{1,2})(?:\.(\d{1,3}))?\]/
  const parsed: Array<{ time: number; text: string }> = []

  raw.split(/\r?\n/).forEach(line => {
    const match = pattern.exec(line)
    if (match) {
      const minute = Number(match[1])
      const second = Number(match[2])
      const millisecond = match[3] ? Number(match[3]) : 0
      const time = minute * 60 + second + millisecond / 1000
      const text = line.replace(pattern, '').trim()
      parsed.push({ time, text })
    }
  })

  parsed.sort((a, b) => a.time - b.time)
  lyrics.value = parsed.length ? parsed : []
}

const goToArtist = () => { if (song.value.artistId) router.push(`/artist/${song.value.artistId}`) }
const goToAlbum = () => { if (song.value.albumId) router.push(`/album/${song.value.albumId}`) }

const handleRatingChange = async (value: number) => {
  if (!song.value.id || !value) return
  try {
    ratingSubmitting.value = true
    await request.post(`/interaction/record`, {
      songId: song.value.id,
      rating: value,
      type: 2,
    })
    ElMessage.success('感谢评分！您的反馈将帮助优化推荐')
  } catch (e) {
    ElMessage.error('评分失败，请重试')
  } finally {
    ratingSubmitting.value = false
  }
}

const loadLikeStatus = async (songId: number) => {
  try {
    const res = await request.get(`/interaction/song/status`, { params: { songId } })
    isLiked.value = res.data?.liked ?? false
    likeCount.value = res.data?.likeCount ?? null
  } catch (e) {
    isLiked.value = false
    likeCount.value = null
  }
}

const toggleLike = async () => {
  if (!song.value.id) return
  try {
    const res = await request.post(`/interaction/song/toggleLike`, null, { params: { songId: song.value.id } })
    isLiked.value = res.data?.liked ?? !isLiked.value
    likeCount.value = res.data?.likeCount ?? likeCount.value
  } catch (e) {
    ElMessage.error('操作失败，请重试')
  }
}

const checkFollowStatus = async (artistId: number) => {
  try {
    const res = await request.get(`/follow/check?artistId=${artistId}`)
    isFollowing.value = res.data
  } catch (e) {}
}
const toggleFollow = async () => {
  if (!song.value.artistId) return
  try {
    await request.post(`/follow/artist?artistId=${song.value.artistId}`)
    isFollowing.value = !isFollowing.value // 切换状态
    ElMessage.success(isFollowing.value ? '关注成功' : '已取消关注')
  } catch (e) {
    ElMessage.error('操作失败 (可能艺人未入驻)')
  }
}
const playSong = () => { if (song.value.id) playerStore.setSong(song.value) }
onMounted(() => { fetchSongDetails() })
watch(() => route.params.id, () => {
  userRating.value = 0
  fetchSongDetails()
})
</script>

<template>
  <div class="song-detail-container">
    <el-skeleton v-if="loading" :rows="6" animated />
    <div v-else-if="song.id" class="detail-wrapper">
      <div class="header-section">
        <img :src="song.coverUrl" class="song-cover" />
        <div class="info-text">
          <p class="type-label">歌曲</p>
          <h1 class="song-title">{{ song.title }}</h1>

          <div class="artist-row">
            <p class="song-artist" @click="goToArtist">{{ song.artistName || '未知歌手' }}</p>
            <el-tag v-if="song.albumTitle" type="success" effect="dark" class="album-pill" @click="goToAlbum">
              {{ song.albumTitle }}
            </el-tag>
            <el-button
                size="small"
                round
                class="follow-btn"
                :type="isFollowing ? 'default' : 'success'"
                plain
                @click="toggleFollow"
            >
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
          </div>

        </div>
      </div>

      <div class="content-section">
        <div class="lyrics-box">
          <h3 class="section-title">歌词</h3>
          <div class="lyrics-panel" v-if="lyrics.length">
            <div class="lyrics-scroll">
              <div
                  v-for="(line, index) in lyrics"
                  :key="line.time + line.text"
                  class="lyric-line"
                  :class="{ active: index === activeLineIndex }"
                  :ref="(el) => { if (el) lyricRefs[index] = el as HTMLDivElement }"
              >
                {{ line.text || '♪' }}
              </div>
            </div>
          </div>
          <div v-else class="lyrics-empty">暂无歌词</div>
        </div>

        <div class="actions">
          <div class="play-btn-large" @click="playSong">
            <svg role="img" height="28" width="28" viewBox="0 0 28 28" fill="black"><path d="M3 1.713a.7.7 0 011.05-.607l19.918 11.5a.7.7 0 010 1.214L4.05 25.319A.7.7 0 013 24.712V1.713z"></path></svg>
          </div>
          <div class="like-btn" :class="{ active: isLiked }" @click="toggleLike">
            <svg v-if="isLiked" viewBox="0 0 24 24" width="26" height="26" fill="#1db954">
              <path d="M12 21s-6.716-4.286-9.8-8.4C-0.964 8.5 1.246 3 6.2 3c2.512 0 4.2 2 4.2 2s1.688-2 4.2-2c4.954 0 7.163 5.5 4.001 9.6C18.716 16.714 12 21 12 21z" />
            </svg>
            <svg v-else viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="#fff" stroke-width="1.8">
              <path d="M12 21s-6.716-4.286-9.8-8.4C-0.964 8.5 1.246 3 6.2 3c2.512 0 4.2 2 4.2 2s1.688-2 4.2-2c4.954 0 7.163 5.5 4.001 9.6C18.716 16.714 12 21 12 21z" stroke-linejoin="round" />
            </svg>
            <span v-if="likeCount !== null" class="like-count">{{ likeCount }}</span>
          </div>
          <SharePopover
              v-if="song.id"
              resource-type="song"
              :resource-id="song.id"
              :title="song.title"
              :artist-name="song.artistName"
          />
        </div>

        <div class="rating-card" v-if="song.id">
          <div class="rating-header">
            <h3 class="section-title">为这首歌打分</h3>
            <span class="rating-hint">评分越高，推荐给你的相似歌曲会更多</span>
          </div>
          <el-rate
              v-model="userRating"
              allow-half
              show-score
              :disabled="ratingSubmitting"
              @change="handleRatingChange"
              score-template="{value} 分"
          />
        </div>

        <CommentBox :song-id="song.id" />
      </div>
    </div>
    <div v-else-if="!loading" class="lyrics-empty">歌曲不存在或已下架</div>
  </div>
</template>

<style scoped>
.song-detail-container {
  /* 模拟专辑封面色提取的渐变 */
  background: linear-gradient(to bottom, #535353 0%, #121212 500px);
  margin: -20px -30px; padding: 20px 30px; min-height: 100%;
}
.detail-wrapper { max-width: 1400px; margin: 0 auto; }
.header-section { display: flex; align-items: flex-end; margin-top: 20px; margin-bottom: 24px; padding-bottom: 24px; }
.song-cover { width: 232px; height: 232px; object-fit: cover; margin-right: 24px; box-shadow: 0 4px 60px rgba(0,0,0,0.5); }
.info-text { display: flex; flex-direction: column; color: white; }
.type-label { font-size: 12px; font-weight: 700; text-transform: uppercase; margin-bottom: 4px; }
.song-title { font-size: 72px; font-weight: 900; margin: 0 0 8px 0; line-height: 1; letter-spacing: -1px; }
.song-artist { font-size: 24px; font-weight: 700; cursor: pointer; }
.song-artist:hover { text-decoration: underline; }
.actions { margin-top: 30px; display: flex; align-items: center; gap: 12px; }
.play-btn-large { width: 56px; height: 56px; border-radius: 50%; background: var(--spotify-green); color: black; font-size: 28px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: transform 0.1s; }
.play-btn-large:hover { transform: scale(1.05); background: #1ed760; }
.like-btn { display: inline-flex; align-items: center; gap: 6px; padding: 10px; border-radius: 50%; border: 1px solid #2a2a2a; background: rgba(255,255,255,0.08); cursor: pointer; transition: 0.2s ease; color: #fff; }
.like-btn:hover { background: rgba(255,255,255,0.15); transform: translateY(-1px); }
.like-btn.active { border-color: #1db954; box-shadow: 0 0 0 1px rgba(29,185,84,0.6); }
.like-count { color: #b3b3b3; font-weight: 700; font-size: 14px; }
.content-section { margin-top: 20px; }
.lyrics-box { margin-bottom: 40px; }
.lyrics-panel { background: rgba(0,0,0,0.25); border-radius: 16px; padding: 20px; max-height: 420px; overflow: hidden; backdrop-filter: blur(6px); }
.lyrics-scroll { max-height: 380px; overflow-y: auto; padding-right: 6px; }
.lyric-line { color: #b3b3b3; font-size: 16px; font-weight: 600; line-height: 1.8; text-align: center; transition: color 0.2s, transform 0.2s; padding: 4px 0; }
.lyric-line.active { color: #fff; font-size: 18px; transform: scale(1.02); }
.lyrics-empty { color: #b3b3b3; padding: 16px 0; }
.section-title { color: white; font-weight: 800; margin-bottom: 12px; }
.artist-row {
  display: flex;
  align-items: center;
  gap: 15px;
}
.album-pill { cursor: pointer; }
.album-pill:hover { opacity: 0.9; }
.follow-btn {
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  background-color: transparent;
  border: 1px solid #878787;
  color: white;
}
.follow-btn:hover {
  border-color: white;
  transform: scale(1.05);
}
/* 如果是未关注状态 (success type)，给绿色边框 */
.el-button--success.is-plain {
  color: #1db954;
  border-color: #1db954;
  background: transparent;
}
.el-button--success.is-plain:hover {
  color: white;
  background-color: #1db954;
  border-color: #1db954;
}
.rating-card {
  margin-top: 12px;
  padding: 16px 18px;
  border-radius: 12px;
  background: rgba(255,255,255,0.04);
  border: 1px solid rgba(255,255,255,0.05);
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #fff;
}
.rating-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.rating-hint {
  color: #b3b3b3;
  font-size: 14px;
}
</style>
