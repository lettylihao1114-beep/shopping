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
.result-page { padding: 40px 20px; display: grid; place-items: center; min-height: 60vh; }
.result-card {
  background: #fff;
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-md);
  padding: 50px 60px;
  text-align: center;
  max-width: 560px;
  width: 100%;
}
.big-ico {
  font-size: 72px;
  margin-bottom: 16px;
  animation: pop 0.5s ease;
}
@keyframes pop {
  0% { transform: scale(0); }
  70% { transform: scale(1.2); }
  100% { transform: scale(1); }
}
.result-card h2 {
  font-size: 26px; font-weight: 700;
  color: var(--text-primary); margin-bottom: 8px;
}
.result-card > p { font-size: 15px; color: var(--text-secondary); margin-bottom: 32px; }

/* 流程 */
.flow {
  display: flex; align-items: center; justify-content: center;
  gap: 8px; margin: 0 auto 32px; flex-wrap: wrap;
}
.step {
  display: flex; flex-direction: column; align-items: center; gap: 6px;
  width: 110px;
}
.step span {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: var(--bg-gray); color: var(--text-muted);
  display: grid; place-items: center;
  font-size: 15px; font-weight: 700;
  border: 2px solid var(--border);
}
.step b { font-size: 13px; color: var(--text-regular); font-weight: 600; }
.step i { font-size: 11px; color: var(--text-muted); font-style: normal; }
.step.done span { background: var(--primary); color: #fff; border-color: var(--primary); }
.step.done b { color: var(--primary); }
.line { width: 24px; height: 2px; background: var(--border); align-self: flex-start; margin-top: 17px; }

/* 按钮 */
.links {
  display: flex; gap: 14px; justify-content: center;
  margin-bottom: 28px; flex-wrap: wrap;
}
.btn-primary {
  padding: 11px 30px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  border-radius: var(--r-round); font-size: 15px; font-weight: 600;
  box-shadow: 0 4px 12px rgba(255,68,0,0.3);
}
.btn-secondary {
  padding: 11px 30px;
  background: var(--bg-gray); color: var(--text-regular);
  border: 1px solid var(--border);
  border-radius: var(--r-round); font-size: 15px;
}
.btn-secondary:hover { color: var(--primary); border-color: var(--primary); }

.hint { font-size: 13px; color: var(--text-muted); }
.hint a { color: var(--primary); }
</style>