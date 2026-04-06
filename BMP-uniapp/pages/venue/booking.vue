<template>
  <view class="booking-page">
    <!-- TopAppBar -->
    <header class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="left-section">
          <view class="back-btn" @click="handleBack">
            <uni-icons type="left" size="24" color="#ff6600"></uni-icons>
          </view>
          <text class="title">场地预约</text>
        </view>
        <text class="brand">KINETIC</text>
      </view>
    </header>

    <scroll-view class="main-content" scroll-y :style="{ height: scrollHeight }">
      <!-- Hero Section: Venue Info -->
      <section class="hero-section">
        <image 
          v-if="venue.venueImage"
          class="hero-image" 
          :src="resolveImageUrl(venue.venueImage)"
          mode="aspectFill"
        ></image>
        <view v-else class="hero-image-placeholder">
          <uni-icons type="image" size="48" color="#999999"></uni-icons>
        </view>
        <view class="hero-overlay"></view>
        <view class="hero-info">
          <view class="status-row">
            <text class="status-badge" :class="{ 'status-offline': venue.status !== 1 }">
              {{ venue.status === 1 ? '营业中' : '已关闭' }}
            </text>
            <view class="rating-badge">
              <uni-icons type="star-filled" size="12" color="#ffffff"></uni-icons>
              <text class="rating-text">4.9 (120+)</text>
            </view>
          </view>
          <text class="venue-name">{{ venue.name }}</text>
          <view class="location-row">
            <uni-icons type="location" size="14" color="#ffffff"></uni-icons>
            <text class="location-text">{{ venue.location || '暂无地址' }}</text>
          </view>
        </view>
      </section>

      <!-- Selection Canvas -->
      <view class="selection-canvas">
        <!-- Date Picker -->
        <section class="date-picker-section">
          <view class="section-header">
            <view class="header-left">
              <text class="label">预约排期</text>
              <text class="h3">选择日期</text>
            </view>
            <text class="month-label">2023年10月</text>
          </view>
          <scroll-view class="date-scroll" scroll-x show-scrollbar="false">
            <view class="date-list">
              <view 
                v-for="(item, index) in dates" 
                :key="index"
                class="date-item"
                :class="{ active: selectedDateIndex === index }"
                @click="handleDateChange(index)"
              >
                <text class="week">{{ item.isToday ? '今天' : item.week }}</text>
                <text class="day">{{ item.day }}</text>
              </view>
            </view>
          </scroll-view>
        </section>

        <!-- Court Grid -->
        <section class="court-section">
          <view class="section-header">
            <view class="header-left">
              <text class="label">场地偏好</text>
              <text class="h3">选择场地</text>
            </view>
          </view>
          <view class="court-grid">
            <view 
              v-for="(court, index) in courts" 
              :key="court.id"
              class="court-card"
              :class="{ 
                selected: selectedCourtIndex === index,
                occupied: court.status === 'occupied'
              }"
              @click="handleCourtSelect(index)"
            >
              <view class="card-header">
                <text class="court-number">{{ court.number }}</text>
                <text v-if="selectedCourtIndex === index" class="selected-badge">已选</text>
                <text v-if="court.status === 'occupied'" class="occupied-badge">客满</text>
              </view>
              <text class="court-name">{{ court.name }}</text>
              <text class="court-desc">{{ court.status === 'occupied' ? '已预约' : court.desc }}</text>
            </view>
          </view>
        </section>

        <!-- Time Slots -->
        <section class="time-section">
          <view class="section-header">
            <view class="header-left">
              <text class="label">时段状态</text>
              <text class="h3">选择时段</text>
            </view>
          </view>

          <!-- Grouped by Period -->
          <view v-for="period in periods" :key="period.id" class="period-group">
            <view class="period-title-row">
              <uni-icons :type="period.icon" size="14" color="#5f5e5e"></uni-icons>
              <text class="period-title">{{ period.title }} ({{ period.range }})</text>
            </view>
            <view class="time-grid">
              <view 
                v-for="slot in period.slots" 
                :key="slot.time"
                class="time-slot"
                :class="{ selected: selectedSlots.includes(slot.time) }"
                @click="toggleTimeSlot(slot.time)"
              >
                <text class="time">{{ slot.time }}</text>
                <text class="price">¥{{ slot.price }}</text>
              </view>
            </view>
          </view>
        </section>
      </view>
      
      <!-- Bottom Spacer for Footer -->
      <view class="bottom-spacer"></view>
    </scroll-view>

    <!-- BottomNavBar: Summary Footer -->
    <nav class="bottom-nav">
      <view class="nav-content">
        <view class="summary-info">
          <text class="summary-label">已选时段 ({{ selectedSlots.length }})</text>
          <text class="total-price">¥{{ totalPrice }}</text>
        </view>
        <button class="book-btn" @click="handleSubmit">
          <text class="btn-top">去结算</text>
          <text class="btn-bottom">立即预约</text>
        </button>
      </view>
    </nav>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { safeNavigateBack } from '@/utils/navigation'

