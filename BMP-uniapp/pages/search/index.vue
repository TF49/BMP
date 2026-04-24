<template>
  <MobileLayout :showTabBar="false" className="search-shell">
    <view class="search-page">
      <view class="hero-panel">
        <view class="hero-top">
          <view class="hero-brand" @click="handleCancel">
            <uni-icons type="left" size="18" color="#a33e00"></uni-icons>
            <text class="hero-brand-text">KINETIC LOGIC</text>
          </view>
          <text class="hero-aside">SEARCH LAB</text>
        </view>

        <view class="hero-copy">
          <text class="hero-eyebrow">DISCOVER THE NEXT SESSION</text>
          <text class="hero-title">搜索场馆、课程、赛事和器材</text>
          <text class="hero-subtitle">当前搜索闭环已对齐四类能力，入口文案和结果页保持一致。</text>
        </view>

        <view class="search-card">
          <view class="search-input-wrap">
            <uni-icons type="search" size="18" color="#94a3b8" class="search-icon"></uni-icons>
            <input
              v-model="searchKeyword"
              class="search-input"
              type="text"
              :placeholder="placeholderText"
              confirm-type="search"
              :focus="true"
              @confirm="handleSearch"
            />
            <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
              <uni-icons type="clear" size="16" color="#64748b"></uni-icons>
            </view>
          </view>

          <view class="quick-actions">
            <view class="quick-action" @click="searchFromPreset('奥体中心')">
              <text class="quick-action-label">VENUE</text>
              <text class="quick-action-text">奥体中心</text>
            </view>
            <view class="quick-action" @click="searchFromPreset('羽毛球培训')">
              <text class="quick-action-label">COURSE</text>
              <text class="quick-action-text">羽毛球培训</text>
            </view>
            <view class="quick-action" @click="searchFromPreset('周末比赛')">
              <text class="quick-action-label">EVENT</text>
              <text class="quick-action-text">周末比赛</text>
            </view>
            <view class="quick-action" @click="searchFromPreset('尤尼克斯球拍')">
              <text class="quick-action-label">EQUIPMENT</text>
              <text class="quick-action-text">尤尼克斯球拍</text>
            </view>
          </view>

          <view class="search-submit" @click="handleSearch">
            <text class="search-submit-text">开始搜索</text>
            <uni-icons type="right" size="16" color="#ffffff"></uni-icons>
          </view>
        </view>
      </view>

      <view class="content-section">
        <view v-if="showSuggestions && suggestions.length > 0" class="content-card">
          <view class="section-head">
            <text class="section-title">搜索建议</text>
            <text class="section-tag">LIVE MATCHES</text>
          </view>
          <view class="chip-list">
            <view
              v-for="(suggestion, index) in suggestions"
              :key="index"
              class="suggestion-chip"
              @click="selectSuggestion(suggestion)"
            >
              <uni-icons type="search" size="14" color="#ea580c"></uni-icons>
              <text class="chip-text">{{ suggestion }}</text>
            </view>
          </view>
        </view>

        <view v-else class="content-grid">
          <view class="content-card">
            <view class="section-head">
              <text class="section-title">热门搜索</text>
              <text class="section-tag">TRENDING</text>
            </view>
            <view class="hot-grid">
              <view
                v-for="(hot, index) in hotSearches"
                :key="index"
                class="hot-card"
                @click="selectHotSearch(hot)"
              >
                <text class="hot-rank">0{{ index + 1 }}</text>
                <text class="hot-text">{{ hot }}</text>
              </view>
            </view>
          </view>

          <view class="content-card">
            <view class="section-head">
              <text class="section-title">搜索历史</text>
              <text v-if="searchHistory.length > 0" class="section-action" @click="clearHistory">清空</text>
            </view>

            <view v-if="searchHistory.length > 0" class="history-list">
              <view
                v-for="(history, index) in searchHistory"
                :key="index"
                class="history-row"
                @click="selectHistory(history)"
              >
                <view class="history-icon-wrap">
                  <uni-icons type="reload" size="15" color="#a33e00"></uni-icons>
                </view>
                <text class="history-text">{{ history }}</text>
                <uni-icons type="right" size="14" color="#94a3b8"></uni-icons>
              </view>
            </view>

            <view v-else class="empty-card">
              <text class="empty-title">还没有搜索历史</text>
              <text class="empty-subtitle">开始搜索后，这里会保留你的高频访问记录。</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import { getSearchSuggestions } from '@/api/search'
