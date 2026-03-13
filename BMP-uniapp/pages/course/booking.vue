<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">预约课程</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Course Info -->
      <view class="section course-info">
        <view class="course-card">
          <view class="course-image">
            <text class="image-placeholder">Course Image</text>
          </view>
          <view class="course-details">
            <text class="course-name">{{ course.name }}</text>
            <text class="course-coach">教练: {{ course.coachName }}</text>
            <text class="course-time">{{ course.date }} {{ course.time }}</text>
            <view class="course-stats">
              <text class="capacity">已约 {{ course.currentStudents }}/{{ course.maxStudents }}</text>
              <view class="progress-bar">
                <view class="progress" :style="{ width: course.progress + '%' }"></view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Booking Info -->
      <view class="section booking-info">
        <view class="info-item">
          <text class="info-label">课程费用</text>
          <text class="info-value">¥{{ course.price }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">预约数量</text>
          <view class="quantity-selector">
            <text class="quantity-btn" @click="decreaseQuantity">-</text>
            <text class="quantity">{{ quantity }}</text>
            <text class="quantity-btn" @click="increaseQuantity">+</text>
          </view>
        </view>
        <view class="info-item">
          <text class="info-label">支付方式</text>
          <picker mode="selector" :range="paymentMethods" @change="onPaymentMethodChange">
            <view class="picker">
              <text class="picker-value">{{ paymentMethods[selectedPaymentMethod] }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- Price Summary -->
      <view class="section price-summary">
        <view class="summary-item">
          <text class="summary-label">课程费用</text>
          <text class="summary-value">¥{{ course.price }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">预约数量</text>
          <text class="summary-value">{{ quantity }} 个</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-item total">
          <text class="summary-label">总计</text>
          <text class="summary-value total-price">¥{{ totalPrice }}</text>
        </view>
      </view>

      <!-- Terms -->
      <view class="section terms-section">
        <view class="terms-item">
          <text class="terms-text">预约后请准时参加，如需取消请提前2小时联系教练</text>
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
        确认预约
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCourseDetail } from '@/api/course'
import { createCourseBooking } from '@/api/course'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const courseId = ref<number>(0)
const course = ref<any>({
  id: 0,
  name: '',
  coachName: '',
  price: 0,
  date: '',
  time: '',
  maxStudents: 0,
  currentStudents: 0,
  progress: 0
})

const quantity = ref<number>(1)
const selectedPaymentMethod = ref<number>(0)
const paymentMethods = ref(['余额支付', '微信支付', '支付宝'])

const userStore = useUserStore()

// 计算属性
const totalPrice = computed(() => {
  return course.value.price * quantity.value
})

const canSubmit = computed(() => {
  return quantity.value > 0 && 
         course.value.id > 0 && 
         course.value.currentStudents + quantity.value <= course.value.maxStudents
})

// 页面加载
onLoad((options) => {
  if (options.id) {
    courseId.value = Number(options.id)
  }
})

// 加载课程详情
const loadCourseDetail = async () => {
  try {
    const result = await getCourseDetail(courseId.value)
    
    course.value = {
      id: result.id,
      name: result.courseName,
      coachName: result.coachName,
      price: result.coursePrice,
      date: result.courseDate,
      time: `${result.startTime} - ${result.endTime}`,
      maxStudents: result.maxStudents,
      currentStudents: result.currentStudents,
      progress: result.maxStudents > 0 ? Math.round((result.currentStudents / result.maxStudents) * 100) : 0
    }
  } catch (error) {
    console.error('加载课程详情失败:', error)
    uni.showToast({
      title: '加载课程详情失败',
      icon: 'none'
    })
  }
}

// 减少数量
const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

// 增加数量
const increaseQuantity = () => {
  if (course.value.currentStudents + quantity.value < course.value.maxStudents) {
    quantity.value++
  }
}

// 支付方式变化
const onPaymentMethodChange = (e: any) => {
  selectedPaymentMethod.value = parseInt(e.detail.value)
}

// 提交预约
const handleSubmit = async () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请检查预约信息',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '预约中...'
    })

    const bookingData = {
      memberId: userStore.userId,
      courseId: course.value.id,
      quantity: quantity.value,
      orderAmount: totalPrice.value,
      paymentMethod: paymentMethods.value[selectedPaymentMethod.value] === '余额支付' ? 'BALANCE' : 
                    paymentMethods.value[selectedPaymentMethod.value] === '微信支付' ? 'WECHAT' : 'ALIPAY'
    }

    const result = await createCourseBooking(bookingData)

    uni.hideLoading()
    uni.showToast({
      title: '预约成功',
      icon: 'success'
    })

    // 延迟跳转到预约详情页
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/course/detail?id=${course.value.id}`
      })
    }, 1500)
  } catch (error) {
    console.error('预约课程失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '预约失败，请重试',
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
  
  if (courseId.value) {
    await loadCourseDetail()
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

.course-info {
  .course-card {
    display: flex;
    gap: 20rpx;
  }

  .course-image {
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

  .course-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .course-name {
    font-size: 28rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 12rpx;
  }

  .course-coach {
    font-size: 24rpx;
    color: #666666;
    margin-bottom: 8rpx;
  }

  .course-time {
    font-size: 24rpx;
    color: #999999;
    margin-bottom: 12rpx;
  }

  .course-stats {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  .capacity {
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
}

.booking-info {
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
    color: #333333;
  }

  .info-value {
    font-size: 24rpx;
    color: #333333;
    font-weight: bold;
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

.terms-section {
  .terms-item {
    padding: 16rpx 0;
  }

  .terms-text {
    font-size: 22rpx;
    color: #999999;
    line-height: 1.5;
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