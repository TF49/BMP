<template>
  <PresidentLayout :showTabBar="true">
    <view class="finance-stats-page">
      <view class="status-bar-placeholder"></view>
      
      <!-- TopAppBar（与赛事管理一致） -->
      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#ff6600"></uni-icons>
            </view>
            <view class="brand-wrap">
              <text class="brand-name">Kinetic Logic</text>
            </view>
          </view>
          <view class="nav-right">
            <text class="audit-link" @click.stop="goAuditLog">审计日志</text>
            <view class="icon-btn" @click.stop="toggleSearch">
              <uni-icons type="search" size="22" color="#71717a"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="main-content">
        <!-- Search Bar -->
        <view v-if="showSearch" class="search-bar">
          <view class="search-input-wrap">
            <uni-icons type="search" size="20" color="#71717a"></uni-icons>
            <input 
              class="search-input" 
              v-model="searchKeyword" 
              placeholder="搜索交易记录..."
              @confirm="handleSearchConfirm"
              @input="handleSearchInput"
            />
            <view v-if="searchKeyword" class="clear-btn" @click="clearSearch">
              <uni-icons type="closeempty" size="16" color="#71717a"></uni-icons>
            </view>
          </view>
          <text class="cancel-btn" @click="toggleSearch">取消</text>
        </view>

        <!-- Welcome & Role Badge -->
        <view class="welcome-section">
          <view class="title-wrap">
            <text class="role-badge">会长管理后台</text>
            <view class="page-title">
              <text class="title-main">财务 </text>
              <text class="title-italic">脉搏</text>
            </view>
          </view>
          <view class="status-badge">
            <view class="pulse-dot"></view>
            <text class="status-text">实时财务数据</text>
          </view>
        </view>

        <!-- High-Level Metrics Bento -->
        <view class="metrics-grid">
          <!-- Total Income -->
          <view class="metric-card group-hover-primary">
            <view class="card-header">
              <uni-icons type="wallet" size="20" color="#a33e00"></uni-icons>
              <text class="card-label">总收入</text>
            </view>
            <text class="card-value">{{ formatAmount(totalIncome) }}</text>
            <view class="card-trend up">
              <uni-icons type="arrow-up" size="14" color="#059669"></uni-icons>
              <text class="trend-text">实时数据</text>
            </view>
          </view>
          
          <!-- Total Expense -->
          <view class="metric-card group-hover-error">
            <view class="card-header">
              <uni-icons type="shop" size="20" color="#ba1a1a"></uni-icons>
              <text class="card-label">总支出</text>
            </view>
            <text class="card-value">{{ formatAmount(totalExpense) }}</text>
            <view class="card-trend down">
              <uni-icons type="arrow-up" size="14" color="#ba1a1a"></uni-icons>
              <text class="trend-text">实时数据</text>
            </view>
          </view>

          <!-- Net Income (Primary Container) -->
          <view class="metric-card primary">
            <view class="card-header">
              <uni-icons type="medal" size="20" color="#ffffff"></uni-icons>
              <text class="card-label light">净利润</text>
            </view>
            <text class="card-value light">{{ formatAmount(netIncome) }}</text>
            <view class="goal-badge">当前净利润</view>
          </view>

          <!-- Monthly Growth -->
          <view class="metric-card group-hover-tertiary">
            <view class="card-header">
              <uni-icons type="settings-filled" size="20" color="#0062a1"></uni-icons>
              <text class="card-label">利润率</text>
            </view>
            <text class="card-value">{{ growthRate.toFixed(1) }}%</text>
            <view class="card-trend up">
              <uni-icons type="paperplane" size="14" color="#059669"></uni-icons>
              <text class="trend-text">收支比率</text>
            </view>
          </view>
        </view>

        <!-- Charts Section -->
        <view class="charts-section">
          <!-- Revenue Trend -->
          <view class="chart-card lg-span-2">
            <view class="chart-header">
              <text class="chart-title">收入趋势</text>
              <view class="chart-actions">
                <text class="action-btn active">6个月</text>
                <text class="action-btn">按年</text>
              </view>
            </view>
            
            <view class="bar-chart-area">
              <!-- Y-Axis Lines -->
              <view class="y-axis">
                <view class="y-line"></view>
                <view class="y-line"></view>
                <view class="y-line"></view>
              </view>
              <!-- Bars -->
              <view class="bars-container">
                <template v-if="trendBars.length === 0">
                  <view class="chart-empty">暂无趋势数据</view>
                </template>
                <template v-else>
                  <view
                    v-for="item in trendBars"
                    :key="item.label"
                    class="bar-group hoverable"
                  >
                    <view class="bar" :class="{ active: item.active }" :style="{ height: `${item.height}%` }">
                      <view class="bar-fill"></view>
                    </view>
                    <text class="bar-label">{{ item.label }}</text>
                  </view>
                </template>
              </view>
            </view>
          </view>

          <!-- Business Type Breakdown Pie Chart -->
          <view class="chart-card pie-chart-card">
            <text class="chart-title mb">业务构成</text>
            <view class="pie-container">
              <view class="pie-visual" :style="{ background: pieBackground }"></view>
              <view class="pie-center">
                <text class="pie-label">收入</text>
                <text class="pie-value">{{ ratioItems.length > 0 ? '100%' : '0%' }}</text>
              </view>
            </view>
            
            <view class="legend-grid">
              <template v-if="ratioItems.length === 0">
                <view class="legend-item">
                  <view class="dot" style="background-color: #e8e8e8"></view>
                  <text class="legend-text">暂无业务占比数据</text>
                </view>
              </template>
              <template v-else>
                <view v-for="item in ratioItems" :key="item.label" class="legend-item">
                  <view class="dot" :style="{ backgroundColor: item.color }"></view>
                  <text class="legend-text">{{ item.label }} ({{ item.percent }}%)</text>
                </view>
              </template>
            </view>
          </view>
        </view>

        <!-- Transaction Table Section -->
        <view class="table-card">
          <view class="table-header">
            <view>
              <text class="table-title">最近交易</text>
              <text class="table-desc">最近的财务记录</text>
            </view>
          </view>
          
          <!-- Loading State -->
          <view v-if="loading" class="loading-container">
            <text class="loading-text">加载中...</text>
          </view>
          
          <!-- Empty State -->
          <view v-else-if="transactions.length === 0" class="empty-container">
            <text class="empty-text">暂无财务记录</text>
          </view>
          
          <!-- Data Table -->
          <scroll-view v-else class="table-scroll" scroll-x="true">
            <view class="table-container">
              <view class="table-head">
                <text class="th w-date">日期</text>
                <text class="th w-type">交易类型</text>
                <text class="th w-channel">渠道</text>
                <text class="th w-amount right">金额</text>
                <text class="th w-status">状态</text>
              </view>
              <view class="table-body">
                <view 
                  v-for="item in transactions" 
                  :key="item.id" 
                  class="tr hover-bg"
                >
                  <view class="td w-date">
                    <text class="date-b">{{ formatDate(item.createTime) }}</text>
                    <text class="tx-id">ID: #{{ item.id }}</text>
                  </view>
                  <view class="td w-type flex-row">
                    <view :class="['icon-box', getBusinessStyle(item.businessType)]">
                      <uni-icons :type="getBusinessIcon(item.businessType)" size="20" color="#a33e00"></uni-icons>
                    </view>
                    <text class="type-text">{{ item.businessType || '其他' }}</text>
                  </view>
                  <view class="td w-channel">{{ item.paymentMethod || '-' }}</view>
                  <view 
                    :class="['td', 'w-amount', 'right', item.incomeExpenseType === 'income' ? 'amt-plus' : 'amt-minus']"
                  >
                    {{ item.incomeExpenseType === 'income' ? '+' : '-' }}{{ formatAmount(item.amount) }}
                  </view>
                  <view class="td w-status">
                    <text :class="['status-tag', getStatusTag(item).class]">
                      {{ getStatusTag(item).text }}
                    </text>
                  </view>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>

      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { 
  getFinanceList, 
  getFinanceStatistics,
  getFinanceTrend,
  getBusinessRatio,
  type FinanceItem 
} from '@/api/president/finance'

