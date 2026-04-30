<template>
  <view class="coach-students-page">
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
          :avatar="avatar"
          brand="KINETIC LOGIC"
          action-icon="notification-filled"
          @action="handleNotice"
        />

        <view class="hero-head">
          <text class="hero-kicker">SESSION ATTENDANCE</text>
          <text class="hero-title">{{ courseName || '学员名单' }}</text>
        </view>

        <view v-if="!courseId" class="state-card">
          <text class="state-title">缺少课程上下文</text>
          <text class="state-desc">请从“我的课程”或“预约明细”进入学员名单页面。</text>
          <view class="state-action" @tap="goCourses">
            <text>回我的课程</text>
          </view>
        </view>

        <template v-else>
          <view class="summary-card">
            <view class="summary-block">
              <text class="summary-label">COURSE ID</text>
              <view class="summary-line">
                <uni-icons type="calendar" size="16" color="#a33e00" />
                <text>#{{ courseId }}</text>
              </view>
            </view>

            <view class="summary-divider" />

            <view class="summary-block">
              <text class="summary-label">STATUS</text>
              <view class="summary-line">
                <uni-icons type="person" size="16" color="#a33e00" />
                <text>{{ total }} 条预约记录</text>
              </view>
            </view>
          </view>

          <view v-if="loading" class="state-card">
            <view class="spinner" />
            <text class="state-title">正在加载学员名单</text>
          </view>

          <view v-else-if="loadFailed" class="state-card">
            <text class="state-title">学员名单加载失败</text>
            <text class="state-desc">{{ errorMessage }}</text>
            <view class="state-action" @tap="loadStudents">
              <text>重新加载</text>
            </view>
          </view>

          <view v-else-if="students.length" class="list-card">
            <view
              v-for="student in students"
              :key="student.id"
              class="student-item"
            >
              <view class="student-main">
                <text class="student-name">{{ student.memberName || '未知学员' }}</text>
                <text class="student-id">{{ student.bookingNo || `预约 #${student.id}` }}</text>

                <view class="student-meta-grid">
                  <view class="student-meta-line">
                    <uni-icons type="refreshtime" size="15" color="#5f5e5e" />
                    <text>{{ timeRange(student) }}</text>
                  </view>
                  <view class="student-meta-line">
                    <uni-icons type="wallet" size="15" color="#5f5e5e" />
                    <text>{{ paymentStatusText(student.paymentStatus) }}</text>
                  </view>
                </view>

                <view class="status-row">
                  <view class="status-pill" :class="statusClass(student.status)">
                    <text>{{ statusText(student.status) }}</text>
                  </view>
                </view>

                <view class="status-actions">
                  <button v-if="canStart(student)" class="checkin-btn" @tap.stop="updateStatus(student, 3)">签到</button>
                  <button v-if="canComplete(student)" class="checkin-btn checkin-btn-blue" @tap.stop="updateStatus(student, 4)">完成</button>
                  <button v-if="canCancel(student)" class="checkin-btn checkin-btn-danger" @tap.stop="updateStatus(student, 0)">缺席/取消</button>
                </view>
              </view>
            </view>
          </view>

          <view v-else class="state-card">
            <text class="state-title">暂无学员记录</text>
            <text class="state-desc">当前课程还没有对应预约记录。</text>
          </view>
        </template>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <CoachTabBar current="courses" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'
import { getBookingsForCoach, updateBookingStatusForCoach, type CoachBookingItem } from '@/api/coachSelf'
import { COACH_UNBOUND_PATH, isCoachUnboundError } from '@/utils/coachAccess'
import { safeReLaunch } from '@/utils/safeRoute'
import {
  canCancelBooking,
  canCompleteBooking,
  canStartBooking,
  formatCoachBookingStatus,
  formatCoachBookingStatusClass,
  formatCoachBookingTime,
  formatCoachPaymentStatus
} from '@/utils/coachView'

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const avatar = '/static/placeholders/avatar.svg'
const loading = ref(true)
const refreshing = ref(false)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const students = ref<CoachBookingItem[]>([])
const courseId = ref<number | null>(null)
const courseName = ref('')
const total = ref(0)

