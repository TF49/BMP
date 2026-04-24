<template>
  <MobileLayout className="result-shell">
    <view class="result-page">
      <view class="result-header">
        <view class="top-row">
          <view class="brand-back" @click="handleBack">
            <uni-icons type="left" size="18" color="#a33e00"></uni-icons>
            <text class="brand-text">KINETIC LOGIC</text>
          </view>
          <text class="header-note">RESULTS</text>
        </view>

        <view class="headline-block">
          <text class="headline-eyebrow">SEARCH OUTPUT</text>
          <text class="headline-title">为你筛选 Stitch 化用户端搜索结果</text>
          <text class="headline-subtitle">场馆、课程、赛事、器材统一用一套更产品化的搜索结果结构承接。</text>
        </view>

        <view class="search-bar">
          <uni-icons type="search" size="18" color="#94a3b8"></uni-icons>
          <input
            v-model="searchKeyword"
            class="search-input"
            type="text"
            placeholder="搜索场馆、课程、赛事或器材..."
            confirm-type="search"
            @confirm="handleSearch"
          />
          <view v-if="searchKeyword" class="search-clear" @click="clearSearch">
            <uni-icons type="clear" size="16" color="#64748b"></uni-icons>
          </view>
        </view>

        <view class="summary-strip">
          <view class="summary-card">
            <text class="summary-label">KEYWORD</text>
            <text class="summary-value keyword-value">{{ displayKeyword }}</text>
          </view>
          <view class="summary-card">
            <text class="summary-label">TOTAL</text>
            <text class="summary-value">{{ totalCount }}</text>
          </view>
          <view class="summary-card compact-action" @click="handleSearch">
            <text class="summary-label">ACTION</text>
            <text class="summary-value action-value">刷新搜索</text>
          </view>
        </view>
      </view>

      <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
        <view class="tab-row">
          <view
            v-for="(tab, index) in resultTabs"
            :key="tab.name"
            class="tab-pill"
            :class="{ active: activeTab === index }"
            @click="switchTab(index)"
          >
            <text class="tab-pill-name">{{ tab.name }}</text>
            <text class="tab-pill-count">{{ tab.count }}</text>
          </view>
        </view>
      </scroll-view>

      <scroll-view class="result-content" scroll-y>
        <view v-if="loading && currentList.length === 0" class="state-card">
          <view class="loader-ring"></view>
          <text class="state-title">正在整理搜索结果</text>
          <text class="state-subtitle">我们正在聚合对应模块的内容。</text>
        </view>

        <view v-else-if="showEmptyState" class="state-card">
          <text class="state-title">没有找到相关结果</text>
          <text class="state-subtitle">试试更短的关键词，或切换到其他分类继续看。</text>
        </view>

        <view v-else class="card-list">
          <template v-if="activeTab === 0">
            <view
              v-for="item in currentVenues"
              :key="`venue-${item.id}`"
              class="result-card"
              @click="goToVenueDetail(item)"
            >
              <view class="result-card-media venue-media">
                <image
                  v-if="item.image"
                  class="result-image"
                  :src="resolveImageUrl(item.image)"
                  mode="aspectFill"
                />
                <view v-else class="media-placeholder">
                  <text class="placeholder-label">VENUE</text>
                </view>
                <view class="floating-badge">COURT READY</view>
              </view>

              <view class="result-card-body">
                <view class="card-heading">
                  <text class="card-title">{{ item.name }}</text>
                  <text class="card-price">¥{{ item.price }}/小时</text>
                </view>
                <view class="meta-row">
                  <view class="meta-chip">
                    <uni-icons type="location" size="14" color="#a33e00"></uni-icons>
                    <text>{{ item.location || '地址待更新' }}</text>
                  </view>
                  <view class="meta-chip">
                    <uni-icons type="star-filled" size="14" color="#f59e0b"></uni-icons>
                    <text>{{ item.rating }}</text>
                  </view>
                </view>
                <view class="tag-row">
                  <text v-for="(tag, tagIndex) in item.tags" :key="tagIndex" class="inline-tag">{{ tag.text }}</text>
                </view>
              </view>
            </view>
          </template>

          <template v-else-if="activeTab === 1">
            <view
              v-for="item in currentCourses"
              :key="`course-${item.id}`"
              class="result-card"
              @click="goToCourseDetail(item)"
            >
              <view class="result-card-body full-body">
                <view class="card-heading">
                  <view>
                    <text class="card-kicker">COURSE SESSION</text>
                    <text class="card-title">{{ item.name }}</text>
                  </view>
                  <text class="card-price">¥{{ item.price }}/课时</text>
                </view>
                <view class="meta-column">
                  <view class="meta-chip">
                    <uni-icons type="person" size="14" color="#a33e00"></uni-icons>
                    <text>{{ item.coachName || '教练待定' }}</text>
                  </view>
                  <view class="meta-chip">
                    <uni-icons type="calendar" size="14" color="#a33e00"></uni-icons>
                    <text>{{ item.date || '日期待定' }} {{ item.time || '' }}</text>
                  </view>
                </view>
                <view class="level-ribbon">{{ item.level }}</view>
              </view>
            </view>
          </template>

          <template v-else-if="activeTab === 2">
            <view
              v-for="item in currentTournaments"
              :key="`tournament-${item.id}`"
              class="result-card"
              @click="goToTournamentDetail(item)"
            >
              <view class="result-card-body full-body">
                <view class="card-heading">
                  <view>
                    <text class="card-kicker">TOURNAMENT</text>
                    <text class="card-title">{{ item.name }}</text>
                  </view>
                  <text class="status-pill" :class="getStatusClass(item.status)">
                    {{ getStatusText(item.status) }}
                  </text>
                </view>
                <view class="meta-column">
                  <view class="meta-chip">
                    <uni-icons type="calendar" size="14" color="#a33e00"></uni-icons>
                    <text>{{ item.date || '日期待定' }}</text>
                  </view>
                  <view class="meta-chip">
                    <uni-icons type="wallet" size="14" color="#a33e00"></uni-icons>
                    <text>报名费 ¥{{ item.fee }}</text>
                  </view>
                  <view class="meta-chip">
                    <uni-icons type="person" size="14" color="#a33e00"></uni-icons>
                    <text>{{ item.participants }}/{{ item.maxParticipants || '-' }} 已报名</text>
                  </view>
                </view>
              </view>
            </view>
          </template>

          <template v-else>
            <view
              v-for="item in currentEquipment"
              :key="`equipment-${item.id}`"
              class="result-card"
              @click="goToEquipmentDetail(item)"
            >
              <view class="result-card-media equipment-media">
                <image
                  v-if="item.image"
                  class="result-image"
                  :src="resolveImageUrl(item.image)"
                  mode="aspectFill"
                />
                <view v-else class="media-placeholder">
                  <text class="placeholder-label">EQUIPMENT</text>
                </view>
                <view class="floating-badge">READY TO RENT</view>
              </view>

              <view class="result-card-body">
                <view class="card-heading">
                  <view>
                    <text class="card-kicker">EQUIPMENT</text>
                    <text class="card-title">{{ item.name }}</text>
                  </view>
                  <text class="card-price">¥{{ item.price }}/次</text>
                </view>
                <view class="meta-row">
                  <view class="meta-chip">
                    <uni-icons type="shop" size="14" color="#a33e00"></uni-icons>
                    <text>{{ item.brand }}</text>
                  </view>
                  <view class="meta-chip">
                    <uni-icons type="wallet" size="14" color="#a33e00"></uni-icons>
                    <text>押金 ¥{{ item.deposit }}</text>
                  </view>
                  <view class="meta-chip">
                    <uni-icons type="person" size="14" color="#a33e00"></uni-icons>
                    <text>库存 {{ item.availableQuantity }}</text>
                  </view>
                </view>
                <view class="tag-row">
                  <text v-for="(tag, tagIndex) in item.tags" :key="tagIndex" class="inline-tag">{{ tag.text }}</text>
                </view>
              </view>
            </view>
          </template>
        </view>
        <view v-if="!loading && currentList.length > 0" class="footer-state">
          <text class="footer-text">当前分类已展示全部搜索结果</text>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { searchCourses, searchEquipment, searchTournaments, searchVenues } from '@/api/search'
