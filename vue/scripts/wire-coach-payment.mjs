import fs from 'fs'
import { fileURLToPath } from 'url'

const coachLayout = fileURLToPath(new URL('../src/layout/CoachLayout.vue', import.meta.url))
const coachBookings = fileURLToPath(new URL('../src/views/coach/Bookings.vue', import.meta.url))

let layout = fs.readFileSync(coachLayout, 'utf8')
if (!layout.includes('ensurePaymentAutoCancelSession')) {
  layout = layout.replace(
    "import { ref, computed, onMounted, onUnmounted } from 'vue'",
    "import { ref, computed, onMounted, onUnmounted, provide } from 'vue'"
  )
  layout = layout.replace(
    "import { getToken, removeToken, removeRefreshToken } from '@/utils/auth'",
    `import { getToken, removeToken, removeRefreshToken } from '@/utils/auth'
import { ensurePaymentAutoCancelSession, PAYMENT_AUTO_CANCEL_KEY } from '@/composables/usePaymentAutoCancel'

const paymentAutoCancelSession = ensurePaymentAutoCancelSession()
provide(PAYMENT_AUTO_CANCEL_KEY, paymentAutoCancelSession.state)`
  )
  layout = layout.replace(
    /onMounted\(\(\) => \{\n/,
    'onMounted(() => {\n  void paymentAutoCancelSession.loadPaymentAutoCancelConfig()\n'
  )
  fs.writeFileSync(coachLayout, layout, 'utf8')
  console.log('patched CoachLayout')
}

let bookings = fs.readFileSync(coachBookings, 'utf8')
if (!bookings.includes('usePaymentAutoCancelPage')) {
  bookings = bookings.replace(
    "import {\n  BOOKING_STATUS_TEXT_MAP,",
    `import PaymentCountdownBadge from '@/components/payment/PaymentCountdownBadge.vue'
import {
  buildPaymentCountdownOptions,
  getPaymentAutoCancelInfo,
  usePaymentAutoCancelPage
} from '@/composables/usePaymentAutoCancel'
import { PAYMENT_ORDER_TYPES, useOrderStatusRefreshListener } from '@/utils/paymentOrderRefresh'
import {
  BOOKING_STATUS_TEXT_MAP,`
  )

  bookings = bookings.replace(
    'const loadFailed = ref(false)',
    `const {
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError
} = usePaymentAutoCancelPage({
  hasExpiredPending: () =>
    list.value.some((row) => Number(row?.status) === 1 && getPaymentCountdownInfo(row).expired),
  refreshOnExpire: () => loadList()
})

const paymentCountdownState = () => ({
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError
})

const getPaymentCountdownInfo = (row) =>
  getPaymentAutoCancelInfo(row, buildPaymentCountdownOptions(paymentCountdownState()))

const showPaymentCountdown = (row) =>
  Number(row?.status) === 1 && Number(row?.paymentStatus ?? 0) === 0

const loadFailed = ref(false)`
  )

  bookings = bookings.replace(
    `              <el-tag :type="formatStatusType(row.status, BOOKING_STATUS_TYPE_MAP)" size="small">
                {{ formatStatusText(row.status, BOOKING_STATUS_TEXT_MAP) }}
              </el-tag>
            </div>`,
    `              <el-tag :type="formatStatusType(row.status, BOOKING_STATUS_TYPE_MAP)" size="small">
                {{ formatStatusText(row.status, BOOKING_STATUS_TEXT_MAP) }}
              </el-tag>
              <PaymentCountdownBadge
                v-if="showPaymentCountdown(row) && getPaymentCountdownInfo(row).show"
                :info="getPaymentCountdownInfo(row)"
                size="small"
              />
            </motion>`
  )
  bookings = bookings.replace('            </motion>', '            </div>')

  bookings = bookings.replace(
    /const loadList = async \(\) => \{/,
    `useOrderStatusRefreshListener(PAYMENT_ORDER_TYPES.courseBooking, () => {
  void loadList()
})

const loadList = async () => {`
  )

  fs.writeFileSync(coachBookings, bookings, 'utf8')
  console.log('patched coach Bookings')
}
