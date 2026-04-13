<template>
  <PresidentLayout :showTabBar="true">
    <view class="member-list-page">
      <view class="status-bar-placeholder"></view>

      <!-- TopAppBar（与赛事管理一致：返回 + 橙色品牌，右侧搜索/设置） -->
      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#ff6600"></uni-icons>
            </view>
            <view class="brand-wrap">
              <text class="brand-name">Kinetic Logic</text>
            </view>
          </view>
          <view class="nav-right">
            <view class="icon-btn" @click.stop="handleNavSearch">
              <uni-icons type="search" size="22" color="#71717a"></uni-icons>
            </view>
            <view class="icon-btn" @click.stop="handleNavSettings">
              <uni-icons type="gear" size="22" color="#71717a"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="main-content">
        <view class="hero-section">
          <view class="hero-text">
            <text class="hero-title">会员管理</text>
            <text class="hero-subtitle">会员信息中心</text>
          </view>
        </view>

        <!-- Search and Stats -->
        <view class="dashboard-section">
          <view class="search-wrap">
            <uni-icons type="search" size="20" color="#5f5e5e" class="search-icon"></uni-icons>
            <input
              id="member-search-input"
              class="search-input"
              placeholder="搜索会员姓名、手机号..."
              v-model="queryParams.memberName"
              :focus="searchInputFocused"
              @blur="onSearchInputBlur"
              @confirm="handleSearch"
            />
            <view class="search-line"></view>
          </view>

          <view class="stats-wrap">
            <view class="stat-card total">
              <text class="stat-label">会员总数</text>
              <text class="stat-value">{{ total }}</text>
            </view>
          </view>
        </view>

        <!-- Filter Tabs -->
        <scroll-view class="filter-tabs" scroll-x :show-scrollbar="false">
          <view 
            v-for="(tab, index) in tabs" 
            :key="index"
            class="tab-btn"
            :class="{ active: currentTab === index }"
            @click="switchTab(index)"
          >
            {{ tab.name }}
          </view>
        </scroll-view>

        <!-- Loading State -->
        <view v-if="loading && list.length === 0" class="state-wrap">
          <view class="spinner"></view>
          <text>正在加载会员...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="reloadList">重新加载</view>
        </view>

        <!-- Empty State -->
        <view v-else-if="list.length === 0" class="state-wrap">
          <text>暂无会员数据</text>
        </view>

        <!-- Member Grid -->
        <view v-else class="member-grid">
          <view v-for="item in list" :key="item.id" class="member-card">
            <view class="card-top">
              <image class="member-avatar" :src="item.avatar || '/static/placeholders/avatar.svg'" mode="aspectFill" />
              <view class="member-info">
                <view class="info-row">
                  <text class="member-name">{{ item.memberName || '未知会员' }}</text>
                  <!-- Gender fallback -->
                  <text class="gender-icon male" v-if="item.gender === 1">♂</text>
                  <text class="gender-icon female" v-else-if="item.gender === 2">♀</text>
                  <view class="vip-badge" :class="(item.memberLevel ?? 1) > 1 ? 'high' : 'low'">
                    会员等级 {{ item.memberLevel ?? 1 }}
                  </view>
                </view>
                <text class="member-phone">{{ formatPhone(item.phone) }}</text>
              </view>
              <view class="member-balance">
                <text class="balance-label">余额</text>
                <text class="balance-value">¥{{ formatMoney(item.balance) }}</text>
              </view>
            </view>
            
            <view class="card-actions">
              <view class="action-btn primary" @click="handleRecharge(item)">充值</view>
              <view class="action-btn secondary" @click="handleDetail(item)">详情</view>
            </view>
          </view>
        </view>

        <view v-if="hasMore && list.length > 0" class="load-more" @click="loadMore">
          <text>{{ loading ? '加载中...' : '加载更多' }}</text>
        </view>
      </view>

      <!-- FAB Button -->
      <view class="fab-btn" @click="handleAdd">
        <uni-icons type="personadd" size="28" color="#ffffff" />
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, reactive, onMounted, nextTick } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getMemberList, type MemberListItem } from '@/api/president/member'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { parsePagedList } from '@/utils/parsePagedList'
import { safeNavigateBack } from '@/utils/navigation'

const loading = ref(false)
const list = ref<MemberListItem[]>([])
const total = ref(0)
const errorText = ref('')
const currentTab = ref(0)

