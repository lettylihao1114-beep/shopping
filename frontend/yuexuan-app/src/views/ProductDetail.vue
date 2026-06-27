<template>
  <div class="container detail" v-if="p">
    <!-- 面包屑 -->
    <nav class="crumb">
      <a @click="$router.push('/')">首页</a>
      <span>›</span>
      <a @click="$router.push('/?cat=' + encodeURIComponent(p.category))">{{ p.category }}</a>
      <span>›</span>
      <span class="ellipsis">{{ p.name }}</span>
    </nav>

    <div class="layout" v-reveal>
      <!-- 左：主图 + 缩略图 -->
      <div class="gallery">
        <div class="main-img">
          <ProductImage :image="thumbs[mainIdx]" :category="p.category" :name="p.name" radius="8px" />
          <div class="ribbon">悦选自营</div>
        </div>
        <div class="thumbs">
          <div
            v-for="(t, i) in thumbs" :key="i"
            class="thumb"
            :class="{ on: mainIdx === i }"
            @click="mainIdx = i"
          >
            <ProductImage :image="t" :category="p.category" :name="p.name" radius="4px" />
          </div>
          <div class="thumb more" v-if="!p.image"><span>更多图<br>暂无</span></div>
        </div>
        <div class="share">
          <span>💬 收藏</span><span>📤 分享</span><span>🛡️ 正品保障</span>
        </div>
      </div>

      <!-- 右：信息 -->
      <div class="info">
        <h1 class="title">{{ p.name }}</h1>
        <p class="desc">{{ p.description || '精选好物，品质之选。悦选严选，每一件都经过严格筛选，让您买得放心、用得舒心。' }}</p>

        <!-- 价格区 -->
        <div class="price-box">
          <div class="price-row">
            <span class="lbl">悦选价</span>
            <span class="big-price">{{ p.price }}</span>
            <span class="market" v-if="marketPrice">¥{{ marketPrice }}</span>
            <span class="discount-tag">打折</span>
          </div>
          <div class="price-meta">
            <span>月销 {{ p.reviewCount || 0 }}+件</span>
            <span>累计评价 {{ p.reviewCount || 0 }}</span>
            <span class="rating" v-if="p.rating">评分 ⭐{{ p.rating }}</span>
          </div>
        </div>

        <!-- 参数 -->
        <div class="props">
          <div class="prop"><span class="k">分类</span><span class="v">{{ p.category }}</span></div>
          <div class="prop"><span class="k">库存</span><span class="v" :class="{ out: p.stock <= 0 }">{{ p.stock > 0 ? p.stock + ' 件（现货）' : '暂时缺货' }}</span></div>
          <div class="prop"><span class="k">发货地</span><span class="v">广东 · 广州</span></div>
          <div class="prop"><span class="k">运费</span><span class="v" style="color:#22c55e">免运费 · 211 限时达</span></div>
        </div>

        <!-- 数量 + 购买 -->
        <div class="buy-row">
          <span class="lbl">数量</span>
          <div class="qty">
            <button @click="dec">-</button>
            <input v-model.number="qty" type="number" min="1" :max="p.stock||999" />
            <button @click="inc">+</button>
          </div>
          <span class="stock-tip">库存 {{ p.stock }} 件</span>
        </div>

        <div class="actions">
          <button class="btn-cart" @click="addCart" :disabled="p.stock <= 0">🛒 加入购物车</button>
          <button class="btn-buy" @click="buyNow" :disabled="p.stock <= 0">立即购买</button>
        </div>

        <!-- 服务保障 -->
        <div class="guarantee">
          <span>✅ 正品保障</span>
          <span>🔄 七天无理由</span>
          <span>⚡ 极速发货</span>
          <span>💳 安全支付</span>
        </div>
      </div>
    </div>

    <!-- 详情说明 -->
    <section class="detail-tabs">
      <nav class="tabs">
        <span :class="{ on: tab==='desc' }" @click="tab='desc'">商品详情</span>
        <span :class="{ on: tab==='review' }" @click="tab='review'">累计评价 {{ p.reviewCount || 0 }}</span>
      </nav>
      <div class="tab-desc" v-if="tab==='desc'">
        <h3>商品介绍</h3>
        <p>{{ p.description || '本商品由悦选严选，采用优质材料，工艺精良，品质有保障。适合日常使用，是您生活品质的好帮手。' }}</p>
        <ul class="param-list">
          <li><span>商品名称</span><span>{{ p.name }}</span></li>
          <li><span>商品分类</span><span>{{ p.category }}</span></li>
          <li><span>商品库存</span><span>{{ p.stock }} 件</span></li>
          <li><span>商品评分</span><span>⭐ {{ p.rating || '暂无' }}</span></li>
          <li><span>累计评价</span><span>{{ p.reviewCount || 0 }} 条</span></li>
        </ul>
      </div>
      <div class="tab-review" v-else>
        <div class="review-summary">
          <div class="rs-score">
            <strong>{{ (p.rating || 5).toFixed(1) }}</strong>
            <span>商品评分</span>
          </div>
          <div class="rs-bars">
            <div><span>好评</span><div class="bar"><i style="width:92%"></i></div><em>92%</em></div>
            <div><span>中评</span><div class="bar"><i style="width:6%"></i></div><em>6%</em></div>
            <div><span>差评</span><div class="bar"><i style="width:2%"></i></div><em>2%</em></div>
          </div>
        </div>
        <div class="review-list">
          <div class="review" v-for="(r, i) in mockReviews" :key="i">
            <div class="r-head">
              <span class="r-user">{{ r.user }}</span>
              <span class="r-stars">⭐⭐⭐⭐⭐</span>
            </div>
            <p class="r-body">{{ r.text }}</p>
            <div class="r-meta">{{ r.time }} · {{ r.sku }}</div>
          </div>
        </div>
      </div>
    </section>

    <transition name="toast">
      <div class="toast" v-if="msg">{{ msg }}</div>
    </transition>
  </div>

  <div v-else class="container loading">
    <div class="spinner" aria-label="加载中"></div>
    <p>商品信息加载中…</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductById, type Product } from '../api'
