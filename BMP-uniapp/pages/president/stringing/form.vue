<template>
  <PresidentLayout :showTabBar="false" className="president-stringing-form-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="nav-icon-btn">
              <uni-icons type="arrow-left" size="22" color="#5f5e5e" />
            </view>
            <text class="nav-title">新增穿线工单</text>
          </view>
          <image class="nav-avatar" :src="operatorAvatar" mode="aspectFill" />
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <!-- 客户基本信息 -->
          <view class="card">
            <view class="section-head">
              <uni-icons type="person" size="22" color="#a33e00" />
              <text class="section-label">客户基本信息</text>
            </view>
            <view class="field-row">
              <view class="field">
                <text class="mini-label">客户姓名</text>
                <input
                  v-model="customerName"
                  class="field-input"
                  type="text"
                  placeholder="请输入姓名"
                  placeholder-class="ph"
                />
              </view>
              <view class="field">
                <text class="mini-label">联系电话</text>
                <input
                  v-model="customerPhone"
                  class="field-input"
                  type="number"
                  placeholder="138 **** ****"
                  placeholder-class="ph"
                />
              </view>
            </view>
          </view>

          <!-- 球拍与线材配置 -->
          <view class="card">
            <view class="section-head">
              <uni-icons type="settings" size="22" color="#a33e00" />
              <text class="section-label">球拍与线材配置</text>
            </view>
            <view class="field-row">
              <view class="field full">
                <text class="mini-label">球拍型号</text>
                <view class="input-with-icon">
                  <input
                    v-model="racketModel"
                    class="field-input pad-r"
                    type="text"
                    placeholder="例如: Yonex Astrox 100ZZ"
                    placeholder-class="ph"
                  />
                  <uni-icons type="search" size="18" color="rgba(95,94,94,0.45)" class="abs-icon" />
                </view>
              </view>
              <view class="field full">
                <text class="mini-label">羽线型号</text>
                <picker mode="selector" :range="stringLabels" :value="stringIndex" @change="onStringPick">
                  <view class="picker-display">
                    <text class="picker-text">{{ stringOptions[stringIndex].label }}</text>
                    <uni-icons type="arrowdown" size="14" color="#5f5e5e" />
                  </view>
                </picker>
              </view>
            </view>

            <view class="tension-box">
              <view class="tension-top">
                <view>
                  <text class="mini-label">拉线磅数 (LBS)</text>
                  <text class="tension-hint">建议范围: 22 - 32 LBS</text>
                </view>
                <view class="tension-value-row">
                  <input
                    class="tension-input"
                    type="number"
                    :value="tensionLbs"
                    @input="onTensionInput"
                  />
                  <text class="tension-unit">LBS</text>
                </view>
              </view>
              <slider
                class="tension-slider"
                :value="tensionLbs"
                :min="TENSION_MIN"
                :max="TENSION_MAX"
                :step="1"
                active-color="#a33e00"
                background-color="#e2e2e2"
                block-size="22"
                @changing="onTensionSliderChanging"
                @change="onTensionSliderChange"
              />
            </view>

            <view class="field full mt">
              <text class="mini-label">特殊要求 (如: 预拉、四结)</text>
              <textarea
                v-model="specialNotes"
                class="remarks"
                placeholder="填写具体的穿线工艺要求..."
                placeholder-class="ph"
                :maxlength="800"
              />
            </view>
          </view>

          <!-- 结算明细 -->
          <view class="card settle-card">
            <view class="section-head">
              <uni-icons type="wallet" size="22" color="#a33e00" />
              <text class="section-label">结算明细</text>
            </view>
            <view class="settle-rows">
              <view class="settle-row">
                <text class="settle-lab">服务费</text>
                <view class="fee-input-wrap">
                  <text class="yen">¥</text>
                  <input
                    class="fee-input"
                    type="digit"
                    :value="serviceFeeInput"
                    @input="onServiceFeeInput"
                  />
                </view>
              </view>
              <view class="settle-row">
                <text class="settle-lab">线材费用</text>
                <text class="settle-strong">¥ {{ stringCostFormatted }}</text>
              </view>
              <view class="settle-row total-row">
                <text class="total-lab">总计金额</text>
                <text class="total-num">¥ {{ totalFormatted }}</text>
              </view>
            </view>

            <text class="mini-label pay-head">支付状态</text>
            <view class="pay-toggle">
              <view
                class="pay-opt"
                :class="{ 'is-paid': paymentStatus === 'paid' }"
                @click="paymentStatus = 'paid'"
              >
                <uni-icons
                  v-if="paymentStatus === 'paid'"
                  type="checkbox-filled"
                  size="16"
                  color="#ffffff"
                />
                <text class="pay-txt">已支付</text>
              </view>
              <view
                class="pay-opt"
                :class="{ 'is-pending': paymentStatus === 'pending' }"
                @click="paymentStatus = 'pending'"
              >
                <text class="pay-txt">待支付</text>
              </view>
            </view>

            <button class="btn-submit" @click="onConfirm">
              <uni-icons type="compose" size="22" color="#ffffff" />
              <text class="btn-submit-txt">确认创建工单</text>
            </button>
          </view>

          <!-- 当前工作量 -->
          <view class="workload-card">
            <image class="workload-img" :src="workloadBg" mode="aspectFill" />
            <view class="workload-mask">
              <text class="workload-k">当前工作量</text>
              <view class="workload-line">
                <view class="pulse-dot" />
                <text class="workload-val">{{ pendingCount }} 待处理工单</text>
              </view>
            </view>
          </view>

          <view class="scroll-pad" />
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const TENSION_MIN = 18
const TENSION_MAX = 35

