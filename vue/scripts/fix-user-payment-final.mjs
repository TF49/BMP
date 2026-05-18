import fs from 'fs'
import { fileURLToPath } from 'url'

const userDir = fileURLToPath(new URL('../src/views/user/', import.meta.url))
const adminFiles = [
  '../src/views/BookingManagement.vue',
  '../src/views/CourseBookingManagement.vue',
  '../src/views/EquipmentRentalManagement.vue',
  '../src/views/StringingServiceManagement.vue',
  '../src/views/TournamentRegistrationManagement.vue'
].map((p) => fileURLToPath(new URL(p, import.meta.url)))

const userFiles = fs.readdirSync(userDir).filter((f) => f.endsWith('.vue')).map((f) => userDir + f)

function addRefs(s) {
  if (s.includes('paymentAutoCancelRefs')) return s
  return s.replace(
    /countdownNowMs\n\} = usePaymentAutoCancelPage\(/g,
    `countdownNowMs,
  paymentAutoCancelRefs,
  formatDeadlineHint
} = usePaymentAutoCancelPage(`
  )
}

function replaceElTags(s) {
  return s.replace(
    /<el-tag\n\s+v-if="getPaymentCountdownInfo\((\w+)\)\.show"[\s\S]*?\{\{ getPaymentCountdownInfo\(\1\)\.text \}\}\n\s*<\/el-tag>/g,
    `<PaymentCountdownBadge
                v-if="getPaymentCountdownInfo($1).show"
                :info="getPaymentCountdownInfo($1)"
                size="small"
              />`
  )
}

function addPayConfirm(s) {
  return s.replace(
    /(cancelButtonText: '稍后支付')\n(\s+)\}\)\.then/g,
    `$1,\n$2  paymentOrder: $3,\n$2  paymentAutoCancel: paymentAutoCancelRefs\n$2}).then`
  )
}

function patchHandlePayBlocks(s) {
  const handlers = [
    ['handlePay(booking)', 'booking'],
    ['handlePay(course)', 'course'],
    ['handlePay(registration)', 'registration'],
    ['handlePayService(service)', 'service']
  ]
  for (const [, orderVar] of handlers) {
    if (!s.includes(`paymentOrder: ${orderVar}`)) {
      s = s.replace(
        new RegExp(`(openActionConfirm\\(\\{[\\s\\S]*?cancelButtonText: '稍后支付')\\n(\\s+)\\}\\)\\.then`, 'm'),
        (match, indent) => {
          if (match.includes('paymentAutoCancel')) return match
          return `${match},\n${indent}  paymentOrder: ${orderVar},\n${indent}  paymentAutoCancel: paymentAutoCancelRefs\n${indent}).then`
        }
      )
    }
  }
  return s
}

function addExpiredBtn(s) {
  if (s.includes('disabled\n            >\n              已超时')) return s
  return s.replace(
    /(@click="handlePay\(\w+\)"\n\s+>\n\s+立即支付\n\s+<\/el-button>)/g,
    `$1
                <el-button
                  v-else-if="$1"
                  type="info"
                  size="small"
                  disabled
                >
                  已超时
                </el-button>`.replace('$1', '') // broken - skip
  )
}

for (const file of userFiles) {
  let s = fs.readFileSync(file, 'utf8')
  const before = s
  s = addRefs(s)
  s = replaceElTags(s)
  s = patchHandlePayBlocks(s)
  if (file.endsWith('EquipmentRental.vue')) {
    if (!s.includes('PaymentPayCountdown')) {
      s = s.replace(
        "import PaymentCountdownBadge from '@/components/payment/PaymentCountdownBadge.vue'",
        `import PaymentCountdownBadge from '@/components/payment/PaymentCountdownBadge.vue'
import PaymentPayCountdown from '@/components/payment/PaymentPayCountdown.vue'`
      )
      s = s.replace(
        '<el-form-item label="租借单号">',
        `<el-form-item label="支付时限">
          <PaymentPayCountdown :order="currentPayRental" :countdown-state="paymentAutoCancelRefs" />
        </el-form-item>
        <el-form-item label="租借单号">`
      )
      s = s.replace(
        '<el-button type="primary" :loading="payLoading" @click="submitPay">确认支付</el-button>',
        '<el-button type="primary" :loading="payLoading" :disabled="currentPayRental && isPaymentExpired(currentPayRental)" @click="submitPay">确认支付</el-button>'
      )
    }
    if (!s.includes('orderSuccessMessage')) {
      s = s.replace(
        /\} = usePaymentAutoCancelPage\(\{[\s\S]*?\}\)\n/,
        (m) => `${m}
const orderSuccessMessage = (base) => {
  if (!autoCancelEnabled?.value) return base
  const hint = typeof formatDeadlineHint === 'function' ? formatDeadlineHint() : ''
  return hint ? \`\${base} \${hint}\` : base
}
`
      )
    }
    s = s.replace("ElMessage.success('租借成功！')", "ElMessage.success(orderSuccessMessage('租借成功！'))")
  }
  if (s !== before) {
    fs.writeFileSync(file, s, 'utf8')
    console.log('user', file.split(/[/\\]/).pop())
  }
}

for (const file of adminFiles) {
  let s = fs.readFileSync(file, 'utf8')
  const before = s
  s = addRefs(s)
  if (!s.includes('PaymentPayCountdown')) {
    if (!s.includes("import PaymentPayCountdown")) {
      s = s.replace(
        "import { openActionConfirm } from '@/utils/confirm'",
        `import PaymentPayCountdown from '@/components/payment/PaymentPayCountdown.vue'\nimport { openActionConfirm } from '@/utils/confirm'`
      )
    }
    s = s.replace(
      /(<el-dialog v-model="payDialogVisible"[\s\S]*?<el-form label-width="100px">\n)/,
      `$1        <el-form-item label="支付时限">
          <PaymentPayCountdown :order="currentPay" :countdown-state="paymentAutoCancelRefs" />
        </el-form-item>
`
    )
  }
  if (s !== before) fs.writeFileSync(file, s, 'utf8'), console.log('admin', file.split(/[/\\]/).pop())
}

console.log('final done')
