<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">报名确认</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Tournament Info -->
      <view class="section tournament-info">
        <view class="tournament-card">
          <view class="tournament-image">
            <text class="image-placeholder">Tournament Image</text>
          </view>
          <view class="tournament-details">
            <text class="tournament-name">{{ tournament.name }}</text>
            <view class="tournament-date">
              <uni-icons type="calendar" size="14" color="#475569"></uni-icons>
              <text>{{ tournament.date }} {{ tournament.time }}</text>
            </view>
            <view class="tournament-fee">
              <uni-icons type="wallet" size="14" color="#475569"></uni-icons>
              <text>报名费: ¥{{ tournament.fee }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Registration Form -->
      <view class="section form-section">
        <view class="form-item">
          <text class="form-label">姓名</text>
          <input 
            class="form-input" 
            type="text" 
            v-model="registrationInfo.name"
            placeholder="请输入真实姓名"
          />
        </view>
        <view class="form-item">
          <text class="form-label">手机号</text>
          <input 
            class="form-input" 
            type="number" 
            v-model="registrationInfo.phone"
            placeholder="请输入手机号码"
          />
        </view>
        <view class="form-item">
          <text class="form-label">性别</text>
          <picker mode="selector" :range="genders" @change="onGenderChange">
            <view class="picker">
              <text class="picker-value">{{ registrationInfo.gender ? '男' : '女' }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="form-label">年龄</text>
          <input 
            class="form-input" 
            type="number" 
            v-model="registrationInfo.age"
            placeholder="请输入年龄"
          />
        </view>
        <view class="form-item">
          <text class="form-label">技术水平</text>
          <picker mode="selector" :range="skillLevels" @change="onSkillLevelChange">
            <view class="picker">
              <text class="picker-value">{{ registrationInfo.skillLevel || '请选择' }}</text>
              <text class="chevron">›</text>
            </view>
          </picker>
        </view>
        <view class="form-item">
          <text class="form-label">紧急联系人</text>
          <input 
            class="form-input" 
            type="text" 
            v-model="registrationInfo.emergencyContact"
            placeholder="请输入紧急联系人姓名"
          />
        </view>
        <view class="form-item">
          <text class="form-label">紧急联系电话</text>
          <input 
            class="form-input" 
            type="number" 
            v-model="registrationInfo.emergencyPhone"
            placeholder="请输入紧急联系电话"
          />
        </view>
      </view>

      <!-- Agreement -->
      <view class="section agreement-section">
        <view class="agreement-item">
          <text class="agreement-title">报名须知</text>
          <view class="agreement-content">
            <text class="agreement-text">1. 参赛者必须身体健康，无心脏病等不适合剧烈运动的疾病</text>
            <text class="agreement-text">2. 参赛者需遵守比赛规则和裁判判罚</text>
            <text class="agreement-text">3. 如有身体不适，应立即停止比赛并寻求医疗帮助</text>
            <text class="agreement-text">4. 主办方有权根据实际情况调整比赛安排</text>
          </view>
          <view class="checkbox-container" @click="toggleAgreement">
            <view class="checkbox" :class="{ checked: agreementChecked }"><uni-icons :type="agreementChecked ? 'checkbox-filled' : 'circle'" size="18" :color="agreementChecked ? '#3cc51f' : '#94a3b8'"></uni-icons></view>
            <text class="checkbox-label">我已阅读并同意以上条款</text>
          </view>
        </view>
      </view>

      <!-- Payment -->
      <view class="section payment-section">
        <view class="payment-method">
          <text class="method-label">支付方式</text>
          <view class="method-options">
            <view 
              v-for="(method, index) in paymentMethods" 
              :key="index"
              class="method-item"
              :class="{ active: selectedPaymentMethod === index }"
              @click="selectPaymentMethod(index)"
            >
              <uni-icons :type="method.iconType" size="20" :color="selectedPaymentMethod === index ? '#3cc51f' : '#475569'" class="method-icon"></uni-icons>
              <text class="method-name">{{ method.name }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Price Summary -->
      <view class="section price-summary">
        <view class="summary-item">
          <text class="summary-label">报名费</text>
          <text class="summary-value">¥{{ tournament.fee }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">优惠</text>
          <text class="summary-value">-¥0</text>
        </view>
        <view class="summary-divider"></view>
        <view class="summary-item total">
          <text class="summary-label">总计</text>
          <text class="summary-value total-price">¥{{ tournament.fee }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Action Bar -->
    <view class="action-bar">
      <view class="total-price-display">
        <text class="total-label">应付</text>
        <text class="total-amount">¥{{ tournament.fee }}</text>
      </view>
      <button class="submit-btn" :disabled="!canSubmit || isSubmitting" @click="handleSubmit">
        {{ isSubmitting ? '报名中...' : '确认报名' }}
      </button>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getTournamentDetail } from '@/api/tournament'
import { createTournamentRegistration } from '@/api/tournament'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const tournamentId = ref<number>(0)
const tournament = ref<any>({
  id: 0,
  name: '',
  date: '',
  time: '',
  fee: 0
})

const registrationInfo = ref({
  name: '',
  phone: '',
  gender: 1,
  age: 0,
  skillLevel: '',
  emergencyContact: '',
  emergencyPhone: ''
})

const agreementChecked = ref(false)
const selectedPaymentMethod = ref(0)
const isSubmitting = ref(false)
const genders = ['女', '男']
const skillLevels = ['初学者', '中级', '高级', '专业']
const paymentMethods = [
  { name: '余额支付', iconType: 'wallet' },
  { name: '微信支付', iconType: 'weixin' },
  { name: '支付宝', iconType: 'wallet' }
]

const userStore = useUserStore()

// 计算属性
const canSubmit = computed(() => {
  return registrationInfo.value.name &&
         registrationInfo.value.phone &&
         registrationInfo.value.emergencyContact &&
         registrationInfo.value.emergencyPhone &&
         agreementChecked.value &&
         tournament.value.id > 0
})

const getTournamentErrorMessage = (error: any): string => {
  const rawMsg = String(error?.message || error?.errMsg || '')
  const msg = rawMsg.toLowerCase()

  if (msg.includes('full') || rawMsg.includes('满员') || rawMsg.includes('名额')) {
    return '赛事名额已满，请关注后续场次'
  }
  if (msg.includes('duplicate') || rawMsg.includes('重复') || rawMsg.includes('已报名')) {
    return '您已报名该赛事，请勿重复提交'
  }
  if (msg.includes('expired') || rawMsg.includes('截止') || rawMsg.includes('结束')) {
    return '赛事报名已截止'
  }
  return rawMsg || '报名失败，请重试'
}

// 页面加载
onLoad((options) => {
  if (options.id) {
    tournamentId.value = Number(options.id)
  }
})

// 加载赛事详情
const loadTournamentDetail = async () => {
  try {
    const result = await getTournamentDetail(tournamentId.value)
    
    tournament.value = {
      id: result.id,
      name: result.tournamentName,
      date: result.startDate,
      time: result.startTime,
      fee: result.entryFee || 0
    }
  } catch (error) {
    console.error('加载赛事详情失败:', error)
    uni.showToast({
      title: '加载赛事详情失败',
      icon: 'none'
    })
  }
}

// 性别变化
const onGenderChange = (e: any) => {
  registrationInfo.value.gender = parseInt(e.detail.value)
}

// 技术水平变化
const onSkillLevelChange = (e: any) => {
  registrationInfo.value.skillLevel = skillLevels[parseInt(e.detail.value)]
}

// 切换协议勾选
const toggleAgreement = () => {
  agreementChecked.value = !agreementChecked.value
}

// 选择支付方式
const selectPaymentMethod = (index: number) => {
  selectedPaymentMethod.value = index
}

// 提交报名
const handleSubmit = async () => {
  if (isSubmitting.value) return

  if (!canSubmit.value) {
    uni.showToast({
      title: '请完善报名信息',
      icon: 'none'
    })
    return
  }

  try {
    isSubmitting.value = true
    uni.showLoading({
      title: '报名中...'
    })

    const registrationData = {
      memberId: userStore.userId,
      tournamentId: tournament.value.id,
      name: registrationInfo.value.name,
      phone: registrationInfo.value.phone,
      gender: registrationInfo.value.gender,
      age: registrationInfo.value.age,
      skillLevel: registrationInfo.value.skillLevel,
      emergencyContact: registrationInfo.value.emergencyContact,
      emergencyPhone: registrationInfo.value.emergencyPhone,
      orderAmount: tournament.value.fee,
      paymentMethod: paymentMethods[selectedPaymentMethod.value].name === '余额支付' ? 'BALANCE' : 
                   paymentMethods[selectedPaymentMethod.value].name === '微信支付' ? 'WECHAT' : 'ALIPAY'
    }

    const result = await createTournamentRegistration(registrationData)

    uni.hideLoading()
    uni.showToast({
      title: '报名成功',
      icon: 'success'
    })

    // 延迟跳转到赛事详情页
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/tournament/detail?id=${tournament.value.id}`
      })
    }, 1500)
  } catch (error) {
    console.error('赛事报名失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: getTournamentErrorMessage(error),
      icon: 'none'
    })
  } finally {
    isSubmitting.value = false
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
  
  if (tournamentId.value) {
    await loadTournamentDetail()
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

.tournament-info {
  .tournament-card {
    display: flex;
    gap: 20rpx;
  }

  .tournament-image {
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

  .tournament-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .tournament-name {
    font-size: 26rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 8rpx;
  }

  .tournament-date {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 22rpx;
    color: #475569;
    margin-bottom: 8rpx;
  }
  .tournament-date text { flex: 1; }

  .tournament-fee {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 24rpx;
    font-weight: bold;
    color: #ef4444;
  }
  .tournament-fee text { flex: 1; }
}

.form-section {
  .form-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 0;
    border-bottom: 1rpx solid #f3f4f6;

    &:last-child {
      border-bottom: none;
    }
  }

  .form-label {
    font-size: 24rpx;
    color: #333333;
    width: 140rpx;
  }

  .form-input {
    flex: 1;
    font-size: 24rpx;
    color: #333333;
    text-align: right;
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
    margin-bottom: 20rpx;
  }

  .agreement-text {
    font-size: 22rpx;
    color: #999999;
    line-height: 1.5;
  }

  .checkbox-container {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  .checkbox {
    width: 36rpx;
    height: 36rpx;
    border: 2rpx solid #ccc;
    border-radius: 6rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20rpx;
    color: transparent;
    
    &.checked {
      border-color: #3cc51f;
      background-color: #3cc51f;
      color: #ffffff;
    }
  }

  .checkbox-label {
    font-size: 22rpx;
    color: #666666;
  }
}

.payment-section {
  .payment-method {
    padding: 16rpx 0;
  }

  .method-label {
    font-size: 24rpx;
    font-weight: bold;
    color: #333333;
    margin-bottom: 16rpx;
    display: block;
  }

  .method-options {
    display: flex;
    gap: 20rpx;
  }

  .method-item {
    flex: 1;
    padding: 16rpx;
    border: 2rpx solid #e6e6e6;
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    transition: all 0.3s;
    
    &.active {
      border-color: #3cc51f;
      background-color: #f0f9f2;
    }
  }

  .method-icon {
    font-size: 32rpx;
    margin-bottom: 8rpx;
  }

  .method-name {
    font-size: 22rpx;
    color: #333333;
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