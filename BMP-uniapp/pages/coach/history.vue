<template>
  <view class="coach-history-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="page-shell">
        <CoachTopBar
          :status-bar-height="statusBarHeight"
          :avatar="coach.avatar"
          brand="KINETIC LOGIC"
          action-icon="notification-filled"
          @action="handleNotice"
        />

        <view class="hero-head">
          <text class="hero-title">课程历史记录</text>
          <text class="hero-subtitle">COURSE HISTORY OVERVIEW</text>
        </view>

        <view class="filter-bar">
          <view class="filter-chip filter-chip-active">
            <text>过去 30 天</text>
          </view>
          <view class="filter-chip" @tap="handleMonthFilter">
            <text>按月筛选</text>
          </view>
        </view>

        <view class="summary-list">
          <view class="summary-card">
            <view class="summary-orbit" />
            <view class="summary-head">
              <uni-icons type="refreshtime" size="18" color="#a33e00" />
              <text class="summary-label">总授课时长</text>
            </view>
            <view class="summary-value-row">
              <text class="summary-value">124</text>
              <text class="summary-unit">小时</text>
            </view>
          </view>

          <view class="summary-card">
            <view class="summary-orbit" />
            <view class="summary-head">
              <uni-icons type="staff-filled" size="18" color="#a33e00" />
              <text class="summary-label">辅导学员数</text>
            </view>
            <view class="summary-value-row">
              <text class="summary-value">342</text>
              <text class="summary-unit">人次</text>
            </view>
          </view>

          <view class="summary-card summary-card-primary">
            <view class="summary-glow" />
            <view class="summary-head summary-head-primary">
              <uni-icons type="star-filled" size="18" color="#561d00" />
              <text class="summary-label summary-label-primary">平均评分</text>
            </view>
            <view class="rating-row">
              <text class="rating-value">4.9</text>
              <view class="rating-stars">
                <uni-icons v-for="idx in 5" :key="idx" type="star-filled" size="16" color="#561d00" />
              </view>
            </view>
          </view>
        </view>

        <view class="history-card">
          <text class="history-card-title">最近课程记录</text>

          <view class="history-list">
            <view
              v-for="item in historyList"
              :key="item.id"
              class="history-item"
              @tap="openHistory(item)"
            >
              <view class="history-icon-wrap">
                <uni-icons :type="item.icon" size="24" color="#a33e00" />
              </view>

              <view class="history-main">
                <text class="history-name">{{ item.name }}</text>

                <view class="history-meta-grid">
                  <view class="history-meta-block">
                    <view class="history-meta-line">
                      <uni-icons type="calendar" size="15" color="#5f5e5e" />
                      <text>{{ item.date }}</text>
                    </view>
                  </view>

                  <view class="history-meta-block">
                    <view class="history-meta-line">
                      <uni-icons type="refreshtime" size="15" color="#5f5e5e" />
                      <text>{{ item.time }}</text>
                    </view>
                  </view>
                </view>

                <view class="history-stats-row">
                  <view class="mini-stat">
                    <text class="mini-stat-label">学员</text>
                    <text class="mini-stat-value">{{ item.students }}</text>
                  </view>

                  <view class="mini-stat">
                    <text class="mini-stat-label">评分</text>
                    <view class="mini-stars">
                      <uni-icons
                        v-for="idx in item.stars"
                        :key="`${item.id}-${idx}`"
                        type="star-filled"
                        size="16"
                        color="#a33e00"
                      />
                    </view>
                  </view>

                  <view class="history-arrow">
                    <uni-icons type="right" size="18" color="#5f5e5e" />
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="load-more-btn" @tap="handleLoadMore">
            <text>加载更多历史记录</text>
          </view>
        </view>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <CoachTabBar current="courses" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'

type HistoryItem = {
  id: number
  name: string
  date: string
  time: string
  students: string
  stars: number
  icon: 'medal' | 'staff-filled' | 'settings'
}

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

const coach = ref({
  avatar: '/static/placeholders/avatar.svg'
})

const historyList = ref<HistoryItem[]>([
  {
    id: 1,
    name: '高级技术强化班',
    date: '10月24日, 2023',
    time: '14:00 - 16:00',
    students: '12 人',
    stars: 5,
    icon: 'medal'
  },
  {
    id: 2,
    name: '青少年基础训练',
    date: '10月22日, 2023',
    time: '09:00 - 11:30',
    students: '24 人',
    stars: 4,
    icon: 'staff-filled'
  },
  {
    id: 3,
    name: '一对一特训 - 李雷',
    date: '10月21日, 2023',
    time: '18:30 - 20:00',
    students: '1 人',
    stars: 5,
    icon: 'settings'
  }
])

function handleNotice() {
  uni.showToast({
    title: '通知入口待接入',
    icon: 'none'
  })
}

function handleMonthFilter() {
  uni.showToast({
    title: '按月筛选待接入',
    icon: 'none'
  })
}

function openHistory(item: HistoryItem) {
  uni.showToast({
    title: `查看 ${item.name}`,
    icon: 'none'
  })
}

