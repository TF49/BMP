import fs from 'fs'
import { execSync } from 'child_process'
import { fileURLToPath } from 'url'

const repoRoot = fileURLToPath(new URL('../../', import.meta.url))
const srcRoot = fileURLToPath(new URL('../src/', import.meta.url))

const adminFiles = [
  'views/BookingManagement.vue',
  'views/CourseBookingManagement.vue',
  'views/EquipmentRentalManagement.vue',
  'views/StringingServiceManagement.vue',
  'views/TournamentRegistrationManagement.vue'
]

const countdownEntities = {
  'views/BookingManagement.vue': 'booking',
  'views/CourseBookingManagement.vue': 'booking',
  'views/EquipmentRentalManagement.vue': 'rental',
  'views/StringingServiceManagement.vue': 'service',
  'views/TournamentRegistrationManagement.vue': 'registration'
}

function read(rel) {
  return fs.readFileSync(srcRoot + rel, 'utf8')
}

function write(rel, content) {
  fs.writeFileSync(srcRoot + rel, content, 'utf8')
  console.log('patched', rel)
}

function restoreFromGit(rel) {
  execSync(`git checkout HEAD -- vue/src/${rel}`, { cwd: repoRoot, encoding: 'utf8' })
  console.log('restored', rel)
}

function patchImports(content) {
  let s = content

  if (s.includes('usePaymentAutoCancel(')) {
    s = s.replace(
      /import \{([^}]*?)usePaymentAutoCancel([^}]*)\} from '@\/composables\/usePaymentAutoCancel'/,
      "import { buildPaymentCountdownOptions, getPaymentAutoCancelInfo, usePaymentAutoCancelPage } from '@/composables/usePaymentAutoCancel'"
    )
    s = s.replace(/\busePaymentAutoCancel\(/g, 'usePaymentAutoCancelPage(')
  }

  if (!s.includes('usePaymentCountdownListCache')) {
    s = s.replace(
      "} from '@/composables/usePaymentAutoCancel'",
      `} from '@/composables/usePaymentAutoCancel'
import {
  bindPaymentCountdownCacheClear,
  usePaymentCountdownListCache
} from '@/composables/usePaymentCountdownListCache'`
    )
  }

  if (!s.includes('useAdminOrdersRefreshListener')) {
    s = s.replace(
      "} from '@/composables/usePaymentAutoCancel'",
      `} from '@/composables/usePaymentAutoCancel'
import { useAdminOrdersRefreshListener } from '@/utils/paymentOrderRefresh'`
    )
  }

  if (!s.includes('PaymentPayCountdown')) {
    s = s.replace(
      "import { openActionConfirm } from '@/utils/confirm'",
      `import PaymentPayCountdown from '@/components/payment/PaymentPayCountdown.vue'
import { openActionConfirm } from '@/utils/confirm'`
    )
  }

  return s
}

