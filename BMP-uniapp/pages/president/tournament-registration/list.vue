<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-bar-left">
          <view class="icon-round" @click="goBack">
            <uni-icons type="arrow-left" size="24" color="#ff6600" />
          </view>
          <text class="top-title">管理</text>
        </view>
        <view class="icon-round">
          <uni-icons type="search" size="22" color="#ff6600" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="content">
          <view class="page-head">
            <view class="head-copy">
              <text class="kicker">管理端审核</text>
              <text class="page-title">赛事报名审核</text>
              <text class="page-desc">审核并授权运动员参加即将举行的春季锦标赛。最终审批需在周五前完成。</text>
            </view>
            <button class="filter-btn" @click="onFilter">
              <uni-icons type="settings" size="18" color="#1a1c1c" />
              <text class="filter-text">筛选</text>
            </button>
          </view>

          <view class="metrics">
            <view
              v-for="m in metricItems"
              :key="m.key"
              class="metric-card"
              :class="{ active: activeFilter === m.key }"
              @click="activeFilter = m.key"
            >
              <text class="metric-label" :class="{ 'label-active': activeFilter === m.key }">{{ m.label }}</text>
              <text class="metric-num">{{ m.value }}</text>
            </view>
          </view>

          <view class="table-shell">
            <view class="table-head">
              <text class="th th-name">申请人姓名</text>
              <text class="th th-event">赛事名称</text>
              <text class="th th-score">技术评分</text>
              <text class="th th-date">注册日期</text>
              <text class="th th-status">状态</text>
              <text class="th th-actions">操作</text>
            </view>

            <view v-for="row in pagedRows" :key="row.id" class="data-row" @click="openDetail(row)">
              <view class="td td-name">
                <view class="avatar">{{ row.initials }}</view>
                <view class="name-block">
                  <text class="applicant-name">{{ row.name }}</text>
                  <text class="applicant-id">ID: {{ row.memberId }}</text>
                </view>
              </view>
              <text class="td td-event">{{ row.tournament }}</text>
              <view class="td td-score">
                <text class="score-val">{{ row.score }}</text>
                <text class="score-tier">{{ row.tier }}</text>
              </view>
              <text class="td td-date">{{ row.date }}</text>
              <view class="td td-status">
                <view class="status-pill" :class="`pill-${row.status}`">
                  <text class="pill-txt">{{ statusLabel(row.status) }}</text>
                </view>
              </view>
              <view class="td td-actions" @click.stop>
                <view v-if="row.status === 'pending'" class="act-group">
                  <view class="act-ok" @click="onApprove(row)">
                    <uni-icons type="checkbox-filled" size="22" color="#16a34a" />
                  </view>
                  <view class="act-no" @click="onReject(row)">
                    <uni-icons type="closeempty" size="22" color="#dc2626" />
                  </view>
                </view>
                <view v-else-if="row.status === 'approved'" class="act-hit" @click="onMore(row)">
                  <uni-icons type="more-filled" size="22" color="#5f5e5e" />
                </view>
                <view v-else class="act-hit" @click="openDetail(row)">
                  <uni-icons type="info" size="22" color="#5f5e5e" />
                </view>
              </view>
            </view>

            <view class="table-footer">
              <text class="footer-hint">{{ footerHint }}</text>
              <view class="pager">
                <view
                  v-for="p in pageButtons"
                  :key="p"
                  class="page-btn"
                  :class="{ current: p === currentPage }"
                  @click="currentPage = p"
                >
                  <text class="page-num">{{ p }}</text>
                </view>
                <view class="page-btn" :class="{ disabled: currentPage >= totalPages }" @click="nextPage">
                  <uni-icons type="right" size="16" :color="currentPage >= totalPages ? '#c8c6c5' : '#1a1c1c'" />
                </view>
              </view>
            </view>
          </view>

          <view class="bottom-space" />
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

type RegStatus = 'pending' | 'approved' | 'rejected'
type FilterKey = 'all' | RegStatus

type RegRow = {
  id: string
  initials: string
  name: string
  memberId: string
  tournament: string
  score: string
  tier: string
  date: string
  status: RegStatus
}

const stats = {
  totalApplicants: '1,284',
  pending: 42,
  approved: '1,192',
  rejected: '50'
}

const activeFilter = ref<FilterKey>('pending')
const currentPage = ref(1)
const pageSize = 4

