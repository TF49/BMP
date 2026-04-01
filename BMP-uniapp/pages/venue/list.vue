<template>
  <MobileLayout>
    <view class="venue-list-page">
      <view class="status-bar-placeholder"></view>

      <!-- Navigation Header -->
      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <uni-icons type="flag" size="28" color="#ff6600" class="nav-left-icon"></uni-icons>
            <text class="nav-title">Kinetic Venues</text>
          </view>
          <view class="nav-right"></view>
        </view>
      </view>

      <view class="main-content">
        <!-- Page Header & Search -->
        <view class="page-intro">
          <view class="intro-left">
            <view class="h1">场馆列表</view>
            <view class="subtitle">VENUE LIST</view>
          </view>
          <view class="search-box">
            <input 
              class="search-input" 
              placeholder="搜索场馆名称..." 
              v-model="queryParams.venueName"
              @confirm="handleSearch"
            />
            <uni-icons type="search" size="18" color="#5f5e5e" class="search-icon"></uni-icons>
          </view>
        </view>

        <!-- Loading State -->
        <view v-if="loading && list.length === 0" class="state-wrap">
          <view class="spinner"></view>
          <text>正在加载场馆...</text>
        </view>

        <!-- Empty State -->
        <view v-else-if="list.length === 0" class="state-wrap">
          <text>暂无场馆数据</text>
        </view>

        <!-- Venue Grid -->
        <view v-else class="venue-grid">
          <view 
            v-for="item in list" 
            :key="item.id" 
            class="venue-card"
            :class="{ 'venue-closed': item.status !== 1 }"
            @click="goDetail(item.id)"
          >
            <view class="card-image-box">
              <image
                v-if="item.venueImage"
                class="card-image"
                :src="resolveImageUrl(item.venueImage)"
                mode="aspectFill"
              />
              <view v-else class="card-image-placeholder">
                <text class="image-placeholder-text">场馆图片</text>
              </view>
              
              <view class="status-badge" :class="item.status === 1 ? 'status-open' : 'status-close'">
                <view class="status-dot"></view>
                <text>{{ item.status === 1 ? '营业中' : '已关闭' }}</text>
              </view>
            </view>

            <view class="card-body">
              <view class="venue-name">{{ item.venueName }}</view>
              
              <view class="info-list">
                <view class="info-item">
                  <uni-icons type="location" size="16" color="#a33e00" class="info-icon"></uni-icons>
                  <text class="info-text">{{ item.address || '未设置地址' }}</text>
                </view>
                <view class="info-item">
                  <uni-icons type="calendar" size="16" color="#a33e00" class="info-icon"></uni-icons>
                  <text class="info-text">{{ item.businessHours || '09:00 - 22:00' }}</text>
                </view>
                <view class="info-item" v-if="item.contactPhone">
                  <uni-icons type="phone" size="16" color="#a33e00" class="info-icon"></uni-icons>
                  <text class="info-text">{{ item.contactPhone }}</text>
                </view>
              </view>

              <view class="detail-btn">
                <text>查看详情</text>
                <uni-icons type="right" size="14" color="#5a4136" class="detail-icon"></uni-icons>
              </view>
            </view>
          </view>
        </view>

        <!-- Load More -->
        <view v-if="hasMore && list.length > 0" class="load-more" @click="loadMore">
          <text>{{ loading ? '加载中...' : '加载更多' }}</text>
        </view>
      </view>
    </view>

    <!-- Custom BottomNavBar -->
    <CustomTabBar :current="1" />
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { getVenueList, type VenueItem } from '@/api/venue'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { parsePagedList } from '@/utils/parsePagedList'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

const userStore = useUserStore()
const loading = ref(false)
const list = ref<VenueItem[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  venueName: '',
  status: undefined as number | undefined
})

const hasMore = computed(() => list.value.length < total.value)

