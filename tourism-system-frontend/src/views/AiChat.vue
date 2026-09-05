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
    <div v-show="visible" class="ai-panel">
      <!-- 头部 -->
      <div class="ai-header">
        <div class="ai-header-title">
          <el-icon :size="18"><MagicStick /></el-icon>
          <span>AI 旅游助手 · 小旅</span>
        </div>
        <span class="ai-header-sub">智谱 GLM-4-Flash 驱动</span>
      </div>

      <!-- 消息区域 -->
      <div class="ai-messages" ref="messagesRef">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="ai-msg"
          :class="msg.role === 'user' ? 'is-user' : 'is-ai'"
        >
          <div class="ai-bubble">{{ msg.content }}</div>
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
          placeholder="问我任何旅游问题..."
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
import { computed, nextTick, ref } from 'vue'
import { ChatDotRound, Close, MagicStick, Promotion, VideoPause } from '@element-plus/icons-vue'
import { chatWithAIStream } from '@/api/ai.js'

const visible = ref(false)
const input = ref('')
const messages = ref([
  { role: 'assistant', content: '你好，我是AI旅游助手小旅 🧭\n可以帮你推荐景点、规划行程、解答旅游问题，快来试试吧！' }
])
const messagesRef = ref(null)
const loading = ref(false)
let abortController = null

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

// 刷新追问建议: 从问题池中排除用户已问过的, 随机取3个
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
      refreshFollowUps()   // 回答结束, 换一批追问建议
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
  width: 380px;
  height: 560px;
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
  max-width: 82%;
  padding: 9px 13px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-msg.is-user .ai-bubble {
  background: linear-gradient(135deg, #2d7cf5, #667eea);
  color: #fff;
  border-top-right-radius: 4px;
}
.ai-msg.is-ai .ai-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
  border-top-left-radius: 4px;
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
}
.ai-send-btn {
  height: 32px;
  padding: 0 14px;
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
</style>
