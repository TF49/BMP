<template>
  <PresidentLayout :showTabBar="false" className="president-finance-audit-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="icon-btn">
              <uni-icons type="arrow-left" size="24" color="#ff6600" />
            </view>
            <text class="brand-title">BMP Executive</text>
          </view>
          <view class="nav-right">
            <text class="page-tag">财务审计日志</text>
            <view class="icon-btn" @click="onSettings">
              <uni-icons type="gear" size="22" color="#5f5e5e" />
            </view>
          </view>
        </view>
        <view class="nav-divider" />
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <text class="search-label">AUDIT SEARCH ENGINE</text>
          <view class="search-row">
            <view class="search-wrap">
              <uni-icons type="search" size="20" color="#a33e00" class="search-ico" />
              <input
                v-model="keyword"
                class="search-input"
                type="text"
                placeholder="Search transaction ID or operator..."
                placeholder-class="search-ph"
                confirm-type="search"
              />
            </view>
            <button class="filter-btn" @click="onFilters">
              <uni-icons type="bars" size="16" color="#1a1c1c" />
              <text class="filter-t">FILTERS</text>
            </button>
          </view>

          <view class="summary-grid">
            <view class="integrity-card">
              <view class="integrity-inner">
                <text class="integrity-label">INTEGRITY SCORE</text>
                <text class="integrity-val">{{ summary.integrityScore }}</text>
                <text class="integrity-desc">{{ summary.integrityDesc }}</text>
              </view>
              <uni-icons type="auth" size="160" color="#ffffff" class="integrity-watermark" />
            </view>
            <view class="today-card">
              <view>
                <text class="today-label">TODAY'S LOGS</text>
                <text class="today-val">{{ summary.todayLogs }}</text>
              </view>
              <view class="today-trend">
                <text class="trend-t">{{ summary.trendText }}</text>
                <uni-icons type="arrow-up" size="14" color="#ff6600" />
              </view>
            </view>
          </view>

          <view class="timeline-head">
            <text class="timeline-title">TIMELINE STREAM</text>
            <text class="timeline-updated">UPDATED JUST NOW</text>
          </view>

          <view v-if="filteredEntries.length === 0" class="empty-hint">
            <text>无匹配记录</text>
          </view>

          <view
            v-for="item in filteredEntries"
            :key="item.id"
            class="entry-card"
            :class="'entry-' + item.action"
          >
            <view class="entry-top">
              <view class="entry-main">
                <view class="entry-icon-wrap" :class="'icon-bg-' + item.action">
                  <uni-icons :type="item.iconType" size="22" :color="iconColor(item.action)" />
                </view>
                <view class="entry-text">
                  <text class="entry-title">{{ item.title }}</text>
                  <text class="entry-sub">{{ item.refId }} · {{ item.operator }}</text>
                </view>
              </view>
              <view class="entry-meta">
                <view class="time-pill">
                  <text class="time-pill-t">{{ item.timeLabel }}</text>
                </view>
                <text class="action-en" :class="'action-t-' + item.action">{{ item.actionLabelEn }}</text>
              </view>
            </view>
            <view class="summary-box" :class="'sum-border-' + item.action">
              <text class="sum-k">{{ item.summaryKind }}</text>
              <text class="sum-v" :class="{ mono: item.mono }">{{ item.summary }}</text>
            </view>
          </view>

          <view class="bottom-pad" />
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

export type AuditActionType = 'create' | 'update' | 'delete'

export interface AuditLogEntry {
  id: string
  action: AuditActionType
  iconType: string
  title: string
  refId: string
  operator: string
  timeLabel: string
  actionLabelEn: string
  summaryKind: string
  summary: string
  mono?: boolean
}

const keyword = ref('')
const actionFilter = ref<AuditActionType | 'all'>('all')

const summary = {
  integrityScore: '99.98%',
  integrityDesc:
    'Financial logs are synchronized across all nodes. No discrepancies detected in the last 24 hours.',
  todayLogs: '142',
  trendText: '+12% VS YESTERDAY'
}

const entries = ref<AuditLogEntry[]>([
  {
    id: '1',
    action: 'create',
    iconType: 'plusempty',
    title: '新增场地预约单',
    refId: 'BK20260301001',
    operator: '李经理 (场馆管理员)',
    timeLabel: '今天 14:20',
    actionLabelEn: 'CREATE',
    summaryKind: 'SUMMARY',
    summary: '新生成羽毛球 04 号场地订单'
  },
  {
    id: '2',
    action: 'update',
    iconType: 'compose',
    title: '修改预约时长',
    refId: 'BK20260301005',
    operator: '李经理 (场馆管理员)',
    timeLabel: '今天 13:45',
    actionLabelEn: 'UPDATE',
    summaryKind: 'CHANGE SUMMARY',
    summary: '2h → 3h',
    mono: true
  },
  {
    id: '3',
    action: 'delete',
    iconType: 'trash',
    title: '取消会员充值记录',
    refId: 'TX99281726300',
    operator: '系统维护员',
    timeLabel: '今天 11:10',
    actionLabelEn: 'DELETE',
    summaryKind: 'SUMMARY',
    summary: '作废重复录入的 ¥500.00 充值单'
  },
  {
    id: '4',
    action: 'update',
    iconType: 'wallet',
    title: '更新场地折扣率',
    refId: 'SYS_PARAM_882',
    operator: '财务总监',
    timeLabel: '昨天 18:00',
    actionLabelEn: 'UPDATE',
    summaryKind: 'CHANGE SUMMARY',
    summary: 'Rate: 0.95 → 0.90',
    mono: true
  }
])

const filteredEntries = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  return entries.value.filter((e) => {
    if (actionFilter.value !== 'all' && e.action !== actionFilter.value) return false
    if (!k) return true
    const hay = `${e.title} ${e.refId} ${e.operator} ${e.summary}`.toLowerCase()
    return hay.includes(k)
  })
})

