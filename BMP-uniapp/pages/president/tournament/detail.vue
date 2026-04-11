<template>
  <PresidentLayout :showTabBar="false">
    <view class="t-detail-page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-inner">
          <view class="top-left" @click="onBack">
            <view class="hit">
              <uni-icons type="arrow-left" size="22" color="#ea580c" />
            </view>
            <text class="title">赛事详情</text>
          </view>
          <view class="top-right">
            <view class="hit" @click="onEdit">
              <uni-icons type="compose" size="18" color="#71717a" />
            </view>
            <view class="avatar-badge">
              <text>{{ avatarText }}</text>
            </view>
          </view>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading && !tournament" class="state-wrap">
          <view class="spinner" />
          <text>加载赛事详情中…</text>
        </view>

        <view v-else-if="!tournament" class="state-wrap">
          <text>赛事不存在或已删除</text>
        </view>

        <template v-else>
          <!-- Hero -->
          <view class="hero">
            <image class="hero-img" :src="heroImg" mode="aspectFill" />
            <view class="hero-mask" />
            <view class="hero-content">
              <view class="pill" :class="pillClass">
                <view class="dot" />
                <text>{{ statusText }}</text>
              </view>
              <text class="hero-title">{{ tournament.tournamentName }}</text>
              <text class="hero-sub">{{ venueLine }}</text>
            </view>
          </view>

          <!-- Metrics -->
          <view class="metrics">
            <view class="m-card">
              <text class="k">总营收</text>
              <text class="v">¥{{ money(revenue) }}</text>
              <view class="trend">
                <uni-icons type="arrow-up" size="14" color="#a33e00" />
                <text>较上届增长 12%</text>
              </view>
            </view>

            <view class="m-card wide">
              <view class="left">
                <text class="k">参赛席位</text>
                <text class="v">{{ tournament.currentParticipants }} / {{ tournament.maxParticipants }}</text>
                <text class="sub">已激活报名</text>
              </view>
              <view class="ring">
                <view class="ring-bg" />
                <view class="ring-fg" :style="{ '--p': `${progress}` }" />
                <view class="ring-text">{{ progress }}%</view>
              </view>
            </view>

            <view class="m-card orange">
              <text class="k w">最后机会</text>
              <text class="v w">{{ remaining }}</text>
              <text class="sub w">剩余名额</text>
              <view class="wm">
                <uni-icons type="flag" size="96" color="#ffffff" />
              </view>
            </view>
          </view>

          <!-- Players -->
          <view class="players">
            <view class="p-head">
              <text class="p-title">已报名选手</text>
              <view class="p-tools">
                <view class="search">
                  <uni-icons type="search" size="16" color="#71717a" />
                  <input class="search-input" v-model="keyword" placeholder="搜索选手..." />
                </view>
                <view class="filter-btn" @click="onFilter">
                  <uni-icons type="settings" size="18" color="#5f5e5e" />
                </view>
              </view>
            </view>

            <view v-if="regLoading" class="list-state">加载中…</view>
            <view v-else-if="filteredRegs.length === 0" class="list-state">暂无报名数据</view>
            <view v-else class="list">
              <view class="player" v-for="r in filteredRegs" :key="r.id">
                <view class="p-left">
                  <view class="p-avatar" :class="{ img: Boolean(r.memberName) }">
                    <text>{{ initials(r.memberName) }}</text>
                    <view class="online" />
                  </view>
                  <view class="p-main">
                    <text class="name">{{ r.memberName }}</text>
                    <text class="meta">{{ tournament.tournamentType }} • {{ r.paymentMethod || '—' }}</text>
                  </view>
                </view>
                <view class="p-right">
                  <view class="badge">已确认</view>
                  <view class="more">
                    <uni-icons type="more-filled" size="18" color="#71717a" />
                  </view>
                </view>
              </view>
            </view>

            <view class="view-all" @click="goAllRegs">
              查看全部 {{ tournament.currentParticipants }} 名参与者
            </view>
          </view>
        </template>

        <view class="bottom-space" />
      </scroll-view>

      <!-- Bottom action -->
      <view v-if="tournament" class="action-bar">
        <view class="primary-btn" @click="onBracket">
          <uni-icons type="list" size="18" color="#561d00" />
          <text>对阵编排</text>
        </view>
        <view class="icon-only" @click="onExport">
          <uni-icons type="download" size="20" color="#1a1c1c" />
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getTournamentDetail, getTournamentRegistrationList, type TournamentItem, type TournamentRegistrationItem } from '@/api/tournament'

