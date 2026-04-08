<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="course-detail-page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-inner">
          <view class="left" @click="onBack">
            <view class="hit">
              <uni-icons type="arrow-left" size="22" color="#ea580c" />
            </view>
            <text class="title">课程详情</text>
          </view>
          <view class="hit" @click="onShare">
            <uni-icons type="redo" size="18" color="#71717a" />
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading && !course" class="state-wrap">
          <view class="spinner" />
          <text>加载课程详情中…</text>
        </view>
        <view v-else-if="!course" class="state-wrap">
          <text>课程不存在或已删除</text>
        </view>
        <template v-else>
          <view class="hero">
            <image class="hero-img" :src="heroImg" mode="aspectFill" />
            <view class="badge">报名中</view>
          </view>

          <view class="head-info">
            <view>
              <text class="name">{{ course.courseName }}</text>
              <text class="code">课程编号: KL-{{ String(course.id).padStart(4, '0') }}-{{ dateCode }}</text>
            </view>
            <view class="price-pill">
              <text class="k">课程费用</text>
              <text class="v">¥ {{ money(course.coursePrice) }}</text>
            </view>
          </view>

          <view class="progress-card">
            <view class="row">
              <text class="k">报名进度</text>
              <text class="v">{{ course.currentStudents }} / {{ course.maxStudents }} 人</text>
            </view>
            <view class="track"><view class="fill" :style="{ width: `${progress}%` }" /></view>
            <text class="tip">目前已有 {{ progress }}% 的名额被锁定</text>
          </view>

          <view class="grid">
            <view class="info-item">
              <view class="icon"><uni-icons type="person" size="16" color="#a33e00" /></view>
              <view>
                <text class="k">授课教练</text>
                <text class="v2">{{ course.coachName || '未指定教练' }}</text>
              </view>
            </view>
            <view class="info-item">
              <view class="icon"><uni-icons type="location" size="16" color="#a33e00" /></view>
              <view>
                <text class="k">场地位置</text>
                <text class="v2">{{ course.courtName || '未指定场地' }}</text>
              </view>
            </view>
            <view class="info-item">
              <view class="icon"><uni-icons type="calendar" size="16" color="#a33e00" /></view>
              <view>
                <text class="k">排期时间</text>
                <text class="v2">{{ weekLine }}</text>
              </view>
            </view>
            <view class="info-item">
              <view class="icon"><uni-icons type="timer" size="16" color="#a33e00" /></view>
              <view>
                <text class="k">课程周期</text>
                <text class="v2">共 {{ mockLessons }} 课时 ({{ totalHours }} 小时)</text>
              </view>
            </view>
          </view>

          <view class="member-card">
            <view class="m-head">
              <text>已报名会员</text>
              <text class="all" @click="goBookings">全部</text>
            </view>
            <view v-if="bookingLoading" class="m-empty">加载中…</view>
            <view v-else-if="bookingList.length === 0" class="m-empty">暂无报名会员</view>
            <view v-else class="m-list">
              <view class="m-row" v-for="item in bookingList.slice(0, 3)" :key="item.id">
                <view class="avatar">{{ initials(item.memberName) }}</view>
                <view class="meta">
                  <text class="n">{{ item.memberName }}</text>
                  <text class="t">报名时间: {{ formatDateTime(item.createTime) }}</text>
                </view>
                <view class="dot" />
              </view>
              <view class="m-row more" v-if="bookingList.length > 3">
                <view class="avatar soft">+{{ bookingList.length - 3 }}</view>
                <text class="t2">其他会员已报名</text>
              </view>
            </view>
          </view>

          <view class="desc-wrap">
            <text class="d-title">课程简介</text>
            <view class="desc">
              <text>
                {{ courseDesc }}
              </text>
            </view>
          </view>
        </template>

        <view class="spacer" />
      </scroll-view>

      <view v-if="course" class="footer">
        <view class="btn ghost" @click="onCancelCourse">
          <uni-icons type="close" size="16" color="#1a1c1c" />
          <text>取消课程</text>
        </view>
        <view class="btn primary" @click="onEditCourse">
          <uni-icons type="compose" size="16" color="#561d00" />
          <text>编辑课程</text>
        </view>
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

const loading = ref(true)
const bookingLoading = ref(true)
const course = ref<CourseItem | null>(null)
const bookingList = ref<CourseBookingItem[]>([])
const courseId = ref(0)

