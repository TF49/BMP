<template>
  <PresidentLayout :showTabBar="false" className="president-notification-detail-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">通知详情</text>
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <view v-if="loading" class="state-card">
            <text class="state-text">正在加载通知详情...</text>
          </view>

          <view v-else-if="loadError" class="state-card">
            <text class="state-text">{{ loadError }}</text>
          </view>

          <template v-else-if="detail">
            <view class="hero-card">
              <text class="hero-tag">官方通知</text>
              <text class="hero-title">{{ detail.title }}</text>
              <view class="meta-row">
                <text>{{ formatDateTime(detail.createTime) || '未知时间' }}</text>
                <text>{{ publisherLabel }}</text>
                <text v-if="detail.venueId">{{ venueLabel(detail.venueId) }}</text>
              </view>
            </view>

            <view class="content-card">
              <text class="section-title">通知正文</text>
              <text class="content-text">{{ detail.content }}</text>
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
import { getNotificationDetail, type NotificationItem } from '@/api/president/notification'
import { getVenueList } from '@/api/president/venue'
import { formatDateTime } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const detail = ref<NotificationItem | null>(null)
const loading = ref(false)
const loadError = ref('')
const venueNameMap = ref<Record<number, string>>({})

const publisherLabel = computed(() => {
  if (!detail.value) return ''
  if (detail.value.publisherName) return detail.value.publisherName
  if (detail.value.publisherId) return `发布人 #${detail.value.publisherId}`
  return '未知发布人'
})

function venueLabel(venueId: number) {
  return venueNameMap.value[venueId] || `场馆 #${venueId}`
}

async function loadVenueOptions() {
  try {
    const res = await getVenueList({ page: 1, size: 200 })
    const list = Array.isArray(res?.data) ? res.data : []
    venueNameMap.value = list.reduce<Record<number, string>>((map, item) => {
      if (item?.id && item.venueName) {
        map[item.id] = item.venueName
      }
      return map
    }, {})
  } catch (error) {
    console.error('Failed to load venue options for notification detail:', error)
    venueNameMap.value = {}
  }
}

async function loadDetail(id: number) {
  loading.value = true
  loadError.value = ''
  try {
    detail.value = await getNotificationDetail(id)
  } catch (error) {
    console.error('Failed to load notification detail:', error)
    detail.value = null
    loadError.value = '通知详情加载失败'
  } finally {
    loading.value = false
  }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.NOTIFICATION_LIST)
}

onLoad((options) => {
  const rawId = Number(options?.id)
  if (!Number.isFinite(rawId) || rawId <= 0) {
    loadError.value = '缺少有效的通知 ID'
    return
  }
  void loadVenueOptions()
  void loadDetail(rawId)
})
</script>

<style lang="scss" scoped>
.president-notification-detail-layout {
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
.content-card {
  border-radius: 24rpx;
  background: #ffffff;
  padding: 32rpx;
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

.hero-tag {
  display: inline-flex;
  padding: 8rpx 18rpx;
  border-radius: 9999px;
  background: #ff6600;
  color: #ffffff;
  font-size: 22rpx;
}

.hero-title {
  display: block;
  margin-top: 18rpx;
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #5f5e5e;
}

.section-title {
  display: block;
  margin-bottom: 18rpx;
  color: #1a1c1c;
  font-size: 28rpx;
  font-weight: 700;
}

.content-text {
  color: #374151;
  font-size: 28rpx;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
