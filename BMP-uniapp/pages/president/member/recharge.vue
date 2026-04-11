<template>
  <PresidentLayout :showTabBar="false">
    <view class="recharge-page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-bar-inner">
          <view class="top-left" @click="onBack">
            <uni-icons type="arrow-left" size="24" color="#ea580c" />
            <text class="top-title">充值中心</text>
          </view>
          <image class="top-avatar" :src="presidentAvatar" mode="aspectFill" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading && !member" class="state-wrap">
          <view class="spinner" />
          <text>加载会员信息…</text>
        </view>

        <view v-else-if="!loading && !member" class="state-wrap">
          <text>无法加载该会员，请返回重试</text>
        </view>

        <template v-else-if="member">
          <!-- 会员身份卡 -->
          <view class="identity-card">
            <view class="id-bg-icon" aria-hidden="true">
              <uni-icons type="wallet" size="120" color="#000000" />
            </view>
            <view class="id-top">
              <view class="id-meta">
                <text class="id-label">Current Member</text>
                <view class="id-name-row">
                  <text class="id-name">{{ member.memberName || '未命名' }}</text>
                  <text class="id-num">#{{ member.id }}</text>
                </view>
              </view>
              <text v-if="member.memberType === 'MEMBER'" class="vip-pill">VIP CLASS</text>
              <text v-else class="vip-pill muted">NORMAL</text>
            </view>
            <view class="balance-block">
              <text class="bal-label">Available Balance</text>
              <view class="bal-row">
                <text class="yen">¥</text>
                <text class="bal-num">{{ formatBalance(member.balance) }}</text>
              </view>
            </view>
          </view>

          <!-- 金额 -->
          <view class="section">
            <view class="section-head">
              <text class="section-title">选择金额</text>
              <text class="reward-tag">充值奖励 +10%</text>
            </view>
            <view class="amount-grid">
              <view
                v-for="(amt, idx) in presetAmounts"
                :key="amt"
                class="amt-cell"
                :class="{ on: isPresetActive(idx) }"
                @click="selectPreset(idx)"
              >
                <text class="amt-yen">¥</text>
                <text class="amt-val">{{ amt }}</text>
              </view>
            </view>
            <view class="custom-wrap">
              <text class="custom-yen">¥</text>
              <input
                class="custom-input"
                type="digit"
                v-model="customAmount"
                placeholder="其他金额"
                @input="onCustomInput"
              />
            </view>
          </view>

          <!-- 支付方式 -->
          <view class="section">
            <text class="section-title solo">支付方式</text>
            <view class="pay-list">
              <view
                v-for="(m, i) in payMethods"
                :key="m.code"
                class="pay-row"
                @click="selectedPay = i"
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
        </template>

        <view class="scroll-spacer" />
      </scroll-view>

      <view v-if="member" class="bottom-bar">
        <view class="sum-row">
          <text class="sum-label">充值总计:</text>
          <text class="sum-val">¥ {{ formatMoney(totalAmount) }}</text>
        </view>
        <view class="submit-btn" :class="{ disabled: submitting || totalAmount <= 0 }" @click="onConfirm">
          <uni-icons type="bolt" size="22" color="#561d00" />
          <text class="submit-text">确认充值</text>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { adminRecharge } from '@/api/recharge'
import { getMemberInfo, type MemberInfo } from '@/api/member'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeNavigateBack } from '@/utils/navigation'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

const defaultAvatar =
  '/static/placeholders/hero.svg'

const presidentAvatar = computed(() => userStore.userInfo?.avatar || defaultAvatar)

const memberId = ref(0)
const member = ref<MemberInfo | null>(null)
const loading = ref(true)
const submitting = ref(false)

const presetAmounts = [100, 500, 1000, 2000, 5000, 10000]
/** 默认选中 500（与原型一致） */
const selectedPreset = ref(1)
const useCustom = ref(false)
const customAmount = ref('')

const payMethods = [
  { code: 'CASH' as const, title: '现金支付', sub: '柜台现金', icon: 'wallet', iconBg: 'emerald', iconColor: '#059669' },
  { code: 'ALIPAY' as const, title: '支付宝', sub: '支付宝', icon: 'compose', iconBg: 'blue', iconColor: '#2563eb' },
  { code: 'WECHAT' as const, title: '微信支付', sub: '微信支付', icon: 'weixin', iconBg: 'green', iconColor: '#16a34a' }
]

const selectedPay = ref(0)

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

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.MEMBER_LIST)
}

async function loadMember() {
  if (!memberId.value) return
  loading.value = true
  try {
    member.value = await getMemberInfo(memberId.value)
  } catch {
    member.value = null
    uni.showToast({ title: '加载会员失败', icon: 'none' })
  } finally {
    loading.value = false
  }
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
    const method = payMethods[selectedPay.value].code
    const res = await adminRecharge({
      memberId: memberId.value,
      amount: totalAmount.value,
      method
    })
    uni.showToast({ title: res.message || '充值成功', icon: 'success' })
    await loadMember()
    setTimeout(() => onBack(), 600)
  } catch {
    // 业务错误已在 request 内 toast
  } finally {
    submitting.value = false
  }
}