// 数据状态
const loading = ref(false)
const transactions = ref<FinanceItem[]>([])
const totalIncome = ref(0)
const totalExpense = ref(0)
const netIncome = ref(0)
const growthRate = ref(0)

// 搜索状态
const showSearch = ref(false)
const searchKeyword = ref('')
let searchTimer: number | undefined

// 图表数据
const trendData = ref<{ dates: string[], incomes: number[], expenses: number[] }>({
  dates: [],
  incomes: [],
  expenses: []
})
const businessRatioData = ref<{ labels: string[], values: number[] }>({
  labels: [],
  values: []
})

const ratioColors = ['#ff6600', '#5f5e5e', '#0062a1', '#e3bfb1', '#ba1a1a', '#7c3aed']

const trendBars = computed(() => {
  const dates = trendData.value.dates || []
  const incomes = trendData.value.incomes || []
  const values = dates.map((date, index) => ({
    label: formatTrendLabel(date, index),
    value: Number(incomes[index] || 0)
  }))
  const maxValue = Math.max(...values.map((item) => item.value), 0)

  return values.map((item, index) => ({
    ...item,
    height: maxValue > 0 ? Math.max(10, Math.round((item.value / maxValue) * 100)) : 10,
    active: index === values.length - 1
  }))
})

const ratioItems = computed(() => {
  const labels = businessRatioData.value.labels || []
  const values = businessRatioData.value.values || []
  const total = values.reduce((sum, value) => sum + Number(value || 0), 0)

  return labels.map((label, index) => {
    const value = Number(values[index] || 0)
    const percent = total > 0 ? Number(((value / total) * 100).toFixed(1)) : 0
    return {
      label: formatBusinessLabel(label),
      value,
      percent,
      color: ratioColors[index % ratioColors.length]
    }
  })
})

