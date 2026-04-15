<template>
  <MobileLayout>
    <view class="stringing-list">
      <!-- Header -->
      <view class="header">
        <text class="header-title">穿线服务</text>
      </view>
      
      <!-- Filter Tabs -->
      <view class="filter-tabs">
        <view 
          v-for="(tab, index) in tabs" 
          :key="index"
          class="tab-item"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          {{ tab }}
        </view>
      </view>
      
      <!-- Search Box (Manager Only) -->
      <view v-if="isManager" class="search-box">
        <input 
          v-model="searchKeyword"
          class="search-input"
          type="text"
          placeholder="搜索会员姓名"
          @input="handleSearch"
        />
      </view>

      <!-- Content -->
      <view class="stringing-content">
        <!-- Loading State (Initial Load) -->
        <view v-if="loading && serviceList.length === 0" class="loading-state">
          <view class="loading-spinner"></view>
          <text class="loading-text">加载中...</text>
        </view>
        
        <!-- Empty State -->
        <view v-else-if="serviceList.length === 0 && !loading" class="empty-state">
          <text class="empty-text">暂无穿线服务记录</text>
        </view>
        
        <!-- Service List -->
        <view v-else class="service-items">
          <view 
            v-for="(item, index) in serviceList" 
            :key="index"
            class="service-card glass-card"
            @click="handleServiceClick(item)"
          >
            <!-- Card Header -->
            <view class="service-header">
              <text class="service-no">{{ item.serviceNo }}</text>
              <text 
                class="service-status" 
                :style="{ 
                  backgroundColor: getStatusBgColor(item.status),
                  color: getStatusColor(item.status)
                }"
              >
                {{ getStatusText(item.status) }}
              </text>
            </view>
            
            <!-- Racket Info -->
            <view class="service-info">
              <!-- Member Name (Manager Only) -->
              <view v-if="isManager" class="info-row">
                <text class="info-label">会员：</text>
                <text class="info-value info-member">{{ item.memberName || '-' }}</text>
              </view>
              
              <view class="info-row">
                <text class="info-label">球拍：</text>
                <text class="info-value">{{ item.racketBrand }} {{ item.racketModel }}</text>
              </view>
              
              <!-- String Info -->
              <view class="info-row">
                <text class="info-label">线材：</text>
                <text class="info-value">
                  {{ item.ownString ? '自带线材' : (item.stringName || '-') }}
                </text>
              </view>
              
              <!-- Tension -->
              <view class="info-row">
                <text class="info-label">磅数：</text>
                <text class="info-value">{{ item.tension }}磅</text>
              </view>
              
              <!-- Create Time -->
              <view class="info-row">
                <text class="info-label">时间：</text>
                <text class="info-value info-time">{{ formatTime(item.createTime) }}</text>
              </view>
            </view>
          </view>
          
          <!-- Load More Indicator -->
          <view v-if="hasMore" class="load-more">
            <view v-if="loadingMore" class="loading-more">
              <view class="loading-spinner-small"></view>
              <text class="loading-more-text">加载更多...</text>
            </view>
            <text v-else class="load-more-hint">上拉加载更多</text>
          </view>
          
          <!-- No More Data -->
          <view v-else class="no-more">
            <text class="no-more-text">没有更多数据了</text>
          </view>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getStringingList, type StringingService } from '@/api/stringing'
import { 
  STRINGING_STATUS, 
  STRINGING_STATUS_TEXT, 
  STRINGING_STATUS_COLOR,
  USER_ROLES
} from '@/utils/constant'

