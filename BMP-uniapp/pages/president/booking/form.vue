<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="booking-page">
      <view class="header" :style="{ paddingTop: `${statusBarHeight}px` }">
        <view class="header-inner">
          <view class="header-left" @tap="handleBack">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
            <text class="header-title">新建预约</text>
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :style="{ height: scrollHeight }" :show-scrollbar="false">
        <view class="hero">
          <image v-if="venue.venueImage" class="hero-image" :src="resolveImageUrl(venue.venueImage)" mode="aspectFill" />
          <view v-else class="hero-image hero-placeholder">
            <uni-icons type="image" size="42" color="#999999" />
          </view>
          <view class="hero-mask" />
          <view class="hero-copy">
            <text class="hero-kicker">Association Management</text>
            <text class="hero-title">{{ venue.venueName || '场馆预约' }}</text>
            <text class="hero-desc">{{ venue.address || '请选择场地与会员，按自由时间段创建预约。' }}</text>
          </view>
        </view>

        <view class="content">
          <view class="section-head">
            <view class="bar" />
            <text>会员选择 / MEMBER</text>
          </view>

          <view class="card">
            <view class="search-row">
              <input
                v-model.trim="memberKeyword"
                class="search-input"
                type="text"
                placeholder="输入会员姓名或手机号（纯数字按手机号搜索）"
                confirm-type="search"
                @confirm="searchMembers"
              />
              <button class="mini-btn" :disabled="memberLoading" @click="searchMembers">搜索</button>
            </view>
            <view v-if="memberLoading" class="sub-state">正在加载会员...</view>
            <view v-else-if="memberOptions.length === 0" class="sub-state">暂无可选会员</view>
            <view v-else class="option-list">
              <view
                v-for="item in memberOptions"
                :key="item.id"
                class="option-item"
                :class="{ active: form.memberId === Number(item.id || 0) }"
                @click="selectMember(item)"
              >
                <view>
                  <text class="option-title">{{ item.memberName || `会员 #${item.id}` }}</text>
                  <text class="option-subtitle">{{ item.phone || '未登记手机号' }}</text>
                </view>
                <uni-icons v-if="form.memberId === Number(item.id || 0)" type="checkmarkempty" size="20" color="#ff6600" />
              </view>
            </view>
          </view>

          <view class="section-head">
            <view class="bar" />
            <text>预约设置 / BOOKING</text>
          </view>

          <view class="card">
            <scroll-view class="date-scroll" scroll-x :show-scrollbar="false">
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
                </view>
              </view>
            </scroll-view>

            <view class="field">
              <text class="label">预约模式</text>
              <view class="mode-switch">
                <view
                  v-for="mode in availableVenueBookingModes"
                  :key="mode"
                  class="mode-item"
                  :class="{ active: bookingMode === mode }"
                  @click="setBookingMode(mode)"
                >
                  <text class="mode-title">{{ mode === 'PACKAGE' ? '包场' : '拼场' }}</text>
                  <text class="mode-copy">{{ mode === 'PACKAGE' ? '多选部分场地，仅允许空闲场地' : '单场地，可继续叠加拼场订单' }}</text>
                </view>
              </view>
            </view>

            <view v-if="bookingMode === 'SHARED'" class="field">
              <text class="label">拼场计费方式</text>
              <view class="pricing-switch">
                <view
                  v-for="mode in availableSharedPricingModes"
                  :key="mode"
                  class="pricing-item"
                  :class="{ active: pricingMode === mode }"
                  @click="setPricingMode(mode)"
                >
                  {{ mode === 'SHARED_TIME' ? '拼场按次' : '拼场按小时' }}
                </view>
              </view>
            </view>

            <view class="field">
              <text class="label">预约场地</text>
              <picker v-if="bookingMode === 'SHARED'" mode="selector" :range="courtLabels" :value="courtIndex" @change="onCourtChange">
                <view class="input picker-row">
                  <text>{{ courtLabels[courtIndex] || '请选择场地' }}</text>
                  <uni-icons type="arrowdown" size="14" color="#a1a1aa" />
                </view>
              </picker>
              <view v-else class="multi-court-list">
                <view
                  v-for="item in courts"
                  :key="item.id"
                  class="multi-court-item"
                  :class="{ active: form.courtIds.includes(item.id), disabled: isCourtDisabled(item) }"
                  @click="togglePackageCourt(item)"
                >
                  <view>
                    <text class="option-title">{{ item.courtName || `场地#${item.id}` }}</text>
                    <text class="option-subtitle">{{ getCourtHintText(item) }}</text>
                  </view>
                  <uni-icons
                    :type="form.courtIds.includes(item.id) ? 'checkbox-filled' : 'circle'"
                    size="20"
                    :color="form.courtIds.includes(item.id) ? '#ff6600' : '#c4c4c8'"
                  />
                </view>
              </view>
            </view>

            <view class="grid2">
              <view class="field">
                <text class="label">开始时间</text>
                <view class="input icon">
                  <uni-icons type="clock" size="16" color="#a33e00" />
                  <picker mode="time" :value="form.startTime" @change="onStartTimeChange">
                    <view class="pick-text">{{ form.startTime || '请选择' }}</view>
                  </picker>
                </view>
              </view>
              <view class="field">
                <text class="label">结束时间</text>
                <view class="input icon">
                  <uni-icons type="redo" size="16" color="#a33e00" />
                  <picker mode="time" :value="form.endTime" @change="onEndTimeChange">
                    <view class="pick-text">{{ form.endTime || '请选择' }}</view>
                  </picker>
                </view>
              </view>
            </view>

            <view class="field">
              <text class="label">备注</text>
              <textarea v-model.trim="form.remark" class="textarea" maxlength="200" placeholder="可选，填写后台备注或预约说明" />
            </view>
          </view>

          <view v-if="showConflictCard" class="conflict-card">
            <view class="conflict-head">
              <uni-icons type="clear" size="16" color="#b42318" />
              <text class="conflict-title">{{ bookingMode === 'PACKAGE' ? '包场场地存在冲突' : '该时段不可拼场' }}</text>
            </view>
            <text class="conflict-text">{{ conflictText }}</text>
            <view v-if="occupancyUsers.length" class="conflict-list">
              <view v-for="item in occupancyUsers" :key="`${item.bookingId}-${item.startTime}-${item.endTime}`" class="conflict-item">
                <text class="conflict-user">{{ item.memberName || '已预约会员' }}</text>
                <text class="conflict-time">{{ item.startTime }} - {{ item.endTime }}</text>
              </view>
            </view>
          </view>

          <view v-if="packageLeadTimeError" class="conflict-card">
            <view class="conflict-head">
              <uni-icons type="info" size="16" color="#b42318" />
              <text class="conflict-title">包场提交时间不足</text>
            </view>
            <text class="conflict-text">{{ packageLeadTimeError }}</text>
          </view>

          <view class="summary-card">
            <text class="summary-kicker">SUMMARY</text>
            <view class="summary-row">
              <text>预约模式</text>
              <text>{{ bookingModeText }}</text>
            </view>
            <view class="summary-row">
              <text>场地信息</text>
              <text>{{ selectedCourtSummary }}</text>
            </view>
            <view class="summary-row">
              <text>计费方式</text>
              <text>{{ pricingModeText }}</text>
            </view>
            <view class="summary-row">
              <text>预约日期</text>
              <text>{{ selectedDate || '-' }}</text>
            </view>
            <view class="summary-row">
              <text>预约时段</text>
              <text>{{ form.startTime }} - {{ form.endTime }}</text>
            </view>
            <view class="summary-row">
              <text>预计时长</text>
              <text>{{ durationLabel }}</text>
            </view>
            <view class="summary-row total">
              <text>订单金额</text>
              <text>¥{{ totalPrice }}</text>
            </view>
          </view>

          <view class="spacer" />
        </view>
      </scroll-view>

      <view class="footer">
        <view class="save-btn" :class="{ disabled: submitDisabled }" @click="handleSubmit">
          <uni-icons type="wallet" size="16" color="#ffffff" />
          <text>创建预约并去结算</text>
        </view>
        <text class="suite">创建后默认待支付，支付流程与现有移动管理端保持一致</text>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { getVenueInfo, type VenueItem } from '@/api/president/venue'
