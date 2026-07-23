<template>
  <MobileLayout className="agent-shell">
    <view class="agent-page">
      <!-- 自定义顶栏 -->
      <view class="topbar" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="topbar__row">
          <view class="topbar__back" @tap="handleBack">
            <uni-icons type="left" size="20" color="#0f172a" />
          </view>
          <text class="topbar__title">智能助手</text>
          <view class="topbar__action" @tap="handleClearCurrentSession">
            <uni-icons v-if="current.messages.length" type="trash" size="20" color="#64748b" />
          </view>
        </view>

        <!-- 助手切换 -->
        <view class="tabs">
          <view
            v-for="tab in tabs"
            :key="tab.type"
            class="tabs__item"
            :class="{ 'tabs__item--active': currentType === tab.type }"
            @tap="switchAgent(tab.type)"
          >
            <text class="tabs__text" :class="{ 'tabs__text--active': currentType === tab.type }">
              {{ tab.label }}
            </text>
          </view>
        </view>
      </view>

      <!-- 消息区 -->
      <view class="chat-area">
        <!-- 欢迎与快捷提问 -->
        <scroll-view v-if="!current.messages.length" scroll-y class="welcome-scroll" :show-scrollbar="false">
          <view class="welcome">
            <view class="welcome__icon">
              <uni-icons :type="activeTab.icon" size="30" color="#ea580c" />
            </view>
            <text class="welcome__title">{{ activeTab.title }}</text>
            <text class="welcome__desc">{{ activeTab.desc }}</text>

            <view class="chips">
              <view
                v-for="(q, qi) in activeTab.prompts"
                :key="qi"
                class="chip"
                @tap="onQuickPrompt(q)"
              >
                <text class="chip__text">{{ q }}</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <AgentMessageList
          v-else
          :messages="current.messages"
          :loading="sending"
          :confirming-id="confirmingId"
          @confirm="onConfirm"
          @reject="onReject"
          @retry="onRetry"
        />
      </view>

      <!-- 输入区 -->
      <view class="composer" :style="{ paddingBottom: safeBottom + 'px' }">
        <view class="composer__box">
          <textarea
            v-model="draft"
            class="composer__input"
            :maxlength="maxLength"
            :disabled="sending"
            auto-height
            :show-confirm-bar="false"
            :adjust-position="true"
            placeholder="输入你的问题…"
            placeholder-style="color:#94a3b8"
          />
        </view>
        <view
          class="composer__send"
          :class="{ 'composer__send--disabled': sending || !draft.trim() }"
          @tap="onSend"
        >
          <uni-icons v-if="!sending" type="paperplane-filled" size="20" color="#ffffff" />
          <text v-else class="composer__send-text">…</text>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import MobileLayout from '@/components/MobileLayout.vue'
import AgentMessageList from '@/components/agent/AgentMessageList.vue'
import {
  createAgentConversation,
  deleteAgentConversation,
  sendAgentMessage,
  type AgentAction,
  type AgentResponse,
  type AgentType,
  type ChatMessage
} from '@/api/agent'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'

type SupportedType = Extract<AgentType, 'BOOKING' | 'SUPPORT'>

interface TabConfig {
  type: SupportedType
  label: string
  icon: string
  title: string
  desc: string
  prompts: string[]
}

const userStore = useUserStore()
const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight || 20)
const safeBottom = ref(uni.getSystemInfoSync().safeAreaInsets?.bottom || 0)

const maxLength = 2000
const draft = ref('')
const sending = ref(false)
// 正在提交确认/取消的消息 id，用于确认卡片按钮 loading
const confirmingId = ref('')

const tabs: TabConfig[] = [
  {
    type: 'BOOKING',
    label: '预订助手',
    icon: 'calendar',
    title: '智能预订助手',
    desc: '用自然语言告诉我场馆、日期、时段和场地，我来为你查询空场、报价并确认下单。金额一律由系统核算。',
    prompts: [
      '帮我订1号馆明天19:00-21:00的场地',
      '这周六下午有空场吗',
      '2号馆后天20:00到22:00，3号场地',
      '看看明天晚上还有没有场'
    ]
  },
  {
    type: 'SUPPORT',
    label: '客服助手',
    icon: 'chatbubble',
    title: '场馆客服助手',
    desc: '咨询营业时间、收费标准、退改规则等常见问题；也可查询实时价格与营业状态，或转接人工客服。',
    prompts: [
      '你们的营业时间是几点',
      '1号馆现在实时价格是多少',
      '3号馆现在还开门吗',
      '我要转人工客服'
    ]
  }
]

