<template>
  <view class="tournament-page bg-surface text-on-surface min-h-screen">
    <!-- TopAppBar -->
    <view
      class="header-bar fixed top-0 left-0 w-full z-50 flex justify-between items-center px-4 py-4"
      :style="{ paddingTop: (statusBarHeight || 44) + 8 + 'px' }"
    >
      <view class="flex items-center gap-2">
        <text class="brand-shuttle">🏸</text>
        <text class="brand-title">KINETIC LOGIC</text>
      </view>
      <view class="flex items-center gap-4" :style="{ marginRight: navBarMarginRight + 'px' }">
        <view hover-class="opacity-80" @tap="showSearchBar = !showSearchBar">
          <uni-icons type="search" size="22" color="#64748b"></uni-icons>
        </view>
        <view hover-class="opacity-80" @tap="goNotice">
          <uni-icons type="chatbubble" size="22" color="#64748b"></uni-icons>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="main-scroll" :style="{ paddingTop: headerOffsetPx + 'px' }" :show-scrollbar="false">
      <view class="px-4 pb-32">
        <!-- Inline search -->
        <view v-if="showSearchBar" class="mb-4 mt-2">
          <view class="search-wrap relative">
            <view class="search-icon-wrap">
              <uni-icons type="search" size="18" color="#94a3b8"></uni-icons>
            </view>
            <input
              v-model="searchKeyword"
              class="search-input"
              type="text"
              placeholder="搜索赛事名称..."
              confirm-type="search"
            />
          </view>
        </view>

        <!-- Hero -->
        <view class="hero-wrap relative mt-6 rounded-3xl overflow-hidden">
          <view class="hero-gradient absolute inset-0 z-10" />
          <image
            class="hero-img absolute inset-0 w-full h-full"
            mode="aspectFill"
            :src="heroImageUrl"
          />
          <view class="hero-inner absolute bottom-0 left-0 p-6 z-20 w-full flex flex-col gap-6">
            <view class="max-w-full">
              <text class="hero-pill inline-block px-3 py-1 text-xs font-bold tracking-widest rounded-full mb-4">{{ heroPillLabel }}</text>
              <text class="hero-title text-white text-4xl font-black italic tracking-tighter leading-none mb-4 block text-shadow-elite">{{ heroTitle }}</text>
              <text class="hero-sub text-white-80 text-base font-medium block max-w-lg">{{ heroSubtitle }}</text>
            </view>
            <view v-if="showHeroCountdown" class="countdown flex gap-4 bg-white-10 backdrop-blur border border-white-20 p-4 rounded-2xl self-start">
              <view class="text-center">
                <text class="text-white text-3xl font-black leading-none block">{{ countdown.d }}</text>
                <text class="text-white-60 text-10 font-bold tracking-widest uppercase mt-1 block">Days</text>
              </view>
              <view class="divider-y" />
              <view class="text-center">
                <text class="text-white text-3xl font-black leading-none block">{{ countdown.h }}</text>
                <text class="text-white-60 text-10 font-bold tracking-widest uppercase mt-1 block">Hours</text>
              </view>
              <view class="divider-y" />
              <view class="text-center">
                <text class="text-white text-3xl font-black leading-none block">{{ countdown.m }}</text>
                <text class="text-white-60 text-10 font-bold tracking-widest uppercase mt-1 block">Mins</text>
              </view>
            </view>
          </view>
        </view>

        <!-- Filters -->
        <view class="mt-12 flex flex-col gap-6">
          <view>
            <text class="section-title text-3xl font-black italic text-secondary tracking-tighter block">UPCOMING EVENTS</text>
            <text class="text-slate-500 font-medium mt-1 block">发现属于你的赛场，共赴羽球之约</text>
          </view>
          <scroll-view scroll-x class="hide-scrollbar pills-scroll" :show-scrollbar="false">
            <view class="pills-row flex gap-2 pb-2">
              <view
                v-for="p in filterPills"
                :key="p.key"
                class="pill px-6 py-2 rounded-full font-bold text-sm whitespace-nowrap flex-shrink-0"
                :class="filterType === p.key ? 'pill-active' : 'pill-idle'"
                hover-class="opacity-90"
                @tap="filterType = p.key"
              >
                <text>{{ p.label }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- Tournament cards -->
        <view class="mt-8 flex flex-col gap-6">
          <view v-if="listState === 'loading'" class="empty-card bg-surface-container-lowest rounded-3xl p-8 text-center">
            <view class="spinner mx-auto mb-4" />
            <text class="text-secondary text-sm block">正在加载真实赛事...</text>
          </view>

          <view v-else-if="listState === 'error'" class="empty-card bg-surface-container-lowest rounded-3xl p-8 text-center">
            <text class="text-secondary text-base font-bold block">赛事加载失败</text>
            <text class="text-slate-500 text-sm mt-2 block">请检查网络后重试，当前不再展示演示赛事数据</text>
            <button
              class="retry-btn mt-6 bg-primary-container text-on-primary-container px-6 py-3 rounded-xl font-black italic tracking-wider text-sm border-none"
              hover-class="register-btn-hover"
              @tap="reloadList"
            >
              重新加载
            </button>
          </view>

          <view v-else-if="filteredCards.length === 0" class="empty-card bg-surface-container-lowest rounded-3xl p-8 text-center">
            <text class="text-secondary text-base font-bold block">{{ searchKeyword.trim() || filterType !== 'all' ? '暂无符合条件的赛事' : '暂无赛事' }}</text>
            <text class="text-slate-500 text-sm mt-2 block">
              {{ searchKeyword.trim() || filterType !== 'all' ? '请尝试调整搜索词或筛选条件' : '当前暂无可展示的真实赛事，请稍后再来查看' }}
            </text>
          </view>

          <view
            v-for="item in filteredCards"
            :key="item.id"
            class="t-card bg-surface-container-lowest rounded-3xl overflow-hidden flex flex-col border-b-4"
            hover-class="t-card-hover"
            @tap="openDetail(item)"
          >
            <view class="relative h-48 overflow-hidden">
              <image class="t-card-img w-full h-full" mode="aspectFill" :src="item.cover" />
              <view class="absolute top-4 left-4 badge-pill flex items-center gap-2">
                <uni-icons :type="item.badgeIcon" size="14" color="#a33e00"></uni-icons>
                <text class="text-10 font-black tracking-widest text-secondary uppercase">{{ item.badgeLabel }}</text>
              </view>
            </view>
            <view class="p-6 flex-1 flex flex-col">
              <text class="text-xl font-bold text-on-surface leading-tight mb-4 block">{{ item.title }}</text>
              <view class="space-y-3 mb-6 flex-1">
                <view class="flex items-start gap-3 text-slate-500">
                  <uni-icons type="medal" size="18" color="#64748b" class="flex-shrink-0 icon-meta"></uni-icons>
                  <text class="text-sm font-medium flex-1">{{ item.threshold }}</text>
                </view>
                <view class="flex items-start gap-3 text-slate-500">
                  <uni-icons type="calendar" size="18" color="#64748b" class="flex-shrink-0 icon-meta"></uni-icons>
                  <text class="text-sm font-medium flex-1">{{ item.dateRange }}</text>
                </view>
                <view class="flex items-start gap-3 text-slate-500">
                  <uni-icons type="location" size="18" color="#64748b" class="flex-shrink-0 icon-meta"></uni-icons>
                  <text class="text-sm font-medium flex-1">{{ item.location }}</text>
                </view>
              </view>
              <view class="mt-auto flex items-center justify-between pt-4 border-t border-surface-container">
                <view>
                  <text class="text-xs font-bold text-slate-400 block tracking-wider">ENTRY FEE</text>
                  <text class="text-2xl font-black text-primary italic leading-none">
                    ¥{{ item.feeDisplay }}<text class="text-xs font-bold italic">{{ item.feeUnit }}</text>
                  </text>
                </view>
                <button
                  class="register-btn bg-primary-container text-on-primary-container px-6 py-3 rounded-xl font-black italic tracking-wider text-sm border-none"
                  hover-class="register-btn-hover"
                  @tap.stop="register(item)"
                >
                  {{ canRegister(item) ? '立即报名' : '查看详情' }}
                </button>
              </view>
            </view>
          </view>
        </view>

        <!-- Bento -->
        <view class="mt-16 mb-12 flex flex-col gap-6">
          <view class="bento-rank bg-slate-900 rounded-3xl p-8 relative overflow-hidden min-h-240" @tap="goRanking">
            <view class="relative z-10">
              <text class="text-primary-container font-black italic tracking-widest text-sm mb-2 block">CLUB RANKING</text>
              <text class="text-white text-3xl font-black italic tracking-tighter mb-4 leading-tight block">查看你的全国积分排名</text>
              <view class="flex items-center gap-2 text-white font-bold">
                <text>立即探索</text>
                <uni-icons type="right" size="18" color="#ffffff"></uni-icons>
              </view>
            </view>
            <image class="bento-rank-deco absolute right-0 bottom-0 opacity-30" mode="aspectFill" :src="bentoRankImg" />
          </view>
          <view class="bento-coupon bg-primary-dark rounded-3xl p-8 relative overflow-hidden min-h-240 flex flex-col justify-end">
            <uni-icons type="cart" size="72" color="rgba(255,255,255,0.2)" class="coupon-deco-icon" />
            <text class="text-white text-2xl font-black italic tracking-tighter mb-2 block">报名优惠券</text>
            <text class="text-white-80 font-medium mb-4 text-sm block">新用户首赛立减 ¥30.00</text>
            <button
              class="coupon-btn w-fit px-6 py-2 bg-white text-primary rounded-xl font-black italic text-sm border-none"
              hover-class="opacity-95"
              @tap.stop="claimCoupon"
            >
              立即领券
            </button>
          </view>
        </view>
      </view>
    </scroll-view>

    <CustomTabBar :current="3" />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import { getTournamentList, type TournamentItem } from '@/api/tournament'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const statusBarHeight = ref(44)
const navBarMarginRight = ref(0)
const headerOffsetPx = computed(() => (statusBarHeight.value || 44) + 56)

const searchKeyword = ref('')
const showSearchBar = ref(false)
const filterType = ref<'all' | 'community' | 'pro' | 'youth'>('all')

const filterPills = [
  { key: 'all' as const, label: '全部赛事' },
  { key: 'community' as const, label: '社区联赛' },
  { key: 'pro' as const, label: '专业排名赛' },
  { key: 'youth' as const, label: '青少年组' }
]

const HERO_IMG =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuAvRoa6r3rVS_vaVuNJnHTTrO8fq2TVNj--LfAsko1-GMyKPjFOVHxvg1p7U9RCu_FhPLnBl9m_sfv-lENkEzig5GBY_hJjyoto_1YlcYeOYnXR-sGrGcH4QFTvs7w8xb7K_2Ve_kQo6KXwI1cE_sP3LqieEidRoAw00tfIOd54DLVbax6_e4q83INbaDW1SvI2LoTWhccLal2tQtCxYtHL_1lWQgi_T82I0nIvYNi_87_iyNQE1fYH_ZjD7NIZDvCCNmy6NcMLRX0j'

const CARD_IMGS = [
  'https://lh3.googleusercontent.com/aida-public/AB6AXuBqncMYV4B-qszuxp-kakHBYONFrVxY4AiiUK8aipoUGNT066CIE_Kf6xKuhBbhTCKR82u0ppU1jwhIByIVK0QpuMYwfePJ2Ly5Fr1Ftbe-UWxH9FYLqIM3kT777F44rLjobIanUQVmlriKPt28l_xcQUEA_y-aHp7vPIfROTIg-LVsHDlhWm3U4PX8EJlSqapkLYfGdaxVTK2YpjkGIgb0Dqq_esSAfL9dBycnbnkh1Pf8uNJbGeKG6GARR72N5ZlWxBAmNVFGlWGZ',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCt-4eWu4gWz5QUbja4aNFlZ_qLbcsNTYW9gfGnOSQKT_jBPyM0st_jvZtbTdkNMsph6L3F0j5VK2Y7sLKDOeQunEy__jLDwg98j1q3klkwigZiLX5vaMfNYULr-hdunoV1pmWSGSALl5PBKQs5IF0rVsOfBUf6hmDpAbBa2tW4Cw0lXELfQMAo9I8lCIEYgTy4B2xjiGiAvlxHsCtYEJDQM-mmHp7nxS6-PPYk6_5yzEz4IxhRJ_Mm3S12tbqDHxGFqQg6K3weWlUb',
  'https://lh3.googleusercontent.com/aida-public/AB6AXuAOoRdrEjXymYdvYhC2gUXBtXdyx_K53c2XUrkcrurtYcrlZCbFkAP7ybMBLDw-_EC4fHeHE3GVfnwKko-xjPsG_TeuXaXY4wX8EN8IcbjYFw4lZcDIwDI-fJNA7RzpjgVxkTw2ifW7HeNSzFyefdLBMgV3k_3PdfJE_wuhmH7PW2UmFeys5qWSeJRzen-JFyaxFvJHW27vY4Nlnp8szNGteWOPxC7WUUFNuzYg4itIPXuTbg9DqRXR0wnk19EZV6J7qu66JfiBx0IN'
]

const bentoRankImg =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuD4cQglCuXcaUf74OZr7NCHqL02vNYuQ89tC3oENVZMNKFjMCOpHOz2WiSJlgmVr9DTNMovyAnd5RuYtSVuOvWU3WBQoVKXVYWbRYYULoPQL4M1F3mML4cZcrAZDLR1t7HVguDRsU1dTuGcA-E-u0SvMokDFQ0VPIMcLBFoFNO6TS_6hCpiqXeOqgNIpqRpjJyYwnxpgT1_OftDWbHNxhXXYTgx38bGfY7iMoLtLS63iqhuRLW3G1nrIXJkoQfmamDyy3le9CwnIsqX'

interface UiCard {
  id: number
  title: string
  threshold: string
  dateRange: string
  location: string
  feeDisplay: string
  feeUnit: string
  cover: string
  badgeIcon: string
  badgeLabel: string
  rawType: string
  status: number
  participants: number
  maxParticipants: number
}

const rawList = ref<TournamentItem[]>([])
const uiCards = ref<UiCard[]>([])
const listState = ref<'loading' | 'success' | 'empty' | 'error'>('loading')

function pad2(n: number) {
  return n < 10 ? `0${n}` : `${n}`
}

function formatDateRange(start?: string, end?: string) {
  if (!start) return '日期待定'
  const s = new Date(start.replace(/-/g, '/'))
  if (Number.isNaN(s.getTime())) return '日期待定'
  const y = s.getFullYear()
  const m = pad2(s.getMonth() + 1)
  const d = pad2(s.getDate())
  if (!end) return `${y}.${m}.${d}`
  const e = new Date(end.replace(/-/g, '/'))
  if (Number.isNaN(e.getTime())) return `${y}.${m}.${d}`
  const ye = e.getFullYear()
  const me = pad2(e.getMonth() + 1)
  const de = pad2(e.getDate())
  if (y === ye && m === me) return `${y}.${m}.${d} - ${de}`
  return `${y}.${m}.${d} - ${ye}.${me}.${de}`
}

function badgeFrom(t: TournamentItem): { icon: string; label: string } {
  const type = (t.tournamentType || '') + (t.tournamentName || '')
  if (/青少年|U-?15|未来之星/i.test(type)) return { icon: 'star', label: 'YOUTH UNDER 15' }
  if (/双打|混双|混合/i.test(type)) return { icon: 'staff', label: 'DOUBLE MIXED' }
  if (/单打/i.test(type)) return { icon: 'person', label: 'SINGLE ELITE' }
  return { icon: 'medal', label: (t.tournamentType || 'OPEN').toUpperCase().slice(0, 16) }
}

function feeUnitFrom(t: TournamentItem): string {
  const n = (t.tournamentName || '') + (t.tournamentType || '')
  if (/双打|混双|队|团体/i.test(n)) return '/队'
  return '/人'
}

function mapItem(t: TournamentItem, index: number): UiCard {
  const b = badgeFrom(t)
  const fee = Number(t.entryFee || 0)
  return {
    id: t.id,
    title: t.tournamentName,
    threshold: t.requirements?.trim() || (t.level ? `参赛门槛：${t.level}` : '参赛门槛：详见赛事说明'),
    dateRange: formatDateRange(t.tournamentStart, t.tournamentEnd),
    location: t.venueName || t.location || '地点待定',
    feeDisplay: fee.toFixed(2),
    feeUnit: feeUnitFrom(t),
    cover: CARD_IMGS[index % CARD_IMGS.length],
    badgeIcon: b.icon,
    badgeLabel: b.label,
    rawType: t.tournamentType || '',
    status: t.status,
    participants: t.currentParticipants || 0,
    maxParticipants: t.maxParticipants || 0
  }
}

const featured = ref<TournamentItem | null>(null)

const heroImageUrl = computed(() => HERO_IMG)
const heroPillLabel = computed(() => {
  if (featured.value?.id) return 'HOT RECOMMENDED'
  if (listState.value === 'error') return 'LIVE STATUS'
  if (listState.value === 'empty') return 'EVENT HUB'
  return 'SYNCING NOW'
})
const heroTitle = computed(() => {
  if (featured.value?.tournamentName) return featured.value.tournamentName
  if (listState.value === 'error') return '真实赛事加载失败'
  if (listState.value === 'empty') return '发现下一场真实赛事'
  return '正在同步真实赛事'
})
const heroSubtitle = computed(() => {
  if (!featured.value) {
    if (listState.value === 'error') {
      return '当前未展示任何演示赛事，请重试后查看最新赛事安排。'
    }
    if (listState.value === 'empty') {
      return '当前暂无可展示的真实赛事，后续上新后会第一时间在这里呈现。'
    }
    return '正在从后端同步最新赛事信息，请稍候片刻。'
  }
  const p = featured.value?.prizeInfo?.trim()
  if (p) return p
  const d = (featured.value?.description || '').trim()
  if (d) return d.slice(0, 80) + (d.length > 80 ? '…' : '')
  return '挑战巅峰，见证传奇。最高组别冠军奖金高达 50,000 元。'
})
const showHeroCountdown = computed(() => !!featured.value)

const countdownDeadline = ref<Date>(new Date(Date.now() + 8 * 86400000 + 14 * 3600000 + 52 * 60000))
const countdown = ref({ d: 8, h: 14, m: 52 })

let timer: ReturnType<typeof setInterval> | null = null

function updateCountdown() {
  const end = countdownDeadline.value.getTime()
  const now = Date.now()
  let diff = Math.max(0, end - now)
  const d = Math.floor(diff / 86400000)
  diff -= d * 86400000
  const h = Math.floor(diff / 3600000)
  diff -= h * 3600000
  const m = Math.floor(diff / 60000)
  countdown.value = { d, h, m }
}

watch(
  () => featured.value,
  (v) => {
    const target = v?.tournamentStart || v?.registrationEnd
    if (target) {
      const dt = new Date(target.replace(/-/g, '/'))
      if (!Number.isNaN(dt.getTime()) && dt.getTime() > Date.now()) {
        countdownDeadline.value = dt
      }
    }
    updateCountdown()
  },
  { immediate: true }
)

const filteredCards = computed(() => {
  let list = uiCards.value
  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) list = list.filter((x) => x.title.toLowerCase().includes(kw))
  const t = filterType.value
  if (t === 'all') return list
  if (t === 'community') {
    return list.filter((x) => /社区|双打|友谊/i.test(x.rawType + x.title))
  }
  if (t === 'pro') {
    return list.filter((x) => /专业|排名|巡回|精英|公开/i.test(x.rawType + x.title))
  }
  if (t === 'youth') {
    return list.filter((x) => /青少年|青年|未来|U-?\d/i.test(x.rawType + x.title))
  }
  return list
})

