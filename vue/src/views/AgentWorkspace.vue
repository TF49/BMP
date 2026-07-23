<template>
  <div class="agent-workspace">
    <div class="agent-workspace__header">
      <div class="agent-workspace__title-wrap">
        <el-icon class="agent-workspace__icon"><MagicStick /></el-icon>
        <div>
          <div class="agent-workspace__title">经营分析助手</div>
          <div class="agent-workspace__subtitle">
            用自然语言查询经营总览、预约趋势、场地利用率、财务与场馆对比。数据均来自后端受控统计，结论可追溯。
          </div>
        </div>
      </div>
      <div class="agent-workspace__actions">
        <el-button v-if="messages.length > 0" text type="danger" :disabled="loading" @click="handleNewSession">
          <el-icon><Plus /></el-icon>
          <span>新建会话</span>
        </el-button>
        <el-button text type="primary" @click="goDashboard">
          <el-icon><Odometer /></el-icon>
          <span>返回数据看板</span>
        </el-button>
      </div>
    </div>

    <div class="agent-workspace__body">
      <!-- 首屏欢迎与快捷提问 -->
      <div v-if="messages.length === 0" class="agent-workspace__welcome">
        <div class="agent-workspace__welcome-title">你好，我是经营分析助手 👋</div>
        <div class="agent-workspace__welcome-desc">可以试试下面的问题，或直接输入你的分析需求：</div>
        <div class="agent-workspace__chips">
          <el-tag
            v-for="(q, i) in quickPrompts"
            :key="i"
            class="agent-workspace__chip"
            :class="{ 'is-disabled': loading }"
            effect="plain"
            round
            @click="onQuickPrompt(q)"
          >
            {{ q }}
          </el-tag>
        </div>
      </div>

      <AgentMessageList
        v-else
        :messages="messages"
        :loading="loading"
        :pending-action-id="pendingActionId"
        @confirm="handleConfirmAction"
        @reject="handleRejectAction"
        @retry="handleRetry"
      />
    </div>

    <AgentComposer :loading="loading" @send="handleSend" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { MagicStick, Odometer, Plus } from '@element-plus/icons-vue'
import AgentMessageList from '@/components/agent/AgentMessageList.vue'
import AgentComposer from '@/components/agent/AgentComposer.vue'
import {
  createAgentConversation,
  sendAgentMessage,
  confirmAgentAction,
  rejectAgentAction,
  deleteAgentConversation
} from '@/api/agent'

const router = useRouter()

const AGENT_TYPE = 'ANALYTICS'
const STORAGE_KEY = 'bmp_agent_session_ANALYTICS'

const quickPrompts = [
  '本月经营总览',
  '最近一周预约趋势',
  '各场馆经营对比',
  '场地利用率热力图',
  '本月财务收支趋势',
  '业务收入构成占比'
]

const messages = ref([])
const loading = ref(false)
const conversationId = ref('')
const pendingActionId = ref('')

let seq = 0
function nextId(prefix) {
  seq += 1
  return `${prefix}-${Date.now()}-${seq}`
}

function saveLocalSession() {
  try {
    const session = {
      conversationId: conversationId.value,
      messages: messages.value
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } catch (e) {
    // 写入失败降级处理
  }
}

function restoreLocalSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (parsed?.conversationId) conversationId.value = parsed.conversationId
      if (Array.isArray(parsed?.messages)) messages.value = parsed.messages
    }
  } catch (e) {
    localStorage.removeItem(STORAGE_KEY)
  }
}

onMounted(() => {
  restoreLocalSession()
})

