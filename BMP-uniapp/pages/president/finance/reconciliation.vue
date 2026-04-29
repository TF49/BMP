<template>
  <PresidentLayout :showTabBar="false">
    <view class="recon-content">
      <view class="status-bar-placeholder" />
      
      <view class="recon-nav" @click="onBack">
        <uni-icons type="arrow-left" size="24" color="#ff6600" />
        <text class="recon-nav-title">财务对账</text>
      </view>

      <view class="info-card glass-card">
        <view class="info-header">
          <uni-icons type="info-filled" size="20" color="#ea580c" />
          <text class="info-title">对账说明</text>
        </view>
        <text class="info-text">{{ reconciliationHint }}</text>
      </view>

      <view class="stats-grid">
        <view class="stat-card glass-card">
          <text class="stat-label">上次对账时间</text>
          <text class="stat-value">{{ lastReconciliationTime || '暂无记录' }}</text>
        </view>
        <view class="stat-card glass-card">
          <text class="stat-label">对账状态</text>
          <text class="stat-value" :class="statusClass">{{ statusText }}</text>
        </view>
      </view>

      <view class="btn-wrap">
        <view class="btn-run" :class="{ loading: running, disabled: !isPresident }" @click="runRecon">
          <uni-icons v-if="!running" type="checkmarkempty" size="20" color="#ffffff" />
          <text>{{ running ? '对账中...' : isPresident ? '执行全面对账' : '仅会长可执行全面对账' }}</text>
        </view>
      </view>

      <view v-if="result" class="result-card glass-card">
        <view class="result-header">
          <uni-icons type="checkmarkempty" size="24" color="#22c55e" />
          <text class="result-title">对账完成</text>
        </view>
        
        <view class="result-section">
          <text class="section-title">对账摘要</text>
          <view class="result-row">
            <text class="row-label">对账时间</text>
            <text class="row-value">{{ formatDateTime(result.reconciliationTime) }}</text>
          </view>
          <view class="result-row">
            <text class="row-label">数据范围</text>
            <text class="row-value">{{ result.dateRange || '全部' }}</text>
          </view>
          <view class="result-row">
            <text class="row-label">总交易笔数</text>
            <text class="row-value highlight">{{ result.totalTransactions || 0 }}</text>
          </view>
          <view class="result-row">
            <text class="row-label">总金额</text>
            <text class="row-value highlight">¥{{ formatAmount(result.totalAmount) }}</text>
          </view>
        </view>

        <view v-if="typedDetails" class="result-section">
          <text class="section-title">业务明细</text>
          <view v-for="(item, key) in typedDetails" :key="key" class="detail-item">
            <view class="detail-header">
              <text class="detail-name">{{ getBusinessName(key) }}</text>
              <text class="detail-amount">¥{{ formatAmount(item.amount) }}</text>
            </view>
            <text class="detail-count">{{ item.count || 0 }} 笔交易</text>
          </view>
        </view>

        <view class="result-section">
          <text class="section-title">原始数据</text>
          <text class="result-json">{{ JSON.stringify(result, null, 2) }}</text>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { reconciliation } from '@/api/president/finance'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { formatDateTime } from '@/utils/format'
import { useUserStore } from '@/store/modules/user'
import { isPresidentRole } from '@/utils/roleCheck'

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.FINANCE_LIST)
}

const running = ref(false)
const result = ref<Record<string, any> | null>(null)
const lastReconciliationTime = ref<string>('')
const userStore = useUserStore()
const isPresident = computed(() => isPresidentRole(userStore.userInfo?.role))
const reconciliationHint = computed(() =>
  isPresident.value
    ? '对账将汇总各业务模块财务数据，包括场地预约、课程预约、器材租借、穿线服务等，生成完整的财务报表。'
    : '对账将汇总各业务模块财务数据，包括场地预约、课程预约、器材租借、穿线服务等。当前账号可查看状态，全面对账需由协会会长执行。'
)

const statusText = computed(() => {
  if (running.value) return '对账中'
  if (result.value) return '已完成'
  return '待执行'
})

const statusClass = computed(() => {
  if (running.value) return 'status-running'
  if (result.value) return 'status-success'
  return 'status-pending'
})