const allRows = ref<RegRow[]>([
  {
    id: '918',
    initials: 'ML',
    name: 'Marcus Løvberg',
    memberId: 'KL-2024-918',
    tournament: 'Nordic Smash Masters',
    score: '2,450',
    tier: '精英',
    date: '2023年10月12日',
    status: 'pending'
  },
  {
    id: '042',
    initials: 'ST',
    name: 'Sarah Tan',
    memberId: 'KL-2024-042',
    tournament: 'East Asia Invitational',
    score: '2,120',
    tier: '职业',
    date: '2023年10月11日',
    status: 'approved'
  },
  {
    id: '115',
    initials: 'DK',
    name: 'David Kim',
    memberId: 'KL-2024-115',
    tournament: 'World Junior Qualifiers',
    score: '1,890',
    tier: '挑战者',
    date: '2023年10月10日',
    status: 'rejected'
  },
  {
    id: '882',
    initials: 'AV',
    name: 'Amara Vance',
    memberId: 'KL-2024-882',
    tournament: 'Nordic Smash Masters',
    score: '2,310',
    tier: '精英',
    date: '2023年10月10日',
    status: 'pending'
  }
])

const metricItems = computed(() => [
  { key: 'all' as const, label: '总申请人数', value: stats.totalApplicants },
  { key: 'pending' as const, label: '待审核', value: String(stats.pending) },
  { key: 'approved' as const, label: '已通过', value: stats.approved },
  { key: 'rejected' as const, label: '已拒绝', value: stats.rejected }
])

const filteredRows = computed(() => {
  if (activeFilter.value === 'all') return allRows.value
  return allRows.value.filter(r => r.status === activeFilter.value)
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize)))

const pagedRows = computed(() => {
  const page = Math.min(currentPage.value, totalPages.value)
  const start = (page - 1) * pageSize
  return filteredRows.value.slice(start, start + pageSize)
})

const pageButtons = computed(() => {
  const n = totalPages.value
  const cur = Math.min(currentPage.value, n)
  if (n <= 3) return Array.from({ length: n }, (_, i) => i + 1)
  if (cur <= 2) return [1, 2, 3]
  if (cur >= n - 1) return [n - 2, n - 1, n]
  return [cur - 1, cur, cur + 1]
})

const footerHint = computed(() => {
  if (activeFilter.value === 'pending') {
    const n = pagedRows.value.length
    return `显示 ${stats.pending} 条待审核记录中的 ${n} 条`
  }
  const total = filteredRows.value.length
  const n = pagedRows.value.length
  return `本页 ${n} 条 · 筛选共 ${total} 条`
})

watch([activeFilter, () => filteredRows.value.length], () => {
  currentPage.value = 1
})

