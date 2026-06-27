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
.confirm-page { padding: 24px 32px 64px; }

.page-head {
  display: flex; align-items: baseline; justify-content: space-between;
  margin: 12px 0 28px;
}
.page-head h2 { font-size: var(--fs-h1); font-weight: 700; letter-spacing: -0.02em; }
.back { font-size: var(--fs-sm); color: var(--primary); font-weight: 500; }

.empty {
  background: var(--bg-card); border-radius: var(--r-xl); box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  text-align: center; padding: 96px 20px;
}
.empty-ico { font-size: 72px; opacity: 0.5; margin-bottom: 20px; }
.empty p { font-size: var(--fs-h3); color: var(--text-regular); margin-bottom: 24px; }
.empty .cta {
  display: inline-block; padding: 11px 32px;
  background: var(--primary); color: #fff;
  border-radius: var(--r-round); font-size: var(--fs-body); font-weight: 600;
  box-shadow: 0 6px 16px rgba(255,68,0,0.28);
  transition: all 0.4s var(--ease-soft);
}
.empty .cta:hover { transform: translateY(-2px); box-shadow: var(--shadow-glow); }

.card {
  background: var(--bg-card);
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  padding: 28px 30px;
  margin-bottom: 20px;
}
.card-title {
  display: flex; align-items: center; gap: 10px;
  font-size: var(--fs-h3); font-weight: 700; margin-bottom: 22px; letter-spacing: -0.01em;
}
.card-title .bar { width: 5px; height: 18px; border-radius: var(--r-round); background: linear-gradient(180deg, var(--primary), var(--yellow)); }

