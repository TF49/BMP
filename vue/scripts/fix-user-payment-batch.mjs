import fs from 'fs'
import { fileURLToPath } from 'url'

const root = fileURLToPath(new URL('../src/views/user/', import.meta.url))

const pages = [
  ['Booking.vue', 'booking', 'booking'],
  ['CourseBooking.vue', 'course', 'course'],
  ['EquipmentRental.vue', 'rental', 'rental'],
  ['StringingService.vue', 'service', 'service'],
  ['TournamentRegistration.vue', 'registration', 'registration']
]

for (const [file, entity, orderVar] of pages) {
  let s = fs.readFileSync(root + file, 'utf8')

  if (!s.includes('paymentAutoCancelRefs')) {
    s = s.replace(
      /countdownNowMs\n\} = usePaymentAutoCancelPage\(/,
      `countdownNowMs,
  paymentAutoCancelRefs,
  formatDeadlineHint
} = usePaymentAutoCancelPage(`
    )
  }

  const elTag = `                  <el-tag
                    v-if="getPaymentCountdownInfo(${entity}).show"
                    :type="isPaymentExpired(${entity}) ? 'danger' : 'warning'"
                    effect="plain"
                    size="small"
                  >
                    {{ getPaymentCountdownInfo(${entity}).text }}
                  </el-tag>`
  const badge = `                  <PaymentCountdownBadge
                    v-if="getPaymentCountdownInfo(${entity}).show"
                    :info="getPaymentCountdownInfo(${entity})"
                    size="small"
                  />`
  if (s.includes(elTag)) s = s.replace(elTag, badge)

  if (!s.includes('orderSuccessMessage')) {
    s = s.replace(
      /\} = usePaymentAutoCancelPage\(\{[\s\S]*?\}\)\n/,
      (m) => m + `
const orderSuccessMessage = (base) => {
  if (!autoCancelEnabled?.value) return base
  const hint = typeof formatDeadlineHint === 'function' ? formatDeadlineHint() : ''
  return hint ? \`\${base} \${hint}\` : base
}
`
    )
  }

  fs.writeFileSync(root + file, s, 'utf8')
  console.log('fixed', file)
}
