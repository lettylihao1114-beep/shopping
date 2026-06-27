<template>
  <div class="page-login">
    <!-- 左侧品牌区 -->
    <section class="brand-side">
      <div class="brand-inner">
        <div class="logo">
          <span class="mark">悦</span>
          <div>
            <strong>悦选</strong>
            <small>ADMIN CONSOLE</small>
          </div>
        </div>
        <h1>统一管理后台<br>掌控全局运营</h1>
        <p>商品 · 订单 · 数据 一站式统一调度</p>
        <ul class="feats">
          <li><span>📊</span><div><b>实时数据看板</b><small>订单/销售/状态全盘监控</small></div></li>
          <li><span>📦</span><div><b>商品全周期管理</b><small>新增/编辑/上下架一体</small></div></li>
          <li><span>🚚</span><div><b>订单发货流转</b><small>状态机驱动，链路可追踪</small></div></li>
          <li><span>🔐</span><div><b>JWT 网关鉴权</b><small>角色权限分层，越权即拒</small></div></li>
        </ul>
      </div>
    </section>

    <!-- 右侧登录卡 -->
    <section class="form-side">
      <div class="card">
        <h2>欢迎回来 👋</h2>
        <p class="lead">请登录管理后台</p>

        <form @submit.prevent="handleLogin" class="form">
          <label class="field">
            <span>账号</span>
            <div class="ipt">
              <el-icon><User /></el-icon>
              <input v-model.trim="username" placeholder="请输入用户名" autocomplete="username" />
            </div>
          </label>
          <label class="field">
            <span>密码</span>
            <div class="ipt">
              <el-icon><Lock /></el-icon>
              <input v-model="password" type="password" placeholder="请输入密码" autocomplete="current-password" />
            </div>
          </label>
          <div class="row">
            <label class="rem"><input type="checkbox" v-model="remember" /> 记住我</label>
            <a class="forgot">忘记密码？</a>
          </div>
          <button type="submit" class="submit" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </button>
        </form>

        <div class="demo">
          <p class="demo-title">演示账号 · 密码 <code>123456</code></p>
          <div class="chips">
            <span
              v-for="a in demoAccounts" :key="a.u"
              @click="fillDemo(a.u, a.role)"
              :class="{ vip: a.vip }"
            >
              {{ a.u }}<em>{{ a.tag }}</em>
            </span>
          </div>
        </div>

        <p v-if="errMsg" class="err">⚠ {{ errMsg }}</p>

        <p class="back"><a @click="goC">← 返回 C 端商城</a></p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'

const router = useRouter()
const username = ref('admin')
const password = ref('123456')
const loading = ref(false)
const errMsg = ref('')
const remember = ref(false)

const demoAccounts = [
  { u: 'admin',      role: 'platform', tag: '平台', vip: true },
  { u: 'shoe_shop',  role: 'shop',     tag: '商家' },
  { u: 'cloth_shop', role: 'shop',     tag: '商家' },
  { u: 'user1',      role: 'user',     tag: '顾客' },
]

function fillDemo(u, role) {
  username.value = u
  password.value = '123456'
}

async function handleLogin() {
  if (!username.value || !password.value) { errMsg.value = '请输入用户名和密码'; return }
  loading.value = true; errMsg.value = ''
  try {
    const data = await login(username.value, password.value)
    sessionStorage.setItem('admin_token', data.token)
    // 解出 JWT 里的 role 存起来给 Layout 显示；暂不验签，后端已校验
    const role = parseRole(data.token) || ''
    sessionStorage.setItem('admin_user', JSON.stringify({ username: username.value, role }))
    router.push('/dashboard')
  } catch (e) {
    errMsg.value = '登录失败：' + (e.message || '请检查用户名密码')
  } finally { loading.value = false }
}

function parseRole(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return payload?.role || ''
  } catch { return '' }
}

function goC() {
  window.location.href = 'http://localhost:5173/'
}
</script>

<style scoped>
.page-login {
  width: 100%;
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background: var(--bg-page);
}

/* 左侧品牌区 */
.brand-side {
  background: linear-gradient(135deg, #FF4400 0%, #FF7A45 55%, #FFB400 100%);
  position: relative;
  overflow: hidden;
  color: #fff;
  display: grid;
  place-items: center;
}
.brand-side::before {
  content: '';
  position: absolute; inset: 0;
  background:
    radial-gradient(circle at 20% 25%, rgba(255,255,255,0.18), transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(0,0,0,0.12), transparent 45%);
}
.brand-side::after {
  content: '';
  position: absolute;
  width: 340px; height: 340px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.16), transparent 70%);
  top: -70px; right: -90px;
  animation: floaty 9s ease-in-out infinite;
  pointer-events: none;
}
@keyframes floaty {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-24px, 26px); }
}
.brand-inner { position: relative; padding: 60px 70px; max-width: 560px; z-index: 1; }
.logo { display: flex; align-items: center; gap: 14px; margin-bottom: 52px; }
.mark {
  width: 56px; height: 56px;
  background: rgba(255,255,255,0.22);
  border: 2px solid rgba(255,255,255,0.45);
  border-radius: var(--r-md);
  display: grid; place-items: center;
  font-size: 30px; font-weight: 800;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.14);
}
.logo strong { display: block; font-size: var(--fs-h2); font-weight: 800; letter-spacing: 2px; }
.logo small { font-size: var(--fs-xs); letter-spacing: 3px; opacity: 0.85; }

