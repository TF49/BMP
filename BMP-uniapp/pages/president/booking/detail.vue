<template>
  <PresidentLayout :showTabBar="false">
    <PresidentNavBar title="预约详情" />
    <view class="detail-content">
      <view v-if="loading" class="loading-wrap">加载中...</view>
      <view v-else-if="detail" class="detail-card glass-card">
        <view class="detail-row"><text class="label">场馆</text><text class="value">{{ detail.venueName || '—' }}</text></view>
        <view class="detail-row"><text class="label">场地</text><text class="value">{{ detail.courtName || '—' }}</text></view>
        <view class="detail-row"><text class="label">预约日期</text><text class="value">{{ detail.bookingDate || '—' }}</text></view>
        <view class="detail-row"><text class="label">时段</text><text class="value">{{ detail.startTime }}-{{ detail.endTime }}</text></view>
        <view class="detail-row"><text class="label">用户</text><text class="value">{{ detail.userName || '—' }}</text></view>
        <view class="detail-row"><text class="label">金额</text><text class="value">¥{{ formatNum(detail.amount) }}</text></view>
        <view class="detail-row"><text class="label">状态</text><text class="value">{{ statusText(detail.status) }}</text></view>
        <view class="btn-row" v-if="detail.status === 1 || detail.status === 2 || detail.status === 3">
          <view class="btn-cancel" @click="onCancel">取消预约</view>
        </view>
      </view>
      <view v-else class="empty">预约不存在</view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import PresidentNavBar from '@/components/president/PresidentNavBar.vue'
import { getBookingDetail, cancelBooking, type BookingItem } from '@/api/president/booking'

const id = computed(() => {
  const pages = getCurrentPages()
  const p = pages[pages.length - 1] as any
  return p?.options?.id ? Number(p.options.id) : 0
})
const loading = ref(true)
const detail = ref<BookingItem | null>(null)

function formatNum(v: unknown) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 }) : '0'
}
function statusText(s: unknown) {
  const m: Record<number, string> = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return m[Number(s)] ?? '—'
}

async function load() {
  if (!id.value) return
  loading.value = true
  try {
    const res = await getBookingDetail(id.value)
    detail.value = (res as BookingItem) || null
  } catch { detail.value = null } finally { loading.value = false }
}

function onCancel() {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该预约吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await cancelBooking(id.value)
        uni.showToast({ title: '已取消', icon: 'success' })
        load()
      } catch (e: any) {
        uni.showToast({ title: e?.message || '取消失败', icon: 'none' })
      }
    }
  })
}

onMounted(() => load())
</script>

<style lang="scss" scoped>
.detail-content { padding: 24rpx; padding-top: 120rpx; }
.detail-card { padding: 32rpx; border-radius: 24rpx; }
.detail-row { display: flex; justify-content: space-between; padding: 24rpx 0; border-bottom: 1rpx solid #E2E8F0; }
.label { font-size: 28rpx; color: #475569; }
.value { font-size: 28rpx; color: #1E293B; }
.btn-row { margin-top: 40rpx; }
.btn-cancel { text-align: center; padding: 24rpx; background: #fef2f2; color: #ef4444; border-radius: 16rpx; font-size: 28rpx; }
.loading-wrap, .empty { padding: 80rpx; text-align: center; color: #475569; }
</style>