function statusLabel(s: RegStatus) {
  if (s === 'pending') return '待审核'
  if (s === 'approved') return '已通过'
  return '已拒绝'
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function onFilter() {
  uni.showToast({ title: '筛选条件开发中', icon: 'none' })
}

function openDetail(row: RegRow) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.TOURNAMENT_REGISTRATION_DETAIL}?id=${encodeURIComponent(row.id)}` })
}

function onApprove(row: RegRow) {
  row.status = 'approved'
  uni.showToast({ title: '已通过', icon: 'success' })
}

function onReject(row: RegRow) {
  row.status = 'rejected'
  uni.showToast({ title: '已拒绝', icon: 'none' })
}

function onMore(row: RegRow) {
  uni.showActionSheet({
    itemList: ['查看详情'],
    success(res) {
      if (res.tapIndex === 0) openDetail(row)
    }
  })
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value += 1
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  color: #1a1c1c;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}
.top-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 28rpx 20rpx;
  background: #f9f9f9;
  position: sticky;
  top: 0;
  z-index: 40;
}
.top-bar-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}
.icon-round {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-round:active {
  background: rgba(0, 0, 0, 0.05);
}
.top-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}
.scroll {
  flex: 1;
  height: 0;
}
.content {
  padding: 8rpx 28rpx 40rpx;
}

.page-head {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  margin-bottom: 40rpx;
}
.head-copy {
  max-width: 100%;
}
.kicker {
  display: block;
  font-size: 22rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 12rpx;
}
.page-title {
  display: block;
  font-size: 64rpx;
  font-weight: 800;
  letter-spacing: -0.04em;
  line-height: 1.1;
  margin-bottom: 16rpx;
}
.page-desc {
  font-size: 26rpx;
  color: #5f5e5e;
  line-height: 1.55;
}
.filter-btn {
  margin: 0;
  align-self: flex-start;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  padding: 22rpx 32rpx;
  background: #e2e2e2;
  border-radius: 16rpx;
  border: none;
}
.filter-btn::after {
  border: none;
}
.filter-text {
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.metrics {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 32rpx;
}
.metric-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  border-left: 6rpx solid transparent;
  box-shadow: 0 4rpx 12rpx rgba(26, 28, 28, 0.04);
}
.metric-card.active {
  border-left-color: #ff6600;
}
.metric-label {
  font-size: 22rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}
.metric-label.label-active {
  color: #a33e00;
}
.metric-num {
  display: block;
  margin-top: 12rpx;
  font-size: 48rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.table-shell {
  background: #ffffff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(26, 28, 28, 0.06);
}
.table-head {
  display: none;
}
.data-row {
  padding: 28rpx;
  border-bottom: 1rpx solid #f3f3f3;
}
.data-row:last-child {
  border-bottom: none;
}
.data-row:active {
  background: #fafafa;
}

.td-name {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 20rpx;
}
.avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 9999px;
  background: #e2dfde;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  font-weight: 800;
  color: #5f5e5e;
}
.name-block {
  flex: 1;
  min-width: 0;
}
.applicant-name {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.applicant-id {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #5f5e5e;
}
.td-event {
  display: block;
  margin-top: 20rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
}
.td-score {
  margin-top: 16rpx;
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 10rpx;
}
.score-val {
  font-size: 30rpx;
  font-weight: 800;
  color: #a33e00;
}
.score-tier {
  font-size: 18rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.td-date {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: #5f5e5e;
}
.td-status {
  margin-top: 20rpx;
}
.status-pill {
  align-self: flex-start;
  padding: 10rpx 20rpx;
  border-radius: 9999px;
}
.pill-pending {
  background: rgba(255, 102, 0, 0.12);
}
.pill-pending .pill-txt {
  color: #a33e00;
}
.pill-approved {
  background: #e2dfde;
}
.pill-approved .pill-txt {
  color: #636262;
}
.pill-rejected {
  background: #ffdad6;
}
.pill-rejected .pill-txt {
  color: #93000a;
}
.pill-txt {
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.td-actions {
  margin-top: 20rpx;
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
}
.act-group {
  display: flex;
  flex-direction: row;
  gap: 8rpx;
}
.act-ok,
.act-no,
.act-hit {
  padding: 12rpx;
  border-radius: 12rpx;
}
.act-ok:active {
  background: rgba(22, 163, 74, 0.12);
}
.act-no:active {
  background: rgba(220, 38, 38, 0.1);
}
.act-hit:active {
  background: #f3f3f3;
}

.table-footer {
  margin-top: 0;
  padding: 24rpx 28rpx;
  background: #f3f3f3;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.footer-hint {
  font-size: 22rpx;
  color: #5f5e5e;
  line-height: 1.4;
}
.pager {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
}
.page-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}
.page-btn.current {
  box-shadow: 0 2rpx 8rpx rgba(26, 28, 28, 0.08);
  font-weight: 800;
}
.page-btn.disabled {
  opacity: 0.45;
}
.page-num {
  font-size: 22rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.bottom-space {
  height: 32rpx;
}

@media screen and (min-width: 768px) {
  .page-head {
    flex-direction: row;
    align-items: flex-end;
    justify-content: space-between;
  }
  .metrics {
    flex-direction: row;
  }
  .metric-card {
    flex: 1;
  }
  .table-head {
    display: grid;
    grid-template-columns: 1.4fr 1.2fr 0.9fr 0.9fr 0.7fr 0.75fr;
    gap: 12rpx;
    padding: 24rpx 28rpx;
    background: #f3f3f3;
  }
  .th {
    font-size: 20rpx;
    font-weight: 800;
    color: #5f5e5e;
    letter-spacing: 0.08em;
    text-transform: uppercase;
  }
  .th-actions {
    text-align: right;
  }
  .data-row {
    display: grid;
    grid-template-columns: 1.4fr 1.2fr 0.9fr 0.9fr 0.7fr 0.75fr;
    gap: 12rpx;
    align-items: center;
    padding: 28rpx;
  }
  .td-event,
  .td-score,
  .td-date,
  .td-status,
  .td-actions {
    margin-top: 0;
  }
  .td-score {
    flex-direction: column;
    align-items: flex-start;
    gap: 4rpx;
  }
  .table-footer {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
