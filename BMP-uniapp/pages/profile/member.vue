<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">会员信息</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Member Card -->
      <view class="member-card">
        <view class="member-header">
          <view class="member-avatar">
            <uni-icons type="vip" size="28" color="#f59e0b" class="avatar-icon"></uni-icons>
          </view>
          <view class="member-info">
            <text class="member-name">{{ memberInfo.memberName || '未设置' }}</text>
            <text class="member-type">{{ getMemberTypeText(memberInfo.memberType) }}</text>
          </view>
        </view>
        <view class="member-stats">
          <view class="stat-item">
            <text class="stat-value">¥{{ formatCurrency(memberInfo.balance || 0) }}</text>
            <text class="stat-label">账户余额</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ memberInfo.memberLevel || 1 }}</text>
            <text class="stat-label">会员等级</text>
          </view>
        </view>
      </view>

      <!-- Member Details -->
      <view class="section details-section">
        <view class="detail-item">
          <text class="detail-label">会员姓名</text>
          <text class="detail-value">{{ memberInfo.memberName || '未设置' }}</text>
        </view>
        <view class="divider"></view>
        <view class="detail-item">
          <text class="detail-label">手机号</text>
          <text class="detail-value">{{ memberInfo.phone || '未设置' }}</text>
        </view>
        <view class="divider"></view>
        <view class="detail-item">
          <text class="detail-label">会员类型</text>
          <text class="detail-value">{{ getMemberTypeText(memberInfo.memberType) }}</text>
        </view>
        <view class="divider"></view>
        <view class="detail-item">
          <text class="detail-label">会员等级</text>
          <text class="detail-value">Lv.{{ memberInfo.memberLevel || 1 }}</text>
        </view>
        <view class="divider"></view>
        <view class="detail-item">
          <text class="detail-label">账户余额</text>
          <text class="detail-value balance">¥{{ formatCurrency(memberInfo.balance || 0) }}</text>
        </view>
        <view class="divider"></view>
        <view class="detail-item">
          <text class="detail-label">注册时间</text>
          <text class="detail-value">{{ formatDate(memberInfo.createTime) }}</text>
        </view>
      </view>

      <!-- Member Benefits -->
      <view class="section benefits-section">
        <text class="section-title">会员权益</text>
        <view class="benefits-list">
          <view class="benefit-item">
            <uni-icons type="gift" size="20" color="#3cc51f" class="benefit-icon"></uni-icons>
            <text class="benefit-text">订场享受会员折扣</text>
          </view>
          <view class="benefit-item">
            <uni-icons type="star-filled" size="20" color="#3cc51f" class="benefit-icon"></uni-icons>
            <text class="benefit-text">优先预订热门场地</text>
          </view>
          <view class="benefit-item">
            <uni-icons type="medal" size="20" color="#3cc51f" class="benefit-icon"></uni-icons>
            <text class="benefit-text">专属客服服务</text>
          </view>
        </view>
      </view>

      <!-- Actions -->
      <view class="actions-section">
        <button class="recharge-btn" @click="handleRecharge">
          立即充值
        </button>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getMemberInfo } from '@/api/member'
import { safeNavigateBack } from '@/utils/navigation'

const userStore = useUserStore()

// 会员信息
const memberInfo = reactive({
  id: 0,
  memberName: '',
  gender: 0,
  phone: '',
  idCard: '',
  memberType: '',
  memberLevel: 1,
  balance: 0,
  status: 1,
  createTime: ''
})

// 获取会员类型文本
const getMemberTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    'NORMAL': '普通会员',
    'VIP': 'VIP会员',
    'GOLD': '黄金会员',
    'PLATINUM': '白金会员',
    'DIAMOND': '钻石会员'
  }
  return typeMap[type] || '普通会员'
}

// 格式化金额
const formatCurrency = (amount: number) => {
  return amount.toFixed(2)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 加载会员信息
const loadMemberInfo = async () => {
  try {
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

    const info = await getMemberInfo(userStore.userId)
    Object.assign(memberInfo, info)
  } catch (error) {
    console.error('加载会员信息失败:', error)
    uni.showToast({
      title: '加载会员信息失败',
      icon: 'none'
    })
  }
}

// 跳转到充值页面
const handleRecharge = () => {
  uni.navigateTo({
    url: '/pages/recharge/index'
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
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  await loadMemberInfo()
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
  padding: 24rpx;
}

.member-card {
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(60, 197, 31, 0.3);
}

.member-header {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.member-avatar {
  width: 96rpx;
  height: 96rpx;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid rgba(255, 255, 255, 0.3);
}

.avatar-icon {
  font-size: 48rpx;
  color: #ffffff;
}

.member-info {
  flex: 1;
  color: #ffffff;
}

.member-name {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.member-type {
  font-size: 24rpx;
  opacity: 0.9;
  display: block;
}

.member-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 24rpx;
  border-top: 1rpx solid rgba(255, 255, 255, 0.2);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
}

.stat-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
}

.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background-color: rgba(255, 255, 255, 0.2);
}

.section {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.details-section {
  padding: 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
}

.detail-label {
  font-size: 24rpx;
  color: #666666;
}

.detail-value {
  font-size: 24rpx;
  color: #333333;
  font-weight: 500;
  
  &.balance {
    color: #3cc51f;
    font-weight: bold;
  }
}

.divider {
  height: 1rpx;
  background-color: #f3f4f6;
  margin: 0 32rpx;
}

.benefits-section {
  .section-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333333;
    display: block;
    margin-bottom: 24rpx;
  }
}

.benefits-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.benefit-icon {
  font-size: 32rpx;
}

.benefit-text {
  font-size: 24rpx;
  color: #666666;
}

.actions-section {
  margin-top: 24rpx;
}

.recharge-btn {
  width: 100%;
  background: linear-gradient(135deg, #3cc51f 0%, #4ade80 100%);
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
  padding: 24rpx;
  border-radius: 18rpx;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(60, 197, 31, 0.3);
}
</style>