/* 地址 */
.addr-form { display: flex; flex-direction: column; gap: 14px; }
.form-line { display: flex; align-items: center; gap: 14px; }
.form-line label { width: 72px; font-size: var(--fs-sm); color: var(--text-secondary); flex-shrink: 0; }
.form-line input {
  flex: 1; height: 46px; padding: 0 16px;
  border: 1px solid var(--border-strong);
  border-radius: var(--r-sm);
  font-size: var(--fs-body); background: var(--bg-soft);
  transition: all 0.35s var(--ease-soft);
}
.form-line input:focus { border-color: var(--primary); background: #fff; box-shadow: 0 0 0 3px var(--primary-light); }
.addr-err { color: #DC2626; font-size: var(--fs-sm); margin-top: 10px; padding-left: 86px; }

/* 商品清单 */
.goods-head, .goods-row {
  display: grid;
  grid-template-columns: 1fr 100px 80px 120px;
  align-items: center;
}
.goods-head {
  padding: 12px 18px;
  background: var(--bg-soft);
  border-radius: var(--r-sm);
  font-size: var(--fs-xs); color: var(--text-muted); margin-bottom: 8px;
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
  .addr-err { padding-left: 74px; }
}
.goods-row { padding: 16px 18px; border-bottom: 1px solid var(--border-light); transition: background 0.4s var(--ease-soft); }
.goods-row:hover { background: var(--bg-tint); }
.goods-row:last-child { border-bottom: none; }
.g-info { display: flex; gap: 14px; cursor: pointer; min-width: 0; }
.g-info .thumb { width: 68px; height: 68px; flex-shrink: 0; border-radius: var(--r-sm); overflow: hidden; box-shadow: var(--shadow-xs); }
.g-info h4 { font-size: var(--fs-body); font-weight: 500; color: var(--text-primary); line-height: 1.45; }
.g-info:hover h4 { color: var(--primary); }
.g-info .cat { font-size: var(--fs-xs); color: var(--text-muted); }
.g-price, .g-qty, .g-sub { text-align: center; }
.g-price { color: var(--text-primary); }
.g-price::before { content: '¥'; font-size: var(--fs-xs); color: var(--text-secondary); }
.g-qty { color: var(--text-secondary); }
.g-sub { color: var(--primary); font-weight: 700; }
.g-sub::before { content: '¥'; font-size: var(--fs-sm); }

/* 配送 */
.dispatch { display: flex; flex-direction: column; gap: 14px; }
.d-row { display: flex; align-items: center; gap: 16px; font-size: var(--fs-body); }
.d-row span { color: var(--text-secondary); width: 84px; }
.d-row b { color: var(--text-primary); font-weight: 500; }
.d-row b.free { color: #22c55e; }

/* 汇总 */
.summary-card .sum-rows { display: flex; flex-direction: column; gap: 12px; }
.summary-card .sum-rows > div {
  display: flex; justify-content: flex-end; gap: 24px;
  font-size: var(--fs-sm); color: var(--text-secondary);
}
.summary-card .sum-rows > div b { color: var(--text-primary); font-size: var(--fs-body); min-width: 90px; text-align: right; }
.summary-card .sum-rows .free { color: #22c55e; }
.pay-row {
  display: flex; justify-content: flex-end; align-items: baseline; gap: 18px;
  margin-top: 18px; padding-top: 18px;
  border-top: 1px dashed var(--border);
}
.pay-row span { font-size: var(--fs-body); color: var(--text-regular); }
.pay-row em {
  font-style: normal;
  font-size: 32px; font-weight: 800; color: var(--primary); letter-spacing: -0.02em;
}
.pay-row em::before { content: '¥'; font-size: var(--fs-body); margin-right: 1px; font-weight: 600; }

/* 提交按钮 */
.action-bar { text-align: center; margin-top: 12px; }
.btn-submit {
  width: 100%; max-width: 360px;
  height: 54px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  font-size: var(--fs-h3); font-weight: 700; letter-spacing: 4px;
  border-radius: var(--r-round);
  box-shadow: 0 6px 16px rgba(255,68,0,0.3);
  transition: all 0.4s var(--ease-soft);
}
.btn-submit:hover:not(:disabled) { transform: translateY(-2px); box-shadow: var(--shadow-glow); }
.btn-submit:disabled { opacity: 0.6; cursor: wait; }
.tip { font-size: var(--fs-xs); color: var(--text-muted); margin-top: 12px; }

/* 支付阶段 */
.pay-card { text-align: left; }
.pay-success { display: flex; align-items: center; gap: 18px; padding-bottom: 20px; border-bottom: 1px dashed var(--border); margin-bottom: 20px; }
.ps-ico { font-size: 48px; }
.pay-success h3 { font-size: var(--fs-h3); color: #22c55e; margin-bottom: 4px; }
.pay-success p { font-size: var(--fs-sm); color: var(--text-secondary); }
.pay-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 20px; }
.pay-row-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 18px; background: var(--bg-soft); border-radius: var(--r-md); border: 1px solid var(--border-light);
}
.pay-row-item span { font-size: var(--fs-body); color: var(--text-regular); }
.pay-row-item code { background: var(--bg-card); padding: 3px 10px; border-radius: var(--r-xs); font-size: var(--fs-sm); font-family: monospace; }
.btn-pay {
  padding: 9px 26px; background: #22c55e; color: #fff;
  font-size: var(--fs-body); font-weight: 600; border-radius: var(--r-md);
  box-shadow: 0 4px 12px rgba(34,197,94,0.25);
  transition: all 0.4s var(--ease-soft);
}
.btn-pay:hover { background: #16a34a; transform: translateY(-1px); }
.pay-methods { display: flex; align-items: center; gap: 12px; padding-top: 18px; border-top: 1px dashed var(--border); }
.pm-title { font-size: var(--fs-sm); color: var(--text-secondary); }
.pm {
  padding: 7px 18px; border: 1px solid var(--border); border-radius: var(--r-round);
  font-size: var(--fs-sm); color: var(--text-regular); cursor: pointer;
  transition: all 0.35s var(--ease-soft);
}
.pm:hover { border-color: var(--primary); color: var(--primary); }
.pm.on { border-color: var(--primary); color: var(--primary); background: var(--primary-bg); }

.msg {
  margin-top: 16px; padding: 14px 18px;
  background: #FEF2F2; color: #DC2626;
  border-radius: var(--r-md); border-left: 3px solid #DC2626;
  font-size: var(--fs-body); text-align: center;
}

.shake-enter-active { animation: shake 0.3s; }
@keyframes shake {
  0%,100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

@media (max-width: 600px) {
  .confirm-page { padding: 16px 16px 48px; }
  .page-head h2 { font-size: var(--fs-h2); }
  .card { padding: 20px 18px; }
  .pay-row em { font-size: 26px; }
  .btn-submit { letter-spacing: 2px; max-width: 100%; }
}
</style>