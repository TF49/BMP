<template>
  <PresidentLayout :showTabBar="true">
    <view class="tournament-page">
      <view class="status-bar-placeholder"></view>

      <!-- TopAppBar -->
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
            <view class="icon-btn" @click="toggleSearch">
              <uni-icons type="search" size="22" color="#71717a"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="main-content">
        <!-- Search Bar -->
        <view v-if="showSearch" class="search-bar">
          <view class="search-input-wrap">
            <uni-icons type="search" size="20" color="#71717a"></uni-icons>
            <input 
              class="search-input" 
              v-model="searchKeyword" 
              placeholder="搜索赛事名称..."
              @confirm="handleSearchConfirm"
              @input="handleSearchInput"
            />
            <view v-if="searchKeyword" class="clear-btn" @click="clearSearch">
              <uni-icons type="closeempty" size="16" color="#71717a"></uni-icons>
            </view>
          </view>
          <text class="cancel-btn" @click="toggleSearch">取消</text>
        </view>

        <!-- Hero Section -->
        <view class="hero-section">
          <view class="hero-text">
            <text class="hero-title">赛事管理</text>
            <text class="hero-subtitle">赛事控制中心</text>
          </view>
        </view>

        <!-- Bento Stats Grid -->
        <view class="stats-grid">
          <!-- Active -->
          <view class="stat-card primary-border">
            <view class="stat-card-top">
              <text class="stat-icon-text">进行中</text>
              <view class="stat-badge active-badge" v-if="stats.ongoing !== '00'">
                <text class="stat-badge-text">活跃</text>
              </view>
            </view>
            <text class="stat-number">{{ stats.ongoing }}</text>
            <text class="stat-label">进行中的赛事</text>
          </view>

          <!-- Total Participants -->
          <view class="stat-card tertiary-border">
            <view class="stat-card-top">
              <text class="stat-icon-text tertiary-icon">参赛</text>
            </view>
            <text class="stat-number">{{ stats.totalParticipants }}</text>
            <text class="stat-label">总参赛人数</text>
          </view>

          <!-- Completed -->
          <view class="stat-card secondary-border">
            <view class="stat-card-top">
              <text class="stat-icon-text secondary-icon">完赛</text>
            </view>
            <text class="stat-number">{{ stats.completed }}</text>
            <text class="stat-label">本年度已完成</text>
            <text class="deco-icon material-icon">workspace_premium</text>
          </view>
        </view>

        <!-- Filter Tabs -->
        <view class="filter-tabs">
          <view
            class="tab-item"
            :class="{ active: currentTab === 0 }"
            @click="currentTab = 0"
          >全部赛事</view>
          <view
            class="tab-item"
            :class="{ active: currentTab === 1 }"
            @click="currentTab = 1"
          >报名中</view>
          <view
            class="tab-item"
            :class="{ active: currentTab === 2 }"
            @click="currentTab = 2"
          >进行中</view>
          <view
            class="tab-item"
            :class="{ active: currentTab === 3 }"
            @click="currentTab = 3"
          >已结束</view>
        </view>

        <!-- Tournament Cards List -->
        <view v-if="loading" class="loading-state">
          <text class="loading-text">加载中...</text>
        </view>
        
        <view v-else-if="filteredTournaments.length === 0" class="empty-state">
          <text class="empty-text">暂无赛事</text>
        </view>
        
        <view v-else class="cards-list">
          <view
            v-for="(t, index) in filteredTournaments"
            :key="t.id"
            class="tournament-card"
            :class="{ ended: t.status === 'ended' }"
            @click="handleCardClick(t)"
          >
            <view class="card-image-wrap">
              <image class="card-image" :src="t.image" mode="aspectFill"></image>
            </view>
            <view class="card-body">
              <view class="card-header-row">
                <view class="status-badge" :class="t.status">
                  <text class="status-text">{{ statusLabel(t.status) }}</text>
                </view>
                <text class="category-text">{{ t.category }}</text>
              </view>

              <text class="card-title">{{ t.name }}</text>
              <view class="location-row">
                <uni-icons type="location" size="14" color="#5f5e5e"></uni-icons>
                <text class="location-text">{{ t.location }}</text>
              </view>

              <!-- Status-specific content -->
              <!-- Registration open -->
              <view v-if="t.status === 'registration'" class="card-detail">
                <view class="progress-bar-wrap">
                  <view class="progress-bar-bg">
                    <view class="progress-bar-fill" :style="{ width: ((t.registered ?? 0) / (t.capacity ?? 1) * 100) + '%' }"></view>
                  </view>
                </view>
                <view class="card-stats-row">
                  <view>
                    <text class="stats-label">报名人数</text>
                    <text class="stats-value-primary">{{ t.registered }} <text class="stats-value-muted">/ {{ t.capacity }}</text></text>
                  </view>
                  <view class="right-align">
                    <text class="stats-label">截止日期</text>
                    <text class="stats-value">{{ t.deadline }}</text>
                  </view>
                </view>
              </view>

              <!-- Ongoing -->
              <view v-else-if="t.status === 'ongoing'" class="card-detail">
                <view class="ongoing-status-row">
                  <view class="ongoing-left">
                    <view class="pulse-dot"></view>
                    <text class="ongoing-round-text">当前场次: {{ t.currentRound }}</text>
                  </view>
                  <uni-icons type="arrowright" size="14" color="#5f5e5e"></uni-icons>
                </view>
                <view class="card-stats-row">
                  <view>
                    <text class="stats-label">总参赛数</text>
                    <text class="stats-value">{{ t.totalPlayers }}</text>
                  </view>
                  <view class="right-align">
                    <text class="stats-label">预计结束</text>
                    <text class="stats-value">{{ t.endTime }}</text>
                  </view>
                </view>
              </view>

              <!-- Ended -->
              <view v-else-if="t.status === 'ended'" class="card-detail">
                <view class="ended-row">
                  <view class="champion-avatars">
                    <view class="champion-item">
                      <text class="stats-label ended-label">冠军</text>
                      <text class="champion-name">{{ t.champion }}</text>
                    </view>
                  </view>
                </view>
              </view>

              <!-- Draft/Upcoming -->
              <view v-else-if="t.status === 'draft'" class="card-detail">
                <view class="draft-row">
                  <view class="edit-link" @click.stop="handleEdit(t)">
                    <text class="edit-link-text">继续编辑资料</text>
                    <uni-icons type="compose" size="14" color="#a33e00"></uni-icons>
                  </view>
                  <view class="right-align">
                    <text class="stats-label">计划时间</text>
                    <text class="stats-value">{{ t.plannedDate }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- FAB -->
      <view class="fab-btn" @click="handleCreate">
        <uni-icons type="plusempty" size="30" color="#ffffff"></uni-icons>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getTournamentList, type TournamentItem } from '@/api/tournament'
