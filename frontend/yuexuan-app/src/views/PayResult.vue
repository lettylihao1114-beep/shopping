<template>
  <div class="container result-page">
    <div class="result-card">
      <div class="big-ico">✅</div>
      <h2>支付成功！</h2>
      <p>订单已支付完成，感谢您在悦选购物</p>

      <!-- 模拟流程示意 -->
      <div class="flow">
        <div class="step done"><span>1</span><b>提交订单</b><i>order-service</i></div>
        <div class="line"></div>
        <div class="step done"><span>2</span><b>支付确认</b><i>payment-service</i></div>
        <div class="line"></div>
        <div class="step"><span>3</span><b>商家发货</b><i>等待中…</i></div>
      </div>

      <div class="links">
        <router-link to="/orders" class="btn-primary">📋 查看我的订单</router-link>
        <router-link to="/" class="btn-secondary">继续购物</router-link>
      </div>

      <p class="hint">
        🔍 调用链路已上报 Sleuth，可在
        <a href="http://localhost:9411/zipkin/" target="_blank">Zipkin</a>
        查看本次下单 → 支付 的微服务 trace
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.result-page { padding: 48px 20px; display: grid; place-items: center; min-height: 64vh; }
.result-card {
  background: var(--bg-card);
  border-radius: var(--r-2xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-light);
  padding: 56px 64px;
  text-align: center;
  max-width: 580px;
  width: 100%;
}
.big-ico {
  font-size: 80px;
  margin-bottom: 20px;
  animation: pop 0.6s var(--ease-soft);
}
@keyframes pop {
  0% { transform: scale(0); opacity: 0; }
  70% { transform: scale(1.2); opacity: 1; }
  100% { transform: scale(1); }
}
.result-card h2 {
  font-size: var(--fs-h1); font-weight: 700;
  color: var(--text-primary); margin-bottom: 10px; letter-spacing: -0.02em;
}
.result-card > p { font-size: var(--fs-body); color: var(--text-secondary); margin-bottom: 40px; }

/* 流程 */
.flow {
  display: flex; align-items: center; justify-content: center;
  gap: 10px; margin: 0 auto 40px; flex-wrap: wrap;
}
.step {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  width: 120px;
}
.step span {
  width: 40px; height: 40px;
  border-radius: 50%;
  background: var(--bg-soft); color: var(--text-muted);
  display: grid; place-items: center;
  font-size: var(--fs-body); font-weight: 700;
  border: 2px solid var(--border);
  transition: all 0.4s var(--ease-soft);
}
.step b { font-size: var(--fs-sm); color: var(--text-regular); font-weight: 600; }
.step i { font-size: var(--fs-xs); color: var(--text-muted); font-style: normal; }
.step.done span { background: var(--primary); color: #fff; border-color: var(--primary); box-shadow: 0 6px 16px rgba(255,68,0,0.28); }
.step.done b { color: var(--primary); }
.line { width: 28px; height: 2px; background: var(--border); align-self: flex-start; margin-top: 19px; }

/* 按钮 */
.links {
  display: flex; gap: 16px; justify-content: center;
  margin-bottom: 36px; flex-wrap: wrap;
}
.btn-primary {
  padding: 13px 34px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  border-radius: var(--r-round); font-size: var(--fs-body); font-weight: 600;
  box-shadow: 0 6px 16px rgba(255,68,0,0.3);
  transition: all 0.4s var(--ease-soft);
}
.btn-primary:hover { transform: translateY(-2px); box-shadow: var(--shadow-glow); }
.btn-secondary {
  padding: 13px 34px;
  background: var(--bg-card); color: var(--text-regular);
  border: 1px solid var(--border-strong);
  border-radius: var(--r-round); font-size: var(--fs-body);
  transition: all 0.35s var(--ease-soft);
}
.btn-secondary:hover { color: var(--primary); border-color: var(--primary); transform: translateY(-1px); }

.hint { font-size: var(--fs-sm); color: var(--text-muted); }
.hint a { color: var(--primary); }
</style>