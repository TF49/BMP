<template>
  <view class="coach-schedule-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="page-shell">
        <CoachTopBar
          :status-bar-height="statusBarHeight"
          :avatar="coach.avatar"
          brand="Kinetic Logic"
          action-icon="calendar"
          @action="handleCalendar"
        />

        <view class="hero-head">
          <view>
            <text class="page-title">教练日程</text>
            <text class="page-subtitle">MARCH 2024</text>
          </view>

          <view class="view-toggle">
            <view class="toggle-item toggle-item-active">
              <text>日视图</text>
            </view>
            <view class="toggle-item" @tap="handleWeekView">
              <text>周视图</text>
            </view>
          </view>
        </view>

        <scroll-view scroll-x class="date-strip-scroll" :show-scrollbar="false">
          <view class="date-strip">
            <view
              v-for="item in dateStrip"
              :key="item.day"
              class="date-card"
              :class="{ active: item.active }"
              @tap="selectDate(item.day)"
            >
              <text class="date-dow">{{ item.dow }}</text>
              <text class="date-day">{{ item.day }}</text>
              <view v-if="item.active" class="date-dot" />
            </view>
          </view>
        </scroll-view>

        <view class="timeline-section">
          <view
            v-for="hour in timelineHours"
            :key="hour"
            class="time-row"
          >
            <text class="time-label">{{ hour }}</text>
            <view class="time-line" />
          </view>

          <view class="current-time-indicator" :style="{ top: currentTimeTop + 'rpx' }">
            <text class="current-time-text">11:30</text>
            <view class="current-time-dot" />
            <view class="current-time-line" />
          </view>

          <view
            v-for="session in sessions"
            :key="session.id"
            class="session-card"
            :class="session.variant"
            :style="{ top: session.top + 'rpx', height: session.height + 'rpx' }"
            @tap="openSession(session)"
          >
            <view class="session-left-indicator" :class="session.indicatorClass" />

            <template v-if="session.variant === 'light'">
              <view class="session-content">
                <view class="session-top">
                  <view>
                    <text class="session-title">{{ session.title }}</text>
                    <view class="session-meta-line">
                      <uni-icons :type="session.metaIcon" size="14" color="#5f5e5e" />
                      <text class="session-meta-text">{{ session.location }}</text>
                    </view>
                  </view>
                </view>

                <view class="session-bottom">
                  <view class="student-stack">
                    <image
                      v-for="(avatar, index) in session.avatars"
                      :key="`${session.id}-${index}`"
                      class="student-avatar"
                      :class="{ overlap: index > 0 }"
                      :src="avatar"
                      mode="aspectFill"
                    />
                    <view class="student-more">+6</view>
                  </view>
                  <view class="status-pill status-pill-blue">
                    <text>{{ session.status }}</text>
                  </view>
                </view>
              </view>
            </template>

            <template v-else-if="session.variant === 'primary'">
              <view class="session-content session-content-primary">
                <view class="session-primary-head">
                  <view>
                    <text class="session-title session-title-white">{{ session.title }}</text>
                    <view class="session-meta-line">
                      <uni-icons type="calendar" size="14" color="#ffffff" />
                      <text class="session-meta-text session-meta-text-white">{{ session.location }}</text>
                    </view>
                  </view>
                  <uni-icons type="star-filled" size="18" color="#ffffff" />
                </view>

                <view class="session-primary-bottom">
                  <view class="student-profile">
                    <image class="student-profile-avatar" :src="session.studentAvatar" mode="aspectFill" />
                    <view class="student-copy">
                      <text class="student-copy-label">学生</text>
                      <text class="student-copy-name">{{ session.studentName }}</text>
                    </view>
                  </view>

                  <view class="duration-block">
                    <text class="duration-label">时长</text>
                    <text class="duration-value">{{ session.duration }}</text>
                  </view>
                </view>
              </view>
            </template>

            <template v-else>
              <view class="session-content session-content-muted">
                <view>
                  <text class="session-title session-title-muted">{{ session.title }}</text>
                  <view class="session-meta-line">
                    <uni-icons type="gear" size="14" color="#8e7164" />
                    <text class="session-meta-text session-meta-text-muted">{{ session.location }}</text>
                  </view>
                </view>

                <view class="session-muted-footer">
                  <text class="session-ended-text">{{ session.status }}</text>
                </view>
              </view>
            </template>
          </view>
        </view>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <view class="fab" @tap="handleAdd">
      <uni-icons type="plusempty" size="24" color="#ffffff" />
    </view>

    <CoachTabBar current="schedule" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'

