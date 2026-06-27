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
          <span class="cat-ico" v-html="c.ico"></span>
          <span class="cat-name">{{ c.label }}</span>
          <span class="cat-arrow">›</span>
        </div>
        <!-- DIN 智能推荐入口 -->
        <div v-if="user" class="cat-item recommend-entry" @click="loadRecommendations">
          <span class="cat-ico"><svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg></span>
          <span class="cat-name">智能推荐</span>
          <span class="cat-badge">AI</span>
        </div>
      </aside>

      <!-- 中间轮播 -->
      <div class="banner-wrap">
        <div class="banner-track" :style="{ transform: `translateX(-${cur * 100}%)` }">
          <div
            v-for="(b, i) in banners" :key="i"
            class="banner-slide"
            :style="{ backgroundImage: `linear-gradient(rgba(0,0,0,0.25), rgba(0,0,0,0.45)), url(${b.image})` }"
            @click="$router.push(b.link)"
          >
            <div class="banner-text">
              <span class="banner-tag">{{ b.tag }}</span>
              <h2>{{ b.title }}</h2>
              <p>{{ b.sub }}</p>
            </div>
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
          <li><router-link to="/orders"><span><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg></span><em>我的订单</em></router-link></li>
          <li><router-link to="/cart"><span><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/></svg></span><em>购物车</em></router-link></li>
          <li><a @click="$router.push('/?kw=耳机')"><span><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="12" rx="2"/><path d="M8 20h8"/><path d="M12 16v4"/><circle cx="12" cy="9" r="1"/></svg></span><em>数码专区</em></a></li>
          <li><a @click="$router.push('/?kw=坚果')"><span><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/><circle cx="12" cy="4" r="1"/></svg></span><em>食品折扣</em></a></li>
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
    <div class="container sec-head" v-if="!active && !kw" v-reveal>
      <h3><span class="bar"></span>为你推荐</h3>
      <p>精挑细选 · 品质好物</p>
    </div>

    <!-- 商品网格 -->
    <section class="container grid" v-if="products.length">
      <article
        v-for="(p, idx) in products" :key="p.pid"
        class="gcard"
        v-reveal="(idx % 4) + 1"
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

    <!-- DIN 推荐结果弹窗 -->
    <el-dialog v-model="recDialogVisible" title="智能推荐 · 猜你喜欢" width="600px" top="10vh">
      <div v-if="recLoading" style="text-align:center;padding:40px;">
        <el-icon class="is-loading" :size="36"><svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/></svg></el-icon>
        <p style="margin-top:16px;color:#999;">AI 正在分析您的兴趣偏好...</p>
      </div>
      <div v-else-if="recError" style="text-align:center;padding:20px;color:#f56c6c;">
        <p>{{ recError }}</p>
      </div>
      <div v-else-if="recResults.length">
        <p style="color:#999;font-size:13px;margin-bottom:12px;">
          基于您的浏览和购买历史，DIN 模型为您推荐以下商品：
        </p>
        <div v-for="(r, idx) in recResults" :key="idx"
          style="display:flex;align-items:center;padding:10px 12px;margin-bottom:6px;background:#fafafa;border-radius:8px;"
        >
          <span style="width:28px;height:28px;border-radius:50%;background:var(--primary);color:#fff;display:grid;place-items:center;font-size:12px;font-weight:700;flex-shrink:0;">{{ idx + 1 }}</span>
          <div style="margin-left:12px;flex:1;">
            <div style="font-weight:600;font-size:14px;">商品 {{ r.item_id }}</div>
            <div style="font-size:12px;color:#999;">类目 {{ r.category }}</div>
          </div>
          <span style="font-size:12px;color:var(--primary);font-weight:600;">{{ (r.score * 100).toFixed(1) }}%</span>
        </div>
      </div>
      <div v-else style="text-align:center;padding:40px;color:#999;">
        <p>暂无推荐结果</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProducts, getRecommendations, type Product, type RecommendItem } from '../api'
import ProductImage from '../components/ProductImage.vue'

const route = useRoute()
const router = useRouter()

const products = ref<Product[]>([])
const active = ref('')
const kw = ref('')
const cur = ref(0)
let timer: any = null

// DIN 推荐
const recDialogVisible = ref(false)
const recLoading = ref(false)
const recError = ref('')
const recResults = ref<RecommendItem[]>([])