function timeRange(item: CoachBookingItem) {
  return formatCoachBookingTime(item)
}

function paymentStatusText(status?: number) {
  return formatCoachPaymentStatus(status)
}

function statusText(status?: number) {
  return formatCoachBookingStatus(status)
}

function statusClass(status?: number) {
  return formatCoachBookingStatusClass(status)
}

function canStart(item: CoachBookingItem) {
  return canStartBooking(item)
}

function canComplete(item: CoachBookingItem) {
  return canCompleteBooking(item)
}

function canCancel(item: CoachBookingItem) {
  return canCancelBooking(item)
}

async function loadStudents() {
  if (!courseId.value) return
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'
  try {
    const result = await getBookingsForCoach({
      page: 1,
      size: 100,
      courseId: courseId.value
    })
    students.value = result.data || []
    total.value = Number(result.total || 0)
  } catch (error) {
    console.error('加载学员名单失败:', error)
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

async function updateStatus(item: CoachBookingItem, status: number) {
  try {
    await updateBookingStatusForCoach({
      id: item.id,
      status
    })
    uni.showToast({
      title: status === 0 ? '已登记缺席/取消' : '状态已更新',
      icon: 'success'
    })
    await loadStudents()
  } catch (error) {
    uni.showToast({
      title: error instanceof Error ? error.message : '更新失败',
      icon: 'none'
    })
  }
}

function handleNotice() {
  uni.showToast({
    title: '学员名单已接入预约数据',
    icon: 'none'
  })
}

function handleRefresh() {
  refreshing.value = true
  loadStudents()
}

function goCourses() {
  safeReLaunch('/pages/coach/courses', '/pages/coach/index')
}

onLoad((options) => {
  const id = Number(options?.courseId || 0)
  courseId.value = Number.isFinite(id) && id > 0 ? id : null
  courseName.value = options?.courseName ? decodeURIComponent(options.courseName) : ''
})

onShow(() => {
  if (courseId.value) {
    loadStudents()
  }
})
</script>

<style lang="scss" scoped>
.coach-students-page {
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
  margin-top: 46rpx;
  padding: 0 10rpx;
}

.hero-kicker {
  display: block;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 3rpx;
  color: #5f5e5e;
}

.hero-title {
  display: block;
  margin-top: 14rpx;
  font-size: 56rpx;
  line-height: 1.08;
  font-weight: 900;
}

.summary-card,
.list-card,
.state-card {
  margin-top: 28rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.05);
}

.summary-card {
  padding: 28rpx 30rpx;
  display: grid;
  grid-template-columns: 1fr 2rpx 1fr;
  gap: 22rpx;
}

.summary-label {
  display: block;
  font-size: 18px;
  letter-spacing: 3rpx;
  color: #474746;
}

.summary-line {
  margin-top: 16rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 22px;
  line-height: 1.45;
  font-weight: 900;
}

.summary-divider {
  width: 2rpx;
  background: #e2e2e2;
}

.list-card {
  padding: 18rpx 0;
}

.student-item {
  display: flex;
  gap: 16rpx;
  padding: 26rpx 24rpx;
}

.student-main {
  flex: 1;
  min-width: 0;
}

.student-name {
  display: block;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 900;
}

.student-id {
  display: block;
  margin-top: 10rpx;
  font-size: 18px;
  color: #5f5e5e;
}

.student-meta-grid {
  margin-top: 20rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14rpx 18rpx;
}

.student-meta-line {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #5f5e5e;
  font-size: 18px;
}

.status-row {
  margin-top: 20rpx;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 110rpx;
  height: 44rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
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

.status-actions {
  margin-top: 24rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.checkin-btn {
  margin: 0;
  min-width: 180rpx;
  height: 74rpx;
  padding: 0 28rpx;
  border: none;
  border-radius: 999rpx;
  background: #ff6600;
  color: #ffffff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 900;
}

.checkin-btn-blue {
  background: #1f4e9d;
}

.checkin-btn-danger {
  background: #ba1a1a;
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
  color: #1a1c1c;
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
  height: 170rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
