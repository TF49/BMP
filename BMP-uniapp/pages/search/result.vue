<template>
  <MobileLayout>
    <!-- Header -->
    <view class="header">
      <view class="header-content">
        <text class="back-icon" @click="handleBack">‹</text>
        <view class="search-container">
          <uni-icons type="search" size="18" color="#475569" class="search-icon"></uni-icons>
          <input 
            class="search-input" 
            type="text" 
            v-model="searchKeyword"
            placeholder="搜索场馆、教练、课程或赛事..."
            @confirm="handleSearch"
            @input="handleInput"
          />
          <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
            <uni-icons type="clear" size="16" color="#475569"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- Result Type Tabs -->
    <view class="result-tabs">
      <view 
        v-for="(tab, index) in resultTabs" 
        :key="index"
        class="tab-item"
        :class="{ active: activeTab === index }"
        @click="switchTab(index)"
      >
        {{ tab.name }}({{ tab.count }})
      </view>
    </view>

    <!-- Results Content -->
    <scroll-view class="content" scroll-y @scrolltolower="loadMore">
      <!-- Venues -->
      <view v-show="activeTab === 0" class="results-list">
        <view 
          v-for="(venue, index) in venues" 
          :key="index"
          class="result-item"
          @click="goToVenueDetail(venue)"
        >
          <view class="result-image">
            <image
              v-if="venue.image"
              class="result-img"
              :src="resolveImageUrl(venue.image)"
              mode="aspectFill"
            />
            <text v-else class="image-placeholder">Venue Image</text>
          </view>
          <view class="result-info">
            <view class="result-header">
              <text class="result-name">{{ venue.name }}</text>
              <text class="result-price">¥{{ venue.price }}/小时</text>
            </view>
            <view class="result-meta">
              <view class="result-location">
                <uni-icons type="location" size="14" color="#475569"></uni-icons>
                <text>{{ venue.location }}</text>
              </view>
              <view class="result-rating">
                <uni-icons type="star-filled" size="14" color="#f59e0b"></uni-icons>
                <text>{{ venue.rating }}</text>
              </view>
            </view>
            <view class="result-tags">
              <text 
                v-for="(tag, tagIndex) in venue.tags" 
                :key="tagIndex"
                class="result-tag"
                :class="tag.class"
              >
                {{ tag.text }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- Courses -->
      <view v-show="activeTab === 1" class="results-list">
        <view 
          v-for="(course, index) in courses" 
          :key="index"
          class="result-item"
          @click="goToCourseDetail(course)"
        >
          <view class="result-image">
            <text class="image-placeholder">Course Image</text>
          </view>
          <view class="result-info">
            <view class="result-header">
              <text class="result-name">{{ course.name }}</text>
              <text class="result-price">¥{{ course.price }}/课时</text>
            </view>
            <view class="result-meta">
              <view class="result-coach">
                <uni-icons type="person" size="14" color="#475569"></uni-icons>
                <text>{{ course.coachName }}</text>
              </view>
              <text class="result-level">{{ course.level }}</text>
            </view>
            <view class="result-time">
              <view class="time-text">
                <uni-icons type="calendar" size="14" color="#475569"></uni-icons>
                <text>{{ course.date }} {{ course.time }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Tournaments -->
      <view v-show="activeTab === 2" class="results-list">
        <view 
          v-for="(tournament, index) in tournaments" 
          :key="index"
          class="result-item"
          @click="goToTournamentDetail(tournament)"
        >
          <view class="result-image">
            <text class="image-placeholder">Tournament Image</text>
          </view>
          <view class="result-info">
            <view class="result-header">
              <text class="result-name">{{ tournament.name }}</text>
              <text class="result-status" :class="getStatusClass(tournament.status)">
                {{ getStatusText(tournament.status) }}
              </text>
            </view>
            <view class="result-meta">
              <view class="result-date">
                <uni-icons type="calendar" size="14" color="#475569"></uni-icons>
                <text>{{ tournament.date }}</text>
              </view>
              <view class="result-fee">
                <uni-icons type="wallet" size="14" color="#475569"></uni-icons>
                <text>¥{{ tournament.fee }}</text>
              </view>
            </view>
            <view class="result-participants">
              <view class="participants-text">
                <uni-icons type="person" size="14" color="#475569"></uni-icons>
                <text>{{ tournament.participants }}/{{ tournament.maxParticipants }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- Load More -->
      <view v-if="hasMore" class="load-more">
        <text class="load-text">加载更多...</text>
      </view>
      <view v-else-if="showNoMore" class="no-more">
        <text class="no-more-text">没有更多结果了</text>
      </view>
    </scroll-view>

    <!-- Empty State -->
    <view v-if="showEmptyState" class="empty-state">
      <text class="empty-text">没有找到相关结果</text>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { searchVenues, searchCourses, searchTournaments } from '@/api/search'
import { safeNavigateBack } from '@/utils/navigation'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

// 响应式数据
const searchKeyword = ref('')
const activeTab = ref(0)
const resultTabs = ref([
  { name: '场馆', count: 0 },
  { name: '课程', count: 0 },
  { name: '赛事', count: 0 }
])
const venues = ref<any[]>([])
const courses = ref<any[]>([])
const tournaments = ref<any[]>([])
const loading = ref(false)
const hasMore = ref(false)
const showNoMore = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)

const userStore = useUserStore()

// 计算属性
const showEmptyState = computed(() => {
  return searchKeyword.value && 
         venues.value.length === 0 && 
         courses.value.length === 0 && 
         tournaments.value.length === 0
})

// 页面加载
onLoad((options?: Record<string, string | undefined>) => {
  if (options?.keyword) {
    searchKeyword.value = decodeURIComponent(options.keyword)
  }
})

// 获取状态文本
const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '已取消',
    1: '报名中',
    2: '报名截止',
    3: '进行中',
    4: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    0: 'status-cancelled',
    1: 'status-registering',
    2: 'status-closed',
    3: 'status-ongoing',
    4: 'status-ended'
  }
  return classMap[status] || ''
}