import { safeNavigateBack } from '@/utils/navigation'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import {
  mapCourseSearchResult,
  mapEquipmentSearchResult,
  mapTournamentSearchResult,
  mapVenueSearchResult,
  type CourseSearchCard,
  type EquipmentSearchCard,
  type TournamentSearchCard,
  type VenueSearchCard
} from '@/utils/searchResult'

const userStore = useUserStore()
const searchKeyword = ref('')
const activeTab = ref(0)
const resultTabs = ref([
  { name: '场馆', count: 0 },
  { name: '课程', count: 0 },
  { name: '赛事', count: 0 },
  { name: '器材', count: 0 }
])
const venues = ref<VenueSearchCard[]>([])
const courses = ref<CourseSearchCard[]>([])
const tournaments = ref<TournamentSearchCard[]>([])
const equipment = ref<EquipmentSearchCard[]>([])
const loading = ref(false)
const initialized = ref(false)

const currentList = computed(() => {
  if (activeTab.value === 0) return venues.value
  if (activeTab.value === 1) return courses.value
  if (activeTab.value === 2) return tournaments.value
  return equipment.value
})
const currentVenues = computed(() => venues.value)
const currentCourses = computed(() => courses.value)
const currentTournaments = computed(() => tournaments.value)
const currentEquipment = computed(() => equipment.value)

