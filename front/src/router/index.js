import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/auth'

const routes = [
  {
    path: '/',
    redirect: () => (isLoggedIn() ? '/list' : '/login')
  },
  {
    path: '/home',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/list',
    name: 'list',
    component: () => import('@/views/ListView.vue'),
    meta: { title: '招标公告', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/detail/:id',
    name: 'detail',
    component: () => import('@/views/DetailView.vue'),
    meta: { title: '公告详情', requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  if (to.name === 'login' && isLoggedIn()) {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : '/list'
    return redirect.startsWith('/') ? redirect : '/list'
  }
  if (to.meta.requiresAuth && !isLoggedIn()) {
    return {
      name: 'login',
      query: { redirect: to.fullPath }
    }
  }
  return true
})

router.afterEach((to) => {
  const base = '招投标信息公示'
  document.title = to.meta.title ? `${to.meta.title} · ${base}` : base
})

export default router