// 切换标签
const switchTab = (index: number) => {
  activeTab.value = index
}

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) return

  // 重置数据
  venues.value = []
  courses.value = []
  tournaments.value = []
  currentPage.value = 1
  hasMore.value = false
  showNoMore.value = false

  await performSearch()
}

// 执行搜索
const performSearch = async () => {
  loading.value = true
  try {
    const params = {
      keyword: searchKeyword.value,
      page: currentPage.value,
      size: pageSize.value
    }

    // 根据当前标签执行相应搜索
    if (activeTab.value === 0) {
      const result = await searchVenues(params)
      venues.value = [...venues.value, ...result.data.map((v: any) => ({
        id: v.id,
        name: v.venueName,
        location: v.address,
        price: v.hourlyPrice || 50,
        // 统一与 web 使用的主图字段：sys_venue.venue_image
        image: v.venueImage,
        rating: v.rating || 4.5,
        tags: [
          { text: '空调', class: 'tag-green' },
          { text: '停车', class: 'tag-orange' }
        ]
      }))]
      resultTabs.value[0].count = result.total
      hasMore.value = venues.value.length < result.total
    } else if (activeTab.value === 1) {
      const result = await searchCourses(params)
      courses.value = [...courses.value, ...result.data.map((c: any) => ({
        id: c.id,
        name: c.courseName,
        coachName: c.coachName,
        price: c.coursePrice,
        level: c.level || '初级',
        date: c.courseDate,
        time: `${c.startTime} - ${c.endTime}`
      }))]
      resultTabs.value[1].count = result.total
      hasMore.value = courses.value.length < result.total
    } else if (activeTab.value === 2) {
      const result = await searchTournaments(params)
      tournaments.value = [...tournaments.value, ...result.data.map((t: any) => ({
        id: t.id,
        name: t.tournamentName,
        date: t.startDate,
        fee: t.entryFee || 0,
        status: t.status,
        participants: t.currentParticipants || 0,
        maxParticipants: t.maxParticipants
      }))]
      resultTabs.value[2].count = result.total
      hasMore.value = tournaments.value.length < result.total
    }
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    showNoMore.value = !hasMore.value && (
      (activeTab.value === 0 && venues.value.length > 0) ||
      (activeTab.value === 1 && courses.value.length > 0) ||
      (activeTab.value === 2 && tournaments.value.length > 0)
    )
  }
}

// 加载更多
const loadMore = async () => {
  if (!hasMore.value || loading.value) return

  currentPage.value++
  await performSearch()
}

