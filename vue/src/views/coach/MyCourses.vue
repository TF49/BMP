<template>
  <div class="coach-my-courses">
    <div class="page-hero">
      <div>
        <h2 class="page-title">我的课程</h2>
        <p class="page-subtitle">从这里查看排课详情、学员名单，并快速进入对应预约明细</p>
      </div>
      <el-button @click="resetFilters">重置筛选</el-button>
    </div>

    <div class="toolbar-card">
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="课程名称"
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
    </div>

    <div v-if="loadFailed" class="error-state">
      <div class="error-title">课程列表加载失败</div>
      <div class="error-description">{{ errorMessage }}</div>
      <el-button type="primary" @click="loadList">重试</el-button>
    </div>

    <template v-else>
      <el-table v-if="list.length" v-loading="loading" :data="list" stripe class="course-table">
        <el-table-column prop="courseName" label="课程名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="courtName" label="场地" min-width="120" show-overflow-tooltip />
        <el-table-column label="时间" min-width="190">
          <template #default="{ row }">
            {{ formatCourseTime(row) }}
          </template>
        </el-table-column>
        <el-table-column label="人数" width="110">
          <template #default="{ row }">{{ formatStudentCount(row) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="formatStatusType(row.status, COURSE_STATUS_TYPE_MAP)" size="small">
              {{ formatStatusText(row.status, COURSE_STATUS_TEXT_MAP) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="action-group">
              <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
              <el-button link type="success" @click="goToBookings(row)">查看预约</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

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
  COURSE_STATUS_TEXT_MAP,
  COURSE_STATUS_TYPE_MAP,
  compareCourseTime,
  formatCourseTime,
  formatStatusText,
  formatStatusType,
  formatStudentCount
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
    errorMessage.value = error?.message || '请稍后重试'
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
    studentErrorMessage.value = error?.message || '请稍后重试'
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
    detailErrorMessage.value = error?.message || '请稍后重试'
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
    memberErrorMessage.value = error?.message || '请稍后重试'
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
    memberHistoryErrorMessage.value = error?.message || '请稍后重试'
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

.toolbar-card {
  padding: 16px;
  border-radius: 16px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
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

.course-table {
  width: 100%;
}

.action-group {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 8px;
  align-items: center;
}

.empty-wrap {
  min-height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: 16px;
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