const pieBackground = computed(() => {
  if (ratioItems.value.length === 0) {
    return 'conic-gradient(#e8e8e8 0% 100%)'
  }

  let current = 0
  const stops = ratioItems.value.map((item) => {
    const start = current
    current += item.percent
    const end = Math.min(current, 100)
    return `${item.color} ${start}% ${end}%`
  })

  if (current < 100) {
    stops.push(`#e8e8e8 ${current}% 100%`)
  }

  return `conic-gradient(${stops.join(', ')})`
})

// 加载财务统计数据
async function loadStatistics() {
  try {
    const stats = await getFinanceStatistics()
    totalIncome.value = stats.totalIncome || 0
    totalExpense.value = stats.totalExpense || 0
    netIncome.value = stats.netIncome || 0
    
    // 计算利润率
    if (totalExpense.value > 0) {
      growthRate.value = (netIncome.value / totalExpense.value * 100)
    } else if (totalIncome.value > 0) {
      growthRate.value = 100
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载趋势数据
async function loadTrendData() {
  try {
    const trend = await getFinanceTrend()
    trendData.value = trend
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 加载业务占比数据
async function loadBusinessRatio() {
  try {
    const ratio = await getBusinessRatio()
    businessRatioData.value = ratio
  } catch (error) {
    console.error('加载业务占比失败:', error)
  }
}

// 加载财务列表数据
async function loadFinanceData() {
  loading.value = true
  try {
    const params: any = {
      page: 1,
      size: 10
    }
    
    // 添加搜索关键词
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    
    const res = await getFinanceList(params)
    transactions.value = res.data || []
  } catch (error) {
    console.error('加载财务数据失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 格式化金额
function formatAmount(amount?: number): string {
  if (!amount) return '$0.00'
  return `$${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

// 格式化日期
function formatDate(dateStr?: string): string {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
}

function formatTrendLabel(dateStr: string, index: number): string {
  if (!dateStr) return `${index + 1}月`
  const date = new Date(dateStr)
  if (!Number.isNaN(date.getTime())) {
    return `${date.getMonth() + 1}月`
  }
  return dateStr.slice(5, 7).replace(/^0/, '') + '月'
}

function formatBusinessLabel(label?: string): string {
  const map: Record<string, string> = {
    court_booking: '场地预约',
    course_booking: '课程预约',
    tournament: '赛事报名',
    tournament_registration: '赛事报名',
    equipment_rental: '器材租借',
    stringing: '穿线服务',
    member_recharge: '会员充值',
    maintenance: '维护服务'
  }
  if (!label) return '其他'
  return map[label] || label
}

// 获取业务类型图标
function getBusinessIcon(type?: string): string {
  const iconMap: Record<string, string> = {
    'court_booking': 'calendar',
    'course_booking': 'flag',
    'equipment_rental': 'shop',
    'tournament': 'medal',
    'maintenance': 'settings'
  }
  return iconMap[type || ''] || 'wallet'
}

// 获取业务类型样式
function getBusinessStyle(type?: string): string {
  const styleMap: Record<string, string> = {
    'court_booking': 'primary-bg',
    'course_booking': 'secondary-bg',
    'equipment_rental': 'tertiary-bg',
    'maintenance': 'error-bg'
  }
  return styleMap[type || ''] || 'primary-bg'
}

// 获取状态标签
function getStatusTag(item: FinanceItem): { text: string; class: string } {
  // 根据实际业务逻辑判断状态
  if (item.remark?.includes('处理中')) {
    return { text: '处理中', class: 'warning' }
  }
  return { text: '已完成', class: 'success' }
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function goAuditLog() {
  uni.navigateTo({ url: PRESIDENT_PAGES.FINANCE_AUDIT_LOG })
}

function toggleSearch() {
  showSearch.value = !showSearch.value
  if (!showSearch.value) {
    searchKeyword.value = ''
    loadFinanceData()
  }
}

function clearSearch() {
  searchKeyword.value = ''
  loadFinanceData()
}

function handleSearchConfirm() {
  loadFinanceData()
}

function handleSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadFinanceData()
  }, 500) as unknown as number
}

onMounted(() => {
  loadStatistics()
  loadTrendData()
  loadBusinessRatio()
  loadFinanceData()
})
</script>

<style lang="scss" scoped>
.finance-stats-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  color: #1a1c1c;
  font-family: "Lexend", sans-serif;
  padding-bottom: 240rpx; /* Leave space for bottom bar */
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f8fafc;
}

/* ── Search Bar ── */
.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 24rpx 48rpx;
  background-color: #ffffff;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  background-color: #f3f3f3;
  padding: 20rpx 24rpx;
  border-radius: 16rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #1a1c1c;
}

.clear-btn {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e5e5e5;
  border-radius: 50%;
}

.cancel-btn {
  font-size: 28rpx;
  font-weight: 600;
  color: #ea580c;
  white-space: nowrap;
}

/* ── Navigation（对齐赛事管理） ── */
.nav-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(248, 250, 252, 0.85);
  backdrop-filter: blur(12px);
  padding: 24rpx 48rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);
}

.audit-link {
  font-size: 24rpx;
  font-weight: 800;
  color: #ff6600;
  padding: 12rpx 16rpx;
  margin-right: 8rpx;
}

.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
  min-width: 0;

  .back-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &:active {
      background-color: #e4e4e7;
    }
  }

  .brand-name {
    font-size: 36rpx;
    font-weight: 800;
    color: #ea580c;
    letter-spacing: -0.04em;
    text-transform: uppercase;
    white-space: nowrap;
  }
}

.nav-right {
  display: flex;
  gap: 8rpx;
  flex-shrink: 0;

  .icon-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;

    &:active {
      background-color: #e4e4e7;
    }
  }
}

/* Main Content */
.main-content {
  padding: 48rpx 32rpx;
  max-width: 1400rpx;
  margin: 0 auto;
  
  @media (min-width: 768px) {
    padding: 64rpx 48rpx;
  }
}

/* Welcome Section */
.welcome-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 48rpx;
  
  @media (min-width: 768px) {
    flex-direction: row;
    align-items: flex-end;
    justify-content: space-between;
  }
}

.title-wrap {
  .role-badge {
    display: block;
    font-size: 24rpx;
    font-weight: 700;
    letter-spacing: 0.2em;
    color: #a33e00;
    text-transform: uppercase;
    margin-bottom: 8rpx;
  }

  .page-title {
    font-size: 60rpx;
    font-weight: 700;
    letter-spacing: -0.05em;
    color: #1a1c1c;
    
    @media (min-width: 768px) {
      font-size: 80rpx;
    }

    .title-italic {
      color: #a33e00;
      font-style: italic;
    }
  }
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background-color: #e8e8e8;
  padding: 16rpx 32rpx;
  border-radius: 99px;
  align-self: flex-start;
  
  @media (min-width: 768px) {
    align-self: auto;
  }

  .pulse-dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    background-color: #a33e00;
    animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  }
  
  .status-text {
    font-size: 28rpx;
    font-weight: 600;
    color: #5a4136;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .5; }
}

/* Bento Metrics Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32rpx;
  margin-bottom: 48rpx;
  
  @media (min-width: 768px) {
    grid-template-columns: repeat(4, 1fr);
  }
}

.metric-card {
  background-color: #ffffff;
  padding: 48rpx;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  transition: all 0.3s;
  border-bottom: 4rpx solid transparent;

  &.primary {
    background-color: #ff6600;
    box-shadow: 0 20rpx 40rpx rgba(255, 102, 0, 0.1);
  }

  /* Hover states */
  &.group-hover-primary:active { border-color: #a33e00; }
  &.group-hover-error:active { border-color: #ba1a1a; }
  &.group-hover-tertiary:active { border-color: #0062a1; }
  
  @media (hover: hover) {
    &.group-hover-primary:hover { border-color: #a33e00; transform: translateY(-4rpx); }
    &.group-hover-error:hover { border-color: #ba1a1a; transform: translateY(-4rpx); }
    &.group-hover-tertiary:hover { border-color: #0062a1; transform: translateY(-4rpx); }
    &.primary:hover { transform: translateY(-4rpx); box-shadow: 0 24rpx 48rpx rgba(255, 102, 0, 0.2); }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .card-label {
      font-size: 20rpx;
      font-weight: 700;
      letter-spacing: 0.1em;
      color: #5f5e5e;
      text-transform: uppercase;
      
      &.light { color: rgba(255, 255, 255, 0.8); }
    }
  }

  .card-value {
    font-size: 60rpx;
    font-weight: 700;
    letter-spacing: -0.05em;
    color: #1a1c1c;
    
    &.light { color: #ffffff; }
  }

  .card-trend {
    display: flex;
    align-items: center;
    gap: 8rpx;
    
    &.up .trend-text { color: #059669; }
    &.down .trend-text { color: #ba1a1a; }
    
    .trend-text {
      font-size: 24rpx;
      font-weight: 700;
    }
  }

  .goal-badge {
    font-size: 24rpx;
    font-weight: 700;
    background-color: rgba(86, 29, 0, 0.1);
    color: #561d00;
    padding: 8rpx 16rpx;
    border-radius: 8rpx;
    align-self: flex-start;
  }
}

/* Charts Section */
.charts-section {
  display: grid;
  grid-template-columns: 1fr;
  gap: 48rpx;
  margin-bottom: 48rpx;
  
  @media (min-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.chart-card {
  background-color: #ffffff;
  padding: 48rpx;
  border-radius: 32rpx;
  display: flex;
  flex-direction: column;
  
  &.lg-span-2 {
    @media (min-width: 1024px) {
      grid-column: span 2;
    }
  }
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 64rpx;
}

.chart-title {
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #1a1c1c;
  
  &.mb { margin-bottom: 64rpx; }
}

.chart-actions {
  display: flex;
  gap: 16rpx;
  
  .action-btn {
    padding: 8rpx 24rpx;
    font-size: 20rpx;
    font-weight: 700;
    text-transform: uppercase;
    border-radius: 8rpx;
    color: #5f5e5e;
    transition: all 0.2s;
    
    &.active {
      background-color: #e8e8e8;
      color: #1a1c1c;
    }
    
    &:not(.active):active {
      background-color: #eeeeee;
    }
  }
}

/* Bar Chart Simulation */
.bar-chart-area {
  height: 512rpx;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16rpx;
}

.y-axis {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  pointer-events: none;
  border-left: 2rpx solid #e8e8e8;
  border-bottom: 2rpx solid #e8e8e8;
  
  .y-line {
    width: 100%;
    border-top: 2rpx solid #f3f3f3;
  }
}

.bars-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16rpx;
  padding: 0 16rpx;
  z-index: 1;
}

.chart-empty {
  width: 100%;
  align-self: center;
  text-align: center;
  font-size: 24rpx;
  font-weight: 600;
  color: #5f5e5e;
  padding-bottom: 80rpx;
}

.bar-group {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  position: relative;
  
  .bar {
    width: 100%;
    background-color: rgba(163, 62, 0, 0.1);
    border-top-left-radius: 16rpx;
    border-top-right-radius: 16rpx;
    position: relative;
    overflow: hidden;
    
    .bar-fill {
      position: absolute;
      bottom: 0;
      width: 100%;
      height: 100%;
      background-color: #ff6600;
      opacity: 0.4;
      transition: opacity 0.3s;
    }
    
    &.active {
      border-top: 4rpx solid #a33e00;
      .bar-fill { opacity: 0.6; }
    }
  }
  
  &.hoverable:active .bar-fill { opacity: 0.7; }
  
  @media (hover: hover) {
    &.hoverable:hover .bar-fill { opacity: 0.7; }
    &.hoverable:hover .bar.active .bar-fill { opacity: 1; }
  }

  .bar-label {
    position: absolute;
    bottom: -48rpx;
    font-size: 20rpx;
    font-weight: 700;
    color: #5f5e5e;
    text-transform: uppercase;
  }
}

/* Pie Chart Simulation */
.pie-chart-card {
  flex-direction: column;
  justify-content: space-between;
}

.pie-container {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  min-height: 320rpx;
}

.pie-visual {
  width: 320rpx;
  height: 320rpx;
  border-radius: 50%;
  background: conic-gradient(
    #ff6600 0% 40%,
    #5f5e5e 40% 75%,
    #0062a1 75% 90%,
    #e3bfb1 90% 100%
  );
  -webkit-mask: radial-gradient(transparent 60%, black 61%);
  mask: radial-gradient(transparent 60%, black 61%);
}

.pie-center {
  position: absolute;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  display: flex;
  
  .pie-label {
    font-size: 24rpx;
    font-weight: 700;
    color: #5f5e5e;
    text-transform: uppercase;
  }
  
  .pie-value {
    font-size: 36rpx;
    font-weight: 900;
    color: #1a1c1c;
  }
}

.legend-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 32rpx;
  margin-top: 48rpx;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  
  .dot {
    width: 24rpx;
    height: 24rpx;
    border-radius: 50%;
  }
  
  .legend-text {
    font-size: 20rpx;
    font-weight: 700;
    color: #5f5e5e;
    text-transform: uppercase;
  }
}

/* Transaction Table */
.table-card {
  background-color: #ffffff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.02);
}

.table-header {
  padding: 48rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2rpx solid #eeeeee;
  
  .table-title {
    display: block;
    font-size: 40rpx;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: #1a1c1c;
  }
  
  .table-desc {
    font-size: 28rpx;
    font-weight: 500;
    color: #5f5e5e;
    margin-top: 8rpx;
    display: block;
  }

}

.loading-container,
.empty-container {
  padding: 96rpx 48rpx;
  text-align: center;
}

.loading-text,
.empty-text {
  font-size: 28rpx;
  color: #5f5e5e;
  font-weight: 500;
}

.table-scroll {
  width: 100%;
}

.table-container {
  min-width: 1200rpx;
}

.table-head {
  display: flex;
  background-color: #f3f3f3;
  
  .th {
    padding: 32rpx 48rpx;
    font-size: 20rpx;
    font-weight: 700;
    letter-spacing: 0.1em;
    color: #5f5e5e;
    text-transform: uppercase;
  }
}

.table-body {
  display: flex;
  flex-direction: column;
  
  .tr {
    display: flex;
    border-bottom: 2rpx solid #eeeeee;
    transition: background-color 0.2s;
    
    &.hover-bg:active {
      background-color: #f3f3f3;
    }
    
    @media (hover: hover) {
      &.hover-bg:hover {
        background-color: #f3f3f3;
      }
    }
  }
}

.td {
  padding: 48rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.w-date { width: 250rpx; flex-shrink: 0; }
.w-type { width: 350rpx; flex-shrink: 0; }
.w-channel { width: 200rpx; flex-shrink: 0; font-weight: 500; color: #5f5e5e; }
.w-amount { flex: 1; min-width: 200rpx; }
.w-status { width: 200rpx; flex-shrink: 0; }

.right { text-align: right; align-items: flex-end; }

.date-b { font-weight: 700; color: #1a1c1c; font-size: 32rpx; }
.tx-id { font-size: 20rpx; color: #5f5e5e; font-weight: 500; text-transform: uppercase; margin-top: 8rpx; }

.flex-row {
  flex-direction: row !important;
  align-items: center;
  justify-content: flex-start;
  gap: 24rpx;
}

.icon-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.primary-bg { background-color: rgba(163, 62, 0, 0.1); }
  &.secondary-bg { background-color: rgba(95, 94, 94, 0.1); }
  &.tertiary-bg { background-color: rgba(0, 98, 161, 0.1); }
  &.error-bg { background-color: rgba(186, 26, 26, 0.1); }
}

.type-text { font-weight: 700; color: #1a1c1c; font-size: 32rpx; }

.amt-plus { font-weight: 900; color: #1a1c1c; font-size: 36rpx; }
.amt-minus { font-weight: 900; color: #ba1a1a; font-size: 36rpx; }

.status-tag {
  font-size: 20rpx;
  font-weight: 900;
  text-transform: uppercase;
  padding: 8rpx 24rpx;
  border-radius: 99px;
  display: inline-block;
  
  &.success {
    background-color: #d1fae5;
    color: #065f46;
  }
  
  &.warning {
    background-color: #e2dfde;
    color: #c8c6c5;
  }
}
</style>
