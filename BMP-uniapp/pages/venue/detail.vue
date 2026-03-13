<template>
  <MobileLayout>
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">场馆详情</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="venue-banner">
        <view class="banner-image">
          <text class="image-placeholder">场馆图片</text>
        </view>
        <view class="banner-content">
          <view class="venue-main">
            <text class="venue-name">{{ venue.name }}</text>
            <text class="venue-price">¥{{ venue.price }}<text class="price-unit">/小时</text></text>
          </view>
          <view class="venue-meta">
            <view class="meta-item">
              <uni-icons type="location" size="16" color="#475569"></uni-icons>
              <text class="meta-text">{{ venue.location }}</text>
            </view>
            <view class="meta-item" v-if="venue.contactPhone">
              <uni-icons type="phone" size="16" color="#475569"></uni-icons>
              <text class="meta-text">{{ venue.contactPhone }}</text>
            </view>
            <view class="meta-item" v-if="venue.businessHours">
              <uni-icons type="calendar" size="16" color="#475569"></uni-icons>
              <text class="meta-text">{{ venue.businessHours }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section info-section">
        <text class="section-title">场馆信息</text>
        <view class="info-item" v-if="venue.description">
          <text class="info-label">场馆介绍</text>
          <text class="info-value">{{ venue.description }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="action-bar">
      <button class="action-btn book-btn" @click="handleBook">
        <uni-icons type="calendar" size="18" color="#ffffff" class="btn-icon"></uni-icons>
        <text class="btn-text">立即预订</text>
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getVenueDetail } from '@/api/venue'
import { safeNavigateBack } from '@/utils/navigation'

const venueId = ref<number>(0)
const venue = ref<any>({
  id: 0,
  name: '',
  location: '',
  price: 50,
  contactPhone: '',
  businessHours: '',
  description: ''
})
const userStore = useUserStore()

onLoad((options) => {
  if (options.id) {
    venueId.value = Number(options.id)
  }
})

const loadVenueDetail = async () => {
  try {
    const result = await getVenueDetail(venueId.value)
    venue.value = {
      id: result.id,
      name: result.venueName,
      location: result.address || '',
      price: result.hourlyPrice ?? 50,
      contactPhone: result.contactPhone || '',
      businessHours: result.businessHours || '',
      description: result.description || ''
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

const handleBook = () => {
  uni.navigateTo({
    url: `/pages/booking/create?venueId=${venue.value.id}`
  })
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
@import '@/styles/common.scss';

.header {
  background-color: #ffffff;
  padding: 20rpx 28rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  border-bottom: 1rpx solid #e6e6e6;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-icon {
  font-size: 40rpx;
  color: #333333;
  font-weight: bold;
  width: 56rpx;
}

.header-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
  text-align: center;
}

.header-placeholder {
  width: 56rpx;
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
}

.venue-banner {
  background-color: #ffffff;
  margin-bottom: 20rpx;
}

.banner-image {
  height: 240rpx;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(153, 153, 153, 0.3);
  font-size: 20rpx;
}

.banner-content {
  padding: 28rpx;
}

.venue-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.venue-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
}

.venue-price {
  font-size: 28rpx;
  font-weight: bold;
  color: #ef4444;
}

.price-unit {
  font-size: 20rpx;
  font-weight: normal;
  color: #999999;
}

.venue-meta {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-text {
  font-size: 22rpx;
  color: #475569;
  flex: 1;
}

.section {
  background-color: #ffffff;
  margin-bottom: 20rpx;
  padding: 28rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
  display: block;
}

.info-item {
  padding: 16rpx 0;
}

.info-label {
  font-size: 24rpx;
  color: #999999;
  display: block;
  margin-bottom: 8rpx;
}

.info-value {
  font-size: 24rpx;
  color: #333333;
  line-height: 1.6;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  height: 120rpx;
  background-color: #ffffff;
  border-top: 1rpx solid #e6e6e6;
  padding: 0 28rpx;
  box-sizing: border-box;
  align-items: center;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
  background-color: #3cc51f;
  color: #ffffff;

  .btn-icon {
    margin-right: 8rpx;
    font-size: 28rpx;
  }
}
</style>
