<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view v-if="drawerOpen" class="drawer-root" @click="drawerOpen = false">
        <view class="drawer-panel" @click.stop>
          <view class="drawer-head">
            <text class="drawer-brand">KINETIC LOGIC</text>
            <view class="drawer-close" @click="drawerOpen = false">
              <uni-icons type="closeempty" size="26" color="#5f5e5e" />
            </view>
          </view>
          <view class="drawer-profile">
            <image
              class="drawer-avatar"
              src="/static/placeholders/hero.svg"
              mode="aspectFill"
            />
            <view class="drawer-profile-text">
              <text class="drawer-name">移动管理端</text>
              <text class="drawer-role">{{ drawerRoleLabel }}</text>
            </view>
          </view>
          <view class="drawer-nav">
            <view
              v-for="item in drawerNav"
              :key="item.label"
              class="drawer-nav-item"
              :class="{ active: item.active }"
              @click="onDrawerNav(item)"
            >
              <uni-icons :type="item.icon" size="22" :color="item.active ? '#ff6600' : '#5f5e5e'" />
              <text class="drawer-nav-label" :class="{ 'nav-active': item.active }">{{ item.label }}</text>
            </view>
          </view>
          <view class="drawer-footer-card">
            <text class="drawer-foot-title">活跃工作台</text>
            <text class="drawer-foot-desc">{{ inventoryHint }}</text>
          </view>
        </view>
      </view>

      <view class="top-bar">
        <view class="top-bar-left">
          <view class="icon-round" @click="goBack">
            <uni-icons type="arrow-left" size="22" color="#ff6600" />
          </view>
          <view class="icon-round" @click="drawerOpen = true">
            <uni-icons type="bars" size="22" color="#ff6600" />
          </view>
          <text class="screen-title">穿线管理</text>
        </view>
        <view class="top-bar-right">
          <button class="cta-btn" @click="openForm">+ 新增穿线</button>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="content">
          <view class="search-panel">
            <view class="search-field">
              <uni-icons class="search-ico" type="search" size="20" color="#5f5e5e" />
              <input
                v-model="keyword"
                class="search-input"
                type="text"
                placeholder="搜索客户、球拍或线材..."
                confirm-type="search"
                @input="onSearchInput"
              />
              <view v-if="keyword" class="clear-btn" @click="clearSearch">
                <uni-icons type="clear" size="18" color="#5f5e5e" />
              </view>
            </view>
          </view>

          <view class="summary-block">
            <view class="summary-hero">
              <text class="summary-label">今日工单总量</text>
              <text class="summary-hero-num">{{ summary.todayTotal }}</text>
              <view class="trend-row">
                <uni-icons type="arrow-up" size="16" color="#a33e00" />
                <text class="trend-text">真实工单数据</text>
              </view>
            </view>
            <view class="summary-grid">
              <view class="mini-stat border-primary">
                <text class="mini-label">待处理</text>
                <text class="mini-num">{{ pad2(summary.pending) }}</text>
                <text class="mini-sub">等待开始穿线</text>
              </view>
              <view class="mini-stat border-tertiary">
                <text class="mini-label">进行中</text>
                <text class="mini-num">{{ pad2(summary.inProgress) }}</text>
                <text class="mini-sub">正在穿线中</text>
              </view>
              <view class="mini-stat solid-orange">
                <text class="mini-label light">已完成</text>
                <text class="mini-num light">{{ pad2(summary.ready) }}</text>
                <text class="mini-sub light dim">已完成工单</text>
              </view>
            </view>
          </view>

          <view class="table-card">
            <view class="table-head">
              <text class="th th-customer">客户与球拍</text>
              <text class="th th-string">线材型号</text>
              <text class="th th-lbs">磅数 (lbs)</text>
              <text class="th th-status">状态</text>
              <text class="th th-actions">操作</text>
            </view>
            <view v-if="jobs.length === 0" class="table-row">
              <view class="td-stack td-customer">
                <text class="customer-name">{{ loading ? '加载中...' : '暂无穿线工单' }}</text>
                <text class="racket-line">{{ loading ? '正在同步真实数据' : '当前没有可展示记录' }}</text>
              </view>
            </view>
            <view v-for="job in jobs" :key="job.id" class="table-row">
              <view class="td-stack td-customer">
                <text class="customer-name">{{ job.customerLabel }}</text>
                <text class="racket-line">{{ job.racketLine }}</text>
              </view>
              <view class="td-string">
                <view class="string-pill">
                  <text class="string-pill-text">{{ job.stringModel }}</text>
                </view>
              </view>
              <view class="td-tension">
                <text class="tension-main">{{ job.tensionMain }}</text>
                <text class="tension-cross">/ {{ job.tensionCross }}</text>
              </view>
              <view class="td-status">
                <view class="status-pill" :class="`st-${job.status}`">
                  <view v-if="job.status === 'in_progress'" class="dot dot-tertiary" />
                  <view v-if="job.status === 'pending'" class="dot dot-secondary" />
                  <uni-icons v-if="job.status === 'ready'" type="checkbox-filled" size="14" color="#561d00" />
                  <uni-icons v-if="job.status === 'cancelled'" type="closeempty" size="14" color="#666666" />
                  <text class="status-text">{{ statusLabel(job.status) }}</text>
                </view>
              </view>
              <view class="td-actions">
                <view v-if="job.status === 'in_progress'" class="act-btn" @click="onComplete(job)">
                  <uni-icons type="compose" size="20" color="#5f5e5e" />
                </view>
                <view v-if="job.status === 'pending'" class="act-btn" @click="onStart(job)">
                  <uni-icons type="right" size="20" color="#5f5e5e" />
                </view>
                <view class="act-btn" @click="onMore(job)">
                  <uni-icons type="more-filled" size="20" color="#5f5e5e" />
                </view>
              </view>
            </view>
          </view>

          <view class="bottom-section">
            <view class="inventory-block">
              <text class="section-title">线材热度</text>
              <view class="inv-grid">
                <view v-for="inv in inventory" :key="inv.name" class="inv-row">
                  <text class="inv-name">{{ inv.name }}</text>
                  <view class="inv-bar-track">
                    <view class="inv-bar-fill" :class="inv.low ? 'fill-error' : 'fill-primary'" :style="{ width: `${inv.pct}%` }" />
                  </view>
                </view>
                <view v-if="inventory.length === 0" class="inv-row">
                  <text class="inv-name">暂无线材使用数据</text>
                  <view class="inv-bar-track">
                    <view class="inv-bar-fill fill-primary" :style="{ width: '8%' }" />
                  </view>
                </view>
              </view>
            </view>
            <view class="maint-card">
              <image
                class="maint-img"
                src="/static/placeholders/hero.svg"
                mode="aspectFill"
              />
              <view class="maint-overlay">
                <text class="maint-tag">维护笔记</text>
                <text class="maint-body">当前列表已切换为真实工单数据，首版仅保留真实可联调的工单能力。</text>
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
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import {
  getStringingList,
  processStringingPayment,
  processStringingRefund,
  updateStringingStatus,
  type StringingService
} from '@/api/president/stringing'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { BUSINESS_PAYMENT_METHOD, PAYMENT_METHOD_TEXT, STRINGING_STATUS } from '@/utils/constant'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getRoleName, isPresidentRole } from '@/utils/roleCheck'

