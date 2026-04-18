<template>
  <view class="venue-detail-page">
    <!-- 与 HTML 原型一致：fixed header bg-neutral-50/70 backdrop-blur -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="back-btn" @click="handleBack">
          <uni-icons type="left" size="22" color="#ea580c"></uni-icons>
        </view>
        <text class="header-title">Venue Details</text>
        <view class="header-spacer"></view>
      </view>
    </view>

    <view v-if="loading" class="state-fill">
      <view class="spinner" />
      <text class="state-text">加载中...</text>
    </view>

    <scroll-view
      v-else-if="venue.id"
      class="main-scroll"
      scroll-y
      :style="{
        marginTop: headerOffsetPx + 'px',
        height: scrollAreaHeight
      }"
      :show-scrollbar="false"
    >
      <view class="hero-wrap">
        <image
          v-if="venue.venueImage"
          class="hero-img"
          :src="resolveImageUrl(venue.venueImage)"
          mode="aspectFill"
        />
        <view v-else class="hero-placeholder">
          <uni-icons type="image" size="32" color="#999999"></uni-icons>
        </view>
        <view class="hero-gradient" />
        <view class="hero-chip-wrap">
          <text class="status-chip" :class="{ 'status-offline': venue.status !== 1 }">
            {{ venue.status === 1 ? '营业中' : '已关闭' }}
          </text>
        </view>
      </view>

      <view class="article">
        <view class="venue-head">
          <text class="venue-name">{{ venue.name }}</text>
          <view class="rating-row">
            <view class="rating-left">
              <uni-icons type="star-filled" size="14" color="#a33e00"></uni-icons>
              <text class="rating-num">4.9</text>
              <text class="rating-reviews">(120+ Reviews)</text>
            </view>
            <view class="rating-dot"></view>
            <text class="rating-grade">Professional Grade</text>
          </view>
        </view>

        <view class="detail-grid">
          <view class="surface-card">
            <view class="card-row">
              <view class="icon-tile">
                <uni-icons type="location" size="22" color="#a33e00"></uni-icons>
              </view>
              <view class="card-main">
                <text class="card-label">地址</text>
                <text class="card-text">{{ venue.location || '暂无地址信息' }}</text>
              </view>
            </view>
          </view>

          <view class="surface-card">
            <view class="card-row">
              <view class="icon-tile">
                <uni-icons type="person" size="22" color="#a33e00"></uni-icons>
              </view>
              <view class="card-main">
                <text class="card-label">联系人 / 电话</text>
                <view class="contact-line">
                  <view>
                    <text class="contact-name">{{ venue.contactPerson || '管理员' }}</text>
                    <text class="contact-phone">{{ venue.contactPhone || '暂无联系电话' }}</text>
                  </view>
                  <view v-if="venue.contactPhone" class="call-btn" @click="handleCall(venue.contactPhone)">
                    <uni-icons type="phone" size="16" color="#ffffff"></uni-icons>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="surface-card">
            <view class="card-row">
              <view class="icon-tile">
                <uni-icons type="refreshtime" size="22" color="#a33e00"></uni-icons>
              </view>
              <view class="card-main">
                <text class="card-label">营业时间</text>
                <text class="card-strong">{{ venue.businessHours || '08:00 - 22:30' }}</text>
                <text class="card-muted">周一至周日 (含法定节假日)</text>
              </view>
            </view>
          </view>

          <view class="surface-card">
            <view class="card-row">
              <view class="icon-tile">
                <uni-icons type="medal" size="22" color="#a33e00"></uni-icons>
              </view>
              <view class="card-main">
                <text class="card-label">场地规格</text>
                <text class="card-strong">24 片标准塑胶场地</text>
                <text class="card-muted">YONEX 官方认证垫层</text>
              </view>
            </view>
          </view>
        </view>

        <view class="desc-block">
          <text class="desc-heading">场馆描述</text>
          <view class="desc-box">
            <text class="desc-body">
              {{
                venue.description ||
                  '奥林匹克羽毛球中心是亚洲领先的羽毛球运动综合体。我们提供符合世界羽联（BWF）标准的专业灯光与地胶系统，有效减少运动员关节冲击。馆内常年保持 24℃ 恒温，并配备专业淋浴间、能量补给站及顶级器材租赁服务。无论是职业训练还是业余休闲，这里都是您挥洒汗水的首选之地。'
              }}
            </text>
          </view>
        </view>

        <view class="amenities">
          <view v-for="(item, idx) in amenityList" :key="idx" class="amenity-pill">
            <uni-icons :type="item.icon" size="18" color="#1a1c1c"></uni-icons>
            <text class="amenity-label">{{ item.label }}</text>
          </view>
        </view>

        <view class="main-pad-bottom"></view>
      </view>
    </scroll-view>

    <view v-else class="state-fill">
      <text class="state-text">场馆不存在</text>
    </view>

    <!-- 与教练详情底栏一致：DELETE / EDIT / SHARE + 起价白 pill + 立即预约 -->
    <view v-if="venue.id" class="bottom-nav">
      <view class="bottom-nav-glass"></view>
      <view class="bottom-nav-content">
        <view class="secondary-actions">
          <view class="action-item" @click="onDelete">
            <uni-icons type="trash" size="18" color="#94a3b8"></uni-icons>
            <text class="action-label">DELETE</text>
          </view>
          <view class="action-item" @click="goEdit">
            <uni-icons type="compose" size="18" color="#94a3b8"></uni-icons>
            <text class="action-label">EDIT</text>
          </view>
        </view>
        <view class="primary-cta" @click="handleBook">
          <view class="price-box">
            <text class="price-label">小时起价</text>
            <view class="price-value">
              <text class="currency">¥</text>
              <text class="amount">{{ displayHourlyPrice }}</text>
              <text class="unit">/ 小时</text>
            </view>
          </view>
          <view class="primary-cta-button">
            <text class="cta-text">立即预约</text>
            <uni-icons type="right" size="16" color="#ffffff"></uni-icons>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import { isPresidentRole } from '@/utils/roleCheck'
