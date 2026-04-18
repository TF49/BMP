<template>
  <view class="booking-page">
    <header class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="left-section">
          <view class="back-btn" @click="handleBack">
            <uni-icons type="left" size="24" color="#ff6600" />
          </view>
          <text class="title">场地预约</text>
        </view>
        <text class="brand">KINETIC</text>
      </view>
    </header>

    <scroll-view class="main-content" scroll-y :style="{ height: scrollHeight }">
      <section class="hero-section">
        <image
          v-if="venue.venueImage"
          class="hero-image"
          :src="resolveImageUrl(venue.venueImage)"
          mode="aspectFill"
        />
        <view v-else class="hero-image-placeholder">
          <uni-icons type="image" size="48" color="#999999" />
        </view>
        <view class="hero-overlay" />
        <view class="hero-info">
          <view class="status-row">
            <text class="status-badge" :class="{ 'status-offline': venue.status !== 1 }">
              {{ venue.status === 1 ? '营业中' : '已关闭' }}
            </text>
            <view class="rating-badge">
              <uni-icons type="star-filled" size="12" color="#ffffff" />
              <text class="rating-text">4.9 (120+)</text>
            </view>
          </view>
          <text class="venue-name">{{ venue.name }}</text>
          <view class="location-row">
            <uni-icons type="location" size="14" color="#ffffff" />
            <text class="location-text">{{ venue.location || '暂无地址' }}</text>
          </view>
        </view>
      </section>

      <view class="selection-canvas">
        <section class="date-picker-section">
          <view class="section-header">
            <view class="header-left">
              <text class="label">预约排期</text>
              <text class="h3">选择日期</text>
            </view>
            <text class="month-label">{{ monthLabel }}</text>
          </view>
          <scroll-view class="date-scroll" scroll-x show-scrollbar="false">
            <view class="date-list">
              <view
                v-for="(item, index) in dates"
                :key="item.date"
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
              :class="{ selected: selectedCourtIndex === index, occupied: court.status !== 1 }"
              @click="handleCourtSelect(index)"
            >
              <view class="card-header">
                <text class="court-number">{{ court.number }}</text>
                <text v-if="selectedCourtIndex === index" class="selected-badge">已选</text>
                <text v-else-if="court.status !== 1" class="occupied-badge">不可约</text>
              </view>
              <text class="court-name">{{ court.name }}</text>
              <text class="court-desc">{{ court.desc }}</text>
              <text class="court-price">¥{{ court.displayPrice }}/{{ court.billingType === 'TIME' ? '次' : '小时' }}</text>
            </view>
          </view>
        </section>

        <section class="time-section">
          <view class="section-header">
            <view class="header-left">
              <text class="label">时段状态</text>
              <text class="h3">自由选择时间</text>
            </view>
          </view>

          <view class="time-grid-panel">
            <view class="time-card">
              <text class="time-card-label">开始时间</text>
              <picker mode="time" :value="startTime" @change="onStartTimeChange">
                <view class="time-card-value">
                  <uni-icons type="clock" size="16" color="#ff6600" />
                  <text>{{ startTime || '请选择开始时间' }}</text>
                </view>
              </picker>
            </view>
            <view class="time-card">
              <text class="time-card-label">结束时间</text>
              <picker mode="time" :value="endTime" @change="onEndTimeChange">
                <view class="time-card-value">
                  <uni-icons type="redo" size="16" color="#ff6600" />
                  <text>{{ endTime || '请选择结束时间' }}</text>
                </view>
              </picker>
            </view>
          </view>

          <view class="tips-card">
            <view class="tips-row">
              <uni-icons type="info" size="14" color="#ff6600" />
              <text>预约时间支持自由选择，最终金额按场地计费规则自动计算。</text>
            </view>
            <view class="tips-row">
              <uni-icons type="calendar" size="14" color="#ff6600" />
              <text>当前预计时长 {{ durationLabel }}</text>
            </view>
          </view>

          <view v-if="showConflictCard" class="conflict-card">
            <view class="conflict-head">
              <uni-icons type="clear" size="16" color="#b42318" />
              <text class="conflict-title">该时段已被占用</text>
            </view>
            <text class="conflict-text">当前有 {{ occupancyCount }} 条重叠预约，请调整时间后再提交。</text>
            <view v-if="occupancyUsers.length" class="conflict-list">
              <view v-for="item in occupancyUsers" :key="`${item.bookingId}-${item.startTime}-${item.endTime}`" class="conflict-item">
                <text class="conflict-user">{{ item.memberName || '已预约会员' }}</text>
                <text class="conflict-time">{{ item.startTime }} - {{ item.endTime }}</text>
              </view>
            </view>
          </view>
        </section>
      </view>

      <view class="bottom-spacer" />
    </scroll-view>

    <nav class="bottom-nav">
      <view class="nav-content">
        <view class="summary-info">
          <text class="summary-label">{{ canEstimate ? durationLabel : '请选择完整时间段' }}</text>
          <text class="total-price">¥{{ totalPrice }}</text>
        </view>
        <button class="book-btn" :class="{ disabled: submitDisabled }" @click="handleSubmit">
          <text class="btn-top">去结算</text>
          <text class="btn-bottom">立即预约</text>
        </button>
      </view>
    </nav>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { safeNavigateBack } from '@/utils/navigation'
