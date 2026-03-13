<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">帮助与反馈</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Quick Actions -->
      <view class="section quick-actions">
        <view class="action-grid">
          <view class="action-item" @click="handleFeedback">
            <view class="action-icon feedback-icon">
              <uni-icons type="compose" size="22" color="#3cc51f"></uni-icons>
            </view>
            <text class="action-name">意见反馈</text>
          </view>
          <view class="action-item" @click="handleReport">
            <view class="action-icon report-icon">
              <uni-icons type="flag" size="22" color="#3cc51f"></uni-icons>
            </view>
            <text class="action-name">问题举报</text>
          </view>
          <view class="action-item" @click="handleCustomerService">
            <view class="action-icon service-icon">
              <uni-icons type="chatbubble" size="22" color="#3cc51f"></uni-icons>
            </view>
            <text class="action-name">在线客服</text>
          </view>
          <view class="action-item" @click="handleCall">
            <view class="action-icon phone-icon">
              <uni-icons type="phone" size="22" color="#3cc51f"></uni-icons>
            </view>
            <text class="action-name">电话客服</text>
          </view>
        </view>
      </view>

      <!-- FAQ -->
      <view class="section faq-section">
        <text class="section-title">常见问题</text>
        
        <view 
          v-for="(faq, index) in faqs" 
          :key="index"
          class="faq-item"
          @click="toggleFaq(index)"
        >
          <view class="faq-header">
            <text class="faq-question">{{ faq.question }}</text>
            <text class="faq-toggle" :class="{ expanded: faq.expanded }">›</text>
          </view>
          <view v-if="faq.expanded" class="faq-answer">
            <text class="answer-text">{{ faq.answer }}</text>
          </view>
        </view>
      </view>

      <!-- Help Categories -->
      <view class="section categories-section">
        <text class="section-title">帮助分类</text>
        
        <view class="category-item" @click="handleCategory('account')">
          <view class="category-left">
            <view class="category-icon account-icon">
              <uni-icons type="person" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="category-name">账户相关</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="category-item" @click="handleCategory('booking')">
          <view class="category-left">
            <view class="category-icon booking-icon">
              <uni-icons type="calendar" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="category-name">预约相关</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="category-item" @click="handleCategory('payment')">
          <view class="category-left">
            <view class="category-icon payment-icon">
              <uni-icons type="wallet" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="category-name">支付相关</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="category-item" @click="handleCategory('refund')">
          <view class="category-left">
            <view class="category-icon refund-icon">
              <uni-icons type="wallet" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="category-name">退款相关</text>
          </view>
          <text class="chevron">›</text>
        </view>

        <view class="divider"></view>

        <view class="category-item" @click="handleCategory('other')">
          <view class="category-left">
            <view class="category-icon other-icon">
              <uni-icons type="help" size="20" color="#3cc51f"></uni-icons>
            </view>
            <text class="category-name">其他问题</text>
          </view>
          <text class="chevron">›</text>
        </view>
      </view>

      <!-- Feedback Form -->
      <view class="section feedback-section">
        <text class="section-title">提交反馈</text>
        
        <view class="form-group">
          <text class="form-label">反馈类型</text>
          <picker mode="selector" :range="feedbackTypes" @change="onTypeChange">
            <view class="picker">
              <text class="picker-text">{{ selectedType || '请选择反馈类型' }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>

        <view class="form-group">
          <text class="form-label">问题描述</text>
          <textarea 
            class="form-textarea"
            v-model="feedbackContent"
            placeholder="请详细描述您遇到的问题或建议..."
            maxlength="500"
          />
          <text class="char-count">{{ feedbackContent.length }}/500</text>
        </view>

        <view class="form-group">
          <text class="form-label">联系方式（选填）</text>
          <input 
            class="form-input"
            type="text"
            v-model="contactInfo"
            placeholder="手机号或邮箱，方便我们联系您"
          />
        </view>

        <button class="submit-btn" @click="handleSubmitFeedback">
          提交反馈
        </button>
      </view>

      <!-- Contact Info -->
      <view class="section contact-section">
        <text class="section-title">联系我们</text>
        <view class="contact-info">
          <view class="contact-item">
            <text class="contact-label">客服热线</text>
            <text class="contact-value" @click="handleCall">400-888-8888</text>
          </view>
          <view class="contact-item">
            <text class="contact-label">服务时间</text>
            <text class="contact-value">09:00 - 21:00</text>
          </view>
          <view class="contact-item">
            <text class="contact-label">客服邮箱</text>
            <text class="contact-value" @click="handleCopyEmail">support@bmp.com</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'

const userStore = useUserStore()

// FAQ数据
const faqs = reactive([
  {
    question: '如何预约场地？',
    answer: '进入首页后，点击"场馆预订"，选择您想要的场馆和时间段，确认信息后完成支付即可。',
    expanded: false
  },
  {
    question: '预约后可以取消吗？',
    answer: '可以的。在预约开始前2小时可以免费取消，2小时内取消将收取一定的手续费。',
    expanded: false
  },
  {
    question: '如何充值会员余额？',
    answer: '进入"我的"页面，点击余额区域进入充值页面，选择充值金额后通过微信支付完成充值。',
    expanded: false
  },
  {
    question: '忘记密码怎么办？',
    answer: '在登录页面点击"忘记密码"，通过绑定的手机号验证后可以重置密码。',
    expanded: false
  },
  {
    question: '如何修改个人信息？',
    answer: '进入"我的"-"个人信息"页面，可以修改头像、昵称、手机号等个人信息。',
    expanded: false
  }
])

// 反馈表单
const feedbackTypes = ['功能建议', '问题反馈', 'Bug报告', '使用疑问', '其他']
const selectedType = ref('')
const feedbackContent = ref('')
const contactInfo = ref('')

// 展开/收起FAQ
const toggleFaq = (index: number) => {
  faqs[index].expanded = !faqs[index].expanded
}

// 选择反馈类型
const onTypeChange = (e: any) => {
  selectedType.value = feedbackTypes[e.detail.value]
}

// 提交反馈
const handleSubmitFeedback = () => {
  if (!selectedType.value) {
    uni.showToast({
      title: '请选择反馈类型',
      icon: 'none'
    })
    return
  }

  if (!feedbackContent.value.trim()) {
    uni.showToast({
      title: '请输入问题描述',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '提交中...'
  })

  // 模拟提交
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '反馈提交成功',
      icon: 'success'
    })
    
    // 重置表单
    selectedType.value = ''
    feedbackContent.value = ''
    contactInfo.value = ''
  }, 1500)
}

