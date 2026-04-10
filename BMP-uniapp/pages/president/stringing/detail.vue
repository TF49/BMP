<template>
  <PresidentLayout :showTabBar="false" className="president-stringing-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="icon-btn">
              <uni-icons type="arrow-left" size="24" color="#ff6600" />
            </view>
            <text class="nav-title">详情回顾</text>
          </view>
          <view class="icon-btn" @click="onShare">
            <uni-icons type="paperplane" size="22" color="#5f5e5e" />
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <!-- 状态 + 横向进度 -->
          <view class="card card-muted status-card">
            <view class="status-head">
              <text class="status-micro">CURRENT STATUS</text>
              <view class="status-line">
                <view class="pulse-dot" />
                <text class="status-main">{{ detail.statusLabel }}</text>
              </view>
            </view>
            <view class="stepper">
              <view class="stepper-lines">
                <view class="line-bg" />
                <view class="line-fill" :style="{ width: progressPct + '%' }" />
              </view>
              <view class="steps-row">
                <view
                  v-for="(step, i) in detail.steps"
                  :key="step.key"
                  class="step-col"
                >
                  <view
                    class="step-circle"
                    :class="{
                      'is-done': step.state === 'done',
                      'is-current': step.state === 'current',
                      'is-pending': step.state === 'pending'
                    }"
                  >
                    <uni-icons
                      v-if="step.state === 'done'"
                      type="checkbox-filled"
                      size="14"
                      color="#ffffff"
                    />
                    <uni-icons
                      v-else-if="step.state === 'current'"
                      type="settings"
                      size="20"
                      color="#ffffff"
                    />
                  </view>
                  <text
                    class="step-label"
                    :class="{
                      'label-active': step.state === 'current',
                      'label-muted': step.state === 'pending'
                    }"
                  >
                    {{ step.label }}
                  </text>
                </view>
              </view>
            </view>
          </view>

          <!-- 工单号 + 客户 -->
          <view class="stack-cards">
            <view class="card card-white compact-card">
              <text class="field-label">ORDER NUMBER</text>
              <text class="order-no">{{ detail.orderNo }}</text>
              <view class="field-gap" />
              <text class="field-label">SUBMITTED TIME</text>
              <text class="field-value">{{ detail.submittedAt }}</text>
            </view>
            <view class="card card-white compact-card">
              <text class="field-label">CUSTOMER</text>
              <view class="customer-row">
                <view class="cust-avatar">
                  <uni-icons type="person" size="28" color="#ff6600" />
                </view>
                <view>
                  <text class="cust-name">{{ detail.customerName }}</text>
                  <text class="cust-phone">{{ detail.customerPhoneMasked }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 球拍参数 -->
          <view class="card card-white racket-card">
            <view class="racket-corner" />
            <view class="racket-head">
              <view>
                <text class="field-label">RACKET MODEL</text>
                <text class="racket-model">{{ detail.racketModel }}</text>
              </view>
              <uni-icons type="shop" size="44" color="#a33e00" />
            </view>
            <view class="racket-grid">
              <view>
                <text class="field-label">STRING TYPE</text>
                <view class="string-row">
                  <text class="string-name">{{ detail.stringType }}</text>
                  <text class="power-pill">{{ detail.stringTag }}</text>
                </view>
              </view>
              <view>
                <text class="field-label">TENSION (LBS)</text>
                <view class="tension-row">
                  <text class="tension-val">{{ detail.tension }}</text>
                  <text class="tension-unit">lbs</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 费用 + 备注 -->
          <view class="fee-notes-row">
            <view class="card card-white fee-mini">
              <text class="field-label">SERVICE FEE</text>
              <view class="fee-big">
                <text class="fee-yen">¥</text>
                <text class="fee-num">{{ detail.serviceFee }}</text>
              </view>
            </view>
            <view class="card card-white notes-mini">
              <text class="field-label">SPECIAL REQUESTS</text>
              <view class="notes-body">
                <uni-icons type="compose" size="20" color="#ff6600" />
                <text class="notes-text">{{ detail.specialRequests }}</text>
              </view>
            </view>
          </view>

          <!-- 装饰横幅 -->
          <view class="banner">
            <image
              v-if="!bannerFailed"
              class="banner-img"
              :src="detail.bannerImage"
              mode="aspectFill"
              @error="onBannerError"
            />
            <view class="banner-fade" />
            <text class="banner-caption">KINETIC PRECISION SERVICE</text>
          </view>

          <view class="scroll-pad" />
        </view>
      </scroll-view>

      <!-- 悬浮操作条（对齐原型，不与全局 TabBar 叠两套导航） -->
      <view class="fab-wrap">
        <view class="fab-inner">
          <button class="btn-print" @click="onPrint">
            <uni-icons type="download" size="20" color="#1a1c1c" />
            <text>打印工单</text>
          </button>
          <button class="btn-update" @click="onUpdateProgress">
            <uni-icons type="loop" size="20" color="#ffffff" />
            <text>更新进度</text>
          </button>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

export type StringingStepState = 'done' | 'current' | 'pending'

export interface StringingStep {
  key: string
  label: string
  state: StringingStepState
}

export interface PresidentStringingOrderDetail {
  id: string
  statusLabel: string
  currentStepIndex: number
  steps: StringingStep[]
  orderNo: string
  submittedAt: string
  customerName: string
  customerPhoneMasked: string
  racketModel: string
  stringType: string
  stringTag: string
  tension: string
  serviceFee: string
  specialRequests: string
  bannerImage: string
}

const orderId = ref('')
const bannerFailed = ref(false)

function buildSteps(currentIndex: number): StringingStep[] {
  const labels = ['待取拍', '穿线中', '待领取', '已完成']
  return labels.map((label, i) => {
    let state: StringingStepState = 'pending'
    if (i < currentIndex) state = 'done'
    else if (i === currentIndex) state = 'current'
    return { key: `s-${i}`, label, state }
  })
}

const mockDetail = (): PresidentStringingOrderDetail => {
  const currentStepIndex = 1
  return {
    id: orderId.value || 'demo-1',
    statusLabel: '穿线中',
    currentStepIndex,
    steps: buildSteps(currentStepIndex),
    orderNo: 'ST2026030101',
    submittedAt: '2026-03-01 14:20',
    customerName: '李大伟',
    customerPhoneMasked: '138 **** 9920',
    racketModel: 'YONEX ASTROX 100ZZ',
    stringType: 'Yonex BG80',
    stringTag: 'POWER',
    tension: '26 / 28',
    serviceFee: '40',
    specialRequests: '手胶需更换，请选用耐磨型黑色手胶。',
    bannerImage:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuBbbQJs49hVPf7chgLkMaBfeLwKqt61I3FiXx7KaJvKhCRH95DyofRMarRlGe1ytuy4TXApYO9fxYwSWv20lotwU_rrIMxZns7gljEjEGq3H-X1-IwGH2MVVvb2e7Z49WwUDOl7ofG3bm87SlZ1TjjaIjjH4Ux43ZPt2QQBwdonobgfKLSslXoHLB2_zLV7W9BKGX2Dbo9Gex-o1OWvA9pgNuBq067kcUzitIhK38Zgp5rplDk_ikmP-5zSS-bBZncda8yCxhKne6Kr'
  }
}

const detail = ref<PresidentStringingOrderDetail>(mockDetail())

/** 进度条填充：当前步中心位置，接近原型 ~45%（穿线中为第 2 步） */
const progressPct = computed(() => {
  const n = detail.value.steps.length
  if (n <= 1) return 0
  const i = detail.value.currentStepIndex
  return Math.min(100, ((i + 0.45) / (n - 1)) * 100)
})

onLoad((options) => {
  if (options?.id) {
    orderId.value = String(options.id)
    detail.value = mockDetail()
  }
  // TODO: getPresidentStringingOrderDetail(orderId.value)
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.STRINGING_LIST)
}

function onShare() {
  const t = `${detail.value.orderNo} · ${detail.value.statusLabel}`
  uni.setClipboardData({
    data: t,
    success: () => uni.showToast({ title: '已复制摘要', icon: 'none' })
  })
}

function onBannerError() {
  bannerFailed.value = true
}

function onPrint() {
  // TODO: 对接打印 / 导出 PDF
  uni.showToast({ title: '打印功能开发中', icon: 'none' })
}

function onUpdateProgress() {
  uni.showActionSheet({
    itemList: ['推进至「待领取」', '推进至「已完成」', '标记异常'],
    success: (res) => {
      // TODO: 调进度接口
      uni.showToast({ title: `已选择项 ${res.tapIndex + 1}（示例）`, icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
.president-stringing-detail-layout {
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
  background-color: #f9f9f9;
  font-family: Lexend, 'PingFang SC', system-ui, sans-serif;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f9f9f9;
}

.nav-header {
  flex-shrink: 0;
  z-index: 50;
  background-color: #f9f9f9;
  padding: 16rpx 48rpx 24rpx;
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

.nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background-color: rgba(0, 0, 0, 0.06);
  }
}

.main-scroll {
  flex: 1;
  height: 0;
  min-height: 200rpx;
}

.scroll-inner {
  padding: 8rpx 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  padding-bottom: 200rpx;
}

.scroll-pad {
  height: 16rpx;
}

.card {
  border-radius: 24rpx;
  padding: 40rpx 48rpx;
  box-sizing: border-box;
}

.card-white {
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(2, 6, 23, 0.06);
}

.card-muted {
  background: #f3f3f3;
}

.status-card {
  padding-bottom: 48rpx;
}

.status-micro {
  display: block;
  font-size: 22rpx;
  font-weight: 800;
  color: #5f5e5e;
  opacity: 0.85;
  letter-spacing: 0.12em;
}

.status-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 8rpx;
}

.pulse-dot {
  width: 16rpx;
  height: 16rpx;
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
    opacity: 0.65;
    transform: scale(1.15);
  }
}

.status-main {
  font-size: 40rpx;
  font-weight: 800;
  color: #ff6600;
}

.stepper {
  margin-top: 40rpx;
  position: relative;
}

.stepper-lines {
  position: absolute;
  left: 8%;
  right: 8%;
  top: 28rpx;
  height: 4rpx;
  z-index: 0;
}

.line-bg {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 4rpx;
  background: #e8e8e8;
  border-radius: 4rpx;
}

.line-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 4rpx;
  background: #ff6600;
  border-radius: 4rpx;
  max-width: 100%;
}

.steps-row {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.step-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 22%;
}

.step-circle {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}

.step-circle.is-done {
  background: #ff6600;
}

.step-circle.is-current {
  width: 64rpx;
  height: 64rpx;
  background: #ff6600;
  box-shadow: 0 0 0 8rpx #ffdbcd;
}

.step-circle.is-pending {
  background: #e8e8e8;
  border: 4rpx solid #e2e2e2;
}

.step-label {
  margin-top: 12rpx;
  font-size: 20rpx;
  font-weight: 800;
  color: #1a1c1c;
  text-align: center;
}

.step-label.label-active {
  color: #ff6600;
}

.step-label.label-muted {
  color: #5f5e5e;
}

.stack-cards {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.compact-card {
  padding: 36rpx 40rpx;
}

.field-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.1em;
}

.order-no {
  display: block;
  margin-top: 8rpx;
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.field-gap {
  height: 28rpx;
}

.field-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.customer-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-top: 16rpx;
}

.cust-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
  background: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cust-name {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.cust-phone {
  display: block;
  margin-top: 6rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #5f5e5e;
}

.racket-card {
  position: relative;
  overflow: hidden;
  padding: 56rpx 48rpx;
}

.racket-corner {
  position: absolute;
  top: 0;
  right: 0;
  width: 200rpx;
  height: 200rpx;
  background: linear-gradient(225deg, rgba(163, 62, 0, 0.08), transparent 60%);
  border-bottom-left-radius: 100%;
  pointer-events: none;
}

.racket-head {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20rpx;
}

.racket-model {
  display: block;
  margin-top: 8rpx;
  font-size: 44rpx;
  font-weight: 900;
  font-style: italic;
  color: #1a1c1c;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.racket-grid {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
  margin-top: 48rpx;
}

.string-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12rpx;
  margin-top: 8rpx;
}

.string-name {
  font-size: 32rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.power-pill {
  padding: 6rpx 16rpx;
  border-radius: 9999px;
  background: #e2dfde;
  color: #636262;
  font-size: 20rpx;
  font-weight: 800;
}

.tension-row {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
  margin-top: 8rpx;
}

.tension-val {
  font-size: 48rpx;
  font-weight: 900;
  color: #ff6600;
}

.tension-unit {
  font-size: 26rpx;
  font-weight: 800;
  color: #5f5e5e;
  text-transform: uppercase;
}

.fee-notes-row {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.fee-mini {
  padding: 36rpx 40rpx;
}

.fee-big {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  margin-top: 12rpx;
}

.fee-yen {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.fee-num {
  font-size: 64rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.notes-mini {
  padding: 36rpx 40rpx;
}

.notes-body {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-top: 12rpx;
}

.notes-text {
  flex: 1;
  font-size: 26rpx;
  font-weight: 700;
  color: #ff6600;
  line-height: 1.55;
}

.banner {
  position: relative;
  height: 220rpx;
  border-radius: 32rpx;
  overflow: hidden;
  background: #e5e5e5;
}

.banner-img {
  width: 100%;
  height: 100%;
  opacity: 0.35;
  filter: grayscale(100%);
}

.banner-fade {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    #f9f9f9 0%,
    transparent 35%,
    transparent 65%,
    #f9f9f9 100%
  );
}

.banner-caption {
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  text-align: center;
  font-size: 20rpx;
  font-weight: 900;
  letter-spacing: 0.5em;
  color: #5f5e5e;
  opacity: 0.45;
}

.fab-wrap {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  padding: 16rpx 48rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  pointer-events: none;
}

.fab-inner {
  pointer-events: auto;
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px);
  box-shadow: 0 16rpx 80rpx rgba(0, 0, 0, 0.12);
  border: 2rpx solid rgba(255, 255, 255, 0.65);
}

.btn-print {
  flex: 1;
  height: 96rpx;
  border-radius: 24rpx;
  background: #e8e8e8;
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
  border: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  &::after {
    border: none;
  }
}

.btn-update {
  flex: 2;
  height: 96rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  font-weight: 800;
  color: #ffffff;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.22);
  border: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  &::after {
    border: none;
  }
}
</style>
