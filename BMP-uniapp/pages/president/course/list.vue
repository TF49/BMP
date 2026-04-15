<template>
  <PresidentLayout :showTabBar="true">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">课程管理</text>
        </view>
        <view class="icon-btn add-btn" @click="goAdd">
          <uni-icons type="plusempty" size="22" color="#ffffff" />
        </view>
      </view>

      <view class="toolbar">
        <view class="search-box">
          <uni-icons type="search" size="18" color="#71717a" />
          <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="搜索课程名称或教练"
            confirm-type="search"
            @confirm="reloadList"
          />
        </view>

        <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
          <view class="tabs">
            <view class="tab" :class="{ active: statusFilter === -1 }" @click="setStatus(-1)">全部</view>
            <view class="tab" :class="{ active: statusFilter === 1 }" @click="setStatus(1)">报名中</view>
            <view class="tab" :class="{ active: statusFilter === 2 }" @click="setStatus(2)">进行中</view>
            <view class="tab" :class="{ active: statusFilter === 3 }" @click="setStatus(3)">已结束</view>
            <view class="tab" :class="{ active: statusFilter === 0 }" @click="setStatus(0)">已取消</view>
          </view>
        </scroll-view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载课程列表中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="reloadList">重试</view>
        </view>

        <view v-else-if="courseList.length === 0" class="state-wrap">
          <text>暂无符合条件的课程</text>
        </view>

        <view v-else class="list">
          <view class="summary">
            <text>共 {{ total }} 门课程</text>
            <text>当前展示 {{ courseList.length }} 门</text>
          </view>

          <view v-for="item in courseList" :key="item.id" class="card" @click="goDetail(item.id)">
            <view class="card-head">
              <view>
                <text class="name">{{ item.name }}</text>
                <text class="sub">{{ item.coachName || '未指定教练' }}</text>
              </view>
              <view class="status-pill" :class="item.statusMeta.key">
                {{ item.statusMeta.label }}
              </view>
            </view>

            <view class="meta-grid">
              <view class="meta-item">
                <text class="label">时间</text>
                <text class="value">{{ item.scheduleText }}</text>
              </view>
              <view class="meta-item">
                <text class="label">场地</text>
                <text class="value">{{ item.courtName || '未指定场地' }}</text>
              </view>
              <view class="meta-item">
                <text class="label">价格</text>
                <text class="value">¥{{ item.priceText }}</text>
              </view>
              <view class="meta-item">
                <text class="label">报名</text>
                <text class="value">{{ item.currentStudents }}/{{ item.maxStudents }}</text>
              </view>
            </view>

            <view class="progress-track">
              <view class="progress-fill" :style="{ width: `${item.progress}%` }" />
            </view>
          </view>

          <view v-if="hasMore" class="more-btn" @click="loadMore">加载更多</view>
        </view>

        <view class="bottom-space" />
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getCourseList, type CourseItem } from '@/api/president/course'
import { parsePagedList } from '@/utils/parsePagedList'
import { getCourseStatusMeta } from '@/utils/presidentStatus'
import { formatDate, formatTime } from '@/utils/format'

type CourseCard = {
  id: number
  name: string
  coachName: string
  courtName: string
  maxStudents: number
  currentStudents: number
  priceText: string
  scheduleText: string
  progress: number
  statusMeta: ReturnType<typeof getCourseStatusMeta>
}

const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const errorText = ref('')
const keyword = ref('')
const statusFilter = ref(-1)
const courseList = ref<CourseCard[]>([])

const hasMore = computed(() => courseList.value.length < total.value)

function mapCourse(item: CourseItem): CourseCard {
  const maxStudents = Number(item.maxStudents || 0)
  const currentStudents = Number(item.currentStudents || 0)
  const progress = maxStudents > 0 ? Math.min(100, Math.round((currentStudents / maxStudents) * 100)) : 0
  const date = formatDate(item.courseDate, 'YYYY.MM.DD')
  const start = formatTime(item.startTime, 'HH:mm')
  const end = formatTime(item.endTime, 'HH:mm')

  return {
    id: Number(item.id || 0),
    name: item.courseName || '未命名课程',
    coachName: item.coachName || '',
    courtName: item.courtName || '',
    maxStudents,
    currentStudents,
    priceText: Number(item.coursePrice || 0).toFixed(2),
    scheduleText: date && start && end ? `${date} ${start}-${end}` : '未设置排期',
    progress,
    statusMeta: getCourseStatusMeta(item.status)
  }
}

