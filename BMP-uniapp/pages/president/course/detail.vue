<template>
  <PresidentLayout :showTabBar="false">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">课程详情</text>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载课程详情中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="loadData">重试</view>
        </view>

        <view v-else-if="!course" class="state-wrap">
          <text>未找到课程信息</text>
        </view>

        <template v-else>
          <view class="hero">
            <image class="hero-img" src="/static/placeholders/hero.svg" mode="aspectFill" />
            <view class="badge" :class="statusMeta.key">{{ statusMeta.label }}</view>
          </view>

          <view class="headline">
            <view>
              <text class="name">{{ course.courseName }}</text>
              <text class="sub">课程编号 KL-{{ String(course.id).padStart(4, '0') }}-{{ dateCode }}</text>
            </view>
            <view class="price-card">
              <text class="price-label">课程费用</text>
              <text class="price-value">¥{{ amountText }}</text>
            </view>
          </view>

          <view class="panel">
            <view class="panel-head">
              <text class="panel-title">报名进度</text>
              <text class="panel-value">{{ course.currentStudents || 0 }}/{{ course.maxStudents || 0 }}</text>
            </view>
            <view class="progress-track">
              <view class="progress-fill" :style="{ width: `${progress}%` }" />
            </view>
            <text class="panel-tip">当前报名完成度 {{ progress }}%</text>
          </view>

          <view class="grid">
            <view class="info-card">
              <text class="label">教练</text>
              <text class="value">{{ course.coachName || '未指定教练' }}</text>
            </view>
            <view class="info-card">
              <text class="label">场地</text>
              <text class="value">{{ course.courtName || '未指定场地' }}</text>
            </view>
            <view class="info-card">
              <text class="label">时间</text>
              <text class="value">{{ scheduleText }}</text>
            </view>
            <view class="info-card">
              <text class="label">时长</text>
              <text class="value">{{ durationText }}</text>
            </view>
          </view>

          <view class="panel">
            <view class="panel-head">
              <text class="panel-title">已报名会员</text>
              <text class="panel-link" @click="goBookings">查看全部</text>
            </view>
            <view v-if="bookingLoading" class="empty-text">加载中...</view>
            <view v-else-if="bookingList.length === 0" class="empty-text">暂无报名会员</view>
            <view v-else class="member-list">
              <view v-for="item in bookingList.slice(0, 5)" :key="item.id" class="member-row">
                <view class="member-avatar">{{ initials(item.memberName) }}</view>
                <view class="member-main">
                  <text class="member-name">{{ item.memberName }}</text>
                  <text class="member-sub">报名时间 {{ bookingTime(item.createTime) }}</text>
                </view>
                <view class="member-status" :class="bookingStatus(item.status).key">
                  {{ bookingStatus(item.status).label }}
                </view>
              </view>
            </view>
          </view>

          <view class="panel">
            <text class="panel-title">课程简介</text>
            <text class="desc">{{ contentText }}</text>
          </view>
        </template>

        <view class="bottom-space" />
      </scroll-view>

      <view v-if="course" class="footer">
        <view class="footer-btn ghost" @click="showUnsupported('当前版本暂未接入课程状态变更')">状态操作</view>
        <view class="footer-btn primary" @click="goEdit">编辑课程</view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getCourseBookingList, getCourseDetail, type CourseBookingItem, type CourseItem } from '@/api/course'
import { parsePagedList } from '@/utils/parsePagedList'
import { formatDate, formatDateTime, formatTime } from '@/utils/format'
import { getCourseBookingStatusMeta, getCourseStatusMeta } from '@/utils/presidentStatus'

const courseId = ref(0)
const loading = ref(true)
const bookingLoading = ref(false)
const errorText = ref('')
const course = ref<(CourseItem & { courseContent?: string }) | null>(null)
const bookingList = ref<CourseBookingItem[]>([])

const statusMeta = computed(() => getCourseStatusMeta(course.value?.status))
const progress = computed(() => {
  const total = Number(course.value?.maxStudents || 0)
  const current = Number(course.value?.currentStudents || 0)
  return total > 0 ? Math.min(100, Math.round((current / total) * 100)) : 0
})
const amountText = computed(() => Number(course.value?.coursePrice || 0).toFixed(2))
const scheduleText = computed(() => {
  if (!course.value) return '-'
  const date = formatDate(course.value.courseDate, 'YYYY.MM.DD')
  const start = formatTime(course.value.startTime, 'HH:mm')
  const end = formatTime(course.value.endTime, 'HH:mm')
  return date && start && end ? `${date} ${start}-${end}` : '-'
})
const durationText = computed(() => {
  const minutes = Number(course.value?.courseDuration || 0)
  if (!minutes) return '-'
  const hours = minutes / 60
  return `${hours % 1 === 0 ? hours.toFixed(0) : hours.toFixed(1)} 小时`
})
const dateCode = computed(() => String(course.value?.courseDate || '').replace(/-/g, '').slice(2))
const contentText = computed(() => {
  if (course.value?.courseContent) return course.value.courseContent
  if (!course.value?.courseName) return '暂无课程简介。'
  return `本课程围绕“${course.value.courseName}”开展训练，重点提升实战能力、动作稳定性与训练节奏。`
})

function bookingStatus(status?: number) {
  return getCourseBookingStatusMeta(status)
}

function bookingTime(value?: string) {
  return formatDateTime(value, 'YYYY-MM-DD HH:mm') || '-'
}

