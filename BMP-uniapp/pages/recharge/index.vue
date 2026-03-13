<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">充值中心</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Balance Card -->
    <view class="balance-card">
      <view class="card-content">
        <view class="balance-info">
          <text class="balance-label">当前余额</text>
          <text class="balance-amount">¥{{ balance }}</text>
        </view>
        <view class="balance-actions">
          <text class="balance-action" @click="handleRecharge">立即充值</text>
          <text class="balance-action" @click="handleRecords">充值记录</text>
        </view>
      </view>
    </view>

    <!-- Recharge Amounts -->
    <view class="section recharge-section">
      <text class="section-title">快速充值</text>
      <view class="recharge-grid">
        <view 
          v-for="(amount, index) in quickAmounts" 
          :key="index"
          class="recharge-item"
          :class="{ active: selectedAmount === amount.value }"
          @click="selectAmount(amount.value)"
        >
          <text class="recharge-amount">¥{{ amount.value }}</text>
          <text class="recharge-bonus" v-if="amount.bonus">送¥{{ amount.bonus }}</text>
          <text class="recharge-text">{{ amount.text }}</text>
        </view>
      </view>
    </view>

    <!-- Custom Amount -->
    <view class="section custom-section">
      <text class="section-title">自定义金额</text>
      <view class="custom-input">
        <text class="currency-symbol">¥</text>
        <input 
          class="amount-input" 
          type="digit" 
          v-model="customAmount"
          placeholder="请输入充值金额"
          @input="onCustomAmountInput"
        />
      </view>
      <view class="recommendations">
        <text class="recommend-text">推荐：单次充值50元以上享受9.8折优惠</text>
      </view>
    </view>

    <!-- Payment Methods -->
    <view class="section payment-section">
      <text class="section-title">支付方式</text>
      <view class="payment-list">
        <view 
          v-for="(method, index) in paymentMethods" 
          :key="index"
          class="payment-item"
          :class="{ active: selectedMethod === index }"
          @click="selectPaymentMethod(index)"
        >
          <view class="payment-icon" :class="method.iconClass">
            <uni-icons :type="method.iconType" size="22" :color="selectedMethod === index ? '#3cc51f' : '#475569'"></uni-icons>
          </view>
          <view class="payment-info">
            <text class="payment-name">{{ method.name }}</text>
            <text class="payment-desc">{{ method.desc }}</text>
          </view>
          <view class="radio" :class="{ checked: selectedMethod === index }">
            <uni-icons :type="selectedMethod === index ? 'checkbox-filled' : 'circle'" size="18" :color="selectedMethod === index ? '#3cc51f' : '#94a3b8'"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- Promotion Banner -->
    <view class="promotion-banner" @click="handlePromotion">
      <view class="banner-content">
        <text class="banner-title">充值优惠</text>
        <text class="banner-desc">充值满100送10，满200送25，多充多送！</text>
        <text class="banner-action">立即参与 ›</text>
      </view>
    </view>
  </MobileLayout>

  <!-- Action Bar -->
  <view class="action-bar">
    <view class="total-display">
      <text class="total-label">充值金额</text>
      <text class="total-amount">¥{{ currentAmount }}</text>
    </view>
    <button class="recharge-btn" :disabled="!canRecharge" @click="handleConfirmRecharge">
      立即充值
    </button>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { createRechargeOrder } from '@/api/recharge'
import { getMemberInfo } from '@/api/member'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const balance = ref<number>(0)
const selectedAmount = ref<number | null>(null)
const customAmount = ref<string>('')
const selectedMethod = ref<number>(0)
const quickAmounts = ref([
  { value: 30, text: '小额度', bonus: 2 },
  { value: 50, text: '常用', bonus: 5 },
  { value: 100, text: '推荐', bonus: 12 },
  { value: 200, text: '超值', bonus: 30 },
  { value: 300, text: '大额', bonus: 50 },
  { value: 500, text: '豪华', bonus: 100 }
])
const paymentMethods = ref([
  { name: '微信支付', desc: '安全便捷', iconType: 'weixin', iconClass: 'wechat' },
  { name: '支付宝', desc: '安全可靠', iconType: 'wallet', iconClass: 'alipay' },
  { name: '银行卡', desc: '直接扣款', iconType: 'wallet', iconClass: 'bank' }
])

const userStore = useUserStore()

// 计算属性
const currentAmount = computed(() => {
  if (selectedAmount.value) {
    return selectedAmount.value
  }
  if (customAmount.value) {
    return parseFloat(customAmount.value) || 0
  }
  return 0
})

const canRecharge = computed(() => {
  return currentAmount.value > 0 && selectedMethod !== -1
})

// 选择金额
const selectAmount = (amount: number) => {
  selectedAmount.value = amount
  customAmount.value = ''
}

// 自定义金额输入
const onCustomAmountInput = (e: any) => {
  const value = e.target.value
  // 验证输入的金额是否合法
  if (value === '' || /^\d*\.?\d*$/.test(value)) {
    customAmount.value = value
    selectedAmount.value = null
  }
}

// 选择支付方式
const selectPaymentMethod = (index: number) => {
  selectedMethod.value = index
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const result = await getMemberInfo(userStore.userId)
    balance.value = result.balance || 0
  } catch (error) {
    console.error('加载用户信息失败:', error)
    uni.showToast({
      title: '加载用户信息失败',
      icon: 'none'
    })
  }
}

