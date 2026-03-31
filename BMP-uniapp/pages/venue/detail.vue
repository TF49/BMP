<template>
  <view class="venue-detail-page">
    <!-- Top Navigation -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-content">
        <view class="back-btn" @click="handleBack">
          <text class="material-symbols-outlined">arrow_back</text>
        </view>
        <text class="header-title">场馆详情</text>
        <view class="spacer"></view>
      </view>
    </view>

    <scroll-view class="main-scroll" scroll-y :style="{ height: scrollHeight }">
      <!-- Hero Section -->
      <view class="hero-section">
        <image 
          class="hero-image" 
          :src="resolveImageUrl(venue.venueImage)" 
          mode="aspectFill"
          v-if="venue.venueImage"
        ></image>
        <view v-else class="hero-image-placeholder">
          <text class="material-symbols-outlined">image</text>
        </view>
        <view class="hero-gradient"></view>
        <!-- Floating Status Chip -->
        <view class="status-chip-wrap">
          <text class="status-chip" :class="{ 'status-offline': venue.status !== 1 }">
            {{ venue.status === 1 ? '营业中' : '已关闭' }}
          </text>
        </view>
      </view>

      <!-- Main Content -->
      <view class="content-body">
        <!-- Header Info -->
        <view class="venue-header-info">
          <view class="name-row">
            <text class="venue-name">{{ venue.name }}</text>
            <view class="venue-price-tag">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ venue.price }}</text>
              <text class="price-unit">/小时</text>
            </view>
          </view>
          <view class="rating-row">
            <view class="rating-box">
              <text class="material-symbols-outlined fill-icon">star</text>
              <text class="rating-text">4.9</text>
              <text class="reviews-text">(120+ Reviews)</text>
            </view>
            <view class="dot-separator"></view>
            <text class="grade-text">PROFESSIONAL GRADE</text>
          </view>
        </view>

        <!-- Bento Grid Info -->
        <view class="info-grid">
          <!-- Address -->
          <view class="info-card">
            <view class="card-icon-box">
              <text class="material-symbols-outlined">location_on</text>
            </view>
            <view class="card-content">
              <text class="card-label">地址</text>
              <text class="card-value">{{ venue.location || '暂无地址信息' }}</text>
            </view>
          </view>

          <!-- Contact -->
          <view class="info-card">
            <view class="card-icon-box">
              <text class="material-symbols-outlined">person</text>
            </view>
            <view class="card-content">
              <text class="card-label">联系人 / 电话</text>
              <view class="contact-row">
                <view class="contact-info">
                  <text class="contact-name">管理员</text>
                  <text class="contact-phone">{{ venue.contactPhone || '暂无联系电话' }}</text>
                </view>
                <view v-if="venue.contactPhone" class="call-btn" @click="handleCall(venue.contactPhone)">
                  <text class="material-symbols-outlined">call</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Hours -->
          <view class="info-card">
            <view class="card-icon-box">
              <text class="material-symbols-outlined">schedule</text>
            </view>
            <view class="card-content">
              <text class="card-label">营业时间</text>
              <text class="card-value-bold">{{ venue.businessHours || '08:00 - 22:30' }}</text>
              <text class="card-subtext">周一至周日 (含法定节假日)</text>
            </view>
          </view>

          <!-- Specs -->
          <view class="info-card">
            <view class="card-icon-box">
              <text class="material-symbols-outlined">sports_tennis</text>
            </view>
            <view class="card-content">
              <text class="card-label">场地规格</text>
              <text class="card-value-bold">专业级标准场地</text>
              <text class="card-subtext">官方认证垫层</text>
            </view>
          </view>
        </view>

        <!-- Description Section -->
        <view class="description-section">
          <text class="section-title">场馆描述</text>
          <view class="description-box">
            <text class="description-text">
              {{ venue.description || '暂无描述信息。本场馆提供专业级的运动体验，配备先进的灯光与通风系统，为运动爱好者提供舒适的竞技环境。' }}
            </text>
          </view>
        </view>

        <!-- Amenities Chips -->
        <view class="amenities-chips">
          <view class="amenity-chip">
            <text class="material-symbols-outlined">wifi</text>
            <text class="amenity-text">FREE WIFI</text>
          </view>
          <view class="amenity-chip">
            <text class="material-symbols-outlined">local_parking</text>
            <text class="amenity-text">PARKING</text>
          </view>
          <view class="amenity-chip">
            <text class="material-symbols-outlined">shower</text>
            <text class="amenity-text">SHOWERS</text>
          </view>
          <view class="amenity-chip">
            <text class="material-symbols-outlined">ac_unit</text>
            <text class="amenity-text">A/C</text>
          </view>
        </view>
        
        <!-- Bottom Spacer -->
        <view class="bottom-safe-spacer"></view>
      </view>
    </scroll-view>

    <!-- Bottom Navigation -->
    <view class="bottom-nav">
      <view class="bottom-nav-glass"></view>
      <view class="bottom-nav-content">
        <view class="secondary-actions">
          <view class="action-item" @click="handleShare">
            <text class="material-symbols-outlined">share</text>
            <text class="action-label">SHARE</text>
          </view>
          <view class="action-item" @click="handleHistory">
            <text class="material-symbols-outlined">calendar_today</text>
            <text class="action-label">HISTORY</text>
          </view>
        </view>
        <view class="primary-cta" @click="handleBook">
          <text class="cta-text">立即预约</text>
          <text class="material-symbols-outlined cta-icon">arrow_forward_ios</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import { getVenueDetail } from '@/api/venue'
