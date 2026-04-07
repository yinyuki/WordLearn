import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'
import Login from '../views/Login.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'learn',
        name: 'Learn',
        component: () => import('../views/Learn.vue')
      },
      // ✅ 词书管理 (现在这里包含单词管理功能)
      {
        path: 'books',
        name: 'BookManager',
        component: () => import('../views/BookManager.vue'),
        meta: { title: '词书管理' }
      },
      // ❌ 注意：这里原有的 'words' (WordList) 路由已被删除，请确保不要粘贴回来
      {
        path: 'stats',
        name: 'Stats',
        component: () => import('../views/Stats.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue')
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('../views/Community.vue'),
        meta: { title: '学习社区' }
      },
      {
        path: 'rank',
        name: 'Rank',
        component: () => import('../views/Rank.vue'),
        meta: { title: '成就榜单' }
      },
      {
        path: 'mistakes',
        name: 'MistakeBook',
        component: () => import('../views/MistakeBook.vue'),
        meta: { title: '错题本' }
      },
      {
        path: 'extension-learning',
        name: 'ExtensionLearning',
        component: () => import('../views/ExtensionLearning.vue'),
        meta: { title: '拓展学习' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router