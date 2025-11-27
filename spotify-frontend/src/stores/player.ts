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
        try {
            const res = await request.post('/interaction/song/toggleLike', null, { params: { songId } })
            const liked = res.data?.liked ?? !likedSongIds.value.has(songId)
            if (liked) {
                likedSongIds.value.add(songId)
            } else {
                likedSongIds.value.delete(songId)
            }
        } catch (e) {
            console.error('切换喜欢状态失败', e)
        }
    }

    // --- 播放控制 ---
    const setSong = (song: any) => {
        if (!song) return

        // 若已在队列中，直接跳转；否则追加到队尾
        const index = playList.value.findIndex(s => s.id === song.id)
        if (index !== -1) {
            currentIndex.value = index
        } else {
            playList.value.push(song)
            currentIndex.value = playList.value.length - 1
        }

        currentSong.value = song
        isPlaying.value = true
        historyRecorded.value = false
    }

    const setQueue = (songs: any[], startIndex = 0) => {
        playList.value = songs ? [...songs] : []
        if (playList.value.length === 0) {
            currentSong.value = {}
            currentIndex.value = -1
            isPlaying.value = false
            return
        }

        const safeIndex = Math.min(Math.max(startIndex, 0), playList.value.length - 1)
        currentIndex.value = safeIndex
        currentSong.value = playList.value[safeIndex]
        isPlaying.value = true
        historyRecorded.value = false
    }

    const addSongsToQueue = (songs: any[]) => {
        if (!songs || songs.length === 0) return
        songs.forEach(song => {
            const exists = playList.value.some(item => item.id === song.id)
            if (!exists) {
                playList.value.push(song)
            }
        })

        if (currentIndex.value === -1 && playList.value.length > 0) {
            currentIndex.value = 0
            currentSong.value = playList.value[0]
            isPlaying.value = false
        }
    }

    const playAt = (index: number) => {
        if (index < 0 || index >= playList.value.length) return
        currentIndex.value = index
        currentSong.value = playList.value[index]
        isPlaying.value = true
        historyRecorded.value = false
    }

    const enqueueNext = (song: any) => {
        if (!song) return
        const existingIndex = playList.value.findIndex(s => s.id === song.id)
        const insertIndex = currentIndex.value === -1 ? 0 : currentIndex.value + 1

        if (existingIndex !== -1) {
            playList.value.splice(existingIndex, 1)
        }

        playList.value.splice(insertIndex, 0, song)

        if (currentIndex.value === -1) {
            currentIndex.value = 0
            currentSong.value = song
            isPlaying.value = true
        } else if (existingIndex === currentIndex.value) {
            currentSong.value = song
        }
    }

    const removeFromQueue = (index: number) => {
        if (index < 0 || index >= playList.value.length) return
        playList.value.splice(index, 1)

        if (playList.value.length === 0) {
            currentIndex.value = -1
            currentSong.value = {}
            isPlaying.value = false
            return
        }

        if (index < currentIndex.value) {
            currentIndex.value -= 1
        } else if (index === currentIndex.value) {
            const safeIndex = Math.min(currentIndex.value, playList.value.length - 1)
            currentIndex.value = safeIndex
            currentSong.value = playList.value[safeIndex]
        }
    }

    const reorderQueue = (from: number, to: number) => {
        if (from === to) return
        if (from < 0 || to < 0 || from >= playList.value.length || to >= playList.value.length) return

        const updated = [...playList.value]
        const [moved] = updated.splice(from, 1)
        updated.splice(to, 0, moved)

        playList.value = updated

        if (currentIndex.value === from) {
            currentIndex.value = to
        } else if (from < currentIndex.value && to >= currentIndex.value) {
            currentIndex.value -= 1
        } else if (from > currentIndex.value && to <= currentIndex.value) {
            currentIndex.value += 1
        }
    }

    const clearQueue = () => {
        playList.value = []
        currentSong.value = {}
        currentIndex.value = -1
        isPlaying.value = false
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
        setSong, setQueue, addSongsToQueue, enqueueNext, playAt, removeFromQueue, reorderQueue, clearQueue,
        togglePlay, next, prev, toggleMode, recordPlayHistory
    }
})