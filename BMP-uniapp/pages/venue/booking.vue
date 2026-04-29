<template>
  <view class="booking-page">
    <view class="topbar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="topbar-inner">
        <view class="topbar-left">
          <view class="icon-btn" @click="handleBack">
            <uni-icons type="left" size="20" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">场地预约</text>
            <text class="topbar-sub">BOOKING FLOW</text>
          </view>
        </view>
        <text class="topbar-brand">KINETIC LOGIC</text>
      </view>
    </view>

    <scroll-view class="page-scroll" scroll-y :style="{ height: scrollHeight }" :show-scrollbar="false">
      <view class="hero-shell">
        <image
          v-if="venue.venueImage"
          class="hero-image"
          :src="resolveImageUrl(venue.venueImage)"
          mode="aspectFill"
        />
        <view v-else class="hero-placeholder">
          <uni-icons type="image" size="44" color="#9ca3af" />
        </view>
        <view class="hero-overlay" />
        <view class="hero-noise" />
        <view class="hero-content">
          <view class="hero-badges">
            <text class="hero-badge">{{ venue.status === 1 ? 'BOOKING OPEN' : 'UNAVAILABLE' }}</text>
            <text class="hero-badge soft">{{ monthLabel || '未来 7 天可预约' }}</text>
          </view>
          <text class="hero-title">{{ venue.name || '场地预约' }}</text>
          <text class="hero-copy">{{ heroDescription }}</text>
          <view class="hero-meta">
            <view class="hero-meta-item">
              <uni-icons type="location" size="14" color="#ffffff" />
              <text class="hero-meta-text">{{ venue.location || '暂无地址' }}</text>
            </view>
            <view class="hero-meta-item">
              <uni-icons type="wallet" size="14" color="#ffffff" />
              <text class="hero-meta-text">{{ canEstimate ? `预计金额 ¥${totalPrice}` : '完成选择后自动计算金额' }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="page-body">
        <view class="flow-ribbon">
          <view v-for="item in flowSteps" :key="item.step" class="flow-card">
            <text class="flow-step">{{ item.step }}</text>
            <text class="flow-title">{{ item.title }}</text>
            <text class="flow-copy">{{ item.copy }}</text>
          </view>
        </view>

        <view class="overview-grid">
          <view class="overview-card warm">
            <text class="overview-kicker">Estimated</text>
            <text class="overview-value">¥{{ totalPrice }}</text>
            <text class="overview-hint">{{ canEstimate ? durationLabel : '完成日期、场地、时间选择后自动更新' }}</text>
          </view>
          <view class="overview-card dark">
            <text class="overview-kicker light">Selected Court</text>
            <text class="overview-mini light">{{ selectedCourt?.name || '待选择场地' }}</text>
            <text class="overview-hint light">{{ selectedCourtStatusText }}</text>
          </view>
          <view class="overview-card">
            <text class="overview-kicker">Booking Day</text>
            <text class="overview-mini">{{ selectedDateDisplay }}</text>
            <text class="overview-hint">支持未来 7 天自由预约</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Schedule</text>
              <text class="section-title">选择日期</text>
            </view>
            <text class="section-note">{{ monthLabel || '近期档期' }}</text>
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
                <text class="date-week">{{ item.isToday ? '今天' : item.week }}</text>
                <text class="date-day">{{ item.day }}</text>
                <text class="date-month">{{ item.monthText }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Court</text>
              <text class="section-title">选择场地</text>
            </view>
            <text class="section-note">共 {{ courts.length }} 片场地</text>
          </view>

          <view class="court-grid">
            <view
              v-for="(court, index) in courts"
              :key="court.id"
              class="court-card"
              :class="{ selected: selectedCourtIndex === index, occupied: court.status !== 1 }"
              @click="handleCourtSelect(index)"
            >
              <view class="court-top">
                <text class="court-code">COURT {{ court.number }}</text>
                <text v-if="selectedCourtIndex === index" class="court-state active">已选</text>
                <text v-else-if="court.status !== 1" class="court-state disabled">不可约</text>
                <text v-else class="court-state normal">可预约</text>
              </view>
              <text class="court-name">{{ court.name }}</text>
              <text class="court-desc">{{ court.desc }}</text>
              <view class="court-tags">
                <text class="court-tag">{{ court.billingType === 'TIME' ? '按次计费' : '按小时计费' }}</text>
                <text class="court-tag">{{ court.status === 1 ? '即时确认' : '暂停使用' }}</text>
              </view>
              <text class="court-price">¥{{ court.displayPrice }}/{{ court.billingType === 'TIME' ? '次' : '小时' }}</text>
            </view>
          </view>
        </view>

        <view class="section-card timing-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Time Slot</text>
              <text class="section-title">选择时间</text>
            </view>
            <text class="section-note">{{ isEndAfterStart ? durationLabel : '请选择有效时间段' }}</text>
          </view>

          <view class="time-grid">
            <view class="time-card">
              <text class="time-label">开始时间</text>
              <picker mode="time" :value="startTime" @change="onStartTimeChange">
                <view class="time-value">
                  <uni-icons type="clock" size="16" color="#ff6600" />
                  <text>{{ startTime || '请选择开始时间' }}</text>
                </view>
              </picker>
            </view>
            <view class="time-card">
              <text class="time-label">结束时间</text>
              <picker mode="time" :value="endTime" @change="onEndTimeChange">
                <view class="time-value">
                  <uni-icons type="redo" size="16" color="#ff6600" />
                  <text>{{ endTime || '请选择结束时间' }}</text>
                </view>
              </picker>
            </view>
          </view>

          <view class="quick-slot-wrap">
            <text class="quick-slot-title">常用时段</text>
            <view class="quick-slot-list">
              <view
                v-for="slot in quickSlots"
                :key="slot.label"
                class="quick-slot-item"
                :class="{ active: startTime === slot.start && endTime === slot.end }"
                @click="applyQuickSlot(slot.start, slot.end)"
              >
                <text class="quick-slot-label">{{ slot.label }}</text>
                <text class="quick-slot-time">{{ slot.start }} - {{ slot.end }}</text>
              </view>
            </view>
          </view>

          <view class="tips-card">
            <view class="tips-row">
              <uni-icons type="info" size="14" color="#ff6600" />
              <text>预约时间支持自由选择，金额会按场地计费方式自动计算。</text>
            </view>
            <view class="tips-row">
              <uni-icons type="calendar" size="14" color="#ff6600" />
              <text>若该时间段已有预约，系统会在下方即时提醒并展示冲突信息。</text>
            </view>
          </view>

          <view v-if="showConflictCard" class="conflict-card">
            <view class="conflict-head">
              <uni-icons type="clear" size="16" color="#b42318" />
              <text class="conflict-title">该时段已被占用</text>
            </view>
            <text class="conflict-text">当前有 {{ occupancyCount }} 条重叠预约，请调整时间后再提交。</text>
            <view v-if="occupancyUsers.length" class="conflict-list">
              <view
                v-for="item in occupancyUsers"
                :key="`${item.bookingId}-${item.startTime}-${item.endTime}`"
                class="conflict-item"
              >
                <text class="conflict-user">{{ item.memberName || '已预约会员' }}</text>
                <text class="conflict-time">{{ item.startTime }} - {{ item.endTime }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="section-card summary-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Booking Summary</text>
              <text class="section-title">预约摘要</text>
            </view>
          </view>

          <view class="summary-lines">
            <view class="summary-line">
              <text class="line-label">场馆</text>
              <text class="line-value">{{ venue.name || '待确认' }}</text>
            </view>
            <view class="summary-line">
              <text class="line-label">场地</text>
              <text class="line-value">{{ selectedCourt?.name || '待选择' }}</text>
            </view>
            <view class="summary-line">
              <text class="line-label">日期</text>
              <text class="line-value">{{ selectedDateDisplay }}</text>
            </view>
            <view class="summary-line">
              <text class="line-label">时段</text>
              <text class="line-value">{{ `${startTime || '--:--'} - ${endTime || '--:--'}` }}</text>
            </view>
            <view class="summary-line strong">
              <text class="line-label">预计金额</text>
              <text class="line-price">¥{{ totalPrice }}</text>
            </view>
          </view>
        </view>

        <view class="section-card promise-card">
          <view class="promise-shell">
            <view>
              <text class="section-kicker inverse">Ready To Checkout</text>
              <text class="promise-title">确认后进入订单页</text>
              <text class="promise-copy">{{ promiseCopy }}</text>
            </view>
            <view class="promise-points">
              <text class="promise-point">日期与场地实时联动</text>
              <text class="promise-point">冲突时段即时提示</text>
              <text class="promise-point">金额按计费规则自动计算</text>
            </view>
          </view>
        </view>

        <view class="scroll-buffer" />
      </view>
    </scroll-view>

    <view class="bottom-bar">
      <view class="bottom-glass" />
      <view class="bottom-content">
        <view class="bottom-info">
          <text class="bottom-caption">{{ canEstimate ? selectedDateDisplay : '请选择完整预约信息' }}</text>
          <text class="bottom-price">¥{{ totalPrice }}</text>
          <text class="bottom-meta">{{ selectedCourt ? selectedCourt.name : '待选场地' }} · {{ durationMeta }}</text>
        </view>
        <button class="bottom-cta" :class="{ disabled: submitDisabled }" @click="handleSubmit">
          <text class="bottom-cta-top">Go Checkout</text>
          <text class="bottom-cta-bottom">立即预约</text>
        </button>
      </view>
    </view>
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
import { isManagementRole } from '@/utils/roleCheck'
import { formatAmount } from '@/utils/format'
import { useCurrentMember } from '@/composables/useCurrentMember'

type DateOption = {
  date: string
  week: string
  day: string
  monthText: string
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

const quickSlots = [
  { label: '晨练档', start: '08:00', end: '09:00' },
  { label: '下班后', start: '18:00', end: '19:00' },
  { label: '晚间双打', start: '19:00', end: '21:00' }
] as const

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value + 60}px)`)

const venueId = ref<number>(0)
const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()
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
const selectedDateDisplay = computed(() => {
  const item = dates.value[selectedDateIndex.value]
  if (!item) return '待选择'
  return `${item.isToday ? '今天' : item.week} · ${item.monthText}${item.day}日`
})
const selectedCourt = computed(() => (selectedCourtIndex.value >= 0 ? courts.value[selectedCourtIndex.value] : null))
const monthLabel = computed(() => {
  if (!selectedDate.value) return ''
  const [year, month] = selectedDate.value.split('-')
  return `${year}年${Number(month)}月`
})

const flowSteps = computed(() => [
  {
    step: '01',
    title: '选日期',
    copy: '先锁定哪一天去打球'
  },
  {
    step: '02',
    title: '选场地',
    copy: '对比不同场地的状态与价格'
  },
  {
    step: '03',
    title: '选时间',
    copy: '确认时段后直接进入结算'
  }
])

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

const durationMeta = computed(() => {
  if (!isEndAfterStart.value) return '待选时段'
  return selectedCourt.value?.billingType === 'TIME' ? '按次计费' : durationLabel.value.replace('预计', '')
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
const heroDescription = computed(() => {
  return venue.value.status === 1
    ? '沿着新版 Stitch 用户端链路完成日期、场地与时段选择，金额与冲突会实时反馈。'
    : '当前场馆暂不支持在线预约，你仍可浏览场地与时段信息，待恢复后继续下单。'
})
const selectedCourtStatusText = computed(() => {
  if (!selectedCourt.value) return '请先选择可用场地'
  return selectedCourt.value.status === 1 ? '当前可预约，支持即时确认' : '当前不可预约，请更换场地'
})
const promiseCopy = computed(() => {
  return submitDisabled.value
    ? '补齐日期、场地和时间后，底部会自动解锁预约按钮，并带着当前摘要进入订单确认页。'
    : '当前组合已经可以提交，系统会保留场馆、场地、时间和金额信息，继续进入订单页完成确认。'
})

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
      monthText: `${Number(mm)}月`,
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

function applyQuickSlot(start: string, end: string) {
  startTime.value = start
  endTime.value = end
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
    const member = await fetchCurrentMember()
    const payload = {
      memberId: member.id,
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

    const isManagement = isManagementRole(userStore.userInfo?.role)
    const returnUrl = isManagement ? `/pages/president/venue/detail?id=${venueId.value}` : '/pages/booking/list'

    uni.navigateTo({
      url: `/pages/booking/confirm?bookingId=${encodeURIComponent(String((res as any)?.id || bookingSummary.bookingId || ''))}&data=${encodeURIComponent(JSON.stringify(bookingSummary))}&returnUrl=${encodeURIComponent(returnUrl)}`
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
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.16), transparent 26%),
    linear-gradient(180deg, #fff8f2 0%, #f7f2ed 22%, #f4efea 100%);
}

.topbar {
  width: 100%;
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(250, 246, 241, 0.82);
  backdrop-filter: blur(20rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 28rpx;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.06);
}

.topbar-copy {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.topbar-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.topbar-sub,
.topbar-brand {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.topbar-brand {
  color: #a33e00;
}

.page-scroll {
  box-sizing: border-box;
}

.hero-shell {
  position: relative;
  height: 620rpx;
  overflow: hidden;
  border-radius: 0 0 42rpx 42rpx;
  box-shadow: 0 22rpx 44rpx rgba(26, 28, 28, 0.08);
}

.hero-image,
.hero-placeholder {
  width: 100%;
  height: 100%;
}

.hero-placeholder {
  background: linear-gradient(135deg, #f3ede8 0%, #e2d8d1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(26, 28, 28, 0.16) 0%, rgba(26, 28, 28, 0.84) 100%);
}

.hero-noise {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.08), transparent 42%),
    radial-gradient(circle at 18% 20%, rgba(255, 255, 255, 0.18), transparent 18%);
}

.hero-content {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: 30rpx;
  z-index: 2;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 18rpx;
}

.hero-badge {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #ff6600;
  color: #561d00;
  font-size: 18rpx;
  font-weight: 900;
  letter-spacing: 2rpx;

  &.soft {
    background: rgba(255, 255, 255, 0.16);
    color: #ffffff;
    backdrop-filter: blur(14rpx);
  }
}

.hero-title {
  display: block;
  font-size: 58rpx;
  line-height: 1.04;
  font-weight: 900;
  color: #ffffff;
}

.hero-copy {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: rgba(255, 244, 236, 0.9);
}

.hero-meta {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-top: 20rpx;
}

.hero-meta-item {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.hero-meta-text {
  flex: 1;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.94);
}

.page-body {
  padding: 0 24rpx 0;
}

.flow-ribbon {
  margin-top: -40rpx;
  position: relative;
  z-index: 3;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
}

.flow-card {
  min-height: 152rpx;
  padding: 20rpx 18rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.06);
}

.flow-step {
  display: block;
  font-size: 18rpx;
  font-weight: 900;
  color: #a33e00;
  letter-spacing: 2rpx;
}

.flow-title {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.flow-copy {
  display: block;
  margin-top: 10rpx;
  font-size: 20rpx;
  line-height: 1.55;
  color: #6b625c;
}

.overview-grid {
  margin-top: 22rpx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.overview-card {
  min-height: 170rpx;
  padding: 22rpx 20rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.06);
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  &.warm {
    background: linear-gradient(135deg, #fff0e8 0%, #ffffff 100%);
  }

  &.dark {
    background: linear-gradient(135deg, #241816 0%, #47312b 100%);
  }
}

.overview-kicker {
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;

  &.light {
    color: rgba(255, 236, 225, 0.78);
  }
}

.overview-value {
  font-size: 44rpx;
  line-height: 1;
  font-weight: 900;
  color: #a33e00;
}

.overview-mini {
  font-size: 24rpx;
  line-height: 1.45;
  font-weight: 800;
  color: #1a1c1c;

  &.light {
    color: #ffffff;
  }
}

.overview-hint {
  font-size: 20rpx;
  line-height: 1.55;
  color: #6b625c;

  &.light {
    color: rgba(255, 241, 234, 0.76);
  }
}

.section-card {
  margin-top: 28rpx;
  padding: 28rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16rpx 34rpx rgba(26, 28, 28, 0.05);
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 22rpx;

  &.compact {
    margin-bottom: 18rpx;
  }
}

.section-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 3rpx;
  text-transform: uppercase;

  &.inverse {
    color: rgba(255, 225, 205, 0.76);
  }
}

.section-title {
  display: block;
  margin-top: 8rpx;
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
  letter-spacing: -0.6rpx;
}

.section-note {
  font-size: 20rpx;
  font-weight: 700;
  color: #8e7164;
  line-height: 1.5;
}

.date-scroll {
  white-space: nowrap;
}

.date-list {
  display: inline-flex;
  gap: 16rpx;
}

.date-item {
  min-width: 138rpx;
  padding: 18rpx 20rpx;
  border-radius: 28rpx;
  background: #f7f3f1;
  border: 2rpx solid transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;

  &.active {
    background: linear-gradient(180deg, #fff0e8 0%, #ffffff 100%);
    border-color: #ff6600;
    box-shadow: 0 12rpx 26rpx rgba(255, 102, 0, 0.12);
  }
}

.date-week {
  font-size: 20rpx;
  color: #8e7164;
}

.date-day {
  font-size: 40rpx;
  line-height: 1;
  font-weight: 900;
  color: #1a1c1c;
}

.date-month {
  font-size: 18rpx;
  color: #6b625c;
}

.court-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.court-card {
  min-height: 262rpx;
  padding: 22rpx;
  border-radius: 28rpx;
  background: linear-gradient(180deg, #fbf8f5 0%, #f7f1ec 100%);
  border: 2rpx solid transparent;
  display: flex;
  flex-direction: column;

  &.selected {
    border-color: #ff6600;
    background: linear-gradient(180deg, #fff1e8 0%, #ffffff 100%);
    box-shadow: 0 14rpx 26rpx rgba(255, 102, 0, 0.1);
  }

  &.occupied {
    opacity: 0.62;
  }
}

.court-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.court-code {
  font-size: 18rpx;
  font-weight: 900;
  letter-spacing: 2rpx;
  color: #8e7164;
}

.court-state {
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  font-size: 18rpx;
  font-weight: 800;

  &.active {
    background: #ff6600;
    color: #ffffff;
  }

  &.disabled {
    background: #ffdad6;
    color: #93000a;
  }

  &.normal {
    background: #e8f7ee;
    color: #0f8f54;
  }
}

.court-name {
  margin-top: 18rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.court-desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: #6b625c;
}

.court-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-top: 14rpx;
}

.court-tag {
  padding: 8rpx 12rpx;
  border-radius: 999rpx;
  background: #ece8e6;
  font-size: 18rpx;
  font-weight: 700;
  color: #5a4136;
}

.court-price {
  margin-top: auto;
  padding-top: 18rpx;
  font-size: 28rpx;
  font-weight: 900;
  color: #a33e00;
}

.timing-card {
  background: linear-gradient(180deg, #ffffff 0%, #fff9f4 100%);
}

.time-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.time-card,
.tips-card,
.conflict-card {
  border-radius: 28rpx;
  background: #faf8f6;
  padding: 24rpx;
}

.time-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  margin-bottom: 14rpx;
}

.time-value {
  min-height: 88rpx;
  border-radius: 22rpx;
  background: #ffffff;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.quick-slot-wrap {
  margin-top: 20rpx;
}

.quick-slot-title {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.quick-slot-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
  margin-top: 16rpx;
}

.quick-slot-item {
  padding: 18rpx 16rpx;
  border-radius: 24rpx;
  background: #f7f3f1;
  border: 2rpx solid transparent;

  &.active {
    border-color: #ff6600;
    background: #fff0e8;
  }
}

.quick-slot-label {
  display: block;
  font-size: 22rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.quick-slot-time {
  display: block;
  margin-top: 8rpx;
  font-size: 18rpx;
  line-height: 1.5;
  color: #6b625c;
}

.tips-card,
.conflict-card {
  margin-top: 18rpx;
}

.tips-row {
  display: flex;
  align-items: flex-start;
  gap: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #5f5e5e;

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
  font-size: 26rpx;
  font-weight: 900;
  color: #b42318;
}

.conflict-text {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #7a271a;
}

.conflict-list {
  margin-top: 16rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.conflict-item {
  background: rgba(255, 255, 255, 0.82);
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

.summary-card {
  background: linear-gradient(180deg, #ffffff 0%, #fffaf6 100%);
}

.summary-lines {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.summary-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding-bottom: 12rpx;
  border-bottom: 2rpx solid #f2ece8;

  &.strong {
    border-bottom: none;
    padding-bottom: 0;
    padding-top: 8rpx;
  }
}

.line-label {
  font-size: 22rpx;
  color: #6b625c;
}

.line-value {
  flex: 1;
  text-align: right;
  font-size: 24rpx;
  line-height: 1.5;
  font-weight: 800;
  color: #1a1c1c;
}

.line-price {
  font-size: 34rpx;
  line-height: 1;
  font-weight: 900;
  color: #a33e00;
}

.promise-card {
  background: transparent;
  padding: 0;
  box-shadow: none;
}

.promise-shell {
  padding: 30rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #241714 0%, #59372d 58%, #9e430b 100%);
  box-shadow: 0 22rpx 48rpx rgba(88, 43, 16, 0.22);
}

.promise-title {
  display: block;
  margin-top: 10rpx;
  font-size: 40rpx;
  line-height: 1.18;
  font-weight: 900;
  color: #ffffff;
}

.promise-copy {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.75;
  color: rgba(255, 242, 232, 0.86);
}

.promise-points {
  margin-top: 22rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.promise-point {
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.12);
  font-size: 20rpx;
  font-weight: 700;
  color: #ffffff;
}

.scroll-buffer {
  height: calc(220rpx + env(safe-area-inset-bottom));
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  padding-bottom: env(safe-area-inset-bottom);
}

.bottom-glass {
  position: absolute;
  inset: 0;
  background: rgba(255, 252, 248, 0.9);
  backdrop-filter: blur(24rpx);
  box-shadow: 0 -16rpx 40rpx rgba(26, 28, 28, 0.06);
}

.bottom-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 22rpx 24rpx 20rpx;
}

.bottom-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.bottom-caption {
  font-size: 20rpx;
  line-height: 1.45;
  color: #8e7164;
}

.bottom-price {
  margin-top: 4rpx;
  font-size: 46rpx;
  line-height: 1;
  font-weight: 900;
  color: #1a1c1c;
}

.bottom-meta {
  margin-top: 6rpx;
  font-size: 20rpx;
  line-height: 1.5;
  color: #6b625c;
}

.bottom-cta {
  width: 248rpx;
  border: none;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  padding: 18rpx 12rpx;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  box-shadow: 0 14rpx 30rpx rgba(255, 102, 0, 0.22);

  &.disabled {
    opacity: 0.45;
  }
}

.bottom-cta-top,
.bottom-cta-bottom {
  color: #ffffff;
}

.bottom-cta-top {
  font-size: 20rpx;
}

.bottom-cta-bottom {
  font-size: 28rpx;
  font-weight: 900;
}
</style>
