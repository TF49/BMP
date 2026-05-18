import { onBeforeUnmount, ref } from 'vue'
import { getPaymentAutoCancelConfig } from '@/api/payment'

function parseBackendDateTime(value) {
  if (!value || typeof value !== 'string') return null
  const normalized = value.trim().replace('T', ' ').replace(/\.\d+$/, '')
  const [datePart, timePart = '00:00:00'] = normalized.split(' ')
  const [year, month, day] = datePart.split('-').map(Number)
  const [hour = 0, minute = 0, second = 0] = timePart.split(':').map(Number)
  if (![year, month, day].every(Number.isFinite)) return null
  return new Date(year, month - 1, day, hour, minute, second)
}

function pad2(value) {
  return String(Math.max(0, value)).padStart(2, '0')
}

function normalizeTickMs(value, fallback = 1000) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed >= 1000 ? Math.floor(parsed) : fallback
}

function normalizePaymentAutoCancelConfig(payload) {
  if (payload && typeof payload === 'object' && payload.data && typeof payload.data === 'object') {
    return payload.data
  }
  return payload && typeof payload === 'object' ? payload : {}
}

export function formatPaymentCountdown(remainingMs) {
  const totalSeconds = Math.max(0, Math.floor(remainingMs / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${pad2(minutes)}:${pad2(seconds)}`
}

export function getPaymentAutoCancelInfo(order, options = {}) {
  const enabled = Boolean(options.enabled)
  const timeoutMinutes = Number(options.timeoutMinutes || 0)
  const nowMs = Number(options.nowMs || Date.now())
  const status = Number(order?.status ?? -1)
  const paymentStatus = order?.paymentStatus == null ? 0 : Number(order.paymentStatus)
  const createdAt = parseBackendDateTime(order?.createTime)

  if (!enabled || timeoutMinutes <= 0 || status !== 1 || paymentStatus !== 0 || !createdAt) {
    return {
      show: false,
      expired: false,
      remainingMs: 0,
      deadlineMs: null,
      text: ''
    }
  }

  const deadlineMs = createdAt.getTime() + timeoutMinutes * 60 * 1000
  const remainingMs = deadlineMs - nowMs
  const expired = remainingMs <= 0
  return {
    show: true,
    expired,
    remainingMs,
    deadlineMs,
    text: expired ? '支付超时，等待系统自动取消' : `剩余支付时间 ${formatPaymentCountdown(remainingMs)}`
  }
}

export function usePaymentAutoCancel(options = {}) {
  const autoCancelEnabled = ref(false)
  const autoCancelTimeoutMinutes = ref(15)
  const countdownNowMs = ref(Date.now())
  const configLoaded = ref(false)
  const countdownTickMs = normalizeTickMs(options.countdownTickMs, 1000)

  let serverOffsetMs = 0
  let timer = null
  let lastExpiredRefreshAt = 0
  let disposed = false

  const syncNow = () => {
    countdownNowMs.value = Date.now() + serverOffsetMs
  }

  const maybeRefreshExpiredOrders = async () => {
    if (typeof options.hasExpiredPending !== 'function' || typeof options.refreshOnExpire !== 'function') {
      return
    }
    if (!options.hasExpiredPending()) {
      return
    }
    const now = Date.now()
    const throttleMs = Number(options.refreshThrottleMs || 8000)
    if (now - lastExpiredRefreshAt < throttleMs) {
      return
    }
    lastExpiredRefreshAt = now
    try {
      await options.refreshOnExpire()
    } catch (error) {
      console.warn('刷新超时订单状态失败:', error)
    }
  }

  const startTimer = () => {
    if (disposed || timer) return
    syncNow()
    timer = window.setInterval(() => {
      syncNow()
      void maybeRefreshExpiredOrders()
    }, countdownTickMs)
  }

  const stopTimer = () => {
    if (timer) {
      window.clearInterval(timer)
      timer = null
    }
  }

  const loadPaymentAutoCancelConfig = async () => {
    try {
      const response = await getPaymentAutoCancelConfig()
      const config = normalizePaymentAutoCancelConfig(response)
      if (disposed) return
      autoCancelEnabled.value = Boolean(config?.enabled)
      const timeoutSeconds = Number(config?.timeoutSeconds || 0)
      autoCancelTimeoutMinutes.value = timeoutSeconds > 0
        ? timeoutSeconds / 60
        : Number(config?.timeoutMinutes || 15)
      const serverTime = parseBackendDateTime(config?.serverTime)
      serverOffsetMs = serverTime ? serverTime.getTime() - Date.now() : 0
      configLoaded.value = true
      syncNow()
    } catch (error) {
      if (disposed) return
      console.warn('加载支付超时配置失败，使用默认本地时间:', error)
      configLoaded.value = true
      syncNow()
    } finally {
      if (!disposed) {
        startTimer()
      }
    }
  }

  onBeforeUnmount(() => {
    disposed = true
    stopTimer()
  })

  return {
    autoCancelEnabled,
    autoCancelTimeoutMinutes,
    countdownNowMs,
    configLoaded,
    loadPaymentAutoCancelConfig,
    stopPaymentCountdown: stopTimer
  }
}
