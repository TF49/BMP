<template>
  <view class="student-detail-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false" refresher-enabled :refresher-triggered="refreshing" @refresherrefresh="handleRefresh">
      <view class="page-shell">
        <CoachTopBar :status-bar-height="statusBarHeight" :avatar="coachAvatar" brand="KINETIC LOGIC" />

        <view v-if="loading" class="state-card"><view class="spinner" /><text class="state-title">正在加载学员详情</text></view>
        <view v-else-if="loadFailed" class="state-card"><text class="state-title">学员详情加载失败</text><text class="state-desc">{{ errorMessage }}</text><view class="state-action" @tap="loadAll">重新加载</view></view>
        <template v-else-if="student">
          <view v-if="student.risk" class="risk-banner">该学员出勤率偏低或已超过14天无完成课程，请及时关注</view>
          <view class="profile-card">
            <image class="profile-avatar" :src="profileAvatar" mode="aspectFill" @error="avatarFailed = true" />
            <view class="profile-main">
              <view class="profile-name-row"><text class="profile-name">{{ student.memberName }}</text><text class="member-tag">{{ memberLabel }}</text></view>
              <text class="profile-phone">{{ maskPhone(student.phone) }}</text>
              <text class="expire-text" :class="expireClass">{{ expireText }}</text>
            </view>
            <view v-if="student.phone" class="phone-button" @tap="callStudent"><uni-icons type="phone-filled" size="24" color="#fff" /></view>
          </view>

          <scroll-view class="tab-scroll" scroll-x :show-scrollbar="false">
            <view class="tab-row">
              <view v-for="tab in tabs" :key="tab.key" class="tab-item" :class="{ active: activeTab === tab.key }" @tap="selectTab(tab.key)">{{ tab.label }}</view>
            </view>
          </scroll-view>

          <view v-if="tabLoading" class="state-card compact"><view class="spinner" /></view>
          <view v-else-if="activeTab === 'overview'" class="tab-panel">
            <view class="kpi-grid">
              <view class="kpi-card"><text class="kpi-label">累计课时</text><text class="kpi-value">{{ numberText(student.totalHours) }}h</text></view>
              <view class="kpi-card"><text class="kpi-label">出勤率</text><text class="kpi-value">{{ numberText(student.attendanceRate) }}%</text></view>
              <view class="kpi-card"><text class="kpi-label">我带课消费</text><text class="kpi-value">¥{{ moneyText(student.totalConsumptionForCoach) }}</text></view>
              <view class="kpi-card"><text class="kpi-label">累计预约</text><text class="kpi-value">{{ student.totalBookings || 0 }}</text></view>
            </view>
            
            <view class="info-card">
              <text class="info-title">近30日出勤率</text>
              <text class="kpi-value-large">{{ attendanceRate30d !== null ? attendanceRate30d + '%' : '暂无数据' }}</text>
              <text class="info-desc">基于过去30天内已完成和缺席的课程统计</text>
            </view>

            <view class="info-card">
              <text class="info-title">最近 7 次出勤趋势</text>
              <view v-if="miniTimeline.length" class="timeline-dots-row">
                <view v-for="(item, index) in miniTimeline" :key="item.bookingId" class="timeline-dot-col">
                  <view class="timeline-dot-dot" :class="dotClass(item.attendanceStatus)">
                    <text class="dot-index">{{ miniTimeline.length - index }}</text>
                  </view>
                  <text class="dot-date">{{ item.courseDate.slice(5) }}</text>
                </view>
              </view>
              <view v-else class="timeline-empty">暂无考勤数据</view>
            </view>

            <view class="info-card">
              <text class="info-title">课程时间线</text>
              <text>最近一次：{{ formatDateTime(student.lastLessonTime) }}</text>
              <text>下一次：{{ formatDateTime(student.nextLessonTime) }}</text>
            </view>
          </view>

          <view v-else-if="activeTab === 'progress'" class="tab-panel">
            <view v-if="progress.length" class="stack-list">
              <view v-for="item in progress" :key="item.courseName" class="data-card">
                <view class="data-head"><text class="data-title">{{ item.courseName }}</text><text>{{ item.completedLessons }}/{{ item.totalBookings }}</text></view>
                <view class="progress-track"><view class="progress-fill" :style="{ width: progressWidth(item.completedLessons, item.totalBookings) }" /></view>
                <text class="data-meta">累计 {{ numberText(item.totalHours) }}h · 最近 {{ formatDateTime(item.lastLessonTime) }}</text>
              </view>
            </view>
            <view v-else class="state-card compact"><text class="state-title">暂无系统课包进度</text></view>
            <view class="notice-card"><uni-icons type="info" size="18" color="#ff6600" /><text>进度基于课程名称自动聚合统计，非正式课程包。如需购买1v1私教包，请联系管理员。</text></view>
          </view>

          <view v-else-if="activeTab === 'schedule'" class="tab-panel">
            <view class="sub-tab-row">
              <view class="sub-tab-item" :class="{ active: scheduleSubTab === 'upcoming' }" @tap="scheduleSubTab = 'upcoming'">即将上课</view>
              <view class="sub-tab-item" :class="{ active: scheduleSubTab === 'history' }" @tap="scheduleSubTab = 'history'">历史课程</view>
            </view>
            
            <view v-if="currentSchedules.length" class="stack-list">
              <view v-for="item in currentSchedules" :key="item.bookingId" class="data-card" @tap="openBooking(item.bookingId)">
                <view class="data-head">
                  <text class="data-title">{{ item.courseName }}</text>
                  <text class="status-text" :class="bookingStatusClass(item.bookingStatus)">{{ bookingStatusText(item.bookingStatus) }}</text>
                </view>
                <text class="data-meta">{{ item.courseDate }} {{ shortTime(item.startTime) }}—{{ shortTime(item.endTime) }}</text>
                <text class="data-meta">考勤：
                  <text :class="attendanceStatusClass(item.attendanceStatus)">{{ attendanceStatusText(item.attendanceStatus) }}</text>
                </text>
              </view>
            </view>
            <view v-else class="state-card compact">
              <text class="state-title">{{ scheduleSubTab === 'upcoming' ? '暂无即将上课的课程' : '暂无历史课程安排' }}</text>
            </view>
          </view>

          <view v-else-if="activeTab === 'attendance'" class="tab-panel">
            <view v-if="monthlyAttendanceList.length" class="stack-list">
              <view v-for="group in monthlyAttendanceList" :key="group.monthStr" class="month-group-container">
                <view class="month-group-header">
                  <text class="month-group-title">{{ group.monthStr }}</text>
                  <text class="month-group-summary">完成 {{ group.completed }} · 缺席 {{ group.absent }}</text>
                </view>
                <view class="month-items">
                  <view v-for="item in group.items" :key="item.bookingId" class="data-card">
                    <view class="data-head">
                      <text class="data-title">{{ item.courseDate.slice(5) }} · {{ item.courseName }}</text>
                      <text class="status-text" :class="attendanceStatusClass(item.attendanceStatus)">
                        {{ attendanceStatusText(item.attendanceStatus) }}
                      </text>
                    </view>
                    <text class="data-meta">预约：
                      <text :class="bookingStatusClass(item.bookingStatus)">{{ bookingStatusText(item.bookingStatus) }}</text>
                      · {{ item.durationMinutes || 0 }}分钟
                    </text>
                    <text v-if="item.remark" class="data-meta">备注：{{ item.remark }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view v-else class="state-card compact"><text class="state-title">暂无出勤记录</text></view>
          </view>

          <view v-else-if="activeTab === 'consume'" class="tab-panel">
            <view v-if="consumeRecords.length" class="stack-list">
              <view v-for="item in consumeRecords" :key="item.id" class="data-card">
                <view class="data-head">
                  <text class="data-title">{{ item.courseName }}</text>
                  <text class="status-text consume-amount">¥{{ moneyText(item.amount) }}</text>
                </view>
                <text class="data-meta">订单号：{{ item.bookingId }}</text>
                <text class="data-meta">支付方式：{{ item.paymentMethod || '其他' }}</text>
                <text class="data-meta">消费时间：{{ formatDateTime(item.createTime) }}</text>
                <text v-if="item.description" class="data-meta">描述：{{ item.description }}</text>
                <text v-if="item.remark" class="data-meta">备注：{{ item.remark }}</text>
              </view>
            </view>
            <view v-else class="state-card compact"><text class="state-title">暂无消费记录</text></view>
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
import { onLoad, onShow } from '@dcloudio/uni-app'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'
import {
  getCoachStudentAttendance,
  getCoachStudentDetail,
  getCoachStudentProgress,
  getCoachStudentSchedule,
  getCoachStudentConsumeRecords,
  getCurrentCoach,
  type CoachStudentAttendanceItem,
  type CoachStudentDetail,
  type CoachStudentProgress,
  type CoachStudentScheduleItem,
  type CoachStudentConsumeRecord
} from '@/api/coachSelf'
import { resolveCoachAvatar } from '@/utils/coachAccess'
import { DEFAULT_COACH_STUDENT_AVATAR, getStudentAvatarWithFallback, maskPhone } from '@/utils/coachStudents'

type TabKey = 'overview' | 'progress' | 'schedule' | 'attendance' | 'consume'
const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight || 20)
const coachAvatar = ref(DEFAULT_COACH_STUDENT_AVATAR)
const memberId = ref(0)
const loading = ref(true)
const refreshing = ref(false)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const tabLoading = ref(false)
const avatarFailed = ref(false)
const student = ref<CoachStudentDetail | null>(null)
const progress = ref<CoachStudentProgress[]>([])
const schedule = ref<CoachStudentScheduleItem[]>([])
const attendance = ref<CoachStudentAttendanceItem[]>([])
const consumeRecords = ref<CoachStudentConsumeRecord[]>([])
const activeTab = ref<TabKey>('overview')
const scheduleSubTab = ref<'upcoming' | 'history'>('upcoming')

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: 'overview', label: '概览' },
  { key: 'progress', label: '训练进度' },
  { key: 'schedule', label: '课程安排' },
  { key: 'attendance', label: '出勤记录' },
  { key: 'consume', label: '消费记录' }
]

