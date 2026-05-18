<template>
  <view
    v-if="info?.show"
    class="payment-countdown-badge"
    :class="[`payment-countdown-badge--${info.urgency}`, size === 'small' ? 'payment-countdown-badge--small' : '']"
  >
    <view class="payment-countdown-badge__inner">
      <view v-if="!info.expired" class="payment-countdown-badge__ring">
        <view
          class="payment-countdown-badge__ring-fill"
          :style="{ transform: `rotate(${ringRotation}deg)` }"
        />
      </view>
      <uni-icons
        v-if="info.expired"
        type="info-filled"
        size="14"
        color="#64748b"
      />
      <uni-icons
        v-else
        type="notification-filled"
        size="14"
        :color="accentColor"
      />
      <text class="payment-countdown-badge__text" :style="{ color: textColor }">{{ info.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type CountdownInfo = {
  show?: boolean
  expired?: boolean
  progress?: number
  urgency?: 'normal' | 'warning' | 'critical' | 'expired'
  text?: string
}

const props = withDefaults(
  defineProps<{
    info: CountdownInfo
    size?: 'small' | 'default'
  }>(),
  {
    size: 'default'
  }
)

const URGENCY_THEME: Record<string, { accent: string; text: string }> = {
  normal: { accent: '#f59e0b', text: '#b45309' },
  warning: { accent: '#f97316', text: '#c2410c' },
  critical: { accent: '#ef4444', text: '#b91c1c' },
  expired: { accent: '#94a3b8', text: '#64748b' }
}

const theme = computed(() => URGENCY_THEME[props.info?.urgency || 'normal'] || URGENCY_THEME.normal)
const accentColor = computed(() => theme.value.accent)
const textColor = computed(() => theme.value.text)
const ringRotation = computed(() => {
  const progress = Math.min(1, Math.max(0, props.info?.progress ?? 0))
  return -90 + progress * 360
})
</script>

<style lang="scss" scoped>
.payment-countdown-badge {
  display: inline-flex;
  max-width: 100%;
}

.payment-countdown-badge--small .payment-countdown-badge__inner {
  padding: 6rpx 16rpx 6rpx 12rpx;
  gap: 8rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
}

.payment-countdown-badge__inner {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  padding: 10rpx 20rpx 10rpx 14rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(255, 251, 235, 0.95));
  border: 1px solid rgba(245, 158, 11, 0.28);
  box-shadow: 0 6rpx 20rpx rgba(245, 158, 11, 0.12);
}

.payment-countdown-badge--warning .payment-countdown-badge__inner {
  border-color: rgba(249, 115, 22, 0.32);
  box-shadow: 0 6rpx 20rpx rgba(249, 115, 22, 0.14);
}

.payment-countdown-badge--critical .payment-countdown-badge__inner {
  border-color: rgba(239, 68, 68, 0.35);
  background: linear-gradient(135deg, #fff, #fef2f2);
  box-shadow: 0 6rpx 22rpx rgba(239, 68, 68, 0.16);
}

.payment-countdown-badge--expired .payment-countdown-badge__inner {
  border-color: rgba(148, 163, 184, 0.35);
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  box-shadow: none;
}

.payment-countdown-badge__ring {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(245, 158, 11, 0.25);
  position: relative;
  flex-shrink: 0;
}

.payment-countdown-badge__ring-fill {
  position: absolute;
  inset: -3rpx;
  border-radius: 50%;
  border: 3rpx solid transparent;
  border-top-color: #f59e0b;
  border-right-color: #f59e0b;
}

.payment-countdown-badge--critical .payment-countdown-badge__ring-fill {
  border-top-color: #ef4444;
  border-right-color: #ef4444;
}

.payment-countdown-badge__text {
  font-size: 24rpx;
  font-weight: 600;
  line-height: 1.3;
}
</style>
