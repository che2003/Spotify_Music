import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import Layout from '../views/Layout.vue'
import Discover from '../views/home/Discover.vue'
import Search from '../views/SearchView.vue'
import MyLibrary from '../views/MyLibraryView.vue'
import PlaylistDetail from '../views/PlaylistDetailView.vue'
import SongDetail from '../views/SongDetailView.vue'
import ArtistDetail from '../views/ArtistDetailView.vue'
import AlbumDetail from '../views/AlbumDetailView.vue'
import UploadView from '../views/musician/UploadView.vue'
import UserManageView from '../views/admin/UserManageView.vue'
import ProfileView from '../views/user/ProfileView.vue'
// 引入可能的其他页面组件，确保所有组件都已定义或导入
import DashboardView from '../views/admin/DashboardView.vue'
import MyWorksView from '../views/musician/MyWorksView.vue'
import SongManageView from '../views/admin/SongManageView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/',
      component: Layout, // 主布局
      redirect: '/discover',
      children: [
        { path: 'discover', name: 'discover', component: Discover },
        { path: 'search', name: 'search', component: Search },
        { path: 'library', name: 'library', component: MyLibrary },

        // 详情页
        { path: 'playlist/:id', name: 'playlistDetail', component: PlaylistDetail },
        { path: 'song/:id', name: 'songDetail', component: SongDetail },
        { path: 'artist/:id', name: 'artistDetail', component: ArtistDetail },
        { path: 'album/:id', name: 'albumDetail', component: AlbumDetail },

        // 个人中心
        { path: 'profile', name: 'profile', component: ProfileView },

        // 音乐人功能
        { path: 'upload', name: 'upload', component: UploadView },
        { path: 'musician/works', name: 'myWorks', component: MyWorksView },

        // 管理员功能
        { path: 'admin/users', name: 'adminUsers', component: UserManageView },
        { path: 'admin/songs', name: 'adminSongs', component: SongManageView },
        { path: 'admin/dashboard', name: 'adminDashboard', component: DashboardView }
      ]
    },
    // 404 路由 (可选)
    // { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFoundView.vue') }
  ]
})

// 路由守卫：未登录拦截
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPages = ['login', 'register'] // 白名单
  const authRequired = !publicPages.includes(to.name as string)

  if (authRequired && !token) {
    next({ name: 'login' })
  } else {
    next()
  }
})

export default router