const profileAvatar = computed(() => avatarFailed.value ? DEFAULT_COACH_STUDENT_AVATAR : getStudentAvatarWithFallback(student.value?.avatar))
const memberLabel = computed(() => student.value?.memberType === 'MEMBER' ? `会员 Lv.${student.value.memberLevel || 1}` : '普通学员')
const expireText = computed(() => {
  if (student.value?.memberType !== 'MEMBER') return '普通学员'
  if (!student.value.expireTime) return '未设置到期时间'
  const days = Math.ceil((new Date(student.value.expireTime).getTime() - Date.now()) / 86400000)
  if (days < 0) return '会员已过期'
  if (days <= 30) return `会员将在 ${days} 天后到期`
  return `会员有效期至 ${student.value.expireTime.slice(0, 10)}`
})
const expireClass = computed(() => expireText.value.includes('已过期') ? 'danger' : expireText.value.includes('天后') ? 'warning' : '')

const attendanceRate30d = computed(() => {
  const thirtyDaysAgo = new Date()
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)
  const thirtyDaysAgoStr = thirtyDaysAgo.toISOString().slice(0, 10)

  const recentRecords = attendance.value.filter(item => {
    return item.courseDate >= thirtyDaysAgoStr && (item.attendanceStatus === 2 || item.attendanceStatus === 3)
  })

  if (recentRecords.length === 0) return null

  const completed = recentRecords.filter(item => item.attendanceStatus === 2).length
  return Math.round((completed * 100) / recentRecords.length)
})

