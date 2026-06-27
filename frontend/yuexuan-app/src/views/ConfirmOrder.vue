<template>
  <div class="container confirm-page">
    <header class="page-head">
      <h2>确认订单</h2>
      <router-link to="/cart" class="back">← 返回购物车</router-link>
    </header>

    <!-- 空 -->
    <div v-if="!items.length" class="empty">
      <div class="empty-ico">📦</div>
      <p>没有待结算的商品</p>
      <router-link to="/cart" class="cta">返回购物车</router-link>
    </div>

    <template v-else>
      <!-- 收货地址 -->
      <section class="card addr-card">
        <h3 class="card-title"><span class="bar"></span>📍 收货地址</h3>
        <div class="addr-form">
          <div class="form-line">
            <label>收货人</label>
            <input v-model.trim="addr.name" placeholder="请输入收货人姓名" />
          </div>
          <div class="form-line">
            <label>手机号</label>
            <input v-model.trim="addr.phone" placeholder="请输入手机号" maxlength="11" />
          </div>
          <div class="form-line">
            <label>详细地址</label>
            <input v-model.trim="addr.address" placeholder="省 / 市 / 区 + 详细地址（街道、门牌号）" />
          </div>
        </div>
        <transition name="shake">
          <p class="addr-err" v-if="formErr">{{ formErr }}</p>
        </transition>
      </section>

      <!-- 商品清单 -->
      <section class="card">
        <h3 class="card-title"><span class="bar"></span>📦 商品清单</h3>
        <div class="goods-head">
          <span class="g-info">商品</span>
          <span class="g-price">单价</span>
          <span class="g-qty">数量</span>
          <span class="g-sub">小计</span>
        </div>
        <div class="goods-row" v-for="item in items" :key="item.pid">
          <div class="g-info" @click="$router.push(`/product/${item.pid}`)">
            <div class="thumb">
              <ProductImage :image="''" :category="item.category" :name="item.name" radius="4px" />
            </div>
            <div>
              <h4 class="ellipsis-2">{{ item.name }}</h4>
              <span class="cat">{{ item.category }}</span>
            </div>
          </div>
          <div class="g-price">{{ item.price }}</div>
          <div class="g-qty">×{{ item.qty }}</div>
          <div class="g-sub">{{ (item.price * item.qty).toFixed(0) }}</div>
        </div>
      </section>

      <!-- 配送 + 备注 -->
      <section class="card">
        <h3 class="card-title"><span class="bar"></span>🚚 配送 & 备注</h3>
        <div class="dispatch">
          <div class="d-row"><span>配送方式</span><b>悦选速递 · 211 限时达</b></div>
          <div class="d-row"><span>运费</span><b class="free">免运费</b></div>
          <div class="d-row"><span>送达时间</span><b>预计 3 个工作日内送达</b></div>
        </div>
      </section>

      <!-- 汇总 -->
      <section class="card summary-card">
        <div class="sum-rows">
          <div><span>商品总额</span><b>¥{{ total.toFixed(2) }}</b></div>
          <div><span>运费</span><b class="free">¥0.00</b></div>
          <div><span>优惠</span><b>-¥0.00</b></div>
        </div>
        <div class="pay-row">
          <span>应付总额</span>
          <em>¥{{ total.toFixed(2) }}</em>
        </div>
      </section>

      <!-- 操作区 -->
      <div class="action-bar" v-if="step === 'confirm'">
        <button class="btn-submit" :disabled="submitting" @click="submit">
          <span v-if="!submitting">提交订单</span>
          <span v-else>提交中...</span>
        </button>
        <p class="tip" v-if="submitting">正在调用 order-service 微服务，请稍候…</p>
      </div>

      <!-- 支付阶段 -->
      <section class="card pay-card" v-if="step === 'pay'">
        <div class="pay-success">
          <div class="ps-ico">✅</div>
          <div>
            <h3>订单提交成功！</h3>
            <p>共 {{ orderIds.length }} 笔订单待支付</p>
          </div>
        </div>
        <div class="pay-list">
          <div class="pay-row-item" v-for="(id, idx) in orderIds" :key="id">
            <span>订单 <code>#{{ id }}</code></span>
            <button class="btn-pay" @click="payOrder(id)">
              💰 立即支付
            </button>
          </div>
        </div>
        <div class="pay-methods">
          <span class="pm-title">支付方式：</span>
          <span class="pm on">💸 余额支付</span>
          <span class="pm">🟢 微信支付</span>
          <span class="pm">🔵 支付宝</span>
        </div>
      </section>

      <p class="msg" v-if="errMsg">{{ errMsg }}</p>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { selectedItems, totalPrice, clearSelected, type CartItem } from '../store/cart'