async function loadRecommendations() {
  recDialogVisible.value = true
  recLoading.value = true
  recError.value = ''
  recResults.value = []
  try {
    const result = await getRecommendations(user.value || undefined)
    recResults.value = result.recommendations || []
  } catch (e: any) {
    recError.value = e?.message || '推荐服务暂时不可用，请稍后再试'
  } finally {
    recLoading.value = false
  }
}

const cats = [
  { label:'全部好物', value:'', ico:'<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>' },
  { label:'运动鞋', value:'运动鞋', ico:'<svg width="20" height="20" viewBox="0 0 960 960" fill="none" stroke="currentColor" stroke-width="28" stroke-linecap="round" stroke-linejoin="round"><path d="M 821,730 Q 786,730 760,730 Q 708,730 660,726 Q 615,719 572,709 Q 528,704 483,707 Q 439,716 396,725 Q 350,728 303,725 Q 259,716 218,704 Q 179,689 143,672 Q 109,654 79,633 Q 57,605 52,570 Q 54,552 65,537 Q 92,511 129,496 Q 172,487 217,480 Q 260,470 298,455 Q 330,435 356,413 Q 380,391 403,370 Q 426,347 449,324 Q 472,301 497,277 Q 524,256 554,242 Q 585,246 611,268 Q 629,302 643,339 Q 666,367 699,377 Q 732,368 759,343 Q 783,314 811,298 Q 838,296 860,305 Q 877,331 891,367 Q 904,424 913,472 Q 916,531 914,574 Q 908,618 900,659 Q 883,694 861,716 Q 836,727 821,730 Z"/></svg>' },
  { label:'T 恤', value:'T恤', ico:'<svg width="20" height="20" viewBox="0 0 960 960" fill="none" stroke="currentColor" stroke-width="28" stroke-linecap="round" stroke-linejoin="round"><path d="M100 200 L315 140 L480 180 L645 140 L860 200 L860 460 L720 420 L720 840 Q480 900 240 840 L240 420 L100 460 Z"/></svg>' },
  { label:'数码', value:'数码', ico:'<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="12" rx="2"/><path d="M8 20h8"/><path d="M12 16v4"/><circle cx="12" cy="9" r="1"/></svg>' },
  { label:'食品', value:'食品', ico:'<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/><circle cx="12" cy="4" r="1"/></svg>' },
  { label:'家居', value:'家居', ico:'<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><path d="M9 22V12h6v10"/><circle cx="12" cy="7" r="1"/></svg>' },
]
const activeLabel = computed(() => cats.find(c => c.value === active.value)?.label || active.value)

const banners = [
  {
    tag: '数码潮品', title: '新潮数码馆', sub: '手机 · 平板 · 耳机 一站配齐',
    image: '/images/xiaomi-1.png', link: '/?cat=数码',
  },
  {
    tag: '运动装备', title: '运动风尚季', sub: '跑鞋 · T恤 · 轻装上阵 活力全开',
    image: '/images/airmax-1.png', link: '/?cat=运动鞋',
  },
  {
    tag: '零食天地', title: '环球零食铺', sub: '伊朗薯片 · 坚果礼盒 · 一口上瘾',
    image: '/images/mazmaz番茄-1.png', link: '/?cat=食品',
  },
  {
    tag: '品质家居', title: '生活美学坊', sub: '北欧台灯 · 简约收纳 · 点亮日常',
    image: '/images/lamp-1.png', link: '/?cat=家居',
  },
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
.home { padding-bottom: 24px; }

/* hero */
.hero {
  display: grid;
  grid-template-columns: 240px 1fr 280px;
  gap: 24px;
  margin-top: 32px;
  align-items: stretch;
}
/* 响应式：窄屏先折叠侧栏 */
@media (max-width: 1200px) {
  .hero { grid-template-columns: 220px 1fr; }
  .user-card { display: none; }
}
@media (max-width: 768px) {
  .hero { grid-template-columns: 1fr; gap: 16px; margin-top: 20px; }
  .cats { height: auto; max-height: none; }
  .banner-wrap { height: 220px; }
}

/* 左侧类目 */
.cats {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  padding: 12px 0;
  height: 384px;
  overflow: hidden;
}
.cat-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 22px;
  font-size: var(--fs-body);
  color: var(--text-regular);
  cursor: pointer;
  transition: all 0.35s var(--ease-soft);
}
.cat-item:hover { background: var(--primary-bg); color: var(--primary); transform: translateX(2px); }
.cat-item.on { background: var(--primary-bg); color: var(--primary); font-weight: 600; }
.cat-ico { font-size: 18px; color: var(--text-secondary); transition: color 0.35s var(--ease-soft); }
.cat-item:hover .cat-ico, .cat-item.on .cat-ico { color: var(--primary); }
.cat-name { flex: 1; }
.cat-arrow { color: var(--text-muted); transition: transform 0.35s var(--ease-soft); }
.cat-item:hover .cat-arrow { transform: translateX(3px); color: var(--primary); }

