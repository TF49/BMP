<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">创建预约</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Venue Info -->
      <view class="section">
        <view class="venue-info">
          <view class="venue-header">
            <text class="venue-name">{{ venue.name }}</text>
            <text class="venue-price">¥{{ venue.price }}/小时</text>
          </view>
          <view class="venue-location">
            <uni-icons type="location" size="14" color="#475569"></uni-icons>
            <text>{{ venue.location }}</text>
          </view>
        </view>
      </view>

      <!-- Date Selection -->
      <view class="section">
        <text class="section-title">选择日期</text>
        <scroll-view class="date-scroll" scroll-x>
          <view class="date-list">
            <view 
              v-for="(date, index) in dateList" 
              :key="index"
              class="date-item"
              :class="{ active: selectedDateIndex === index }"
              @click="selectDate(index)"
            >
              <text class="date-week">{{ date.week }}</text>
              <text class="date-day">{{ date.day }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- Court Selection -->
      <view class="section">
        <text class="section-title">选择场地</text>
        <view class="court-list">
          <view 
            v-for="(court, index) in courtList" 
            :key="court.id"
            class="court-item"
            :class="{ selected: selectedCourtId === court.id }"
            @click="selectCourt(court.id)"
          >
            <text class="court-name">{{ court.name }}</text>
            <text class="court-status" :class="getStatusClass(court.status)">
              {{ getStatusText(court.status) }}
            </text>
          </view>
        </view>
      </view>

      <!-- Time Selection -->
      <view class="section">
        <text class="section-title">选择时间</text>
        <view class="time-section">
          <view class="time-range">
            <picker mode="selector" :range="timeSlots" @change="onStartTimeChange">
              <view class="picker">
                <text class="picker-label">开始时间</text>
                <text class="picker-value">{{ selectedStartTime || '请选择' }}</text>
              </view>
            </picker>
            <picker mode="selector" :range="endTimeSlots" @change="onEndTimeChange">
              <view class="picker">
                <text class="picker-label">结束时间</text>
                <text class="picker-value">{{ selectedEndTime || '请选择' }}</text>
              </view>
            </picker>
          </view>
          <view class="time-duration">
            <text class="duration-text">时长：{{ duration }}小时</text>
          </view>
        </view>
      </view>

      <!-- Price Calculation -->
      <view class="section">
        <text class="section-title">费用明细</text>
        <view class="price-breakdown">
          <view class="price-item">
            <text class="price-label">单价</text>
            <text class="price-value">¥{{ venue.price }}/小时</text>
          </view>
          <view class="price-item">
            <text class="price-label">时长</text>
            <text class="price-value">{{ duration }}小时</text>
          </view>
          <view class="price-divider"></view>
          <view class="price-item total">
            <text class="price-label">总计</text>
            <text class="price-value total-price">¥{{ totalPrice }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Bottom Action Bar -->
    <view class="action-bar">
      <view class="total-price-display">
        <text class="total-label">合计</text>
        <text class="total-amount">¥{{ totalPrice }}</text>
      </view>
      <button class="submit-btn" :disabled="!canSubmit" @click="handleSubmit">
        确认预约
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getVenueDetail } from '@/api/venue'
import { getCourtList } from '@/api/court'
import { createBooking } from '@/api/booking'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const venueId = ref<number>(0)
const venue = ref<any>({
  id: 0,
  name: '',
  location: '',
  price: 50
})
const dateList = ref<any[]>([])
const selectedDateIndex = ref(0)
const courtList = ref<any[]>([])
const selectedCourtId = ref<number | null>(null)
const selectedStartTime = ref<string>('')
const selectedEndTime = ref<string>('')
const timeSlots = ref<string[]>([])
const endTimeSlots = ref<string[]>([])
const userStore = useUserStore()

// 计算属性
const duration = computed(() => {
  if (!selectedStartTime.value || !selectedEndTime.value) return 0
  
  const startHour = parseInt(selectedStartTime.value.split(':')[0])
  const endHour = parseInt(selectedEndTime.value.split(':')[0])
  
  return Math.max(0, endHour - startHour)
})

const totalPrice = computed(() => {
  return venue.value.price * duration.value
})

const canSubmit = computed(() => {
  return selectedCourtId.value && 
         selectedStartTime.value && 
         selectedEndTime.value && 
         duration.value > 0
})

// 页面加载
onLoad((options) => {
  if (options.venueId) {
    venueId.value = Number(options.venueId)
  }
})

// 生成日期列表
const generateDateList = () => {
  const dates = []
  const today = new Date()
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    
    const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    const week = weekDays[date.getDay()]
    const day = `${date.getMonth() + 1}-${date.getDate()}`
    
    dates.push({
      date: date.toISOString().split('T')[0], // YYYY-MM-DD
      week,
      day
    })
  }
  
  dateList.value = dates
}

