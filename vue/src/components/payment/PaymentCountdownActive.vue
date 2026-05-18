<template>
  <div class="payment-countdown-active" :class="{ 'payment-countdown-active--compact': compact }">
    <svg class="payment-countdown-active__ring" viewBox="0 0 40 40" aria-hidden="true">
      <circle
        class="payment-countdown-active__ring-track"
        cx="20"
        cy="20"
        r="16"
        :style="{ stroke: theme.track }"
      />
      <circle
        class="payment-countdown-active__ring-progress"
        cx="20"
        cy="20"
        r="16"
        :style="{ stroke: theme.accent, strokeDashoffset: progressOffset }"
      />
    </svg>
    <div class="payment-countdown-active__content">
      <span class="payment-countdown-active__prefix">剩余</span>
      <ShinyText
        class="payment-countdown-active__timer"
        :text="`${info.minutes}:${info.seconds}`"
        :color="theme.text"
        :shine-color="theme.shine"
        :speed="info.urgency === 'critical' ? 1.2 : 2"
        :spread="110"
      />
      <span class="payment-countdown-active__suffix">支付</span>
    </div>
  </div>
</template>

<script setup>
import ShinyText from '@/components/react-bits/ShinyText.vue'

defineProps({
  info: { type: Object, required: true },
  theme: { type: Object, required: true },
  progressOffset: { type: Number, required: true },
  compact: { type: Boolean, default: false }
})
</script>

<style scoped>
.payment-countdown-active {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.payment-countdown-active__ring {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  transform: rotate(-90deg);
}

.payment-countdown-active__ring-track,
.payment-countdown-active__ring-progress {
  fill: none;
  stroke-width: 3;
}

.payment-countdown-active__ring-progress {
  stroke-linecap: round;
  stroke-dasharray: 100.53;
  transition: stroke-dashoffset 0.35s ease;
}

.payment-countdown-active__content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.2;
}

.payment-countdown-active__prefix,
.payment-countdown-active__suffix {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: #78716c;
}

.payment-countdown-active__timer {
  font-size: 18px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.06em;
  line-height: 1;
}

.payment-countdown-active--compact {
  gap: 6px;
}

.payment-countdown-active--compact .payment-countdown-active__ring {
  width: 28px;
  height: 28px;
}

.payment-countdown-active--compact .payment-countdown-active__prefix,
.payment-countdown-active--compact .payment-countdown-active__suffix {
  font-size: 9px;
}

.payment-countdown-active--compact .payment-countdown-active__timer {
  font-size: 14px;
}
</style>
