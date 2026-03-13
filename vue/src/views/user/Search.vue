<template>
  <div class="search-container">
    <!-- 头部：标题 + 搜索 -->
    <div class="page-header">
      <div class="page-title-wrap">
        <h1 class="page-title">搜索</h1>
        <p class="page-subtitle">快速查找场馆、课程、赛事与器材</p>
      </div>
    </div>

    <!-- 搜索框（卡片化） -->
    <div class="search-card" :class="{ 'is-loading': loading }">
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索场馆、课程、赛事或器材..."
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" class="search-btn" @click="handleSearch" :loading="loading">
          搜索
        </el-button>
      </div>
      <div class="search-hint" v-if="!hasSearched">
        支持回车搜索；点击下方标签可快速填充关键词。
      </div>
    </div>

    <!-- 搜索建议和历史 -->
    <div v-if="!hasSearched && !loading" class="search-suggestions">
      <!-- 搜索历史 -->
      <div v-if="searchHistory.length > 0" class="history-section">
        <div class="section-header">
          <div class="section-title">
            <span class="section-title-text">搜索历史</span>
            <span class="section-title-sub">最近 10 条</span>
          </div>
          <el-button text type="primary" class="link-btn" @click="clearHistory">清空</el-button>
        </div>
        <div class="history-tags">
          <el-tag
            v-for="(item, index) in searchHistory"
            :key="index"
            class="history-tag"
            @click="selectHistory(item)"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>

      <!-- 热门搜索 -->
      <div class="hot-section">
        <div class="section-header">
          <div class="section-title">
            <span class="section-title-text">热门搜索</span>
            <span class="section-title-sub">大家都在搜</span>
          </div>
        </div>
        <div class="hot-tags">
          <el-tag
            v-for="(item, index) in hotSearches"
            :key="index"
            class="hot-tag"
            @click="selectHotSearch(item)"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="hasSearched" class="search-results">
      <div class="results-header">
        <div class="results-meta">
          <span class="results-count">找到 {{ totalResults }} 条结果</span>
          <span class="results-keyword" v-if="searchKeyword.trim()">关键词：{{ searchKeyword.trim() }}</span>
        </div>
      </div>

      <!-- 加载态 -->
      <div v-if="loading" class="results-loading">
        <el-skeleton :rows="0" animated>
          <template #template>
            <div class="skeleton-grid">
              <div class="skeleton-card" v-for="i in 6" :key="i">
                <div class="skeleton-icon"></div>
                <div class="skeleton-lines">
                  <div class="skeleton-line w-70"></div>
                  <div class="skeleton-line w-55"></div>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>

      <!-- 场馆结果 -->
      <div v-if="!loading && venueResults.length > 0" class="result-section">
        <h3 class="result-category">场地</h3>
        <el-card
          v-for="venue in venueResults"
          :key="'venue-' + venue.id"
          class="result-card"
          shadow="hover"
          @click="goToVenueDetail(venue)"
        >
          <div class="result-content">
            <el-icon class="result-icon venue-icon"><Location /></el-icon>
            <div class="result-info">
              <div class="result-title">{{ venue.venueName || venue.name }}</div>
              <div class="result-subtitle">
                {{ venue.address || venue.location }} • ¥{{ venue.hourlyPrice || venue.price }}/小时
              </div>
            </div>
            <div class="result-cta" aria-hidden="true">查看</div>
          </div>
        </el-card>
      </div>

      <!-- 课程结果 -->
      <div v-if="!loading && courseResults.length > 0" class="result-section">
        <h3 class="result-category">课程</h3>
        <el-card
          v-for="course in courseResults"
          :key="'course-' + course.id"
          class="result-card"
          shadow="hover"
          @click="goToCourseDetail(course)"
        >
          <div class="result-content">
            <el-icon class="result-icon course-icon"><Document /></el-icon>
            <div class="result-info">
              <div class="result-title">{{ course.courseName || course.name }}</div>
              <div class="result-subtitle">
                教练: {{ course.coachName }} • ¥{{ course.coursePrice || course.price }}/课时
              </div>
            </div>
            <div class="result-cta" aria-hidden="true">查看</div>
          </div>
        </el-card>
      </div>

      <!-- 赛事结果 -->
      <div v-if="!loading && tournamentResults.length > 0" class="result-section">
        <h3 class="result-category">赛事</h3>
        <el-card
          v-for="tournament in tournamentResults"
          :key="'tournament-' + tournament.id"
          class="result-card"
          shadow="hover"
          @click="goToTournamentDetail(tournament)"
        >
          <div class="result-content">
            <el-icon class="result-icon tournament-icon"><Trophy /></el-icon>
            <div class="result-info">
              <div class="result-title">{{ tournament.tournamentName || tournament.name }}</div>
              <div class="result-subtitle">
                {{ tournament.tournamentStart || tournament.date }} • 报名费 ¥{{ tournament.entryFee || tournament.fee }}
              </div>
            </div>
            <div class="result-cta" aria-hidden="true">查看</div>
          </div>
        </el-card>
      </div>

      <!-- 器材结果 -->
      <div v-if="!loading && equipmentResults.length > 0" class="result-section">
        <h3 class="result-category">器材</h3>
        <el-card
          v-for="equipment in equipmentResults"
          :key="'equipment-' + equipment.id"
          class="result-card"
          shadow="hover"
          @click="goToEquipmentDetail(equipment)"
        >
          <div class="result-content">
            <el-icon class="result-icon equipment-icon"><Box /></el-icon>
            <div class="result-info">
              <div class="result-title">{{ equipment.equipmentName || equipment.name }}</div>
              <div class="result-subtitle">
                {{ equipment.equipmentType }} • 租借价 ¥{{ equipment.rentalPrice || equipment.price }}/天
              </div>
            </div>
            <div class="result-cta" aria-hidden="true">查看</div>
          </div>
        </el-card>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && totalResults === 0"
        description="没有找到相关结果"
        :image-size="120"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Location, Document, Trophy, Box } from '@element-plus/icons-vue'
