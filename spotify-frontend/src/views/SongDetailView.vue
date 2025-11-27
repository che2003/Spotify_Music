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
    if (res.data?.liked) {
      playerStore.likedSongIds.add(song.value.id)
    } else {
      playerStore.likedSongIds.delete(song.value.id)
    }
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
const playNext = () => {
  if (!song.value.id) return
  playerStore.enqueueNext(song.value)
  ElMessage.success('已添加为下一首播放')
}
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
      <div class="floating-accent one"></div>
      <div class="floating-accent two"></div>

      <div class="hero-section">
        <div class="artwork-column">
          <div class="artwork-card">
            <div class="glow-ring"></div>
            <img :src="song.coverUrl" class="song-cover" />
          </div>

          <div class="info-panel">
            <div class="type-chip">正在播放</div>
            <h1 class="song-title">{{ song.title }}</h1>
            <p class="song-artist" @click="goToArtist">{{ song.artistName || '未知歌手' }}</p>

            <div class="meta-row">
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

            <div class="actions">
              <div class="play-btn-large" @click="playSong">
                <svg role="img" height="28" width="28" viewBox="0 0 28 28" fill="black"><path d="M3 1.713a.7.7 0 011.05-.607l19.918 11.5a.7.7 0 010 1.214L4.05 25.319A.7.7 0 013 24.712V1.713z"></path></svg>
              </div>
              <el-button class="secondary-action" round plain type="primary" @click="playNext">下一首播放</el-button>
              <div class="like-btn" :class="{ active: isLiked }" @click="toggleLike">
                <svg v-if="isLiked" viewBox="0 0 24 24" width="26" height="26" fill="#1db954">
                  <path d="M12 21s-6.716-4.286-9.8-8.4C-0.964 8.5 1.246 3 6.2 3c2.512 0 4.2 2 4.2 2s1.688-2 4.2-2c4.954 0 7.163 5.54 9.001 9.6C18.716 16.714 12 21 12 21z" />
                </svg>
                <svg v-else viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="#fff" stroke-width="1.8">
                  <path d="M12 21s-6.716-4.286-9.8-8.4C-0.964 8.5 1.246 3 6.2 3c2.512 0 4.2 2 4.2 2s1.688-2 4.2-2c4.954 0 7.163 5.54 9.001 9.6C18.716 16.714 12 21 12 21z" stroke-linejoin="round" />
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
          </div>
        </div>

        <div class="lyrics-card">
          <div class="lyrics-header">
            <div>
              <p class="eyebrow">歌词 LYRICS</p>
              <p class="lyrics-title">{{ song.title }}</p>
              <p class="lyrics-artist">— {{ song.artistName || '未知歌手' }} —</p>
            </div>
            <el-tag v-if="song.albumTitle" size="large" class="album-chip" effect="dark" @click="goToAlbum">
              {{ song.albumTitle }}
            </el-tag>
          </div>

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
      </div>

      <div class="bottom-grid" v-if="song.id">
        <div class="rating-card">
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

        <div class="comment-card">
          <h3 class="section-title">乐迷热议</h3>
          <CommentBox :song-id="song.id" />
        </div>
      </div>
    </div>
    <div v-else-if="!loading" class="lyrics-empty">歌曲不存在或已下架</div>
  </div>
</template>

<style scoped>
.song-detail-container {
  background: radial-gradient(circle at 20% 20%, rgba(29, 185, 84, 0.12), transparent 35%),
    radial-gradient(circle at 90% 10%, rgba(30, 215, 96, 0.2), transparent 28%),
    linear-gradient(135deg, #0f172a 0%, #0a0a0a 55%, #060606 100%);
  margin: -20px -30px;
  padding: 20px 30px 36px;
  min-height: 100%;
  position: relative;
  overflow: hidden;
}

.detail-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.floating-accent {
  position: absolute;
  filter: blur(70px);
  border-radius: 50%;
  opacity: 0.45;
  pointer-events: none;
}

.floating-accent.one {
  width: 280px;
  height: 280px;
  background: #1ed760;
  top: 6%;
  right: 4%;
}

.floating-accent.two {
  width: 320px;
  height: 320px;
  background: #1db954;
  bottom: 8%;
  left: -6%;
}

.hero-section {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 48px;
  align-items: center;
}

.artwork-column {
  display: grid;
  grid-template-columns: auto;
  gap: 18px;
  align-items: center;
}

.artwork-card {
  position: relative;
  background: radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.08), rgba(255, 255, 255, 0.02));
  border-radius: 28px;
  padding: 22px;
  box-shadow: 0 40px 90px rgba(0, 0, 0, 0.55);
  overflow: hidden;
}

