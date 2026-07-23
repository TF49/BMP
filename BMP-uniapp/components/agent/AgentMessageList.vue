<template>
  <scroll-view
    scroll-y
    class="msg-scroll"
    :scroll-into-view="scrollAnchor"
    :scroll-with-animation="true"
    :show-scrollbar="false"
  >
    <view class="msg-list">
      <view
        v-for="msg in messages"
        :id="'msg-' + msg.id"
        :key="msg.id"
        class="msg-item"
        :class="msg.role === 'user' ? 'msg-item--user' : 'msg-item--agent'"
      >
        <!-- 用户气泡：纯文本 -->
        <view v-if="msg.role === 'user'" class="bubble bubble--user">
          <text class="bubble__text bubble__text--user">{{ msg.content }}</text>
        </view>

        <!-- Agent 气泡 -->
        <view v-else class="bubble" :class="{ 'bubble--error': msg.error }">
          <!-- 结构化文本渲染（跨端安全，不使用 v-html） -->
          <view class="agent-text">
            <view
              v-for="(seg, si) in parseSegments(msg.content)"
              :key="si"
              class="seg"
              :class="'seg--' + seg.type"
            >
              <text v-if="seg.type === 'divider'" class="seg__divider"></text>
              <text v-else-if="seg.type === 'bullet'" class="seg__bullet">• {{ seg.text }}</text>
              <text v-else :class="'seg__text seg__text--' + seg.type">{{ seg.text }}</text>
            </view>
          </view>

          <!-- 转人工结果卡片 -->
          <view
            v-for="(ho, hi) in handoffActions(msg)"
            :key="'ho-' + hi"
            class="info-card info-card--handoff"
          >
            <text class="info-card__title">人工客服转接</text>
            <text class="info-card__line">状态：{{ ho.status }}</text>
            <text v-if="ho.handoff_id" class="info-card__line">单号：{{ ho.handoff_id }}</text>
          </view>

          <!-- 预订确认卡片：仅在需要动作时出现 -->
          <AgentActionConfirm
            v-for="(act, ai) in confirmActions(msg)"
            :key="'act-' + ai"
            :action="act"
            :loading="confirmingId === msg.id"
            :disabled="!!msg.actionDone"
            @confirm="onConfirm(msg, $event)"
            @reject="onReject(msg, $event)"
          />

          <!-- 错误重试按钮 -->
          <view v-if="msg.error" class="retry-wrap" @tap="onRetry(msg)">
            <text class="retry-btn">点击重试</text>
          </view>

          <!-- 引用来源 -->
          <view v-if="msg.references && msg.references.length" class="refs">
            <text class="refs__title">来源</text>
            <view v-for="(ref, ri) in msg.references" :key="'ref-' + ri" class="refs__item">
              <text class="refs__name">{{ ref.title || ref.doc_id || '未命名来源' }}</text>
              <text v-if="ref.version || ref.updated_at" class="refs__meta">
                {{ [ref.version ? '版本 ' + ref.version : '', ref.updated_at ? formatTime(ref.updated_at) : ''].filter(Boolean).join(' · ') }}
              </text>
            </view>
          </view>

          <text v-if="msg.traceId" class="trace">TraceId: {{ msg.traceId }}</text>
        </view>
      </view>

      <!-- 打字中占位 -->
      <view v-if="loading" class="msg-item msg-item--agent">
        <view class="bubble bubble--typing">
          <view class="dot"></view>
          <view class="dot"></view>
          <view class="dot"></view>
        </view>
      </view>

      <view id="agent-bottom-anchor" class="bottom-anchor"></view>
    </view>
  </scroll-view>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import AgentActionConfirm from './AgentActionConfirm.vue'
import type { AgentAction, ChatMessage } from '@/api/agent'

interface TextSegment {
  type: 'heading' | 'subheading' | 'bullet' | 'divider' | 'text'
  text: string
}

const props = withDefaults(
  defineProps<{
    messages: ChatMessage[]
    loading?: boolean
    // 正在提交确认/取消的消息 id
    confirmingId?: string
  }>(),
  {
    loading: false,
    confirmingId: ''
  }
)

const emit = defineEmits<{
  (e: 'confirm', payload: { message: ChatMessage; action: AgentAction }): void
  (e: 'reject', payload: { message: ChatMessage; action: AgentAction }): void
  (e: 'retry', message: ChatMessage): void
}>()

const scrollAnchor = ref('')

// 消息变化或进入加载态时滚动到底部
watch(
  () => [props.messages.length, props.loading],
  () => {
    nextTick(() => {
      // 先清空再赋值，确保重复触底也能生效
      scrollAnchor.value = ''
      nextTick(() => {
        scrollAnchor.value = 'agent-bottom-anchor'
      })
    })
  },
  { immediate: true }
)

