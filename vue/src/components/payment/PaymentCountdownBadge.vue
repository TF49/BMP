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
    <AnimatedContent :duration="220" :distance="8">
      <div
        class="payment-countdown-badge__inner payment-countdown-badge__inner--soft"
        :style="{
          '--payment-countdown-bg': theme.background,
          '--payment-countdown-border': theme.border,
          '--payment-countdown-shadow': theme.shadow
        }"
      >
        <ExpiredState v-if="info.expired" />
        <ActiveState v-else :info="info" :theme="theme" :progress-offset="progressOffset" :compact="size === 'small'" />
      </div>
    </AnimatedContent>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AnimatedContent from '@/components/react-bits/AnimatedContent.vue'
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
  normal: {
    accent: '#f59e0b',
    text: '#b45309',
    track: 'rgba(245, 158, 11, 0.2)',
    border: 'rgba(245, 158, 11, 0.22)',
    shadow: 'rgba(245, 158, 11, 0.12)',
    background: 'linear-gradient(135deg, rgba(255,255,255,0.96), rgba(255,251,235,0.95))'
  },
  warning: {
    accent: '#f97316',
    text: '#c2410c',
    track: 'rgba(249, 115, 22, 0.22)',
    border: 'rgba(249, 115, 22, 0.28)',
    shadow: 'rgba(249, 115, 22, 0.14)',
    background: 'linear-gradient(135deg, rgba(255,255,255,0.96), rgba(255,237,213,0.94))'
  },
  critical: {
    accent: '#ef4444',
    text: '#b91c1c',
    track: 'rgba(239, 68, 68, 0.25)',
    border: 'rgba(239, 68, 68, 0.32)',
    shadow: 'rgba(239, 68, 68, 0.18)',
    background: 'linear-gradient(135deg, rgba(255,255,255,0.97), rgba(254,226,226,0.95))'
  },
  expired: {
    accent: '#94a3b8',
    text: '#64748b',
    track: 'rgba(148, 163, 184, 0.3)',
    border: 'rgba(148, 163, 184, 0.28)',
    shadow: 'rgba(148, 163, 184, 0.08)',
    background: 'linear-gradient(135deg, #f8fafc, #f1f5f9)'
  }
}

const theme = computed(() => URGENCY_THEME[props.info?.urgency] || URGENCY_THEME.normal)

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

.payment-countdown-badge__inner {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 8px;
  border-radius: 12px;
  background: var(--payment-countdown-bg);
  backdrop-filter: blur(8px);
}

.payment-countdown-badge--small .payment-countdown-badge__inner {
  padding: 4px 10px 4px 6px;
  gap: 6px;
  border-radius: 10px;
}

.payment-countdown-badge__inner--soft {
  border: 1px solid var(--payment-countdown-border);
  box-shadow: 0 8px 20px var(--payment-countdown-shadow);
}
</style>
