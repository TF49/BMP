<template>
  <view class="coach-bookings-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="hero-shell">
        <view class="hero-topbar" :style="{ paddingTop: `${statusBarHeight}px` }">
          <view class="brand-wrap">
            <text class="brand-title">COACH BOOKINGS</text>
            <text class="brand-sub">复用预约明细结构</text>
          </view>
        </view>

        <view class="hero-card">
          <text class="hero-kicker">Reusable</text>
          <text class="hero-title">预约明细</text>
          <text class="hero-desc">复用了现有预约列表的状态信息、金额区和卡片阅读顺序，更适合快速二次设计。</text>
        </view>
      </view>

      <view class="page-body">
        <view class="tabs">
          <view class="tab active">全部</view>
          <view class="tab">待确认</view>
          <view class="tab">已完成</view>
        </view>

        <view class="stats-card">
          <view class="stat-item">
            <text class="stat-value">12</text>
            <text class="stat-label">本周预约</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">3</text>
            <text class="stat-label">待签到</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">¥2,364</text>
            <text class="stat-label">课时金额</text>
          </view>
        </view>

        <view class="section-head">
          <text class="section-kicker">List Reference</text>
          <text class="section-title">预约卡片</text>
        </view>

        <view class="booking-list">
          <view v-for="item in bookingList" :key="item.id" class="booking-card">
            <view class="card-head">
              <view>
                <text class="booking-no">{{ item.bookingNo }}</text>
                <text class="member-name">{{ item.memberName }}</text>
              </view>
              <view class="head-right">
                <text class="amount">¥{{ item.amount }}</text>
                <text class="status" :class="item.statusType">{{ item.statusText }}</text>
              </view>
            </view>

            <view class="meta-row">
              <text>{{ item.courseName }}</text>
              <text>{{ item.courseDate }}</text>
            </view>
            <view class="meta-row">
              <text>{{ item.timeRange }}</text>
              <text>{{ item.courtName }}</text>
            </view>

            <view class="action-row">
              <button class="ghost-btn">查看详情</button>
            </view>
          </view>
        </view>
      </view>

      <view class="scroll-buffer" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

const bookingList = ref([
  {
    id: 1,
    bookingNo: 'CB20260502001',
    memberName: '王昕',
    courseName: '青少年基础步伐训练',
    courseDate: '2026-05-02',
    timeRange: '19:00 - 20:30',
    courtName: 'A1 号场',
    amount: '168.00',
    statusText: '待签到',
    statusType: 'warning'
  },
  {
    id: 2,
    bookingNo: 'CB20260503007',
    memberName: '陈朗',
    courseName: '双打轮转专项课',
    courseDate: '2026-05-03',
    timeRange: '14:00 - 15:30',
    courtName: 'B2 号场',
    amount: '228.00',
    statusText: '已支付',
    statusType: 'success'
  },
  {
    id: 3,
    bookingNo: 'CB20260504003',
    memberName: '赵一鸣',
    courseName: '成人进阶拉吊突击',
    courseDate: '2026-05-04',
    timeRange: '20:00 - 21:30',
    courtName: 'A3 号场',
    amount: '198.00',
    statusText: '已完成',
    statusType: 'neutral'
  }
])
</script>

<style lang="scss" scoped>
.coach-bookings-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.14), transparent 28%),
    #f9f9f9;
}

.page-scroll {
  min-height: 100vh;
}

.hero-shell {
  padding: 0 24rpx 30rpx;
}

.hero-topbar {
  padding-bottom: 20rpx;
}

.brand-wrap {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.brand-title {
  font-size: 30rpx;
  font-weight: 900;
  color: #a33e00;
}

.brand-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.hero-card {
  padding: 34rpx 30rpx;
  border-radius: 34rpx;
  background: linear-gradient(135deg, #1f1f1f 0%, #2f3131 100%);
  color: #ffffff;
  box-shadow: 0 16rpx 32rpx rgba(26, 28, 28, 0.18);
}

.hero-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  text-transform: uppercase;
  color: rgba(255, 215, 0, 0.72);
}

.hero-title {
  display: block;
  margin-top: 12rpx;
  font-size: 48rpx;
  font-weight: 900;
  color: #ffd700;
}

.hero-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.82);
}

.page-body {
  padding: 0 24rpx;
}

.tabs {
  display: inline-flex;
  gap: 10rpx;
  padding: 8rpx;
  border-radius: 22rpx;
  background: #ececec;
}

.tab {
  padding: 14rpx 24rpx;
  border-radius: 16rpx;
  color: #6b625c;
  font-size: 22rpx;
  font-weight: 700;

  &.active {
    background: #ff6600;
    color: #ffffff;
  }
}

.stats-card {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.stat-item {
  min-width: 0;
}

.stat-value {
  display: block;
  font-size: 32rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.stat-label {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #6b625c;
}

.section-head {
  margin-top: 24rpx;
}

.section-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 3rpx;
  text-transform: uppercase;
}

.section-title {
  display: block;
  margin-top: 8rpx;
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.booking-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.booking-card {
  padding: 26rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.booking-no {
  display: block;
  font-size: 18rpx;
  color: #8e7164;
}

.member-name {
  display: block;
  margin-top: 8rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.head-right {
  text-align: right;
}

.amount {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
  color: #ff6600;
}

.status {
  display: inline-block;
  margin-top: 10rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;

  &.warning {
    background: #fff1e8;
    color: #a33e00;
  }

  &.success {
    background: #e6f6ea;
    color: #1f7a37;
  }

  &.neutral {
    background: #ededed;
    color: #5f5e5e;
  }
}

.meta-row {
  margin-top: 14rpx;
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b625c;
}

.action-row {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f4efec;
  display: flex;
  justify-content: flex-end;
}

.ghost-btn {
  margin: 0;
  padding: 0 28rpx;
  height: 78rpx;
  border: none;
  border-radius: 22rpx;
  background: #1a1c1c;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 800;
}

.scroll-buffer {
  height: 60rpx;
}
</style>