import { addToCart } from '../store/cart'
import ProductImage from '../components/ProductImage.vue'

const route = useRoute()
const router = useRouter()
const p = ref<Product | null>(null)
const qty = ref(1)
const mainIdx = ref(0)
const msg = ref('')
const tab = ref<'desc' | 'review'>('desc')

const thumbs = computed(() => {
  // 根据主图名生成 4 张缩略图（如 xiaomi-1.png → xiaomi-2/3/4.png）
  const base = p.value?.image || ''
  if (!base) return ['', '', '', '']
  const m = base.match(/^(.+?-)(\d+)(\.[^.]+)$/)
  if (!m) return [base, '', '', '']
  return [1,2,3,4].map(n => m[1] + n + m[3])
})

const marketPrice = computed(() => p.value ? Math.round(p.value.price * 1.18) : 0)

const _user = computed(() => {
  try { return JSON.parse(sessionStorage.getItem('yuexuan_user') || '')?.username } catch { return '' }
})

const mockReviews = [
  { user: '悦***选', text: '收到货了,质量非常好,包装精致,物流速度快,客服态度也很好,非常满意的一次购物体验!', time: '2026-06-15', sku: '默认规格' },
  { user: 'user***88', text: '性价比超高,比想象中要好,做工扎实,细节到位,会回购的。', time: '2026-06-10', sku: '默认规格' },
  { user: '匿名***客', text: '一直在悦选买,品质有保障,物流也快,好评!', time: '2026-06-02', sku: '默认规格' },
]

onMounted(async () => {
  try {
    const res: any = await getProductById(Number(route.params.id))
    p.value = res
  } catch (e) {}
})

