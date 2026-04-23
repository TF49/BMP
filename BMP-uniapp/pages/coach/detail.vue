<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="header-inner">
        <view class="nav-left" @tap="handleBack">
          <view class="icon-btn">
            <uni-icons type="left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">教练详情</text>
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
    >
      <view class="content">
        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载教练详情…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadCoachDetail">重新加载</view>
        </view>

        <template v-else-if="coach">
          <view class="hero-card">
            <image class="hero-image" :src="coach.avatar" mode="aspectFill" />
            <view class="hero-overlay" />
            <view class="hero-content">
              <view class="hero-badge">
                <uni-icons type="star-filled" size="14" color="#ffffff" />
                <text>{{ coach.statusText }}</text>
              </view>
              <text class="hero-title">{{ coach.name }}</text>
              <text class="hero-sub">{{ coach.venueName || '未关联场馆' }}</text>
              <view class="hero-rating">
                <text>评分 {{ coach.ratingText }}</text>
                <text>·</text>
                <text>¥{{ coach.priceText }}/小时</text>
              </view>
            </view>
          </view>

          <view class="summary-grid">
            <view class="summary-card">
              <text class="summary-label">联系电话</text>
              <text class="summary-value">{{ coach.phone || '未填写' }}</text>
            </view>
            <view class="summary-card">
              <text class="summary-label">课程数量</text>
              <text class="summary-value">{{ courses.length }}</text>
            </view>
            <view class="summary-card">
              <text class="summary-label">价格</text>
              <text class="summary-value">¥{{ coach.priceText }}</text>
            </view>
            <view class="summary-card">
              <text class="summary-label">专长标签</text>
              <text class="summary-value">{{ specialtyTags.length }} 项</text>
            </view>
          </view>

          <view class="section-card">
            <text class="section-title">执教简介</text>
            <text class="section-text">{{ coach.experience || '当前教练尚未补充执教经验。' }}</text>
          </view>

          <view class="section-card">
            <text class="section-title">技术特长</text>
            <view v-if="specialtyTags.length" class="chip-wrap">
              <text v-for="tag in specialtyTags" :key="tag" class="chip">{{ tag }}</text>
            </view>
            <text v-else class="section-text">当前教练尚未补充专长标签。</text>
          </view>

          <view class="section-card">
            <view class="section-head">
              <text class="section-title">开设课程</text>
              <text class="section-note">{{ courses.length > 0 ? '按真实课程列表展示' : '暂无课程' }}</text>
            </view>

            <view v-if="courses.length === 0" class="empty-card">
              <text class="empty-title">当前教练还没有可展示课程</text>
              <text class="empty-desc">后续如新增课程，会在这里自动显示，不再补演示内容。</text>
            </view>

            <view v-else class="course-list">
              <view
                v-for="item in courses"
                :key="item.id"
                class="course-card"
                @tap="goCourseDetail(item.id)"
              >
                <view class="course-copy">
                  <text class="course-name">{{ item.courseName }}</text>
                  <text class="course-meta">{{ item.courseDate || '待定日期' }} · {{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}</text>
                  <text class="course-meta">{{ item.courtName || item.venueName || '待定场地' }} · ¥{{ formatPrice(item.coursePrice) }}</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>
            </view>
          </view>
        </template>
      </view>
    </scroll-view>

    <view v-if="coach" class="footer">
      <view class="footer-left">
        <text class="footer-label">当前教练</text>
        <text class="footer-value">{{ coach.name }}</text>
      </view>
      <button class="footer-btn" @tap="goCoachCourses">
        {{ courses.length > 0 ? '查看该教练课程' : '返回课程中心' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCoachDetail, type CoachDto } from '@/api/coach'
import { getCourseList, type CourseItem } from '@/api/course'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

type CoachViewModel = {
  id: number
  name: string
  avatar: string
  venueName: string
  phone: string
  specialty: string
  experience: string
  ratingText: string
  priceText: string
  statusText: string
}

const userStore = useUserStore()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const loading = ref(true)
const errorText = ref('')
const coachId = ref(0)
const coach = ref<CoachViewModel | null>(null)
const courses = ref<CourseItem[]>([])

const specialtyTags = computed(() => {
  const raw = coach.value?.specialty || ''
  return raw
    .split(/[、,，;；/\s]+/)
    .map((item) => item.trim())
    .filter(Boolean)
    .slice(0, 6)
})

function formatTime(value?: string) {
  if (!value) return '--:--'
  return String(value).slice(0, 5)
}

function formatPrice(value?: number) {
  return Number(value || 0).toFixed(2)
}

function mapCoach(item: CoachDto): CoachViewModel {
  return {
    id: Number(item.id || 0),
    name: item.coachName || '未命名教练',
    avatar: resolveImageUrl(item.avatar) || '/static/placeholders/avatar.svg',
    venueName: item.venueName || '',
    phone: item.phone || '',
    specialty: item.specialty || '',
    experience: item.experience || '',
    ratingText: item.rating != null ? Number(item.rating).toFixed(1) : '暂无',
    priceText: Number(item.hourlyPrice || 0).toFixed(2),
    statusText: Number(item.status ?? 1) === 1 ? '在岗教练' : '暂停接单'
  }
}

async function loadCoachDetail() {
  if (!coachId.value) {
    loading.value = false
    errorText.value = '缺少教练参数'
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    const [coachDetail, coursePage] = await Promise.all([
      getCoachDetail(coachId.value),
      getCourseList({
        coachId: coachId.value,
        page: 1,
        size: 20
      })
    ])

    coach.value = mapCoach(coachDetail)
    courses.value = Array.isArray(coursePage?.data) ? coursePage.data : []
  } catch (error) {
    console.error('加载教练详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载教练详情失败'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/course/list')
}

function goCourseDetail(id: number) {
  uni.navigateTo({
    url: `/pages/course/detail?id=${id}`
  })
}

function goCoachCourses() {
  if (courses.value.length > 0) {
    uni.navigateTo({
      url: `/pages/course/list?coachId=${coachId.value}`
    })
    return
  }
  uni.navigateTo({
    url: '/pages/course/list'
  })
}

onLoad(async (options?: Record<string, string | undefined>) => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  coachId.value = Number(options?.id || 0)
  await loadCoachDetail()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fbfbfb 0%, #f4f4f4 100%);
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
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
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
.summary-card,
.section-card,
.state-card {
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 12rpx 36rpx rgba(15, 23, 42, 0.04);
}

.hero-card {
  position: relative;
  height: 420rpx;
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

.hero-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.hero-rating {
  margin-top: 12rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 700;
}

.summary-grid {
  margin-top: 20rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.summary-card {
  min-height: 148rpx;
  border-radius: 24rpx;
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.summary-label {
  font-size: 22rpx;
  color: #666666;
  font-weight: 700;
}

.summary-value {
  font-size: 30rpx;
  line-height: 1.4;
  font-weight: 900;
  color: #111111;
}

.section-card {
  margin-top: 20rpx;
  border-radius: 26rpx;
  padding: 30rpx 26rpx;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.section-title {
  display: block;
  font-size: 38rpx;
  font-weight: 900;
  color: #121212;
}

.section-note {
  font-size: 20rpx;
  color: #8a8a8a;
}

.section-text {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  color: #666666;
  line-height: 1.75;
}

.chip-wrap {
  margin-top: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.chip {
  padding: 12rpx 18rpx;
  border-radius: 9999rpx;
  background: #fff3eb;
  color: #a33e00;
  font-size: 22rpx;
  font-weight: 800;
}

.course-list {
  margin-top: 22rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.course-card {
  padding: 22rpx;
  border-radius: 22rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #ffffff 100%);
  border: 1rpx solid rgba(255, 102, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.course-copy {
  flex: 1;
}

.course-name {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1f2937;
}

.course-meta {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.empty-card {
  margin-top: 20rpx;
  padding: 26rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #ffffff 100%);
  border: 1rpx solid rgba(255, 102, 0, 0.08);
}

.empty-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1f2937;
}

.empty-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: #64748b;
}

.footer {
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

.footer-left {
  min-width: 180rpx;
}

.footer-label {
  display: block;
  font-size: 20rpx;
  color: #666666;
  font-weight: 700;
}

.footer-value {
  display: block;
  margin-top: 8rpx;
  font-size: 34rpx;
  font-weight: 900;
  color: #111111;
}

.footer-btn {
  flex: 1;
  height: 92rpx;
  border: none;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #c94e00 0%, #ff6600 100%);
  color: #ffffff;
  font-size: 30rpx;
  font-weight: 900;
  box-shadow: 0 12rpx 28rpx rgba(255, 102, 0, 0.22);
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
