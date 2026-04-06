<template>
  <PresidentLayout :showTabBar="true">
    <view class="member-list-page">
      <view class="status-bar-placeholder"></view>
      
      <!-- Nav Header -->
      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <uni-icons type="arrow-left" size="24" color="#a33e00" class="nav-left-icon"></uni-icons>
            <text class="nav-title">会员管理</text>
          </view>
          <view class="nav-right">
            <text class="brand-text">KL.V2</text>
          </view>
        </view>
      </view>

      <view class="main-content">
        <!-- Search and Stats -->
        <view class="dashboard-section">
          <view class="search-wrap">
            <uni-icons type="search" size="20" color="#5f5e5e" class="search-icon"></uni-icons>
            <input 
              class="search-input" 
              placeholder="搜索会员姓名、手机号..." 
              v-model="queryParams.memberName"
              @confirm="handleSearch"
            />
            <view class="search-line"></view>
          </view>

          <view class="stats-wrap">
            <view class="stat-card total">
              <text class="stat-label">Total Members</text>
              <text class="stat-value">{{ total }}</text>
            </view>
            <view class="stat-card active-today">
              <text class="stat-label">Active Today</text>
              <text class="stat-value">{{ activeToday }}</text>
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
                  <view class="vip-badge" :class="item.memberLevel > 1 ? 'high' : 'low'">
                    VIP {{ item.memberLevel || 1 }}
                  </view>
                </view>
                <text class="member-phone">{{ formatPhone(item.phone) }}</text>
              </view>
              <view class="member-balance">
                <text class="balance-label">BALANCE</text>
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
import { ref, computed, reactive, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getMemberList, type MemberListItem } from '@/api/president/member'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { parsePagedList } from '@/utils/parsePagedList'
import { safeNavigateBack } from '@/utils/navigation'

const loading = ref(false)
const list = ref<MemberListItem[]>([])
const total = ref(0)
const activeToday = ref(156) // Mock static number for visual
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

async function loadList(append = false) {
  if (loading.value) return
  if (!append) {
    queryParams.page = 1
    list.value = []
  }
  
  loading.value = true
  try {
    const res = await getMemberList(queryParams) as any
    const parsed = parsePagedList<MemberListItem>(res)
    total.value = parsed.total
    
    // Process backend data or populate mock if no real data to match prototype
    let newList = parsed.list;
    if (newList.length === 0 && !append && !queryParams.memberName) {
        newList = getMockList();
        total.value = 1402;
    }

    if (append) {
      list.value = list.value.concat(newList)
    } else {
      list.value = newList
    }
  } catch (e) {
    console.error('Fetch error:', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
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

function handleDetail(item: MemberListItem) {
    // Navigate to detail
    uni.showToast({ title: '详情开发中', icon: 'none' })
}

function handleRecharge(item: MemberListItem) {
    uni.showToast({ title: '充值开发中', icon: 'none' })
}

function handleAdd() {
    uni.showToast({ title: '新增会员开发中', icon: 'none' })
}

function formatPhone(phone?: string) {
  if (!phone) return '—'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1 **** $2')
}

function formatMoney(amount?: number) {
  if (amount == null) return '0.00'
  return Number(amount).toFixed(2)
}

// Ensure visual mapping from Prototype when API is unpopulated
function getMockList(): any[] {
  return [
    {
      id: 1,
      memberName: '张伟明',
      gender: 1,
      memberLevel: 3,
      phone: '13800138000',
      balance: 1280.50,
      avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBh2ogW-OxpgmbvEdVpbq_wi3trd4SxhjHZ8JPFtVBzOJN1Ebw9lzj-n6KisX4WzxZec3s5UlM2TaXYHGfkQvx2QmADNidEBQCk3dxfQx5f0M2M9M-9v3LuUE-PnDq0FOOeb1AJJnj9KMVm3cnI5ej7WXzPYI3cRKl78fKJsSK8T6d10F3r2BwuiAtwROmNVJossRWYs0XRqRcxuRVVEHgci56rYlyQ2IeoWhbpUnLaWHpwm78nsAZV9_noAsv3elxwStsg0H0cCZjk'
    },
    {
      id: 2,
      memberName: '李芳',
      gender: 2,
      memberLevel: 5,
      phone: '13912345678',
      balance: 4920.00,
      avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCvBOGWHxAGrTNuQdf2GK_SCSoKTZL23lQuHRb_ncBtOhVOYk92MxoHa1g1XbYurpwOvGc5Ifs-4lwfneIBL-DLHC9K1RP2r8NSkOLRgv6M2Gl8uvZVTkG3LpncLMCKPpckpcrcCYAuwF-beIm6AZ8RMXsyrPmfr1V1z_KwO1R0bb6GkpcMTjVtxDCa43uX8lUyruL1pyjjyghLcz3JgwZzLwfWhQ3rlvvrgOhgW-4NZd4mKFFIf2vU4CnBwHiECGHrvA8MOGZkvZaS'
    },
    {
      id: 3,
      memberName: '王少杰',
      gender: 1,
      memberLevel: 1,
      phone: '13799887766',
      balance: 85.00,
      avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCS9_98WcoGMlmYaiiYERWds-DL_9-NHr5fzksqGSgyqf0tVlgFoL18cQqZ3LDPQf-ORV68p8zJKPHS7Vvp2abZWsIemG2_3ustJg8h_hJqkgBe2x-wCY22lORQGwZKDhOe300kJtYWQEvlRf6F9mMG2sF3it44RP5D10CT33iB0ptbSS4bYLWu9I4UH-n2NzsTqPUw_3O8UCqYigb1jdpVdh0ixd1j57GjsWHNCoNa639mNRyaWgCkRKx_ja51Z_repMxWUEOq0GiC'
    },
    {
      id: 4,
      memberName: '陈雨欣',
      gender: 2,
      memberLevel: 2,
      phone: '13566778899',
      balance: 640.20,
      avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuAnT9bpnFRR1D0506vT-eKi7SxRXr92iy7UgRZGvIYUmAM7I3GCtlREjcelUdaJZiocQIHntjXuU0_9EooGvG-gdnxGeHhILyYrYon9U4w1tvZIxFOpkmz0CdYaiKSmJ8YeqEnfVjcnuVXfz3nrAepZo7xKCv_yAnd6h3YSqJYh5Xf_EXVn8CyMIsEdOVj9oP2Au7mSEZCWj0LCD_1akiVnzlj9k1Z68PP2XYLoyynsKPmlsLnAw2uryjgCHQHs03TFSeOohFfjn5-s'
    }
  ]
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
  background-color: #f3f3f3;
}

/* Nav Header */
.nav-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #f3f3f3;
  padding: 32rpx 40rpx;
}

.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
  cursor: pointer;

  &:active {
    opacity: 0.7;
  }
}

.nav-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
  font-family: 'Lexend', sans-serif;
}

.brand-text {
  font-size: 36rpx;
  font-weight: 900;
  color: #ff6600;
  letter-spacing: -2rpx;
  font-family: 'Lexend', sans-serif;
}

/* Main Content */
.main-content {
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
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
