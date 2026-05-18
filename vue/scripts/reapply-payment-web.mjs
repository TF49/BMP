import fs from 'fs'
import path from 'path'

const root = new URL('../src/', import.meta.url)

const adminPages = [
  'views/BookingManagement.vue',
  'views/CourseBookingManagement.vue',
  'views/EquipmentRentalManagement.vue',
  'views/StringingServiceManagement.vue',
  'views/TournamentRegistrationManagement.vue'
]

const userPages = [
  'views/user/Booking.vue',
  'views/user/CourseBooking.vue',
  'views/user/EquipmentRental.vue',
  'views/user/StringingService.vue',
  'views/user/TournamentRegistration.vue'
]

function patchFile(rel, opts = {}) {
  const filePath = new URL(rel, root)
  if (!fs.existsSync(filePath)) return
  let s = fs.readFileSync(filePath, 'utf8')
  let changed = false

  if (s.includes('usePaymentAutoCancel(') && !s.includes('usePaymentAutoCancelPage(')) {
    s = s.replace(
      /import \{([^}]*?)usePaymentAutoCancel([^}]*)\} from '@\/composables\/usePaymentAutoCancel'/,
      (m, a, b) => {
        const parts = `${a}usePaymentAutoCancelPage${b}`
        if (!parts.includes('buildPaymentCountdownOptions') && parts.includes('getPaymentAutoCancelInfo')) {
          return `import {${a}buildPaymentCountdownOptions, getPaymentAutoCancelInfo, usePaymentAutoCancelPage${b}} from '@/composables/usePaymentAutoCancel'`
        }
        return `import {${a}usePaymentAutoCancelPage${b}} from '@/composables/usePaymentAutoCancel'`
      }
    )
    s = s.replace(/\busePaymentAutoCancel\(/g, 'usePaymentAutoCancelPage(')
    changed = true
  }

  if (s.includes('loadPaymentAutoCancelConfig()')) {
    s = s.replace(/\s*loadPaymentAutoCancelConfig\(\)\s*\n/g, '\n')
    changed = true
  }

  if (opts.adminRefresh && !s.includes('useAdminOrdersRefreshListener')) {
    s = s.replace(
      "} from '@/composables/usePaymentAutoCancel'",
      "} from '@/composables/usePaymentAutoCancel'\nimport { useAdminOrdersRefreshListener } from '@/utils/paymentOrderRefresh'"
    )
    if (s.includes('onMounted(() => {') && !s.includes('useAdminOrdersRefreshListener(')) {
      s = s.replace(
        /onMounted\(\(\) => \{\n/,
        `onMounted(() => {\n  useAdminOrdersRefreshListener(() => {\n    void Promise.all([loadList(), loadStatistics?.() ?? Promise.resolve()])\n  })\n`
      )
    }
    changed = true
  }

  if (opts.listCache && !s.includes('usePaymentCountdownListCache')) {
    const entity = opts.entity || 'row'
    s = s.replace(
      /const getPaymentCountdownInfo = \((\w+)\) =>\s*\n\s*getPaymentAutoCancelInfo\(\1, buildPaymentCountdownOptions\(paymentCountdownState\(\)\)\)\nconst isPaymentExpired/,
      `const { getPaymentCountdownInfo: getCachedPaymentCountdown, clearCache: clearCountdownCache } = usePaymentCountdownListCache(paymentCountdownState)
bindPaymentCountdownCacheClear({ countdownNowMs }, clearCountdownCache)
const getPaymentCountdownInfo = (${entity}) => getCachedPaymentCountdown(${entity})
const isPaymentExpired`
    )
    if (!s.includes('usePaymentCountdownListCache')) {
      s = s.replace(
        "} from '@/composables/usePaymentAutoCancel'",
        `} from '@/composables/usePaymentAutoCancel'
import {
  bindPaymentCountdownCacheClear,
  usePaymentCountdownListCache
} from '@/composables/usePaymentCountdownListCache'`
      )
    }
    changed = true
  }

  if (opts.stringingCache && !s.includes('usePaymentCountdownListCache')) {
    s = s.replace(
      /const getPaymentCountdownInfo = \(service\) => \{[\s\S]*?\}\nconst isPaymentExpired = \(service\)/,
      `const { getPaymentCountdownInfo: getCachedPaymentCountdown, clearCache: clearCountdownCache } =
  usePaymentCountdownListCache(paymentCountdownState)
bindPaymentCountdownCacheClear({ countdownNowMs }, clearCountdownCache)
const getPaymentCountdownInfo = (service) => {
  if (!shouldShowStringingPaymentCountdown(service)) {
    return EMPTY_PAYMENT_COUNTDOWN_INFO
  }
  return getCachedPaymentCountdown(service)
}
const isPaymentExpired = (service)`
    )
    s = s.replace(
      "} from '@/composables/usePaymentAutoCancel'",
      `} from '@/composables/usePaymentAutoCancel'
import {
  bindPaymentCountdownCacheClear,
  usePaymentCountdownListCache
} from '@/composables/usePaymentCountdownListCache'`
    )
    changed = true
  }

  if (opts.userRefresh && !s.includes('useOrderStatusRefreshListener')) {
    const type = opts.orderType || 'booking'
    s = s.replace(
      "} from '@/composables/usePaymentAutoCancel'",
      `} from '@/composables/usePaymentAutoCancel'
import { PAYMENT_ORDER_TYPES, useOrderStatusRefreshListener } from '@/utils/paymentOrderRefresh'`
    )
    if (!s.includes('useOrderStatusRefreshListener(')) {
      s = s.replace(
        /onMounted\(\(\) => \{\n/,
        `useOrderStatusRefreshListener(PAYMENT_ORDER_TYPES.${type}, () => {\n  void loadList()\n})\n\nonMounted(() => {\n`
      )
    }
    changed = true
  }

  if (changed) {
    fs.writeFileSync(filePath, s, 'utf8')
    console.log('patched', rel)
  }
}

// Admin layout session
const adminLayout = new URL('../src/layout/AdminLayout.vue', import.meta.url)
let al = fs.readFileSync(adminLayout, 'utf8')
if (!al.includes('ensurePaymentAutoCancelSession')) {
  al = al.replace(
    "import { ref, computed, onMounted, onUnmounted } from 'vue'",
    "import { ref, computed, onMounted, onUnmounted, provide } from 'vue'"
  )
  al = al.replace(
    /import \{ getToken \} from '@\/utils\/auth'\r?\nimport \{ getOperationTodoStatistics \} from '@\/api\/booking'/,
    `import { getToken } from '@/utils/auth'
import { ensurePaymentAutoCancelSession, PAYMENT_AUTO_CANCEL_KEY } from '@/composables/usePaymentAutoCancel'
import { getOperationTodoStatistics } from '@/api/booking'

const paymentAutoCancelSession = ensurePaymentAutoCancelSession()
provide(PAYMENT_AUTO_CANCEL_KEY, paymentAutoCancelSession.state)`
  )
  al = al.replace(
    /onMounted\(async \(\) => \{\r?\n  handleStorageChange\(\)/,
    'onMounted(async () => {\n  void paymentAutoCancelSession.loadPaymentAutoCancelConfig()\n  handleStorageChange()'
  )
  fs.writeFileSync(adminLayout, al, 'utf8')
  console.log('patched AdminLayout')
}

adminPages.forEach((f) => patchFile(f, { adminRefresh: true, listCache: true, entity: f.includes('Course') ? 'booking' : f.includes('Equipment') ? 'rental' : f.includes('Tournament') ? 'registration' : f.includes('Stringing') ? 'service' : 'booking' }))
patchFile('views/StringingServiceManagement.vue', { adminRefresh: true, stringingCache: true })

userPages.forEach((f) => {
  const type = f.includes('Course') ? 'courseBooking' : f.includes('Equipment') ? 'equipmentRental' : f.includes('Tournament') ? 'tournamentRegistration' : f.includes('Stringing') ? 'stringingService' : 'booking'
  patchFile(f, { userRefresh: true, orderType: type })
})

console.log('reapply done')
