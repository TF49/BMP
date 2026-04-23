<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="round-btn" @tap="handleBack">
          <uni-icons type="left" size="22" color="#ff6600" />
        </view>
        <text class="header-title">赛事详情</text>
        <view class="header-spacer" />
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <view class="content">
        <view v-if="loading" class="state-card">
          <view class="spinner" />
          <text class="state-text">正在加载赛事详情…</text>
        </view>

        <view v-else-if="errorText" class="state-card">
          <text class="state-text">{{ errorText }}</text>
          <view class="state-action" @tap="loadTournamentDetail">重新加载</view>
        </view>

        <template v-else-if="detail">
          <view class="hero-card">
            <image class="hero-image" src="/static/placeholders/hero.svg" mode="aspectFill" />
            <view class="hero-overlay" />
            <view class="hero-content">
              <view class="hero-pill">
                <uni-icons type="checkbox-filled" size="14" color="#561d00" />
                <text>{{ detail.statusText }}</text>
              </view>
              <text class="hero-title">{{ detail.name }}</text>
              <view class="hero-location">
                <uni-icons type="location" size="16" color="#f5f5f5" />
                <text>{{ detail.location }}</text>
              </view>
            </view>
          </view>

          <view class="stats-grid">
            <view class="stat-card">
              <text class="stat-label">日期</text>
              <text class="stat-value">{{ detail.dateRange }}</text>
            </view>
            <view class="stat-card">
              <text class="stat-label">级别</text>
              <text class="stat-value">{{ detail.levelText }}</text>
            </view>
            <view class="stat-card">
              <text class="stat-label">赛制</text>
              <text class="stat-value">{{ detail.modeText }}</text>
            </view>
            <view class="stat-card stat-card-accent">
              <text class="stat-label accent-label">奖金池</text>
              <text class="stat-value accent-value">{{ detail.prizeText }}</text>
            </view>
          </view>

          <view class="section-card">
            <text class="section-title">赛事日程</text>
            <view class="timeline-list">
              <view
                v-for="(group, index) in detail.scheduleGroups"
                :key="`${group.dayLabel}-${index}`"
                class="timeline-group"
                :class="{ separated: index > 0 }"
              >
                <view class="timeline-date">
                  <text class="timeline-day">{{ group.dayLabel }}</text>
                  <text class="timeline-week">{{ group.weekLabel }}</text>
                </view>
                <view class="timeline-events">
                  <view
                    v-for="event in group.events"
                    :key="`${group.dayLabel}-${event.title}`"
                    class="timeline-event"
                  >
                    <text class="event-title">{{ event.title }}</text>
                    <text class="event-time">{{ event.value }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <view class="section-card">
            <text class="section-title">比赛规则</text>
            <view class="rule-list">
              <view
                v-for="(item, index) in detail.rules"
                :key="`${index}-${item}`"
                class="rule-item"
              >
                <view class="rule-icon">
                  <uni-icons type="checkbox-filled" size="16" color="#ff6600" />
                </view>
                <text class="rule-text">{{ item }}</text>
              </view>
            </view>
          </view>

          <view class="action-card">
            <view class="price-block">
              <text class="price-label">报名费</text>
              <view class="price-line">
                <text class="price-sign">¥</text>
                <text class="price-amount">{{ detail.feeText }}</text>
                <text class="price-unit">/人</text>
              </view>
            </view>

            <view class="meta-list">
              <view class="meta-row">
                <text class="meta-row-label">剩余名额</text>
                <text class="meta-row-value accent">{{ detail.remaining }} / {{ detail.maxParticipants }}</text>
              </view>
              <view class="meta-row">
                <text class="meta-row-label">截止日期</text>
                <text class="meta-row-value">{{ detail.deadlineText }}</text>
              </view>
            </view>

            <view class="cta-btn" :class="{ disabled: !detail.canRegister }" @tap="handleRegister">
              <text>{{ detail.canRegister ? detail.actionText : '当前不可报名' }}</text>
              <uni-icons v-if="detail.canRegister" type="right" size="18" color="#561d00" />
            </view>
            <text class="action-tip">{{ detail.tipText }}</text>
          </view>
        </template>
      </view>
    </scroll-view>

    <view v-if="detail" class="bottom-bar">
      <view class="bottom-price">
        <text class="bottom-label">报名费</text>
        <text class="bottom-value">¥{{ detail.feeText }}</text>
      </view>
      <view class="bottom-btn" :class="{ disabled: !detail.canRegister }" @tap="handleRegister">
        <text>{{ detail.canRegister ? detail.actionText : '当前不可报名' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import { getTournamentDetail, type TournamentItem } from '@/api/tournament'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'

type TimelineEvent = {
  title: string
  value: string
}

type TimelineGroup = {
  dayLabel: string
  weekLabel: string
  events: TimelineEvent[]
}

type TournamentDetailVm = {
  id: number
  name: string
  statusText: string
  location: string
  dateRange: string
  levelText: string
  modeText: string
  prizeText: string
  feeText: string
  remaining: number
  maxParticipants: number
  deadlineText: string
  canRegister: boolean
  actionText: string
  tipText: string
  scheduleGroups: TimelineGroup[]
  rules: string[]
}

const userStore = useUserStore()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)
const refreshing = ref(false)
const loading = ref(true)
const errorText = ref('')
const tournamentId = ref(0)
const tournament = ref<TournamentItem | null>(null)

function pad2(value: number) {
  return value < 10 ? `0${value}` : `${value}`
}

function toDate(raw?: string) {
  if (!raw) return null
  const date = new Date(String(raw).replace(/-/g, '/'))
  return Number.isNaN(date.getTime()) ? null : date
}

function formatMoney(value: number) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  })
}

