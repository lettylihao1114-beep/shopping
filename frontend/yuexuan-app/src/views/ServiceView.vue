<template>
  <div class="service-page">
    <div class="chat-container">
      <!-- 头部 -->
      <div class="chat-header">
        <span class="chat-avatar">🤖</span>
        <div>
          <strong>悦选智能客服</strong>
          <small>在线 · 秒回</small>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="chat-body" ref="bodyRef">
        <div v-for="(m, i) in messages" :key="i" :class="['msg-row', m.role]">
          <div class="msg-bubble">{{ m.text }}</div>
        </div>
        <!-- 快捷入口 -->
        <div class="quick-btns" v-if="messages.length <= 1">
          <span v-for="q in quickQuestions" :key="q" @click="send(q)">{{ q }}</span>
        </div>
        <div ref="bottomRef" />
      </div>

      <!-- 输入区 -->
      <div class="chat-footer">
        <input
          v-model="input"
          placeholder="输入你的问题…"
          @keyup.enter="send(input)"
          :disabled="loading"
        />
        <button @click="send(input)" :disabled="loading || !input.trim()">
          {{ loading ? '…' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'

interface Msg { role: 'user' | 'bot'; text: string }

const input = ref('')
const loading = ref(false)
const messages = ref<Msg[]>([
  { role: 'bot', text: '你好！我是悦选智能客服，可以问我商品价格、退换货、物流、商品推荐等问题~' }
])
const bodyRef = ref<HTMLElement | null>(null)
const bottomRef = ref<HTMLElement | null>(null)

const quickQuestions = ['小米多少钱', '怎么退货', '包邮吗', '有什么好吃的']

async function send(text: string) {
  const q = text.trim()
  if (!q || loading.value) return
  messages.value.push({ role: 'user', text: q })
  input.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const res = await fetch(`/qa/ask?q=${encodeURIComponent(q)}`)
    const data = await res.json()
    messages.value.push({ role: 'bot', text: data.answer || '抱歉，出了点问题~' })
  } catch {
    messages.value.push({ role: 'bot', text: '网络故障，请稍后再试~' })
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (bodyRef.value) {
    bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  }
}
</script>

<style scoped>
.service-page {
  max-width: 720px;
  margin: 40px auto;
  padding: 0 16px;
}

.chat-container {
  background: #fff;
  border-radius: var(--r-lg);
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  height: 70vh;
  min-height: 520px;
  overflow: hidden;
  border: 1px solid var(--border-light);
}

/* 头部 */
.chat-header {
  display: flex; align-items: center; gap: 10px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-light);
  background: var(--bg-soft);
}
.chat-avatar { font-size: 32px; }
.chat-header strong { display: block; font-size: 15px; }
.chat-header small { font-size: 12px; color: #22c55e; }

/* 消息区 */
.chat-body {
  flex: 1; overflow-y: auto;
  padding: 16px 20px;
  background: var(--bg-gray);
  display: flex; flex-direction: column; gap: 10px;
}
.msg-row { display: flex; }
.msg-row.user { justify-content: flex-end; }
.msg-row.bot  { justify-content: flex-start; }

.msg-bubble {
  max-width: 78%;
  padding: 10px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-row.user .msg-bubble {
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-row.bot .msg-bubble {
  background: #fff;
  border: 1px solid var(--border);
  border-bottom-left-radius: 4px;
  color: var(--text-regular);
}

/* 快捷按钮 */
.quick-btns {
  display: flex; flex-wrap: wrap; gap: 8px;
  margin-top: 8px;
}
.quick-btns span {
  padding: 6px 16px;
  background: #fff;
  border: 1px solid var(--border-strong);
  border-radius: var(--r-round);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.quick-btns span:hover {
  border-color: var(--primary);
  color: var(--primary);
}

/* 输入区 */
.chat-footer {
  display: flex; gap: 10px;
  padding: 12px 20px;
  border-top: 1px solid var(--border-light);
  background: #fff;
}
.chat-footer input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid var(--border-strong);
  border-radius: var(--r-round);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}
.chat-footer input:focus { border-color: var(--primary); }
.chat-footer button {
  padding: 10px 28px;
  background: linear-gradient(135deg, var(--primary), var(--primary-hover));
  color: #fff;
  border: none;
  border-radius: var(--r-round);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: transform 0.15s;
}
.chat-footer button:hover:not(:disabled) { transform: translateY(-1px); }
.chat-footer button:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 600px) {
  .service-page { margin: 16px auto; padding: 0 12px; }
  .chat-container { height: 76vh; min-height: 440px; }
  .chat-header { padding: 14px 16px; }
  .chat-body { padding: 12px 14px; }
  .msg-bubble { max-width: 86%; font-size: 13px; }
  .chat-footer { padding: 10px 14px; flex-wrap: nowrap; }
  .chat-footer button { padding: 10px 18px; }
}
</style>