const totalCount = computed(() => venues.value.length + courses.value.length + tournaments.value.length + equipment.value.length)
const displayKeyword = computed(() => searchKeyword.value.trim() || '未输入')
const showEmptyState = computed(() => !loading.value && !!searchKeyword.value.trim() && currentList.value.length === 0)

onLoad((options?: Record<string, string | undefined>) => {
  if (options?.keyword) {
    searchKeyword.value = decodeURIComponent(options.keyword)
  }
  if (options?.tab) {
    const tabIndex = Number(options.tab)
    if (!Number.isNaN(tabIndex) && tabIndex >= 0 && tabIndex <= 3) {
      activeTab.value = tabIndex
    }
  }
})

async function loadAllResults() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) return

  loading.value = true
  try {
    const [venueResult, courseResult, tournamentResult, equipmentResult] = await Promise.all([
      searchVenues({ keyword }),
      searchCourses({ keyword }),
      searchTournaments({ keyword }),
      searchEquipment({ keyword })
    ])

    venues.value = Array.isArray(venueResult) ? venueResult.map(mapVenueSearchResult) : []
    courses.value = Array.isArray(courseResult) ? courseResult.map(mapCourseSearchResult) : []
    tournaments.value = Array.isArray(tournamentResult) ? tournamentResult.map(mapTournamentSearchResult) : []
    equipment.value = Array.isArray(equipmentResult) ? equipmentResult.map(mapEquipmentSearchResult) : []

    resultTabs.value = [
      { name: '场馆', count: venues.value.length },
      { name: '课程', count: courses.value.length },
      { name: '赛事', count: tournaments.value.length },
      { name: '器材', count: equipment.value.length }
    ]
    initialized.value = true
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    uni.showToast({
      title: '请输入搜索内容',
      icon: 'none'
    })
    return
  }

  await loadAllResults()
}

function switchTab(index: number) {
  activeTab.value = index
}

function clearSearch() {
  searchKeyword.value = ''
  venues.value = []
  courses.value = []
  tournaments.value = []
  equipment.value = []
  resultTabs.value = [
    { name: '场馆', count: 0 },
    { name: '课程', count: 0 },
    { name: '赛事', count: 0 },
    { name: '器材', count: 0 }
  ]
}