const currentType = ref<SupportedType>('BOOKING')

// 每种助手各自维护会话与消息，切换时保留上下文
const buckets = reactive<Record<SupportedType, { conversationId: string; messages: ChatMessage[] }>>({
  BOOKING: { conversationId: '', messages: [] },
  SUPPORT: { conversationId: '', messages: [] }
})

const current = computed(() => buckets[currentType.value])
const activeTab = computed(() => tabs.find((t) => t.type === currentType.value) || tabs[0])

let seq = 0
function genId(): string {
  seq += 1
  return `${Date.now()}-${seq}`
}

function saveStorageSession(type: SupportedType) {
  try {
    const key = `bmp_uniapp_agent_session_${type}`
    uni.setStorageSync(key, JSON.stringify(buckets[type]))
  } catch (e) {
    // 静默降级
  }
}

function restoreStorageSessions() {
  ;(['BOOKING', 'SUPPORT'] as SupportedType[]).forEach((type) => {
    try {
      const key = `bmp_uniapp_agent_session_${type}`
      const raw = uni.getStorageSync(key)
      if (raw) {
        const parsed = JSON.parse(raw)
        if (parsed?.conversationId) buckets[type].conversationId = parsed.conversationId
        if (Array.isArray(parsed?.messages)) buckets[type].messages = parsed.messages
      }
    } catch (e) {
      // 静默降级
    }
  })
}

function handleClearCurrentSession() {
  uni.showModal({
    title: '重置会话',
    content: `确定要清空当前【${activeTab.value.label}】的会话记录吗？`,
    success: async (res) => {
      if (res.confirm) {
        const bucket = current.value
        if (bucket.conversationId) {
          try {
            await deleteAgentConversation(bucket.conversationId)
          } catch (e) {
            // 忽略后端删除失败
          }
        }
        bucket.conversationId = ''
        bucket.messages = []
        saveStorageSession(currentType.value)
        uni.showToast({ title: '会话已清空', icon: 'none' })
      }
    }
  })
}

function switchAgent(type: SupportedType) {
  if (type === currentType.value || sending.value) return
  currentType.value = type
}

function handleBack() {
  safeNavigateBack('/pages/index/index')
}

function onQuickPrompt(text: string) {
  void handleSend(text)
}

function onSend() {
  const text = draft.value
  draft.value = ''
  void handleSend(text)
}

function onConfirm(payload: { message: ChatMessage; action: AgentAction }) {
  void handleSend('确认', { fromMessageId: payload.message.id })
}

function onReject(payload: { message: ChatMessage; action: AgentAction }) {
  void handleSend('取消', { fromMessageId: payload.message.id })
}

function onRetry(message: ChatMessage) {
  if (sending.value) return
  const bucket = current.value
  const idx = bucket.messages.findIndex((m) => m.id === message.id)
  if (idx !== -1) {
    bucket.messages.splice(idx, 1)
  }
  const lastUserMsg = bucket.messages.slice().reverse().find((m) => m.role === 'user')
  if (lastUserMsg && lastUserMsg.content) {
    void handleSend(lastUserMsg.content)
  }
}

/** 惰性建会话：首次发送前创建，成功后缓存会话 ID。 */
async function ensureConversation(): Promise<string> {
  const bucket = current.value
  if (bucket.conversationId) return bucket.conversationId
  const conv = await createAgentConversation(currentType.value)
  bucket.conversationId = conv.conversationId
  saveStorageSession(currentType.value)
  return bucket.conversationId
}

function toAgentMessage(res: AgentResponse): ChatMessage {
  return {
    id: genId(),
    role: 'agent',
    content: res.response || '（无内容）',
    requiresAction: res.requiresAction,
    actions: res.actions || [],
    references: res.references || [],
    traceId: res.traceId
  }
}

function markActionDone(bucketMessages: ChatMessage[], messageId: string) {
  const target = bucketMessages.find((m) => m.id === messageId)
  if (target) target.actionDone = true
}