import { confirmOrder, submitOrder, payByService } from '../api'
import ProductImage from '../components/ProductImage.vue'

const router = useRouter()
const items = ref<CartItem[]>(selectedItems())
const total = computed(() => totalPrice())
const submitting = ref(false)
const errMsg = ref('')
const formErr = ref('')
const step = ref<'confirm' | 'pay'>('confirm')
const orderIds = ref<number[]>([])

const addr = reactive({ name: '', phone: '', address: '' })

function valid(): boolean {
  formErr.value = ''
  if (!addr.name) { formErr.value = '请填写收货人'; return false }
  if (!/^1\d{10}$/.test(addr.phone)) { formErr.value = '请输入正确的 11 位手机号'; return false }
  if (addr.address.length < 5) { formErr.value = '请填写完整收货地址（至少 5 个字）'; return false }
  return true
}

async function submit() {
  if (submitting.value) return
  if (!valid()) { window.scrollTo({ top: 0, behavior: 'smooth' }); return }
  submitting.value = true; errMsg.value = ''

  try {
    const confirmVO: any = await confirmOrder({
      items: items.value.map(i => ({ pid: i.pid, qty: i.qty })),
      receiverName: addr.name,
      receiverPhone: addr.phone,
      address: addr.address,
    })

    const orders: any[] = await submitOrder({
      confirmToken: confirmVO.confirmToken,
      items: items.value.map(i => ({ pid: i.pid, qty: i.qty })),
      receiverName: addr.name,
      receiverPhone: addr.phone,
      address: addr.address,
    })

    clearSelected()
    orderIds.value = orders.map((o: any) => o.id)
    step.value = 'pay'
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (e: any) {
    errMsg.value = '❌ 提交失败：' + (e.response?.data?.message || e.message || '请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function payOrder(orderId: number) {
  try {
    await payByService(orderId)
    router.push({ path: '/result', query: { ok: '1' } })
  } catch (e: any) {
    errMsg.value = '❌ 支付失败：' + (e.response?.data?.message || e.message || '请稍后重试')
  }
}
</script>

<style scoped>
.confirm-page { padding: 16px 20px 40px; }

.page-head {
  display: flex; align-items: baseline; justify-content: space-between;
  margin: 8px 0 16px;
}
.page-head h2 { font-size: 22px; font-weight: 700; }
.back { font-size: 13px; color: var(--primary); }

.empty {
  background: #fff; border-radius: var(--r-md); box-shadow: var(--shadow-card);
  text-align: center; padding: 80px 20px;
}
.empty-ico { font-size: 64px; opacity: 0.5; margin-bottom: 16px; }
.empty p { font-size: 16px; color: var(--text-regular); margin-bottom: 20px; }
.empty .cta {
  display: inline-block; padding: 9px 28px;
  background: var(--primary); color: #fff;
  border-radius: var(--r-round); font-size: 14px;
}

.card {
  background: #fff;
  border-radius: var(--r-md);
  box-shadow: var(--shadow-card);
  padding: 20px;
  margin-bottom: 16px;
}
.card-title {
  display: flex; align-items: center; gap: 10px;
  font-size: 15px; font-weight: 600; margin-bottom: 16px;
}
.card-title .bar { width: 3px; height: 16px; background: var(--primary); border-radius: 2px; }

/* 地址 */
.addr-form { display: flex; flex-direction: column; gap: 12px; }
.form-line { display: flex; align-items: center; gap: 12px; }
.form-line label { width: 72px; font-size: 13px; color: var(--text-secondary); flex-shrink: 0; }
.form-line input {
  flex: 1; height: 40px; padding: 0 14px;
  border: 1px solid var(--border-strong);
  border-radius: var(--r-md);
  font-size: 14px; background: var(--bg-soft);
  transition: all 0.2s;
}
.form-line input:focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 3px var(--primary-light); }
.addr-err { color: #DC2626; font-size: 13px; margin-top: 10px; padding-left: 84px; }

/* 商品清单 */
.goods-head, .goods-row {
  display: grid;
  grid-template-columns: 1fr 100px 80px 120px;
  align-items: center;
}
.goods-head {
  padding: 10px 16px;
  background: var(--bg-soft);
  border-radius: var(--r-sm);
  font-size: 12px; color: var(--text-muted); margin-bottom: 8px;
}
.goods-head span { text-align: center; }
.goods-head .g-info { text-align: left; }
@media (max-width: 640px) {
  .goods-head { display: none; }
  .goods-row {
    display: flex; flex-wrap: wrap; align-items: center;
    gap: 8px 16px; padding: 14px 12px;
  }
  .g-info { flex: 1 1 100%; }
  .g-price, .g-qty, .g-sub { flex: 0 0 auto; text-align: left; }
  .g-sub { margin-left: auto; }
  .summary-card .sum-rows > div { gap: 12px; }
  .form-line label { width: 60px; }
  .addr-err { padding-left: 72px; }
}
.goods-row { padding: 14px 12px; border-bottom: 1px solid var(--border-light); }
.goods-row:last-child { border-bottom: none; }
.g-info { display: flex; gap: 12px; cursor: pointer; min-width: 0; }
.g-info .thumb { width: 64px; height: 64px; flex-shrink: 0; }
.g-info h4 { font-size: 14px; font-weight: 500; color: var(--text-primary); line-height: 1.4; }
.g-info:hover h4 { color: var(--primary); }
.g-info .cat { font-size: 12px; color: var(--text-muted); }
.g-price, .g-qty, .g-sub { text-align: center; }
.g-price { color: var(--text-primary); }
.g-price::before { content: '¥'; font-size: 12px; color: var(--text-secondary); }
.g-qty { color: var(--text-secondary); }
.g-sub { color: var(--primary); font-weight: 700; }
.g-sub::before { content: '¥'; font-size: 13px; }

/* 配送 */
.dispatch { display: flex; flex-direction: column; gap: 12px; }
.d-row { display: flex; align-items: center; gap: 16px; font-size: 14px; }
.d-row span { color: var(--text-secondary); width: 80px; }
.d-row b { color: var(--text-primary); font-weight: 500; }
.d-row b.free { color: #22c55e; }

/* 汇总 */
.summary-card .sum-rows { display: flex; flex-direction: column; gap: 10px; }
.summary-card .sum-rows > div {
  display: flex; justify-content: flex-end; gap: 24px;
  font-size: 13px; color: var(--text-secondary);
}
.summary-card .sum-rows > div b { color: var(--text-primary); font-size: 15px; min-width: 90px; text-align: right; }
.summary-card .sum-rows .free { color: #22c55e; }
.pay-row {
  display: flex; justify-content: flex-end; align-items: baseline; gap: 18px;
  margin-top: 16px; padding-top: 16px;
  border-top: 1px dashed var(--border);
}
.pay-row span { font-size: 14px; color: var(--text-regular); }
.pay-row em {
  font-style: normal;
  font-size: 28px; font-weight: 800; color: var(--primary);
}
.pay-row em::before { content: '¥'; font-size: 15px; margin-right: 1px; }

/* 提交按钮 */
.action-bar { text-align: center; margin-top: 8px; }
.btn-submit {
  width: 100%; max-width: 320px;
  height: 50px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  font-size: 18px; font-weight: 700; letter-spacing: 4px;
  border-radius: var(--r-round);
  box-shadow: 0 6px 16px rgba(255,68,0,0.3);
  transition: all 0.2s;
}
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 8px 20px rgba(255,68,0,0.4); }
.btn-submit:disabled { opacity: 0.6; cursor: wait; }
.tip { font-size: 12px; color: var(--text-muted); margin-top: 10px; }

/* 支付阶段 */
.pay-card { text-align: left; }
.pay-success { display: flex; align-items: center; gap: 16px; padding-bottom: 18px; border-bottom: 1px dashed var(--border); margin-bottom: 18px; }
.ps-ico { font-size: 44px; }
.pay-success h3 { font-size: 18px; color: #22c55e; margin-bottom: 4px; }
.pay-success p { font-size: 13px; color: var(--text-secondary); }
.pay-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 18px; }
.pay-row-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: var(--bg-soft); border-radius: var(--r-md);
}
.pay-row-item span { font-size: 14px; color: var(--text-regular); }
.pay-row-item code { background: #fff; padding: 2px 8px; border-radius: 3px; font-size: 13px; }
.btn-pay {
  padding: 8px 24px; background: #22c55e; color: #fff;
  font-size: 14px; font-weight: 600; border-radius: var(--r-md);
}
.btn-pay:hover { background: #16a34a; }
.pay-methods { display: flex; align-items: center; gap: 12px; padding-top: 16px; border-top: 1px dashed var(--border); }
.pm-title { font-size: 13px; color: var(--text-secondary); }
.pm {
  padding: 6px 16px; border: 1px solid var(--border); border-radius: var(--r-round);
  font-size: 13px; color: var(--text-regular); cursor: pointer;
}
.pm.on { border-color: var(--primary); color: var(--primary); background: var(--primary-bg); }

.msg {
  margin-top: 14px; padding: 12px 16px;
  background: #FEF2F2; color: #DC2626;
  border-radius: var(--r-md); border-left: 3px solid #DC2626;
  font-size: 14px; text-align: center;
}

.shake-enter-active { animation: shake 0.3s; }
@keyframes shake {
  0%,100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}
</style>