type DateItem = {
  dow: string
  day: number
  active: boolean
}

type SessionItem = {
  id: number
  variant: 'light' | 'primary' | 'muted'
  top: number
  height: number
  title: string
  location: string
  status: string
  indicatorClass: string
  metaIcon: 'calendar' | 'location' | 'gear'
  avatars: string[]
  studentAvatar?: string
  studentName?: string
  duration?: string
}

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

const coach = ref({
  avatar: '/static/placeholders/avatar.svg'
})

const dateStrip = ref<DateItem[]>([
  { dow: 'MON', day: 11, active: false },
  { dow: 'TUE', day: 12, active: false },
  { dow: 'WED', day: 13, active: true },
  { dow: 'THU', day: 14, active: false },
  { dow: 'FRI', day: 15, active: false },
  { dow: 'SAT', day: 16, active: false },
  { dow: 'SUN', day: 17, active: false }
])

const timelineHours = ref([
  '08:00',
  '09:00',
  '10:00',
  '11:00',
  '12:00',
  '13:00',
  '14:00',
  '15:00',
  '16:00',
  '17:00',
  '18:00',
  '19:00',
  '20:00',
  '21:00',
  '22:00'
])

const currentTimeTop = ref(742)

const sessions = ref<SessionItem[]>([
  {
    id: 1,
    variant: 'light',
    top: 300,
    height: 168,
    title: '青少年基础训练',
    location: 'COURT 04',
    status: '进行中',
    indicatorClass: 'indicator-tertiary',
    metaIcon: 'calendar',
    avatars: [
      '/static/placeholders/avatar.svg',
      '/static/placeholders/avatar.svg'
    ]
  },
  {
    id: 2,
    variant: 'primary',
    top: 962,
    height: 240,
    title: '中级步法专项',
    location: 'COURT 01 (VVIP)',
    status: '',
    indicatorClass: 'indicator-primary',
    metaIcon: 'calendar',
    avatars: [],
    studentAvatar: '/static/placeholders/avatar.svg',
    studentName: '陈志平',
    duration: '90 MINS'
  },
  {
    id: 3,
    variant: 'muted',
    top: 1462,
    height: 164,
    title: '体能强化班',
    location: 'GYM ZONE B',
    status: '已结束 (17:00 - 18:00)',
    indicatorClass: 'indicator-secondary',
    metaIcon: 'gear',
    avatars: []
  }
])

function selectDate(day: number) {
  dateStrip.value = dateStrip.value.map((item) => ({
    ...item,
    active: item.day === day
  }))
}

function handleCalendar() {
  uni.showToast({
    title: '日历入口待接入',
    icon: 'none'
  })
}

function handleWeekView() {
  uni.showToast({
    title: '周视图待接入',
    icon: 'none'
  })
}

function openSession(session: SessionItem) {
  uni.showToast({
    title: `查看：${session.title}`,
    icon: 'none'
  })
}

function handleAdd() {
  uni.showToast({
    title: '新增日程待接入',
    icon: 'none'
  })
}

</script>

<style lang="scss" scoped>
.coach-schedule-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.page-scroll {
  min-height: 100vh;
}

.page-shell {
  padding: 0 24rpx;
}

.hero-head {
  margin-top: 48rpx;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20rpx;
}

.page-title {
  display: block;
  font-size: 62rpx;
  line-height: 1.08;
  font-weight: 900;
  letter-spacing: -2rpx;
}

.page-subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 18px;
  color: #474746;
  letter-spacing: 3rpx;
}

.view-toggle {
  flex-shrink: 0;
  display: flex;
  padding: 8rpx;
  border-radius: 18rpx;
  background: #f3f3f3;
}

.toggle-item {
  min-width: 112rpx;
  height: 64rpx;
  padding: 0 22rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5f5e5e;
  font-size: 24rpx;
  font-weight: 500;
}

.toggle-item-active {
  background: #ffffff;
  color: #1a1c1c;
  box-shadow: 0 4rpx 12rpx rgba(26, 28, 28, 0.06);
}

.date-strip-scroll {
  margin-top: 34rpx;
  width: 100%;
  white-space: nowrap;
}

.date-strip {
  display: inline-flex;
  gap: 18rpx;
  padding: 10rpx 0 18rpx;
}

.date-card {
  width: 124rpx;
  height: 156rpx;
  border-radius: 26rpx;
  background: #f3f3f3;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #5f5e5e;
  flex-shrink: 0;
}

.date-card.active {
  background: #ff6600;
  color: #ffffff;
  transform: scale(1.06);
  box-shadow: 0 14rpx 28rpx rgba(255, 102, 0, 0.28);
}

