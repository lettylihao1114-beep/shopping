<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <section class="hero-side">
      <div class="hero-inner">
        <div class="brand">
          <span class="brand-mark">悦</span>
          <div>
            <strong>悦选</strong>
            <small>YUE SELECT</small>
          </div>
        </div>
        <h1>AI 智能购物<br>遇见生活美好</h1>
        <p>Spring Cloud Alibaba 微服务实训项目 · 全栈真实链路</p>
        <ul class="feats">
          <li>🛒 两步式防重下单</li>
          <li>🔐 JWT 网关统一鉴权</li>
          <li>📊 管理后台数据看板</li>
          <li>🔍 Sleuth + Zipkin 链路追踪</li>
          <li>⚖️ Sentinel 流控 · Nacos 注册中心</li>
        </ul>
      </div>
    </section>

    <!-- 右侧表单区 -->
    <section class="form-side">
      <div class="form-card">
        <div class="tabs">
          <span :class="{ on: tab === 'login' }" @click="tab = 'login'">
            <b>登录</b><i v-if="tab==='login'"></i>
          </span>
          <span :class="{ on: tab === 'register' }" @click="tab = 'register'">
            <b>注册</b><i v-if="tab==='register'"></i>
          </span>
        </div>

        <!-- 登录表单 -->
        <form v-if="tab === 'login'" @submit.prevent="handleLogin" class="form">
          <label class="field">
            <span>用户名</span>
            <div class="ipt">
              <el-icon><User /></el-icon>
              <input v-model.trim="loginForm.username" placeholder="请输入用户名" autocomplete="username" />
            </div>
          </label>
          <label class="field">
            <span>密码</span>
            <div class="ipt">
              <el-icon><Lock /></el-icon>
              <input v-model="loginForm.password" type="password" placeholder="请输入密码" autocomplete="current-password" />
            </div>
          </label>
          <div class="row">
            <label class="remember"><input type="checkbox" v-model="remember" /> 记住我</label>
            <a class="forgot">忘记密码？</a>
          </div>
          <button type="submit" class="submit" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </button>
        </form>

        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister" class="form">
          <label class="field">
            <span>用户名</span>
            <div class="ipt">
              <el-icon><User /></el-icon>
              <input v-model.trim="regForm.username" placeholder="请设置用户名" />
            </div>
          </label>
          <label class="field">
            <span>密码</span>
            <div class="ipt">
              <el-icon><Lock /></el-icon>
              <input v-model="regForm.password" type="password" placeholder="请设置密码" />
            </div>
          </label>
          <label class="field">
            <span>手机号</span>
            <div class="ipt">
              <el-icon><Phone /></el-icon>
              <input v-model.trim="regForm.phone" placeholder="请输入手机号" />
            </div>
          </label>
          <button type="submit" class="submit" :disabled="loading">
            <span v-if="!loading">注 册</span>
            <span v-else>注册中...</span>
          </button>
        </form>

        <!-- 演示账号 -->
        <div class="demo">
          <p class="demo-title">演示账号 · 密码 <code>123456</code></p>
          <div class="demo-chips">
            <span
              v-for="a in demoAccounts" :key="a.u"
              @click="fillDemo(a.u)"
              :class="{ vip: a.vip }"
            >
              {{ a.u }}<em>{{ a.tag }}</em>
            </span>
          </div>
        </div>

        <p v-if="errMsg" class="err">⚠ {{ errMsg }}</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login, register, parseRole } from '../api'

const router = useRouter()
const tab = ref<'login' | 'register'>('login')
const loading = ref(false)
const errMsg = ref('')
const remember = ref(false)

const loginForm = reactive({ username: 'user1', password: '123456' })
const regForm = reactive({ username: '', password: '', phone: '' })

const demoAccounts = [
  { u: 'user1',      tag: '顾客', vip: false },
  { u: 'shop1',      tag: '商家', vip: false },
  { u: 'shoe_shop',  tag: '商家', vip: false },
  { u: 'cloth_shop', tag: '商家', vip: false },
  { u: 'admin',      tag: '平台', vip: true },
]

const ADMIN_ROLES = ['shop', 'admin', 'platform']

function fillDemo(u: string) {
  loginForm.username = u
  loginForm.password = '123456'
  tab.value = 'login'
}

/** 登录成功后根据角色跳转 */
function redirectByRole(role: string) {
  if (ADMIN_ROLES.includes(role)) {
    router.push('/admin/dashboard')
  } else {
    router.push('/')
  }
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) { errMsg.value = '请输入用户名和密码'; return }
  loading.value = true; errMsg.value = ''
  try {
    const res: any = await login(loginForm.username, loginForm.password)
    const token = res.token
    const role = parseRole(token)
    sessionStorage.setItem('yuexuan_token', token)
    sessionStorage.setItem('yuexuan_user', JSON.stringify({ username: loginForm.username, role }))
    redirectByRole(role)
  } catch (e: any) {
    errMsg.value = '登录失败：' + (e?.message || '账号或密码错误')
  } finally { loading.value = false }
}

