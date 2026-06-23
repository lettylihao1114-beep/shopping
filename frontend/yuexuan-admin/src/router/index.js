import { createRouter, createWebHashHistory } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

export const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/dashboard/index.vue') },
      { path: 'products',  component: () => import('@/views/product/index.vue') },
      { path: 'orders',    component: () => import('@/views/order/index.vue') },
    ]
  },
  { path: '/login', component: () => import('@/views/login/index.vue') },
]

const router = createRouter({ history: createWebHashHistory(), routes })

// 路由守卫 — 未登录跳转到登录页
const PUBLIC_PATHS = ['/login']
router.beforeEach((to, _from) => {
  const token = sessionStorage.getItem('admin_token')
  if (!token && !PUBLIC_PATHS.includes(to.path)) {
    return '/login'
  }
})

export default router
