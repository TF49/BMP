<template>
  <MobileLayout :showTabBar="false">
    <!-- Search Header -->
    <view class="search-header">
      <view class="search-container">
        <uni-icons type="search" size="18" color="#475569" class="search-icon"></uni-icons>
        <input 
          class="search-input" 
          type="text" 
          v-model="searchKeyword"
          :placeholder="placeholderText"
          @confirm="handleSearch"
          @input="handleInput"
          :focus="true"
        />
        <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
          <uni-icons type="clear" size="16" color="#475569"></uni-icons>
        </view>
      </view>
      <text class="cancel-btn" @click="handleCancel">取消</text>
    </view>

    <!-- Search Suggestions -->
    <scroll-view v-if="showSuggestions && suggestions.length > 0" class="suggestions-container" scroll-y>
      <view class="suggestion-item" v-for="(suggestion, index) in suggestions" :key="index" @click="selectSuggestion(suggestion)">
        <uni-icons type="search" size="16" color="#475569" class="suggestion-icon"></uni-icons>
        <text class="suggestion-text">{{ suggestion }}</text>
      </view>
    </scroll-view>

    <!-- Search History -->
    <view v-else-if="showHistory && searchHistory.length > 0" class="history-container">
      <view class="history-header">
        <text class="history-title">搜索历史</text>
        <text class="clear-history" @click="clearHistory">清空</text>
      </view>
      <view class="history-list">
        <view 
          v-for="(history, index) in searchHistory" 
          :key="index"
          class="history-item"
          @click="selectHistory(history)"
        >
          <uni-icons type="calendar" size="16" color="#475569" class="history-icon"></uni-icons>
          <text class="history-text">{{ history }}</text>
        </view>
      </view>
    </view>

    <!-- Hot Searches -->
    <view v-else-if="showHotSearches && hotSearches.length > 0" class="hot-searches-container">
      <text class="hot-title">热门搜索</text>
      <view class="hot-list">
        <view 
          v-for="(hot, index) in hotSearches" 
          :key="index"
          class="hot-item"
          @click="selectHotSearch(hot)"
        >
          {{ hot }}
        </view>
      </view>
    </view>

    <!-- Search Results -->
    <scroll-view v-else-if="searchResults.length > 0" class="results-container" scroll-y>
      <view class="results-header">
        <text class="results-count">找到 {{ searchResults.length }} 条结果</text>
      </view>
      
      <!-- Venues -->
      <view v-if="venueResults.length > 0">
        <text class="result-category">场地</text>
        <view 
          v-for="(venue, index) in venueResults" 
          :key="'venue-' + index"
          class="result-item"
          @click="goToVenueDetail(venue)"
        >
          <view class="result-icon"><uni-icons type="location" size="24" color="#3cc51f"></uni-icons></view>
          <view class="result-content">
            <text class="result-title">{{ venue.name }}</text>
            <text class="result-subtitle">{{ venue.location }} • ¥{{ venue.price }}/小时</text>
          </view>
        </view>
      </view>

      <!-- Courses -->
      <view v-if="courseResults.length > 0">
        <text class="result-category">课程</text>
        <view 
          v-for="(course, index) in courseResults" 
          :key="'course-' + index"
          class="result-item"
          @click="goToCourseDetail(course)"
        >
          <view class="result-icon"><uni-icons type="compose" size="24" color="#3cc51f"></uni-icons></view>
          <view class="result-content">
            <text class="result-title">{{ course.name }}</text>
            <text class="result-subtitle">教练: {{ course.coachName }} • ¥{{ course.price }}/课时</text>
          </view>
        </view>
      </view>

      <!-- Tournaments -->
      <view v-if="tournamentResults.length > 0">
        <text class="result-category">赛事</text>
        <view 
          v-for="(tournament, index) in tournamentResults" 
          :key="'tournament-' + index"
          class="result-item"
          @click="goToTournamentDetail(tournament)"
        >
          <view class="result-icon"><uni-icons type="medal" size="24" color="#3cc51f"></uni-icons></view>
          <view class="result-content">
            <text class="result-title">{{ tournament.name }}</text>
            <text class="result-subtitle">{{ tournament.date }} • 报名费 ¥{{ tournament.fee }}</text>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- Empty State -->
    <view v-else-if="searchKeyword && searchResults.length === 0" class="empty-state">
      <text class="empty-text">没有找到相关结果</text>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { searchVenues, searchCourses, searchTournaments, getSearchSuggestions } from '@/api/search'
import { debounce } from '@/utils/debounce'
import { safeNavigateBack } from '@/utils/navigation'

// 响应式数据
const searchKeyword = ref('')
const searchHistory = ref<string[]>([])
const hotSearches = ref(['奥体中心', '羽毛球培训', '青少年训练营', '周末比赛'])
const suggestions = ref<string[]>([])
const searchResults = ref<any[]>([])
const showSuggestions = ref(false)
const showHistory = ref(true)
const showHotSearches = ref(true)

// 计算属性
const placeholderText = computed(() => {
  return '搜索场馆、教练、课程或赛事...'
})

const venueResults = computed(() => {
  return searchResults.value.filter(item => item.type === 'venue')
})

const courseResults = computed(() => {
  return searchResults.value.filter(item => item.type === 'course')
})

const tournamentResults = computed(() => {
  return searchResults.value.filter(item => item.type === 'tournament')
})

// 监听搜索关键词变化
watch(searchKeyword, (newVal) => {
  if (newVal.trim()) {
    showSuggestions.value = true
    showHistory.value = false
    showHotSearches.value = false
    // 模拟获取搜索建议
    getSuggestions(newVal)
  } else {
    showSuggestions.value = false
    showHistory.value = true
    showHotSearches.value = true
    suggestions.value = []
  }
})

