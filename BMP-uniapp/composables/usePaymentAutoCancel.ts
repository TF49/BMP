import { onUnmounted, ref, watch } from 'vue'
import { getPaymentAutoCancelConfig } from '@/api/internal/payment'

/** 与后端 scan-interval-ms 对齐：开发 1s，生产 5s（仅用于 UI 时钟 tick，不用于列表轮询） */
export const DEFAULT_PAYMENT_REFRESH_CHECK_MS = process.env.NODE_ENV === 'development' ? 1000 : 5000

/** 超时后轮询列表的最小间隔，避免压垮后端 */
const MIN_EXPIRE_POLL_MS = process.env.NODE_ENV === 'development' ? 8000 : 12000

/** 与 application.properties 默认 bmp.payment.auto-cancel.timeout-seconds=5 对齐（勿按 NODE_ENV 区分，小程序构建常为 production） */
const FALLBACK_TIMEOUT_SECONDS = 5
const CONFIG_CACHE_MS = 60_000

let cachedConfigPayload: Record<string, unknown> | null = null
let cachedConfigAt = 0
let sharedConfigPromise: Promise<Record<string, unknown>> | null = null
/** 配置接口失败时已提示过用户，避免每个页面 onMounted 重复弹 Toast */
let fallbackToastShown = false

export type PaymentCountdownOptions = {
  enabled?: boolean
  timeoutSeconds?: number
  nowMs?: number
}

type SharedPaymentCancelConfig = {
  enabled: boolean
  timeoutSeconds: number
  serverOffsetMs: number
  configLoadError: boolean
}

let sharedPaymentCancelConfig: SharedPaymentCancelConfig | null = null
let sharedPreloadPromise: Promise<void> | null = null

function readTimeoutSecondsFromPayload(config: Record<string, unknown>) {
  const timeoutSeconds = Number(config?.timeoutSeconds)
  return Number.isFinite(timeoutSeconds) && timeoutSeconds > 0
    ? Math.floor(timeoutSeconds)
    : FALLBACK_TIMEOUT_SECONDS
}

function commitSharedPaymentCancelConfig(payload: SharedPaymentCancelConfig) {
  sharedPaymentCancelConfig = payload
}

function syncInstanceFromShared(state: PaymentCountdownState): boolean {
  if (!sharedPaymentCancelConfig) return false
  state.autoCancelEnabled.value = sharedPaymentCancelConfig.enabled
  state.autoCancelTimeoutSeconds.value = sharedPaymentCancelConfig.timeoutSeconds
  state.configLoadError.value = sharedPaymentCancelConfig.configLoadError
  state.configLoaded.value = true
  return true
}

function applySharedConfigPayload(config: Record<string, unknown>) {
  const scanIntervalMs = Number(config?.scanIntervalMs || 0)
  const serverTimeValue = typeof config?.serverTime === 'string' ? config.serverTime : null
  const serverTime = parseBackendDateTime(serverTimeValue)
  commitSharedPaymentCancelConfig({
    enabled: Boolean(config?.enabled),
    timeoutSeconds: readTimeoutSecondsFromPayload(config),
    serverOffsetMs: serverTime ? serverTime.getTime() - Date.now() : 0,
    configLoadError: false
  })
  return scanIntervalMs
}

function applySharedFallbackConfig() {
  commitSharedPaymentCancelConfig({
    enabled: true,
    timeoutSeconds: FALLBACK_TIMEOUT_SECONDS,
    serverOffsetMs: 0,
    configLoadError: true
  })
  if (!fallbackToastShown) {
    fallbackToastShown = true
    uni.showToast({
      title: `支付超时配置加载失败，已按 ${FALLBACK_TIMEOUT_SECONDS} 秒本地规则限制支付`,
      icon: 'none',
      duration: 3000
    })
  }
}

function parseBackendDateTime(value?: string | null) {
  if (!value) return null
  const normalized = String(value).trim().replace('T', ' ').replace(/\.\d+$/, '')
  const [datePart, timePart = '00:00:00'] = normalized.split(' ')
  const [year, month, day] = datePart.split('-').map(Number)
  const [hour = 0, minute = 0, second = 0] = timePart.split(':').map(Number)
  if (![year, month, day].every(Number.isFinite)) return null
  return new Date(year, month - 1, day, hour, minute, second)
}

function pad2(value: number) {
  return String(Math.max(0, value)).padStart(2, '0')
}

function normalizeTickMs(value: number | undefined, fallback = 1000) {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed >= 1000 ? Math.floor(parsed) : fallback
}