function formatMonthDay(raw?: string) {
  const date = toDate(raw)
  if (!date) return '日期待定'
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function formatFullDate(raw?: string) {
  const date = toDate(raw)
  if (!date) return '待定'
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
}

function formatDateRange(start?: string, end?: string) {
  const startDate = toDate(start)
  const endDate = toDate(end)
  if (!startDate && !endDate) return '日期待定'
  if (startDate && !endDate) return formatMonthDay(start)
  if (!startDate && endDate) return formatMonthDay(end)
  if (!startDate || !endDate) return '日期待定'

  const startMonth = startDate.getMonth() + 1
  const endMonth = endDate.getMonth() + 1
  const startDay = startDate.getDate()
  const endDay = endDate.getDate()

  if (startMonth === endMonth) {
    return `${startMonth}月${startDay}日 - ${endDay}日`
  }
  return `${startMonth}月${startDay}日 - ${endMonth}月${endDay}日`
}

function resolveStatusText(status?: number) {
  const map: Record<number, string> = {
    0: '已取消',
    1: '报名中',
    2: '进行中',
    3: '已结束'
  }
  return map[Number(status)] || '报名中'
}

function resolveModeText(item: TournamentItem) {
  const source = `${item.tournamentType || ''}${item.description || ''}`
  if (/循环/u.test(source)) return '循环赛'
  if (/积分/u.test(source)) return '积分赛'
  if (/团体/u.test(source)) return '团体赛'
  return '淘汰赛'
}

function resolveLevelText(item: TournamentItem) {
  if (item.level?.trim()) return item.level.trim()
  const type = item.tournamentType || ''
  if (/青少年/u.test(type)) return '青少年组'
  if (/双打/u.test(type)) return '业余双打组'
  if (/单打/u.test(type)) return '业余单打组'
  return '业余 A/B组'
}

function resolvePrizeText(item: TournamentItem) {
  const source = `${item.prizeInfo || ''} ${item.prizes || ''}`.trim()
  const moneyMatch = source.match(/¥\s*[\d,]+(?:\.\d+)?/)
  if (moneyMatch) return moneyMatch[0].replace(/\s+/g, '')
  if (source) return source.length > 14 ? `${source.slice(0, 14)}…` : source
  return '待公布'
}

function buildSchedule(item: TournamentItem): TimelineGroup[] {
  return [
    {
      dayLabel: '报名阶段',
      weekLabel: 'Registration',
      events: [
        { title: '报名开始', value: formatFullDate(item.registrationStart) },
        { title: '报名截止', value: formatFullDate(item.registrationEnd || item.registrationDeadline) }
      ]
    },
    {
      dayLabel: '比赛阶段',
      weekLabel: 'Tournament',
      events: [
        { title: '比赛开始', value: formatFullDate(item.tournamentStart || item.startDate) },
        { title: '比赛结束', value: formatFullDate(item.tournamentEnd) }
      ]
    }
  ]
}

function buildRules(item: TournamentItem) {
  const source = String(item.rules || '').trim()
  if (!source) {
    return [
      '当前页面未提供独立赛事规则详情，请以赛事主办方最新通知和现场安排为准。'
    ]
  }

  const normalized = source
    .split(/\r?\n|；|;/)
    .map((itemText) => itemText.replace(/^\d+[.、]\s*/u, '').trim())
    .filter(Boolean)

  return normalized.length > 0 ? normalized.slice(0, 6) : [source]
}

const detail = computed<TournamentDetailVm | null>(() => {
  if (!tournament.value) return null

  const maxParticipants = Math.max(0, Number(tournament.value.maxParticipants || 0))
  const currentParticipants = Math.max(0, Number(tournament.value.currentParticipants || 0))
  const remaining = Math.max(0, maxParticipants - currentParticipants)
  const statusText = resolveStatusText(tournament.value.status)
  const canRegister = Number(tournament.value.status) === 1 && remaining > 0

  return {
    id: tournament.value.id,
    name: tournament.value.tournamentName || '赛事详情',
    statusText,
    location: tournament.value.venueName || tournament.value.location || '场馆待补充',
    dateRange: formatDateRange(tournament.value.tournamentStart || tournament.value.startDate, tournament.value.tournamentEnd),
    levelText: resolveLevelText(tournament.value),
    modeText: resolveModeText(tournament.value),
    prizeText: resolvePrizeText(tournament.value),
    feeText: formatMoney(Number(tournament.value.entryFee || 0)),
    remaining,
    maxParticipants,
    deadlineText: formatFullDate(tournament.value.registrationEnd || tournament.value.registrationDeadline),
    canRegister,
    actionText: canRegister ? '立即报名' : statusText,
    tipText: canRegister ? '报名即表示您同意比赛条款和条件。' : '当前赛事已暂停报名，可返回列表查看其他赛事。',
    scheduleGroups: buildSchedule(tournament.value),
    rules: buildRules(tournament.value)
  }
})

async function loadTournamentDetail() {
  if (!tournamentId.value) {
    loading.value = false
    errorText.value = '缺少赛事参数'
    return
  }

  loading.value = true
  errorText.value = ''
  try {
    tournament.value = await getTournamentDetail(tournamentId.value)
  } catch (error) {
    console.error('加载赛事详情失败:', error)
    errorText.value = error instanceof Error ? error.message : '加载赛事详情失败'
  } finally {
    loading.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/tournament/list')
}

function handleRegister() {
  if (!detail.value) return
  if (!detail.value.canRegister) {
    uni.showToast({
      title: '当前赛事暂不可报名',
      icon: 'none'
    })
    return
  }
  uni.navigateTo({
    url: `/pages/tournament/register?id=${detail.value.id}`
  })
}

function handleRefresh() {
  refreshing.value = true
  loadTournamentDetail().finally(() => {
    refreshing.value = false
  })
}

onLoad(async (options?: Record<string, string | undefined>) => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  tournamentId.value = Number(options?.id || 0)
  await loadTournamentDetail()
})

onPullDownRefresh(() => {
  loadTournamentDetail().finally(() => {
    uni.stopPullDownRefresh()
  })
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fbfbfb 0%, #f3f3f3 100%);
  color: #1a1c1c;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.95);
  backdrop-filter: blur(18px);
}

.header-inner {
  min-height: 112rpx;
  padding: 10rpx 26rpx 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.round-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ghost-btn {
  background: rgba(255, 255, 255, 0.92);
}

.header-spacer {
  width: 72rpx;
  height: 72rpx;
}

.header-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: -1rpx;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 0 18rpx 220rpx;
}

.hero-card,
.section-card,
.action-card,
.stat-card,
.state-card {
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 12rpx 36rpx rgba(15, 23, 42, 0.04);
}

.hero-card {
  position: relative;
  height: 430rpx;
  border-radius: 28rpx;
  overflow: hidden;
  margin-top: 20rpx;
}

.hero-image {
  width: 100%;
  height: 100%;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(11, 18, 32, 0.12) 0%, rgba(11, 18, 32, 0.9) 100%);
}

.hero-content {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: 28rpx;
  z-index: 2;
}

.hero-pill {
  width: fit-content;
  min-width: 144rpx;
  height: 54rpx;
  padding: 0 18rpx;
  border-radius: 9999rpx;
  background: #ff6600;
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #561d00;
  font-size: 22rpx;
  font-weight: 900;
}

.hero-title {
  display: block;
  margin-top: 22rpx;
  font-size: 56rpx;
  line-height: 1.08;
  font-weight: 900;
  color: #ffffff;
  text-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.28);
}