import { searchVenues, searchCourses, searchTournaments, searchEquipment } from '@/api/search'

const router = useRouter()

const searchKeyword = ref('')
const loading = ref(false)
const hasSearched = ref(false)
const searchHistory = ref([])
const hotSearches = ref(['奥体中心', '羽毛球培训', '青少年训练营', '周末比赛'])

const venueResults = ref([])
const courseResults = ref([])
const tournamentResults = ref([])
const equipmentResults = ref([])

const totalResults = computed(() => {
  return venueResults.value.length + courseResults.value.length + 
         tournamentResults.value.length + equipmentResults.value.length
})

// 加载搜索历史
const loadSearchHistory = () => {
  try {
    const history = localStorage.getItem('search_history')
    if (history) {
      searchHistory.value = JSON.parse(history)
    }
  } catch (error) {
    console.error('加载搜索历史失败:', error)
  }
}

// 保存搜索历史
const saveSearchHistory = (keyword) => {
  if (!keyword.trim()) return
  
  if (!searchHistory.value.includes(keyword)) {
    searchHistory.value.unshift(keyword)
    if (searchHistory.value.length > 10) {
      searchHistory.value.pop()
    }
    localStorage.setItem('search_history', JSON.stringify(searchHistory.value))
  }
}

// 清空搜索历史
const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('search_history')
}

// 选择历史记录
const selectHistory = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 选择热门搜索
const selectHotSearch = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

// 执行搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    return
  }

  loading.value = true
  hasSearched.value = true
  saveSearchHistory(searchKeyword.value.trim())

  try {
    const keyword = searchKeyword.value.trim()
    const [venues, courses, tournaments, equipment] = await Promise.all([
      searchVenues({ keyword }).catch(() => ({ data: [] })),
      searchCourses({ keyword }).catch(() => ({ data: [] })),
      searchTournaments({ keyword }).catch(() => ({ data: [] })),
      searchEquipment({ keyword }).catch(() => ({ data: [] }))
    ])

    venueResults.value = venues.data || []
    courseResults.value = courses.data || []
    tournamentResults.value = tournaments.data || []
    equipmentResults.value = equipment.data || []
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 清空搜索
const handleClear = () => {
  hasSearched.value = false
  venueResults.value = []
  courseResults.value = []
  tournamentResults.value = []
  equipmentResults.value = []
}

// 跳转到详情页
const goToVenueDetail = (venue) => {
  router.push({ path: '/user/booking', query: { venueId: venue.id } })
}

const goToCourseDetail = (course) => {
  router.push({ path: '/user/course', query: { courseId: course.id } })
}

const goToTournamentDetail = (tournament) => {
  router.push({ path: '/user/tournament', query: { tournamentId: tournament.id } })
}

const goToEquipmentDetail = (equipment) => {
  router.push({ path: '/user/equipment', query: { equipmentId: equipment.id } })
}

onMounted(() => {
  loadSearchHistory()
})
</script>

<style scoped lang="scss">
.search-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 减少动画（尊重用户偏好） */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

.page-header {
  margin-bottom: 16px;
  padding: 10px 0 8px;
  position: relative;
}

.page-title {
  margin: 0 0 6px 0;
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary, #1E293B);
  letter-spacing: -0.2px;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
}

.search-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 18px;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;

  &.is-loading {
    opacity: 0.96;
  }
}

