<template>
  <div class="home">
    <!-- 主区：左侧类目 + 中间轮播 + 右侧用户卡 -->
    <section class="hero container">
      <!-- 左侧类目导航 -->
      <aside class="cats">
        <div
          v-for="c in cats" :key="c.value"
          class="cat-item" :class="{ on: active === c.value }"
          @click="filter(c.value)"
        >
          <span class="cat-ico">{{ c.ico }}</span>
          <span class="cat-name">{{ c.label }}</span>
          <span class="cat-arrow">›</span>
        </div>
      </aside>

      <!-- 中间轮播 -->
      <div class="banner-wrap">
        <div class="banner-track" :style="{ transform: `translateX(-${cur * 100}%)` }">
          <div v-for="(b, i) in banners" :key="i" class="banner-slide" :style="{ background: b.bg }">
            <div class="banner-text">
              <h2>{{ b.title }}</h2>
              <p>{{ b.sub }}</p>
              <button @click="b.action">{{ b.btn }}</button>
            </div>
            <div class="banner-ico">{{ b.ico }}</div>
          </div>
        </div>
        <div class="banner-dots">
          <span v-for="(_, i) in banners" :key="i" :class="{ on: cur === i }" @click="cur = i"></span>
        </div>
      </div>

      <!-- 右侧用户卡 -->
      <aside class="user-card">
        <div class="user-top">
          <div class="avatar">{{ userInitial }}</div>
          <div class="hello" v-if="user">Hi, {{ user }}</div>
          <div class="hello" v-else>你好, 欢迎来到悦选</div>
          <div class="sub" v-if="!user">
            <router-link to="/login" class="btn-login">登录</router-link>
            <router-link to="/login" class="btn-reg">注册</router-link>
          </div>
          <div class="sub" v-else>
            <router-link to="/orders">我的订单 ›</router-link>
          </div>
        </div>
        <ul class="user-quick">
          <li><router-link to="/orders"><span>📋</span><em>我的订单</em></router-link></li>
          <li><router-link to="/cart"><span>🛒</span><em>购物车</em></router-link></li>
          <li><a @click="$router.push('/?kw=耳机')"><span>🎧</span><em>数码专区</em></a></li>
          <li><a @click="$router.push('/?kw=坚果')"><span>🥜</span><em>食品折扣</em></a></li>
        </ul>
      </aside>
    </section>

    <!-- 搜索结果回显 -->
    <div class="container search-hint" v-if="active || kw">
      <span>当前筛选：</span>
      <em v-if="active" class="chip">{{ activeLabel }}</em>
      <em v-if="kw" class="chip">关键词「{{ kw }}」</em>
      <a class="clear" @click="clearFilter">清除</a>
      <span class="count">共 {{ products.length }} 件商品</span>
    </div>

    <!-- 推荐栏标题 -->
    <div class="container sec-head" v-if="!active && !kw">
      <h3><span class="bar"></span>为你推荐</h3>
      <p>精挑细选 · 品质好物</p>
    </div>

    <!-- 商品网格 -->
    <section class="container grid" v-if="products.length">
      <article
        v-for="p in products" :key="p.pid"
        class="gcard"
        @click="$router.push(`/product/${p.pid}`)"
      >
        <ProductImage :image="p.image" :category="p.category" :name="p.name" radius="8px 8px 0 0" />
        <div class="gbody">
          <h4 class="gname ellipsis-2">{{ p.name }}</h4>
          <div class="tags">
            <span class="tag-cat">{{ p.category }}</span>
            <span class="tag-stock" v-if="p.stock > 0">现货 {{ p.stock }}件</span>
            <span class="tag-stock out" v-else>暂时缺货</span>
          </div>
          <div class="gprice">
            <span class="now">{{ p.price }}</span>
            <span class="sales">⭐{{ p.rating }} · {{ p.reviewCount }}评</span>
          </div>
        </div>
      </article>
    </section>

    <div v-else class="container empty">
      <div class="empty-ico">🔍</div>
      <p>没有找到相关商品</p>
      <a class="empty-btn" @click="clearFilter">看看全部好物</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProducts, type Product } from '../api'
import ProductImage from '../components/ProductImage.vue'

const route = useRoute()
const router = useRouter()

const products = ref<Product[]>([])
const active = ref('')
const kw = ref('')
const cur = ref(0)
let timer: any = null