onLoad((q?: Record<string, string | undefined>) => {
  const raw = q?.memberId ?? q?.id
  const n = raw ? parseInt(String(raw), 10) : NaN
  if (!Number.isFinite(n) || n <= 0) {
    uni.showToast({ title: '缺少会员参数', icon: 'none' })
    setTimeout(() => safeNavigateBack(PRESIDENT_PAGES.MEMBER_LIST), 1200)
    return
  }
  memberId.value = n
  loadMember()
})
</script>

<style lang="scss" scoped>
.recharge-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
  position: relative;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f8fafc;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 40;
  background: rgba(248, 250, 252, 0.95);
  backdrop-filter: blur(12px);
}

.top-bar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 48rpx 24rpx;
  min-height: 88rpx;
}

.top-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.top-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #ea580c;
  letter-spacing: -0.02em;
}

.top-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 9999px;
  border: 4rpx solid rgba(255, 237, 213, 0.9);
}

.scroll {
  height: calc(100vh - var(--status-bar-height) - 200rpx);
  padding: 32rpx 48rpx 0;
  box-sizing: border-box;
}

.state-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx;
  gap: 24rpx;
  color: #5f5e5e;
  font-size: 28rpx;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #e5e5e5;
  border-top-color: #ea580c;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.identity-card {
  position: relative;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 48rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 28rpx rgba(15, 23, 42, 0.06);
}

.id-bg-icon {
  position: absolute;
  right: -24rpx;
  top: -24rpx;
  opacity: 0.05;
  pointer-events: none;
}

.id-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40rpx;
}

.id-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #5f5e5e;
  margin-bottom: 8rpx;
}

.id-name-row {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 12rpx;
}

.id-name {
  font-size: 56rpx;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.id-num {
  font-size: 28rpx;
  color: #5f5e5e;
  font-weight: 500;
}

.vip-pill {
  font-size: 20rpx;
  font-weight: 800;
  color: #ea580c;
  background: rgba(255, 102, 0, 0.1);
  padding: 8rpx 16rpx;
  border-radius: 9999px;
}

.vip-pill.muted {
  color: #5f5e5e;
  background: #f3f3f3;
}

.balance-block {
  padding-top: 8rpx;
}

.bal-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #5f5e5e;
  margin-bottom: 8rpx;
}

.bal-row {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.yen {
  font-size: 36rpx;
  font-weight: 600;
}

.bal-num {
  font-size: 72rpx;
  font-weight: 900;
  letter-spacing: -0.04em;
}

.section {
  margin-bottom: 48rpx;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 26rpx;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #5f5e5e;
}

.section-title.solo {
  margin-bottom: 24rpx;
  display: block;
}

.reward-tag {
  font-size: 20rpx;
  font-weight: 800;
  color: #ea580c;
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.amt-cell {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 28rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-bottom: 4rpx solid transparent;
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.04);
  transition: transform 0.15s;
  &:active {
    transform: scale(0.98);
  }
}

.amt-cell.on {
  background: #a33e00;
  border-bottom-color: #ff6600;
  box-shadow: 0 12rpx 32rpx rgba(234, 88, 12, 0.18);
}

.amt-yen {
  font-size: 22rpx;
  font-weight: 600;
  color: #5f5e5e;
  margin-bottom: 4rpx;
}

.amt-cell.on .amt-yen {
  color: rgba(255, 255, 255, 0.85);
}

.amt-val {
  font-size: 36rpx;
  font-weight: 800;
}

.amt-cell.on .amt-val {
  color: #ffffff;
}

.custom-wrap {
  position: relative;
  margin-top: 32rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx 28rpx 28rpx 72rpx;
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.04);
}

.custom-yen {
  position: absolute;
  left: 32rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 32rpx;
  font-weight: 800;
  color: #5f5e5e;
}

.custom-input {
  font-size: 34rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.pay-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.pay-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(15, 23, 42, 0.04);
  &:active {
    transform: scale(0.99);
  }
}

.pay-left {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.pay-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pay-icon.emerald {
  background: #ecfdf5;
}
.pay-icon.blue {
  background: #eff6ff;
}
.pay-icon.green {
  background: #f0fdf4;
}

.pay-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.pay-sub {
  display: block;
  font-size: 20rpx;
  color: #5f5e5e;
  margin-top: 4rpx;
}

.scroll-spacer {
  height: 280rpx;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  padding: 24rpx 48rpx calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.sum-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24rpx;
  padding: 0 8rpx;
}

.sum-label {
  font-size: 24rpx;
  color: #5f5e5e;
  font-weight: 600;
}

.sum-val {
  font-size: 48rpx;
  font-weight: 900;
  letter-spacing: -0.03em;
}

.submit-btn {
  height: 112rpx;
  border-radius: 20rpx;
  background: #ff6600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 16rpx 40rpx rgba(234, 88, 12, 0.22);
  &:active {
    transform: scale(0.99);
  }
}

.submit-btn.disabled {
  opacity: 0.55;
  pointer-events: none;
}

.submit-text {
  font-size: 32rpx;
  font-weight: 800;
  color: #561d00;
}
</style>
