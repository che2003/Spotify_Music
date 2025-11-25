import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router' // 引入路由实例

// 1. 创建 axios 实例
const request = axios.create({
    baseURL: '/api',
    timeout: 5000
})

// 2. 请求拦截器 (自动带上 Token，保持不变)
request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
}, error => {
    return Promise.reject(error)
})

// 3. 响应拦截器 (核心修改：处理登录过期和权限不足)
request.interceptors.response.use(
    response => {
        const res = response.data
        // 如果后端返回 code 不是 200，说明业务逻辑出错（比如密码错误）
        if (res.code !== 200) {
            ElMessage.error(res.msg || '系统错误')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    error => {
        // --- JWT 过期/权限不足的判断逻辑 ---
        if (error.response) {
            const status = error.response.status

            if (status === 401 || status === 403) {
                // 1. 清空本地存储 (Token/用户信息)
                localStorage.clear()

                // 2. 提示用户并强制跳转到登录页
                ElMessage.warning('登录已过期，请重新登录！')

                // 避免在登录页再次触发跳转
                if (router.currentRoute.value.name !== 'login') {
                    router.push('/login')
                }
                // 终止后续操作
                return Promise.reject(new Error('JWT expired or unauthorized'))
            }

            // 处理其他 HTTP 错误 (404, 500, etc.)
            let msg = '网络请求失败或服务器错误'
            switch (status) {
                case 404: msg = '接口不存在，请检查 URL'; break
                case 500: msg = '服务器内部错误'; break
            }
            ElMessage.error(msg)
        }

        return Promise.reject(error)
    }
)

export default request