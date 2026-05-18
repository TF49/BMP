<template>
  <div class="my-orders-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">待支付订单</h2>
        <p class="page-desc">汇总场地、课程、器材、赛事、穿线等业务的待支付订单</p>
      </div>
      <el-button :icon="Refresh" :loading="loading" @click="loadPendingOrders">刷新</el-button>
    </div>

    <PaymentAutoCancelFeatureHint
      :config-loaded="configLoaded"
      :auto-cancel-enabled="autoCancelEnabled"
    />

    <div v-loading="loading" class="order-list">
      <div
        v-for="item in pendingOrders"
        :key="`${item.orderType}-${item.orderNo}`"
        class="order-card"
      >
        <div class="order-card__main">
          <div class="order-card__head">
            <el-tag size="small" type="warning">{{ item.title }}</el-tag>
            <span class="order-no">{{ item.orderNo }}</span>
          </div>
          <p v-if="item.subtitle" class="order-subtitle">{{ item.subtitle }}</p>
          <p class="order-amount">¥{{ formatCurrency(item.amount) }}</p>
          <PaymentCountdownBadge
            :info="getPaymentCountdownInfo(item.raw)"
            size="default"
          />
          <p class="order-time">下单时间：{{ formatDateTime(item.createTime) }}</p>
        </div>
        <div class="order-card__actions">
          <el-button
            v-if="!getPaymentCountdownInfo(item.raw).expired"
            type="primary"
            size="small"
            @click="goPay(item)"
          >
            去支付
          </el-button>
          <el-button v-else type="info" size="small" disabled>已超时</el-button>
        </div>
      </div>
      <el-empty v-if="!loading && pendingOrders.length === 0" description="暂无待支付订单" :image-size="120" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import PaymentCountdownBadge from '@/components/payment/PaymentCountdownBadge.vue'
import PaymentAutoCancelFeatureHint from '@/components/payment/PaymentAutoCancelFeatureHint.vue'
import { usePaymentAutoCancelPage } from '@/composables/usePaymentAutoCancel'
import {
  bindPaymentCountdownCacheClear,
  usePaymentCountdownListCache
} from '@/composables/usePaymentCountdownListCache'
import { fetchUserPendingPaymentOrders } from '@/utils/pendingUserOrders'
import { PAYMENT_ORDER_TYPES, useOrderStatusRefreshListener } from '@/utils/paymentOrderRefresh'

const router = useRouter()
const loading = ref(false)
const pendingOrders = ref([])

const paymentCountdownState = () => ({
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError
})

const {
  autoCancelEnabled,
  autoCancelTimeoutMinutes,
  countdownNowMs,
  configLoaded,
  configLoadError
} = usePaymentAutoCancelPage({
  hasExpiredPending: () => pendingOrders.value.some((item) => getPaymentCountdownInfo(item.raw).expired),
  refreshOnExpire: loadPendingOrders
})

const { getPaymentCountdownInfo, clearCache } = usePaymentCountdownListCache(paymentCountdownState)
bindPaymentCountdownCacheClear({ countdownNowMs }, clearCache)

const formatCurrency = (val) => {
  const num = Number(val)
  return Number.isFinite(num) ? num.toFixed(2) : '0.00'
}

const formatDateTime = (value) => {
  if (!value) return '-'
  const date = new Date(String(value).replace('T', ' '))
  if (Number.isNaN(date.getTime())) return String(value)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function loadPendingOrders() {
  loading.value = true
  try {
    pendingOrders.value = await fetchUserPendingPaymentOrders(20)
    clearCache()
  } catch (error) {
    console.error('加载待支付订单失败:', error)
    pendingOrders.value = []
  } finally {
    loading.value = false
  }
}

function goPay(item) {
  if (item?.route) {
    router.push(item.route)
  }
}

useOrderStatusRefreshListener(
  [
    PAYMENT_ORDER_TYPES.booking,
    PAYMENT_ORDER_TYPES.courseBooking,
    PAYMENT_ORDER_TYPES.equipmentRental,
    PAYMENT_ORDER_TYPES.tournamentRegistration,
    PAYMENT_ORDER_TYPES.stringingService
  ],
  () => {
    void loadPendingOrders()
  }
)

onMounted(() => {
  void loadPendingOrders()
})
</script>

<style scoped>
.my-orders-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.order-list {
  min-height: 200px;
}

.order-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  margin-bottom: 12px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
}

.order-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.order-no {
  font-size: 13px;
  color: #475569;
  font-weight: 600;
}

.order-subtitle,
.order-amount,
.order-time {
  margin: 4px 0;
  font-size: 13px;
  color: #64748b;
}

.order-amount {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.order-card__actions {
  flex-shrink: 0;
  padding-top: 8px;
}
</style>