type JobStatus = 'in_progress' | 'pending' | 'ready' | 'cancelled'

type Job = {
  id: number
  customerLabel: string
  racketLine: string
  stringModel: string
  tensionMain: string
  tensionCross: string
  paymentStatus: number
  status: JobStatus
}

type InventoryItem = {
  name: string
  pct: number
  low: boolean
}

const drawerOpen = ref(false)
const loading = ref(false)
const records = ref<StringingService[]>([])
const keyword = ref('')
let searchTimer: ReturnType<typeof setTimeout> | null = null
const userStore = useUserStore()
const isPresident = computed(() => isPresidentRole(userStore.userInfo?.role))
const drawerRoleLabel = computed(() => getRoleName(userStore.userInfo?.role))

const drawerNav = computed(() => [
  { label: '仪表盘', icon: 'home', active: false, action: 'dashboard' as const },
  { label: isPresident.value ? '协会管理' : '会员管理', icon: 'staff', active: true, action: 'assoc' as const },
  { label: '赛事联赛', icon: 'flag', active: false, action: 'league' as const },
  { label: '设置', icon: 'gear', active: false, action: 'settings' as const }
])

const summary = ref({
  todayTotal: 0,
  pending: 0,
  inProgress: 0,
  ready: 0
})

const jobs = computed<Job[]>(() =>
  records.value.map((item) => {
    const tension = formatPound(item.pound)
    return {
      id: item.id,
      customerLabel: item.memberName || item.userName || `客户 #${item.userId || item.id}`,
      racketLine: [item.racketBrand, item.racketModel].filter(Boolean).join(' ') || '未知球拍',
      stringModel: resolveStringModel(item),
      tensionMain: tension,
      tensionCross: tension,
      paymentStatus: Number(item.paymentStatus ?? 0),
      status: mapJobStatus(item.status)
    }
  })
)

