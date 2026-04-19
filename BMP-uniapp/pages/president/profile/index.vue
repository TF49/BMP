<template>
  <PresidentLayout :showTabBar="true">
    <view class="president-profile">
      <view class="top-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
        <view class="top-bar-left">
          <image class="top-bar-avatar" :src="headerAvatar" mode="aspectFill" />
          <text class="top-bar-title">President Profile</text>
        </view>
        <view class="top-bar-settings" @tap="goSettings">
          <uni-icons type="gear" size="22" color="#ea580c" />
        </view>
      </view>

      <view class="profile-body">
        <view class="profile-inner">
          <view class="user-row">
            <view class="user-avatar-wrap">
              <image class="user-avatar" :src="mainAvatar" mode="aspectFill" />
            </view>
            <view class="user-meta">
              <text class="user-name">{{ displayName }}</text>
              <view class="user-sub">
                <text class="role-badge">协会会长</text>
                <text class="user-id">{{ idLabel }}</text>
              </view>
            </view>
          </view>

          <view class="brief-card">
            <view class="brief-card-bg" />
            <view class="brief-card-inner">
              <text class="brief-label">协会经营简报</text>
              <text class="brief-amount">{{ briefingAmount }}</text>
            </view>
            <view class="brief-actions">
              <view class="brief-btn" hover-class="brief-btn-hover" @tap="goFinanceDetails">
                <text class="brief-btn-text">经营详情</text>
                <uni-icons type="right" size="16" color="#ffffff" />
              </view>
            </view>
          </view>

          <view class="section">
            <text class="section-title">Core Management</text>
            <view class="grid-2">
              <view class="grid-tile" hover-class="grid-tile-hover" @tap="openTabPage(PRESIDENT_PAGES.VENUE_LIST)">
                <view class="tile-icon-wrap">
                  <uni-icons type="location" size="28" color="#a33e00" />
                </view>
                <text class="tile-label">场馆管理</text>
              </view>
              <view class="grid-tile" hover-class="grid-tile-hover" @tap="goMemberManagement">
                <view class="tile-icon-wrap">
                  <uni-icons type="staff" size="28" color="#a33e00" />
                </view>
                <text class="tile-label">会员管理</text>
              </view>
              <view class="grid-tile" hover-class="grid-tile-hover" @tap="goCoachCourses">
                <view class="tile-icon-wrap">
                  <uni-icons type="compose" size="28" color="#a33e00" />
                </view>
                <text class="tile-label">教练课程</text>
              </view>
              <view class="grid-tile" hover-class="grid-tile-hover" @tap="goTournaments">
                <view class="tile-icon-wrap">
                  <uni-icons type="medal" size="28" color="#a33e00" />
                </view>
                <text class="tile-label">赛事活动</text>
              </view>
            </view>
          </view>

          <view class="section">
            <text class="section-title system-tools-gap">System Tools</text>
            <view class="list-card">
              <view class="list-row" hover-class="list-row-hover" @tap="goAuditLog">
                <view class="list-row-left">
                  <view class="list-icon-wrap">
                    <uni-icons type="paperplane" size="20" color="#5f5e5e" />
                  </view>
                  <text class="list-label">财务审计日志</text>
                </view>
                <uni-icons type="right" size="18" color="#5f5e5e" />
              </view>
              <view class="list-divider" />
              <view class="list-row" hover-class="list-row-hover" @tap="goNotificationPublish">
                <view class="list-row-left">
                  <view class="list-icon-wrap">
                    <uni-icons type="sound" size="20" color="#5f5e5e" />
                  </view>
                  <text class="list-label">系统通知发布</text>
                </view>
                <uni-icons type="right" size="18" color="#5f5e5e" />
              </view>
              <view class="list-divider" />
              <view class="list-row" hover-class="list-row-hover" @tap="goDashboard">
                <view class="list-row-left">
                  <view class="list-icon-wrap">
                    <uni-icons type="bars" size="20" color="#5f5e5e" />
                  </view>
                  <text class="list-label">平台数据看板</text>
                </view>
                <uni-icons type="right" size="18" color="#5f5e5e" />
              </view>
            </view>
          </view>

          <view class="section">
            <text class="section-title general-gap">General</text>
            <view class="list-card">
              <view class="list-row" hover-class="list-row-hover" @tap="goSettings">
                <view class="list-row-left">
                  <view class="list-icon-wrap">
                    <uni-icons type="gear" size="20" color="#5f5e5e" />
                  </view>
                  <text class="list-label">系统设置</text>
                </view>
                <uni-icons type="right" size="18" color="#5f5e5e" />
              </view>
              <view class="list-divider" />
              <view class="list-row" hover-class="list-row-hover" @tap="goHelp">
                <view class="list-row-left">
                  <view class="list-icon-wrap">
                    <uni-icons type="help" size="20" color="#5f5e5e" />
                  </view>
                  <text class="list-label">帮助与支持</text>
                </view>
                <uni-icons type="right" size="18" color="#5f5e5e" />
              </view>
            </view>
          </view>

          <view class="logout-wrap">
            <view class="logout-btn" hover-class="logout-btn-hover" @tap="logout">
              <uni-icons type="redo" size="20" color="#1a1c1c" />
              <text class="logout-text">登出账号</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { useUserStore } from '@/store/modules/user'
