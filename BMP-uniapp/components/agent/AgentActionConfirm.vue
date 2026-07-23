<template>
  <view class="action-confirm">
    <view class="action-confirm__title">
      <uni-icons type="checkmarkempty" size="16" color="#c2410c" />
      <text class="action-confirm__title-text">请确认预约信息</text>
    </view>

    <!-- 报价明细：金额、时长等一律来自后端受控报价，前端仅展示 -->
    <view v-if="quote" class="action-confirm__grid">
      <view v-if="quote.venueId !== undefined" class="action-confirm__row">
        <text class="action-confirm__label">场馆</text>
        <text class="action-confirm__value">{{ quote.venueId }} 号馆</text>
      </view>
      <view v-if="quote.date" class="action-confirm__row">
        <text class="action-confirm__label">日期</text>
        <text class="action-confirm__value">{{ quote.date }}</text>
      </view>
      <view v-if="timeRange" class="action-confirm__row">
        <text class="action-confirm__label">时段</text>
        <text class="action-confirm__value">{{ timeRange }}</text>
      </view>
      <view v-if="courtText" class="action-confirm__row">
        <text class="action-confirm__label">场地</text>
        <text class="action-confirm__value">{{ courtText }}</text>
      </view>
      <view v-if="quote.duration !== undefined" class="action-confirm__row">
        <text class="action-confirm__label">时长</text>
        <text class="action-confirm__value">{{ quote.duration }} 小时</text>
      </view>
      <view v-if="quote.totalAmount !== undefined" class="action-confirm__row action-confirm__row--amount">
        <text class="action-confirm__label">合计</text>
        <text class="action-confirm__value action-confirm__value--amount">¥{{ quote.totalAmount }}</text>
      </view>
    </view>

    <!-- 已消费提示：一次性确认后禁用，避免重复下单 -->
    <view v-if="disabled" class="action-confirm__done">
      <text class="action-confirm__done-text">该确认已处理</text>
    </view>

    <view v-else class="action-confirm__buttons">
      <view
        class="action-btn action-btn--reject"
        :class="{ 'action-btn--loading': loading }"
        @tap="onReject"
      >
        <text class="action-btn__text action-btn__text--reject">取消</text>
      </view>
      <view
        class="action-btn action-btn--confirm"
        :class="{ 'action-btn--loading': loading }"
        @tap="onConfirm"
      >
        <text class="action-btn__text">{{ loading ? '处理中…' : '确认预约' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { AgentAction, AgentQuote } from '@/api/agent'

const props = withDefaults(
  defineProps<{
    // 单个待执行动作（type = confirm_booking），含 quote 报价
    action: AgentAction
    // 提交确认 / 取消中：禁用按钮，避免并发请求
    loading?: boolean
    // 已处理：一次性消费后禁用
    disabled?: boolean
  }>(),
  {
    loading: false,
    disabled: false
  }
)

const emit = defineEmits<{
  (e: 'confirm', action: AgentAction): void
  (e: 'reject', action: AgentAction): void
}>()

const quote = computed<AgentQuote | null>(() => props.action?.quote ?? null)

const timeRange = computed(() => {
  const q = quote.value
  if (!q || !q.startTime || !q.endTime) return ''
  return `${q.startTime} - ${q.endTime}`
})

const courtText = computed(() => {
  const ids = quote.value?.courtIds
  if (!Array.isArray(ids) || ids.length === 0) return ''
  return ids.map((id) => `${id} 号`).join('、')
})

function onConfirm() {
  if (props.loading || props.disabled) return
  emit('confirm', props.action)
}

function onReject() {
  if (props.loading || props.disabled) return
  emit('reject', props.action)
}
</script>

<style lang="scss" scoped>
.action-confirm {
  margin-top: 20rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #ffffff 0%, #fff7ed 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.2);
}

.action-confirm__title {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 18rpx;
}

.action-confirm__title-text {
  font-size: 26rpx;
  font-weight: 800;
  color: #9a3412;
}

.action-confirm__grid {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.action-confirm__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14rpx 0;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.7);
}

.action-confirm__row--amount {
  border-bottom: none;
}

.action-confirm__label {
  flex: 0 0 auto;
  font-size: 24rpx;
  color: #94a3b8;
  font-weight: 700;
}

.action-confirm__value {
  flex: 1;
  text-align: right;
  font-size: 26rpx;
  color: #0f172a;
  font-weight: 700;
}

.action-confirm__value--amount {
  font-size: 34rpx;
  color: #c2410c;
  font-weight: 800;
}

.action-confirm__done {
  margin-top: 8rpx;
  text-align: center;
}

.action-confirm__done-text {
  font-size: 24rpx;
  color: #94a3b8;
}

.action-confirm__buttons {
  display: flex;
  gap: 16rpx;
  margin-top: 20rpx;
}

.action-btn {
  flex: 1;
  height: 84rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn--loading {
  opacity: 0.6;
}

.action-btn--reject {
  background: #f1f5f9;
}

.action-btn--confirm {
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
  box-shadow: 0 12rpx 24rpx rgba(234, 88, 12, 0.24);
}

.action-btn__text {
  font-size: 28rpx;
  color: #ffffff;
  font-weight: 800;
}

.action-btn__text--reject {
  color: #64748b;
}
</style>