async function handleNewSession() {
  try {
    await ElMessageBox.confirm('确定要清空当前会话并创建新会话吗？', '新建会话', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (conversationId.value) {
      try {
        await deleteAgentConversation(conversationId.value)
      } catch (e) {
        // 删除失败静默降级
      }
    }
    conversationId.value = ''
    messages.value = []
    localStorage.removeItem(STORAGE_KEY)
    ElMessage.success('已清空当前会话')
  } catch (e) {
    // 用户取消
  }
}

function goDashboard() {
  router.push('/dashboard')
}

function onQuickPrompt(text) {
  if (loading.value) return
  handleSend(text)
}

/** 惰性创建会话，返回 conversationId。 */
async function ensureConversation() {
  if (conversationId.value) return conversationId.value
  const res = await createAgentConversation(AGENT_TYPE)
  conversationId.value = res?.data?.conversationId || ''
  saveLocalSession()
  return conversationId.value
}

/** 将错误转换为面向用户的内联提示文案。 */
function resolveErrorText(error) {
  const status = error?.response?.status
  if (status === 403) {
    return '❌ 你没有访问经营分析的权限（仅限会长与场馆管理者）。'
  }
  if (status === 429) {
    return '⏳ 请求过于频繁，请稍后再试。'
  }
  if (status === 503 || status === 502 || status === 504 || status === 500) {
    return '⚠️ 智能助手暂时不可用，请稍后重试，或点击右上角「返回数据看板」查看经营数据。'
  }
  // axios 网络错误：有 request 但无 response
  if (error?.isAxiosError || error?.request) {
    return '⚠️ 网络异常或智能助手未启动，请稍后重试，或使用数据看板查看经营数据。'
  }
  // 业务码非 200：拦截器 reject 的普通 Error
  return error?.message || '请求失败，请稍后重试。'
}

async function handleSend(text) {
  if (loading.value) return
  const content = String(text || '').trim()
  if (!content) return

  messages.value.push({
    id: nextId('user'),
    role: 'user',
    content
  })
  saveLocalSession()

  loading.value = true
  try {
    const cid = await ensureConversation()
    if (!cid) {
      throw new Error('会话创建失败，请稍后重试。')
    }
    const res = await sendAgentMessage(cid, {
      content,
      messageId: nextId('msg')
    })
    const data = res?.data || {}
    messages.value.push({
      id: nextId('agent'),
      role: 'agent',
      content: data.response || '（未返回内容）',
      type: data.type || 'text',
      requiresAction: Boolean(data.requiresAction),
      actions: Array.isArray(data.actions) ? data.actions : [],
      references: Array.isArray(data.references) ? data.references : [],
      traceId: data.traceId || ''
    })
  } catch (error) {
    messages.value.push({
      id: nextId('agent'),
      role: 'agent',
      content: resolveErrorText(error),
      error: true,
      rawContent: content,
      actions: [],
      references: []
    })
  } finally {
    loading.value = false
    saveLocalSession()
  }
}

async function handleConfirmAction({ message, action }) {
  const actId = action.actionId || action.action_id
  if (!actId || pendingActionId.value) return

  pendingActionId.value = actId
  try {
    const cid = await ensureConversation()
    await confirmAgentAction(cid, actId)
    message.actionResolved = true
    messages.value.push({
      id: nextId('agent'),
      role: 'agent',
      content: '✅ 动作已确认并成功提交处理。'
    })
  } catch (error) {
    ElMessage.error(resolveErrorText(error))
  } finally {
    pendingActionId.value = ''
    saveLocalSession()
  }
}

async function handleRejectAction({ message, action }) {
  const actId = action.actionId || action.action_id
  if (!actId || pendingActionId.value) return

  pendingActionId.value = actId
  try {
    const cid = await ensureConversation()
    await rejectAgentAction(cid, actId)
    message.actionResolved = true
    messages.value.push({
      id: nextId('agent'),
      role: 'agent',
      content: '🚫 动作已被取消。'
    })
  } catch (error) {
    ElMessage.error(resolveErrorText(error))
  } finally {
    pendingActionId.value = ''
    saveLocalSession()
  }
}

function handleRetry(msg) {
  if (loading.value) return
  // 如果包含了失败发送的 rawContent，则重试重新发送
  const textToRetry = msg.rawContent
  if (textToRetry) {
    // 移除当前的错误消息
    const idx = messages.value.findIndex((m) => m.id === msg.id)
    if (idx !== -1) {
      messages.value.splice(idx, 1)
    }
    // 移除之前的用户消息重发
    const lastUserIdx = messages.value.findLastIndex((m) => m.role === 'user' && m.content === textToRetry)
    if (lastUserIdx !== -1) {
      messages.value.splice(lastUserIdx, 1)
    }
    handleSend(textToRetry)
  }
}

</script>

<style scoped>
.agent-workspace {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 84px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.agent-workspace__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  border-bottom: 1px solid var(--el-border-color-light, #e4e7ed);
}

.agent-workspace__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.agent-workspace__title-wrap {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.agent-workspace__icon {
  font-size: 22px;
  color: var(--el-color-primary, #409eff);
  margin-top: 2px;
}

.agent-workspace__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
}

.agent-workspace__subtitle {
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
  margin-top: 2px;
  max-width: 620px;
}

.agent-workspace__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.agent-workspace__welcome {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 24px;
  text-align: center;
}

.agent-workspace__welcome-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
}

.agent-workspace__welcome-desc {
  font-size: 13px;
  color: var(--el-text-color-secondary, #909399);
  margin: 8px 0 16px;
}

.agent-workspace__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  max-width: 560px;
}

.agent-workspace__chip {
  cursor: pointer;
}

.agent-workspace__chip.is-disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
