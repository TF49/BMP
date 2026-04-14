<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f8fafc">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">课程预约管理</text>
        </view>
      </view>

      <view class="toolbar">
        <view class="search-box">
          <uni-icons type="search" size="18" color="#71717a" />
          <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="搜索会员或课程"
            confirm-type="search"
            @confirm="reloadList"
          />
        </view>
        <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
          <view class="tabs">
            <view class="tab" :class="{ active: statusFilter === -1 }" @click="setStatus(-1)">全部</view>
            <view class="tab" :class="{ active: statusFilter === 1 }" @click="setStatus(1)">待支付</view>
            <view class="tab" :class="{ active: statusFilter === 2 }" @click="setStatus(2)">已支付</view>
            <view class="tab" :class="{ active: statusFilter === 3 }" @click="setStatus(3)">进行中</view>
            <view class="tab" :class="{ active: statusFilter === 4 }" @click="setStatus(4)">已完成</view>
            <view class="tab" :class="{ active: statusFilter === 0 }" @click="setStatus(0)">已取消</view>
          </view>
        </scroll-view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载课程预约中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="reloadList">重试</view>
        </view>

        <view v-else-if="bookingList.length === 0" class="state-wrap">
          <text>暂无符合条件的课程预约</text>
        </view>

        <view v-else class="list">
          <view class="summary">
            <text>共 {{ total }} 条预约</text>
            <text>当前展示 {{ bookingList.length }} 条</text>
          </view>

          <view v-for="item in bookingList" :key="item.id" class="card" @click="goDetail(item.id)">
            <view class="row">
              <view>
                <text class="name">{{ item.memberName || '未知会员' }}</text>
                <text class="sub">{{ item.courseName || '未命名课程' }}</text>
              </view>
              <view class="status-pill" :class="item.statusMeta.key">
                {{ item.statusMeta.label }}
              </view>
            </view>

            <view class="meta-grid">
              <view class="meta-item">
                <text class="label">教练</text>
                <text class="value">{{ item.coachName || '未指定教练' }}</text>
              </view>
              <view class="meta-item">
                <text class="label">时间</text>
                <text class="value">{{ item.scheduleText }}</text>
              </view>
              <view class="meta-item">
                <text class="label">金额</text>
                <text class="value">¥{{ item.amountText }}</text>
              </view>
              <view class="meta-item">
                <text class="label">创建时间</text>
                <text class="value">{{ item.createTimeText }}</text>
              </view>
            </view>
            <view class="card-actions" @click.stop>
              <view class="action-btn" @click="openActions(item)">操作</view>
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
import { onLoad, onShow } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import {
  deleteCourseBooking,
  getCourseBookingList,
  processCourseBookingPayment,
  processCourseBookingRefund,
  type CourseBookingItem,
  updateCourseBookingStatus
} from '@/api/course'
import { parsePagedList } from '@/utils/parsePagedList'
import { formatDate, formatDateTime, formatTime } from '@/utils/format'
import { getCourseBookingStatusMeta } from '@/utils/presidentStatus'
import { BOOKING_STATUS, PAYMENT_METHOD, PAYMENT_METHOD_TEXT } from '@/utils/constant'

type BookingCard = {
  id: number
  memberName: string
  courseName: string
  coachName: string
  scheduleText: string
  amountText: string
  createTimeText: string
  statusMeta: ReturnType<typeof getCourseBookingStatusMeta>
  rawStatus: number
  paymentStatus: number
}

const page = ref(1)
const size = 10
const total = ref(0)
const loading = ref(false)
const errorText = ref('')
const keyword = ref('')
const statusFilter = ref(-1)
const courseId = ref<number | undefined>(undefined)
const bookingList = ref<BookingCard[]>([])

const hasMore = computed(() => bookingList.value.length < total.value)