import { getCourtList, type CourtItem } from '@/api/court'
import { getMemberList, type MemberListItem } from '@/api/president/member'
import { createBooking, getBookingCount, getBookingRangeOccupancy, type BookingOccupancyUser } from '@/api/president/booking'
import { formatAmount } from '@/utils/format'

type DateOption = {
  date: string
  week: string
  day: string
  isToday: boolean
}

const statusBarHeight = ref(20)
const venueId = ref(0)
const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value}px - 96rpx)`)

const venue = ref<VenueItem>({
  id: 0,
  venueName: '',
  address: '',
  venueImage: '',
  status: 1
})

const dates = ref<DateOption[]>([])
const selectedDateIndex = ref(0)
const selectedDate = computed(() => dates.value[selectedDateIndex.value]?.date || '')

const courts = ref<CourtItem[]>([])
const courtIndex = ref(0)
const bookingMode = ref<'SHARED' | 'PACKAGE'>('SHARED')
const pricingMode = ref<'PACKAGE_HOUR' | 'SHARED_HOUR' | 'SHARED_TIME'>('SHARED_HOUR')

const memberKeyword = ref('')
const memberLoading = ref(false)
const memberOptions = ref<MemberListItem[]>([])

const form = reactive({
  memberId: 0,
  courtId: 0,
  courtIds: [] as number[],
  startTime: '18:00',
  endTime: '19:00',
  remark: ''
})

const occupancyUsers = ref<BookingOccupancyUser[]>([])
const occupancyCount = ref(0)
const occupancyLoading = ref(false)
let occupancyToken = 0

const courtLabels = computed(() => ['请选择场地', ...courts.value.map((item) => item.courtName || `场地#${item.id}`)])
const availableVenueBookingModes = computed(() => {
  const modes: Array<'SHARED' | 'PACKAGE'> = []
  if (courts.value.some(item => item.enableSharedHour || item.enableSharedTime)) modes.push('SHARED')
  if (courts.value.some(item => item.enablePackageHour)) modes.push('PACKAGE')
  return modes
})
const getAvailableSharedPricingModes = (court?: CourtItem | null) => {
  if (!court) {
    const modes: Array<'SHARED_HOUR' | 'SHARED_TIME'> = []
    if (courts.value.some(item => item.enableSharedHour)) modes.push('SHARED_HOUR')
    if (courts.value.some(item => item.enableSharedTime)) modes.push('SHARED_TIME')
    return modes
  }
  const modes: Array<'SHARED_HOUR' | 'SHARED_TIME'> = []
  if (court.enableSharedHour) modes.push('SHARED_HOUR')
  if (court.enableSharedTime) modes.push('SHARED_TIME')
  return modes
}
const availableSharedPricingModes = computed(() => getAvailableSharedPricingModes(selectedCourt.value))
const selectedCourts = computed(() => courts.value.filter((item) => form.courtIds.includes(item.id)))
const selectedCourt = computed(() => selectedCourts.value[0] || null)
const selectedCourtSummary = computed(() => {
  if (!selectedCourts.value.length) return '待选择'
  if (selectedCourts.value.length === 1) return selectedCourts.value[0].courtName || `场地#${selectedCourts.value[0].id}`
  return `${selectedCourts.value[0].courtName || `场地#${selectedCourts.value[0].id}`} 等 ${selectedCourts.value.length} 块场地`
})
const bookingModeText = computed(() => (bookingMode.value === 'PACKAGE' ? '包场' : '拼场'))
const pricingModeText = computed(() => {
  const map: Record<string, string> = {
    PACKAGE_HOUR: '包场按小时',
    SHARED_HOUR: '拼场按小时',
    SHARED_TIME: '拼场按次'
  }
  return map[bookingMode.value === 'PACKAGE' ? 'PACKAGE_HOUR' : pricingMode.value] || '按订单规则计费'
})
const isEndAfterStart = computed(() => toMinutes(form.endTime) > toMinutes(form.startTime))
const durationHours = computed(() => {
  if (!isEndAfterStart.value) return 0
  return Math.ceil((toMinutes(form.endTime) - toMinutes(form.startTime)) / 60)
})
const durationLabel = computed(() => {
  if (!isEndAfterStart.value) return '无效时间段'
  return pricingMode.value === 'SHARED_TIME' && bookingMode.value === 'SHARED' ? '按次计费' : `${durationHours.value} 小时`
})
const totalPrice = computed(() => {
  if (!selectedCourts.value.length || !isEndAfterStart.value) return '0.00'
  const amount = selectedCourts.value.reduce((sum, court) => {
    const packagePrice = Number(court.packagePricePerHour || court.pricePerHour || 0)
    const sharedHourPrice = Number(court.sharedPricePerHour || court.pricePerHour || 0)
    const sharedTimePrice = Number(court.sharedPricePerTime || court.pricePerTime || court.pricePerHour || 0)
    if (bookingMode.value === 'PACKAGE') return sum + packagePrice * durationHours.value
    if (pricingMode.value === 'SHARED_TIME') return sum + sharedTimePrice
    return sum + sharedHourPrice * durationHours.value
  }, 0)
  return formatAmount(amount)
})
const showConflictCard = computed(() => Boolean(form.courtIds.length && selectedDate.value && occupancyCount.value > 0))
const packageLeadTimeError = computed(() => {
  if (bookingMode.value !== 'PACKAGE' || !selectedDate.value || !form.startTime) return ''
  const bookingStart = new Date(`${selectedDate.value}T${form.startTime}:00`)
  return bookingStart.getTime() - Date.now() < 2 * 60 * 60 * 1000 ? '包场需至少提前 2 小时提交申请。' : ''
})
const conflictText = computed(() => {
  if (bookingMode.value === 'PACKAGE') {
    return `所选场地中有 ${occupancyCount.value} 块在当前时段已有预约，不能创建包场。`
  }
  return '当前时段该场地已被包场，请调整时间或更换场地。'
})
const submitDisabled = computed(() => {
  if (!form.memberId || !form.courtIds.length || !selectedDate.value || !isEndAfterStart.value || occupancyLoading.value) return true
  if (bookingMode.value === 'PACKAGE') {
    return !!packageLeadTimeError.value || form.courtIds.length >= courts.value.length || occupancyCount.value > 0
  }
  return occupancyCount.value > 0
})