function inc() { if (p.value && qty.value < (p.value.stock || 999)) qty.value++ }
function dec() { if (qty.value > 1) qty.value-- }

function addCart() {
  if (!p.value || p.value.stock <= 0) return
  addToCart(p.value.pid, p.value.name, p.value.price, p.value.category, p.value.image || '', qty.value)
  msg.value = '✅ 已加入购物车'
  setTimeout(() => msg.value = '', 1500)
}
function buyNow() {
  if (!p.value || p.value.stock <= 0) return
  addToCart(p.value.pid, p.value.name, p.value.price, p.value.category, p.value.image || '', qty.value)
  router.push('/cart')
}
</script>

<style scoped>
.detail { padding: 24px 32px 64px; }

.crumb {
  font-size: var(--fs-sm); color: var(--text-muted);
  padding: 12px 0 18px;
  display: flex; align-items: center; gap: 8px;
}
.crumb a { color: var(--text-secondary); cursor: pointer; transition: color 0.3s var(--ease-soft); }
.crumb a:hover { color: var(--primary); }
.crumb span { color: var(--text-muted); }
.crumb .ellipsis { max-width: 320px; }

.layout {
  display: grid;
  grid-template-columns: 460px 1fr;
  gap: 40px;
  background: var(--bg-card);
  padding: 36px;
  border-radius: var(--r-xl);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
}
@media (max-width: 900px) {
  .layout { grid-template-columns: 1fr; gap: 28px; }
  .gallery { position: static; }
  .gallery .main-img { max-width: 460px; }
}
@media (max-width: 480px) {
  .detail { padding: 12px 16px 48px; }
  .layout { padding: 20px; gap: 22px; border-radius: var(--r-lg); }
  .title { font-size: var(--fs-h2); }
  .big-price { font-size: 30px; }
  .btn-cart, .btn-buy { flex: 1 1 0; }
  .actions { gap: 12px; }
}

/* 左图 */
.gallery { position: sticky; top: 92px; }
.main-img { position: relative; width: 100%; border-radius: var(--r-lg); overflow: hidden; }
.main-img :deep(.pimg) { transition: transform 0.7s var(--ease-soft); }
.main-img:hover :deep(.pimg) { transform: scale(1.04); }
.ribbon {
  position: absolute; left: -1px; top: 14px;
  background: var(--primary); color: #fff;
  padding: 5px 14px; font-size: var(--fs-xs);
  border-radius: 0 var(--r-round) var(--r-round) 0;
  box-shadow: 0 4px 12px rgba(255,68,0,0.3);
  z-index: 2;
}
.thumbs {
  display: flex; gap: 12px; margin-top: 16px;
}
.thumb {
  width: 68px; height: 68px;
  border: 2px solid var(--border);
  border-radius: var(--r-sm);
  cursor: pointer; overflow: hidden;
  transition: all 0.4s var(--ease-soft);
}
.thumb:hover { transform: translateY(-2px); }
.thumb.on { border-color: var(--primary); box-shadow: 0 4px 12px rgba(255,68,0,0.2); }
.thumb.more {
  display: grid; place-items: center;
  font-size: var(--fs-xs); color: var(--text-muted);
  background: var(--bg-soft);
  text-align: center;
}
.share {
  display: flex; gap: 28px; margin-top: 24px;
  font-size: var(--fs-sm); color: var(--text-secondary);
}
.share span { cursor: pointer; transition: color 0.3s var(--ease-soft); }
.share span:hover { color: var(--primary); }

/* 右信息 */
.info { display: flex; flex-direction: column; }
.title { font-size: var(--fs-h1); font-weight: 700; color: var(--text-primary); line-height: 1.35; margin-bottom: 14px; letter-spacing: -0.02em; }
.desc { font-size: var(--fs-sm); color: var(--text-secondary); line-height: 1.7; margin-bottom: 28px; }

