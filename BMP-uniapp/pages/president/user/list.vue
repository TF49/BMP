<template>
  <PresidentLayout :showTabBar="true">
    <view class="user-list-page">
      <view class="status-bar-placeholder"></view>

      <!-- TopAppBar：与会员/赛事管理保持一致 -->
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
        <!-- Hero Section -->
        <view class="hero-section">
          <view class="hero-text">
            <text class="hero-title">人员管理</text>
            <text class="hero-subtitle">系统用户中心</text>
          </view>
        </view>

        <!-- Search + Stats -->
        <view class="dashboard-section">
          <!-- 搜索栏 -->
          <view class="search-wrap">
            <uni-icons type="search" size="20" color="#5f5e5e" class="search-icon"></uni-icons>
            <input
              id="user-search-input"
              class="search-input"
              placeholder="按姓名查找员工..."
              v-model="searchForm.username"
              :focus="searchInputFocused"
              @blur="onSearchInputBlur"
              @confirm="onSearch"
            />
            <view class="search-line"></view>
          </view>

          <!-- 角色筛选 + 总数 -->
          <view class="stats-wrap">
            <view class="stat-card total">
              <text class="stat-label">系统账号数</text>
              <text class="stat-value">{{ total }}</text>
            </view>
            <view class="stat-card active-today">
              <picker
                mode="selector"
                :range="roleOptions"
                range-key="label"
                :value="roleIndex"
                @change="onRoleChange"
              >
                <view class="role-picker">
                  <text class="role-picker-label">角色筛选</text>
                  <text class="role-picker-value">
                    {{ roleOptions[roleIndex]?.label || '全部' }}
                  </text>
                </view>
              </picker>
            </view>
          </view>
        </view>

        <!-- Loading / Empty -->
        <view v-if="loading && list.length === 0" class="state-wrap">
          <view class="spinner"></view>
          <text>正在加载用户...</text>
        </view>
        <view v-else-if="list.length === 0" class="state-wrap">
          <text>暂无用户数据</text>
        </view>

        <!-- User Cards -->
        <view v-else class="user-grid">
          <view
            v-for="(item, index) in list"
            :key="item.id || index"
            class="user-card"
            @click="goDetail(item.id)"
          >
            <view class="card-top">
              <image
                class="user-avatar"
                :src="getAvatarImage(undefined)"
                mode="aspectFill"
              />
              <view class="user-info">
                <view class="info-row">
                  <text class="user-name">{{ item.username || '未命名用户' }}</text>
                  <view class="role-chip" :class="item.role">
                    {{ roleLabel(item.role) }}
                  </view>
                  <view
                    class="status-chip"
                    :class="item.status === 1 ? 'active' : 'disabled'"
                  >
                    {{ item.status === 1 ? 'ACTIVE' : 'DISABLED' }}
                  </view>
                </view>
                <text class="user-meta">
                  {{ venueLabel(item.venueId) }}
                </text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="hasMore && list.length > 0" class="load-more" @click="loadMore">
          <text>{{ loading ? '加载中...' : '加载更多' }}</text>
        </view>
      </view>

      <!-- FAB：新增用户 -->
      <view class="fab-btn" @click="goAdd">
        <uni-icons type="personadd" size="28" color="#ffffff" />
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getUserList, type UserListItem } from '@/api/president/user'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { parsePagedList } from '@/utils/parsePagedList'
import { safeNavigateBack } from '@/utils/navigation'
import { getAvatarImage } from '@/utils/displayImage'

const loading = ref(false)
const list = ref<UserListItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const searchForm = ref({ username: '', role: '' as string })
const roleOptions = [
  { label: '全部', value: '' },
  { label: '协会会长', value: 'PRESIDENT' },
  { label: '场馆管理者', value: 'VENUE_MANAGER' },
  { label: '普通用户', value: 'USER' }
]
const roleIndex = ref(0)

const hasMore = computed(() => list.value.length < total.value)

// 顶栏「搜索」与下方 input 联动聚焦
const searchInputFocused = ref(false)

function onSearchInputBlur() {
  searchInputFocused.value = false
}

function roleLabel(role: string) {
  const r = roleOptions.find(o => o.value === role)
  return r ? r.label : role
}

function venueLabel(venueId?: number) {
  if (!venueId) return '所属场馆未设置'
  // 先用占位文案，后续可接入场馆名称映射
  return `关联场馆 ID #${venueId}`
}

function onRoleChange(e: any) {
  const i = Number(e.detail?.value ?? 0)
  roleIndex.value = i
  searchForm.value.role = roleOptions[i]?.value ?? ''
}

async function loadList(append = false) {
  if (loading.value && !append) return
  if (!append) {
    page.value = 1
    list.value = []
  }
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size,
      username: searchForm.value.username || undefined,
      role: searchForm.value.role || undefined
    })
    const parsed = parsePagedList<UserListItem>(res)
    total.value = parsed.total
    if (append) list.value = list.value.concat(parsed.list)
    else list.value = parsed.list
    page.value += 1
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function onSearch() {
  loadList()
}

function loadMore() {
  if (hasMore.value && !loading.value) loadList(true)
}

function goDetail(id: number) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.USER_DETAIL}?id=${id}` })
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.USER_FORM })
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
    selector: '#user-search-input',
    duration: 200,
    complete: focusInput
  })
}

function handleNavSettings() {
  uni.showToast({ title: '设置功能开发中', icon: 'none' })
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.user-list-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding-bottom: 140rpx;
  color: #1a1c1c;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f8fafc;
}

/* 顶部导航，与会员/赛事管理对齐 */
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

.main-content {
  padding: 32rpx 48rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* Hero */
.hero-section {
  margin-bottom: 8rpx;
}

.hero-text {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.hero-title {
  font-size: 40rpx;
  font-weight: 900;
  letter-spacing: -0.05em;
  color: #1a1c1c;
}

.hero-subtitle {
  font-size: 22rpx;
  font-weight: 600;
  color: #71717a;
  letter-spacing: 0.15em;
  margin-top: 8rpx;
  text-transform: uppercase;
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
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);
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
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);
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

.role-picker {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.role-picker-label {
  font-size: 20rpx;
  color: #5f5e5e;
  font-weight: 600;
}

.role-picker-value {
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
}

/* 状态提示 */
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
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* User Grid */
.user-grid {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

.user-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.03);
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

.user-avatar {
  width: 110rpx;
  height: 110rpx;
  border-radius: 24rpx;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.info-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12rpx;
}

.user-name {
  font-size: 34rpx;
  font-weight: 800;
}

.role-chip {
  font-size: 18rpx;
  font-weight: 800;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background-color: #e8e8e8;
  color: #636262;
}

.role-chip.PRESIDENT {
  background-color: #ff6600;
  color: #561d00;
}

.role-chip.VENUE_MANAGER {
  background-color: #009cfc;
  color: #001d35;
}

.role-chip.USER {
  background-color: #e8e8e8;
  color: #636262;
}

.status-chip {
  font-size: 18rpx;
  font-weight: 800;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
}

.status-chip.active {
  background-color: #bbf7d0;
  color: #166534;
}

.status-chip.disabled {
  background-color: #e5e5e5;
  color: #525252;
}

.user-meta {
  display: block;
  margin-top: 10rpx;
  font-size: 26rpx;
  color: #5f5e5e;
  font-weight: 600;
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
  box-shadow: 0 24rpx 40rpx rgba(26, 28, 28, 0.1);
  z-index: 1001;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.9);
  }
}
</style>
