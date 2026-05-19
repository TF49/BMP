<template>
  <div class="payment-countdown-expired">
    <span class="payment-countdown-expired__marker" aria-hidden="true">
      <span class="payment-countdown-expired__marker-core" />
    </span>
    <AnimatedContent class="payment-countdown-expired__content" :distance="6" :duration="220">
      <span class="payment-countdown-expired__label">支付超时</span>
      <span class="payment-countdown-expired__hint">等待系统自动取消</span>
    </AnimatedContent>
  </div>
</template>

<script setup>
import AnimatedContent from '@/components/react-bits/AnimatedContent.vue'
</script>

<style scoped>
.payment-countdown-expired {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.payment-countdown-expired__marker {
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  border: 1px solid rgba(239, 68, 68, 0.24);
  background: linear-gradient(135deg, rgba(254, 242, 242, 0.96), rgba(255, 255, 255, 0.98));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.payment-countdown-expired__marker-core {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12);
}

.payment-countdown-expired__content {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.2;
}

.payment-countdown-expired__label {
  font-weight: 700;
  color: #dc2626;
}

.payment-countdown-expired__hint {
  font-size: 11px;
  color: #94a3b8;
}
</style>
