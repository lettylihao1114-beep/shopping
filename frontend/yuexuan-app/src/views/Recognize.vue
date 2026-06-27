<template>
  <div class="container reco-page">
    <nav class="crumb">
      <a @click="$router.push('/')">首页</a>
      <span>›</span>
      <span>AI 识物</span>
    </nav>

    <header class="reco-head">
      <h1>🤖 AI 智能识物</h1>
      <p>上传商品图片，AI 自动识别并匹配商城同款商品</p>
    </header>

    <div class="reco-body">
      <!-- 上传区 -->
      <section class="upload-zone" v-if="!preview" v-reveal>
        <label class="drop-area" @dragover.prevent @drop.prevent="onDrop">
          <input type="file" accept="image/*" @change="onFile" hidden ref="fileInput" />
          <div class="drop-inner">
            <div class="drop-ico">📸</div>
            <h3>点击或拖拽上传商品图片</h3>
            <p>支持 JPG / PNG / WebP，单张不超过 10MB</p>
            <button type="button" class="drop-btn" @click="($refs.fileInput as any).click()">选择图片</button>
          </div>
        </label>
      </section>

      <!-- 预览 + 结果 -->
      <div class="result-layout" v-else>
        <!-- 左侧预览 -->
        <section class="preview-side">
          <div class="preview-box">
            <img :src="preview" alt="预览" />
            <button class="btn-reset" @click="reset">✕ 重新上传</button>
          </div>
        </section>

        <!-- 右侧结果 -->
        <section class="result-side">
          <!-- 模型选择 -->
          <div class="model-switch">
            <span
              v-for="m in models" :key="m.value"
              :class="{ on: model === m.value }"
              @click="onModelChange(m.value)"
            >{{ m.label }}</span>
          </div>

          <button class="btn-detect" @click="detect" :disabled="detecting">
            <span v-if="!detecting">🔍 开始识别</span>
            <span v-else>⏳ 识别中...</span>
          </button>

          <!-- 通用匹配结果（/recognize） -->
          <div class="detections" v-if="productMatch">
            <h3>🎯 最佳匹配</h3>
            <div class="det-card product-card">
              <div class="det-rank">
                <span class="det-conf">{{ (productMatch.confidence * 100).toFixed(1) }}%</span>
                <div class="conf-bar"><i :style="{ width: (productMatch.confidence * 100) + '%' }"></i></div>
              </div>
              <div class="det-info">
                <strong class="det-name">{{ productMatch.product.name }}</strong>
                <span class="det-price">¥{{ productMatch.product.price }}</span>
                <span class="det-class-id">{{ productMatch.product.category }} · 特征匹配</span>
              </div>
              <div class="det-actions">
                <button class="btn-search" @click="goProduct(productMatch.product.pid)">🛒 看同款</button>
              </div>
            </div>
            <!-- 次选匹配 -->
            <div v-if="productMatch.top_scores && productMatch.top_scores.length > 1" class="sub-scores">
              <span class="sub-label">其他可能：</span>
              <span v-for="s in productMatch.top_scores.slice(1)" :key="s.pid" class="sub-chip" @click="goProduct(s.pid)">
                {{ getProductName(s.pid) }} {{ (s.similarity * 100).toFixed(0) }}%
              </span>
            </div>
          </div>

          <!-- 零食检测结果（/detect?model=snacks）— 保持不变 -->
          <div class="detections" v-if="snackResults.length">
            <h3>{{ productMatch ? '🍿 同时检测到的零食' : '🍿 零食检测结果（共 ' + snackResults.length + ' 件）' }}</h3>
            <div class="det-card" v-for="d in snackResults" :key="'snack_' + d.class_id">
              <div class="det-rank">
                <span class="det-conf">{{ (d.confidence * 100).toFixed(1) }}%</span>
                <div class="conf-bar"><i :style="{ width: (d.confidence * 100) + '%' }"></i></div>
              </div>
              <div class="det-info">
                <strong class="det-name">{{ d.class_name }}</strong>
                <span class="det-class-id">🍿 零食模型 · class_id: {{ d.class_id }}</span>
              </div>
              <div class="det-actions">
                <button class="btn-search" @click="searchProduct(d.class_name)">🔍 搜同款</button>
              </div>
            </div>
          </div>

          <!-- 空结果 -->
          <div class="no-result" v-if="done && !productMatch && !snackResults.length">
            <div class="no-ico">🤷</div>
            <p>AI 未能识别出商品</p>
            <span>请尝试更清晰、正面拍摄的商品图片</span>
          </div>

          <!-- 错误 -->
          <div class="err-msg" v-if="errMsg">
            <span>⚠ {{ errMsg }}</span>
          </div>
        </section>
      </div>
    </div>

    <!-- 模型说明 -->
    <footer class="reco-footer">
      <div class="model-badge">
        <span class="mb-dot"></span> MobileNetV3 商品特征匹配 + YOLOv8n 零食检测
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const fileInput = ref<HTMLInputElement | null>(null)
const preview = ref('')
const detecting = ref(false)
const done = ref(false)
const errMsg = ref('')
const model = ref('recognize')
const models = [
  { label: '🌐 通用', value: 'recognize' },
  { label: '🍿 零食', value: 'snacks' },
  { label: '🔀 全部', value: 'both' },
]

