<template>
  <PresidentLayout :showTabBar="false" className="president-notification-list-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">通知管理</text>
        </view>
        <button class="publish-btn" @click="goCreate">发布通知</button>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex @scrolltolower="loadMore">
        <view class="scroll-inner">
          <view class="search-card">
            <input
              v-model.trim="keyword"
              class="search-input"
              type="text"
              placeholder="按标题或内容搜索"
              placeholder-class="search-placeholder"
              confirm-type="search"
            />
          </view>

          <view v-if="loading && visibleList.length === 0" class="state-card">
            <text class="state-text">正在加载通知...</text>
          </view>

          <view v-else-if="!loading && visibleList.length === 0" class="state-card">
            <text class="state-text">暂无通知数据</text>
          </view>

          <view v-else class="list-wrap">
            <view v-for="item in visibleList" :key="item.id" class="notice-card" @click="openDetail(item)">
              <text class="notice-title">{{ item.title }}</text>
              <text class="notice-content">{{ item.content }}</text>
              <view class="notice-meta">
                <text>{{ item.publisherName || `发布人 #${item.publisherId || '-'}` }}</text>
                <text>{{ formatDateTime(item.createTime) || '未知时间' }}</text>
              </view>
            </view>
          </view>

          <view v-if="!keyword && list.length > 0" class="load-more">
            <text>{{ hasMore ? (loading ? '加载中...' : '上拉加载更多') : '没有更多通知了' }}</text>
          </view>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getNotificationList, type NotificationItem } from '@/api/notification'
import { formatDateTime } from '@/utils/format'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const list = ref<NotificationItem[]>([])
const keyword = ref('')

const hasMore = computed(() => list.value.length < total.value)

const visibleList = computed(() => {
  const term = keyword.value.trim().toLowerCase()
  if (!term) return list.value
  return list.value.filter((item) => `${item.title} ${item.content}`.toLowerCase().includes(term))
})

async function loadList(append = false) {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getNotificationList({ page: page.value, size: size.value })
    const records = Array.isArray(res.data) ? res.data : []
    total.value = Number(res.total || 0)
    list.value = append ? list.value.concat(records) : records
  } catch (error) {
    console.error('Failed to load notifications:', error)
    if (!append) {
      list.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value || keyword.value || !hasMore.value) return
  page.value += 1
  loadList(true)
}

function openDetail(item: NotificationItem) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.NOTIFICATION_DETAIL}?id=${item.id}` })
}

function goCreate() {
  uni.navigateTo({ url: PRESIDENT_PAGES.NOTIFICATION_FORM })
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

onLoad(() => {
  loadList(false)
})

onPullDownRefresh(() => {
  page.value = 1
  loadList(false).finally(() => uni.stopPullDownRefresh())
})
</script>

<style lang="scss" scoped>
.president-notification-list-layout {
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

.nav-header,
.nav-left,
.notice-meta {
  display: flex;
  align-items: center;
}

.nav-header,
.notice-meta {
  justify-content: space-between;
}

.nav-header {
  padding: 20rpx 32rpx 16rpx;
  background: #f9f9f9;
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

.publish-btn {
  margin: 0;
  padding: 0 28rpx;
  height: 72rpx;
  line-height: 72rpx;
  border: none;
  border-radius: 9999px;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #ffffff;
  font-size: 26rpx;
}

.publish-btn::after {
  border: none;
}

.main-scroll {
  height: calc(100vh - var(--status-bar-height) - 88rpx);
}

.scroll-inner {
  padding: 12rpx 32rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.search-card,
.state-card,
.notice-card {
  border-radius: 24rpx;
  background: #ffffff;
  padding: 28rpx 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.search-input {
  min-height: 48rpx;
  font-size: 28rpx;
  color: #1a1c1c;
}

.search-placeholder {
  color: #9ca3af;
}

.state-text,
.load-more text {
  color: #5f5e5e;
  text-align: center;
  font-size: 28rpx;
}

.list-wrap {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.notice-title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1a1c1c;
}

.notice-content {
  display: block;
  margin-top: 14rpx;
  font-size: 26rpx;
  line-height: 1.6;
  color: #5f5e5e;
}

.notice-meta {
  margin-top: 16rpx;
  gap: 16rpx;
  font-size: 22rpx;
  color: #8a8a8a;
}

.load-more {
  padding: 12rpx 0;
}
</style>
