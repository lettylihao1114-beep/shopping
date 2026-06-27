<template>
  <div class="container orders-page">
    <header class="page-head">
      <h2>我的订单 <small v-if="orders.length">（共 {{ orders.length }} 笔）</small></h2>
      <router-link to="/" class="go-buy">继续购物 ›</router-link>
    </header>

    <!-- 状态 tabs -->
    <nav class="tabs" v-if="orders.length">
      <span
        v-for="t in tabs" :key="t.value"
        :class="{ on: activeStatus === t.value }"
        @click="filterStatus(t.value)"
      >
        {{ t.label }}
        <em v-if="countByStatus(t.value) > 0">{{ countByStatus(t.value) }}</em>
      </span>
    </nav>

    <!-- 加载/空 -->
    <div v-if="loading" class="loading-box">
      <div class="spinner"></div>
      <p>订单加载中…</p>
    </div>
    <div v-else-if="!filtered.length" class="empty">
      <div class="empty-ico">{{ activeStatus ? '📭' : '🛒' }}</div>
      <p>{{ activeStatus ? '该状态下暂无订单' : '你还没有任何订单' }}</p>
      <span v-if="!activeStatus">下单后可在这里查看订单状态</span>
      <router-link to="/" class="cta">去逛逛</router-link>
    </div>

    <!-- 订单卡片 -->
    <ul class="order-list" v-else>
      <li class="order-card" v-for="(o, i) in filtered" :key="o.id" v-reveal="(i % 3) + 1">
        <!-- 头部 -->
        <div class="oc-head">
          <span class="oc-no">订单号 <code>{{ o.orderNo || o.id }}</code></span>
          <span class="oc-time">{{ fmtTime(o.createTime) }}</span>
          <span class="oc-port" title="由哪个商品服务实例处理">服务实例 :{{ o.handledByPort || '-' }}</span>
          <span class="oc-status" :class="statusClass(o.status)">{{ statusText(o.status) }}</span>
        </div>

        <!-- 商品区 -->
        <div class="oc-body">
          <div class="oc-thumb">
            <ProductImage :image="''" :category="guessCategory(o.productName)" :name="o.productName" radius="4px" />
          </div>
          <div class="oc-info">
            <h4 class="ellipsis-2" @click="$router.push('/product/' + o.pid)">{{ baseName(o.productName) }}</h4>
            <div class="oc-meta">
              <span>×{{ o.number }}件</span>
              <span>·</span>
              <span>收货人 {{ o.receiverName || '-' }}</span>
            </div>
          </div>
          <div class="oc-amount">
            <div class="amt">¥{{ o.totalAmount }}</div>
            <div class="amt-sub">{{ statusText(o.status) === '待付款' ? '待付款' : '实付款' }}</div>
          </div>
        </div>

        <!-- 底部操作 -->
        <div class="oc-foot">
          <template v-if="o.status === 'PENDING'">
            <button class="btn-line" @click="onCancel(o)">取消订单</button>
            <button class="btn-pay-mini" @click="onPay(o)">立即支付</button>
          </template>
          <template v-else-if="o.status === 'PAID'">
            <span class="wait">⏳ 等待商家发货</span>
          </template>
          <template v-else-if="o.status === 'SHIPPING'">
            <span class="wait">🚚 商品配送中</span>
            <button class="btn-line" @click="onReceive(o)">确认收货</button>
          </template>
          <template v-else-if="o.status === 'COMPLETED'">
            <span class="done">✅ 已完成</span>
            <button class="btn-line">再次购买</button>
          </template>
          <template v-else-if="o.status === 'CANCELLED'">
            <span class="cancelled">已取消</span>
          </template>
        </div>
      </li>
    </ul>

    <p class="tip-foot" v-if="filtered.length">🔍 前往 <a href="http://localhost:9411/zipkin/" target="_blank">Zipkin</a> 查看微服务调用链路</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getOrders, cancelOrder, payByService, receiveOrder, type Order } from '../api'
import ProductImage from '../components/ProductImage.vue'

const orders = ref<Order[]>([])
const activeStatus = ref('')
const loading = ref(false)