const loading = ref(true)
const regLoading = ref(true)
const tournamentId = ref(0)
const tournament = ref<TournamentItem | null>(null)
const regs = ref<TournamentRegistrationItem[]>([])
const keyword = ref('')

const heroImg =
  '/static/placeholders/hero.svg'

const revenue = computed(() => {
  // 原型：总营收。后端字段暂未提供，先以报名费*人数推导展示。
  const fee = Number(tournament.value?.entryFee || 0)
  const cnt = Number(tournament.value?.currentParticipants || 0)
  return fee * cnt
})

const remaining = computed(() => {
  const max = Number(tournament.value?.maxParticipants || 0)
  const cur = Number(tournament.value?.currentParticipants || 0)
  return Math.max(0, max - cur)
})

const progress = computed(() => {
  const max = Number(tournament.value?.maxParticipants || 0)
  const cur = Number(tournament.value?.currentParticipants || 0)
  if (max <= 0) return 0
  return Math.max(0, Math.min(100, Math.round((cur / max) * 100)))
})

const venueLine = computed(() => {
  const v = tournament.value?.venueName || '—'
  const s = fmtDate(tournament.value?.tournamentStart)
  const e = fmtDate(tournament.value?.tournamentEnd)
  return `${v} • ${s} - ${e} • ${tournament.value?.tournamentType || '—'}`
})

const statusText = computed(() => {
  const s = tournament.value?.status
  if (s === 1) return '报名中'
  if (s === 2) return '进行中'
  if (s === 3) return '已结束'
  return '已取消'
})

const pillClass = computed(() => {
  const s = tournament.value?.status
  if (s === 1) return 'reg'
  if (s === 2) return 'on'
  if (s === 3) return 'end'
  return 'off'
})

const avatarText = computed(() => 'AU')

const filteredRegs = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return regs.value
  return regs.value.filter((r) => (r.memberName || '').toLowerCase().includes(k))
})

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.TOURNAMENT_LIST)
}

function onEdit() {
  if (!tournamentId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.TOURNAMENT_FORM}?id=${tournamentId.value}` })
}

function onFilter() {
  uni.showToast({ title: '筛选功能开发中', icon: 'none' })
}

function onBracket() {
  uni.showToast({ title: '对阵编排开发中', icon: 'none' })
}

function onExport() {
  uni.showToast({ title: '导出功能开发中', icon: 'none' })
}

function goAllRegs() {
  if (!tournamentId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.TOURNAMENT_REGISTRATION_LIST}?tournamentId=${tournamentId.value}` })
}

