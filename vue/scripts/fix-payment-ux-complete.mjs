import fs from 'fs'
import { fileURLToPath } from 'url'

const root = fileURLToPath(new URL('../src/', import.meta.url))

function read(rel) {
  return fs.readFileSync(root + rel, 'utf8')
}
function write(rel, s) {
  fs.writeFileSync(root + rel, s, 'utf8')
  console.log('updated', rel)
}

function addPaymentRefsToPage(s) {
  if (s.includes('paymentAutoCancelRefs')) return s
  return s.replace(
    /(\s+countdownNowMs\n)(\} = usePaymentAutoCancelPage\()/,
    `$1  paymentAutoCancelRefs,\n  formatDeadlineHint$2`
  )
}

function addOrderSuccessHelper(s) {
  if (s.includes('function orderSuccessMessage')) return s
  const marker = '} = usePaymentAutoCancelPage({'
  const idx = s.indexOf(marker)
  if (idx === -1) return s
  const closeIdx = s.indexOf('})\n', idx)
  if (closeIdx === -1) return s
  const insertAt = closeIdx + 3
  const helper = `
const orderSuccessMessage = (base) => {
  if (!autoCancelEnabled?.value) return base
  const hint = typeof formatDeadlineHint === 'function' ? formatDeadlineHint() : ''
  return hint ? \`\${base} \${hint}\` : base
}
`
  return s.slice(0, insertAt) + helper + s.slice(insertAt)
}

function patchHandlePay(s, orderVar) {
  const re = new RegExp(
    `(openActionConfirm\\(\\{\\n    title:[^\\n]+\\n(?:    [^\\n]+\\n)*?    cancelButtonText:[^\\n]+)\\n  \\}\\)`
  )
  if (!s.includes('paymentAutoCancel: paymentAutoCancelRefs') && re.test(s)) {
    s = s.replace(
      re,
      `$1,\n    paymentOrder: ${orderVar},\n    paymentAutoCancel: paymentAutoCancelRefs\n  })`
    )
  }
  return s
}

function replaceCountdownTag(s) {
  const block = /              <el-tag\n                v-if="getPaymentCountdownInfo\([^)]+\)\.show"\n                :type="isPaymentExpired\([^)]+\) \? 'danger' : 'warning'"\n                effect="plain"\n                size="small"\n              >\n                \{\{ getPaymentCountdownInfo\([^)]+\)\.text \}\}\n              <\/el-tag>/g
  if (!block.test(s)) return s
  return s.replace(
    block,
    `              <PaymentCountdownBadge
                v-if="getPaymentCountdownInfo($1).show"
                :info="getPaymentCountdownInfo($1)"
                size="small"
              />`
  )
}

// Simpler tag replace per entity name
function replaceElTagCountdown(s, entity) {
  const old = `              <el-tag
                v-if="getPaymentCountdownInfo(${entity}).show"
                :type="isPaymentExpired(${entity}) ? 'danger' : 'warning'"
                effect="plain"
                size="small"
              >
                {{ getPaymentCountdownInfo(${entity}).text }}
              </el-tag>`
  const neu = `              <PaymentCountdownBadge
                v-if="getPaymentCountdownInfo(${entity}).show"
                :info="getPaymentCountdownInfo(${entity})"
                size="small"
              />`
  if (s.includes(old)) {
    s = s.replace(old, neu)
  }
  return s
}

function addExpiredButton(s, entity, payCondition) {
  const marker = `              @click="handlePay(${entity})"
            >
              立即支付
            </el-button>`
  const extra = `            </el-button>
            <el-button
              v-else-if="${payCondition} && isPaymentExpired(${entity})"
              type="info"
              size="small"
              disabled
            >
              已超时
            </el-button>`
  if (s.includes(marker) && !s.includes(`v-else-if="${payCondition} && isPaymentExpired`)) {
    s = s.replace(marker, marker.replace('            </el-button>', extra))
  }
  return s
}

function addBadgeImport(s) {
  if (s.includes('PaymentCountdownBadge')) return s
  return s.replace(
    "import { openActionConfirm } from '@/utils/confirm'",
    `import PaymentCountdownBadge from '@/components/payment/PaymentCountdownBadge.vue'\nimport { openActionConfirm } from '@/utils/confirm'`
  )
}

