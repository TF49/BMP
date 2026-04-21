<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="header-left">
          <view class="icon-btn" @tap="handleBack">
            <uni-icons type="left" size="22" color="#ea580c" />
          </view>
          <text class="header-title">充值中心</text>
        </view>
        <image class="header-avatar" :src="avatarUrl" mode="aspectFill" />
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="content">
        <view v-if="loading && !member" class="state-card">
          <view class="spinner" />
          <text class="state-text">加载账户信息中…</text>
        </view>

        <view v-else-if="!loading && !member" class="state-card">
          <text class="state-text">无法加载会员信息，请返回重试</text>
          <view class="state-action" @tap="loadMember">重新加载</view>
        </view>

        <template v-else-if="member">
          <view class="identity-card">
            <view class="card-bg-icon">
              <uni-icons type="wallet" size="120" color="rgba(0,0,0,0.12)" />
            </view>
            <view class="identity-top">
              <view>
                <text class="identity-label">Current Member</text>
                <view class="identity-name-row">
                  <text class="identity-name">{{ member.memberName || displayName }}</text>
                  <text class="identity-id">#{{ member.id }}</text>
                </view>
              </view>
              <text class="vip-pill" :class="{ muted: member.memberType !== 'MEMBER' }">
                {{ member.memberType === 'MEMBER' ? 'VIP CLASS' : 'NORMAL' }}
              </text>
            </view>
            <view class="balance-block">
              <text class="balance-label">Available Balance</text>
              <view class="balance-row">
                <text class="currency">¥</text>
                <text class="balance-num">{{ formatBalance(member.balance) }}</text>
              </view>
            </view>
          </view>

          <view class="section-card">
            <view class="section-head">
              <text class="section-title">选择金额</text>
              <text class="reward-tag">充值奖励 +10%</text>
            </view>

            <view class="amount-grid">
              <view
                v-for="(amt, idx) in presetAmounts"
                :key="amt"
                class="amount-cell"
                :class="{ active: isPresetActive(idx) }"
                @tap="selectPreset(idx)"
              >
                <text class="amount-yen">¥</text>
                <text class="amount-val">{{ amt }}</text>
              </view>
            </view>

            <view class="custom-wrap">
              <text class="custom-yen">¥</text>
              <input
                v-model="customAmount"
                class="custom-input"
                type="digit"
                placeholder="其他金额"
                @input="onCustomInput"
              />
            </view>
          </view>

          <view class="section-card">
            <text class="section-title solo">支付方式</text>
            <view class="pay-list">
              <view
                v-for="(m, i) in payMethods"
                :key="m.code"
                class="pay-row"
                @tap="selectedPay = i"
              >
                <view class="pay-left">
                  <view class="pay-icon" :class="m.iconBg">
                    <uni-icons :type="m.icon" size="24" :color="m.iconColor" />
                  </view>
                  <view>
                    <text class="pay-title">{{ m.title }}</text>
                    <text class="pay-sub">{{ m.sub }}</text>
                  </view>
                </view>
                <uni-icons
                  :type="selectedPay === i ? 'checkbox-filled' : 'circle'"
                  size="22"
                  :color="selectedPay === i ? '#ea580c' : '#d4d4d8'"
                />
              </view>
            </view>
          </view>

          <view class="section-card promotion-card">
            <text class="promotion-title">充值优惠</text>
            <text class="promotion-desc">充值满100送10，满200送25，多充多送。</text>
            <text class="promotion-link" @tap="goRecords">查看充值记录</text>
          </view>
        </template>
      </view>
    </scroll-view>

    <view v-if="member" class="bottom-bar">
      <view class="sum-row">
        <text class="sum-label">充值总计</text>
        <text class="sum-val">¥ {{ formatMoney(totalAmount) }}</text>
      </view>
      <view class="submit-btn" :class="{ disabled: submitting || totalAmount <= 0 }" @tap="onConfirm">
        <uni-icons type="bolt" size="22" color="#561d00" />
        <text class="submit-text">{{ submitting ? '充值中...' : '确认充值' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { createRechargeOrder } from '@/api/recharge'
import type { MemberInfo } from '@/api/member'
import { useUserStore } from '@/store/modules/user'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { safeNavigateBack } from '@/utils/navigation'
import { getAvatarImage } from '@/utils/displayImage'
import { useCurrentMember } from '@/composables/useCurrentMember'

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const refreshing = ref(false)
const loading = ref(true)
const submitting = ref(false)
const member = ref<MemberInfo | null>(null)

const presetAmounts = [100, 500, 1000, 2000, 5000, 10000]
const selectedPreset = ref(1)
const useCustom = ref(false)
const customAmount = ref('')
const selectedPay = ref(0)

const payMethods = [
  { code: 'WECHAT' as const, title: '微信支付', sub: '微信支付', icon: 'weixin', iconBg: 'green', iconColor: '#16a34a' },
  { code: 'ALIPAY' as const, title: '支付宝', sub: '支付宝', icon: 'compose', iconBg: 'blue', iconColor: '#2563eb' },
  { code: 'BANK' as const, title: '银行卡', sub: '银行卡支付', icon: 'wallet', iconBg: 'slate', iconColor: '#475569' }
]

const avatarUrl = computed(() => getAvatarImage(userStore.userInfo?.avatar))
const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '会员')

const totalAmount = computed(() => {
  if (useCustom.value && customAmount.value.trim()) {
    const n = parseFloat(customAmount.value)
    return Number.isFinite(n) && n > 0 ? n : 0
  }
  return presetAmounts[selectedPreset.value] ?? 0
})

function isPresetActive(idx: number) {
  return !useCustom.value && selectedPreset.value === idx
}

function selectPreset(idx: number) {
  selectedPreset.value = idx
  useCustom.value = false
  customAmount.value = ''
}

function onCustomInput() {
  useCustom.value = true
}

function formatBalance(v: number | undefined) {
  if (v == null || Number.isNaN(Number(v))) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatMoney(v: number) {
  return v.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadMember() {
  loading.value = true
  try {
    member.value = await fetchCurrentMember(true)
  } catch (error) {
    console.error('加载会员信息失败:', error)
    member.value = null
  } finally {
    loading.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/profile/index')
}

function goRecords() {
  uni.navigateTo({ url: '/pages/recharge/records' })
}

async function onConfirm() {
  if (submitting.value || !member.value || totalAmount.value <= 0) {
    if (totalAmount.value <= 0) {
      uni.showToast({ title: '请选择或输入充值金额', icon: 'none' })
    }
    return
  }

  submitting.value = true
  try {
    await createRechargeOrder({
      memberId: member.value.id,
      amount: totalAmount.value,
      method: payMethods[selectedPay.value].code,
      paymentMethod: payMethods[selectedPay.value].code,
      orderType: 'RECHARGE'
    })
    uni.showToast({ title: '充值成功', icon: 'success' })
    await loadMember()
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/recharge/records' })
    }, 900)
  } catch (error) {
    console.error('充值失败:', error)
    uni.showToast({ title: '充值失败，请稍后重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function handleRefresh() {
  refreshing.value = true
  loadMember().finally(() => {
    refreshing.value = false
  })
}

onLoad(async () => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  await loadMember()
})

onPullDownRefresh(() => {
  loadMember().finally(() => {
    uni.stopPullDownRefresh()
  })
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
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(16px);
}

.header-inner {
  min-height: 112rpx;
  padding: 14rpx 28rpx 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
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

.header-title {
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.header-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  background: #e8e8e8;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 20rpx 18rpx 220rpx;
}

.identity-card,
.section-card,
.state-card {
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05);
}

.identity-card {
  position: relative;
  overflow: hidden;
  padding: 30rpx 28rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #fff7f2 0%, #ffffff 100%);
}

.card-bg-icon {
  position: absolute;
  right: 22rpx;
  top: 10rpx;
}

.identity-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
}

.identity-label,
.price-caption {
  display: block;
  font-size: 20rpx;
  color: #6b7280;
  font-weight: 800;
  letter-spacing: 1rpx;
}

.identity-name-row {
  margin-top: 10rpx;
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.identity-name {
  font-size: 42rpx;
  font-weight: 900;
  color: #111111;
}

.identity-id {
  font-size: 24rpx;
  color: #6b7280;
  font-weight: 700;
}

.vip-pill {
  min-width: 140rpx;
  height: 46rpx;
  padding: 0 16rpx;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #561d00;
  font-size: 20rpx;
  font-weight: 900;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.vip-pill.muted {
  background: #e5e7eb;
  color: #4b5563;
}

.balance-block {
  position: relative;
  z-index: 1;
  margin-top: 34rpx;
}

.balance-label {
  display: block;
  font-size: 22rpx;
  color: #6b7280;
  font-weight: 700;
}

.balance-row {
  margin-top: 10rpx;
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.currency {
  font-size: 34rpx;
  font-weight: 900;
  color: #111111;
}

.balance-num {
  font-size: 62rpx;
  line-height: 1;
  font-weight: 900;
  letter-spacing: -1rpx;
  color: #111111;
}

.section-card {
  margin-top: 22rpx;
  border-radius: 26rpx;
  padding: 28rpx 26rpx;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.section-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #111111;
}

.section-title.solo {
  display: block;
  margin-bottom: 24rpx;
}

.reward-tag {
  font-size: 20rpx;
  font-weight: 800;
  color: #a33e00;
}

.amount-grid {
  margin-top: 24rpx;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.amount-cell {
  min-height: 108rpx;
  border-radius: 18rpx;
  background: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  color: #111111;
}

.amount-cell.active {
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
}

.amount-yen {
  font-size: 24rpx;
  font-weight: 800;
}

.amount-val {
  font-size: 34rpx;
  font-weight: 900;
}

.custom-wrap {
  margin-top: 22rpx;
  height: 92rpx;
  border-radius: 18rpx;
  background: #f9f9f9;
  display: flex;
  align-items: center;
  padding: 0 22rpx;
}

.custom-yen {
  font-size: 34rpx;
  font-weight: 900;
  color: #111111;
  margin-right: 10rpx;
}

.custom-input {
  flex: 1;
  height: 100%;
  font-size: 30rpx;
  color: #111111;
}

.pay-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.pay-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f1f1f1;
}

.pay-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.pay-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.pay-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pay-icon.green {
  background: #dcfce7;
}

.pay-icon.blue {
  background: #dbeafe;
}

.pay-icon.slate {
  background: #e2e8f0;
}

.pay-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #111111;
}

.pay-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #6b7280;
}

.promotion-card {
  background: linear-gradient(135deg, #fff3ec 0%, #ffffff 100%);
}

.promotion-title {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
  color: #111111;
}

.promotion-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #5f5e5e;
}

.promotion-link {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #a33e00;
  font-weight: 800;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(16px);
}

.sum-row {
  display: flex;
  flex-direction: column;
  min-width: 180rpx;
}

.sum-label {
  font-size: 20rpx;
  color: #6b7280;
  font-weight: 700;
}

.sum-val {
  margin-top: 6rpx;
  font-size: 36rpx;
  font-weight: 900;
  color: #111111;
}

.submit-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #561d00;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  box-shadow: 0 12rpx 28rpx rgba(163, 62, 0, 0.22);
}

.submit-btn.disabled {
  background: #d1d5db;
  color: #9ca3af;
  box-shadow: none;
}

.submit-text {
  font-size: 30rpx;
  font-weight: 900;
}

.state-card {
  border-radius: 28rpx;
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