function getStatusText(status: number) {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '报名中',
    2: '报名截止',
    3: '进行中',
    4: '已结束'
  }
  return statusMap[status] || '未知'
}

function getStatusClass(status: number) {
  const classMap: Record<number, string> = {
    0: 'status-cancelled',
    1: 'status-registering',
    2: 'status-closed',
    3: 'status-ongoing',
    4: 'status-ended'
  }
  return classMap[status] || ''
}

function goToVenueDetail(venue: VenueSearchCard) {
  uni.navigateTo({
    url: `/pages/venue/detail?id=${venue.id}`
  })
}

function goToCourseDetail(course: CourseSearchCard) {
  uni.navigateTo({
    url: `/pages/course/detail?id=${course.id}`
  })
}

function goToTournamentDetail(tournament: TournamentSearchCard) {
  uni.navigateTo({
    url: `/pages/tournament/detail?id=${tournament.id}`
  })
}

function goToEquipmentDetail(item: EquipmentSearchCard) {
  uni.navigateTo({
    url: `/pages/equipment/detail?id=${item.id}`
  })
}

function handleBack() {
  safeNavigateBack()
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }

  if (searchKeyword.value.trim() && !initialized.value) {
    await loadAllResults()
  }
})
</script>

<style lang="scss" scoped>
.result-shell {
  background:
    radial-gradient(circle at top right, rgba(255, 177, 120, 0.3), transparent 24%),
    linear-gradient(180deg, #fff7ed 0%, #f8fafc 34%, #f8fafc 100%);
}

.result-page {
  min-height: 100vh;
  padding: 28rpx 24rpx 40rpx;
  box-sizing: border-box;
}

.result-header {
  padding: 28rpx;
  border-radius: 36rpx;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 247, 237, 0.96));
  box-shadow: 0 24rpx 60rpx rgba(15, 23, 42, 0.08);
}

.top-row,
.summary-strip,
.card-heading,
.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.top-row {
  margin-bottom: 28rpx;
}

.brand-back {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 237, 213, 0.88);
}

.brand-text,
.header-note,
.headline-eyebrow,
.summary-label,
.card-kicker {
  letter-spacing: 0.18em;
}

.brand-text {
  font-size: 20rpx;
  font-weight: 800;
  color: #9a3412;
}

.header-note {
  font-size: 20rpx;
  color: #94a3b8;
  font-weight: 700;
}

.headline-block {
  margin-bottom: 28rpx;
}

.headline-eyebrow {
  display: block;
  margin-bottom: 14rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.headline-title {
  display: block;
  font-size: 48rpx;
  line-height: 1.18;
  color: #0f172a;
  font-weight: 800;
}

.headline-subtitle {
  display: block;
  margin-top: 16rpx;
  font-size: 24rpx;
  line-height: 1.65;
  color: #64748b;
}

.search-bar {
  display: flex;
  align-items: center;
  height: 94rpx;
  padding: 0 22rpx;
  border-radius: 26rpx;
  background: #f8fafc;
  border: 2rpx solid rgba(148, 163, 184, 0.12);
}

.search-input {
  flex: 1;
  margin-left: 14rpx;
  font-size: 28rpx;
  color: #0f172a;
}

.search-clear {
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-strip {
  gap: 16rpx;
  margin-top: 22rpx;
}

.summary-card {
  flex: 1;
  min-height: 112rpx;
  padding: 18rpx 20rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #ffffff 0%, #fff7ed 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.18);
  box-sizing: border-box;
}

.compact-action {
  background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%);
}

.summary-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.summary-value {
  display: block;
  font-size: 30rpx;
  color: #0f172a;
  font-weight: 800;
}

.keyword-value {
  word-break: break-all;
}

.action-value {
  font-size: 24rpx;
}

.tabs-scroll {
  margin-top: 24rpx;
  white-space: nowrap;
}

.tab-row {
  display: inline-flex;
  gap: 16rpx;
  padding-right: 24rpx;
}

.tab-pill {
  min-width: 180rpx;
  padding: 18rpx 24rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.88);
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 10rpx 26rpx rgba(15, 23, 42, 0.05);
}