// 意见反馈
const handleFeedback = () => {
  // 滚动到反馈表单
  uni.pageScrollTo({
    selector: '.feedback-section',
    duration: 300
  })
}

// 问题举报
const handleReport = () => {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

// 在线客服
const handleCustomerService = () => {
  uni.showModal({
    title: '在线客服',
    content: '客服工作时间: 09:00 - 21:00\n如需帮助请拨打客服热线',
    showCancel: false
  })
}

// 拨打客服电话
const handleCall = () => {
  uni.makePhoneCall({
    phoneNumber: '4008888888',
    fail: () => {
      uni.showToast({
        title: '拨打失败',
        icon: 'none'
      })
    }
  })
}

// 帮助分类
const handleCategory = (category: string) => {
  const categoryNames: Record<string, string> = {
    account: '账户相关',
    booking: '预约相关',
    payment: '支付相关',
    refund: '退款相关',
    other: '其他问题'
  }
  
  uni.showToast({
    title: `${categoryNames[category]}帮助开发中`,
    icon: 'none'
  })
}

// 复制邮箱
const handleCopyEmail = () => {
  uni.setClipboardData({
    data: 'support@bmp.com',
    success: () => {
      uni.showToast({
        title: '邮箱已复制',
        icon: 'success'
      })
    }
  })
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 页面加载
onMounted(() => {
  // 可以添加初始化逻辑
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
  margin: 24rpx;
  border-radius: 24rpx;
  overflow: hidden;
}

.section-title {
  display: block;
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
  padding: 24rpx 28rpx 16rpx;
}

.quick-actions {
  padding: 28rpx;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.action-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.feedback-icon {
    background-color: #e8f5e9;
  }
  
  &.report-icon {
    background-color: #ffebee;
  }
  
  &.service-icon {
    background-color: #e3f2fd;
  }
  
  &.phone-icon {
    background-color: #fff3e0;
  }
}

.icon-text {
  font-size: 32rpx;
}

.action-name {
  font-size: 22rpx;
  color: #666666;
}

.faq-item {
  border-bottom: 1rpx solid #f3f4f6;
  
  &:last-child {
    border-bottom: none;
  }
}

.faq-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  
  &:active {
    background-color: #f9fafb;
  }
}

.faq-question {
  font-size: 24rpx;
  color: #333333;
  flex: 1;
}

.faq-toggle {
  font-size: 26rpx;
  color: #999999;
  transition: transform 0.3s;
  
  &.expanded {
    transform: rotate(90deg);
  }
}

.faq-answer {
  padding: 0 28rpx 24rpx;
  background-color: #f9fafb;
}

.answer-text {
  font-size: 22rpx;
  color: #666666;
  line-height: 1.6;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 28rpx;
  transition: background-color 0.2s;
  
  &:active {
    background-color: #f9fafb;
  }
}

.category-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.category-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.account-icon {
    background-color: #e8f5e9;
  }
  
  &.booking-icon {
    background-color: #e3f2fd;
  }
  
  &.payment-icon {
    background-color: #fff3e0;
  }
  
  &.refund-icon {
    background-color: #fce4ec;
  }
  
  &.other-icon {
    background-color: #f3e5f5;
  }
}

.category-name {
  font-size: 24rpx;
  color: #333333;
}

.chevron {
  font-size: 26rpx;
  color: #999999;
}

.divider {
  height: 1rpx;
  background-color: #f3f4f6;
  margin: 0 28rpx;
}

.feedback-section {
  padding-bottom: 28rpx;
}

.form-group {
  padding: 0 28rpx 20rpx;
}

.form-label {
  font-size: 24rpx;
  color: #333333;
  margin-bottom: 12rpx;
  display: block;
}

.picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
}

.picker-text {
  font-size: 24rpx;
  color: #666666;
}

.form-textarea {
  width: 100%;
  height: 200rpx;
  padding: 20rpx 24rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #333333;
  box-sizing: border-box;
}

.char-count {
  font-size: 20rpx;
  color: #999999;
  text-align: right;
  display: block;
  margin-top: 8rpx;
}

.form-input {
  width: 100%;
  padding: 20rpx 24rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #333333;
  box-sizing: border-box;
}

.submit-btn {
  width: calc(100% - 56rpx);
  margin: 20rpx 28rpx 0;
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
  padding: 24rpx;
  border-radius: 12rpx;
  border: none;
}

.contact-section {
  padding-bottom: 28rpx;
}

.contact-info {
  padding: 0 28rpx;
}

.contact-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
  
  &:last-child {
    border-bottom: none;
  }
}

.contact-label {
  font-size: 24rpx;
  color: #666666;
}

.contact-value {
  font-size: 24rpx;
  color: #3cc51f;
}
</style>
