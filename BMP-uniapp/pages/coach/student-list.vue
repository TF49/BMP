<template>
  <view class="student-list-page">
    <scroll-view
      class="page-scroll"
      scroll-y
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="page-shell">
        <CoachTopBar :status-bar-height="statusBarHeight" :avatar="coachAvatar" brand="KINETIC LOGIC" />

        <view class="hero-section">
          <text class="hero-kicker">MY STUDENTS</text>
          <text class="hero-title">我的学员</text>
          <text class="hero-desc">已服务 {{ summary.totalStudents }} 人 · 平均出勤率 {{ summary.averageAttendanceRate }}%</text>
        </view>

        <view class="filter-card">
          <view class="search-row">
            <input v-model="keyword" class="search-input" placeholder="搜索姓名 / 手机号" confirm-type="search" @confirm="applyFilters" />
            <view class="search-button" @tap="applyFilters">搜索</view>
          </view>
          <scroll-view class="chip-scroll" scroll-x :show-scrollbar="false">
            <view class="chip-row">
              <view v-for="chip in chips" :key="chip.key" class="chip" :class="{ active: activeChip === chip.key }" @tap="selectChip(chip.key)">
                <text>{{ chip.label }}</text>
              </view>
            </view>
          </scroll-view>
          <picker :range="sortOptions" range-key="label" @change="handleSortChange">
            <view class="sort-row">
              <text>排序</text>
              <text class="sort-value">{{ currentSort.label }} ›</text>
            </view>
          </picker>
        </view>

        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-title">正在加载学员</text>
        </view>
        <view v-else-if="loadFailed" class="state-card">
          <text class="state-title">学员列表加载失败</text>
          <text class="state-desc">{{ errorMessage }}</text>
          <view class="state-action" @tap="retryLoad">重新加载</view>
        </view>
        <view v-else-if="!students.length" class="state-card">
          <text class="state-title">还没有学员</text>
          <text class="state-desc">学员预约你的课程后，会自动出现在这里。</text>
          <view class="state-action" @tap="goCourses">查看我的课程</view>
        </view>
        <view v-else class="student-list">
          <view v-for="item in students" :key="item.id" class="student-card" @tap="openDetail(item.id)">
            <image
              class="student-avatar"
              :src="avatarFor(item)"
              mode="aspectFill"
              @error="markAvatarFailed(item.id)"
            />
            <view class="student-content">
              <view class="name-row">
                <text class="student-name">{{ item.memberName }}</text>
                <text v-if="item.memberType === 'MEMBER'" class="member-pill">Lv.{{ item.memberLevel || 1 }}</text>
                <text v-if="item.risk" class="risk-pill">高风险</text>
              </view>
              <text class="student-phone">{{ item.maskedPhone }}</text>
              <text class="student-meta">最近上课 {{ formatDateTime(item.lastLessonTime) }}</text>
              <view class="metric-row">
                <text>出勤率 {{ numberText(item.attendanceRate) }}%</text>
                <text>累计 {{ numberText(item.totalHours) }}h</text>
                <text>消费 ¥{{ moneyText(item.totalPaidAmount) }}</text>
              </view>
            </view>
            <view class="attendance-pill" :class="{ risk: item.risk }">{{ numberText(item.attendanceRate) }}%</view>
          </view>
        </view>

        <view v-if="page.pages > 1" class="pagination-card">
          <button class="page-button" :disabled="page.page <= 1" @tap="changePage(-1)">上一页</button>
          <text>{{ page.page }} / {{ page.pages }}</text>
          <button class="page-button" :disabled="page.page >= page.pages" @tap="changePage(1)">下一页</button>
        </view>
        <view class="bottom-space" />
      </view>
    </scroll-view>
    <CoachTabBar current="home" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'
import { getCoachStudents, getCurrentCoach, type CoachStudentListItem, type CoachStudentListQuery } from '@/api/coachSelf'
import { resolveCoachAvatar } from '@/utils/coachAccess'
import { safeReLaunch } from '@/utils/safeRoute'
import { useCoachStudentRealtimeRefresh } from '@/composables/useCoachStudentRealtimeRefresh'
import {
  buildCoachStudentDetailUrl,
  DEFAULT_COACH_STUDENT_AVATAR,
  getStudentAvatarWithFallback,
  parseCoachStudentListRoute
} from '@/utils/coachStudents'