onLoad((query?: Record<string, string | undefined>) => {
  statusBarHeight.value = uni.getSystemInfoSync().statusBarHeight || 20
  if (query?.venueId) {
    venueId.value = Number(query.venueId)
  }
  generateDateList()
  void initialize()
})

watch(
  () => [form.courtId, JSON.stringify(form.courtIds), selectedDate.value, form.startTime, form.endTime, bookingMode.value, pricingMode.value],
  () => {
    void checkOccupancy()
  }
)

watch(courts, () => {
  syncModeAndPricing()
})

async function initialize() {
  await Promise.all([loadVenue(), loadCourts(), searchMembers()])
}

function generateDateList() {
  const today = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  dates.value = Array.from({ length: 7 }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() + index)
    const yyyy = date.getFullYear()
    const mm = String(date.getMonth() + 1).padStart(2, '0')
    const dd = String(date.getDate()).padStart(2, '0')
    return {
      date: `${yyyy}-${mm}-${dd}`,
      week: weekDays[date.getDay()],
      day: dd,
      isToday: index === 0
    }
  })
}

async function loadVenue() {
  if (!venueId.value) return
  try {
    venue.value = await getVenueInfo(venueId.value)
  } catch (error) {
    console.error('Failed to load venue info:', error)
  }
}