const cats = [
  { label:'全部好物', value:'', ico:'🏠' },
  { label:'运动鞋',   value:'运动鞋', ico:'👟' },
  { label:'T 恤',     value:'T恤',   ico:'👕' },
  { label:'数码',     value:'数码',  ico:'🎧' },
  { label:'食品',     value:'食品',  ico:'🥜' },
  { label:'家居',     value:'家居',  ico:'🛋️' },
]
const activeLabel = computed(() => cats.find(c => c.value === active.value)?.label || active.value)

const banners = [
  { title:'悦选年中盛典', sub:'全场好物 低至5折 · 限时抢购', btn:'立即抢购 ›', ico:'🎉', bg:'linear-gradient(135deg,#FF4400,#FF8A4C)', action: () => router.push('/?kw=跑鞋') },
  { title:'数码新潮区', sub:'耳机 · 手表 · 音箱 一站到位', btn:'去看看 ›', ico:'🎧', bg:'linear-gradient(135deg,#1A2A6C,#B21F1F,#FDBB2D)', action: () => router.push('/?kw=耳机') },
  { title:'吃货专享', sub:'坚果 · 绿茶 健康又解馋', ico:'🥜', btn:'馋了就买 ›', bg:'linear-gradient(135deg,#11998e,#38ef7d)', action: () => router.push('/?kw=坚果') },
  { title:'潮流穿搭', sub:'运动鞋 + T恤 一周不重样', ico:'👟', btn:'选我的 ›', bg:'linear-gradient(135deg,#6a11cb,#2575fc)', action: () => router.push('/?kw=跑鞋') },
]

const user = computed(() => {
  try { return JSON.parse(sessionStorage.getItem('yuexuan_user') || '')?.username } catch { return '' }
})
const userInitial = computed(() => (user.value ? user.value.slice(0, 1).toUpperCase() : 'Guest'))

async function load() {
  try {
    products.value = await getProducts(active.value || undefined, kw.value || undefined) || []
  } catch (e) {
    products.value = []
  }
}

function filter(v: string) {
  active.value = active.value === v ? '' : v
  router.replace({ path: '/', query: { ...route.query, cat: active.value || undefined } })
  load()
}
function clearFilter() {
  active.value = ''; kw.value = ''
  router.replace('/')
  load()
}

// 从 URL 同步 cat / kw
watch(() => route.query, (q) => {
  active.value = typeof q.cat === 'string' ? q.cat : ''
  kw.value = typeof q.kw === 'string' ? decodeURIComponent(q.kw) : ''
  load()
}, { immediate: true })

// 自动轮播
onMounted(() => {
  load()
  timer = setInterval(() => { cur.value = (cur.value + 1) % banners.length }, 4000)
})
onUnmounted(() => { clearInterval(timer) })
</script>

<style scoped>
.home { padding-bottom: 10px; }

/* hero */
.hero {
  display: grid;
  grid-template-columns: 220px 1fr 240px;
  gap: 16px;
  margin-top: 16px;
  align-items: stretch;
}

/* 左侧类目 */
.cats {
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: var(--shadow-card);
  padding: 8px 0;
  height: 340px;
  overflow: hidden;
}
.cat-item {
  display: flex; align-items: center; gap: 10px;
  padding: 11px 18px;
  font-size: 14px;
  color: var(--text-regular);
  cursor: pointer;
  transition: all 0.15s;
}
.cat-item:hover { background: var(--primary-bg); color: var(--primary); }
.cat-item.on { background: var(--primary-bg); color: var(--primary); font-weight: 600; }
.cat-ico { font-size: 18px; }
.cat-name { flex: 1; }
.cat-arrow { color: var(--text-muted); }

