<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
        <view class="header-inner">
        <view class="round-btn" @tap="handleBack">
          <uni-icons type="left" size="22" color="#ff6600" />
        </view>
        <text class="header-title">课程详情</text>
        <view class="round-btn" @tap="toggleFavorite">
          <uni-icons :type="isFavorite ? 'heart-filled' : 'heart'" size="20" color="#ff6600" />
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="content">
        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载课程详情…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadCourseDetail">重新加载</view>
        </view>

        <template v-else-if="detail">
          <view class="hero-card">
            <image class="hero-image" src="/static/placeholders/hero.svg" mode="aspectFill" />
            <view class="hero-overlay" />
            <view class="hero-content">
              <view class="hero-badge">
                <uni-icons type="bolt" size="14" color="#ffffff" />
                <text>精英系列</text>
              </view>
              <text class="hero-title">{{ detail.name }}</text>
              <view class="hero-rating">
                <uni-icons type="star-filled" size="14" color="#ffffff" />
                <text>{{ detail.ratingText }}</text>
              </view>
            </view>
          </view>

          <view class="coach-card">
            <view class="coach-main">
              <image class="coach-avatar" src="/static/placeholders/avatar.svg" mode="aspectFill" />
              <view class="coach-info">
                <text class="coach-name">{{ detail.coachName }}</text>
                <text class="coach-role">{{ detail.coachRole }}</text>
              </view>
            </view>
            <view class="coach-badge">
              <uni-icons type="medal-filled" size="20" color="#ff6600" />
            </view>
          </view>

          <view class="info-grid">
            <view class="info-card">
              <uni-icons type="calendar" size="24" color="#666666" />
              <text class="info-label">课程时长</text>
              <text class="info-value">{{ detail.durationText }}</text>
            </view>
            <view class="info-card">
              <uni-icons type="clock" size="24" color="#666666" />
              <text class="info-label">上课时间</text>
              <text class="info-value multiline">{{ detail.scheduleText }}</text>
            </view>
          </view>

          <view class="section-card">
            <text class="section-title">课程大纲</text>
            <view class="syllabus-list">
              <view v-for="(item, index) in detail.syllabus" :key="`${index}-${item.title}`" class="syllabus-item">
                <view class="syllabus-index">{{ index + 1 }}</view>
                <view class="syllabus-body">
                  <text class="syllabus-title">{{ item.title }}</text>
                  <text class="syllabus-desc">{{ item.desc }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="section-card location-card">
            <view class="location-head">
              <view>
                <text class="section-title compact">{{ detail.locationName }}</text>
                <view class="location-meta">
                  <uni-icons type="location" size="14" color="#666666" />
                  <text>{{ detail.locationAddress }}</text>
                </view>
              </view>
              <view class="location-action" @tap="openLocation">
                <uni-icons type="redo" size="18" color="#1a1c1c" />
              </view>
            </view>
            <image class="map-image" src="/static/placeholders/map.svg" mode="aspectFill" />
          </view>

          <view class="price-card">
            <view class="price-block">
              <text class="price-label">总价</text>
              <view class="price-value">
                <text class="price-currency">¥</text>
                <text class="price-amount">{{ detail.priceText }}</text>
              </view>
            </view>

            <view class="quota-block">
              <view class="quota-head">
                <text class="quota-label">剩余名额</text>
                <text class="quota-value">{{ detail.remaining }}/{{ detail.maxStudents }}</text>
              </view>
              <view class="quota-track">
                <view class="quota-fill" :style="{ width: `${detail.progress}%` }" />
              </view>
              <view class="quota-hot">
                <uni-icons type="fire-filled" size="14" color="#ff6600" />
                <text>{{ detail.hotText }}</text>
              </view>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>

    <view v-if="detail" class="bottom-bar">
      <view class="bottom-price">
        <text class="bottom-price-label">价格</text>
        <text class="bottom-price-value">¥{{ detail.priceText }}</text>
      </view>
      <view
        class="book-btn"
        :class="{ disabled: !detail.canBook }"
        @tap="handleBook"
      >
        <text>{{ detail.canBook ? '立即预订' : '名额已满' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getCourseDetail, type CourseItem } from '@/api/course'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'

type SyllabusItem = {
  title: string
  desc: string
}

type CourseDetailVm = {
  id: number
  name: string
  coachName: string
  coachRole: string
  ratingText: string
  durationText: string
  scheduleText: string
  priceText: string
  locationName: string
  locationAddress: string
  syllabus: SyllabusItem[]
  remaining: number
  maxStudents: number
  progress: number
  hotText: string
  canBook: boolean
}

const userStore = useUserStore()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const refreshing = ref(false)
const loading = ref(true)
const errorText = ref('')
const isFavorite = ref(false)
const courseId = ref(0)
const course = ref<CourseItem | null>(null)

function pad2(value: number) {
  return value < 10 ? `0${value}` : `${value}`
}

function formatMoney(value: number) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  })
}

