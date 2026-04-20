<template>
  <view class="venue-detail-page">
    <view class="topbar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="topbar-inner">
        <view class="icon-btn" @click="handleBack">
          <uni-icons type="left" size="20" color="#ff6600" />
        </view>
        <view class="topbar-brand">
          <text class="brand-word">KINETIC</text>
          <text class="brand-sub">VENUE PROFILE</text>
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
        <text class="state-desc">正在同步场馆信息与预约资料</text>
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
          <view class="hero-content">
            <view class="hero-tags">
              <text class="hero-pill">{{ venue.status === 1 ? 'BOOKING OPEN' : 'TEMP CLOSED' }}</text>
              <text class="hero-pill soft">{{ openHoursText }}</text>
            </view>
            <text class="hero-title">{{ venue.name || '场馆详情' }}</text>
            <view class="hero-meta">
              <view class="hero-meta-item">
                <uni-icons type="location" size="14" color="#ffffff" />
                <text class="hero-meta-text">{{ venue.location || '暂无地址' }}</text>
              </view>
              <view class="hero-meta-item">
                <uni-icons type="star-filled" size="14" color="#ffffff" />
                <text class="hero-meta-text">4.9 / 120+ 条评价</text>
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
          </view>
          <view class="summary-card">
            <text class="summary-label">Business Time</text>
            <text class="summary-mini">{{ openHoursText }}</text>
          </view>
          <view class="summary-card">
            <text class="summary-label">Contact</text>
            <text class="summary-mini">{{ venue.contactPerson || '场馆前台' }}</text>
          </view>
        </view>

        <view class="section-card spotlight-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Venue Snapshot</text>
              <text class="section-title">预约前快速了解</text>
            </view>
            <text class="section-badge" :class="{ offline: venue.status !== 1 }">
              {{ venue.status === 1 ? '可预约' : '暂停营业' }}
            </text>
          </view>

          <view class="spotlight-grid">
            <view class="spotlight-item">
              <view class="spotlight-icon">
                <uni-icons type="location" size="18" color="#ff6600" />
              </view>
              <text class="spotlight-label">场馆位置</text>
              <text class="spotlight-value">{{ venue.location || '暂无地址信息' }}</text>
            </view>
            <view class="spotlight-item">
              <view class="spotlight-icon">
                <uni-icons type="person" size="18" color="#ff6600" />
              </view>
              <text class="spotlight-label">联系人</text>
              <text class="spotlight-value">{{ venue.contactPerson || '场馆管理员' }}</text>
            </view>
            <view class="spotlight-item">
              <view class="spotlight-icon">
                <uni-icons type="phone" size="18" color="#ff6600" />
              </view>
              <text class="spotlight-label">联系电话</text>
              <text class="spotlight-value">{{ venue.contactPhone || '暂无联系电话' }}</text>
            </view>
            <view class="spotlight-item">
              <view class="spotlight-icon">
                <uni-icons type="refreshtime" size="18" color="#ff6600" />
              </view>
              <text class="spotlight-label">营业时段</text>
              <text class="spotlight-value">{{ openHoursText }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Highlights</text>
              <text class="section-title">场馆亮点与服务</text>
            </view>
          </view>
          <view class="feature-pills">
            <view v-for="item in amenityList" :key="item.label" class="feature-pill">
              <uni-icons :type="item.icon" size="16" color="#1a1c1c" />
              <text class="feature-text">{{ item.label }}</text>
            </view>
          </view>
          <view class="feature-panel">
            <text class="feature-panel-title">Professional Atmosphere</text>
            <text class="feature-panel-text">
              {{ descriptionText }}
            </text>
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
              <text class="action-copy">咨询营业时段与场地情况</text>
            </view>
            <view class="action-tile" @click="handleHistory">
              <view class="action-icon">
                <uni-icons type="calendar" size="18" color="#ff6600" />
              </view>
              <text class="action-name">查看预约</text>
              <text class="action-copy">快速返回你的历史预约列表</text>
            </view>
            <view class="action-tile" @click="handleShare">
              <view class="action-icon">
                <uni-icons type="redo" size="18" color="#ff6600" />
              </view>
              <text class="action-name">分享场馆</text>
              <text class="action-copy">把这家场馆发给球友一起约场</text>
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
          <text class="bottom-label">Starting Price</text>
          <view class="bottom-amount-row">
            <text class="bottom-currency">¥</text>
            <text class="bottom-amount">{{ displayHourlyPrice }}</text>
            <text class="bottom-unit">/ 小时</text>
          </view>
        </view>
        <view class="bottom-cta" @click="handleBook">
          <text class="bottom-cta-text">立即预约</text>
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

const amenityList = [
  { icon: 'sound', label: 'Free Wifi' },
  { icon: 'location', label: 'Parking' },
  { icon: 'compose', label: 'Locker Room' },
  { icon: 'staff', label: 'Pro Support' },
  { icon: 'fire', label: 'Warm-up Zone' },
  { icon: 'medal', label: 'Match Standard' }
] as const

const descriptionText = computed(() => {
  return (
    venue.value.description ||
    '场馆提供标准羽毛球训练与比赛环境，适合日常约场、朋友对打、训练提升与社群活动。整体空间明亮，动线清晰，适合高频复购型用户。'
  )
})

const openHoursText = computed(() => venue.value.businessHours || '08:00 - 22:30')
const displayHourlyPrice = computed(() => {
  const price = Number(venue.value.hourlyPrice || 0)
  return Number.isFinite(price) ? price.toFixed(0) : '0'
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

function handleShare() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

function handleHistory() {
  uni.navigateTo({ url: '/pages/booking/list' })
}
</script>

<style lang="scss" scoped>
.venue-detail-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.12), transparent 28%),
    #f9f9f9;
  color: #1a1c1c;
}

.topbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background: rgba(249, 249, 249, 0.78);
  backdrop-filter: blur(20rpx);
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
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.06);

  &:active {
    transform: scale(0.96);
  }

  &.ghost {
    background: rgba(255, 241, 234, 0.9);
  }
}

.topbar-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.brand-word {
  font-size: 30rpx;
  line-height: 1;
  font-weight: 900;
  letter-spacing: 1rpx;
  color: #a33e00;
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
  height: 720rpx;
  border-radius: 0 0 36rpx 36rpx;
  overflow: hidden;
  box-shadow: 0 18rpx 40rpx rgba(26, 28, 28, 0.08);
}

.hero-image,
.hero-placeholder {
  width: 100%;
  height: 100%;
}

.hero-placeholder {
  background: linear-gradient(135deg, #f3f3f3 0%, #e2e2e2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(26, 28, 28, 0.12) 0%, rgba(26, 28, 28, 0.78) 100%);
}

.hero-content {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: 34rpx;
  z-index: 2;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 18rpx;
}

.hero-pill {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 2rpx;
  color: #561d00;
  background: #ff6600;

  &.soft {
    color: #ffffff;
    background: rgba(255, 255, 255, 0.16);
    backdrop-filter: blur(14rpx);
  }
}

.hero-title {
  display: block;
  font-size: 58rpx;
  line-height: 1.08;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -1rpx;
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
  color: rgba(255, 255, 255, 0.92);
}

.summary-ribbon {
  margin-top: -38rpx;
  position: relative;
  z-index: 3;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.summary-card {
  min-height: 138rpx;
  padding: 22rpx 20rpx;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 16rpx 32rpx rgba(26, 28, 28, 0.06);
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  &.warm {
    background: linear-gradient(135deg, #fff0e8 0%, #ffffff 100%);
  }
}

.summary-label {
  font-size: 18rpx;
  font-weight: 800;
  color: #8e7164;
  letter-spacing: 2rpx;
  text-transform: uppercase;
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
  color: #a33e00;
  line-height: 1;
}

.summary-mini {
  font-size: 24rpx;
  font-weight: 700;
  color: #1a1c1c;
  line-height: 1.35;
}

.section-card {
  margin-top: 28rpx;
  padding: 28rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 16rpx 36rpx rgba(26, 28, 28, 0.05);
}

.spotlight-card {
  margin-top: 24rpx;
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
  min-height: 196rpx;
  padding: 22rpx;
  border-radius: 24rpx;
  background: #f8f5f3;
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
  line-height: 1.5;
  font-weight: 700;
  color: #1a1c1c;
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
  background: #f3f3f3;
}

.feature-text {
  font-size: 20rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.feature-panel {
  margin-top: 24rpx;
  padding: 26rpx;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #fff8f3 0%, #f3f3f3 100%);
  border-left: 8rpx solid #ff6600;
}

.feature-panel-title {
  display: block;
  font-size: 22rpx;
  font-weight: 900;
  color: #a33e00;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.feature-panel-text {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.8;
  color: #5a4136;
}

.action-card {
  background: linear-gradient(180deg, #ffffff 0%, #fffaf6 100%);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16rpx;
}

.action-tile {
  min-height: 206rpx;
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

.scroll-buffer {
  height: calc(180rpx + env(safe-area-inset-bottom));
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
  background: rgba(255, 255, 255, 0.86);
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

.bottom-cta {
  min-width: 260rpx;
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
