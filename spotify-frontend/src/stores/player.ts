import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

export const usePlayerStore = defineStore('player', () => {
    // --- 播放器状态 ---
    const currentSong = ref<any>({})
    const isPlaying = ref(false)
    const playList = ref<any[]>([])
    const currentIndex = ref(-1)

    const duration = ref(0)
    const currentTime = ref(0)
    const volume = ref(80)
    const mode = ref<'sequence' | 'loop' | 'random'>('sequence')
    const historyRecorded = ref(false)

    // --- 喜欢列表状态 ---
    const likedSongIds = ref<Set<number>>(new Set())

    // 获取喜欢的歌曲列表
    const fetchLikedSongs = async () => {
        try {
            const res = await request.get('/interaction/liked')
            likedSongIds.value = new Set(res.data)
        } catch (e) {
            console.error("获取喜欢列表失败", e)
        }
    }

    // 切换喜欢状态
    const toggleLike = async (songId: number) => {
        if (!songId) return

        if (likedSongIds.value.has(songId)) {
            // 取消喜欢 (前端视觉去除)
            likedSongIds.value.delete(songId)
            // 真实项目中应调用 delete 接口，这里暂时略过
        } else {
            // 添加喜欢
            likedSongIds.value.add(songId)
            await request.post('/interaction/record', { songId, type: 3 })
        }
    }

    // --- 播放控制 ---
    const setSong = (song: any) => {
        if (currentSong.value.id === song.id) { togglePlay(); return }
        const index = playList.value.findIndex(s => s.id === song.id)
        if (index !== -1) currentIndex.value = index
        else {
            if (currentIndex.value === -1) currentIndex.value = 0
            playList.value.splice(currentIndex.value + 1, 0, song)
            currentIndex.value++
        }
        currentSong.value = song
        isPlaying.value = true
        historyRecorded.value = false
    }

    const togglePlay = () => { isPlaying.value = !isPlaying.value }

    const next = () => {
        if (playList.value.length === 0) return
        if (mode.value === 'random') currentIndex.value = Math.floor(Math.random() * playList.value.length)
        else {
            currentIndex.value++
            if (currentIndex.value >= playList.value.length) currentIndex.value = 0
        }
        currentSong.value = playList.value[currentIndex.value]
        isPlaying.value = true
        historyRecorded.value = false
    }

    const prev = () => {
        if (playList.value.length === 0) return
        currentIndex.value--
        if (currentIndex.value < 0) currentIndex.value = playList.value.length - 1
        currentSong.value = playList.value[currentIndex.value]
        isPlaying.value = true
        historyRecorded.value = false
    }

    const toggleMode = () => {
        if (mode.value === 'sequence') mode.value = 'loop'
        else if (mode.value === 'loop') mode.value = 'random'
        else mode.value = 'sequence'
    }

    const recordInteraction = (songId: number, type: number) => {
        request.post('/interaction/record', { songId, type }).catch(() => {})
    }

    const recordPlayHistory = async () => {
        if (!currentSong.value.id || historyRecorded.value) return
        try {
            await request.post('/history/record', { songId: currentSong.value.id })
            historyRecorded.value = true
        } catch (e) {
            console.error('记录播放历史失败', e)
        }
    }

    return {
        currentSong, isPlaying, playList, currentIndex,
        duration, currentTime, volume, mode, historyRecorded,
        likedSongIds, fetchLikedSongs, toggleLike,
        setSong, togglePlay, next, prev, toggleMode, recordPlayHistory
    }
})