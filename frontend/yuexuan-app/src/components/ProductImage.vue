<template>
  <div class="pimg" :style="{ borderRadius: radius }">
    <!-- 真实图：image 非空且 load 未失败 -->
    <img
      v-if="src && !failed"
      :src="src"
      :alt="alt"
      class="pimg-img"
      @error="failed = true"
    />
    <!-- 占位：无图 / 图加载失败 -->
    <div v-else class="pimg-ph" :style="{ background: cat.bg }">
      <span class="pimg-ico" :style="{ color: cat.fg }">{{ cat.ico }}</span>
      <span class="pimg-tag">{{ tag }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  image?: string         // 后端 image 字段，如 "shoe1.jpg" / 已是完整URL
  category?: string
  name?: string
  radius?: string       // 圆角，默认 8px
  alt?: string
}>(), {
  image: '',
  category: '',
  name: '',
  radius: '8px',
  alt: '商品图',
})

const failed = ref(false)
watch(() => props.image, () => { failed.value = false })

const CATEGORY_MAP: Record<string, { ico: string; bg: string; fg: string; label: string }> = {
  '运动鞋': { ico: '👟', bg: 'linear-gradient(135deg,#FFF3E0,#FFE0B2)', fg: '#E65100', label: '运动鞋' },
  '跑鞋':   { ico: '🏃', bg: 'linear-gradient(135deg,#E3F2FD,#BBDEFB)', fg: '#1565C0', label: '跑鞋' },
  'T恤':   { ico: '👕', bg: 'linear-gradient(135deg,#F3E5F5,#E1BEE7)', fg: '#7B1FA2', label: 'T恤' },
  '衣服':   { ico: '🧥', bg: 'linear-gradient(135deg,#FCE4EC,#F8BBD0)', fg: '#C2185B', label: '服装' },
  '数码':   { ico: '🎧', bg: 'linear-gradient(135deg,#E0F7FA,#B2EBF2)', fg: '#00838F', label: '数码' },
  '食品':   { ico: '🥜', bg: 'linear-gradient(135deg,#F1F8E9,#DCEDC8)', fg: '#558B2F', label: '食品' },
  '家居':   { ico: '🏠', bg: 'linear-gradient(135deg,#EFEBE9,#D7CCC8)', fg: '#5D4037', label: '家居' },
}
const DEFAULT_CAT = { ico: '📦', bg: 'linear-gradient(135deg,#ECEFF1,#CFD8DC)', fg: '#455A64', label: '商品' }

const cat = computed(() => CATEGORY_MAP[props.category] || DEFAULT_CAT)

const src = computed(() => {
  const im = props.image
  if (!im) return ''
  // 完整 http(s) URL 直接用
  if (/^https?:\/\//i.test(im)) return im
  // 已经带 / 前缀（如 "/images/xx.jpg"）
  if (im.startsWith('/')) {
    // 只编码路径部分
    const parts = im.split('/')
    return parts.map(p => p ? encodeURIComponent(p) : '').join('/') || im
  }
  // 否则当作 public/images 下的文件名，对中文名编码
  return `/images/${encodeURIComponent(im)}`
})

const tag = computed(() => cat.value.label || (props.name || 'の商品'))
const _alt = computed(() => props.alt || props.name || cat.value.label || '商品图')
</script>

<style scoped>
.pimg {
  width: 100%;
  aspect-ratio: 1 / 1;
  overflow: hidden;
  position: relative;
  background: #fff;
}
.pimg-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.pimg-ph {
  width: 100%; height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  user-select: none;
}
.pimg-ico { font-size: 3.2em; line-height: 1; }
.pimg-tag {
  font-size: 12px;
  padding: 2px 10px;
  background: rgba(255,255,255,0.6);
  border-radius: 999px;
  color: #555;
}
</style>