import { safeReLaunch } from '@/utils/safeRoute'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { PRESIDENT_TAB_BAR_LIST } from '@/utils/presidentTabBar'
import { getPresidentDashboardSummary, type PresidentDashboardSummary } from '@/api/president/dashboard'
import { formatAmount } from '@/utils/format'
import { getSafeSystemInfo } from '@/utils/systemInfo'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const statusBarHeight = ref(44)
const summary = ref<PresidentDashboardSummary | null>(null)
const summaryLoading = ref(false)

const TAB_RELAUNCH_PATHS = new Set(PRESIDENT_TAB_BAR_LIST.map((t) => `/${t.pagePath}`))

const placeholderAvatar = '/static/placeholders/avatar.svg'

const headerAvatar = computed(() => userInfo.value?.avatar || placeholderAvatar)
const mainAvatar = computed(() => userInfo.value?.avatar || placeholderAvatar)

const displayName = computed(() => {
  const u = userInfo.value
  if (!u) return '协会会长'
  return (u.nickname || u.username || '').trim() || '协会会长'
})

const idLabel = computed(() => {
  const u = userInfo.value
  if (!u) return 'ID: —'
  const name = (u.username || '').trim()
  if (name) return `ID: ${name.toUpperCase()}`
  return `ID: ${u.id}`
})

function toNumber(value: unknown): number | null {
  if (value === null || value === undefined || value === '') return null
  const num = Number(value)
  return Number.isFinite(num) ? num : null
}

function pickNumber(source: Record<string, unknown> | null | undefined, keys: string[]): number | null {
  if (!source) return null
  for (const key of keys) {
    const result = toNumber(source[key])
    if (result !== null) return result
  }
  return null
}

const financeSummary = computed(() => summary.value?.finance as Record<string, unknown> | null | undefined)

const briefingAmount = computed(() => {
  const income = pickNumber(financeSummary.value, ['totalIncome', 'income', 'todayIncome', 'totalAmount'])
  if (income !== null) return `¥${formatAmount(income)}`
  const net = pickNumber(financeSummary.value, ['netIncome', 'profit'])
  if (net !== null) return `¥${formatAmount(net)}`
  if (summaryLoading.value) return '加载中…'
  return '¥0.00'
})

function openPage(url: string) {
  const target = url.startsWith('/') ? url : `/${url}`
  if (TAB_RELAUNCH_PATHS.has(target)) {
    safeReLaunch(target, '/pages/index/index')
    return
  }
  uni.navigateTo({
    url: target,
    fail: () => {
      uni.showToast({ title: '页面打开失败', icon: 'none' })
    }
  })
}

function openTabPage(url: string) {
  openPage(url)
}

function goFinanceDetails() {
  openPage(PRESIDENT_PAGES.FINANCE_LIST)
}

function goMemberManagement() {
  openPage(PRESIDENT_PAGES.MEMBER_LIST)
}

function goCoachCourses() {
  openPage(PRESIDENT_PAGES.COURSE_LIST)
}

function goTournaments() {
  openPage(PRESIDENT_PAGES.TOURNAMENT_LIST)
}

function goAuditLog() {
  openPage(PRESIDENT_PAGES.FINANCE_AUDIT_LOG)
}

function goNotificationPublish() {
  openPage(PRESIDENT_PAGES.NOTIFICATION_FORM)
}

function goDashboard() {
  openPage(PRESIDENT_PAGES.DASHBOARD)
}

function goSettings() {
  openPage('/pages/settings/index')
}

function goHelp() {
  openPage('/pages/settings/help')
}

