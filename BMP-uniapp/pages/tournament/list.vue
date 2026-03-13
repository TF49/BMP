<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">赛事报名</text>
        <view class="header-search" @click="handleSearch">
          <uni-icons type="search" size="18" color="#475569"></uni-icons>
          <text class="search-text">搜索赛事</text>
        </view>
      </view>
    </view>

    <!-- Status Tabs -->
    <view class="status-tabs">
      <view class="tabs-container">
        <view 
          v-for="(tab, index) in tabs" 
          :key="index"
          class="tab-item"
          :class="{ active: selectedTab === index }"
          @click="selectTab(index)"
        >
          {{ tab.name }}
        </view>
      </view>
    </view>

    <!-- Filter Bar -->
    <view class="filter-bar">
      <view class="filter-item" @click="toggleSort('date')">
        <text class="filter-text">时间</text>
        <uni-icons :type="sortField === 'date' && sortDirection === 'asc' ? 'arrowup' : 'arrowdown'" size="14" color="#475569" class="sort-icon"></uni-icons>
      </view>
      <view class="filter-item" @click="toggleSort('level')">
        <text class="filter-text">级别</text>
        <uni-icons :type="sortField === 'level' && sortDirection === 'asc' ? 'arrowup' : 'arrowdown'" size="14" color="#475569" class="sort-icon"></uni-icons>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <view v-if="tournamentList.length === 0" class="empty-state">
        <text class="empty-text">暂无赛事</text>
      </view>
      
      <view v-else class="tournament-list">
        <view 
          v-for="(tournament, index) in tournamentList" 
          :key="index"
          class="tournament-item"
          @click="handleTournamentClick(tournament)"
        >
          <view class="tournament-image">
            <text class="image-placeholder">Tournament Image</text>
          </view>
          <view class="tournament-info">
            <view class="tournament-header">
              <text class="tournament-name">{{ tournament.name }}</text>
              <text class="tournament-status" :class="getStatusClass(tournament.status)">
                {{ getStatusText(tournament.status) }}
              </text>
            </view>
            <view class="tournament-meta">
              <view class="meta-item">
                <uni-icons type="calendar" size="14" color="#475569"></uni-icons>
                <text>{{ tournament.date }} {{ tournament.time }}</text>
              </view>
              <view class="meta-item">
                <uni-icons type="location" size="14" color="#475569"></uni-icons>
                <text>{{ tournament.location }}</text>
              </view>
            </view>
            <view class="tournament-level">
              <text class="level-tag">{{ tournament.level }}</text>
              <text class="fee-tag">报名费: ¥{{ tournament.fee }}</text>
            </view>
            <view class="tournament-stats">
              <text class="participants">参赛者: {{ tournament.participants }}/{{ tournament.maxParticipants }}</text>
              <view class="progress-bar">
                <view class="progress" :style="{ width: tournament.progress + '%' }"></view>
              </view>
            </view>
            <view class="tournament-actions">
              <button 
                class="register-btn" 
                :class="{ disabled: !canRegister(tournament) }"
                :disabled="!canRegister(tournament)"
                @click.stop="handleRegister(tournament)"
              >
                {{ getRegisterButtonText(tournament) }}
              </button>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getTournamentList } from '@/api/tournament'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const tournamentList = ref<any[]>([])
const tabs = ref([
  { id: 0, name: '全部' },
  { id: 1, name: '报名中' },
  { id: 2, name: '进行中' },
  { id: 3, name: '已结束' }
])
const selectedTab = ref<number>(0)
const sortField = ref<string>('date')
const sortDirection = ref<'asc'|'desc'>('desc')
const loading = ref(false)
const userStore = useUserStore()

// 获取状态文本（与后端一致：0已取消 1报名中 2进行中 3已结束）
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '报名中',
    2: '进行中',
    3: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'status-cancelled',
    1: 'status-registering',
    2: 'status-ongoing',
    3: 'status-ended'
  }
  return classMap[status] || ''
}

// 检查是否可以报名
const canRegister = (tournament: any) => {
  return tournament.status === 1 && tournament.participants < tournament.maxParticipants
}

// 获取报名按钮文字
const getRegisterButtonText = (tournament: any) => {
  if (tournament.status === 1) {
    if (tournament.participants >= tournament.maxParticipants) {
      return '人数已满'
    }
    return '立即报名'
  } else if (tournament.status === 2) {
    return '进行中'
  } else if (tournament.status === 3) {
    return '已结束'
  }
  return '查看详情'
}

