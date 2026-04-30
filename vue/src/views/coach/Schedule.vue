<template>
  <div class="coach-schedule">
    <div class="page-hero">
      <div>
        <h2 class="page-title">我的课表</h2>
        <p class="page-subtitle">按天或按周查看课程安排，快速进入详情、学员名单与预约管理</p>
      </div>
      <el-button @click="goToToday">回到今天</el-button>
    </div>

    <div class="stats-grid">
      <div class="stat-card stat-today">
        <div class="stat-label">今天课程</div>
        <div class="stat-value">{{ todaySummary.total }}</div>
        <div class="stat-desc">今日排课总数</div>
      </div>
      <div class="stat-card stat-ongoing">
        <div class="stat-label">进行中课程</div>
        <div class="stat-value">{{ todaySummary.ongoing }}</div>
        <div class="stat-desc">今天正在上课</div>
      </div>
      <div class="stat-card stat-upcoming">
        <div class="stat-label">即将开始课程</div>
        <div class="stat-value">{{ todaySummary.upcoming }}</div>
        <div class="stat-desc">今天待开始课节</div>
      </div>
    </div>

    <div class="toolbar-card">
      <div class="toolbar-top">
        <el-segmented v-model="viewMode" :options="viewModeOptions" @change="handleViewModeChange" />
        <div class="toolbar-actions">
          <template v-if="viewMode === 'day'">
            <el-button @click="shiftDay(-1)">前一天</el-button>
            <el-date-picker
              v-model="selectedDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="date-picker"
              @change="handleSelectedDateChange"
            />
            <el-button @click="shiftDay(1)">后一天</el-button>
          </template>
          <template v-else>
            <el-button @click="shiftWeek(-1)">前一周</el-button>
            <div class="range-chip">{{ weekLabel }}</div>
            <el-button @click="shiftWeek(1)">下一周</el-button>
          </template>
        </div>
      </div>

      <div class="toolbar-bottom">
        <div class="range-summary">
          <span class="range-summary-label">{{ viewMode === 'day' ? '当前日期' : '当前周' }}</span>
          <span class="range-summary-value">{{ currentRangeLabel }}</span>
        </div>
        <el-button type="primary" @click="reloadSchedule">刷新课表</el-button>
      </div>
    </div>

    <div v-if="loadFailed" class="error-state">
      <div class="error-title">课表加载失败</div>
      <div class="error-description">{{ errorMessage }}</div>
      <el-button type="primary" @click="loadSchedule">重试</el-button>
    </div>

    <template v-else>
      <div v-if="upcomingCourses.length" class="upcoming-section">
        <div class="section-header">
          <h3 class="section-title">即将开始</h3>
          <span class="section-subtitle">优先关注今天最近开始的课程</span>
        </div>
        <div class="upcoming-list">
          <button
            v-for="course in upcomingCourses"
            :key="`upcoming-${course.id}`"
            type="button"
            class="upcoming-card"
            @click="openDetail(course)"
          >
            <div class="upcoming-time">{{ formatTimeRange(course) }}</div>
            <div class="upcoming-name">{{ course.courseName || '-' }}</div>
            <div class="upcoming-meta">{{ course.courtName || '未安排场地' }} · {{ formatStudentCount(course) }}</div>
          </button>
        </div>
      </div>

      <div v-if="groupedCourses.length" class="schedule-groups">
        <section
          v-for="group in groupedCourses"
          :key="group.date"
          class="schedule-group"
          :class="{ 'is-today-group': group.date === todayKey }"
        >
          <div class="group-header">
            <div>
              <div class="group-date">{{ formatGroupDate(group.date) }}</div>
              <div class="group-subtitle">{{ group.label }} · {{ group.items.length }} 节课程</div>
            </div>
            <el-tag v-if="group.date === todayKey" type="danger" size="small">今天</el-tag>
          </div>

          <div class="course-card-list">
            <article
              v-for="course in group.items"
              :key="course.id"
              class="course-card"
              :class="{
                'is-ongoing': isTodayCourseOngoing(course),
                'is-upcoming': isUpcomingCourse(course)
              }"
            >
              <div class="course-main">
                <div class="course-time">{{ formatTimeRange(course) }}</div>
                <div class="course-name-row">
                  <h4 class="course-name">{{ course.courseName || '-' }}</h4>
                  <el-tag :type="formatStatusType(course.status, COURSE_STATUS_TYPE_MAP)" size="small">
                    {{ formatStatusText(course.status, COURSE_STATUS_TEXT_MAP) }}
                  </el-tag>
                </div>
                <div class="course-meta">
                  <span>{{ course.courtName || '未安排场地' }}</span>
                  <span>{{ formatStudentCount(course) }}</span>
                </div>
              </div>
              <div class="course-actions">
                <el-button link type="primary" @click="openDetail(course)">查看详情</el-button>
                <el-button link type="success" @click="goToBookings(course)">查看预约</el-button>
              </div>
            </article>
          </div>
        </section>
      </div>

      <div v-else-if="!loading" class="empty-wrap">
        <EmptyState
          type="data"
          :title="viewMode === 'day' ? '今日无课' : '本周暂无课程安排'"
          :description="viewMode === 'day' ? '换个日期看看，或稍后刷新课表。' : '切换到其他周，查看不同时间段的安排。'"
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
        @current-change="loadSchedule"
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
  WEEK_LABELS,
  addDays,
  compareCourseTime,
  formatCourseTime,
  formatDateKey,
  formatGroupDate,
  formatStatusText,
  formatStatusType,
  formatStudentCount,
  formatTimeRange,
  getWeekStart,
  getWeekdayLabel,
  isCoachUnboundError,
  isTodayCourseOngoing,
  isUpcomingCourse
} from './coachViewUtils'

