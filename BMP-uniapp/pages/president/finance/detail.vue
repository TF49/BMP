<template>
  <PresidentLayout :showTabBar="false">
    <view class="detail-content">
      <view class="status-bar-placeholder" />
      <view class="detail-nav" @click="onBack">
        <uni-icons type="arrow-left" size="24" color="#ff6600" />
        <text class="detail-nav-title">财务详情</text>
      </view>
      <view v-if="loading" class="loading-wrap">加载中...</view>
      <view v-else-if="detail" class="detail-card glass-card">
        <view class="detail-row"><text class="label">业务类型</text><text class="value">{{ detail.businessType || '—' }}</text></view>
        <view class="detail-row"><text class="label">收支类型</text><text class="value">{{ detail.incomeExpenseType === 'INCOME' ? '收入' : '支出' }}</text></view>
        <view class="detail-row"><text class="label">金额</text><text class="value amount">¥{{ formatNum(detail.amount) }}</text></view>
        <view class="detail-row"><text class="label">支付方式</text><text class="value">{{ detail.paymentMethod || '—' }}</text></view>
        <view class="detail-row"><text class="label">场馆</text><text class="value">{{ detail.venueName || '—' }}</text></view>
        <view class="detail-row"><text class="label">操作人</text><text class="value">{{ detail.operatorName || '—' }}</text></view>
        <view class="detail-row"><text class="label">备注</text><text class="value">{{ detail.remark || '—' }}</text></view>
        <view class="detail-row"><text class="label">创建时间</text><text class="value">{{ detail.createTime || '—' }}</text></view>
        <view v-if="isPresident" class="btn-row">
          <view class="btn-del" @click="onDelete">删除记录</view>
        </view>
      </view>
      <view v-else class="empty">记录不存在</view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getFinanceInfo, deleteFinance, type FinanceItem } from '@/api/president/finance'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { useUserStore } from '@/store/modules/user'
import { isPresidentRole } from '@/utils/roleCheck'

const id = computed(() => {
  const pages = getCurrentPages()
  const p = pages[pages.length - 1] as any
  return p?.options?.id ? Number(p.options.id) : 0
})
const loading = ref(true)
const detail = ref<FinanceItem | null>(null)
const userStore = useUserStore()
const isPresident = computed(() => isPresidentRole(userStore.userInfo?.role))

function formatNum(v: unknown) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) : '0'
}

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.FINANCE_LIST)
}

async function load() {
  if (!id.value) return
  loading.value = true
  try {
    const res = await getFinanceInfo(id.value)
    detail.value = (res as FinanceItem) || null
  } catch { detail.value = null } finally { loading.value = false }
}

function onDelete() {
  if (!isPresident.value) {
    uni.showToast({ title: '当前账号无权删除财务记录', icon: 'none' })
    return
  }
  uni.showModal({
    title: '确认删除',
    content: '确定要删除该财务记录吗？该操作仅协会会长可执行。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteFinance(id.value)
        uni.showToast({ title: '删除成功', icon: 'success' })
        setTimeout(() => safeNavigateBack(PRESIDENT_PAGES.FINANCE_LIST), 800)
      } catch (e: any) {
        uni.showToast({ title: e?.message || '删除失败', icon: 'none' })
      }
    }
  })
}

onMounted(() => load())
</script>

<style lang="scss" scoped>
.detail-content { padding: 24rpx; }
.status-bar-placeholder { height: var(--status-bar-height); }
.detail-nav { display: flex; align-items: center; gap: 12rpx; margin-bottom: 20rpx; }
.detail-nav-title { font-size: 32rpx; font-weight: 800; color: #1e293b; }
.detail-card { padding: 32rpx; border-radius: 24rpx; }
.detail-row { display: flex; justify-content: space-between; padding: 24rpx 0; border-bottom: 1rpx solid #E2E8F0; }
.label { font-size: 28rpx; color: #475569; }
.value { font-size: 28rpx; color: #1E293B; }
.value.amount { font-weight: 600; font-size: 32rpx; }
.btn-row { margin-top: 40rpx; }
.btn-del { text-align: center; padding: 24rpx; background: #fef2f2; color: #ef4444; border-radius: 16rpx; font-size: 28rpx; }
.loading-wrap, .empty { padding: 80rpx; text-align: center; color: #475569; }
</style>
