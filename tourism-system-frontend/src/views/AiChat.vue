<template>
  <!-- 悬浮按钮 -->
  <div class="ai-fab" :class="{ 'is-open': visible }" @click="visible = !visible">
    <el-icon :size="26">
      <ChatDotRound v-if="!visible" />
      <Close v-else />
    </el-icon>
  </div>

  <!-- 对话面板 -->
  <transition name="ai-panel">
    <div v-show="visible" class="ai-panel" :style="panelStyle">
      <!-- 头部 -->
      <div class="ai-header">
        <div class="ai-header-title">
          <el-icon :size="18"><MagicStick /></el-icon>
          <span>AI 旅游助手 · 小旅</span>
        </div>
        <span class="ai-header-sub">智谱 GLM-4-Flash 驱动 · 支持 Markdown</span>
      </div>

      <!-- 消息区域 -->
      <div class="ai-messages" ref="messagesRef" @mousedown.prevent>
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="ai-msg"
          :class="msg.role === 'user' ? 'is-user' : 'is-ai'"
        >
          <!-- 用户消息: 纯文本 -->
          <div v-if="msg.role === 'user'" class="ai-bubble ai-bubble-text">{{ msg.content }}</div>
          <!-- AI 消息: Markdown 渲染(含流式增量) -->
          <div v-else class="ai-bubble ai-bubble-md" v-html="renderMarkdown(msg.content)"></div>
        </div>
        <!-- 生成中的加载动画 -->
        <div v-if="showTyping" class="ai-msg is-ai">
          <div class="ai-bubble ai-typing"><span></span><span></span><span></span></div>
        </div>
      </div>

      <!-- 追问建议(每轮回答结束后刷新, 空闲时显示) -->
      <div v-if="!loading && followUpQuestions.length" class="ai-suggestions">
        <span class="suggestion-label">猜你想问：</span>
        <span v-for="s in followUpQuestions" :key="s" class="suggestion-chip" @click="handleSuggestion(s)">
          {{ s }}
        </span>
      </div>

      <!-- 输入区域 -->
      <div class="ai-input-area">
        <el-input
          v-model="input"
          type="textarea"
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="问我任何旅游问题...(Shift+Enter 换行, Enter 发送)"
          @keydown.enter.exact.prevent="handleSend"
        />
        <el-button
          v-if="!loading"
          type="primary"
          class="ai-send-btn"
          :disabled="!input.trim()"
          @click="handleSend"
        >
          <el-icon><Promotion /></el-icon>
        </el-button>
        <el-button v-else type="warning" class="ai-send-btn" @click="handleStop">
          <el-icon><VideoPause /></el-icon>
        </el-button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { ChatDotRound, Close, MagicStick, Promotion, VideoPause } from '@element-plus/icons-vue'
import { chatWithAIStream } from '@/api/ai.js'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// ─── Markdown 渲染器配置 ─────────────────────────────────────────────────────
// 1) 代码块用 highlight.js 高亮
// 2) 开启行内 HTML 转义(默认关闭), 再手动通过 renderer 恢复 code/pre 块
marked.setOptions({
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: false,   // 不自动把单换行变 <br>, 避免破坏 Markdown 语义
  gfm: true        // GitHub 风格 Markdown
})

const mdRenderer = marked.getDefaults().renderer || new marked.Renderer()

// 把行内 <code> 和块级 <pre><code> 的转义 HTML 还原成可执行标签
function restoreInlineCode(html) {
  // inline code: `` &lt;code&gt;...&lt;/code&gt; `` → <code>...</code>
  return html
    .replace(/&lt;code class="hljs[^"]*"&gt;([\s\S]*?)&lt;\/code&gt;/g, '<code>$1</code>')
    .replace(/&lt;code&gt;([\s\S]*?)&lt;\/code&gt;/g, '<code>$1</code>')
    .replace(/&lt;pre&gt;&lt;code[^&gt;]*&gt;([\s\S]*?)&lt;\/code&gt;&lt;\/pre&gt;/g, '<pre><code>$1</code></pre>')
}

// 挂载时的渲染钩子: 每次 content 更新都重新解析
function renderMarkdown(raw) {
  if (!raw) return ''
  const html = marked.parse(raw)
  return restoreInlineCode(html)
}

// ─── 状态 ────────────────────────────────────────────────────────────────────
const visible = ref(false)
const input = ref('')
const messages = ref([
  { role: 'assistant', content: '你好，我是AI旅游助手小旅 🧭\n可以帮你推荐景点、规划行程、解答旅游问题，快来试试吧！' }
])
const messagesRef = ref(null)
const loading = ref(false)
let abortController = null

