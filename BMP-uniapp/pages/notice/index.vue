<template>
  <view class="notice-page">
    <scroll-view scroll-y class="main-scroll" lower-threshold="120" :show-scrollbar="false" @scrolltolower="loadMore">
      <view class="topbar" :style="{ paddingTop: `${statusBarHeight || 44}px` }">
        <view class="topbar-inner">
          <view class="icon-btn" @tap="goBack">
            <uni-icons type="left" size="18" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">通知中心</text>
            <text class="topbar-sub">INBOX FEED</text>
          </view>
          <view class="icon-btn ghost" @tap="markAllAsRead">
            <uni-icons type="checkmarkempty" size="18" color="#a33e00" />
          </view>
        </view>
      </view>

      <view class="page-body">
        <view class="hero-card">
          <view class="hero-glow" />
          <text class="hero-kicker">消息动态</text>
          <text class="hero-title">时刻掌握最新动态</text>
          <text class="hero-copy">课程、赛事、充值与系统通知都集中在这里；当前设备会记录本机已读状态。</text>
          <view class="hero-stats">
            <view class="stat-chip">
              <text class="stat-label">本机未读</text>
              <text class="stat-value">{{ unreadCount }}</text>
            </view>
            <view class="stat-chip">
              <text class="stat-label">通知总数</text>
              <text class="stat-value">{{ list.length }}</text>
            </view>
          </view>
        </view>

        <view class="search-card">
          <view class="search-shell">
            <uni-icons type="search" size="18" color="#8e7164" />
            <input
              v-model.trim="keyword"
              class="search-input"
              type="text"
              placeholder="按标题或内容搜索通知"
              placeholder-class="search-placeholder"
              confirm-type="search"
            />
          </view>
          <view class="search-caption">
            <text>{{ keyword ? `搜索结果 ${visibleList.length} 条` : '按时间倒序展示最近通知' }}</text>
          </view>
        </view>

        <view class="filter-row">
          <view class="filter-pill active">全部</view>
          <view class="filter-pill ghost" @tap="markAllAsRead">本机全部已读</view>
        </view>
        <view class="local-note">
          <text>已读状态仅保存在当前设备，换设备或清除本地数据后不会同步。</text>
        </view>

        <view v-if="loading && list.length === 0" class="state-card">
          <view class="spinner" />
          <text class="state-title">加载通知中</text>
          <text class="state-desc">正在同步你的通知列表</text>
        </view>

        <view v-else-if="!loading && visibleList.length === 0" class="state-card">
          <text class="state-title">暂无通知</text>
          <text class="state-desc">{{ keyword ? '没有匹配到相关通知内容' : '当前还没有新的系统通知' }}</text>
        </view>

        <view v-else class="notice-list">
          <view
            v-for="item in visibleList"
            :key="item.id"
            class="notice-card"
            :class="{ unread: !isRead(item.id) }"
            @tap="openDetail(item)"
          >
            <view class="notice-accent" />
            <view class="notice-icon" :class="{ unread: !isRead(item.id) }">
              <uni-icons type="chatbubble" size="18" :color="!isRead(item.id) ? '#ff6600' : '#64748b'" />
            </view>
            <view class="notice-main">
              <view class="notice-head">
                <text class="notice-title">{{ item.title }}</text>
                <text class="notice-time">{{ formatTime(item.createTime) }}</text>
              </view>
              <text class="notice-content">{{ item.content }}</text>
              <view class="notice-meta">
                <text>{{ item.publisherName || `系统发布 #${item.publisherId || '-'}` }}</text>
                <text>{{ isRead(item.id) ? '已读' : '未读' }}</text>
              </view>
            </view>
          </view>
        </view>

        <view class="load-more">
          <text>{{ keyword ? '搜索模式下仅展示已加载内容' : hasMore ? (loading ? '加载中...' : '上拉加载更多') : '没有更多了' }}</text>
        </view>
      </view>

      <view class="safe-buffer" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getNotificationList, type NotificationItem } from '@/api/notification'
import { safeNavigateBack } from '@/utils/navigation'

const statusBarHeight = ref(44)
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const list = ref<NotificationItem[]>([])
const keyword = ref('')

const READ_IDS_KEY = 'notice_read_ids_v1'
const readIds = ref<Set<number>>(new Set())

function loadReadIds() {
  try {
    const raw = uni.getStorageSync(READ_IDS_KEY)
    const arr = Array.isArray(raw) ? raw : []
    readIds.value = new Set(arr.map((x: any) => Number(x)).filter((n: any) => Number.isFinite(n)))
  } catch {
    readIds.value = new Set()
  }
}

function persistReadIds() {
  try {
    uni.setStorageSync(READ_IDS_KEY, Array.from(readIds.value))
  } catch {
    // ignore
  }
}

function isRead(id: number) {
  return readIds.value.has(Number(id))
}

function markRead(id: number) {
  readIds.value.add(Number(id))
  persistReadIds()
}

const hasMore = computed(() => list.value.length < total.value)
const unreadCount = computed(() => list.value.filter((item) => !isRead(item.id)).length)

const visibleList = computed(() => {
  const term = keyword.value.trim().toLowerCase()
  if (!term) return list.value
  return list.value.filter((item) => `${item.title} ${item.content}`.toLowerCase().includes(term))
})

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  loadReadIds()
  void loadList(false)
})

