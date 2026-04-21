<template>
  <PresidentLayout :showTabBar="false" className="president-stringing-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">穿线详情</text>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <view v-if="loading" class="state-card">
            <text class="state-text">正在加载工单详情...</text>
          </view>

          <view v-else-if="loadError" class="state-card">
            <text class="state-text">{{ loadError }}</text>
          </view>

          <template v-else-if="detail">
            <view class="hero-card">
              <text class="hero-label">当前状态</text>
              <text class="hero-value">{{ statusLabel }}</text>
              <text class="hero-sub">{{ detail.serviceNo || `工单 #${detail.id}` }}</text>
            </view>

            <view class="card">
              <text class="section-title">工单信息</text>
              <view class="field-row">
                <text class="field-label">提交时间</text>
                <text class="field-value">{{ formatDateTime(detail.createTime) || '未知时间' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">客户</text>
                <text class="field-value">{{ detail.memberName || detail.userName || `会员 #${detail.memberId || '-'}` }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">手机号</text>
                <text class="field-value">{{ formatPhone(detail.memberPhone || '') || '未知手机号' }}</text>
              </view>
            </view>

            <view class="card">
              <text class="section-title">穿线信息</text>
              <view class="field-row">
                <text class="field-label">球拍型号</text>
                <text class="field-value">{{ racketLabel }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">线材</text>
                <text class="field-value">{{ stringLabel }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">磅数</text>
                <text class="field-value">{{ tensionLabel }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">穿线方式</text>
                <text class="field-value">{{ methodLabel }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">服务费用</text>
                <text class="field-value amount">￥{{ formatAmount(detail.totalPrice || detail.servicePrice || 0) }}</text>
              </view>
              <view class="field-row multiline">
                <text class="field-label">备注</text>
                <text class="field-value remark">{{ detail.remark || '无特殊备注' }}</text>
              </view>
            </view>

            <view class="action-row">
              <view class="action-btn secondary" @click="openActions">管理工单</view>
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
  getStringingDetail,
  processStringingPayment,
  processStringingRefund,
  type StringingService,
  updateStringingStatus
} from '@/api/president/stringing'
import { formatAmount, formatDateTime, formatPhone } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { BUSINESS_PAYMENT_METHOD, PAYMENT_METHOD_TEXT, STRINGING_STATUS } from '@/utils/constant'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const detail = ref<StringingService | null>(null)
const loading = ref(false)
const loadError = ref('')

const statusLabel = computed(() => {
  const status = detail.value?.status
  if (status === STRINGING_STATUS.CANCELLED) return '已取消'
  if (status === STRINGING_STATUS.WAITING) return '等待穿线'
  if (status === STRINGING_STATUS.IN_PROGRESS) return '正在穿线'
  if (status === STRINGING_STATUS.COMPLETED) return '已完成'
  return '未知状态'
})

const racketLabel = computed(() => {
  const current = detail.value
  if (!current) return '未知球拍'
  return [current.racketBrand, current.racketModel].filter(Boolean).join(' ') || '未知球拍'
})

const tensionLabel = computed(() => {
  const tension = detail.value?.pound
  if (tension === undefined || tension === null || tension === '') return '未知磅数'
  return `${String(tension).replace(/\.0$/, '')} lbs`
})

const stringLabel = computed(() => {
  const current = detail.value
  if (!current) return '未知线材'
  if (current.isOwnString === 1) return current.stringName || '自带线材'
  return current.stringName || current.stringEquipmentName || '未知线材'
})

const methodLabel = computed(() => {
  const method = detail.value?.stringingMethod
  if (method === 'TWO_SECTION') return '两结'
  if (method === 'FOUR_SECTION') return '四结'
  if (method === 'AUTO') return '自动判断'
  return method || '未知方式'
})

async function loadDetail(id: number) {
  loading.value = true
  loadError.value = ''
  try {
    detail.value = await getStringingDetail(id)
  } catch (error) {
    console.error('Failed to load stringing detail:', error)
    detail.value = null
    loadError.value = '详情加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.STRINGING_LIST)
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

  if (currentStatus === STRINGING_STATUS.WAITING) {
    actions.push({
      label: '开始穿线',
      handler: async () => {
        await updateStringingStatus(detail.value!.id, STRINGING_STATUS.IN_PROGRESS)
        uni.showToast({ title: '已开始穿线', icon: 'success' })
        await refreshDetail()
      }
    })
    actions.push({
      label: '取消工单',
      handler: async () => {
        await updateStringingStatus(detail.value!.id, STRINGING_STATUS.CANCELLED)
        uni.showToast({ title: '已取消工单', icon: 'success' })
        await refreshDetail()
      }
    })
  } else if (currentStatus === STRINGING_STATUS.IN_PROGRESS) {
    actions.push({
      label: '标记完成',
      handler: async () => {
        await updateStringingStatus(detail.value!.id, STRINGING_STATUS.COMPLETED)
        uni.showToast({ title: '已标记完成', icon: 'success' })
        await refreshDetail()
      }
    })
    actions.push({
      label: '取消工单',
      handler: async () => {
        await updateStringingStatus(detail.value!.id, STRINGING_STATUS.CANCELLED)
        uni.showToast({ title: '已取消工单', icon: 'success' })
        await refreshDetail()
      }
    })
  } else if (currentStatus === STRINGING_STATUS.COMPLETED) {
    if (currentPaymentStatus !== 1) {
      actions.push({
        label: '确认收款',
        handler: async () => {
          await processStringingPayment(detail.value!.id, BUSINESS_PAYMENT_METHOD)
          uni.showToast({ title: `${PAYMENT_METHOD_TEXT[BUSINESS_PAYMENT_METHOD]}收款成功`, icon: 'success' })
          await refreshDetail()
        }
      })
    } else {
      actions.push({
        label: '退款',
        handler: async () => {
          await processStringingRefund(detail.value!.id)
          uni.showToast({ title: '退款成功', icon: 'success' })
          await refreshDetail()
        }
      })
    }
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
    console.error('Failed to manage stringing detail:', error)
  }
}

onLoad((options) => {
  const rawId = Number(options?.orderId || options?.id)
  if (!Number.isFinite(rawId) || rawId <= 0) {
    loadError.value = '缺少有效的工单 ID'
    return
  }
  void loadDetail(rawId)
})
</script>

<style lang="scss" scoped>
.president-stringing-detail-layout {
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

.nav-left {
  display: flex;
  align-items: center;
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
  background: linear-gradient(135deg, #fff3eb 0%, #ffffff 100%);
}

.hero-label,
.section-title,
.field-label {
  color: #8a8a8a;
  font-size: 24rpx;
}

.hero-value {
  display: block;
  margin-top: 12rpx;
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.hero-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #5f5e5e;
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