async function handleSend(rawText: string, opts?: { fromMessageId?: string }) {
  const content = (rawText || '').trim()
  if (!content || sending.value) return

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  const bucket = current.value
  bucket.messages.push({ id: genId(), role: 'user', content })
  saveStorageSession(currentType.value)

  if (opts?.fromMessageId) confirmingId.value = opts.fromMessageId
  sending.value = true

  try {
    const conversationId = await ensureConversation()
    const msgId = genId()
    const res = await sendAgentMessage(conversationId, { content, messageId: msgId })
    bucket.messages.push(toAgentMessage(res))
    // 确认/取消成功后，标记来源确认卡片为已处理，避免重复下单
    if (opts?.fromMessageId) markActionDone(bucket.messages, opts.fromMessageId)
  } catch (err) {
    const message = err instanceof Error ? err.message : String(err || '')
    bucket.messages.push({
      id: genId(),
      role: 'agent',
      content: buildDegradeText(message),
      error: true
    })
  } finally {
    sending.value = false
    confirmingId.value = ''
    saveStorageSession(currentType.value)
  }
}

/** 依据错误信息给出内联降级文案，引导用户回到原有业务页面。 */
function buildDegradeText(message: string): string {
  if (message.includes('权限') || message.includes('403')) {
    return '抱歉，当前账号无权使用该助手。你仍可通过底部菜单进入对应功能页办理业务。'
  }
  if (message.includes('频繁') || message.includes('429')) {
    return '请求有点频繁，请稍等几秒再试。'
  }
  if (message.includes('登录') || message.includes('401')) {
    return '登录状态已失效，请重新登录后再试。'
  }
  return '智能助手暂时不可用，请稍后重试。你也可以直接进入场馆预订或客服页面完成操作。'
}

onLoad((query) => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  restoreStorageSessions()
  const type = (query?.type || '').toString().toUpperCase()
  if (type === 'SUPPORT' || type === 'BOOKING') {
    currentType.value = type as SupportedType
  }
})

</script>

<style lang="scss" scoped>
.agent-shell {
  background: linear-gradient(180deg, #fff7ed 0%, #f8fafc 30%, #eef2ff 100%);
}

.agent-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.topbar {
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05);
}

.topbar__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
}

.topbar__back,
.topbar__action,
.topbar__placeholder {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.topbar__title {
  font-size: 34rpx;
  font-weight: 800;
  color: #0f172a;
}

.tabs {
  display: flex;
  gap: 12rpx;
  padding: 6rpx 24rpx 20rpx;
}

.tabs__item {
  flex: 1;
  height: 68rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(241, 245, 249, 0.9);
}

.tabs__item--active {
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
  box-shadow: 0 10rpx 24rpx rgba(234, 88, 12, 0.22);
}

.tabs__text {
  font-size: 26rpx;
  font-weight: 700;
  color: #64748b;
}

.tabs__text--active {
  color: #ffffff;
  font-weight: 800;
}

.chat-area {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.welcome-scroll {
  height: 100%;
}

.welcome {
  padding: 48rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome__icon {
  width: 108rpx;
  height: 108rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(251, 146, 60, 0.14);
  margin-bottom: 24rpx;
}

.welcome__title {
  font-size: 38rpx;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 16rpx;
}

.welcome__desc {
  font-size: 26rpx;
  line-height: 1.65;
  color: #64748b;
  text-align: center;
  margin-bottom: 36rpx;
}

.chips {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.chip {
  padding: 24rpx 28rpx;
  border-radius: 20rpx;
  background: rgba(255, 255, 255, 0.94);
  border: 1rpx solid rgba(148, 163, 184, 0.18);
  box-shadow: 0 8rpx 20rpx rgba(15, 23, 42, 0.04);
}

.chip__text {
  font-size: 28rpx;
  color: #334155;
  font-weight: 600;
}

.composer {
  flex-shrink: 0;
  display: flex;
  align-items: flex-end;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -8rpx 24rpx rgba(15, 23, 42, 0.05);
}

.composer__box {
  flex: 1;
  border-radius: 24rpx;
  background: #f1f5f9;
  padding: 16rpx 20rpx;
}

.composer__input {
  width: 100%;
  min-height: 44rpx;
  max-height: 200rpx;
  font-size: 28rpx;
  line-height: 1.5;
  color: #0f172a;
}

.composer__send {
  width: 84rpx;
  height: 84rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
  box-shadow: 0 12rpx 24rpx rgba(234, 88, 12, 0.24);
}

.composer__send--disabled {
  opacity: 0.5;
}

.composer__send-text {
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 800;
}
</style>