function patchOrderSuccessMessages(s, pairs) {
  for (const [from, to] of pairs) {
    if (s.includes(from) && !s.includes('orderSuccessMessage')) {
      s = s.replace(from, to)
    } else if (s.includes(from)) {
      s = s.replace(from, to)
    }
  }
  return s
}

// --- User pages ---
const userPages = [
  { file: 'views/user/Booking.vue', entity: 'booking', payCond: 'booking.status === 1', orderVar: 'booking',
    success: [["ElMessage.success('预订成功！')", "ElMessage.success(orderSuccessMessage('预订成功！'))"]] },
  { file: 'views/user/CourseBooking.vue', entity: 'course', payCond: 'course.status === 1', orderVar: 'course',
    success: [["ElMessage.success('预约成功！')", "ElMessage.success(orderSuccessMessage('预约成功！'))"]] },
  { file: 'views/user/EquipmentRental.vue', entity: 'rental', payCond: 'rental.status === 1', orderVar: 'rental',
    success: [["ElMessage.success('租借成功！')", "ElMessage.success(orderSuccessMessage('租借成功！'))"]] },
  { file: 'views/user/StringingService.vue', entity: 'service', payCond: 'canPayService(service)', orderVar: 'service',
    success: [["ElMessage.success('申请成功！')", "ElMessage.success(orderSuccessMessage('申请成功！'))"]] },
  { file: 'views/user/TournamentRegistration.vue', entity: 'registration', payCond: 'registration.status === 1', orderVar: 'registration',
    success: [["ElMessage.success('报名成功！')", "ElMessage.success(orderSuccessMessage('报名成功！'))"]] }
]

for (const p of userPages) {
  let s = read(p.file)
  s = addBadgeImport(s)
  s = addPaymentRefsToPage(s)
  s = addOrderSuccessHelper(s)
  s = replaceElTagCountdown(s, p.entity)
  if (p.file.includes('StringingService')) {
    // handlePayService
    s = patchHandlePay(s.replace(/handlePayService/g, 'HANDLEPAY_TEMP'), 'service').replace(/HANDLEPAY_TEMP/g, 'handlePayService')
    if (!s.includes('paymentAutoCancel: paymentAutoCancelRefs')) {
      s = s.replace(
        /(openActionConfirm\(\{\n    title: '穿线服务支付确认'[\s\S]*?cancelButtonText: '稍后支付')\n  \}\)/,
        `$1,\n    paymentOrder: service,\n    paymentAutoCancel: paymentAutoCancelRefs\n  })`
      )
    }
  } else {
    s = patchHandlePay(s, p.orderVar)
  }
  for (const [a, b] of p.success) s = s.replace(a, b)
  write(p.file, s)
}

// EquipmentRental handlePay might be different - grep
let equip = read('views/user/EquipmentRental.vue')
if (!equip.includes('paymentAutoCancel: paymentAutoCancelRefs')) {
  equip = equip.replace(
    /(openActionConfirm\(\{[\s\S]*?entityValue: rental\.rentalNo,[\s\S]*?cancelButtonText: '稍后支付')\n  \}\)/m,
    `$1,\n    paymentOrder: rental,\n    paymentAutoCancel: paymentAutoCancelRefs\n  })`
  )
  write('views/user/EquipmentRental.vue', equip)
}

