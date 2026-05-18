<template>
  <view v-if="modelValue" class="payment-confirm-mask" @tap="onMaskTap">
    <view class="payment-confirm-panel" @tap.stop>
      <text class="payment-confirm-title">{{ title }}</text>
      <text v-if="content" class="payment-confirm-content">{{ content }}</text>
      <PaymentCountdownBadge v-if="countdownInfo?.show" :info="countdownInfo" size="small" />
      <view class="payment-confirm-actions">
        <button class="payment-confirm-btn payment-confirm-btn--ghost" @tap="emitCancel">
          {{ cancelText }}
        </button>
        <button
          class="payment-confirm-btn payment-confirm-btn--primary"
          :class="{ 'payment-confirm-btn--disabled': confirmDisabled }"
          :disabled="confirmDisabled"
          @tap="emitConfirm"
        >
          {{ confirmButtonText }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PaymentCountdownBadge from './PaymentCountdownBadge.vue'

type CountdownInfo = {
  show?: boolean
  expired?: boolean
  text?: string
  urgency?: 'normal' | 'warning' | 'critical' | 'expired'
  progress?: number
}

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title: string
    content?: string
    countdownInfo?: CountdownInfo | null
    confirmText?: string
    cancelText?: string
    closeOnMask?: boolean
  }>(),
  {
    content: '',
    countdownInfo: null,
    confirmText: '确认',
    cancelText: '取消',
    closeOnMask: true
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
  cancel: []
}>()

const confirmDisabled = computed(() => Boolean(props.countdownInfo?.expired))
const confirmButtonText = computed(() =>
  props.countdownInfo?.expired ? '已超时，请关闭' : props.confirmText
)

function close() {
  emit('update:modelValue', false)
}

function onMaskTap() {
  if (props.closeOnMask) {
    close()
    emit('cancel')
  }
}

function emitCancel() {
  close()
  emit('cancel')
}

function emitConfirm() {
  if (confirmDisabled.value) {
    uni.showToast({
      title: '订单已超时，系统正在自动取消，请稍后刷新',
      icon: 'none'
    })
    return
  }
  emit('confirm')
}
</script>

<style lang="scss" scoped>
.payment-confirm-mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48rpx;
}

.payment-confirm-panel {
  width: 100%;
  max-width: 620rpx;
  background: #fff;
  border-radius: 28rpx;
  padding: 40rpx 36rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  box-shadow: 0 24rpx 60rpx rgba(15, 23, 42, 0.18);
}

.payment-confirm-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1a1c1c;
  text-align: center;
}

.payment-confirm-content {
  font-size: 28rpx;
  color: #5f5e5e;
  line-height: 1.6;
  white-space: pre-wrap;
}

.payment-confirm-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 8rpx;
}

.payment-confirm-btn {
  flex: 1;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 9999px;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  margin: 0;
  padding: 0;
}

.payment-confirm-btn--ghost {
  background: #f3f4f6;
  color: #5f5e5e;
}

.payment-confirm-btn--primary {
  background: linear-gradient(135deg, #ff6600, #ea580c);
  color: #fff;
}

.payment-confirm-btn--disabled {
  opacity: 0.55;
}
</style>
