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
      <li class="order-card" v-for="o in filtered" :key="o.id">
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
.orders-page { padding: 16px 20px 40px; }

.page-head {
  display: flex; align-items: baseline; justify-content: space-between;
  margin: 8px 0 16px;
}
.page-head h2 { font-size: 22px; font-weight: 700; }
.page-head h2 small { font-size: 14px; color: var(--text-muted); font-weight: 400; }
.go-buy { color: var(--primary); font-size: 14px; }

/* tabs */
.tabs {
  display: flex; gap: 8px; flex-wrap: wrap;
  margin-bottom: 16px;
  background: #fff;
  padding: 6px; border-radius: var(--r-md); box-shadow: var(--shadow-sm);
}
.tabs span {
  position: relative;
  padding: 9px 20px; font-size: 14px;
  color: var(--text-regular); cursor: pointer;
  border-radius: var(--r-sm);
  transition: all 0.2s;
}
.tabs span:hover { background: var(--bg-gray); }
.tabs span.on { background: var(--primary); color: #fff; font-weight: 600; }
.tabs span em {
  font-style: normal;
  margin-left: 4px;
  background: rgba(255,255,255,0.25);
  padding: 0 6px; border-radius: 999px; font-size: 11px;
}
.tabs span:not(.on) em { background: var(--primary-bg); color: var(--primary); }

/* 空 */
.empty {
  background: #fff; border-radius: var(--r-md); box-shadow: var(--shadow-card);
  text-align: center; padding: 80px 20px;
}
.empty-ico { font-size: 70px; opacity: 0.5; margin-bottom: 18px; }
.empty p { font-size: 17px; color: var(--text-primary); margin-bottom: 6px; }
.empty span { font-size: 13px; color: var(--text-muted); display: block; margin-bottom: 24px; }
.empty .cta {
  display: inline-block; padding: 10px 36px;
  background: var(--primary); color: #fff;
  border-radius: var(--r-round); font-size: 15px; font-weight: 600;
}

/* 订单卡片 */
.order-list { display: flex; flex-direction: column; gap: 14px; }
.order-card {
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  border: 1px solid var(--border-light);
  transition: box-shadow 0.2s;
}
.order-card:hover { box-shadow: var(--shadow-md); }

.oc-head {
  display: flex; align-items: center; gap: 16px;
  padding: 12px 18px;
  background: var(--bg-soft);
  border-bottom: 1px solid var(--border-light);
  font-size: 13px; color: var(--text-secondary);
}
.oc-no code {
  background: #fff; padding: 2px 8px; border-radius: 3px;
  font-family: monospace; font-size: 12px; color: var(--text-regular);
}
.oc-time { color: var(--text-muted); }
.oc-port { color: var(--text-muted); margin-left: auto; font-size: 12px; }
.oc-port::before { content: '⚙ '; }
.oc-status {
  font-size: 12px; font-weight: 600;
  padding: 3px 10px; border-radius: var(--r-round);
  margin-left: 12px;
}
.st-pending { color: #D97706; background: #FEF3C7; }
.st-paid    { color: #2563EB; background: #DBEAFE; }
.st-shipping{ color: #7C3AED; background: #EDE9FE; }
.st-done    { color: #16A34A; background: #DCFCE7; }
.st-cancel  { color: var(--text-muted); background: var(--bg-gray); }

.oc-body {
  display: flex; align-items: center; gap: 16px;
  padding: 16px 18px;
}
.oc-thumb { width: 80px; height: 80px; flex-shrink: 0; cursor: pointer; }
.oc-info { flex: 1; min-width: 0; }
.oc-info h4 {
  font-size: 15px; font-weight: 500; color: var(--text-primary);
  line-height: 1.4; cursor: pointer;
}
.oc-info h4:hover { color: var(--primary); }
.oc-meta {
  display: flex; gap: 8px; align-items: center;
  font-size: 12px; color: var(--text-muted); margin-top: 6px;
}
.oc-amount { text-align: right; min-width: 100px; }
.oc-amount .amt { font-size: 20px; font-weight: 700; color: var(--primary); }
.oc-amount .amt::before { content: '¥'; font-size: 13px; }
.oc-amount .amt-sub { font-size: 12px; color: var(--text-muted); margin-top: 4px; }

.oc-foot {
  display: flex; align-items: center; justify-content: flex-end; gap: 10px;
  padding: 12px 18px;
  border-top: 1px dashed var(--border-light);
}
.oc-foot .wait, .oc-foot .done, .oc-foot .cancelled { font-size: 13px; color: var(--text-secondary); margin-right: auto; }
.oc-foot .done { color: #16A34A; }
.oc-foot .cancelled { color: var(--text-muted); }
.btn-line {
  padding: 7px 18px; border: 1px solid var(--border-strong); background: #fff;
  border-radius: var(--r-round); font-size: 13px; color: var(--text-regular);
}
.btn-line:hover { border-color: var(--primary); color: var(--primary); }
.btn-pay-mini {
  padding: 7px 22px; background: var(--primary); color: #fff;
  border-radius: var(--r-round); font-size: 13px; font-weight: 600;
}
.btn-pay-mini:hover { background: var(--primary-hover); }

.tip-foot {
  text-align: center; font-size: 12px; color: var(--text-muted);
  margin-top: 24px;
}
.tip-foot a { color: var(--primary); }
</style>