async function loadList() {
  listState.value = 'loading'
  try {
    const page = await getTournamentList({ page: 1, size: 50 })
    const raw = page as any
    const list = Array.isArray(raw) ? raw : raw?.data ?? []
    rawList.value = Array.isArray(list) ? list : []
    if (rawList.value.length === 0) {
      uiCards.value = []
      featured.value = null
      listState.value = 'empty'
      countdownDeadline.value = new Date(Date.now() + 8 * 86400000 + 14 * 3600000 + 52 * 60000)
    } else {
      featured.value = rawList.value[0]
      uiCards.value = rawList.value.map((it, i) => mapItem(it, i))
      listState.value = 'success'
      const target = featured.value.tournamentStart || featured.value.registrationEnd
      if (target) {
        const dt = new Date(target.replace(/-/g, '/'))
        if (!Number.isNaN(dt.getTime()) && dt.getTime() > Date.now()) {
          countdownDeadline.value = dt
        } else {
          countdownDeadline.value = new Date(Date.now() + 8 * 86400000)
        }
      }
    }
    updateCountdown()
  } catch (error) {
    console.error('加载赛事列表失败:', error)
    rawList.value = []
    uiCards.value = []
    featured.value = null
    listState.value = 'error'
    countdownDeadline.value = new Date(Date.now() + 8 * 86400000 + 14 * 3600000 + 52 * 60000)
    updateCountdown()
  }
}