import { safeNavigateBack } from '@/utils/navigation'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

// System info for custom header
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)
const scrollHeight = computed(() => `calc(100vh - ${statusBarHeight.value + 44}px)`)

const venueId = ref<number>(0)
const venue = ref<any>({
  id: 0,
  name: '',
  venueImage: '',
  location: '',
  price: 0,
  contactPhone: '',
  businessHours: '',
  description: '',
  status: 1
})
const userStore = useUserStore()

onLoad((options: any) => {
  if (options && options.id) {
    venueId.value = Number(options.id)
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
      contactPhone: result.contactPhone || '',
      businessHours: result.businessHours || '',
      description: result.description || '',
      status: result.status ?? 1
    }
  } catch (error) {
    console.error('加载场馆详情失败:', error)
    uni.showToast({
      title: '加载场馆详情失败',
      icon: 'none'
    })
  }
}

const handleBack = () => {
  safeNavigateBack()
}

const handleCall = (phone: string) => {
  uni.makePhoneCall({
    phoneNumber: phone
  })
}

const handleBook = () => {
  uni.navigateTo({
    url: `/pages/booking/create?venueId=${venue.value.id}`
  })
}

const handleShare = () => {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

const handleHistory = () => {
  uni.navigateTo({ url: '/pages/booking/list' })
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  if (venueId.value) {
    await loadVenueDetail()
  }
})
</script>

<style lang="scss" scoped>
.material-symbols-outlined {
  font-family: 'Material Symbols Outlined';
  font-weight: normal;
  font-style: normal;
  font-size: 24px;
  line-height: 1;
  letter-spacing: normal;
  text-transform: none;
  display: inline-block;
  white-space: nowrap;
  word-wrap: normal;
  direction: ltr;
  -webkit-font-smoothing: antialiased;
}

.fill-icon {
  font-variation-settings: 'FILL' 1;
}

.venue-detail-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  display: flex;
  flex-direction: column;
}

/* Header */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background-color: rgba(249, 249, 249, 0.8);
  backdrop-filter: blur(10px);
}

.header-content {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.back-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a33e00;
  transition: transform 0.2s;
  &:active { transform: scale(0.9); }
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1c1c;
}

.spacer { width: 40px; }

/* Scroll Area */
.main-scroll {
  flex: 1;
  margin-top: calc(env(safe-area-inset-top) + 44px);
}

