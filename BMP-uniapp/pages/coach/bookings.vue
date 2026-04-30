<template>
  <view class="coach-bookings-page">
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
            <text class="brand-title">COACH BOOKINGS</text>
            <text class="brand-sub">真实预约明细</text>
          </view>
        </view>

        <view class="hero-card">
          <text class="hero-kicker">Manage</text>
          <text class="hero-title">预约明细</text>
          <text class="hero-desc">可以查看学员预约、签到上课、完成课程，或登记缺席与取消。</text>
        </view>
      </view>

      <view class="page-body">
        <view class="filter-card">
          <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="预约单号 / 课程名 / 会员名"
            confirm-type="search"
            @confirm="applyFilters"
          />

          <scroll-view scroll-x class="status-scroll" :show-scrollbar="false">
            <view class="status-row">
              <view
                v-for="item in statusOptions"
                :key="item.label"
                class="tab"
                :class="{ active: selectedStatus === item.value }"
                @tap="selectStatus(item.value)"
              >
                <text>{{ item.label }}</text>
              </view>
            </view>
          </scroll-view>

          <view v-if="activeCourseName" class="filter-banner">
            <text>当前仅查看课程：{{ activeCourseName }}</text>
            <text class="filter-clear" @tap="clearCourseFilter">清除</text>
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
          <text class="state-title">正在加载预约记录</text>
        </view>

        <view v-else-if="loadFailed" class="state-card">
          <text class="state-title">预约数据加载失败</text>
          <text class="state-desc">{{ errorMessage }}</text>
          <view class="state-action" @tap="loadList">
            <text>重新加载</text>
          </view>
        </view>

        <view v-else-if="list.length" class="booking-list">
          <view v-for="item in list" :key="item.id" class="booking-card">
            <view class="card-head">
              <view>
                <text class="booking-no">{{ item.bookingNo || `预约 #${item.id}` }}</text>
                <text class="member-name">{{ item.memberName || '未知学员' }}</text>
              </view>
              <view class="head-right">
                <text class="amount">¥{{ formatAmount(item.orderAmount) }}</text>
                <text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
              </view>
            </view>

            <view class="meta-row">
              <text>{{ item.courseName || '未命名课程' }}</text>
              <text>{{ item.courseDate || '待定日期' }}</text>
            </view>
            <view class="meta-row">
              <text>{{ timeRange(item) }}</text>
              <text>{{ item.courtName || '待安排场地' }}</text>
            </view>
            <view class="meta-row meta-row-emphasis">
              <text>支付：{{ paymentStatusText(item.paymentStatus) }}</text>
              <text>{{ item.remark || '暂无备注' }}</text>
            </view>

            <view class="action-row">
              <button class="ghost-btn ghost-btn-light" @tap="openDetail(item)">详情</button>
              <button v-if="canStart(item)" class="ghost-btn ghost-btn-green" @tap="updateStatus(item, 3)">签到</button>
              <button v-if="canComplete(item)" class="ghost-btn ghost-btn-blue" @tap="updateStatus(item, 4)">完成</button>
              <button v-if="canCancel(item)" class="ghost-btn ghost-btn-danger" @tap="updateStatus(item, 0)">缺席/取消</button>
            </view>
          </view>
        </view>

        <view v-else class="state-card">
          <text class="state-title">暂无预约</text>
          <text class="state-desc">当前筛选条件下没有预约记录。</text>
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

    <view v-if="detailVisible" class="detail-mask" @tap="closeDetail">
      <view class="detail-panel" @tap.stop>
        <view class="detail-head">
          <text class="detail-title">预约详情</text>
          <text class="detail-close" @tap="closeDetail">关闭</text>
        </view>

        <view v-if="detailLoading" class="detail-state">
          <view class="spinner" />
          <text class="state-desc">正在加载预约详情...</text>
        </view>

        <view v-else-if="detailError" class="detail-state">
          <text class="state-desc">{{ detailError }}</text>
        </view>

        <template v-else-if="detail">
          <view class="detail-item">
            <text class="detail-label">预约单号</text>
            <text class="detail-value">{{ detail.bookingNo || '-' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">会员</text>
            <text class="detail-value">{{ detail.memberName || '-' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">课程</text>
            <text class="detail-value">{{ detail.courseName || '-' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">时间</text>
            <text class="detail-value">{{ detail.courseDate || '-' }} {{ timeRange(detail) }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">场地</text>
            <text class="detail-value">{{ detail.courtName || '-' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">支付状态</text>
            <text class="detail-value">{{ paymentStatusText(detail.paymentStatus) }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">预约状态</text>
            <text class="detail-value">{{ statusText(detail.status) }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">备注</text>
            <text class="detail-value">{{ detail.remark || '无' }}</text>
          </view>
        </template>
      </view>
    </view>

    <CoachTabBar current="bookings" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import {
  getBookingDetailForCoach,
  getBookingsForCoach,
  updateBookingStatusForCoach,
  type CoachBookingItem
} from '@/api/coachSelf'
import { COACH_UNBOUND_PATH, isCoachUnboundError } from '@/utils/coachAccess'
import { safeReLaunch } from '@/utils/safeRoute'
import {
  canCancelBooking,
  canCompleteBooking,
  canStartBooking,
  formatAmount,
  formatCoachBookingStatus,
  formatCoachBookingStatusClass,
  formatCoachBookingTime,
  formatCoachPaymentStatus
} from '@/utils/coachView'

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const loading = ref(true)
const refreshing = ref(false)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const list = ref<CoachBookingItem[]>([])
const total = ref(0)
const totalPages = ref(1)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const selectedStatus = ref<number | null>(null)
const activeCourseId = ref<number | null>(null)
const activeCourseName = ref('')
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const detail = ref<CoachBookingItem | null>(null)

const statusOptions = [
  { label: '全部', value: null },
  { label: '已取消', value: 0 },
  { label: '待支付', value: 1 },
  { label: '已支付', value: 2 },
  { label: '进行中', value: 3 },
  { label: '已完成', value: 4 }
]

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

async function loadList() {
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'
  try {
    const result = await getBookingsForCoach({
      page: page.value,
      size: size.value,
      courseId: activeCourseId.value ?? undefined,
      status: selectedStatus.value ?? undefined,
      keyword: keyword.value || undefined
    })
    list.value = result.data || []
    total.value = Number(result.total || 0)
    totalPages.value = Math.max(1, Number(result.pages || 1))
  } catch (error) {
    console.error('加载预约明细失败:', error)
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

async function openDetail(item: CoachBookingItem) {
  detailVisible.value = true
  detailLoading.value = true
  detailError.value = ''
  detail.value = null
  try {
    detail.value = await getBookingDetailForCoach(item.id)
  } catch (error) {
    detailError.value = error instanceof Error ? error.message : '加载详情失败'
  } finally {
    detailLoading.value = false
  }
}

function closeDetail() {
  detailVisible.value = false
}

async function updateStatus(item: CoachBookingItem, status: number) {
  const titleMap: Record<number, string> = {
    0: '确认登记缺席/取消吗？已支付订单如需退款，请联系管理员处理。',
    3: '确认将该预约更新为签到上课吗？',
    4: '确认将该预约更新为已完成吗？'
  }

  uni.showModal({
    title: '更新预约状态',
    content: titleMap[status] || '确认更新状态吗？',
    confirmColor: '#ff6600',
    success: async (result) => {
      if (!result.confirm) return
      try {
        await updateBookingStatusForCoach({
          id: item.id,
          status
        })
        uni.showToast({
          title: status === 0 ? '已登记，请联系管理员处理退款' : '状态已更新',
          icon: 'success'
        })
        await loadList()
        if (detailVisible.value && detail.value?.id === item.id) {
          await openDetail(item)
        }
      } catch (error) {
        uni.showToast({
          title: error instanceof Error ? error.message : '更新失败',
          icon: 'none'
        })
      }
    }
  })
}

function applyFilters() {
  page.value = 1
  loadList()
}

function selectStatus(value: number | null) {
  selectedStatus.value = value
  applyFilters()
}

function clearCourseFilter() {
  activeCourseId.value = null
  activeCourseName.value = ''
  applyFilters()
}

function resetFilters() {
  keyword.value = ''
  selectedStatus.value = null
  activeCourseId.value = null
  activeCourseName.value = ''
  page.value = 1
  loadList()
}

function changePage(nextPage: number) {
  if (nextPage < 1 || nextPage > totalPages.value || nextPage === page.value) return
  page.value = nextPage
  loadList()
}

function handleRefresh() {
  refreshing.value = true
  loadList()
}

onLoad((options) => {
  const courseId = Number(options?.courseId || 0)
  activeCourseId.value = Number.isFinite(courseId) && courseId > 0 ? courseId : null
  activeCourseName.value = options?.courseName ? decodeURIComponent(options.courseName) : ''
  const rawStatus = options?.status
  const routeStatus = rawStatus !== undefined && rawStatus !== '' ? Number(rawStatus) : null
  if (routeStatus !== null && Number.isInteger(routeStatus)) {
    selectedStatus.value = routeStatus
  }
})

onShow(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.coach-bookings-page {
  min-height: 100vh;
  background: radial-gradient(circle at top, rgba(255, 102, 0, 0.14), transparent 28%), #f9f9f9;
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
  color: #a33e00;
}

.brand-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.hero-card {
  padding: 34rpx 30rpx;
  border-radius: 34rpx;
  background: linear-gradient(135deg, #1f1f1f 0%, #2f3131 100%);
  color: #ffffff;
  box-shadow: 0 16rpx 32rpx rgba(26, 28, 28, 0.18);
}

.hero-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  text-transform: uppercase;
  color: rgba(255, 215, 0, 0.72);
}

.hero-title {
  display: block;
  margin-top: 12rpx;
  font-size: 48rpx;
  font-weight: 900;
  color: #ffd700;
}

.hero-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.82);
}

.page-body {
  padding: 0 24rpx;
}

.filter-card,
.booking-card,
.pagination-card,
.state-card,
.detail-panel {
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

.status-scroll {
  margin-top: 20rpx;
  white-space: nowrap;
}

.status-row {
  display: inline-flex;
  gap: 10rpx;
  padding: 8rpx;
  border-radius: 22rpx;
  background: #ececec;
}

.tab {
  padding: 14rpx 24rpx;
  border-radius: 16rpx;
  color: #6b625c;
  font-size: 22rpx;
  font-weight: 700;
}

.tab.active {
  background: #ff6600;
  color: #ffffff;
}

.filter-banner {
  margin-top: 18rpx;
  padding: 18rpx 20rpx;
  border-radius: 20rpx;
  background: #fff1e8;
  color: #a33e00;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.filter-clear {
  font-weight: 800;
}

.filter-actions,
.action-row,
.pagination-actions {
  display: flex;
  gap: 12rpx;
}

.filter-actions {
  margin-top: 20rpx;
}

.action-btn,
.page-btn {
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

.booking-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.booking-card {
  padding: 26rpx;
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
}

.booking-no {
  display: block;
  font-size: 18rpx;
  color: #8e7164;
}

.member-name {
  display: block;
  margin-top: 8rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.head-right {
  text-align: right;
}

.amount {
  display: block;
  font-size: 30rpx;
  font-weight: 900;
  color: #ff6600;
}

.status {
  display: inline-block;
  margin-top: 10rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;
}

.status.warning {
  background: #fff1e8;
  color: #a33e00;
}

.status.success {
  background: #e6f6ea;
  color: #1f7a37;
}

.status.neutral {
  background: #ededed;
  color: #5f5e5e;
}

.meta-row {
  margin-top: 14rpx;
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b625c;
}

.meta-row-emphasis {
  color: #1a1c1c;
}

.action-row {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 2rpx solid #f4efec;
  flex-wrap: wrap;
}

.ghost-btn {
  margin: 0;
  min-width: 0;
  height: 78rpx;
  padding: 0 22rpx;
  border: none;
  border-radius: 22rpx;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 800;
  background: #1a1c1c;
}

.ghost-btn-light {
  background: #f3f3f3;
  color: #1a1c1c;
}

.ghost-btn-green {
  background: #1f7a37;
}

.ghost-btn-blue {
  background: #1f4e9d;
}

.ghost-btn-danger {
  background: #ba1a1a;
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
  border-top-color: #ff6600;
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
  background: #ff6600;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 800;
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

.detail-mask {
  position: fixed;
  inset: 0;
  z-index: 80;
  background: rgba(0, 0, 0, 0.36);
  padding: 120rpx 24rpx 180rpx;
}

.detail-panel {
  width: 100%;
  max-height: 100%;
  overflow-y: auto;
  padding: 28rpx 24rpx 34rpx;
}

.detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.detail-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.detail-close {
  font-size: 24rpx;
  color: #a33e00;
  font-weight: 800;
}

.detail-state {
  padding: 60rpx 0 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.detail-item {
  margin-top: 20rpx;
  padding-bottom: 18rpx;
  border-bottom: 2rpx solid #f3f3f3;
}

.detail-label {
  display: block;
  font-size: 20rpx;
  color: #8e7164;
}

.detail-value {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: #1a1c1c;
  line-height: 1.6;
  font-weight: 700;
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