// 获取搜索建议（防抖处理）
const fetchSuggestions = debounce(async (keyword: string) => {
  if (!keyword || keyword.length < 1) {
    suggestions.value = []
    return
  }
  
  try {
    const result = await getSearchSuggestions({ keyword })
    if (result && Array.isArray(result)) {
      suggestions.value = result
    } else {
      suggestions.value = []
    }
  } catch (error) {
    console.error('获取搜索建议失败:', error)
    suggestions.value = []
  }
}, 500)

const getSuggestions = (keyword: string) => {
  fetchSuggestions(keyword)
}

// 处理搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) return

  // 添加到搜索历史
  addToHistory(searchKeyword.value.trim())

  try {
    uni.showLoading({
      title: '搜索中...'
    })

    // 并行搜索所有类型
    const [venues, courses, tournaments] = await Promise.all([
      searchVenues({ keyword: searchKeyword.value }),
      searchCourses({ keyword: searchKeyword.value }),
      searchTournaments({ keyword: searchKeyword.value })
    ])

    // 合并结果并添加类型标识
    const results = [
      ...venues.data.map((v: any) => ({ ...v, type: 'venue' })),
      ...courses.data.map((c: any) => ({ ...c, type: 'course' })),
      ...tournaments.data.map((t: any) => ({ ...t, type: 'tournament' }))
    ]

    searchResults.value = results
    showSuggestions.value = false
    showHistory.value = false
    showHotSearches.value = false
  } catch (error) {
    console.error('搜索失败:', error)
    uni.showToast({
      title: '搜索失败',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 处理输入
const handleInput = (e: any) => {
  searchKeyword.value = e.target.value
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  suggestions.value = []
  searchResults.value = []
  showSuggestions.value = false
  showHistory.value = true
  showHotSearches.value = true
}

// 选择搜索建议
const selectSuggestion = (suggestion: string) => {
  searchKeyword.value = suggestion
  handleSearch()
}

// 选择搜索历史
const selectHistory = (history: string) => {
  searchKeyword.value = history
  handleSearch()
}

// 选择热门搜索
const selectHotSearch = (hot: string) => {
  searchKeyword.value = hot
  handleSearch()
}

// 添加到搜索历史
const addToHistory = (keyword: string) => {
  if (!searchHistory.value.includes(keyword)) {
    searchHistory.value.unshift(keyword)
    // 限制历史记录数量
    if (searchHistory.value.length > 10) {
      searchHistory.value.pop()
    }
    // 保存到本地存储
    uni.setStorageSync('search_history', searchHistory.value)
  }
}

// 清空历史记录
const clearHistory = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (res.confirm) {
        searchHistory.value = []
        uni.removeStorageSync('search_history')
      }
    }
  })
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

// 取消搜索
const handleCancel = () => {
  safeNavigateBack()
}

// 页面加载时获取历史记录
// 注意：在setup中无法直接使用onLoad，这里简单获取历史记录
try {
  const history = uni.getStorageSync('search_history') || []
  searchHistory.value = history
} catch (error) {
  console.error('获取搜索历史失败:', error)
}
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.search-header {
  display: flex;
  align-items: center;
  padding: 20rpx 28rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #e6e6e6;
}

.search-container {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #f5f5f5;
  border-radius: 50rpx;
  padding: 12rpx 20rpx;
  margin-right: 20rpx;
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

.cancel-btn {
  font-size: 28rpx;
  color: #3cc51f;
  font-weight: 500;
}

.suggestions-container {
  position: absolute;
  top: 88rpx;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ffffff;
  z-index: 100;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.suggestion-icon {
  font-size: 24rpx;
  color: #999999;
  margin-right: 16rpx;
}

.suggestion-text {
  font-size: 28rpx;
  color: #333333;
}

.history-container {
  padding: 24rpx 28rpx;
  background-color: #ffffff;
  margin-top: 88rpx;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.history-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
}

.clear-history {
  font-size: 24rpx;
  color: #999999;
}

.history-list {
  display: flex;
  flex-direction: column;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f3f4f6;
}

.history-icon {
  font-size: 24rpx;
  color: #999999;
  margin-right: 16rpx;
}

.history-text {
  font-size: 26rpx;
  color: #333333;
}

.hot-searches-container {
  padding: 24rpx 28rpx;
  background-color: #ffffff;
  margin-top: 88rpx;
}

.hot-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 24rpx;
  display: block;
}

.hot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.hot-item {
  padding: 12rpx 24rpx;
  background-color: #f5f5f5;
  border-radius: 50rpx;
  font-size: 24rpx;
  color: #333333;
}

.results-container {
  margin-top: 88rpx;
  background-color: #ffffff;
}

.results-header {
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.results-count {
  font-size: 24rpx;
  color: #999999;
}

.result-category {
  display: block;
  padding: 24rpx 28rpx 12rpx;
  font-size: 24rpx;
  font-weight: bold;
  color: #333333;
}

.result-item {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid #f3f4f6;
}

.result-icon {
  width: 64rpx;
  height: 64rpx;
  background-color: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #3cc51f;
  margin-right: 20rpx;
}

.result-content {
  flex: 1;
}

.result-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #333333;
  display: block;
  margin-bottom: 8rpx;
}

.result-subtitle {
  font-size: 22rpx;
  color: #999999;
  display: block;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 160rpx 0;
  margin-top: 88rpx;
}

.empty-text {
  font-size: 24rpx;
  color: #999999;
}
</style>