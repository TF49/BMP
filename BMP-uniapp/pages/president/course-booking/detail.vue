<template>
  <PresidentLayout :showTabBar="false" className="president-course-booking-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">预约详情</text>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <view v-if="loading" class="state-card">
            <text class="state-text">正在加载预约详情...</text>
          </view>

          <view v-else-if="loadError" class="state-card">
            <text class="state-text">{{ loadError }}</text>
          </view>

          <template v-else-if="detail">
            <view class="hero-card">
              <text class="hero-label">预约状态</text>
              <text class="hero-value">{{ statusLabel }}</text>
              <text class="hero-sub">{{ detail.bookingNo || `预约 #${detail.id}` }}</text>
            </view>

            <view class="card">
              <text class="section-title">课程信息</text>
              <view class="field-row">
                <text class="field-label">课程名称</text>
                <text class="field-value">{{ detail.courseName || '未命名课程' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">教练</text>
                <text class="field-value">{{ detail.coachName || '未分配' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">日期</text>
                <text class="field-value">{{ detail.courseDate || '-' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">时间段</text>
                <text class="field-value">{{ timeRangeLabel }}</text>
              </view>
            </view>

            <view class="card">
              <text class="section-title">预约人与支付</text>
              <view class="field-row">
                <text class="field-label">会员</text>
                <text class="field-value">{{ detail.memberName || `会员 #${detail.memberId || '-'}` }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">课程 ID</text>
                <text class="field-value">{{ detail.courseId || '-' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">支付方式</text>
                <text class="field-value">{{ paymentMethodLabel }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">支付状态</text>
                <text class="field-value">{{ paymentStatusLabel }}</text>
              </view>
            </view>

            <view class="card">
              <text class="section-title">订单信息</text>
              <view class="field-row">
                <text class="field-label">订单金额</text>
                <text class="field-value amount">¥{{ formatAmount(detail.orderAmount || 0) }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">创建时间</text>
                <text class="field-value">{{ formatDateTime(detail.createTime) || '未知时间' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">更新时间</text>
                <text class="field-value">{{ formatDateTime(detail.updateTime) || '未知时间' }}</text>
              </view>
              <view class="field-row multiline">
                <text class="field-label">备注</text>
                <text class="field-value remark">{{ detail.remark || '无' }}</text>
              </view>
            </view>

            <view class="action-row">
              <view class="action-btn secondary" @click="openActions">管理预约</view>
            </view>
          </template>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import {
  deleteCourseBooking,
  getCourseBookingDetail,
  processCourseBookingPayment,
  processCourseBookingRefund,
  type CourseBookingItem,
  updateCourseBookingStatus
} from '@/api/president/course'
import { formatAmount, formatDateTime } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { BOOKING_STATUS, PAYMENT_METHOD, PAYMENT_METHOD_TEXT } from '@/utils/constant'

type CourseBookingDetail = CourseBookingItem & {
  coachName?: string
  courseDate?: string
  courseStartTime?: string
  courseEndTime?: string
  updateTime?: string
  remark?: string
  paymentStatus?: number
}

const detail = ref<CourseBookingDetail | null>(null)
const loading = ref(false)
const loadError = ref('')

const statusLabel = computed(() => {
  const status = detail.value?.status
  if (status === BOOKING_STATUS.CANCELLED) return '已取消'
  if (status === BOOKING_STATUS.PENDING_PAYMENT) return '待支付'
  if (status === BOOKING_STATUS.PAID) return '已支付'
  if (status === BOOKING_STATUS.IN_PROGRESS) return '进行中'
  if (status === BOOKING_STATUS.COMPLETED) return '已完成'
  return '未知状态'
})

const paymentMethodLabel = computed(() => detail.value?.paymentMethod || '未知支付方式')

const paymentStatusLabel = computed(() => {
  const status = detail.value?.paymentStatus
  if (status === 1) return '支付成功'
  if (status === 0) return '待支付'
  if (status === 2) return '已退款'
  return '支付状态未知'
})

const timeRangeLabel = computed(() => {
  const current = detail.value
  if (!current?.courseStartTime || !current?.courseEndTime) return '-'
  return `${current.courseStartTime} - ${current.courseEndTime}`
})

async function loadDetail(id: number) {
  loading.value = true
  loadError.value = ''
  try {
    detail.value = (await getCourseBookingDetail(id)) as CourseBookingDetail
  } catch (error) {
    console.error('Failed to load course booking detail:', error)
    detail.value = null
    loadError.value = '预约详情加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURSE_BOOKING_LIST)
}

async function refreshDetail() {
  if (!detail.value?.id) return
  await loadDetail(detail.value.id)
}

async function openActions() {
  if (!detail.value?.id) return
  const actions: Array<{ label: string; handler: () => Promise<void> | void }> = []
  const currentStatus = Number(detail.value.status ?? -1)

  if (currentStatus === BOOKING_STATUS.PENDING_PAYMENT) {
    actions.push({
      label: '确认收款',
      handler: async () => {
        const methods = [
          PAYMENT_METHOD.CASH,
          PAYMENT_METHOD.ALIPAY,
          PAYMENT_METHOD.WECHAT,
          PAYMENT_METHOD.BALANCE
        ] as const
        const result = await uni.showActionSheet({
          itemList: methods.map((method) => PAYMENT_METHOD_TEXT[method])
        })
        const selectedMethod = methods[result.tapIndex]
        if (!selectedMethod) return
        await processCourseBookingPayment(detail.value!.id, selectedMethod)
        uni.showToast({ title: `${PAYMENT_METHOD_TEXT[selectedMethod]}收款成功`, icon: 'success' })
        await refreshDetail()
      }
    })
    actions.push({
      label: '取消预约',
      handler: async () => {
        await updateCourseBookingStatus(detail.value!.id, BOOKING_STATUS.CANCELLED)
        uni.showToast({ title: '已取消预约', icon: 'success' })
        await refreshDetail()
      }
    })
  } else if (currentStatus === BOOKING_STATUS.PAID) {
    actions.push({
      label: '开始上课',
      handler: async () => {
        await updateCourseBookingStatus(detail.value!.id, BOOKING_STATUS.IN_PROGRESS)
        uni.showToast({ title: '已开始上课', icon: 'success' })
        await refreshDetail()
      }
    })
    actions.push({
      label: '退款',
      handler: async () => {
        await processCourseBookingRefund(detail.value!.id)
        uni.showToast({ title: '退款成功', icon: 'success' })
        await refreshDetail()
      }
    })
  } else if (currentStatus === BOOKING_STATUS.IN_PROGRESS) {
    actions.push({
      label: '标记完成',
      handler: async () => {
        await updateCourseBookingStatus(detail.value!.id, BOOKING_STATUS.COMPLETED)
        uni.showToast({ title: '已标记完成', icon: 'success' })
        await refreshDetail()
      }
    })
  } else if (currentStatus === BOOKING_STATUS.CANCELLED || currentStatus === BOOKING_STATUS.COMPLETED) {
    actions.push({
      label: '删除记录',
      handler: async () => {
        await deleteCourseBooking(detail.value!.id)
        uni.showToast({ title: '删除成功', icon: 'success' })
        goBack()
      }
    })
  }

  if (!actions.length) {
    uni.showToast({ title: '当前状态暂无可执行操作', icon: 'none' })
    return
  }

  const result = await uni.showActionSheet({ itemList: actions.map((action) => action.label) })
  const action = actions[result.tapIndex]
  if (!action) return

  try {
    await action.handler()
  } catch (error) {
    console.error('Failed to manage course booking detail:', error)
  }
}

onLoad((options) => {
  const rawId = Number(options?.bookingId || options?.id)
  if (!Number.isFinite(rawId) || rawId <= 0) {
    loadError.value = '缺少有效的预约 ID'
    return
  }
  loadDetail(rawId)
})
</script>

<style lang="scss" scoped>
.president-course-booking-detail-layout {
  :deep(.president-layout-content) {
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
}

.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
}

.nav-header {
  padding: 20rpx 32rpx 16rpx;
  background: #f9f9f9;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.main-scroll {
  height: calc(100vh - var(--status-bar-height) - 88rpx);
}

.scroll-inner {
  padding: 12rpx 32rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.state-card,
.hero-card,
.card {
  border-radius: 24rpx;
  background: #ffffff;
  padding: 36rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.state-text {
  color: #5f5e5e;
  text-align: center;
  font-size: 28rpx;
}

.hero-card {
  background: linear-gradient(135deg, #fff3eb 0%, #ffffff 100%);
}

.hero-label,
.section-title,
.field-label {
  color: #8a8a8a;
  font-size: 24rpx;
}

.hero-value {
  display: block;
  margin-top: 12rpx;
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.hero-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #5f5e5e;
}

.section-title {
  display: block;
  margin-bottom: 20rpx;
  font-weight: 700;
}

.field-row {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  padding: 18rpx 0;
  border-top: 1rpx solid #f1f1f1;
}

.field-row:first-of-type {
  border-top: none;
  padding-top: 0;
}

.field-value {
  flex: 1;
  text-align: right;
  color: #1a1c1c;
  font-size: 28rpx;
}

.amount {
  color: #a33e00;
  font-weight: 800;
}

.multiline {
  align-items: flex-start;
}

.remark {
  white-space: pre-wrap;
}

.action-row {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  min-width: 200rpx;
  padding: 20rpx 28rpx;
  border-radius: 20rpx;
  text-align: center;
  font-size: 24rpx;
  font-weight: 700;
}

.action-btn.secondary {
  background: #ffedd5;
  color: #c2410c;
}
</style>
