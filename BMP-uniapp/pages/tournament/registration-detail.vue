<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="header-inner">
        <view class="icon-btn" @tap="handleBack">
          <uni-icons type="left" size="20" color="#1a1c1c" />
        </view>
        <text class="header-title">报名详情</text>
        <view class="header-tag">RESULT</view>
      </view>
    </view>

    <scroll-view scroll-y class="main" :style="{ paddingTop: `${statusBarHeight + 64}px` }">
      <view class="content">
        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载报名信息…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadDetail">重新加载</view>
        </view>

        <template v-else-if="detail">
          <view class="hero-card">
            <text class="hero-kicker">TOURNAMENT REGISTRATION</text>
            <text class="hero-title">{{ detail.tournamentName || '赛事报名' }}</text>
            <text class="hero-sub">{{ detail.registrationNo || `报名 #${detail.id}` }}</text>
            <view class="hero-grid">
              <view class="hero-cell">
                <text class="cell-label">报名状态</text>
                <text class="cell-value">{{ statusText }}</text>
              </view>
              <view class="hero-cell">
                <text class="cell-label">支付状态</text>
                <text class="cell-value">{{ paymentStatusText }}</text>
              </view>
            </view>
          </view>

          <view class="card">
            <view class="section-head">
              <text class="section-title">支付信息</text>
              <text class="section-note">业务订单仅支持余额支付</text>
            </view>

            <view class="row">
              <text class="label">报名费</text>
              <text class="value amount">¥{{ amountText }}</text>
            </view>
            <view class="row">
              <text class="label">当前余额</text>
              <text class="value">¥{{ memberBalanceText }}</text>
            </view>
            <view class="row">
              <text class="label">支付方式</text>
              <text class="value">{{ paymentMethodText }}</text>
            </view>
          </view>

          <view class="card">
            <text class="section-title">报名信息</text>

            <view class="row">
              <text class="label">报名人</text>
              <text class="value">{{ detail.memberName || `会员 #${detail.memberId || '-'}` }}</text>
            </view>
            <view v-if="detail.partnerName" class="row">
              <text class="label">搭档</text>
              <text class="value">{{ detail.partnerName }}</text>
            </view>
            <view class="row">
              <text class="label">场馆</text>
              <text class="value">{{ detail.venueName || '待补充' }}</text>
            </view>
            <view class="row">
              <text class="label">赛事时间</text>
              <text class="value">{{ tournamentTimeText }}</text>
            </view>
            <view class="row">
              <text class="label">提交时间</text>
              <text class="value">{{ formatDateTime(detail.createTime) || '未知时间' }}</text>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>

    <view v-if="detail && showPayButton" class="footer">
      <view class="footer-left">
        <text class="footer-label">待支付金额</text>
        <text class="footer-value">¥{{ amountText }}</text>
      </view>
      <button class="footer-btn" :class="{ disabled: submitting }" @tap="handlePayment">
        {{ submitting ? '处理中...' : '本人余额支付' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getTournamentRegistrationDetail,
  processMemberTournamentRegistrationPayment,
  type TournamentRegistrationItem
} from '@/api/tournament'
import { useCurrentMember } from '@/composables/useCurrentMember'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { PAYMENT_METHOD_TEXT } from '@/utils/constant'
import { formatAmount, formatDateTime } from '@/utils/format'

const userStore = useUserStore()
const { currentMember, fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(20)
const loading = ref(true)
const errorText = ref('')
const submitting = ref(false)
const registrationId = ref(0)
const tournamentId = ref(0)
const detail = ref<TournamentRegistrationItem | null>(null)

const amountText = computed(() => formatAmount(detail.value?.entryFee || 0))
const memberBalanceText = computed(() => formatAmount(currentMember.value?.balance || 0))
const paymentMethodText = computed(() => PAYMENT_METHOD_TEXT.BALANCE)

const statusText = computed(() => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '已支付',
    3: '已参赛',
    4: '已退赛'
  }
  return statusMap[Number(detail.value?.status ?? 1)] || '未知状态'
})

const paymentStatusText = computed(() => {
  const paymentStatus = Number(detail.value?.paymentStatus ?? 0)
  if (paymentStatus === 1) return '已支付'
  if (paymentStatus === 2) return '已退款'
  return '待支付'
})

const tournamentTimeText = computed(() => {
  const current = detail.value
  if (!current) return '未知赛事时间'
  const start = formatDateTime(current.tournamentStartTime || current.tournamentStart)
  const end = formatDateTime(current.tournamentEndTime || current.tournamentEnd)
  if (start && end) return `${start} - ${end}`
  return start || end || '未知赛事时间'
})