import { getVenueDetail } from '@/api/venue'
import { getCourtList, type CourtItem } from '@/api/court'
import { createBooking, getBookingRangeOccupancy, type BookingOccupancyUser } from '@/api/booking'
import { useUserStore } from '@/store/modules/user'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { isPresidentRole } from '@/utils/roleCheck'
import { formatAmount } from '@/utils/format'

type DateOption = {
  date: string
  week: string
  day: string
  isToday: boolean
}

type CourtCard = {
  id: number
  number: string
  name: string
  desc: string
  status: number
  billingType: string
  pricePerHour: number
  displayPrice: string
}

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value + 60}px)`)

const venueId = ref<number>(0)
const userStore = useUserStore()
const venue = ref({
  id: 0,
  name: '正在加载...',
  venueImage: '',
  location: '',
  status: 1
})

const dates = ref<DateOption[]>([])
const selectedDateIndex = ref(0)
const courts = ref<CourtCard[]>([])
const selectedCourtIndex = ref(-1)
const startTime = ref('18:00')
const endTime = ref('19:00')
const occupancyUsers = ref<BookingOccupancyUser[]>([])
const occupancyCount = ref(0)
const occupancyLoading = ref(false)
let occupancyToken = 0

const selectedDate = computed(() => dates.value[selectedDateIndex.value]?.date || '')
const selectedCourt = computed(() => (selectedCourtIndex.value >= 0 ? courts.value[selectedCourtIndex.value] : null))
const monthLabel = computed(() => {
  if (!selectedDate.value) return ''
  const [year, month] = selectedDate.value.split('-')
  return `${year}年${Number(month)}月`
})

const canEstimate = computed(() => Boolean(selectedCourt.value && selectedDate.value && startTime.value && endTime.value))
const submitDisabled = computed(() => !canEstimate.value || !isEndAfterStart.value || occupancyCount.value > 0 || occupancyLoading.value)
const isEndAfterStart = computed(() => toMinutes(endTime.value) > toMinutes(startTime.value))

const durationHours = computed(() => {
  if (!isEndAfterStart.value) return 0
  const minutes = toMinutes(endTime.value) - toMinutes(startTime.value)
  return Math.ceil(minutes / 60)
})

const durationLabel = computed(() => {
  if (!isEndAfterStart.value) return '请选择有效时间段'
  const minutes = toMinutes(endTime.value) - toMinutes(startTime.value)
  const hours = Math.floor(minutes / 60)
  const remain = minutes % 60
  if (remain === 0) return `预计时长 ${hours} 小时`
  return `预计时长 ${hours > 0 ? `${hours} 小时 ` : ''}${remain} 分钟`
})

const totalPrice = computed(() => {
  const court = selectedCourt.value
  if (!court || !isEndAfterStart.value) return '0.00'
  const amount = court.billingType === 'TIME'
    ? court.pricePerHour
    : court.pricePerHour * durationHours.value
  return formatAmount(amount)
})

const showConflictCard = computed(() => canEstimate.value && isEndAfterStart.value && occupancyCount.value > 0)

onLoad((options?: Record<string, string | undefined>) => {
  if (options?.venueId) {
    venueId.value = Number(options.venueId)
  }
  generateDateList()
  if (venueId.value) {
    void loadVenueDetail()
    void loadCourtList()
  }
})

watch([selectedDateIndex, selectedCourtIndex, startTime, endTime], () => {
  void checkOccupancy()
})

function generateDateList() {
  const list: DateOption[] = []
  const today = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

  for (let i = 0; i < 7; i += 1) {
    const d = new Date(today)
    d.setDate(today.getDate() + i)
    const yyyy = d.getFullYear()
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    list.push({
      date: `${yyyy}-${mm}-${dd}`,
      week: weekDays[d.getDay()],
      day: dd,
      isToday: i === 0
    })
  }
  dates.value = list
}

async function loadVenueDetail() {
  try {
    const result = await getVenueDetail(venueId.value)
    venue.value = {
      id: result.id,
      name: result.venueName,
      venueImage: result.venueImage || '',
      location: result.address || '',
      status: result.status ?? 1
    }
  } catch (error) {
    console.error('加载场馆详情失败:', error)
  }
}

async function loadCourtList() {
  if (!venueId.value) return
  try {
    const result = await getCourtList({
      venueId: venueId.value,
      page: 1,
      size: 100
    })
    courts.value = (result.data || []).map((court: CourtItem, index: number) => ({
      id: court.id,
      number: String(index + 1).padStart(2, '0'),
      name: court.courtName || `场地 ${index + 1}`,
      desc: court.status === 1 ? '支持自由预约时间段' : '当前不可预约',
      status: court.status,
      billingType: court.billingType || 'HOUR',
      pricePerHour: Number(court.pricePerHour || court.pricePerTime || 0),
      displayPrice: formatAmount(Number(court.pricePerHour || court.pricePerTime || 0), 0)
    }))
    selectedCourtIndex.value = courts.value.findIndex((court) => court.status === 1)
  } catch (error) {
    console.error('加载场地失败:', error)
  }
}

function handleDateChange(index: number) {
  selectedDateIndex.value = index
}

function handleCourtSelect(index: number) {
  if (courts.value[index]?.status !== 1) return
  selectedCourtIndex.value = index
}

function onStartTimeChange(event: { detail: { value: string } }) {
  startTime.value = event.detail.value
}

function onEndTimeChange(event: { detail: { value: string } }) {
  endTime.value = event.detail.value
}

function toMinutes(value: string) {
  if (!value) return 0
  const [hour, minute] = value.split(':').map((item) => Number(item))
  return hour * 60 + minute
}

function formatApiTime(value: string) {
  return value.length === 5 ? `${value}:00` : value
}

async function checkOccupancy() {
  occupancyUsers.value = []
  occupancyCount.value = 0

  if (!selectedCourt.value || !selectedDate.value || !startTime.value || !endTime.value || !isEndAfterStart.value) {
    return
  }

  occupancyLoading.value = true
  const currentToken = occupancyToken + 1
  occupancyToken = currentToken

  try {
    const result = await getBookingRangeOccupancy({
      courtId: selectedCourt.value.id,
      bookingDate: selectedDate.value,
      startTime: formatApiTime(startTime.value),
      endTime: formatApiTime(endTime.value)
    })

    if (currentToken !== occupancyToken) return

    occupancyCount.value = Number(result.count || 0)
    occupancyUsers.value = Array.isArray(result.users) ? result.users : []
  } catch (error) {
    console.error('检查时段占用失败:', error)
  } finally {
    if (currentToken === occupancyToken) {
      occupancyLoading.value = false
    }
  }
}

function handleBack() {
  safeNavigateBack()
}

async function handleSubmit() {
  if (!selectedCourt.value) {
    uni.showToast({ title: '请选择场地', icon: 'none' })
    return
  }
  if (!selectedDate.value) {
    uni.showToast({ title: '请选择日期', icon: 'none' })
    return
  }
  if (!startTime.value || !endTime.value) {
    uni.showToast({ title: '请选择完整时间', icon: 'none' })
    return
  }
  if (!isEndAfterStart.value) {
    uni.showToast({ title: '结束时间必须晚于开始时间', icon: 'none' })
    return
  }
  if (occupancyCount.value > 0) {
    uni.showToast({ title: '该时段已被占用，请调整时间', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '准备订单...' })
    const payload = {
      courtId: selectedCourt.value.id,
      bookingDate: selectedDate.value,
      startTime: formatApiTime(startTime.value),
      endTime: formatApiTime(endTime.value),
      orderAmount: Number(totalPrice.value),
      paymentMethod: 'BALANCE'
    }
    const res = await createBooking(payload)
    uni.hideLoading()

    const bookingSummary = {
      venueName: venue.value.name,
      courtName: selectedCourt.value.name,
      date: selectedDate.value,
      slot: `${startTime.value} - ${endTime.value}`,
      duration: durationHours.value,
      totalAmount: Number(totalPrice.value),
      vipDiscount: 0,
      payableAmount: Number(totalPrice.value),
      bookingId: (res as any)?.id
    }

    const isPresident = isPresidentRole(userStore.userInfo?.role)
    const returnUrl = isPresident ? `/pages/president/venue/detail?id=${venueId.value}` : '/pages/booking/list'

    uni.navigateTo({
      url: `/pages/booking/confirm?data=${encodeURIComponent(JSON.stringify(bookingSummary))}&returnUrl=${encodeURIComponent(returnUrl)}`
    })
  } catch (error: any) {
    uni.hideLoading()
    uni.showToast({ title: error?.message || '创建预约失败', icon: 'none' })
  }
}
</script>

<style lang="scss" scoped>
.booking-page {
  min-height: 100vh;
  background-color: #f9f9f9;
}

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

  &:active {
    background-color: #f0f0f0;
  }
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

.hero-section {
  position: relative;
  height: 397rpx;
  width: 100%;
  overflow: hidden;
}

.hero-image,
.hero-image-placeholder {
  width: 100%;
  height: 100%;
}

.hero-image-placeholder {
  background-color: #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
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

.rating-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8rpx);
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
}

.rating-text,
.location-text {
  color: #ffffff;
  font-size: 20rpx;
}

.venue-name {
  color: #ffffff;
  font-size: 46rpx;
  font-weight: 800;
}

.location-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.selection-canvas {
  padding: 28rpx 24rpx 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.label {
  color: #a33e00;
  font-size: 20rpx;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.h3,
.month-label {
  color: #1a1c1c;
  font-size: 30rpx;
  font-weight: 800;
}

.date-picker-section,
.court-section,
.time-section {
  margin-bottom: 32rpx;
}

.date-scroll {
  white-space: nowrap;
}

.date-list {
  display: inline-flex;
  gap: 16rpx;
}

.date-item {
  min-width: 116rpx;
  padding: 18rpx 20rpx;
  border-radius: 28rpx;
  background: #ffffff;
  border: 2rpx solid transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.05);

  &.active {
    border-color: #ff6600;
    background: #fff3eb;
  }
}

.week {
  color: #7a7a7a;
  font-size: 20rpx;
}

.day {
  color: #1a1c1c;
  font-size: 36rpx;
  font-weight: 800;
}

.court-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.court-card {
  padding: 22rpx;
  border-radius: 28rpx;
  background: #ffffff;
  box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.05);
  border: 2rpx solid transparent;

  &.selected {
    border-color: #ff6600;
    background: #fff3eb;
  }

  &.occupied {
    opacity: 0.6;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14rpx;
}

.court-number,
.selected-badge,
.occupied-badge,
.court-price {
  font-size: 20rpx;
  font-weight: 700;
}

.selected-badge {
  color: #ff6600;
}

.occupied-badge {
  color: #b42318;
}

.court-name {
  font-size: 28rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.court-desc {
  margin-top: 8rpx;
  color: #666666;
  font-size: 22rpx;
}

.court-price {
  margin-top: 14rpx;
  color: #a33e00;
}

.time-grid-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.time-card,
.tips-card,
.conflict-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.05);
}

.time-card-label {
  display: block;
  color: #7a7a7a;
  font-size: 22rpx;
  margin-bottom: 12rpx;
}

.time-card-value {
  min-height: 84rpx;
  border-radius: 22rpx;
  background: #f6f7f8;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #1a1c1c;
  font-size: 28rpx;
  font-weight: 700;
}

.tips-card,
.conflict-card {
  margin-top: 18rpx;
}

.tips-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
  color: #5f5e5e;
  font-size: 22rpx;

  &:not(:first-child) {
    margin-top: 12rpx;
  }
}

.conflict-card {
  background: #fff4f2;
  border: 2rpx solid #fdb8b2;
}

.conflict-head {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.conflict-title {
  color: #b42318;
  font-size: 26rpx;
  font-weight: 800;
}

.conflict-text {
  display: block;
  margin-top: 12rpx;
  color: #7a271a;
  font-size: 22rpx;
}

.conflict-list {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.conflict-item {
  background: rgba(255, 255, 255, 0.75);
  border-radius: 18rpx;
  padding: 16rpx 18rpx;
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.conflict-user,
.conflict-time {
  font-size: 22rpx;
  color: #7a271a;
}

.bottom-spacer {
  height: 180rpx;
}

.bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(16rpx);
  border-top: 1rpx solid rgba(0, 0, 0, 0.06);
  padding: 22rpx 24rpx calc(env(safe-area-inset-bottom) + 22rpx);
}

.nav-content {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.summary-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.summary-label {
  color: #7a7a7a;
  font-size: 22rpx;
}

.total-price {
  color: #1a1c1c;
  font-size: 40rpx;
  font-weight: 900;
}

.book-btn {
  width: 240rpx;
  border: none;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff6600 0%, #ff8b2c 100%);
  color: #ffffff;
  padding: 18rpx 12rpx;
  display: flex;
  flex-direction: column;
  gap: 4rpx;

  &.disabled {
    opacity: 0.45;
  }
}

.btn-top,
.btn-bottom {
  color: #ffffff;
}

.btn-top {
  font-size: 20rpx;
}

.btn-bottom {
  font-size: 28rpx;
  font-weight: 800;
}
</style>