.search-card:hover {
  border-color: var(--color-primary-light, #93C5FD);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
}

.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  flex: 1;
}

.search-btn {
  height: 40px;
  border-radius: 12px;
  padding: 0 18px;
  font-weight: 600;
}

.search-hint {
  margin-top: 10px;
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
  padding-left: 2px;
}

.search-suggestions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  .history-section,
  .hot-section {
    background: var(--color-card-bg, #FFFFFF);
    border: 1px solid var(--color-border, #E2E8F0);
    border-radius: 18px;
    padding: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .section-title {
    display: flex;
    align-items: baseline;
    gap: 10px;
  }

  .section-title-text {
    font-size: 15px;
    font-weight: 700;
    color: var(--color-text-primary, #1E293B);
  }

  .section-title-sub {
    font-size: 12px;
    color: var(--color-text-secondary, #64748B);
  }

  .history-tags,
  .hot-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .history-tag,
  .hot-tag {
    cursor: pointer;
    user-select: none;
    transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
    border-radius: 999px;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 10px 18px rgba(0, 0, 0, 0.08);
    }
  }
}

.search-results {
  .results-header {
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--color-border, #E2E8F0);
  }

  .results-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  .results-count {
    font-size: 13px;
    color: var(--color-text-secondary, #64748B);
    font-weight: 500;
  }

  .results-keyword {
    font-size: 12px;
    color: var(--color-text-secondary, #64748B);
    padding: 4px 10px;
    border: 1px solid var(--color-border, #E2E8F0);
    border-radius: 999px;
    background: var(--color-background, #F8FAFC);
  }

  .result-section {
    margin-bottom: 22px;
  }

  .result-category {
    font-size: 16px;
    font-weight: 700;
    color: var(--color-text-primary, #1E293B);
    margin: 0 0 12px 0;
  }

  .result-card {
    margin-bottom: 12px;
    cursor: pointer;
    border-radius: 16px;
    border: 1px solid var(--color-border, #E2E8F0);
    transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
    
    &:hover {
      transform: translateY(-2px);
      border-color: var(--color-primary-light, #93C5FD);
      box-shadow: 0 16px 32px rgba(0, 0, 0, 0.10);
    }
  }

  .result-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .result-icon {
    font-size: 32px;
    
    &.venue-icon {
      color: #409eff;
    }
    
    &.course-icon {
      color: #67c23a;
    }
    
    &.tournament-icon {
      color: #e6a23c;
    }
    
    &.equipment-icon {
      color: #f56c6c;
    }
  }

  .result-info {
    flex: 1;
  }

  .result-title {
    font-size: 16px;
    font-weight: 700;
    color: var(--color-text-primary, #1E293B);
    margin-bottom: 6px;
  }

  .result-subtitle {
    font-size: 13px;
    color: var(--color-text-secondary, #64748B);
    line-height: 1.4;
  }

  .result-cta {
    font-size: 12px;
    font-weight: 700;
    color: var(--color-primary, #2563EB);
    padding: 6px 10px;
    border-radius: 999px;
    background: rgba(37, 99, 235, 0.08);
    border: 1px solid rgba(37, 99, 235, 0.18);
    flex-shrink: 0;
  }
}

/* 加载态 skeleton */
.results-loading {
  padding: 8px 0 4px;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.skeleton-card {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid var(--color-border, #E2E8F0);
  background: var(--color-card-bg, #FFFFFF);
}

.skeleton-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
}

.skeleton-lines {
  flex: 1;
}

.skeleton-line {
  height: 10px;
  border-radius: 999px;
  background: #EEF2F7;
  margin-bottom: 10px;

  &.w-70 { width: 70%; }
  &.w-55 { width: 55%; margin-bottom: 0; }
}

@media (max-width: 768px) {
  .search-suggestions {
    grid-template-columns: 1fr;
  }

  .search-card {
    padding: 14px;
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-btn {
    width: 100%;
  }

  .skeleton-grid {
    grid-template-columns: 1fr;
  }
}
</style>