.price-box {
  background: linear-gradient(180deg, var(--primary-bg), var(--bg-card) 70%);
  padding: 24px 28px;
  border-radius: var(--r-lg);
  border: 1px solid var(--primary-light);
  margin-bottom: 28px;
}
.price-row { display: flex; align-items: baseline; gap: 14px; flex-wrap: wrap; }
.lbl { color: var(--text-muted); font-size: var(--fs-sm); }
.big-price {
  font-size: 40px; font-weight: 800; color: var(--primary);
  line-height: 1; letter-spacing: -0.02em;
}
.big-price::before { content: '¥'; font-size: 20px; margin-right: 2px; font-weight: 600; }
.market { font-size: var(--fs-body); color: var(--text-muted); text-decoration: line-through; }
.discount-tag {
  background: var(--primary); color: #fff;
  font-size: var(--fs-xs); padding: 3px 10px; border-radius: var(--r-round); font-weight: 600;
}
.price-meta {
  display: flex; gap: 28px;
  margin-top: 14px;
  font-size: var(--fs-sm); color: var(--text-secondary);
}
.price-meta .rating { color: var(--primary); font-weight: 500; }

/* 参数 */
.props {
  display: grid; grid-template-columns: 1fr 1fr;
  gap: 14px 28px;
  padding: 22px 0;
  border-top: 1px dashed var(--border);
  border-bottom: 1px dashed var(--border);
  margin-bottom: 28px;
}
@media (max-width: 480px) { .props { grid-template-columns: 1fr; gap: 12px 0; } }
.prop { display: flex; gap: 12px; font-size: var(--fs-sm); }
.prop .k { color: var(--text-muted); width: 52px; }
.prop .v { color: var(--text-primary); }
.prop .v.out { color: var(--text-muted); }

/* 数量 */
.buy-row {
  display: flex; align-items: center; gap: 18px;
  margin-bottom: 28px;
}
.buy-row .lbl { color: var(--text-muted); font-size: var(--fs-sm); }
.qty { display: flex; border: 1px solid var(--border-strong); border-radius: var(--r-sm); overflow: hidden; }
.qty button {
  width: 36px; height: 38px;
  background: var(--bg-soft);
  font-size: 18px; color: var(--text-regular);
  transition: background 0.3s var(--ease-soft);
}
.qty button:hover { background: var(--border-light); color: var(--primary); }
.qty input {
  width: 56px; height: 38px;
  text-align: center;
  border: none; border-left: 1px solid var(--border-light);
  border-right: 1px solid var(--border-light);
  font-size: var(--fs-body);
}
.stock-tip { font-size: var(--fs-xs); color: var(--text-muted); }

/* 购买按钮 */
.actions { display: flex; gap: 16px; margin-bottom: 28px; }
.btn-cart, .btn-buy {
  flex: 0 0 180px;
  height: 52px;
  border-radius: var(--r-md);
  font-size: var(--fs-h3); font-weight: 700;
  border: 2px solid;
  transition: all 0.4s var(--ease-soft);
}
.btn-cart {
  background: var(--primary-bg);
  border-color: var(--primary);
  color: var(--primary);
}
.btn-cart:hover { background: var(--primary-light); transform: translateY(-2px); }
.btn-buy {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
  box-shadow: 0 6px 16px rgba(255,68,0,0.28);
}
.btn-buy:hover { background: var(--primary-hover); transform: translateY(-2px); box-shadow: var(--shadow-glow); }
.btn-cart:disabled, .btn-buy:disabled { opacity: 0.5; cursor: not-allowed; transform: none; box-shadow: none; }

.guarantee {
  display: flex; gap: 24px;
  font-size: var(--fs-xs); color: var(--text-muted);
  flex-wrap: wrap;
}
.guarantee span { display: inline-flex; align-items: center; }

