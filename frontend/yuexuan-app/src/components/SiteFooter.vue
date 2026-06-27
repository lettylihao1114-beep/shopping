<template>
  <footer class="site-footer">
    <div class="container footer-inner">
      <div class="footer-cols">
        <div class="col">
          <h4>购物指南</h4>
          <a>购物流程</a><a>会员介绍</a><a>常见问题</a>
        </div>
        <div class="col">
          <h4>配送方式</h4>
          <a>上门自提</a><a>211 限时达</a><a>配送服务</a>
        </div>
        <div class="col">
          <h4>支付方式</h4>
          <a>货到付款</a><a>在线支付</a><a>分期付款</a>
        </div>
        <div class="col">
          <h4>售后服务</h4>
          <a>售后政策</a><a>价格保护</a><a>退款说明</a>
        </div>
      </div>
      <div class="footer-bottom">
        <p>悦选 YueSelect · Spring Cloud Alibaba 微服务实训项目 © 2026</p>
        <p class="muted">本站为教学实训演示用途 · 数据均为虚构</p>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  margin-top: 96px;
  background: var(--bg-card);
  border-top: 1px solid var(--border-light);
}
.footer-inner { padding: 56px 32px 32px; }
.footer-cols {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  padding-bottom: 32px;
  border-bottom: 1px solid var(--border-light);
}
.col h4 { font-size: var(--fs-body); margin-bottom: 16px; color: var(--text-primary); font-weight: 600; }
.col a {
  display: block;
  font-size: var(--fs-sm);
  color: var(--text-muted);
  margin-bottom: 10px;
  cursor: pointer;
  transition: color 0.3s var(--ease-soft);
}
.col a:hover { color: var(--primary); }
.footer-bottom { padding-top: 24px; text-align: center; }
.footer-bottom p { font-size: var(--fs-sm); color: var(--text-secondary); margin: 4px 0; }
.footer-bottom .muted { color: var(--text-muted); font-size: var(--fs-xs); }
@media (max-width: 640px) {
  .site-footer { margin-top: 64px; }
  .footer-inner { padding: 40px 16px 24px; }
  .footer-cols { grid-template-columns: 1fr 1fr; gap: 28px 20px; }
}
</style>