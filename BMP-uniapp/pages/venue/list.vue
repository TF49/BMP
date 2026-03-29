<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">场地预订</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Date & Filter -->
    <view class="date-filter">
      <scroll-view class="date-scroll" scroll-x>
        <view class="date-list">
          <view 
            v-for="(date, idx) in dateList" 
            :key="idx"
            class="date-item"
            :class="{ active: idx === selectedDateIndex }"
            @click="selectDate(idx)"
          >
            {{ date }}
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="sort-bar">
      <view class="sort-options">
        <text class="sort-item active">综合排序</text>
        <text class="sort-item">离我最近</text>
        <text class="sort-item">价格最低</text>
      </view>
      <view class="filter-btn" @click="handleFilter">
        <text>筛选</text>
        <uni-icons type="gear" size="16" color="#475569" class="filter-icon"></uni-icons>
      </view>
    </view>

    <!-- Venue List -->
    <view class="venue-list">
      <view 
        v-for="(venue, index) in venueList" 
        :key="index"
        class="venue-card"
        @click="handleVenueClick(venue)"
      >
        <view class="venue-image-wrapper">
          <view class="venue-image">
            <text class="image-placeholder">Venue Photo Placeholder</text>
          </view>
          <view class="venue-rating">
            <uni-icons type="star-filled" size="14" color="#f59e0b"></uni-icons>
            <text class="rating-text">4.8</text>
          </view>
        </view>
        <view class="venue-content">
          <view class="venue-header">
            <text class="venue-name">{{ venue.name }}</text>
            <text class="venue-price">¥{{ venue.price }}<text class="price-unit">/h</text></text>
          </view>
          <view class="venue-location">
            <uni-icons type="location" size="14" color="#475569"></uni-icons>
            <text>{{ venue.location }} · {{ venue.distance }}</text>
          </view>
          <view class="venue-tags">
            <text 
              v-for="(tag, tagIndex) in venue.tags" 
              :key="tagIndex"
              class="venue-tag"
              :class="tag.class"
            >
              {{ tag.text }}
            </text>
          </view>
          <view class="venue-footer">
            <view class="time-slots">
              <view 
                v-for="(slot, slotIndex) in venue.timeSlots" 
                :key="slotIndex"
                class="time-slot"
              >
                {{ slot }}
              </view>
              <view class="more-slots">
                <text>+{{ venue.moreSlots }}</text>
              </view>
            </view>
            <button class="book-btn" @click.stop="handleBook(venue)">预订</button>
          </view>
        </view>
      </view>
    </view>
    <!-- Custom BottomNavBar Shell -->
    <CustomTabBar :current="1" />
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { getVenueList } from '@/api/venue'
import { safeNavigateBack } from '@/utils/navigation'

const selectedDateIndex = ref(0)
const dateList = ref(['今天 10.24', '明天 10.25', '周六 10.26', '周日 10.27', '周一 10.28'])
const venueList = ref<any[]>([])
const loading = ref(false)
const userStore = useUserStore()