// 通用匹配结果
const productMatch = ref<any>(null)
// 零食检测结果
const snackResults = ref<any[]>([])

// 商品名映射（pid → name）
const PRODUCT_NAMES: Record<number, string> = {
  1: '小米14 Ultra', 2: '华为 MatePad Pro', 3: 'Air Max 运动跑鞋',
  4: '纯棉圆领T恤', 5: '坚果礼盒', 6: '北欧风台灯',
  7: 'Maz Maz 番茄薯片', 8: 'Mini Lina 迷你饼干', 9: 'Maz Maz 土豆条',
}

function getProductName(pid: number): string {
  return PRODUCT_NAMES[pid] || `商品#${pid}`
}

function onModelChange(val: string) {
  model.value = val
  productMatch.value = null
  snackResults.value = []
  done.value = false
}

function onFile(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (file) readFile(file)
}
function onDrop(e: DragEvent) {
  const file = e.dataTransfer?.files?.[0]
  if (file) readFile(file)
}
function readFile(file: File) {
  if (file.size > 10 * 1024 * 1024) { errMsg.value = '图片不能超过 10MB'; return }
  const reader = new FileReader()
  reader.onload = () => {
    preview.value = reader.result as string
    errMsg.value = ''; productMatch.value = null; snackResults.value = []; done.value = false
  }
  reader.readAsDataURL(file)
}
function reset() {
  preview.value = ''
  productMatch.value = null
  snackResults.value = []
  errMsg.value = ''
  done.value = false
}

async function detect() {
  if (!preview.value) return
  detecting.value = true; errMsg.value = ''; productMatch.value = null; snackResults.value = []; done.value = false

  try {
    const blob = await fetch(preview.value).then(r => r.blob())
    const form = new FormData()
    form.append('image', blob, 'product.jpg')

    const mode = model.value

    // 通用 / 全部 → 调用 /recognize
    if (mode === 'recognize' || mode === 'both') {
      const res = await axios.post('/yolo/recognize', form, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 30000,
      })
      if (res.data.matched) {
        productMatch.value = res.data
      }
    }

    // 零食 / 全部 → 调用 /detect?model=snacks
    if (mode === 'snacks' || mode === 'both') {
      const form2 = new FormData()
      form2.append('image', blob, 'product.jpg')
      const res = await axios.post('/yolo/detect?model=snacks', form2, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 30000,
      })
      snackResults.value = (res.data.detections || []).sort((a: any, b: any) => b.confidence - a.confidence)
    }
  } catch (e: any) {
    errMsg.value = '识别失败：' + (e.message || '请确认 YOLO 服务已启动')
  } finally {
    detecting.value = false
    done.value = true
  }
}

function goProduct(pid: number) {
  router.push(`/product/${pid}`)
}

function searchProduct(name: string) {
  router.push({ path: '/', query: { kw: name } })
}
</script>

