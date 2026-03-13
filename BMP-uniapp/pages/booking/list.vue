<template>
  <MobileLayout>
    <view class="booking-list">
      <view class="header">
        <text class="header-title">我的预约</text>
      </view>
      
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

      <view class="booking-content">
        <view v-if="bookingList.length === 0" class="empty-state">
          <text class="empty-text">暂无预约记录</text>
        </view>
        
        <view v-else class="booking-items">
          <view 
            v-for="(item, index) in bookingList" 
            :key="index"
            class="booking-card"
            @click="handleBookingClick(item)"
          >
            <view class="booking-header">
              <text class="venue-name">{{ item.venueName }}</text>
              <text class="booking-status" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </text>
            </view>
            <view class="booking-info">
              <text class="info-item">日期：{{ item.date }}</text>
              <text class="info-item">时间：{{ item.startTime }} - {{ item.endTime }}</text>
              <text class="info-item">金额：¥{{ item.amount }}</text>
            </view>
            <view class="booking-actions">
              <button 
                v-if="item.status === 1 || item.status === 2"
                class="cancel-btn"
                @click.stop="handleCancel(item)"
              >
                取消预约
              </button>
            </view>
          </view>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getBookingList, cancelBooking } from '@/api/booking'

const currentTab = ref(0)
const tabs = ref(['全部', '待支付', '已支付', '进行中', '已完成'])
const bookingList = ref<any[]>([])
const loading = ref(false)
const userStore = useUserStore()

// 加载预约列表数据
const loadBookingList = async () => {
  loading.value = true
  try {
    // 确保只获取当前用户的预约（普通用户权限）
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
      memberId: userStore.userId, // 只查询当前用户的预约
      page: 1,
      size: 20
    }
    
    // 根据当前标签筛选状态
    if (currentTab.value !== 0) {
      const statusMap = [null, 1, 2, 3, 4] // 0对应全部
      if (statusMap[currentTab.value]) {
        params.status = statusMap[currentTab.value]
      }
    }
    
    const result = await getBookingList(params)
    
    // 将API数据转换为页面所需格式
    bookingList.value = result.data.map((booking: any) => ({
      id: booking.id,
      venueName: booking.venueName || booking.courtName,
      courtName: booking.courtName,
      date: booking.bookingDate,
      startTime: booking.startTime,
      endTime: booking.endTime,
      amount: booking.orderAmount,
      status: booking.status,
      bookingNo: booking.bookingNo,
      createTime: booking.createTime
    }))
  } catch (error) {
    console.error('加载预约列表失败:', error)
    uni.showToast({
      title: '加载预约列表失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const switchTab = (index: number) => {
  currentTab.value = index
  loadBookingList() // 切换标签时重新加载数据
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '已支付',
    3: '进行中',
    4: '已完成'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'status-cancelled',
    1: 'status-pending',
    2: 'status-paid',
    3: 'status-ongoing',
    4: 'status-completed'
  }
  return classMap[status] || ''
}

const handleBookingClick = (item: any) => {
  uni.navigateTo({
    url: `/pages/booking/detail?id=${item.id}`
  })
}

const handleCancel = async (item: any) => {
  uni.showModal({
    title: '提示',
    content: '确定要取消预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await cancelBooking(item.id)
          uni.showToast({
            title: '取消成功',
            icon: 'success'
          })
          // 重新加载列表
          await loadBookingList()
        } catch (error) {
          console.error('取消预约失败:', error)
          uni.showToast({
            title: '取消失败',
            icon: 'none'
          })
        }
      }
    }
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
  
  await loadBookingList()
})

// 启用下拉刷新
onPullDownRefresh(() => {
  loadBookingList().finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.booking-list {
  min-height: 100vh;
  background-color: #f5f7fa;
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

.booking-content {
  padding: 24rpx 28rpx;
  padding-bottom: 180rpx;
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

.booking-items {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.booking-card {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 24rpx 28rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.venue-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
}

.booking-status {
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  
  &.status-pending {
    background-color: #fff3e0;
    color: #ff9800;
  }
  
  &.status-paid {
    background-color: #e8f5e9;
    color: #3cc51f;
  }
  
  &.status-ongoing {
    background-color: #e3f2fd;
    color: #2196f3;
  }
  
  &.status-completed {
    background-color: #f5f5f5;
    color: #999999;
  }
}

.booking-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  margin-bottom: 18rpx;
}

.info-item {
  font-size: 24rpx;
  color: #666666;
}

.booking-actions {
  display: flex;
  justify-content: flex-end;
}

.cancel-btn {
  background-color: #ffffff;
  color: #ef4444;
  font-size: 24rpx;
  padding: 12rpx 24rpx;
  border-radius: 12rpx;
  border: 1rpx solid #ef4444;
}
</style>