const miniTimeline = computed(() => {
  return attendance.value.slice(0, 7)
})

const currentSchedules = computed(() => {
  const now = new Date()
  const nowStr = now.toISOString().slice(0, 10)
  const nowTimeStr = now.toTimeString().slice(0, 8)
  
  if (scheduleSubTab.value === 'upcoming') {
    return schedule.value.filter(item => {
      if (item.courseDate > nowStr) return true
      if (item.courseDate === nowStr) {
        return (item.startTime || '00:00:00') >= nowTimeStr
      }
      return false
    })
  } else {
    return schedule.value.filter(item => {
      if (item.courseDate < nowStr) return true
      if (item.courseDate === nowStr) {
        return (item.startTime || '00:00:00') < nowTimeStr
      }
      return false
    })
  }
})

interface MonthlyAttendance {
  monthStr: string
  completed: number
  absent: number
  items: CoachStudentAttendanceItem[]
}
const monthlyAttendanceList = computed<MonthlyAttendance[]>(() => {
  const groups: Record<string, CoachStudentAttendanceItem[]> = {}
  attendance.value.forEach(item => {
    const month = item.courseDate ? item.courseDate.slice(0, 7) : '其他'
    if (!groups[month]) {
      groups[month] = []
    }
    groups[month].push(item)
  })

  return Object.keys(groups).sort((a, b) => b.localeCompare(a)).map(month => {
    const items = groups[month]
    const completed = items.filter(item => item.attendanceStatus === 2).length
    const absent = items.filter(item => item.attendanceStatus === 3).length
    
    let monthStr = month
    if (month !== '其他') {
      const [year, m] = month.split('-')
      monthStr = `${year}年${m}月`
    }
    return {
      monthStr,
      completed,
      absent,
      items
    }
  })
})