async function loadCourts() {
  if (!venueId.value) return
  try {
    const res = await getCourtList({ venueId: venueId.value, page: 1, size: 100 })
    courts.value = Array.isArray(res?.data) ? res.data : []
    syncModeAndPricing()
    if (courts.value.length > 0) {
      const firstAvailable = courts.value.find((item) => !isCourtDisabled(item))
      if (firstAvailable) {
        form.courtId = firstAvailable.id
        form.courtIds = [firstAvailable.id]
        courtIndex.value = courts.value.findIndex((item) => item.id === firstAvailable.id) + 1
      }
    }
  } catch (error) {
    console.error('Failed to load courts:', error)
  }
}

async function searchMembers() {
  memberLoading.value = true
  try {
    const keyword = memberKeyword.value.trim()
    const isPhoneKeyword = /^\d+$/.test(keyword)
    const res = await getMemberList({
      memberName: keyword && !isPhoneKeyword ? keyword : undefined,
      phone: keyword && isPhoneKeyword ? keyword : undefined,
      page: 1,
      size: 10
    })
    memberOptions.value = Array.isArray(res?.data) ? res.data : []
  } catch (error) {
    console.error('Failed to search members:', error)
    memberOptions.value = []
  } finally {
    memberLoading.value = false
  }
}

function selectMember(item: MemberListItem) {
  form.memberId = Number(item.id || 0)
}

