import fs from 'fs'

const p = new URL('../src/views/user/Dashboard.vue', import.meta.url)
let s = fs.readFileSync(p, 'utf8')

s = s.replace(/<motion class="pending-pay-list">/g, '<div class="pending-pay-list">')

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

if (!s.includes('已超时')) {
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
}

fs.writeFileSync(p, s, 'utf8')
console.log('dashboard template ok')