function money(v?: number) {
  if (v == null || Number.isNaN(Number(v))) return '0.00'
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function fmtDate(raw?: string) {
  if (!raw) return '—'
  const s = raw.replace('T', ' ')
  return s.slice(0, 10).replace(/-/g, '.')
}

function initials(name?: string) {
  const n = (name || '').trim()
  if (!n) return 'U'
  if (/[\u4e00-\u9fa5]/.test(n)) return n.slice(0, 1)
  const parts = n.split(/\s+/).filter(Boolean)
  const a = parts[0]?.[0] || 'U'
  const b = parts[1]?.[0] || ''
  return (a + b).toUpperCase()
}

async function loadAll() {
  loading.value = true
  regLoading.value = true
  try {
    tournament.value = await getTournamentDetail(tournamentId.value)
  } catch {
    tournament.value = null
  } finally {
    loading.value = false
  }

  try {
    const res = await getTournamentRegistrationList({ tournamentId: tournamentId.value, page: 1, size: 20 })
    regs.value = res.data || []
  } catch {
    regs.value = []
  } finally {
    regLoading.value = false
  }
}

onLoad((q?: Record<string, string | undefined>) => {
  const raw = q?.tournamentId ?? q?.id
  const id = raw ? parseInt(String(raw), 10) : NaN
  if (!Number.isFinite(id) || id <= 0) {
    uni.showToast({ title: '缺少赛事参数', icon: 'none' })
    setTimeout(() => onBack(), 800)
    return
  }
  tournamentId.value = id
  loadAll()
})
</script>

<style lang="scss" scoped>
.t-detail-page { min-height: 100vh; background: #f9f9f9; color: #1a1c1c; padding-bottom: 140rpx; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { position: sticky; top: 0; z-index: 40; background: rgba(248, 250, 252, 0.92); backdrop-filter: blur(14px); }
.top-inner { display: flex; align-items: center; justify-content: space-between; padding: 16rpx 24rpx; }
.top-left,.top-right { display: flex; align-items: center; gap: 12rpx; }
.hit { width: 56rpx; height: 56rpx; border-radius: 9999px; display: flex; align-items: center; justify-content: center; background: #fff; }
.title { font-size: 36rpx; font-weight: 900; color: #ea580c; }
.avatar-badge { width: 56rpx; height: 56rpx; border-radius: 9999px; background: #eeeeee; display: flex; align-items: center; justify-content: center; font-weight: 900; color: #a33e00; }
.scroll { height: calc(100vh - var(--status-bar-height) - 92rpx); padding: 18rpx 24rpx 0; box-sizing: border-box; }
.state-wrap { min-height: 360rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14rpx; color: #71717a; }
.spinner { width: 44rpx; height: 44rpx; border-radius: 9999px; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.hero { position: relative; border-radius: 28rpx; overflow: hidden; aspect-ratio: 21/9; background: #f3f3f3; }
.hero-img { width: 100%; height: 100%; }
.hero-mask { position: absolute; inset: 0; background: linear-gradient(180deg, rgba(0,0,0,.10), rgba(0,0,0,.72)); }
.hero-content { position: absolute; left: 24rpx; right: 24rpx; bottom: 22rpx; }
.pill { display: inline-flex; align-items: center; gap: 10rpx; padding: 8rpx 14rpx; border-radius: 9999px; font-size: 16rpx; font-weight: 900; letter-spacing: .12em; text-transform: uppercase; background: #ff6600; color: #561d00; }
.pill.on { background: #ff6600; }
.pill.reg { background: #ff6600; }
.pill.end { background: rgba(255,255,255,.22); color: #fff; }
.pill.off { background: rgba(255,218,214,.7); color: #93000a; }
.dot { width: 10rpx; height: 10rpx; border-radius: 9999px; background: currentColor; }
.hero-title { display: block; margin-top: 12rpx; color: #fff; font-size: 60rpx; font-weight: 900; letter-spacing: -0.04em; }
.hero-sub { display: block; margin-top: 6rpx; color: rgba(255,255,255,.82); font-size: 22rpx; font-weight: 700; }

.metrics { margin-top: 16rpx; display: grid; grid-template-columns: 1fr; gap: 12rpx; }
.m-card { background: #fff; border-radius: 22rpx; padding: 22rpx; box-shadow: 0 6rpx 20rpx rgba(2,6,23,.05); }
.m-card.orange { background: #a33e00; color: #fff; position: relative; overflow: hidden; }
.m-card.orange .wm { position: absolute; right: -10rpx; bottom: -14rpx; opacity: .12; }
.k { font-size: 16rpx; color: #a1a1aa; font-weight: 900; letter-spacing: .12em; text-transform: uppercase; }
.k.w { color: rgba(255,255,255,.82); }
.v { display: block; margin-top: 10rpx; font-size: 52rpx; font-weight: 900; letter-spacing: -0.03em; }
.v.w { color: #fff; }
.sub { margin-top: 10rpx; font-size: 22rpx; color: #5f5e5e; font-weight: 700; }
.sub.w { color: rgba(255,255,255,.88); }
.trend { margin-top: 10rpx; display: flex; align-items: center; gap: 8rpx; color: #a33e00; font-size: 22rpx; font-weight: 800; }
.m-card.wide { display: flex; justify-content: space-between; align-items: center; }
.m-card.wide .left .sub { margin-top: 6rpx; }
.ring { width: 120rpx; height: 120rpx; position: relative; }
.ring-bg { position: absolute; inset: 0; border-radius: 9999px; border: 12rpx solid #e5e7eb; }
.ring-fg { position: absolute; inset: 0; border-radius: 9999px; border: 12rpx solid #ff6600; clip-path: polygon(50% 50%, 100% 0, 100% 100%, 0 100%, 0 0); opacity: 0.0; }
.ring-text { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; font-size: 30rpx; font-weight: 900; }
/* 简化：用文字显示进度，视觉可再迭代为真实圆环 */

.players { margin-top: 18rpx; }
.p-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14rpx; gap: 12rpx; }
.p-title { font-size: 40rpx; font-weight: 900; letter-spacing: -0.03em; }
.p-tools { display: flex; align-items: center; gap: 10rpx; }
.search { display: flex; align-items: center; gap: 8rpx; background: #eeeeee; padding: 10rpx 14rpx; border-radius: 9999px; }
.search-input { width: 220rpx; font-size: 22rpx; font-weight: 700; }
.filter-btn { width: 56rpx; height: 56rpx; border-radius: 9999px; background: #eeeeee; display: flex; align-items: center; justify-content: center; }
.list-state { text-align: center; color: #a1a1aa; padding: 48rpx 0; font-weight: 700; }
.list { display: flex; flex-direction: column; gap: 10rpx; }
.player { background: #fff; border-radius: 18rpx; padding: 18rpx; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4rpx 14rpx rgba(2,6,23,.04); }
.p-left { display: flex; gap: 12rpx; align-items: center; min-width: 0; }
.p-avatar { width: 56rpx; height: 56rpx; border-radius: 9999px; background: #ffedd5; color: #a33e00; display: flex; align-items: center; justify-content: center; font-weight: 900; position: relative; }
.online { width: 14rpx; height: 14rpx; border-radius: 9999px; background: #22c55e; border: 2rpx solid #fff; position: absolute; right: -2rpx; bottom: -2rpx; }
.p-main { min-width: 0; display: flex; flex-direction: column; }
.name { font-size: 26rpx; font-weight: 900; }
.meta { margin-top: 4rpx; font-size: 16rpx; color: #71717a; font-weight: 800; letter-spacing: .08em; text-transform: uppercase; }
.p-right { display: flex; align-items: center; gap: 10rpx; }
.badge { background: #e2dfde; color: #474746; font-size: 16rpx; font-weight: 900; padding: 6rpx 12rpx; border-radius: 9999px; }
.more { width: 40rpx; height: 40rpx; border-radius: 9999px; display: flex; align-items: center; justify-content: center; background: #f3f3f3; }
.view-all { margin-top: 12rpx; padding: 20rpx; border-radius: 18rpx; border: 2rpx dashed #e3bfb1; color: #5f5e5e; font-weight: 900; text-align: center; }
.bottom-space { height: 120rpx; }

.action-bar { position: fixed; left: 0; right: 0; bottom: 0; padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom)); background: linear-gradient(180deg, rgba(249,249,249,0), rgba(249,249,249,.95) 40%, rgba(249,249,249,1)); display: flex; gap: 12rpx; }
.primary-btn { flex: 1; height: 96rpx; border-radius: 18rpx; background: #ff6600; color: #561d00; display: flex; align-items: center; justify-content: center; gap: 10rpx; font-size: 30rpx; font-weight: 900; box-shadow: 0 16rpx 40rpx rgba(234,88,12,.22); }
.icon-only { width: 96rpx; height: 96rpx; border-radius: 18rpx; background: #e2e2e2; display: flex; align-items: center; justify-content: center; }
</style>

