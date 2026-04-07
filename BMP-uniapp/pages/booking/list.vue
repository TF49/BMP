<template>
  <view class="page">
    <view class="top-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="top-inner">
        <view class="brand">
          <uni-icons type="star-filled" size="18" color="#ff6a00" />
          <text class="brand-text">Kinetic Logic 羽擎</text>
        </view>
        <view class="actions">
          <view class="icon-btn" @tap="handleNotifications">
            <uni-icons type="chatbubble" size="18" color="#3a3a3a" />
          </view>
          <view class="avatar" @tap="handleProfile">
            <image class="avatar-img" :src="avatarUrl" mode="aspectFill" />
          </view>
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="content"
      :style="{ paddingTop: topOffset + 'px' }"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      show-scrollbar="false"
    >
      <view class="wrap">
        <view class="title-row">
          <view>
            <text class="sub-title">活动中心</text>
            <text class="title">我的预约</text>
          </view>
          <view class="tabs">
            <view
              v-for="(tab, i) in tabs"
              :key="tab"
              class="tab"
              :class="{ active: currentTab === i }"
              @tap="switchTab(i)"
            >
              {{ tab }}
            </view>
          </view>
        </view>

        <view
          v-if="nextMatch && (currentTab === 0 || currentTab === 1)"
          class="hero-card"
          @tap="handleBookingClick(nextMatch)"
        >
          <text class="hero-badge">下一场比赛</text>
          <text class="hero-title">{{ nextMatch.venueName }}</text>
          <text class="hero-sub">{{ nextMatch.bookingNo }} · {{ nextMatch.courtName }}</text>
          <view class="hero-grid">
            <view class="hero-cell">
              <text class="cell-k">DATE</text>
              <text class="cell-v">{{ formatDateShort(nextMatch.date) }}</text>
            </view>
            <view class="hero-cell">
              <text class="cell-k">TIME</text>
              <text class="cell-v">{{ nextMatch.startTime }} - {{ nextMatch.endTime }}</text>
            </view>
          </view>
          <view class="hero-actions">
            <button class="btn-checkin" @tap.stop="handleCheckIn(nextMatch)">立即签到</button>
            <view class="btn-share" @tap.stop="handleShare(nextMatch)">
              <uni-icons type="paperplane" size="18" color="#444" />
            </view>
          </view>
        </view>

        <view v-if="currentTab === 0" class="stats">
          <text class="stats-k">预约概览</text>
          <view class="stats-row">
            <text class="stats-v">{{ stats.monthlyCount }}</text>
            <text class="stats-t">本月预约</text>
          </view>
          <view class="divider" />
          <view class="stats-row">
            <text class="stats-v money">${{ stats.totalSpent }}</text>
            <text class="stats-t">Total Spent</text>
          </view>
          <view class="vip">
            <uni-icons type="medal" size="16" color="#ff8d3a" />
            <text>金牌会员状态</text>
          </view>
        </view>

        <view
          v-for="item in filteredList"
          :key="item.id"
          class="card"
          :class="{ cancelled: item.status === 0 }"
          @tap="handleBookingClick(item)"
        >
          <view class="card-head">
            <view>
              <text class="booking-no">{{ item.bookingNo }}</text>
              <text class="status" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</text>
            </view>
            <text class="amount" :class="{ strike: item.status === 0 }">${{ item.amount }}</text>
          </view>
          <text class="venue" :class="{ muted: item.status === 0 }">{{ item.venueName }}</text>
          <view class="line"><uni-icons type="location" size="14" color="#666" /><text>{{ item.courtName }}</text></view>
          <view class="line"><uni-icons type="calendar" size="14" color="#666" /><text>{{ item.date }}</text></view>
          <view class="line"><uni-icons type="refreshtime" size="14" color="#666" /><text>{{ item.startTime }} - {{ item.endTime }}</text></view>

          <view v-if="item.status === 0" class="refund">退款已入账至钱包</view>
          <button v-else class="card-btn" :class="{ rebook: item.status === 4 }" @tap.stop="handleAction(item)">
            {{ item.status === 4 ? '再次预约' : '查看详情' }}
          </button>
        </view>

        <view v-if="filteredList.length === 0 && !nextMatch" class="empty">
          <uni-icons type="calendar" size="36" color="#b6b6b6" />
          <text>暂无预约记录</text>
        </view>
      </view>
    </scroll-view>

    <CustomTabBar :current="-1" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { getBookingList } from '@/api/booking'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { getAvatarImage } from '@/utils/displayImage'