function formatDate(raw?: string) {
  if (!raw) return '待定日期'
  const date = new Date(String(raw).replace(/-/g, '/'))
  if (Number.isNaN(date.getTime())) return raw
  return `${date.getFullYear()}年${pad2(date.getMonth() + 1)}月${pad2(date.getDate())}日`
}

function normalizeTime(value?: string) {
  if (!value) return '--:--'
  return String(value).slice(0, 5)
}

function buildSyllabus(source?: string): SyllabusItem[] {
  const text = String(source || '').trim()
  if (!text) {
    return [
      { title: '站位与重心转移', desc: '掌握准备姿势与爆发性起步。' },
      { title: '六点步法', desc: '高效移动至球场各个角落。' },
      { title: '恢复与平衡', desc: '攻防后快速回位。' }
    ]
  }

  const segments = text
    .split(/[\n；;。]/)
    .map((item) => item.trim())
    .filter(Boolean)
    .slice(0, 3)

  if (segments.length === 0) {
    return [
      { title: '课程重点', desc: text }
    ]
  }

  return segments.map((segment, index) => {
    const parts = segment.split(/[：:]/).map((item) => item.trim()).filter(Boolean)
    return {
      title: parts[0] || `训练模块 ${index + 1}`,
      desc: parts[1] || segment
    }
  })
}

function coachRole(courseInfo: CourseItem) {
  if (courseInfo.coachInfo?.trim()) return courseInfo.coachInfo.trim()
  if ((courseInfo.level || '').includes('高级')) return '国家队运动员'
  if ((courseInfo.level || '').includes('中')) return '国家队运动员'
  return '国家队运动员'
}

const detail = computed<CourseDetailVm | null>(() => {
  if (!course.value) return null

  const maxStudents = Number(course.value.maxStudents || 0)
  const currentStudents = Number(course.value.currentStudents || 0)
  const remaining = Math.max(0, maxStudents - currentStudents)
  const progress = maxStudents > 0 ? Math.min(100, Math.round((currentStudents / maxStudents) * 100)) : 0
  const locationName = course.value.venueName || course.value.courtName || '动能竞技场'
  const locationAddress = course.value.location || course.value.courtName || '体育枢纽大道 128 号'
  const canBook = remaining > 0 && Number(course.value.status ?? 1) === 1

  let hotText = '热卖中'
  if (!canBook) hotText = '名额已满'
  else if (remaining <= 2) hotText = '即将满员'

  return {
    id: course.value.id,
    name: course.value.courseName || '课程详情',
    coachName: course.value.coachName || '待定教练',
    coachRole: coachRole(course.value),
    ratingText: `${Number(course.value.coachRating || 4.9).toFixed(1)} (${course.value.reviews?.length || 128} 条评价)`,
    durationText: `${Math.max(1, Math.ceil(Number(course.value.courseDuration || 120) / 30))}周 (${Math.max(1, Math.ceil(Number(course.value.courseDuration || 120) / 15))}节课)`,
    scheduleText: `${formatDate(course.value.courseDate)}\n${normalizeTime(course.value.startTime)}`,
    priceText: formatMoney(Number(course.value.coursePrice || 0)),
    locationName,
    locationAddress,
    syllabus: buildSyllabus(course.value.courseContent || course.value.description),
    remaining,
    maxStudents,
    progress,
    hotText,
    canBook
  }
})