const operatorAvatar =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCqcSOo_xDMtKXETdjb5XfQv9m3pTOtbcguBf6s_3qcw7yewAFE81vd-ftuAog5l8MzPIpwhNL9Rkrd0x3NaD2g0HxT-2xCr4DySi8gSWY5cA8VYiIj-4_RFClD9KpBUtsBkQtl4Rwt82864rK-PWZCb2XvlXZ909o_wHMmUzmkVE1VmiqtODJz3eAk3bBi2i5hYgyFEGJxZWcXRbXn-B0f8658wrFMOghA0lV0Ealt3sevOiO7BOSZhiKKP1uigJ-zy8AhM81uj8T5'

const workloadBg =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCWKePGDbPaOB9TBC01JcPk7yrBoLpR8UaK6PaquG_m5JaJraHDAB3_mOh-AUOmMHJjDUutGd4YjxJ-iHVUUmSRxoN_ot2kHlGMICI4qI-EahkRydrfBiCnTwdVxzf5rPwaTzuGcM7b7IB2d4HPNV9pCaB2Hp32MTEl6fWyHRpKpb2kVdyCW6oEk135Q6hd4D2f7DfeXTXaNbRGL8f99xAX4LUf8vFxp_NaxecgGFQ38SAETXKqYxaZrU-xL-K1jgYNHHpd8nsoOy3T'

const stringOptions = [
  { id: 'bg65', label: 'BG65 (耐用型)', cost: 55 },
  { id: 'bg80', label: 'BG80 (进攻型)', cost: 62 },
  { id: 'exbolt', label: 'Exbolt 65 (弹性型)', cost: 58 },
  { id: 'aerobite', label: 'Aerobite (控制型)', cost: 68 }
] as const

const stringLabels = stringOptions.map((o) => o.label)
const stringIndex = ref(0)

const customerName = ref('')
const customerPhone = ref('')
const racketModel = ref('')
const tensionLbs = ref(26)
const specialNotes = ref('')
const serviceFee = ref(30)
const serviceFeeInput = ref('30')
const paymentStatus = ref<'paid' | 'pending'>('paid')
const pendingCount = ref(3)

const stringCost = computed(() => stringOptions[stringIndex.value].cost)

const stringCostFormatted = computed(() => stringCost.value.toFixed(2))

const totalAmount = computed(() => {
  const fee = Number(serviceFee.value)
  const safe = Number.isFinite(fee) && fee >= 0 ? fee : 0
  return safe + stringCost.value
})

const totalFormatted = computed(() => totalAmount.value.toFixed(2))

function onStringPick(e: { detail: { value: string } }) {
  stringIndex.value = Number(e.detail.value) || 0
}

function clampTension(n: number) {
  return Math.min(TENSION_MAX, Math.max(TENSION_MIN, Math.round(n)))
}

function onTensionInput(e: { detail?: { value?: string } }) {
  const raw = e.detail?.value ?? ''
  const n = parseInt(String(raw).replace(/\D/g, ''), 10)
  if (Number.isFinite(n)) {
    tensionLbs.value = clampTension(n)
  }
}

function readSliderValue(e: { detail?: { value?: number | string } }) {
  const v = e.detail?.value
  const n = typeof v === 'number' ? v : parseInt(String(v), 10)
  return Number.isFinite(n) ? n : tensionLbs.value
}

function onTensionSliderChanging(e: { detail?: { value?: number | string } }) {
  tensionLbs.value = clampTension(readSliderValue(e))
}

function onTensionSliderChange(e: { detail?: { value?: number | string } }) {
  tensionLbs.value = clampTension(readSliderValue(e))
}

function onServiceFeeInput(e: { detail?: { value?: string } }) {
  const raw = (e.detail?.value ?? '').replace(/[^\d.]/g, '')
  serviceFeeInput.value = raw
  const n = parseFloat(raw)
  serviceFee.value = Number.isFinite(n) && n >= 0 ? n : 0
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.STRINGING_LIST)
}

function onConfirm() {
  // TODO: POST 创建穿线工单
  uni.showToast({
    title: '工单已创建（示例）',
    icon: 'success'
  })
}
</script>