// 生成时间槽
const generateTimeSlots = () => {
  const slots = []
  for (let hour = 8; hour <= 22; hour++) {
    slots.push(`${hour.toString().padStart(2, '0')}:00`)
  }
  timeSlots.value = slots
}

// 更新结束时间选项
const updateEndTimeSlots = () => {
  if (!selectedStartTime.value) {
    endTimeSlots.value = timeSlots.value
    return
  }
  
  const startIndex = timeSlots.value.indexOf(selectedStartTime.value)
  if (startIndex !== -1) {
    endTimeSlots.value = timeSlots.value.slice(startIndex + 1)
  } else {
    endTimeSlots.value = timeSlots.value
  }
  
  // 如果当前选择的结束时间不在新的选项中，则清空
  if (selectedEndTime.value && !endTimeSlots.value.includes(selectedEndTime.value)) {
    selectedEndTime.value = ''
  }
}

// 选择日期
const selectDate = (index: number) => {
  selectedDateIndex.value = index
  // 选择日期后重新加载场地信息
  loadCourtList()
}

// 选择场地
const selectCourt = (id: number) => {
  selectedCourtId.value = id
}

// 开始时间变化
const onStartTimeChange = (e: any) => {
  const index = parseInt(e.detail.value)
  selectedStartTime.value = timeSlots.value[index]
  updateEndTimeSlots()
}

// 结束时间变化
const onEndTimeChange = (e: any) => {
  const index = parseInt(e.detail.value)
  selectedEndTime.value = endTimeSlots.value[index]
}

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '维护中',
    1: '空闲',
    2: '预约中',
    3: '使用中'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'status-maintenance',
    1: 'status-available',
    2: 'status-booked',
    3: 'status-occupied'
  }
  return classMap[status] || ''
}

// 加载场馆详情
const loadVenueDetail = async () => {
  try {
    const result = await getVenueDetail(venueId.value)
    venue.value = {
      id: result.id,
      name: result.venueName,
      location: result.address,
      price: result.hourlyPrice || 50
    }
  } catch (error) {
    console.error('加载场馆详情失败:', error)
    uni.showToast({
      title: '加载场馆详情失败',
      icon: 'none'
    })
  }
}

// 加载场地列表
const loadCourtList = async () => {
  try {
    const selectedDate = dateList.value[selectedDateIndex.value]?.date
    const result = await getCourtList({
      venueId: venueId.value,
      date: selectedDate,
      page: 1,
      size: 100
    })
    
    courtList.value = result.data.map((court: any) => ({
      id: court.id,
      name: court.courtName || court.name,
      status: court.status
    }))
  } catch (error) {
    console.error('加载场地列表失败:', error)
    uni.showToast({
      title: '加载场地列表失败',
      icon: 'none'
    })
  }
}