const inventory = computed<InventoryItem[]>(() => {
  const counts = new Map<string, number>()
  jobs.value.forEach((item) => {
    counts.set(item.stringModel, (counts.get(item.stringModel) || 0) + 1)
  })

  const totalCount = jobs.value.length || 1
  return Array.from(counts.entries())
    .sort((a, b) => b[1] - a[1])
    .slice(0, 4)
    .map(([name, count]) => ({
      name,
      pct: Math.max(8, Math.min(100, Math.round((count / totalCount) * 100))),
      low: count / totalCount < 0.2
    }))
})

const inventoryHint = computed(() => {
  if (inventory.value.length === 0) return '暂无线材使用记录'
  return inventory.value.map((item) => item.name).slice(0, 2).join(' / ')
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function pad2(n: number) {
  return n < 10 ? `0${n}` : String(n)
}

function formatPound(value?: number | string) {
  if (value === undefined || value === null || value === '') return '--'
  return String(value).replace(/\.0$/, '')
}

function mapJobStatus(status?: number): JobStatus {
  if (status === STRINGING_STATUS.IN_PROGRESS) return 'in_progress'
  if (status === STRINGING_STATUS.COMPLETED) return 'ready'
  if (status === STRINGING_STATUS.CANCELLED) return 'cancelled'
  return 'pending'
}

function resolveStringModel(item: StringingService) {
  if (item.isOwnString === 1) return item.stringName || '自带线材'
  return item.stringName || item.stringEquipmentName || '未选择线材'
}

function statusLabel(status: JobStatus) {
  if (status === 'in_progress') return '进行中'
  if (status === 'pending') return '待处理'
  if (status === 'cancelled') return '已取消'
  return '已完成'
}

async function loadRecords() {
  loading.value = true
  try {
    const searchKeyword = keyword.value.trim() || undefined
    const [allRes, pendingRes, progressRes, readyRes] = await Promise.all([
      getStringingList({ page: 1, size: 100, keyword: searchKeyword }),
      getStringingList({ page: 1, size: 1, status: STRINGING_STATUS.WAITING }),
      getStringingList({ page: 1, size: 1, status: STRINGING_STATUS.IN_PROGRESS }),
      getStringingList({ page: 1, size: 1, status: STRINGING_STATUS.COMPLETED })
    ])

    records.value = Array.isArray(allRes.data) ? allRes.data : []
    summary.value = {
      todayTotal: Number(allRes.total || records.value.length),
      pending: Number(pendingRes.total || 0),
      inProgress: Number(progressRes.total || 0),
      ready: Number(readyRes.total || 0)
    }
  } catch (error) {
    console.error('Failed to load stringing records:', error)
    records.value = []
    summary.value = {
      todayTotal: 0,
      pending: 0,
      inProgress: 0,
      ready: 0
    }
  } finally {
    loading.value = false
  }
}

function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    void loadRecords()
  }, 500)
}

function clearSearch() {
  keyword.value = ''
  void loadRecords()
}