const statusBarHeight = ref(uni.getSystemInfoSync().statusBarHeight || 20)
const coachAvatar = ref(DEFAULT_COACH_STUDENT_AVATAR)
const loading = ref(true)
const refreshing = ref(false)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')
const retryCount = ref(0)
const keyword = ref('')
const activeChip = ref('all')
const students = ref<CoachStudentListItem[]>([])
const avatarFailures = ref<Record<number, boolean>>({})
const page = ref({ page: 1, size: 20, total: 0, pages: 0 })
const summary = ref({ totalStudents: 0, riskStudents: 0, todayStudents: 0, averageAttendanceRate: 0 })

const chips = [
  { key: 'all', label: '全部' },
  { key: 'risk', label: '高风险缺勤' },
  { key: 'today', label: '今日有课' },
  { key: 'member', label: '会员' },
  { key: 'normal', label: '普通' }
]
const sortOptions: Array<{ label: string; orderBy: CoachStudentListQuery['orderBy']; orderDirection: 'ASC' | 'DESC' }> = [
  { label: '最近上课', orderBy: 'lastLessonTime', orderDirection: 'DESC' },
  { label: '出勤率低到高', orderBy: 'attendanceRate', orderDirection: 'ASC' },
  { label: '累计消费高到低', orderBy: 'totalPaidAmount', orderDirection: 'DESC' },
  { label: '累计课时高到低', orderBy: 'totalHours', orderDirection: 'DESC' }
]
const sortIndex = ref(0)
const currentSort = computed(() => sortOptions[sortIndex.value])

function buildQuery(): CoachStudentListQuery {
  const query: CoachStudentListQuery = {
    page: page.value.page,
    size: page.value.size,
    keyword: keyword.value.trim() || undefined,
    orderBy: currentSort.value.orderBy,
    orderDirection: currentSort.value.orderDirection
  }
  if (activeChip.value === 'risk') query.riskOnly = true
  if (activeChip.value === 'today') query.todayOnly = true
  if (activeChip.value === 'member') query.memberType = 'MEMBER'
  if (activeChip.value === 'normal') query.memberType = 'NORMAL'
  return query
}

