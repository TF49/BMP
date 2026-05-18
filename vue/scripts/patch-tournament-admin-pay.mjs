import fs from 'fs'
import { fileURLToPath } from 'url'

const f = fileURLToPath(new URL('../src/views/TournamentRegistrationManagement.vue', import.meta.url))
let s = fs.readFileSync(f, 'utf8')

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

fs.writeFileSync(f, s, 'utf8')
console.log('patched', f)
