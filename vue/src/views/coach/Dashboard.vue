<template>
  <div class="coach-dashboard">
    <div v-loading="coachLoading" class="coach-info-card" :class="{ 'not-bound': notBound }">
      <template v-if="coachLoadFailed">
        <div class="empty-workbench">
          <div class="empty-title">工作台加载失败</div>
          <p class="empty-description">{{ coachLoadErrorMessage }}</p>
          <el-button type="primary" @click="reloadCoachDashboard">重试</el-button>
        </div>
      </template>

      <template v-else-if="notBound">
        <div class="empty-workbench">
          <div class="empty-title">尚未绑定教练档案</div>
          <p class="empty-description">请联系管理员在教练管理中为当前账号关联教练档案，绑定后即可查看课程、课表与预约工作台。</p>
          <el-button type="primary" :icon="User" @click="$router.push('/coach/profile')">去我的档案</el-button>
        </div>
      </template>

      <template v-else>
        <div class="coach-avatar-section">
          <el-avatar
            :size="80"
            :src="coachAvatarUrl || undefined"
            :icon="!coachAvatarUrl ? UserFilled : undefined"
            class="coach-avatar"
          />
          <div class="coach-badge">教练工作台</div>
        </div>
        <div class="coach-details">
          <h2 class="coach-name">{{ displayName }}</h2>
          <p class="coach-greeting">{{ greeting }}，今天的重点都在这里。</p>
          <div class="coach-stats">
            <div class="stat-item">
              <span class="stat-label">今日课程</span>
              <span class="stat-value">{{ dashboardStats.todayCourses }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">待处理预约</span>
              <span class="stat-value">{{ dashboardStats.pendingBookings }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">累计学员</span>
              <span class="stat-value">{{ coachInfo?.totalStudents ?? 0 }}</span>
            </div>
          </div>
        </div>
        <div class="coach-actions">
          <el-button type="primary" :icon="User" class="action-btn" @click="$router.push('/coach/profile')">我的档案</el-button>
        </div>
      </template>
    </div>

    <div class="quick-actions">
      <h3 class="section-title">快捷功能</h3>
      <div class="action-grid">
        <button
          v-for="action in quickActions"
          :key="action.title"
          type="button"
          class="action-card"
          @click="goQuickAction(action)"
        >
          <div class="action-icon" :style="{ background: action.gradient }">
            <el-icon :size="28"><component :is="action.icon" /></el-icon>
          </div>
          <div class="action-content">
            <h4 class="action-title">{{ action.title }}</h4>
            <p class="action-desc">{{ action.desc }}</p>
          </div>
        </button>
      </div>
    </div>

    <template v-if="!notBound">
      <div class="stats-section">
        <h3 class="section-title">今日重点</h3>
        <div class="stats-grid">
          <div class="stat-card stat-pending">
            <div class="stat-icon"><el-icon :size="24"><Bell /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ dashboardStats.pendingBookings }}</div>
              <div class="stat-label">待处理预约</div>
            </div>
          </div>
          <div class="stat-card stat-ongoing">
            <div class="stat-icon"><el-icon :size="24"><Timer /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ dashboardStats.ongoingCourses }}</div>
              <div class="stat-label">今日进行中课程</div>
            </div>
          </div>
          <div class="stat-card stat-completed">
            <div class="stat-icon"><el-icon :size="24"><CircleCheck /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ dashboardStats.completedCourses }}</div>
              <div class="stat-label">今日已完成课节</div>
            </div>
          </div>
          <div class="stat-card stat-today">
            <div class="stat-icon"><el-icon :size="24"><Calendar /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ dashboardStats.todayCourses }}</div>
              <div class="stat-label">今日课程总数</div>
            </div>
          </div>
        </div>
      </div>

      <div class="board-grid">
        <section class="board-panel">
          <div class="panel-header">
            <div>
              <h3 class="section-title">最近课程</h3>
              <p class="section-subtitle">优先处理最近要上的课节</p>
            </div>
            <el-button link type="primary" @click="$router.push('/coach/courses')">查看全部</el-button>
          </div>

          <div v-if="courseLoadFailed" class="error-state">
            <div class="error-title">课程数据加载失败</div>
            <div class="error-description">{{ courseErrorMessage }}</div>
            <el-button type="primary" @click="loadDashboardData">重试</el-button>
          </div>

          <div v-else-if="recentCourses.length" class="course-list">
            <article v-for="course in recentCourses" :key="course.id" class="course-card">
              <div class="course-main">
                <div class="course-time">{{ formatCourseTime(course) }}</div>
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
                <el-button link type="primary" @click="openCourseDetail(course)">查看详情</el-button>
                <el-button link type="success" @click="goToCourseBookings(course)">查看预约</el-button>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无最近课程" />
        </section>

        <section class="board-panel">
          <div class="panel-header">
            <div>
              <h3 class="section-title">最近预约</h3>
              <p class="section-subtitle">待签到与异常记录都能从这里快速进入</p>
            </div>
            <el-button link type="primary" @click="$router.push('/coach/bookings')">查看全部</el-button>
          </div>

          <div v-if="bookingLoadFailed" class="error-state">
            <div class="error-title">预约数据加载失败</div>
            <div class="error-description">{{ bookingErrorMessage }}</div>
            <el-button type="primary" @click="loadDashboardData">重试</el-button>
          </div>

          <div v-else-if="recentBookings.length" class="booking-list">
            <article v-for="booking in recentBookings" :key="booking.id" class="booking-card">
              <div class="booking-main">
                <div class="booking-name-row">
                  <h4 class="booking-name">{{ booking.memberName || '-' }}</h4>
                  <el-tag :type="formatStatusType(booking.status, BOOKING_STATUS_TYPE_MAP)" size="small">
                    {{ formatStatusText(booking.status, BOOKING_STATUS_TEXT_MAP) }}
                  </el-tag>
                </div>
                <div class="booking-course">{{ booking.courseName || '-' }}</div>
                <div class="booking-meta">
                  <span>{{ formatCourseTime(booking) }}</span>
                  <span>{{ booking.courtName || '未安排场地' }}</span>
                </div>
              </div>
              <div class="booking-actions">
                <el-button link type="primary" @click="openBookingDetail(booking)">查看详情</el-button>
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无最近预约" />
        </section>
      </div>
    </template>

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
      @view-bookings="goToCourseBookings"
      @view-member="openMemberHistory"
    />

    <el-dialog v-model="bookingDetailVisible" title="预约详情" width="720px" destroy-on-close>
      <div v-loading="bookingDetailLoading">
        <template v-if="bookingDetail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="预约单号">{{ bookingDetail.bookingNo }}</el-descriptions-item>
            <el-descriptions-item label="会员">{{ bookingDetail.memberName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程">{{ bookingDetail.courseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="日期">{{ bookingDetail.courseDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="时间">{{ formatCourseTime(bookingDetail) }}</el-descriptions-item>
            <el-descriptions-item label="场地">{{ bookingDetail.courtName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="formatStatusType(bookingDetail.status, BOOKING_STATUS_TYPE_MAP)" size="small">
                {{ formatStatusText(bookingDetail.status, BOOKING_STATUS_TEXT_MAP) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">{{ paymentStatusText(bookingDetail.paymentStatus) }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ bookingDetail.remark || '无' }}</el-descriptions-item>
          </el-descriptions>

          <div class="detail-actions">
            <el-button v-if="bookingDetail.memberId" type="primary" plain @click="openMemberHistory(bookingDetail.memberId)">查看学员历史</el-button>
            <el-button type="primary" @click="goToBookingsPage(bookingDetail)">进入预约页处理</el-button>
          </div>
        </template>
        <el-empty v-else-if="bookingDetailLoadFailed" description="预约详情加载失败" />
      </div>
    </el-dialog>

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
import {
  Bell,
  Calendar,
  CircleCheck,
  Document,
  List,
  Timer,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCurrentCoach } from '@/api/coach'
import { getCourseInfo, getMyCourses } from '@/api/course'
import { getBookingDetailForCoach, getBookingsForCoach } from '@/api/courseBooking'
import { getMemberConsumeList, getMemberInfo } from '@/api/member'
import CourseDetailDrawer from './components/CourseDetailDrawer.vue'
import MemberHistoryDrawer from './components/MemberHistoryDrawer.vue'
import {
  BOOKING_STATUS_TEXT_MAP,
  BOOKING_STATUS_TYPE_MAP,
  COACH_UNBOUND_MESSAGE,
  COURSE_STATUS_TEXT_MAP,
  COURSE_STATUS_TYPE_MAP,
  compareCourseTime,
  formatCourseTime,
  formatDateKey,
  formatStatusText,
  formatStatusType,
  formatStudentCount,
  isCoachUnboundError,
  isTodayCourseOngoing,
  toAbsoluteAssetUrl
} from './coachViewUtils'

const router = useRouter()

const coachLoading = ref(false)
const coachInfo = ref(null)
const notBound = ref(false)
const coachLoadFailed = ref(false)
const coachLoadErrorMessage = ref('工作台加载失败，请稍后重试')

const dashboardStats = ref({
  pendingBookings: 0,
  ongoingCourses: 0,
  completedCourses: 0,
  todayCourses: 0
})

const recentCourses = ref([])
const recentBookings = ref([])
const courseLoadFailed = ref(false)
const bookingLoadFailed = ref(false)
const courseErrorMessage = ref('请稍后重试')
const bookingErrorMessage = ref('请稍后重试')

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

const bookingDetailVisible = ref(false)
const bookingDetailLoading = ref(false)
const bookingDetail = ref(null)
const bookingDetailLoadFailed = ref(false)

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

const displayName = computed(() => {
  if (coachInfo.value?.coachName) return coachInfo.value.coachName
  try {
    const user = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return user.username || '教练'
  } catch (_) {
    return '教练'
  }
})

const coachAvatarUrl = computed(() => toAbsoluteAssetUrl(coachInfo.value?.avatar))

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})

const quickActions = [
  {
    title: '待签到预约',
    desc: '直达已支付待处理预约',
    path: '/coach/bookings',
    query: { status: '2' },
    icon: Bell,
    gradient: 'linear-gradient(135deg, #fb7185 0%, #f97316 100%)'
  },
  {
    title: '今日日程',
    desc: '进入课表并定位到今天',
    path: '/coach/schedule',
    query: { view: 'day', date: formatDateKey(new Date()) },
    icon: Calendar,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    title: '我的课程',
    desc: '查看最近排课与详情',
    path: '/coach/courses',
    icon: Document,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '预约明细',
    desc: '处理签到、完成与取消',
    path: '/coach/bookings',
    icon: List,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
]

const paymentStatusText = (status) => {
  const map = { 0: '未支付', 1: '已支付', 2: '已退款' }
  return map[status] ?? '未知'
}

const loadCoach = async () => {
  coachLoading.value = true
  coachLoadFailed.value = false
  coachLoadErrorMessage.value = '工作台加载失败，请稍后重试'
  try {
    const res = await getCurrentCoach()
    if (res?.code === 200 && res?.data) {
      coachInfo.value = res.data
      notBound.value = false
      return true
    }
    coachInfo.value = null
    notBound.value = true
    return false
  } catch (error) {
    coachInfo.value = null
    notBound.value = isCoachUnboundError(error)
    coachLoadFailed.value = !notBound.value
    coachLoadErrorMessage.value = error?.message || '工作台加载失败，请稍后重试'
    return false
  } finally {
    coachLoading.value = false
  }
}

const loadCoursePanelData = async () => {
  if (notBound.value) return
  const today = formatDateKey(new Date())
  courseLoadFailed.value = false
  try {
    const [todayCoursesRes, recentCoursesRes] = await Promise.all([
      getMyCourses({ page: 1, size: 100, startTime: `${today} 00:00:00`, endTime: `${today} 23:59:59` }),
      getMyCourses({ page: 1, size: 5, startTime: `${today} 00:00:00` })
    ])

    if (todayCoursesRes?.code === 200 && recentCoursesRes?.code === 200) {
      const todayCourses = todayCoursesRes.data?.data ?? []
      dashboardStats.value = {
        ...dashboardStats.value,
        ongoingCourses: todayCourses.filter((course) => isTodayCourseOngoing(course)).length,
        completedCourses: todayCourses.filter((course) => Number(course.status) === 3).length,
        todayCourses: todayCoursesRes?.data?.total ?? 0
      }
      recentCourses.value = [...(recentCoursesRes.data?.data ?? [])].sort(compareCourseTime).slice(0, 5)
    } else {
      throw new Error(recentCoursesRes?.message || todayCoursesRes?.message || '课程数据获取失败')
    }

  } catch (error) {
    recentCourses.value = []
    dashboardStats.value = {
      ...dashboardStats.value,
      ongoingCourses: 0,
      completedCourses: 0,
      todayCourses: 0
    }
    courseLoadFailed.value = true
    if (isCoachUnboundError(error)) {
      notBound.value = true
      courseErrorMessage.value = COACH_UNBOUND_MESSAGE
      return
    }
    courseErrorMessage.value = error?.message || '请稍后重试'
  }
}

const loadBookingPanelData = async () => {
  if (notBound.value) return
  bookingLoadFailed.value = false
  try {
    const [pendingBookingsRes, recentBookingsRes] = await Promise.all([
      getBookingsForCoach({ page: 1, size: 1, status: 2 }),
      getBookingsForCoach({ page: 1, size: 5 })
    ])

    if (pendingBookingsRes?.code === 200) {
      dashboardStats.value = {
        ...dashboardStats.value,
        pendingBookings: pendingBookingsRes.data?.total ?? 0
      }
    } else {
      throw new Error(pendingBookingsRes?.message || '预约数据获取失败')
    }

    if (recentBookingsRes?.code === 200) {
      recentBookings.value = recentBookingsRes.data?.data ?? []
      return
    }
    throw new Error(recentBookingsRes?.message || '预约数据获取失败')
  } catch (error) {
    recentBookings.value = []
    dashboardStats.value = {
      ...dashboardStats.value,
      pendingBookings: 0
    }
    bookingLoadFailed.value = true
    if (isCoachUnboundError(error)) {
      notBound.value = true
      bookingErrorMessage.value = COACH_UNBOUND_MESSAGE
      return
    }
    bookingErrorMessage.value = error?.message || '请稍后重试'
  }
}

const loadDashboardData = async () => {
  await Promise.all([loadCoursePanelData(), loadBookingPanelData()])
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
    const res = await getBookingsForCoach({ page: 1, size: 100, courseId })
    if (res?.code === 200 && res?.data) {
      courseStudents.value = [...(res.data.data ?? [])].sort(compareCourseTime)
      return
    }
    throw new Error(res?.message || '学员名单获取失败')
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

const openCourseDetail = async (course) => {
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

const goToCourseBookings = (course) => {
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

const openBookingDetail = async (booking) => {
  bookingDetailVisible.value = true
  bookingDetailLoading.value = true
  bookingDetailLoadFailed.value = false
  bookingDetail.value = null
  try {
    const res = await getBookingDetailForCoach(booking.id)
    if (res?.code === 200) {
      bookingDetail.value = res.data
    } else {
      bookingDetailLoadFailed.value = true
    }
  } catch (error) {
    bookingDetailLoadFailed.value = true
    if (isCoachUnboundError(error)) {
      ElMessage.error(COACH_UNBOUND_MESSAGE)
    }
  } finally {
    bookingDetailLoading.value = false
  }
}

const goToBookingsPage = (booking) => {
  router.push({
    path: '/coach/bookings',
    query: {
      courseId: booking?.courseId ? String(booking.courseId) : undefined,
      courseName: booking?.courseName || undefined
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

const goQuickAction = (action) => {
  router.push({
    path: action.path,
    query: action.query
  })
}

const reloadCoachDashboard = async () => {
  const hasCoach = await loadCoach()
  if (hasCoach) {
    await loadDashboardData()
  }
}

onMounted(async () => {
  const hasCoach = await loadCoach()
  if (hasCoach) {
    await loadDashboardData()
  }
})
</script>

<style scoped>
.coach-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes cardEntrance {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes shimmer {
  0%, 100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(180deg);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1.1) rotate(5deg);
  }
  50% {
    transform: scale(1.15) rotate(5deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

.coach-info-card {
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-secondary, #8B5CF6) 100%);
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  color: #ffffff;
  box-shadow: 0 12px 40px rgba(37, 99, 235, 0.3), 0 4px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  animation: cardEntrance 0.6s ease-out;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.coach-info-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: shimmer 8s ease-in-out infinite;
  pointer-events: none;
}

.coach-info-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  opacity: 0.6;
  pointer-events: none;
}

.coach-info-card:not(.not-bound):hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 16px 48px color-mix(in srgb, var(--color-primary, #2563EB) 28%, transparent), 0 6px 16px rgba(0, 0, 0, 0.15);
}

.coach-info-card.not-bound {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  animation: none;
}

.coach-info-card.not-bound::before,
.coach-info-card.not-bound::after {
  display: none;
}

.empty-workbench {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
}

.empty-title {
  font-size: 24px;
  font-weight: 700;
}

.empty-description {
  margin: 0;
  max-width: 620px;
  font-size: 14px;
  line-height: 1.7;
  opacity: 0.92;
}

.coach-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.coach-avatar {
  border: 4px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2), 0 0 0 4px rgba(255, 255, 255, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.coach-info-card:not(.not-bound):hover .coach-avatar {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3), 0 0 0 6px rgba(255, 255, 255, 0.15);
}

.coach-badge {
  background: rgba(255, 255, 255, 0.22);
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.coach-info-card:not(.not-bound):hover .coach-badge {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.coach-details {
  flex: 1;
}

.coach-name {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 700;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.coach-greeting {
  margin: 0 0 16px;
  font-size: 16px;
  opacity: 0.9;
  position: relative;
  z-index: 1;
}

.coach-stats {
  display: flex;
  gap: 32px;
  position: relative;
  z-index: 1;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item .stat-label {
  font-size: 13px;
  opacity: 0.8;
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: 700;
}

.coach-actions .action-btn {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  border: 2px solid rgba(255, 255, 255, 0.35);
  color: #fff;
  font-weight: 600;
  padding: 12px 24px;
  border-radius: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
  overflow: hidden;
}

.coach-actions .action-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
}

.coach-info-card:not(.not-bound) .coach-actions .action-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
}

.coach-info-card:not(.not-bound) .coach-actions .action-btn:hover::before {
  width: 200px;
  height: 200px;
}

.coach-info-card:not(.not-bound) .coach-actions .action-btn:active {
  transform: translateY(-1px) scale(1.02);
}

.quick-actions,
.stats-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.section-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.action-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.action-card,
.board-panel {
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.action-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  text-align: left;
  position: relative;
  overflow: hidden;
  border: 1px solid var(--color-border, #E2E8F0);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--color-primary, #2563EB) 8%, transparent), transparent);
  transition: left 0.5s ease;
}

.action-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15), 0 4px 12px color-mix(in srgb, var(--color-primary, #2563EB) 18%, transparent);
  border-color: var(--color-primary-light, var(--el-color-primary-light-5, #60A5FA));
}

.action-card:hover::before {
  left: 100%;
}

.action-card:active {
  transform: translateY(-3px) scale(1.01);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.action-card:hover .action-icon {
  transform: scale(1.15) rotate(5deg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.action-card .action-icon .el-icon {
  transition: transform 0.3s ease;
}

.action-card:hover .action-icon .el-icon {
  transform: scale(1.1);
}

.action-content {
  min-width: 0;
}

.action-title {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.action-desc {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
}

.stats-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, color-mix(in srgb, var(--color-primary, #2563EB) 4%, transparent) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--color-primary-light, var(--el-color-primary-light-5, #60A5FA));
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
  animation: iconPulse 1.5s ease-in-out infinite;
}

.stat-pending .stat-icon {
  background: linear-gradient(135deg, #fb7185 0%, #f97316 100%);
}

.stat-ongoing .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-completed .stat-icon {
  background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
}

.stat-today .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-content .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary, #1E293B);
  margin-bottom: 4px;
  display: inline-block;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-content .stat-value {
  transform: scale(1.05);
  color: var(--color-primary, var(--el-color-primary, #2563EB));
}

.stat-content .stat-label {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
}

.board-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.board-panel {
  padding: 18px;
  transition: box-shadow 0.3s ease, border-color 0.3s ease;
}

.board-panel:hover {
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
  border-color: color-mix(in srgb, var(--color-primary, #2563EB) 18%, var(--color-border, #E2E8F0));
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.course-list,
.booking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-card,
.booking-card {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-blank, #fff);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.course-card::before,
.booking-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--color-primary, var(--el-color-primary, #2563EB));
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.course-card::after,
.booking-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--color-primary, #2563EB) 7%, transparent), transparent);
  transition: left 0.5s ease;
}

.course-card:hover,
.booking-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.12), 0 4px 10px color-mix(in srgb, var(--color-primary, #2563EB) 14%, transparent);
  border-color: var(--color-primary-light, var(--el-color-primary-light-5, #60A5FA));
}

.course-card:hover::before,
.booking-card:hover::before {
  transform: scaleY(1);
}

.course-card:hover::after,
.booking-card:hover::after {
  left: 100%;
}

.course-main,
.booking-main {
  flex: 1;
  min-width: 0;
}

.course-time {
  font-size: 13px;
  color: var(--el-color-primary);
  font-weight: 600;
}

.course-name-row,
.booking-name-row {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.course-name,
.booking-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.course-meta,
.booking-meta,
.booking-course {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.course-meta,
.booking-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.course-actions,
.booking-actions,
.detail-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-actions {
  margin-top: 20px;
  justify-content: flex-end;
}

.error-state {
  padding: 20px;
  border-radius: 16px;
  background: #fff7f7;
  border: 1px solid #f5c2c7;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
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
  margin-top: 8px;
  font-size: 14px;
  color: #7c2d12;
}

@media (max-width: 1100px) {
  .action-grid,
  .stats-grid,
  .board-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .coach-info-card,
  .coach-stats,
  .panel-header,
  .course-card,
  .booking-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