/** 将 Agent 文本拆成可安全渲染的结构段，剥离行内 Markdown 标记。 */
function parseSegments(content: string): TextSegment[] {
  if (!content) return []
  const lines = String(content).split('\n')
  const segs: TextSegment[] = []
  for (const raw of lines) {
    const line = raw.trimEnd()
    const trimmed = line.trim()
    if (trimmed === '') continue
    if (/^---+$/.test(trimmed)) {
      segs.push({ type: 'divider', text: '' })
      continue
    }
    // 表格分隔行如 |---|---| 跳过
    if (/^\|?\s*:?-+:?\s*(\|?\s*:?-+:?\s*)+\|?$/.test(trimmed)) {
      continue
    }
    // 表格数据行 | col1 | col2 | 简易转换
    if (trimmed.startsWith('|') && trimmed.endsWith('|')) {
      const cells = trimmed.slice(1, -1).split('|').map((c) => stripInline(c.trim())).filter(Boolean)
      if (cells.length) {
        segs.push({ type: 'text', text: cells.join(' │ ') })
        continue
      }
    }
    if (line.startsWith('### ')) {
      segs.push({ type: 'subheading', text: stripInline(line.slice(4)) })
      continue
    }
    if (line.startsWith('## ')) {
      segs.push({ type: 'heading', text: stripInline(line.slice(3)) })
      continue
    }
    const bullet = line.match(/^\s*[-*]\s+(.*)$/)
    if (bullet) {
      segs.push({ type: 'bullet', text: stripInline(bullet[1]) })
      continue
    }
    segs.push({ type: 'text', text: stripInline(line) })
  }
  return segs
}

/** 剥离 **加粗**、`行内代码`、emoji 前缀等标记，保留可读文本。 */
function stripInline(text: string): string {
  return text
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/^📌\s*/, '')
    .trim()
}

/** 该消息中的预订确认动作。 */
function confirmActions(msg: ChatMessage): AgentAction[] {
  if (!msg.requiresAction || !Array.isArray(msg.actions)) return []
  return msg.actions.filter((a) => a && a.type === 'confirm_booking')
}

/** 该消息中的转人工动作。 */
function handoffActions(msg: ChatMessage): AgentAction[] {
  if (!Array.isArray(msg.actions)) return []
  return msg.actions.filter((a) => a && a.name === 'human_handoff')
}

function formatTime(value?: string): string {
  if (!value) return ''
  const text = String(value)
  // 仅保留到分钟，避免移动端溢出
  return text.length >= 16 ? text.slice(0, 16).replace('T', ' ') : text
}

function onConfirm(message: ChatMessage, action: AgentAction) {
  emit('confirm', { message, action })
}

function onReject(message: ChatMessage, action: AgentAction) {
  emit('reject', { message, action })
}

function onRetry(message: ChatMessage) {
  emit('retry', message)
}

</script>

<style lang="scss" scoped>
.msg-scroll {
  height: 100%;
}

.msg-list {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.msg-item {
  display: flex;
  width: 100%;
}

.msg-item--user {
  justify-content: flex-end;
}

.msg-item--agent {
  justify-content: flex-start;
}

.bubble {
  max-width: 80%;
  padding: 20rpx 24rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.bubble--user {
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
  border-bottom-right-radius: 8rpx;
}

.bubble:not(.bubble--user) {
  border-bottom-left-radius: 8rpx;
}

.bubble--error {
  background: #fef2f2;
  border: 1rpx solid #fecaca;
}

.retry-wrap {
  margin-top: 12rpx;
  display: flex;
}

.retry-btn {
  font-size: 24rpx;
  color: #ef4444;
  font-weight: 700;
  text-decoration: underline;
}

.bubble__text--user {
  color: #ffffff;
  font-size: 28rpx;
  line-height: 1.6;
}

.agent-text {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.seg__text {
  font-size: 28rpx;
  line-height: 1.65;
  color: #0f172a;
}

.seg__text--heading {
  font-size: 30rpx;
  font-weight: 800;
  color: #0f172a;
}

.seg__text--subheading {
  font-size: 28rpx;
  font-weight: 700;
  color: #334155;
}

.seg__bullet {
  font-size: 28rpx;
  line-height: 1.65;
  color: #334155;
}

.seg__divider {
  display: block;
  height: 1rpx;
  margin: 8rpx 0;
  background: rgba(226, 232, 240, 0.9);
}

.info-card {
  margin-top: 18rpx;
  padding: 18rpx 20rpx;
  border-radius: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.info-card--handoff {
  background: #eff6ff;
  border: 1rpx solid #bfdbfe;
}

.info-card__title {
  font-size: 26rpx;
  font-weight: 800;
  color: #1d4ed8;
}

.info-card__line {
  font-size: 24rpx;
  color: #334155;
}

.refs {
  margin-top: 18rpx;
  padding-top: 16rpx;
  border-top: 1rpx dashed rgba(148, 163, 184, 0.5);
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.refs__title {
  font-size: 22rpx;
  font-weight: 800;
  color: #94a3b8;
  letter-spacing: 0.08em;
}

.refs__item {
  display: flex;
  flex-direction: column;
  gap: 2rpx;
}

.refs__name {
  font-size: 24rpx;
  color: #475569;
  font-weight: 700;
}

.refs__meta {
  font-size: 22rpx;
  color: #94a3b8;
}

.trace {
  display: block;
  margin-top: 14rpx;
  font-size: 20rpx;
  color: #cbd5e1;
}

.bubble--typing {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 24rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #cbd5e1;
  animation: blink 1.2s infinite ease-in-out;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

.bottom-anchor {
  height: 1rpx;
}

@keyframes blink {
  0%, 80%, 100% {
    opacity: 0.3;
  }
  40% {
    opacity: 1;
  }
}
</style>