// --- Dashboard ---
let dash = read('views/user/Dashboard.vue')
dash = dash.replace(
  /const \{\n  autoCancelEnabled,\n  autoCancelTimeoutMinutes,\n  countdownNowMs,\n  loadPaymentAutoCancelConfig\n\} = usePaymentAutoCancel\(\{[\s\S]*?\}\)\n\nconst greeting/,
  `const paymentCountdownState = () => ({
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError
})

const {
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError,
  paymentAutoCancelRefs,
  formatDeadlineHint
} = usePaymentAutoCancelPage({
  hasExpiredPending: () =>
    recentBookings.value.some((item) => isPaymentExpired(item)) ||
    pendingPayOrders.value.some((item) => getPaymentCountdownInfo(item.raw).expired),
  refreshOnExpire: async () => {
    await Promise.all([loadBookings(), loadPendingPayOrders()])
  }
})

const greeting`
)
dash = dash.replace(
  /const getPaymentCountdownInfo = \(booking\) => getPaymentAutoCancelInfo\(booking, \{\n  enabled: autoCancelEnabled\.value,\n  timeoutMinutes: autoCancelTimeoutMinutes\.value,\n  nowMs: countdownNowMs\.value\n\}\)\n\nconst isPaymentExpired/,
  `const getPaymentCountdownInfo = (booking) =>
  getPaymentAutoCancelInfo(booking, buildPaymentCountdownOptions(paymentCountdownState()))

const getPendingItemCountdown = (item) => getPaymentCountdownInfo(item.raw)

const isPaymentExpired`
)
dash = replaceElTagCountdown(dash, 'booking')
dash = dash.replace(
  `            <el-button
              v-if="booking.status === 1 && !isPaymentExpired(booking)"
              type="primary"
              size="small"
              @click="handlePay(booking)"
            >
              立即支付
            </el-button>`,
  `            <el-button
              v-if="booking.status === 1 && !isPaymentExpired(booking)"
              type="primary"
              size="small"
              @click="handlePay(booking)"
            >
              立即支付
            </el-button>
            <el-button
              v-else-if="booking.status === 1 && isPaymentExpired(booking)"
              type="info"
              size="small"
              disabled
            >
              已超时
            </el-button>`
)
if (!dash.includes('paymentAutoCancel: paymentAutoCancelRefs')) {
  dash = dash.replace(
    /(openActionConfirm\(\{\n    title: '预约支付确认'[\s\S]*?cancelButtonText: '稍后支付')\n  \}\)/,
    `$1,\n    paymentOrder: booking,\n    paymentAutoCancel: paymentAutoCancelRefs\n  })`
  )
}
dash = dash.replace(
  `onMounted(() => {
  loadPaymentAutoCancelConfig()
  loadUserInfo()
  loadBalance()
  loadBookings()
})`,
  `useOrderStatusRefreshListener(PAYMENT_ORDER_TYPES.booking, () => {
  void Promise.all([loadBookings(), loadPendingPayOrders()])
})

onMounted(() => {
  loadUserInfo()
  loadBalance()
  loadBookings()
  loadPendingPayOrders()
})`
)
if (!dash.includes("title: '待支付订单'")) {
  dash = dash.replace(
    `const quickActions = [
  {
    title: '场地预订',`,
    `const quickActions = [
  {
    title: '待支付订单',
    desc: '查看全部待支付业务订单',
    path: '/user/orders',
    icon: Clock,
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #ef4444 100%)'
  },
  {
    title: '场地预订',`
  )
}
write('views/user/Dashboard.vue', dash)

// --- Admin management pay dialogs ---
const adminFiles = [
  'views/BookingManagement.vue',
  'views/CourseBookingManagement.vue',
  'views/EquipmentRentalManagement.vue',
  'views/StringingServiceManagement.vue'
]

for (const file of adminFiles) {
  let s = read(file)
  s = addPaymentRefsToPage(s)
  if (!s.includes('PaymentPayCountdown')) {
    s = s.replace(
      "import { openActionConfirm } from '@/utils/confirm'",
      `import PaymentPayCountdown from '@/components/payment/PaymentPayCountdown.vue'\nimport { openActionConfirm } from '@/utils/confirm'`
    )
    s = s.replace(
      /(<el-form-item label="[^"]*">\n          <el-select v-model="payForm\.method")/,
      `        <el-form-item label="支付时限">
          <PaymentPayCountdown :order="currentPay" :countdown-state="paymentAutoCancelRefs" />
        </el-form-item>
        $1`
    )
    if (!s.includes('PaymentPayCountdown')) {
      // fallback: after first form item in pay dialog
      s = s.replace(
        /(<el-dialog v-model="payDialogVisible"[\s\S]*?<el-form label-width="100px">\n)/,
        `$1        <el-form-item label="支付时限">
          <PaymentPayCountdown :order="currentPay" :countdown-state="paymentAutoCancelRefs" />
        </el-form-item>
`
      )
    }
    s = s.replace(
      '<el-button type="primary" :loading="payLoading" @click="submitPay">',
      '<el-button type="primary" :loading="payLoading" :disabled="currentPay && isPaymentExpired(currentPay)" @click="submitPay">'
    )
  }
  write(file, s)
}

console.log('done')
