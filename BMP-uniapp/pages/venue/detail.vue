<template>
  <view class="venue-detail-page">
    <view class="topbar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="topbar-inner">
        <view class="icon-btn" @click="handleBack">
          <uni-icons type="left" size="20" color="#ff6600" />
        </view>
        <view class="topbar-brand">
          <text class="brand-word">KINETIC LOGIC</text>
          <text class="brand-sub">VENUE EXPERIENCE</text>
        </view>
        <view class="icon-btn ghost" @click="handleHistory">
          <uni-icons type="calendar" size="18" color="#a33e00" />
        </view>
      </view>
    </view>

    <scroll-view
      class="page-scroll"
      scroll-y
      :style="{ marginTop: `${headerOffsetPx}px`, height: scrollAreaHeight }"
      :show-scrollbar="false"
    >
      <view v-if="loading" class="state-panel">
        <view class="state-spinner" />
        <text class="state-title">加载场馆中</text>
        <text class="state-desc">正在同步场馆信息、场地价格与预约入口</text>
      </view>

      <view v-else-if="venue.id" class="page-body">
        <view class="hero-shell">
          <image
            v-if="venue.venueImage"
            class="hero-image"
            :src="resolveImageUrl(venue.venueImage)"
            mode="aspectFill"
          />
          <view v-else class="hero-placeholder">
            <uni-icons type="image" size="40" color="#9ca3af" />
          </view>
          <view class="hero-overlay" />
          <view class="hero-noise" />
          <view class="hero-content">
            <view class="hero-badge-row">
              <text class="hero-badge accent">{{ venue.status === 1 ? '当前可预约' : '暂不可预约' }}</text>
              <text class="hero-badge glass">{{ openHoursText }}</text>
            </view>
            <text class="hero-title">{{ venue.name || '场馆详情' }}</text>
            <text class="hero-subtitle">{{ heroIntro }}</text>
            <view class="hero-meta">
              <view class="hero-meta-item">
                <uni-icons type="location" size="14" color="#ffffff" />
                <text class="hero-meta-text">{{ venue.location || '暂无地址' }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="summary-ribbon">
          <view class="summary-card warm">
            <text class="summary-label">Hourly From</text>
            <view class="summary-value-row">
              <text class="summary-unit">¥</text>
              <text class="summary-value">{{ displayHourlyPrice }}</text>
              <text class="summary-tail">/ 小时</text>
            </view>
            <text class="summary-foot">价格以场地配置和实际下单结果为准</text>
          </view>
          <view class="summary-card dark">
            <text class="summary-label light">营业时间</text>
            <text class="summary-mini light">{{ openHoursText }}</text>
            <text class="summary-foot light">营业时间以场馆当前配置为准</text>
          </view>
          <view class="summary-card">
            <text class="summary-label">场馆联系</text>
            <text class="summary-mini">{{ venue.contactPerson || '场馆前台' }}</text>
            <text class="summary-foot">{{ venue.contactPhone || '暂无联系电话' }}</text>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Snapshot</text>
              <text class="section-title">预约前快速确认</text>
            </view>
            <text class="section-badge" :class="{ offline: venue.status !== 1 }">
              {{ venue.status === 1 ? '当前可约' : '暂不可约' }}
            </text>
          </view>
          <view class="spotlight-grid">
            <view v-for="item in spotlightCards" :key="item.label" class="spotlight-item">
              <view class="spotlight-icon">
                <uni-icons :type="item.icon" size="18" color="#ff6600" />
              </view>
              <text class="spotlight-label">{{ item.label }}</text>
              <text class="spotlight-value">{{ item.value }}</text>
              <text class="spotlight-desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>

        <view class="section-card statement-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Description</text>
              <text class="section-title">场馆说明</text>
            </view>
          </view>
          <view class="statement-panel">
            <text class="statement-title">场馆介绍</text>
            <text class="statement-copy">{{ descriptionText }}</text>
          </view>
        </view>

        <view class="section-card program-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Guide</text>
              <text class="section-title">预约说明</text>
            </view>
          </view>
          <view class="journey-list">
            <view v-for="item in bookingJourney" :key="item.step" class="journey-item">
              <view class="journey-index">{{ item.step }}</view>
              <view class="journey-main">
                <text class="journey-title">{{ item.title }}</text>
                <text class="journey-copy">{{ item.copy }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="section-card action-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Quick Actions</text>
              <text class="section-title">预约前常用操作</text>
            </view>
          </view>
          <view class="action-grid">
            <view class="action-tile" @click="handleCall(venue.contactPhone)">
              <view class="action-icon filled">
                <uni-icons type="phone" size="18" color="#ffffff" />
              </view>
              <text class="action-name">联系场馆</text>
              <text class="action-copy">确认联系电话、营业时间和线下到馆信息</text>
            </view>
            <view class="action-tile" @click="handleHistory">
              <view class="action-icon">
                <uni-icons type="calendar" size="18" color="#ff6600" />
              </view>
              <text class="action-name">查看预约</text>
              <text class="action-copy">返回我的预约，查看当前订单和历史记录</text>
            </view>
            <view class="action-tile" @click="handleLocation">
              <view class="action-icon">
                <uni-icons type="location" size="18" color="#ff6600" />
              </view>
              <text class="action-name">复制地址</text>
              <text class="action-copy">复制当前场馆地址，方便自行导航或转发</text>
            </view>
          </view>
        </view>

        <view class="section-card promise-card">
          <view class="promise-shell">
            <view>
              <text class="section-kicker inverse">Ready To Play</text>
              <text class="promise-title">现在就进入预约流程</text>
              <text class="promise-copy">{{ servicePromise }}</text>
            </view>
            <view class="promise-cta" @click="handleBook">
              <text class="promise-cta-text">进入预约</text>
              <uni-icons type="right" size="16" color="#ffffff" />
            </view>
          </view>
        </view>

        <view class="scroll-buffer" />
      </view>

      <view v-else class="state-panel">
        <text class="state-title">场馆不存在</text>
        <text class="state-desc">这条场馆信息可能已下线或被移除</text>
      </view>
    </scroll-view>

    <view v-if="venue.id" class="bottom-bar">
      <view class="bottom-glass" />
      <view class="bottom-content">
        <view class="bottom-price">
          <text class="bottom-label">参考价格</text>
          <view class="bottom-amount-row">
            <text class="bottom-currency">¥</text>
            <text class="bottom-amount">{{ displayHourlyPrice }}</text>
            <text class="bottom-unit">/ 小时</text>
          </view>
          <text class="bottom-foot">{{ venue.status === 1 ? '价格以预约页实时计算结果为准' : '当前暂停在线预约' }}</text>
        </view>
        <view class="bottom-cta" :class="{ disabled: venue.status !== 1 }" @click="handleBook">
          <text class="bottom-cta-text">{{ venue.status === 1 ? '立即预约' : '暂不可预约' }}</text>
          <uni-icons type="right" size="18" color="#ffffff" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import { getVenueDetail } from '@/api/venue'
import type { VenueItem } from '@/api/venue'
import { safeNavigateBack } from '@/utils/navigation'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

type VenueDetailVm = {
  id: number
  name: string
  venueImage: string
  location: string
  contactPhone: string
  contactPerson: string
  businessHours: string
  description: string
  status: number
  hourlyPrice: number
}

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const headerOffsetPx = computed(() => statusBarHeight.value + 64)
const scrollAreaHeight = computed(() => {
  const viewport = systemInfo.windowHeight || systemInfo.screenHeight || 0
  return `${Math.max(viewport - headerOffsetPx.value, 0)}px`
})

const userStore = useUserStore()
const venueId = ref(0)
const loading = ref(true)

const venue = ref<VenueDetailVm>({
  id: 0,
  name: '',
  venueImage: '',
  location: '',
  contactPhone: '',
  contactPerson: '',
  businessHours: '',
  description: '',
  status: 1,
  hourlyPrice: 0
})

const descriptionText = computed(() => {
  return (
    venue.value.description ||
    '当前场馆暂无更多图文说明，你仍可以查看基础信息并进入预约流程。'
  )
})

const openHoursText = computed(() => venue.value.businessHours || '08:00 - 22:30')
const displayHourlyPrice = computed(() => {
  const price = Number(venue.value.hourlyPrice || 0)
  return Number.isFinite(price) ? price.toFixed(0) : '0'
})

const heroIntro = computed(() => {
  if (venue.value.description) {
    return venue.value.description
  }
  return venue.value.status === 1
    ? '当前页面展示场馆的基础信息、营业时间和预约入口，具体可约时段以下单页为准。'
    : '场馆当前暂不可在线预约，你仍可查看基础信息并联系场馆确认开放时间。'
})

const spotlightCards = computed(() => [
  {
    icon: 'location',
    label: '场馆位置',
    value: venue.value.location || '暂无地址信息',
    desc: '可使用复制地址功能保存或转发位置信息'
  },
  {
    icon: 'person',
    label: '联系人',
    value: venue.value.contactPerson || '场馆管理员',
    desc: venue.value.contactPhone || '暂无联系电话'
  },
  {
    icon: 'refreshtime',
    label: '营业时段',
    value: openHoursText.value,
    desc: '营业时间以场馆当前配置为准'
  },
  {
    icon: 'wallet',
    label: '价格体系',
    value: `¥${displayHourlyPrice.value} / 小时起`,
    desc: '实际金额以预约页实时计算结果为准'
  }
])

const bookingJourney = computed(() => [
  {
    step: '01',
    title: '查看场馆信息',
    copy: '先确认位置、营业时间和联系电话，了解场馆当前基础信息。'
  },
  {
    step: '02',
    title: '进入预约页选场地',
    copy: '在预约页查看场地、时段和金额，以下单页的实时结果为准。'
  },
  {
    step: '03',
    title: '完成预约后查看订单',
    copy: '下单成功后可回到我的预约查看订单状态、时间和费用明细。'
  }
])

const servicePromise = computed(() => {
  return venue.value.status === 1
    ? '下一步会进入真实预约页查看可预约时段、场地和订单金额，最终以下单结果为准。'
    : '当前场馆暂未开放在线预约，你仍可保存地址和联系电话，后续开放时再继续下单。'
})

function mapVenue(result: VenueItem): VenueDetailVm {
  return {
    id: result.id,
    name: result.venueName,
    venueImage: result.venueImage || '',
    location: result.address || '',
    contactPhone: result.contactPhone || '',
    contactPerson: result.contactPerson || '',
    businessHours: result.businessHours || '',
    description: result.description || '',
    status: result.status ?? 1,
    hourlyPrice: Number(result.hourlyPrice || 0)
  }
}

async function loadVenueDetail() {
  if (!venueId.value) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const result = await getVenueDetail(venueId.value)
    venue.value = mapVenue(result)
  } catch (error) {
    console.error('加载场馆详情失败:', error)
    uni.showToast({ title: '加载场馆详情失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onLoad(async (options?: Record<string, string | undefined>) => {
  if (options?.id) {
    venueId.value = Number(options.id)
  }
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  await loadVenueDetail()
})

function handleBack() {
  safeNavigateBack()
}

function handleCall(phone?: string) {
  if (!phone) {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({ phoneNumber: phone })
}

function handleBook() {
  if (!venue.value.id || venue.value.status !== 1) {
    uni.showToast({ title: venue.value.status === 1 ? '场馆信息异常' : '场馆暂不可预约', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `/pages/venue/booking?venueId=${venue.value.id}` })
}

function handleLocation() {
  if (!venue.value.location) {
    uni.showToast({ title: '暂无场馆地址', icon: 'none' })
    return
  }
  uni.setClipboardData({
    data: venue.value.location,
    success: () => {
      uni.showToast({ title: '地址已复制', icon: 'none' })
    }
  })
}

function handleHistory() {
  uni.navigateTo({ url: '/pages/booking/list' })
}
</script>

<style lang="scss" scoped>
.venue-detail-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.18), transparent 24%),
    linear-gradient(180deg, #fff8f2 0%, #f9f6f2 18%, #f6f3ef 100%);
  color: #1a1c1c;
}

.topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background: rgba(250, 246, 241, 0.78);
  backdrop-filter: blur(24rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 28rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.07);

  &:active {
    transform: scale(0.96);
  }

  &.ghost {
    background: rgba(255, 241, 234, 0.92);
  }
}

.topbar-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.brand-word {
  font-size: 28rpx;
  line-height: 1;
  font-weight: 900;
  letter-spacing: 1.4rpx;
  color: #8f3300;
}

.brand-sub {
  font-size: 18rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  color: #8e7164;
}

.page-scroll {
  box-sizing: border-box;
}

.page-body {
  padding: 0 24rpx 0;
}

.hero-shell {
  position: relative;
  height: 760rpx;
  border-radius: 0 0 42rpx 42rpx;
  overflow: hidden;
  box-shadow: 0 26rpx 50rpx rgba(26, 28, 28, 0.1);
}

.hero-image,
.hero-placeholder {
  width: 100%;
  height: 100%;
}

.hero-placeholder {
  background: linear-gradient(135deg, #f3ede8 0%, #ddd3cc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(26, 28, 28, 0.1) 0%, rgba(26, 28, 28, 0.78) 68%, rgba(26, 28, 28, 0.92) 100%);
}

.hero-noise {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.08), transparent 45%),
    radial-gradient(circle at 18% 16%, rgba(255, 255, 255, 0.24), transparent 20%);
}

.hero-content {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: 38rpx;
  z-index: 2;
}

.hero-badge-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.hero-badge {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 2rpx;

  &.accent {
    color: #561d00;
    background: #ff6600;
  }

  &.glass {
    color: #ffffff;
    background: rgba(255, 255, 255, 0.14);
    backdrop-filter: blur(14rpx);
  }
}

.hero-title {
  display: block;
  font-size: 60rpx;
  line-height: 1.02;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -1rpx;
}

.hero-subtitle {
  display: block;
  margin-top: 20rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: rgba(255, 247, 240, 0.92);
}

.hero-meta {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-top: 22rpx;
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

.summary-ribbon {
  margin-top: -48rpx;
  position: relative;
  z-index: 3;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.summary-card {
  min-height: 176rpx;
  padding: 24rpx 22rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 18rpx 36rpx rgba(26, 28, 28, 0.06);
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  &.warm {
    background: linear-gradient(135deg, #fff0e3 0%, #fffaf6 100%);
  }

  &.dark {
    background: linear-gradient(135deg, #241816 0%, #47312b 100%);
  }
}

.summary-label {
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2.6rpx;
  text-transform: uppercase;

  &.light {
    color: rgba(255, 236, 225, 0.78);
  }
}

.summary-value-row {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.summary-unit,
.summary-tail {
  font-size: 20rpx;
  color: #5f5e5e;
}

.summary-value {
  font-size: 46rpx;
  font-weight: 900;
  color: #9e3700;
  line-height: 1;
}

.summary-mini {
  font-size: 24rpx;
  font-weight: 800;
  color: #1a1c1c;
  line-height: 1.45;

  &.light {
    color: #ffffff;
  }
}

.summary-foot {
  font-size: 20rpx;
  line-height: 1.55;
  color: #6b625c;

  &.light {
    color: rgba(255, 241, 234, 0.76);
  }
}

.section-card {
  margin-top: 28rpx;
  padding: 30rpx 28rpx;
  border-radius: 34rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16rpx 36rpx rgba(26, 28, 28, 0.05);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 24rpx;

  &.compact {
    margin-bottom: 20rpx;
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

.section-badge {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: #ffefe7;
  color: #a33e00;
  font-size: 20rpx;
  font-weight: 800;

  &.offline {
    background: #eeeeee;
    color: #636262;
  }
}

.spotlight-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.spotlight-item {
  min-height: 228rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fbf8f5 0%, #f6f0eb 100%);
  display: flex;
  flex-direction: column;
}

.spotlight-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  background: rgba(255, 102, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
}

.spotlight-label {
  margin-top: 16rpx;
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
}

.spotlight-value {
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 1.45;
  font-weight: 800;
  color: #1a1c1c;
}

.spotlight-desc {
  margin-top: auto;
  padding-top: 18rpx;
  font-size: 20rpx;
  line-height: 1.55;
  color: #6b625c;
}

.statement-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 248, 242, 0.98) 100%);
}

.feature-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.feature-pill {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 18rpx;
  border-radius: 999rpx;
  background: #f3f0ed;
}

.feature-text {
  font-size: 20rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.statement-panel {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #2e1f1b 0%, #4d342d 100%);
}

.statement-title {
  display: block;
  font-size: 22rpx;
  font-weight: 900;
  color: #ffd8c2;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.statement-copy {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.8;
  color: rgba(255, 245, 237, 0.92);
}

.insight-grid {
  margin-top: 18rpx;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
}

.insight-card {
  min-height: 182rpx;
  padding: 22rpx 18rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.9);
  border: 2rpx solid rgba(255, 102, 0, 0.08);
}

.insight-title {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.insight-value {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  line-height: 1.25;
  font-weight: 900;
  color: #1a1c1c;
}

.insight-copy {
  display: block;
  margin-top: 12rpx;
  font-size: 20rpx;
  line-height: 1.55;
  color: #6b625c;
}

.program-card {
  background: linear-gradient(180deg, #fffdfb 0%, #fff5ec 100%);
}

.journey-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.journey-item {
  display: flex;
  gap: 18rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.9);
}

.journey-index {
  width: 68rpx;
  height: 68rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 900;
  color: #ffffff;
  flex-shrink: 0;
}

.journey-main {
  flex: 1;
}

.journey-title {
  display: block;
  font-size: 26rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.journey-copy {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.65;
  color: #5f5e5e;
}

.action-card {
  background: linear-gradient(180deg, #ffffff 0%, #fff9f4 100%);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.action-tile {
  min-height: 214rpx;
  padding: 22rpx 18rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.92);
  border: 2rpx solid rgba(255, 102, 0, 0.08);
  display: flex;
  flex-direction: column;

  &:active {
    transform: scale(0.98);
  }
}

.action-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  background: rgba(255, 102, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;

  &.filled {
    background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  }
}

.action-name {
  margin-top: 18rpx;
  font-size: 24rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.action-copy {
  margin-top: 10rpx;
  font-size: 20rpx;
  line-height: 1.6;
  color: #6b625c;
}

.promise-card {
  background: transparent;
  padding: 0;
  box-shadow: none;
}

.promise-shell {
  padding: 30rpx 30rpx 32rpx;
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

.promise-cta {
  margin-top: 24rpx;
  height: 96rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  border: 2rpx solid rgba(255, 255, 255, 0.16);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;

  &:active {
    transform: scale(0.98);
  }
}

.promise-cta-text {
  font-size: 28rpx;
  font-weight: 900;
  color: #ffffff;
}

.scroll-buffer {
  height: calc(196rpx + env(safe-area-inset-bottom));
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 40;
  padding-bottom: env(safe-area-inset-bottom);
}

.bottom-glass {
  position: absolute;
  inset: 0;
  background: rgba(255, 252, 248, 0.88);
  backdrop-filter: blur(24rpx);
  box-shadow: 0 -16rpx 40rpx rgba(26, 28, 28, 0.06);
}

.bottom-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 22rpx 24rpx 20rpx;
}

.bottom-price {
  display: flex;
  flex-direction: column;
}

.bottom-label {
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.bottom-amount-row {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  margin-top: 8rpx;
}

.bottom-currency,
.bottom-unit {
  font-size: 22rpx;
  color: #5f5e5e;
}

.bottom-amount {
  font-size: 48rpx;
  line-height: 1;
  font-weight: 900;
  color: #1a1c1c;
}

.bottom-foot {
  margin-top: 6rpx;
  font-size: 20rpx;
  line-height: 1.5;
  color: #6b625c;
}

.bottom-cta {
  min-width: 274rpx;
  padding: 24rpx 30rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 14rpx 30rpx rgba(255, 102, 0, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;

  &:active {
    transform: scale(0.98);
  }

  &.disabled {
    opacity: 0.55;
  }
}

.bottom-cta-text {
  font-size: 28rpx;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: 1rpx;
}

.state-panel {
  min-height: 60vh;
  padding: 140rpx 48rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.state-spinner {
  width: 60rpx;
  height: 60rpx;
  border-radius: 999rpx;
  border: 6rpx solid #e8e8e8;
  border-top-color: #ff6600;
  animation: spin 0.8s linear infinite;
}

.state-title {
  margin-top: 24rpx;
  font-size: 32rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.state-desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #6b625c;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