const route = useRoute()
const router = useRouter()

const MAX_SUMMARY_SIZE = 100
const viewModeOptions = [
  { label: '按天', value: 'day' },
  { label: '按周', value: 'week' }
]

const loading = ref(false)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const viewMode = ref('week')
const selectedDate = ref('')
const weekStartDate = ref('')
const todaySummary = ref({ total: 0, ongoing: 0, upcoming: 0 })

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

const todayKey = computed(() => formatDateKey(new Date()))
const currentRange = computed(() => {
  if (viewMode.value === 'day') {
    return {
      start: selectedDate.value,
      end: selectedDate.value
    }
  }
  const start = weekStartDate.value
  return { start, end: addDays(start, 6) }
})

const weekLabel = computed(() => `${currentRange.value.start} 至 ${currentRange.value.end}`)
const currentRangeLabel = computed(() => {
  return viewMode.value === 'day'
    ? formatGroupDate(selectedDate.value)
    : `${formatGroupDate(currentRange.value.start)} - ${formatGroupDate(currentRange.value.end)}`
})

const groupedCourses = computed(() => {
  const map = new Map()
  list.value.forEach((course) => {
    const key = course.courseDate || '未知日期'
    if (!map.has(key)) {
      map.set(key, {
        date: key,
        label: getWeekdayLabel(key),
        items: []
      })
    }
    map.get(key).items.push(course)
  })
  return Array.from(map.values()).map((group) => ({
    ...group,
    items: [...group.items].sort(compareCourseTime)
  }))
})

const upcomingCourses = computed(() => {
  const now = new Date()
  return list.value
    .filter((course) => isUpcomingCourse(course, now))
    .sort(compareCourseTime)
    .slice(0, 3)
})

const buildScheduleParams = () => ({
  page: page.value,
  size: size.value,
  startTime: `${currentRange.value.start} 00:00:00`,
  endTime: `${currentRange.value.end} 23:59:59`
})

const applyRouteViewState = () => {
  const queryView = typeof route.query.view === 'string' ? route.query.view : ''
  const queryDate = typeof route.query.date === 'string' ? route.query.date : ''
  const today = todayKey.value
  const normalizedDate = queryDate || today

  if (queryView === 'day') {
    viewMode.value = 'day'
    selectedDate.value = normalizedDate
    weekStartDate.value = getWeekStart(normalizedDate)
    return
  }

  viewMode.value = 'week'
  selectedDate.value = normalizedDate
  weekStartDate.value = getWeekStart(normalizedDate)
}

const loadTodaySummary = async () => {
  const today = todayKey.value
  try {
    const [todayCountRes, todayListRes] = await Promise.all([
      getMyCourses({ page: 1, size: 1, startTime: `${today} 00:00:00`, endTime: `${today} 23:59:59` }),
      getMyCourses({ page: 1, size: MAX_SUMMARY_SIZE, startTime: `${today} 00:00:00`, endTime: `${today} 23:59:59` })
    ])
    const todayList = todayListRes?.data?.data ?? []
    const now = new Date()
    todaySummary.value = {
      total: todayCountRes?.data?.total ?? 0,
      ongoing: todayList.filter((course) => isTodayCourseOngoing(course, now)).length,
      upcoming: todayList.filter((course) => isUpcomingCourse(course, now)).length
    }
  } catch (_) {
    todaySummary.value = { total: 0, ongoing: 0, upcoming: 0 }
  }
}

