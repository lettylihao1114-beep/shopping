<template>
  <div class="container cart-page">
    <header class="page-head">
      <h2>购物车 <small v-if="cart.items.length">（{{ totalCount }} 件商品）</small></h2>
      <router-link to="/" class="go-buy">继续购物 ›</router-link>
    </header>

    <!-- 空购物车 -->
    <div v-if="!cart.items.length" class="empty">
      <div class="empty-ico">🛒</div>
      <p>购物车还是空的</p>
      <span>快去挑几件心仪的好物吧</span>
      <router-link to="/" class="cta">去逛逛</router-link>
    </div>

    <template v-else>
      <!-- 列表头 -->
      <div class="cart-head">
        <label class="col-check">
          <input type="checkbox" :checked="allSelected" @change="toggleAll()" /> 全选
        </label>
        <span class="col-info">商品信息</span>
        <span class="col-price">单价</span>
        <span class="col-qty">数量</span>
        <span class="col-sub">小计</span>
        <span class="col-op">操作</span>
      </div>

      <!-- 列表 -->
      <div class="cart-body">
        <div class="cart-row" v-for="item in cart.items" :key="item.pid">
          <label class="col-check">
            <input type="checkbox" :checked="item.selected" @change="toggleSelect(item.pid)" />
          </label>
          <div class="col-info" @click="$router.push(`/product/${item.pid}`)">
            <div class="thumb">
              <ProductImage :image="item.image" :category="item.category" :name="item.name" radius="4px" />
            </div>
            <div class="meta">
              <h4 class="ellipsis-2">{{ item.name }}</h4>
              <span class="cat">{{ item.category }}</span>
            </div>
          </div>
          <div class="col-price">{{ item.price }}</div>
          <div class="col-qty">
            <div class="qty">
              <button @click="updateQty(item.pid, item.qty - 1)" :disabled="item.qty <= 1">-</button>
              <input :value="item.qty" @change="onQtyChange(item.pid, $event)" />
              <button @click="updateQty(item.pid, item.qty + 1)">+</button>
            </div>
          </div>
          <div class="col-sub">{{ (item.price * item.qty).toFixed(0) }}</div>
          <div class="col-op">
            <span @click="removeFromCart(item.pid)">删除</span>
            <span>移入收藏</span>
          </div>
        </div>
      </div>

      <!-- 结算条 -->
      <div class="settle-bar">
        <label class="col-check">
          <input type="checkbox" :checked="allSelected" @change="toggleAll()" /> 全选
        </label>
        <span class="del-all" @click="clearAll">清空购物车</span>
        <div class="settle-right">
          <span class="sel-info">已选 <b>{{ selCount }}</b> 件</span>
          <span class="total">合计 <em>{{ total.toFixed(0) }}</em></span>
          <button class="btn-checkout" :disabled="selCount === 0" @click="goConfirm">
            结 算
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  cart, selectedItems, totalPrice, toggleAll, toggleSelect,
  updateQty, removeFromCart,
} from '../store/cart'
import ProductImage from '../components/ProductImage.vue'

const router = useRouter()

const selCount = computed(() => selectedItems().reduce((s, i) => s + i.qty, 0))
const total = computed(() => totalPrice())
const totalCount = computed(() => cart.items.reduce((s, i) => s + i.qty, 0))
const allSelected = computed(() => cart.items.length > 0 && cart.items.every(i => i.selected))

function onQtyChange(pid: number, e: Event) {
  const v = Number((e.target as HTMLInputElement).value)
  if (v > 0) updateQty(pid, v)
}

function clearAll() {
  if (confirm('确认清空购物车吗？')) {
    cart.items = []; localStorage.setItem('yuexuan_cart', '[]')
  }
}

function goConfirm() {
  if (selCount.value === 0) return
  router.push('/confirm')
}
</script>

<style scoped>
.cart-page { padding: 24px 32px 64px; }

.page-head {
  display: flex; align-items: baseline; justify-content: space-between;
  margin: 12px 0 28px;
}
.page-head h2 { font-size: var(--fs-h1); font-weight: 700; letter-spacing: -0.02em; }
.page-head h2 small { font-size: var(--fs-body); color: var(--text-muted); font-weight: 400; margin-left: 8px; }
.go-buy { color: var(--primary); font-size: var(--fs-sm); font-weight: 500; }

/* 空 */
.empty {
  background: var(--bg-card); border-radius: var(--r-xl); box-shadow: var(--shadow-card);
  border: 1px solid var(--border-light);
  text-align: center; padding: 112px 20px;
}
.empty-ico { font-size: 80px; margin-bottom: 24px; opacity: 0.5; }
.empty p { font-size: var(--fs-h3); color: var(--text-primary); margin-bottom: 8px; }
.empty span { font-size: var(--fs-sm); color: var(--text-muted); display: block; margin-bottom: 32px; }
.empty .cta {
  display: inline-block;
  padding: 12px 40px;
  background: var(--primary); color: #fff;
  border-radius: var(--r-round); font-size: var(--fs-body); font-weight: 600;
  box-shadow: 0 6px 16px rgba(255,68,0,0.28);
  transition: all 0.4s var(--ease-soft);
}
.empty .cta:hover { background: var(--primary-hover); transform: translateY(-2px); box-shadow: var(--shadow-glow); }