function handleDateChange(index: number) {
  selectedDateIndex.value = index
}

function setBookingMode(mode: 'SHARED' | 'PACKAGE') {
  bookingMode.value = mode
  pricingMode.value = mode === 'PACKAGE' ? 'PACKAGE_HOUR' : (availableSharedPricingModes.value[0] || 'SHARED_HOUR')
  if (mode === 'SHARED') {
    const firstId = form.courtIds[0] || form.courtId || 0
    form.courtIds = firstId ? [firstId] : []
    form.courtId = firstId
    courtIndex.value = firstId ? courts.value.findIndex((item) => item.id === firstId) + 1 : 0
  } else if (form.courtId) {
    form.courtIds = [form.courtId]
  }
}

function setPricingMode(mode: 'SHARED_HOUR' | 'SHARED_TIME') {
  pricingMode.value = mode
}

function syncModeAndPricing() {
  const modes = availableVenueBookingModes.value
  if (!modes.length) {
    bookingMode.value = 'SHARED'
    pricingMode.value = 'SHARED_HOUR'
    form.courtId = 0
    form.courtIds = []
    return
  }
  if (!modes.includes(bookingMode.value)) {
    bookingMode.value = modes[0]
  }
  if (bookingMode.value === 'PACKAGE') {
    pricingMode.value = 'PACKAGE_HOUR'
    form.courtIds = form.courtIds.filter(id => {
      const court = courts.value.find(item => item.id === id)
      return court && court.enablePackageHour && Number(court.status) === 1
    })
    form.courtId = form.courtIds[0] || 0
    return
  }
  const selected = selectedCourt.value || selectedCourts.value[0]
  const sharedModes = getAvailableSharedPricingModes(selected)
  if (sharedModes.length && !sharedModes.includes(pricingMode.value as 'SHARED_HOUR' | 'SHARED_TIME')) {
    pricingMode.value = sharedModes[0]
  }
}

function onCourtChange(event: { detail: { value: string } }) {
  courtIndex.value = Number(event.detail.value)
  if (courtIndex.value <= 0) {
    form.courtId = 0
    form.courtIds = []
    return
  }
  form.courtId = Number(courts.value[courtIndex.value - 1]?.id || 0)
  form.courtIds = form.courtId ? [form.courtId] : []
  const targetCourt = courts.value[courtIndex.value - 1]
  const modes = getAvailableSharedPricingModes(targetCourt)
  if (modes.length && !modes.includes(pricingMode.value as 'SHARED_HOUR' | 'SHARED_TIME')) {
    pricingMode.value = modes[0]
  }
}

function isCourtDisabled(item: CourtItem) {
  if (Number(item.status) !== 1) return true
  if (bookingMode.value === 'PACKAGE') return item.enablePackageHour !== true
  return !getAvailableSharedPricingModes(item).includes(pricingMode.value as 'SHARED_HOUR' | 'SHARED_TIME')
}

function getCourtHintText(item: CourtItem) {
  if (Number(item.status) !== 1) return '维护中，当前不可选'
  if (bookingMode.value === 'PACKAGE' && item.enablePackageHour !== true) return '该场地未开放包场'
  if (bookingMode.value === 'SHARED' && !getAvailableSharedPricingModes(item).includes(pricingMode.value as 'SHARED_HOUR' | 'SHARED_TIME')) {
    return getCourtMarketingSummary(item)
  }
  const packagePrice = Number(item.packagePricePerHour || item.pricePerHour || 0)
  const sharedHourPrice = Number(item.sharedPricePerHour || item.pricePerHour || 0)
  const sharedTimePrice = Number(item.sharedPricePerTime || item.pricePerTime || item.pricePerHour || 0)
  if (bookingMode.value === 'PACKAGE') return `包场 ¥${formatAmount(packagePrice)}/小时`
  return pricingMode.value === 'SHARED_TIME' ? `拼场 ¥${formatAmount(sharedTimePrice)}/次` : `拼场 ¥${formatAmount(sharedHourPrice)}/小时`
}

function getCourtMarketingSummary(item: CourtItem) {
  const modes: string[] = []
  if (item.enablePackageHour) modes.push('包场')
  if (item.enableSharedHour) modes.push('拼场小时')
  if (item.enableSharedTime) modes.push('拼场按次')
  return modes.length ? `已开放：${modes.join(' / ')}` : '当前未开放预约'
}

