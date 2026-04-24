<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="nav-left" @tap="handleBack">
          <view class="icon-btn">
            <uni-icons type="left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">穿线详情</text>
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
    >
      <view class="content">
        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载工单详情…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadDetail">重新加载</view>
        </view>

        <template v-else-if="detail">
          <view class="hero-card">
            <text class="hero-label">当前状态</text>
            <text class="hero-value">{{ statusLabel }}</text>
            <text class="hero-sub">{{ detail.serviceNo || `工单 #${detail.id}` }}</text>
          </view>

          <view class="section-card">
            <text class="section-title">工单信息</text>
            <view class="field-row">
              <text class="field-label">提交时间</text>
              <text class="field-value">{{ formatDateTime(detail.createTime) || '未知时间' }}</text>
            </view>
            <view class="field-row">
              <text class="field-label">客户</text>
              <text class="field-value">{{ memberLabel }}</text>
            </view>
            <view class="field-row">
              <text class="field-label">手机号</text>
              <text class="field-value">{{ formatPhone(detail.memberPhone || '') || '未绑定手机号' }}</text>
            </view>
            <view class="field-row">
              <text class="field-label">支付状态</text>
              <text class="field-value">{{ paymentStatusLabel }}</text>
            </view>
            <view class="field-row">
              <text class="field-label">当前余额</text>
              <text class="field-value">¥{{ memberBalanceText }}</text>
            </view>
          </view>

          <view class="section-card">
            <text class="section-title">穿线信息</text>
            <view class="field-row">
              <text class="field-label">球拍型号</text>
              <text class="field-value">{{ racketLabel }}</text>
            </view>
            <view class="field-row">
              <text class="field-label">线材</text>
              <text class="field-value">{{ stringLabel }}</text>
            </view>
            <view class="field-row">
              <text class="field-label">磅数</text>
              <text class="field-value">{{ tensionLabel }}</text>
            </view>
            <view class="field-row">
              <text class="field-label">穿线方式</text>
              <text class="field-value">{{ methodLabel }}</text>
            </view>
            <view class="field-row">
              <text class="field-label">服务费用</text>
              <text class="field-value amount">¥{{ formatAmount(detail.totalPrice || detail.servicePrice || 0) }}</text>
            </view>
            <view class="field-row multiline">
              <text class="field-label">备注</text>
              <text class="field-value remark">{{ detail.remark || '无特殊备注' }}</text>
            </view>
          </view>

          <view class="action-row">
            <view v-if="showPayButton" class="action-btn pay" @tap="handlePay">
              余额支付
            </view>
            <view v-if="showCancelButton" class="action-btn secondary" @tap="handleCancel">
              取消服务
            </view>
            <view class="action-btn primary" @tap="handleReorder">
              再次预约
            </view>
          </view>
        </template>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  cancelStringing,
  getStringingDetail,
  getStringingPound,
  getStringingStringLabel,
  processMemberStringingPayment,
  type StringingService
} from '@/api/stringing'
import { useCurrentMember } from '@/composables/useCurrentMember'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { PAYMENT_METHOD_TEXT, STRINGING_STATUS } from '@/utils/constant'

