import fs from 'fs'

const p = new URL('../src/views/user/Dashboard.vue', import.meta.url)
let s = fs.readFileSync(p, 'utf8')

if (s.includes('usePaymentAutoCancelPage')) {
  console.log('dashboard already patched')
  process.exit(0)
}

// imports
s = s.replace(
  "import { getPaymentAutoCancelInfo, usePaymentAutoCancel } from '@/composables/usePaymentAutoCancel'",
  `import PaymentCountdownBadge from '@/components/payment/PaymentCountdownBadge.vue'
import {
  buildPaymentCountdownOptions,
  getPaymentAutoCancelInfo,
  usePaymentAutoCancelPage
} from '@/composables/usePaymentAutoCancel'
import { PAYMENT_ORDER_TYPES, useOrderStatusRefreshListener } from '@/utils/paymentOrderRefresh'
import { fetchUserPendingPaymentOrders } from '@/utils/pendingUserOrders'`
)

s = s.replace('const recentBookings = ref([])', 'const recentBookings = ref([])\nconst pendingPayOrders = ref([])')

s = s.replace(
  /const \{\n  autoCancelEnabled,\n  autoCancelTimeoutMinutes,\n  countdownNowMs,\n  loadPaymentAutoCancelConfig\n\} = usePaymentAutoCancel\(\{[\s\S]*?\}\)/,
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
})`
)

// quick action
s = s.replace(
  'const quickActions = [\n  {\n    title: \'场地预订\'',
  `const quickActions = [
  {
    title: '待支付订单',
    desc: '查看全部待支付业务订单',
    path: '/user/orders',
    icon: Clock,
    gradient: 'linear-gradient(135deg, #f59e0b 0%, #ef4444 100%)'
  },
  {
    title: '场地预订'`
)

// countdown helpers
s = s.replace(
  /const getPaymentCountdownInfo = \(booking\) => getPaymentAutoCancelInfo\(booking, \{[\s\S]*?\}\)\n\nconst isPaymentExpired/,
  `const getPaymentCountdownInfo = (booking) =>
  getPaymentAutoCancelInfo(booking, buildPaymentCountdownOptions(paymentCountdownState()))

const getPendingItemCountdown = (item) => getPaymentCountdownInfo(item.raw)

const isPaymentExpired`
)

// loadPendingPayOrders before loadBookings
s = s.replace(
  'const loadBookings = async () => {',
  `const loadPendingPayOrders = async () => {
  try {
    pendingPayOrders.value = await fetchUserPendingPaymentOrders(3)
  } catch (e) {
    console.error('加载待支付订单失败:', e)
    pendingPayOrders.value = []
  }
}

const loadBookings = async () => {`
)

// handlePay paymentAutoCancel
s = s.replace(
  '  openActionConfirm({\n    title: \'预约支付确认\'',
  '  openActionConfirm({\n    paymentAutoCancel: paymentAutoCancelRefs,\n    title: \'预约支付确认\''
)

// onMounted
s = s.replace(
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

// template: pending section before my-bookings
s = s.replace(
  '    <!-- 我的预约 -->',
  `    <!-- 待支付提醒 -->
    <div v-if="pendingPayOrders.length > 0" class="pending-pay-section">
      <div class="section-header">
        <h3 class="section-title">待支付提醒</h3>
        <el-button type="primary" link @click="$router.push('/user/orders')">查看全部</el-button>
      </div>
      <motion class="pending-pay-list">
        <div
          v-for="item in pendingPayOrders"
          :key="\`\${item.orderType}-\${item.orderNo}\`"
          class="pending-pay-card"
          @click="$router.push('/user/orders')"
        >
          <div class="pending-pay-main">
            <el-tag size="small" type="warning">{{ item.title }}</el-tag>
            <span class="pending-pay-no">{{ item.orderNo }}</span>
            <PaymentCountdownBadge :info="getPendingItemCountdown(item)" size="small" />
          </div>
          <span class="pending-pay-amount">¥{{ formatCurrency(item.amount) }}</span>
        </div>
      </div>
    </div>

    <!-- 我的预约 -->`
).replace(/<motion/g, '<motion').replace(/<\/motion>/g, '</div>')

// template: el-tag -> PaymentCountdownBadge
s = s.replace(
  `              <el-tag
                v-if="getPaymentCountdownInfo(booking).show"
                :type="isPaymentExpired(booking) ? 'danger' : 'warning'"
                effect="plain"
                size="small"
              >
                {{ getPaymentCountdownInfo(booking).text }}
              </el-tag>`,
  `              <PaymentCountdownBadge
                v-if="getPaymentCountdownInfo(booking).show"
                :info="getPaymentCountdownInfo(booking)"
                size="small"
              />`
)

// expired pay button
s = s.replace(
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

// styles for pending section
if (!s.includes('.pending-pay-section')) {
  s = s.replace(
    '.my-bookings {',
    `.pending-pay-section {
  margin-bottom: 32px;
}

.pending-pay-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pending-pay-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #fde68a;
  background: linear-gradient(135deg, #fffbeb 0%, #fff7ed 100%);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.pending-pay-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(245, 158, 11, 0.15);
}

.pending-pay-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pending-pay-no {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
}

.pending-pay-amount {
  font-size: 16px;
  font-weight: 700;
  color: #b45309;
}

.my-bookings {`
  )
}

fs.writeFileSync(p, s, 'utf8')
console.log('dashboard patched', s.includes('usePaymentAutoCancelPage'))