function initials(name?: string) {
  const text = (name || '').trim()
  if (!text) return 'U'
  if (/[\u4e00-\u9fa5]/.test(text)) return text.slice(0, 1)
  const parts = text.split(/\s+/).filter(Boolean)
  return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase() || 'U'
}

function showUnsupported(text: string) {
  uni.showToast({ title: text, icon: 'none' })
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURSE_LIST)
}

function goEdit() {
  if (!courseId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURSE_FORM}?id=${courseId.value}` })
}

function goBookings() {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURSE_BOOKING_LIST}?courseId=${courseId.value}` })
}

async function loadBookings() {
  if (!courseId.value) return
  bookingLoading.value = true
  try {
    const res = await getCourseBookingList({ courseId: courseId.value, page: 1, size: 50 })
    bookingList.value = parsePagedList<CourseBookingItem>(res).list
  } catch {
    bookingList.value = []
  } finally {
    bookingLoading.value = false
  }
}

async function loadData() {
  if (!courseId.value) return
  loading.value = true
  errorText.value = ''
  course.value = null

  try {
    course.value = await getCourseDetail(courseId.value)
    await loadBookings()
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onLoad((query?: Record<string, string | undefined>) => {
  const id = Number(query?.courseId || query?.id || 0)
  if (!id) {
    loading.value = false
    errorText.value = '缺少课程参数'
    return
  }
  courseId.value = id
  loadData()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f8fafc; color: #111827; padding-bottom: 132rpx; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { padding: 16rpx 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.icon-btn { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #ffffff; display: flex; align-items: center; justify-content: center; }
.title { font-size: 38rpx; font-weight: 800; }
.scroll { height: calc(100vh - var(--status-bar-height) - 108rpx - 132rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.hero { position: relative; height: 320rpx; border-radius: 28rpx; overflow: hidden; background: #e5e7eb; }
.hero-img { width: 100%; height: 100%; }
.badge { position: absolute; left: 18rpx; top: 18rpx; padding: 10rpx 18rpx; border-radius: 9999px; font-size: 20rpx; font-weight: 800; }
.badge.enrolling { background: #ffedd5; color: #c2410c; }
.badge.ongoing { background: #dbeafe; color: #1d4ed8; }
.badge.finished { background: #dcfce7; color: #166534; }
.badge.cancelled { background: #fee2e2; color: #b91c1c; }
.badge.unknown { background: #e5e7eb; color: #4b5563; }
.headline { margin-top: 18rpx; display: flex; gap: 16rpx; justify-content: space-between; align-items: flex-start; }
.name { display: block; font-size: 42rpx; font-weight: 900; }
.sub { display: block; margin-top: 8rpx; font-size: 22rpx; color: #6b7280; }
.price-card, .panel, .info-card { background: #ffffff; border-radius: 24rpx; box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05); }
.price-card { padding: 18rpx 20rpx; min-width: 220rpx; }
.price-label { display: block; font-size: 18rpx; color: #6b7280; }
.price-value { display: block; margin-top: 8rpx; font-size: 34rpx; color: #c2410c; font-weight: 900; }
.panel { margin-top: 18rpx; padding: 22rpx; display: flex; flex-direction: column; gap: 12rpx; }
.panel-head { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; }
.panel-title { font-size: 26rpx; font-weight: 800; }
.panel-value, .panel-link { font-size: 22rpx; font-weight: 700; color: #c2410c; }
.progress-track { height: 12rpx; border-radius: 9999px; background: #e5e7eb; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 9999px; background: linear-gradient(90deg, #a33e00, #ff6600); }
.panel-tip, .empty-text { font-size: 22rpx; color: #6b7280; }
.grid { margin-top: 18rpx; display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.info-card { padding: 22rpx; }
.label { display: block; font-size: 20rpx; color: #6b7280; margin-bottom: 8rpx; }
.value { font-size: 24rpx; font-weight: 700; line-height: 1.5; }
.member-list { display: flex; flex-direction: column; gap: 14rpx; }
.member-row { display: flex; align-items: center; gap: 14rpx; }
.member-avatar { width: 56rpx; height: 56rpx; border-radius: 9999px; background: #ffedd5; color: #c2410c; display: flex; align-items: center; justify-content: center; font-size: 22rpx; font-weight: 800; }
.member-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6rpx; }
.member-name { font-size: 24rpx; font-weight: 700; }
.member-sub { font-size: 20rpx; color: #6b7280; }
.member-status { padding: 8rpx 14rpx; border-radius: 9999px; font-size: 18rpx; font-weight: 800; }
.member-status.pending { background: #fef3c7; color: #b45309; }
.member-status.paid { background: #dbeafe; color: #1d4ed8; }
.member-status.ongoing { background: #e0f2fe; color: #0369a1; }
.member-status.completed { background: #dcfce7; color: #166534; }
.member-status.cancelled { background: #fee2e2; color: #b91c1c; }
.member-status.unknown { background: #e5e7eb; color: #4b5563; }
.desc { font-size: 24rpx; color: #4b5563; line-height: 1.7; }
.bottom-space { height: 36rpx; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; display: flex; gap: 12rpx; padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom)); background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(16px); }
.footer-btn { height: 88rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 800; }
.footer-btn.ghost { flex: 1; background: #e5e7eb; color: #111827; }
.footer-btn.primary { flex: 1; background: #ea580c; color: #ffffff; }
</style>
