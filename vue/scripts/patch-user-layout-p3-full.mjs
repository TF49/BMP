import fs from 'fs'

const filePath = new URL('../src/layout/UserLayout.vue', import.meta.url)
let s = fs.readFileSync(filePath, 'utf8')

s = s.replace(
  "import { ref, computed, onMounted, onUnmounted } from 'vue'",
  "import { ref, computed, onMounted, onUnmounted, provide } from 'vue'"
)

if (!s.includes('ensurePaymentAutoCancelSession')) {
  s = s.replace(
    /import \{ getToken \} from '@\/utils\/auth'\r?\nimport \{ ElMessage \} from 'element-plus'/,
    `import { getToken } from '@/utils/auth'
import { dispatchOrderStatusRefresh } from '@/utils/paymentOrderRefresh'
import { ensurePaymentAutoCancelSession, PAYMENT_AUTO_CANCEL_KEY } from '@/composables/usePaymentAutoCancel'
import { ElMessage } from 'element-plus'

const paymentAutoCancelSession = ensurePaymentAutoCancelSession()
provide(PAYMENT_AUTO_CANCEL_KEY, paymentAutoCancelSession.state)`
  )
}

if (!s.includes("/user/orders'")) {
  s = s.replace(
    /(\{ path: '\/user\/dashboard', title: '[^']+', icon: Odometer \},)/,
    `$1\n  { path: '/user/orders', title: '待支付', icon: Clock },`
  )
}

if (!s.includes('  Clock,\n  Money,')) {
  s = s.replace(/  Trophy,\r?\n  Money,/, '  Trophy,\n  Clock,\n  Money,')
}

if (!s.includes('loadPaymentAutoCancelConfig')) {
  s = s.replace(
    /onMounted\(async \(\) => \{\r?\n  handleStorageChange\(\)/,
    'onMounted(async () => {\n  void paymentAutoCancelSession.loadPaymentAutoCancelConfig()\n  handleStorageChange()'
  )
  s = s.replace(
    /setOnOrderStatus\(\(payload\) => \{\r?\n    const title = payload\.title \|\| '[^']+'\r?\n    ElMessage\.success\(`\$\{title\}[^`]+`\)\r?\n  \}\)/,
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
}

fs.writeFileSync(filePath, s, 'utf8')
console.log('full patch', {
  session: s.includes('ensurePaymentAutoCancelSession'),
  orders: s.includes("/user/orders'"),
  clock: s.includes('  Clock,'),
  config: s.includes('loadPaymentAutoCancelConfig')
})
