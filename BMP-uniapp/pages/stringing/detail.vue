<template>
  <MobileLayout>
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <text class="header-title">服务详情</text>
        <view class="header-placeholder"></view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="detail-card">
        <view class="status-row">
          <text class="service-no">{{ service.serviceNo }}</text>
          <text class="service-status" :style="{ backgroundColor: getStatusBgColor(service.status), color: getStatusColor(service.status) }">
            {{ getStatusText(service.status) }}
          </text>
        </view>

        <view class="detail-list">
          <view class="detail-item">
            <text class="detail-label">球拍品牌</text>
            <text class="detail-value">{{ service.racketBrand }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">球拍型号</text>
            <text class="detail-value">{{ service.racketModel }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">线材信息</text>
            <view class="string-info">
              <text class="detail-value" v-if="service.ownString">
                <text class="own-string-badge">自带线材</text>
              </text>
              <text class="detail-value" v-else>
                {{ service.stringName || '-' }}
                <text class="string-detail" v-if="service.stringBrand">
                  ({{ service.stringBrand }} / {{ service.stringGauge }})
                </text>
              </text>
            </view>
          </view>
          <view class="detail-item">
            <text class="detail-label">磅数</text>
            <text class="detail-value">{{ service.tension }} 磅</text>
          </view>
          <view class="detail-item" v-if="!service.ownString && service.stringPrice">
            <text class="detail-label">线材价格</text>
            <text class="detail-value price">¥{{ service.stringPrice }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">服务费</text>
            <text class="detail-value price">¥{{ service.servicePrice }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">总价格</text>
            <text class="detail-value price total">¥{{ service.totalPrice }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">支付方式</text>
            <text class="detail-value">{{ getPaymentMethodText(service.paymentMethod) }}</text>
          </view>
          <view class="detail-item" v-if="service.remark">
            <text class="detail-label">备注</text>
            <text class="detail-value">{{ service.remark }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">创建时间</text>
            <text class="detail-value">{{ service.createTime }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">更新时间</text>
            <text class="detail-value">{{ service.updateTime }}</text>
          </view>
        </view>

        <!-- 普通用户取消服务按钮 -->
        <view class="action-row" v-if="showCancelButton">
          <button class="cancel-btn" @click="handleCancel">取消服务</button>
        </view>

        <!-- 管理者更新状态按钮 -->
        <view class="action-row" v-if="showManagerActions">
          <view class="status-selector">
            <text class="selector-label">更新状态：</text>
            <picker mode="selector" :range="managerStatusOptions" range-key="label" @change="handleStatusChange">
              <view class="picker-value">
                {{ selectedStatusLabel }}
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>
          <button class="update-btn" @click="handleUpdateStatus">更新状态</button>
        </view>
      </view>
    </scroll-view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getStringingDetail, updateStringingStatus, type StringingService } from '@/api/stringing'
import { safeNavigateBack } from '@/utils/navigation'
import { 
  STRINGING_STATUS, 
  STRINGING_STATUS_TEXT, 
  STRINGING_STATUS_COLOR,
  PAYMENT_METHOD_TEXT,
  USER_ROLES
} from '@/utils/constant'

const serviceId = ref<number>(0)
const service = ref<StringingService>({
  id: 0,
  serviceNo: '',
  memberId: 0,
  racketBrand: '',
  racketModel: '',
  tension: 0,
  ownString: false,
  servicePrice: 0,
  totalPrice: 0,
  paymentMethod: '',
  status: 0,
  createTime: '',
  updateTime: ''
})
const userStore = useUserStore()
const selectedStatus = ref<number>(1)

// 管理者状态选项（不包括已取消）
const managerStatusOptions = [
  { value: STRINGING_STATUS.WAITING, label: STRINGING_STATUS_TEXT[STRINGING_STATUS.WAITING] },
  { value: STRINGING_STATUS.IN_PROGRESS, label: STRINGING_STATUS_TEXT[STRINGING_STATUS.IN_PROGRESS] },
  { value: STRINGING_STATUS.COMPLETED, label: STRINGING_STATUS_TEXT[STRINGING_STATUS.COMPLETED] }
]

onLoad((options) => {
  if (options.id) {
    serviceId.value = Number(options.id)
  }
})

// 是否显示取消按钮（普通用户，且状态为等待穿线或正在穿线）
const showCancelButton = computed(() => {
  const isManager = userStore.userRole === USER_ROLES.VENUE_MANAGER || userStore.userRole === USER_ROLES.PRESIDENT
  return !isManager && (service.value.status === STRINGING_STATUS.WAITING || service.value.status === STRINGING_STATUS.IN_PROGRESS)
})

// 是否显示管理者操作（管理者或会长）
const showManagerActions = computed(() => {
  return userStore.userRole === 'VENUE_MANAGER' || userStore.userRole === 'PRESIDENT'
})

// 选中的状态标签
const selectedStatusLabel = computed(() => {
  const option = managerStatusOptions.find(opt => opt.value === selectedStatus.value)
  return option?.label || ''
})

const loadDetail = async () => {
  try {
    const result = await getStringingDetail(serviceId.value)
    service.value = result
    // 初始化选中状态为当前状态（如果是有效状态）
    if (result.status !== STRINGING_STATUS.CANCELLED) {
      selectedStatus.value = result.status
    }
  } catch (error) {
    console.error('加载服务详情失败:', error)
    uni.showToast({
      title: '加载服务详情失败',
      icon: 'none'
    })
  }
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

const getPaymentMethodText = (method: string) => {
  return PAYMENT_METHOD_TEXT[method] || method
}

const handleBack = () => {
  safeNavigateBack()
}

const handleCancel = async () => {
  uni.showModal({
    title: '提示',
    content: '确定要取消该服务吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await updateStringingStatus(service.value.id, STRINGING_STATUS.CANCELLED)
          uni.showToast({ title: '取消成功', icon: 'success' })
          await loadDetail()
        } catch (e) {
          console.error('取消服务失败:', e)
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

const handleStatusChange = (e: any) => {
  selectedStatus.value = managerStatusOptions[e.detail.value].value
}

const handleUpdateStatus = async () => {
  if (selectedStatus.value === service.value.status) {
    uni.showToast({ title: '状态未改变', icon: 'none' })
    return
  }

  try {
    await updateStringingStatus(service.value.id, selectedStatus.value)
    uni.showToast({ title: '更新成功', icon: 'success' })
    await loadDetail()
  } catch (e) {
    console.error('更新状态失败:', e)
    uni.showToast({ title: '更新失败', icon: 'none' })
  }
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  if (serviceId.value) {
    await loadDetail()
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
  height: calc(100vh - 120rpx);
  background-color: #f5f7fa;
  padding: 24rpx 28rpx;
}

.detail-card {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 28rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
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

.action-row {
  margin-top: 32rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.cancel-btn {
  width: 100%;
  padding: 24rpx;
  background-color: #ffffff;
  color: #ef4444;
  font-size: 28rpx;
  font-weight: bold;
  border-radius: 12rpx;
  border: 1rpx solid #ef4444;
}

.status-selector {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 16rpx;
  background-color: #f5f7fa;
  border-radius: 8rpx;
}

.selector-label {
  font-size: 26rpx;
  color: #666666;
  margin-right: 16rpx;
}

.picker-value {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
  color: #333333;
}

.picker-arrow {
  font-size: 20rpx;
  color: #999999;
}

.update-btn {
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