<style scoped>
.reco-page { padding: 24px 32px 64px; }

.crumb {
  font-size: var(--fs-sm); color: var(--text-muted);
  padding: 12px 0 18px;
  display: flex; align-items: center; gap: 8px;
}
.crumb a { color: var(--text-secondary); cursor: pointer; transition: color 0.3s var(--ease-soft); }
.crumb a:hover { color: var(--primary); }

.reco-head {
  text-align: center;
  padding: 32px 0 48px;
}
.reco-head h1 { font-size: var(--fs-h1); font-weight: 800; color: var(--text-primary); letter-spacing: -0.02em; }
.reco-head p { font-size: var(--fs-body); color: var(--text-muted); margin-top: 12px; }

/* 上传区 */
.upload-zone { max-width: 580px; margin: 0 auto; }
.drop-area { display: block; cursor: pointer; }
.drop-inner {
  border: 2px dashed var(--border-strong);
  border-radius: var(--r-xl);
  padding: 72px 32px;
  text-align: center;
  transition: all 0.4s var(--ease-soft);
  background: var(--bg-soft);
  border: 2px dashed var(--border-strong);
}
.drop-inner:hover {
  border-color: var(--primary);
  background: var(--primary-bg);
  box-shadow: var(--shadow-glow);
  transform: translateY(-2px);
}
.drop-ico { font-size: 64px; margin-bottom: 20px; transition: transform 0.4s var(--ease-soft); }
.drop-inner:hover .drop-ico { transform: scale(1.08); }
.drop-inner h3 { font-size: var(--fs-h3); font-weight: 600; color: var(--text-primary); margin-bottom: 10px; }
.drop-inner p { font-size: var(--fs-sm); color: var(--text-muted); margin-bottom: 28px; }
.drop-btn {
  padding: 12px 36px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  font-size: var(--fs-body); font-weight: 600;
  border-radius: var(--r-round);
  box-shadow: 0 6px 16px rgba(255,68,0,0.3);
  transition: all 0.4s var(--ease-soft);
}
.drop-btn:hover { transform: translateY(-2px); box-shadow: var(--shadow-glow); }

/* 结果布局 */
.result-layout {
  display: grid;
  grid-template-columns: 460px 1fr;
  gap: 36px;
  max-width: 1000px;
  margin: 0 auto;
}
@media (max-width: 860px) { .result-layout { grid-template-columns: 1fr; gap: 24px; } }

/* 预览 */
.preview-box {
  position: relative;
  border-radius: var(--r-lg);
  overflow: hidden;
  box-shadow: var(--shadow-card);
  background: var(--bg-card);
  border: 1px solid var(--border-light);
}
.preview-box img {
  width: 100%;
  max-height: 460px;
  object-fit: contain;
  background: var(--bg-soft);
}
.btn-reset {
  position: absolute; top: 14px; right: 14px;
  padding: 7px 16px;
  background: rgba(27,26,23,0.6);
  color: #fff;
  font-size: var(--fs-xs);
  border-radius: var(--r-round);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: background 0.3s var(--ease-soft);
}
.btn-reset:hover { background: rgba(27,26,23,0.82); }

/* 模型切换 */
.model-switch {
  display: flex; gap: 6px;
  margin-bottom: 16px;
  padding: 5px;
  background: var(--bg-soft);
  border-radius: var(--r-md);
  border: 1px solid var(--border-light);
}
.model-switch span {
  flex: 1;
  padding: 9px 0;
  text-align: center;
  font-size: var(--fs-sm); font-weight: 600;
  color: var(--text-muted);
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: all 0.35s var(--ease-soft);
}
.model-switch span:hover { color: var(--text-primary); }
.model-switch span.on {
  background: var(--bg-card);
  color: var(--primary);
  box-shadow: var(--shadow-sm);
}