/* 列表头 */
.cart-head, .cart-row {
  display: grid;
  grid-template-columns: 80px 1fr 120px 150px 120px 80px;
  align-items: center;
}
.cart-head {
  background: var(--bg-card);
  border-radius: var(--r-lg) var(--r-lg) 0 0;
  padding: 18px 24px;
  font-size: var(--fs-sm); color: var(--text-secondary);
  border: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border);
}
.col-check { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.col-price, .col-sub { text-align: center; }

/* 列表体 */
.cart-body { background: var(--bg-card); border-left: 1px solid var(--border-light); border-right: 1px solid var(--border-light); }
.cart-row {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
  transition: background 0.4s var(--ease-soft);
}
.cart-row:hover { background: var(--bg-tint); }
.cart-row:last-child { border-bottom: none; border-radius: 0 0 var(--r-lg) var(--r-lg); }

.col-info { display: flex; gap: 16px; cursor: pointer; min-width: 0; }
.col-info .thumb { width: 88px; height: 88px; flex-shrink: 0; border-radius: var(--r-sm); overflow: hidden; box-shadow: var(--shadow-xs); }
.col-info .meta { display: flex; flex-direction: column; justify-content: center; gap: 8px; min-width: 0; }
.col-info h4 { font-size: var(--fs-body); font-weight: 500; color: var(--text-primary); line-height: 1.45; }
.col-info:hover h4 { color: var(--primary); }
.col-info .cat { font-size: var(--fs-xs); color: var(--text-muted); }

.col-price {
  color: var(--text-primary); font-size: var(--fs-body); font-weight: 600; text-align: center;
}
.col-price::before { content: '¥'; font-size: var(--fs-xs); color: var(--text-secondary); margin-right: 2px; font-weight: 400; }
.col-sub { color: var(--primary); font-size: var(--fs-h3); font-weight: 700; text-align: center; }
.col-sub::before { content: '¥'; font-size: var(--fs-sm); margin-right: 1px; }

.qty {
  display: flex; justify-content: center;
  border: 1px solid var(--border-strong);
  border-radius: var(--r-sm);
  width: 116px; margin: 0 auto;
  overflow: hidden;
  background: var(--bg-soft);
}
.qty button {
  width: 32px; height: 36px;
  background: transparent;
  font-size: 16px; color: var(--text-regular);
  transition: background 0.3s var(--ease-soft);
}
.qty button:hover:not(:disabled) { background: var(--border-light); color: var(--primary); }
.qty button:disabled { color: var(--text-muted); cursor: not-allowed; }
.qty input {
  width: 50px; height: 36px;
  text-align: center; border: none;
  border-left: 1px solid var(--border-light);
  border-right: 1px solid var(--border-light);
  font-size: var(--fs-sm);
  background: var(--bg-card);
}
.qty input::-webkit-outer-spin-button,
.qty input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }

.col-op { display: flex; flex-direction: column; gap: 10px; align-items: center; font-size: var(--fs-xs); color: var(--text-muted); }
.col-op span { cursor: pointer; transition: color 0.3s var(--ease-soft); }
.col-op span:hover { color: var(--primary); }

input[type="checkbox"] {
  width: 16px; height: 16px;
  accent-color: var(--primary);
  cursor: pointer;
}

/* 结算条 */
.settle-bar {
  display: flex; align-items: center; gap: 20px;
  background: var(--bg-card);
  border-radius: var(--r-lg);
  border: 1px solid var(--border-light);
  padding: 16px 24px;
  margin-top: 20px;
  box-shadow: var(--shadow-md);
  position: sticky; bottom: 20px;
}
.del-all { color: var(--text-secondary); font-size: var(--fs-sm); cursor: pointer; transition: color 0.3s var(--ease-soft); }
.del-all:hover { color: var(--primary); }
.settle-right {
  margin-left: auto;
  display: flex; align-items: center; gap: 28px;
}
.sel-info { font-size: var(--fs-body); color: var(--text-regular); }
.sel-info b { color: var(--primary); margin: 0 2px; }
.total { font-size: var(--fs-body); color: var(--text-regular); }
.total em {
  font-style: normal;
  font-size: 28px; font-weight: 800; color: var(--primary);
  margin-left: 4px; letter-spacing: -0.02em;
}
.total em::before { content: '¥'; font-size: var(--fs-body); font-weight: 600; }
.btn-checkout {
  height: 52px; padding: 0 52px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  font-size: var(--fs-h3); font-weight: 700; letter-spacing: 4px;
  border-radius: var(--r-round);
  box-shadow: 0 6px 16px rgba(255,68,0,0.3);
  transition: all 0.4s var(--ease-soft);
}
.btn-checkout:hover:not(:disabled) { transform: translateY(-2px); box-shadow: var(--shadow-glow); }
.btn-checkout:disabled { opacity: 0.4; cursor: not-allowed; background: var(--text-muted); box-shadow: none; }

@media (max-width: 768px) {
  .cart-head { display: none; }
  .cart-row {
    grid-template-columns: 28px 1fr auto;
    grid-template-areas:
      "check info sub"
      ".    qty  op";
    gap: 10px 14px;
    padding: 16px 14px;
  }
  .col-check { grid-area: check; align-self: start; }
  .col-info { grid-area: info; }
  .col-sub { grid-area: sub; text-align: right; }
  .col-qty { grid-area: qty; justify-self: start; }
  .col-op { grid-area: op; flex-direction: row; justify-self: end; }
  .col-price { display: none; }
}
@media (max-width: 600px) {
  .cart-page { padding: 16px 16px 48px; }
  .page-head h2 { font-size: var(--fs-h2); }
  .settle-bar { flex-wrap: wrap; gap: 12px; padding: 14px 16px; }
  .settle-right { width: 100%; justify-content: space-between; gap: 12px; }
  .btn-checkout { flex: 1; height: 48px; padding: 0 24px; letter-spacing: 2px; }
  .total em { font-size: 22px; }
}
</style>