const userStore = useUserStore()
const currentTab = ref(0)
const statusBarHeight = ref(20)
const tabs = ['全部', '进行中', '已完成']
const bookingList = ref<any[]>([])
const loading = ref(false)
const refreshing = ref(false)
const topOffset = computed(() => statusBarHeight.value + 54)
const avatarUrl = computed(() => getAvatarImage(userStore.userInfo?.avatar))

// 统计数据
const stats = computed(() => {
  const now = new Date()
  const thisMonth = now.getMonth()
  const thisYear = now.getFullYear()
  
  const monthly = bookingList.value.filter(item => {
    const d = new Date(item.date)
    return d.getMonth() === thisMonth && d.getFullYear() === thisYear
  })
  
  const total = bookingList.value.reduce((acc, curr) => acc + (Number(curr.amount) || 0), 0)
  
  return {
    monthlyCount: monthly.length,
    totalSpent: total.toFixed(0)
  }
})

// 下一场比赛 (优先支付或进行中的，且日期最近的)
const nextMatch = computed(() => {
  const ongoing = bookingList.value
    .filter(item => item.status === 2 || item.status === 3)
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime())
  return ongoing[0] || null
})

// 过滤后的列表
const filteredList = computed(() => {
  let list = bookingList.value
  
  if (currentTab.value === 1) { // 进行中
    list = list.filter(item => item.status === 2 || item.status === 3)
  } else if (currentTab.value === 2) { // 已完成
    list = list.filter(item => item.status === 4)
  }
  
  // 排除 nextMatch
  if (nextMatch.value && (currentTab.value === 0 || currentTab.value === 1)) {
    list = list.filter(item => item.id !== nextMatch.value.id)
  }
  
  return list
})

const loadBookingList = async () => {
  if (loading.value) return
  loading.value = true
  try {
    if (!userStore.userId) return
    const params: any = {
      memberId: userStore.userId,
      page: 1,
      size: 50
    }
    const result = await getBookingList(params)
    bookingList.value = result.data.map((booking: any) => ({
      id: booking.id,
      venueName: booking.venueName || booking.courtName,
      courtName: booking.courtName,
      date: booking.bookingDate,
      startTime: booking.startTime,
      endTime: booking.endTime,
      amount: booking.orderAmount,
      status: booking.status,
      bookingNo: booking.bookingNo || `BK${new Date(booking.createTime).getTime()}`
    }))
  } catch (error) {
    console.error('加载预约列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  refreshing.value = true
  loadBookingList()
}

const switchTab = (index: number) => {
  currentTab.value = index
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '待支付',
    2: '已支付',
    3: '进行中',
    4: '已完成'
  }
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'cancelled',
    1: 'pending',
    2: 'paid',
    3: 'ongoing',
    4: 'completed'
  }
  return classMap[status] || ''
}

const formatDateShort = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return dateStr
  const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
  return `${monthNames[date.getMonth()]} ${date.getDate().toString().padStart(2, '0')}`
}

const handleBookingClick = (item: any) => {
  uni.navigateTo({ url: `/pages/booking/detail?id=${item.id}` })
}

const handleAction = (item: any) => {
  if (item.status === 4) {
    uni.navigateTo({ url: `/pages/venue/detail?id=${item.venueId || ''}` })
  } else {
    handleBookingClick(item)
  }
}

const handleCheckIn = (item: any) => {
  uni.showToast({ title: '签到成功', icon: 'success' })
}

const handleShare = (item: any) => {
  uni.showToast({ title: '分享功能', icon: 'none' })
}

const handleNotifications = () => {
  uni.navigateTo({ url: '/pages/notice/index' })
}

const handleProfile = () => {
  uni.navigateTo({ url: '/pages/profile/index' })
}