async function loadSummary() {
  summaryLoading.value = true
  try {
    summary.value = await getPresidentDashboardSummary()
  } catch {
    summary.value = null
  } finally {
    summaryLoading.value = false
  }
}

function logout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        safeReLaunch('/pages/login/login', '/pages/login/login')
      }
    }
  })
}

onShow(() => {
  loadSummary()
})

const systemInfo = getSafeSystemInfo()
statusBarHeight.value = systemInfo.statusBarHeight || 44
</script>

<style lang="scss" scoped>
.president-profile {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  font-family:
    'Lexend',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 40;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 48rpx 24rpx;
  background: #f3f3f3;
  box-sizing: border-box;
}

.top-bar-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
  min-width: 0;
}

.top-bar-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 9999px;
  background: #e2e2e2;
}

.top-bar-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #ea580c;
  letter-spacing: -0.02em;
}

.top-bar-settings {
  padding: 16rpx;
  border-radius: 9999px;
  flex-shrink: 0;
}

.profile-body {
  flex: 1;
}

.profile-inner {
  padding: 24rpx 32rpx 48rpx;
}

.user-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 40rpx;
  padding-top: 16rpx;
  padding-bottom: 8rpx;
}

.user-avatar-wrap {
  width: 160rpx;
  height: 160rpx;
  border-radius: 9999px;
  overflow: hidden;
  border: 2rpx solid rgba(227, 191, 177, 0.35);
  background: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.user-avatar {
  width: 100%;
  height: 100%;
}

.user-meta {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #1a1c1c;
  margin-bottom: 12rpx;
}

.user-sub {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  gap: 16rpx;
}

.role-badge {
  padding: 8rpx 16rpx;
  border-radius: 9999px;
  font-size: 22rpx;
  font-weight: 600;
  color: #636262;
  background: #e2dfde;
}

.user-id {
  font-size: 26rpx;
  color: #5f5e5e;
  font-weight: 500;
}

.brief-card {
  margin-top: 32rpx;
  border-radius: 24rpx;
  padding: 48rpx;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 16rpx 60rpx rgba(163, 62, 0, 0.18);
}

.brief-card-bg {
  position: absolute;
  right: -80rpx;
  top: -80rpx;
  width: 320rpx;
  height: 320rpx;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.1);
  filter: blur(48rpx);
}

.brief-card-inner {
  position: relative;
  z-index: 1;
}

.brief-label {
  display: block;
  font-size: 24rpx;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.92);
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 16rpx;
}

.brief-amount {
  display: block;
  font-size: 56rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -0.03em;
}

.brief-actions {
  margin-top: 48rpx;
  display: flex;
  justify-content: flex-end;
  position: relative;
  z-index: 1;
}

.brief-btn {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 36rpx;
  border-radius: 16rpx;
  background: rgba(255, 255, 255, 0.22);
}

.brief-btn-hover {
  opacity: 0.92;
}

.brief-btn-text {
  font-size: 26rpx;
  font-weight: 700;
  color: #ffffff;
}

.section {
  margin-top: 48rpx;
}

.section-title {
  display: block;
  font-size: 34rpx;
  font-weight: 700;
  color: #1a1c1c;
  margin-bottom: 32rpx;
}

.system-tools-gap {
  margin-top: 16rpx;
}

.general-gap {
  margin-top: 16rpx;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32rpx;
}

.grid-tile {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
}

.grid-tile-hover {
  background: #e8e8e8;
}

.tile-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 9999px;
  background: rgba(255, 181, 150, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tile-label {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
  text-align: center;
}

.list-card {
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  padding: 8rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
}

.list-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
}

.list-row-hover {
  background: #f3f3f3;
}

.list-row-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
  min-width: 0;
}

.list-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 9999px;
  background: #eeeeee;
  display: flex;
  align-items: center;
  justify-content: center;
}

.list-label {
  font-size: 30rpx;
  font-weight: 500;
  color: #1a1c1c;
}

.list-divider {
  height: 2rpx;
  margin: 0 16rpx;
  background: rgba(226, 226, 226, 0.9);
}

.logout-wrap {
  margin-top: 48rpx;
  margin-bottom: 24rpx;
}

.logout-btn {
  width: 100%;
  padding: 32rpx;
  border-radius: 24rpx;
  background: #e2e2e2;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.logout-btn-hover {
  background: #dadada;
}

.logout-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1c;
}
</style>
