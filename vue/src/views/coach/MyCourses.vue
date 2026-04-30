<template>
  <div class="coach-my-courses">
    <div class="page-hero">
      <div class="hero-copy">
        <h2 class="page-title">我的课程</h2>
        <p class="page-subtitle">从这里查看排课详情、学员名单，并快速进入对应预约明细</p>
      </div>
      <div class="hero-actions">
        <div class="hero-chip">
          <span class="hero-chip-label">当前课程</span>
          <span class="hero-chip-value">{{ total }}</span>
        </div>
        <el-button @click="resetFilters">重置筛选</el-button>
      </div>
    </div>

    <div class="overview-grid">
      <div class="overview-card">
        <div class="overview-label">进行中</div>
        <div class="overview-value">{{ summaryStats.ongoing }}</div>
        <div class="overview-desc">当前正在带的课程</div>
      </div>
      <div class="overview-card">
        <div class="overview-label">报名中</div>
        <div class="overview-value">{{ summaryStats.open }}</div>
        <div class="overview-desc">可继续接收预约</div>
      </div>
      <div class="overview-card">
        <div class="overview-label">已结束</div>
        <div class="overview-value">{{ summaryStats.finished }}</div>
        <div class="overview-desc">已完成课节记录</div>
      </div>
    </div>

    <div class="toolbar-card">
      <div class="toolbar-header">
        <div>
          <div class="toolbar-title">轻筛选</div>
          <div class="toolbar-subtitle">按课程名、状态和日期快速收敛列表</div>
        </div>
        <div v-if="hasActiveFilters" class="active-filter-tip">已启用筛选</div>
      </div>
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索课程名称"
          clearable
          class="toolbar-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
        <el-select
          v-model="searchStatus"
          placeholder="全部状态"
          clearable
          class="toolbar-select"
          @change="handleFilterChange"
        >
          <el-option label="报名中" :value="1" />
          <el-option label="进行中" :value="2" />
          <el-option label="已结束" :value="3" />
          <el-option label="已取消" :value="0" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          class="toolbar-date"
          @change="handleFilterChange"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </div>
      <div v-if="hasActiveFilters" class="filter-summary">
        <span v-if="searchKeyword" class="filter-tag">关键词：{{ searchKeyword }}</span>
        <span v-if="searchStatus !== null" class="filter-tag">状态：{{ formatStatusText(searchStatus, COURSE_STATUS_TEXT_MAP) }}</span>
        <span v-if="dateRange.length === 2" class="filter-tag">{{ dateRange[0] }} 至 {{ dateRange[1] }}</span>
      </div>
    </div>

    <div v-if="loadFailed" class="error-state">
      <div class="error-title">课程列表加载失败</div>
      <div class="error-description">{{ errorMessage }}</div>
      <el-button type="primary" @click="loadList">重试</el-button>
    </div>

    <template v-else>
      <div v-if="list.length" v-loading="loading" class="course-list">
        <article v-for="row in list" :key="row.id" class="course-card">
          <div class="course-card-top">
            <div>
              <div class="course-name">{{ row.courseName || '-' }}</div>
              <div class="course-time">{{ formatCourseTime(row) }}</div>
            </div>
            <el-tag :type="formatStatusType(row.status, COURSE_STATUS_TYPE_MAP)" size="small">
              {{ formatStatusText(row.status, COURSE_STATUS_TEXT_MAP) }}
            </el-tag>
          </div>

          <div class="course-meta-grid">
            <div class="meta-block">
              <span class="meta-label">场地</span>
              <span class="meta-value">{{ row.courtName || '未安排场地' }}</span>
            </div>
            <div class="meta-block">
              <span class="meta-label">人数</span>
              <span class="meta-value">{{ formatStudentCount(row) }}</span>
            </div>
          </div>

          <div class="course-card-actions">
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <el-button link type="success" @click="goToBookings(row)">查看预约</el-button>
          </div>
        </article>
      </div>

      <div v-else-if="!loading" class="empty-wrap">
        <EmptyState
          type="document"
          :title="hasActiveFilters ? '当前筛选条件下暂无课程' : '暂无课程'"
          :description="hasActiveFilters ? '试试调整状态、日期或关键词筛选。' : '当前还没有分配到您的课程。'"
        />
      </div>
    </template>

    <div v-if="!loadFailed && total > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="handleSizeChange"
      />
    </div>

    <CourseDetailDrawer
      v-model="detailVisible"
      :loading="detailLoading"
      :detail="detail"
      :load-failed="detailLoadFailed"
      :error-message="detailErrorMessage"
      :students="courseStudents"
      :student-loading="studentLoading"
      :student-load-failed="studentLoadFailed"
      :student-error-message="studentErrorMessage"
      @reload-detail="reloadDetail"
      @reload-students="reloadStudents"
      @view-bookings="goToBookings"
      @view-member="openMemberHistory"
    />

    <MemberHistoryDrawer
      v-model="memberHistoryVisible"
      :member-loading="memberLoading"
      :member-load-failed="memberLoadFailed"
      :member-error-message="memberErrorMessage"
      :member="memberInfo"
      :history-loading="memberHistoryLoading"
      :history-load-failed="memberHistoryLoadFailed"
      :history-error-message="memberHistoryErrorMessage"
      :history="memberHistoryList"
      :history-page="memberHistoryPage"
      :history-size="memberHistorySize"
      :history-total="memberHistoryTotal"
      @reload-member="reloadMemberInfo"
      @reload-history="reloadMemberHistory"
      @history-page-change="handleMemberHistoryPageChange"
      @history-size-change="handleMemberHistorySizeChange"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import EmptyState from '@/components/common/EmptyState.vue'