.brand-side h1 {
  font-size: var(--fs-display); font-weight: 800; line-height: 1.25;
  margin-bottom: 16px; text-shadow: 0 4px 16px rgba(0,0,0,0.14);
  letter-spacing: -0.02em;
}
.brand-side > .brand-inner > p { font-size: var(--fs-body); opacity: 0.92; margin-bottom: 40px; }

.feats { display: flex; flex-direction: column; gap: 14px; }
.feats li {
  display: flex; align-items: center; gap: 14px;
  background: rgba(255,255,255,0.13);
  padding: 15px 18px;
  border-radius: var(--r-md);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.18);
  transition: transform 0.4s var(--ease-soft), background 0.4s var(--ease-soft);
}
.feats li:hover { transform: translateX(6px); background: rgba(255,255,255,0.20); }
.feats span { font-size: 24px; flex-shrink: 0; }
.feats b { display: block; font-size: var(--fs-body); font-weight: 700; }
.feats small { font-size: var(--fs-sm); opacity: 0.85; }

/* 右侧 */
.form-side { display: grid; place-items: center; padding: 48px 40px; }
.card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-white);
  border-radius: var(--r-xl);
  padding: 48px 44px 36px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
}
.card h2 { font-size: var(--fs-h1); font-weight: 800; color: var(--text-primary); letter-spacing: -0.02em; }
.card .lead { font-size: var(--fs-body); color: var(--text-muted); margin: 8px 0 32px; }

.form { display: flex; flex-direction: column; gap: 20px; }
.field { display: flex; flex-direction: column; gap: 9px; }
.field > span { font-size: var(--fs-sm); color: var(--text-secondary); font-weight: 500; }
.ipt {
  display: flex; align-items: center; gap: 12px;
  height: 48px; padding: 0 16px;
  border: 1px solid var(--border-strong);
  border-radius: var(--r-md);
  background: var(--bg-soft);
  transition: all 0.35s var(--ease-soft);
}
.ipt .el-icon { color: var(--text-muted); font-size: 18px; flex-shrink: 0; }
.ipt input {
  flex: 1; border: none; background: transparent;
  font-size: var(--fs-body); outline: none;
}
.ipt:focus-within {
  border-color: var(--primary);
  background: var(--bg-white);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: var(--fs-sm);
}
.rem { display: flex; align-items: center; gap: 6px; color: var(--text-secondary); cursor: pointer; }
.rem input { accent-color: var(--primary); }
.forgot { color: var(--primary); cursor: pointer; font-weight: 500; }

.submit {
  height: 52px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  font-size: var(--fs-body); font-weight: 700; letter-spacing: 8px;
  border-radius: var(--r-md);
  box-shadow: 0 8px 20px rgba(255,68,0,0.32);
  transition: all 0.4s var(--ease-soft);
  border: none;
}
.submit:hover:not(:disabled) { transform: translateY(-2px); box-shadow: var(--shadow-glow); }
.submit:disabled { opacity: 0.6; cursor: wait; transform: none; }

.demo {
  margin-top: 30px;
  padding-top: 22px;
  border-top: 1px dashed var(--border);
}
.demo-title { font-size: var(--fs-xs); color: var(--text-muted); margin-bottom: 14px; text-align: center; }
.demo-title code { background: var(--bg-soft); padding: 2px 8px; border-radius: var(--r-xs); color: var(--primary); font-family: monospace; }
.chips { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.chips span {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 6px 14px;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: var(--r-round);
  font-size: var(--fs-sm); color: var(--text-regular);
  cursor: pointer;
  transition: all 0.35s var(--ease-soft);
}
.chips span:hover { background: var(--primary-bg); border-color: var(--primary); color: var(--primary); transform: translateY(-1px); }
.chips span.vip { background: #FFF8E1; border-color: #FFE082; color: #B8860B; }
.chips em { font-style: normal; font-size: 10px; padding: 1px 5px; background: rgba(0,0,0,0.07); border-radius: var(--r-xs); }

.err {
  margin-top: 16px;
  padding: 11px 16px;
  background: #FEF2F2; color: #DC2626;
  font-size: var(--fs-sm); border-radius: var(--r-sm);
  border-left: 3px solid #DC2626; text-align: center;
}
.back { text-align: center; margin-top: 20px; }
.back a { font-size: var(--fs-sm); color: var(--text-muted); cursor: pointer; transition: color 0.3s var(--ease-soft); }
.back a:hover { color: var(--primary); }

@media (max-width: 860px) {
  .page-login { grid-template-columns: 1fr; }
  .brand-side { display: none; }
  .form-side { padding: 24px 16px; }
  .card { max-width: 420px; padding: 32px 24px 28px; }
  .submit { letter-spacing: 4px; }
}
</style>