import fs from 'fs'

const p = new URL('../src/layout/UserLayout.vue', import.meta.url)
let s = fs.readFileSync(p, 'utf8')

if (s.includes('ensurePaymentAutoCancelSession')) {
  console.log('already patched')
  process.exit(0)
}

s = s.replace(
  "import { ref, computed, onMounted, onUnmounted } from 'vue'",
  "import { ref, computed, onMounted, onUnmounted, provide } from 'vue'"
)
s = s.replace(
  "import { getToken } from '@/utils/auth'\nimport { ElMessage } from 'element-plus'",
  `import { getToken } from '@/utils/auth'
import { dispatchOrderStatusRefresh } from '@/utils/paymentOrderRefresh'
import { ensurePaymentAutoCancelSession, PAYMENT_AUTO_CANCEL_KEY } from '@/composables/usePaymentAutoCancel'
import { ElMessage } from 'element-plus'

const paymentAutoCancelSession = ensurePaymentAutoCancelSession()
provide(PAYMENT_AUTO_CANCEL_KEY, paymentAutoCancelSession.state)`
)
s = s.replace(/  Trophy,\n  Money,/, '  Trophy,\n  Clock,\n  Money,')
s = s.replace(
  /(\{ path: '\/user\/dashboard', title: '[^']+', icon: Odometer \},)/,
  `$1\n  { path: '/user/orders', title: '待支付', icon: Clock },`
)
s = s.replace(
  'onMounted(async () => {\n  handleStorageChange()',
  'onMounted(async () => {\n  void paymentAutoCancelSession.loadPaymentAutoCancelConfig()\n  handleStorageChange()'
)
s = s.replace(
  /setOnOrderStatus\(\(payload\) => \{\n    const title = payload\.title \|\| '[^']+'\n    ElMessage\.success\(`\$\{title\}[^`]+`\)\n  \}\)/,
  `setOnOrderStatus((payload) => {
    dispatchOrderStatusRefresh(payload)
    const title = payload.title || '订单'
    if (Number(payload.status) === 0) {
      ElMessage.warning(\`\${title}已取消：\${payload.statusText || '已超时自动取消'}\`)
      return
    }
    ElMessage.success(\`\${title}状态已更新：\${payload.statusText}\`)
  })`
)

fs.writeFileSync(p, s, 'utf8')
console.log('patched ok', s.includes('/user/orders'))