async function handleRegister() {
  if (!regForm.username || !regForm.password) { errMsg.value = '请填写用户名和密码'; return }
  loading.value = true; errMsg.value = ''
  try {
    const res: any = await register(regForm.username, regForm.password, regForm.phone)
    const token = res.token
    const role = parseRole(token)
    sessionStorage.setItem('yuexuan_token', token)
    sessionStorage.setItem('yuexuan_user', JSON.stringify({ username: regForm.username, role }))
    redirectByRole(role)
  } catch (e: any) {
    errMsg.value = '注册失败：' + (e?.message || '该用户名可能已存在')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: var(--bg-page);
}

/* 左侧品牌区 */
.hero-side {
  background: linear-gradient(135deg, #FF4400 0%, #FF7A45 55%, #FFB400 100%);
  display: grid; place-items: center;
  position: relative;
  overflow: hidden;
  color: #fff;
}
.hero-side::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 20% 30%, rgba(255,255,255,0.20), transparent 40%),
    radial-gradient(circle at 80% 80%, rgba(0,0,0,0.10), transparent 45%);
}
/* 柔光漂浮装饰，呼吸感 */
.hero-side::after {
  content: '';
  position: absolute;
  width: 320px; height: 320px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.18), transparent 70%);
  top: -60px; right: -80px;
  animation: floaty 9s ease-in-out infinite;
}
@keyframes floaty {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-20px, 24px); }
}
.hero-inner { position: relative; padding: 60px 64px; z-index: 1; }
.brand { display: flex; align-items: center; gap: 14px; margin-bottom: 56px; }
.brand-mark {
  width: 56px; height: 56px;
  background: rgba(255,255,255,0.22);
  border: 2px solid rgba(255,255,255,0.45);
  border-radius: var(--r-md);
  display: grid; place-items: center;
  font-size: 30px; font-weight: 800;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
}
.brand strong { display: block; font-size: var(--fs-h2); font-weight: 800; letter-spacing: 1px; }
.brand small { font-size: var(--fs-xs); letter-spacing: 2px; opacity: 0.85; }

.hero-side h1 {
  font-size: var(--fs-display); font-weight: 800; line-height: 1.25;
  margin-bottom: 18px; text-shadow: 0 4px 16px rgba(0,0,0,0.14);
  letter-spacing: -0.02em;
}
.hero-side p {
  font-size: var(--fs-body); opacity: 0.92; margin-bottom: 44px;
}
.feats { display: flex; flex-direction: column; gap: 14px; }
.feats li {
  display: flex; align-items: center; gap: 14px;
  font-size: var(--fs-body);
  background: rgba(255,255,255,0.13);
  padding: 14px 20px;
  border-radius: var(--r-md);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.18);
  transition: transform 0.4s var(--ease-soft), background 0.4s var(--ease-soft);
}
.feats li:hover { transform: translateX(6px); background: rgba(255,255,255,0.20); }

/* 右侧表单区 */
.form-side { display: grid; place-items: center; padding: 48px 40px; }
.form-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-card);
  border-radius: var(--r-xl);
  padding: 44px 44px 36px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
}
.tabs {
  display: flex;
  gap: 32px;
  padding-bottom: 4px;
  margin-bottom: 28px;
  border-bottom: 1px solid var(--border);
}
.tabs span {
  position: relative;
  padding-bottom: 14px;
  font-size: var(--fs-h3);
  color: var(--text-muted);
  cursor: pointer;
  font-weight: 500;
  transition: color 0.35s var(--ease-soft);
}
.tabs span:hover { color: var(--text-regular); }
.tabs span.on { color: var(--primary); }
.tabs span.on i {
  position: absolute; left: 0; right: 0; bottom: -1px;
  height: 3px; background: var(--primary);
  border-radius: 3px 3px 0 0;
}

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
  background: var(--bg-card);
  box-shadow: 0 0 0 3px var(--primary-light);
}

.row {
  display: flex; justify-content: space-between; align-items: center;
  font-size: var(--fs-sm);
}
.remember { display: flex; align-items: center; gap: 6px; color: var(--text-secondary); cursor: pointer; }
.remember input { accent-color: var(--primary); }
.forgot { color: var(--primary); cursor: pointer; font-weight: 500; }

.submit {
  height: 50px;
  margin-top: 8px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  font-size: var(--fs-body); font-weight: 700; letter-spacing: 4px;
  border-radius: var(--r-md);
  box-shadow: 0 8px 20px rgba(255,68,0,0.3);
  transition: all 0.4s var(--ease-soft);
  border: none;
}
.submit:hover:not(:disabled) { transform: translateY(-2px); box-shadow: var(--shadow-glow); }
.submit:disabled { opacity: 0.6; cursor: wait; transform: none; }

.demo {
  margin-top: 28px;
  padding-top: 22px;
  border-top: 1px dashed var(--border);
}
.demo-title {
  font-size: var(--fs-xs); color: var(--text-muted); margin-bottom: 12px; text-align: center;
}
.demo-title code {
  background: var(--bg-soft); padding: 2px 8px; border-radius: var(--r-xs);
  font-family: monospace; color: var(--primary);
}
.demo-chips {
  display: flex; gap: 8px; justify-content: center; flex-wrap: wrap;
}
.demo-chips span {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px;
  background: var(--bg-soft);
  border-radius: var(--r-round);
  font-size: var(--fs-sm); color: var(--text-regular);
  cursor: pointer;
  border: 1px solid var(--border);
  transition: all 0.35s var(--ease-soft);
}
.demo-chips span:hover { background: var(--primary-bg); border-color: var(--primary); color: var(--primary); transform: translateY(-1px); }
.demo-chips span.vip { background: #FFF8E1; border-color: #FFE082; color: #B8860B; }
.demo-chips span em {
  font-style: normal; font-size: 10px;
  padding: 1px 5px; background: rgba(0,0,0,0.07); border-radius: var(--r-xs);
}

.err {
  margin-top: 16px;
  padding: 11px 16px;
  background: #FEF2F2;
  color: #DC2626;
  font-size: var(--fs-sm);
  border-radius: var(--r-sm);
  border-left: 3px solid #DC2626;
  text-align: center;
}

@media (max-width: 800px) {
  .login-page { grid-template-columns: 1fr; }
  .hero-side { display: none; }
  .form-side { padding: 24px 16px; }
  .form-card { padding: 32px 24px 28px; }
  .submit { letter-spacing: 2px; }
}
</style>