// 面板尺寸响应式
const panelWidth = ref(420)
const panelHeight = ref(600)

function updatePanelSize() {
  const w = window.innerWidth
  const h = window.innerHeight
  // 大屏保持固定宽度, 小屏(≤768)全宽
  panelWidth.value = w < 768 ? w - 16 : Math.min(420, w - 40)
  panelHeight.value = h < 700 ? h - 80 : Math.min(640, h - 120)
}
onMounted(() => { updatePanelSize(); window.addEventListener('resize', updatePanelSize) })
onUnmounted(() => window.removeEventListener('resize', updatePanelSize))

const panelStyle = computed(() => ({
  width: panelWidth.value + 'px',
  height: panelHeight.value + 'px'
}))

// 是否显示"打字中"动画: 正在生成且最后一条AI消息还没有内容
const showTyping = computed(() => {
  const last = messages.value[messages.value.length - 1]
  return loading.value && last && last.role === 'assistant' && last.content === ''
})

// 推荐问题池(回答结束后随机展示3个未问过的)
const questionPool = [
  '推荐几个必去的5A景区',
  '预算2000元玩3天怎么安排？',
  '适合亲子游的景点有哪些？',
  '带老人出行有什么推荐？',
  '冬天去哪里旅游比较暖和？',
  '评分最高且免费的景点有哪些？',
  '第一次去北京怎么规划行程？',
  '有哪些适合避暑的景点？',
  '适合拍照打卡的小众景点推荐',
  '情侣出游有什么浪漫的地方？'
]
const followUpQuestions = ref([])

// 刷新追问建议
function refreshFollowUps() {
  const asked = new Set(
    messages.value.filter((m) => m.role === 'user').map((m) => m.content)
  )
  followUpQuestions.value = questionPool
    .filter((q) => !asked.has(q))
    .sort(() => Math.random() - 0.5)
    .slice(0, 3)
}
refreshFollowUps()

// 打开面板时滚到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 点击快捷提问
function handleSuggestion(text) {
  input.value = text
  handleSend()
}

// 发送消息
function handleSend() {
  const text = input.value.trim()
  if (!text || loading.value) return

  // 记录历史(去掉开场白, 只保留真实对话, 最多取最近6条)
  const history = messages.value
    .filter((m, i) => i > 0)
    .map((m) => ({ role: m.role, content: m.content }))

  messages.value.push({ role: 'user', content: text })
  // 提前创建AI占位气泡, 流式过程中只往这一条里追加内容
  messages.value.push({ role: 'assistant', content: '' })
  input.value = ''
  loading.value = true
  scrollToBottom()

  // 流式接收
  abortController = chatWithAIStream({
    message: text,
    history: history.slice(-6),
    onChunk: (chunk) => {
      const last = messages.value[messages.value.length - 1]
      if (last && last.role === 'assistant') {
        last.content += chunk
      }
      scrollToBottom()
    },
    onError: (err) => {
      const last = messages.value[messages.value.length - 1]
      if (last && last.role === 'assistant' && last.content === '') {
        last.content = '❌ ' + err
      }
      loading.value = false
      abortController = null
      scrollToBottom()
    },
    onDone: () => {
      loading.value = false
      abortController = null
      refreshFollowUps()
      scrollToBottom()
    }
  })
}

// 停止生成
function handleStop() {
  if (abortController) {
    abortController.abort()
    abortController = null
  }
  loading.value = false
  const last = messages.value[messages.value.length - 1]
  if (last && last.role === 'assistant' && last.content === '') {
    last.content = '（已停止生成）'
  }
}
</script>

<style scoped>
/* ===== 悬浮按钮 ===== */
.ai-fab {
  position: fixed;
  right: 28px;
  bottom: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2d7cf5, #667eea);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(45, 124, 245, 0.45);
  z-index: 2000;
  transition: transform 0.25s ease;
}
.ai-fab:hover {
  transform: translateY(-3px) scale(1.05);
}

/* ===== 对话面板 ===== */
.ai-panel {
  position: fixed;
  right: 28px;
  bottom: 100px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 2001;
}

