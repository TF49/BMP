import fs from 'fs'
import { fileURLToPath } from 'url'

const src = fileURLToPath(new URL('../src/views/', import.meta.url))

const configs = [
  { file: 'BookingManagement.vue', param: 'booking' },
  { file: 'CourseBookingManagement.vue', param: 'booking' },
  { file: 'EquipmentRentalManagement.vue', param: 'rental' },
  { file: 'TournamentRegistrationManagement.vue', param: 'registration' }
]

const directBlock = (param) => `const getPaymentCountdownInfo = (${param}) => getPaymentAutoCancelInfo(${param}, {
  enabled: autoCancelEnabled.value,
  timeoutMinutes: autoCancelTimeoutMinutes.value,
  nowMs: countdownNowMs.value
})

const isPaymentExpired = (${param}) => getPaymentCountdownInfo(${param}).expired`

const cachedBlock = (param) => `const paymentCountdownState = () => ({
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError
})
const { getPaymentCountdownInfo, clearCache: clearCountdownCache } = usePaymentCountdownListCache(paymentCountdownState)
bindPaymentCountdownCacheClear({ countdownNowMs }, clearCountdownCache)
const isPaymentExpired = (${param}) => getPaymentCountdownInfo(${param}).expired`

for (const { file, param } of configs) {
  const p = src + file
  let s = fs.readFileSync(p, 'utf8')

  s = s.replace(
    `  countdownNowMs,
  loadPaymentAutoCancelConfig
} = usePaymentAutoCancelPage(`,
    `  countdownNowMs,
  configLoaded,
  configLoadError
} = usePaymentAutoCancelPage(`
  )

  const old = directBlock(param)
  const neu = cachedBlock(param)
  if (s.includes(old)) {
    s = s.replace(old, neu)
    console.log('cache wired', file)
  } else if (s.includes('usePaymentCountdownListCache(paymentCountdownState)')) {
    console.log('cache already', file)
  } else {
    console.warn('cache pattern missing', file)
  }

  if (!s.includes('useAdminOrdersRefreshListener(')) {
    s = s.replace(
      'onMounted(() => {\n  calculateTableHeight()',
      `onMounted(() => {
  useAdminOrdersRefreshListener(() => {
    void Promise.all([loadList(), loadStatistics()])
  })
  calculateTableHeight()`
    )
  }

  fs.writeFileSync(p, s, 'utf8')
}

// Stringing
const sp = src + 'StringingServiceManagement.vue'
let ss = fs.readFileSync(sp, 'utf8')
ss = ss.replace(
  `  countdownNowMs,
  loadPaymentAutoCancelConfig
} = usePaymentAutoCancelPage(`,
  `  countdownNowMs,
  configLoaded,
  configLoadError
} = usePaymentAutoCancelPage(`
)
if (!ss.includes('shouldShowStringingPaymentCountdown')) {
  ss = ss.replace(
    "import { buildPaymentCountdownOptions, getPaymentAutoCancelInfo, usePaymentAutoCancelPage } from '@/composables/usePaymentAutoCancel'",
    `import {
  buildPaymentCountdownOptions,
  EMPTY_PAYMENT_COUNTDOWN_INFO,
  getPaymentAutoCancelInfo,
  shouldShowStringingPaymentCountdown,
  usePaymentAutoCancelPage
} from '@/composables/usePaymentAutoCancel'`
  )
}
const stringingOld = directBlock('service')
const stringingNew = `const paymentCountdownState = () => ({
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError
})
const { getPaymentCountdownInfo: getCachedPaymentCountdown, clearCache: clearCountdownCache } =
  usePaymentCountdownListCache(paymentCountdownState)
bindPaymentCountdownCacheClear({ countdownNowMs }, clearCountdownCache)
const getPaymentCountdownInfo = (service) => {
  if (!shouldShowStringingPaymentCountdown(service)) {
    return EMPTY_PAYMENT_COUNTDOWN_INFO
  }
  return getCachedPaymentCountdown(service)
}
const isPaymentExpired = (service) => getPaymentCountdownInfo(service).expired`
if (ss.includes(stringingOld)) {
  ss = ss.replace(stringingOld, stringingNew)
  console.log('cache wired StringingServiceManagement.vue')
}
if (!ss.includes('useAdminOrdersRefreshListener(')) {
  ss = ss.replace(
    'onMounted(() => {\n  calculateTableHeight()',
    `onMounted(() => {
  useAdminOrdersRefreshListener(() => {
    void Promise.all([loadList(), loadStatistics()])
  })
  calculateTableHeight()`
  )
}
fs.writeFileSync(sp, ss, 'utf8')
