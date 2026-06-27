import { createApp } from 'vue'
import App from './App.vue'
import router from '@/router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// v-reveal 滚动呼吸指令（与 C 端一致）
const RevealDirective = {
  mounted(el, binding) {
    el.setAttribute('v-reveal', '')
    if (binding.value && binding.value > 1) el.classList.add('reveal-d' + Math.min(binding.value, 4))
    const io = new IntersectionObserver((entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) { e.target.classList.add('reveal-in'); io.unobserve(e.target) }
      })
    }, { threshold: 0.12, rootMargin: '0px 0px -8% 0px' })
    io.observe(el)
    el._revealIO = io
  },
  unmounted(el) { el._revealIO?.disconnect() },
}

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.directive('reveal', RevealDirective)
app.use(router).use(ElementPlus).mount('#app')