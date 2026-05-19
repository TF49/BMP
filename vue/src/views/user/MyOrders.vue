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
          <p class="order-time">下单时间：{{ formatDateTime(item.createTime) }}</p>
          <PaymentCountdownBadge
            v-if="getPaymentCountdownInfo(item.raw).show"
            :info="getPaymentCountdownInfo(item.raw)"
            size="small"
            class="order-countdown"
          />
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
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import PaymentAutoCancelFeatureHint from '@/components/payment/PaymentAutoCancelFeatureHint.vue'
import PaymentCountdownBadge from '@/components/payment/PaymentCountdownBadge.vue'
import { usePaymentAutoCancelPage } from '@/composables/usePaymentAutoCancel'
import {
  bindPaymentCountdownCacheClear,
  usePaymentCountdownListCache
} from '@/composables/usePaymentCountdownListCache'
import { openActionConfirm } from '@/utils/confirm'
import { payMemberBooking } from '@/api/booking'
import { payMemberCourseBooking } from '@/api/courseBooking'
import { processEquipmentRentalPayment } from '@/api/equipmentRental'
import { processMemberTournamentRegistrationPayment } from '@/api/tournamentRegistration'
import { processMemberStringingPayment } from '@/api/stringing'
import { fetchUserPendingPaymentOrders } from '@/utils/pendingUserOrders'
import { PAYMENT_ORDER_TYPES, useOrderStatusRefreshListener } from '@/utils/paymentOrderRefresh'

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
  configLoadError,
  paymentAutoCancelRefs
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

function getPayConfirmMeta(item) {
  const amountText = `¥${formatCurrency(item?.amount)}`
  switch (item?.orderType) {
    case PAYMENT_ORDER_TYPES.booking:
      return {
        title: '预约支付确认',
        message: '确认使用余额支付这笔场地预约订单吗？',
        detail: `订单金额：${amountText}`,
        entityLabel: '预约单号'
      }
    case PAYMENT_ORDER_TYPES.courseBooking:
      return {
        title: '课程支付确认',
        message: '确认使用余额支付这笔课程订单吗？',
        detail: `订单金额：${amountText}`,
        entityLabel: '课程单号'
      }
    case PAYMENT_ORDER_TYPES.equipmentRental:
      return {
        title: '器材租借支付确认',
        message: '确认使用余额支付这笔器材租借订单吗？',
        detail: `订单金额：${amountText}`,
        entityLabel: '租借单号'
      }
    case PAYMENT_ORDER_TYPES.tournamentRegistration:
      return {
        title: '赛事报名支付确认',
        message: '确认使用余额支付这笔赛事报名吗？',
        detail: `报名费用：${amountText}`,
        entityLabel: '报名编号'
      }
    case PAYMENT_ORDER_TYPES.stringingService:
      return {
        title: '穿线服务支付确认',
        message: '确认使用余额支付这笔穿线服务吗？',
        detail: `服务金额：${amountText}`,
        entityLabel: '服务单号'
      }
    default:
      return {
        title: '支付确认',
        message: '确认支付这笔订单吗？',
        detail: `订单金额：${amountText}`,
        entityLabel: '订单编号'
      }
  }
}

async function submitPendingOrderPayment(item) {
  switch (item?.orderType) {
    case PAYMENT_ORDER_TYPES.booking:
      return payMemberBooking({
        bookingId: item.raw.id,
        paymentMethod: 'BALANCE'
      })
    case PAYMENT_ORDER_TYPES.courseBooking:
      return payMemberCourseBooking({
        bookingId: item.raw.id,
        paymentMethod: 'BALANCE'
      }, {
        skipErrorMessage: true
      })
    case PAYMENT_ORDER_TYPES.equipmentRental:
      return processEquipmentRentalPayment(item.raw.id, 'BALANCE')
    case PAYMENT_ORDER_TYPES.tournamentRegistration:
      return processMemberTournamentRegistrationPayment(item.raw.id, 'BALANCE')
    case PAYMENT_ORDER_TYPES.stringingService:
      return processMemberStringingPayment(item.raw.id, 'BALANCE')
    default:
      throw new Error('暂不支持该订单类型的支付')
  }
}

async function goPay(item) {
  if (!item?.raw) return
  if (getPaymentCountdownInfo(item.raw).expired) {
    ElMessage.warning('该订单已超过支付时限，系统正在自动取消，请稍后刷新')
    await loadPendingOrders()
    return
  }

  const meta = getPayConfirmMeta(item)

  openActionConfirm({
    title: meta.title,
    eyebrow: 'BALANCE PAYMENT',
    message: meta.message,
    detail: meta.detail,
    entityLabel: meta.entityLabel,
    entityValue: item.orderNo,
    tone: 'warning',
    confirmButtonText: '确认支付',
    cancelButtonText: '稍后支付',
    paymentOrder: item.raw,
    paymentAutoCancel: paymentAutoCancelRefs
  }).then(async () => {
    if (getPaymentCountdownInfo(item.raw).expired) {
      ElMessage.warning('该订单已超过支付时限，系统正在自动取消，请稍后刷新')
      await loadPendingOrders()
      return
    }
    try {
      const res = await submitPendingOrderPayment(item)
      if (res.code === 200) {
        ElMessage.success('支付成功')
        await loadPendingOrders()
      } else {
        ElMessage.error(res.message || '支付失败')
      }
    } catch (error) {
      console.error('待支付订单支付失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '支付失败，请稍后重试')
    }
  }).catch(() => {})
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
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
  padding-top: 8px;
}

.order-countdown {
  margin-top: 8px;
}
</style>