const goBack = () => {
  safeNavigateBack('/pages/index/index')
}

function formatTime(createTime?: string) {
  return createTime ? String(createTime).slice(0, 16) : ''
}

const markAllAsRead = () => {
  list.value.forEach((n) => {
    if (n?.id != null) readIds.value.add(Number(n.id))
  })
  persistReadIds()
  uni.showToast({
    title: '当前设备已全部标为已读',
    icon: 'success'
  })
}

async function loadList(append: boolean) {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getNotificationList({ page: page.value, size: size.value })
    total.value = res.total || 0
    if (append) {
      list.value = list.value.concat(res.data || [])
    } else {
      list.value = res.data || []
    }
  } catch (e) {
    console.error('加载通知失败:', e)
    uni.showToast({ title: '加载通知失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (loading.value) return
  if (keyword.value) return
  if (!hasMore.value) return
  page.value += 1
  void loadList(true)
}

function openDetail(item: NotificationItem) {
  if (item?.id != null) {
    markRead(item.id)
  }
  uni.showModal({
    title: item.title || '通知',
    content: item.content || '',
    showCancel: false,
    confirmText: '我知道了'
  })
}

onPullDownRefresh(() => {
  page.value = 1
  loadList(false).finally(() => uni.stopPullDownRefresh())
})
</script>

<style lang="scss" scoped>
.notice-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.14), transparent 28%),
    #f9f9f9;
  font-family: 'Lexend', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.main-scroll {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(249, 249, 249, 0.82);
  backdrop-filter: blur(20rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.06);

  &.ghost {
    background: rgba(255, 241, 234, 0.9);
  }
}

.topbar-copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.topbar-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.topbar-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.page-body {
  padding: 12rpx 24rpx 40rpx;
}

.hero-card {
  position: relative;
  padding: 34rpx 28rpx;
  border-radius: 36rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 18rpx 40rpx rgba(163, 62, 0, 0.2);
}

.hero-glow {
  position: absolute;
  right: -80rpx;
  top: -80rpx;
  width: 260rpx;
  height: 260rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  filter: blur(36rpx);
}

.hero-kicker,
.hero-title,
.hero-copy,
.hero-stats {
  position: relative;
  z-index: 1;
}

.hero-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.78);
}

.hero-title {
  display: block;
  margin-top: 10rpx;
  font-size: 48rpx;
  line-height: 1.12;
  font-weight: 900;
  color: #ffffff;
}

.hero-copy {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.88);
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 26rpx;
}

.stat-chip {
  padding: 18rpx 20rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.14);
}

.stat-label {
  display: block;
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.stat-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 900;
  color: #ffffff;
}

.search-card {
  margin-top: 24rpx;
  padding: 24rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
}

.search-shell {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 0 18rpx;
  min-height: 84rpx;
  border-radius: 24rpx;
  background: #faf8f6;
}

.search-input {
  flex: 1;
  min-height: 44rpx;
  font-size: 26rpx;
  color: #1a1c1c;
}

.search-placeholder {
  color: #8e7164;
}

.search-caption {
  margin-top: 16rpx;
  font-size: 20rpx;
  color: #6b625c;
}

.filter-row {
  display: flex;
  gap: 12rpx;
  margin-top: 20rpx;
}

.local-note {
  margin-top: 14rpx;
  font-size: 20rpx;
  line-height: 1.6;
  color: #6b625c;
}

.filter-pill {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 800;

  &.active {
    background: #ff6600;
    color: #ffffff;
  }

  &.ghost {
    background: #fff1e8;
    color: #a33e00;
  }
}

.state-card {
  margin-top: 22rpx;
  padding: 60rpx 40rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.spinner {
  width: 54rpx;
  height: 54rpx;
  border-radius: 999rpx;
  border: 5rpx solid #ece8e6;
  border-top-color: #ff6600;
  animation: spin 0.8s linear infinite;
}

.state-title {
  margin-top: 20rpx;
  font-size: 30rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.state-desc {
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6b625c;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-top: 20rpx;
}

.notice-card {
  position: relative;
  display: flex;
  gap: 16rpx;
  padding: 26rpx 24rpx;
  border-radius: 30rpx;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 14rpx 28rpx rgba(26, 28, 28, 0.05);

  &.unread {
    background: linear-gradient(180deg, #fff8f3 0%, #ffffff 100%);
  }
}

.notice-accent {
  position: absolute;
  left: 0;
  top: 24rpx;
  bottom: 24rpx;
  width: 8rpx;
  border-radius: 0 999rpx 999rpx 0;
  background: transparent;
}

.notice-card.unread .notice-accent {
  background: #a33e00;
}

.notice-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 22rpx;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.unread {
    background: #fff1e8;
  }
}

.notice-main {
  flex: 1;
  min-width: 0;
}

.notice-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
}

.notice-title {
  flex: 1;
  font-size: 28rpx;
  line-height: 1.45;
  font-weight: 900;
  color: #1a1c1c;
}

.notice-time {
  font-size: 20rpx;
  color: #8e7164;
  white-space: nowrap;
}

.notice-content {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #6b625c;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  margin-top: 16rpx;
  font-size: 20rpx;
  color: #8e7164;
}

.load-more {
  padding: 32rpx 0;
  text-align: center;
  font-size: 20rpx;
  color: #6b625c;
}

.safe-buffer {
  height: 40rpx;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