// State
const currentTab = ref(0)
const tabs = ref(['全部', '等待穿线', '正在穿线', '已完成', '已取消'])
const serviceList = ref<StringingService[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const hasMore = ref(true)
const userStore = useUserStore()
const searchKeyword = ref('')

// Check if user is manager (PRESIDENT or VENUE_MANAGER)
const isManager = computed(() => {
  return userStore.userRole === 'PRESIDENT' || userStore.userRole === 'VENUE_MANAGER'
})

// Load service list
const loadServiceList = async (refresh = false) => {
  // Prevent duplicate requests
  if (loading.value || loadingMore.value) return
  
  // If no more data and not refreshing, return
  if (!refresh && !hasMore.value) return
  
  if (refresh) {
    page.value = 1
    hasMore.value = true
    loading.value = true
  } else {
    loadingMore.value = true
  }
  
  try {
    // Ensure user is logged in
    if (!userStore.userId) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      uni.redirectTo({
        url: '/pages/login/login'
      })
      return
    }
    
    const params: any = {
      page: page.value,
      size: 10
    }
    
    // For regular users, filter by their memberId
    // For managers (PRESIDENT), show all records
    if (!isManager.value) {
      params.memberId = userStore.userId
    }
    
    // Add search keyword if provided (for managers)
    if (isManager.value && searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    
    // Map tab index to status
    // 0: 全部 (no status filter)
    // 1: 等待穿线 (status = 1)
    // 2: 正在穿线 (status = 2)
    // 3: 已完成 (status = 3)
    // 4: 已取消 (status = 0)
    if (currentTab.value !== 0) {
      const statusMap = [null, STRINGING_STATUS.WAITING, STRINGING_STATUS.IN_PROGRESS, STRINGING_STATUS.COMPLETED, STRINGING_STATUS.CANCELLED]
      params.status = statusMap[currentTab.value]
    }
    
    const result = await getStringingList(params)
    
    if (refresh) {
      serviceList.value = result.data
    } else {
      serviceList.value.push(...result.data)
    }
    
    // Update pagination state
    hasMore.value = page.value < result.pages
    
    // If there's more data, increment page for next load
    if (hasMore.value) {
      page.value++
    }
  } catch (error) {
    console.error('加载穿线服务列表失败:', error)
    uni.showToast({
      title: '加载列表失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// Switch tab
const switchTab = (index: number) => {
  currentTab.value = index
  loadServiceList(true)
}

// Search with debounce (for managers)
let searchTimer: ReturnType<typeof setTimeout> | null = null
const handleSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(() => {
    loadServiceList(true)
  }, 500)
}

// Get status text
const getStatusText = (status: number) => {
  return (STRINGING_STATUS_TEXT as unknown as Record<number, string>)[status] || '未知'
}

// Get status color
const getStatusColor = (status: number) => {
  return (STRINGING_STATUS_COLOR as unknown as Record<number, string>)[status] || '#999999'
}

// Get status background color (lighter version)
const getStatusBgColor = (status: number) => {
  const colorMap: Record<number, string> = {
    [STRINGING_STATUS.CANCELLED]: '#f5f5f5',
    [STRINGING_STATUS.WAITING]: '#fff3e0',
    [STRINGING_STATUS.IN_PROGRESS]: '#e3f2fd',
    [STRINGING_STATUS.COMPLETED]: '#e8f5e9'
  }
  return colorMap[status] || '#f5f5f5'
}

// Format time
const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  
  try {
    const date = new Date(timeStr)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch (error) {
    return timeStr
  }
}

// Handle service click
const handleServiceClick = (item: StringingService) => {
  uni.navigateTo({
    url: `/pages/stringing/detail?id=${item.id}`
  })
}

// Page mounted
onMounted(async () => {
  // Check if user is logged in
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  await loadServiceList(true)
})

// Pull down refresh
onPullDownRefresh(() => {
  loadServiceList(true).finally(() => {
    uni.stopPullDownRefresh()
  })
})

// Reach bottom - load more
onReachBottom(() => {
  loadServiceList(false)
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.stringing-list {
  min-height: 100vh;
  background-color: transparent;
}

.header {
  background-color: #ffffff;
  padding: 24rpx 28rpx;
  text-align: center;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.header-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.filter-tabs {
  background-color: #ffffff;
  padding: 18rpx 28rpx;
  display: flex;
  gap: 18rpx;
  overflow-x: auto;
  border-bottom: 1rpx solid #e6e6e6;
}

.tab-item {
  flex-shrink: 0;
  padding: 10rpx 20rpx;
  font-size: 24rpx;
  color: #999999;
  border-radius: 9999rpx;
  transition: all 0.3s;
  
  &.active {
    background-color: #3cc51f;
    color: #ffffff;
  }
}

.search-box {
  background-color: #ffffff;
  padding: 18rpx 28rpx;
  border-bottom: 1rpx solid #e6e6e6;
}

.search-input {
  width: 100%;
  height: 64rpx;
  padding: 0 24rpx;
  font-size: 24rpx;
  background-color: #f5f7fa;
  border-radius: 32rpx;
  border: none;
}

.stringing-content {
  padding: 24rpx 28rpx;
  padding-bottom: 180rpx;
}

.loading-state {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 160rpx 0;
  gap: 24rpx;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #3cc51f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  font-size: 24rpx;
  color: #999999;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 160rpx 0;
}

.empty-text {
  font-size: 24rpx;
  color: #999999;
}

.service-items {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.service-card {
  border-radius: 24rpx;
  padding: 28rpx;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.service-no {
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
}

.service-status {
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
  font-weight: 500;
}

.service-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 24rpx;
}

.info-label {
  color: #999999;
  min-width: 100rpx;
}

.info-value {
  color: #333333;
  flex: 1;
}

.info-time {
  color: #666666;
}

.info-member {
  color: #3cc51f;
  font-weight: 500;
}

.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32rpx 0;
}

.loading-more {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.loading-spinner-small {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid #f3f3f3;
  border-top: 3rpx solid #3cc51f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-more-text {
  font-size: 24rpx;
  color: #999999;
}

.load-more-hint {
  font-size: 24rpx;
  color: #cccccc;
}

.no-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 32rpx 0;
}

.no-more-text {
  font-size: 24rpx;
  color: #cccccc;
}
</style>