// 加载场馆列表数据
const loadVenueList = async () => {
  loading.value = true
  try {
    const result = await getVenueList({
      page: 1,
      size: 20,
      status: 1 // 只获取营业中的场馆
    })
    
    // 将API数据转换为页面所需格式
    venueList.value = result.data.map((venue: any) => ({
      id: venue.id,
      name: venue.venueName,
      location: venue.address || venue.location,
      distance: venue.distance || '未知距离',
      price: venue.hourlyPrice || venue.price || 50,
      rating: venue.rating || 4.5,
      tags: venue.tags || [
        { text: '空调', class: 'tag-green' },
        { text: '停车', class: 'tag-orange' }
      ],
      timeSlots: venue.availableSlots || ['18:00', '19:00', '20:00'],
      moreSlots: venue.moreSlots || 5
    }))
  } catch (error) {
    console.error('加载场馆列表失败:', error)
    uni.showToast({
      title: '加载场馆列表失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const selectDate = (index: number) => {
  selectedDateIndex.value = index
}

const handleBack = () => {
  safeNavigateBack()
}

const handleFilter = () => {
  uni.navigateTo({
    url: '/pages/venue/filter'
  })
}

const handleVenueClick = (venue: any) => {
  uni.navigateTo({
    url: `/pages/venue/detail?id=${venue.id}`
  })
}

const handleBook = (venue: any) => {
  uni.navigateTo({
    url: `/pages/booking/create?venueId=${venue.id}`
  })
}

// 页面加载时获取数据
onMounted(async () => {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    // 未登录用户重定向到登录页
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  await loadVenueList()
})

// 启用下拉刷新
onPullDownRefresh(() => {
  loadVenueList().finally(() => {
    uni.stopPullDownRefresh()
  })
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

.date-filter {
  background-color: #ffffff;
  padding: 18rpx 28rpx;
  border-bottom: 1rpx solid #e6e6e6;
}

.date-scroll {
  white-space: nowrap;
}

.date-list {
  display: flex;
  gap: 12rpx;
}

.date-item {
  flex-shrink: 0;
  padding: 10rpx 24rpx;
  border-radius: 9999rpx;
  font-size: 22rpx;
  font-weight: 500;
  background-color: #f5f5f5;
  color: #999999;
  transition: all 0.3s;
}

.date-item.active {
  background-color: #3cc51f;
  color: #ffffff;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.sort-bar {
  background-color: #ffffff;
  padding: 12rpx 28rpx;
  border-bottom: 1rpx solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 22rpx;
}

.sort-options {
  display: flex;
  gap: 28rpx;
}

.sort-item {
  color: #999999;
  
  &.active {
    color: #3cc51f;
    font-weight: bold;
  }
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  color: $text-color-secondary;
}

.filter-icon {
  flex-shrink: 0;
}

.venue-list {
  padding: 24rpx 28rpx;
  padding-bottom: 180rpx;
}

.venue-card {
  background-color: #ffffff;
  border-radius: 18rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  border: 1rpx solid $border-color;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.venue-card:active {
  transform: translateY(2rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.venue-image-wrapper {
  height: 200rpx;
  position: relative;
  background-color: #f5f5f5;
}

.venue-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(153, 153, 153, 0.3);
  font-size: 20rpx;
}

.venue-rating {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  background-color: rgba(0, 0, 0, 0.5);
  color: #ffffff;
  font-size: 18rpx;
  padding: 6rpx 12rpx;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.star-icon {
  font-size: 18rpx;
  color: #ffc107;
}

.venue-content {
  padding: 18rpx 20rpx;
}

.venue-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10rpx;
}

.venue-name {
  font-size: 24rpx;
  font-weight: bold;
  color: $text-color;
}

.venue-price {
  font-size: 24rpx;
  font-weight: bold;
  color: #ef4444;
}

.price-unit {
  font-size: 20rpx;
  font-weight: normal;
  color: #475569;
}

.venue-location {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 20rpx;
  color: #475569;
  margin-bottom: 12rpx;
}

.venue-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 14rpx;
}

.venue-tag {
  font-size: 18rpx;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  
  &.tag-blue {
    background-color: #e3f2fd;
    color: #2196f3;
  }
  
  &.tag-green {
    background-color: #e8f5e9;
    color: #3cc51f;
  }
  
  &.tag-orange {
    background-color: #fff3e0;
    color: #ff9800;
  }
}

.venue-footer {
  border-top: 1rpx dashed #e6e6e6;
  padding-top: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-slots {
  display: flex;
  gap: 6rpx;
  align-items: center;
}

.time-slot {
  width: 80rpx;
  height: 40rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  color: #999999;
  background-color: #f9fafb;
}

.more-slots {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999999;
  font-size: 18rpx;
}

.book-btn {
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: bold;
  padding: 8rpx 24rpx;
  border-radius: 9999rpx;
  border: none;
  box-shadow: 0 2rpx 6rpx rgba(60, 197, 31, 0.2);
}
</style>