const tabs = [
  { name: '全部', value: undefined },
  { name: '正式会员', value: 1 },
  { name: '普通用户', value: 0 },
  { name: '已冻结', value: 2 }
]

const queryParams = reactive({
  page: 1,
  size: 10,
  memberName: '',
  status: undefined as number | undefined
})

const hasMore = computed(() => list.value.length < total.value)

/** 顶栏「搜索」与下方 input 联动聚焦（小程序需 toggle :focus） */
const searchInputFocused = ref(false)

function onSearchInputBlur() {
  searchInputFocused.value = false
}

async function loadList(append = false) {
  if (loading.value) return
  if (!append) {
    queryParams.page = 1
    list.value = []
    errorText.value = ''
  }
  
  loading.value = true
  try {
    const res = await getMemberList(queryParams)
    const parsed = parsePagedList<MemberListItem>(res)
    total.value = parsed.total
    const newList = parsed.list

    if (append) {
      list.value = list.value.concat(newList)
    } else {
      list.value = newList
    }
  } catch (e) {
    console.error('Fetch error:', e)
    if (!append) {
      errorText.value = e instanceof Error ? e.message : '会员列表加载失败，请稍后重试'
      list.value = []
      total.value = 0
    } else {
      queryParams.page = Math.max(1, queryParams.page - 1)
      uni.showToast({ title: '加载更多失败，请稍后重试', icon: 'none' })
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadList()
}

function reloadList() {
  loadList()
}

function switchTab(index: number) {
  currentTab.value = index
  const nextStatus = tabs[index].value
  queryParams.status = typeof nextStatus === 'number' ? nextStatus : undefined
  loadList()
}

function loadMore() {
  if (hasMore.value) {
    queryParams.page++
    loadList(true)
  }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function handleNavSearch() {
  searchInputFocused.value = false
  const focusInput = () => {
    nextTick(() => {
      searchInputFocused.value = true
    })
  }
  uni.pageScrollTo({
    selector: '#member-search-input',
    duration: 200,
    complete: focusInput
  })
}

function handleNavSettings() {
  uni.navigateTo({ url: '/pages/settings/index' })
}

function handleDetail(item: MemberListItem) {
  if (item.id == null) {
    uni.showToast({ title: '会员信息不完整', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `${PRESIDENT_PAGES.MEMBER_DETAIL}?memberId=${item.id}`
  })
}

function handleRecharge(item: MemberListItem) {
  if (item.id == null) {
    uni.showToast({ title: '会员信息不完整', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `${PRESIDENT_PAGES.MEMBER_RECHARGE}?memberId=${item.id}`
  })
}

function handleAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.MEMBER_FORM })
}

function formatPhone(phone?: string) {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1 **** $2')
}

function formatMoney(amount?: number) {
  if (amount == null) return '0.00'
  return Number(amount).toFixed(2)
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.member-list-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding-bottom: 140rpx;
  color: #1a1c1c;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f8fafc;
}

/* Nav（对齐赛事管理） */
.nav-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(248, 250, 252, 0.85);
  backdrop-filter: blur(12px);
  padding: 24rpx 48rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);
}

.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
  min-width: 0;

  .back-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &:active {
      background-color: #e4e4e7;
    }
  }

  .brand-name {
    font-size: 36rpx;
    font-weight: 800;
    color: #ea580c;
    letter-spacing: -0.04em;
    text-transform: uppercase;
  }
}

.nav-right {
  display: flex;
  gap: 8rpx;
  flex-shrink: 0;

  .icon-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;

    &:active {
      background-color: #e4e4e7;
    }
  }
}

/* Main Content */
.main-content {
  padding: 48rpx 48rpx 40rpx;
  max-width: 1200rpx;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

/* Hero（大标题在导航下方，与赛事管理一致） */
.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 8rpx;
  gap: 32rpx;
  flex-wrap: wrap;
}

.hero-text {
  display: flex;
  flex-direction: column;

  .hero-title {
    font-size: 72rpx;
    font-weight: 900;
    color: #18181b;
    letter-spacing: -0.04em;
    line-height: 1;
  }

  .hero-subtitle {
    font-size: 22rpx;
    font-weight: 600;
    color: #71717a;
    letter-spacing: 0.15em;
    margin-top: 8rpx;
    text-transform: uppercase;
  }
}