import { safeReLaunch } from '@/utils/safeRoute'
import { getVenueInfo, deleteVenue, type VenueItem } from '@/api/president/venue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { safeNavigateBack } from '@/utils/navigation'

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const headerBarPx = 52

const headerOffsetPx = computed(() => statusBarHeight.value + headerBarPx)
const scrollAreaHeight = computed(() => {
  const h = systemInfo.windowHeight || systemInfo.screenHeight
  return `${h - headerOffsetPx.value}px`
})

const userStore = useUserStore()
const id = ref(0)
const loading = ref(true)

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
  { icon: 'loop', label: 'Showers' },
  { icon: 'bolt', label: 'A/C' }
] as const

function emptyVenue(): VenueDetailVm {
  return {
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
  }
}

function mapPresidentVenue(r: VenueItem): VenueDetailVm {
  return {
    id: r.id,
    name: r.venueName,
    venueImage: r.venueImage || '',
    location: r.address || '',
    contactPhone: r.contactPhone || '',
    contactPerson: r.contactPerson || '',
    businessHours: r.businessHours || '',
    description: r.description || '',
    status: r.status ?? 1,
    hourlyPrice: r.hourlyPrice ?? 0
  }
}

const displayHourlyPrice = computed(() => {
  const p = venue.value.hourlyPrice
  if (p == null || Number.isNaN(p)) return '0'
  return Number(p).toFixed(0)
})

async function load() {
  if (!id.value) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const res = await getVenueInfo(id.value)
    venue.value = res ? mapPresidentVenue(res as VenueItem) : emptyVenue()
  } catch {
    venue.value = emptyVenue()
  } finally {
    loading.value = false
  }
}

onLoad((options?: Record<string, string | undefined>) => {
  if (options?.id) id.value = Number(options.id)
  load()
})

onMounted(() => {
  if (!isPresidentRole(userStore.userRole)) {
    safeReLaunch('/pages/index/index', '/pages/index/index')
  }
})

function handleBack() {
  safeNavigateBack(PRESIDENT_PAGES.VENUE_LIST)
}

function goEdit() {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.VENUE_FORM}?id=${id.value}` })
}

function handleCall(phone: string) {
  uni.makePhoneCall({ phoneNumber: phone })
}

function handleBook() {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.BOOKING_FORM}?venueId=${venue.value.id}` })
}