import { getCourseInfo, getMyCourses } from '@/api/course'
import { getBookingsForCoach } from '@/api/courseBooking'
import { getMemberConsumeList, getMemberInfo } from '@/api/member'
import CourseDetailDrawer from './components/CourseDetailDrawer.vue'
import MemberHistoryDrawer from './components/MemberHistoryDrawer.vue'
import {
  COACH_UNBOUND_MESSAGE,
  COURSE_STATUS_TEXT_MAP,
  COURSE_STATUS_TYPE_MAP,
  compareCourseTime,
  formatCourseTime,
  formatStatusText,
  formatStatusType,
  formatStudentCount,
  isCoachUnboundError
} from './coachViewUtils'

const router = useRouter()

const loading = ref(false)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const searchKeyword = ref('')
const searchStatus = ref(null)
const dateRange = ref([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const detailLoadFailed = ref(false)
const detailErrorMessage = ref('请稍后重试')
const detailCourseId = ref(null)

const courseStudents = ref([])
const studentLoading = ref(false)
const studentLoadFailed = ref(false)
const studentErrorMessage = ref('请稍后重试')

const memberHistoryVisible = ref(false)
const activeMemberId = ref(null)
const memberLoading = ref(false)
const memberLoadFailed = ref(false)
const memberErrorMessage = ref('请稍后重试')
const memberInfo = ref(null)
const memberHistoryLoading = ref(false)
const memberHistoryLoadFailed = ref(false)
const memberHistoryErrorMessage = ref('请稍后重试')
const memberHistoryList = ref([])
const memberHistoryTotal = ref(0)
const memberHistoryPage = ref(1)
const memberHistorySize = ref(10)

const hasActiveFilters = computed(() => {
  return Boolean(searchKeyword.value?.trim() || searchStatus.value != null || (dateRange.value?.length === 2))
})

const summaryStats = computed(() => {
  const data = list.value || []
  return {
    open: data.filter((item) => Number(item.status) === 1).length,
    ongoing: data.filter((item) => Number(item.status) === 2).length,
    finished: data.filter((item) => Number(item.status) === 3).length
  }
})

const buildParams = () => {
  const params = {
    page: page.value,
    size: size.value,
    status: searchStatus.value ?? undefined,
    keyword: searchKeyword.value?.trim() || undefined
  }
  if (dateRange.value?.length === 2) {
    params.startTime = `${dateRange.value[0]} 00:00:00`
    params.endTime = `${dateRange.value[1]} 23:59:59`
  }
  return params
}

const loadList = async () => {
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'
  try {
    const res = await getMyCourses(buildParams())
    if (res?.code === 200 && res?.data) {
      list.value = res.data.data ?? []
      total.value = res.data.total ?? 0
      return
    }
    throw new Error(res?.message || '未获取到课程数据')
  } catch (error) {
    list.value = []
    total.value = 0
    loadFailed.value = true
    errorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadList()
}

const handleFilterChange = () => {
  page.value = 1
  loadList()
}

const handleSizeChange = () => {
  page.value = 1
  loadList()
}

const resetFilters = () => {
  searchKeyword.value = ''
  searchStatus.value = null
  dateRange.value = []
  page.value = 1
  loadList()
}

const loadCourseStudents = async (courseId) => {
  if (!courseId) {
    courseStudents.value = []
    return
  }
  studentLoading.value = true
  studentLoadFailed.value = false
  studentErrorMessage.value = '请稍后重试'
  try {
    const res = await getBookingsForCoach({
      page: 1,
      size: 100,
      courseId
    })
    if (res?.code === 200 && res?.data) {
      courseStudents.value = [...(res.data.data ?? [])].sort(compareCourseTime)
      return
    }
    throw new Error(res?.message || '未获取到学员名单')
  } catch (error) {
    courseStudents.value = []
    studentLoadFailed.value = true
    studentErrorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    studentLoading.value = false
  }
}

const loadDetail = async (courseId) => {
  detailLoading.value = true
  detailLoadFailed.value = false
  detail.value = null
  detailErrorMessage.value = '请稍后重试'
  try {
    const [detailRes] = await Promise.all([
      getCourseInfo(courseId),
      loadCourseStudents(courseId)
    ])
    if (detailRes?.code === 200) {
      detail.value = detailRes.data
      return
    }
    throw new Error(detailRes?.message || '课程详情获取失败')
  } catch (error) {
    detailLoadFailed.value = true
    detailErrorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    detailLoading.value = false
  }
}

const openDetail = async (row) => {
  detailCourseId.value = row?.id ?? null
  detailVisible.value = true
  if (!detailCourseId.value) {
    detailLoadFailed.value = true
    detailErrorMessage.value = '课程ID缺失，无法加载详情'
    return
  }
  await loadDetail(detailCourseId.value)
}

const reloadDetail = async () => {
  if (!detailCourseId.value) {
    ElMessage.error('课程ID缺失，无法重新加载')
    return
  }
  await loadDetail(detailCourseId.value)
}

const reloadStudents = async () => {
  if (!detailCourseId.value) {
    ElMessage.error('课程ID缺失，无法加载学员名单')
    return
  }
  await loadCourseStudents(detailCourseId.value)
}

const goToBookings = (row) => {
  if (!row?.id) {
    ElMessage.error('课程ID缺失，无法查看预约')
    return
  }
  router.push({
    path: '/coach/bookings',
    query: {
      courseId: String(row.id),
      courseName: row.courseName || ''
    }
  })
}

const loadMemberInfo = async () => {
  if (!activeMemberId.value) return
  memberLoading.value = true
  memberLoadFailed.value = false
  memberErrorMessage.value = '请稍后重试'
  try {
    const res = await getMemberInfo(activeMemberId.value)
    if (res?.code === 200) {
      memberInfo.value = res.data
      return
    }
    throw new Error(res?.message || '学员信息获取失败')
  } catch (error) {
    memberInfo.value = null
    memberLoadFailed.value = true
    memberErrorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    memberLoading.value = false
  }
}

const loadMemberHistory = async () => {
  if (!activeMemberId.value) return
  memberHistoryLoading.value = true
  memberHistoryLoadFailed.value = false
  memberHistoryErrorMessage.value = '请稍后重试'
  try {
    const res = await getMemberConsumeList(activeMemberId.value, {
      page: memberHistoryPage.value,
      size: memberHistorySize.value
    })
    if (res?.code === 200 && res?.data) {
      memberHistoryList.value = res.data.data ?? []
      memberHistoryTotal.value = res.data.total ?? 0
      return
    }
    throw new Error(res?.message || '历史记录获取失败')
  } catch (error) {
    memberHistoryList.value = []
    memberHistoryTotal.value = 0
    memberHistoryLoadFailed.value = true
    memberHistoryErrorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    memberHistoryLoading.value = false
  }
}

const openMemberHistory = async (memberId) => {
  if (!memberId) {
    ElMessage.error('学员ID缺失，无法查看历史记录')
    return
  }
  activeMemberId.value = memberId
  memberHistoryVisible.value = true
  memberHistoryPage.value = 1
  await Promise.all([loadMemberInfo(), loadMemberHistory()])
}

const reloadMemberInfo = async () => {
  await loadMemberInfo()
}

const reloadMemberHistory = async () => {
  await loadMemberHistory()
}

const handleMemberHistoryPageChange = (nextPage) => {
  memberHistoryPage.value = nextPage
  loadMemberHistory()
}

const handleMemberHistorySizeChange = (nextSize) => {
  memberHistorySize.value = nextSize
  memberHistoryPage.value = 1
  loadMemberHistory()
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.coach-my-courses {
  max-width: 100%;
}

.page-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-chip {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
  min-width: 120px;
  padding: 10px 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--el-fill-color-light) 0%, var(--color-card-bg, #fff) 100%);
  border: 1px solid color-mix(in srgb, var(--color-primary, #2563EB) 12%, var(--color-border, #e2e8f0));
}

.hero-chip-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.hero-chip-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary, var(--el-color-primary, #2563EB));
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.page-subtitle {
  color: var(--el-text-color-secondary);
  margin: 0;
  font-size: 14px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.overview-card {
  padding: 18px;
  border-radius: 18px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.overview-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
  border-color: color-mix(in srgb, var(--color-primary, #2563EB) 18%, var(--color-border, #e2e8f0));
}

.overview-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.overview-value {
  margin-top: 10px;
  font-size: 30px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.overview-desc {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.toolbar-card {
  padding: 16px;
  border-radius: 16px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
  margin-bottom: 16px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.toolbar-card:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--color-primary, #2563EB) 18%, var(--color-border, #e2e8f0));
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
}

.toolbar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.toolbar-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.active-filter-tip {
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary, var(--el-color-primary, #2563EB));
  background: color-mix(in srgb, var(--color-primary, #2563EB) 8%, #ffffff);
  border: 1px solid color-mix(in srgb, var(--color-primary, #2563EB) 16%, var(--color-border, #e2e8f0));
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filter-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
}

.toolbar-input {
  width: 220px;
}

.toolbar-select {
  width: 140px;
}

.toolbar-date {
  width: 320px;
}

.course-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.course-card {
  padding: 18px;
  border-radius: 18px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.course-card:hover {
  transform: translateY(-4px);
  border-color: color-mix(in srgb, var(--color-primary, #2563EB) 18%, var(--color-border, #e2e8f0));
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.10), 0 4px 12px color-mix(in srgb, var(--color-primary, #2563EB) 10%, transparent);
}

.course-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.course-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.course-time {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-primary, var(--el-color-primary, #2563EB));
  font-weight: 600;
}

.course-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.meta-block {
  padding: 12px 14px;
  border-radius: 14px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
}

.meta-label {
  display: block;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.meta-value {
  display: block;
  margin-top: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.course-card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 16px;
}

.empty-wrap {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: 16px;
  transition: all 0.3s ease;
}

.empty-wrap:hover {
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.06);
}

.error-state {
  padding: 24px;
  border-radius: 16px;
  background: #fff7f7;
  border: 1px solid #f5c2c7;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
  transition: box-shadow 0.2s ease;
}

.error-state:hover {
  box-shadow: 0 6px 18px rgba(194, 65, 12, 0.08);
}

.error-title {
  font-size: 16px;
  font-weight: 600;
  color: #c2410c;
}

.error-description {
  font-size: 14px;
  color: #7c2d12;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 900px) {
  .page-hero {
    flex-direction: column;
  }

  .hero-actions {
    width: 100%;
    justify-content: space-between;
  }

  .overview-grid,
  .course-list {
    grid-template-columns: 1fr;
  }

  .toolbar-input,
  .toolbar-select,
  .toolbar-date {
    width: 100%;
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
