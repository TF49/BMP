<template>
  <view class="coach-schedule-page">
    <scroll-view
      class="page-scroll"
      scroll-y
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="page-shell">
        <CoachTopBar
          :status-bar-height="statusBarHeight"
          :avatar="coachAvatar"
          brand="Kinetic Logic"
          action-icon="calendar"
          @action="handleCalendar"
        />

        <view class="hero-head">
          <view>
            <text class="page-title">教练日程</text>
            <text class="page-subtitle">{{ currentMonthLabel }}</text>
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
              :key="item.iso"
              class="date-card"
              :class="{ active: item.iso === selectedDate }"
              @tap="selectDate(item.iso)"
            >
              <text class="date-dow">{{ item.dow }}</text>
              <text class="date-day">{{ item.day }}</text>
              <view v-if="item.iso === selectedDate" class="date-dot" />
            </view>
          </view>
        </scroll-view>

        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-title">正在加载课表</text>
        </view>

        <view v-else-if="loadFailed" class="state-card">
          <text class="state-title">课表加载失败</text>
          <text class="state-desc">{{ errorMessage }}</text>
          <view class="state-action" @tap="loadSchedule">
            <text>重新加载</text>
          </view>
        </view>

        <view v-else-if="sessions.length" class="timeline-section">
          <view
            v-for="item in sessions"
            :key="item.id"
            class="timeline-card"
            @tap="openSession(item)"
          >
            <view class="time-block">
              <text class="time-start">{{ item.startTime }}</text>
              <text class="time-end">{{ item.endTime }}</text>
            </view>

            <view class="course-block">
              <text class="timeline-course-name">{{ item.courseName }}</text>
              <view class="course-meta-row">
                <view class="course-meta-item">
                  <uni-icons type="location" size="15" color="#5f5e5e" />
                  <text>{{ item.courtName }}</text>
                </view>
                <view class="course-meta-item">
                  <uni-icons type="person" size="15" color="#5f5e5e" />
                  <text>{{ item.studentText }}</text>
                </view>
              </view>
            </view>

            <view class="status-pill" :class="item.statusClass">
              <text>{{ item.statusText }}</text>
            </view>
          </view>
        </view>

        <view v-else class="state-card">
          <text class="state-title">当天暂无课程</text>
          <text class="state-desc">这一天的课表是空的，可以安心喘口气。</text>
        </view>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <CoachTabBar current="schedule" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'
import { getCurrentCoach, getMyCourses, type CoachCourseItem } from '@/api/coachSelf'
import { COACH_UNBOUND_PATH, isCoachUnboundError, resolveCoachAvatar } from '@/utils/coachAccess'
import { safeReLaunch } from '@/utils/safeRoute'
import {
  compareCoachCourseTime,
  formatCoachCourseStatus,
  formatCoachCourseStatusClass,
  formatCoachCourseTime,
  formatDateKey,
  formatStudentText
} from '@/utils/coachView'

type DateStripItem = {
  iso: string
  dow: string
  day: string
}

type SessionItem = {
  id: number
  courseId: number
  courseName: string
  startTime: string
  endTime: string
  courtName: string
  studentText: string
  statusText: string
  statusClass: string
}

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const loading = ref(true)
const refreshing = ref(false)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const coachAvatar = ref('/static/placeholders/avatar.svg')
const selectedDate = ref(formatDateKey(new Date()))
const sessions = ref<SessionItem[]>([])

const currentMonthLabel = computed(() => {
  const current = new Date(selectedDate.value.replace(/-/g, '/'))
  return `${current.getFullYear()}-${String(current.getMonth() + 1).padStart(2, '0')}`
})

const dateStrip = computed<DateStripItem[]>(() => {
  const current = new Date(selectedDate.value.replace(/-/g, '/'))
  const result: DateStripItem[] = []
  for (let offset = -3; offset <= 3; offset += 1) {
    const next = new Date(current)
    next.setDate(current.getDate() + offset)
    const iso = formatDateKey(next)
    result.push({
      iso,
      dow: ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'][next.getDay()],
      day: String(next.getDate()).padStart(2, '0')
    })
  }
  return result
})