async function loadCourseDetail() {
  if (!courseId.value) {
    loading.value = false
    errorText.value = '缺少课程参数'
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    course.value = await getCourseDetail(courseId.value)
  } catch (error) {
    console.error('加载课程详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载课程详情失败'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/course/list')
}

function toggleFavorite() {
  isFavorite.value = !isFavorite.value
  uni.showToast({
    title: isFavorite.value ? '已加入收藏' : '已取消收藏',
    icon: 'none'
  })
}

function openLocation() {
  if (!detail.value) return
  uni.showToast({
    title: `导航到${detail.value.locationName}`,
    icon: 'none'
  })
}

function handleBook() {
  if (!detail.value) return
  if (!detail.value.canBook) {
    uni.showToast({
      title: '当前课程暂不可预约',
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages/course/booking?id=${detail.value.id}`
  })
}

function handleRefresh() {
  refreshing.value = true
  loadCourseDetail().finally(() => {
    refreshing.value = false
  })
}

onLoad(async (options?: Record<string, string | undefined>) => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  courseId.value = Number(options?.id || 0)
  await loadCourseDetail()
})

onPullDownRefresh(() => {
  loadCourseDetail().finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fbfbfb 0%, #f4f4f4 100%);
  color: #1a1c1c;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.95);
  backdrop-filter: blur(18px);
}

.header-inner {
  min-height: 112rpx;
  padding: 10rpx 26rpx 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.round-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: -1rpx;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 18rpx 18rpx 220rpx;
}

.hero-card,
.coach-card,
.info-card,
.section-card,
.price-card,
.state-card {
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 12rpx 36rpx rgba(15, 23, 42, 0.04);
}

.hero-card {
  position: relative;
  height: 400rpx;
  border-radius: 26rpx;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(6, 17, 31, 0.08) 0%, rgba(6, 17, 31, 0.88) 100%);
}

.hero-content {
  position: absolute;
  left: 26rpx;
  right: 26rpx;
  bottom: 24rpx;
  z-index: 2;
}

.hero-badge {
  width: fit-content;
  min-width: 156rpx;
  height: 50rpx;
  padding: 0 20rpx;
  border-radius: 9999rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.65);
  background: rgba(32, 44, 58, 0.42);
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
}

.hero-title {
  display: block;
  margin-top: 18rpx;
  font-size: 62rpx;
  line-height: 1.05;
  font-weight: 900;
  color: #ffffff;
  text-shadow: 0 3rpx 12rpx rgba(0, 0, 0, 0.32);
}

.hero-rating {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #ffffff;
  font-size: 27rpx;
  font-weight: 700;
}

.coach-card {
  margin-top: 20rpx;
  padding: 24rpx 26rpx;
  border-radius: 26rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.coach-main {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.coach-avatar {
  width: 92rpx;
  height: 92rpx;
  border-radius: 9999rpx;
  background: #101828;
}

.coach-name {
  display: block;
  font-size: 34rpx;
  font-weight: 900;
  color: #151515;
}

.coach-role {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #555555;
}

.coach-badge {
  width: 52rpx;
  height: 52rpx;
  border-radius: 9999rpx;
  background: rgba(255, 102, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-grid {
  margin-top: 20rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.info-card {
  min-height: 180rpx;
  border-radius: 24rpx;
  padding: 24rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.info-label {
  font-size: 24rpx;
  color: #666666;
  font-weight: 700;
}

.info-value {
  font-size: 22rpx;
  color: #111111;
  font-weight: 900;
  line-height: 1.5;
}

.info-value.multiline {
  white-space: pre-line;
}

.section-card {
  margin-top: 20rpx;
  border-radius: 26rpx;
  padding: 30rpx 26rpx;
}

.section-title {
  display: block;
  font-size: 38rpx;
  font-weight: 900;
  color: #121212;
}

.section-title.compact {
  font-size: 36rpx;
}

.syllabus-list {
  margin-top: 26rpx;
  display: flex;
  flex-direction: column;
  gap: 22rpx;
}

.syllabus-item {
  display: flex;
  gap: 16rpx;
}

.syllabus-index {
  width: 44rpx;
  height: 44rpx;
  border-radius: 9999rpx;
  background: #f1f1f1;
  color: #555555;
  font-size: 22rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.syllabus-body {
  flex: 1;
}

.syllabus-title {
  display: block;
  font-size: 26rpx;
  font-weight: 900;
  color: #1d1d1d;
}

.syllabus-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 23rpx;
  color: #666666;
  line-height: 1.6;
}

.location-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.location-meta {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 23rpx;
  color: #666666;
}

.location-action {
  width: 54rpx;
  height: 54rpx;
  border-radius: 9999rpx;
  background: #f1f1f1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.map-image {
  width: calc(100% + 56rpx);
  height: 260rpx;
  margin: 26rpx -28rpx -34rpx;
  border-bottom-left-radius: 26rpx;
  border-bottom-right-radius: 26rpx;
}

.price-card {
  margin-top: 20rpx;
  border-radius: 26rpx;
  padding: 30rpx 26rpx 28rpx;
}

.price-label,
.quota-label {
  display: block;
  font-size: 24rpx;
  color: #666666;
  font-weight: 700;
}

.price-value {
  margin-top: 14rpx;
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.price-currency {
  font-size: 34rpx;
  font-weight: 900;
}

.price-amount {
  font-size: 70rpx;
  line-height: 1;
  font-weight: 900;
  letter-spacing: -2rpx;
}

.quota-block {
  margin-top: 34rpx;
}

.quota-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quota-value {
  font-size: 28rpx;
  font-weight: 900;
  color: #111111;
}

.quota-track {
  margin-top: 16rpx;
  height: 12rpx;
  border-radius: 9999rpx;
  background: #ededed;
  overflow: hidden;
}

.quota-fill {
  height: 100%;
  border-radius: 9999rpx;
  background: linear-gradient(90deg, #ff6600 0%, #ff7e2d 100%);
}

.quota-hot {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #ff6600;
  font-size: 23rpx;
  font-weight: 800;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  box-shadow: 0 -8rpx 30rpx rgba(26, 28, 28, 0.05);
  padding: 20rpx 28rpx calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 22rpx;
}

.bottom-price {
  min-width: 160rpx;
}

.bottom-price-label {
  display: block;
  font-size: 20rpx;
  color: #666666;
  font-weight: 700;
}

.bottom-price-value {
  display: block;
  margin-top: 8rpx;
  font-size: 48rpx;
  line-height: 1;
  font-weight: 900;
  color: #111111;
}

.book-btn {
  flex: 1;
  height: 92rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #c94e00 0%, #ff6600 100%);
  color: #24180f;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 900;
  box-shadow: 0 12rpx 28rpx rgba(255, 102, 0, 0.22);
}

.book-btn.disabled {
  background: #d9d9d9;
  color: #8c8c8c;
  box-shadow: none;
}

.state-card {
  margin-top: 24rpx;
  border-radius: 28rpx;
  padding: 90rpx 28rpx;
  text-align: center;
}

.state-text {
  font-size: 28rpx;
  color: #777777;
}

.state-action {
  width: 220rpx;
  height: 76rpx;
  margin: 22rpx auto 0;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  margin: 0 auto 18rpx;
  border: 4rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 9999rpx;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
