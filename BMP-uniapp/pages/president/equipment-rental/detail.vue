<template>
  <PresidentLayout :showTabBar="false" className="president-equipment-rental-detail-layout">
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
          <view class="icon-btn" @click="onMore">
            <uni-icons type="more-filled" size="22" color="#5f5e5e" />
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <!-- 状态 + 应还时间 -->
          <view class="status-row">
            <view class="status-block">
              <text class="micro-label">RENTAL STATUS</text>
              <text class="status-title">{{ detail.statusLabel }}</text>
            </view>
            <view class="status-block align-end">
              <text class="micro-label">DUE DATE</text>
              <text class="due-value">{{ detail.dueDateLabel }}</text>
            </view>
          </view>

          <!-- 器材主卡片 + 累计时长 -->
          <view class="bento-equip">
            <view class="card card-white equip-hero">
              <view class="equip-main">
                <text class="series-pill">{{ detail.seriesTag }}</text>
                <text class="equip-name">{{ detail.equipmentName }}</text>
                <text class="equip-serial">{{ detail.equipmentNo }}</text>
              </view>
              <view class="equip-price-block">
                <text class="price-label">租借单价</text>
                <text class="price-value">{{ detail.unitPriceLabel }}</text>
              </view>
              <uni-icons type="shop" size="200" color="#a33e00" class="equip-watermark" />
            </view>
            <view class="card card-white duration-card">
              <view class="duration-icon-wrap">
                <uni-icons type="timer" size="28" color="#ff6600" />
              </view>
              <view class="duration-text">
                <text class="duration-label">累计时长</text>
                <text class="duration-value">{{ detail.accumulatedHours }}</text>
              </view>
            </view>
          </view>

          <!-- 借用人 + 时间轴 -->
          <view class="card card-muted user-card">
            <view class="user-top">
              <view class="user-left">
                <image
                  class="user-avatar"
                  :src="detail.borrowerAvatar"
                  mode="aspectFill"
                  @error="onAvatarError"
                />
                <view class="user-meta">
                  <text class="user-name">{{ detail.borrowerName }}</text>
                  <view class="user-tags">
                    <text class="vvip-pill">{{ detail.memberBadge }}</text>
                    <text class="user-phone">{{ detail.phoneMasked }}</text>
                  </view>
                </view>
              </view>
              <view class="phone-circle" @click="callUser">
                <uni-icons type="phone" size="22" color="#ff6600" />
              </view>
            </view>

            <view class="timeline">
              <view class="timeline-row">
                <view class="timeline-left">
                  <view class="dot-col">
                    <view class="dot dot-solid" />
                    <view class="dot-line" />
                  </view>
                  <view>
                    <text class="tl-k">START TIME</text>
                    <text class="tl-v">{{ detail.startTime }}</text>
                  </view>
                </view>
                <view class="timeline-right">
                  <text class="tl-k">LOCATION</text>
                  <text class="tl-v">{{ detail.venueName }}</text>
                </view>
              </view>
              <view class="timeline-row timeline-row-last">
                <view class="timeline-left">
                  <view class="dot-col">
                    <view class="dot dot-soft" />
                  </view>
                  <view>
                    <text class="tl-k">EXPECTED RETURN</text>
                    <text class="tl-v">{{ detail.expectedReturn }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 费用明细 -->
          <view class="card card-white fee-card">
            <view class="fee-head">
              <uni-icons type="list" size="18" color="#a33e00" />
              <text class="fee-title">费用明细</text>
            </view>
            <view class="fee-lines">
              <view class="fee-line">
                <text class="fee-k">器材押金</text>
                <text class="fee-v">{{ detail.deposit }}</text>
              </view>
              <view class="fee-line">
                <text class="fee-k">{{ detail.rentLineLabel }}</text>
                <text class="fee-v">{{ detail.rentAmount }}</text>
              </view>
            </view>
            <view class="fee-total">
              <text class="total-k">预估待结</text>
              <view class="total-right">
                <text class="total-num">{{ detail.estimatedTotal }}</text>
                <text class="total-sub">{{ detail.estimatedNote }}</text>
              </view>
            </view>
          </view>

          <view class="scroll-pad" />
        </view>
      </scroll-view>

      <view class="footer-bar">
        <view class="footer-inner">
          <button class="btn-chat" @click="contactUser">
            <uni-icons type="chatbubble" size="20" color="#1a1c1c" />
            <text>联系用户</text>
          </button>
          <button class="btn-return" @click="confirmReturn">
            <uni-icons type="checkbox-filled" size="20" color="#ffffff" />
            <text>确认归还</text>
          </button>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

export interface PresidentEquipmentRentalDetail {
  id: string
  statusLabel: string
  dueDateLabel: string
  seriesTag: string
  equipmentName: string
  equipmentNo: string
  unitPriceLabel: string
  accumulatedHours: string
  borrowerAvatar: string
  borrowerName: string
  memberBadge: string
  phoneMasked: string
  /** 拨号用完整号码，接口就绪后由后端返回 */
  phoneDial: string
  startTime: string
  venueName: string
  expectedReturn: string
  deposit: string
  rentLineLabel: string
  rentAmount: string
  estimatedTotal: string
  estimatedNote: string
}

const rentalId = ref('')

const mockDetail = (): PresidentEquipmentRentalDetail => ({
  id: rentalId.value || 'demo-1',
  statusLabel: '租借中',
  dueDateLabel: '今天 18:00',
  seriesTag: 'PRO SERIES',
  equipmentName: '尤尼克斯天斧99',
  equipmentNo: 'EQ2025001',
  unitPriceLabel: '¥15 / 小时',
  accumulatedHours: '4.5h',
  borrowerAvatar:
    'https://lh3.googleusercontent.com/aida-public/AB6AXuD9zYz66x02yhVOS3S7_Phpi9VsFKny5Xs4pj8Sm-jIrA4Hn-Lfn3y-W8MCCT1J5bcJ66-kF31c4-_6d5e7iFlG5Oz9-BtjQ2Dc1Mmoyap7S7WW297h3vI1PofmdIc_82GvJ6C539tQY5t8vd9WcjJSOPPffudR3bWn5suDMRkTdzRl0rg7eE_AiInzEIe8L10jxHwhvZCOPwL2Kog6wsdEgLkoiF1YoLKl1YJpjabu6Phgo5397hp0eKJAhtbOeqH_oh_xgLsZKqR5',
  borrowerName: '王小羽',
  memberBadge: 'VVIP 会员',
  phoneMasked: '138 **** 8888',
  phoneDial: '13800008888',
  startTime: '2023.10.27 14:00',
  venueName: 'A号场馆',
  expectedReturn: '2023.10.27 18:00',
  deposit: '¥500.00',
  rentLineLabel: '当前累计租金 (4.5h)',
  rentAmount: '¥60.00',
  estimatedTotal: '¥560.00',
  estimatedNote: '含已收押金'
})

const detail = ref<PresidentEquipmentRentalDetail>(mockDetail())

onLoad((options) => {
  if (options?.id) {
    rentalId.value = String(options.id)
    detail.value = mockDetail()
  }
  // TODO: getPresidentEquipmentRentalDetail(rentalId.value)
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_RENTAL_LIST)
}

function onMore() {
  uni.showActionSheet({
    itemList: ['备注订单', '上报异常']
  })
}

function onAvatarError() {
  detail.value = { ...detail.value, borrowerAvatar: '/static/placeholders/avatar.svg' }
}

function callUser() {
  dial(detail.value.phoneDial)
}

function contactUser() {
  uni.showActionSheet({
    itemList: ['拨打电话', '复制手机号'],
    success: (res) => {
      if (res.tapIndex === 0) dial(detail.value.phoneDial)
      if (res.tapIndex === 1) {
        uni.setClipboardData({
          data: detail.value.phoneDial,
          success: () => uni.showToast({ title: '已复制', icon: 'none' })
        })
      }
    }
  })
}

function dial(num: string) {
  if (!num) {
    uni.showToast({ title: '暂无号码', icon: 'none' })
    return
  }
  uni.makePhoneCall({
    phoneNumber: num,
    fail: () => uni.showToast({ title: '无法拨号', icon: 'none' })
  })
}

function confirmReturn() {
  uni.showModal({
    title: '确认归还',
    content: '确认器材已收回并办结本单？',
    success: (res) => {
      if (!res.confirm) return
      // TODO: 归还接口
      uni.showToast({ title: '已提交（示例）', icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
.president-equipment-rental-detail-layout {
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
  padding: 16rpx 32rpx 24rpx;
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
  padding: 8rpx 32rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 48rpx;
}

.scroll-pad {
  height: 32rpx;
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 16rpx 0 8rpx;
}

.status-block {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.align-end {
  align-items: flex-end;
  text-align: right;
}

.micro-label {
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 0.14em;
}

.status-title {
  font-size: 64rpx;
  font-weight: 900;
  color: #a33e00;
  letter-spacing: -0.04em;
  line-height: 1.1;
}

.due-value {
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.bento-equip {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.card {
  border-radius: 24rpx;
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

.equip-hero {
  position: relative;
  overflow: hidden;
  min-height: 280rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.equip-main {
  position: relative;
  z-index: 2;
}

.series-pill {
  display: inline-block;
  padding: 8rpx 20rpx;
  border-radius: 9999px;
  background: #e2dfde;
  color: #636262;
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.08em;
  margin-bottom: 24rpx;
}

.equip-name {
  display: block;
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
  line-height: 1.25;
}

.equip-serial {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #5f5e5e;
  font-family: ui-monospace, monospace;
  letter-spacing: 0.06em;
}

.equip-price-block {
  position: relative;
  z-index: 2;
  margin-top: 48rpx;
}

.price-label {
  display: block;
  font-size: 24rpx;
  color: #5f5e5e;
}

.price-value {
  display: block;
  margin-top: 8rpx;
  font-size: 36rpx;
  font-weight: 800;
  color: #a33e00;
}

.equip-watermark {
  position: absolute;
  right: -40rpx;
  bottom: -40rpx;
  opacity: 0.1;
  pointer-events: none;
  z-index: 1;
}

.duration-card {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 28rpx;
  padding: 40rpx 48rpx;
}

.duration-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: rgba(255, 102, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
}

.duration-text {
  text-align: left;
}

.duration-label {
  display: block;
  font-size: 22rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 0.04em;
}

.duration-value {
  display: block;
  margin-top: 8rpx;
  font-size: 44rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.user-card {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.user-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-left {
  display: flex;
  align-items: center;
  gap: 28rpx;
  flex: 1;
  min-width: 0;
}

.user-avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  background: #e8e8e8;
  flex-shrink: 0;
}

.user-name {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12rpx;
  margin-top: 10rpx;
}

.vvip-pill {
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
}

.user-phone {
  font-size: 22rpx;
  color: #5f5e5e;
}

.phone-circle {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  &:active {
    transform: scale(0.94);
  }
}

.timeline {
  padding-top: 32rpx;
  border-top: 2rpx solid rgba(227, 191, 177, 0.35);
}

.timeline-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24rpx;
}

.timeline-row-last {
  margin-top: 16rpx;
}

.timeline-left {
  display: flex;
  gap: 20rpx;
  flex: 1;
  min-width: 0;
}

.dot-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 10rpx;
  flex-shrink: 0;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.dot-solid {
  background: #a33e00;
}

.dot-soft {
  background: #e3bfb1;
}

.dot-line {
  width: 4rpx;
  flex: 1;
  min-height: 56rpx;
  background: rgba(227, 191, 177, 0.45);
  margin-top: 4rpx;
}

.timeline-right {
  text-align: right;
  flex-shrink: 0;
  max-width: 42%;
}

.tl-k {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.06em;
}

.tl-v {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.fee-card {
  position: relative;
  overflow: hidden;
}

.fee-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 36rpx;
}

.fee-title {
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: 0.12em;
}

.fee-lines {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}

.fee-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fee-k {
  font-size: 28rpx;
  color: #5f5e5e;
}

.fee-v {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.fee-total {
  margin-top: 32rpx;
  padding-top: 32rpx;
  border-top: 2rpx solid rgba(227, 191, 177, 0.35);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.total-k {
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
  padding-top: 8rpx;
}

.total-right {
  text-align: right;
}

.total-num {
  display: block;
  font-size: 56rpx;
  font-weight: 900;
  color: #a33e00;
  letter-spacing: -0.03em;
  line-height: 1.1;
}

.total-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #5f5e5e;
}

.footer-bar {
  flex-shrink: 0;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(24px);
  box-shadow: 0 -16rpx 60rpx rgba(0, 0, 0, 0.04);
}

.footer-inner {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.btn-chat {
  flex: 1;
  height: 112rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
  background: #e8e8e8;
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

.btn-return {
  flex: 1.5;
  height: 112rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
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
  gap: 10rpx;
  &::after {
    border: none;
  }
}
</style>