import { searchTournaments } from '@/api/search'
import { getTournamentStatusMeta } from '@/utils/presidentStatus'
import { parsePagedList } from '@/utils/parsePagedList'

const currentTab = ref(0)
const loading = ref(false)
const showSearch = ref(false)
const searchKeyword = ref('')
let searchTimer: number | undefined

type TournamentStatus = 'registration' | 'ongoing' | 'ended' | 'draft' | 'cancelled'

interface Tournament {
  id: number
  name: string
  status: TournamentStatus
  category: string
  location: string
  image: string
  registered?: number
  capacity?: number
  deadline?: string
  currentRound?: string
  totalPlayers?: number
  endTime?: string
  champion?: string
  plannedDate?: string
}

const tournaments = ref<Tournament[]>([])

// 日期格式化工具函数
function formatDate(dateStr?: string): string {
  if (!dateStr) return '待定'
  try {
    const date = new Date(dateStr)
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}月${day}日`
  } catch {
    return dateStr
  }
}

// 状态映射函数
function mapTournamentStatus(status?: number): TournamentStatus {
  const meta = getTournamentStatusMeta(status)
  return meta.key
}

// 数据转换函数
function transformTournament(item: TournamentItem): Tournament {
  const status = mapTournamentStatus(item.status)
  
  return {
    id: item.id,
    name: item.tournamentName || '未命名赛事',
    status,
    category: item.tournamentType || '未分类',
    location: item.venueName || '待定',
    image: '/static/placeholders/hero.svg',
    
    // 报名中状态字段
    registered: item.currentParticipants || 0,
    capacity: item.maxParticipants || 0,
    deadline: formatDate(item.registrationEnd),
    
    // 进行中状态字段
    currentRound: '进行中',
    totalPlayers: item.currentParticipants || 0,
    endTime: formatDate(item.tournamentEnd),
    
    // 已结束状态字段
    champion: '待公布',
    
    // 筹备中状态字段
    plannedDate: formatDate(item.tournamentStart)
  }
}

// 加载赛事列表
async function loadTournaments() {
  loading.value = true
  try {
    // 如果有搜索关键词，使用搜索接口
    if (searchKeyword.value.trim()) {
      const res = await searchTournaments({
        keyword: searchKeyword.value.trim(),
        page: 1,
        size: 100
      })
      const list = res.data || []
      tournaments.value = list.map(transformTournament)
    } else {
      // 否则使用列表接口
      const res = await getTournamentList({
        page: 1,
        size: 100
      })
      const { list } = parsePagedList<TournamentItem>(res)
      tournaments.value = list.map(transformTournament)
    }
  } catch (error) {
    console.error('加载赛事列表失败:', error)
    uni.showToast({ 
      title: '加载失败，请重试', 
      icon: 'none' 
    })
  } finally {
    loading.value = false
  }
}

// 统计数据
const stats = computed(() => {
  const ongoing = tournaments.value.filter(t => t.status === 'ongoing').length
  const totalParticipants = tournaments.value.reduce((sum, t) => sum + (t.registered || 0), 0)
  const completed = tournaments.value.filter(t => t.status === 'ended').length
  
  return {
    ongoing: String(ongoing).padStart(2, '0'),
    totalParticipants: totalParticipants.toLocaleString(),
    completed: String(completed)
  }
})

// 筛选后的赛事列表
const filteredTournaments = computed(() => {
  if (currentTab.value === 0) return tournaments.value
  const map: Record<number, TournamentStatus> = {
    1: 'registration',
    2: 'ongoing',
    3: 'ended'
  }
  const target = map[currentTab.value]
  if (!target) return tournaments.value
  return tournaments.value.filter(t => t.status === target)
})

function statusLabel(status: TournamentStatus) {
  const map: Record<TournamentStatus, string> = {
    registration: '报名中',
    ongoing: '进行中',
    ended: '已结束',
    draft: '筹备中',
    cancelled: '已取消'
  }
  return map[status] || status
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function toggleSearch() {
  showSearch.value = !showSearch.value
  if (!showSearch.value) {
    searchKeyword.value = ''
    loadTournaments()
  }
}

function clearSearch() {
  searchKeyword.value = ''
  loadTournaments()
}

function handleSearchConfirm() {
  loadTournaments()
}

function handleSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadTournaments()
  }, 500) as unknown as number
}

function handleCreate() {
  uni.navigateTo({ url: PRESIDENT_PAGES.TOURNAMENT_FORM })
}

function handleCardClick(t: Tournament) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.TOURNAMENT_DETAIL}?tournamentId=${t.id}` })
}