function patchDestructuring(content) {
  return content.replace(
    /const \{\n  autoCancelEnabled,\n  autoCancelTimeoutMinutes,\n  countdownNowMs,\n  loadPaymentAutoCancelConfig\n\} = usePaymentAutoCancelPage\(/,
    `const {
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError,
  paymentAutoCancelRefs
} = usePaymentAutoCancelPage(`
  )
}

function patchCountdownLogic(content, entity, rel) {
  const directBlock = `const getPaymentCountdownInfo = (${entity}) => getPaymentAutoCancelInfo(${entity}, {
  enabled: autoCancelEnabled.value,
  timeoutMinutes: autoCancelTimeoutMinutes.value,
  nowMs: countdownNowMs.value
})

const isPaymentExpired = (${entity}) => getPaymentCountdownInfo(${entity}).expired`

  if (rel.includes('StringingService')) {
    if (!content.includes('shouldShowStringingPaymentCountdown')) {
      content = content.replace(
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

    const stringingBlock = `const paymentCountdownState = () => ({
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

    if (content.includes(directBlock)) {
      return content.replace(directBlock, stringingBlock)
    }
    return content
  }

  const cachedBlock = `const paymentCountdownState = () => ({
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError
})
const { getPaymentCountdownInfo, clearCache: clearCountdownCache } = usePaymentCountdownListCache(paymentCountdownState)
bindPaymentCountdownCacheClear({ countdownNowMs }, clearCountdownCache)
const isPaymentExpired = (${entity}) => getPaymentCountdownInfo(${entity}).expired`

  if (content.includes(directBlock)) {
    return content.replace(directBlock, cachedBlock)
  }
  return content
}

function patchOnMounted(content) {
  let s = content.replace(/\s*loadPaymentAutoCancelConfig\(\)\s*\n/g, '\n')

  if (!s.includes('useAdminOrdersRefreshListener(')) {
    s = s.replace(
      /onMounted\(\(\) => \{\n  calculateTableHeight\(\)/,
      `onMounted(() => {
  useAdminOrdersRefreshListener(() => {
    void Promise.all([loadList(), loadStatistics()])
  })
  calculateTableHeight()`
    )
  }

  return s
}

function patchPayDialog(content) {
  let s = content

  if (!s.includes('PaymentPayCountdown :order="currentPay"')) {
    const insert = `        <el-form-item label="支付时限">
          <PaymentPayCountdown :order="currentPay" :countdown-state="paymentAutoCancelRefs" />
        </el-form-item>
`
    s = s.replace(
      /(<el-dialog v-model="payDialogVisible"[^>]*>\s*<el-form label-width="100px">\s*\n)/,
      `$1${insert}`
    )
  }

  s = s.replace(
    '<el-button type="primary" :loading="payLoading" @click="submitPay">',
    '<el-button type="primary" :loading="payLoading" :disabled="currentPay && isPaymentExpired(currentPay)" @click="submitPay">'
  )

  return s
}

function patchTournamentPayment(content) {
  let s = content

  if (!s.includes('currentPayRegistration')) {
    s = s.replace(
      'const paymentDialogVisible = ref(false)\nconst paymentForm = reactive({',
      `const paymentDialogVisible = ref(false)
const currentPayRegistration = ref(null)
const paymentForm = reactive({`
    )

    s = s.replace(
      /const handlePayment = \(row\) => \{[\s\S]*?Object\.assign\(paymentForm,/,
      `const handlePayment = (row) => {
  if (!canCollectRegistrationPayment(row)) {
    ElMessage.warning(isPaymentExpired(row) ? '订单已超时，暂不可支付' : '当前报名状态不可支付')
    if (isPaymentExpired(row)) {
      Promise.all([loadList(), loadStatistics()])
    }
    return
  }
  currentPayRegistration.value = row
  Object.assign(paymentForm,`
    )
  }

  if (!s.includes('PaymentPayCountdown :order="currentPayRegistration"')) {
    const insert = `        <el-form-item label="支付时限">
          <PaymentPayCountdown :order="currentPayRegistration" :countdown-state="paymentAutoCancelRefs" />
        </el-form-item>
`
    s = s.replace(
      /(<el-form :model="paymentForm" label-position="top" class="modern-form">\s*\n)/,
      `$1${insert}`
    )
  }

  s = s.replace(
    '<el-button type="primary" class="modern-btn-submit" :loading="paymentLoading" @click="handlePaymentSubmit">',
    `<el-button
            type="primary"
            class="modern-btn-submit"
            :loading="paymentLoading"
            :disabled="currentPayRegistration && isPaymentExpired(currentPayRegistration)"
            @click="handlePaymentSubmit"
          >`
  )

  return s
}

function verifyChinese(content, rel) {
  const badPatterns = [/[\uFFFD]/, /\?约/, /\?付/, /\?名/, /管\?/]
  for (const pattern of badPatterns) {
    if (pattern.test(content)) {
      console.warn('possible garbled text remains in', rel, pattern.toString())
    }
  }
}

for (const rel of adminFiles) {
  restoreFromGit(rel)
  let content = read(rel)
  content = patchImports(content)
  content = patchDestructuring(content)
  content = patchCountdownLogic(content, countdownEntities[rel], rel)
  content = patchOnMounted(content)

  if (rel.includes('TournamentRegistration')) {
    content = patchTournamentPayment(content)
  } else {
    content = patchPayDialog(content)
  }

  write(rel, content)
  verifyChinese(content, rel)
}

console.log('restore complete')