const showPayButton = computed(() => {
  if (!detail.value) return false
  const paymentStatus = Number(detail.value.paymentStatus ?? 0)
  return Number(detail.value.status ?? -1) === 1 && paymentStatus !== 1 && paymentStatus !== 2
})

async function loadDetail() {
  if (!registrationId.value) {
    errorText.value = '缺少报名参数'
    loading.value = false
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    const [registration] = await Promise.all([
      getTournamentRegistrationDetail(registrationId.value),
      fetchCurrentMember(true)
    ])
    detail.value = registration
  } catch (error) {
    console.error('加载报名详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载报名详情失败'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  if (tournamentId.value) {
    safeNavigateBack(`/pages/tournament/detail?id=${tournamentId.value}`)
    return
  }
  safeNavigateBack('/pages/tournament/list')
}

async function handlePayment() {
  if (!detail.value || submitting.value || !showPayButton.value) return

  uni.showModal({
    title: '确认本人余额支付',
    content: `将使用本人余额支付赛事报名费用。\n当前余额：¥${memberBalanceText.value}\n支付金额：¥${amountText.value}`,
    success: async (res) => {
      if (!res.confirm || !detail.value) return
      try {
        submitting.value = true
        uni.showLoading({ title: '余额支付中...' })
        await processMemberTournamentRegistrationPayment(detail.value.id, 'BALANCE')
        await Promise.all([loadDetail(), fetchCurrentMember(true)])
        uni.hideLoading()
        uni.showToast({ title: '余额支付成功', icon: 'success' })
      } catch (error) {
        console.error('赛事报名支付失败:', error)
        uni.hideLoading()
        uni.showToast({
          title: error instanceof Error ? error.message : '支付失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        submitting.value = false
      }
    }
  })
}

onLoad((options?: Record<string, string | undefined>) => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 20

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  registrationId.value = Number(options?.registrationId || options?.id || 0)
  tournamentId.value = Number(options?.tournamentId || 0)
  void loadDetail()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.header {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  z-index: 30;
  background: rgba(249, 249, 249, 0.94);
  backdrop-filter: blur(18px);
}

.header-inner {
  min-height: 64px;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 34rpx;
  font-weight: 900;
}

.header-tag {
  min-width: 96rpx;
  padding: 10rpx 16rpx;
  border-radius: 9999rpx;
  background: #fff1e8;
  color: #a33e00;
  font-size: 20rpx;
  font-weight: 800;
  text-align: center;
}

.main {
  height: 100vh;
}

.content {
  padding: 24rpx 20rpx 220rpx;
}

.hero-card,
.card,
.state-card {
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 12rpx 30rpx rgba(26, 28, 28, 0.04);
}

.hero-card {
  padding: 32rpx 28rpx;
  background: linear-gradient(135deg, #241714 0%, #59372d 58%, #9e430b 100%);
}

.hero-kicker,
.cell-label,
.section-note {
  font-size: 20rpx;
  color: rgba(255, 241, 234, 0.74);
}

.hero-title {
  display: block;
  margin-top: 12rpx;
  font-size: 44rpx;
  font-weight: 900;
  color: #ffffff;
}

.hero-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: rgba(255, 241, 234, 0.86);
}

.hero-grid {
  margin-top: 22rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.hero-cell {
  padding: 20rpx;
  border-radius: 22rpx;
  background: rgba(255, 255, 255, 0.12);
}

.cell-value {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  font-weight: 800;
  color: #ffffff;
}

.card {
  margin-top: 22rpx;
  padding: 28rpx 26rpx;
}

.section-head,
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 900;
}

.row {
  margin-top: 18rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid #f1f1f1;
}

.label {
  color: #6b7280;
  font-size: 24rpx;
}

.value {
  font-size: 26rpx;
  font-weight: 700;
  text-align: right;
}

.value.amount {
  color: #a33e00;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  padding: 18rpx 24rpx calc(env(safe-area-inset-bottom) + 18rpx);
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(16px);
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.footer-left {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.footer-label {
  font-size: 20rpx;
  color: #6b7280;
}

.footer-value {
  margin-top: 6rpx;
  font-size: 42rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.footer-btn {
  min-width: 280rpx;
  height: 92rpx;
  border: none;
  border-radius: 9999rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 900;
}

.footer-btn.disabled {
  opacity: 0.45;
}

.state-card {
  margin-top: 24rpx;
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
