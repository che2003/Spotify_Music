import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import Layout from '../views/Layout.vue'
import Discover from '../views/home/Discover.vue'
import Search from '../views/SearchView.vue'
import MyLibrary from '../views/MyLibraryView.vue' // 导入列表页
import PlaylistDetail from '../views/PlaylistDetailView.vue' // 导入详情页

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/',
      component: Layout, // 主布局
      redirect: '/discover',
      children: [
        {
          path: 'discover',
          name: 'discover',
          component: Discover
        },
        {
          path: 'search',
          name: 'search',
          component: Search
        },
        {
          path: 'library', // 我的歌单列表页
          name: 'library',
          component: MyLibrary
        },
        {
          path: 'playlist/:id', // 歌单详情页，动态 ID
          name: 'playlistDetail',
          component: PlaylistDetail
        }
      ]
    }
  ]
})

// ... (路由守卫保持不变)

export default router