const userStore = useUserStore()
const { currentMember, fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const serviceId = ref(0)
const loading = ref(false)
const errorText = ref('')
const detail = ref<StringingService | null>(null)

const statusLabel = computed(() => {
  const status = Number(detail.value?.status ?? -1)
  if (status === STRINGING_STATUS.CANCELLED) return '已取消'
  if (status === STRINGING_STATUS.WAITING) return '等待穿线'
  if (status === STRINGING_STATUS.IN_PROGRESS) return '正在穿线'
  if (status === STRINGING_STATUS.COMPLETED) return '已完成'
  return '未知状态'
})

const paymentStatusLabel = computed(() => {
  const paymentStatus = Number(detail.value?.paymentStatus ?? 0)
  if (paymentStatus === 1) return '已支付'
  if (paymentStatus === 2) return '已退款'
  return '待支付'
})

const memberBalanceText = computed(() => Number(currentMember.value?.balance || 0).toFixed(2))

const memberLabel = computed(() => {
  if (!detail.value) return '未知客户'
  return detail.value.memberName || detail.value.userName || userStore.userInfo?.username || `会员 #${detail.value.memberId || '-'}`
})

const racketLabel = computed(() => {
  if (!detail.value) return '未知球拍'
  return [detail.value.racketBrand, detail.value.racketModel].filter(Boolean).join(' ') || '未知球拍'
})

const tensionLabel = computed(() => {
  const tension = detail.value ? getStringingPound(detail.value) : undefined
  if (tension === undefined || tension === null || tension === '') return '未知磅数'
  return `${String(tension).replace(/\.0$/, '')} lbs`
})

const stringLabel = computed(() => {
  return getStringingStringLabel(detail.value)
})

const methodLabel = computed(() => {
  const method = detail.value?.stringingMethod
  if (method === 'TWO_SECTION') return '两结'
  if (method === 'FOUR_SECTION') return '四结'
  if (method === 'AUTO') return '自动判断'
  return method || '未知方式'
})

const showCancelButton = computed(() => {
  if (!detail.value) return false
  const status = Number(detail.value?.status ?? -1)
  const paymentStatus = Number(detail.value?.paymentStatus ?? 0)
  return status === STRINGING_STATUS.WAITING && paymentStatus !== 1 && paymentStatus !== 2
})

const showPayButton = computed(() => {
  if (!detail.value) return false
  return Number(detail.value.status ?? -1) === STRINGING_STATUS.WAITING && Number(detail.value.paymentStatus ?? 0) === 0
})

function formatDateTime(value?: string) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}`
}

function formatPhone(phone: string) {
  if (!phone) return ''
  return phone
}

function formatAmount(value: number) {
  return (Math.round(Number(value || 0) * 100) / 100).toFixed(2)
}

async function loadDetail() {
  if (!serviceId.value) {
    errorText.value = '缺少工单参数'
    loading.value = false
    return
  }
  loading.value = true
  errorText.value = ''
  try {
    const [serviceDetail] = await Promise.all([getStringingDetail(serviceId.value), fetchCurrentMember(true)])
    detail.value = serviceDetail
  } catch (error) {
    console.error('加载穿线详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载穿线详情失败'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/stringing/list')
}

async function handleCancel() {
  if (!detail.value) return
  uni.showModal({
    title: '提示',
    content: '确定要取消该服务吗？',
    success: async (res) => {
      if (!res.confirm || !detail.value) return
      try {
        await cancelStringing(detail.value.id)
        uni.showToast({ title: '取消成功', icon: 'success' })
        await loadDetail()
      } catch (error) {
        console.error('取消服务失败:', error)
        uni.showToast({ title: error instanceof Error ? error.message : '取消失败', icon: 'none' })
      }
    }
  })
}

function handlePay() {
  if (!detail.value || !showPayButton.value) return
  const amount = formatAmount(detail.value.totalPrice || detail.value.servicePrice || 0)
  uni.showModal({
    title: '确认余额支付',
    content: `将使用本人余额支付穿线服务。\n当前余额：¥${memberBalanceText.value}\n支付金额：¥${amount}`,
    success: async (res) => {
      if (!res.confirm || !detail.value) return
      try {
        uni.showLoading({ title: '支付中...' })
        await processMemberStringingPayment(detail.value.id, 'BALANCE')
        await Promise.all([loadDetail(), fetchCurrentMember(true)])
        uni.hideLoading()
        uni.showToast({
          title: `${PAYMENT_METHOD_TEXT.BALANCE}支付完成`,
          icon: 'success'
        })
      } catch (error) {
        console.error('穿线支付失败:', error)
        uni.hideLoading()
        uni.showToast({
          title: error instanceof Error ? error.message : '支付失败',
          icon: 'none'
        })
      }
    }
  })
}

function handleReorder() {
  uni.navigateTo({ url: '/pages/stringing/create' })
}

onLoad(async (options?: Record<string, string | undefined>) => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  serviceId.value = Number(options?.id || 0)
  await loadDetail()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(16px);
}

.header-inner {
  min-height: 112rpx;
  padding: 12rpx 28rpx 20rpx;
  display: flex;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 20rpx 18rpx 80rpx;
}

.hero-card,
.section-card,
.state-card {
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05);
}

.hero-card {
  padding: 30rpx 28rpx;
  background: linear-gradient(135deg, #fff3ec 0%, #ffffff 100%);
}

.hero-label {
  display: block;
  font-size: 22rpx;
  color: #6b7280;
  font-weight: 800;
}

.hero-value {
  display: block;
  margin-top: 14rpx;
  font-size: 52rpx;
  line-height: 1.1;
  font-weight: 900;
  color: #111111;
}

.hero-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #5f5e5e;
}

.section-card {
  margin-top: 20rpx;
  padding: 28rpx 26rpx;
}

.section-title {
  display: block;
  font-size: 32rpx;
  font-weight: 900;
  color: #111111;
}

.field-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
}

.field-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.field-row.multiline {
  align-items: flex-start;
}

.field-label {
  min-width: 160rpx;
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 700;
}

.field-value {
  flex: 1;
  text-align: right;
  font-size: 26rpx;
  color: #111111;
  font-weight: 700;
  line-height: 1.6;
}

.field-value.amount {
  color: #a33e00;
  font-size: 30rpx;
}

.field-value.remark {
  color: #475569;
}

.action-row {
  margin-top: 24rpx;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14rpx;
}

.action-btn {
  height: 88rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 900;
}

.action-btn.secondary {
  background: #ffffff;
  color: #ba1a1a;
  box-shadow: 0 8rpx 20rpx rgba(26, 28, 28, 0.04);
}

.action-btn.pay {
  background: #fff3eb;
  color: #a33e00;
  box-shadow: 0 8rpx 20rpx rgba(163, 62, 0, 0.08);
}

.action-btn.primary {
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  box-shadow: 0 8rpx 20rpx rgba(163, 62, 0, 0.2);
}

.state-card {
  padding: 90rpx 28rpx;
  text-align: center;
}

.state-text {
  font-size: 28rpx;
  color: #777777;
}

.state-action {
  width: 220rpx;
  height: 76rpx;
  margin: 22rpx auto 0;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  margin: 0 auto 18rpx;
  border: 4rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 9999rpx;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
