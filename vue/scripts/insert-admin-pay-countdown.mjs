import fs from 'fs'
import { fileURLToPath } from 'url'

const files = [
  'src/views/BookingManagement.vue',
  'src/views/CourseBookingManagement.vue',
  'src/views/EquipmentRentalManagement.vue',
  'src/views/StringingServiceManagement.vue'
].map((p) => fileURLToPath(new URL('../' + p, import.meta.url)))

const insert = `        <el-form-item label="支付时限">
          <PaymentPayCountdown :order="currentPay" :countdown-state="paymentAutoCancelRefs" />
        </el-form-item>
`

for (const f of files) {
  let s = fs.readFileSync(f, 'utf8')
  if (s.includes('PaymentPayCountdown :order="currentPay"')) {
    console.log('has countdown', f)
    continue
  }
  const re = /(<el-dialog v-model="payDialogVisible"[^>]*>\s*<el-form label-width="100px">\s*\n)/
  if (!re.test(s)) {
    console.warn('no match', f)
    continue
  }
  s = s.replace(re, `$1${insert}`)
  fs.writeFileSync(f, s, 'utf8')
  console.log('inserted', f)
}
