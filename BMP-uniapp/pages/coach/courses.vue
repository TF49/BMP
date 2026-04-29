<template>
  <view class="coach-courses-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="hero-shell">
        <view class="hero-topbar" :style="{ paddingTop: `${statusBarHeight}px` }">
          <view class="brand-wrap">
            <text class="brand-title">COACH COURSES</text>
            <text class="brand-sub">可复用课程列表骨架</text>
          </view>
        </view>

        <view class="hero-main">
          <text class="hero-kicker">Reusable</text>
          <text class="hero-title">我的课程</text>
          <text class="hero-desc">复用了现有课程中心的卡片信息层级，后续可直接替换为教练端真实数据。</text>
        </view>
      </view>

      <view class="page-body">
        <view class="toolbar-card">
          <view class="toolbar-chip active">
            <text>全部课程</text>
          </view>
          <view class="toolbar-chip">
            <text>报名中</text>
          </view>
          <view class="toolbar-chip">
            <text>进行中</text>
          </view>
          <view class="toolbar-chip">
            <text>已结束</text>
          </view>
        </view>

        <view class="section-head">
          <view>
            <text class="section-kicker">Card Reference</text>
            <text class="section-title">课程卡片</text>
          </view>
        </view>

        <view class="course-list">
          <view v-for="item in courseList" :key="item.id" class="course-card">
            <view class="card-badge" :class="item.badgeType">
              {{ item.badge }}
            </view>
            <view class="card-main">
              <view class="card-copy">
                <text class="course-name">{{ item.courseName }}</text>
                <text class="course-meta">{{ item.courseDate }} {{ item.startTime }} - {{ item.endTime }}</text>
                <text class="course-meta">{{ item.courtName }} · {{ item.venueName }}</text>
              </view>
              <view class="stat-panel">
                <text class="stat-label">人数</text>
                <text class="stat-value">{{ item.currentStudents }}/{{ item.maxStudents }}</text>
              </view>
            </view>

            <view class="bottom-row">
              <view class="price-block">
                <text class="price-label">课时费</text>
                <text class="price-value">¥{{ item.price }}/节</text>
              </view>
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

const courseList = ref([
  {
    id: 1,
    courseName: '青少年基础步伐训练',
    courseDate: '2026-05-02',
    startTime: '19:00',
    endTime: '20:30',
    courtName: 'A1 号场',
    venueName: '羽擎主馆',
    currentStudents: 10,
    maxStudents: 12,
    price: '168.00',
    badge: '报名中',
    badgeType: 'warning'
  },
  {
    id: 2,
    courseName: '双打轮转专项课',
    courseDate: '2026-05-03',
    startTime: '14:00',
    endTime: '15:30',
    courtName: 'B2 号场',
    venueName: '羽擎西馆',
    currentStudents: 8,
    maxStudents: 8,
    price: '228.00',
    badge: '满员',
    badgeType: 'neutral'
  },
  {
    id: 3,
    courseName: '成人进阶拉吊突击',
    courseDate: '2026-05-04',
    startTime: '20:00',
    endTime: '21:30',
    courtName: 'A3 号场',
    venueName: '羽擎主馆',
    currentStudents: 6,
    maxStudents: 10,
    price: '198.00',
    badge: '进行中',
    badgeType: 'success'
  }
])
</script>

<style lang="scss" scoped>
.coach-courses-page {
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

.hero-main {
  margin-top: 12rpx;
  padding: 34rpx 30rpx;
  border-radius: 34rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  box-shadow: 0 18rpx 38rpx rgba(163, 62, 0, 0.22);
}

.hero-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.74);
}

.hero-title {
  display: block;
  margin-top: 12rpx;
  font-size: 48rpx;
  font-weight: 900;
}

.hero-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
}

.page-body {
  padding: 0 24rpx;
}

.toolbar-card {
  display: flex;
  gap: 12rpx;
  overflow-x: auto;
  padding-bottom: 10rpx;
}

.toolbar-chip {
  flex-shrink: 0;
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  background: #ffffff;
  color: #6b625c;
  font-size: 22rpx;
  font-weight: 700;

  &.active {
    background: #fff1e8;
    color: #a33e00;
  }
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

.course-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.course-card {
  position: relative;
  padding: 26rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
}

.card-badge {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;

  &.warning {
    background: #fff1e8;
    color: #a33e00;
  }

  &.neutral {
    background: #ededed;
    color: #5f5e5e;
  }

  &.success {
    background: #e6f6ea;
    color: #1f7a37;
  }
}

.card-main {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.card-copy {
  flex: 1;
  min-width: 0;
}

.course-name {
  display: block;
  padding-right: 140rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #1a1c1c;
  line-height: 1.4;
}

.course-meta {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b625c;
}

.stat-panel {
  min-width: 120rpx;
  padding: 18rpx 16rpx;
  border-radius: 22rpx;
  background: #faf8f6;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 18rpx;
  color: #8e7164;
  font-weight: 800;
}

.stat-value {
  display: block;
  margin-top: 12rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  font-weight: 900;
}

.bottom-row {
  margin-top: 22rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f4efec;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
}

.price-label {
  display: block;
  font-size: 18rpx;
  color: #8e7164;
  font-weight: 800;
}

.price-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  font-weight: 900;
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
