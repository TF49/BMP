<template>
  <view class="coach-home-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="page-shell">
        <CoachTopBar
          :status-bar-height="statusBarHeight"
          :avatar="coach.avatar"
          brand="Kinetic Logic"
          action-icon="calendar"
          @action="handleCalendar"
        />

        <view class="hero-section">
          <text class="coach-name">{{ coach.name }}</text>
          <view class="hero-meta">
            <view class="rating-pill">
              <uni-icons type="star-filled" size="14" color="#561d00" />
              <text>{{ coach.rating }}</text>
            </view>
            <view class="venue-line">
              <uni-icons type="location" size="15" color="#5f5e5e" />
              <text>{{ coach.venueName }}</text>
            </view>
          </view>
        </view>

        <view class="metrics-grid">
          <view class="metric-card">
            <text class="metric-label">今日课程</text>
            <view class="metric-main metric-split">
              <text class="metric-highlight">{{ metrics.todayCourses }}</text>
              <text class="metric-unit">节次</text>
            </view>
          </view>

          <view class="metric-card">
            <text class="metric-label">本周收入</text>
            <view class="metric-main">
              <text class="metric-value">{{ metrics.weekIncome }}</text>
            </view>
          </view>

          <view class="metric-card">
            <text class="metric-label">已签到学员</text>
            <view class="metric-main metric-attendance">
              <view class="attendance-row">
                <text class="metric-value">{{ metrics.checkedIn }}</text>
                <text class="attendance-total">/ {{ metrics.totalStudents }}</text>
              </view>
              <view class="progress-track">
                <view class="progress-fill" :style="{ width: attendancePercent + '%' }" />
              </view>
            </view>
          </view>
        </view>

        <view class="actions-row">
          <view class="action-card action-card-primary" @tap="handleQuickAction('add-course')">
            <text class="action-title vertical-text">添加课程</text>
            <view class="action-icon">
              <uni-icons type="plusempty" size="24" color="#ffffff" />
            </view>
          </view>

          <view class="action-card" @tap="handleQuickAction('history')">
            <text class="action-title vertical-text">课程历史</text>
            <view class="action-icon action-icon-dark">
              <uni-icons type="reload" size="22" color="#1a1c1c" />
            </view>
          </view>

          <view class="action-card" @tap="handleQuickAction('students')">
            <text class="action-title vertical-text">学员名单</text>
            <view class="action-icon action-icon-dark">
              <uni-icons type="staff" size="22" color="#1a1c1c" />
            </view>
          </view>
        </view>

        <view class="agenda-head">
          <text class="agenda-title">今日日程</text>
          <text class="agenda-date">{{ agendaDate }}</text>
        </view>

        <view class="timeline-list">
          <view
            v-for="item in scheduleList"
            :key="item.id"
            class="timeline-card"
            :class="[item.variant, { active: item.status === 'ongoing' }]"
          >
            <view v-if="item.status === 'ongoing'" class="left-indicator left-indicator-primary" />
            <view v-else-if="item.variant === 'intense'" class="left-indicator left-indicator-tertiary" />

            <view class="time-block">
              <text class="time-start">{{ item.startTime }}</text>
              <text class="time-end">{{ item.endTime }}</text>
            </view>

            <view class="course-block">
              <text class="timeline-course-name">{{ item.courseName }}</text>
              <view class="course-meta-row">
                <view class="course-meta-item">
                  <uni-icons type="calendar" size="15" color="#5f5e5e" />
                  <text>{{ item.courtName }}</text>
                </view>
                <view class="course-meta-item">
                  <uni-icons type="person" size="15" color="#5f5e5e" />
                  <text>{{ item.studentText }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <CoachTabBar current="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

const coach = ref({
  name: '张伟教练',
  rating: '4.9',
  venueName: '奥体中心羽毛球馆',
  avatar: '/static/placeholders/avatar.svg'
})

const metrics = ref({
  todayCourses: 8,
  weekIncome: '¥12.8k',
  checkedIn: 42,
  totalStudents: 48
})

const scheduleList = ref([
  {
    id: 1,
    startTime: '09:00',
    endTime: '10:30',
    courseName: '青少年进阶班',
    courtName: '3号场地',
    studentText: '6名学员',
    status: 'ongoing',
    variant: 'default'
  },
  {
    id: 2,
    startTime: '14:00',
    endTime: '15:30',
    courseName: '私人1对1私教课',
    courtName: '5号场地',
    studentText: '王小明',
    status: 'pending',
    variant: 'default'
  },
  {
    id: 3,
    startTime: '16:30',
    endTime: '18:00',
    courseName: '成人兴趣基础班',
    courtName: '2号场地',
    studentText: '8名学员',
    status: 'pending',
    variant: 'default'
  },
  {
    id: 4,
    startTime: '19:30',
    endTime: '21:00',
    courseName: '竞技队集训',
    courtName: '1-2号场地',
    studentText: '12名学员',
    status: 'pending',
    variant: 'intense'
  }
])

const attendancePercent = computed(() => {
  const checkedIn = Number(metrics.value.checkedIn || 0)
  const totalStudents = Number(metrics.value.totalStudents || 0)
  if (!totalStudents) return 0
  return Math.min(100, Math.round((checkedIn / totalStudents) * 100))
})

const agendaDate = computed(() => '10月24日 · 星期四')

function handleCalendar() {
  uni.showToast({
    title: '日历入口待接入',
    icon: 'none'
  })
}

function handleQuickAction(type: 'add-course' | 'history' | 'students') {
  if (type === 'students') {
    uni.navigateTo({
      url: '/pages/coach/students'
    })
    return
  }

  if (type === 'history') {
    uni.navigateTo({
      url: '/pages/coach/history'
    })
    return
  }

  const messageMap = {
    'add-course': '添加课程入口待接入',
    history: '课程历史入口待接入',
    students: '学员名单入口待接入'
  }

  uni.showToast({
    title: messageMap[type],
    icon: 'none'
  })
}

</script>

<style lang="scss" scoped>
.coach-home-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.page-scroll {
  min-height: 100vh;
}

.page-shell {
  padding: 0 26rpx;
}

.hero-section {
  margin-top: 38rpx;
}

.coach-name {
  display: block;
  font-size: 68rpx;
  line-height: 1.05;
  font-weight: 900;
  letter-spacing: -2rpx;
}

.hero-meta {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20rpx;
}

.rating-pill {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: #ffdbcd;
  color: #561d00;
  font-size: 24rpx;
  font-weight: 800;
}

.venue-line {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #5f5e5e;
  font-size: 24rpx;
  font-weight: 500;
}

.metrics-grid {
  margin-top: 46rpx;
  display: grid;
  grid-template-columns: 1fr;
  gap: 22rpx;
}

.metric-card {
  min-height: 196rpx;
  border-radius: 28rpx;
  background: #ffffff;
  padding: 28rpx 26rpx;
  box-shadow: 0 8rpx 26rpx rgba(26, 28, 28, 0.04);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-label {
  font-size: 24rpx;
  color: #5f5e5e;
  font-weight: 500;
}

.metric-main {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.metric-split {
  align-items: flex-end;
}

.metric-highlight {
  font-size: 76rpx;
  line-height: 1;
  font-weight: 900;
  color: #a33e00;
}

.metric-unit {
  font-size: 28rpx;
  color: #5f5e5e;
  padding-bottom: 10rpx;
}

.metric-value {
  font-size: 76rpx;
  line-height: 1;
  font-weight: 900;
  color: #1a1c1c;
  letter-spacing: -2rpx;
}

.metric-attendance {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 22rpx;
}

.attendance-row {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.attendance-total {
  font-size: 34rpx;
  font-weight: 700;
  color: #5f5e5e;
}

.progress-track {
  width: 100%;
  height: 8rpx;
  border-radius: 999rpx;
  background: #e2e2e2;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: #a33e00;
}

.actions-row {
  margin-top: 32rpx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
}

.action-card {
  min-height: 212rpx;
  border-radius: 22rpx;
  background: #ffffff;
  padding: 26rpx 18rpx;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.04);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
}

.action-card-primary {
  background: linear-gradient(180deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
}

.vertical-text {
  writing-mode: vertical-rl;
  text-orientation: upright;
  letter-spacing: 4rpx;
}

.action-title {
  font-size: 26rpx;
  font-weight: 900;
  line-height: 1.2;
}

.action-icon {
  width: 54rpx;
  height: 54rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon-dark {
  color: #1a1c1c;
}

.agenda-head {
  margin-top: 52rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.agenda-title {
  font-size: 56rpx;
  line-height: 1.1;
  font-weight: 900;
}

.agenda-date {
  font-size: 26rpx;
  color: #a33e00;
  font-weight: 800;
}

.timeline-list {
  margin-top: 26rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.timeline-card {
  position: relative;
  min-height: 146rpx;
  border-radius: 26rpx;
  background: #ffffff;
  padding: 26rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.04);

  &.intense {
    background: #f3f3f3;
  }

  &.default {
    border: 2rpx solid rgba(227, 191, 177, 0.18);
  }

  &.active {
    border: none;
  }
}

.left-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 6rpx;
  border-radius: 999rpx;
}

.left-indicator-primary {
  background: #a33e00;
}

.left-indicator-tertiary {
  background: #009cfc;
}

.time-block {
  width: 110rpx;
  flex-shrink: 0;
  text-align: center;
}

.time-start {
  display: block;
  font-size: 28px;
  line-height: 1;
  font-weight: 900;
}

.time-end {
  display: block;
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #5f5e5e;
  font-weight: 500;
}

.course-block {
  flex: 1;
  min-width: 0;
}

.timeline-course-name {
  display: block;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 900;
  color: #1a1c1c;
}

.course-meta-row {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 18rpx;
}

.course-meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  color: #5f5e5e;
}

.bottom-space {
  height: 170rpx;
}

</style>
