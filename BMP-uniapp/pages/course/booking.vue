<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="header-left">
          <view class="icon-btn icon-btn-ghost" @tap="handleBack">
            <uni-icons type="left" size="22" color="#5f5e5e" />
          </view>
          <text class="brand-title">KINETIC LOGIC</text>
        </view>
        <view class="icon-btn icon-btn-ghost" @tap="openNotice">
          <uni-icons type="notification-filled" size="20" color="#ff6600" />
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="content">
        <view class="hero-copy">
          <text class="page-title">提交订单</text>
          <text class="page-subtitle">请核对以下课程信息及支付明细</text>
        </view>

        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载课程订单…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadCourseDetail">重新加载</view>
        </view>

        <template v-else-if="detail">
          <view class="course-card">
            <view class="card-orbit" />
            <image class="course-image" src="/static/placeholders/hero.svg" mode="aspectFill" />

            <view class="course-info">
              <view class="course-head">
                <text class="course-name">{{ detail.name }}</text>
                <text class="status-badge">CONFIRMED</text>
              </view>

              <view class="coach-line">
                <uni-icons type="person-filled" size="14" color="#6b7280" />
                <text>主教练: {{ detail.coachText }}</text>
              </view>

              <view class="meta-panel">
                <view class="meta-item">
                  <view class="meta-icon">
                    <uni-icons type="clock-filled" size="16" color="#a33e00" />
                  </view>
                  <view class="meta-copy">
                    <text class="meta-main">{{ detail.dateText }}</text>
                    <text class="meta-sub">{{ detail.timeText }}</text>
                  </view>
                </view>

                <view class="meta-item">
                  <view class="meta-icon">
                    <uni-icons type="location-filled" size="16" color="#a33e00" />
                  </view>
                  <view class="meta-copy">
                    <text class="meta-main">{{ detail.venueText }}</text>
                    <text class="meta-sub">{{ detail.courtText }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="section-card">
            <text class="section-title">学员信息</text>

            <view class="field-block">
              <text class="field-label">STUDENT NAME</text>
              <text class="field-value">{{ detail.studentName }}</text>
            </view>

            <view class="field-block">
              <text class="field-label">CONTACT NUMBER</text>
              <text class="field-value">{{ detail.contactNumber }}</text>
            </view>
          </view>

          <view class="section-card">
            <text class="section-title">支付明细</text>

            <view class="price-list">
              <view class="price-row">
                <text class="price-key">课程订单金额</text>
                <text class="price-val">¥ {{ detail.orderAmount }}</text>
              </view>
            </view>

            <view class="total-row">
              <text class="total-key">实付款</text>
              <text class="total-val">¥ {{ detail.payableAmount }}</text>
            </view>
            <text class="amount-note">{{ detail.amountNote }}</text>
          </view>
        </template>
      </view>
    </scroll-view>

    <view v-if="detail" class="bottom-bar">
      <view class="bottom-total desktop-total">
        <text class="bottom-total-label">总计应付</text>
        <text class="bottom-total-value">¥ {{ detail.payableAmount }}</text>
      </view>
      <view class="pay-btn" :class="{ disabled: !canSubmit || isSubmitting }" @tap="handleSubmit">
        <text>{{ isSubmitting ? '支付中...' : '确认支付 Confirm & Pay' }}</text>
        <uni-icons v-if="!isSubmitting" type="right" size="18" color="#561d00" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { createCourseBooking, getCourseDetail, type CourseItem } from '@/api/course'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { useCurrentMember } from '@/composables/useCurrentMember'

type CourseBookingVm = {
  id: number
  name: string
  coachText: string
  dateText: string
  timeText: string
  venueText: string
  courtText: string
  studentName: string
  contactNumber: string
  orderAmount: string
  payableAmount: string
  amountNote: string
}

const userStore = useUserStore()
const { fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const refreshing = ref(false)
const loading = ref(true)
const errorText = ref('')
const isSubmitting = ref(false)
const courseId = ref(0)
const course = ref<CourseItem | null>(null)

function pad2(value: number) {
  return value < 10 ? `0${value}` : `${value}`
}

function toDate(raw?: string) {
  if (!raw) return null
  const date = new Date(String(raw).replace(/-/g, '/'))
  return Number.isNaN(date.getTime()) ? null : date
}

function normalizeTime(value?: string) {
  if (!value) return '--:--'
  return String(value).slice(0, 5)
}

function formatDate(raw?: string) {
  const date = toDate(raw)
  if (!date) return '待定日期'
  const weeks = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${date.getFullYear()}年${pad2(date.getMonth() + 1)}月${pad2(date.getDate())}日 (${weeks[date.getDay()]})`
}

function formatMoney(value: number) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function resolveStudentName() {
  const user = userStore.userInfo
  return user?.nickname || user?.username || '未登录学员'
}

function resolvePhone() {
  return userStore.userInfo?.phone || '未绑定手机号'
}

const payableValue = computed(() => {
  if (!course.value) return 0
  return Number(course.value.coursePrice || 0)
})

const canSubmit = computed(() => {
  if (!course.value) return false
  return Number(course.value.status ?? 1) === 1 && Number(course.value.currentStudents || 0) < Number(course.value.maxStudents || 0)
})

const detail = computed<CourseBookingVm | null>(() => {
  if (!course.value) return null

  const durationText = Number(course.value.courseDuration || 0) > 0 ? `${course.value.courseDuration}分钟` : '时长待定'
  const venueText = course.value.venueName?.trim() || '待补充'
  const courtText = course.value.courtName?.trim() || course.value.location?.trim() || '待补充'
  const coachName = course.value.coachName?.trim() || ''
  const coachRole = course.value.coachInfo?.trim() || ''
  const coachText = coachName && coachRole ? `${coachName} (${coachRole})` : coachName || coachRole || '待补充'
  const orderAmount = formatMoney(Number(course.value.coursePrice || 0))

  return {
    id: course.value.id,
    name: course.value.courseName || '课程预约',
    coachText,
    dateText: formatDate(course.value.courseDate),
    timeText: `${normalizeTime(course.value.startTime)} - ${normalizeTime(course.value.endTime)} (${durationText})`,
    venueText,
    courtText,
    studentName: resolveStudentName(),
    contactNumber: resolvePhone(),
    orderAmount,
    payableAmount: formatMoney(payableValue.value),
    amountNote: '当前页面仅展示课程订单真实金额，金额拆分能力未接入时不再补静态优惠说明。'
  }
})

const getCourseBookingErrorMessage = (error: unknown): string => {
  const rawMsg = String((error as { message?: string; errMsg?: string })?.message || (error as { errMsg?: string })?.errMsg || '')
  const msg = rawMsg.toLowerCase()

  if (msg.includes('full') || rawMsg.includes('满员') || rawMsg.includes('名额')) {
    return '课程名额已满，请选择其他课程'
  }
  if (msg.includes('duplicate') || rawMsg.includes('重复') || rawMsg.includes('已预约')) {
    return '您已预约该课程，请勿重复提交'
  }
  if (msg.includes('expired') || rawMsg.includes('截止') || rawMsg.includes('结束')) {
    return '课程预约已截止'
  }
  return rawMsg || '预约失败，请重试'
}

async function loadCourseDetail() {
  if (!courseId.value) {
    loading.value = false
    errorText.value = '缺少课程参数'
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    course.value = await getCourseDetail(courseId.value)
  } catch (error) {
    console.error('加载课程详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载课程详情失败'
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (isSubmitting.value || !detail.value) return

  if (!canSubmit.value) {
    uni.showToast({
      title: '当前课程暂不可预约',
      icon: 'none'
    })
    return
  }

  try {
    isSubmitting.value = true
    uni.showLoading({ title: '支付中...' })
    const member = await fetchCurrentMember()

    await createCourseBooking({
      memberId: Number(member.id || 0),
      courseId: detail.value.id,
      orderAmount: payableValue.value,
      paymentMethod: 'BALANCE'
    })

    uni.hideLoading()
    uni.showToast({
      title: '预约成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/course/detail?id=${detail.value?.id}`
      })
    }, 1200)
  } catch (error) {
    console.error('预约课程失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: getCourseBookingErrorMessage(error),
      icon: 'none'
    })
  } finally {
    isSubmitting.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/course/detail')
}