function onDrawerNav(item: (typeof drawerNav.value)[0]) {
  drawerOpen.value = false
  if (item.action === 'dashboard') {
    safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
    return
  }
  if (item.action === 'assoc') {
    uni.reLaunch({ url: isPresident.value ? PRESIDENT_PAGES.USER_LIST : PRESIDENT_PAGES.MEMBER_LIST })
    return
  }
  if (item.action === 'league') {
    uni.navigateTo({ url: PRESIDENT_PAGES.TOURNAMENT_LIST })
    return
  }
  uni.switchTab({ url: PRESIDENT_PAGES.PROFILE })
}

function openForm() {
  uni.navigateTo({ url: PRESIDENT_PAGES.STRINGING_FORM })
}

function openDetail(job: Job) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.STRINGING_DETAIL}?id=${encodeURIComponent(job.id)}` })
}

async function updateJobStatus(id: number, status: number, title: string) {
  try {
    await updateStringingStatus(id, status)
    uni.showToast({ title, icon: 'success' })
    await loadRecords()
  } catch (error) {
    console.error('Failed to update stringing status:', error)
  }
}

async function refundJob(job: Job) {
  const result = await uni.showModal({
    title: '确认退款',
    content: '退款后将撤销当前穿线支付记录，是否继续？'
  })
  if (!result.confirm) return

  try {
    await processStringingRefund(job.id)
    uni.showToast({ title: '退款成功', icon: 'success' })
    await loadRecords()
  } catch (error) {
    console.error('Failed to refund stringing order:', error)
  }
}

async function payForJob(job: Job) {
  try {
    await processStringingPayment(job.id, BUSINESS_PAYMENT_METHOD)
    uni.showToast({ title: `${PAYMENT_METHOD_TEXT[BUSINESS_PAYMENT_METHOD]}收款成功`, icon: 'success' })
    await loadRecords()
  } catch (error) {
    console.error('Failed to process stringing payment:', error)
  }
}

function onCancel(job: Job) {
  void updateJobStatus(job.id, STRINGING_STATUS.CANCELLED, '已取消工单')
}

function onStart(job: Job) {
  void updateJobStatus(job.id, STRINGING_STATUS.IN_PROGRESS, '已开始穿线')
}

function onComplete(job: Job) {
  void updateJobStatus(job.id, STRINGING_STATUS.COMPLETED, '已标记完成')
}

function onMore(job: Job) {
  const itemList = ['查看详情']
  if (job.status === 'pending') {
    itemList.push('开始穿线')
    itemList.push('取消工单')
  } else if (job.status === 'in_progress') {
    itemList.push('标记完成')
    itemList.push('取消工单')
  } else if (job.status === 'ready') {
    if (job.paymentStatus !== 1) itemList.push('确认收款')
    if (job.paymentStatus === 1) itemList.push('退款')
  }

  uni.showActionSheet({
    itemList,
    success(res) {
      if (res.tapIndex === 0) {
        openDetail(job)
        return
      }
      if (job.status === 'pending') {
        if (res.tapIndex === 1) onStart(job)
        if (res.tapIndex === 2) onCancel(job)
        return
      }
      if (job.status === 'in_progress') {
        if (res.tapIndex === 1) onComplete(job)
        if (res.tapIndex === 2) onCancel(job)
        return
      }
      if (job.status === 'ready') {
        if (job.paymentStatus !== 1 && res.tapIndex === 1) {
          void payForJob(job)
          return
        }
        if (job.paymentStatus === 1 && res.tapIndex === 1) {
          void refundJob(job)
        }
      }
    }
  })
}

onShow(() => {
  void loadRecords()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.drawer-root {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.5);
}
.drawer-panel {
  width: 576rpx;
  max-width: 82vw;
  height: 100%;
  background: #f3f3f3;
  display: flex;
  flex-direction: column;
  padding: 56rpx 40rpx 40rpx;
  box-sizing: border-box;
}
.drawer-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 48rpx;
}
.drawer-brand {
  font-size: 32rpx;
  font-weight: 900;
  color: #ff6600;
  letter-spacing: -0.04em;
}
.drawer-close {
  padding: 8rpx;
}
.drawer-profile {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 40rpx;
}
.drawer-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 16rpx;
}
.drawer-profile-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.drawer-name {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.drawer-role {
  font-size: 20rpx;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}
.drawer-nav {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  flex: 1;
}
.drawer-nav-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 20rpx;
  padding: 22rpx 24rpx;
  border-radius: 16rpx;
}
.drawer-nav-item.active {
  background: #ffffff;
}
.drawer-nav-label {
  font-size: 24rpx;
  font-weight: 600;
  color: #5f5e5e;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.nav-active {
  color: #ff6600;
  font-weight: 800;
}
.drawer-footer-card {
  margin-top: auto;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 28rpx;
}
.drawer-foot-title {
  font-size: 20rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}
.drawer-foot-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #5f5e5e;
}

.top-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 28rpx 20rpx;
  background: rgba(249, 249, 249, 0.92);
  position: sticky;
  top: 0;
  z-index: 50;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);
}
.top-bar-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  min-width: 0;
}
.top-bar-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
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
.screen-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: -0.02em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cta-btn {
  margin: 0;
  padding: 0 24rpx;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 24rpx;
  font-weight: 800;
  color: #561d00;
  background: #ff6600;
  border-radius: 16rpx;
  border: none;
  white-space: nowrap;
}
.cta-btn::after {
  border: none;
}

.scroll {
  flex: 1;
  height: 0;
}
.content {
  padding: 24rpx 28rpx 40rpx;
}

.search-panel {
  background: #f3f3f3;
  border-radius: 24rpx;
  padding: 28rpx;
  margin-bottom: 32rpx;
}
.search-field {
  position: relative;
  width: 100%;
}
.search-ico {
  position: absolute;
  left: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1;
}
.search-input {
  width: 100%;
  padding: 24rpx 72rpx 24rpx 72rpx;
  background: #ffffff;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #1a1c1c;
  border: none;
}
.clear-btn {
  position: absolute;
  right: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}
.clear-btn:active {
  background: #f3f3f3;
}

.summary-block {
  margin-bottom: 32rpx;
}
.summary-hero {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 4rpx 16rpx rgba(26, 28, 28, 0.06);
  margin-bottom: 24rpx;
}
.summary-label {
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}
.summary-hero-num {
  display: block;
  margin-top: 16rpx;
  font-size: 88rpx;
  font-weight: 800;
  color: #1a1c1c;
  line-height: 1;
}
.trend-row {
  margin-top: 28rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
}
.trend-text {
  font-size: 26rpx;
  font-weight: 800;
  color: #a33e00;
}

.summary-grid {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.mini-stat {
  background: #f3f3f3;
  border-radius: 24rpx;
  padding: 36rpx 32rpx;
  border-bottom: 4rpx solid transparent;
}
.mini-stat.border-primary {
  border-bottom-color: #ff6600;
}
.mini-stat.border-tertiary {
  border-bottom-color: #0062a1;
}
.mini-stat.solid-orange {
  background: #ff6600;
  border-bottom: none;
}
.mini-label {
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}
.mini-label.light {
  color: #561d00;
  opacity: 0.95;
}
.mini-num {
  display: block;
  margin-top: 12rpx;
  font-size: 48rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.mini-num.light {
  color: #ffffff;
}
.mini-sub {
  display: block;
  margin-top: 12rpx;
  font-size: 20rpx;
  color: #5f5e5e;
}
.mini-sub.light {
  color: #ffffff;
}
.mini-sub.light.dim {
  opacity: 0.85;
}

.table-card {
  background: #ffffff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(26, 28, 28, 0.06);
}
.table-head {
  display: none;
}
.table-row {
  padding: 32rpx 28rpx;
  border-bottom: 1rpx solid rgba(226, 226, 226, 0.6);
}
.table-row:last-child {
  border-bottom: none;
}
.td-stack {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.customer-name {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.racket-line {
  font-size: 20rpx;
  color: #5f5e5e;
}
.td-string {
  margin-top: 20rpx;
}
.string-pill {
  align-self: flex-start;
  background: #e8e8e8;
  border-radius: 8rpx;
  padding: 8rpx 16rpx;
}
.string-pill-text {
  font-size: 20rpx;
  font-weight: 600;
  color: #1a1c1c;
}
.td-tension {
  margin-top: 16rpx;
  display: flex;
  flex-direction: row;
  align-items: baseline;
  gap: 6rpx;
}
.tension-main {
  font-size: 32rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.tension-cross {
  font-size: 20rpx;
  color: #5f5e5e;
}
.td-status {
  margin-top: 20rpx;
}
.status-pill {
  align-self: flex-start;
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  padding: 10rpx 20rpx;
  border-radius: 9999px;
}
.st-in_progress {
  background: rgba(0, 156, 252, 0.1);
}
.st-in_progress .status-text {
  color: #0062a1;
}
.st-pending {
  background: rgba(227, 191, 177, 0.35);
}
.st-pending .status-text {
  color: #5f5e5e;
}
.st-ready {
  background: #ff6600;
}
.st-ready .status-text {
  color: #561d00;
  font-weight: 800;
}
.st-cancelled {
  background: rgba(0, 0, 0, 0.08);
}
.st-cancelled .status-text {
  color: #666666;
}
.dot {
  width: 10rpx;
  height: 10rpx;
  border-radius: 9999px;
}
.dot-tertiary {
  background: #0062a1;
}
.dot-secondary {
  background: #5f5e5e;
}
.status-text {
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.td-actions {
  margin-top: 24rpx;
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  gap: 8rpx;
}
.act-btn {
  padding: 12rpx;
  border-radius: 12rpx;
}
.act-btn:active {
  background: #e8e8e8;
}

.bottom-section {
  margin-top: 48rpx;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}
.section-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
  margin-bottom: 24rpx;
}
.inv-grid {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.inv-row {
  background: #f3f3f3;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
}
.inv-name {
  flex: 1;
  font-size: 22rpx;
  font-weight: 800;
  color: #5f5e5e;
  text-transform: uppercase;
}
.inv-bar-track {
  width: 200rpx;
  height: 8rpx;
  background: #e2e2e2;
  border-radius: 9999px;
  overflow: hidden;
}
.inv-bar-fill {
  height: 100%;
  border-radius: 9999px;
}
.fill-primary {
  background: #ff6600;
}
.fill-error {
  background: #ba1a1a;
}

.maint-card {
  position: relative;
  border-radius: 32rpx;
  overflow: hidden;
  aspect-ratio: 16 / 9;
}
.maint-img {
  width: 100%;
  height: 100%;
}
.maint-overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 28rpx;
  background: linear-gradient(to top, rgba(26, 28, 28, 0.85), transparent);
}
.maint-tag {
  font-size: 20rpx;
  font-weight: 800;
  color: #ff6600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}
.maint-body {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #ffffff;
  line-height: 1.45;
}

.bottom-space {
  height: 48rpx;
}

@media screen and (min-width: 768px) {
  .summary-grid {
    flex-direction: row;
    flex-wrap: wrap;
  }
  .summary-grid .mini-stat {
    flex: 1;
    min-width: 200rpx;
  }
  .table-head {
    display: grid;
    grid-template-columns: 2fr 1.2fr 1fr 1.3fr 1fr;
    gap: 16rpx;
    padding: 24rpx 32rpx;
    background: #e8e8e8;
    align-items: center;
  }
  .th {
    font-size: 20rpx;
    font-weight: 800;
    color: #5f5e5e;
    letter-spacing: 0.12em;
    text-transform: uppercase;
  }
  .th-actions {
    text-align: right;
  }
  .table-row {
    display: grid;
    grid-template-columns: 2fr 1.2fr 1fr 1.3fr 1fr;
    gap: 16rpx;
    align-items: center;
    padding: 28rpx 32rpx;
  }
  .td-string,
  .td-tension,
  .td-status,
  .td-actions {
    margin-top: 0;
  }
  .bottom-section {
    flex-direction: row;
    align-items: flex-start;
  }
  .inventory-block {
    flex: 1;
  }
  .maint-card {
    width: 38%;
    flex-shrink: 0;
  }
}
</style>
