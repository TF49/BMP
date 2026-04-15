<template>
  <PresidentLayout :showTabBar="false" className="president-tournament-registration-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">报名详情</text>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <view v-if="loading" class="state-card">
            <text class="state-text">正在加载报名详情...</text>
          </view>

          <view v-else-if="loadError" class="state-card">
            <text class="state-text">{{ loadError }}</text>
          </view>

          <template v-else-if="detail">
            <view class="hero-card">
              <text class="hero-label">报名状态</text>
              <text class="hero-value">{{ statusLabel }}</text>
              <text class="hero-sub">{{ detail.registrationNo || `报名 #${detail.id}` }}</text>
            </view>

            <view class="card">
              <text class="section-title">赛事与报名信息</text>
              <view class="field-row">
                <text class="field-label">赛事名称</text>
                <text class="field-value">{{ detail.tournamentName || '未命名赛事' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">报名会员</text>
                <text class="field-value">{{ detail.memberName || `会员 #${detail.memberId || '-'}` }}</text>
              </view>
              <view v-if="detail.partnerName" class="field-row">
                <text class="field-label">搭档</text>
                <text class="field-value">{{ detail.partnerName }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">赛事时间</text>
                <text class="field-value">{{ tournamentTimeLabel }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">报名时间</text>
                <text class="field-value">{{ formatDateTime(detail.createTime) || '未知时间' }}</text>
              </view>
            </view>

            <view class="card">
              <text class="section-title">支付与结果</text>
              <view class="field-row">
                <text class="field-label">报名费</text>
                <text class="field-value amount">¥{{ formatAmount(detail.entryFee || 0) }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">支付方式</text>
                <text class="field-value">{{ detail.paymentMethod || '未知支付方式' }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">支付状态</text>
                <text class="field-value">{{ paymentStatusLabel }}</text>
              </view>
              <view class="field-row">
                <text class="field-label">比赛结果</text>
                <text class="field-value">{{ detail.matchResult || '暂无结果' }}</text>
              </view>
              <view class="field-row multiline">
                <text class="field-label">备注</text>
                <text class="field-value remark">{{ detail.remark || '无' }}</text>
              </view>
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
import { getTournamentRegistrationDetail, type TournamentRegistrationItem } from '@/api/president/tournament'
import { formatAmount, formatDateTime } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

type TournamentRegistrationDetail = TournamentRegistrationItem & {
  paymentStatus?: number
  remark?: string
  matchResult?: string
  tournamentStartTime?: string
  tournamentEndTime?: string
}

const detail = ref<TournamentRegistrationDetail | null>(null)
const loading = ref(false)
const loadError = ref('')

const statusLabel = computed(() => {
  const status = detail.value?.status
  if (status === 0) return '已取消'
  if (status === 1) return '待支付'
  if (status === 2) return '已支付'
  if (status === 3) return '已参赛'
  if (status === 4) return '已退赛'
  return '未知状态'
})

const paymentStatusLabel = computed(() => {
  const paymentStatus = detail.value?.paymentStatus
  if (paymentStatus === 1) return '支付成功'
  if (paymentStatus === 0) return '待支付'
  if (paymentStatus === 2) return '已退款'

  const registrationStatus = detail.value?.status
  if (registrationStatus === 1) return '待支付'
  if (registrationStatus === 2 || registrationStatus === 3 || registrationStatus === 4) return '支付成功'
  return '支付状态未知'
})

const tournamentTimeLabel = computed(() => {
  const current = detail.value
  if (!current) return '未知赛事时间'
  const start = formatDateTime(current.tournamentStartTime)
  const end = formatDateTime(current.tournamentEndTime)
  if (start && end) return `${start} - ${end}`
  return start || end || '未知赛事时间'
})

async function loadDetail(id: number) {
  loading.value = true
  loadError.value = ''
  try {
    detail.value = (await getTournamentRegistrationDetail(id)) as TournamentRegistrationDetail
  } catch (error) {
    console.error('Failed to load tournament registration detail:', error)
    detail.value = null
    loadError.value = '报名详情加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.TOURNAMENT_REGISTRATION_LIST)
}

onLoad((options) => {
  const rawId = Number(options?.registrationId || options?.id)
  if (!Number.isFinite(rawId) || rawId <= 0) {
    loadError.value = '缺少有效的报名 ID'
    return
  }
  loadDetail(rawId)
})
</script>

<style lang="scss" scoped>
.president-tournament-registration-detail-layout {
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
</style>
