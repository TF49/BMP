<template>
  <MobileLayout>
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">预约详情</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="detail-card">
        <view class="status-row">
          <text class="venue-name">{{ booking.venueName }}</text>
          <text class="booking-status" :class="getStatusClass(booking.status)">
            {{ getStatusText(booking.status) }}
          </text>
        </view>
        <view class="detail-list">
          <view class="detail-item">
            <text class="detail-label">预约单号</text>
            <text class="detail-value">{{ booking.bookingNo }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">场地</text>
            <text class="detail-value">{{ booking.courtName }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">预约日期</text>
            <text class="detail-value">{{ booking.bookingDate }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">时间段</text>
            <text class="detail-value">{{ booking.startTime }} - {{ booking.endTime }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">订单金额</text>
            <text class="detail-value price">¥{{ booking.orderAmount }}</text>
          </view>
          <view class="detail-item" v-if="booking.createTime">
            <text class="detail-label">下单时间</text>
            <text class="detail-value">{{ booking.createTime }}</text>
          </view>
        </view>
        <view class="action-row" v-if="booking.status === 1 || booking.status === 2">
          <button class="cancel-btn" @click="handleCancel">取消预约</button>
        </view>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getBookingDetail, cancelBooking } from '@/api/booking'
import { safeNavigateBack } from '@/utils/navigation'

const bookingId = ref<number>(0)
const booking = ref<any>({
  id: 0,
  bookingNo: '',
  venueName: '',
  courtName: '',
  bookingDate: '',
  startTime: '',
  endTime: '',
  orderAmount: 0,
  status: 0,
  createTime: ''
})
const userStore = useUserStore()

onLoad((options?: Record<string, string | undefined>) => {
  if (options?.id) {
    bookingId.value = Number(options.id)
  }
})

const loadBookingDetail = async () => {
  try {
    const result = await getBookingDetail(bookingId.value)
    booking.value = {
      id: result.id,
      bookingNo: result.bookingNo,
      venueName: result.venueName || result.courtName,
      courtName: result.courtName,
      bookingDate: result.bookingDate,
      startTime: result.startTime,
      endTime: result.endTime,
      orderAmount: result.orderAmount,
      status: result.status,
      createTime: result.createTime
    }
  } catch (error) {
    console.error('加载预约详情失败:', error)
    uni.showToast({
      title: '加载预约详情失败',
      icon: 'none'
    })
  }
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '已支付',
    3: '进行中',
    4: '已完成'
  }
  return map[status] ?? '未知'
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = {
    0: 'status-cancelled',
    1: 'status-pending',
    2: 'status-paid',
    3: 'status-ongoing',
    4: 'status-completed'
  }
  return map[status] ?? ''
}

const handleBack = () => {
  safeNavigateBack()
}

const handleCancel = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消该预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelBooking(booking.value.id)
          uni.showToast({ title: '取消成功', icon: 'success' })
          await loadBookingDetail()
        } catch (e) {
          console.error('取消预约失败:', e)
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  if (bookingId.value) {
    await loadBookingDetail()
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.header {
  background-color: #ffffff;
  padding: 20rpx 28rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  border-bottom: 1rpx solid #e6e6e6;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-icon {
  font-size: 40rpx;
  color: #333333;
  font-weight: bold;
  width: 56rpx;
}

.header-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
  text-align: center;
}

.header-placeholder {
  width: 56rpx;
}

.content {
  flex: 1;
  height: calc(100vh - 120rpx);
  background-color: #f5f7fa;
  padding: 24rpx 28rpx;
}

.detail-card {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.venue-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
}

.booking-status {
  font-size: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;

  &.status-pending {
    background-color: #fff3e0;
    color: #ff9800;
  }
  &.status-paid {
    background-color: #e8f5e9;
    color: #3cc51f;
  }
  &.status-ongoing {
    background-color: #e3f2fd;
    color: #2196f3;
  }
  &.status-completed {
    background-color: #f5f5f5;
    color: #999999;
  }
  &.status-cancelled {
    background-color: #ffebee;
    color: #f44336;
  }
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
}

.detail-label {
  color: #999999;
  width: 160rpx;
}

.detail-value {
  color: #333333;
  flex: 1;
  text-align: right;

  &.price {
    font-weight: bold;
    color: #ef4444;
    font-size: 28rpx;
  }
}

.action-row {
  margin-top: 32rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.cancel-btn {
  width: 100%;
  padding: 24rpx;
  background-color: #ffffff;
  color: #ef4444;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 12rpx;
  border: 1rpx solid #ef4444;
}
</style>