import { onLoad } from '@dcloudio/uni-app'
import { getVenueDetail } from '@/api/venue'
import { getCourtList } from '@/api/court'
import { createBooking } from '@/api/booking'
import { useUserStore } from '@/store/modules/user'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { isPresidentRole } from '@/utils/roleCheck'

// System info for height calculation
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value + 60}px)`)

const venueId = ref<number>(0)
const userStore = useUserStore()
const venue = ref<any>({
  id: 0,
  name: '正在加载...',
  venueImage: '',
  location: '',
  price: 0,
  status: 1
})

const courts = ref<any[]>([])
const selectedCourtIndex = ref(-1)

const periods = ref([
  {
    id: 'morning',
    title: '上午',
    range: '08:00 - 12:00',
    icon: 'tune', 
    slots: [
      { time: '08:00', price: 60 },
      { time: '10:00', price: 60 },
      { time: '11:00', price: 60 },
    ]
  },
  {
    id: 'afternoon',
    title: '下午',
    range: '12:00 - 18:00',
    icon: 'sun',
    slots: [
      { time: '14:00', price: 100 },
      { time: '16:00', price: 100 },
    ]
  },
  {
    id: 'evening',
    title: '晚上',
    range: '18:00 - 22:00',
    icon: 'moon',
    slots: [
      { time: '18:00', price: 120 },
      { time: '20:00', price: 120 },
    ]
  }
])

const selectedSlots = ref<string[]>(['10:00', '14:00'])

onLoad((options: any) => {
  if (options && options.venueId) {
    venueId.value = Number(options.venueId)
  }
})

const loadVenueDetail = async () => {
  try {
    const result = await getVenueDetail(venueId.value)
    venue.value = {
      id: result.id,
      name: result.venueName,
      venueImage: result.venueImage || '',
      location: result.address || '',
      price: result.hourlyPrice ?? 0,
      status: result.status ?? 1
    }
    // Update slot prices based on venue hourly price if needed
    // For now keeping period mock prices as they differ by time of day in prototype
  } catch (error) {
    console.error('加载场馆详情失败:', error)
  }
}

// Generate Date List
const dates = ref<any[]>([])
const selectedDateIndex = ref(0)

const generateDateList = () => {
  const list = []
  const today = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  for (let i = 0; i < 7; i++) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    list.push({
      date: d.toISOString().split('T')[0],
      week: weekDays[d.getDay()],
      day: d.getDate().toString().padStart(2, '0'),
      isToday: i === 0
    })
  }
  dates.value = list
}

const loadCourtList = async () => {
  if (!venueId.value) return
  try {
    const selectedDate = dates.value[selectedDateIndex.value]?.date
    const result = await getCourtList({
      venueId: venueId.value,
      date: selectedDate,
      page: 1,
      size: 100
    })
    
    courts.value = result.data.map((c: any, idx: number) => ({
      id: c.id,
      number: (idx + 1).toString().padStart(2, '0'),
      name: c.courtName || `场地 ${idx + 1}`,
      desc: c.status === 1 ? '专业防滑面层' : '不可预约',
      status: c.status === 1 ? 'available' : 'occupied'
    }))
    
    // Automatically select first available if none selected
    if (selectedCourtIndex.value === -1 || selectedCourtIndex.value >= courts.value.length) {
      selectedCourtIndex.value = courts.value.findIndex(c => c.status === 'available')
    }
  } catch (e) {
    console.error('Load court list failed:', e)
  }
}

onMounted(async () => {
  generateDateList()
  if (venueId.value) {
    await loadVenueDetail()
    await loadCourtList()
  }
})

function handleDateChange(index: number) {
  selectedDateIndex.value = index
  loadCourtList()
}

// Computed
const totalPrice = computed(() => {
  let total = 0
  periods.value.forEach(p => {
    p.slots.forEach(s => {
      if (selectedSlots.value.includes(s.time)) {
        total += s.price
      }
    })
  })
  return total
})

// Methods
const handleBack = () => {
  safeNavigateBack()
}

const handleCourtSelect = (index: number) => {
  if (courts.value[index].status === 'occupied') return
  selectedCourtIndex.value = index
}

const toggleTimeSlot = (time: string) => {
  const index = selectedSlots.value.indexOf(time)
  if (index > -1) {
    selectedSlots.value.splice(index, 1)
  } else {
    selectedSlots.value.push(time)
  }
}

const handleSubmit = async () => {
  if (selectedSlots.value.length === 0) {
    uni.showToast({ title: '请选择时段', icon: 'none' })
    return
  }
  
  if (selectedCourtIndex.value === -1) {
    uni.showToast({ title: '请选择场地', icon: 'none' })
    return
  }

  const selectedCourt = courts.value[selectedCourtIndex.value]
  const selectedDate = dates.value[selectedDateIndex.value].date
  
  // To keep it simple and consistent with previous flow:
  // We sort slots and take first and last for startTime/endTime
  const sortedSlots = [...selectedSlots.value].sort()
  const startTime = sortedSlots[0]
  // Extract hour from last slot and add 1 hour (simplified)
  const lastHour = parseInt(sortedSlots[sortedSlots.length - 1].split(':')[0])
  const endTime = `${(lastHour + 1).toString().padStart(2, '0')}:00`

  try {
    uni.showLoading({ title: '准备订单...' })
    
    // Create the actual booking (PENDING)
    const bookingData = {
      memberId: userStore.userInfo?.id || 0,
      courtId: selectedCourt.id,
      bookingDate: selectedDate,
      startTime: startTime,
      endTime: endTime,
      orderAmount: totalPrice.value,
      paymentMethod: 'BALANCE' 
    }

    const res = await createBooking(bookingData)
    uni.hideLoading()

    const bookingSummary = {
      venueName: venue.value.name,
      courtName: selectedCourt.name,
      date: selectedDate,
      slot: `${startTime} - ${endTime}`,
      totalAmount: totalPrice.value,
      vipDiscount: totalPrice.value * 0.2, 
      payableAmount: totalPrice.value * 0.8,
      bookingId: (res as any)?.id
    }

    const isPresident = isPresidentRole(userStore.userInfo?.role)
    const returnUrl = isPresident
      ? `/pages/president/venue/detail?id=${venueId.value}`
      : '/pages/booking/list'

    uni.navigateTo({
      url: `/pages/booking/confirm?data=${encodeURIComponent(JSON.stringify(bookingSummary))}&returnUrl=${encodeURIComponent(returnUrl)}`
    })
  } catch (e: any) {
    uni.hideLoading()
    uni.showToast({ title: e?.message || '创建预约失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.booking-page {
  min-height: 100vh;
  background-color: #f9f9f9;
}

/* Header */
.header {
  width: 100%;
  background-color: #ffffff;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #eeeeee;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
}

.left-section {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.back-btn {
  padding: 8rpx;
  border-radius: 50%;
  &:active { background-color: #f0f0f0; }
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.brand {
  font-size: 28rpx;
  font-weight: 900;
  color: #a33e00;
  letter-spacing: -1px;
}

/* Hero Section */
.hero-section {
  position: relative;
  height: 397rpx;
  width: 100%;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(0deg, rgba(26, 28, 28, 0.8) 0%, rgba(26, 28, 28, 0) 100%);
}

.hero-info {
  position: absolute;
  bottom: 32rpx;
  left: 32rpx;
  right: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.status-badge {
  background-color: #10b981;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 700;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  letter-spacing: 2rpx;

  &.status-offline {
    background-color: #e2e2e2 !important;
    color: #5f5e5e !important;
  }
}

.hero-image-placeholder {
  width: 100%;
  height: 100%;
  background-color: #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rating-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8rpx);
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
}

.rating-text {
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 700;
}

.venue-name {
  color: #ffffff;
  font-size: 48rpx;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.location-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.location-text {
  color: rgba(255, 255, 255, 0.8);
  font-size: 24rpx;
}

/* Selection Canvas */
.selection-canvas {
  padding: 48rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 64rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24rpx;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.label {
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  text-transform: uppercase;
  letter-spacing: 4rpx;
}

.h3 {
  font-size: 44rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.month-label {
  color: #a33e00;
  font-weight: 700;
  font-size: 24rpx;
}

/* Date Picker */
.date-scroll {
  white-space: nowrap;
  width: 100%;
}

.date-list {
  display: flex;
  gap: 24rpx;
  padding-bottom: 8rpx;
}

.date-item {
  flex-shrink: 0;
  width: 112rpx;
  height: 168rpx;
  background-color: #ffffff;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  transition: all 0.2s ease;

  &.active {
    background-color: #ff6600;
    box-shadow: 0 16rpx 32rpx rgba(255, 102, 0, 0.2);
    .week, .day { color: #ffffff; }
    .week { opacity: 0.8; }
  }
}

.week {
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  text-transform: uppercase;
}

.day {
  font-size: 44rpx;
  font-weight: 900;
  color: #1a1c1c;
}

/* Court Grid */
.court-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
}

.court-card {
  background-color: #ffffff;
  padding: 32rpx;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  border: 4rpx solid transparent;
  transition: all 0.2s ease;

  &.selected {
    border-color: #a33e00;
    .court-number { color: #a33e00; }
  }

  &.occupied {
    opacity: 0.5;
    pointer-events: none;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24rpx;
}

.court-number {
  font-size: 56rpx;
  font-weight: 900;
  color: #e2e2e2;
  line-height: 1;
}

.selected-badge {
  background-color: rgba(163, 62, 0, 0.1);
  color: #a33e00;
  font-size: 20rpx;
  font-weight: 700;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

.occupied-badge {
  background-color: #e2e2e2;
  color: #5f5e5e;
  font-size: 20rpx;
  font-weight: 700;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
}

.court-name {
  font-size: 24rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.court-desc {
  font-size: 20rpx;
  color: #5f5e5e;
}

/* Time Slots */
.time-section {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.period-group {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.period-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.period-title {
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.time-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.time-slot {
  background-color: #ffffff;
  padding: 24rpx 32rpx;
  border-radius: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;

  &.selected {
    background-color: #ff6600;
    transform: scale(1.02);
    box-shadow: 0 12rpx 24rpx rgba(255, 102, 0, 0.3);
    .time, .price { color: #ffffff; }
  }

  &:active { opacity: 0.8; }
}

.time {
  font-size: 24rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.price {
  font-size: 24rpx;
  font-weight: 700;
  color: #5f5e5e;
}

.bottom-spacer {
  height: 200rpx;
}

/* Bottom Nav */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(24rpx);
  border-top-left-radius: 48rpx;
  border-top-right-radius: 48rpx;
  box-shadow: 0 -16rpx 48rpx rgba(0, 0, 0, 0.05);
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 48rpx;
}

.summary-info {
  display: flex;
  flex-direction: column;
}

.summary-label {
  font-size: 20rpx;
  font-weight: 500;
  color: #5f5e5e;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}

.total-price {
  font-size: 48rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.book-btn {
  background-color: #ff6600;
  color: #ffffff;
  border-radius: 24rpx;
  padding: 24rpx 64rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  line-height: 1.2;
  box-shadow: 0 16rpx 32rpx rgba(255, 102, 0, 0.3);
  margin: 0;
  border: none;
  
  &:active { transform: scale(0.95); opacity: 0.9; }
}

.btn-top {
  font-size: 20rpx;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 4rpx;
}

.btn-bottom {
  font-size: 32rpx;
  font-weight: 700;
}
</style>
