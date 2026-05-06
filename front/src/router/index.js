import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn } from '@/auth'

const routes = [
  {
    path: '/',
    redirect: '/list'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录', bare: true }
  },
  {
    path: '/list',
    name: 'list',
    component: () => import('@/views/ListView.vue'),
    meta: { title: '招标公告', requiresAuth: true }
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

router.beforeEach(to => {
  // 已登录时访问登录页 -> 重定向到目标或列表页
  if (to.path === '/login' && isLoggedIn()) {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : ''
    if (redirect && redirect.startsWith('/')) return redirect
    return '/list'
  }
  // 需要鉴权的页面而未登录 -> 跳转到登录页并带上 redirect
  if (to.meta.requiresAuth && !isLoggedIn()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

router.afterEach(to => {
  const base = '招投标信息公示'
  document.title = to.meta.title ? `${to.meta.title} · ${base}` : base
})

export default router
