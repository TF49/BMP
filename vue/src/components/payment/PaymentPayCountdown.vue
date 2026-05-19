<template>
  <div v-if="wrapperVisible" class="payment-pay-countdown">
    <p v-if="showConfigLoadingHint" class="payment-pay-countdown__hint payment-pay-countdown__hint--loading">
      正在加载支付超时规则…
    </p>
    <p v-else-if="showConfigFallbackHint" class="payment-pay-countdown__hint payment-pay-countdown__hint--fallback">
      支付超时规则加载失败，当前以服务端校验结果为准
    </p>
    <p v-else-if="showFeatureDisabledHint" class="payment-pay-countdown__hint payment-pay-countdown__hint--disabled">
      系统未启用支付超时自动取消，请尽快完成支付
    </p>
    <PaymentCountdownBadge v-if="info.show" :info="info" size="small" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import PaymentCountdownBadge from '@/components/payment/PaymentCountdownBadge.vue'
import {
  buildPaymentCountdownOptions,
  getPaymentAutoCancelInfo,
  isPendingPaymentOrder,
  usePaymentAutoCancelContext
} from '@/composables/usePaymentAutoCancel'

const props = defineProps({
  order: {
    type: Object,
    default: null
  },
  enabled: {
    type: Boolean,
    default: undefined
  },
  timeoutMinutes: {
    type: Number,
    default: undefined
  },
  nowMs: {
    type: Number,
    default: undefined
  },
  countdownState: {
    type: Object,
    default: null
  }
})

const injected = usePaymentAutoCancelContext()
const activeState = computed(() => props.countdownState || injected)

const showConfigFallbackHint = computed(() => Boolean(activeState.value?.configLoadError?.value))
const showConfigLoadingHint = computed(() =>
  Boolean(props.order)
  && isPendingPaymentOrder(props.order)
  && activeState.value?.configLoaded?.value === false
)
const showFeatureDisabledHint = computed(() =>
  Boolean(props.order)
  && isPendingPaymentOrder(props.order)
  && activeState.value?.configLoaded?.value === true
  && !showConfigFallbackHint.value
  && activeState.value?.autoCancelEnabled?.value === false
)

const countdownOptions = computed(() => {
  if (props.enabled !== undefined && props.timeoutMinutes !== undefined && props.nowMs !== undefined) {
    return {
      enabled: props.enabled,
      timeoutMinutes: props.timeoutMinutes,
      nowMs: props.nowMs,
      configLoaded: true
    }
  }
  const state = activeState.value
  if (state?.autoCancelEnabled && state?.autoCancelTimeoutMinutes && state?.countdownNowMs) {
    return buildPaymentCountdownOptions(state)
  }
  return {
    enabled: false,
    timeoutMinutes: 0,
    nowMs: Date.now(),
    configLoaded: state?.configLoaded?.value !== false
  }
})

const info = computed(() => {
  if (!props.order) {
    return { show: false, expired: false }
  }
  return getPaymentAutoCancelInfo(props.order, countdownOptions.value)
})

const wrapperVisible = computed(() =>
  info.value.show
  || showConfigFallbackHint.value
  || showConfigLoadingHint.value
  || showFeatureDisabledHint.value
)

defineExpose({ info })
</script>

<style scoped>
.payment-pay-countdown {
  margin-bottom: 12px;
}

.payment-pay-countdown__hint {
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.4;
}

.payment-pay-countdown__hint--loading {
  color: #64748b;
}

.payment-pay-countdown__hint--disabled {
  color: #475569;
}

.payment-pay-countdown__hint--fallback {
  color: #b45309;
}

.brand-confirm-content__countdown {
  margin-top: 12px;
}
</style>