const tabs = [
  { label: '全部',   value: '' },
  { label: '待付款', value: 'PENDING' },
  { label: '待发货', value: 'PAID' },
  { label: '待收货', value: 'SHIPPING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
]

const filtered = computed(() =>
  activeStatus.value ? orders.value.filter(o => o.status === activeStatus.value) : orders.value
)

function countByStatus(s: string) {
  return s ? orders.value.filter(o => o.status === s).length : orders.value.length
}

function filterStatus(v: string) {
  activeStatus.value = activeStatus.value === v ? '' : v
}

function statusText(s: string) {
  return ({
    PENDING: '待付款', PAID: '待发货', SHIPPING: '待收货',
    COMPLETED: '已完成', CANCELLED: '已取消',
  } as Record<string,string>)[s] || s
}
function statusClass(s: string) {
  return ({
    PENDING: 'st-pending', PAID: 'st-paid', SHIPPING: 'st-shipping',
    COMPLETED: 'st-done', CANCELLED: 'st-cancel',
  } as Record<string,string>)[s] || ''
}

function baseName(n: string) { return (n || '').split('—')[0].trim() }
function guessCategory(n: string) {
  const s = n || ''
  if (/鞋|跑鞋|板鞋|篮球鞋/.test(s)) return '运动鞋'
  if (/T恤|速干|卫衣/.test(s)) return 'T恤'
  if (/耳机|手表|音箱|数码|蓝牙/.test(s)) return '数码'
  if (/坚果|绿茶|零食|食品/.test(s)) return '食品'
  if (/台灯|坐垫|收纳|家居/.test(s)) return '家居'
  return ''
}
function fmtTime(t?: string) { return t ? t.substring(0, 19).replace('T', ' ') : '-' }

async function load() {
  loading.value = true
  try { orders.value = (await getOrders()) || [] } catch (e) { orders.value = [] }
  finally { loading.value = false }
}
onMounted(load)

async function onCancel(o: Order) {
  if (!confirm(`确认取消订单 #${o.id} 吗？库存将回滚。`)) return
  try { await cancelOrder(o.id); await load() } catch (e: any) { alert('取消失败：' + (e.message || '')) }
}
async function onPay(o: Order) {
  try { await payByService(o.id); await load() } catch (e: any) { alert('支付失败：' + (e.message || '')) }
}
async function onReceive(o: Order) {
  try { await receiveOrder(o.id); await load() } catch (e: any) { alert('操作失败：' + (e.message || '')) }
}
</script>

<style scoped>
.orders-page { padding: 24px 32px 64px; }

.page-head {
  display: flex; align-items: baseline; justify-content: space-between;
  margin: 12px 0 28px;
}
.page-head h2 { font-size: var(--fs-h1); font-weight: 700; letter-spacing: -0.02em; }
.page-head h2 small { font-size: var(--fs-body); color: var(--text-muted); font-weight: 400; }
.go-buy { color: var(--primary); font-size: var(--fs-sm); font-weight: 500; }

/* tabs */
.tabs {
  display: flex; gap: 6px; flex-wrap: wrap;
  margin-bottom: 24px;
  background: var(--bg-card);
  padding: 6px;
  border-radius: var(--r-round);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}
.tabs span {
  position: relative;
  padding: 9px 22px; font-size: var(--fs-body);
  color: var(--text-regular); cursor: pointer;
  border-radius: var(--r-round);
  transition: all 0.35s var(--ease-soft);
}
.tabs span:hover { background: var(--bg-soft); color: var(--primary); }
.tabs span.on { background: var(--primary); color: #fff; font-weight: 600; box-shadow: 0 4px 10px rgba(255,68,0,0.25); }
.tabs span em {
  font-style: normal;
  margin-left: 5px;
  background: rgba(255,255,255,0.22);
  padding: 1px 7px; border-radius: var(--r-round); font-size: var(--fs-xs);
}
.tabs span:not(.on) em { background: var(--primary-bg); color: var(--primary); }

/* 空 */
.empty {
  background: var(--bg-card); border-radius: var(--r-xl); box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  text-align: center; padding: 112px 20px;
}
.empty-ico { font-size: 80px; opacity: 0.5; margin-bottom: 22px; }
.empty p { font-size: var(--fs-h3); color: var(--text-primary); margin-bottom: 8px; }
.empty span { font-size: var(--fs-sm); color: var(--text-muted); display: block; margin-bottom: 32px; }
.empty .cta {
  display: inline-block; padding: 12px 40px;
  background: var(--primary); color: #fff;
  border-radius: var(--r-round); font-size: var(--fs-body); font-weight: 600;
  box-shadow: 0 6px 16px rgba(255,68,0,0.28);
  transition: all 0.4s var(--ease-soft);
}
.empty .cta:hover { background: var(--primary-hover); transform: translateY(-2px); box-shadow: var(--shadow-glow); }

/* 订单卡片 */
.order-list { display: flex; flex-direction: column; gap: 18px; }
.order-card {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  border: 1px solid var(--border-light);
  transition: box-shadow 0.5s var(--ease-soft), transform 0.5s var(--ease-soft);
}
.order-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }

.oc-head {
  display: flex; align-items: center; gap: 18px;
  padding: 14px 22px;
  background: var(--bg-soft);
  border-bottom: 1px solid var(--border-light);
  font-size: var(--fs-sm); color: var(--text-secondary);
}
.oc-no code {
  background: var(--bg-card); padding: 3px 10px; border-radius: var(--r-xs);
  font-family: monospace; font-size: var(--fs-xs); color: var(--text-regular);
}
.oc-time { color: var(--text-muted); }
.oc-port { color: var(--text-muted); margin-left: auto; font-size: var(--fs-xs); }
.oc-port::before { content: '⚙ '; }
.oc-status {
  font-size: var(--fs-xs); font-weight: 600;
  padding: 4px 12px; border-radius: var(--r-round);
  margin-left: 12px;
}
.st-pending { color: #D97706; background: #FEF3C7; }
.st-paid    { color: #2563EB; background: #DBEAFE; }
.st-shipping{ color: #7C3AED; background: #EDE9FE; }
.st-done    { color: #16A34A; background: #DCFCE7; }
.st-cancel  { color: var(--text-muted); background: var(--bg-soft); }

.oc-body {
  display: flex; align-items: center; gap: 18px;
  padding: 20px 22px;
}
.oc-thumb { width: 84px; height: 84px; flex-shrink: 0; cursor: pointer; border-radius: var(--r-sm); overflow: hidden; box-shadow: var(--shadow-xs); }
.oc-info { flex: 1; min-width: 0; }
.oc-info h4 {
  font-size: var(--fs-h3); font-weight: 500; color: var(--text-primary);
  line-height: 1.45; cursor: pointer;
  transition: color 0.3s var(--ease-soft);
}
.oc-info h4:hover { color: var(--primary); }
.oc-meta {
  display: flex; gap: 10px; align-items: center;
  font-size: var(--fs-xs); color: var(--text-muted); margin-top: 8px;
}
.oc-amount { text-align: right; min-width: 110px; }
.oc-amount .amt { font-size: 22px; font-weight: 800; color: var(--primary); letter-spacing: -0.02em; }
.oc-amount .amt::before { content: '¥'; font-size: var(--fs-sm); font-weight: 600; }
.oc-amount .amt-sub { font-size: var(--fs-xs); color: var(--text-muted); margin-top: 4px; }

.oc-foot {
  display: flex; align-items: center; justify-content: flex-end; gap: 12px;
  padding: 14px 22px;
  border-top: 1px dashed var(--border-light);
}
.oc-foot .wait, .oc-foot .done, .oc-foot .cancelled { font-size: var(--fs-sm); color: var(--text-secondary); margin-right: auto; }
.oc-foot .done { color: #16A34A; }
.oc-foot .cancelled { color: var(--text-muted); }
.btn-line {
  padding: 8px 20px; border: 1px solid var(--border-strong); background: var(--bg-card);
  border-radius: var(--r-round); font-size: var(--fs-sm); color: var(--text-regular);
  transition: all 0.35s var(--ease-soft);
}
.btn-line:hover { border-color: var(--primary); color: var(--primary); transform: translateY(-1px); }
.btn-pay-mini {
  padding: 8px 24px; background: var(--primary); color: #fff;
  border-radius: var(--r-round); font-size: var(--fs-sm); font-weight: 600;
  box-shadow: 0 4px 10px rgba(255,68,0,0.22);
  transition: all 0.4s var(--ease-soft);
}
.btn-pay-mini:hover { background: var(--primary-hover); transform: translateY(-1px); box-shadow: var(--shadow-glow); }

.tip-foot {
  text-align: center; font-size: var(--fs-xs); color: var(--text-muted);
  margin-top: 32px;
}
.tip-foot a { color: var(--primary); }

@media (max-width: 640px) {
  .orders-page { padding: 16px 16px 48px; }
  .page-head h2 { font-size: var(--fs-h2); }
  .oc-head { gap: 10px; padding: 12px 14px; flex-wrap: wrap; }
  .oc-port { width: 100%; margin-left: 0; }
  .oc-body { flex-wrap: wrap; gap: 14px; padding: 16px 14px; }
  .oc-amount { width: 100%; text-align: left; }
  .oc-amount .amt { font-size: var(--fs-h3); }
}
</style>