const heroImg =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuAglNLSw9IYcf7SwcpSWamDYotDelz_MbLA0waKv9H4LXvo9i0rb6fklywWKHdJrII_mD9VvW0TvMFGQIaTUD53qd6qBaLUr3w3FUx8bqTG6LLCBzmGtE__IUWz9fQ-RH3vko3IEf-f9w_Ip9BaXM33QC79F_cyltV5mELWEWkjavd2GSinSw4PwJJIZu_gg-ZNnjNd7cqQLDJYmk59qHadYTcqifvnUJ_UK3WrxpqRD2AgdAyVJUqfMTsVa7jX5F48Gcu1Kxcu6Wig'

const progress = computed(() => {
  const max = Number(course.value?.maxStudents || 0)
  const cur = Number(course.value?.currentStudents || 0)
  if (max <= 0) return 0
  return Math.max(0, Math.min(100, Math.round((cur / max) * 100)))
})
const totalHours = computed(() => {
  const mins = Number(course.value?.courseDuration || 0)
  const lessons = mockLessons.value
  return ((mins * lessons) / 60).toFixed(0)
})
const weekLine = computed(() => {
  if (!course.value) return '—'
  return `${fmtDate(course.value.courseDate)} ${course.value.startTime} - ${course.value.endTime}`
})
const mockLessons = computed(() => 10)
const dateCode = computed(() => (course.value?.courseDate || '').replace(/-/g, '').slice(2))
const courseDesc = computed(() => {
  if (!course.value?.courseName) return '暂无课程简介。'
  return `本课程围绕「${course.value.courseName}」进行系统训练，重点强化步法、击球与实战配合。课程采用小班制，确保每位学员获得充分指导。`
})

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURSE_LIST)
}
function onShare() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}
function onCancelCourse() {
  uni.showToast({ title: '取消课程功能开发中', icon: 'none' })
}
function onEditCourse() {
  if (!courseId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURSE_FORM}?id=${courseId.value}` })
}
function goBookings() {
  uni.navigateTo({ url: PRESIDENT_PAGES.COURSE_BOOKING_LIST })
}

function money(v?: number) {
  if (v == null || Number.isNaN(Number(v))) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}
function fmtDate(v?: string) {
  if (!v) return '—'
  return v.slice(0, 10).replace(/-/g, '.')
}
function formatDateTime(v?: string) {
  if (!v) return '-'
  return v.replace('T', ' ').slice(5, 16)
}
function initials(name?: string) {
  const n = (name || '').trim()
  if (!n) return 'U'
  if (/[\u4e00-\u9fa5]/.test(n)) return n.slice(0, 1)
  const parts = n.split(/\s+/).filter(Boolean)
  return ((parts[0]?.[0] || '') + (parts[1]?.[0] || '')).toUpperCase() || 'U'
}

async function loadDetail(id: number) {
  loading.value = true
  try {
    course.value = await getCourseDetail(id)
  } catch {
    course.value = null
  } finally {
    loading.value = false
  }
}
async function loadBookings(id: number) {
  bookingLoading.value = true
  try {
    const res = await getCourseBookingList({ courseId: id, page: 1, size: 50 })
    bookingList.value = res.data || []
  } catch {
    bookingList.value = []
  } finally {
    bookingLoading.value = false
  }
}

onLoad((q?: Record<string, string | undefined>) => {
  const raw = q?.courseId ?? q?.id
  const id = raw ? parseInt(String(raw), 10) : NaN
  if (!Number.isFinite(id) || id <= 0) {
    uni.showToast({ title: '缺少课程参数', icon: 'none' })
    setTimeout(() => onBack(), 800)
    return
  }
  courseId.value = id
  loadDetail(id)
  loadBookings(id)
})
</script>

<style lang="scss" scoped>
.course-detail-page { min-height: 100vh; background: #f9f9f9; color: #1a1c1c; padding-bottom: 140rpx; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { position: sticky; top: 0; z-index: 50; background: rgba(248,250,252,.92); backdrop-filter: blur(14px); }
.top-inner { height: 88rpx; padding: 0 16rpx; display: flex; align-items: center; justify-content: space-between; }
.left { display: flex; align-items: center; gap: 10rpx; }
.hit { width: 56rpx; height: 56rpx; border-radius: 9999px; display: flex; align-items: center; justify-content: center; background: #fff; }
.title { font-size: 32rpx; color: #ea580c; font-weight: 900; }
.scroll { height: calc(100vh - var(--status-bar-height) - 92rpx - 126rpx); padding: 10rpx 16rpx 0; box-sizing: border-box; }

.state-wrap { min-height: 360rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14rpx; color: #71717a; }
.spinner { width: 44rpx; height: 44rpx; border-radius: 9999px; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.hero { border-radius: 24rpx; overflow: hidden; height: 360rpx; position: relative; background: #f3f3f3; }
.hero-img { width: 100%; height: 100%; }
.badge { position: absolute; left: 14rpx; top: 14rpx; background: #ff6600; color: #561d00; padding: 8rpx 14rpx; border-radius: 9999px; font-size: 18rpx; font-weight: 900; }

.head-info { margin-top: 14rpx; display: flex; justify-content: space-between; align-items: flex-end; gap: 12rpx; }
.name { display: block; font-size: 48rpx; font-weight: 900; letter-spacing: -0.03em; }
.code { display: block; margin-top: 6rpx; color: #71717a; font-weight: 700; font-size: 20rpx; }
.price-pill { background: #e2e2e2; border-radius: 16rpx; padding: 10rpx 14rpx; }
.price-pill .k { display: block; font-size: 14rpx; color: #71717a; font-weight: 900; letter-spacing: .1em; text-transform: uppercase; }
.price-pill .v { display: block; margin-top: 2rpx; font-size: 36rpx; color: #a33e00; font-weight: 900; }

.progress-card { margin-top: 14rpx; background: #fff; border-radius: 20rpx; padding: 16rpx; box-shadow: 0 4rpx 12rpx rgba(2,6,23,.04); }
.row { display: flex; justify-content: space-between; align-items: center; }
.row .k { color: #71717a; font-size: 18rpx; font-weight: 900; letter-spacing: .08em; text-transform: uppercase; }
.row .v { font-size: 24rpx; font-weight: 900; }
.track { margin-top: 10rpx; height: 10rpx; border-radius: 9999px; background: #e5e7eb; overflow: hidden; }
.fill { height: 100%; border-radius: 9999px; background: linear-gradient(90deg, #a33e00, #ff6600); }
.tip { margin-top: 8rpx; font-size: 16rpx; color: #71717a; font-style: italic; }

.grid { margin-top: 14rpx; display: grid; grid-template-columns: 1fr; gap: 10rpx; }
.info-item { background: #f3f3f3; border-radius: 16rpx; padding: 14rpx; display: flex; gap: 10rpx; }
.icon { width: 42rpx; height: 42rpx; border-radius: 12rpx; background: #ffedd5; display: flex; align-items: center; justify-content: center; }
.info-item .k { display: block; font-size: 14rpx; color: #71717a; font-weight: 900; letter-spacing: .1em; text-transform: uppercase; }
.v2 { display: block; margin-top: 2rpx; font-size: 24rpx; font-weight: 800; }

.member-card { margin-top: 14rpx; background: #fff; border-radius: 20rpx; padding: 16rpx; box-shadow: 0 4rpx 12rpx rgba(2,6,23,.04); }
.m-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; font-size: 20rpx; font-weight: 900; letter-spacing: .1em; text-transform: uppercase; color: #71717a; }
.all { color: #a33e00; }
.m-empty { text-align: center; padding: 24rpx 0; color: #a1a1aa; font-weight: 700; }
.m-list { display: flex; flex-direction: column; gap: 10rpx; }
.m-row { display: flex; align-items: center; gap: 10rpx; }
.avatar { width: 44rpx; height: 44rpx; border-radius: 9999px; background: #ffedd5; display: flex; align-items: center; justify-content: center; color: #a33e00; font-weight: 900; }
.avatar.soft { background: #e5e7eb; color: #71717a; }
.meta { flex: 1; display: flex; flex-direction: column; gap: 2rpx; }
.n { font-size: 24rpx; font-weight: 800; }
.t { font-size: 14rpx; color: #71717a; }
.dot { width: 10rpx; height: 10rpx; border-radius: 9999px; background: #22c55e; }
.t2 { font-size: 16rpx; color: #71717a; font-weight: 700; }

.desc-wrap { margin-top: 14rpx; }
.d-title { display: block; font-size: 18rpx; font-weight: 900; color: #71717a; letter-spacing: .1em; text-transform: uppercase; margin-bottom: 8rpx; }
.desc { background: #fff; border-radius: 20rpx; border-left: 6rpx solid #a33e00; padding: 14rpx; font-size: 22rpx; line-height: 1.65; box-shadow: 0 4rpx 12rpx rgba(2,6,23,.04); }

.spacer { height: 20rpx; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; z-index: 60; padding: 18rpx 16rpx calc(18rpx + env(safe-area-inset-bottom)); background: rgba(255,255,255,.74); backdrop-filter: blur(18px); display: flex; gap: 10rpx; }
.btn { height: 88rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; gap: 8rpx; font-size: 28rpx; font-weight: 900; }
.btn.ghost { flex: 1; background: #e2e2e2; color: #1a1c1c; }
.btn.primary { flex: 2; background: linear-gradient(90deg, #a33e00, #ff6600); color: #561d00; box-shadow: 0 16rpx 36rpx rgba(234,88,12,.22); }
</style>