.glow-ring {
  position: absolute;
  inset: 0;
  border-radius: 32px;
  background: linear-gradient(135deg, rgba(30, 215, 96, 0.28), rgba(24, 24, 24, 0));
  filter: blur(26px);
  z-index: 0;
}

.song-cover {
  position: relative;
  width: 100%;
  max-width: 360px;
  display: block;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 22px;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.6);
  z-index: 1;
}

.info-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: white;
  padding-left: 6px;
}

.type-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(29, 185, 84, 0.12);
  color: #1ed760;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  letter-spacing: 1px;
  text-transform: uppercase;
  font-weight: 800;
  width: fit-content;
}

.song-title {
  font-size: 54px;
  font-weight: 900;
  margin: 0;
  line-height: 1.05;
  letter-spacing: -0.5px;
}

.song-artist {
  font-size: 22px;
  color: #d9d9d9;
  margin: 0;
  cursor: pointer;
  transition: color 0.2s ease;
}

.song-artist:hover {
  color: #ffffff;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
}

.album-pill {
  cursor: pointer;
}

.album-pill:hover {
  opacity: 0.9;
}

.follow-btn {
  font-weight: 700;
  letter-spacing: 0.5px;
  background-color: transparent;
  border: 1px solid #3a3a3a;
  color: white;
}

.follow-btn:hover {
  border-color: white;
  transform: translateY(-1px);
}

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

.actions {
  margin-top: 18px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.secondary-action {
  height: 40px;
  border-radius: 999px;
}

.play-btn-large {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1ed760, #1db954);
  color: black;
  font-size: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.2s ease;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
}

.play-btn-large:hover {
  transform: translateY(-1px) scale(1.04);
  box-shadow: 0 18px 44px rgba(0, 0, 0, 0.38);
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px;
  border-radius: 50%;
  border: 1px solid #2a2a2a;
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: 0.2s ease;
  color: #fff;
}

.like-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.like-btn.active {
  border-color: #1db954;
  box-shadow: 0 0 0 1px rgba(29, 185, 84, 0.6);
}

.like-count {
  color: #b3b3b3;
  font-weight: 700;
  font-size: 14px;
}

.lyrics-card {
  background: rgba(255, 255, 255, 0.04);
  border-radius: 24px;
  padding: 22px 22px 24px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(8px);
  box-shadow: 0 30px 70px rgba(0, 0, 0, 0.5);
}

.lyrics-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #e9e9e9;
  margin-bottom: 12px;
}

.eyebrow {
  letter-spacing: 2px;
  font-size: 12px;
  color: #9ae6b4;
  margin: 0 0 4px;
}

.lyrics-title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #ffffff;
}

.lyrics-artist {
  margin: 4px 0 0;
  color: #c5c5c5;
}

.album-chip {
  cursor: pointer;
  background: #1db954;
  color: #0a0a0a;
  border: none;
}

.lyrics-panel {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0.04));
  border-radius: 18px;
  padding: 12px 14px;
  max-height: 420px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.lyrics-scroll {
  max-height: 390px;
  overflow-y: auto;
  padding-right: 6px;
}

.lyric-line {
  color: #b3b3b3;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.8;
  text-align: center;
  transition: color 0.2s, transform 0.2s, background 0.2s;
  padding: 6px 10px;
  border-radius: 10px;
}

.lyric-line.active {
  color: #0a0a0a;
  background: linear-gradient(135deg, #1ed760, #1db954);
  transform: scale(1.02);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.35);
}

.lyrics-empty {
  color: #b3b3b3;
  padding: 16px 0;
}

.section-title {
  color: white;
  font-weight: 800;
  margin-bottom: 6px;
}

.bottom-grid {
  margin-top: 28px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.rating-card,
.comment-card {
  padding: 18px 18px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(6px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.35);
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

.comment-card h3 {
  margin-top: 0;
}

@media (max-width: 1200px) {
  .hero-section {
    grid-template-columns: 1fr;
  }

  .bottom-grid {
    grid-template-columns: 1fr;
  }

  .artwork-card {
    max-width: 420px;
  }
}
</style>
