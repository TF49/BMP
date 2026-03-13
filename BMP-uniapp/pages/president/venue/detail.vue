<template>
  <PresidentLayout :showTabBar="false">
    <PresidentNavBar title="场馆详情" />
    <view class="detail-content">
      <view v-if="loading" class="loading-wrap">加载中...</view>
      <view v-else-if="detail" class="detail-card glass-card">
        <view class="detail-row"><text class="label">场馆名称</text><text class="value">{{ detail.venueName }}</text></view>
        <view class="detail-row"><text class="label">地址</text><text class="value">{{ detail.address || '—' }}</text></view>
        <view class="detail-row"><text class="label">联系人</text><text class="value">{{ detail.contactPerson || '—' }}</text></view>
        <view class="detail-row"><text class="label">联系电话</text><text class="value">{{ detail.contactPhone || '—' }}</text></view>
        <view class="detail-row"><text class="label">营业时间</text><text class="value">{{ detail.businessHours || '—' }}</text></view>
        <view class="detail-row"><text class="label">状态</text><text class="value">{{ detail.status === 1 ? '营业中' : '关闭' }}</text></view>
        <view class="btn-row">
          <view class="btn-edit" @click="goEdit">编辑</view>
          <view class="btn-del" @click="onDelete">删除</view>
        </view>
      </view>
      <view v-else class="empty">场馆不存在</view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import PresidentNavBar from '@/components/president/PresidentNavBar.vue'
import { getVenueInfo, deleteVenue, type VenueItem } from '@/api/president/venue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const id = computed(() => {
  const pages = getCurrentPages()
  const p = pages[pages.length - 1] as any
  return p?.options?.id ? Number(p.options.id) : 0
})
const loading = ref(true)
const detail = ref<VenueItem | null>(null)

async function load() {
  if (!id.value) return
  loading.value = true
  try {
    const res = await getVenueInfo(id.value)
    detail.value = (res as VenueItem) || null
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function goEdit() {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.VENUE_FORM}?id=${id.value}` })
}

function onDelete() {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除场馆「${detail.value?.venueName}」吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteVenue(id.value)
        uni.showToast({ title: '删除成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch (e: any) {
        uni.showToast({ title: e?.message || '删除失败', icon: 'none' })
      }
    }
  })
}

onMounted(() => load())
</script>

<style lang="scss" scoped>
.detail-content { padding: 24rpx; padding-top: 120rpx; }
.detail-card { padding: 32rpx; border-radius: 24rpx; }
.detail-row {
  display: flex; justify-content: space-between; padding: 24rpx 0;
  border-bottom: 1rpx solid var(--color-border, #E2E8F0);
}
.label { font-size: 28rpx; color: #475569; }
.value { font-size: 28rpx; color: #1E293B; }
.btn-row { display: flex; gap: 24rpx; margin-top: 40rpx; }
.btn-edit {
  flex: 1; text-align: center; padding: 24rpx; background: linear-gradient(135deg, #3cc51f, #4ade80);
  color: #fff; border-radius: 16rpx; font-size: 28rpx;
}
.btn-del {
  flex: 1; text-align: center; padding: 24rpx; background: #f1f5f9; color: #64748b;
  border-radius: 16rpx; font-size: 28rpx;
}
.loading-wrap, .empty { padding: 80rpx; text-align: center; color: #475569; }
</style>
