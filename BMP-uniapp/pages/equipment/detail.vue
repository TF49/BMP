<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">器材详情</text>
        <view class="header-actions">
          <view class="action-icon" @click="handleShare"><uni-icons type="paperplane" size="18" color="#475569"></uni-icons></view>
          <view class="action-icon" @click="handleFavorite"><uni-icons type="heart" size="18" color="#475569"></uni-icons></view>
        </view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Equipment Banner -->
      <view class="equipment-banner">
        <view class="banner-image">
          <text class="image-placeholder">Equipment Banner</text>
        </view>
        <view class="banner-content">
          <view class="equipment-main">
            <text class="equipment-name">{{ equipment.name }}</text>
            <text class="equipment-price">¥{{ equipment.price }}<text class="price-unit">/天</text></text>
          </view>
          <view class="equipment-meta">
            <view class="meta-item">
              <uni-icons type="flag" size="16" color="#475569"></uni-icons>
              <text class="meta-text">{{ equipment.brand }}</text>
            </view>
            <view class="meta-item">
              <uni-icons type="bars" size="16" color="#475569"></uni-icons>
              <text class="meta-text">库存 {{ equipment.quantity }}</text>
            </view>
            <view class="meta-item">
              <uni-icons type="star-filled" size="16" color="#f59e0b"></uni-icons>
              <text class="meta-text">评分 {{ equipment.rating }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Equipment Info -->
      <view class="section equipment-info">
        <view class="info-item">
          <text class="info-label">器材类型</text>
          <text class="info-value">{{ equipment.type }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">品牌型号</text>
          <text class="info-value">{{ equipment.model }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">规格参数</text>
          <text class="info-value">{{ equipment.specifications }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">适用场景</text>
          <text class="info-value">{{ equipment.usage }}</text>
        </view>
      </view>

      <!-- Description -->
      <view class="section description-section">
        <text class="section-title">器材介绍</text>
        <text class="description-text">{{ equipment.description }}</text>
      </view>

      <!-- Images -->
      <view class="section images-section">
        <text class="section-title">实物图片</text>
        <view class="images-container">
          <view v-for="(image, index) in equipment.images" :key="index" class="image-item">
            <text class="image-placeholder">Image {{ index + 1 }}</text>
          </view>
        </view>
      </view>

      <!-- Rental Options -->
      <view class="section rental-options">
        <text class="section-title">租借选项</text>
        <view class="rental-option">
          <text class="option-label">租借天数</text>
          <view class="quantity-selector">
            <text class="quantity-btn" @click="decreaseDays">-</text>
            <text class="quantity">{{ rentDays }}</text>
            <text class="quantity-btn" @click="increaseDays">+</text>
          </view>
        </view>
        <view class="rental-option">
          <text class="option-label">租借数量</text>
          <view class="quantity-selector">
            <text class="quantity-btn" @click="decreaseQuantity">-</text>
            <text class="quantity">{{ rentQuantity }}</text>
            <text class="quantity-btn" @click="increaseQuantity">+</text>
          </view>
        </view>
      </view>

      <!-- Rental Summary -->
      <view class="section summary-section">
        <text class="section-title">租借摘要</text>
        <view class="summary-item">
          <text class="summary-label">单价</text>
          <text class="summary-value">¥{{ equipment.price }}/天</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">天数</text>
          <text class="summary-value">{{ rentDays }} 天</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">数量</text>
          <text class="summary-value">{{ rentQuantity }} 件</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-item total">
          <text class="summary-label">总计</text>
          <text class="summary-value total-price">¥{{ totalPrice }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Action Bar -->
    <view class="action-bar">
      <button class="action-btn favorite-btn" @click="handleFavoriteBtn">
        <uni-icons type="heart" size="18" color="#475569" class="btn-icon"></uni-icons>
        <text class="btn-text">收藏</text>
      </button>
      <button 
        class="action-btn rent-btn" 
        :class="{ disabled: equipment.quantity <= 0 }"
        :disabled="equipment.quantity <= 0"
        @click="handleRent"
      >
        <uni-icons type="cart" size="18" color="#ffffff" class="btn-icon"></uni-icons>
        <text class="btn-text">
          {{ equipment.quantity <= 0 ? '库存不足' : '立即租借' }}
        </text>
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getEquipmentDetail } from '@/api/equipment'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const equipmentId = ref<number>(0)
const equipment = ref<any>({
  id: 0,
  name: '羽毛球拍',
  brand: '尤尼克斯',
  price: 20,
  quantity: 5,
  rating: 4.8,
  type: '球拍',
  model: 'Duora 10',
  specifications: '3U, G4握把',
  usage: '专业比赛用拍',
  description: '这款球拍采用先进的材料科技，提供卓越的操控性和力量传递，适合进攻型打法的选手使用。',
  images: ['/static/images/equipment1.jpg', '/static/images/equipment2.jpg']
})

const rentDays = ref<number>(1)
const rentQuantity = ref<number>(1)
const userStore = useUserStore()

// 计算总价
const totalPrice = computed(() => {
  return equipment.value.price * rentDays.value * rentQuantity.value
})

// 页面加载
onLoad((options) => {
  if (options.id) {
    equipmentId.value = Number(options.id)
  }
})

// 加载器材详情
const loadEquipmentDetail = async () => {
  try {
    const result = await getEquipmentDetail(equipmentId.value)
    
    // 将API數據轉換為頁面所需格式
    equipment.value = {
      id: result.id,
      name: result.equipmentName,
      brand: result.brand,
      price: result.rentalPrice || result.price,
      quantity: result.availableQuantity || result.quantity,
      rating: result.rating || 4.8,
      type: result.equipmentType,
      model: result.model || '型号',
      specifications: result.specifications || '规格',
      usage: result.usage || '适用场景',
      description: result.description,
      images: result.images || []
    }
  } catch (error) {
    console.error('加载器材详情失败:', error)
    uni.showToast({
      title: '加载器材详情失败',
      icon: 'none'
    })
  }
}

// 减少租借天數
const decreaseDays = () => {
  if (rentDays.value > 1) {
    rentDays.value--
  }
}

// 增加租借天數
const increaseDays = () => {
  if (rentDays.value < 30) {
    rentDays.value++
  }
}

// 減少租借數量
const decreaseQuantity = () => {
  if (rentQuantity.value > 1) {
    rentQuantity.value--
  }
}

// 增加租借數量
const increaseQuantity = () => {
  if (rentQuantity.value < equipment.value.quantity) {
    rentQuantity.value++
  }
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 分享功能
const handleShare = () => {
  uni.showActionSheet({
    itemList: ['微信好友', '朋友圈', '复制链接'],
    success: (res) => {
      uni.showToast({
        title: `已分享给${['微信好友', '朋友圈', '复制链接'][res.tapIndex]}`,
        icon: 'none'
      })
    }
  })
}

// 收藏功能
const handleFavorite = () => {
  uni.showToast({
    title: '已收藏',
    icon: 'none'
  })
}

// 收藏按钮
const handleFavoriteBtn = () => {
  handleFavorite()
}

// 租借器材
const handleRent = () => {
  uni.showModal({
    title: '租借确认',
    content: `确定要租借"${equipment.value.name}"吗？数量: ${rentQuantity.value}，天数: ${rentDays.value}，总计: ¥${totalPrice.value}`,
    success: (res) => {
      if (res.confirm) {
        uni.navigateTo({
          url: `/pages/equipment/rental?id=${equipment.value.id}&quantity=${rentQuantity.value}&days=${rentDays.value}`
        })
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
  
  if (equipmentId.value) {
    await loadEquipmentDetail()
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

.header-actions {
  display: flex;
  gap: 24rpx;
}

.action-icon {
  font-size: 32rpx;
  color: #999999;
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
}

.equipment-banner {
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

.equipment-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.equipment-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
}

.equipment-price {
  font-size: 28rpx;
  font-weight: bold;
  color: #ef4444;
}

.price-unit {
  font-size: 20rpx;
  font-weight: normal;
  color: #999999;
}

.equipment-meta {
  display: flex;
  gap: 32rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx;
  color: #999999;
}

.meta-text {
  font-size: 22rpx;
  color: #666666;
}

.section {
  background-color: #ffffff;
  margin-bottom: 20rpx;
  padding: 28rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f3f4f6;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 24rpx;
  color: #999999;
  width: 120rpx;
}

.info-value {
  font-size: 24rpx;
  color: #333333;
  flex: 1;
  text-align: right;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
  display: block;
}

.description-text {
  font-size: 24rpx;
  color: #666666;
  line-height: 1.6;
}

.images-section {
  .images-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16rpx;
  }

  .image-item {
    aspect-ratio: 1;
    background-color: #f5f5f5;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(153, 153, 153, 0.3);
    font-size: 20rpx;
  }
}

.rental-options {
  .rental-option {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f3f4f6;

    &:last-child {
      border-bottom: none;
    }
  }

  .option-label {
    font-size: 24rpx;
    color: #333333;
  }

  .quantity-selector {
    display: flex;
    align-items: center;
    gap: 20rpx;
  }

  .quantity-btn {
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    background-color: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    color: #333333;
  }

  .quantity {
    font-size: 28rpx;
    color: #333333;
    font-weight: bold;
    min-width: 40rpx;
    text-align: center;
  }
}

.summary-section {
  .summary-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;

    &.total {
      margin-top: 10rpx;
    }
  }

  .summary-label {
    font-size: 24rpx;
    color: #333333;
  }

  .summary-value {
    font-size: 24rpx;
    color: #333333;
    font-weight: bold;

    &.total-price {
      color: #ef4444;
      font-size: 28rpx;
    }
  }

  .summary-divider {
    height: 1rpx;
    background-color: #e6e6e6;
    margin: 10rpx 0;
  }
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
  margin: 20rpx 8rpx;

  .btn-icon {
    margin-right: 8rpx;
    font-size: 28rpx;
  }

  .btn-text {
    font-size: 28rpx;
  }
}

.favorite-btn {
  background-color: #f5f5f5;
  color: #333333;
}

.rent-btn {
  background-color: #3cc51f;
  color: #ffffff;

  &.disabled {
    background-color: #cccccc;
    color: #999999;
  }
}
</style>