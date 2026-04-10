<template>
  <PresidentLayout :showTabBar="false" className="president-course-booking-detail-layout">
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
          <!-- 状态主卡片（对齐原型橙色渐变 + 核销扫码意象） -->
          <view class="hero-card">
            <view class="hero-content">
              <text class="hero-label">BOOKING STATUS</text>
              <view class="hero-row">
                <text class="hero-status">{{ detail.statusLabel }}</text>
                <uni-icons type="camera-filled" size="56" color="rgba(255,255,255,0.45)" />
              </view>
            </view>
            <view class="hero-glow" />
          </view>

          <!-- 课程信息 -->
          <view class="card card-white">
            <view class="course-head">
              <view class="course-titles">
                <text class="course-name-cn">{{ detail.courseNameCn }}</text>
                <text class="course-name-en">{{ detail.courseNameEn }}</text>
              </view>
              <view class="code-pill">
                <text class="code-text">{{ detail.courseCode }}</text>
              </view>
            </view>
            <view class="info-grid">
              <view class="info-cell" v-for="item in infoCells" :key="item.key">
                <view class="info-icon-wrap">
                  <uni-icons :type="item.icon" size="22" color="#a33e00" />
                </view>
                <view class="info-text">
                  <text class="info-k">{{ item.label }}</text>
                  <text class="info-v">{{ item.value }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 预约人 + 会员权益 -->
          <view class="bento-row">
            <view class="card card-muted bento-booker">
              <text class="section-label">预约人信息</text>
              <view class="booker-rows">
                <view class="booker-line">
                  <text class="booker-k">姓名</text>
                  <text class="booker-v">{{ detail.bookerName }}</text>
                </view>
                <view class="booker-line">
                  <text class="booker-k">联系电话</text>
                  <text class="booker-v">{{ detail.bookerPhoneMasked }}</text>
                </view>
              </view>
            </view>
            <view class="card card-muted bento-benefits">
              <text class="section-label">会员权益</text>
              <view class="benefits-body">
                <view class="vip-badge">
                  <text class="vip-text">VIP</text>
                </view>
                <view>
                  <text class="level-line">等级 {{ detail.memberLevel }}</text>
                  <text class="discount-line">折扣率: {{ detail.discountLabel }}</text>
                </view>
              </view>
              <uni-icons type="medal" size="120" color="#1a1c1c" class="benefits-watermark" />
            </view>
          </view>

          <!-- 结算明细 -->
          <view class="card card-white settle-card">
            <text class="section-label">结算明细</text>
            <view class="settle-total-row">
              <text class="settle-total-k">订单总额</text>
              <text class="settle-total-v">¥ {{ detail.orderAmount }}</text>
            </view>
            <view class="divider" />
            <view class="settle-meta">
              <view class="meta-line">
                <text class="meta-k">支付方式</text>
                <text class="meta-v">{{ detail.payMethod }}</text>
              </view>
              <view class="meta-line">
                <text class="meta-k">下单时间</text>
                <text class="meta-v">{{ detail.orderTime }}</text>
              </view>
              <view class="meta-line">
                <text class="meta-k">流水号</text>
                <text class="meta-v mono">{{ detail.transactionId }}</text>
              </view>
            </view>
          </view>

          <!-- 提示横幅 -->
          <view class="tip-banner">
            <image
              v-if="!bannerFailed"
              class="tip-img"
              :src="detail.bannerImage"
              mode="aspectFill"
              @error="onBannerError"
            />
            <view class="tip-overlay">
              <text class="tip-text">
                请确保教练已准备好，并在上课前通过核销系统录入考勤。
              </text>
            </view>
          </view>

          <view class="scroll-pad" />
        </view>
      </scroll-view>

      <view class="footer-bar">
        <view class="footer-inner">
          <button class="btn-cancel" @click="onCancelBooking">取消预约</button>
          <button class="btn-verify" @click="onVerifyNow">
            <uni-icons type="checkbox-filled" size="20" color="#ffffff" />
            <text>立即核销</text>
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

/** 课程预约详情（会长端）；接口就绪后由 onLoad 的 id 拉取并替换 mock */
export interface PresidentCourseBookingDetail {
  id: string
  statusLabel: string
  courseNameCn: string
  courseNameEn: string
  courseCode: string
  coachName: string
  courseDate: string
  timeRange: string
  courtName: string
  bookerName: string
  bookerPhoneMasked: string
  memberLevel: number
  discountLabel: string
  orderAmount: string
  payMethod: string
  orderTime: string
  transactionId: string
  bannerImage: string
}

const bookingId = ref('')

const mockDetail = (): PresidentCourseBookingDetail => ({
  id: bookingId.value || 'demo-1',
  statusLabel: '待核销',
  courseNameCn: '全能技术进阶班',
  courseNameEn: 'Badminton Pro Clinic',
  courseCode: 'BMP-ADV-2026',
  coachName: '张伟',
  courseDate: '2026-03-05',
  timeRange: '19:00 - 21:00',
  courtName: 'A1号场',
  bookerName: '李慕华',
  bookerPhoneMasked: '138 **** 9012',
  memberLevel: 3,
  discountLabel: '9.2折',
  orderAmount: '1,299.00',
  payMethod: '账户余额支付',
  orderTime: '2026-03-01 14:23:55',
  transactionId: 'TXN_982341029',
  bannerImage:
    'https://lh3.googleusercontent.com/aida-public/AB6AXuCAnvo4Uz1GRVCngD9KQZaJeVgoShSUnzFM6rGANpmgQqkw53vJlcsDUle5oc6ppQqNn06UP323A-i6j4lZjMccxRHvMMUigYtqWRhVcDJ9Br8YwwLftxmyUWEVm4YW7MSZLSDOI62qT-LCasjBtLhoNNEVhts1soISp2ZIrjEqbdJLIyoScmJ3TrB28f-Jpv0IWbH-2mdwa5VqaYQ06RcNTucQyn-Sdz2qFQwxqzsKD-_jtkY0rOyowyli4O2xECZNhP_DZJTGcDLa'
})

const detail = ref<PresidentCourseBookingDetail>(mockDetail())
const bannerFailed = ref(false)

const infoCells = computed(() => [
  { key: 'coach', label: '教练', value: detail.value.coachName, icon: 'person' },
  { key: 'date', label: '日期', value: detail.value.courseDate, icon: 'calendar' },
  { key: 'time', label: '时间段', value: detail.value.timeRange, icon: 'calendar-filled' },
  { key: 'court', label: '场地', value: detail.value.courtName, icon: 'location' }
])

onLoad((options) => {
  if (options?.id) {
    bookingId.value = String(options.id)
    detail.value = mockDetail()
  }
  // TODO: 接入会长端课程预约详情 API：getPresidentCourseBookingDetail(bookingId.value)
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURSE_BOOKING_LIST)
}

function onShare() {
  const summary = `${detail.value.courseNameCn} · ${detail.value.statusLabel}`
  uni.setClipboardData({
    data: summary,
    success: () => {
      uni.showToast({ title: '已复制摘要', icon: 'none' })
    },
    fail: () => {
      uni.showToast({ title: '分享功能开发中', icon: 'none' })
    }
  })
}

function onBannerError() {
  bannerFailed.value = true
}

function onCancelBooking() {
  uni.showModal({
    title: '取消预约',
    content: '确定要取消该课程预约吗？',
    success: (res) => {
      if (!res.confirm) return
      // TODO: 调用取消预约接口
      uni.showToast({ title: '已提交取消（示例）', icon: 'none' })
    }
  })
}

function onVerifyNow() {
  // TODO: 跳转扫码核销或调用核销接口
  uni.showToast({ title: '请对接核销流程', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.president-course-booking-detail-layout {
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
  letter-spacing: -0.02em;
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
  padding: 0 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 48rpx;
}

.scroll-pad {
  height: 32rpx;
}

.hero-card {
  position: relative;
  border-radius: 32rpx;
  overflow: hidden;
  padding: 64rpx 48rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 16rpx 48rpx rgba(163, 62, 0, 0.35);
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-label {
  font-size: 22rpx;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 0.2em;
}

.hero-row {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-status {
  font-size: 72rpx;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -0.04em;
}

.hero-glow {
  position: absolute;
  right: -80rpx;
  bottom: -80rpx;
  width: 240rpx;
  height: 240rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  filter: blur(40rpx);
  z-index: 1;
}

.card {
  border-radius: 28rpx;
  padding: 48rpx;
  box-sizing: border-box;
}

.card-white {
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(2, 6, 23, 0.06);
}

.card-muted {
  background: #f3f3f3;
}

.course-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24rpx;
  margin-bottom: 40rpx;
}

.course-name-cn {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
  line-height: 1.25;
}

.course-name-en {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #5f5e5e;
}

.code-pill {
  flex-shrink: 0;
  padding: 16rpx 28rpx;
  border-radius: 9999px;
  background: #e8e8e8;
}

.code-text {
  font-size: 24rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 0.08em;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40rpx 24rpx;
}

.info-cell {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.info-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  background: #ffdbcd;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-k {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.06em;
}

.info-v {
  display: block;
  margin-top: 6rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.bento-row {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.bento-booker {
  border-left: 8rpx solid #ff6600;
}

.section-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.14em;
  margin-bottom: 28rpx;
}

.booker-rows {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.booker-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.booker-k {
  font-size: 28rpx;
  font-weight: 600;
  color: #5f5e5e;
}

.booker-v {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.bento-benefits {
  position: relative;
  overflow: hidden;
}

.benefits-body {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.vip-badge {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #1a1c1c;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vip-text {
  font-size: 22rpx;
  font-weight: 900;
  color: #ffffff;
}

.level-line {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.discount-line {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #5f5e5e;
}

.benefits-watermark {
  position: absolute;
  right: -16rpx;
  bottom: -32rpx;
  opacity: 0.06;
  pointer-events: none;
}

.settle-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
}

.settle-total-k {
  font-size: 28rpx;
  color: #5a4136;
}

.settle-total-v {
  font-size: 40rpx;
  font-weight: 800;
  color: #ff6600;
}

.divider {
  height: 2rpx;
  background: #eeeeee;
  margin-bottom: 28rpx;
}

.settle-meta {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.meta-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-k {
  font-size: 26rpx;
  color: #5f5e5e;
}

.meta-v {
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.mono {
  font-family: ui-monospace, monospace;
}

.tip-banner {
  position: relative;
  height: 200rpx;
  border-radius: 28rpx;
  overflow: hidden;
  background: #e5e5e5;
}

.tip-img {
  width: 100%;
  height: 100%;
  filter: grayscale(100%);
}

.tip-overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  top: 0;
  background: linear-gradient(to top, rgba(249, 249, 249, 0.95), transparent);
  display: flex;
  align-items: flex-end;
  padding: 28rpx;
}

.tip-text {
  font-size: 24rpx;
  line-height: 1.5;
  color: #5f5e5e;
  font-weight: 600;
}

.footer-bar {
  flex-shrink: 0;
  padding: 24rpx 48rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px);
  box-shadow: 0 -16rpx 60rpx rgba(0, 0, 0, 0.04);
}

.footer-inner {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.btn-cancel {
  flex: 1;
  height: 112rpx;
  line-height: 112rpx;
  border-radius: 28rpx;
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
  background: #e8e8e8;
  border: none;
  margin: 0;
  padding: 0;
  &::after {
    border: none;
  }
}

.btn-verify {
  flex: 1.5;
  height: 112rpx;
  border-radius: 28rpx;
  font-size: 30rpx;
  font-weight: 800;
  color: #ffffff;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.25);
  border: none;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  &::after {
    border: none;
  }
}
</style>
