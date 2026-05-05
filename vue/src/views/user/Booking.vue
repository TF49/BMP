<template>
  <div class="user-booking">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">场地预订</h1>
      <p class="page-subtitle">选择您心仪的场地，轻松完成预订</p>
    </div>

    <!-- Tab切换：预订场地 / 我的预约 -->
    <el-tabs v-model="activeTab" class="booking-tabs">
      <el-tab-pane label="预订场地" name="book">
        <!-- 步骤指示器 -->
        <div class="step-indicator" v-if="step > 0">
          <div class="step-indicator-container">
            <div 
              v-for="(stepItem, index) in steps"
              :key="index"
              class="step-item"
              :class="{ 
                active: step > index + 1,
                current: step === index + 1,
                completed: step > index + 1
              }"
            >
              <div class="step-number">
                <el-icon v-if="step > index + 1"><CircleCheck /></el-icon>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div class="step-label">{{ stepItem }}</div>
              <div class="step-line" v-if="index < steps.length - 1"></div>
            </div>
          </div>
        </div>
        
        <!-- 步骤1: 选择场馆 -->
        <div v-if="step === 1" class="booking-step">
          <h3 class="step-title">选择场馆</h3>
          <div class="venue-grid">
            <div
              v-for="venue in venues"
              :key="venue.id"
              class="venue-card"
              :class="{ selected: selectedVenue?.id === venue.id }"
              @click="selectVenue(venue)"
            >
              <div class="venue-image">
                <el-icon :size="48"><OfficeBuilding /></el-icon>
              </div>
              <div class="venue-info">
                <h4 class="venue-name">{{ venue.venueName }}</h4>
                <p class="venue-address">{{ venue.address || '暂无地址' }}</p>
                <div class="venue-tags">
                  <el-tag size="small" :type="venue.status === 1 ? 'success' : 'info'">
                    {{ venue.status === 1 ? '营业中' : '暂停' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
          <div v-if="venues.length === 0" class="empty-state">
            <el-empty description="暂无可用场馆" :image-size="120" />
          </div>
        </div>

        <!-- 步骤2: 选择场地（按日期查看预约情况） -->
        <div v-if="step === 2" class="booking-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 1" text>返回选择场馆</el-button>
            <h3 class="step-title">{{ selectedVenue?.venueName }} - 选择场地</h3>
          </div>
          <div class="court-date-bar booking-mode-bar">
            <span class="court-date-label">预约模式</span>
            <el-radio-group v-model="bookingMode" @change="handleBookingModeChange">
              <el-radio-button label="SHARED">拼场</el-radio-button>
              <el-radio-button label="PACKAGE">包场</el-radio-button>
            </el-radio-group>
            <el-radio-group v-if="bookingMode === 'SHARED'" v-model="pricingMode">
              <el-radio-button label="SHARED_HOUR">按小时</el-radio-button>
              <el-radio-button label="SHARED_TIME">按次</el-radio-button>
            </el-radio-group>
            <span v-else class="court-date-hint">包场只能选择部分空闲场地，且需提前 2 小时提交</span>
          </div>
          <div class="court-date-bar">
            <span class="court-date-label">查看日期</span>
            <el-date-picker
              v-model="bookingDateForCourts"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              :disabled-date="disabledDate"
              class="court-date-picker"
              @change="loadTodayBookingCounts"
            />
            <span class="court-date-hint">场地状态按所选日期统计（维护中始终不可选）</span>
          </div>
          <div class="court-grid">
            <div
              v-for="court in courts"
              :key="court.id"
              class="court-card"
              :class="{ 
                selected: selectedCourtIds.includes(court.id),
                unavailable: isCourtUnavailable(court)
              }"
              @click="selectCourt(court)"
            >
              <div class="court-header">
                <h4 class="court-name">{{ court.courtName || court.courtCode }}</h4>
                <el-tag 
                  size="small" 
                  :type="getCourtDisplayStatus(court).type"
                >
                  {{ getCourtDisplayStatus(court).text }}
                </el-tag>
              </div>
              <div class="court-info">
                <div class="court-price">
                  <span class="price-label">价格</span>
                  <span class="price-value">
                    {{ getCourtPriceText(court) }}
                  </span>
                </div>
                <div class="court-type">
                  <span>{{ getCourtHintText(court) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-if="courts.length === 0" class="empty-state">
            <el-empty description="该场馆暂无可用场地" :image-size="120" />
          </div>
        </div>

        <!-- 步骤3: 选择时间 -->
        <div v-if="step === 3" class="booking-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 2" text>返回选择场地</el-button>
            <h3 class="step-title">选择预约时间</h3>
          </div>
          <div class="time-selection">
            <!-- 预约摘要卡片：三列等分，带图标，充分利用横向空间 -->
            <div class="selected-info-card">
              <div class="info-item">
                <div class="info-icon-wrap info-icon-venue">
                  <el-icon :size="22"><CircleCheck /></el-icon>
                </div>
                <span class="info-label">当前会员</span>
                <span class="info-value">{{ currentMember?.memberName || '未获取到会员信息' }}</span>
              </div>
              <div class="info-item">
                <div class="info-icon-wrap info-icon-venue">
                  <el-icon :size="22"><OfficeBuilding /></el-icon>
                </div>
                <span class="info-label">场馆</span>
                <span class="info-value">{{ selectedVenue?.venueName }}</span>
              </div>
              <div class="info-item">
                <div class="info-icon-wrap info-icon-court">
                  <el-icon :size="22"><Location /></el-icon>
                </div>
                <span class="info-label">场地</span>
                <span class="info-value">{{ selectedCourtSummary }}</span>
              </div>
              <div class="info-item">
                <div class="info-icon-wrap info-icon-price">
                  <el-icon :size="22"><Coin /></el-icon>
                </div>
                <span class="info-label">计费</span>
                <span class="info-value price">
                  {{ bookingModeText }} / {{ pricingModeText }}
                </span>
              </div>
            </div>

            <!-- 当前使用情况 -->
            <div class="current-occupancy-card" v-if="bookingMode === 'SHARED'">
              <div class="current-occupancy-header">
                <el-icon class="occupancy-icon" :size="18"><InfoFilled /></el-icon>
                <span class="current-occupancy-title">当前使用情况</span>
              </div>
              <div class="current-occupancy-body">
                <span v-if="!currentOccupancy.count && currentOccupancy.count !== 0">
                  正在获取当前使用情况...
                </span>
                <span v-else-if="currentOccupancy.count === 0">
                  当前时刻暂无人在使用该场地。
                </span>
                <div v-else>
                  <div class="current-occupancy-summary">
                    当前时刻有
                    <span class="occupancy-count">{{ currentOccupancy.count }}</span>
                    人正在使用该场地：
                  </div>
                  <div class="current-occupancy-tags">
                    <el-tag
                      v-for="user in currentOccupancy.users"
                      :key="user.bookingId"
                      size="small"
                      type="info"
                      class="occupancy-tag"
                    >
                      {{ user.memberName }}（{{ user.startTime }}-{{ user.endTime }}）
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>

            <div class="time-form-card">
              <h4 class="time-form-card-title">预约信息</h4>
              <el-form :model="timeForm" label-width="100px" class="time-form" @submit.prevent>
                <el-form-item label="预约日期" class="time-form-item-date">
                  <el-date-picker
                    v-model="timeForm.date"
                    type="date"
                    placeholder="选择日期"
                    :disabled-date="disabledDate"
                    value-format="YYYY-MM-DD"
                    class="time-form-picker"
                  />
                </el-form-item>
                <div class="time-form-row">
                  <el-form-item label="开始时间" class="time-form-item-half">
                    <el-time-picker
                      v-model="timeForm.startTime"
                      format="HH:mm"
                      value-format="HH:mm"
                      placeholder="选择开始时间"
                      class="time-form-picker"
                    />
                  </el-form-item>
                  <el-form-item label="结束时间" class="time-form-item-half">
                    <el-time-picker
                      v-model="timeForm.endTime"
                      format="HH:mm"
                      value-format="HH:mm"
                      placeholder="选择结束时间"
                      class="time-form-picker"
                    />
                  </el-form-item>
                </div>
                <el-form-item label="预计费用" class="time-form-item-cost">
                  <div class="estimated-cost-block">
                    <span class="cost-value">¥{{ estimatedCost }}</span>
                    <span class="cost-desc">{{ bookingMode === 'PACKAGE' ? '按包场场地小时价汇总' : '根据拼场计费规则自动计算' }}</span>
                  </div>
                </el-form-item>
                <el-form-item v-if="bookingMode === 'SHARED'" label="时段预约情况" class="time-form-item-occupancy">
                  <div class="booking-occupancy">
                  <span v-if="rangeOccupancy.count === null">
                    请选择完整的日期和时间，以查看当前已有多少人预约该场地。
                  </span>
                  <span v-else-if="rangeOccupancy.count === 0">
                    该时段暂无其他预约，您可以独享或和好友一起使用场地。
                  </span>
                  <div v-else>
                    <div>
                      该时段已有
                      <span class="occupancy-count">{{ rangeOccupancy.count }}</span>
                      笔预约，将与其他用户共享场地：
                    </div>
                    <div class="range-occupancy-tags">
                      <el-tag
                        v-for="user in rangeOccupancy.users"
                        :key="user.bookingId"
                        size="small"
                        type="info"
                        class="occupancy-tag"
                      >
                        {{ user.memberName }}（{{ user.startTime }}-{{ user.endTime }}）
                      </el-tag>
                    </div>
                  </div>
                </div>
              </el-form-item>
                <el-form-item v-if="bookingMode === 'PACKAGE' && packageLeadTimeError" label="包场提示" class="time-form-item-occupancy">
                  <div class="booking-occupancy">{{ packageLeadTimeError }}</div>
                </el-form-item>
                <el-form-item label="补充说明" class="time-form-item-occupancy">
                  <el-input
                    v-model="timeForm.remark"
                    type="textarea"
                    :rows="3"
                    maxlength="500"
                    show-word-limit
                    placeholder="可选，填写给会长的补充说明"
                  />
                </el-form-item>
              </el-form>
              <div class="step-actions">
                <el-button @click="step = 2" size="large">返回</el-button>
                <el-button type="primary" @click="goToConfirmStep" size="large">
                  下一步
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤4: 确认预订 -->
        <div v-if="step === 4" class="booking-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 3" text>返回修改时间</el-button>
            <h3 class="step-title">确认预订信息</h3>
          </div>
          <div class="confirm-card">
            <div class="confirm-section">
              <h4 class="section-title">预订详情</h4>
              <div class="confirm-info">
                <div class="confirm-item">
                  <span class="confirm-label">当前会员</span>
                  <span class="confirm-value">{{ currentMember?.memberName || '-' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">场馆</span>
                  <span class="confirm-value">{{ selectedVenue?.venueName }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">预约模式</span>
                  <span class="confirm-value">{{ bookingModeText }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">场地</span>
                  <span class="confirm-value">{{ selectedCourtSummary }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">计费方式</span>
                  <span class="confirm-value">{{ pricingModeText }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">预约时间</span>
                  <span class="confirm-value">{{ formatBookingTime() }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">费用</span>
                  <span class="confirm-value price">¥{{ estimatedCost }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">补充说明</span>
                  <span class="confirm-value">{{ timeForm.remark || '无' }}</span>
                </div>
              </div>
            </div>
            <div class="confirm-actions">
              <el-button @click="step = 3" size="large">返回修改</el-button>
              <el-button type="primary" :loading="submitting" @click="submitBooking" size="large">
                确认预订
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的预约" name="my-bookings">
        <div class="my-bookings-section">
          <!-- 简单筛选 -->
          <div class="filter-bar">
            <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px" @change="loadMyBookings">
              <el-option label="待支付" value="1" />
              <el-option label="已支付" value="2" />
              <el-option label="进行中" value="3" />
              <el-option label="已完成" value="4" />
              <el-option label="已取消" value="0" />
            </el-select>
          </div>

          <!-- 预约列表 -->
          <div v-if="myBookings.length > 0" class="bookings-list">
            <div
              v-for="booking in myBookings"
              :key="booking.id"
              class="booking-item-card"
            >
              <div class="booking-main">
                <div class="booking-header">
                  <span class="booking-no">{{ booking.bookingNo }}</span>
                  <el-tag :type="getStatusType(booking.status)" size="small">
                    {{ getStatusText(booking.status) }}
                  </el-tag>
                </div>
                <div class="booking-details">
                  <p class="booking-venue">{{ booking.venueName }} · {{ getBookingCourtSummary(booking) }}</p>
                  <p class="booking-time">{{ getBookingModeText(booking.bookingMode) }} · {{ getPricingModeText(booking.pricingMode, booking.pricingModeSummary) }}</p>
                  <p class="booking-time">{{ formatTimeRange(booking.startTime, booking.endTime) }}</p>
                  <!-- 后端金额字段为 orderAmount，这里使用它来展示实际订单金额 -->
                  <p class="booking-amount">¥{{ formatCurrency(booking.orderAmount) }}</p>
                </div>
              </div>
              <div class="booking-actions">
                <el-button
                  v-if="booking.status === 1"
                  type="primary"
                  size="small"
                  @click="handlePay(booking)"
                >
                  立即支付
                </el-button>
                <el-button
                  v-if="booking.status === 2 || booking.status === 3"
                  type="warning"
                  size="small"
                  @click="handleCancel(booking)"
                >
                  取消预约
                </el-button>
                <el-button
                  v-if="booking.status === 4"
                  type="info"
                  size="small"
                  @click="handleViewDetail(booking)"
                >
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无预约记录" :image-size="120" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, OfficeBuilding, Location, Coin, InfoFilled } from '@element-plus/icons-vue'
import { CircleCheck } from '@element-plus/icons-vue'
import { getVenueList } from '@/api/venue'
import { getCourtList, getTodayBookingCounts } from '@/api/court'
import {
  getBookingList,
  addBooking,
  payMemberBooking,
  updateBookingStatus,
  getBookingCount,
  getBookingCurrentOccupancy,
  getBookingRangeOccupancy
} from '@/api/booking'
import { useAuth } from '@/composables/useAuth'
import { getCurrentMember } from '@/api/member'

const router = useRouter()
const { userRole } = useAuth()

const activeTab = ref('book')
const step = ref(1) // 1-选择场馆, 2-选择场地, 3-选择时间, 4-确认

// 步骤配置
const steps = ['选择场馆', '选择场地', '选择时间', '确认预订']

// 数据
const venues = ref([])
const courts = ref([])
const selectedVenue = ref(null)
const selectedCourt = ref(null)
const selectedCourtIds = ref([])
const bookingMode = ref('SHARED')
const pricingMode = ref('SHARED_HOUR')
const myBookings = ref([])
const filterStatus = ref(null)
const currentMember = ref(null)

// 选择场地步骤使用的日期（与管理员端一致：按日期显示场地状态）
const getLocalTodayDateStr = () => {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
const bookingDateForCourts = ref(getLocalTodayDateStr())
// 所选日期下各场地的预约数量 courtId -> count（用于展示 空闲/预约中）
const todayBookingCounts = ref({})

// 时间表单
const timeForm = ref({
  date: '',
  startTime: '',
  endTime: '',
  remark: ''
})

// 当前时刻占用情况
const currentOccupancy = ref({
  count: null,
  users: []
})

// 所选时段占用情况
const rangeOccupancy = ref({
  count: null,
  users: []
})

const submitting = ref(false)

const selectedCourts = computed(() => courts.value.filter(court => selectedCourtIds.value.includes(court.id)))
const selectedCourtSummary = computed(() => {
  if (!selectedCourts.value.length) return '-'
  if (selectedCourts.value.length === 1) {
    return selectedCourts.value[0].courtName || selectedCourts.value[0].courtCode
  }
  const first = selectedCourts.value[0]
  return `${first.courtName || first.courtCode} 等 ${selectedCourts.value.length} 块场地`
})
const bookingModeText = computed(() => (bookingMode.value === 'PACKAGE' ? '包场' : '拼场'))
const pricingModeText = computed(() => getPricingModeText(bookingMode.value === 'PACKAGE' ? 'PACKAGE_HOUR' : pricingMode.value))
const packageLeadTimeError = computed(() => {
  if (bookingMode.value !== 'PACKAGE' || !timeForm.value.date || !timeForm.value.startTime) return ''
  const bookingStart = new Date(`${timeForm.value.date}T${timeForm.value.startTime}:00`)
  return bookingStart.getTime() - Date.now() < 2 * 60 * 60 * 1000 ? '包场需至少提前 2 小时提交申请。' : ''
})

// 计算预计费用（与后端 BookingServiceImpl.calculateOrderAmount 逻辑保持一致）
const estimatedCost = computed(() => {
  if (!selectedCourts.value.length) {
    return '0.00'
  }

  // 按小时计费：根据时长计算
  if (!timeForm.value.startTime || !timeForm.value.endTime) {
    return '0.00'
  }

  const start = timeForm.value.startTime.split(':')
  const end = timeForm.value.endTime.split(':')
  const startHour = parseInt(start[0]) + parseInt(start[1]) / 60
  const endHour = parseInt(end[0]) + parseInt(end[1]) / 60
  const hours = Math.max(0, endHour - startHour)

  const cost = selectedCourts.value.reduce((sum, court) => {
    const packagePrice = Number(court.packagePricePerHour || court.pricePerHour || 0)
    const sharedHourPrice = Number(court.sharedPricePerHour || court.pricePerHour || 0)
    const sharedTimePrice = Number(court.sharedPricePerTime || court.pricePerTime || court.pricePerHour || 0)
    if (bookingMode.value === 'PACKAGE') return sum + hours * packagePrice
    if (pricingMode.value === 'SHARED_TIME') return sum + sharedTimePrice
    return sum + hours * sharedHourPrice
  }, 0)
  return cost.toFixed(2)
})

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 不能选择今天之前的日期
}

const formatCurrency = (val) => {
  if (val === null || val === undefined) return '0.00'
  const num = Number(val) || 0
  return num.toFixed(2)
}

const formatTimeRange = (start, end) => {
  if (!start || !end) return '-'
  const startDate = new Date(start)
  const endDate = new Date(end)
  const dateStr = `${startDate.getMonth() + 1}月${startDate.getDate()}日`
  const startTime = `${String(startDate.getHours()).padStart(2, '0')}:${String(startDate.getMinutes()).padStart(2, '0')}`
  const endTime = `${String(endDate.getHours()).padStart(2, '0')}:${String(endDate.getMinutes()).padStart(2, '0')}`
  return `${dateStr} ${startTime}-${endTime}`
}

const formatBookingTime = () => {
  if (!timeForm.value.date || !timeForm.value.startTime || !timeForm.value.endTime) {
    return '请选择时间'
  }
  return `${timeForm.value.date} ${timeForm.value.startTime} - ${timeForm.value.endTime}`
}

const getBookingModeText = (mode) => {
  return mode === 'PACKAGE' ? '包场' : '拼场'
}

const getPricingModeText = (mode, summary) => {
  const map = {
    PACKAGE_HOUR: '包场按小时',
    SHARED_HOUR: '拼场按小时',
    SHARED_TIME: '拼场按次'
  }
  return map[summary || mode] || '按订单规则计费'
}

const getBookingCourtSummary = (booking) => {
  const primary = booking.primaryCourtName || booking.courtName || '-'
  const count = Number(booking.courtCount || (Array.isArray(booking.courtNames) ? booking.courtNames.length : 0) || 0)
  return count > 1 ? `${primary} 等 ${count} 块场地` : primary
}

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: '' }
  return map[status] || 'info'
}

const getCourtStatusText = (status) => {
  const map = { 0: '维护中', 1: '空闲', 2: '预约中', 3: '使用中' }
  return map[status] || '未知'
}

const getCourtStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

// 按所选日期显示场地状态（与管理员端一致：维护中始终维护中，其余按该日预约数量显示 空闲/预约中）
const getCourtDisplayStatus = (court) => {
  if (court?.status === 0) {
    return { text: '维护中', type: 'info' }
  }
  const count = todayBookingCounts.value[court?.id] ?? 0
  if (bookingMode.value === 'PACKAGE' && count > 0) {
    return { text: '不可包场', type: 'danger' }
  }
  if (count > 0) {
    return { text: '预约中', type: 'warning' }
  }
  return { text: '空闲', type: 'success' }
}

const isCourtUnavailable = (court) => {
  if (court?.status === 0) return true
  return bookingMode.value === 'PACKAGE' && (todayBookingCounts.value[court?.id] ?? 0) > 0
}

const getCourtPriceText = (court) => {
  if (bookingMode.value === 'PACKAGE') {
    return `¥${formatCurrency(court.packagePricePerHour || court.pricePerHour)}/小时`
  }
  if (pricingMode.value === 'SHARED_TIME') {
    return `¥${formatCurrency(court.sharedPricePerTime || court.pricePerTime || court.pricePerHour)}/次`
  }
  return `¥${formatCurrency(court.sharedPricePerHour || court.pricePerHour)}/小时`
}

const getCourtHintText = (court) => {
  if (court?.status === 0) return '维护中，暂不可预约'
  if (bookingMode.value === 'PACKAGE') {
    return (todayBookingCounts.value[court?.id] ?? 0) > 0 ? '该日期已有预约，不能加入包场' : '空闲，可加入包场'
  }
  return pricingMode.value === 'SHARED_TIME' ? '拼场按次计费' : '拼场按小时计费'
}

// 加载场馆列表
const loadVenues = async () => {
  try {
    const res = await getVenueList({ page: 1, size: 100, status: 1 }) // 只获取营业中的场馆
    if (res.code === 200) {
      venues.value = res.data?.data || res.data?.records || res.data || []
    }
  } catch (e) {
    console.error('加载场馆列表失败:', e)
    ElMessage.error('加载场馆列表失败')
  }
}

// 选择场馆
const selectVenue = (venue) => {
  selectedVenue.value = venue
  step.value = 2
  loadCourts(venue.id)
}

const handleBookingModeChange = () => {
  selectedCourt.value = null
  selectedCourtIds.value = []
  currentOccupancy.value = { count: null, users: [] }
  rangeOccupancy.value = { count: null, users: [] }
  pricingMode.value = bookingMode.value === 'PACKAGE' ? 'PACKAGE_HOUR' : 'SHARED_HOUR'
}

// 加载场地列表
const loadCourts = async (venueId) => {
  try {
    const res = await getCourtList({ venueId, page: 1, size: 100 })
    if (res.code === 200) {
      courts.value = res.data?.data || res.data?.records || res.data || []
      await loadTodayBookingCounts()
    }
  } catch (e) {
    console.error('加载场地列表失败:', e)
    ElMessage.error('加载场地列表失败')
  }
}

// 加载所选日期下各场地的预约数量（与管理员端一致，用于展示 空闲/预约中）
const loadTodayBookingCounts = async () => {
  if (!courts.value?.length) {
    todayBookingCounts.value = {}
    return
  }
  try {
    const courtIds = courts.value.map((c) => c.id)
    const date = bookingDateForCourts.value || getLocalTodayDateStr()
    const res = await getTodayBookingCounts(courtIds, date)
    if (res.code === 200) {
      todayBookingCounts.value = res.data || {}
    } else {
      todayBookingCounts.value = {}
    }
  } catch (e) {
    console.error('加载预约数量失败:', e)
    todayBookingCounts.value = {}
  }
}

// 选择场地（仅维护中不可选；预约中仍可选，可与他人共享时段）
const selectCourt = (court) => {
  if (isCourtUnavailable(court)) {
    ElMessage.warning(bookingMode.value === 'PACKAGE' ? '该场地当前不可加入包场' : '该场地维护中，暂不可选')
    return
  }
  if (bookingMode.value === 'SHARED') {
    selectedCourt.value = court
    selectedCourtIds.value = [court.id]
  } else {
    const exists = selectedCourtIds.value.includes(court.id)
    if (exists) {
      selectedCourtIds.value = selectedCourtIds.value.filter(id => id !== court.id)
    } else {
      if (selectedCourtIds.value.length >= Math.max(0, courts.value.length - 1)) {
        ElMessage.warning('包场不能包下整个场馆的全部场地')
        return
      }
      selectedCourtIds.value = [...selectedCourtIds.value, court.id]
    }
    selectedCourt.value = selectedCourts.value[0] || null
  }
  step.value = 3
  // 预约日期默认使用「选择场地」步骤所选日期，与管理员端日期设计一致
  timeForm.value.date = bookingDateForCourts.value || getLocalTodayDateStr()
  timeForm.value.remark = ''
  rangeOccupancy.value = { count: null, users: [] }
  if (bookingMode.value === 'SHARED') {
    fetchCurrentOccupancy()
  } else {
    currentOccupancy.value = { count: null, users: [] }
  }
}

// 获取当前时刻的占用情况
const fetchCurrentOccupancy = async () => {
  if (!selectedCourt.value) {
    currentOccupancy.value = { count: null, users: [] }
    return
  }
  try {
    const res = await getBookingCurrentOccupancy({
      courtId: selectedCourt.value.id
    })
    if (res.code === 200) {
      currentOccupancy.value = {
        count: Number(res.data?.count ?? 0),
        users: res.data?.users || []
      }
    } else {
      currentOccupancy.value = { count: 0, users: [] }
    }
  } catch (e) {
    console.error('获取当前使用情况失败:', e)
    currentOccupancy.value = { count: 0, users: [] }
  }
}

// 获取所选时段已有多少人预约及名单
const fetchRangeOccupancy = async () => {
  if (!timeForm.value.date || !timeForm.value.startTime || !timeForm.value.endTime) {
    rangeOccupancy.value = { count: null, users: [] }
    return
  }
  if (bookingMode.value !== 'SHARED' || !selectedCourt.value) {
    rangeOccupancy.value = { count: null, users: [] }
    return
  }

  if (timeForm.value.startTime >= timeForm.value.endTime) {
    rangeOccupancy.value = { count: null, users: [] }
    return
  }

  try {
    const res = await getBookingRangeOccupancy({
      courtId: selectedCourt.value.id,
      bookingDate: timeForm.value.date,
      startTime: `${timeForm.value.startTime}:00`,
      endTime: `${timeForm.value.endTime}:00`
    })
    if (res.code === 200) {
      rangeOccupancy.value = {
        count: Number(res.data?.count ?? 0),
        users: res.data?.users || []
      }
    } else {
      rangeOccupancy.value = { count: null, users: [] }
    }
  } catch (e) {
    console.error('获取所选时段预约情况失败:', e)
    rangeOccupancy.value = { count: null, users: [] }
  }
}

const validateBookingForm = () => {
  if (!timeForm.value.date || !timeForm.value.startTime || !timeForm.value.endTime) {
    ElMessage.warning('请选择完整的预约时间')
    return false
  }

  if (timeForm.value.startTime >= timeForm.value.endTime) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return false
  }

  if (!selectedCourtIds.value.length) {
    ElMessage.warning('请至少选择一块场地')
    return false
  }

  if (bookingMode.value === 'PACKAGE') {
    if (packageLeadTimeError.value) {
      ElMessage.warning(packageLeadTimeError.value)
      return false
    }
    if (selectedCourtIds.value.length >= courts.value.length) {
      ElMessage.warning('包场不能包下整个场馆的全部场地')
      return false
    }
  }

  return true
}

const goToConfirmStep = () => {
  if (!validateBookingForm()) {
    return
  }
  step.value = 4
}

// 监听时间变化，校验并刷新当前预约人数
watch(
  [() => timeForm.value.date, () => timeForm.value.startTime, () => timeForm.value.endTime],
  () => {
    if (timeForm.value.date && timeForm.value.startTime && timeForm.value.endTime) {
      // 验证时间
      if (timeForm.value.startTime >= timeForm.value.endTime) {
        ElMessage.warning('结束时间必须晚于开始时间')
        rangeOccupancy.value = { count: null, users: [] }
        return
      }
      fetchRangeOccupancy()
    } else {
      rangeOccupancy.value = { count: null, users: [] }
    }
  }
)

// 提交预订
const submitBooking = async () => {
  if (!validateBookingForm()) {
    return
  }

  submitting.value = true
  try {
      const res = await addBooking({
        courtId: selectedCourtIds.value[0],
        courtIds: selectedCourtIds.value,
        bookingMode: bookingMode.value,
        pricingMode: bookingMode.value === 'PACKAGE' ? 'PACKAGE_HOUR' : pricingMode.value,
        bookingDate: timeForm.value.date,
        startTime: `${timeForm.value.startTime}:00`,
        endTime: `${timeForm.value.endTime}:00`,
        remark: timeForm.value.remark
      })
    
    if (res.code === 200) {
      ElMessage.success('预订成功！')
      // 重置表单
      step.value = 1
        selectedVenue.value = null
        selectedCourt.value = null
        selectedCourtIds.value = []
        bookingMode.value = 'SHARED'
        pricingMode.value = 'SHARED_HOUR'
        timeForm.value = { date: '', startTime: '', endTime: '', remark: '' }
      // 切换到我的预约标签页
      activeTab.value = 'my-bookings'
      loadMyBookings()
    } else {
      ElMessage.error(res.message || '预订失败')
    }
  } catch (e) {
    console.error('提交预订失败:', e)
    ElMessage.error('预订失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 加载我的预约
const loadMyBookings = async () => {
  try {
    const params = { page: 1, size: 20 }
    if (filterStatus.value) {
      params.status = parseInt(filterStatus.value)
    }
    if (currentMember.value?.id) {
      params.memberId = currentMember.value.id
    }
    const res = await getBookingList(params)
    if (res.code === 200) {
      myBookings.value = res.data?.data || []
    }
  } catch (e) {
    console.error('加载预约列表失败:', e)
  }
}

const handlePay = (booking) => {
  ElMessageBox.confirm(
    `确认使用余额支付该预约订单吗？\n订单金额：¥${formatCurrency(booking.orderAmount)}`,
    '预约支付确认',
    {
      type: 'warning',
      confirmButtonText: '确认支付',
      cancelButtonText: '稍后支付'
    }
  ).then(async () => {
    try {
      const res = await payMemberBooking({
        bookingId: booking.id,
        paymentMethod: 'BALANCE'
      })
      if (res.code === 200) {
        ElMessage.success('支付成功')
        await loadMyBookings()
      } else {
        ElMessage.error(res.message || '支付失败')
      }
    } catch (e) {
      console.error('预约支付失败:', e)
      ElMessage.error(e.response?.data?.message || e.message || '支付失败，请稍后重试')
    }
  }).catch(() => {})
}

const handleCancel = async (booking) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      type: 'warning'
    })
    
    const res = await updateBookingStatus(booking.id, 0)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadMyBookings()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消预约失败:', e)
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}

const handleViewDetail = (booking) => {
  // 可以打开详情弹窗或跳转到详情页
  ElMessage.info('查看详情功能开发中')
}

// 监听tab切换
watch(activeTab, (newTab) => {
  if (newTab === 'my-bookings') {
    loadMyBookings()
    } else {
      // 重置预订流程
      step.value = 1
      selectedVenue.value = null
      selectedCourt.value = null
      selectedCourtIds.value = []
      bookingMode.value = 'SHARED'
      pricingMode.value = 'SHARED_HOUR'
      bookingDateForCourts.value = getLocalTodayDateStr()
    todayBookingCounts.value = {}
    timeForm.value = { date: '', startTime: '', endTime: '', remark: '' }
    currentOccupancy.value = { count: null, users: [] }
    rangeOccupancy.value = { count: null, users: [] }
  }
})

const loadCurrentMember = async () => {
  try {
    const res = await getCurrentMember()
    if (res.code === 200) {
      currentMember.value = res.data
    }
  } catch (e) {
    console.error('加载当前会员信息失败:', e)
  }
}

onMounted(() => {
  loadCurrentMember()
  loadVenues()
  if (activeTab.value === 'my-bookings') {
    loadMyBookings()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-booking {
  padding: 0;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 减少动画（尊重用户的偏好设置） */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 标题动画定义 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes titleShimmer {
  0% {
    background-position: -200% center;
  }
  100% {
    background-position: 200% center;
  }
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.page-header {
  margin-bottom: 32px;
  padding: 24px 0;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, 
    var(--color-primary, #2563EB) 0%, 
    var(--color-secondary, #8B5CF6) 100%);
  border-radius: 2px;
  opacity: 0.6;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    var(--color-border, rgba(226, 232, 240, 0.5)) 50%, 
    transparent 100%);
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  font-family: 'Poppins', 'Inter', sans-serif;
  position: relative;
  padding-left: 16px;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
  
  /* 渐变文字效果 */
  background: linear-gradient(135deg, 
    var(--color-text-primary, #1E293B) 0%, 
    var(--color-primary, #2563EB) 50%,
    var(--color-secondary, #8B5CF6) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  
  /* 文字阴影（作为后备） */
  text-shadow: 0 2px 8px rgba(37, 99, 235, 0.15);
  
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, 
    var(--color-primary, #2563EB) 0%, 
    var(--color-secondary, #8B5CF6) 100%);
  border-radius: 2px;
  opacity: 0.8;
}

.page-title:hover {
  transform: translateX(4px);
  background-position: 100% 50%;
}

.page-subtitle {
  font-size: 17px;
  margin: 0;
  padding-left: 16px;
  line-height: 1.6;
  letter-spacing: 0.3px;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s forwards;
  opacity: 0;
  
  /* 渐变文字效果 */
  background: linear-gradient(135deg, 
    var(--color-text-secondary, #64748B) 0%, 
    var(--color-primary, #2563EB) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: var(--color-text-secondary, #64748B);
  
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-subtitle:hover {
  transform: translateX(4px);
  opacity: 1;
}

/* 深色模式适配 */
html.theme-dark-mode .page-title {
  background: linear-gradient(135deg, 
    var(--color-text-primary, #F1F5F9) 0%, 
    var(--color-primary, #60A5FA) 50%,
    var(--color-secondary, #A78BFA) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 8px rgba(96, 165, 250, 0.2);
}

html.theme-dark-mode .page-subtitle {
  background: linear-gradient(135deg, 
    var(--color-text-secondary, #94A3B8) 0%, 
    var(--color-primary, #60A5FA) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 无障碍：减少动画 */
@media (prefers-reduced-motion: reduce) {
  .page-title,
  .page-subtitle {
    animation: none;
    opacity: 1;
  }
  
  .page-title:hover,
  .page-subtitle:hover {
    transform: none;
  }
}

.booking-tabs {
  margin-top: 24px;
}

/* 步骤指示器 */
.step-indicator {
  margin-bottom: 32px;
  padding: 24px;
  background: var(--color-card-bg, #FFFFFF);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.step-indicator-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  max-width: 800px;
  margin: 0 auto;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background, #F1F5F9);
  border: 3px solid var(--color-border, #E2E8F0);
  color: var(--color-text-secondary, #64748B);
  font-weight: 600;
  font-size: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.step-item.current .step-number {
  background: var(--color-primary, #2563EB);
  border-color: var(--color-primary, #2563EB);
  color: #ffffff;
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
  animation: pulseStep 2s ease-in-out infinite;
}

.step-item.completed .step-number {
  background: var(--color-success, #10B981);
  border-color: var(--color-success, #10B981);
  color: #ffffff;
  transform: scale(1.05);
}

.step-item.completed .step-number .el-icon {
  font-size: 20px;
}

@keyframes pulseStep {
  0%, 100% {
    box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
  }
  50% {
    box-shadow: 0 4px 24px rgba(37, 99, 235, 0.6);
  }
}

.step-label {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
  transition: all 0.3s ease;
  text-align: center;
}

.step-item.current .step-label {
  color: var(--color-primary, #2563EB);
  font-weight: 600;
}

.step-item.completed .step-label {
  color: var(--color-success, #10B981);
}

.step-line {
  position: absolute;
  top: 20px;
  left: calc(50% + 20px);
  right: calc(-50% + 20px);
  height: 3px;
  background: var(--color-border, #E2E8F0);
  z-index: 0;
  transition: all 0.4s ease;
}

.step-item.completed + .step-item .step-line {
  background: var(--color-success, #10B981);
}

.step-item.current .step-line {
  background: linear-gradient(90deg, var(--color-success, #10B981) 0%, var(--color-border, #E2E8F0) 100%);
}

/* 步骤样式 */
.booking-step {
  padding: 24px;
  animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

/* 选择场地步骤：日期栏（与管理员端按日期显示状态一致） */
.court-date-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 20px;
  margin-bottom: 20px;
  padding: 14px 18px;
  background: var(--color-background, #F8FAFC);
  border-radius: 12px;
  border: 1px solid var(--color-border, #E2E8F0);
}

.court-date-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary, #64748B);
}

.court-date-picker {
  width: 160px;
}

.court-date-hint {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
  margin-left: 4px;
}

/* 场馆卡片网格：minmax(0,1fr) 保证多列等分、最右列不被挤压 */
.venue-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  box-sizing: border-box;
}

.venue-grid .venue-card {
  min-width: 0; /* 允许在 grid 中正确收缩，避免撑破布局 */
}

.venue-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 20px;
  padding: 28px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.venue-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.05), transparent);
  transition: left 0.5s ease;
}

.venue-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(37, 99, 235, 0.2);
  border-color: var(--color-primary-light, #60A5FA);
}

.venue-card:hover::before {
  left: 100%;
}

.venue-card.selected {
  border-color: var(--color-primary, #2563EB);
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, rgba(37, 99, 235, 0.08) 100%);
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.3), inset 0 0 0 2px rgba(37, 99, 235, 0.1);
  transform: scale(1.02);
  animation: selectedPulse 2s ease-in-out infinite;
}

@keyframes selectedPulse {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(37, 99, 235, 0.3), inset 0 0 0 2px rgba(37, 99, 235, 0.1);
  }
  50% {
    box-shadow: 0 8px 28px rgba(37, 99, 235, 0.4), inset 0 0 0 2px rgba(37, 99, 235, 0.15);
  }
}

.venue-image {
  width: 88px;
  height: 88px;
  border-radius: 20px;
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-secondary, #8B5CF6) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.35);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.venue-card:hover .venue-image {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.45);
}

.venue-card.selected .venue-image {
  animation: iconBounce 1s ease-in-out infinite;
}

@keyframes iconBounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-4px);
  }
}

.venue-info {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.venue-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 8px 0;
}

.venue-address {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin: 0 0 12px 0;
  text-align: center;
}

.venue-tags {
  display: flex;
  justify-content: center;
  width: 100%;
  gap: 8px;
}

/* 场地卡片网格 */
.court-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.court-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.court-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--color-primary, #2563EB);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.court-card:hover:not(.unavailable) {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(37, 99, 235, 0.2);
  border-color: var(--color-primary-light, #60A5FA);
}

.court-card:hover:not(.unavailable)::before {
  transform: scaleX(1);
}

.court-card.selected {
  border-color: var(--color-primary, #2563EB);
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, rgba(37, 99, 235, 0.08) 100%);
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.3), inset 0 0 0 2px rgba(37, 99, 235, 0.1);
  transform: scale(1.02);
}

.court-card.selected::before {
  transform: scaleX(1);
  height: 4px;
}

.court-card.unavailable {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(0.5);
}

.court-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.court-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.court-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.court-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-label {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
}

.price-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
}

.court-type {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

/* ========== 选择预约时间（步骤3）美化 ========== */
/* 参考 Galaxy 卡片 + React Bits 高亮块：图标、等分三列、柔和阴影与过渡 */
.time-selection {
  max-width: 720px;
  margin: 0 auto;
  animation: timeStepFade 0.4s ease-out;
}

@keyframes timeStepFade {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 预约摘要卡片：三列等分、带图标，解决右侧宽度与留白问题 */
.selected-info-card {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px 32px;
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, rgba(37, 99, 235, 0.06) 100%);
  border: 1px solid var(--color-primary-light, #93C5FD);
  border-radius: 20px;
  padding: 28px 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.08), 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}

.selected-info-card:hover {
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.12), 0 2px 6px rgba(0, 0, 0, 0.04);
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 8px;
}

.info-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  transition: transform 0.25s ease;
}

.selected-info-card:hover .info-icon-wrap {
  transform: scale(1.05);
}

.info-icon-venue {
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, #3B82F6 100%);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
}

.info-icon-court {
  background: linear-gradient(135deg, #059669 0%, #10B981 100%);
  box-shadow: 0 4px 12px rgba(5, 150, 105, 0.35);
}

.info-icon-price {
  background: linear-gradient(135deg, #DC2626 0%, #EF4444 100%);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.35);
}

.info-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-secondary, #64748B);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  line-height: 1.4;
  word-break: break-all;
}

.info-value.price {
  color: var(--color-danger, #DC2626);
  font-size: 20px;
  font-family: 'Poppins', sans-serif;
  letter-spacing: 0.02em;
}

/* 当前使用情况区块 */
.current-occupancy-card {
  margin-bottom: 24px;
  padding: 20px 24px;
  border-radius: 16px;
  border: 1px solid var(--color-border, #E2E8F0);
  background: var(--color-card-bg, #FFFFFF);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.current-occupancy-card:hover {
  border-color: var(--color-primary-light, #93C5FD);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.06);
}

.current-occupancy-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.occupancy-icon {
  color: var(--color-primary, #2563EB);
  flex-shrink: 0;
}

.current-occupancy-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.current-occupancy-body {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  line-height: 1.6;
  padding-left: 28px;
}

.current-occupancy-summary {
  margin-bottom: 4px;
}

.current-occupancy-tags,
.range-occupancy-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.occupancy-tag {
  font-size: 12px;
}

/* 预约表单卡片（ui-ux-pro-max：层次、焦点环、过渡、可访问对比） */
.time-form-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 20px;
  padding: 28px 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.25s ease;
}

.time-form-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}

.time-form-card-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 24px 0;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  letter-spacing: 0.02em;
}

.time-form {
  margin: 0;
}

.time-form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 24px;
  align-items: start;
  margin-bottom: 20px;
}

.time-form-row :deep(.el-form-item) {
  margin-bottom: 0;
  align-items: center;
}

.time-form-row :deep(.el-form-item .el-form-item__content) {
  min-height: 32px;
  display: flex;
  align-items: center;
}

.time-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.time-form :deep(.el-form-item:last-child),
.time-form :deep(.time-form-item-occupancy) {
  margin-bottom: 0;
}

.time-form :deep(.el-form-item .el-form-item__label) {
  font-weight: 500;
  color: var(--color-text-primary, #334155);
  font-size: 14px;
}

/* 日期/时间选择器：统一宽度、圆角、焦点环、过渡（ui-ux-pro-max 稳定 hover/焦点） */
.time-form :deep(.time-form-picker),
.time-form :deep(.el-date-editor),
.time-form :deep(.el-time-picker) {
  width: 100%;
}

.time-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px var(--color-border, #E2E8F0);
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.time-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary-light, #93C5FD);
}

.time-form :deep(.el-input.is-focus .el-input__wrapper),
.time-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--color-primary, #2563EB);
}

.time-form :deep(.el-input .el-input__wrapper) {
  padding: 8px 14px;
}

/* 预计费用高亮块 */
.estimated-cost-block {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 10px;
  padding: 14px 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.08) 0%, rgba(220, 38, 38, 0.04) 100%);
  border-left: 4px solid var(--color-danger, #DC2626);
  transition: background 0.25s ease;
}

.cost-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-danger, #DC2626);
  font-family: 'Poppins', sans-serif;
  letter-spacing: 0.02em;
}

.cost-desc {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

/* 时段预约情况：提示框（左侧主色条 + 浅底，信息层级清晰） */
.booking-occupancy {
  font-size: 13px;
  color: var(--color-text-secondary, #475569);
  line-height: 1.65;
  padding: 14px 18px;
  border-radius: 12px;
  background: linear-gradient(90deg, rgba(37, 99, 235, 0.06) 0%, var(--color-background, #F8FAFC) 12px);
  border: 1px solid var(--color-border, #E2E8F0);
  border-left: 4px solid var(--color-primary, #2563EB);
  transition: border-color 0.2s ease, background 0.2s ease;
}

.booking-occupancy .occupancy-count {
  font-weight: 700;
  color: var(--color-primary, #2563EB);
  margin: 0 2px;
}

/* 确认卡片 */
.confirm-card {
  max-width: 600px;
  margin: 0 auto;
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-primary-light, #60A5FA);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(37, 99, 235, 0.15);
  animation: scaleIn 0.4s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.confirm-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 20px 0;
}

.confirm-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.confirm-item:last-child {
  border-bottom: none;
}

.confirm-label {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
}

.confirm-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.confirm-value.price {
  font-size: 24px;
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
}

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border, #E2E8F0);
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

/* 我的预约 */
.my-bookings-section {
  padding: 24px 0;
}

.filter-bar {
  margin-bottom: 20px;
}

.bookings-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.booking-item-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.booking-item-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--color-primary, #2563EB);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.booking-item-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(37, 99, 235, 0.15);
  border-color: var(--color-primary-light, #60A5FA);
  transform: translateX(4px);
}

.booking-item-card:hover::before {
  transform: scaleY(1);
}

.booking-main {
  flex: 1;
}

.booking-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.booking-no {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Code', monospace;
}

.booking-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.booking-venue {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.booking-time {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

.booking-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  margin: 4px 0 0 0;
  font-family: 'Poppins', sans-serif;
}

.booking-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 响应式 - 移动端优化 */
@media (max-width: 768px) {
  .step-indicator {
    padding: 16px;
  }
  
  .step-indicator-container {
    gap: 8px;
  }
  
  .step-number {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }
  
  .step-label {
    font-size: 11px;
  }
  
  .step-line {
    display: none;
  }
  
  .venue-grid,
  .court-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .venue-card,
  .court-card {
    padding: 20px;
  }
  
  .booking-item-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .booking-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .time-selection,
  .confirm-card {
    max-width: 100%;
  }

  .selected-info-card {
    grid-template-columns: 1fr;
    padding: 20px 24px;
    gap: 20px;
  }

  .time-form-card {
    padding: 20px 24px;
  }

  .time-form-card-title {
    font-size: 15px;
    margin-bottom: 16px;
  }

  .time-form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
  
  /* 移动端触摸优化 */
  .venue-card,
  .court-card,
  .booking-item-card {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
}
</style>
