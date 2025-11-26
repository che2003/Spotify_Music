<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

interface PublicUser {
  id: number
  username: string
  nickname?: string
  avatarUrl?: string
}

interface PublicProfile {
  user: PublicUser
  playlists: any[]
  likedArtists: any[]
  following: PublicUser[]
  fans: PublicUser[]
}

const route = useRoute()
const profile = ref<PublicProfile | null>(null)
const loading = ref(false)
const following = ref(false)
const currentUserId = ref<number | null>(null)

const displayName = computed(() => profile.value?.user?.nickname || profile.value?.user?.username || '用户')
const isSelf = computed(() => profile.value?.user?.id && currentUserId.value === profile.value.user.id)

const fetchCurrentUser = async () => {
  try {
    const res = await request.get('/user/profile')
    currentUserId.value = res.data.id
  } catch (error) {
    console.error(error)
  }
}

const refreshFollowState = () => {
  if (!profile.value || !currentUserId.value) {
    following.value = false
    return
  }
  following.value = profile.value.fans?.some((fan) => fan.id === currentUserId.value) || false
}

const fetchProfile = async (userId: string) => {
  loading.value = true
  try {
    const res = await request.get(`/user/public/${userId}`)
    profile.value = res.data
    refreshFollowState()
  } catch (error) {
    ElMessage.error('无法加载用户信息')
  } finally {
    loading.value = false
  }
}

const toggleFollow = async () => {
  if (!profile.value?.user?.id) return
  try {
    const res = await request.post(`/follow/user/toggle?followedUserId=${profile.value.user.id}`)
    following.value = res.data === true
    await fetchProfile(String(profile.value.user.id))
    ElMessage.success(following.value ? '已关注该用户' : '已取消关注')
  } catch (error) {
    console.error(error)
  }
}

onMounted(async () => {
  await fetchCurrentUser()
  if (route.params.id) {
    await fetchProfile(route.params.id as string)
  }
})

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      fetchProfile(newId as string)
    }
  }
)
</script>

<template>
  <div class="public-profile" v-loading="loading">
    <div v-if="profile" class="profile-header">
      <el-avatar :size="100" :src="profile.user.avatarUrl || ''" class="avatar" />
      <div class="user-meta">
        <div class="name-row">
          <h2>{{ displayName }}</h2>
          <el-button v-if="!isSelf" type="success" :plain="!following" @click="toggleFollow">
            {{ following ? '已关注' : '关注' }}
          </el-button>
        </div>
        <p class="username">@{{ profile.user.username }}</p>
        <div class="stats">
          <span>歌单 {{ profile.playlists?.length || 0 }}</span>
          <span>关注 {{ profile.following?.length || 0 }}</span>
          <span>粉丝 {{ profile.fans?.length || 0 }}</span>
        </div>
      </div>
    </div>

    <div v-if="profile" class="profile-content">
      <section>
        <div class="section-header">
          <h3>公开歌单</h3>
        </div>
        <div class="card-grid">
          <div v-for="pl in profile.playlists" :key="pl.id" class="card">
            <img :src="pl.coverUrl || 'https://via.placeholder.com/160/1db954/FFFFFF?text=P'" class="card-cover" />
            <div class="card-title">{{ pl.title }}</div>
          </div>
          <div v-if="!profile.playlists || profile.playlists.length === 0" class="empty">暂无歌单</div>
        </div>
      </section>

      <section>
        <div class="section-header">
          <h3>喜欢的艺人</h3>
        </div>
        <div class="card-grid">
          <div v-for="artist in profile.likedArtists" :key="artist.id" class="card">
            <img :src="artist.avatarUrl || 'https://via.placeholder.com/160/1db954/FFFFFF?text=A'" class="card-cover" />
            <div class="card-title">{{ artist.name }}</div>
          </div>
          <div v-if="!profile.likedArtists || profile.likedArtists.length === 0" class="empty">暂无喜欢的艺人</div>
        </div>
      </section>

      <section class="relations">
        <div class="section-header">
          <h3>关注</h3>
        </div>
        <div class="chip-list">
          <div v-for="user in profile.following" :key="user.id" class="chip">{{ user.nickname || user.username }}</div>
          <div v-if="!profile.following || profile.following.length === 0" class="empty">暂无关注</div>
        </div>
      </section>

      <section class="relations">
        <div class="section-header">
          <h3>粉丝</h3>
        </div>
        <div class="chip-list">
          <div v-for="user in profile.fans" :key="user.id" class="chip">{{ user.nickname || user.username }}</div>
          <div v-if="!profile.fans || profile.fans.length === 0" class="empty">暂无粉丝</div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.public-profile {
  padding: 32px;
  color: white;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
}

.avatar {
  border: 2px solid var(--spotify-green);
}

.user-meta h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.username {
  margin: 6px 0;
  color: #b3b3b3;
}

.stats {
  display: flex;
  gap: 16px;
  color: #b3b3b3;
  font-size: 14px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-content section {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.card {
  background: #181818;
  padding: 12px;
  border-radius: 8px;
  text-align: center;
}

.card-cover {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 6px;
  margin-bottom: 10px;
}

.card-title {
  color: white;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty {
  color: #777;
  grid-column: 1 / -1;
}

.relations .chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip {
  background: #181818;
  padding: 8px 12px;
  border-radius: 16px;
  border: 1px solid #282828;
}
</style>