function onDelete() {
  uni.showModal({
    title: '确认删除',
    content: `确定删除场馆「${venue.value.name || ''}」吗？`,
    confirmColor: '#ba1a1a',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteVenue(id.value)
        uni.showToast({ title: '删除成功', icon: 'success' })
        setTimeout(() => safeNavigateBack(PRESIDENT_PAGES.VENUE_LIST), 800)
      } catch (e: any) {
        uni.showToast({ title: e?.message || '删除失败', icon: 'none' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
$bg-surface: #f9f9f9;
$on-surface: #1a1c1c;
$primary: #a33e00;
$on-secondary-container: #636262;
$surface-container-lowest: #ffffff;
$surface-container-low: #f3f3f3;
$secondary-container: #e2dfde;
$on-surface-variant: #5a4136;
$primary-container: #ff6600;
$on-primary-container: #561d00;

.venue-detail-page {
  min-height: 100vh;
  background-color: $bg-surface;
  color: $on-surface;
  font-family: 'Lexend', 'PingFang SC', 'Helvetica Neue', system-ui, sans-serif;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 50;
  background-color: rgba(250, 250, 250, 0.7);
  backdrop-filter: blur(12px);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  box-sizing: border-box;
}

.back-btn {
  width: 24px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  &:active {
    transform: scale(0.96);
  }
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: $on-surface;
}

.header-spacer {
  width: 24px;
  height: 1px;
}

.main-scroll {
  box-sizing: border-box;
}

.state-fill {
  flex: 1;
  min-height: 50vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 120px;
}

.spinner {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  border: 3px solid #e5e7eb;
  border-top-color: #ea580c;
  animation: spin 0.9s linear infinite;
}

.state-text {
  margin-top: 12px;
  font-size: 14px;
  color: #6b7280;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.hero-wrap {
  position: relative;
  width: 100%;
  height: 397px;
  overflow: hidden;
}

.hero-img,
.hero-placeholder {
  width: 100%;
  height: 100%;
}

.hero-placeholder {
  background: #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(25, 26, 26, 0.4) 0%,
    rgba(25, 26, 26, 0) 40%,
    rgba(249, 249, 249, 1) 100%
  );
  pointer-events: none;
}

.hero-chip-wrap {
  position: absolute;
  bottom: 32px;
  left: 24px;
  z-index: 2;
}

.status-chip {
  background-color: $primary-container;
  color: $on-primary-container;
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.2em;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);

  &.status-offline {
    background-color: #e2e2e2;
    color: $on-surface-variant;
    box-shadow: none;
  }
}

.article {
  padding: 0 24px;
  margin-top: -16px;
  position: relative;
  z-index: 10;
}

.venue-head {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 32px;
}

.venue-name {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: $on-surface;
}

.rating-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.rating-left {
  display: flex;
  align-items: center;
  gap: 4px;
  color: $primary;
}

.rating-num {
  font-size: 14px;
  font-weight: 700;
}

.rating-reviews {
  font-size: 12px;
  color: $on-secondary-container;
  margin-left: 4px;
  font-weight: 500;
}

.rating-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #e2e2e2;
}

.rating-grade {
  font-size: 12px;
  font-weight: 500;
  color: $on-secondary-container;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.surface-card {
  background: $surface-container-lowest;
  border-radius: 12px;
  padding: 20px;
  box-sizing: border-box;
}

.card-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.icon-tile {
  padding: 12px;
  background: $surface-container-low;
  border-radius: 8px;
  flex-shrink: 0;
}

.card-main {
  flex: 1;
  min-width: 0;
}

.card-label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  color: $on-secondary-container;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 4px;
}

.card-text {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.6;
  color: $on-surface;
}

.card-strong {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: $on-surface;
}

.card-muted {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: $on-secondary-container;
}

.contact-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.contact-name {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: $on-surface;
}

.contact-phone {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: $on-secondary-container;
}

.call-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: $primary;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  &:active {
    transform: scale(1.05);
  }
}

.desc-block {
  margin-top: 32px;
}

.desc-heading {
  display: block;
  font-size: 10px;
  font-weight: 700;
  color: $on-secondary-container;
  text-transform: uppercase;
  letter-spacing: 0.2em;
  margin-bottom: 16px;
}

.desc-box {
  background: $surface-container-low;
  padding: 24px;
  border-radius: 16px;
  border-left: 4px solid $primary;
  box-sizing: border-box;
}

.desc-body {
  font-size: 14px;
  line-height: 1.75;
  font-weight: 500;
  font-style: italic;
  color: $on-surface-variant;
}

.amenities {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 32px;
}

.amenity-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 999px;
  background: $secondary-container;
  &:active {
    transform: scale(1.02);
  }
}

.amenity-label {
  font-size: 12px;
  font-weight: 700;
  text-transform: capitalize;
  letter-spacing: 0.06em;
  color: $on-surface;
}

/* 底栏加高，与教练详情 pill 一致 */
.main-pad-bottom {
  height: calc(200rpx + env(safe-area-inset-bottom));
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 50;
  padding-bottom: env(safe-area-inset-bottom);
}

.bottom-nav-glass {
  position: absolute;
  inset: 0;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-top-left-radius: 40rpx;
  border-top-right-radius: 40rpx;
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.05);
}

.bottom-nav-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx 16rpx;
  column-gap: 24rpx;
  box-sizing: border-box;
}

.secondary-actions {
  display: flex;
  gap: 20rpx;
  flex-shrink: 0;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  padding: 12rpx;
  transition: all 0.2s;

  &:active {
    background-color: #f1f5f9;
    border-radius: 12rpx;
  }
}

.action-label {
  font-size: 20rpx;
  font-weight: 700;
  margin-top: 4rpx;
  letter-spacing: 1rpx;
}

.primary-cta {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #ffffff;
  padding: 16rpx 20rpx;
  border-radius: 999rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.08);
  flex: 1;
  min-width: 0;
  max-width: 62%;
}

.price-box {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.price-label {
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 2rpx;
}

.price-value {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  margin-top: 4rpx;
}

.currency {
  font-size: 26rpx;
  font-weight: 900;
  color: #0f172a;
}

.amount {
  font-size: 36rpx;
  font-weight: 900;
  color: #0f172a;
}

.unit {
  font-size: 22rpx;
  color: #5f5e5e;
  margin-left: 8rpx;
}

.primary-cta-button {
  flex-shrink: 0;
  background: linear-gradient(45deg, #a33e00 0%, #ff6600 100%);
  padding: 18rpx 32rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.cta-text {
  font-size: 26rpx;
  font-weight: 700;
  color: #ffffff;
}
</style>