function iconColor(action: AuditActionType): string {
  if (action === 'create') return '#059669'
  if (action === 'delete') return '#e11d48'
  return '#2563eb'
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.FINANCE_LIST)
}

function onSettings() {
  uni.showToast({ title: '审计设置开发中', icon: 'none' })
}

function onFilters() {
  uni.showActionSheet({
    itemList: ['全部类型', '仅 CREATE', '仅 UPDATE', '仅 DELETE'],
    success: (res) => {
      const map: (AuditActionType | 'all')[] = ['all', 'create', 'update', 'delete']
      actionFilter.value = map[res.tapIndex] ?? 'all'
    }
  })
}

// TODO: 对接 GET /api/president/finance/audit-logs 分页与筛选
</script>

<style lang="scss" scoped>
.president-finance-audit-layout {
  :deep(.president-layout-content) {
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
}

.page {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f9f9f9;
  font-family: Lexend, 'PingFang SC', system-ui, sans-serif;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.nav-header {
  flex-shrink: 0;
  background: #f9f9f9;
  z-index: 50;
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 48rpx 20rpx;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-width: 0;
}

.brand-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: -0.02em;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.page-tag {
  font-size: 22rpx;
  font-weight: 800;
  color: #ff6600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background: rgba(0, 0, 0, 0.06);
  }
}

.nav-divider {
  height: 2rpx;
  background: #f3f3f3;
  margin: 0 48rpx;
}

.main-scroll {
  flex: 1;
  height: 0;
  min-height: 200rpx;
}

.scroll-inner {
  padding: 24rpx 48rpx 48rpx;
}

.search-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.14em;
  margin-bottom: 16rpx;
}

.search-row {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  margin-bottom: 40rpx;
}

.search-wrap {
  position: relative;
  width: 100%;
}

.search-ico {
  position: absolute;
  left: 28rpx;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}

.search-input {
  width: 100%;
  height: 96rpx;
  padding: 0 28rpx 0 88rpx;
  background: #ffffff;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  box-sizing: border-box;
}

.search-ph {
  color: #e3bfb1;
  font-size: 26rpx;
}

.filter-btn {
  align-self: flex-end;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 36rpx;
  background: #e8e8e8;
  border-radius: 16rpx;
  border: none;
  margin: 0;
  &::after {
    border: none;
  }
}

.filter-t {
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 0.14em;
  color: #1a1c1c;
}

.summary-grid {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-bottom: 48rpx;
}

.integrity-card {
  position: relative;
  overflow: hidden;
  border-radius: 24rpx;
  padding: 40rpx 48rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 12rpx 40rpx rgba(163, 62, 0, 0.3);
}

.integrity-inner {
  position: relative;
  z-index: 2;
}

.integrity-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: rgba(86, 29, 0, 0.85);
  letter-spacing: 0.16em;
}

.integrity-val {
  display: block;
  margin-top: 12rpx;
  font-size: 72rpx;
  font-weight: 800;
  color: #561d00;
}

.integrity-desc {
  display: block;
  margin-top: 20rpx;
  font-size: 24rpx;
  line-height: 1.55;
  color: rgba(86, 29, 0, 0.92);
  max-width: 520rpx;
}

.integrity-watermark {
  position: absolute;
  right: -24rpx;
  bottom: -24rpx;
  opacity: 0.12;
  pointer-events: none;
}

.today-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx 48rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.today-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.16em;
}

.today-val {
  display: block;
  margin-top: 8rpx;
  font-size: 56rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.today-trend {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.trend-t {
  font-size: 22rpx;
  font-weight: 800;
  color: #ff6600;
  letter-spacing: 0.04em;
}

.timeline-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8rpx 24rpx;
}

.timeline-title {
  font-size: 24rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.12em;
}

.timeline-updated {
  font-size: 20rpx;
  font-weight: 800;
  color: #e3bfb1;
  letter-spacing: 0.08em;
}

.empty-hint {
  padding: 80rpx;
  text-align: center;
  color: #5f5e5e;
  font-size: 28rpx;
}

.entry-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.entry-top {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.entry-main {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
}

.entry-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-bg-create {
  background: #d1fae5;
}

.icon-bg-update {
  background: #dbeafe;
}

.icon-bg-delete {
  background: #ffe4e6;
}

.entry-text {
  flex: 1;
  min-width: 0;
}

.entry-title {
  display: block;
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.entry-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #5f5e5e;
  line-height: 1.4;
}

.entry-meta {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12rpx;
}

.time-pill {
  padding: 8rpx 20rpx;
  background: #e8e8e8;
  border-radius: 9999px;
}

.time-pill-t {
  font-size: 20rpx;
  font-weight: 800;
  color: #5a4136;
}

.action-en {
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.action-t-create {
  color: #059669;
}

.action-t-update {
  color: #2563eb;
}

.action-t-delete {
  color: #e11d48;
}

.summary-box {
  margin-top: 24rpx;
  padding: 20rpx 28rpx;
  background: #f9f9f9;
  border-radius: 16rpx;
  border-left: 8rpx solid #e5e7eb;
}

.sum-border-create {
  border-left-color: #10b981;
}

.sum-border-update {
  border-left-color: #3b82f6;
}

.sum-border-delete {
  border-left-color: #f43f5e;
}

.sum-k {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.08em;
  margin-bottom: 8rpx;
}

.sum-v {
  font-size: 24rpx;
  font-weight: 600;
  color: #1a1c1c;
  line-height: 1.45;
}

.sum-v.mono {
  font-family: ui-monospace, monospace;
}

.bottom-pad {
  height: 48rpx;
}
</style>