async function loadList(append = false) {
  if (loading.value) return
  if (!append) {
    queryParams.page = 1
    list.value = []
  }
  
  loading.value = true
  try {
    const res = await getVenueList(queryParams) as any
    const parsed = parsePagedList<VenueItem>(res)
    total.value = parsed.total
    if (append) {
      list.value = list.value.concat(parsed.list)
    } else {
      list.value = parsed.list
    }
  } catch (e) {
    console.error('加载场馆列表失败:', e)
    uni.showToast({
      title: '加载场馆列表失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadList()
}

function loadMore() {
  if (hasMore.value) {
    queryParams.page++
    loadList(true)
  }
}

function goBack() {
  safeNavigateBack()
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/venue/detail?id=${id}` })
}

onMounted(() => {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  loadList()
})
</script>

<style lang="scss" scoped>
.venue-list-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding-bottom: 180rpx;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f4f4f5;
}

.nav-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #f4f4f5;
  padding: 20rpx 32rpx;
}

.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;

  .nav-title {
    font-size: 40rpx;
    font-weight: 900;
    color: #1a1c1c;
  }
}

.avatar-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  border: 4rpx solid #ff6600;
  overflow: hidden;
  
  .avatar {
    width: 100%;
    height: 100%;
  }
}

.main-content {
  padding: 40rpx 32rpx;
}

.page-intro {
  margin-bottom: 60rpx;
  
  .h1 {
    font-size: 64rpx;
    font-weight: 700;
    color: #1a1c1c;
    margin-bottom: 8rpx;
  }
  
  .subtitle {
    font-size: 20rpx;
    color: #5f5e5e;
    letter-spacing: 4rpx;
    font-weight: 500;
  }
}

.search-box {
  margin-top: 40rpx;
  position: relative;
  
  .search-input {
    background-color: #eee;
    border-radius: 24rpx;
    height: 100rpx;
    padding: 0 100rpx 0 40rpx;
    font-size: 28rpx;
    transition: all 0.3s;
    
    &:focus {
      background-color: #fff;
      box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
    }
  }
  
  .search-icon {
    position: absolute;
    right: 32rpx;
    top: 50%;
    transform: translateY(-50%);
    color: #5f5e5e;
  }
}

.venue-grid {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.venue-card {
  background-color: #fff;
  border-radius: 48rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.04);
  transition: transform 0.2s;
  
  &:active {
    transform: scale(0.98);
  }
}

.venue-closed {
  opacity: 0.8;
  
  .card-image-box {
    filter: grayscale(0.5);
  }
}

.card-image-box {
  height: 400rpx;
  position: relative;
  
  .card-image {
    width: 100%;
    height: 100%;
  }
}

.card-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
}

.image-placeholder-text {
  font-size: 26rpx;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.35);
}

.status-badge {
  position: absolute;
  top: 32rpx;
  left: 32rpx;
  padding: 8rpx 24rpx;
  border-radius: 40rpx;
  background-color: rgba(255,255,255,0.7);
  backdrop-filter: blur(8rpx);
  display: flex;
  align-items: center;
  gap: 12rpx;
  
  .status-dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
  }
  
  text {
    font-size: 20rpx;
    font-weight: 700;
  }
}

.status-open {
  color: #0d9488;
  .status-dot { background-color: #10b981; }
}

.status-close {
  color: #71717a;
  .status-dot { background-color: #94a3b8; }
}

.card-body {
  padding: 40rpx;
}

.venue-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #1a1c1c;
  margin-bottom: 32rpx;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.info-icon {
  flex: 0 0 auto;
}

.info-text {
  font-size: 26rpx;
  color: #5f5e5e;
}

.detail-btn {
  background-color: #eee;
  height: 90rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  
  text {
    font-size: 28rpx;
    font-weight: 700;
    color: #5a4136;
  }
  
  &:active {
    background-color: #ff6600;
    text { color: #fff; }
  }
}

.state-wrap {
  padding: 100rpx 0;
  text-align: center;
  color: #5f5e5e;
  font-size: 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #eee;
  border-top-color: #ff6600;
  border-radius: 50%;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.load-more {
  padding: 40rpx;
  text-align: center;
  color: #a33e00;
  font-size: 26rpx;
  font-weight: 600;
}
</style>
