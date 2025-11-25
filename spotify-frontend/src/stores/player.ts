import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePlayerStore = defineStore('player', () => {
    // 1. 当前播放的歌曲信息
    const currentSong = ref<any>({})
    // 2. 是否正在播放
    const isPlaying = ref(false)
    // 3. 歌曲总时长(秒)
    const duration = ref(0)
    // 4. 当前播放进度(秒)
    const currentTime = ref(0)

    // 动作：设置并播放歌曲
    const setSong = (song: any) => {
        currentSong.value = song
        isPlaying.value = true
    }

    // 动作：暂停/播放切换
    const togglePlay = () => {
        isPlaying.value = !isPlaying.value
    }

    return {
        currentSong,
        isPlaying,
        duration,
        currentTime,
        setSong,
        togglePlay
    }
})