import { debounce } from '@/utils/debounce'
import { safeNavigateBack } from '@/utils/navigation'

const SEARCH_HISTORY_KEY = 'search_history'

const searchKeyword = ref('')
const searchHistory = ref<string[]>([])
const hotSearches = ref(['奥体中心', '羽毛球培训', '青少年训练营', '周末比赛', '场地包场', '球拍租借'])
const suggestions = ref<string[]>([])
const showSuggestions = ref(false)

const placeholderText = computed(() => '搜索场馆、课程、赛事或器材...')

watch(searchKeyword, (newValue) => {
  const keyword = newValue.trim()
  if (!keyword) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }

  showSuggestions.value = true
  fetchSuggestions(keyword)
})

const fetchSuggestions = debounce(async (keyword: string) => {
  if (!keyword) {
    suggestions.value = []
    return
  }

  try {
    const result = await getSearchSuggestions({ keyword })
    suggestions.value = Array.isArray(result) ? result.slice(0, 8) : []
  } catch (error) {
    console.error('获取搜索建议失败:', error)
    suggestions.value = []
  }
}, 300)

function persistHistory() {
  uni.setStorageSync(SEARCH_HISTORY_KEY, searchHistory.value)
}

function addToHistory(keyword: string) {
  const normalized = keyword.trim()
  if (!normalized) return

  searchHistory.value = [
    normalized,
    ...searchHistory.value.filter(item => item !== normalized)
  ].slice(0, 8)

  persistHistory()
}

function navigateToResult(keyword: string) {
  uni.navigateTo({
    url: `/pages/search/result?keyword=${encodeURIComponent(keyword)}`
  })
}

function handleSearch() {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    uni.showToast({
      title: '请输入搜索内容',
      icon: 'none'
    })
    return
  }

  addToHistory(keyword)
  showSuggestions.value = false
  navigateToResult(keyword)
}

function clearSearch() {
  searchKeyword.value = ''
  suggestions.value = []
  showSuggestions.value = false
}

function selectSuggestion(suggestion: string) {
  searchKeyword.value = suggestion
  handleSearch()
}

function selectHistory(history: string) {
  searchKeyword.value = history
  handleSearch()
}

function selectHotSearch(hot: string) {
  searchKeyword.value = hot
  handleSearch()
}

function searchFromPreset(keyword: string) {
  searchKeyword.value = keyword
  handleSearch()
}

function clearHistory() {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空搜索历史吗？',
    success: (res) => {
      if (!res.confirm) return
      searchHistory.value = []
      uni.removeStorageSync(SEARCH_HISTORY_KEY)
    }
  })
}

function handleCancel() {
  safeNavigateBack()
}

try {
  const history = uni.getStorageSync(SEARCH_HISTORY_KEY)
  searchHistory.value = Array.isArray(history) ? history : []
} catch (error) {
  console.error('获取搜索历史失败:', error)
}
</script>

<style lang="scss" scoped>
.search-shell {
  background:
    radial-gradient(circle at top left, rgba(255, 174, 111, 0.32), transparent 30%),
    radial-gradient(circle at top right, rgba(251, 146, 60, 0.18), transparent 22%),
    linear-gradient(180deg, #fff7ed 0%, #f8fafc 34%, #eef2ff 100%);
}

.search-page {
  min-height: 100vh;
  padding: 32rpx 24rpx 48rpx;
  box-sizing: border-box;
}

.hero-panel {
  position: relative;
  overflow: hidden;
  border-radius: 40rpx;
  padding: 32rpx;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 247, 237, 0.98)),
    #ffffff;
  box-shadow: 0 28rpx 70rpx rgba(161, 98, 7, 0.12);
}