function mapBooking(item: CourseBookingItem): BookingCard {
  const date = item.courseDate ? formatDate(item.courseDate, 'YYYY.MM.DD') : ''
  const start = item.courseStartTime ? formatTime(item.courseStartTime, 'HH:mm') : ''
  const end = item.courseEndTime ? formatTime(item.courseEndTime, 'HH:mm') : ''

  return {
    id: Number(item.id || 0),
    memberName: item.memberName || '',
    courseName: item.courseName || '',
    coachName: item.coachName || '',
    scheduleText: date && start && end ? `${date} ${start}-${end}` : '未设置排期',
    amountText: Number(item.orderAmount || 0).toFixed(2),
    createTimeText: formatDateTime(item.createTime, 'YYYY-MM-DD HH:mm') || '-',
    statusMeta: getCourseBookingStatusMeta(item.status),
    rawStatus: Number(item.status ?? -1),
    paymentStatus: Number(item.paymentStatus ?? 0)
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
    const res = await getCourseBookingList({
      page: page.value,
      size,
      courseId: courseId.value,
      status: statusFilter.value >= 0 ? statusFilter.value : undefined,
      keyword: keyword.value || undefined
    })
    const parsed = parsePagedList<CourseBookingItem>(res)
    const mapped = parsed.list.map(mapBooking).filter(item => item.id > 0)
    total.value = parsed.total
    bookingList.value = reset ? mapped : bookingList.value.concat(mapped)
  } catch (error) {
    if (!bookingList.value.length) {
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
  if (courseId.value) {
    safeNavigateBack(`${PRESIDENT_PAGES.COURSE_DETAIL}?id=${courseId.value}`)
    return
  }
  safeNavigateBack(PRESIDENT_PAGES.COURSE_LIST)
}

function goDetail(id: number) {
  if (!id) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURSE_BOOKING_DETAIL}?id=${id}` })
}

async function updateBookingStatusAction(item: BookingCard, status: number, title: string) {
  try {
    await updateCourseBookingStatus(item.id, status)
    uni.showToast({ title, icon: 'success' })
    reloadList()
  } catch (error) {
    console.error('Failed to update course booking status:', error)
  }
}

async function payBooking(item: BookingCard) {
  const paymentMethods = [
    PAYMENT_METHOD.CASH,
    PAYMENT_METHOD.ALIPAY,
    PAYMENT_METHOD.WECHAT,
    PAYMENT_METHOD.BALANCE
  ] as const
  const res = await uni.showActionSheet({
    itemList: paymentMethods.map((method) => PAYMENT_METHOD_TEXT[method])
  })
  const selectedMethod = paymentMethods[res.tapIndex]
  if (!selectedMethod) return

  try {
    await processCourseBookingPayment(item.id, selectedMethod)
    uni.showToast({ title: `${PAYMENT_METHOD_TEXT[selectedMethod]}收款成功`, icon: 'success' })
    reloadList()
  } catch (error) {
    console.error('Failed to process course booking payment:', error)
  }
}

async function refundBooking(item: BookingCard) {
  const { confirm } = await uni.showModal({
    title: '确认退款',
    content: '退款后将撤销当前课程预约支付记录，是否继续？'
  })
  if (!confirm) return

  try {
    await processCourseBookingRefund(item.id)
    uni.showToast({ title: '退款成功', icon: 'success' })
    reloadList()
  } catch (error) {
    console.error('Failed to refund course booking:', error)
  }
}

async function deleteBooking(item: BookingCard) {
  const { confirm } = await uni.showModal({
    title: '确认删除',
    content: '删除后将无法恢复该预约记录，是否继续？'
  })
  if (!confirm) return

  try {
    await deleteCourseBooking(item.id)
    uni.showToast({ title: '删除成功', icon: 'success' })
    reloadList()
  } catch (error) {
    console.error('Failed to delete course booking:', error)
  }
}

function openActions(item: BookingCard) {
  const actions: Array<{ label: string; handler: () => void }> = [{ label: '查看详情', handler: () => goDetail(item.id) }]

  if (item.rawStatus === BOOKING_STATUS.PENDING_PAYMENT) {
    actions.push({ label: '确认收款', handler: () => void payBooking(item) })
    actions.push({
      label: '取消预约',
      handler: () => void updateBookingStatusAction(item, BOOKING_STATUS.CANCELLED, '已取消预约')
    })
  } else if (item.rawStatus === BOOKING_STATUS.PAID) {
    actions.push({
      label: '开始上课',
      handler: () => void updateBookingStatusAction(item, BOOKING_STATUS.IN_PROGRESS, '已开始上课')
    })
    actions.push({ label: '退款', handler: () => void refundBooking(item) })
  } else if (item.rawStatus === BOOKING_STATUS.IN_PROGRESS) {
    actions.push({
      label: '标记完成',
      handler: () => void updateBookingStatusAction(item, BOOKING_STATUS.COMPLETED, '已标记完成')
    })
  } else if (item.rawStatus === BOOKING_STATUS.CANCELLED || item.rawStatus === BOOKING_STATUS.COMPLETED) {
    actions.push({ label: '删除记录', handler: () => void deleteBooking(item) })
  }

  uni.showActionSheet({
    itemList: actions.map((action) => action.label),
    success: (res) => actions[res.tapIndex]?.handler()
  })
}

onLoad((query?: Record<string, string | undefined>) => {
  const id = Number(query?.courseId || 0)
  courseId.value = id > 0 ? id : undefined
})

onShow(() => {
  reloadList()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f8fafc; color: #111827; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { padding: 16rpx 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.icon-btn { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #ffffff; display: flex; align-items: center; justify-content: center; }
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
.card-actions { display: flex; justify-content: flex-end; }
.action-btn { display: inline-flex; align-items: center; justify-content: center; min-width: 120rpx; height: 64rpx; padding: 0 24rpx; border-radius: 16rpx; background: #ffedd5; color: #c2410c; font-size: 22rpx; font-weight: 700; }
.row { display: flex; justify-content: space-between; gap: 12rpx; align-items: flex-start; }
.name { display: block; font-size: 30rpx; font-weight: 800; }
.sub { display: block; margin-top: 6rpx; font-size: 22rpx; color: #6b7280; }
.status-pill { padding: 10rpx 18rpx; border-radius: 9999px; font-size: 18rpx; font-weight: 800; }
.status-pill.pending { background: #fef3c7; color: #b45309; }
.status-pill.paid { background: #dbeafe; color: #1d4ed8; }
.status-pill.ongoing { background: #e0f2fe; color: #0369a1; }
.status-pill.completed { background: #dcfce7; color: #166534; }
.status-pill.cancelled { background: #fee2e2; color: #b91c1c; }
.status-pill.unknown { background: #e5e7eb; color: #4b5563; }
.meta-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.label { display: block; font-size: 18rpx; color: #6b7280; margin-bottom: 6rpx; }
.value { font-size: 24rpx; font-weight: 700; line-height: 1.5; }
.bottom-space { height: 36rpx; }
</style>