<style lang="scss" scoped>
.president-stringing-form-layout {
  :deep(.president-layout-content) {
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
}

.page {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f9f9f9;
  font-family: Lexend, 'PingFang SC', system-ui, sans-serif;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.nav-header {
  flex-shrink: 0;
  background: #f9f9f9;
  padding: 20rpx 32rpx 16rpx;
  z-index: 50;
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.nav-icon-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background: #e8e8e8;
  }
}

.nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #ff6600;
  letter-spacing: -0.02em;
}

.nav-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  border: 4rpx solid #ff6600;
  background: #e2e2e2;
}

.main-scroll {
  flex: 1;
  height: 0;
  min-height: 200rpx;
}

.scroll-inner {
  padding: 16rpx 32rpx 48rpx;
}

.card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 48rpx 40rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.04);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 36rpx;
}

.section-label {
  font-size: 24rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.mini-label {
  display: block;
  font-size: 20rpx;
  font-weight: 600;
  color: #5f5e5e;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 12rpx;
}

.field-row {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.field {
  flex: 1;
  min-width: 0;
}

.field.full {
  width: 100%;
}

.field-input {
  width: 100%;
  height: 88rpx;
  padding: 0 28rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  box-sizing: border-box;
}

.field-input.pad-r {
  padding-right: 72rpx;
}

.ph {
  color: rgba(95, 94, 94, 0.45);
  font-size: 26rpx;
}

.input-with-icon {
  position: relative;
}

.abs-icon {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

.picker-display {
  height: 88rpx;
  padding: 0 28rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.picker-text {
  font-size: 28rpx;
  color: #1a1c1c;
  font-weight: 600;
}

.tension-box {
  margin-top: 28rpx;
  padding: 36rpx 28rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
}

.tension-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24rpx;
}

.tension-hint {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: rgba(95, 94, 94, 0.65);
}

.tension-value-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.tension-input {
  width: 120rpx;
  text-align: right;
  font-size: 56rpx;
  font-weight: 800;
  color: #a33e00;
  background: transparent;
  border: none;
  padding: 0;
}

.tension-unit {
  font-size: 22rpx;
  font-weight: 800;
  color: #a33e00;
}

.tension-slider {
  margin: 0;
}

.field.mt {
  margin-top: 28rpx;
}

.remarks {
  width: 100%;
  min-height: 160rpx;
  padding: 24rpx 28rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #1a1c1c;
  box-sizing: border-box;
}

.settle-card {
  box-shadow: 0 16rpx 48rpx rgba(0, 0, 0, 0.08);
}

.settle-rows {
  margin-bottom: 36rpx;
}

.settle-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
}

.settle-lab {
  font-size: 24rpx;
  color: #5f5e5e;
}

.settle-strong {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.fee-input-wrap {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.yen {
  font-size: 22rpx;
  color: #5f5e5e;
}

.fee-input {
  width: 120rpx;
  height: 56rpx;
  padding: 0 12rpx;
  background: #f3f3f3;
  border-radius: 8rpx;
  text-align: right;
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.total-row {
  padding-top: 24rpx;
  margin-top: 8rpx;
  border-top: 2rpx solid #eeeeee;
}

.total-lab {
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: #1a1c1c;
}

.total-num {
  font-size: 44rpx;
  font-weight: 900;
  font-style: italic;
  color: #a33e00;
}

.pay-head {
  margin-bottom: 16rpx;
}

.pay-toggle {
  display: flex;
  gap: 12rpx;
  margin-bottom: 32rpx;
}

.pay-opt {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 22rpx 12rpx;
  border-radius: 16rpx;
  background: #e8e8e8;
  border: 2rpx solid transparent;
  box-sizing: border-box;
}

.pay-opt.is-paid {
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
}

.pay-opt.is-pending {
  background: rgba(255, 102, 0, 0.12);
  border-color: #ff6600;
}

.pay-txt {
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #5f5e5e;
}

.pay-opt.is-paid .pay-txt {
  color: #ffffff;
}

.pay-opt.is-pending .pay-txt {
  color: #a33e00;
}

.btn-submit {
  width: 100%;
  height: 100rpx;
  border-radius: 16rpx;
  border: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  background: linear-gradient(90deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 12rpx 36rpx rgba(163, 62, 0, 0.25);
  &::after {
    border: none;
  }
}

.btn-submit-txt {
  font-size: 28rpx;
  font-weight: 800;
  color: #ffffff;
}

.workload-card {
  position: relative;
  height: 320rpx;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  background: #e2dfde;
}

.workload-img {
  width: 100%;
  height: 100%;
  opacity: 0.55;
  mix-blend-mode: overlay;
}

.workload-mask {
  position: absolute;
  inset: 0;
  padding: 40rpx 36rpx;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.65) 0%, transparent 100%);
}

.workload-k {
  font-size: 20rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  margin-bottom: 8rpx;
}

.workload-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.pulse-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: #ff6600;
  animation: pulse-dot 1.6s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(0.92);
  }
}

.workload-val {
  font-size: 32rpx;
  font-weight: 800;
  color: #ffffff;
}

.scroll-pad {
  height: 24rpx;
}
</style>