function normalizePaymentAutoCancelConfig(payload: unknown) {
  if (payload && typeof payload === 'object' && 'data' in payload) {
    const data = (payload as { data?: unknown }).data
    if (data && typeof data === 'object') {
      return data as Record<string, unknown>
    }
  }
  return payload && typeof payload === 'object' ? (payload as Record<string, unknown>) : {}
}

async function fetchPaymentAutoCancelConfigCached() {
  const now = Date.now()
  if (cachedConfigPayload && now - cachedConfigAt < CONFIG_CACHE_MS) {
    return cachedConfigPayload
  }
  if (sharedConfigPromise) {
    return sharedConfigPromise
  }

  sharedConfigPromise = getPaymentAutoCancelConfig({ suppressErrorToast: true })
    .then((response) => {
      const config = normalizePaymentAutoCancelConfig(response)
      cachedConfigPayload = config
      cachedConfigAt = Date.now()
      return config
    })
    .catch((error) => {
      cachedConfigPayload = null
      cachedConfigAt = 0
      throw error
    })
    .finally(() => {
      sharedConfigPromise = null
    })

  return sharedConfigPromise
}

export function formatPaymentCountdown(remainingMs: number) {
  const totalSeconds = Math.max(0, Math.floor(remainingMs / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${pad2(minutes)}:${pad2(seconds)}`
}

export const EMPTY_PAYMENT_COUNTDOWN_INFO = {
  show: false,
  expired: false,
  remainingMs: 0,
  deadlineMs: null as number | null,
  totalMs: 0,
  progress: 0,
  urgency: 'normal' as const,
  minutes: '00',
  seconds: '00',
  text: ''
}

type CountdownOrderLike = {
  status?: number | null
  paymentStatus?: number | null
  createTime?: string | null
  servicePrice?: number | null
  totalPrice?: number | null
}

/** 穿线服务金额为 0 时不参与支付倒计时 */
export function shouldShowStringingPaymentCountdown(service: CountdownOrderLike | null | undefined) {
  if (!service) return false
  const price = Number(service.servicePrice ?? service.totalPrice ?? 0)
  return price > 0
}

export function getPaymentAutoCancelInfo(
  order: CountdownOrderLike | null | undefined,
  options: { enabled?: boolean; timeoutSeconds?: number; nowMs?: number } = {}
) {
  const enabled = Boolean(options.enabled)
  const timeoutSeconds = Number(options.timeoutSeconds || 0)
  const nowMs = Number(options.nowMs || Date.now())
  const status = Number(order?.status ?? -1)
  const paymentStatus = order?.paymentStatus == null ? 0 : Number(order.paymentStatus)
  const createdAt = parseBackendDateTime(order?.createTime)

  if (!enabled || timeoutSeconds <= 0 || status !== 1 || paymentStatus !== 0 || !createdAt) {
    return { ...EMPTY_PAYMENT_COUNTDOWN_INFO }
  }

  const totalMs = timeoutSeconds * 1000
  const deadlineMs = createdAt.getTime() + totalMs
  const remainingMs = deadlineMs - nowMs
  const expired = remainingMs <= 0
  const safeRemainingMs = Math.max(0, remainingMs)
  const totalSeconds = Math.floor(safeRemainingMs / 1000)
  const minutes = pad2(Math.floor(totalSeconds / 60))
  const seconds = pad2(totalSeconds % 60)
  const progress = expired ? 0 : Math.min(1, safeRemainingMs / totalMs)

  let urgency: 'normal' | 'warning' | 'critical' | 'expired' = 'normal'
  if (expired) {
    urgency = 'expired'
  } else if (progress <= 0.2) {
    urgency = 'critical'
  } else if (progress <= 0.5) {
    urgency = 'warning'
  }

  return {
    show: true,
    expired,
    remainingMs: safeRemainingMs,
    deadlineMs,
    totalMs,
    progress,
    urgency,
    minutes,
    seconds,
    text: expired ? '支付超时，等待系统自动取消' : `剩余支付时间 ${minutes}:${seconds}`
  }
}

export function resolvePaymentCountdownInfo(
  order: CountdownOrderLike | null | undefined,
  options: PaymentCountdownOptions = {},
  businessType?: 'STRINGING'
) {
  if (businessType === 'STRINGING' && !shouldShowStringingPaymentCountdown(order)) {
    return { ...EMPTY_PAYMENT_COUNTDOWN_INFO }
  }
  return getPaymentAutoCancelInfo(order, options)
}

export type PaymentCountdownState = {
  autoCancelEnabled: ReturnType<typeof ref<boolean>>
  autoCancelTimeoutSeconds: ReturnType<typeof ref<number>>
  countdownNowMs: ReturnType<typeof ref<number>>
  configLoaded: ReturnType<typeof ref<boolean>>
  configLoadError: ReturnType<typeof ref<boolean>>
}

export function buildPaymentCountdownOptions(state: PaymentCountdownState) {
  if (!state.configLoaded.value) {
    return { enabled: false, timeoutSeconds: 0, nowMs: state.countdownNowMs.value }
  }
  return {
    enabled: state.autoCancelEnabled.value,
    timeoutSeconds: state.autoCancelTimeoutSeconds.value,
    nowMs: state.countdownNowMs.value
  }
}

export function buildPaymentAutoCancelRefsBundle(state: PaymentCountdownState) {
  return {
    autoCancelEnabled: state.autoCancelEnabled,
    autoCancelTimeoutSeconds: state.autoCancelTimeoutSeconds,
    countdownNowMs: state.countdownNowMs,
    configLoadError: state.configLoadError
  }
}

export function usePaymentAutoCancel(options: {
  hasExpiredPending?: () => boolean
  refreshOnExpire?: () => void | Promise<void>
  refreshThrottleMs?: number
  countdownTickMs?: number
  /** 是否在持续超时期间轮询刷新（默认关闭，仅边沿 + 有限次补刷） */
  pollWhileExpired?: boolean
  pollIntervalMs?: number
} = {}) {
  const autoCancelEnabled = ref(false)
  const autoCancelTimeoutSeconds = ref(FALLBACK_TIMEOUT_SECONDS)
  const countdownNowMs = ref(Date.now())
  const configLoaded = ref(false)
  const configLoadError = ref(false)
  const countdownTickMs = normalizeTickMs(options.countdownTickMs, 1000)
  const pollWhileExpired = options.pollWhileExpired === true
  let resolvedPollIntervalMs = normalizeTickMs(
    options.pollIntervalMs,
    Math.max(MIN_EXPIRE_POLL_MS, DEFAULT_PAYMENT_REFRESH_CHECK_MS)
  )
  let refreshThrottleMs = Math.max(
    Number(options.refreshThrottleMs ?? resolvedPollIntervalMs),
    MIN_EXPIRE_POLL_MS
  )

  let timer: ReturnType<typeof setInterval> | null = null
  let lastExpiredRefreshAt = 0
  let lastPollRefreshAt = 0
  let wasExpiredPending = false
  let refreshInFlight = false
  let disposed = false
  const followUpTimers: ReturnType<typeof setTimeout>[] = []

  const state: PaymentCountdownState = {
    autoCancelEnabled,
    autoCancelTimeoutSeconds,
    countdownNowMs,
    configLoaded,
    configLoadError
  }

  if (syncInstanceFromShared(state)) {
    countdownNowMs.value = Date.now() + (sharedPaymentCancelConfig?.serverOffsetMs ?? 0)
  }

  let serverOffsetMs = sharedPaymentCancelConfig?.serverOffsetMs ?? 0

  const clearFollowUpTimers = () => {
    followUpTimers.forEach((id) => clearTimeout(id))
    followUpTimers.length = 0
  }

  const syncNow = () => {
    countdownNowMs.value = Date.now() + serverOffsetMs
  }

  const runRefreshOnExpire = async (force = false) => {
    if (!options.hasExpiredPending || !options.refreshOnExpire) return
    if (!options.hasExpiredPending()) return
    const now = Date.now()
    if (!force && now - lastExpiredRefreshAt < refreshThrottleMs) return
    if (refreshInFlight) return

    lastExpiredRefreshAt = now
    refreshInFlight = true
    try {
      await options.refreshOnExpire()
    } catch (error) {
      console.warn('刷新超时订单状态失败:', error)
    } finally {
      refreshInFlight = false
    }
  }

  const triggerRefreshOnExpire = () => runRefreshOnExpire(true)

  const scheduleFollowUpRefresh = () => {
    clearFollowUpTimers()
    const delays = pollWhileExpired ? [resolvedPollIntervalMs] : [3000, 8000, 15000]
    delays.forEach((delay) => {
      const timerId = setTimeout(() => {
        if (disposed) return
        if (!options.hasExpiredPending?.()) {
          clearFollowUpTimers()
          return
        }
        void runRefreshOnExpire(true)
      }, delay)
      followUpTimers.push(timerId)
    })
  }

  const checkExpiredEdgeAndRefresh = async () => {
    if (!options.hasExpiredPending) {
      wasExpiredPending = false
      return
    }
    const isExpiredPending = options.hasExpiredPending()
    if (!wasExpiredPending && isExpiredPending) {
      await runRefreshOnExpire(true)
      scheduleFollowUpRefresh()
    } else if (!isExpiredPending) {
      clearFollowUpTimers()
    }
    wasExpiredPending = isExpiredPending
  }

  const maybePollWhileExpired = async () => {
    if (!pollWhileExpired || !options.hasExpiredPending?.()) return
    const now = Date.now()
    if (now - lastPollRefreshAt < resolvedPollIntervalMs) return
    lastPollRefreshAt = now
    await runRefreshOnExpire()
  }

  const startTimer = () => {
    if (disposed || timer) return
    syncNow()
    wasExpiredPending = false
    lastPollRefreshAt = 0
    timer = setInterval(() => {
      syncNow()
      void checkExpiredEdgeAndRefresh()
      void maybePollWhileExpired()
    }, countdownTickMs)
  }

  const stopTimer = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
    clearFollowUpTimers()
  }

  const applyFallbackConfig = () => {
    applySharedFallbackConfig()
    syncInstanceFromShared(state)
    serverOffsetMs = sharedPaymentCancelConfig?.serverOffsetMs ?? 0
    syncNow()
  }

  const applyConfig = (config: Record<string, unknown>) => {
    const scanIntervalMs = applySharedConfigPayload(config)
    if (scanIntervalMs > 0) {
      resolvedPollIntervalMs = Math.max(normalizeTickMs(scanIntervalMs), MIN_EXPIRE_POLL_MS)
      refreshThrottleMs = Math.max(
        Number(options.refreshThrottleMs ?? resolvedPollIntervalMs),
        MIN_EXPIRE_POLL_MS
      )
    }
    syncInstanceFromShared(state)
    serverOffsetMs = sharedPaymentCancelConfig?.serverOffsetMs ?? 0
    syncNow()
  }

  const loadPaymentAutoCancelConfig = async () => {
    if (configLoaded.value) {
      if (!disposed && !timer) {
        startTimer()
      }
      return
    }

    if (sharedPaymentCancelConfig) {
      syncInstanceFromShared(state)
      serverOffsetMs = sharedPaymentCancelConfig.serverOffsetMs
      syncNow()
      if (!disposed && !timer) {
        startTimer()
      }
      return
    }

    try {
      const config = await fetchPaymentAutoCancelConfigCached()
      if (disposed) return
      applyConfig(config)
    } catch (error) {
      if (disposed) return
      console.warn('加载支付超时配置失败，使用本地兜底规则:', error)
      applyFallbackConfig()
    } finally {
      if (!disposed) {
        startTimer()
      }
    }
  }

  onUnmounted(() => {
    disposed = true
    stopTimer()
  })

  return {
    autoCancelEnabled,
    autoCancelTimeoutSeconds,
    countdownNowMs,
    configLoaded,
    configLoadError,
    loadPaymentAutoCancelConfig,
    stopPaymentCountdown: stopTimer,
    buildCountdownOptions: () => buildPaymentCountdownOptions(state),
    paymentAutoCancelRefs: {
      ...buildPaymentAutoCancelRefsBundle(state),
      triggerRefreshOnExpire
    },
    triggerRefreshOnExpire
  }
}

/**
 * 支付确认弹窗：倒计时归零时自动关闭并提示
 */
export function usePayDialogExpireGuard(
  visible: { value: boolean },
  payCountdownInfo: { value: { expired?: boolean } | null | undefined },
  onExpire?: () => void | Promise<void>
) {
  let stopWatch: (() => void) | null = null

  const dispose = () => {
    stopWatch?.()
    stopWatch = null
  }

  stopWatch = watch(
    () => payCountdownInfo.value?.expired,
    (expired) => {
      if (!expired || !visible.value) return
      visible.value = false
      uni.showToast({
        title: '订单已超时，系统正在自动取消，请稍后刷新',
        icon: 'none',
        duration: 2500
      })
      if (typeof onExpire === 'function') {
        void onExpire()
      }
    }
  )

  onUnmounted(dispose)

  return { dispose }
}

/** 应用启动时预加载，避免各页面倒计时先用错规则 */
export function preloadPaymentAutoCancelConfig() {
  if (sharedPaymentCancelConfig) {
    return Promise.resolve()
  }
  if (sharedPreloadPromise) {
    return sharedPreloadPromise
  }

  sharedPreloadPromise = fetchPaymentAutoCancelConfigCached()
    .then((config) => {
      applySharedConfigPayload(config)
    })
    .catch((error) => {
      console.warn('预加载支付超时配置失败，使用本地兜底规则:', error)
      applySharedFallbackConfig()
    })
    .finally(() => {
      sharedPreloadPromise = null
    })

  return sharedPreloadPromise
}