/* Hero Section */
.hero-section {
  position: relative;
  width: 100%;
  height: 397px;
  overflow: hidden;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-image-placeholder {
  width: 100%;
  height: 100%;
  background-color: #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 48px;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(25, 26, 26, 0.2) 0%, rgba(25, 26, 26, 0) 40%, rgba(249, 249, 249, 1) 100%);
}

.status-chip-wrap {
  position: absolute;
  bottom: 32px;
  left: 24px;
}

.status-chip {
  background-color: #ff6600;
  color: #561d00;
  padding: 6px 16px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  box-shadow: 0 4px 12px rgba(255, 102, 0, 0.3);
  
  &.status-offline {
    background-color: #e2e2e2;
    color: #5a4136;
    box-shadow: none;
  }
}

/* Content Body */
.content-body {
  padding: 0 24px;
  position: relative;
  z-index: 10;
  margin-top: -20px;
}

.venue-header-info {
  margin-bottom: 32px;
}

.name-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.venue-name {
  font-size: 28px;
  font-weight: 700;
  color: #1a1c1c;
  line-height: 1.2;
  flex: 1;
  margin-right: 16px;
}

.venue-price-tag {
  display: flex;
  align-items: baseline;
  color: #a33e00;
}

.price-symbol { font-size: 14px; font-weight: 700; margin-right: 2px; }
.price-value { font-size: 24px; font-weight: 700; }
.price-unit { font-size: 12px; color: #636262; margin-left: 4px; }

.rating-row {
  display: flex;
  align-items: center;
}

.rating-box {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #a33e00;
}

.rating-text { font-size: 14px; font-weight: 700; }
.reviews-text { font-size: 12px; color: #636262; margin-left: 4px; }

.dot-separator {
  width: 4px;
  height: 4px;
  background-color: #e2e2e2;
  border-radius: 50%;
  margin: 0 16px;
}

.grade-text {
  font-size: 12px;
  font-weight: 600;
  color: #636262;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* Bento Grid */
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 32px;
}

.info-card {
  background-color: #ffffff;
  padding: 20px;
  border-radius: 16px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.02);
  transition: transform 0.2s;
  &:active { transform: scale(1.01); }
}

.card-icon-box {
  background-color: #f3f3f3;
  padding: 12px;
  border-radius: 12px;
  color: #a33e00;
  display: flex;
  align-items: center;
  justify-content: center;
  .material-symbols-outlined { font-size: 20px; }
}

.card-content {
  flex: 1;
}

.card-label {
  font-size: 10px;
  font-weight: 700;
  color: #636262;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 4px;
  display: block;
}

.card-value {
  font-size: 14px;
  font-weight: 500;
  color: #1a1c1c;
  line-height: 1.5;
}

.card-value-bold {
  font-size: 14px;
  font-weight: 700;
  color: #1a1c1c;
}

.card-subtext {
  font-size: 12px;
  color: #636262;
  margin-top: 4px;
  display: block;
}

.contact-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contact-info {
  display: flex;
  flex-direction: column;
}

.contact-name { font-size: 14px; font-weight: 700; color: #1a1c1c; }
.contact-phone { font-size: 14px; font-weight: 500; color: #636262; }

.call-btn {
  width: 36px;
  height: 36px;
  background-color: #a33e00;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  &:active { transform: scale(1.1); }
  .material-symbols-outlined { font-size: 16px; }
}

/* Description */
.description-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 10px;
  font-weight: 700;
  color: #636262;
  text-transform: uppercase;
  letter-spacing: 3px;
  margin-bottom: 16px;
  display: block;
}

.description-box {
  background-color: #f3f3f3;
  padding: 24px;
  border-radius: 20px;
  border-left: 4px solid #a33e00;
}

.description-text {
  font-size: 14px;
  color: #5a4136;
  line-height: 1.6;
  font-weight: 500;
  font-style: italic;
}

/* Amenities */
.amenities-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 40px;
}

.amenity-chip {
  background-color: #e2dfde;
  padding: 8px 16px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: transform 0.2s;
  &:active { transform: scale(1.05); }
  .material-symbols-outlined { font-size: 18px; color: #1a1c1c; }
}

.amenity-text {
  font-size: 10px;
  font-weight: 700;
  color: #1a1c1c;
  letter-spacing: 1px;
}

.bottom-safe-spacer { height: 120px; }

/* Bottom Nav */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);
}

.bottom-nav-glass {
  position: absolute;
  inset: 0;
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-top-left-radius: 40px;
  border-top-right-radius: 40px;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.05);
}

.bottom-nav-content {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px 10px;
}

.secondary-actions {
  display: flex;
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  padding: 12px;
  transition: all 0.2s;
  &:active { background-color: #f1f5f9; border-radius: 12px; }
  .material-symbols-outlined { font-size: 24px; }
}

.action-label {
  font-size: 8px;
  font-weight: 700;
  margin-top: 4px;
  letter-spacing: 1px;
}

.primary-cta {
  background: linear-gradient(45deg, #a33e00 0%, #ff6600 100%);
  padding: 16px 32px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 8px 16px rgba(163, 62, 0, 0.2);
  transition: all 0.2s;
  &:active { transform: scale(0.95); opacity: 0.9; }
}

.cta-text {
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 2px;
}

.cta-icon {
  color: #ffffff;
  font-size: 16px;
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.content-body {
  animation: fadeIn 0.6s ease-out forwards;
}
</style>

