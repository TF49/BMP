<template>
  <view class="coach-history-page">
    <scroll-view
      class="page-scroll"
      scroll-y
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="hero-shell">
        <view class="hero-topbar" :style="{ paddingTop: `${statusBarHeight}px` }">
          <view class="brand-wrap">
            <text class="brand-title">COURSE HISTORY</text>
            <text class="brand-sub">真实历史课程</text>
          </view>
        </view>

        <view class="hero-main">
          <text class="hero-kicker">Review</text>
          <text class="hero-title">课程历史</text>
          <text class="hero-desc">这里展示已经结束的课程记录，也可以继续查看对应预约和学员名单。</text>
        </view>
      </view>

      <view class="page-body">
        <view class="filter-card">
          <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="搜索课程名称"
            confirm-type="search"
            @confirm="applyFilters"
          />

          <view class="date-row">
            <picker mode="date" :value="startDate" @change="handleStartDateChange">
              <view class="date-chip">
                <text>{{ startDate || '开始日期' }}</text>
              </view>
            </picker>
            <picker mode="date" :value="endDate" @change="handleEndDateChange">
              <view class="date-chip">
                <text>{{ endDate || '结束日期' }}</text>
              </view>
            </picker>
          </view>

          <view class="filter-actions">
            <view class="action-btn action-btn-primary" @tap="applyFilters">
              <text>查询</text>
            </view>
            <view class="action-btn" @tap="resetFilters">
              <text>重置</text>
            </view>
          </view>
        </view>

        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-title">正在加载历史课程</text>
        </view>

        <view v-else-if="loadFailed" class="state-card">
          <text class="state-title">历史课程加载失败</text>
          <text class="state-desc">{{ errorMessage }}</text>
          <view class="state-action" @tap="loadList">
            <text>重新加载</text>
          </view>
        </view>

        <view v-else-if="list.length" class="course-list">
          <view v-for="item in list" :key="item.id" class="course-card">
            <view class="card-badge" :class="statusBadgeClass(item.status)">
              {{ statusText(item.status) }}
            </view>
            <view class="card-main">
              <view class="card-copy">
                <text class="course-name">{{ item.courseName || '未命名课程' }}</text>
                <text class="course-meta">{{ item.courseDate || '待定日期' }} {{ timeRange(item) }}</text>
                <text class="course-meta">{{ item.courtName || '待安排场地' }} · {{ item.venueName || '教练课程' }}</text>
              </view>
              <view class="stat-panel">
                <text class="stat-label">人数</text>
                <text class="stat-value">{{ studentCount(item) }}</text>
              </view>
            </view>

            <view class="bottom-row">
              <view class="price-block">
                <text class="price-label">课时费</text>
                <text class="price-value">¥{{ formatAmount(item.coursePrice) }}/节</text>
              </view>
              <view class="button-row">
                <button class="ghost-btn ghost-btn-light" @tap="openBookings(item)">看预约</button>
                <button class="ghost-btn ghost-btn-light" @tap="openStudents(item)">学员</button>
                <button class="ghost-btn" @tap="openDetail(item)">看详情</button>
              </view>
            </view>
          </view>
        </view>

        <view v-else class="state-card">
          <text class="state-title">暂无历史课程</text>
          <text class="state-desc">当前筛选条件下还没有已结束的课程记录。</text>
        </view>

        <view v-if="total > 0 && !loadFailed" class="pagination-card">
          <text class="pagination-text">第 {{ page }} / {{ totalPages }} 页 · 共 {{ total }} 条</text>
          <view class="pagination-actions">
            <view class="page-btn" :class="{ disabled: page <= 1 }" @tap="changePage(page - 1)">
              <text>上一页</text>
            </view>
            <view class="page-btn" :class="{ disabled: page >= totalPages }" @tap="changePage(page + 1)">
              <text>下一页</text>
            </view>
          </view>
        </view>
      </view>

      <view class="scroll-buffer" />
    </scroll-view>

    <CoachTabBar current="courses" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import { getMyCourses, type CoachCourseItem } from '@/api/coachSelf'
import { COACH_UNBOUND_PATH, isCoachUnboundError } from '@/utils/coachAccess'
import { safeReLaunch } from '@/utils/safeRoute'
import {
  buildCoachBookingsUrl,
  buildCoachStudentsUrl,
  compareCoachCourseTime,
  formatAmount,
  formatCoachCourseStatus,
  formatCoachCourseStatusClass,
  formatCoachCourseTime,
  formatStudentCount,
  isCoachCourseFinished
} from '@/utils/coachView'

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const loading = ref(true)
const refreshing = ref(false)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const list = ref<CoachCourseItem[]>([])
const total = ref(0)
const totalPages = ref(1)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const startDate = ref('')
const endDate = ref('')

function timeRange(item: CoachCourseItem) {
  return formatCoachCourseTime(item)
}

function studentCount(item: CoachCourseItem) {
  return formatStudentCount(item)
}

function statusText(status?: number) {
  return formatCoachCourseStatus(status)
}

function statusBadgeClass(status?: number) {
  return formatCoachCourseStatusClass(status)
}