function openNotice() {
  uni.navigateTo({
    url: '/pages/notice/index'
  })
}

function handleRefresh() {
  refreshing.value = true
  loadCourseDetail().finally(() => {
    refreshing.value = false
  })
}

onLoad(async (options?: Record<string, string | undefined>) => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  courseId.value = Number(options?.id || 0)
  await loadCourseDetail()
})

onPullDownRefresh(() => {
  loadCourseDetail().finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f7f7f7 0%, #f3f3f3 100%);
  color: #1a1c1c;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(243, 243, 243, 0.9);
  backdrop-filter: blur(18px);
  box-shadow: 0 20rpx 40rpx rgba(26, 28, 28, 0.04);
}

.header-inner {
  min-height: 112rpx;
  padding: 10rpx 28rpx 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-btn-ghost {
  border-radius: 9999rpx;
  background: rgba(255, 255, 255, 0.75);
}

.brand-title {
  font-size: 38rpx;
  line-height: 1;
  font-weight: 900;
  font-style: italic;
  color: #ff6600;
  letter-spacing: -1rpx;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 24rpx 18rpx 240rpx;
}

.hero-copy {
  padding: 8rpx 6rpx 18rpx;
}

.page-title {
  display: block;
  font-size: 60rpx;
  line-height: 1.08;
  font-weight: 900;
  color: #101010;
}

.page-subtitle {
  display: block;
  margin-top: 14rpx;
  font-size: 25rpx;
  color: #5f5e5e;
  font-weight: 500;
}

.course-card,
.section-card,
.state-card {
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 12rpx 36rpx rgba(26, 28, 28, 0.04);
}

.course-card {
  position: relative;
  margin-top: 16rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  overflow: hidden;
}

.card-orbit {
  position: absolute;
  top: -40rpx;
  right: -38rpx;
  width: 220rpx;
  height: 220rpx;
  border-radius: 0 0 0 9999rpx;
  background: rgba(255, 102, 0, 0.05);
}

.course-image {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 420rpx;
  border-radius: 24rpx;
  background: #111111;
}

.course-info {
  position: relative;
  z-index: 1;
  margin-top: 24rpx;
}

.course-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.course-name {
  flex: 1;
  font-size: 46rpx;
  line-height: 1.15;
  font-weight: 900;
  color: #111111;
}

.status-badge {
  flex-shrink: 0;
  padding: 10rpx 18rpx;
  border-radius: 9999rpx;
  background: #e2dfde;
  color: #636262;
  font-size: 20rpx;
  font-weight: 900;
  letter-spacing: 2rpx;
}

.coach-line {
  margin-top: 18rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 25rpx;
  color: #5f5e5e;
}

.meta-panel {
  margin-top: 24rpx;
  padding: 20rpx;
  border-radius: 20rpx;
  background: #f3f3f3;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.meta-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 9999rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.meta-copy {
  flex: 1;
}

.meta-main {
  display: block;
  font-size: 29rpx;
  font-weight: 900;
  color: #111111;
}

.meta-sub {
  display: block;
  margin-top: 6rpx;
  font-size: 23rpx;
  color: #5f5e5e;
}

.section-card {
  margin-top: 22rpx;
  border-radius: 28rpx;
  padding: 30rpx 26rpx;
}

.section-title {
  display: block;
  font-size: 42rpx;
  font-weight: 900;
  color: #111111;
}

.field-block {
  padding: 28rpx 0 22rpx;
  border-top: 2rpx solid #ececec;
}

.field-block:first-of-type {
  margin-top: 24rpx;
}

.field-label {
  display: block;
  font-size: 20rpx;
  font-weight: 900;
  letter-spacing: 4rpx;
  color: #5f5e5e;
}

.field-value {
  display: block;
  margin-top: 24rpx;
  font-size: 42rpx;
  font-weight: 900;
  color: #111111;
  line-height: 1.2;
}

.price-list {
  margin-top: 24rpx;
}

.price-row {
  padding: 18rpx 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.price-key {
  font-size: 27rpx;
  color: #5f5e5e;
}

.price-val {
  font-size: 27rpx;
  font-weight: 800;
  color: #111111;
}

.total-row {
  margin-top: 10rpx;
  padding-top: 22rpx;
  border-top: 2rpx solid #ececec;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18rpx;
}

.total-key {
  font-size: 34rpx;
  font-weight: 900;
  color: #111111;
}

.total-val {
  font-size: 64rpx;
  line-height: 1;
  font-weight: 900;
  letter-spacing: -2rpx;
  color: #a33e00;
}

.amount-note {
  display: block;
  margin-top: 18rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b7280;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  padding: 18rpx 22rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(18px);
  border-top: 2rpx solid #eeeeee;
  box-shadow: 0 -20rpx 40rpx rgba(26, 28, 28, 0.05);
}

.desktop-total {
  display: none;
}

.pay-btn {
  width: 100%;
  height: 96rpx;
  border-radius: 18rpx;
  background: linear-gradient(90deg, #a33e00 0%, #ff6600 100%);
  color: #561d00;
  box-shadow: 0 10rpx 30rpx rgba(255, 102, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: 34rpx;
  font-weight: 900;
}

.pay-btn.disabled {
  background: #d7d7d7;
  color: #8c8c8c;
  box-shadow: none;
}

.bottom-total-label {
  display: block;
  font-size: 20rpx;
  color: #5f5e5e;
  font-weight: 600;
}

.bottom-total-value {
  display: block;
  margin-top: 6rpx;
  font-size: 36rpx;
  font-weight: 900;
  color: #a33e00;
}

.state-card {
  margin-top: 24rpx;
  border-radius: 28rpx;
  padding: 90rpx 28rpx;
  text-align: center;
}

.state-text {
  font-size: 28rpx;
  color: #777777;
}

.state-action {
  width: 220rpx;
  height: 76rpx;
  margin: 22rpx auto 0;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  margin: 0 auto 18rpx;
  border: 4rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 9999rpx;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media screen and (min-width: 768px) {
  .bottom-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20rpx;
    padding-left: 40rpx;
    padding-right: 40rpx;
  }

  .desktop-total {
    display: block;
    min-width: 180rpx;
  }

  .pay-btn {
    flex: 1;
    max-width: 460rpx;
  }
}
</style>
