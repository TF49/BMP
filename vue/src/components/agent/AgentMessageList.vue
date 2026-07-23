<template>
  <div ref="scrollRef" class="agent-msg-list">
    <div
      v-for="msg in messages"
      :key="msg.id"
      class="agent-msg"
      :class="msg.role === 'user' ? 'agent-msg--user' : 'agent-msg--agent'"
    >
      <div class="agent-msg__bubble" :class="{ 'agent-msg__bubble--error': msg.error }">
        <!-- 用户消息：纯文本 -->
        <div v-if="msg.role === 'user'" class="agent-msg__text">{{ msg.content }}</div>

        <!-- Agent 消息：安全 Markdown -->
        <template v-else>
          <div class="agent-md" v-html="renderMarkdown(msg.content)"></div>

          <!-- 受控图表 -->
          <AgentChart
            v-for="(chart, ci) in chartActions(msg)"
            :key="`${msg.id}-chart-${ci}`"
            :chart-data="chart.chart_data || chart.chartData"
          />

          <!-- 待确认动作 -->
          <AgentActionConfirm
            v-for="action in confirmActions(msg)"
            :key="`${msg.id}-action-${action.actionId || action.action_id}`"
            :action="normalizeAction(action)"
            :loading="pendingActionId === (action.actionId || action.action_id)"
            :disabled="Boolean(msg.actionResolved)"
            @confirm="(a) => emit('confirm', { message: msg, action: a })"
            @reject="(a) => emit('reject', { message: msg, action: a })"
          />

          <!-- 引用来源 -->
          <div v-if="msg.references && msg.references.length" class="agent-msg__refs">
            <div class="agent-msg__refs-title">参考来源</div>
            <div
              v-for="(ref, ri) in msg.references"
              :key="`${msg.id}-ref-${ri}`"
              class="agent-msg__ref"
            >
              <span class="agent-msg__ref-name">{{ refTitle(ref) }}</span>
              <span v-if="ref.version" class="agent-msg__ref-meta">版本 {{ ref.version }}</span>
              <span v-if="ref.updatedAt || ref.updated_at" class="agent-msg__ref-meta">
                更新于 {{ ref.updatedAt || ref.updated_at }}
              </span>
            </div>
          </div>

          <!-- 错误重试操作 -->
          <div v-if="msg.error" class="agent-msg__retry">
            <el-button type="danger" link size="small" :disabled="loading" @click="emit('retry', msg)">
              重试
            </el-button>
          </div>

          <div v-if="msg.traceId" class="agent-msg__trace">TraceId：{{ msg.traceId }}</div>
        </template>
      </div>
    </div>

    <!-- 打字中占位 -->
    <div v-if="loading" class="agent-msg agent-msg--agent">
      <div class="agent-msg__bubble agent-msg__typing">
        <span class="dot"></span><span class="dot"></span><span class="dot"></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import AgentChart from './AgentChart.vue'
import AgentActionConfirm from './AgentActionConfirm.vue'
import { renderAgentMarkdown } from '@/utils/agentMarkdown'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  // 正在处理中的动作 ID（确认/拒绝提交期间）
  pendingActionId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['confirm', 'reject', 'retry'])

const scrollRef = ref(null)

function renderMarkdown(content) {
  return renderAgentMarkdown(content)
}

/** 从消息 actions 中筛出渲染图表的动作。 */
function chartActions(msg) {
  return (msg.actions || []).filter((a) => a && a.type === 'render_chart')
}

/** 从消息 actions 中筛出需要人工确认的动作（排除图表）。 */
function confirmActions(msg) {
  if (!msg.requiresAction) return []
  return (msg.actions || []).filter((a) => a && a.type !== 'render_chart')
}

function normalizeAction(action) {
  return {
    ...action,
    actionId: action.actionId || action.action_id
  }
}

function refTitle(ref) {
  return ref.title || ref.name || ref.source || '未命名来源'
}

async function scrollToBottom() {
  await nextTick()
  const el = scrollRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

watch(
  () => [props.messages.length, props.loading],
  () => {
    scrollToBottom()
  }
)
</script>

<style scoped>
.agent-msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: var(--el-fill-color-blank, #f5f7fa);
}

.agent-msg {
  display: flex;
}

.agent-msg--user {
  justify-content: flex-end;
}

.agent-msg--agent {
  justify-content: flex-start;
}

.agent-msg__bubble {
  max-width: 82%;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.agent-msg--user .agent-msg__bubble {
  background: var(--el-color-primary, #409eff);
  color: #fff;
  border-bottom-right-radius: 2px;
}

.agent-msg--agent .agent-msg__bubble {
  background: #fff;
  color: var(--el-text-color-primary, #303133);
  border-bottom-left-radius: 2px;
}

.agent-msg__bubble--error {
  border: 1px solid var(--el-color-danger, #f56c6c);
  background: #fef2f2;
}

.agent-msg__retry {
  margin-top: 6px;
}

.agent-msg__text {
  white-space: pre-wrap;
  word-break: break-word;
}

.agent-msg__refs {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed var(--el-border-color, #dcdfe6);
}

.agent-msg__refs-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  margin-bottom: 4px;
}

.agent-msg__ref {
  font-size: 12px;
  color: var(--el-text-color-regular, #606266);
}

.agent-msg__ref-meta {
  margin-left: 8px;
  color: var(--el-text-color-secondary, #909399);
}

.agent-msg__trace {
  margin-top: 8px;
  font-size: 11px;
  color: var(--el-text-color-placeholder, #a8abb2);
}

.agent-msg__typing {
  display: flex;
  gap: 4px;
  align-items: center;
}

.agent-msg__typing .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--el-text-color-placeholder, #a8abb2);
  animation: agent-typing 1.2s infinite ease-in-out;
}

.agent-msg__typing .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.agent-msg__typing .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes agent-typing {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: translateY(0);
  }
  30% {
    opacity: 1;
    transform: translateY(-3px);
  }
}
</style>

<style>
/* Markdown 渲染的全局样式（v-html 内容不受 scoped 影响） */
.agent-md .agent-md-h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 10px 0 6px;
}

.agent-md .agent-md-h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 8px 0 4px;
}

.agent-md .agent-md-p {
  margin: 4px 0;
  word-break: break-word;
}

.agent-md .agent-md-ul {
  margin: 4px 0;
  padding-left: 20px;
}

.agent-md .agent-md-code {
  background: rgba(0, 0, 0, 0.06);
  border-radius: 3px;
  padding: 1px 5px;
  font-family: 'Menlo', 'Consolas', monospace;
  font-size: 12px;
}

/* 表格样式 */
.agent-md-table-wrap {
  width: 100%;
  overflow-x: auto;
  margin: 10px 0;
}

.agent-md-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  text-align: left;
}

.agent-md-table th,
.agent-md-table td {
  padding: 6px 10px;
  border: 1px solid var(--el-border-color-light, #e4e7ed);
}

.agent-md-table th {
  background-color: var(--el-fill-color-light, #f5f7fa);
  font-weight: 600;
}

.agent-md-table tr:nth-child(even) td {
  background-color: var(--el-fill-color-lighter, #fafafa);
}
</style>

