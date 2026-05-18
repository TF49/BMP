<template>
  <div class="payment-countdown-expired">
    <span class="payment-countdown-expired__pulse" />
    <span class="payment-countdown-expired__label">支付超时</span>
    <span class="payment-countdown-expired__hint">等待系统自动取消</span>
  </div>
</template>

<style scoped>
.payment-countdown-expired {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
}

.payment-countdown-expired__pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  animation: payment-countdown-pulse 1.2s ease-in-out infinite;
}

.payment-countdown-expired__label {
  font-weight: 700;
  color: #dc2626;
}

.payment-countdown-expired__hint {
  font-size: 11px;
  color: #94a3b8;
}

@keyframes payment-countdown-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.45;
    transform: scale(0.85);
  }
}
</style>