.date-dow {
  font-size: 18rpx;
  font-weight: 900;
  letter-spacing: 3rpx;
}

.date-day {
  margin-top: 12rpx;
  font-size: 22px;
  font-weight: 900;
}

.date-dot {
  width: 8rpx;
  height: 8rpx;
  margin-top: 10rpx;
  border-radius: 999rpx;
  background: #ffffff;
}

.timeline-section {
  position: relative;
  margin-top: 28rpx;
  padding-bottom: 40rpx;
}

.time-row {
  height: 160rpx;
  display: flex;
  align-items: flex-start;
}

.time-label {
  width: 96rpx;
  flex-shrink: 0;
  font-size: 24px;
  line-height: 1;
  font-weight: 500;
  color: #474746;
}

.time-line {
  flex: 1;
  margin-top: 18rpx;
  height: 2rpx;
  background: #e8e8e8;
}

.current-time-indicator {
  position: absolute;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  z-index: 10;
}

.current-time-text {
  width: 96rpx;
  flex-shrink: 0;
  font-size: 22rpx;
  font-weight: 700;
  color: #a33e00;
}

.current-time-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 999rpx;
  background: #a33e00;
}

.current-time-line {
  flex: 1;
  height: 2rpx;
  background: rgba(163, 62, 0, 0.4);
}

.session-card {
  position: absolute;
  left: 136rpx;
  right: 0;
  border-radius: 28rpx;
  overflow: hidden;
  box-shadow: 0 10rpx 24rpx rgba(26, 28, 28, 0.06);
}

.session-left-indicator {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 8rpx;
}

.indicator-tertiary {
  background: #009cfc;
}

.indicator-primary {
  background: #ff6600;
}

.indicator-secondary {
  background: #8e7164;
}

.session-content {
  height: 100%;
  padding: 22rpx 26rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.light {
  background: #f4fafe;
}

.primary {
  background: #ff6600;
  color: #ffffff;
}

.muted {
  background: #eeeeee;
  opacity: 0.68;
}

.session-top {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.session-title {
  display: block;
  font-size: 16px;
  line-height: 1.3;
  font-weight: 900;
  color: #1a1c1c;
}

.session-title-white {
  color: #ffffff;
  font-size: 17px;
}

.session-title-muted {
  color: #474746;
}

.session-meta-line {
  margin-top: 8rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.session-meta-text {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
  text-transform: uppercase;
  color: #5f5e5e;
}

.session-meta-text-white {
  color: rgba(255, 255, 255, 0.92);
}

.session-meta-text-muted {
  color: #8e7164;
}

.session-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.student-stack {
  display: flex;
  align-items: center;
}

.student-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 999rpx;
  border: 2rpx solid #ffffff;
  background: #ffffff;
}

.student-avatar.overlap {
  margin-left: -10rpx;
}

.student-more {
  min-width: 40rpx;
  height: 40rpx;
  margin-left: -10rpx;
  border-radius: 999rpx;
  border: 2rpx solid #ffffff;
  background: #e2e2e2;
  color: #1a1c1c;
  font-size: 16rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-pill {
  height: 44rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  font-weight: 900;
}

.status-pill-blue {
  background: #d0e4ff;
  color: #001d35;
}

.session-content-primary {
  padding-top: 24rpx;
  padding-bottom: 24rpx;
}

.session-primary-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18rpx;
}

.session-primary-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16rpx;
}

.student-profile {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.student-profile-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.32);
}

.student-copy-label {
  display: block;
  font-size: 16rpx;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.74);
}

.student-copy-name {
  display: block;
  margin-top: 4rpx;
  font-size: 26rpx;
  font-weight: 900;
  color: #ffffff;
}

.duration-block {
  text-align: right;
}

.duration-label {
  display: block;
  font-size: 16rpx;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.74);
}

.duration-value {
  display: block;
  margin-top: 4rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #ffffff;
}

.session-content-muted {
  padding-top: 24rpx;
  padding-bottom: 24rpx;
}

.session-muted-footer {
  display: flex;
  align-items: center;
}

.session-ended-text {
  font-size: 18rpx;
  font-weight: 700;
  color: #8e7164;
}

.bottom-space {
  height: 220rpx;
}

.fab {
  position: fixed;
  right: 24rpx;
  bottom: 180rpx;
  z-index: 40;
  width: 92rpx;
  height: 92rpx;
  border-radius: 999rpx;
  background: #ff6600;
  box-shadow: 0 14rpx 30rpx rgba(255, 102, 0, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
}

</style>
