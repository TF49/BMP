<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">新增穿线服务</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <!-- Content -->
    <scroll-view class="content" scroll-y>
      <!-- Racket Info Section -->
      <view class="section">
        <text class="section-title">球拍信息</text>
        <view class="form-group">
          <view class="form-item">
            <text class="form-label required">球拍品牌</text>
            <input 
              class="form-input" 
              v-model="formData.racketBrand" 
              placeholder="请输入球拍品牌"
              maxlength="50"
              @blur="validateField('racketBrand')"
            />
          </view>
          <text v-if="errors.racketBrand" class="error-text">{{ errors.racketBrand }}</text>
          
          <view class="form-item">
            <text class="form-label required">球拍型号</text>
            <input 
              class="form-input" 
              v-model="formData.racketModel" 
              placeholder="请输入球拍型号"
              maxlength="50"
              @blur="validateField('racketModel')"
            />
          </view>
          <text v-if="errors.racketModel" class="error-text">{{ errors.racketModel }}</text>
        </view>
      </view>

      <!-- String Selection Section -->
      <view class="section">
        <text class="section-title">线材选择</text>
        
        <!-- Own String Toggle -->
        <view class="form-group">
          <view class="form-item checkbox-item">
            <checkbox-group @change="handleOwnStringChange">
              <label class="checkbox-label">
                <checkbox :checked="formData.ownString" color="#3cc51f" />
                <text class="checkbox-text">自带线材</text>
              </label>
            </checkbox-group>
          </view>
        </view>

        <!-- String Selector (if not own string) -->
        <view v-if="!formData.ownString" class="form-group">
          <view class="form-item">
            <text class="form-label required">选择线材</text>
            <picker 
              mode="selector" 
              :range="stringList" 
              range-key="displayName"
              @change="handleStringChange"
              :value="selectedStringIndex"
            >
              <view class="picker">
                <text class="picker-value" :class="{ placeholder: !selectedString }">
                  {{ selectedString ? selectedString.displayName : '请选择线材' }}
                </text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
          <text v-if="errors.stringId" class="error-text">{{ errors.stringId }}</text>
          
          <!-- String Details -->
          <view v-if="selectedString" class="string-details">
            <view class="detail-row">
              <text class="detail-label">品牌：</text>
              <text class="detail-value">{{ selectedString.brand }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">粗细：</text>
              <text class="detail-value">{{ selectedString.gauge }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">价格：</text>
              <text class="detail-value price">¥{{ selectedString.price }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Tension Section -->
      <view class="section">
        <text class="section-title">磅数设置</text>
        <view class="form-group">
          <view class="form-item">
            <text class="form-label required">磅数 (18-35)</text>
            <slider 
              :value="formData.tension" 
              :min="18" 
              :max="35" 
              :step="1"
              show-value
              activeColor="#3cc51f"
              @change="handleTensionChange"
            />
          </view>
          <text v-if="errors.tension" class="error-text">{{ errors.tension }}</text>
        </view>
      </view>

      <!-- Payment Method Section -->
      <view class="section">
        <text class="section-title">支付方式</text>
        <view class="form-group">
          <radio-group @change="handlePaymentMethodChange">
            <label v-for="method in paymentMethods" :key="method.value" class="radio-item">
              <radio :value="method.value" :checked="formData.paymentMethod === method.value" color="#3cc51f" />
              <text class="radio-text">{{ method.label }}</text>
            </label>
          </radio-group>
          
          <!-- Balance Warning -->
          <view v-if="formData.paymentMethod === 'BALANCE' && showBalanceWarning" class="warning-box">
            <text class="warning-text">余额不足，请选择其他支付方式</text>
          </view>
        </view>
      </view>

      <!-- Remark Section -->
      <view class="section">
        <text class="section-title">备注信息</text>
        <view class="form-group">
          <textarea 
            class="form-textarea" 
            v-model="formData.remark" 
            placeholder="请输入备注信息（选填）"
            maxlength="200"
            :show-confirm-bar="false"
            @blur="validateField('remark')"
          />
          <view class="char-count">
            <text>{{ formData.remark?.length || 0 }}/200</text>
          </view>
          <text v-if="errors.remark" class="error-text">{{ errors.remark }}</text>
        </view>
      </view>

      <!-- Price Calculation Section -->
      <view class="section price-section">
        <text class="section-title">费用明细</text>
        <view class="price-breakdown">
          <view v-if="!formData.ownString && priceInfo.stringPrice > 0" class="price-item">
            <text class="price-label">线材价格</text>
            <text class="price-value">¥{{ priceInfo.stringPrice }}</text>
          </view>
          <view class="price-item">
            <text class="price-label">服务费</text>
            <text class="price-value">¥{{ priceInfo.servicePrice }}</text>
          </view>
          <view class="price-divider"></view>
          <view class="price-item total">
            <text class="price-label">总计</text>
            <text class="price-value total-price">¥{{ priceInfo.totalPrice }}</text>
          </view>
        </view>
      </view>

      <!-- Draft Actions -->
      <view v-if="hasDraft" class="draft-actions">
        <text class="draft-hint">检测到未提交的草稿</text>
        <button class="clear-draft-btn" @click="handleClearDraft">清除草稿</button>
      </view>
    </scroll-view>

    <!-- Bottom Action Bar -->
    <view class="action-bar">
      <view class="total-price-display">
        <text class="total-label">合计</text>
        <text class="total-amount">¥{{ priceInfo.totalPrice }}</text>
      </view>
      <button 
        class="submit-btn" 
        :disabled="!canSubmit || submitting" 
        @click="handleSubmit"
      >
        {{ submitting ? '提交中...' : '提交申请' }}
      </button>
    </view>
  </MobileLayout>
</template>


<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { 
  getStringList, 
  calculatePrice, 
  createStringing,
  type StringInfo,
  type CreateStringingParams,
  type PriceCalculation
} from '@/api/stringing'
import { safeNavigateBack } from '@/utils/navigation'
import { 
  validateRacketBrand, 
  validateRacketModel, 
  validateTension, 
  validateRemark 
} from '@/utils/validate'
import { PAYMENT_METHOD_TEXT, TENSION_RANGE } from '@/utils/constant'
import { getMemberInfo } from '@/api/member'

// Constants
const DRAFT_KEY = 'stringing_draft'
const CACHE_KEY = 'stringing_string_list_cache'
const CACHE_DURATION = 30 * 60 * 1000 // 30分钟

// State
const userStore = useUserStore()
const formData = ref<CreateStringingParams>({
  racketBrand: '',
  racketModel: '',
  stringId: undefined,
  tension: 24,
  ownString: false,
  paymentMethod: 'ALIPAY',
  remark: ''
})

const stringList = ref<(StringInfo & { displayName: string })[]>([])
const selectedString = ref<StringInfo | null>(null)
const selectedStringIndex = ref(-1)
const priceInfo = ref<PriceCalculation>({
  stringPrice: 0,
  servicePrice: 0,
  totalPrice: 0
})
const errors = ref<Record<string, string>>({})
const submitting = ref(false)
const hasDraft = ref(false)
const showBalanceWarning = ref(false)
const priceCalculating = ref(false)
let priceDebounceTimer: number | null = null
const userBalance = ref<number | null>(null)

// Payment methods
const paymentMethods = [
  { value: 'ALIPAY', label: PAYMENT_METHOD_TEXT.ALIPAY },
  { value: 'WECHAT', label: PAYMENT_METHOD_TEXT.WECHAT },
  { value: 'BALANCE', label: PAYMENT_METHOD_TEXT.BALANCE },
  { value: 'CASH', label: PAYMENT_METHOD_TEXT.CASH }
]

// Computed
const canSubmit = computed(() => {
  return (
    formData.value.racketBrand.trim() !== '' &&
    formData.value.racketModel.trim() !== '' &&
    (formData.value.ownString || formData.value.stringId !== undefined) &&
    formData.value.tension >= TENSION_RANGE.MIN &&
    formData.value.tension <= TENSION_RANGE.MAX &&
    !showBalanceWarning.value &&
    Object.keys(errors.value).length === 0
  )
})

// Load string list
const loadStringList = async () => {
  try {
    // 检查缓存
    const cached = uni.getStorageSync(CACHE_KEY)
    if (cached && cached.timestamp && Date.now() - cached.timestamp < CACHE_DURATION) {
      stringList.value = cached.data
      return
    }
    
    // 从 API 加载
    const result = await getStringList()
    const data = result.map(item => ({
      ...item,
      displayName: `${item.stringName} - ${item.brand} (¥${item.price})`
    }))
    
    stringList.value = data
    
    // 保存到缓存
    uni.setStorageSync(CACHE_KEY, {
      data,
      timestamp: Date.now()
    })
  } catch (error) {
    console.error('加载线材列表失败:', error)
    uni.showToast({ title: '加载线材列表失败', icon: 'none' })
  }
}

// Calculate price with debounce (300ms)
const calculateServicePrice = () => {
  // Clear previous timer
  if (priceDebounceTimer) {
    clearTimeout(priceDebounceTimer)
  }

  // Set new timer
  priceDebounceTimer = setTimeout(async () => {
    if (!formData.value.ownString && !formData.value.stringId) {
      priceInfo.value = {
        stringPrice: 0,
        servicePrice: 0,
        totalPrice: 0
      }
      return
    }

    try {
      priceCalculating.value = true
      const result = await calculatePrice({
        stringId: formData.value.stringId,
        ownString: formData.value.ownString
      })
      priceInfo.value = result
    } catch (error) {
      console.error('计算价格失败:', error)
      uni.showToast({
        title: '计算价格失败',
        icon: 'none'
      })
    } finally {
      priceCalculating.value = false
    }
  }, 300)
}

// Validate field
const validateField = (field: string) => {
  switch (field) {
    case 'racketBrand':
      if (!validateRacketBrand(formData.value.racketBrand)) {
        errors.value.racketBrand = '请输入球拍品牌（1-50字符）'
      } else {
        delete errors.value.racketBrand
      }
      break
    case 'racketModel':
      if (!validateRacketModel(formData.value.racketModel)) {
        errors.value.racketModel = '请输入球拍型号（1-50字符）'
      } else {
        delete errors.value.racketModel
      }
      break
    case 'tension':
      if (!validateTension(formData.value.tension)) {
        errors.value.tension = '磅数范围为18-35'
      } else {
        delete errors.value.tension
      }
      break
    case 'remark':
      if (formData.value.remark && !validateRemark(formData.value.remark)) {
        errors.value.remark = '备注长度不能超过200字符'
      } else {
        delete errors.value.remark
      }
      break
    case 'stringId':
      if (!formData.value.ownString && !formData.value.stringId) {
        errors.value.stringId = '请选择线材'
      } else {
        delete errors.value.stringId
      }
      break
  }
}

// Validate all fields
const validateForm = (): boolean => {
  errors.value = {}
  
  if (!validateRacketBrand(formData.value.racketBrand)) {
    errors.value.racketBrand = '请输入球拍品牌（1-50字符）'
  }
  
  if (!validateRacketModel(formData.value.racketModel)) {
    errors.value.racketModel = '请输入球拍型号（1-50字符）'
  }
  
  if (!formData.value.ownString && !formData.value.stringId) {
    errors.value.stringId = '请选择线材'
  }
  
  if (!validateTension(formData.value.tension)) {
    errors.value.tension = '磅数范围为18-35'
  }
  
  if (formData.value.remark && !validateRemark(formData.value.remark)) {
    errors.value.remark = '备注长度不能超过200字符'
  }
  
  return Object.keys(errors.value).length === 0
}

// Handle own string change
const handleOwnStringChange = (e: any) => {
  formData.value.ownString = e.detail.value.length > 0
  if (formData.value.ownString) {
    formData.value.stringId = undefined
    selectedString.value = null
    selectedStringIndex.value = -1
    delete errors.value.stringId
  }
}

// Handle string selection
const handleStringChange = (e: any) => {
  const index = e.detail.value
  selectedStringIndex.value = index
  selectedString.value = stringList.value[index]
  formData.value.stringId = selectedString.value.id
  validateField('stringId')
}

// Handle tension change
const handleTensionChange = (e: any) => {
  formData.value.tension = e.detail.value
  validateField('tension')
}

// Handle payment method change
const handlePaymentMethodChange = (e: any) => {
  formData.value.paymentMethod = e.detail.value
  
  // Check balance if BALANCE is selected
  if (formData.value.paymentMethod === 'BALANCE') {
    if (userBalance.value !== null && priceInfo.value.totalPrice > userBalance.value) {
      showBalanceWarning.value = true
      uni.showToast({
        title: '余额不足，请选择其他支付方式',
        icon: 'none'
      })
    } else {
      showBalanceWarning.value = false
    }
  } else {
    showBalanceWarning.value = false
  }
}

// Handle submit
const handleSubmit = async () => {
  if (!validateForm()) {
    uni.showToast({
      title: '请完善表单信息',
      icon: 'none'
    })
    return
  }

  if (!canSubmit.value) {
    return
  }

  try {
    submitting.value = true
    uni.showLoading({
      title: '提交中...'
    })

    const result = await createStringing(formData.value)
    
    uni.hideLoading()
    
    // Clear draft after successful submission
    uni.removeStorageSync(DRAFT_KEY)
    hasDraft.value = false
    
    uni.showToast({
      title: '提交成功',
      icon: 'success'
    })

    // Navigate to detail page
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/stringing/detail?id=${result.id}`
      })
    }, 1500)
  } catch (error) {
    console.error('提交失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '提交失败，请重试',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

// Handle clear draft
const handleClearDraft = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清除草稿吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync(DRAFT_KEY)
        hasDraft.value = false
        // Reset form
        formData.value = {
          racketBrand: '',
          racketModel: '',
          stringId: undefined,
          tension: 24,
          ownString: false,
          paymentMethod: 'ALIPAY',
          remark: ''
        }
        selectedString.value = null
        selectedStringIndex.value = -1
        errors.value = {}
        uni.showToast({
          title: '草稿已清除',
          icon: 'success'
        })
      }
    }
  })
}

// Handle back
const handleBack = () => {
  safeNavigateBack()
}

// Save draft
const saveDraft = () => {
  try {
    uni.setStorageSync(DRAFT_KEY, {
      ...formData.value,
      selectedStringIndex: selectedStringIndex.value
    })
  } catch (error) {
    console.error('保存草稿失败:', error)
  }
}

// Restore draft
const restoreDraft = () => {
  try {
    const draft = uni.getStorageSync(DRAFT_KEY)
    if (draft) {
      formData.value = {
        racketBrand: draft.racketBrand || '',
        racketModel: draft.racketModel || '',
        stringId: draft.stringId,
        tension: draft.tension || 24,
        ownString: draft.ownString || false,
        paymentMethod: draft.paymentMethod || 'ALIPAY',
        remark: draft.remark || ''
      }
      
      // Restore selected string
      if (draft.selectedStringIndex >= 0 && stringList.value.length > 0) {
        selectedStringIndex.value = draft.selectedStringIndex
        selectedString.value = stringList.value[draft.selectedStringIndex]
      }
      
      hasDraft.value = true
    }
  } catch (error) {
    console.error('恢复草稿失败:', error)
  }
}

// Watch form data changes and save draft
watch(formData, () => {
  saveDraft()
}, { deep: true })

// Watch string selection and own string to trigger price calculation
watch([() => formData.value.stringId, () => formData.value.ownString], () => {
  calculateServicePrice()
})

// Page mounted
onMounted(async () => {
  // Check if user is logged in
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }

  // Load string list
  await loadStringList()
  
  // Load member balance if user has memberId
  try {
    const currentUser = userStore.userInfo
    const memberId = (currentUser as any)?.memberId || (currentUser as any)?.id
    if (memberId) {
      const memberInfo = await getMemberInfo(memberId)
      userBalance.value = memberInfo.balance
    }
  } catch (error) {
    console.error('加载会员信息失败:', error)
  }
  
  // Restore draft
  restoreDraft()
  
  // Calculate initial price
  calculateServicePrice()
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

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
  display: block;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.form-label {
  font-size: 24rpx;
  color: #666666;
  
  &.required::before {
    content: '*';
    color: #ef4444;
    margin-right: 4rpx;
  }
}

.form-input {
  padding: 16rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #333333;
  background-color: #ffffff;
}

.form-textarea {
  padding: 16rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 12rpx;
  font-size: 24rpx;
  color: #333333;
  background-color: #ffffff;
  min-height: 160rpx;
}

.char-count {
  text-align: right;
  font-size: 20rpx;
  color: #999999;
}

.error-text {
  font-size: 20rpx;
  color: #ef4444;
  margin-top: -12rpx;
}

.checkbox-item {
  flex-direction: row;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.checkbox-text {
  font-size: 24rpx;
  color: #333333;
}

.picker {
  padding: 16rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
}

.picker-value {
  font-size: 24rpx;
  color: #333333;
  
  &.placeholder {
    color: #999999;
  }
}

.picker-arrow {
  font-size: 20rpx;
  color: #999999;
}

.string-details {
  padding: 16rpx;
  background-color: #f9f9f9;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.detail-row {
  display: flex;
  align-items: center;
  font-size: 22rpx;
}

.detail-label {
  color: #999999;
  min-width: 80rpx;
}

.detail-value {
  color: #333333;
  
  &.price {
    color: #ef4444;
    font-weight: bold;
  }
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 0;
}

.radio-text {
  font-size: 24rpx;
  color: #333333;
}

.warning-box {
  padding: 16rpx;
  background-color: #fff3e0;
  border-radius: 12rpx;
  border-left: 4rpx solid #ff9800;
}

.warning-text {
  font-size: 22rpx;
  color: #ff9800;
}

.price-section {
  background-color: #f9f9f9;
}

.price-breakdown {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  
  &.total {
    margin-top: 10rpx;
  }
}

.price-label {
  color: #666666;
}

.price-value {
  font-weight: bold;
  color: #333333;
  
  &.total-price {
    color: #ef4444;
    font-size: 28rpx;
  }
}

.price-divider {
  height: 1rpx;
  background-color: #e6e6e6;
  margin: 10rpx 0;
}

.draft-actions {
  background-color: #e3f2fd;
  padding: 20rpx 28rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.draft-hint {
  font-size: 22rpx;
  color: #2196f3;
}

.clear-draft-btn {
  padding: 8rpx 20rpx;
  background-color: #ffffff;
  color: #2196f3;
  font-size: 22rpx;
  border-radius: 8rpx;
  border: 1rpx solid #2196f3;
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
    box-shadow: none;
  }
}
</style>
