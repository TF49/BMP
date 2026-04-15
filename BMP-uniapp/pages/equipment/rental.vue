<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">租借确认</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Equipment Info -->
      <view class="section equipment-info">
        <view class="equipment-card">
          <view class="equipment-image">
            <text class="image-placeholder">Equipment Image</text>
          </view>
          <view class="equipment-details">
            <text class="equipment-name">{{ equipment.name }}</text>
            <text class="equipment-brand">品牌: {{ equipment.brand }}</text>
            <text class="equipment-price">¥{{ equipment.price }}/天</text>
          </view>
        </view>
      </view>

      <!-- Rental Details -->
      <view class="section rental-details">
        <view class="detail-item">
          <text class="detail-label">租借天数</text>
          <view class="quantity-selector">
            <text class="quantity-btn" @click="decreaseDays">-</text>
            <text class="quantity">{{ rentDays }}</text>
            <text class="quantity-btn" @click="increaseDays">+</text>
          </view>
        </view>
        <view class="detail-item">
          <text class="detail-label">租借数量</text>
          <view class="quantity-selector">
            <text class="quantity-btn" @click="decreaseQuantity">-</text>
            <text class="quantity">{{ rentQuantity }}</text>
            <text class="quantity-btn" @click="increaseQuantity">+</text>
          </view>
        </view>
        <view class="detail-item">
          <text class="detail-label">取货时间</text>
          <picker mode="date" :value="pickupDate" @change="onPickupDateChange">
            <view class="picker">
              <text class="picker-value">{{ pickupDate }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
        <view class="detail-item">
          <text class="detail-label">归还时间</text>
          <picker mode="date" :value="returnDate" @change="onReturnDateChange">
            <view class="picker">
              <text class="picker-value">{{ returnDate }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
        <view class="detail-item">
          <text class="detail-label">支付方式</text>
          <picker mode="selector" :range="paymentMethods" @change="onPaymentMethodChange">
            <view class="picker">
              <text class="picker-value">{{ paymentMethods[selectedPaymentMethod] }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- Rental Agreement -->
      <view class="section agreement-section">
        <view class="agreement-item">
          <text class="agreement-title">租借协议</text>
          <view class="agreement-content">
            <text class="agreement-text">1. 请妥善保管租借器材，如有损坏需照价赔偿</text>
            <text class="agreement-text">2. 请按时归还，逾期将收取滞纳金</text>
            <text class="agreement-text">3. 如有争议，可通过客服协商解决</text>
          </view>
        </view>
      </view>

      <!-- Price Summary -->
      <view class="section price-summary">
        <view class="summary-item">
          <text class="summary-label">单价</text>
          <text class="summary-value">¥{{ equipment.price }}</text>
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
      <view class="total-price-display">
        <text class="total-label">应付</text>
        <text class="total-amount">¥{{ totalPrice }}</text>
      </view>
      <button class="submit-btn" :disabled="!canSubmit" @click="handleSubmit">
        确认租借
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
import { createEquipmentRental } from '@/api/equipment'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const equipmentId = ref<number>(0)
const equipment = ref<any>({
  id: 0,
  name: '',
  brand: '',
  price: 0,
  quantity: 0
})

const rentDays = ref<number>(1)
const rentQuantity = ref<number>(1)
const pickupDate = ref<string>('')
const returnDate = ref<string>('')
const selectedPaymentMethod = ref<number>(0)
const paymentMethods = ref(['余额支付', '微信支付', '支付宝'])

const userStore = useUserStore()

// 计算属性
const totalPrice = computed(() => {
  return equipment.value.price * rentDays.value * rentQuantity.value
})

const canSubmit = computed(() => {
  return rentQuantity.value > 0 && 
         rentQuantity.value <= equipment.value.quantity &&
         pickupDate.value && 
         returnDate.value &&
         equipment.value.id > 0
})

// 页面加载
onLoad((options?: Record<string, string | undefined>) => {
  if (options?.id) {
    equipmentId.value = Number(options.id)
  }
  if (options?.quantity) {
    rentQuantity.value = Number(options.quantity)
  }
  if (options?.days) {
    rentDays.value = Number(options.days)
  }
})

// 日期初始化
onMounted(() => {
  const today = new Date()
  const tomorrow = new Date()
  tomorrow.setDate(today.getDate() + 1)
  
  pickupDate.value = today.toISOString().split('T')[0]
  returnDate.value = tomorrow.toISOString().split('T')[0]
})

// 加载器材详情
const loadEquipmentDetail = async () => {
  try {
    const result = await getEquipmentDetail(equipmentId.value)
    
    equipment.value = {
      id: result.id,
      name: result.equipmentName,
      brand: result.brand,
      price: result.rentalPrice || result.price,
      quantity: result.availableQuantity || result.quantity
    }
  } catch (error) {
    console.error('加载器材详情失败:', error)
    uni.showToast({
      title: '加载器材详情失败',
      icon: 'none'
    })
  }
}

// 减少租借天数
const decreaseDays = () => {
  if (rentDays.value > 1) {
    rentDays.value--
  }
}

// 增加租借天数
const increaseDays = () => {
  if (rentDays.value < 30) {
    rentDays.value++
  }
}

// 减少租借数量
const decreaseQuantity = () => {
  if (rentQuantity.value > 1) {
    rentQuantity.value--
  }
}

// 增加租借数量
const increaseQuantity = () => {
  if (rentQuantity.value < equipment.value.quantity) {
    rentQuantity.value++
  }
}

// 取货日期变化
const onPickupDateChange = (e: any) => {
  pickupDate.value = e.detail.value
  // 自动计算归还日期（比取货日期晚一天）
  const pickup = new Date(pickupDate.value)
  const returnDay = new Date(pickup)
  returnDay.setDate(pickup.getDate() + 1)
  returnDate.value = returnDay.toISOString().split('T')[0]
}

// 归还日期变化
const onReturnDateChange = (e: any) => {
  returnDate.value = e.detail.value
}

// 支付方式变化
const onPaymentMethodChange = (e: any) => {
  selectedPaymentMethod.value = parseInt(e.detail.value)
}

// 提交租借
const handleSubmit = async () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请完善租借信息',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '租借中...'
    })

    const rentalData = {
      memberId: userStore.userId,
      equipmentId: equipment.value.id,
      quantity: rentQuantity.value,
      startDate: pickupDate.value,
      endDate: returnDate.value,
      orderAmount: totalPrice.value,
      paymentMethod: paymentMethods.value[selectedPaymentMethod.value] === '余额支付' ? 'BALANCE' : 
                    paymentMethods.value[selectedPaymentMethod.value] === '微信支付' ? 'WECHAT' : 'ALIPAY'
    }

    const result = await createEquipmentRental(rentalData)

    uni.hideLoading()
    uni.showToast({
      title: '租借成功',
      icon: 'success'
    })

    // 延迟跳转到租借记录页
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/profile/records`
      })
    }, 1500)
  } catch (error) {
    console.error('租借器材失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '租借失败，请重试',
      icon: 'none'
    })
  }
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

.header-placeholder {
  width: 56rpx;
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
}

.section {
  background-color: #ffffff;
  margin-bottom: 20rpx;
  padding: 28rpx;
}

.equipment-info {
  .equipment-card {
    display: flex;
    gap: 20rpx;
  }

  .equipment-image {
    width: 120rpx;
    height: 120rpx;
    background-color: #f5f5f5;
    border-radius: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(153, 153, 153, 0.3);
    font-size: 20rpx;
  }

  .equipment-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .equipment-name {
    font-size: 26rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 8rpx;
  }

  .equipment-brand {
    font-size: 22rpx;
    color: #666666;
    margin-bottom: 8rpx;
  }

  .equipment-price {
    font-size: 24rpx;
    font-weight: bold;
    color: #ef4444;
  }
}

.rental-details {
  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f3f4f6;

    &:last-child {
      border-bottom: none;
    }
  }

  .detail-label {
    font-size: 24rpx;
    color: #333333;
    width: 120rpx;
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

  .picker {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    align-items: center;
    font-size: 24rpx;
    color: #333333;
  }

  .picker-value {
    flex: 1;
    text-align: right;
    color: #666666;
  }

  .chevron {
    font-size: 26rpx;
    color: #999999;
    margin-left: 12rpx;
  }
}

.agreement-section {
  .agreement-item {
    padding: 16rpx 0;
  }

  .agreement-title {
    font-size: 24rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 16rpx;
    display: block;
  }

  .agreement-content {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  .agreement-text {
    font-size: 22rpx;
    color: #999999;
    line-height: 1.5;
  }
}

.price-summary {
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

.total-price-display {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.total-label {
  font-size: 20rpx;
  color: #999999;
}

.total-amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #ef4444;
}

.submit-btn {
  flex: 1;
  height: 80rpx;
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 12rpx;
  border: none;
  margin-left: 28rpx;
  box-shadow: 0 2rpx 6rpx rgba(60, 197, 31, 0.2);

  &:disabled {
    background-color: #cccccc;
    color: #999999;
  }
}
</style>