/* 头部 */
.ai-header {
  background: linear-gradient(135deg, #2d7cf5, #667eea);
  color: #fff;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.ai-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}
.ai-header-sub {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 2px;
}

/* 消息区域 */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.ai-msg {
  display: flex;
}
.ai-msg.is-user {
  justify-content: flex-end;
}
.ai-msg.is-ai {
  justify-content: flex-start;
}
.ai-bubble {
  max-width: 86%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
  overflow-wrap: break-word;
}
.ai-bubble-text {
  white-space: pre-wrap;
}

/* 用户气泡 */
.ai-msg.is-user .ai-bubble {
  background: linear-gradient(135deg, #2d7cf5, #667eea);
  color: #fff;
  border-top-right-radius: 4px;
}

/* AI 气泡 · Markdown 渲染区 */
.ai-msg.is-ai .ai-bubble {
  background: #fff;
  color: #1f2329;
  border: 1px solid #ebeef5;
  border-top-left-radius: 4px;
}
.ai-bubble-md {
  /* 覆盖 marked 生成的默认样式 */
  font-size: 13px;
  line-height: 1.75;
  word-break: break-word;
  overflow-wrap: break-word;
}
.ai-bubble-md p { margin: 0 0 8px; }
.ai-bubble-md p:last-child { margin-bottom: 0; }
.ai-bubble-md h1, .ai-bubble-md h2, .ai-bubble-md h3,
.ai-bubble-md h4, .ai-bubble-md h5, .ai-bubble-md h6 {
  margin: 10px 0 6px;
  font-weight: 600;
  line-height: 1.4;
  color: #1f2329;
}
.ai-bubble-md h1 { font-size: 18px; border-bottom: 1px solid #e4e7ed; padding-bottom: 4px; }
.ai-bubble-md h2 { font-size: 16px; }
.ai-bubble-md h3 { font-size: 14px; }
.ai-bubble-md ul, .ai-bubble-md ol {
  margin: 6px 0;
  padding-left: 20px;
}
.ai-bubble-md li { margin: 3px 0; }
.ai-bubble-md li::marker { color: #2d7cf5; }
.ai-bubble-md blockquote {
  margin: 8px 0;
  padding: 6px 12px;
  border-left: 4px solid #2d7cf5;
  background: #f0f4ff;
  border-radius: 0 6px 6px 0;
  color: #555;
}
.ai-bubble-md a { color: #2d7cf5; text-decoration: none; }
.ai-bubble-md a:hover { text-decoration: underline; }
.ai-bubble-md strong { color: #1f2329; }
.ai-bubble-md table {
  border-collapse: collapse;
  width: 100%;
  margin: 8px 0;
  font-size: 12px;
}
.ai-bubble-md th, .ai-bubble-md td {
  border: 1px solid #e4e7ed;
  padding: 6px 10px;
  text-align: left;
}
.ai-bubble-md th { background: #f5f7fa; font-weight: 600; }
.ai-bubble-md hr { border: none; border-top: 1px solid #e4e7ed; margin: 10px 0; }

/* 代码块: 使用 highlight.js github 主题 */
.ai-bubble-md code {
  font-family: 'JetBrains Mono', 'Fira Code', 'Cascadia Code', Consolas, Monaco, monospace;
  font-size: 12px;
  background: #f6f8fa;
  color: #e83e8c;
  padding: 1px 5px;
  border-radius: 3px;
}
.ai-bubble-md pre {
  margin: 8px 0;
  padding: 0;
  border-radius: 8px;
  overflow-x: auto;
  background: #f6f8fa;
}
.ai-bubble-md pre code {
  background: none;
  color: inherit;
  padding: 0;
  font-size: 12px;
}

/* 打字中动画 */
.ai-typing {
  display: flex;
  gap: 4px;
  align-items: center;
}
.ai-typing span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: ai-blink 1.2s infinite ease-in-out;
}
.ai-typing span:nth-child(2) { animation-delay: 0.2s; }
.ai-typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes ai-blink {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.85); }
  40% { opacity: 1; transform: scale(1); }
}

/* 快捷提问 */
.ai-suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 14px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
}
.suggestion-label {
  font-size: 11px;
  color: #909399;
  align-self: center;
  margin-right: 2px;
}
.suggestion-chip {
  font-size: 12px;
  color: #2d7cf5;
  background: rgba(45, 124, 245, 0.08);
  border: 1px solid rgba(45, 124, 245, 0.25);
  border-radius: 14px;
  padding: 4px 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.suggestion-chip:hover {
  background: rgba(45, 124, 245, 0.18);
}

/* 输入区域 */
.ai-input-area {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 12px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
}
.ai-send-btn {
  height: 32px;
  padding: 0 14px;
  flex-shrink: 0;
}

/* 面板出入场动画 */
.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.ai-panel-enter-from,
.ai-panel-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.97);
}

/* 滚动条美化 */
.ai-messages::-webkit-scrollbar { width: 5px; }
.ai-messages::-webkit-scrollbar-track { background: transparent; }
.ai-messages::-webkit-scrollbar-thumb { background: #dcdfe6; border-radius: 3px; }
.ai-messages::-webkit-scrollbar-thumb:hover { background: #c0c4cc; }
</style>
