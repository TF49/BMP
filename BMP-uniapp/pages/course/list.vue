<template>
  <view class="course-center-page bg-background text-on-surface min-h-screen">
    <!-- Fixed TopAppBar -->
    <view
      class="header-bar fixed top-0 left-0 w-full z-50 flex justify-between items-center px-6 py-4"
      :style="{ paddingTop: (statusBarHeight || 44) + 8 + 'px' }"
    >
      <view class="flex items-center gap-2">
        <text class="brand-shuttle">🏸</text>
        <text class="brand-title">KINETIC LOGIC</text>
      </view>
      <view class="flex gap-4 items-center" :style="{ marginRight: navBarMarginRight + 'px' }">
        <view hover-class="opacity-80" @tap="goNotice">
          <uni-icons type="chatbubble" size="22" color="#64748b"></uni-icons>
        </view>
        <view hover-class="opacity-80" @tap="goProfile">
          <uni-icons type="person" size="22" color="#64748b"></uni-icons>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll" :style="{ paddingTop: headerOffsetPx + 'px' }" :show-scrollbar="false">
      <view class="px-6 pb-32">
        <!-- Search -->
        <view class="mt-4">
          <view class="search-wrap relative">
            <view class="search-icon-wrap">
              <uni-icons type="search" size="18" color="#94a3b8"></uni-icons>
            </view>
            <input
              v-model="searchKeyword"
              class="search-input"
              type="text"
              placeholder="搜索课程或教练..."
              confirm-type="search"
              @confirm="applySearch"
            />
          </view>
        </view>

        <!-- Coach highlight -->
        <view class="mt-10 overflow-hidden">
          <view class="flex justify-between items-end mb-6">
            <view>
              <text class="text-primary font-bold tracking-widest text-10 uppercase block mb-1">Elite Staff</text>
              <text class="text-2xl font-extrabold tracking-tight block">教练团队</text>
            </view>
            <view class="text-primary text-sm font-medium flex items-center gap-1" hover-class="opacity-70" @tap="onAllCoaches">
              <text>全部教练</text>
              <uni-icons type="right" size="14" color="#a33e00"></uni-icons>
            </view>
          </view>
          <scroll-view scroll-x class="hide-scrollbar coach-scroll" :show-scrollbar="false">
            <view class="coach-row flex gap-4 pb-4">
              <view
                v-for="(c, idx) in displayCoaches"
                :key="idx"
                class="coach-card flex-shrink-0 w-40 bg-surface-container-lowest rounded-2xl p-4"
                hover-class="coach-card-active"
                @tap="onCoachTap(c)"
              >
                <image
                  v-if="c.avatar"
                  class="w-full aspect-square object-cover rounded-xl mb-3 coach-img"
                  mode="aspectFill"
                  :src="c.avatar"
                />
                <view v-else class="w-full aspect-square bg-surface-container-low rounded-xl mb-3 flex items-center justify-center">
                  <uni-icons type="person" size="40" color="#cbd5e1"></uni-icons>
                </view>
                <text class="font-bold text-sm block">{{ c.name }}</text>
                <text class="text-xs text-secondary mt-1 block">{{ c.title }}</text>
                <view class="flex items-center mt-2 text-orange-600">
                  <uni-icons type="star-filled" size="12" color="#ea580c"></uni-icons>
                  <text class="text-10 font-bold ml-1">{{ c.rating }} ({{ c.reviews }}+)</text>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- Date selector -->
        <view class="mt-8">
          <scroll-view scroll-x class="hide-scrollbar date-scroll" :show-scrollbar="false">
            <view class="date-row flex gap-3">
              <view
                v-for="(d, i) in weekDates"
                :key="d.iso"
                class="date-chip flex-shrink-0 w-16 h-20 flex flex-col items-center justify-center rounded-2xl"
                :class="selectedDateIndex === i ? 'date-chip-active' : 'date-chip-idle'"
                hover-class="opacity-90"
                @tap="selectedDateIndex = i"
              >
                <text class="text-10 font-bold date-chip-dow">{{ d.dow }}</text>
                <text class="text-xl font-black">{{ d.day }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- Course list -->
        <view class="mt-8 flex flex-col gap-4">
          <text class="text-xl font-extrabold tracking-tight px-1 block">今日排期</text>

          <view v-if="filteredCourses.length === 0" class="empty-card bg-surface-container-lowest rounded-3xl p-8 text-center">
            <text class="text-secondary text-sm block">该日期暂无课程</text>
            <text class="text-xs-10 text-secondary mt-2 block">试试其他日期或清空搜索</text>
          </view>

          <view
            v-for="item in filteredCourses"
            :key="item.id"
            class="course-card bg-surface-container-lowest rounded-3xl p-5 relative overflow-hidden"
            hover-class="opacity-98"
          >
            <view v-if="item.badge" class="badge-fast absolute top-0 right-0 text-10 font-black px-4 py-1 rounded-bl-xl tracking-widest uppercase">
              {{ item.badge }}
            </view>

            <view class="flex flex-col gap-4">
              <view class="flex-1 pr-2">
                <view class="flex items-start justify-between mb-3">
                  <view>
                    <text class="text-lg font-black leading-tight block">{{ item.title }}</text>
                    <view class="flex flex-wrap items-center gap-3 mt-2">
                      <view class="flex items-center text-secondary text-xs">
                        <uni-icons type="calendar" size="14" color="#5f5e5e"></uni-icons>
                        <text class="ml-1">{{ item.timeRange }}</text>
                      </view>
                      <view class="flex items-center text-secondary text-xs">
                        <uni-icons type="location" size="14" color="#5f5e5e"></uni-icons>
                        <text class="ml-1">{{ item.location }}</text>
                      </view>
                    </view>
                  </view>
                </view>

                <view class="flex items-center gap-3 mb-6">
                  <image
                    class="w-10 h-10 rounded-full border-2 border-surface-container-low object-cover coach-avatar-sm"
                    mode="aspectFill"
                    :src="item.coachAvatar || '/static/placeholders/avatar.svg'"
                  />
                  <view>
                    <text class="text-xs font-bold block">{{ item.coachName }}</text>
                    <text class="text-10 text-secondary tracking-wide uppercase block">{{ item.coachRole }}</text>
                  </view>
                </view>

                <view class="space-y-2">
                  <view class="flex justify-between text-10 font-bold text-secondary uppercase tracking-widest">
                    <text>Availability</text>
                    <text class="text-primary">{{ item.availText }}</text>
                  </view>
                  <view class="h-1-5 w-full bg-surface-container-low rounded-full overflow-hidden">
                    <view
                      class="h-full rounded-full avail-fill"
                      :class="item.availSoft ? 'avail-fill-soft' : ''"
                      :style="{ width: item.availPercent + '%' }"
                    />
                  </view>
                </view>
              </view>

              <view class="footer-row flex flex-row items-center justify-between gap-4 pt-2 border-t border-surface-container-low">
                <view>
                  <text class="text-10 font-bold text-secondary block">Price</text>
                  <text class="text-xl font-black text-on-surface">¥{{ item.price }}</text>
                </view>
                <button class="book-btn bg-on-surface text-white h-12 px-8 rounded-xl font-bold text-sm" hover-class="book-btn-hover" @tap="bookCourse(item)">
                  立即预约
                </button>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- FAB -->
    <view class="fixed bottom-24 right-6 fab-layer" hover-class="scale-95">
      <view class="fab-btn w-14 h-14 rounded-full flex items-center justify-center" @tap="showFilterSheet = true">
        <uni-icons type="bars" size="22" color="#561d00"></uni-icons>
      </view>
    </view>

    <CustomTabBar :current="2" />

    <!-- Filter sheet -->
    <view v-if="showFilterSheet" class="filter-mask" @tap="showFilterSheet = false">
      <view class="filter-sheet" @tap.stop>
        <view class="filter-sheet-header flex justify-between items-center mb-4">
          <text class="font-bold text-base">筛选</text>
          <text class="text-secondary" @tap="showFilterSheet = false">关闭</text>
        </view>
        <text class="text-xs text-secondary mb-2 block">教练</text>
        <scroll-view scroll-y class="filter-options mb-4">
          <view
            class="filter-chip"
            :class="{ active: filterCoachId === null }"
            @tap="filterCoachId = null; showFilterSheet = false; loadCourseList()"
          >
            <text>全部</text>
          </view>
          <view
            v-for="c in apiCoaches"
            :key="c.id"
            class="filter-chip"
            :class="{ active: filterCoachId === c.id }"
            @tap="filterCoachId = c.id; showFilterSheet = false; loadCourseList()"
          >
            <text>{{ c.name }}</text>
          </view>
        </scroll-view>
        <button class="book-btn bg-on-surface text-white h-11 w-full rounded-xl font-bold text-sm" @tap="resetFilters">重置筛选</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { getCourseList, getCoachList, type CourseItem } from '@/api/course'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const statusBarHeight = ref(44)
const navBarMarginRight = ref(0)
const headerOffsetPx = computed(() => (statusBarHeight.value || 44) + 56)

const searchKeyword = ref('')
const selectedDateIndex = ref(0)
const showFilterSheet = ref(false)
const filterCoachId = ref<number | null>(null)

const DOW = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'] as const

function pad2(n: number) {
  return n < 10 ? `0${n}` : `${n}`
}

function formatIso(d: Date) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`
}

const weekDates = computed(() => {
  const out: { dow: string; day: number; iso: string }[] = []
  const start = new Date()
  for (let i = 0; i < 7; i++) {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    out.push({
      dow: DOW[d.getDay()],
      day: d.getDate(),
      iso: formatIso(d)
    })
  }
  return out
})

const DEFAULT_COACHES = [
  {
    id: -1,
    name: 'Zhang Wei',
    title: '前国家队成员',
    rating: '4.9',
    reviews: '120',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuCKHKS3Jdx-Eg62VB1rwleaMzf36RSgWb1ZJhHJEr1YcEHDnHPyMd64Sa0sikeO7ov8ACp5kzdRlO004SGaYuyC2gOyTBD1XBN-lQd0bPPeukWD1Tga3uPZalLJPF8p-hPSYLr-LNaKV2SAFwusljPIraeDjh5dJW9WNUMtjMUYkQG9VVtOMcSEH269e5EdneFRnQlcdZeaEf_fMvg7EWxkEPKwb2JhXUGw6ybXF02Q13jSg1E514CWA_rwHaQ6jacjYZHSmO-ZStUt'
  },
  {
    id: -2,
    name: 'Lin Xiao',
    title: '职业级教练',
    rating: '5.0',
    reviews: '86',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuCxL55pmkWvbeqd4WvQBQuJVOWKStv1-9J81xYrfiCLlqIEcDKgvIQwqBE4kJ6JECTNX0jee8H5Gdd6H0zkcb5d9fCfvtL57QcshMFwbmgPM5FTaw2GSK2ZDoWCCT5ZelhKFE4NEaZiLvcc0cX0mQWXALWGOouJo4wBEopJS7sgbr0g2HTexTQ9BZH1dlkdMvbi_XFc-cDHS3eH44QOYGpvss5MHPP4zvz_eGPKB9trxL-G6wtNaZ2_JO3TVTY6CziDtBNgpeNwiFvT'
  },
  {
    id: -3,
    name: 'Coach Chen',
    title: '青少年教学专家',
    rating: '4.8',
    reviews: '210',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuChWPeRyGiEQEW8oiQLE1Xq_ZkyJl6w-sOmvF0hfei8djt4YkTiUa0NdY-ALuzysmVflkFNGPxoyiagjNJG-E1Xrbd7e_LwtCcNEzIuy_iScI-OH3dYXRzBKOSWIXHspa2wDEyMkotslei7GCwvki9sgfkatkhaCkw9-n49v8TFbwEVNjh0SAPvdqcFkfjAdQOOXhYeI6r2_vNGNU41MTWPY6jeNJ-IamvinsNKj6tyidfMAko_QcYJI7nPDOr_nEeaJsFC3Y7dtYPk'
  },
  {
    id: -4,
    name: 'Li Qiang',
    title: '高级技术分析',
    rating: '4.7',
    reviews: '45',
    avatar: ''
  }
]

interface CoachCard {
  id: number
  name: string
  title: string
  rating: string
  reviews: string
  avatar: string
}

interface ApiCoach {
  id: number
  name: string
}

const apiCoaches = ref<ApiCoach[]>([])
const rawCourses = ref<CourseItem[]>([])

const displayCoaches = computed<CoachCard[]>(() => {
  if (apiCoaches.value.length > 0) {
    return apiCoaches.value.slice(0, 8).map((c, i) => ({
      id: c.id,
      name: c.name,
      title: '专业教练',
      rating: (4.6 + (i % 5) * 0.08).toFixed(1),
      reviews: `${50 + i * 17}`,
      avatar: DEFAULT_COACHES[i % DEFAULT_COACHES.length]?.avatar || ''
    }))
  }
  return DEFAULT_COACHES
})

interface UiCourse {
  id: number
  title: string
  timeRange: string
  location: string
  coachName: string
  coachRole: string
  coachAvatar: string
  price: string
  availText: string
  availPercent: number
  availSoft: boolean
  badge: string
  courseDate: string
}

const MOCK_COURSES: UiCourse[] = [
  {
    id: 9001,
    title: '全能技术进阶班',
    timeRange: '19:00 - 21:00',
    location: '3号场地',
    coachName: 'Zhang Wei',
    coachRole: 'CHIEF INSTRUCTOR',
    coachAvatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDaqgXrt8m4j2qxyLVTUtl7dEDa5rirqE6Oe-WpGB6i5W4IkQj-41fe5UYemfnZDBRSwA6eDU1YncqY1V4_FVVBFbjcS1KSND18mW_O2L9aMQe14oEBws9h4KlRfaqbMjbZpRDd8F8dX-TKCf2cqOKodcZlw8sOn6_-eZyCvHTu68p0fg98oSTktkhNpf46W1sNF5CgCxGRLh7za_JJx9raS_TwsAGf7sE0DMw5DJc4dGmurW8cypwGDd2yS6Mmf8H0eaTNxGuMyPFB',
    price: '1,299',
    availText: '仅剩 2 名额',
    availPercent: 85,
    availSoft: false,
    badge: 'Filling Fast',
    courseDate: ''
  },
  {
    id: 9002,
    title: '青少年羽球启蒙',
    timeRange: '16:00 - 18:00',
    location: '1-2号场地',
    coachName: 'Coach Chen',
    coachRole: 'JUNIOR EXPERT',
    coachAvatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuCEQz_pwuGXlItfhjwFtwio3yEdxLcSrfpN1PwaZqNUydJzjtZ88HmkjkKlPR5GY52A34O-zrLvdYM3bLcQ3NGBH6JWpCUGNrRR2uWlm6oSSUcjPHZKDjt8OLkae196V2eZ_vO-7uZBXHuEDgOVi-cfZBg_6NB8Vm2BCSANq4GUHrdXp1ti3x1AEZ-N_FDCYuIMDzdM1hIbx7JdWbmUtxI9KLyDCJFXXuD9TfDz6mI2jXwxS1w41H3hq6ZeqaeJBCskHTWTSyTIM9Eh',
    price: '899',
    availText: '名额充足',
    availPercent: 30,
    availSoft: true,
    badge: '',
    courseDate: ''
  },
  {
    id: 9003,
    title: '体能爆发力专项训练',
    timeRange: '20:00 - 21:30',
    location: '5号场地',
    coachName: 'Lin Xiao',
    coachRole: 'STRENGTH SPECIALIST',
    coachAvatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuBBkG5hBXblZAdpQBXlWlDr9gsUDlcs8yu6f2dSmN0oQBSROXeFqz-3fCav7PrPJ6ybxjbKC8qxopNs0nZQ7Nb4kw-Bew45yw30QWuMGlzUiHTNY8nMZOf3v5YGIceg25ZXLxe4vTCRv7z4XxubJlISuWsJYZIueNUNVX7vVhFXfU-P5KHdbzmdCU_J7BWxSQWuTO9IBm0RqcBR5oIVxLToOVsT3116eEOK-KBI3D6d67VJ5BG2mEWBzi-xpBwtc82Tw4VrIoiiV6OK',
    price: '1,500',
    availText: '仅剩 1 名额',
    availPercent: 92,
    availSoft: false,
    badge: '',
    courseDate: ''
  }
]

const uiCourses = ref<UiCourse[]>([])

function coachRoleFromCourse(course: CourseItem): string {
  const name = course.courseName || ''
  const lv = course.level || ''
  if (name.includes('青少年') || lv.includes('青')) return 'JUNIOR EXPERT'
  if (name.includes('体能') || lv.includes('体')) return 'STRENGTH SPECIALIST'
  return 'CHIEF INSTRUCTOR'
}

function mapCourseToUi(course: CourseItem): UiCourse {
  const max = course.maxStudents || 1
  const cur = course.currentStudents || 0
  const remaining = Math.max(0, max - cur)
  const occ = max > 0 ? cur / max : 0
  let availText = '名额充足'
  if (remaining <= 1 && max > 0) availText = '仅剩 1 名额'
  else if (remaining === 2) availText = '仅剩 2 名额'
  else if (remaining <= Math.ceil(max * 0.15) && remaining > 2) availText = `仅剩 ${remaining} 名额`
  else if (occ >= 0.45 && remaining > 2) availText = `仅剩 ${remaining} 名额`

  const availPercent = Math.min(100, Math.round(occ * 100))
  const availSoft = occ < 0.45
  const badge =
    remaining <= 2 && max > 0 && occ >= 0.7 ? 'Filling Fast' : occ >= 0.85 && remaining > 2 ? 'Filling Fast' : ''

  const loc = course.courtName || course.location || course.venueName || '场地待定'
  const priceNum = course.coursePrice ?? 0
  const price =
    priceNum >= 1000
      ? priceNum.toLocaleString('zh-CN', { maximumFractionDigits: 0 })
      : `${priceNum}`

  return {
    id: course.id,
    title: course.courseName,
    timeRange: `${course.startTime || ''} - ${course.endTime || ''}`.trim(),
    location: loc,
    coachName: course.coachName || '教练',
    coachRole: coachRoleFromCourse(course),
    coachAvatar: '/static/placeholders/avatar.svg',
    price,
    availText,
    availPercent: Math.max(8, availPercent),
    availSoft,
    badge,
    courseDate: (course.courseDate || '').slice(0, 10)
  }
}

const filteredCourses = computed(() => {
  const sel = weekDates.value[selectedDateIndex.value]
  const kw = searchKeyword.value.trim().toLowerCase()
  return uiCourses.value.filter((c) => {
    if (sel && c.courseDate && c.courseDate !== sel.iso) return false
    if (!kw) return true
    return (
      c.title.toLowerCase().includes(kw) ||
      c.coachName.toLowerCase().includes(kw)
    )
  })
})

function applySearch() {
  /* keyword reactive already filters */
}

function resetFilters() {
  filterCoachId.value = null
  searchKeyword.value = ''
  showFilterSheet.value = false
  loadCourseList()
}

async function loadCourseList() {
  try {
    const params: Record<string, unknown> = { page: 1, size: 50 }
    if (filterCoachId.value != null) params.coachId = filterCoachId.value
    if (searchKeyword.value.trim()) params.keyword = searchKeyword.value.trim()

    const page = await getCourseList(params as any)
    const raw = page as any
    const list = Array.isArray(raw) ? raw : raw?.data ?? []
    rawCourses.value = Array.isArray(list) ? list : []
    if (rawCourses.value.length === 0) {
      uiCourses.value = MOCK_COURSES.map((m, i) => ({
        ...m,
        id: m.id + i,
        courseDate: ''
      }))
    } else {
      uiCourses.value = rawCourses.value.map(mapCourseToUi)
    }
  } catch {
    uiCourses.value = MOCK_COURSES.map((m, i) => ({ ...m, id: m.id + i, courseDate: '' }))
  }
}

async function loadCoachList() {
  try {
    const result = await getCoachList({ page: 1, size: 50 })
    const arr = Array.isArray(result) ? result : []
    apiCoaches.value = arr.map((coach: any) => ({
      id: coach.id,
      name: coach.coachName || coach.name || '教练'
    }))
    if (apiCoaches.value.length === 0) {
      const cached = uni.getStorageSync('coaches_cache')
      if (cached && Array.isArray(cached)) {
        apiCoaches.value = cached.map((coach: any) => ({
          id: coach.id,
          name: coach.coachName || coach.name
        }))
      }
    } else {
      uni.setStorageSync('coaches_cache', arr)
    }
  } catch {
    apiCoaches.value = []
  }
}

function bookCourse(item: UiCourse) {
  if (item.id >= 9000) {
    uni.showToast({ title: '展示数据，请连接后端后预约', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages/course/detail?id=${item.id}`
  })
}

