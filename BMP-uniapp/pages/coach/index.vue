<template>
  <view class="coach-home-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="page-shell">
        <CoachTopBar
          :status-bar-height="statusBarHeight"
          :avatar="coachAvatar"
          brand="Kinetic Logic"
          action-icon="person"
          @action="handleQuickAction('profile')"
        />

        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-title">正在加载教练工作台</text>
          <text class="state-desc">正在同步课程、预约和档案信息...</text>
        </view>

        <view v-else-if="loadFailed" class="state-card">
          <text class="state-title">工作台加载失败</text>
          <text class="state-desc">{{ errorMessage }}</text>
          <view class="state-action" @tap="loadDashboard">
            <text>重新加载</text>
          </view>
        </view>

        <template v-else>
          <view class="hero-section">
            <text class="coach-name">{{ coachName }}</text>
            <view class="hero-meta">
              <view class="rating-pill">
                <uni-icons type="star-filled" size="14" color="#561d00" />
                <text>{{ coachRating }}</text>
              </view>
              <view class="venue-line">
                <uni-icons type="location" size="15" color="#5f5e5e" />
                <text>{{ coachVenue }}</text>
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
              <text class="metric-label">待处理预约</text>
              <view class="metric-main">
                <text class="metric-value">{{ metrics.pendingBookings }}</text>
              </view>
            </view>

            <view class="metric-card">
              <text class="metric-label">累计学员</text>
              <view class="metric-main metric-attendance">
                <view class="attendance-row">
                  <text class="metric-value">{{ metrics.totalStudents }}</text>
                  <text class="attendance-total">人</text>
                </view>
                <view class="progress-track">
                  <view class="progress-fill" :style="{ width: studentProgress + '%' }" />
                </view>
              </view>
            </view>
          </view>

          <view class="actions-row">
            <view class="action-card action-card-primary" @tap="handleQuickAction('courses')">
              <text class="action-title vertical-text">我的课程</text>
              <view class="action-icon">
                <uni-icons type="compose" size="24" color="#ffffff" />
              </view>
            </view>

            <view class="action-card" @tap="handleQuickAction('bookings')">
              <text class="action-title vertical-text">预约明细</text>
              <view class="action-icon action-icon-dark">
                <uni-icons type="list" size="22" color="#1a1c1c" />
              </view>
            </view>

            <view class="action-card" @tap="handleQuickAction('profile')">
              <text class="action-title vertical-text">我的档案</text>
              <view class="action-icon action-icon-dark">
                <uni-icons type="person" size="22" color="#1a1c1c" />
              </view>
            </view>
          </view>

          <view class="agenda-head">
            <text class="agenda-title">今日日程</text>
            <text class="agenda-date">{{ agendaDate }}</text>
          </view>

          <view v-if="scheduleList.length" class="timeline-list">
            <view
              v-for="item in scheduleList"
              :key="item.id"
              class="timeline-card"
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
            </view>
          </view>

          <view v-else class="empty-card">
            <text class="empty-title">今天还没有排课</text>
            <text class="empty-desc">空下来也不错，可以整理一下后面的课程安排。</text>
          </view>
        </template>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <CoachTabBar current="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'
import { getBookingsForCoach, getCurrentCoach, getMyCourses, type CoachCourseItem, type CoachProfile } from '@/api/coachSelf'
import { COACH_UNBOUND_PATH, isCoachUnboundError, resolveCoachAvatar } from '@/utils/coachAccess'
import { safeReLaunch } from '@/utils/safeRoute'
import {
  buildCoachBookingsUrl,
  compareCoachCourseTime,
  formatDateKey,
  formatStudentText,
  normalizeTime
} from '@/utils/coachView'

type ScheduleCard = {
  id: number
  startTime: string
  endTime: string
  courseName: string
  courtName: string
  studentText: string
}

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const loading = ref(true)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const coach = ref<CoachProfile | null>(null)
const metrics = ref({
  todayCourses: 0,
  pendingBookings: 0,
  totalStudents: 0
})
const scheduleList = ref<ScheduleCard[]>([])

const coachName = computed(() => coach.value?.coachName || '教练')
const coachVenue = computed(() => coach.value?.venueName || '暂未绑定场馆')
const coachRating = computed(() => {
  const rating = Number(coach.value?.rating ?? 0)
  return rating > 0 ? rating.toFixed(1) : '暂无评分'
})
const coachAvatar = computed(() => resolveCoachAvatar(coach.value?.avatar))
const studentProgress = computed(() => Math.min(100, Number(metrics.value.totalStudents || 0)))
const agendaDate = computed(() => {
  const now = new Date()
  const week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'][now.getDay()]
  return `${now.getMonth() + 1}月${now.getDate()}日 · ${week}`
})

async function loadDashboard() {
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'

  const today = formatDateKey(new Date())
  try {
    const [coachInfo, todayCourses, pendingBookings] = await Promise.all([
      getCurrentCoach(),
      getMyCourses({
        page: 1,
        size: 50,
        startTime: `${today} 00:00:00`,
        endTime: `${today} 23:59:59`
      }),
      getBookingsForCoach({
        page: 1,
        size: 1,
        status: 2
      })
    ])

    coach.value = coachInfo
    metrics.value = {
      todayCourses: Number(todayCourses.total || 0),
      pendingBookings: Number(pendingBookings.total || 0),
      totalStudents: Number(coachInfo.totalStudents || 0)
    }
    scheduleList.value = [...(todayCourses.data || [])]
      .sort(compareCoachCourseTime)
      .slice(0, 6)
      .map((item) => ({
        id: item.id,
        startTime: normalizeTime(item.startTime),
        endTime: normalizeTime(item.endTime),
        courseName: item.courseName || '未命名课程',
        courtName: item.courtName || item.venueName || '待安排场地',
        studentText: formatStudentText(item)
      }))
  } catch (error) {
    console.error('加载教练工作台失败:', error)
    if (isCoachUnboundError(error)) {
      safeReLaunch(COACH_UNBOUND_PATH, COACH_UNBOUND_PATH)
      return
    }
    loadFailed.value = true
    errorMessage.value = error instanceof Error ? error.message : '请稍后重试'
  } finally {
    loading.value = false
  }
}

function handleQuickAction(type: 'courses' | 'bookings' | 'profile') {
  const urlMap: Record<typeof type, string> = {
    courses: '/pages/coach/courses',
    bookings: buildCoachBookingsUrl(undefined, undefined, 2),
    profile: '/pages/coach/profile'
  }
  safeReLaunch(urlMap[type], '/pages/coach/index')
}

onShow(() => {
  loadDashboard()
})
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

.metric-card,
.state-card,
.empty-card {
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 26rpx rgba(26, 28, 28, 0.04);
}

.metric-card {
  min-height: 196rpx;
  padding: 28rpx 26rpx;
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
  min-height: 146rpx;
  border-radius: 26rpx;
  background: #ffffff;
  padding: 26rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.04);
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

.state-card,
.empty-card {
  margin-top: 40rpx;
  padding: 80rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.state-title,
.empty-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.state-desc,
.empty-desc {
  margin-top: 18rpx;
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

.spinner {
  width: 52rpx;
  height: 52rpx;
  border: 5rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 999rpx;
  animation: spin 0.8s linear infinite;
}

.bottom-space {
  height: 170rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