const loadSchedule = async () => {
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'
  try {
    const [scheduleRes] = await Promise.all([
      getMyCourses(buildScheduleParams()),
      loadTodaySummary()
    ])
    if (scheduleRes?.code === 200 && scheduleRes?.data) {
      list.value = scheduleRes.data.data ?? []
      total.value = scheduleRes.data.total ?? 0
      return
    }
    throw new Error(scheduleRes?.message || '未获取到课表数据')
  } catch (error) {
    list.value = []
    total.value = 0
    loadFailed.value = true
    errorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    loading.value = false
  }
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

const openDetail = async (course) => {
  detailCourseId.value = course?.id ?? null
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

const goToBookings = (course) => {
  if (!course?.id) {
    ElMessage.error('课程ID缺失，无法查看预约')
    return
  }
  router.push({
    path: '/coach/bookings',
    query: {
      courseId: String(course.id),
      courseName: course.courseName || ''
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

const reloadSchedule = () => {
  page.value = 1
  loadSchedule()
}

const handleViewModeChange = () => {
  page.value = 1
  loadSchedule()
}

const handleSelectedDateChange = () => {
  page.value = 1
  loadSchedule()
}

const handleSizeChange = () => {
  page.value = 1
  loadSchedule()
}

const shiftDay = (offset) => {
  selectedDate.value = addDays(selectedDate.value, offset)
  page.value = 1
  loadSchedule()
}

const shiftWeek = (offset) => {
  weekStartDate.value = addDays(weekStartDate.value, offset * 7)
  page.value = 1
  loadSchedule()
}

const goToToday = () => {
  const today = todayKey.value
  selectedDate.value = today
  weekStartDate.value = getWeekStart(today)
  page.value = 1
  loadSchedule()
}

onMounted(() => {
  applyRouteViewState()
  loadSchedule()
})

watch(
  () => [route.query.view, route.query.date],
  (next, prev) => {
    if (next[0] === prev?.[0] && next[1] === prev?.[1]) return
    applyRouteViewState()
    page.value = 1
    loadSchedule()
  }
)
</script>

<style scoped>
.coach-schedule {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: 18px;
  padding: 20px 22px;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.stat-value {
  margin-top: 8px;
  font-size: 32px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.stat-desc {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.stat-today {
  border-color: rgba(79, 172, 254, 0.25);
}

.stat-ongoing {
  border-color: rgba(250, 112, 154, 0.25);
}

.stat-upcoming {
  border-color: rgba(67, 233, 123, 0.25);
}

.toolbar-card {
  padding: 16px;
  border-radius: 16px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
  margin-bottom: 16px;
}

.toolbar-top,
.toolbar-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar-bottom {
  margin-top: 14px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.date-picker {
  width: 180px;
}

.range-chip {
  padding: 10px 14px;
  border-radius: 12px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
  color: var(--el-text-color-primary);
  font-size: 14px;
  min-width: 180px;
  text-align: center;
}

.range-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.range-summary-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.range-summary-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
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

.upcoming-section {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.section-subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.upcoming-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.upcoming-card {
  border: 1px solid rgba(67, 233, 123, 0.28);
  background: linear-gradient(135deg, rgba(67, 233, 123, 0.08), rgba(56, 249, 215, 0.12));
  border-radius: 16px;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.upcoming-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(67, 233, 123, 0.15);
}

.upcoming-time {
  font-size: 13px;
  color: #047857;
  font-weight: 600;
}

.upcoming-name {
  margin-top: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.upcoming-meta {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.schedule-groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.schedule-group {
  padding: 18px;
  border-radius: 18px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
}

.schedule-group.is-today-group {
  border-color: rgba(250, 112, 154, 0.3);
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.group-date {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.group-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.course-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  background: var(--el-fill-color-blank, #fff);
  border: 1px solid var(--el-border-color-lighter);
}

.course-card.is-ongoing {
  border-color: rgba(250, 112, 154, 0.35);
  background: linear-gradient(135deg, rgba(250, 112, 154, 0.08), rgba(254, 225, 64, 0.12));
}

.course-card.is-upcoming {
  border-color: rgba(67, 233, 123, 0.32);
  background: linear-gradient(135deg, rgba(67, 233, 123, 0.06), rgba(56, 249, 215, 0.1));
}

.course-main {
  flex: 1;
  min-width: 0;
}

.course-time {
  font-size: 13px;
  color: var(--el-color-primary);
  font-weight: 600;
}

.course-name-row {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.course-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.course-meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.course-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
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

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1100px) {
  .stats-grid,
  .upcoming-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .page-hero,
  .toolbar-top,
  .toolbar-bottom,
  .group-header,
  .course-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-actions,
  .course-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .date-picker,
  .range-chip {
    width: 100%;
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
