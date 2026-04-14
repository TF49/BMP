<template>
  <PresidentLayout :showTabBar="false" className="president-equipment-rental-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">租借详情</text>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <view v-if="loading" class="state-card">
            <text class="state-text">正在加载租借详情...</text>
          </view>

          <view v-else-if="loadError" class="state-card">
            <text class="state-text">{{ loadError }}</text>
          </view>

          <template v-else-if="detail">
            <view class="hero-card">
              <view>
                <text class="hero-label">租借状态</text>
                <text class="hero-value">{{ statusLabel }}</text>
              </view>
              <view class="hero-side">
                <text class="hero-label">预计归还</text>
                <text class="hero-side-value">{{ expectedReturnLabel }}</text>
              </view>
            </view>

            <view class="card">
              <text class="section-title">器材信息</text>
              <view class="field-row">
                <text class="field-label">器材名称</text>
                <text class="field-value">{{ detail.equipmentName || '未命名器材' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">租借编号</text>
                <text class="field-value">{{ detail.rentalNo || `#${detail.id}` }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">租借单价</text>
                <text class="field-value amount">¥{{ formatAmount(detail.unitPrice || 0) }}</text>
              </view>
            </view>

            <view class="card">
              <text class="section-title">借用人与费用</text>
              <view class="field-row">
                <text class="field-label">会员</text>
                <text class="field-value">{{ detail.memberName || `会员 #${detail.memberId || '-'}` }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">数量</text>
                <text class="field-value">{{ detail.quantity || 0 }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">租借日期</text>
                <text class="field-value">{{ formatDate(detail.rentalDate) || '未知日期' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">押金</text>
                <text class="field-value">¥{{ formatAmount(detail.depositAmount || 0) }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">租借金额</text>
                <text class="field-value">¥{{ formatAmount(detail.rentalAmount || 0) }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">支付方式</text>
                <text class="field-value">{{ detail.paymentMethod || '未知支付方式' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">支付状态</text>
                <text class="field-value">{{ paymentStatusLabel }}</text>
              </view>
              <view class="field-row multiline">
                <text class="field-label">备注</text>
                <text class="field-value remark">{{ detail.remark || '无' }}</text>
              </view>
            </view>

            <view class="action-row">
              <view class="action-btn secondary" @click="openActions">管理租借</view>
            </view>
          </template>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import {
  deleteEquipmentRental,
  getEquipmentRentalDetail,
  processEquipmentRentalPayment,
  processEquipmentRentalRefund,
  type EquipmentRentalItem,
  updateEquipmentRentalStatus
} from '@/api/equipment'
import { formatAmount, formatDate, formatDateTime } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { PAYMENT_METHOD, PAYMENT_METHOD_TEXT } from '@/utils/constant'

type EquipmentRentalDetail = EquipmentRentalItem & {
  expectedReturnDate?: string
  returnDate?: string
  unitPrice?: number
  depositAmount?: number
  paymentStatus?: number
  remark?: string
}

const detail = ref<EquipmentRentalDetail | null>(null)
const loading = ref(false)
const loadError = ref('')

const statusLabel = computed(() => {
  const status = detail.value?.status
  if (status === 0) return '已取消'
  if (status === 1) return '租借中'
  if (status === 2) return '已归还'
  if (status === 3) return '已逾期'
  return '未知状态'
})

const paymentStatusLabel = computed(() => {
  const status = detail.value?.paymentStatus
  if (status === 1) return '支付成功'
  if (status === 0) return '待支付'
  if (status === 2) return '已退款'
  return '支付状态未知'
})

const expectedReturnLabel = computed(() => {
  if (!detail.value) return '未知时间'
  return formatDate(detail.value.expectedReturnDate) || formatDateTime(detail.value.returnDate) || '未知时间'
})

async function loadDetail(id: number) {
  loading.value = true
  loadError.value = ''
  try {
    detail.value = (await getEquipmentRentalDetail(id)) as EquipmentRentalDetail
  } catch (error) {
    console.error('Failed to load equipment rental detail:', error)
    detail.value = null
    loadError.value = '租借详情加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_RENTAL_LIST)
}

async function refreshDetail() {
  if (!detail.value?.id) return
  await loadDetail(detail.value.id)
}

async function openActions() {
  if (!detail.value?.id) return
  const actions: Array<{ label: string; handler: () => Promise<void> | void }> = []
  const currentStatus = Number(detail.value.status ?? -1)
  const currentPaymentStatus = Number(detail.value.paymentStatus ?? 0)

  if (currentStatus === 1 || currentStatus === 3) {
    if (currentPaymentStatus !== 1) {
      actions.push({
        label: '确认收款',
        handler: async () => {
          const methods = [
            PAYMENT_METHOD.CASH,
            PAYMENT_METHOD.ALIPAY,
            PAYMENT_METHOD.WECHAT,
            PAYMENT_METHOD.BALANCE
          ] as const
          const result = await uni.showActionSheet({
            itemList: methods.map((method) => PAYMENT_METHOD_TEXT[method])
          })
          const selectedMethod = methods[result.tapIndex]
          if (!selectedMethod) return
          await processEquipmentRentalPayment(detail.value!.id, selectedMethod)
          uni.showToast({ title: `${PAYMENT_METHOD_TEXT[selectedMethod]}收款成功`, icon: 'success' })
          await refreshDetail()
        }
      })
    }
    actions.push({
      label: '标记归还',
      handler: async () => {
        await updateEquipmentRentalStatus(detail.value!.id, 2)
        uni.showToast({ title: '已标记归还', icon: 'success' })
        await refreshDetail()
      }
    })
    actions.push({
      label: '取消租借',
      handler: async () => {
        await updateEquipmentRentalStatus(detail.value!.id, 0)
        uni.showToast({ title: '已取消租借', icon: 'success' })
        await refreshDetail()
      }
    })
    if (currentPaymentStatus === 1) {
      actions.push({
        label: '退款',
        handler: async () => {
          await processEquipmentRentalRefund(detail.value!.id)
          uni.showToast({ title: '退款成功', icon: 'success' })
          await refreshDetail()
        }
      })
    }
  } else if (currentStatus === 0 || currentStatus === 2) {
    actions.push({
      label: '删除记录',
      handler: async () => {
        await deleteEquipmentRental(detail.value!.id)
        uni.showToast({ title: '删除成功', icon: 'success' })
        goBack()
      }
    })
  }

  if (!actions.length) {
    uni.showToast({ title: '当前状态暂无可执行操作', icon: 'none' })
    return
  }

  const result = await uni.showActionSheet({ itemList: actions.map((action) => action.label) })
  const action = actions[result.tapIndex]
  if (!action) return

  try {
    await action.handler()
  } catch (error) {
    console.error('Failed to manage equipment rental detail:', error)
  }
}

onLoad((options) => {
  const rawId = Number(options?.rentalId || options?.id)
  if (!Number.isFinite(rawId) || rawId <= 0) {
    loadError.value = '缺少有效的租借 ID'
    return
  }
  loadDetail(rawId)
})
</script>

<style lang="scss" scoped>
.president-equipment-rental-detail-layout {
  :deep(.president-layout-content) {
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
}

.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
}

.nav-header {
  padding: 20rpx 32rpx 16rpx;
  background: #f9f9f9;
}

.nav-left,
.hero-card {
  display: flex;
  align-items: center;
}

.nav-left {
  gap: 16rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.main-scroll {
  height: calc(100vh - var(--status-bar-height) - 88rpx);
}

.scroll-inner {
  padding: 12rpx 32rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.state-card,
.hero-card,
.card {
  border-radius: 24rpx;
  background: #ffffff;
  padding: 36rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.state-text {
  color: #5f5e5e;
  text-align: center;
  font-size: 28rpx;
}

.hero-card {
  justify-content: space-between;
  gap: 24rpx;
  background: linear-gradient(135deg, #fff3eb 0%, #ffffff 100%);
}

.hero-label,
.section-title,
.field-label {
  color: #8a8a8a;
  font-size: 24rpx;
}

.hero-value,
.hero-side-value {
  display: block;
  margin-top: 12rpx;
  color: #1a1c1c;
}

.hero-value {
  font-size: 40rpx;
  font-weight: 800;
}

.hero-side {
  text-align: right;
}

.hero-side-value {
  font-size: 28rpx;
}

.section-title {
  display: block;
  margin-bottom: 20rpx;
  font-weight: 700;
}

.field-row {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  padding: 18rpx 0;
  border-top: 1rpx solid #f1f1f1;
}

.field-row:first-of-type {
  border-top: none;
  padding-top: 0;
}

.field-value {
  flex: 1;
  text-align: right;
  color: #1a1c1c;
  font-size: 28rpx;
}

.amount {
  color: #a33e00;
  font-weight: 800;
}

.multiline {
  align-items: flex-start;
}

.remark {
  white-space: pre-wrap;
}

.action-row {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  min-width: 200rpx;
  padding: 20rpx 28rpx;
  border-radius: 20rpx;
  text-align: center;
  font-size: 24rpx;
  font-weight: 700;
}

.action-btn.secondary {
  background: #ffedd5;
  color: #c2410c;
}
</style>
