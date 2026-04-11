<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-bar-left">
          <view class="icon-round" @click="goBack">
            <uni-icons type="arrow-left" size="24" color="#ff6600" />
          </view>
          <text class="top-title">管理</text>
        </view>
        <view class="icon-round">
          <uni-icons type="search" size="22" color="#ff6600" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="content">
          <view class="hero-block">
            <text class="hero-heading">课程预约列表</text>
            <text class="hero-desc">查看和管理所有区域中心的近期运动认证和技能研讨会预约。</text>
            <view class="hero-actions">
              <button class="btn-secondary" @click="onExport">导出数据</button>
              <button class="btn-primary" @click="openNewCourse">新增课程</button>
            </view>
          </view>

          <view class="tabs-search">
            <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
              <view class="tabs-inner">
                <view
                  v-for="tab in tabs"
                  :key="tab.key"
                  class="tab-chip"
                  :class="{ active: activeTab === tab.key }"
                  @click="activeTab = tab.key"
                >
                  <text class="tab-text" :class="{ 'tab-text-active': activeTab === tab.key }">{{ tabLabel(tab) }}</text>
                </view>
              </view>
            </scroll-view>
            <view class="search-field">
              <uni-icons class="search-ico" type="search" size="20" color="#5f5e5e" />
              <input v-model="keyword" class="search-input" type="text" placeholder="搜索会员或课程..." confirm-type="search" />
            </view>
          </view>

          <view class="list">
            <view
              v-for="row in visibleBookings"
              :key="row.id"
              class="booking-card"
              :class="{ cancelled: row.status === 'cancelled' }"
              @click="openDetail(row)"
            >
              <view class="card-top">
                <image class="avatar" :src="row.avatar" mode="aspectFill" />
                <view class="user-block">
                  <view class="name-row">
                    <text class="user-name">{{ row.name }}</text>
                    <view class="tier-pill" :class="row.memberTier === 'pro' ? 'tier-pro' : 'tier-normal'">
                      <text class="tier-text">{{ row.memberTier === 'pro' ? 'PRO 会员' : '普通会员' }}</text>
                    </view>
                  </view>
                  <view class="email-row">
                    <uni-icons type="email" size="14" color="#5f5e5e" />
                    <text class="email-text">{{ row.email }}</text>
                  </view>
                </view>
                <view class="more-hit" @click.stop="onMore(row)">
                  <uni-icons type="more-filled" size="22" color="#5f5e5e" />
                </view>
              </view>

              <view class="detail-grid">
                <view class="detail-cell">
                  <text class="detail-label">课程名称</text>
                  <text class="detail-value">{{ row.courseName }}</text>
                </view>
                <view class="detail-cell">
                  <text class="detail-label">日期 & 时间</text>
                  <text class="detail-value">{{ row.dateTime }}</text>
                </view>
              </view>

              <view class="status-row">
                <text class="status-label">状态</text>
                <view class="status-pill" :class="`st-${row.status}`">
                  <view v-if="row.status === 'pending'" class="pulse-dot" />
                  <view v-else class="status-dot" :class="`dot-${row.status}`" />
                  <text class="status-pill-text">{{ statusText(row.status) }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="load-more-wrap">
            <button class="load-more" @click="onLoadMore">
              <text class="load-more-text">加载更多预约</text>
              <uni-icons type="down" size="20" color="#5f5e5e" />
            </button>
          </view>

          <view class="scroll-bottom-pad" />
        </view>
      </scroll-view>

      <view class="fab" @click="openNewCourse">
        <uni-icons type="plusempty" size="36" color="#561d00" />
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

type BookingStatus = 'pending' | 'confirmed' | 'cancelled'
type TabKey = 'all' | BookingStatus

type Booking = {
  id: string
  name: string
  avatar: string
  memberTier: 'pro' | 'normal'
  email: string
  courseName: string
  dateTime: string
  status: BookingStatus
}

const activeTab = ref<TabKey>('all')
const keyword = ref('')

const tabs: { key: TabKey; base: string }[] = [
  { key: 'all', base: '全部预约' },
  { key: 'pending', base: '待处理' },
  { key: 'confirmed', base: '已确认' },
  { key: 'cancelled', base: '已取消' }
]

const allBookings = ref<Booking[]>([
  {
    id: '1',
    name: 'Julian Schmidt',
    avatar:
      '/static/placeholders/hero.svg',
    memberTier: 'pro',
    email: 'julian.s@kinetic.com',
    courseName: '高级扣杀动力学',
    dateTime: '10月 24, 2023 • 14:00 PM',
    status: 'pending'
  },
  {
    id: '2',
    name: 'Mila Laurent',
    avatar:
      '/static/placeholders/hero.svg',
    memberTier: 'normal',
    email: 'mila.l@web.fr',
    courseName: '耐力蓝图 II 级',
    dateTime: '10月 26, 2023 • 09:30 AM',
    status: 'confirmed'
  },
  {
    id: '3',
    name: 'Daisuke Ken',
    avatar:
      '/static/placeholders/hero.svg',
    memberTier: 'pro',
    email: 'd.ken@tokyo-bad.jp',
    courseName: '心理战：半职业预备',
    dateTime: '10月 28, 2023 • 18:00 PM',
    status: 'cancelled'
  },
  {
    id: '4',
    name: 'Sarah Williams',
    avatar:
      '/static/placeholders/hero.svg',
    memberTier: 'normal',
    email: 'sarah.w@uk-athletics.co.uk',
    courseName: '敏捷与步法大师课',
    dateTime: '10月 30, 2023 • 11:00 AM',
    status: 'confirmed'
  }
])

const counts = computed(() => {
  const list = allBookings.value
  return {
    all: list.length,
    pending: list.filter(b => b.status === 'pending').length,
    confirmed: list.filter(b => b.status === 'confirmed').length,
    cancelled: list.filter(b => b.status === 'cancelled').length
  }
})

function tabLabel(tab: (typeof tabs)[0]) {
  const n =
    tab.key === 'all'
      ? counts.value.all
      : tab.key === 'pending'
        ? counts.value.pending
        : tab.key === 'confirmed'
          ? counts.value.confirmed
          : counts.value.cancelled
  return `${tab.base} (${n})`
}

const filteredBookings = computed(() => {
  let list = allBookings.value
  if (activeTab.value !== 'all') {
    list = list.filter(b => b.status === activeTab.value)
  }
  const q = keyword.value.trim().toLowerCase()
  if (q) {
    list = list.filter(
      b =>
        b.name.toLowerCase().includes(q) ||
        b.email.toLowerCase().includes(q) ||
        b.courseName.toLowerCase().includes(q)
    )
  }
  return list
})

const visibleBookings = computed(() => filteredBookings.value)

function statusText(s: BookingStatus) {
  if (s === 'pending') return '待处理'
  if (s === 'confirmed') return '已确认'
  return '已取消'
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function openNewCourse() {
  uni.navigateTo({ url: PRESIDENT_PAGES.COURSE_FORM })
}

function openDetail(row: Booking) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COURSE_BOOKING_DETAIL}?id=${encodeURIComponent(row.id)}` })
}

function onExport() {
  uni.showToast({ title: '导出任务已创建（示例）', icon: 'none' })
}

function onLoadMore() {
  uni.showToast({ title: '暂无更多数据', icon: 'none' })
}

function onMore(row: Booking) {
  uni.showActionSheet({
    itemList: ['查看详情', '联系会员'],
    success(res) {
      if (res.tapIndex === 0) openDetail(row)
    }
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}
.top-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 28rpx 20rpx;
  background: #f9f9f9;
  position: sticky;
  top: 0;
  z-index: 40;
}
.top-bar-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}
.icon-round {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-round:active {
  background: rgba(0, 0, 0, 0.05);
}
.top-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}
.scroll {
  flex: 1;
  height: 0;
}
.content {
  padding: 16rpx 28rpx 200rpx;
  max-width: 1600rpx;
  margin: 0 auto;
}

.hero-block {
  margin-bottom: 40rpx;
}
.hero-heading {
  display: block;
  font-size: 64rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: -0.04em;
  line-height: 1.1;
}
.hero-desc {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #5f5e5e;
  line-height: 1.5;
}
.hero-actions {
  margin-top: 28rpx;
  display: flex;
  flex-direction: row;
  gap: 16rpx;
  flex-wrap: wrap;
}
.btn-secondary {
  margin: 0;
  padding: 0 36rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: #e2e2e2;
  color: #1a1c1c;
  font-size: 26rpx;
  font-weight: 800;
  border-radius: 16rpx;
  border: none;
}
.btn-secondary::after {
  border: none;
}
.btn-primary {
  margin: 0;
  padding: 0 36rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: #ff6600;
  color: #561d00;
  font-size: 26rpx;
  font-weight: 800;
  border-radius: 16rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(255, 102, 0, 0.25);
}
.btn-primary::after {
  border: none;
}

.tabs-search {
  margin-bottom: 32rpx;
}
.tabs-scroll {
  width: 100%;
  white-space: nowrap;
  margin-bottom: 20rpx;
}
.tabs-inner {
  display: inline-flex;
  flex-direction: row;
  gap: 12rpx;
  padding: 8rpx;
  background: #f3f3f3;
  border-radius: 20rpx;
}
.tab-chip {
  padding: 16rpx 28rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}
.tab-chip.active {
  background: #ffffff;
  box-shadow: 0 2rpx 8rpx rgba(26, 28, 28, 0.08);
}
.tab-text {
  font-size: 24rpx;
  font-weight: 800;
  color: #5f5e5e;
}
.tab-text-active {
  color: #a33e00;
}
.search-field {
  position: relative;
}
.search-ico {
  position: absolute;
  left: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.5;
  z-index: 1;
}
.search-input {
  width: 100%;
  padding: 24rpx 24rpx 24rpx 72rpx;
  background: #ffffff;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #1a1c1c;
  border: none;
  box-sizing: border-box;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.booking-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 16rpx rgba(26, 28, 28, 0.06);
}
.booking-card.cancelled {
  opacity: 0.82;
}
.card-top {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  gap: 24rpx;
}
.avatar {
  width: 112rpx;
  height: 112rpx;
  border-radius: 9999px;
  background: #e8e8e8;
  flex-shrink: 0;
}
.user-block {
  flex: 1;
  min-width: 0;
}
.name-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 8rpx;
}
.user-name {
  font-size: 32rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.tier-pill {
  padding: 6rpx 14rpx;
  border-radius: 9999px;
}
.tier-pro {
  background: #ffdbcd;
}
.tier-pro .tier-text {
  color: #360f00;
}
.tier-normal {
  background: #e8e8e8;
}
.tier-normal .tier-text {
  color: #1a1c1c;
}
.tier-text {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.email-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}
.email-text {
  font-size: 24rpx;
  color: #5f5e5e;
}
.more-hit {
  padding: 8rpx;
  flex-shrink: 0;
}

.detail-grid {
  margin-top: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.detail-cell {
  padding-left: 16rpx;
  border-left: 4rpx solid rgba(255, 102, 0, 0.2);
}
.detail-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}
.detail-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.status-row {
  margin-top: 28rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.status-label {
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}
.status-pill {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  padding: 10rpx 20rpx;
  border-radius: 9999px;
}
.st-pending {
  background: #e2dfde;
}
.st-pending .status-pill-text {
  color: #636262;
}
.st-confirmed {
  background: rgba(255, 102, 0, 0.12);
}
.st-confirmed .status-pill-text {
  color: #a33e00;
}
.st-cancelled {
  background: #ffdad6;
}
.st-cancelled .status-pill-text {
  color: #93000a;
}
.pulse-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 9999px;
  background: #f59e0b;
}
.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 9999px;
}
.dot-confirmed {
  background: #a33e00;
}
.dot-cancelled {
  background: #ba1a1a;
}
.status-pill-text {
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.load-more-wrap {
  margin-top: 48rpx;
  display: flex;
  justify-content: center;
}
.load-more {
  margin: 0;
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  padding: 28rpx 48rpx;
  background: #f3f3f3;
  border-radius: 24rpx;
  border: none;
}
.load-more::after {
  border: none;
}
.load-more-text {
  font-size: 28rpx;
  font-weight: 800;
  color: #5f5e5e;
}

.scroll-bottom-pad {
  height: 32rpx;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: calc(40rpx + env(safe-area-inset-bottom));
  width: 112rpx;
  height: 112rpx;
  border-radius: 9999px;
  background: #ff6600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(255, 102, 0, 0.4);
  z-index: 60;
}
.fab:active {
  transform: scale(0.96);
}

@media screen and (min-width: 768px) {
  .detail-grid {
    flex-direction: row;
    gap: 32rpx;
  }
  .detail-cell {
    flex: 1;
  }
  .card-top {
    align-items: center;
  }
}
</style>
