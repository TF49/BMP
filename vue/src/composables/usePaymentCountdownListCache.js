import { watch } from 'vue'
import {
  buildPaymentCountdownOptions,
  getPaymentAutoCancelInfo
} from '@/composables/usePaymentAutoCancel'

/**
 * 列表/表格场景：按秒缓存倒计时结果，避免每行重复计算
 */
export function usePaymentCountdownListCache(paymentCountdownState) {
  const cache = new Map()
  let lastBucket = -1

  const resolveOptions = () => {
    const state = typeof paymentCountdownState === 'function'
      ? paymentCountdownState()
      : paymentCountdownState
    return buildPaymentCountdownOptions(state)
  }

  const getPaymentCountdownInfoCached = (order) => {
    const options = resolveOptions()
    const bucket = Math.floor((options.nowMs || Date.now()) / 1000)
    if (bucket !== lastBucket) {
      cache.clear()
      lastBucket = bucket
    }
    const orderKey = order?.id ?? order?.bookingNo ?? order?.serviceNo ?? order?.rentalNo ?? order?.registrationNo
    const key = `${orderKey}-${order?.createTime ?? ''}`
    if (!cache.has(key)) {
      cache.set(key, getPaymentAutoCancelInfo(order, options))
    }
    return cache.get(key)
  }

  const clearCache = () => {
    cache.clear()
    lastBucket = -1
  }

  return {
    getPaymentCountdownInfo: getPaymentCountdownInfoCached,
    clearCache
  }
}

export function bindPaymentCountdownCacheClear(session, clearCache) {
  if (!session?.countdownNowMs || typeof clearCache !== 'function') {
    return
  }
  watch(session.countdownNowMs, clearCache)
}