.hero-location {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.88);
}

.stats-grid {
  margin-top: 20rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
}

.stat-card {
  min-height: 150rpx;
  border-radius: 22rpx;
  padding: 24rpx 22rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.stat-card-accent {
  position: relative;
  overflow: hidden;
  background:
    linear-gradient(135deg, rgba(255, 102, 0, 0.08) 0%, rgba(255, 102, 0, 0.02) 100%),
    rgba(255, 255, 255, 0.97);
}

.stat-label {
  font-size: 21rpx;
  font-weight: 800;
  color: #6b7280;
  letter-spacing: 1rpx;
}

.accent-label {
  color: #a33e00;
}

.stat-value {
  font-size: 29rpx;
  line-height: 1.35;
  font-weight: 900;
  color: #1a1c1c;
}

.accent-value {
  color: #a33e00;
  font-size: 34rpx;
}

.section-card {
  margin-top: 22rpx;
  border-radius: 28rpx;
  padding: 30rpx 26rpx;
}

.section-title {
  display: block;
  font-size: 38rpx;
  font-weight: 900;
  color: #101010;
}

.timeline-list {
  margin-top: 28rpx;
}

.timeline-group {
  display: flex;
  gap: 22rpx;
}

.timeline-group.separated {
  margin-top: 28rpx;
  padding-top: 28rpx;
  border-top: 2rpx solid #ededed;
}

.timeline-date {
  width: 160rpx;
  flex-shrink: 0;
}

.timeline-day {
  display: block;
  font-size: 31rpx;
  font-weight: 900;
  color: #a33e00;
}

.timeline-week {
  display: block;
  margin-top: 8rpx;
  font-size: 23rpx;
  color: #6b7280;
}

.timeline-events {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.timeline-event {
  padding: 22rpx 22rpx;
  border-radius: 20rpx;
  background: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.event-title {
  font-size: 28rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.event-time {
  font-size: 25rpx;
  font-weight: 900;
  color: #1a1c1c;
  text-align: right;
}

.rule-list {
  margin-top: 26rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.rule-item {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
}

.rule-icon {
  width: 32rpx;
  height: 32rpx;
  margin-top: 2rpx;
  flex-shrink: 0;
}

.rule-text {
  flex: 1;
  font-size: 25rpx;
  line-height: 1.7;
  color: #5a4136;
}

.action-card {
  margin-top: 22rpx;
  border-radius: 28rpx;
  padding: 32rpx 26rpx 28rpx;
}

.price-label,
.meta-row-label {
  display: block;
  font-size: 22rpx;
  font-weight: 800;
  color: #6b7280;
  letter-spacing: 1rpx;
}

.price-line {
  margin-top: 14rpx;
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.price-sign {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.price-amount {
  font-size: 72rpx;
  line-height: 1;
  font-weight: 900;
  color: #1a1c1c;
  letter-spacing: -2rpx;
}

.price-unit {
  font-size: 24rpx;
  font-weight: 700;
  color: #6b7280;
}

.meta-list {
  margin-top: 26rpx;
}

.meta-row {
  padding: 20rpx 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2rpx solid #efefef;
}

.meta-row:last-child {
  border-bottom: none;
}

.meta-row-value {
  font-size: 27rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.meta-row-value.accent {
  color: #ff6600;
}

.cta-btn {
  margin-top: 24rpx;
  height: 92rpx;
  border-radius: 20rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #561d00;
  box-shadow: 0 12rpx 30rpx rgba(255, 102, 0, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: 31rpx;
  font-weight: 900;
}

.cta-btn.disabled {
  background: #d7d7d7;
  color: #8b8b8b;
  box-shadow: none;
}

.action-tip {
  display: block;
  margin-top: 18rpx;
  font-size: 21rpx;
  line-height: 1.5;
  text-align: center;
  color: #7b7b7b;
}

.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.93);
  backdrop-filter: blur(18px);
  box-shadow: 0 -8rpx 30rpx rgba(26, 28, 28, 0.05);
  padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.bottom-price {
  min-width: 180rpx;
}

.bottom-label {
  display: block;
  font-size: 20rpx;
  color: #6b7280;
  font-weight: 700;
}

.bottom-value {
  display: block;
  margin-top: 8rpx;
  font-size: 46rpx;
  line-height: 1;
  font-weight: 900;
  color: #1a1c1c;
}

.bottom-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  color: #561d00;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 900;
  box-shadow: 0 10rpx 26rpx rgba(255, 102, 0, 0.22);
}

.bottom-btn.disabled {
  background: #d7d7d7;
  color: #8b8b8b;
  box-shadow: none;
}

.state-card {
  margin-top: 24rpx;
  border-radius: 28rpx;
  padding: 90rpx 28rpx;
  text-align: center;
}

.state-text {
  font-size: 28rpx;
  color: #777777;
}

.state-action {
  width: 220rpx;
  height: 76rpx;
  margin: 22rpx auto 0;
  border-radius: 9999rpx;
  background: #ff6600;
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 48rpx;
  height: 48rpx;
  margin: 0 auto 18rpx;
  border: 4rpx solid #ededed;
  border-top-color: #ff6600;
  border-radius: 9999rpx;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