function canRegister(item: UiCard) {
  return item.status === 1 && item.participants < item.maxParticipants
}

function openDetail(item: UiCard) {
  uni.navigateTo({ url: `/pages/tournament/detail?id=${item.id}` })
}

function register(item: UiCard) {
  if (canRegister(item)) {
    uni.navigateTo({ url: `/pages/tournament/register?id=${item.id}` })
  } else {
    uni.navigateTo({ url: `/pages/tournament/detail?id=${item.id}` })
  }
}

function reloadList() {
  void loadList()
}

function goNotice() {
  uni.navigateTo({ url: '/pages/notice/index' })
}

function goRanking() {
  uni.navigateTo({ url: '/pages/profile/index' })
}

function claimCoupon() {
  uni.showToast({ title: '优惠券活动即将上线', icon: 'none' })
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

  void loadList()
  timer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

onPullDownRefresh(() => {
  loadList().finally(() => uni.stopPullDownRefresh())
})
</script>

<style lang="scss" scoped>
.tournament-page {
  background-color: #f9f9f9;
  min-height: 100vh;
}

.text-on-surface {
  color: #1a1c1c;
}
.bg-surface {
  background-color: #f9f9f9;
}
.bg-surface-container-lowest {
  background-color: #ffffff;
}
.text-secondary {
  color: #5f5e5e;
}
.text-primary {
  color: #a33e00;
}
.text-slate-500 {
  color: #64748b;
}
.text-slate-400 {
  color: #94a3b8;
}
.bg-primary-container {
  background-color: #ff6600;
}
.text-on-primary-container {
  color: #561d00;
}
.bg-slate-900 {
  background-color: #0f172a;
}
.text-primary-container {
  color: #ff6600;
}
.bg-primary-dark {
  background-color: #a33e00;
}
.text-white {
  color: #ffffff;
}
.text-white-80 {
  color: rgba(255, 255, 255, 0.8);
}
.text-white-60 {
  color: rgba(255, 255, 255, 0.6);
}
.border-surface-container {
  border-color: #eeeeee;
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

.min-h-screen {
  min-height: 100vh;
}

.fixed {
  position: fixed;
}
.absolute {
  position: absolute;
}
.relative {
  position: relative;
}
.inset-0 {
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
}
.top-0 {
  top: 0;
}
.left-0 {
  left: 0;
}
.bottom-0 {
  bottom: 0;
}
.right-0 {
  right: 0;
}
.w-full {
  width: 100%;
}
.h-full {
  height: 100%;
}
.z-10 {
  z-index: 10;
}
.z-20 {
  z-index: 20;
}
.z-50 {
  z-index: 50;
}

.flex {
  display: flex;
}
.flex-col {
  flex-direction: column;
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
.justify-between {
  justify-content: space-between;
}
.justify-end {
  justify-content: flex-end;
}
.self-start {
  align-self: flex-start;
}

.px-4 {
  padding-left: 16rpx;
  padding-right: 16rpx;
}
.p-6 {
  padding: 24rpx;
}
.p-8 {
  padding: 32rpx;
}
.p-4 {
  padding: 16rpx;
}
.py-4 {
  padding-top: 16rpx;
  padding-bottom: 16rpx;
}
.py-2 {
  padding-top: 8rpx;
  padding-bottom: 8rpx;
}
.py-3 {
  padding-top: 12rpx;
  padding-bottom: 12rpx;
}
.px-6 {
  padding-left: 24rpx;
  padding-right: 24rpx;
}
.px-3 {
  padding-left: 12rpx;
  padding-right: 12rpx;
}
.pb-32 {
  padding-bottom: 240rpx;
}
.pb-2 {
  padding-bottom: 8rpx;
}
.mb-4 {
  margin-bottom: 16rpx;
}
.mb-6 {
  margin-bottom: 24rpx;
}
.mb-2 {
  margin-bottom: 8rpx;
}
.mb-12 {
  margin-bottom: 48rpx;
}
.icon-meta {
  margin-top: 2px;
}
.mt-1 {
  margin-top: 4rpx;
}
.mt-2 {
  margin-top: 8rpx;
}
.mt-6 {
  margin-top: 24rpx;
}
.mt-8 {
  margin-top: 32rpx;
}
.mt-12 {
  margin-top: 48rpx;
}
.mt-16 {
  margin-top: 64rpx;
}
.gap-2 {
  gap: 8rpx;
}
.gap-4 {
  gap: 16rpx;
}
.gap-6 {
  gap: 24rpx;
}

.space-y-3 > view + view {
  margin-top: 12rpx;
}

.rounded-3xl {
  border-radius: 48rpx;
}
.rounded-2xl {
  border-radius: 32rpx;
}
.rounded-xl {
  border-radius: 24rpx;
}
.rounded-full {
  border-radius: 9999px;
}

.overflow-hidden {
  overflow: hidden;
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
.text-3xl {
  font-size: 28px;
}
.text-4xl {
  font-size: 32px;
}
.text-10 {
  font-size: 10px;
}

.font-bold {
  font-weight: 700;
}
.font-black {
  font-weight: 900;
}
.font-medium {
  font-weight: 500;
}

.italic {
  font-style: italic;
}
.leading-none {
  line-height: 1;
}
.leading-tight {
  line-height: 1.25;
}
.tracking-tighter {
  letter-spacing: -0.04em;
}
.tracking-widest {
  letter-spacing: 0.1em;
}
.tracking-wider {
  letter-spacing: 0.05em;
}
.uppercase {
  text-transform: uppercase;
}
.block {
  display: block;
}
.inline-block {
  display: inline-block;
}

.border-t {
  border-top-width: 1px;
  border-top-style: solid;
}
.border-b-4 {
  border-bottom: 4px solid transparent;
}

.hero-wrap {
  aspect-ratio: 16 / 9;
  min-height: 200px;
  background: #111;
}
.hero-gradient {
  background: linear-gradient(to top, rgba(0, 0, 0, 0.85), rgba(0, 0, 0, 0.2), transparent);
}
.hero-img {
  z-index: 1;
}
.hero-pill {
  background-color: #ff6600;
  color: #561d00;
}
.hero-title {
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.35);
}
.text-shadow-elite {
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.bg-white-10 {
  background-color: rgba(255, 255, 255, 0.1);
}
.backdrop-blur {
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}
.border-white-20 {
  border: 1px solid rgba(255, 255, 255, 0.2);
}
.divider-y {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  align-self: center;
}

.section-title {
  font-family: 'Lexend', sans-serif;
}

.pills-scroll {
  width: 100%;
  white-space: nowrap;
}
.pills-row {
  display: inline-flex;
  flex-direction: row;
}
.pill {
  line-height: 1.4;
}
.pill-active {
  background-color: #a33e00;
  color: #ffffff;
}
.pill-idle {
  background-color: #f3f3f3;
  color: #5f5e5e;
}

.hide-scrollbar ::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}

.h-48 {
  height: 192px;
}
.t-card-img {
  display: block;
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
}
.badge-pill {
  background-color: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  padding: 8rpx 16rpx;
  border-radius: 9999px;
}
.t-card-hover {
  opacity: 0.96;
}
.t-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}
.t-card.border-b-4:active,
.t-card-hover:active {
  border-bottom-color: #ff6600;
}

.register-btn {
  box-shadow: 0 10px 20px rgba(255, 102, 0, 0.25);
}
.register-btn::after {
  border: none;
}
.register-btn-hover {
  transform: scale(1.02);
}

.border-none {
  border: none;
}

.min-h-240 {
  min-height: 240px;
}
.bento-rank-deco {
  width: 50%;
  height: 100%;
  pointer-events: none;
}
.coupon-deco-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  transform: rotate(12deg);
}
.coupon-btn::after {
  border: none;
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
  height: 48px;
  padding-left: 44px;
  padding-right: 16px;
  background: #fff;
  border-radius: 24rpx;
  font-size: 15px;
  color: #1a1c1c;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  box-sizing: border-box;
}

.empty-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.spinner {
  width: 44rpx;
  height: 44rpx;
  border: 4rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 9999rpx;
  animation: spin 0.8s linear infinite;
}

.retry-btn::after {
  border: none;
}

.mx-auto {
  margin-left: auto;
  margin-right: auto;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