async function loadSchedule() {
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'
  try {
    const [coach, result] = await Promise.all([
      getCurrentCoach(),
      getMyCourses({
        page: 1,
        size: 100,
        startTime: `${selectedDate.value} 00:00:00`,
        endTime: `${selectedDate.value} 23:59:59`
      })
    ])

    coachAvatar.value = resolveCoachAvatar(coach.avatar)
    sessions.value = [...(result.data || [])]
      .sort(compareCoachCourseTime)
      .map((item: CoachCourseItem) => ({
        id: item.id,
        courseId: item.id,
        courseName: item.courseName || '未命名课程',
        startTime: formatCoachCourseTime(item).split(' - ')[0],
        endTime: formatCoachCourseTime(item).split(' - ')[1],
        courtName: item.courtName || item.venueName || '待安排场地',
        studentText: formatStudentText(item),
        statusText: formatCoachCourseStatus(item.status),
        statusClass: formatCoachCourseStatusClass(item.status)
      }))
  } catch (error) {
    console.error('加载教练课表失败:', error)
    if (isCoachUnboundError(error)) {
      safeReLaunch(COACH_UNBOUND_PATH, COACH_UNBOUND_PATH)
      return
    }
    loadFailed.value = true
    errorMessage.value = error instanceof Error ? error.message : '请稍后重试'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function selectDate(iso: string) {
  if (selectedDate.value === iso) return
  selectedDate.value = iso
  loadSchedule()
}

function handleCalendar() {
  uni.showToast({
    title: '当前按日查看课表',
    icon: 'none'
  })
}

function handleWeekView() {
  uni.showToast({
    title: '周视图将在后续版本开放',
    icon: 'none'
  })
}

function openSession(item: SessionItem) {
  uni.navigateTo({
    url: `/pages/coach/bookings?courseId=${item.courseId}&courseName=${encodeURIComponent(item.courseName)}`
  })
}

function handleRefresh() {
  refreshing.value = true
  loadSchedule()
}

onShow(() => {
  loadSchedule()
})
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

.timeline-section,
.state-card {
  margin-top: 24rpx;
}

.timeline-card,
.state-card {
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.05);
}

.timeline-card {
  padding: 26rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.timeline-card + .timeline-card {
  margin-top: 16rpx;
}

.time-block {
  width: 120rpx;
  text-align: center;
  flex-shrink: 0;
}

.time-start {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
}

.time-end {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #5f5e5e;
}

.course-block {
  flex: 1;
  min-width: 0;
}

.timeline-course-name {
  display: block;
  font-size: 30rpx;
  line-height: 1.3;
  font-weight: 900;
}

.course-meta-row {
  margin-top: 12rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.course-meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  color: #5f5e5e;
}

.status-pill {
  min-width: 110rpx;
  height: 48rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 800;
}

.status-pill.warning {
  background: #fff1e8;
  color: #a33e00;
}

.status-pill.success {
  background: #e6f6ea;
  color: #1f7a37;
}

.status-pill.neutral {
  background: #ededed;
  color: #5f5e5e;
}

.state-card {
  padding: 80rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.spinner {
  width: 52rpx;
  height: 52rpx;
  border: 5rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 999rpx;
  animation: spin 0.8s linear infinite;
}

.state-title {
  margin-top: 22rpx;
  font-size: 34rpx;
  font-weight: 900;
}

.state-desc {
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #5f5e5e;
}

.state-action {
  min-width: 220rpx;
  height: 78rpx;
  margin-top: 28rpx;
  padding: 0 26rpx;
  border-radius: 999rpx;
  background: #ff6600;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  font-weight: 800;
}

.bottom-space {
  height: 180rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
