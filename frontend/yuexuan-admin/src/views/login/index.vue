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
  background: linear-gradient(135deg, #FF4400 0%, #FF7A45 45%, #FFC400 100%);
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
    radial-gradient(circle at 20% 25%, rgba(255,255,255,0.16), transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(0,0,0,0.12), transparent 45%);
}
.brand-inner { position: relative; padding: 60px 70px; max-width: 560px; }
.logo { display: flex; align-items: center; gap: 14px; margin-bottom: 48px; }
.mark {
  width: 54px; height: 54px;
  background: rgba(255,255,255,0.25);
  border: 2px solid rgba(255,255,255,0.5);
  border-radius: var(--r-md);
  display: grid; place-items: center;
  font-size: 30px; font-weight: 800;
  backdrop-filter: blur(8px);
}
.logo strong { display: block; font-size: 24px; font-weight: 800; letter-spacing: 2px; }
.logo small { font-size: 11px; letter-spacing: 3px; opacity: 0.85; }

.brand-side h1 {
  font-size: 38px; font-weight: 800; line-height: 1.3;
  margin-bottom: 14px; text-shadow: 0 3px 12px rgba(0,0,0,0.12);
}
.brand-side > .brand-inner > p { font-size: 15px; opacity: 0.92; margin-bottom: 36px; }

.feats { display: flex; flex-direction: column; gap: 14px; }
.feats li {
  display: flex; align-items: center; gap: 14px;
  background: rgba(255,255,255,0.13);
  padding: 14px 18px;
  border-radius: var(--r-md);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255,255,255,0.18);
}
.feats span { font-size: 22px; flex-shrink: 0; }
.feats b { display: block; font-size: 15px; font-weight: 700; }
.feats small { font-size: 12px; opacity: 0.85; }

/* 右侧 */
.form-side { display: grid; place-items: center; padding: 40px; }
.card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: var(--r-lg);
  padding: 44px 40px 34px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.08);
}
.card h2 { font-size: 26px; font-weight: 700; color: var(--text-primary); }
.card .lead { font-size: 14px; color: var(--text-muted); margin: 6px 0 28px; }

.form { display: flex; flex-direction: column; gap: 18px; }
.field { display: flex; flex-direction: column; gap: 8px; }
.field > span { font-size: 13px; color: var(--text-secondary); }
.ipt {
  display: flex; align-items: center; gap: 10px;
  height: 46px; padding: 0 14px;
  border: 1px solid var(--border-strong);
  border-radius: var(--r-md);
  background: var(--bg-soft);
  transition: all 0.2s;
}
.ipt .el-icon { color: var(--text-muted); font-size: 17px; }
.ipt input {
  flex: 1; border: none; background: transparent;
  font-size: 14px; outline: none;
}
.ipt:focus-within {
  border-color: var(--primary);
  background: #fff;
  box-shadow: 0 0 0 3px var(--primary-light);
}

.row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 13px;
}
.rem { display: flex; align-items: center; gap: 6px; color: var(--text-secondary); cursor: pointer; }
.rem input { accent-color: var(--primary); }
.forgot { color: var(--primary); cursor: pointer; }

.submit {
  height: 48px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  font-size: 16px; font-weight: 700; letter-spacing: 8px;
  border-radius: var(--r-md);
  box-shadow: 0 6px 16px rgba(255,68,0,0.32);
  transition: all 0.2s;
}
.submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 8px 20px rgba(255,68,0,0.42); }
.submit:disabled { opacity: 0.6; cursor: wait; }

.demo {
  margin-top: 26px;
  padding-top: 20px;
  border-top: 1px dashed var(--border);
}
.demo-title { font-size: 12px; color: var(--text-muted); margin-bottom: 12px; text-align: center; }
.demo-title code { background: var(--bg-gray); padding: 1px 6px; border-radius: 3px; color: var(--primary); font-family: monospace; }
.chips { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.chips span {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 6px 14px;
  background: var(--bg-gray);
  border: 1px solid var(--border);
  border-radius: var(--r-round);
  font-size: 12px; color: var(--text-regular);
  cursor: pointer;
  transition: all 0.2s;
}
.chips span:hover { background: var(--primary-bg); border-color: var(--primary); color: var(--primary); }
.chips span.vip { background: #FFF8E1; border-color: #FFE082; color: #B8860B; }
.chips em { font-style: normal; font-size: 10px; padding: 1px 5px; background: rgba(0,0,0,0.08); border-radius: 3px; }

.err {
  margin-top: 14px;
  padding: 10px 14px;
  background: #FEF2F2; color: #DC2626;
  font-size: 13px; border-radius: var(--r-sm);
  border-left: 3px solid #DC2626; text-align: center;
}
.back { text-align: center; margin-top: 18px; }
.back a { font-size: 13px; color: var(--text-muted); cursor: pointer; }
.back a:hover { color: var(--primary); }

@media (max-width: 860px) {
  .page-login { grid-template-columns: 1fr; }
  .brand-side { display: none; }
}
</style>