/* Dashboard Section */
.dashboard-section {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* Search */
.search-wrap {
  position: relative;
  background-color: #ffffff;
  border-radius: 20rpx;
  height: 110rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  overflow: hidden;
}

.search-icon {
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 30rpx;
  height: 100%;
}

.search-line {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 4rpx;
  background-color: #a33e00;
  transition: width 0.3s ease;
}

.search-wrap:focus-within .search-line {
  width: 100%;
}

/* Stats */
.stats-wrap {
  display: flex;
  gap: 24rpx;
}

.stat-card {
  flex: 1;
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 180rpx;
}

.stat-card.total {
  border-left: 8rpx solid #a33e00;
}

.stat-card.active-today {
  border-left: 8rpx solid #009cfc;
}

.stat-label {
  font-size: 20rpx;
  color: #5f5e5e;
  text-transform: uppercase;
  letter-spacing: 2rpx;
  font-weight: 600;
}

.stat-value {
  font-size: 52rpx;
  font-weight: 800;
  color: #1a1c1c;
}

/* Filter Tabs */
.filter-tabs {
  width: 100%;
  white-space: nowrap;
  padding-bottom: 8rpx;
}

.tab-btn {
  display: inline-block;
  padding: 16rpx 40rpx;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 600;
  margin-right: 20rpx;
  transition: all 0.2s;
  background-color: #e8e8e8;
  color: #5a4136;
}

.tab-btn:active {
  transform: scale(0.95);
}

.tab-btn.active {
  background-color: #ff6600;
  color: #561d00;
}

/* State Wrap (Empty/Loading) */
.state-wrap {
  padding: 100rpx 0;
  text-align: center;
  color: #5f5e5e;
  font-size: 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.retry-btn {
  min-width: 220rpx;
  height: 72rpx;
  padding: 0 32rpx;
  border-radius: 999rpx;
  background: #ff6600;
  color: #fff7ed;
  font-size: 26rpx;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #eee;
  border-top-color: #ff6600;
  border-radius: 50%;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Member Grid */
.member-grid {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.member-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
  transition: transform 0.3s ease;
  
  &:active {
    transform: scale(0.98);
  }
}

.card-top {
  display: flex;
  align-items: flex-start;
  gap: 28rpx;
}

.member-avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.member-info {
  flex: 1;
}

.info-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
}

.member-name {
  font-size: 34rpx;
  font-weight: 800;
}

.gender-icon {
  font-size: 26rpx;
  font-weight: bold;
}

.gender-icon.male {
  color: #3b82f6;
}

.gender-icon.female {
  color: #ec4899;
}

.vip-badge {
  font-size: 18rpx;
  font-weight: 800;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
}

.vip-badge.high {
  background-color: #ff6600;
  color: #561d00;
}

.vip-badge.low {
  background-color: #e8e8e8;
  color: #636262;
}

.member-phone {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  color: #5f5e5e;
  font-weight: 600;
}

.member-balance {
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.balance-label {
  font-size: 20rpx;
  color: #5f5e5e;
  text-transform: uppercase;
  letter-spacing: -0.5rpx;
  font-weight: 600;
}

.balance-value {
  margin-top: 4rpx;
  font-size: 38rpx;
  font-weight: 800;
  color: #a33e00;
}

.card-actions {
  margin-top: 40rpx;
  display: flex;
  gap: 24rpx;
}

.action-btn {
  flex: 1;
  padding: 24rpx 0;
  border-radius: 16rpx;
  text-align: center;
  font-size: 30rpx;
  font-weight: 800;
  transition: all 0.2s;

  &:active {
    transform: scale(0.95);
  }
}

.action-btn.primary {
  background-color: #ff6600;
  color: #561d00;
}

.action-btn.secondary {
  background-color: #e8e8e8;
  color: #1a1c1c;
  
  &:active {
    background-color: #e2e2e2;
  }
}

.load-more {
  padding: 40rpx;
  text-align: center;
  color: #a33e00;
  font-size: 26rpx;
  font-weight: 600;
}

/* FAB */
.fab-btn {
  position: fixed;
  right: 48rpx;
  bottom: 240rpx;
  width: 120rpx;
  height: 120rpx;
  background-color: #ff6600;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 24rpx 40rpx rgba(26,28,28,0.1);
  z-index: 1001;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.9);
  }
}
</style>