// 提交预约
const handleSubmit = async () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请完善预约信息',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '提交中...'
    })

    const bookingData = {
      memberId: userStore.userInfo?.id,
      courtId: selectedCourtId.value,
      bookingDate: dateList.value[selectedDateIndex.value].date,
      startTime: selectedStartTime.value,
      endTime: selectedEndTime.value,
      orderAmount: totalPrice.value,
      paymentMethod: 'BALANCE' // 默认使用余额支付
    }

    const result = await createBooking(bookingData)
    
    uni.hideLoading()
    
    uni.showToast({
      title: '预约成功',
      icon: 'success'
    })

    // 延迟跳转到预约详情页
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/booking/detail?id=${result.id}`
      })
    }, 1500)
  } catch (error) {
    console.error('创建预约失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '预约失败，请重试',
      icon: 'none'
    })
  }
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 页面加载时获取数据
onMounted(async () => {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    // 未登录用户重定向到登录页
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }

  if (venueId.value) {
    generateDateList()
    generateTimeSlots()
    await loadVenueDetail()
    await loadCourtList()
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
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
}

.section {
  background-color: #ffffff;
  margin-bottom: 20rpx;
  padding: 28rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
  display: block;
}

.venue-info {
  padding: 16rpx 0;
}

.venue-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.venue-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.venue-price {
  font-size: 24rpx;
  color: #ef4444;
  font-weight: bold;
}

.venue-location {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #475569;
}

.date-scroll {
  white-space: nowrap;
  padding: 10rpx 0;
}

.date-list {
  display: flex;
  gap: 20rpx;
}

.date-item {
  flex-shrink: 0;
  width: 80rpx;
  padding: 20rpx 0;
  border-radius: 18rpx;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;

  &.active {
    background-color: #3cc51f;
    color: #ffffff;
  }
}

.date-week {
  font-size: 20rpx;
  margin-bottom: 6rpx;

  .date-item.active & {
    color: #ffffff;
  }
}

.date-day {
  font-size: 24rpx;
  font-weight: bold;

  .date-item.active & {
    color: #ffffff;
  }
}

.court-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.court-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 12rpx;
  transition: all 0.3s;

  &.selected {
    border-color: #3cc51f;
    background-color: #f0f9f2;
  }
}

.court-name {
  font-size: 24rpx;
  color: #333333;
}

.court-status {
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;

  &.status-available {
    background-color: #e8f5e9;
    color: #3cc51f;
  }

  &.status-booked {
    background-color: #fff3e0;
    color: #ff9800;
  }

  &.status-occupied {
    background-color: #ffebee;
    color: #f44336;
  }

  &.status-maintenance {
    background-color: #f5f5f5;
    color: #999999;
  }
}

.time-section {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.time-range {
  display: flex;
  gap: 20rpx;
}

.picker {
  flex: 1;
  padding: 16rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
}

.picker-label {
  font-size: 20rpx;
  color: #999999;
  margin-bottom: 8rpx;
}

.picker-value {
  font-size: 24rpx;
  color: #333333;
  font-weight: bold;
}

.time-duration {
  padding: 16rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  text-align: center;
}

.duration-text {
  font-size: 24rpx;
  color: #666666;
}

.price-breakdown {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  color: #333333;

  &.total {
    margin-top: 10rpx;
  }
}

.price-label {
  color: #666666;
}

.price-value {
  font-weight: bold;
  color: #333333;

  &.total-price {
    color: #ef4444;
    font-size: 28rpx;
  }
}

.price-divider {
  height: 1rpx;
  background-color: #e6e6e6;
  margin: 10rpx 0;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  height: 120rpx;
  background-color: #ffffff;
  border-top: 1rpx solid #e6e6e6;
  padding: 0 28rpx;
  box-sizing: border-box;
  align-items: center;
}

.total-price-display {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.total-label {
  font-size: 20rpx;
  color: #999999;
}

.total-amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #ef4444;
}

.submit-btn {
  flex: 1;
  height: 80rpx;
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 12rpx;
  border: none;
  margin-left: 28rpx;
  box-shadow: 0 2rpx 6rpx rgba(60, 197, 31, 0.2);

  &:disabled {
    background-color: #cccccc;
    color: #999999;
  }
}
</style>