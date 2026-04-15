<template>
  <MobileLayout>
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">服务查询</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <!-- 查询输入区域 -->
      <view class="query-card">
        <view class="query-section">
          <text class="section-title">服务编号查询</text>
          <text class="section-hint">格式：ST+8位日期+序号（如：ST202401010001）</text>
          
          <view class="input-row">
            <input 
              class="service-input" 
              v-model="serviceNo" 
              placeholder="请输入服务编号"
              placeholder-style="color: #cccccc"
              @confirm="handleQuery"
            />
            <button class="scan-btn" @click="handleScan">
              <text class="scan-icon">📷</text>
            </button>
          </view>

          <button class="query-btn" @click="handleQuery">查询</button>
        </view>
      </view>

      <!-- 查询结果展示区域 -->
      <view class="result-card" v-if="showResult && queryResult">
        <view class="result-header">
          <text class="result-title">查询结果</text>
        </view>

        <view class="status-row">
          <text class="service-no">{{ queryResult.serviceNo }}</text>
          <text class="service-status" :style="{ backgroundColor: getStatusBgColor(queryResult.status), color: getStatusColor(queryResult.status) }">
            {{ getStatusText(queryResult.status) }}
          </text>
        </view>

        <view class="detail-list">
          <view class="detail-item">
            <text class="detail-label">球拍品牌</text>
            <text class="detail-value">{{ queryResult.racketBrand }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">球拍型号</text>
            <text class="detail-value">{{ queryResult.racketModel }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">线材信息</text>
            <view class="string-info">
              <text class="detail-value" v-if="queryResult.ownString">
                <text class="own-string-badge">自带线材</text>
              </text>
              <text class="detail-value" v-else>
                {{ queryResult.stringName || '-' }}
                <text class="string-detail" v-if="queryResult.stringBrand">
                  ({{ queryResult.stringBrand }} / {{ queryResult.stringGauge }})
                </text>
              </text>
            </view>
          </view>
          <view class="detail-item">
            <text class="detail-label">磅数</text>
            <text class="detail-value">{{ queryResult.tension }} 磅</text>
          </view>
          <view class="detail-item" v-if="!queryResult.ownString && queryResult.stringPrice">
            <text class="detail-label">线材价格</text>
            <text class="detail-value price">¥{{ queryResult.stringPrice }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">服务费</text>
            <text class="detail-value price">¥{{ queryResult.servicePrice }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">总价格</text>
            <text class="detail-value price total">¥{{ queryResult.totalPrice }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">支付方式</text>
            <text class="detail-value">{{ getPaymentMethodText(queryResult.paymentMethod) }}</text>
          </view>
          <view class="detail-item" v-if="queryResult.remark">
            <text class="detail-label">备注</text>
            <text class="detail-value">{{ queryResult.remark }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">创建时间</text>
            <text class="detail-value">{{ queryResult.createTime }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">更新时间</text>
            <text class="detail-value">{{ queryResult.updateTime }}</text>
          </view>
        </view>

        <button class="view-detail-btn" @click="handleViewDetail">查看完整详情</button>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { getStringingByNo, type StringingService } from '@/api/stringing'
import { validateServiceNo } from '@/utils/validate'
import { safeNavigateBack } from '@/utils/navigation'
import { 
  STRINGING_STATUS_TEXT, 
  STRINGING_STATUS_COLOR,
  STRINGING_STATUS,
  PAYMENT_METHOD_TEXT
} from '@/utils/constant'

const serviceNo = ref('')
const queryResult = ref<StringingService | null>(null)
const showResult = ref(false)

const handleBack = () => {
  safeNavigateBack()
}

const getStatusText = (status: number) => {
  return STRINGING_STATUS_TEXT[status] || '未知'
}

const getStatusColor = (status: number) => {
  return STRINGING_STATUS_COLOR[status] || '#999999'
}

const getStatusBgColor = (status: number) => {
  const colorMap: Record<number, string> = {
    [STRINGING_STATUS.CANCELLED]: '#f5f5f5',
    [STRINGING_STATUS.WAITING]: '#fff3e0',
    [STRINGING_STATUS.IN_PROGRESS]: '#e3f2fd',
    [STRINGING_STATUS.COMPLETED]: '#e8f5e9'
  }
  return colorMap[status] || '#f5f5f5'
}

const getPaymentMethodText = (method?: string) => {
  if (!method) return '未知'
  return PAYMENT_METHOD_TEXT[method as keyof typeof PAYMENT_METHOD_TEXT] || method
}

const handleQuery = async () => {
  if (!serviceNo.value.trim()) {
    uni.showToast({ title: '请输入服务编号', icon: 'none' })
    return
  }

  if (!validateServiceNo(serviceNo.value)) {
    uni.showToast({ title: '服务编号格式不正确', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '查询中...' })
    queryResult.value = await getStringingByNo(serviceNo.value)
    showResult.value = true
    uni.hideLoading()
  } catch (error) {
    console.error('查询服务失败:', error)
    uni.hideLoading()
    uni.showToast({ title: '未找到该服务记录', icon: 'none' })
    showResult.value = false
  }
}

const handleScan = () => {
  uni.scanCode({
    success: (res) => {
      serviceNo.value = res.result
      handleQuery()
    },
    fail: (err) => {
      console.error('扫码失败:', err)
      uni.showToast({ title: '扫码失败', icon: 'none' })
    }
  })
}

const handleViewDetail = () => {
  if (queryResult.value) {
    uni.navigateTo({
      url: `/pages/stringing/detail?id=${queryResult.value.id}`
    })
  }
}
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
  height: calc(100vh - 120rpx);
  background-color: #f5f7fa;
  padding: 24rpx 28rpx;
}

.query-card {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 24rpx;
}

.query-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
}

.section-hint {
  font-size: 22rpx;
  color: #999999;
  line-height: 1.5;
}

.input-row {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.service-input {
  flex: 1;
  padding: 24rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #333333;
}

.scan-btn {
  width: 88rpx;
  height: 88rpx;
  background-color: #3cc51f;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: none;
}

.scan-icon {
  font-size: 36rpx;
}

.query-btn {
  width: 100%;
  padding: 24rpx;
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 12rpx;
  border: none;
  margin-top: 8rpx;
}

.result-card {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.result-header {
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.result-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.service-no {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
}

.service-status {
  font-size: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-bottom: 28rpx;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 26rpx;
}

.detail-label {
  color: #999999;
  width: 160rpx;
  flex-shrink: 0;
}

.detail-value {
  color: #333333;
  flex: 1;
  text-align: right;

  &.price {
    font-weight: bold;
    color: #ef4444;
    font-size: 28rpx;

    &.total {
      font-size: 32rpx;
    }
  }
}

.string-info {
  flex: 1;
  text-align: right;
}

.own-string-badge {
  display: inline-block;
  background-color: #e8f5e9;
  color: #3cc51f;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  font-size: 22rpx;
}

.string-detail {
  color: #999999;
  font-size: 22rpx;
  margin-left: 8rpx;
}

.view-detail-btn {
  width: 100%;
  padding: 24rpx;
  background-color: #3cc51f;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 12rpx;
  border: none;
}
</style>