function togglePackageCourt(item: CourtItem) {
  if (bookingMode.value !== 'PACKAGE' || isCourtDisabled(item)) return
  if (form.courtIds.includes(item.id)) {
    form.courtIds = form.courtIds.filter((id) => id !== item.id)
    form.courtId = form.courtIds[0] || 0
    return
  }
  if (form.courtIds.length >= Math.max(0, courts.value.length - 1)) {
    uni.showToast({ title: '包场不能包下整个场馆全部场地', icon: 'none' })
    return
  }
  form.courtIds = [...form.courtIds, item.id]
  form.courtId = form.courtIds[0] || item.id
}

function onStartTimeChange(event: { detail: { value: string } }) {
  form.startTime = event.detail.value
}

function onEndTimeChange(event: { detail: { value: string } }) {
  form.endTime = event.detail.value
}

function toMinutes(value: string) {
  const [hour, minute] = value.split(':').map((item) => Number(item))
  return hour * 60 + minute
}

function toApiTime(value: string) {
  return value.length === 5 ? `${value}:00` : value
}

async function checkOccupancy() {
  occupancyUsers.value = []
  occupancyCount.value = 0

  const courtIds = bookingMode.value === 'PACKAGE' ? form.courtIds : form.courtId ? [form.courtId] : []
  if (!courtIds.length || !selectedDate.value || !form.startTime || !form.endTime || !isEndAfterStart.value) {
    return
  }

  occupancyLoading.value = true
  const currentToken = occupancyToken + 1
  occupancyToken = currentToken

  try {
    if (bookingMode.value === 'PACKAGE') {
      const results = await Promise.all(
        courtIds.map((courtId) =>
          getBookingRangeOccupancy({
            courtId,
            bookingDate: selectedDate.value,
            startTime: toApiTime(form.startTime),
            endTime: toApiTime(form.endTime)
          })
        )
      )
      if (currentToken !== occupancyToken) return
      occupancyCount.value = results.filter((res) => Number(res.count || 0) > 0).length
      occupancyUsers.value = results.flatMap((res) => (Array.isArray(res.users) ? res.users : []))
    } else {
      const [countRes, rangeRes] = await Promise.all([
        getBookingCount({
          courtId: form.courtId,
          bookingDate: selectedDate.value,
          startTime: toApiTime(form.startTime),
          endTime: toApiTime(form.endTime)
        }),
        getBookingRangeOccupancy({
          courtId: form.courtId,
          bookingDate: selectedDate.value,
          startTime: toApiTime(form.startTime),
          endTime: toApiTime(form.endTime)
        })
      ])
      if (currentToken !== occupancyToken) return
      const sharedUsers = Array.isArray(rangeRes.users) ? rangeRes.users : []
      const totalCount = Number(countRes.count || 0)
      occupancyCount.value = Math.max(0, Number(rangeRes.count || 0) - sharedUsers.length) > 0 ? 1 : 0
      occupancyUsers.value = totalCount > 0 ? sharedUsers : []
    }
  } catch (error) {
    console.error('Failed to check occupancy:', error)
  } finally {
    if (currentToken === occupancyToken) {
      occupancyLoading.value = false
    }
  }
}

function handleBack() {
  safeNavigateBack(venueId.value ? `${PRESIDENT_PAGES.VENUE_DETAIL}?id=${venueId.value}` : PRESIDENT_PAGES.VENUE_LIST)
}

