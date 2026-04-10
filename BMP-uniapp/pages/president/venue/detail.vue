<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="handleBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#1a1c1c" />
            </view>
            <view class="brand-wrap">
              <text class="brand-name">场馆详情</text>
            </view>
          </view>
          <view class="nav-right">
            <view class="icon-btn" @click="handleShare">
              <uni-icons type="paperplane" size="20" color="#71717a" />
            </view>
          </view>
        </view>
      </view>

      <view v-if="loading" class="state-wrap">
        <view class="spinner" />
        <text class="state-text">加载中...</text>
      </view>

      <scroll-view v-else-if="detail" scroll-y class="main-content">
        <view class="hero-card">
          <image
            v-if="detail.venueImage"
            class="hero-image"
            :src="resolveImageUrl(detail.venueImage)"
            mode="aspectFill"
          />
          <view v-else class="hero-placeholder">
            <uni-icons type="image" size="34" color="#a1a1aa" />
          </view>
          <view class="hero-overlay" />
          <view class="hero-meta">
            <view class="status-pill" :class="{ offline: detail.status !== 1 }">
              <text>{{ detail.status === 1 ? '营业中' : '已停用' }}</text>
            </view>
            <text class="hero-title">{{ detail.venueName }}</text>
            <text class="hero-subtitle">ID #{{ detail.id }}</text>
          </view>
        </view>

        <view class="info-grid">
          <view class="info-card">
            <text class="info-label">地址</text>
            <text class="info-value">{{ detail.address || '暂无地址信息' }}</text>
          </view>
          <view class="info-card">
            <text class="info-label">联系人</text>
            <text class="info-value">{{ detail.contactPerson || '未设置' }}</text>
            <text class="info-sub">{{ detail.contactPhone || '暂无联系电话' }}</text>
          </view>
          <view class="info-card">
            <text class="info-label">营业时间</text>
            <text class="info-value">{{ detail.businessHours || '08:00 - 22:30' }}</text>
          </view>
          <view class="info-card">
            <text class="info-label">运营状态</text>
            <text class="info-value" :class="{ error: detail.status !== 1 }">
              {{ detail.status === 1 ? '正常营业' : '暂停服务' }}
            </text>
          </view>
        </view>

        <view class="description-card">
          <text class="description-label">场馆描述</text>
          <text class="description-text">{{ detail.description || '暂无描述信息。' }}</text>
        </view>

        <view class="action-bar">
          <view class="action-secondary" @click="goEdit">
            <uni-icons type="compose" size="18" color="#a33e00" />
            <text>编辑</text>
          </view>
          <view class="action-secondary danger" @click="onDelete">
            <uni-icons type="trash" size="18" color="#ba1a1a" />
            <text>删除</text>
          </view>
          <view class="action-primary" @click="handleBook">
            <text>立即预约</text>
            <uni-icons type="right" size="16" color="#ffffff" />
          </view>
        </view>

        <view class="bottom-space" />
      </scroll-view>

      <view v-else class="state-wrap">
        <text class="state-text">场馆不存在</text>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getVenueInfo, deleteVenue, type VenueItem } from '@/api/president/venue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { safeNavigateBack } from '@/utils/navigation'

const id = computed(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  return page?.options?.id ? Number(page.options.id) : 0
})

const loading = ref(true)
const detail = ref<VenueItem | null>(null)

async function load() {
  if (!id.value) {
    loading.value = false
    return
  }
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

function handleBack() {
  safeNavigateBack(PRESIDENT_PAGES.VENUE_LIST)
}

function goEdit() {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.VENUE_FORM}?id=${id.value}` })
}

function handleBook() {
  uni.navigateTo({ url: `/pages/venue/booking?venueId=${detail.value?.id}` })
}

function handleShare() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

function onDelete() {
  uni.showModal({
    title: '确认删除',
    content: `确定删除场馆“${detail.value?.venueName || ''}”吗？`,
    confirmColor: '#ba1a1a',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteVenue(id.value)
        uni.showToast({ title: '删除成功', icon: 'success' })
        setTimeout(() => safeNavigateBack(PRESIDENT_PAGES.VENUE_LIST), 800)
      } catch (e: any) {
        uni.showToast({ title: e?.message || '删除失败', icon: 'none' })
      }
    }
  })
}

onMounted(() => {
  load()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.nav-header {
  position: sticky;
  top: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(12px);
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
}

.back-btn,
.icon-btn {
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.main-content {
  height: calc(100vh - var(--status-bar-height) - 104rpx);
  padding: 24rpx;
}

.hero-card,
.info-card,
.description-card,
.action-bar {
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.06);
}

.hero-card {
  position: relative;
  overflow: hidden;
  min-height: 360rpx;
}

.hero-image,
.hero-placeholder {
  width: 100%;
  height: 360rpx;
}

.hero-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.08) 0%, rgba(15, 23, 42, 0.55) 100%);
}

.hero-meta {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  bottom: 24rpx;
}

.status-pill {
  display: inline-flex;
  padding: 10rpx 16rpx;
  margin-bottom: 16rpx;
  border-radius: 999rpx;
  background: rgba(22, 163, 74, 0.92);
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 700;
}

.status-pill.offline {
  background: rgba(186, 26, 26, 0.92);
}

.hero-title,
.hero-subtitle {
  display: block;
  color: #ffffff;
}

.hero-title {
  font-size: 44rpx;
  font-weight: 800;
}

.hero-subtitle {
  margin-top: 8rpx;
  font-size: 24rpx;
  opacity: 0.9;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 16rpx;
}

.info-card,
.description-card {
  padding: 24rpx;
}

.info-label,
.description-label {
  display: block;
  font-size: 22rpx;
  color: #71717a;
}

.info-value,
.info-sub,
.description-text {
  display: block;
  margin-top: 10rpx;
}

.info-value {
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.info-value.error {
  color: #ba1a1a;
}

.info-sub,
.description-text {
  font-size: 24rpx;
  color: #5f5e5e;
}

.description-card {
  margin-top: 16rpx;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 16rpx;
  padding: 16rpx;
}

.action-secondary,
.action-primary {
  height: 88rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: 28rpx;
  font-weight: 700;
}

.action-secondary {
  flex: 1;
  background: #fff1e6;
  color: #a33e00;
}

.action-secondary.danger {
  background: #fff1f2;
  color: #ba1a1a;
}

.action-primary {
  flex: 1.4;
  background: linear-gradient(135deg, #c2410c, #ea580c);
  color: #ffffff;
}

.state-wrap {
  min-height: calc(100vh - var(--status-bar-height) - 104rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  border: 6rpx solid #e5e7eb;
  border-top-color: #ea580c;
  animation: spin 1s linear infinite;
}

.state-text {
  margin-top: 20rpx;
  color: #6b7280;
}

.bottom-space {
  height: 80rpx;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}
</style>
