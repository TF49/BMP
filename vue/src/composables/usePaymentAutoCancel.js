import { inject, onBeforeUnmount, onMounted, provide, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getPaymentAutoCancelConfig } from '@/api/payment'

export const PAYMENT_AUTO_CANCEL_KEY = Symbol('paymentAutoCancel')

/** 与后端 scan-interval-ms 对齐：开发 1s，生产 5s */
export const DEFAULT_PAYMENT_REFRESH_CHECK_MS = import.meta.env.DEV ? 1000 : 5000

const FALLBACK_TIMEOUT_SECONDS = import.meta.env.DEV ? 5 : 900
const EXPIRE_FOLLOW_UP_REFRESH_DELAYS = [3000, 8000, 15000]

function parseBackendDateTime(value) {
  if (!value || typeof value !== 'string') return null
  const normalized = value.trim().replace('T', ' ').replace(/\.\d+$/, '')
  const [datePart, timePart = '00:00:00'] = normalized.split(' ')
  const [year, month, day] = datePart.split('-').map(Number)
  const [hour = 0, minute = 0, second = 0] = timePart.split(':').map(Number)
  if (![year, month, day].every(Number.isFinite)) return null
  return new Date(year, month - 1, day, hour, minute, second)
}

function parseBackendServerTime(config) {
  const serverTimestamp = Number(config?.serverTimestamp)
  if (Number.isFinite(serverTimestamp) && serverTimestamp > 0) {
    return new Date(serverTimestamp)
  }
  return parseBackendDateTime(config?.serverTime)
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

export const EMPTY_PAYMENT_COUNTDOWN_INFO = {
  show: false,
  expired: false,
  remainingMs: 0,
  deadlineMs: null,
  totalMs: 0,
  progress: 0,
  urgency: 'normal',
  minutes: '00',
  seconds: '00',
  text: ''
}

/** 穿线服务金额为 0 时不参与支付倒计时 */
export function shouldShowStringingPaymentCountdown(service) {
  if (!service) return false
  return Number(service.servicePrice ?? 0) > 0
}

/** 下单成功等场景：根据超时秒数生成提示文案 */
export function formatPaymentDeadlineHint(timeoutSeconds) {
  const seconds = Math.max(1, Math.floor(Number(timeoutSeconds) || 0))
  if (seconds < 60) {
    return `请在 ${seconds} 秒内完成支付，超时订单将自动取消`
  }
  const minutes = Math.max(1, Math.ceil(seconds / 60))
  return `请在 ${minutes} 分钟内完成支付，超时订单将自动取消`
}

/** 下单/支付成功提示：开启自动取消时附带支付时限说明 */
export function createOrderSuccessMessage(autoCancelEnabled, formatDeadlineHint) {
  return (base) => {
    if (!autoCancelEnabled?.value) return base
    const hint = typeof formatDeadlineHint === 'function' ? formatDeadlineHint() : ''
    return hint ? `${base} ${hint}` : base
  }
}

export function isPendingPaymentOrder(order) {
  if (!order) return false
  const status = Number(order.status ?? -1)
  const paymentStatus = order.paymentStatus == null ? 0 : Number(order.paymentStatus)
  return status === 1 && paymentStatus === 0
}

export function getPaymentAutoCancelInfo(order, options = {}) {
  const configLoaded = options.configLoaded !== false
  const enabled = Boolean(options.enabled)
  const timeoutMinutes = Number(options.timeoutMinutes || 0)
  const nowMs = Number(options.nowMs || Date.now())
  const status = Number(order?.status ?? -1)
  const paymentStatus = order?.paymentStatus == null ? 0 : Number(order.paymentStatus)
  const createdAt = parseBackendDateTime(order?.createTime)

  if (!configLoaded) {
    return { ...EMPTY_PAYMENT_COUNTDOWN_INFO }
  }

  if (!enabled || timeoutMinutes <= 0 || status !== 1 || paymentStatus !== 0 || !createdAt) {
    return {
      show: false,
      expired: false,
      remainingMs: 0,
      deadlineMs: null,
      totalMs: 0,
      progress: 0,
      urgency: 'normal',
      minutes: '00',
      seconds: '00',
      text: ''
    }
  }

  const totalMs = timeoutMinutes * 60 * 1000
  const deadlineMs = createdAt.getTime() + totalMs
  const remainingMs = deadlineMs - nowMs
  const expired = remainingMs <= 0
  const safeRemainingMs = Math.max(0, remainingMs)
  const totalSeconds = Math.floor(safeRemainingMs / 1000)
  const minutes = pad2(Math.floor(totalSeconds / 60))
  const seconds = pad2(totalSeconds % 60)
  const progress = expired ? 0 : Math.min(1, safeRemainingMs / totalMs)

  let urgency = 'normal'
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

export function buildPaymentCountdownOptions(state) {
  return {
    enabled: state.autoCancelEnabled.value,
    timeoutMinutes: state.autoCancelTimeoutMinutes.value,
    nowMs: state.countdownNowMs.value,
    configLoaded: state.configLoaded?.value !== false
  }
}

/** 供 openActionConfirm 传入，避免 MessageBox teleport 导致 inject 失效 */
export function buildPaymentAutoCancelRefsBundle(state) {
  return {
    autoCancelEnabled: state.autoCancelEnabled,
    autoCancelTimeoutMinutes: state.autoCancelTimeoutMinutes,
    autoCancelTimeoutSeconds: state.autoCancelTimeoutSeconds,
    countdownNowMs: state.countdownNowMs,
    configLoaded: state.configLoaded,
    configLoadError: state.configLoadError
  }
}

export function usePaymentAutoCancelContext() {
  return inject(PAYMENT_AUTO_CANCEL_KEY, null)
}

/**
 * 支付弹窗打开时，倒计时归零自动关闭并提示
 */
export function usePayDialogExpireGuard(payDialogVisible, payCountdownInfo, onExpire) {
  watch(
    () => payCountdownInfo.value?.expired,
    (expired) => {
      if (!expired || !payDialogVisible.value) return
      payDialogVisible.value = false
      ElMessage.warning('订单已超时，系统正在自动取消，请稍后刷新列表')
      if (typeof onExpire === 'function') {
        void onExpire()
      }
    }
  )
}

function createPaymentAutoCancelEngine(options = {}) {
  const autoCancelEnabled = ref(false)
  const autoCancelTimeoutMinutes = ref(15)
  const autoCancelTimeoutSeconds = ref(300)
  const countdownNowMs = ref(Date.now())
  const configLoaded = ref(false)
  const configLoadError = ref(false)
  const countdownTickMs = normalizeTickMs(options.countdownTickMs, 1000)
  let refreshCheckIntervalMs = normalizeTickMs(
    options.refreshCheckIntervalMs,
    DEFAULT_PAYMENT_REFRESH_CHECK_MS
  )
  let refreshThrottleMs = Number(options.refreshThrottleMs ?? refreshCheckIntervalMs)

  let serverOffsetMs = 0
  let timer = null
  let lastExpiredRefreshAt = 0
  let lastRefreshCheckAt = 0
  let wasExpiredPending = false
  let disposed = false
  const followUpTimers = []

  const state = {
    autoCancelEnabled,
    autoCancelTimeoutMinutes,
    autoCancelTimeoutSeconds,
    countdownNowMs,
    configLoaded,
    configLoadError
  }

  const syncNow = () => {
    countdownNowMs.value = Date.now() + serverOffsetMs
  }

  const runRefreshOnExpire = async (force = false) => {
    if (typeof options.hasExpiredPending !== 'function' || typeof options.refreshOnExpire !== 'function') {
      return
    }
    if (!options.hasExpiredPending()) {
      return
    }
    const now = Date.now()
    if (!force && now - lastExpiredRefreshAt < refreshThrottleMs) {
      return
    }
    lastExpiredRefreshAt = now
    try {
      await options.refreshOnExpire()
    } catch (error) {
      console.warn('刷新超时订单状态失败:', error)
    }
  }

  const triggerRefreshOnExpire = () => runRefreshOnExpire(true)

  const maybeRefreshExpiredOrders = async (force = false) => {
    await runRefreshOnExpire(force)
  }

  const clearFollowUpTimers = () => {
    followUpTimers.forEach((timerId) => window.clearTimeout(timerId))
    followUpTimers.length = 0
  }

  const scheduleFollowUpRefresh = () => {
    clearFollowUpTimers()
    EXPIRE_FOLLOW_UP_REFRESH_DELAYS.forEach((delay) => {
      const timerId = window.setTimeout(() => {
        if (disposed || !options.hasExpiredPending?.()) {
          clearFollowUpTimers()
          return
        }
        void runRefreshOnExpire(true)
      }, delay)
      followUpTimers.push(timerId)
    })
  }

  const checkExpiredEdgeAndRefresh = async () => {
    if (typeof options.hasExpiredPending !== 'function') {
      wasExpiredPending = false
      return
    }
    const isExpiredPending = options.hasExpiredPending()
    if (!wasExpiredPending && isExpiredPending) {
      await maybeRefreshExpiredOrders(true)
      scheduleFollowUpRefresh()
    } else if (!isExpiredPending) {
      clearFollowUpTimers()
    }
    wasExpiredPending = isExpiredPending
  }

  const startTimer = () => {
    if (disposed || timer) return
    syncNow()
    lastRefreshCheckAt = 0
    wasExpiredPending = false
    timer = window.setInterval(() => {
      syncNow()
      void checkExpiredEdgeAndRefresh()
      const now = Date.now()
      if (now - lastRefreshCheckAt >= refreshCheckIntervalMs) {
        lastRefreshCheckAt = now
        void maybeRefreshExpiredOrders()
      }
    }, countdownTickMs)
  }

  const stopTimer = () => {
    if (timer) {
      window.clearInterval(timer)
      timer = null
    }
    clearFollowUpTimers()
  }

  const applyFallbackConfig = () => {
    autoCancelEnabled.value = false
    autoCancelTimeoutSeconds.value = FALLBACK_TIMEOUT_SECONDS
    autoCancelTimeoutMinutes.value = FALLBACK_TIMEOUT_SECONDS / 60
    serverOffsetMs = 0
    configLoadError.value = true
    configLoaded.value = true
    syncNow()
    ElMessage.warning('支付超时规则加载失败，当前不按本地规则锁定支付，请以服务端校验结果为准')
  }

  const loadPaymentAutoCancelConfig = async () => {
    try {
      const response = await getPaymentAutoCancelConfig()
      const config = normalizePaymentAutoCancelConfig(response)
      if (disposed) return
      autoCancelEnabled.value = Boolean(config?.enabled)
      const timeoutSeconds = Number(config?.timeoutSeconds || 0)
      const resolvedSeconds = timeoutSeconds > 0
        ? timeoutSeconds
        : Math.max(1, Math.round(Number(config?.timeoutMinutes || 15) * 60))
      autoCancelTimeoutSeconds.value = resolvedSeconds
      autoCancelTimeoutMinutes.value = resolvedSeconds / 60
      const scanIntervalMs = Number(config?.scanIntervalMs || 0)
      if (scanIntervalMs > 0) {
        refreshCheckIntervalMs = normalizeTickMs(scanIntervalMs, DEFAULT_PAYMENT_REFRESH_CHECK_MS)
        refreshThrottleMs = Math.min(
          Number(options.refreshThrottleMs ?? refreshCheckIntervalMs),
          refreshCheckIntervalMs
        )
      }
      const serverTime = parseBackendServerTime(config)
      serverOffsetMs = serverTime ? serverTime.getTime() - Date.now() : 0
      configLoadError.value = false
      configLoaded.value = true
      syncNow()
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

  const api = {
    state,
    autoCancelEnabled,
    autoCancelTimeoutMinutes,
    autoCancelTimeoutSeconds,
    countdownNowMs,
    configLoaded,
    configLoadError,
    loadPaymentAutoCancelConfig,
    stopPaymentCountdown: stopTimer,
    formatDeadlineHint: () => formatPaymentDeadlineHint(autoCancelTimeoutSeconds.value),
    buildCountdownOptions: () => buildPaymentCountdownOptions(state),
    paymentAutoCancelRefs: {
      ...buildPaymentAutoCancelRefsBundle(state),
      triggerRefreshOnExpire
    },
    triggerRefreshOnExpire,
    dispose: () => {
      disposed = true
      stopTimer()
    }
  }

  return api
}

/** 全站唯一计时器：由 Layout 初始化，业务页注册刷新回调 */
let paymentAutoCancelSession = null
const paymentPageHandlers = new Map()
let paymentPageHandlerSeq = 0

function aggregateHasExpiredPending() {
  for (const handler of paymentPageHandlers.values()) {
    if (typeof handler.hasExpiredPending === 'function' && handler.hasExpiredPending()) {
      return true
    }
  }
  return false
}

async function aggregateRefreshOnExpire() {
  const tasks = []
  for (const handler of paymentPageHandlers.values()) {
    if (typeof handler.refreshOnExpire === 'function') {
      tasks.push(handler.refreshOnExpire())
    }
  }
  if (tasks.length) {
    await Promise.all(tasks)
  }
}

export function ensurePaymentAutoCancelSession() {
  if (!paymentAutoCancelSession) {
    paymentAutoCancelSession = createPaymentAutoCancelEngine({
      hasExpiredPending: aggregateHasExpiredPending,
      refreshOnExpire: aggregateRefreshOnExpire
    })
  }
  return paymentAutoCancelSession
}

/** Layout 调用：provide + 加载配置 */
export function setupPaymentAutoCancelSession(provideFn) {
  const session = ensurePaymentAutoCancelSession()
  if (typeof provideFn === 'function') {
    provideFn(PAYMENT_AUTO_CANCEL_KEY, session.state)
  }
  return session
}

/**
 * 业务页使用：共享全局配置/计时器，仅注册本页超时刷新逻辑
 */
export function usePaymentAutoCancelPage(options = {}) {
  const session = ensurePaymentAutoCancelSession()
  let handlerId = 0

  const registerPageHandler = () => {
    if (typeof options.hasExpiredPending !== 'function' && typeof options.refreshOnExpire !== 'function') {
      return
    }
    handlerId = ++paymentPageHandlerSeq
    paymentPageHandlers.set(handlerId, {
      hasExpiredPending: options.hasExpiredPending,
      refreshOnExpire: options.refreshOnExpire
    })
  }

  onMounted(registerPageHandler)

  onBeforeUnmount(() => {
    if (handlerId) {
      paymentPageHandlers.delete(handlerId)
    }
  })

  provide(PAYMENT_AUTO_CANCEL_KEY, session.state)

  return {
    autoCancelEnabled: session.autoCancelEnabled,
    autoCancelTimeoutMinutes: session.autoCancelTimeoutMinutes,
    autoCancelTimeoutSeconds: session.autoCancelTimeoutSeconds,
    countdownNowMs: session.countdownNowMs,
    configLoaded: session.configLoaded,
    configLoadError: session.configLoadError,
    loadPaymentAutoCancelConfig: session.loadPaymentAutoCancelConfig,
    stopPaymentCountdown: session.stopPaymentCountdown,
    formatDeadlineHint: session.formatDeadlineHint,
    buildCountdownOptions: session.buildCountdownOptions,
    paymentAutoCancelRefs: session.paymentAutoCancelRefs,
    triggerRefreshOnExpire: session.triggerRefreshOnExpire,
    orderSuccessMessage: createOrderSuccessMessage(
      session.autoCancelEnabled,
      session.formatDeadlineHint
    )
  }
}

/** @deprecated 请使用 usePaymentAutoCancelPage；保留兼容独立页场景 */
export function usePaymentAutoCancel(options = {}) {
  const engine = createPaymentAutoCancelEngine(options)
  provide(PAYMENT_AUTO_CANCEL_KEY, engine.state)
  onBeforeUnmount(() => {
    engine.dispose()
  })
  return {
    autoCancelEnabled: engine.autoCancelEnabled,
    autoCancelTimeoutMinutes: engine.autoCancelTimeoutMinutes,
    autoCancelTimeoutSeconds: engine.autoCancelTimeoutSeconds,
    countdownNowMs: engine.countdownNowMs,
    configLoaded: engine.configLoaded,
    configLoadError: engine.configLoadError,
    loadPaymentAutoCancelConfig: engine.loadPaymentAutoCancelConfig,
    stopPaymentCountdown: engine.stopPaymentCountdown,
    formatDeadlineHint: engine.formatDeadlineHint,
    buildCountdownOptions: engine.buildCountdownOptions,
    paymentAutoCancelRefs: engine.paymentAutoCancelRefs,
    triggerRefreshOnExpire: engine.triggerRefreshOnExpire
  }
}