async function loadStudents() {
  loading.value = true
  loadFailed.value = false
  try {
    const [coach, result] = await Promise.all([getCurrentCoach(), getCoachStudents(buildQuery())])
    coachAvatar.value = resolveCoachAvatar(coach.avatar)
    students.value = result.page?.data || []
    page.value = {
      page: result.page?.page || 1,
      size: result.page?.size || 20,
      total: result.page?.total || 0,
      pages: result.page?.pages || 0
    }
    summary.value = {
      totalStudents: result.totalStudents || 0,
      riskStudents: result.riskStudents || 0,
      todayStudents: result.todayStudents || 0,
      averageAttendanceRate: Number(result.averageAttendanceRate || 0)
    }
    retryCount.value = 0
  } catch (error) {
    loadFailed.value = true
    errorMessage.value = error instanceof Error ? error.message : '请稍后重试'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function applyFilters() { page.value.page = 1; loadStudents() }
function selectChip(key: string) { activeChip.value = key; applyFilters() }
function handleSortChange(event: { detail: { value: string | number } }) { sortIndex.value = Number(event.detail.value); applyFilters() }
function changePage(delta: number) { page.value.page += delta; loadStudents() }
function handleRefresh() { refreshing.value = true; page.value.page = 1; loadStudents() }
function retryLoad() { if (retryCount.value >= 3) return; retryCount.value += 1; loadStudents() }
function goCourses() { safeReLaunch('/pages/coach/courses', '/pages/coach/index') }
function openDetail(id: number) { uni.navigateTo({ url: buildCoachStudentDetailUrl(id) }) }
function markAvatarFailed(id: number) { avatarFailures.value[id] = true }
function avatarFor(item: CoachStudentListItem) { return avatarFailures.value[item.id] ? DEFAULT_COACH_STUDENT_AVATAR : getStudentAvatarWithFallback(item.avatar) }
function formatDateTime(value?: string) { return value ? value.slice(0, 16) : '暂无记录' }
function numberText(value?: number) { return Number(value || 0).toFixed(value && value % 1 ? 1 : 0) }
function moneyText(value?: number) { return Number(value || 0).toFixed(2) }

onLoad(options => {
  const route = parseCoachStudentListRoute(options)
  keyword.value = route.keyword
  if (route.riskOnly) activeChip.value = 'risk'
})
useCoachStudentRealtimeRefresh(loadStudents)
onShow(loadStudents)
</script>

<style lang="scss" scoped>
.student-list-page { min-height: 100vh; background: #f9f9f9; color: #1a1c1c; }
.page-scroll { min-height: 100vh; }
.page-shell { padding: 0 24rpx; }
.hero-section { margin: 38rpx 8rpx 0; }
.hero-kicker { display: block; color: #a33e00; font-size: 22rpx; font-weight: 800; letter-spacing: 4rpx; }
.hero-title { display: block; margin-top: 12rpx; font-size: 62rpx; font-weight: 900; }
.hero-desc { display: block; margin-top: 14rpx; color: #5f5e5e; font-size: 24rpx; }
.filter-card, .state-card, .student-card, .pagination-card { border-radius: 26rpx; background: #fff; box-shadow: 0 8rpx 24rpx rgba(26,28,28,.05); }
.filter-card { margin-top: 30rpx; padding: 24rpx; }
.search-row { display: flex; gap: 14rpx; }
.search-input { flex: 1; height: 76rpx; border-radius: 18rpx; background: #f2f2f2; padding: 0 22rpx; font-size: 26rpx; }
.search-button { width: 110rpx; border-radius: 18rpx; background: #ff6600; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 800; }
.chip-scroll { margin-top: 20rpx; white-space: nowrap; }
.chip-row { display: inline-flex; gap: 12rpx; }
.chip { padding: 14rpx 22rpx; border-radius: 999rpx; background: #f0f0f0; color: #5f5e5e; font-size: 22rpx; }
.chip.active { background: #ffdbcd; color: #7b2e00; font-weight: 800; }
.sort-row { margin-top: 20rpx; padding-top: 20rpx; border-top: 1rpx solid #ededed; display: flex; justify-content: space-between; font-size: 24rpx; }
.sort-value { color: #a33e00; font-weight: 800; }
.student-list { margin-top: 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.student-card { padding: 24rpx; display: flex; align-items: center; gap: 20rpx; }
.student-avatar { width: 108rpx; height: 108rpx; flex-shrink: 0; border-radius: 28rpx; background: #ededed; }
.student-content { flex: 1; min-width: 0; }
.name-row { display: flex; align-items: center; flex-wrap: wrap; gap: 10rpx; }
.student-name { font-size: 32rpx; font-weight: 900; }
.member-pill, .risk-pill { padding: 6rpx 12rpx; border-radius: 999rpx; font-size: 18rpx; font-weight: 800; }
.member-pill { background: #fff1e8; color: #a33e00; }
.risk-pill { background: #ffe5e5; color: #ba1a1a; }
.student-phone, .student-meta { display: block; margin-top: 10rpx; color: #5f5e5e; font-size: 22rpx; }
.metric-row { margin-top: 14rpx; display: flex; flex-wrap: wrap; gap: 14rpx; color: #474746; font-size: 20rpx; }
.attendance-pill { padding: 12rpx; border-radius: 16rpx; background: #e6f6ea; color: #1f7a37; font-size: 22rpx; font-weight: 900; }
.attendance-pill.risk { background: #ffe5e5; color: #ba1a1a; }
.state-card { margin-top: 28rpx; padding: 72rpx 32rpx; text-align: center; display: flex; flex-direction: column; align-items: center; }
.state-title { margin-top: 18rpx; font-size: 32rpx; font-weight: 900; }
.state-desc { margin-top: 14rpx; color: #5f5e5e; font-size: 24rpx; line-height: 1.7; }
.state-action { margin-top: 24rpx; padding: 20rpx 30rpx; border-radius: 999rpx; background: #ff6600; color: #fff; font-weight: 800; }
.spinner { width: 48rpx; height: 48rpx; border: 5rpx solid #ededed; border-top-color: #ff6600; border-radius: 50%; animation: spin .8s linear infinite; }
.pagination-card { margin-top: 24rpx; padding: 20rpx; display: flex; align-items: center; justify-content: space-between; }
.page-button { margin: 0; min-width: 150rpx; border: 0; border-radius: 999rpx; background: #fff1e8; color: #a33e00; font-size: 22rpx; }
.bottom-space { height: 180rpx; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