.hero-panel::before {
  content: '';
  position: absolute;
  right: -80rpx;
  top: -90rpx;
  width: 260rpx;
  height: 260rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.22), rgba(251, 146, 60, 0));
}

.hero-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40rpx;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 237, 213, 0.82);
}

.hero-brand-text,
.hero-aside,
.hero-eyebrow,
.section-tag {
  letter-spacing: 0.18em;
}

.hero-brand-text {
  font-size: 20rpx;
  font-weight: 800;
  color: #9a3412;
}

.hero-aside {
  font-size: 20rpx;
  color: #94a3b8;
  font-weight: 700;
}

.hero-copy {
  position: relative;
  z-index: 1;
  margin-bottom: 36rpx;
}

.hero-eyebrow {
  display: block;
  margin-bottom: 16rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.hero-title {
  display: block;
  font-size: 58rpx;
  line-height: 1.15;
  font-weight: 800;
  color: #0f172a;
}

.hero-subtitle {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}

.search-card {
  position: relative;
  z-index: 1;
  padding: 24rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.88);
  border: 1rpx solid rgba(255, 237, 213, 0.9);
  backdrop-filter: blur(12rpx);
}

.search-input-wrap {
  display: flex;
  align-items: center;
  height: 100rpx;
  padding: 0 24rpx;
  border-radius: 26rpx;
  background: #f8fafc;
  border: 2rpx solid rgba(148, 163, 184, 0.12);
}

.search-icon {
  margin-right: 14rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #0f172a;
}

.clear-icon {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 20rpx;
}

.quick-action {
  padding: 18rpx 18rpx 20rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fff7ed 0%, #ffffff 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.16);
}

.quick-action-label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 18rpx;
  font-weight: 800;
  color: #c2410c;
  letter-spacing: 0.18em;
}

.quick-action-text {
  display: block;
  font-size: 24rpx;
  line-height: 1.5;
  color: #1e293b;
  font-weight: 700;
}

.search-submit {
  margin-top: 20rpx;
  height: 92rpx;
  border-radius: 24rpx;
  background: linear-gradient(135deg, #ff7a1a 0%, #ea580c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 18rpx 36rpx rgba(234, 88, 12, 0.25);
}

.search-submit-text {
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 800;
}

.content-section {
  margin-top: 28rpx;
}

.content-grid {
  display: grid;
  gap: 24rpx;
}

.content-card {
  padding: 28rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18rpx 48rpx rgba(15, 23, 42, 0.06);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #0f172a;
}

.section-tag {
  font-size: 18rpx;
  color: #94a3b8;
  font-weight: 700;
}

.section-action {
  font-size: 22rpx;
  font-weight: 700;
  color: #c2410c;
}

.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.suggestion-chip {
  min-width: calc(50% - 8rpx);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx 22rpx;
  border-radius: 22rpx;
  background: #fff7ed;
}

.chip-text {
  font-size: 24rpx;
  color: #7c2d12;
  font-weight: 700;
}

.hot-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
}

.hot-card {
  min-height: 132rpx;
  padding: 20rpx;
  border-radius: 24rpx;
  background: linear-gradient(145deg, #ffffff 0%, #fff7ed 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.15);
}

.hot-rank {
  display: block;
  margin-bottom: 14rpx;
  font-size: 20rpx;
  color: #c2410c;
  font-weight: 800;
  letter-spacing: 0.18em;
}

.hot-text {
  display: block;
  font-size: 28rpx;
  line-height: 1.4;
  color: #0f172a;
  font-weight: 800;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.history-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.9);
}

.history-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.history-icon-wrap {
  width: 58rpx;
  height: 58rpx;
  border-radius: 18rpx;
  background: #fff7ed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-text {
  flex: 1;
  font-size: 26rpx;
  color: #1e293b;
  font-weight: 700;
}

.empty-card {
  padding: 24rpx 0 8rpx;
}

.empty-title {
  display: block;
  font-size: 28rpx;
  color: #0f172a;
  font-weight: 800;
}

.empty-subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #94a3b8;
}
</style>
