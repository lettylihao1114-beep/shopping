<template>
  <header class="site-header">
    <!-- 顶部窄条 -->
    <div class="top-bar">
      <div class="container top-bar-inner">
        <span>欢迎来到悦选 · 你的智能购物伙伴</span>
        <nav class="top-links">
          <a v-if="isAdmin" @click="$router.push('/admin/dashboard')" class="admin-link">⚙ 管理后台</a>
          <router-link to="/recognize" class="ai-link">🤖 AI 识物</router-link>
          <router-link to="/service" class="service-link">💬 客服</router-link>
          <router-link to="/orders">我的订单</router-link>
          <router-link to="/cart">购物车</router-link>
          <a v-if="user" @click="logout" class="user">👤 {{ user }} · 退出</a>
          <router-link v-else to="/login">请登录</router-link>
        </nav>
      </div>
    </div>

    <!-- 主体：logo + 搜索 + 头部按钮 -->
    <div class="container header-main">
      <router-link to="/" class="logo">
        <span class="logo-mark">悦</span>
        <span class="logo-text">
          <strong>悦选</strong>
          <small>YUE SELECT</small>
        </span>
      </router-link>

      <div class="search">
        <div class="search-box">
          <input
            v-model="kw"
            placeholder="搜索你想要的商品"
            @keyup.enter="onSearch"
            @focus="showHist = true"
            @blur="showHist = false"
          />
          <button class="search-btn" @click="onSearch">搜 — 索</button>
        </div>
        <div class="hot-words">
          <span v-for="(h, i) in hotWords" :key="i" @click="$router.push('/?kw=' + encodeURIComponent(h))">{{ h }}</span>
        </div>
        <transition name="fade">
          <ul v-if="showHist && histories.length" class="search-hist">
            <li v-for="(h, i) in histories" :key="i" @click="$router.push('/?kw=' + encodeURIComponent(h))">
              <span>{{ h }}</span><em @click.stop="delHist(i)">×</em>
            </li>
          </ul>
        </transition>
      </div>

      <div class="header-actions">
        <router-link to="/cart" class="cart-btn">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/></svg>
          购物车
          <span class="cart-badge" v-if="count">{{ count }}</span>
        </router-link>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { cartCount } from '../store/cart'

const router = useRouter()
const route = useRoute()
const kw = ref('')
const showHist = ref(false)
const hotWords = ref(['跑鞋', 'T恤', '蓝牙耳机', '智能手表', '坚果零食', '台灯'])

const user = computed(() => {
  try { return JSON.parse(sessionStorage.getItem('yuexuan_user') || '')?.username } catch { return '' }
})
const isAdmin = computed(() => {
  try {
    const role = JSON.parse(sessionStorage.getItem('yuexuan_user') || '{}')?.role || ''
    return ['shop', 'admin', 'platform'].includes(role)
  } catch { return false }
})

function logout() {
  sessionStorage.removeItem('yuexuan_token')
  sessionStorage.removeItem('yuexuan_user')
  router.push('/login')
}

// 购物车实时角标
function get() { return cartCount() }
const count = ref(get())
let stop: any = null
onMounted(async () => {
  const { watchEffect } = await import('vue')
  stop = watchEffect(() => { count.value = get() })
})
onUnmounted(() => { stop?.() })

// 搜索历史（localStorage）
const HIST_KEY = 'yuexuan_search_hist'
const histories = ref<string[]>(loadHist())
function loadHist(): string[] { try { return JSON.parse(localStorage.getItem(HIST_KEY) || '[]') } catch { return [] } }
function saveHist(h: string[]) { localStorage.setItem(HIST_KEY, JSON.stringify(h)) }
function pushHist(w: string) {
  if (!w) return
  let h = loadHist().filter(x => x !== w)
  h.unshift(w)
  if (h.length > 8) h = h.slice(0, 8)
  saveHist(h); histories.value = h
}
function delHist(i: number) {
  const h = histories.value.slice()
  h.splice(i, 1); saveHist(h); histories.value = h
}

function onSearch() {
  const w = kw.value.trim()
  pushHist(w)
  // Home 通过 query 读取关键词并自动 load
  router.push('/?kw=' + encodeURIComponent(w))
}

// 外部跳转带 kw 时回填输入框
watch(() => route.query.kw, (v) => {
  if (typeof v === 'string') kw.value = decodeURIComponent(v)
}, { immediate: true })
</script>

<style scoped>
.site-header {
  background: rgba(255,255,255,0.92);
  backdrop-filter: saturate(180%) blur(12px);
  -webkit-backdrop-filter: saturate(180%) blur(12px);
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 100;
}