/* DIN 智能推荐入口 */
.recommend-entry {
  border-top: 1px dashed var(--border);
  margin-top: 6px;
  padding-top: 15px;
  color: var(--primary);
  font-weight: 600;
}
.recommend-entry:hover {
  background: linear-gradient(135deg, var(--primary-bg), #fff5f0);
}
.cat-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: var(--r-xs);
  background: linear-gradient(135deg, var(--primary), #ff6b3d);
  color: #fff;
  letter-spacing: 0.5px;
}

/* 中间 banner */
.banner-wrap {
  position: relative;
  border-radius: var(--r-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  height: 384px;
}
.banner-track { display: flex; height: 100%; transition: transform 0.7s var(--ease-soft); }
.banner-slide {
  flex: 0 0 100%;
  height: 100%;
  display: flex; align-items: flex-end;
  padding: 0 56px 56px;
  background-size: cover;
  background-position: center;
  cursor: pointer;
  color: #fff;
  position: relative;
}
/* ken burns 慢呼吸，高级感关键 */
@keyframes kenburns {
  0%   { transform: scale(1.02) translate(0, 0); }
  100% { transform: scale(1.10) translate(-1.5%, -1%); }
}
.banner-slide { animation: kenburns 6s ease-out both; }
.banner-slide::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(27,26,23,0.65) 0%, rgba(27,26,23,0.15) 45%, transparent 70%);
  pointer-events: none;
}
.banner-text { position: relative; z-index: 1; }
.banner-tag {
  display: inline-block;
  font-size: var(--fs-xs); font-weight: 700;
  padding: 5px 12px;
  background: rgba(255,255,255,0.18);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.28);
  border-radius: var(--r-round);
  margin-bottom: 16px;
  letter-spacing: 1.5px;
  text-transform: uppercase;
}
.banner-text h2 {
  font-size: var(--fs-display); font-weight: 800; margin-bottom: 12px;
  letter-spacing: -0.02em;
  text-shadow: 0 4px 24px rgba(0,0,0,0.4);
}
.banner-text p {
  font-size: var(--fs-body); opacity: 0.92;
  text-shadow: 0 2px 12px rgba(0,0,0,0.35);
}
.banner-dots {
  position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 8px; z-index: 2;
}
.banner-dots span {
  width: 8px; height: 8px; border-radius: 50%;
  background: rgba(255,255,255,0.5);
  cursor: pointer;
  transition: all 0.4s var(--ease-soft);
}
.banner-dots span.on { width: 28px; border-radius: var(--r-round); background: #fff; }

/* 右侧用户卡 */
.user-card {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  display: flex; flex-direction: column;
  overflow: hidden;
  min-height: 384px;
}
.user-top { padding: 28px 18px 18px; text-align: center; flex-shrink: 0; }
.avatar {
  width: 60px; height: 60px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary), var(--yellow));
  color: #fff; font-size: 24px; font-weight: 700;
  display: grid; place-items: center;
  box-shadow: 0 8px 20px rgba(255,68,0,0.28);
}
.hello { font-size: var(--fs-body); color: var(--text-primary); margin-bottom: 12px; font-weight: 600; }
.sub { display: flex; gap: 10px; justify-content: center; }
.btn-login, .btn-reg {
  padding: 7px 22px;
  border-radius: var(--r-round);
  font-size: var(--fs-sm);
  font-weight: 600;
  transition: all 0.35s var(--ease-soft);
}
.btn-login { background: var(--primary); color: #fff; box-shadow: 0 4px 12px rgba(255,68,0,0.25); }
.btn-login:hover { background: var(--primary-hover); transform: translateY(-1px); }
.btn-reg { border: 1px solid var(--primary); color: var(--primary); }
.btn-reg:hover { background: var(--primary-bg); }
.sub a { color: var(--primary); font-size: var(--fs-sm); font-weight: 500; }
.user-quick {
  flex: 1;
  display: grid; grid-template-columns: 1fr 1fr;
  border-top: 1px solid var(--border-light);
  min-height: 0;
}
.user-quick li { border-right: 1px solid var(--border-light); border-bottom: 1px solid var(--border-light); }
.user-quick li:nth-child(2n) { border-right: none; }
.user-quick li:nth-last-child(-n+2) { border-bottom: none; }
.user-quick a {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 16px 6px; gap: 7px;
  color: var(--text-regular); font-size: var(--fs-xs);
  cursor: pointer;
  text-align: center;
  transition: background 0.35s var(--ease-soft), color 0.35s var(--ease-soft);
}
.user-quick a em { font-style: normal; line-height: 1.2; }
.user-quick a:hover { color: var(--primary); background: var(--bg-tint); }
.user-quick span { font-size: 22px; }

/* 搜索回显 */
.search-hint {
  display: flex; align-items: center; gap: 10px;
  margin-top: 32px; font-size: var(--fs-sm); color: var(--text-secondary);
  padding: 16px 24px; background: var(--bg-card); border-radius: var(--r-md);
  border: 1px solid var(--border-light); box-shadow: var(--shadow-xs);
}
.chip {
  font-style: normal;
  padding: 4px 12px;
  background: var(--primary-bg); color: var(--primary);
  border-radius: var(--r-round); font-size: var(--fs-xs);
}
.clear { color: var(--primary); cursor: pointer; margin-left: 4px; font-weight: 500; }
.count { margin-left: auto; color: var(--text-muted); }

/* 推荐标题 */
.sec-head {
  display: flex; align-items: baseline; gap: 16px;
  margin-top: 72px; padding: 0 4px;
}
.sec-head h3 {
  font-size: var(--fs-h1); font-weight: 700; display: flex; align-items: center; gap: 12px;
  letter-spacing: -0.02em; color: var(--text-primary);
}
.sec-head h3 .bar { width: 6px; height: 28px; border-radius: var(--r-round); background: linear-gradient(180deg, var(--primary), var(--yellow)); }
.sec-head p { font-size: var(--fs-sm); color: var(--text-muted); }

/* 商品网格 */
.grid {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}
@media (max-width: 1100px) { .grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 900px)  { .grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 560px)  { .grid { grid-template-columns: repeat(2, 1fr); gap: 12px; } }

.gcard {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
  cursor: pointer;
  overflow: hidden;
  transition: box-shadow 0.5s var(--ease-soft), transform 0.5s var(--ease-soft);
  display: flex; flex-direction: column;
}
.gcard:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-light);
}
.gcard:hover :deep(.pimg) { transform: scale(1.05); }
.gcard :deep(.pimg) { transition: transform 0.7s var(--ease-soft); }
.gbody { padding: 16px 18px 20px; flex: 1; display: flex; flex-direction: column; }
.gname {
  font-size: var(--fs-body); font-weight: 500;
  color: var(--text-primary);
  line-height: 1.45; height: 2.9em;
  margin-bottom: 12px;
  transition: color 0.35s var(--ease-soft);
}
.gcard:hover .gname { color: var(--primary); }
.tags { display: flex; gap: 6px; margin-bottom: 12px; }
.tag-cat {
  background: var(--primary-bg); color: var(--primary);
  font-size: var(--fs-xs); padding: 3px 9px; border-radius: var(--r-xs); font-weight: 500;
}
.tag-stock {
  font-size: var(--fs-xs); color: var(--text-muted);
  padding: 3px 9px; background: var(--bg-soft); border-radius: var(--r-xs);
}
.tag-stock.out { color: var(--text-muted); }
.gprice {
  display: flex; align-items: baseline; justify-content: space-between;
  margin-top: auto;
}
.gprice .now {
  color: var(--primary); font-size: 22px; font-weight: 800;
  letter-spacing: -0.01em;
}
.gprice .now::before { content: '¥'; font-size: 14px; font-weight: 600; margin-right: 1px; }
.gprice .sales { font-size: var(--fs-xs); color: var(--text-muted); }

/* 空 */
.empty { text-align: center; padding: 112px 20px; color: var(--text-muted); }
.empty-ico { font-size: 64px; margin-bottom: 20px; opacity: 0.6; }
.empty p { margin-bottom: 20px; font-size: var(--fs-h3); color: var(--text-secondary); }
.empty-btn { color: var(--primary); font-size: var(--fs-body); cursor: pointer; border-bottom: 1px solid var(--primary); padding-bottom: 2px; }
</style>