/* 中间 banner */
.banner-wrap {
  position: relative;
  border-radius: var(--r-md);
  overflow: hidden;
  box-shadow: var(--shadow-card);
  height: 340px;
}
.banner-track { display: flex; height: 100%; transition: transform 0.5s ease; }
.banner-slide {
  flex: 0 0 100%;
  height: 100%;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 60px;
  color: #fff;
}
.banner-text h2 { font-size: 38px; font-weight: 800; margin-bottom: 12px; text-shadow: 0 2px 6px rgba(0,0,0,0.15); }
.banner-text p { font-size: 16px; opacity: 0.95; margin-bottom: 22px; }
.banner-text button {
  padding: 11px 28px;
  background: rgba(255,255,255,0.95);
  color: var(--primary);
  font-size: 15px; font-weight: 700;
  border-radius: var(--r-round);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.banner-text button:hover { transform: translateY(-2px); }
.banner-ico { font-size: 140px; opacity: 0.85; }
.banner-dots {
  position: absolute; bottom: 16px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 8px;
}
.banner-dots span {
  width: 8px; height: 8px; border-radius: 50%;
  background: rgba(255,255,255,0.55);
  cursor: pointer;
  transition: all 0.2s;
}
.banner-dots span.on { width: 24px; border-radius: 4px; background: #fff; }

/* 右侧用户卡 */
.user-card {
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: var(--shadow-card);
  display: flex; flex-direction: column;
  overflow: hidden;
  height: 340px;
}
.user-top { padding: 22px 18px 16px; text-align: center; }
.avatar {
  width: 56px; height: 56px;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--yellow));
  color: #fff; font-size: 24px; font-weight: 700;
  display: grid; place-items: center;
}
.hello { font-size: 15px; color: var(--text-primary); margin-bottom: 10px; }
.sub { display: flex; gap: 8px; justify-content: center; }
.btn-login, .btn-reg {
  padding: 6px 18px;
  border-radius: var(--r-round);
  font-size: 13px;
  font-weight: 600;
}
.btn-login { background: var(--primary); color: #fff; }
.btn-reg { border: 1px solid var(--primary); color: var(--primary); }
.sub a { color: var(--primary); font-size: 13px; }
.user-quick {
  flex: 1;
  display: grid; grid-template-columns: 1fr 1fr;
  border-top: 1px solid var(--border-light);
}
.user-quick li { border-right: 1px solid var(--border-light); border-bottom: 1px solid var(--border-light); }
.user-quick li:nth-child(2n) { border-right: none; }
.user-quick li:nth-last-child(-n+2) { border-bottom: none; }
.user-quick a {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 18px 6px; gap: 6px;
  color: var(--text-regular); font-size: 12px;
  cursor: pointer;
}
.user-quick a:hover { color: var(--primary); }
.user-quick span { font-size: 22px; }

/* 搜索回显 */
.search-hint {
  display: flex; align-items: center; gap: 8px;
  margin-top: 24px; font-size: 13px; color: var(--text-secondary);
  padding: 12px 20px; background: #fff; border-radius: var(--r-md);
  border: 1px solid var(--border-light);
}
.chip {
  font-style: normal;
  padding: 3px 10px;
  background: var(--primary-bg); color: var(--primary);
  border-radius: var(--r-round); font-size: 12px;
}
.clear { color: var(--primary); cursor: pointer; margin-left: 4px; }
.count { margin-left: auto; color: var(--text-muted); }

/* 推荐标题 */
.sec-head {
  display: flex; align-items: baseline; gap: 14px;
  margin-top: 28px; padding: 0 4px;
}
.sec-head h3 { font-size: 22px; font-weight: 700; display: flex; align-items: center; gap: 10px; }
.sec-head h3 .bar { width: 4px; height: 20px; background: var(--primary); border-radius: 2px; }
.sec-head p { font-size: 13px; color: var(--text-muted); }

/* 商品网格 */
.grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
@media (max-width: 1100px) { .grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 900px)  { .grid { grid-template-columns: repeat(3, 1fr); } }

.gcard {
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: var(--shadow-card);
  cursor: pointer;
  overflow: hidden;
  transition: all 0.25s;
  display: flex; flex-direction: column;
}
.gcard:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
  border-color: var(--primary);
}
.gbody { padding: 10px 12px 14px; flex: 1; display: flex; flex-direction: column; }
.gname {
  font-size: 14px; font-weight: 500;
  color: var(--text-primary);
  line-height: 1.4; height: 2.8em;
  margin-bottom: 8px;
}
.gcard:hover .gname { color: var(--primary); }
.tags { display: flex; gap: 6px; margin-bottom: 8px; }
.tag-cat {
  background: var(--primary-bg); color: var(--primary);
  font-size: 11px; padding: 2px 7px; border-radius: var(--r-sm);
}
.tag-stock {
  font-size: 11px; color: var(--text-muted);
  padding: 2px 7px; background: var(--bg-gray); border-radius: var(--r-sm);
}
.tag-stock.out { color: #bbb; }
.gprice {
  display: flex; align-items: baseline; justify-content: space-between;
  margin-top: auto;
}
.gprice .now {
  color: var(--primary); font-size: 20px; font-weight: 800;
}
.gprice .now::before { content: '¥'; font-size: 14px; margin-right: 1px; }
.gprice .sales { font-size: 11px; color: var(--text-muted); }

/* 空 */
.empty { text-align: center; padding: 80px 20px; color: var(--text-muted); }
.empty-ico { font-size: 56px; margin-bottom: 14px; }
.empty p { margin-bottom: 18px; font-size: 15px; }
.empty-btn { color: var(--primary); font-size: 14px; cursor: pointer; border-bottom: 1px solid var(--primary); }
</style>