/* 顶部条 */
.top-bar {
  background: linear-gradient(90deg, #2A1A12, #2D2520);
  color: var(--text-muted);
  font-size: var(--fs-xs);
  height: 34px;
  line-height: 34px;
}
.top-bar-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.top-links { display: flex; gap: 20px; align-items: center; }
.top-links a { cursor: pointer; transition: color 0.3s var(--ease-soft); }
.top-links a:hover { color: #fff; }
.top-links .user { color: var(--yellow); font-weight: 500; }
.top-links .admin-link { color: #FFD500; font-weight: 600; }
.top-links .ai-link { color: #4ade80; font-weight: 600; }
.top-links .ai-link:hover { color: #86efac; }
.top-links .service-link { color: #FFD500; font-weight: 600; }
.top-links .service-link:hover { color: #FFE566; }

/* 主区 */
.header-main {
  display: flex;
  align-items: center;
  gap: 36px;
  padding: 18px 32px;
}

/* logo */
.logo { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.logo-mark {
  width: 46px; height: 46px;
  background: linear-gradient(135deg, var(--primary), var(--yellow));
  color: #fff;
  font-size: 26px; font-weight: 800;
  display: grid; place-items: center;
  border-radius: var(--r-md);
  box-shadow: 0 6px 16px rgba(255,68,0,0.32);
  transition: transform 0.4s var(--ease-soft);
}
.logo:hover .logo-mark { transform: rotate(-6deg) scale(1.05); }
.logo-text { display: flex; flex-direction: column; line-height: 1.1; }
.logo-text strong { font-size: var(--fs-h2); font-weight: 800; color: var(--text-primary); letter-spacing: -0.01em; }
.logo-text small { font-size: var(--fs-xs); color: var(--primary); letter-spacing: 2px; }

/* 搜索 */
.search { flex: 1; max-width: 580px; position: relative; }
.search-box {
  display: flex;
  border: 2px solid var(--primary);
  border-radius: var(--r-round);
  overflow: hidden;
  background: #fff;
  box-shadow: 0 4px 14px rgba(255,68,0,0.10);
  transition: box-shadow 0.35s var(--ease-soft);
}
.search-box:focus-within { box-shadow: 0 6px 18px rgba(255,68,0,0.22); }
.search-box input {
  flex: 1;
  padding: 10px 20px;
  border: none;
  font-size: var(--fs-body);
  background: transparent;
}
.search-btn {
  padding: 0 32px;
  background: linear-gradient(135deg, var(--primary), var(--yellow));
  color: #fff;
  font-size: var(--fs-body);
  font-weight: 700;
  letter-spacing: 4px;
  transition: filter 0.35s var(--ease-soft);
}
.search-btn:hover { filter: brightness(1.05); }
.hot-words {
  display: flex; gap: 16px;
  margin-top: 8px; padding: 0 20px;
  font-size: var(--fs-xs); color: var(--text-muted);
}
.hot-words span { cursor: pointer; transition: color 0.3s var(--ease-soft); }
.hot-words span:hover { color: var(--primary); }

.search-hist {
  position: absolute;
  top: calc(100% + 6px);
  left: 0; right: 0;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  box-shadow: var(--shadow-md);
  padding: 8px 0;
  z-index: 20;
}
.search-hist li {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 20px;
  font-size: var(--fs-sm); color: var(--text-regular);
  cursor: pointer;
  transition: background 0.3s var(--ease-soft);
}
.search-hist li:hover { background: var(--bg-soft); color: var(--primary); }
.search-hist em {
  width: 20px; height: 20px;
  text-align: center; line-height: 20px;
  color: var(--text-muted);
  border-radius: 50%;
  font-style: normal;
  font-size: var(--fs-sm);
  transition: background 0.3s var(--ease-soft);
}
.search-hist em:hover { background: var(--border-light); color: var(--text-primary); }

/* 购物车按钮 */
.header-actions { margin-left: auto; }
.cart-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 24px;
  border: 1px solid var(--border-strong);
  border-radius: var(--r-round);
  font-size: var(--fs-body);
  color: var(--text-regular);
  background: var(--bg-card);
  transition: all 0.35s var(--ease-soft);
}
.cart-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255,68,0,0.14);
}
.cart-badge {
  background: var(--primary);
  color: #fff;
  font-size: var(--fs-xs);
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: var(--r-round);
  display: inline-flex; align-items: center; justify-content: center;
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(255,68,0,0.3);
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s var(--ease-soft); }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 响应式：窄屏收起顶部条与热搜 */
@media (max-width: 768px) {
  .top-bar { font-size: 11px; }
  .top-links { gap: 10px; }
  .top-links .service-link { display: none; }
  .header-main { gap: 12px; padding: 12px 14px; }
  .logo-text small { display: none; }
  .search { max-width: none; }
  .hot-words { display: none; }
  .search-btn { padding: 0 16px; letter-spacing: 2px; }
  .cart-btn span:not(.cart-badge) { display: none; }
}
@media (max-width: 480px) {
  .top-bar-inner span { display: none; }
  .top-links .ai-link { display: none; }
}
</style>