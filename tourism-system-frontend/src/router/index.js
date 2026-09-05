import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '@/utils/auth.js'

// 路由配置 - 区分用户端和管理员端
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '用户注册', requiresAuth: false }
  },
  // ============ 用户端（前台） ============
  {
    path: '/portal',
    component: () => import('@/views/PortalLayout.vue'),
    redirect: '/portal/spot',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'spot',
        name: 'PortalSpot',
        component: () => import('@/views/Portal.vue'),
        meta: { title: '景点浏览', requiresAuth: true }
      },
      {
        path: 'guess',
        name: 'PortalGuess',
        component: () => import('@/views/GuessYouLike.vue'),
        meta: { title: '猜你喜欢', requiresAuth: true }
      },
      {
        path: 'dashboard',
        name: 'PortalDashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '可视化大屏', requiresAuth: true }
      },
      {
        path: 'favorites',
        name: 'PortalFavorites',
        component: () => import('@/views/Favorites.vue'),
        meta: { title: '我的收藏', requiresAuth: true }
      },
      {
        path: 'history',
        name: 'PortalHistory',
        component: () => import('@/views/History.vue'),
        meta: { title: '浏览历史', requiresAuth: true }
      }
    ]
  },
  // ============ 管理员端（后台） ============
  {
    path: '/admin',
    component: () => import('@/views/Layout.vue'),
    redirect: '/admin/manage',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'manage',
        name: 'AdminManage',
        component: () => import('@/views/AdminManage.vue'),
        meta: { title: '管理员管理', requiresAuth: true }
      },
      {
        path: 'user',
        name: 'UserListManage',
        component: () => import('@/views/UserListManage.vue'),
        meta: { title: '用户管理', requiresAuth: true }
      },
      {
        path: 'city',
        name: 'CityManage',
        component: () => import('@/views/CityManage.vue'),
        meta: { title: '城市管理', requiresAuth: true }
      },
      {
        path: 'spot',
        name: 'SpotManage',
        component: () => import('@/views/SpotManage.vue'),
        meta: { title: '景点管理', requiresAuth: true }
      },
      {
        path: 'comment',
        name: 'CommentManage',
        component: () => import('@/views/CommentManage.vue'),
        meta: { title: '评论审核', requiresAuth: true }
      },
      {
        path: 'portal',
        name: 'AdminPortal',
        component: () => import('@/views/Portal.vue'),
        meta: { title: '景点浏览', requiresAuth: true }
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '可视化大屏', requiresAuth: true }
      }
    ]
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

// 创建路由实例(hash模式)
const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由错误自愈: 前端重新构建后, 旧页面引用的chunk文件已被删除,
// 动态导入会失败导致"登录成功但不跳转", 此时整页刷新一次即可拿到最新index.html
router.onError((error, to) => {
  const msg = String(error?.message || '')
  if (msg.includes('Failed to fetch dynamically imported module') || msg.includes('Importing a module script failed')) {
    window.location.href = to?.fullPath ? '/#' + to.fullPath : '/'
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()

  // 登录页不需要token
  if (to.path === '/login') {
    if (token) {
      next('/admin/manage')
    } else {
      next()
    }
    return
  }

  // 需要鉴权的页面
  if (to.meta.requiresAuth !== false) {
    if (!token) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
