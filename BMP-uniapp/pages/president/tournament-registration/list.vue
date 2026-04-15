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
              <text class="page-desc">审核并授权运动员参加即将举行的赛事，审核结果以真实后端状态为准。</text>
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
              <text class="th th-date">报名日期</text>
              <text class="th th-status">状态</text>
              <text class="th th-actions">操作</text>
            </view>

            <view v-if="loading" class="loading-state">
              <text class="loading-text">加载中...</text>
            </view>

            <view v-else-if="pagedRows.length === 0" class="empty-state">
              <text class="empty-text">暂无报名数据</text>
            </view>

            <template v-else>
              <view v-for="row in pagedRows" :key="row.id" class="data-row" @click="openDetail(row)">
                <view class="td td-name">
                  <view class="avatar">{{ row.initials }}</view>
                  <view class="name-block">
                    <text class="applicant-name">{{ row.name }}</text>
                    <text class="applicant-id">{{ row.memberId }}</text>
                  </view>
                </view>
                <text class="td td-event">{{ row.tournament }}</text>
                <text class="td td-date">{{ row.date }}</text>
                <view class="td td-status">
                  <view class="status-pill" :class="`pill-${row.status}`">
                    <text class="pill-txt">{{ statusLabel(row) }}</text>
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
            </template>

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
import { computed, onMounted, ref, watch } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import {
  getTournamentRegistrationList,
  updateTournamentRegistrationStatus,
  type TournamentRegistrationItem
} from '@/api/president/tournament'
import { parsePagedList } from '@/utils/parsePagedList'

type RegStatus = 'pending' | 'approved' | 'rejected' | 'readonly'
type FilterKey = 'all' | 'pending' | 'approved' | 'rejected'

type RegRow = {
  id: string
  initials: string
  name: string
  memberId: string
  tournament: string
  date: string
  status: RegStatus
  rawStatus: number
}

const loading = ref(false)
const actionLoadingId = ref('')
const activeFilter = ref<FilterKey>('pending')
const currentPage = ref(1)
const pageSize = 10
const allRows = ref<RegRow[]>([])