async function loadList() {
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'
  try {
    const result = await getMyCourses({
      page: page.value,
      size: size.value,
      status: 3,
      keyword: keyword.value || undefined,
      startTime: startDate.value ? `${startDate.value} 00:00:00` : undefined,
      endTime: endDate.value ? `${endDate.value} 23:59:59` : undefined
    })
    const rows = [...(result.data || [])]
      .filter((item) => isCoachCourseFinished(item))
      .sort((left, right) => compareCoachCourseTime(right, left))
    list.value = rows
    total.value = Number(result.total || rows.length || 0)
    totalPages.value = Math.max(1, Number(result.pages || 1))
  } catch (error) {
    console.error('加载课程历史失败:', error)
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

function applyFilters() {
  page.value = 1
  loadList()
}

function resetFilters() {
  keyword.value = ''
  startDate.value = ''
  endDate.value = ''
  page.value = 1
  loadList()
}

function handleStartDateChange(event: { detail: { value: string } }) {
  startDate.value = event.detail.value
}

function handleEndDateChange(event: { detail: { value: string } }) {
  endDate.value = event.detail.value
}

function changePage(nextPage: number) {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === page.value) return
  page.value = nextPage
  loadList()
}

function openDetail(item: CoachCourseItem) {
  uni.navigateTo({
    url: `/pages/course/detail?id=${item.id}`
  })
}

function openBookings(item: CoachCourseItem) {
  uni.navigateTo({
    url: buildCoachBookingsUrl(item.id, item.courseName)
  })
}

function openStudents(item: CoachCourseItem) {
  uni.navigateTo({
    url: buildCoachStudentsUrl(item.id, item.courseName)
  })
}

function handleRefresh() {
  refreshing.value = true
  loadList()
}

onShow(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.coach-history-page {
  min-height: 100vh;
  background: radial-gradient(circle at top, rgba(31, 78, 157, 0.12), transparent 28%), #f9f9f9;
}

.page-scroll {
  min-height: 100vh;
}

.hero-shell {
  padding: 0 24rpx 30rpx;
}

.hero-topbar {
  padding-bottom: 20rpx;
}

.brand-wrap {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.brand-title {
  font-size: 30rpx;
  font-weight: 900;
  color: #1f4e9d;
}

.brand-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #6a7ea9;
}

.hero-main {
  margin-top: 12rpx;
  padding: 34rpx 30rpx;
  border-radius: 34rpx;
  background: linear-gradient(135deg, #1f4e9d 0%, #3d6dba 100%);
  color: #ffffff;
  box-shadow: 0 18rpx 38rpx rgba(31, 78, 157, 0.2);
}

.hero-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.74);
}

.hero-title {
  display: block;
  margin-top: 12rpx;
  font-size: 48rpx;
  font-weight: 900;
}

.hero-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
}

.page-body {
  padding: 0 24rpx;
}

.filter-card,
.course-card,
.pagination-card,
.state-card {
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
}

.filter-card {
  padding: 24rpx;
}

.search-input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  border-radius: 20rpx;
  background: #f6f3f1;
  font-size: 26rpx;
  color: #1a1c1c;
}

.date-row,
.filter-actions,
.button-row,
.pagination-actions {
  display: flex;
  gap: 12rpx;
}

.date-row {
  margin-top: 20rpx;
}

.date-chip {
  min-width: 0;
  height: 76rpx;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: #f6f3f1;
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: #5f5e5e;
}

.filter-actions {
  margin-top: 20rpx;
}

.action-btn,
.page-btn {
  min-width: 0;
  flex: 1;
  height: 76rpx;
  border-radius: 18rpx;
  background: #f3f3f3;
  color: #1a1c1c;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 800;
}

.action-btn-primary {
  background: #1a1c1c;
  color: #ffffff;
}

.course-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.course-card {
  position: relative;
  padding: 26rpx;
}

.card-badge {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;
}

.card-badge.warning {
  background: #fff1e8;
  color: #a33e00;
}

.card-badge.neutral {
  background: #ededed;
  color: #5f5e5e;
}

.card-badge.success {
  background: #e6f6ea;
  color: #1f7a37;
}

.card-main {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.card-copy {
  flex: 1;
  min-width: 0;
}

.course-name {
  display: block;
  padding-right: 140rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #1a1c1c;
  line-height: 1.4;
}

.course-meta {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b625c;
}

.stat-panel {
  min-width: 120rpx;
  padding: 18rpx 16rpx;
  border-radius: 22rpx;
  background: #faf8f6;
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 18rpx;
  color: #8e7164;
  font-weight: 800;
}

.stat-value {
  display: block;
  margin-top: 12rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  font-weight: 900;
}

.bottom-row {
  margin-top: 22rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f4efec;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
}

.price-label {
  display: block;
  font-size: 18rpx;
  color: #8e7164;
  font-weight: 800;
}

.price-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  font-weight: 900;
}

.ghost-btn {
  margin: 0;
  padding: 0 24rpx;
  height: 78rpx;
  border: none;
  border-radius: 22rpx;
  background: #1a1c1c;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 800;
}

.ghost-btn-light {
  background: #f3f3f3;
  color: #1a1c1c;
}

.state-card,
.pagination-card {
  margin-top: 18rpx;
  padding: 32rpx 26rpx;
}

.state-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid #ededed;
  border-top-color: #1f4e9d;
  border-radius: 999rpx;
  animation: spin 0.8s linear infinite;
}

.state-title {
  margin-top: 20rpx;
  font-size: 32rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.state-desc {
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #6b625c;
}

.state-action {
  min-width: 220rpx;
  height: 76rpx;
  margin-top: 22rpx;
  padding: 0 26rpx;
  border-radius: 18rpx;
  background: #1f4e9d;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 800;
}

.pagination-card {
  margin-bottom: 24rpx;
}

.pagination-text {
  display: block;
  font-size: 22rpx;
  color: #5f5e5e;
}

.pagination-actions {
  margin-top: 18rpx;
}

.page-btn.disabled {
  opacity: 0.45;
}

.scroll-buffer {
  height: 160rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