/* 识别按钮 */
.btn-detect {
  width: 100%;
  height: 54px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  font-size: var(--fs-h3); font-weight: 700;
  border-radius: var(--r-md);
  box-shadow: 0 8px 20px rgba(255,68,0,0.3);
  transition: all 0.4s var(--ease-soft);
  margin-bottom: 28px;
}
.btn-detect:hover:not(:disabled) { transform: translateY(-2px); box-shadow: var(--shadow-glow); }
.btn-detect:disabled { opacity: 0.6; cursor: wait; }

/* 结果卡片 */
.detections h3 {
  font-size: var(--fs-h3); font-weight: 700; color: var(--text-primary);
  margin-bottom: 16px; padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
  letter-spacing: -0.01em;
}
.det-card {
  display: flex; align-items: center; gap: 16px;
  padding: 18px 20px;
  background: var(--bg-card);
  border-radius: var(--r-lg);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  margin-bottom: 12px;
  transition: all 0.4s var(--ease-soft);
}
.det-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.det-card.product-card { border-left: 4px solid var(--primary); }

.det-rank { flex-shrink: 0; width: 76px; }
.det-conf { font-size: var(--fs-h3); font-weight: 800; color: var(--primary); letter-spacing: -0.02em; }
.conf-bar { height: 6px; background: var(--border-light); border-radius: var(--r-round); margin-top: 6px; overflow: hidden; }
.conf-bar i { display: block; height: 100%; background: linear-gradient(90deg, var(--primary), var(--yellow)); border-radius: var(--r-round); transition: width 0.6s var(--ease-soft); }

.det-info { flex: 1; min-width: 0; }
.det-name { font-size: var(--fs-body); font-weight: 600; color: var(--text-primary); display: block; }
.det-price { font-size: var(--fs-h3); font-weight: 800; color: var(--primary); display: block; margin-top: 4px; letter-spacing: -0.02em; }
.det-class-id { font-size: var(--fs-xs); color: var(--text-muted); margin-top: 4px; display: block; }

/* 次选匹配 */
.sub-scores {
  margin-top: 12px; padding: 10px 14px;
  background: var(--bg-soft); border-radius: var(--r-sm); border: 1px solid var(--border-light);
  font-size: var(--fs-xs); color: var(--text-muted);
  display: flex; align-items: center; flex-wrap: wrap; gap: 8px;
}
.sub-label { flex-shrink: 0; }
.sub-chip {
  padding: 3px 12px;
  background: var(--bg-card); border: 1px solid var(--border-light);
  border-radius: var(--r-round);
  cursor: pointer; color: var(--text-secondary);
  transition: all 0.3s var(--ease-soft);
}
.sub-chip:hover { border-color: var(--primary); color: var(--primary); transform: translateY(-1px); }

.btn-search {
  padding: 9px 18px;
  background: var(--primary-bg);
  color: var(--primary);
  font-size: var(--fs-xs); font-weight: 600;
  border-radius: var(--r-round);
  border: 1px solid var(--primary-light);
  white-space: nowrap;
  transition: all 0.35s var(--ease-soft);
}
.btn-search:hover { background: var(--primary); color: #fff; transform: translateY(-1px); box-shadow: 0 4px 10px rgba(255,68,0,0.22); }

/* 空 / 错误 */
.no-result, .err-msg {
  text-align: center;
  padding: 48px 20px;
}
.no-ico { font-size: 56px; margin-bottom: 14px; }
.no-result p { font-size: var(--fs-body); color: var(--text-regular); }
.no-result span { font-size: var(--fs-sm); color: var(--text-muted); }
.err-msg {
  padding: 18px;
  background: #FEF2F2;
  color: #DC2626;
  border-radius: var(--r-md);
  border-left: 3px solid #DC2626;
  font-size: var(--fs-sm);
}

/* 底部模型信息 */
.reco-footer {
  margin-top: 64px;
  text-align: center;
}
.model-badge {
  display: inline-flex; align-items: center; gap: 10px;
  padding: 12px 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--r-round);
  box-shadow: var(--shadow-xs);
  font-size: var(--fs-xs); color: var(--text-muted);
}
.mb-dot { width: 8px; height: 8px; border-radius: 50%; background: #4ade80; box-shadow: 0 0 8px #4ade80; }
</style>
