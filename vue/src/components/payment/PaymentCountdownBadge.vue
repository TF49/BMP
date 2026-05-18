<template>
  <div
    v-if="info?.show"
    class="payment-countdown-badge"
    :class="[
      `payment-countdown-badge--${info.urgency}`,
      `payment-countdown-badge--${size}`
    ]"
    role="timer"
    :aria-label="info.text"
  >
    <ElectricBorder
      v-if="useElectricBorder"
      :color="theme.accent"
      :border-radius="borderRadius"
      :speed="info.urgency === 'critical' ? 1.4 : 1"
      :chaos="info.urgency === 'critical' ? 0.16 : 0.1"
      class="payment-countdown-badge__electric"
    >
      <div class="payment-countdown-badge__inner">
        <ExpiredState v-if="info.expired" />
        <ActiveState v-else :info="info" :theme="theme" :progress-offset="progressOffset" :compact="size === 'small'" />
      </div>
    </ElectricBorder>
    <div v-else class="payment-countdown-badge__inner payment-countdown-badge__inner--soft">
      <ExpiredState v-if="info.expired" />
      <ActiveState v-else :info="info" :theme="theme" :progress-offset="progressOffset" :compact="size === 'small'" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import ElectricBorder from '@/components/react-bits/ElectricBorder.vue'
import ActiveState from './PaymentCountdownActive.vue'
import ExpiredState from './PaymentCountdownExpired.vue'

const props = defineProps({
  info: { type: Object, required: true },
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['small', 'default'].includes(v)
  }
})

const URGENCY_THEME = {
  normal: { accent: '#f59e0b', text: '#b45309', shine: '#fff7ed', track: 'rgba(245, 158, 11, 0.2)' },
  warning: { accent: '#f97316', text: '#c2410c', shine: '#ffedd5', track: 'rgba(249, 115, 22, 0.22)' },
  critical: { accent: '#ef4444', text: '#b91c1c', shine: '#fee2e2', track: 'rgba(239, 68, 68, 0.25)' },
  expired: { accent: '#94a3b8', text: '#64748b', shine: '#f1f5f9', track: 'rgba(148, 163, 184, 0.3)' }
}

const theme = computed(() => URGENCY_THEME[props.info?.urgency] || URGENCY_THEME.normal)
const borderRadius = computed(() => (props.size === 'small' ? 10 : 14))
const useElectricBorder = computed(() => {
  if (props.info?.expired) return false
  // 仅危急态启用 ElectricBorder，避免列表/卡片中大量 Canvas 动画影响性能
  return props.info?.urgency === 'critical'
})

const progressOffset = computed(() => {
  const circumference = 2 * Math.PI * 16
  const progress = Math.min(1, Math.max(0, props.info?.progress ?? 0))
  return circumference * (1 - progress)
})
</script>

<style scoped>
.payment-countdown-badge {
  display: inline-flex;
  max-width: 100%;
}

.payment-countdown-badge--small {
  font-size: 12px;
}

.payment-countdown-badge--default {
  font-size: 13px;
}

.payment-countdown-badge__electric {
  display: inline-flex;
}

.payment-countdown-badge__inner {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 8px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 251, 235, 0.92));
  backdrop-filter: blur(8px);
}

.payment-countdown-badge--small .payment-countdown-badge__inner {
  padding: 4px 10px 4px 6px;
  gap: 6px;
  border-radius: 10px;
}

.payment-countdown-badge__inner--soft {
  border: 1px solid rgba(245, 158, 11, 0.25);
  box-shadow: 0 4px 14px rgba(245, 158, 11, 0.12);
}

.payment-countdown-badge--warning .payment-countdown-badge__inner--soft {
  border-color: rgba(249, 115, 22, 0.3);
  box-shadow: 0 4px 14px rgba(249, 115, 22, 0.14);
}

.payment-countdown-badge--critical .payment-countdown-badge__inner--soft {
  border-color: rgba(239, 68, 68, 0.35);
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.18);
}

.payment-countdown-badge--expired .payment-countdown-badge__inner--soft {
  border-color: rgba(148, 163, 184, 0.35);
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  box-shadow: none;
}
</style>