function goNotice() {
  uni.navigateTo({ url: '/pages/notice/index' })
}

function goProfile() {
  uni.navigateTo({ url: '/pages/profile/index' })
}

function onAllCoaches() {
  uni.showToast({ title: '教练列表即将开放', icon: 'none' })
}

function onCoachTap(_c: CoachCard) {
  uni.showToast({ title: '教练主页即将开放', icon: 'none' })
}

onMounted(() => {
  const systemInfo = getSafeSystemInfo()
  statusBarHeight.value = systemInfo.statusBarHeight || 44
  // #ifdef MP
  try {
    const menuInfo = uni.getMenuButtonBoundingClientRect()
    if (menuInfo && systemInfo.windowWidth) {
      navBarMarginRight.value = Math.max(0, systemInfo.windowWidth - menuInfo.left - 12 + 8)
    }
  } catch {
    navBarMarginRight.value = 0
  }
  // #endif

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }
  void loadCoachList()
  void loadCourseList()
})

onPullDownRefresh(() => {
  Promise.all([loadCourseList(), loadCoachList()]).finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.course-center-page {
  background-color: #f9f9f9;
  min-height: 100vh;
}

.text-on-surface {
  color: #1a1c1c;
}
.bg-background {
  background-color: #f9f9f9;
}
.bg-surface-container-lowest {
  background-color: #ffffff;
}
.bg-surface-container-low {
  background-color: #f3f3f3;
}
.text-secondary {
  color: #5f5e5e;
}
.text-primary {
  color: #a33e00;
}
.text-orange-600 {
  color: #ea580c;
}
.bg-on-surface {
  background-color: #1a1c1c;
}

.header-bar {
  background-color: rgba(248, 250, 252, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-sizing: border-box;
}

.brand-shuttle {
  font-size: 22px;
  line-height: 1;
}
.brand-title {
  font-size: 14px;
  font-weight: 900;
  font-style: italic;
  color: #ea580c;
  letter-spacing: 0.12em;
}

.main-scroll {
  box-sizing: border-box;
  height: 100vh;
}

.flex {
  display: flex;
}
.flex-col {
  flex-direction: column;
}
.flex-row {
  flex-direction: row;
}
.flex-1 {
  flex: 1;
}
.flex-shrink-0 {
  flex-shrink: 0;
}
.items-center {
  align-items: center;
}
.items-start {
  align-items: flex-start;
}
.items-end {
  align-items: flex-end;
}
.justify-between {
  justify-content: space-between;
}
.justify-center {
  justify-content: center;
}
.relative {
  position: relative;
}
.absolute {
  position: absolute;
}
.fixed {
  position: fixed;
}
.top-0 {
  top: 0;
}
.left-0 {
  left: 0;
}
.right-0 {
  right: 0;
}
.bottom-24 {
  bottom: 240rpx;
}
.right-6 {
  right: 24rpx;
}
.w-full {
  width: 100%;
}
.z-50 {
  z-index: 50;
}
.z-60 {
  z-index: 200;
}
.fab-layer {
  z-index: 1000;
}

.px-6 {
  padding-left: 24rpx;
  padding-right: 24rpx;
}
.py-4 {
  padding-top: 16rpx;
  padding-bottom: 16rpx;
}
.p-5 {
  padding: 20rpx;
}
.p-8 {
  padding: 32rpx;
}
.p-4 {
  padding: 16rpx;
}
.pb-32 {
  padding-bottom: 240rpx;
}
.pb-4 {
  padding-bottom: 16rpx;
}
.pr-2 {
  padding-right: 8rpx;
}
.pt-2 {
  padding-top: 8rpx;
}
.mb-6 {
  margin-bottom: 24rpx;
}
.mb-3 {
  margin-bottom: 12rpx;
}
.mb-4 {
  margin-bottom: 16rpx;
}
.mb-1 {
  margin-bottom: 4rpx;
}
.mt-1 {
  margin-top: 4rpx;
}
.mt-2 {
  margin-top: 8rpx;
}
.mt-4 {
  margin-top: 16rpx;
}
.mt-8 {
  margin-top: 32rpx;
}
.mt-10 {
  margin-top: 40rpx;
}
.ml-1 {
  margin-left: 4rpx;
}
.gap-1 {
  gap: 4rpx;
}
.gap-2 {
  gap: 8rpx;
}
.gap-3 {
  gap: 12rpx;
}
.gap-4 {
  gap: 16rpx;
}
.space-y-2 > view + view {
  margin-top: 8rpx;
}

.text-10 {
  font-size: 10px;
}
.text-xs {
  font-size: 12px;
}
.text-sm {
  font-size: 14px;
}
.text-base {
  font-size: 16px;
}
.text-lg {
  font-size: 18px;
}
.text-xl {
  font-size: 20px;
}
.text-2xl {
  font-size: 24px;
}

.font-bold {
  font-weight: 700;
}
.font-extrabold {
  font-weight: 800;
}
.font-black {
  font-weight: 900;
}
.font-medium {
  font-weight: 500;
}

.leading-tight {
  line-height: 1.25;
}
.tracking-tight {
  letter-spacing: -0.02em;
}
.tracking-widest {
  letter-spacing: 0.1em;
}
.uppercase {
  text-transform: uppercase;
}
.block {
  display: block;
}

.text-xs-10 {
  font-size: 10px;
}

.min-h-screen {
  min-height: 100vh;
}

.overflow-hidden {
  overflow: hidden;
}

.rounded-xl {
  border-radius: 24rpx;
}
.rounded-2xl {
  border-radius: 32rpx;
}
.rounded-3xl {
  border-radius: 48rpx;
}
.rounded-full {
  border-radius: 9999px;
}
.rounded-bl-xl {
  border-bottom-left-radius: 24rpx;
}

.border-t {
  border-top-width: 1px;
  border-top-style: solid;
}
.border-2 {
  border-width: 2px;
  border-style: solid;
}
.border-surface-container-low {
  border-color: #f3f3f3;
}

.w-40 {
  width: 160px;
}
.w-16 {
  width: 64px;
}
.h-20 {
  height: 80px;
}
.w-10 {
  width: 40px;
}
.h-10 {
  height: 40px;
}
.h-12 {
  height: 48px;
}
.h-11 {
  height: 44px;
}
.h-14 {
  height: 56px;
}
.w-14 {
  width: 56px;
}
.h-1-5 {
  height: 6px;
}

.aspect-square {
  aspect-ratio: 1;
}
.object-cover {
  object-fit: cover;
}

.coach-img {
  width: 100%;
  display: block;
}

.coach-scroll,
.date-scroll {
  width: 100%;
  white-space: nowrap;
}
.coach-row,
.date-row {
  display: inline-flex;
  flex-direction: row;
}

.hide-scrollbar ::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

.date-chip-idle {
  background-color: #ffffff;
  color: #1a1c1c;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
.date-chip-active {
  background-color: #ea580c;
  color: #ffffff;
  box-shadow: 0 8px 24px rgba(234, 88, 12, 0.25);
}
.date-chip-dow {
  opacity: 0.85;
}
.date-chip-idle .date-chip-dow {
  color: #5f5e5e;
}

.badge-fast {
  background-color: #ff6600;
  color: #561d00;
}

.avail-fill {
  background-color: #ff6600;
}
.avail-fill-soft {
  background-color: rgba(255, 102, 0, 0.4);
}

.search-wrap {
  width: 100%;
}
.search-icon-wrap {
  position: absolute;
  left: 16px;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  z-index: 1;
}
.search-input {
  width: 100%;
  height: 56px;
  padding-left: 44px;
  padding-right: 20px;
  background-color: #ffffff;
  border-radius: 24rpx;
  font-size: 15px;
  color: #1a1c1c;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.coach-card-active {
  transform: scale(0.98);
}

.footer-row {
  border-color: #f3f3f3;
}

.book-btn {
  border: none;
  line-height: 48px;
  margin: 0;
}
.book-btn::after {
  border: none;
}
.book-btn-hover {
  opacity: 0.92;
}

.fab-btn {
  background-color: #ff6600;
  box-shadow: 0 20px 40px rgba(255, 102, 0, 0.35);
}

.filter-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 300;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.filter-sheet {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 24rpx 28rpx 48rpx;
  box-sizing: border-box;
  max-height: 70vh;
}
.filter-options {
  max-height: 200px;
}
.filter-chip {
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
  background: #f3f3f3;
  margin-bottom: 12rpx;
  font-size: 14px;
}
.filter-chip.active {
  background: rgba(255, 102, 0, 0.15);
  color: #a33e00;
  font-weight: 700;
}

.scale-95 {
  transform: scale(0.95);
}

.coach-avatar-sm {
  flex-shrink: 0;
}
</style>