async function loadAll() {
  if (!memberId.value) return
  loading.value = true
  loadFailed.value = false
  try {
    const [coach, detail, attResult] = await Promise.all([
      getCurrentCoach(),
      getCoachStudentDetail(memberId.value),
      getCoachStudentAttendance(memberId.value, { page: 1, size: 100 })
    ])
    coachAvatar.value = resolveCoachAvatar(coach.avatar)
    student.value = detail
    attendance.value = attResult.data || []
    avatarFailed.value = false
    await loadActiveTab()
  } catch (error) {
    loadFailed.value = true
    errorMessage.value = error instanceof Error ? error.message : '请稍后重试'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

async function loadActiveTab() {
  if (!memberId.value) return
  if (activeTab.value === 'overview') return
  tabLoading.value = true
  try {
    if (activeTab.value === 'progress') progress.value = await getCoachStudentProgress(memberId.value)
    if (activeTab.value === 'schedule') schedule.value = (await getCoachStudentSchedule(memberId.value, { page: 1, size: 100 })).data || []
    if (activeTab.value === 'attendance') attendance.value = (await getCoachStudentAttendance(memberId.value, { page: 1, size: 100 })).data || []
    if (activeTab.value === 'consume') consumeRecords.value = (await getCoachStudentConsumeRecords(memberId.value, { page: 1, size: 100 })).data || []
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '加载失败', icon: 'none' })
  } finally {
    tabLoading.value = false
  }
}

function selectTab(key: TabKey) { activeTab.value = key; loadActiveTab() }
function handleRefresh() { refreshing.value = true; loadAll() }
function callStudent() {
  const phone = student.value?.phone
  if (!phone) return
  uni.showModal({
    title: '确认拨打',
    content: `即将拨打 ${phone}`,
    confirmText: '拨打',
    success: (result) => { if (result.confirm) uni.makePhoneCall({ phoneNumber: phone }) }
  })
}
function openBooking(bookingId: number) { uni.navigateTo({ url: `/pages/coach/bookings?id=${bookingId}` }) }
function formatDateTime(value?: string) { return value ? value.slice(0, 16) : '暂无记录' }
function numberText(value?: number) { return Number(value || 0).toFixed(value && value % 1 ? 1 : 0) }
function moneyText(value?: number) { return Number(value || 0).toFixed(2) }
function shortTime(value?: string) { return value ? value.slice(0, 5) : '--:--' }
function progressWidth(done?: number, total?: number) { return `${Math.min(100, total ? Number(done || 0) * 100 / total : 0)}%` }
function bookingStatusText(status?: number) { return ['已取消', '待支付', '已支付', '进行中', '已完成'][Number(status ?? -1)] || '未知' }
function attendanceStatusText(status?: number) { return ['未登记', '已签到', '已完成', '缺席'][Number(status ?? 0)] || '未知' }

