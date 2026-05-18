import fs from 'fs'
import { fileURLToPath } from 'url'

const src = fileURLToPath(new URL('../src/views/', import.meta.url))
const files = [
  'BookingManagement.vue',
  'CourseBookingManagement.vue',
  'EquipmentRentalManagement.vue',
  'StringingServiceManagement.vue',
  'TournamentRegistrationManagement.vue'
]

for (const f of files) {
  const s = fs.readFileSync(src + f, 'utf8')
  const bad = /[\uFFFD]|\?约|\?付|管\?/.test(s)
  const title = s.match(/page-title">([^<]+)/)?.[1]
  console.log(f, {
    bad,
    title,
    payCountdown: s.includes('PaymentPayCountdown'),
    pageApi: s.includes('usePaymentAutoCancelPage'),
    sample: s.includes('余额支付') && s.includes('支付时限')
  })
}