/* 详情 tabs */
.detail-tabs {
  margin-top: 32px;
  background: var(--bg-card);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  overflow: hidden;
}
.tabs {
  display: flex;
  border-bottom: 1px solid var(--border);
  background: var(--bg-soft);
  padding: 0 12px;
}
.tabs span {
  padding: 18px 30px;
  font-size: var(--fs-body); color: var(--text-regular);
  cursor: pointer; position: relative;
  transition: color 0.3s var(--ease-soft);
}
.tabs span:hover { color: var(--primary); }
.tabs span.on { color: var(--primary); font-weight: 600; background: var(--bg-card); }
.tabs span.on::after {
  content: ''; position: absolute; left: 14px; right: 14px; bottom: -1px;
  height: 3px; background: var(--primary); border-radius: 3px 3px 0 0;
}
.tab-desc, .tab-review { padding: 32px 36px; }
.tab-desc h3 { font-size: var(--fs-h3); margin-bottom: 16px; }
.tab-desc p { font-size: var(--fs-body); color: var(--text-regular); line-height: 1.9; margin-bottom: 24px; }
.param-list {
  border: 1px solid var(--border);
  border-radius: var(--r-md);
  overflow: hidden;
}
.param-list li {
  display: grid; grid-template-columns: 160px 1fr;
  padding: 14px 22px;
  font-size: var(--fs-sm);
  border-bottom: 1px solid var(--border-light);
}
.param-list li:last-child { border-bottom: none; }
.param-list li span:first-child { color: var(--text-muted); }
.param-list li span:last-child { color: var(--text-primary); }

/* 评价 */
.review-summary {
  display: flex; gap: 48px;
  padding: 28px 32px;
  background: var(--bg-soft);
  border-radius: var(--r-md);
  margin-bottom: 28px;
  border: 1px solid var(--border-light);
}
.rs-score { text-align: center; }
.rs-score strong { display: block; font-size: 48px; color: var(--primary); font-weight: 800; letter-spacing: -0.03em; }
.rs-score span { font-size: var(--fs-sm); color: var(--text-muted); }
.rs-bars { flex: 1; display: flex; flex-direction: column; gap: 12px; justify-content: center; }
.rs-bars > div { display: flex; align-items: center; gap: 14px; font-size: var(--fs-sm); }
.rs-bars > div span { width: 40px; color: var(--text-secondary); }
.rs-bars .bar { flex: 1; height: 8px; background: var(--border-light); border-radius: var(--r-round); overflow: hidden; }
.rs-bars .bar i { display: block; height: 100%; background: linear-gradient(90deg, var(--primary), var(--yellow)); border-radius: var(--r-round); }
.rs-bars em { font-style: normal; color: var(--text-muted); width: 44px; }

.review-list { display: flex; flex-direction: column; gap: 22px; }
.review { padding-bottom: 22px; border-bottom: 1px dashed var(--border); }
.review:last-child { border-bottom: none; padding-bottom: 0; }
.r-head { display: flex; gap: 14px; align-items: center; margin-bottom: 10px; }
.r-user { font-size: var(--fs-sm); font-weight: 600; color: var(--text-primary); }
.r-stars { font-size: var(--fs-xs); }
.r-body { font-size: var(--fs-body); color: var(--text-regular); line-height: 1.8; }
.r-meta { font-size: var(--fs-xs); color: var(--text-muted); margin-top: 8px; }

/* toast */
.toast {
  position: fixed; top: 96px; left: 50%; transform: translateX(-50%);
  background: rgba(27,26,23,0.9); color: #fff;
  padding: 14px 32px; border-radius: var(--r-round);
  font-size: var(--fs-sm); z-index: 9999;
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(10px);
}
.toast-enter-active, .toast-leave-active { transition: all 0.35s var(--ease-soft); }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, -12px); }

.loading { padding: 96px 20px; text-align: center; color: var(--text-muted); }
.loading p { margin-top: 16px; font-size: var(--fs-sm); }
</style>