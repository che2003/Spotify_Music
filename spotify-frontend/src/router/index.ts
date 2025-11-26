import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import Layout from '../views/Layout.vue'
import Discover from '../views/home/Discover.vue'
import Search from '../views/SearchView.vue'
import MyLibrary from '../views/MyLibraryView.vue'
import HistoryView from '../views/home/HistoryView.vue'
import PlaylistDetail from '../views/PlaylistDetailView.vue'
import SongDetail from '../views/SongDetailView.vue'
import ArtistDetail from '../views/ArtistDetailView.vue'
import AlbumDetail from '../views/AlbumDetailView.vue'
import UploadView from '../views/musician/UploadView.vue'
import NewReleasesView from '../views/NewReleasesView.vue'
import UserManageView from '../views/admin/UserManageView.vue'
import ProfileView from '../views/user/ProfileView.vue'
import GenreBrowseView from '../views/GenreBrowseView.vue'

import ChartsView from '../views/ChartsView.vue'
import NotFoundView from '../views/NotFoundView.vue'

// 引入可能的其他页面组件，确保所有组件都已定义或导入
import DashboardView from '../views/admin/DashboardView.vue'
import MyWorksView from '../views/musician/MyWorksView.vue'
import StatsDashboard from '../views/musician/StatsDashboard.vue'
import SongManageView from '../views/admin/SongManageView.vue'
import AlbumManageView from '../views/admin/AlbumManageView.vue'
import MusicianAlbumManageView from '../views/musician/AlbumManageView.vue'

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
        { path: 'discover', name: 'discover', component: Discover, meta: { requiresAuth: true } },
        { path: 'new', name: 'newReleases', component: NewReleasesView, meta: { requiresAuth: true } },
        { path: 'search', name: 'search', component: Search, meta: { requiresAuth: true } },

        { path: 'charts', name: 'charts', component: ChartsView, meta: { requiresAuth: true } },

        { path: 'genres', name: 'genres', component: GenreBrowseView, meta: { requiresAuth: true } },
        { path: 'library', name: 'library', component: MyLibrary, meta: { requiresAuth: true } },
        { path: 'history', name: 'history', component: HistoryView, meta: { requiresAuth: true } },

        // 详情页
        { path: 'playlist/:id', name: 'playlistDetail', component: PlaylistDetail, meta: { requiresAuth: true } },
        { path: 'song/:id', name: 'songDetail', component: SongDetail, meta: { requiresAuth: true } },
        { path: 'artist/:id', name: 'artistDetail', component: ArtistDetail, meta: { requiresAuth: true } },
        { path: 'album/:id', name: 'albumDetail', component: AlbumDetail, meta: { requiresAuth: true } },

        // 个人中心
        { path: 'profile', name: 'profile', component: ProfileView, meta: { requiresAuth: true } },

        // 音乐人功能
        {
          path: 'upload',
          name: 'upload',
          component: UploadView,
          meta: { requiresAuth: true, roles: ['musician'] }
        },
        {
          path: 'musician/stats',
          name: 'musicianStats',
          component: StatsDashboard,
          meta: { requiresAuth: true, roles: ['musician'] }
        },
        {
          path: 'musician/works',
          name: 'myWorks',
          component: MyWorksView,
          meta: { requiresAuth: true, roles: ['musician'] }
        },
        {
          path: 'musician/albums',
          name: 'musicianAlbums',
          component: MusicianAlbumManageView,
          meta: { requiresAuth: true, roles: ['musician'] }
        },

        // 管理员功能
        {
          path: 'admin/users',
          name: 'adminUsers',
          component: UserManageView,
          meta: { requiresAuth: true, roles: ['admin'] }
        },
        {
          path: 'admin/songs',
          name: 'adminSongs',
          component: SongManageView,
          meta: { requiresAuth: true, roles: ['admin'] }
        },
        {
          path: 'admin/albums',
          name: 'adminAlbums',
          component: AlbumManageView,
          meta: { requiresAuth: true, roles: ['admin'] }
        },
        {
          path: 'admin/dashboard',
          name: 'adminDashboard',
          component: DashboardView,
          meta: { requiresAuth: true, roles: ['admin'] }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: NotFoundView
    }
  ]
})

// 路由守卫：未登录或权限不足时拦截
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPages = ['login', 'register', 'NotFound']
  const authRequired = !publicPages.includes((to.name as string) || '')

  if (authRequired && !token) {
    next({ name: 'login' })
    return
  }

  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const roles: string[] = Array.isArray(user.roles) ? user.roles : []
  const requiredRoles = (to.meta?.roles as string[]) || []

  if (requiredRoles.length > 0 && !requiredRoles.some((role) => roles.includes(role))) {
    next({ name: 'discover' })
    return
  }

  next()
})

export default router