const typedDetails = computed<Record<string, { amount?: number; count?: number }> | null>(() => {
  const details = result.value?.details
  if (!details || typeof details !== 'object') return null
  return details as Record<string, { amount?: number; count?: number }>
})

function formatAmount(amount: any): string {
  const num = Number(amount || 0)
  return num.toFixed(2)
}

function getBusinessName(key: string | number): string {
  const nameMap: Record<string, string> = {
    booking: '场地预约',
    course: '课程预约',
    equipment: '器材租借',
    stringing: '穿线服务',
    tournament: '赛事报名',
    recharge: '会员充值'
  }
  return nameMap[String(key)] || String(key)
}

async function runRecon() {
  if (running.value) return
  if (!isPresident.value) {
    uni.showToast({ title: '当前账号仅可查看对账状态', icon: 'none' })
    return
  }
  running.value = true
  result.value = null
  
  try {
    const res = await reconciliation()
    result.value = (res as Record<string, any>) || {}
    lastReconciliationTime.value = formatDateTime(new Date().toISOString()) || ''
    uni.showToast({ title: '对账完成', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e?.message || '对账失败', icon: 'none' })
  } finally {
    running.value = false
  }
}

onLoad(() => {
  // 可以在这里加载上次对账记录
})
</script>

<style lang="scss" scoped>
.recon-content {
  padding: 24rpx;
  min-height: 100vh;
  background: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
}

.recon-nav {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
}

.recon-nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1e293b;
}

.glass-card {
  background: #ffffff;
  border-radius: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.info-card {
  padding: 28rpx 32rpx;
  margin-bottom: 24rpx;
}

.info-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.info-title {
  font-size: 28rpx;
  font-weight: 700;
  color: #1e293b;
}

.info-text {
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.stat-card {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.stat-label {
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 600;
}

.stat-value {
  font-size: 26rpx;
  font-weight: 700;
  color: #1e293b;
  
  &.status-running {
    color: #ea580c;
  }
  
  &.status-success {
    color: #22c55e;
  }
  
  &.status-pending {
    color: #94a3b8;
  }
}

.btn-wrap {
  margin-bottom: 32rpx;
}

.btn-run {
  width: 100%;
  padding: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  background: linear-gradient(135deg, #c2410c, #ea580c);
  color: #ffffff;
  border-radius: 20rpx;
  font-size: 30rpx;
  font-weight: 700;
  box-shadow: 0 12rpx 24rpx rgba(234, 88, 12, 0.3);
  transition: all 0.3s;
  
  &.loading {
    opacity: 0.7;
    box-shadow: 0 8rpx 16rpx rgba(234, 88, 12, 0.2);
  }

  &.disabled {
    opacity: 0.65;
  }
}

.result-card {
  padding: 32rpx;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #f1f5f9;
}

.result-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #1e293b;
}

.result-section {
  margin-bottom: 32rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  display: block;
  font-size: 26rpx;
  font-weight: 700;
  color: #475569;
  margin-bottom: 20rpx;
  text-transform: uppercase;
  letter-spacing: 1rpx;
}

.result-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f1f5f9;
  
  &:last-child {
    border-bottom: none;
  }
}

.row-label {
  font-size: 26rpx;
  color: #64748b;
}

.row-value {
  font-size: 26rpx;
  font-weight: 600;
  color: #1e293b;
  
  &.highlight {
    font-size: 30rpx;
    font-weight: 800;
    color: #ea580c;
  }
}

.detail-item {
  padding: 20rpx;
  background: #f8fafc;
  border-radius: 16rpx;
  margin-bottom: 12rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.detail-name {
  font-size: 26rpx;
  font-weight: 700;
  color: #1e293b;
}

.detail-amount {
  font-size: 28rpx;
  font-weight: 800;
  color: #ea580c;
}

.detail-count {
  font-size: 22rpx;
  color: #94a3b8;
}

.result-json {
  display: block;
  font-size: 22rpx;
  color: #64748b;
  word-break: break-all;
  white-space: pre-wrap;
  font-family: 'Courier New', monospace;
  background: #f8fafc;
  padding: 20rpx;
  border-radius: 12rpx;
  line-height: 1.6;
}
</style>