// 加载赛事列表
const loadTournamentList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: 1,
      size: 20
    }

    // 根据状态筛选
    if (selectedTab.value > 0) {
      const statusMap = [null, 1, 2, 3] // 0对应全部，1报名中，2进行中，3已结束
      if (statusMap[selectedTab.value]) {
        params.status = statusMap[selectedTab.value]
      }
    }

    // 排序
    params.sortBy = sortField.value
    params.sortOrder = sortDirection.value

    const result = await getTournamentList(params)
    
    // 将API数据转换为页面所需格式
    tournamentList.value = result.data.map((t: any) => ({
      id: t.id,
      name: t.tournamentName,
      date: t.startDate,
      time: t.startTime,
      location: t.location,
      level: t.level || '业余',
      fee: t.entryFee || 0,
      maxParticipants: t.maxParticipants,
      participants: t.currentParticipants || 0,
      status: t.status,
      progress: t.maxParticipants > 0 ? Math.round((t.currentParticipants / t.maxParticipants) * 100) : 0
    }))
  } catch (error) {
    console.error('加载赛事列表失败:', error)
    uni.showToast({
      title: '加载赛事列表失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 选择标签
const selectTab = (index: number) => {
  selectedTab.value = index
  loadTournamentList() // 重新加载列表
}

// 切换排序
const toggleSort = (field: string) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'desc'
  }
  loadTournamentList() // 重新加载列表
}

// 赛事点击事件
const handleTournamentClick = (tournament: any) => {
  uni.navigateTo({
    url: `/pages/tournament/detail?id=${tournament.id}`
  })
}

// 立即报名
const handleRegister = (tournament: any) => {
  if (canRegister(tournament)) {
    uni.navigateTo({
      url: `/pages/tournament/register?id=${tournament.id}`
    })
  }
}

// 搜索功能
const handleSearch = () => {
  uni.navigateTo({
    url: '/pages/tournament/search'
  })
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
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
  
  await loadTournamentList()
})

// 启用下拉刷新
onPullDownRefresh(() => {
  loadTournamentList().finally(() => {
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

.header-search {
  width: 200rpx;
  height: 60rpx;
  background-color: #f5f5f5;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.search-icon {
  font-size: 24rpx;
  color: #999999;
}

.search-text {
  font-size: 22rpx;
  color: #999999;
}

.status-tabs {
  background-color: #ffffff;
  padding: 18rpx 28rpx;
  border-bottom: 1rpx solid #e6e6e6;
}

.tabs-container {
  display: flex;
  justify-content: space-around;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10rpx 16rpx;
  font-size: 24rpx;
  color: #999999;
  border-radius: 9999rpx;
  transition: all 0.3s;
  background-color: #f5f5f5;
  
  &.active {
    background-color: #3cc51f;
    color: #ffffff;
  }
}

.filter-bar {
  background-color: #ffffff;
  padding: 12rpx 28rpx;
  display: flex;
  justify-content: space-around;
  border-bottom: 1rpx solid #e6e6e6;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border-radius: 9999rpx;
  background-color: #f5f5f5;
}

.filter-text {
  font-size: 22rpx;
  color: #666666;
}

.sort-icon {
  flex-shrink: 0;
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
  padding: 24rpx 28rpx;
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

.tournament-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.tournament-item {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 20rpx;
}

.tournament-image {
  width: 140rpx;
  height: 140rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(153, 153, 153, 0.3);
  font-size: 20rpx;
}

.tournament-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.tournament-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.tournament-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
}

.tournament-status {
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  
  &.status-registering {
    background-color: #e8f5e9;
    color: #3cc51f;
  }
  
  &.status-closed {
    background-color: #fff3e0;
    color: #ff9800;
  }
  
  &.status-ongoing {
    background-color: #e3f2fd;
    color: #2196f3;
  }
  
  &.status-ended {
    background-color: #f5f5f5;
    color: #999999;
  }
  
  &.status-cancelled {
    background-color: #ffebee;
    color: #f44336;
  }
}

.tournament-meta {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 8rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  color: $text-color-secondary;
}
.meta-item text {
  flex: 1;
}

.tournament-level {
  display: flex;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.level-tag {
  font-size: 20rpx;
  color: #3cc51f;
  background-color: #e8f5e9;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.fee-tag {
  font-size: 20rpx;
  color: #ef4444;
  background-color: #fef2f2;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.tournament-stats {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.participants {
  font-size: 20rpx;
  color: #999999;
}

.progress-bar {
  height: 8rpx;
  background-color: #f3f4f6;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress {
  height: 100%;
  background-color: #3cc51f;
  border-radius: 4rpx;
  transition: width 0.3s;
}

.tournament-actions {
  display: flex;
  justify-content: flex-end;
}

.register-btn {
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: bold;
  padding: 8rpx 24rpx;
  border-radius: 9999rpx;
  border: none;
  box-shadow: 0 2rpx 6rpx rgba(60, 197, 31, 0.2);
  
  &.disabled {
    background-color: #cccccc;
    color: #999999;
  }
}
</style>