onMounted(() => {
  const system = uni.getSystemInfoSync()
  statusBarHeight.value = system.statusBarHeight || 20
  if (userStore.isLoggedIn) {
    loadBookingList()
  } else {
    uni.redirectTo({ url: '/pages/login/login' })
  }
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background-color: #f9f9f9;
  color: #1a1a1a;
}

.top-bar {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  z-index: 10;
  background: #f3f3f3;
}

.top-inner {
  height: 54px;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.brand-text {
  font-size: 30rpx;
  color: #b14a00;
  font-weight: 700;
}

.actions {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.icon-btn {
  width: 58rpx;
  height: 58rpx;
  border-radius: 29rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 58rpx;
  height: 58rpx;
  border-radius: 29rpx;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.content {
  height: 100vh;
}

.wrap {
  padding: 28rpx 24rpx 220rpx;
}

.title-row {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  margin-bottom: 24rpx;
}

.sub-title {
  display: block;
  font-size: 18rpx;
  color: #b14a00;
  letter-spacing: 2rpx;
}

.title {
  font-size: 52rpx;
  font-weight: 800;
}

.tabs {
  background: #ececec;
  border-radius: 18rpx;
  display: inline-flex;
  padding: 6rpx;
}

.tab {
  padding: 14rpx 28rpx;
  border-radius: 14rpx;
  color: #666;
  font-size: 24rpx;
}

.tab.active {
  background: #ff6a00;
  color: #fff;
  font-weight: 700;
}

.hero-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.hero-badge {
  display: inline-block;
  font-size: 18rpx;
  color: #fff;
  background: #8f3400;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
}

.hero-title {
  display: block;
  margin-top: 14rpx;
  font-size: 44rpx;
  font-weight: 800;
}

.hero-sub {
  display: block;
  margin-top: 8rpx;
  color: #666;
  font-size: 24rpx;
}

.hero-grid {
  margin-top: 18rpx;
  display: flex;
  gap: 12rpx;
}

.hero-cell {
  flex: 1;
  background: #f3f3f3;
  border-radius: 14rpx;
  padding: 16rpx;
}

.cell-k {
  color: #8a8a8a;
  font-size: 18rpx;
}

.cell-v {
  display: block;
  margin-top: 8rpx;
  font-weight: 700;
}

.hero-actions {
  margin-top: 18rpx;
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.btn-checkin {
  flex: 1;
  border-radius: 14rpx;
  background: #ff6a00;
  color: #fff;
  font-weight: 700;
}

.btn-share {
  width: 84rpx;
  height: 84rpx;
  border-radius: 14rpx;
  background: #efefef;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stats {
  border-radius: 24rpx;
  background: #171717;
  color: #fff;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.stats-k {
  color: #8d8d8d;
  font-size: 20rpx;
}

.stats-row {
  margin-top: 16rpx;
  display: flex;
  align-items: flex-end;
  gap: 10rpx;
}

.stats-v {
  font-size: 58rpx;
  font-weight: 800;
  color: #ff8d3a;
}

.stats-v.money {
  color: #fff;
}

.stats-t {
  color: #979797;
  margin-bottom: 8rpx;
  font-size: 22rpx;
}

.divider {
  margin-top: 14rpx;
  height: 1px;
  background: #2e2e2e;
}

.vip {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #ff8d3a;
  font-size: 22rpx;
}

.card {
  background: #fff;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.card.cancelled {
  opacity: 0.75;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.booking-no {
  display: block;
  font-size: 18rpx;
  color: #8b8b8b;
}

.status {
  display: inline-block;
  margin-top: 8rpx;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 18rpx;
}

.status.paid,
.status.ongoing {
  background: #d8e9ff;
  color: #0e406a;
}
.status.completed {
  background: #ececec;
  color: #666;
}
.status.cancelled {
  background: #ffe1dc;
  color: #9a1e0f;
}
.status.pending {
  background: #ffe3d4;
  color: #963800;
}

.amount {
  font-size: 44rpx;
  font-weight: 800;
  color: #ff6a00;
}

.amount.strike {
  color: #bbb;
  text-decoration: line-through;
}

.venue {
  display: block;
  margin-top: 16rpx;
  font-size: 40rpx;
  font-weight: 700;
}

.venue.muted {
  color: #888;
}

.line {
  margin-top: 12rpx;
  display: flex;
  gap: 10rpx;
  align-items: center;
  color: #5f5f5f;
  font-size: 24rpx;
}

.refund {
  margin-top: 16rpx;
  background: #f2f2f2;
  border-radius: 12rpx;
  text-align: center;
  padding: 16rpx;
  color: #8a8a8a;
}

.card-btn {
  margin-top: 16rpx;
  border-radius: 12rpx;
  background: #ececec;
  color: #222;
}

.card-btn.rebook {
  background: #fff;
  border: 1px solid #e3bfb1;
  color: #666;
}

.empty {
  margin-top: 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #b6b6b6;
  gap: 10rpx;
}

</style>