.tab-pill.active {
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
}

.tab-pill-name,
.tab-pill-count {
  font-size: 24rpx;
  font-weight: 800;
  color: #0f172a;
}

.tab-pill.active .tab-pill-name,
.tab-pill.active .tab-pill-count {
  color: #ffffff;
}

.result-content {
  height: calc(100vh - 460rpx);
  margin-top: 20rpx;
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  padding-bottom: 20rpx;
}

.result-card,
.state-card {
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 20rpx 48rpx rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.result-card-media {
  position: relative;
  height: 260rpx;
}

.venue-media {
  background: linear-gradient(135deg, #ffd7b5 0%, #fff7ed 100%);
}

.equipment-media {
  background: linear-gradient(135deg, #fee2e2 0%, #fff7ed 100%);
}

.result-image {
  width: 100%;
  height: 100%;
}

.media-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    linear-gradient(135deg, rgba(255, 237, 213, 0.86), rgba(255, 255, 255, 0.96));
}

.placeholder-label {
  font-size: 22rpx;
  font-weight: 800;
  color: #9a3412;
  letter-spacing: 0.22em;
}

.floating-badge {
  position: absolute;
  left: 22rpx;
  bottom: 22rpx;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.74);
  color: #ffffff;
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 0.14em;
}

.result-card-body {
  padding: 26rpx;
}

.full-body {
  min-height: 220rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-heading {
  align-items: flex-start;
  gap: 16rpx;
}

.card-title {
  display: block;
  font-size: 34rpx;
  line-height: 1.25;
  color: #0f172a;
  font-weight: 800;
}

.card-price {
  flex-shrink: 0;
  font-size: 26rpx;
  color: #c2410c;
  font-weight: 800;
}

.card-kicker {
  display: block;
  margin-bottom: 12rpx;
  font-size: 18rpx;
  color: #94a3b8;
  font-weight: 800;
}

.meta-row {
  margin-top: 20rpx;
  gap: 12rpx;
}

.meta-column {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  margin-top: 24rpx;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  padding: 14rpx 16rpx;
  border-radius: 18rpx;
  background: #fff7ed;
  color: #7c2d12;
  font-size: 22rpx;
  font-weight: 700;
  box-sizing: border-box;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 20rpx;
}

.inline-tag,
.level-ribbon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;
}

.inline-tag {
  background: #f1f5f9;
  color: #475569;
}

.level-ribbon {
  align-self: flex-start;
  margin-top: 24rpx;
  background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%);
  color: #9a3412;
}

.status-pill {
  flex-shrink: 0;
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 800;
  background: #fff7ed;
  color: #9a3412;
}

.status-registering {
  background: rgba(249, 115, 22, 0.14);
  color: #c2410c;
}

.status-closed {
  background: rgba(245, 158, 11, 0.16);
  color: #b45309;
}

.status-ongoing {
  background: rgba(59, 130, 246, 0.14);
  color: #1d4ed8;
}

.status-ended,
.status-cancelled {
  background: rgba(148, 163, 184, 0.18);
  color: #475569;
}

.state-card {
  padding: 72rpx 36rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.loader-ring {
  width: 72rpx;
  height: 72rpx;
  margin-bottom: 24rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(251, 146, 60, 0.16);
  border-top-color: #ea580c;
  animation: spin 0.8s linear infinite;
}

.state-title {
  font-size: 30rpx;
  color: #0f172a;
  font-weight: 800;
}

.state-subtitle,
.footer-text {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #94a3b8;
}

.footer-state {
  padding: 28rpx 0 12rpx;
  text-align: center;
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