function dotClass(status?: number) {
  return ['dot-unrecorded', 'dot-checkin', 'dot-completed', 'dot-absent'][Number(status ?? 0)] || ''
}
function bookingStatusClass(status?: number) {
  return ['status-cancelled', 'status-unpaid', 'status-paid', 'status-ongoing', 'status-completed'][Number(status ?? -1)] || ''
}
function attendanceStatusClass(status?: number) {
  return ['status-unrecorded', 'status-checkin', 'status-completed', 'status-absent'][Number(status ?? 0)] || ''
}

onLoad(options => {
  memberId.value = Number(options?.id || 0)
  if (!Number.isFinite(memberId.value) || memberId.value <= 0) {
    loading.value = false
    loadFailed.value = true
    errorMessage.value = '缺少有效的学员ID'
  }
})
onShow(loadAll)
</script>

<style lang="scss" scoped>
.student-detail-page { min-height: 100vh; background: #f9f9f9; color: #1a1c1c; }
.page-scroll { min-height: 100vh; }
.page-shell { padding: 0 24rpx; }
.risk-banner { margin-top: 24rpx; padding: 20rpx 24rpx; border-radius: 20rpx; background: #ffe5e5; color: #9f1010; font-size: 22rpx; font-weight: 800; }
.profile-card, .state-card, .kpi-card, .info-card, .data-card, .notice-card, .month-summary { background: #fff; border-radius: 26rpx; box-shadow: 0 8rpx 24rpx rgba(26,28,28,.05); }
.profile-card { margin-top: 26rpx; padding: 26rpx; display: flex; align-items: center; gap: 20rpx; }
.profile-avatar { width: 120rpx; height: 120rpx; border-radius: 30rpx; background: #ededed; }
.profile-main { flex: 1; min-width: 0; }
.profile-name-row { display: flex; align-items: center; flex-wrap: wrap; gap: 10rpx; }
.profile-name { font-size: 38rpx; font-weight: 900; }
.member-tag { padding: 7rpx 12rpx; border-radius: 999rpx; background: #fff1e8; color: #a33e00; font-size: 19rpx; font-weight: 800; }
.profile-phone, .expire-text { display: block; margin-top: 10rpx; color: #5f5e5e; font-size: 22rpx; }
.expire-text.warning { color: #a33e00; }.expire-text.danger { color: #ba1a1a; }
.phone-button { width: 72rpx; height: 72rpx; border-radius: 22rpx; background: #ff6600; display: flex; align-items: center; justify-content: center; }
.tab-scroll { margin-top: 24rpx; white-space: nowrap; }
.tab-row { display: inline-flex; gap: 12rpx; }
.tab-item { padding: 16rpx 24rpx; border-radius: 999rpx; background: #ededed; color: #5f5e5e; font-size: 23rpx; }
.tab-item.active { background: #1a1c1c; color: #fff; font-weight: 800; }
.tab-panel { margin-top: 22rpx; }
.kpi-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; }
.kpi-card { min-height: 150rpx; padding: 22rpx; display: flex; flex-direction: column; justify-content: space-between; }
.kpi-label { color: #5f5e5e; font-size: 22rpx; }.kpi-value { font-size: 38rpx; font-weight: 900; }
.info-card { margin-top: 16rpx; padding: 24rpx; display: flex; flex-direction: column; gap: 12rpx; color: #5f5e5e; font-size: 23rpx; }
.info-title { color: #1a1c1c; font-size: 28rpx; font-weight: 900; }
.info-desc { font-size: 20rpx; color: #888; margin-top: 4rpx; }
.stack-list { display: flex; flex-direction: column; gap: 14rpx; }
.data-card { padding: 24rpx; }
.data-head { display: flex; align-items: center; justify-content: space-between; gap: 16rpx; }
.data-title { flex: 1; font-size: 28rpx; font-weight: 900; }
.status-text { font-size: 22rpx; font-weight: 800; }
.consume-amount { font-size: 28rpx; font-weight: 900; color: #ff6600; }
.data-meta { display: block; margin-top: 12rpx; color: #5f5e5e; font-size: 22rpx; }
.progress-track { height: 10rpx; margin-top: 18rpx; border-radius: 999rpx; background: #ededed; overflow: hidden; }.progress-fill { height: 100%; background: #ff6600; }
.notice-card { margin-top: 16rpx; padding: 22rpx; display: flex; gap: 12rpx; color: #5f5e5e; font-size: 22rpx; line-height: 1.6; background: #fff5ef; }
.state-card { margin-top: 28rpx; padding: 72rpx 32rpx; text-align: center; display: flex; flex-direction: column; align-items: center; }.state-card.compact { padding: 40rpx 24rpx; }
.state-title { margin-top: 16rpx; font-size: 30rpx; font-weight: 900; }.state-desc { margin-top: 12rpx; color: #5f5e5e; font-size: 23rpx; }.state-action { margin-top: 22rpx; padding: 18rpx 28rpx; border-radius: 999rpx; background: #ff6600; color: #fff; }
.spinner { width: 48rpx; height: 48rpx; border: 5rpx solid #ededed; border-top-color: #ff6600; border-radius: 50%; animation: spin .8s linear infinite; }
.bottom-space { height: 180rpx; }

/* Sub Tabs for schedule */
.sub-tab-row { display: flex; gap: 8rpx; margin-bottom: 20rpx; padding: 6rpx; background: #e0e0e0; border-radius: 16rpx; width: fit-content; }
.sub-tab-item { padding: 10rpx 24rpx; border-radius: 12rpx; font-size: 22rpx; color: #5f5e5e; }
.sub-tab-item.active { background: #fff; color: #1a1c1c; font-weight: 800; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.05); }

/* Color coding status classes */
.status-cancelled { color: #888; }
.status-unpaid { color: #ff6600; }
.status-paid { color: #1f7a37; }
.status-ongoing { color: #ff6600; }
.status-completed { color: #1f7a37; }
.status-unrecorded { color: #888; }
.status-checkin { color: #ff6600; }
.status-absent { color: #ba1a1a; }

/* Month Group Styles */
.month-group-container {
  margin-bottom: 24rpx;
}
.month-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 8rpx;
  margin-bottom: 12rpx;
}
.month-group-title {
  font-size: 28rpx;
  font-weight: 900;
  color: #1a1c1c;
}
.month-group-summary {
  font-size: 22rpx;
  font-weight: 800;
  color: #a33e00;
}
.month-items {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

/* Overview Enhancements styles */
.kpi-value-large {
  font-size: 48rpx;
  font-weight: 900;
  color: #1a1c1c;
}
.timeline-dots-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  position: relative;
  padding: 0 10rpx;
}
.timeline-dots-row::before {
  content: '';
  position: absolute;
  top: 22rpx;
  left: 30rpx;
  right: 30rpx;
  height: 4rpx;
  background: #ededed;
  z-index: 1;
}
.timeline-dot-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  z-index: 2;
}
.timeline-dot-dot {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  font-weight: 800;
  color: #fff;
  background: #5f5e5e;
}
.timeline-dot-dot.dot-unrecorded { background: #b0b0b0; }
.timeline-dot-dot.dot-checkin { background: #ff6600; }
.timeline-dot-dot.dot-completed { background: #1f7a37; }
.timeline-dot-dot.dot-absent { background: #ba1a1a; }
.dot-date {
  font-size: 20rpx;
  color: #5f5e5e;
  font-weight: 600;
}
.timeline-empty {
  text-align: center;
  font-size: 24rpx;
  color: #888;
  padding: 20rpx 0;
}

@keyframes spin { to { transform: rotate(360deg); } }
</style>