function handleEdit(t: Tournament) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.TOURNAMENT_FORM}?id=${t.id}` })
}

// 页面加载时获取数据
onMounted(() => {
  loadTournaments()
})
</script>

<style lang="scss" scoped>
.tournament-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding-bottom: 240rpx;
  font-family: "Lexend", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f8fafc;
}

/* ── Search Bar ── */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 48rpx;
  background-color: #ffffff;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  background-color: #f3f3f3;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1a1c1c;
}

.clear-btn {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e5e5;
  border-radius: 50%;
}

.cancel-btn {
  font-size: 28rpx;
  font-weight: 600;
  color: #ea580c;
  white-space: nowrap;
}

/* ── Navigation ── */
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

  .back-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

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

/* ── Main Content ── */
.main-content {
  padding: 48rpx 48rpx 48rpx;
  max-width: 1200rpx;
  margin: 0 auto;
}

/* ── Hero ── */
.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 64rpx;
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

/* ── Stats Grid ── */
.stats-grid {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  margin-bottom: 64rpx;
}

.stat-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx;
  border-left: 8rpx solid transparent;
  position: relative;
  overflow: hidden;

  &.primary-border {
    border-left-color: #a33e00;
  }
  &.tertiary-border {
    border-left-color: #0062a1;
  }
  &.secondary-border {
    border-left-color: #5f5e5e;
  }
}

.stat-card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32rpx;
}

.material-icon {
  font-family: 'Material Symbols Outlined';
  font-size: 48rpx;
  color: #a33e00;
  font-style: normal;
  line-height: 1;

  &.tertiary-icon {
    color: #0062a1;
  }
  &.secondary-icon {
    color: #5f5e5e;
  }
}

.stat-badge {
  &.active-badge {
    background-color: #ffdbcd;
    padding: 6rpx 16rpx;
    border-radius: 8rpx;

    .stat-badge-text {
      font-size: 20rpx;
      font-weight: 700;
      color: #a33e00;
      text-transform: uppercase;
      letter-spacing: 0.08em;
    }
  }
}

.stat-number {
  display: block;
  font-size: 72rpx;
  font-weight: 800;
  color: #18181b;
  letter-spacing: -0.04em;
  line-height: 1.1;
  margin-bottom: 8rpx;
}

.stat-label {
  display: block;
  font-size: 22rpx;
  font-weight: 600;
  color: #71717a;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.deco-icon {
  position: absolute;
  right: -30rpx;
  bottom: -30rpx;
  font-size: 200rpx !important;
  color: #1a1c1c;
  opacity: 0.04;
}

/* ── Filter Tabs ── */
.filter-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 48rpx;
  border-bottom: 2rpx solid #eeeeee;
  overflow-x: auto;

  &::-webkit-scrollbar {
    display: none;
  }
}

.tab-item {
  padding: 0 32rpx 32rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #71717a;
  white-space: nowrap;
  border-bottom: 4rpx solid transparent;
  margin-bottom: -2rpx;
  transition: all 0.2s ease;

  &.active {
    color: #ea580c;
    border-bottom-color: #ea580c;
  }

  &:active {
    color: #ea580c;
  }
}

/* ── Cards ── */
.loading-state,
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 120rpx 48rpx;
}

.loading-text,
.empty-text {
  font-size: 28rpx;
  color: #71717a;
  font-weight: 500;
}

.cards-list {
  display: flex;
  flex-direction: column;
  gap: 48rpx;
}

.tournament-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:active {
    transform: scale(0.99);
  }

  &.ended {
    opacity: 0.82;
  }
}

.card-image-wrap {
  width: 100%;
  height: 320rpx;
  overflow: hidden;

  .card-image {
    width: 100%;
    height: 100%;
    transition: transform 0.5s ease;
  }
}

.card-body {
  padding: 40rpx 40rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.status-badge {
  padding: 6rpx 20rpx;
  border-radius: 8rpx;

  .status-text {
    font-size: 20rpx;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }

  &.registration {
    background-color: #ffdbcd;
    .status-text { color: #a33e00; }
  }
  &.ongoing {
    background-color: #009cfc;
    .status-text { color: #ffffff; }
  }
  &.ended {
    background-color: #e5e5e5;
    .status-text { color: #6b6b6b; }
  }
  &.draft {
    background-color: #fff7ed;
    .status-text { color: #9a3412; }
  }
}

.category-text {
  font-size: 22rpx;
  font-weight: 700;
  color: #5f5e5e;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.card-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #18181b;
  letter-spacing: -0.02em;
}

.location-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 8rpx;

  .location-text {
    font-size: 26rpx;
    color: #5f5e5e;
  }
}

/* ── Card Detail Sections ── */
.card-detail {
  margin-top: 16rpx;
}

.progress-bar-wrap {
  margin-bottom: 24rpx;
}

.progress-bar-bg {
  width: 100%;
  height: 8rpx;
  background-color: #eeeeee;
  border-radius: 99rpx;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background-color: #a33e00;
  border-radius: 99rpx;
}

.card-stats-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.stats-label {
  display: block;
  font-size: 20rpx;
  font-weight: 700;
  color: #71717a;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  margin-bottom: 4rpx;
}

.stats-value {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #18181b;
}

.stats-value-primary {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #18181b;
}

.stats-value-muted {
  font-size: 28rpx;
  font-weight: 400;
  color: #a1a1aa;
}

.right-align {
  text-align: right;
}

/* Ongoing */
.ongoing-status-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #eee;
  padding: 24rpx 28rpx;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
}

.ongoing-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.pulse-dot {
  width: 16rpx;
  height: 16rpx;
  background-color: #22c55e;
  border-radius: 50%;
  animation: pulse-anim 1.5s infinite;
}

@keyframes pulse-anim {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.3); }
}

.ongoing-round-text {
  font-size: 24rpx;
  font-weight: 700;
  color: #18181b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* Ended */
.ended-row {
  display: flex;
  align-items: center;
}

.ended-label {
  display: block;
}

.champion-name {
  font-size: 30rpx;
  font-weight: 700;
  color: #a33e00;
}

/* Draft */
.draft-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.edit-link {
  display: flex;
  align-items: center;
  gap: 8rpx;

  &:active {
    opacity: 0.7;
  }

  .edit-link-text {
    font-size: 28rpx;
    font-weight: 700;
    color: #a33e00;
  }
}

/* ── FAB ── */
.fab-btn {
  position: fixed;
  bottom: 240rpx;
  right: 48rpx;
  width: 120rpx;
  height: 120rpx;
  background-color: #ea580c;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(234, 88, 12, 0.4);
  z-index: 1001;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.9);
  }
}
</style>
