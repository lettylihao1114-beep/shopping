import { createApp } from 'vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import './style.css'
import App from './App.vue'
import HomePage from './views/HomePage.vue'
import ProductDetail from './views/ProductDetail.vue'
import Cart from './views/Cart.vue'
import ConfirmOrder from './views/ConfirmOrder.vue'
import PayResult from './views/PayResult.vue'
import Orders from './views/Orders.vue'
import Login from './views/Login.vue'

const routes = [
  { path: '/', name: 'home', component: HomePage },
  { path: '/product/:id', name: 'product', component: ProductDetail },
  { path: '/cart', name: 'cart', component: Cart },
  { path: '/confirm', name: 'confirm', component: ConfirmOrder },
  { path: '/result', name: 'result', component: PayResult },
  { path: '/orders', name: 'orders', component: Orders },
  { path: '/login', name: 'login', component: Login },
]

const router = createRouter({ history: createWebHashHistory(), routes })

// Day10+: 路由守卫 — 未登录强制跳转登录页
const PUBLIC_PATHS = ['/login']
router.beforeEach((to, _from) => {
  const token = sessionStorage.getItem('yuexuan_token')
  if (!token && !PUBLIC_PATHS.includes(to.path)) {
    return '/login'
  }
})

createApp(App).use(router).mount('#app')