async function handleSubmit() {
  if (!form.memberId) {
    uni.showToast({ title: '请选择会员', icon: 'none' })
    return
  }
  if (!form.courtIds.length) {
    uni.showToast({ title: bookingMode.value === 'PACKAGE' ? '请至少选择一块场地' : '请选择场地', icon: 'none' })
    return
  }
  if (!isEndAfterStart.value) {
    uni.showToast({ title: '结束时间必须晚于开始时间', icon: 'none' })
    return
  }
  if (bookingMode.value === 'PACKAGE' && packageLeadTimeError.value) {
    uni.showToast({ title: packageLeadTimeError.value, icon: 'none' })
    return
  }
  if (!availableVenueBookingModes.value.includes(bookingMode.value)) {
    uni.showToast({ title: '当前场馆未开放所选预约模式', icon: 'none' })
    return
  }
  if (bookingMode.value === 'PACKAGE' && form.courtIds.length >= courts.value.length) {
    uni.showToast({ title: '包场不能包下整个场馆全部场地', icon: 'none' })
    return
  }
  if (bookingMode.value === 'PACKAGE' && selectedCourts.value.some(item => item.enablePackageHour !== true)) {
    uni.showToast({ title: '所选场地包含未开放包场的场地', icon: 'none' })
    return
  }
  if (bookingMode.value === 'SHARED' && selectedCourt.value && !getAvailableSharedPricingModes(selectedCourt.value).includes(pricingMode.value as 'SHARED_HOUR' | 'SHARED_TIME')) {
    uni.showToast({ title: '该场地未开放当前拼场计费方式', icon: 'none' })
    return
  }
  if (occupancyCount.value > 0) {
    uni.showToast({ title: bookingMode.value === 'PACKAGE' ? '所选场地存在冲突，请调整后再创建' : '该时段已有包场，不能继续拼场', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '准备订单...' })
    const res = await createBooking({
      memberId: form.memberId,
      courtId: form.courtId,
      courtIds: form.courtIds,
      bookingMode: bookingMode.value,
      pricingMode: bookingMode.value === 'PACKAGE' ? 'PACKAGE_HOUR' : pricingMode.value,
      bookingDate: selectedDate.value,
      startTime: toApiTime(form.startTime),
      endTime: toApiTime(form.endTime),
      orderAmount: Number(totalPrice.value),
      paymentMethod: 'BALANCE',
      remark: form.remark || undefined
    })
    uni.hideLoading()

    const bookingSummary = {
      venueName: venue.value.venueName || '未知场馆',
      courtName: selectedCourt.value?.courtName || '未知场地',
      courtNames: selectedCourts.value.map((item) => item.courtName || `场地#${item.id}`),
      courtCount: selectedCourts.value.length,
      bookingMode: bookingMode.value,
      pricingModeSummary: pricingModeText.value,
      date: selectedDate.value,
      slot: `${form.startTime} - ${form.endTime}`,
      duration: durationHours.value,
      totalAmount: Number(totalPrice.value),
      vipDiscount: 0,
      payableAmount: Number(totalPrice.value),
      bookingId: res?.id
    }

    uni.navigateTo({
      url: `/pages/booking/confirm?bookingId=${encodeURIComponent(String(res?.id || bookingSummary.bookingId || ''))}&data=${encodeURIComponent(JSON.stringify(bookingSummary))}&returnUrl=${encodeURIComponent(`${PRESIDENT_PAGES.VENUE_DETAIL}?id=${venueId.value}`)}`
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
  background: #f9f9f9;
  color: #1a1c1c;
}

.header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: #f9f9f9;
}

.header-inner {
  height: 96rpx;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-title {
  color: #ff6600;
  font-size: 34rpx;
  font-weight: 700;
}

.hero {
  position: relative;
  height: 360rpx;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-placeholder {
  background: #ececec;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.12) 0%, rgba(0, 0, 0, 0.72) 100%);
}

.hero-copy {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.hero-kicker {
  color: rgba(255, 255, 255, 0.84);
  font-size: 20rpx;
  letter-spacing: 0.2em;
  font-weight: 700;
}

.hero-title {
  color: #ffffff;
  font-size: 46rpx;
  font-weight: 800;
}

.hero-desc {
  color: rgba(255, 255, 255, 0.86);
  font-size: 24rpx;
}

.content {
  padding: 28rpx 24rpx 0;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 22rpx 0 18rpx;
  font-size: 24rpx;
  font-weight: 800;
  color: #5f5e5e;
}

.bar {
  width: 8rpx;
  height: 28rpx;
  border-radius: 999rpx;
  background: #ff6600;
}

.card,
.conflict-card,
.summary-card {
  background: #ffffff;
  border-radius: 28rpx;
  padding: 24rpx;
  box-shadow: 0 12rpx 28rpx rgba(0, 0, 0, 0.05);
}

.search-row {
  display: flex;
  gap: 14rpx;
}

.search-input,
.input,
.textarea {
  width: 100%;
  box-sizing: border-box;
  border-radius: 22rpx;
  background: #f6f7f8;
}

.search-input {
  height: 84rpx;
  padding: 0 22rpx;
}

.mini-btn {
  min-width: 132rpx;
  height: 84rpx;
  border: none;
  border-radius: 22rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 700;
}

.sub-state {
  margin-top: 18rpx;
  color: #7a7a7a;
  font-size: 22rpx;
}

.option-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.option-item {
  padding: 20rpx;
  border-radius: 22rpx;
  background: #f6f7f8;
  display: flex;
  align-items: center;
  justify-content: space-between;

  &.active {
    background: #fff3eb;
    border: 2rpx solid #ff6600;
  }
}

.option-title {
  display: block;
  color: #1a1c1c;
  font-size: 28rpx;
  font-weight: 700;
}

.option-subtitle {
  display: block;
  margin-top: 6rpx;
  color: #7a7a7a;
  font-size: 22rpx;
}

.date-scroll {
  white-space: nowrap;
  margin-bottom: 18rpx;
}

.date-list {
  display: inline-flex;
  gap: 14rpx;
}

.date-item {
  min-width: 116rpx;
  padding: 18rpx;
  border-radius: 24rpx;
  background: #f6f7f8;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;

  &.active {
    background: #fff3eb;
    border: 2rpx solid #ff6600;
  }
}

.date-week {
  color: #7a7a7a;
  font-size: 20rpx;
}

.date-day {
  color: #1a1c1c;
  font-size: 34rpx;
  font-weight: 800;
}

.field {
  margin-top: 18rpx;
}

.mode-switch,
.pricing-switch {
  display: grid;
  gap: 14rpx;
}

.mode-switch {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.mode-item,
.pricing-item {
  border-radius: 22rpx;
  border: 2rpx solid transparent;
  background: #f6f7f8;
}

.mode-item {
  padding: 18rpx;
}

.mode-item.active,
.pricing-item.active {
  background: #fff3eb;
  border-color: #ff6600;
}

.mode-title {
  display: block;
  color: #1a1c1c;
  font-size: 26rpx;
  font-weight: 800;
}

.mode-copy {
  display: block;
  margin-top: 8rpx;
  color: #7a7a7a;
  font-size: 20rpx;
  line-height: 1.55;
}

.pricing-switch {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.pricing-item {
  min-height: 84rpx;
  padding: 0 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #1a1c1c;
  font-size: 22rpx;
  font-weight: 700;
}

.label {
  display: block;
  margin-bottom: 10rpx;
  color: #7a7a7a;
  font-size: 22rpx;
}

.picker-row,
.icon {
  min-height: 84rpx;
  padding: 0 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.pick-text {
  color: #1a1c1c;
  font-size: 28rpx;
  font-weight: 700;
}

.grid2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.textarea {
  min-height: 160rpx;
  padding: 20rpx;
}

.multi-court-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.multi-court-item {
  padding: 18rpx 20rpx;
  border-radius: 22rpx;
  background: #f6f7f8;
  border: 2rpx solid transparent;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;

  &.active {
    background: #fff3eb;
    border-color: #ff6600;
  }

  &.disabled {
    opacity: 0.52;
  }
}

.conflict-card {
  margin-top: 20rpx;
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
  padding: 16rpx 18rpx;
  background: rgba(255, 255, 255, 0.75);
  border-radius: 18rpx;
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.conflict-user,
.conflict-time {
  color: #7a271a;
  font-size: 22rpx;
}

.summary-card {
  margin-top: 20rpx;
}

.summary-kicker {
  display: block;
  color: #a33e00;
  font-size: 20rpx;
  letter-spacing: 0.18em;
  font-weight: 800;
  margin-bottom: 16rpx;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #5f5e5e;
  font-size: 24rpx;

  &:not(:first-of-type) {
    margin-top: 12rpx;
  }

  &.total {
    margin-top: 18rpx;
    padding-top: 18rpx;
    border-top: 1rpx dashed rgba(0, 0, 0, 0.12);
    color: #1a1c1c;
    font-weight: 800;
  }
}

.spacer {
  height: 180rpx;
}

.footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 24rpx calc(env(safe-area-inset-bottom) + 20rpx);
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(14rpx);
  border-top: 1rpx solid rgba(0, 0, 0, 0.06);
}

.save-btn {
  height: 92rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ff6600 0%, #ff8b2c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 800;

  &.disabled {
    opacity: 0.45;
  }
}

.suite {
  display: block;
  margin-top: 12rpx;
  text-align: center;
  color: #7a7a7a;
  font-size: 20rpx;
}
</style>