async function fetchList(reset = false) {
  if (loading.value) return
  loading.value = true
  errorText.value = ''

  if (reset) {
    page.value = 1
  }

  try {
    const res = await getCourseList({
      page: page.value,
      size,
      status: statusFilter.value >= 0 ? statusFilter.value : undefined,
      keyword: keyword.value || undefined
    })
    const parsed = parsePagedList<CourseItem>(res)
    const mapped = parsed.list.map(mapCourse).filter(item => item.id > 0)
    total.value = parsed.total
    courseList.value = reset ? mapped : courseList.value.concat(mapped)
  } catch (error) {
    if (!courseList.value.length) {
      errorText.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

function reloadList() {
  fetchList(true)
}

function loadMore() {
  if (!hasMore.value || loading.value) return
  page.value += 1
  fetchList()
}

function setStatus(status: number) {
  if (statusFilter.value === status) return
  statusFilter.value = status
  reloadList()
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.COURSE_FORM })
}

function goDetail(id: number) {
  if (!id) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURSE_DETAIL}?id=${id}` })
}

onShow(() => {
  reloadList()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f8fafc; color: #111827; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { display: flex; align-items: center; justify-content: space-between; padding: 16rpx 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.icon-btn { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #ffffff; display: flex; align-items: center; justify-content: center; }
.add-btn { background: #ea580c; }
.title { font-size: 38rpx; font-weight: 800; }
.toolbar { padding: 0 24rpx 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.search-box { display: flex; align-items: center; gap: 12rpx; padding: 0 24rpx; height: 88rpx; border-radius: 20rpx; background: #ffffff; }
.search-input { flex: 1; font-size: 26rpx; }
.tabs-scroll { white-space: nowrap; }
.tabs { display: inline-flex; gap: 12rpx; }
.tab { padding: 14rpx 28rpx; border-radius: 9999px; background: #e5e7eb; color: #4b5563; font-size: 24rpx; font-weight: 700; white-space: nowrap; }
.tab.active { background: #ffedd5; color: #c2410c; }
.scroll { height: calc(100vh - var(--status-bar-height) - 232rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn, .more-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.list { display: flex; flex-direction: column; gap: 18rpx; }
.summary { display: flex; justify-content: space-between; color: #6b7280; font-size: 22rpx; margin-bottom: 4rpx; }
.card { padding: 24rpx; border-radius: 24rpx; background: #ffffff; box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05); display: flex; flex-direction: column; gap: 18rpx; }
.card-head { display: flex; justify-content: space-between; gap: 12rpx; align-items: flex-start; }
.name { display: block; font-size: 32rpx; font-weight: 800; }
.sub { display: block; margin-top: 6rpx; font-size: 22rpx; color: #6b7280; }
.status-pill { padding: 10rpx 18rpx; border-radius: 9999px; font-size: 18rpx; font-weight: 800; }
.status-pill.enrolling { background: #ffedd5; color: #c2410c; }
.status-pill.ongoing { background: #dbeafe; color: #1d4ed8; }
.status-pill.finished { background: #dcfce7; color: #166534; }
.status-pill.cancelled { background: #fee2e2; color: #b91c1c; }
.status-pill.unknown { background: #e5e7eb; color: #4b5563; }
.meta-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.label { display: block; font-size: 18rpx; color: #6b7280; margin-bottom: 6rpx; }
.value { font-size: 24rpx; font-weight: 700; }
.progress-track { height: 12rpx; border-radius: 9999px; background: #e5e7eb; overflow: hidden; }
.progress-fill { height: 100%; border-radius: 9999px; background: linear-gradient(90deg, #a33e00, #ff6600); }
.bottom-space { height: 36rpx; }
</style>