// 立即充值
const handleRecharge = () => {
  // 这个方法会在用户点击"立即充值"按钮时触发
  if (!canRecharge.value) {
    uni.showToast({
      title: '请选择充值金额',
      icon: 'none'
    })
    return
  }
  
  confirmRecharge()
}

// 确认充值
const confirmRecharge = async () => {
  try {
    uni.showLoading({
      title: '创建订单...'
    })

    const paymentMethod = paymentMethods.value[selectedMethod.value].name
    const methodCode = paymentMethod === '微信支付' ? 'WECHAT' : 
                      paymentMethod === '支付宝' ? 'ALIPAY' : 'BANKCARD'

    const rechargeData = {
      memberId: userStore.userId,
      amount: currentAmount.value,
      paymentMethod: methodCode,
      orderType: 'RECHARGE'
    }

    const result = await createRechargeOrder(rechargeData)

    uni.hideLoading()
    
    // 这里应该调用相应的支付接口
    if (methodCode === 'WECHAT') {
      // 调用微信支付
      uni.requestPayment({
        provider: 'wxpay',
        orderId: result.orderId,
        success: (res) => {
          uni.showToast({
            title: '充值成功',
            icon: 'success'
          })
          // 刷新余额
          loadUserInfo()
        },
        fail: (err) => {
          console.error('支付失败:', err)
          uni.showToast({
            title: '支付失败',
            icon: 'none'
          })
        }
      })
    } else if (methodCode === 'ALIPAY') {
      // 调用支付宝支付
      // 这里需要实现支付宝支付逻辑
    }
  } catch (error) {
    console.error('充值失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '充值失败',
      icon: 'none'
    })
  }
}

// 确认充值（从按钮触发）
const handleConfirmRecharge = () => {
  handleRecharge()
}

// 查看充值记录
const handleRecords = () => {
  uni.navigateTo({
    url: '/pages/recharge/records'
  })
}

// 优惠活动
const handlePromotion = () => {
  uni.showModal({
    title: '充值优惠',
    content: '充值满100送10，满200送25，多充多送！',
    showCancel: false,
    confirmText: '我知道了'
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
  
  await loadUserInfo()
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

.balance-card {
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  margin: 24rpx 28rpx;
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(60, 197, 31, 0.3);
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #ffffff;
}

.balance-info {
  text-align: center;
  margin-bottom: 32rpx;
}

.balance-label {
  font-size: 24rpx;
  opacity: 0.8;
  display: block;
  margin-bottom: 8rpx;
}

.balance-amount {
  font-size: 56rpx;
  font-weight: bold;
}

.balance-actions {
  display: flex;
  gap: 32rpx;
}

.balance-action {
  font-size: 24rpx;
  padding: 8rpx 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  border-radius: 9999rpx;
}

.section {
  background-color: #ffffff;
  margin: 20rpx 28rpx;
  padding: 28rpx;
  border-radius: 18rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
  display: block;
}

.recharge-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.recharge-item {
  background-color: #f5f5f5;
  border-radius: 12rpx;
  padding: 24rpx 16rpx;
  text-align: center;
  transition: all 0.3s;
  
  &.active {
    background-color: #3cc51f;
    color: #ffffff;
  }
}

.recharge-amount {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  display: block;
  margin-bottom: 8rpx;
  
  .recharge-item.active & {
    color: #ffffff;
  }
}

.recharge-bonus {
  font-size: 20rpx;
  color: #ef4444;
  display: block;
  margin-bottom: 8rpx;
  
  .recharge-item.active & {
    color: rgba(255, 255, 255, 0.8);
  }
}

.recharge-text {
  font-size: 20rpx;
  color: #999999;
  
  .recharge-item.active & {
    color: rgba(255, 255, 255, 0.8);
  }
}

.custom-section {
  .custom-input {
    display: flex;
    align-items: center;
    background-color: #f5f5f5;
    border-radius: 12rpx;
    padding: 0 20rpx;
  }

  .currency-symbol {
    font-size: 32rpx;
    color: #333333;
    margin-right: 8rpx;
  }

  .amount-input {
    flex: 1;
    height: 80rpx;
    font-size: 32rpx;
    color: #333333;
  }
}

.recommendations {
  margin-top: 16rpx;
}

.recommend-text {
  font-size: 20rpx;
  color: #999999;
}

.payment-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.payment-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 12rpx;
  transition: all 0.3s;
  
  &.active {
    border-color: #3cc51f;
    background-color: #f0f9f2;
  }
}

.payment-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  font-size: 32rpx;
  
  &.wechat {
    background-color: #e3f2fd;
    color: #2196f3;
  }
  
  &.alipay {
    background-color: #fff3e0;
    color: #ff9800;
  }
  
  &.bank {
    background-color: #e8f5e9;
    color: #4caf50;
  }
}

.payment-info {
  flex: 1;
}

.payment-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
  display: block;
  margin-bottom: 4rpx;
}

.payment-desc {
  font-size: 22rpx;
  color: #999999;
}

.radio {
  font-size: 40rpx;
  color: #e6e6e6;
  
  &.checked {
    color: #3cc51f;
  }
}

.promotion-banner {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  margin: 20rpx 28rpx;
  border-radius: 18rpx;
  padding: 28rpx;
  color: #ffffff;
}

.banner-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.banner-title {
  font-size: 26rpx;
  font-weight: bold;
  display: block;
}

.banner-desc {
  font-size: 22rpx;
  opacity: 0.9;
  display: block;
  margin-bottom: 8rpx;
}

.banner-action {
  font-size: 22rpx;
  align-self: flex-end;
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

.total-display {
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

.recharge-btn {
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