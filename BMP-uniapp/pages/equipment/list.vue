<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">器材租借</text>
        <view class="header-search" @click="handleSearch">
          <uni-icons type="search" size="18" color="#475569"></uni-icons>
          <text class="search-text">搜索器材</text>
        </view>
      </view>
    </view>

    <!-- Category Tabs -->
    <view class="category-tabs">
      <scroll-view class="tabs-scroll" scroll-x>
        <view class="tabs-container">
          <view 
            v-for="(category, index) in categories" 
            :key="index"
            class="tab-item"
            :class="{ active: selectedCategory === index }"
            @click="selectCategory(index)"
          >
            {{ category.name }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- Filter Bar -->
    <view class="filter-bar">
      <view class="filter-item" @click="toggleSort('price')">
        <text class="filter-text">价格</text>
        <uni-icons :type="sortField === 'price' && sortDirection === 'asc' ? 'arrowup' : 'arrowdown'" size="14" color="#475569" class="sort-icon"></uni-icons>
      </view>
      <view class="filter-item" @click="toggleSort('popularity')">
        <text class="filter-text">热度</text>
        <uni-icons :type="sortField === 'popularity' && sortDirection === 'asc' ? 'arrowup' : 'arrowdown'" size="14" color="#475569" class="sort-icon"></uni-icons>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <view v-if="equipmentList.length === 0" class="empty-state">
        <text class="empty-text">暂无器材</text>
      </view>
      
      <view v-else class="equipment-list">
        <view 
          v-for="(equipment, index) in equipmentList" 
          :key="index"
          class="equipment-item"
          @click="handleEquipmentClick(equipment)"
        >
          <view class="equipment-image">
            <text class="image-placeholder">Equipment Image</text>
          </view>
          <view class="equipment-info">
            <view class="equipment-header">
              <text class="equipment-name">{{ equipment.name }}</text>
              <text class="equipment-price">¥{{ equipment.price }}/天</text>
            </view>
            <view class="equipment-meta">
              <text class="equipment-brand">品牌: {{ equipment.brand }}</text>
              <text class="equipment-type">{{ equipment.type }}</text>
            </view>
            <view class="equipment-stats">
              <text class="availability">库存: {{ equipment.quantity }}</text>
              <view class="rating">
                <uni-icons type="star-filled" size="14" color="#f59e0b"></uni-icons>
                <text>{{ equipment.rating }}</text>
              </view>
            </view>
            <view class="equipment-actions">
              <button class="rent-btn" @click.stop="handleRent(equipment)">立即租借</button>
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
import { getEquipmentList } from '@/api/equipment'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const equipmentList = ref<any[]>([])
const categories = ref([
  { id: 0, name: '全部' },
  { id: 1, name: '球拍' },
  { id: 2, name: '羽毛球' },
  { id: 3, name: '运动装备' },
  { id: 4, name: '护具' },
  { id: 5, name: '其他' }
])
const selectedCategory = ref<number>(0)
const sortField = ref<string>('popularity')
const sortDirection = ref<'asc'|'desc'>('desc')
const loading = ref(false)
const userStore = useUserStore()

// 加载器材列表
const loadEquipmentList = async () => {
  loading.value = true
  try {
    const params: any = {
      page: 1,
      size: 20,
      status: 1 // 只获取可用器材
    }

    // 根据分类筛选
    if (selectedCategory.value > 0) {
      params.category = categories.value[selectedCategory.value].id
    }

    // 排序
    params.sortBy = sortField.value
    params.sortOrder = sortDirection.value

    const result = await getEquipmentList(params)
    
    // 将API数据转换为页面所需格式
    equipmentList.value = result.data.map((eq: any) => ({
      id: eq.id,
      name: eq.equipmentName,
      brand: eq.brand || '品牌',
      type: eq.equipmentType || '类型',
      price: eq.rentalPrice || eq.price,
      quantity: eq.availableQuantity || eq.quantity,
      rating: eq.rating || 4.5,
      images: eq.images || [],
      description: eq.description || ''
    }))
  } catch (error) {
    console.error('加载器材列表失败:', error)
    uni.showToast({
      title: '加载器材列表失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 选择分类
const selectCategory = (index: number) => {
  selectedCategory.value = index
  loadEquipmentList() // 重新加载列表
}

// 切换排序
const toggleSort = (field: string) => {
  if (sortField.value === field) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDirection.value = 'desc'
  }
  loadEquipmentList() // 重新加载列表
}

// 器材点击事件
const handleEquipmentClick = (equipment: any) => {
  uni.navigateTo({
    url: `/pages/equipment/detail?id=${equipment.id}`
  })
}

// 立即租借
const handleRent = (equipment: any) => {
  uni.navigateTo({
    url: `/pages/equipment/rental?id=${equipment.id}`
  })
}

// 搜索功能
const handleSearch = () => {
  uni.navigateTo({
    url: '/pages/equipment/search'
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
  
  await loadEquipmentList()
})

// 启用下拉刷新
onPullDownRefresh(() => {
  loadEquipmentList().finally(() => {
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

.category-tabs {
  background-color: #ffffff;
  padding: 18rpx 28rpx;
  border-bottom: 1rpx solid #e6e6e6;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-container {
  display: flex;
  gap: 24rpx;
}

.tab-item {
  flex-shrink: 0;
  padding: 10rpx 24rpx;
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
  font-size: 20rpx;
  color: #999999;
  
  &.asc {
    color: #3cc51f;
  }
  
  &.desc {
    color: #3cc51f;
  }
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

.equipment-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.equipment-item {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 20rpx;
}

.equipment-image {
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

.equipment-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.equipment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.equipment-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
}

.equipment-price {
  font-size: 24rpx;
  font-weight: bold;
  color: #ef4444;
}

.equipment-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.equipment-brand {
  font-size: 22rpx;
  color: #666666;
}

.equipment-type {
  font-size: 20rpx;
  color: #3cc51f;
  background-color: #e8f5e9;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
}

.equipment-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.availability {
  font-size: 20rpx;
  color: #999999;
}

.rating {
  font-size: 20rpx;
  color: #ffc107;
}

.equipment-actions {
  display: flex;
  justify-content: flex-end;
}

.rent-btn {
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: bold;
  padding: 8rpx 24rpx;
  border-radius: 9999rpx;
  border: none;
  box-shadow: 0 2rpx 6rpx rgba(60, 197, 31, 0.2);
}
</style>