function getInitials(name?: string): string {
  if (!name) return 'U'
  const trimmed = name.trim()
  if (/[\u4e00-\u9fa5]/.test(trimmed)) return trimmed.slice(0, 1)
  const parts = trimmed.split(/\s+/).filter(Boolean)
  const first = parts[0]?.[0] || 'U'
  const second = parts[1]?.[0] || ''
  return (first + second).toUpperCase()
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`
  } catch {
    return dateStr
  }
}

function mapRegistrationStatus(status?: number): RegStatus {
  if (status === 1) return 'pending'
  if (status === 2) return 'approved'
  if (status === 0) return 'rejected'
  return 'readonly'
}

function transformRegistration(item: TournamentRegistrationItem): RegRow {
  const rawStatus = Number(item.status ?? -1)
  return {
    id: String(item.id),
    initials: getInitials(item.memberName),
    name: item.memberName || '未知会员',
    memberId: `ID-${item.memberId || '-'}`,
    tournament: item.tournamentName || '未知赛事',
    date: formatDate(item.createTime),
    status: mapRegistrationStatus(rawStatus),
    rawStatus
  }
}

async function loadRegistrations() {
  loading.value = true
  try {
    const res = await getTournamentRegistrationList({ page: 1, size: 100 })
    const { list } = parsePagedList<TournamentRegistrationItem>(res)
    allRows.value = list.map(transformRegistration)
  } catch (error) {
    console.error('Failed to load tournament registrations:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const stats = computed(() => {
  const total = allRows.value.length
  const pending = allRows.value.filter((row) => row.status === 'pending').length
  const approved = allRows.value.filter((row) => row.status === 'approved').length
  const rejected = allRows.value.filter((row) => row.status === 'rejected').length

  return {
    totalApplicants: total.toLocaleString(),
    pending,
    approved: approved.toLocaleString(),
    rejected: rejected.toLocaleString()
  }
})

const metricItems = computed(() => [
  { key: 'all' as const, label: '总申请人数', value: stats.value.totalApplicants },
  { key: 'pending' as const, label: '待审核', value: String(stats.value.pending) },
  { key: 'approved' as const, label: '已通过', value: stats.value.approved },
  { key: 'rejected' as const, label: '已拒绝', value: stats.value.rejected }
])

const filteredRows = computed(() => {
  if (activeFilter.value === 'all') return allRows.value
  return allRows.value.filter((row) => row.status === activeFilter.value)
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize)))

const pagedRows = computed(() => {
  const page = Math.min(currentPage.value, totalPages.value)
  const start = (page - 1) * pageSize
  return filteredRows.value.slice(start, start + pageSize)
})

const pageButtons = computed(() => {
  const total = totalPages.value
  const current = Math.min(currentPage.value, total)
  if (total <= 3) return Array.from({ length: total }, (_, index) => index + 1)
  if (current <= 2) return [1, 2, 3]
  if (current >= total - 1) return [total - 2, total - 1, total]
  return [current - 1, current, current + 1]
})

const footerHint = computed(() => {
  if (activeFilter.value === 'pending') {
    return `显示 ${stats.value.pending} 条待审核记录中的 ${pagedRows.value.length} 条`
  }
  return `本页 ${pagedRows.value.length} 条，筛选后共 ${filteredRows.value.length} 条`
})

watch([activeFilter, () => filteredRows.value.length], () => {
  currentPage.value = 1
})

function statusLabel(row: RegRow) {
  if (row.status === 'pending') return '待审核'
  if (row.status === 'approved') return '已通过'
  if (row.status === 'rejected') return '已拒绝'
  if (row.rawStatus === 3) return '已参赛'
  if (row.rawStatus === 4) return '已退赛'
  return '已处理'
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function onFilter() {
  const options: Array<{ label: string; key: FilterKey }> = [
    { label: '全部报名', key: 'all' },
    { label: '待审核', key: 'pending' },
    { label: '已通过', key: 'approved' },
    { label: '已拒绝', key: 'rejected' }
  ]
  uni.showActionSheet({
    itemList: options.map((item) => item.label),
    success: (res) => {
      const selected = options[res.tapIndex]
      if (!selected) return
      activeFilter.value = selected.key
    }
  })
}

function openDetail(row: RegRow) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.TOURNAMENT_REGISTRATION_DETAIL}?id=${encodeURIComponent(row.id)}` })
}

async function updateStatus(row: RegRow, status: number, title: string) {
  if (actionLoadingId.value) return
  try {
    actionLoadingId.value = row.id
    await updateTournamentRegistrationStatus(Number(row.id), status)
    uni.showToast({ title, icon: 'success' })
    await loadRegistrations()
  } catch (error) {
    console.error('Failed to update tournament registration status:', error)
  } finally {
    actionLoadingId.value = ''
  }
}

function onApprove(row: RegRow) {
  void updateStatus(row, 2, '已通过')
}

function onReject(row: RegRow) {
  void updateStatus(row, 0, '已拒绝')
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

onMounted(() => {
  void loadRegistrations()
})
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
.loading-state,
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 80rpx 28rpx;
}
.loading-text,
.empty-text {
  font-size: 26rpx;
  color: #71717a;
  font-weight: 600;
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
.pill-readonly {
  background: #e8e8e8;
}
.pill-readonly .pill-txt {
  color: #5f5e5e;
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
    grid-template-columns: 1.4fr 1.2fr 0.9fr 0.7fr 0.75fr;
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
    grid-template-columns: 1.4fr 1.2fr 0.9fr 0.7fr 0.75fr;
    gap: 12rpx;
    align-items: center;
    padding: 28rpx;
  }
  .td-event,
  .td-date,
  .td-status,
  .td-actions {
    margin-top: 0;
  }
  .table-footer {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