function handleLoadMore() {
  uni.showToast({
    title: '更多历史记录待接入',
    icon: 'none'
  })
}

</script>

<style lang="scss" scoped>
.coach-history-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.page-scroll {
  min-height: 100vh;
}

.page-shell {
  padding: 0 18rpx;
}

.hero-head {
  margin-top: 54rpx;
  padding: 0 10rpx;
}

.hero-title {
  display: block;
  font-size: 66rpx;
  line-height: 1.08;
  font-weight: 900;
  letter-spacing: -2rpx;
}

.hero-subtitle {
  display: block;
  margin-top: 16rpx;
  font-size: 17px;
  color: #474746;
  letter-spacing: 4rpx;
}

.filter-bar {
  margin-top: 30rpx;
  display: inline-flex;
  padding: 8rpx;
  border-radius: 18rpx;
  background: #f3f3f3;
  gap: 10rpx;
}

.filter-chip {
  min-width: 150rpx;
  height: 68rpx;
  padding: 0 24rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5f5e5e;
  font-size: 18px;
  font-weight: 500;
}

.filter-chip-active {
  background: #ffffff;
  color: #1a1c1c;
  box-shadow: 0 6rpx 16rpx rgba(26, 28, 28, 0.06);
}

.summary-list {
  margin-top: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.summary-card {
  position: relative;
  min-height: 214rpx;
  border-radius: 28rpx;
  background: #ffffff;
  padding: 28rpx 28rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 26rpx rgba(26, 28, 28, 0.04);
}

.summary-orbit {
  position: absolute;
  top: -30rpx;
  right: -40rpx;
  width: 200rpx;
  height: 200rpx;
  border-radius: 999rpx;
  background: rgba(163, 62, 0, 0.05);
}

.summary-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.summary-label {
  font-size: 18px;
  color: #5f5e5e;
}

.summary-value-row {
  position: relative;
  z-index: 1;
  margin-top: 48rpx;
  display: flex;
  align-items: flex-end;
  gap: 12rpx;
}

.summary-value {
  font-size: 84rpx;
  line-height: 1;
  font-weight: 900;
}

.summary-unit {
  font-size: 22px;
  color: #5f5e5e;
  padding-bottom: 8rpx;
}

.summary-card-primary {
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 14rpx 34rpx rgba(163, 62, 0, 0.18);
}

.summary-glow {
  position: absolute;
  top: -40rpx;
  right: -30rpx;
  width: 220rpx;
  height: 220rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.1);
  filter: blur(10rpx);
}

.summary-head-primary {
  color: #561d00;
}

.summary-label-primary {
  color: #561d00;
}

.rating-row {
  position: relative;
  z-index: 1;
  margin-top: 44rpx;
  display: flex;
  align-items: flex-end;
  gap: 18rpx;
}

.rating-value {
  font-size: 84rpx;
  line-height: 1;
  font-weight: 900;
  color: #561d00;
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding-bottom: 10rpx;
}

.history-card {
  margin-top: 30rpx;
  border-radius: 28rpx;
  background: #ffffff;
  padding: 28rpx 0 18rpx;
  box-shadow: 0 12rpx 32rpx rgba(26, 28, 28, 0.05);
}

.history-card-title {
  display: block;
  padding: 0 26rpx;
  font-size: 42rpx;
  font-weight: 900;
}

.history-list {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 0 12rpx;
}

.history-item {
  display: flex;
  gap: 18rpx;
  padding: 22rpx;
  border-radius: 22rpx;
  background: #f9f9f9;
}

.history-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(163, 62, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.history-main {
  flex: 1;
  min-width: 0;
}

.history-name {
  display: block;
  font-size: 24px;
  line-height: 1.2;
  font-weight: 900;
}

.history-meta-grid {
  margin-top: 16rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx 20rpx;
}

.history-meta-line {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
  color: #5f5e5e;
  font-size: 18px;
  line-height: 1.5;
}

.history-stats-row {
  margin-top: 22rpx;
  display: grid;
  grid-template-columns: 1fr 1fr 64rpx;
  align-items: end;
  gap: 16rpx;
}

.mini-stat-label {
  display: block;
  font-size: 16rpx;
  color: #5f5e5e;
}

.mini-stat-value {
  display: block;
  margin-top: 8rpx;
  font-size: 24px;
  font-weight: 900;
}

.mini-stars {
  display: flex;
  align-items: center;
  gap: 4rpx;
  margin-top: 8rpx;
}

.history-arrow {
  width: 64rpx;
  height: 64rpx;
  border-radius: 999rpx;
  background: #e2e2e2;
  display: flex;
  align-items: center;
  justify-content: center;
  justify-self: end;
}

.load-more-btn {
  width: 100%;
  margin-top: 18rpx;
  padding: 24rpx 0 14rpx;
  text-align: center;
  color: #a33e00;
  font-size: 18px;
}

.bottom-space {
  height: 170rpx;
}

</style>