// 处理输入
const handleInput = (e: any) => {
  searchKeyword.value = e.target.value
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  venues.value = []
  courses.value = []
  tournaments.value = []
  resultTabs.value = [
    { name: '场馆', count: 0 },
    { name: '课程', count: 0 },
    { name: '赛事', count: 0 }
  ]
}

// 跳转到场馆详情
const goToVenueDetail = (venue: any) => {
  uni.navigateTo({
    url: `/pages/venue/detail?id=${venue.id}`
  })
}

// 跳转到课程详情
const goToCourseDetail = (course: any) => {
  uni.navigateTo({
    url: `/pages/course/detail?id=${course.id}`
  })
}

// 跳转到赛事详情
const goToTournamentDetail = (tournament: any) => {
  uni.navigateTo({
    url: `/pages/tournament/detail?id=${tournament.id}`
  })
}

// 返回上一页
const handleBack = () => {
  safeNavigateBack()
}

// 页面加载时执行搜索
onMounted(async () => {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    // 未登录用户重定向到登录页
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }
  
  if (searchKeyword.value) {
    await handleSearch()
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.header {
  background-color: #ffffff;
  padding: 20rpx 28rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  border-bottom: 1rpx solid #e6e6e6;
}

.header-content {
  display: flex;
  align-items: center;
}

.back-icon {
  font-size: 40rpx;
  color: #333333;
  font-weight: bold;
  width: 56rpx;
}

.search-container {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 50rpx;
  padding: 12rpx 20rpx;
}

.search-icon {
  font-size: 28rpx;
  color: #999999;
  margin-right: 12rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333333;
}

.clear-icon {
  font-size: 32rpx;
  color: #999999;
  margin-left: 12rpx;
}

.result-tabs {
  background-color: #ffffff;
  padding: 18rpx 28rpx;
  display: flex;
  gap: 24rpx;
  border-bottom: 1rpx solid #e6e6e6;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10rpx 16rpx;
  font-size: 24rpx;
  color: #999999;
  border-radius: 9999rpx;
  transition: all 0.3s;
  background-color: #f5f5f5;
  
  &.active {
    background-color: #3cc51f;
    color: #ffffff;
  }
}

.content {
  flex: 1;
  height: calc(100vh - 200rpx);
  background-color: #f5f7fa;
}

.results-list {
  padding: 24rpx 28rpx;
}

.result-item {
  background-color: #ffffff;
  border-radius: 18rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.result-image {
  width: 120rpx;
  height: 120rpx;
  background-color: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(153, 153, 153, 0.3);
  font-size: 20rpx;
  overflow: hidden;
}

.result-img {
  width: 100%;
  height: 100%;
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.result-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
  flex: 1;
}

.result-price, .result-status {
  font-size: 24rpx;
  font-weight: bold;
  color: #ef4444;
  
  &.status-registering {
    color: #3cc51f;
  }
  
  &.status-closed {
    color: #ff9800;
  }
  
  &.status-ongoing {
    color: #2196f3;
  }
  
  &.status-ended {
    color: #999999;
  }
  
  &.status-cancelled {
    color: #f44336;
  }
}

.result-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.result-location, .result-rating, .result-coach, .result-date, .result-fee {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 22rpx;
  color: #475569;
}
.result-location text, .result-rating text, .result-coach text, .result-date text, .result-fee text { flex: 1; }
.result-level {
  font-size: 22rpx;
  color: #475569;
}

.result-tags {
  display: flex;
  gap: 12rpx;
}

.result-tag {
  font-size: 18rpx;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  
  &.tag-green {
    background-color: #e8f5e9;
    color: #3cc51f;
  }
  
  &.tag-orange {
    background-color: #fff3e0;
    color: #ff9800;
  }
}

.result-time, .result-participants {
  margin-top: 8rpx;
}

.time-text, .participants-text {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 20rpx;
  color: #475569;
}
.time-text text, .participants-text text { flex: 1; }

.load-more, .no-more {
  padding: 40rpx 0;
  text-align: center;
}

.load-text, .no-more-text {
  font-size: 24rpx;
  color: #999999;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 160rpx 0;
  margin-top: 100rpx;
}

.empty-text {
  font-size: 24rpx;
  color: #999999;
}
</style>
