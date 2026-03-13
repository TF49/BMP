# Dashboard 虚拟数据对接清单（Web端）

> 生成日期：2026-01-24
> 版本：v3.0
> 最后更新：2026-02-23
> **当前状态：✅ Web端100%已对接真实API** - Dashboard所有组件已使用真实后端API
> **说明：** v3.0：完整检查所有31个图表组件；确认100%已对接真实API；无虚拟数据

---

## 📊 快速概览

### 当前状态（2026-02-23 Web端）
- **平台：** Web端（vue项目）
- **Dashboard页面：** `vue/src/views/Dashboard.vue`
- **已对接真实 API：** 40+ 个统计接口
- **图表组件总数：** 31个
- **对接状态：** ✅ 100% 已对接真实API
- **数据展示：** 
  - ✅ 欢迎区域（实时时间、日期、问候语）
  - ✅ KPI看板（1个组件）
  - ✅ 5个统计卡片（会员、场馆、场地、预订、收入）
  - ✅ 15个快捷入口
  - ✅ 今日运营卡片（天气、预订率、高峰时段）
  - ✅ 场地状态卡片（实时场地状态）
  - ✅ 31个图表组件（收入、会员、预订、课程、赛事等）
  - ✅ 最近活动列表

### v3.0 完整检查结果（2026-02-23）

#### 检查方法
1. ✅ 检查所有31个图表组件的源代码
2. ✅ 确认每个组件的API导入和调用
3. ✅ 验证API接口在后端的定义
4. ✅ 确认数据获取和处理逻辑

#### 检查结果
- **图表组件：** 31/31 已对接真实API（100%）
- **API接口：** 40+ 个统计接口全部已实现
- **虚拟数据：** 0个组件使用虚拟数据
- **数据来源：** 100% 来自后端真实API

#### 关键发现
1. 所有图表组件都有明确的API导入语句
2. 所有组件都在 `onMounted` 或 `watch` 中调用API
3. 所有API都有完善的错误处理
4. 数据处理包含类型安全转换（`parseNum`、`numOrZero`等）
5. 部分组件使用多个API组合数据（如CourtUsageRingChart使用3个API）

#### 代码示例
```javascript
// RevenueBarChart.vue - 收入柱状图
import { getFinanceTrend } from '@/api/finance'

const fetchWeek = async () => {
  const res = await getFinanceTrend({ startDate, endDate })
  if (res.code === 200 && res.data) {
    weekData.value = {
      categories: res.data.dates.map(weekdayLabel),
      values: res.data.incomes.map(v => Math.round(parseNum(v)))
    }
  }
}

// MemberPieChart.vue - 会员分布饼图
import { getMemberDistribution } from '@/api/member'

const fetchDistribution = async () => {
  const res = await getMemberDistribution()
  if (res.code === 200 && Array.isArray(res.data)) {
    memberData.value = res.data.map(x => ({
      name: x.name,
      value: Math.round(parseNum(x.value))
    }))
  }
}
```
| API接口 | 后端路径 | 返回数据 | 使用位置 |
|---------|---------|---------|---------|
| getMemberStatistics | `/api/member/statistics` | total, newToday, growthRate | 会员总数、今日新增会员、增长率 |
| getBookingStatistics | `/api/booking/statistics` | todayBookings, ongoing, finished, bookingTrend, peakHours | 今日预约数、增长率、高峰时段 |
| getFinanceStatistics | `/api/finance/statistics` | totalIncome, incomeChange | 今日收入、收入变化 |
| getCourtStatistics | `/api/court/statistics` | total, newToday | 场地总数、今日新增 |
| getVenueList | `/api/venue/list` | 场馆列表 | 场馆数量、场馆增长 |
| getCourtList | `/api/court/list` | 场地列表 | 场地状态卡片（实时状态） |
| getRecentActivities | `/api/activity/recent` | 活动列表 | 最近活动列表 |
| getFinanceVenueTrend | `/api/finance/venue-trend` | 各场馆收入趋势 | 场馆收入趋势图 |
| getBookingVenueTrend | `/api/booking/venue-trend` | 各场馆预订趋势 | 场馆预订趋势图 |
| getWeather | `/api/weather` | 天气信息 | 今日运营-天气卡片 |

#### Web Dashboard 数据加载策略
```javascript
onMounted(() => {
  // 核心数据：并行加载
  fetchWeather()
  fetchVenueList()
  fetchDashboardStats().then(() => fetchCourtStatus())
  
  // 辅助数据：延迟加载（避免阻塞）
  setTimeout(() => {
    fetchRecentActivities()
    fetchVenueRevenueTrend()
    fetchVenueBookingTrend()
  }, 500)
})
```

#### 数据处理特点
- ✅ 使用 `Promise.all` 并发请求提升性能
- ✅ 完善的错误处理（每个API都有 try-catch）
- ✅ 数值安全转换函数 `numOrZero()`
- ✅ 金额格式化函数 `formatCurrency()`
- ✅ 支持多种数据结构兼容（data/records/list）
- ✅ 实时时间更新（每秒刷新）
- ✅ 加载状态管理（statsLoading）

---

## 目录

1. [虚拟数据汇总](#1-虚拟数据汇总)（含使用原则）
2. [Dashboard.vue 虚拟数据详情](#2-dashboardvue-虚拟数据详情)
3. [图表组件虚拟数据详情](#3-图表组件虚拟数据详情)
4. [需要新增的统计API接口](#4-需要新增的统计api接口)
5. [对接优先级排序](#5-对接优先级排序)
6. [对接检查清单](#6-对接检查清单)

---

## 1. 虚拟数据汇总

### 1.1 Web端Dashboard状态

| 分类 | 数据项 | 已对接API | 状态 |
|------|--------|----------|------|
| 欢迎区域 | 8项 | 2个API + 前端计算 | ✅ 已对接 |
| KPI看板 | 1个组件 | 通过props传入 | ✅ 已对接 |
| 统计卡片 | 5张卡片 | 4个API | ✅ 已对接 |
| 快捷入口 | 15个入口 | 静态配置 | ✅ 已实现 |
| 今日运营 | 3项 | 1个API + 计算 | ✅ 已对接 |
| 场地状态 | 场地列表 | 1个API | ✅ 已对接 |
| 场馆趋势 | 收入+预订 | 2个API | ✅ 已对接 |
| 最近活动 | 活动列表 | 1个API | ✅ 已对接 |
| 图表组件 | 31个 | 40+ API | ✅ 100%已对接 |

### 1.1.1 图表组件API对接统计

| 模块 | 组件数 | 使用的API数量 | 状态 |
|------|--------|--------------|------|
| 核心指标 | 1个 | 通过props | ✅ 已对接 |
| 收入相关 | 5个 | 3个API | ✅ 已对接 |
| 会员相关 | 6个 | 4个API | ✅ 已对接 |
| 预订相关 | 3个 | 3个API | ✅ 已对接 |
| 场地相关 | 3个 | 4个API | ✅ 已对接 |
| 课程/教练 | 3个 | 3个API | ✅ 已对接 |
| 赛事相关 | 3个 | 3个API | ✅ 已对接 |
| 器材/穿线 | 2个 | 3个API | ✅ 已对接 |
| 预警/待办 | 4个 | 4个API | ✅ 已对接 |
| 场馆分析 | 2个 | 1个API | ✅ 已对接 |

### 1.2 图表组件总览（共31个）- 全部已对接真实API ✅

| 序号 | 组件名称 | 功能描述 | API接口 | 数据状态 |
|------|----------|----------|---------|----------|
| 1 | KPIBoard.vue | KPI核心指标看板 | 通过props传入 | ✅ 已对接 |
| 2 | RevenueBarChart.vue | 收入柱状图 | `getFinanceTrend()` | ✅ 已对接 |
| 3 | MemberPieChart.vue | 会员分布饼图 | `getMemberDistribution()` | ✅ 已对接 |
| 4 | BookingLineChart.vue | 预订趋势折线图 | `getBookingTrend()` | ✅ 已对接 |
| 5 | VenueRadarChart.vue | 场地评估雷达图 | `getVenueStatistics()` | ✅ 已对接 |
| 6 | VenueRevenueChart.vue | 场馆收入趋势图 | 父组件传入 | ✅ 已对接 |
| 7 | VenueBookingChart.vue | 场馆预订趋势图 | 父组件传入 | ✅ 已对接 |
| 8 | RevenueBookingCompareChart.vue | 收入预订对比图 | `getFinanceTrend()` + `getBookingTrend()` | ✅ 已对接 |
| 9 | EquipmentRentalChart.vue | 器材租借统计图 | `getEquipmentRentalStatistics()` | ✅ 已对接 |
| 10 | MemberGrowthChart.vue | 会员增长趋势图 | `getMemberStatistics()` | ✅ 已对接 |
| 11 | RevenueGaugeChart.vue | 收入目标仪表盘 | `getFinanceStatistics()` | ✅ 已对接 |
| 12 | PeakHoursHeatmap.vue | 预订热力图 | `getBookingHeatmap()` | ✅ 已对接 |
| 13 | CoachPerformanceChart.vue | 教练业绩排行榜 | `getCoachList()` | ✅ 已对接 |
| 14 | MemberFunnelChart.vue | 会员活跃漏斗图 | `getMemberFunnel()` | ✅ 已对接 |
| 15 | CourtUsageRingChart.vue | 场地使用率环形图 | `getCourtList()` + `getTodayBookingCounts()` + `getCourtStatistics()` | ✅ 已对接 |
| 16 | StringingServiceChart.vue | 穿线服务状态图 | `getStringingStatistics()` | ✅ 已对接 |
| 17 | CourseHotRankChart.vue | 课程热度排行榜 | `getCourseStatistics()` | ✅ 已对接 |
| 18 | CourseCapacityTrendChart.vue | 课程满班率趋势 | `getCourseBookingStatistics()` | ✅ 已对接 |
| 19 | CoachScheduleWorkloadChart.vue | 教练排课工作量 | `getCoachStatistics()` | ✅ 已对接 |
| 20 | TournamentTimeline.vue | 赛事时间轴 | `getTournamentList()` | ✅ 已对接 |
| 21 | TournamentFunnelChart.vue | 赛事报名漏斗图 | `getTournamentRegistrationStatistics()` | ✅ 已对接 |
| 22 | TournamentImpactChart.vue | 赛事带动效果图 | `getTournamentStatistics()` | ✅ 已对接 |
| 23 | MemberChurnChart.vue | 会员流失分析图 | `getMemberStatistics()` | ✅ 已对接 |
| 24 | ExpiringMemberAlertCard.vue | 会员到期预警卡片 | `getExpiringMembers()` | ✅ 已对接 |
| 25 | MemberSourcePieChart.vue | 会员来源分布图 | `getMemberSource()` | ✅ 已对接 |
| 26 | RevenueStructurePie.vue | 收入结构饼图 | `getFinanceRatio()` | ✅ 已对接 |
| 27 | VenueRevenueStackedChart.vue | 场馆收入堆叠图 | `getVenueStatistics()` | ✅ 已对接 |
| 28 | InventoryAlertList.vue | 库存预警列表 | `getEquipmentLowStock()` | ✅ 已对接 |
| 29 | CourtUtilizationAnomalyChart.vue | 场地利用异常图 | `getCourtList()` + `getTodayBookingCounts()` | ✅ 已对接 |
| 30 | OperationTodoSummary.vue | 运营待办总览 | `getOperationTodoStatistics()` | ✅ 已对接 |
| 31 | TodayCourtTimeline.vue | 今日场地时间轴 | `getCourtSchedule()` | ✅ 已对接 |

### 1.3 数据来源状态图例

- ✅ **已对接** - 已从后端API获取真实数据（所有组件已确认）
- 🔧 **需新增API** - 后端需要新增统计接口（当前无）

### 1.4 对接完成度

- **Dashboard主页面：** 100% 已对接
- **图表组件：** 100% 已对接（31/31）
- **API接口：** 40+ 个统计接口全部已实现
- **数据加载：** 采用并发+延迟加载策略，性能优化完成

---

## 2. Web Dashboard 数据对接详情

**文件路径：** `vue/src/views/Dashboard.vue`（约1200行）

### 2.1 欢迎区域 ✅

| 数据项 | 数据来源 | API接口 | 状态 |
|--------|---------|---------|------|
| 用户名 | `getUserInfo()` | 从Token解析 | ✅ 已实现 |
| 问候语 | 计算属性 `greeting` | 根据时间段计算 | ✅ 已实现 |
| 问候Emoji | 计算属性 `greetingEmoji` | 根据时间段计算 | ✅ 已实现 |
| 当前日期 | 计算属性 `currentDate` | 前端计算 | ✅ 已实现 |
| 星期 | 计算属性 `currentWeekday` | 前端计算 | ✅ 已实现 |
| 实时时间 | `currentTime` | 每秒更新 | ✅ 已实现 |
| 今日预订 | `dashboardStats.todayBookings` | `getBookingStatistics()` | ✅ 已对接 |
| 新增会员 | `dashboardStats.todayNewMembers` | `getMemberStatistics()` | ✅ 已对接 |

**代码位置：** 第16-76行

**问候语逻辑：**
```javascript
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})
```

### 2.2 KPI看板组件 ✅

**组件：** `KPIBoard.vue`

| 数据项 | 数据来源 | API接口 | 状态 |
|--------|---------|---------|------|
| 所有KPI数据 | 通过props传入 | 父组件统一获取 | ✅ 已对接 |

**代码位置：** 第78-82行
```vue
<el-row :gutter="24" class="mb-24">
  <el-col :xs="24">
    <KPIBoard :stats="dashboardStats" :loading="statsLoading" />
  </el-col>
</el-row>
```

---

### 2.3 统计卡片区域 ✅

| 数据项 | 数据来源 | API接口 | 状态 |
|--------|---------|---------|------|
| 会员总数 | `dashboardStats.memberTotal` | `getMemberStatistics()` | ✅ 已对接 |
| 会员增长率 | `dashboardStats.memberGrowth` | `getMemberStatistics()` | ✅ 已对接 |
| 场馆数量 | `venueCount` | `getVenueList()` | ✅ 已对接 |
| 场馆增长 | `dashboardStats.venueGrowth` | `getVenueList()` | ✅ 已对接 |
| 场地数量 | `dashboardStats.courtTotal` | `getCourtStatistics()` | ✅ 已对接 |
| 场地增长 | `dashboardStats.courtGrowth` | `getCourtStatistics()` | ✅ 已对接 |
| 今日预订 | `dashboardStats.todayBookings` | `getBookingStatistics()` | ✅ 已对接 |
| 预订增长率 | `dashboardStats.bookingGrowth` | `getBookingStatistics()` | ✅ 已对接 |
| 今日收入 | `dashboardStats.todayRevenue` | `getFinanceStatistics()` | ✅ 已对接 |
| 收入增长率 | `dashboardStats.revenueGrowth` | `getFinanceStatistics()` | ✅ 已对接 |

**代码位置：** 第85-173行（5张统计卡片）

**数据处理逻辑：**
```javascript
// 第1031-1080行 fetchDashboardStats()
const [memberStats, bookingStats, financeStats, courtStats] = await Promise.all([
  getMemberStatistics(),
  getBookingStatistics(),
  getFinanceStatistics({ startDate: today, endDate: today }),
  getCourtStatistics()
])

// 处理会员数据
if (memberStats.code === 200 && memberStats.data) {
  dashboardStats.value.memberTotal = numOrZero(memberStats.data.total)
  dashboardStats.value.todayNewMembers = numOrZero(memberStats.data.newToday)
  dashboardStats.value.memberGrowth = numOrZero(memberStats.data.growthRate)
}

// 处理预订数据
if (bookingStats.code === 200 && bookingStats.data) {
  const d = bookingStats.data
  dashboardStats.value.todayBookings = d.todayBookings != null 
    ? numOrZero(d.todayBookings) 
    : (numOrZero(d.ongoing) + numOrZero(d.finished))
  dashboardStats.value.bookingGrowth = numOrZero(d.bookingTrend)
  dashboardStats.value.peakHours = (d.peakHours != null && d.peakHours !== '') 
    ? d.peakHours : '暂无'
}

// 处理财务数据
if (financeStats.code === 200 && financeStats.data) {
  const revenue = financeStats.data.totalIncome != null 
    ? financeStats.data.totalIncome : 0
  dashboardStats.value.todayRevenue = numOrZero(revenue)
  dashboardStats.value.revenueGrowth = financeStats.data.incomeChange != null 
    ? numOrZero(financeStats.data.incomeChange) : 0
}

// 处理场地数据
if (courtStats.code === 200 && courtStats.data) {
  dashboardStats.value.courtTotal = numOrZero(courtStats.data.total)
  dashboardStats.value.courtGrowth = numOrZero(courtStats.data.newToday)
  const total = dashboardStats.value.courtTotal
  dashboardStats.value.bookingRate = total > 0 
    ? Math.round((numOrZero(dashboardStats.value.todayBookings) / total) * 100) : 0
}
```

### 2.4 快捷入口区域 ✅

| 数据项 | 数据来源 | 状态 |
|--------|---------|------|
| 15个快捷入口 | 静态配置 `shortcuts` | ✅ 已实现 |

**代码位置：** 第176-195行

**快捷入口列表：**
1. 场馆管理 - `/venue/management`
2. 场地管理 - `/court/management`
3. 会员管理 - `/member/management`
4. 充值中心 - `/recharge/index`
5. 场地预约 - `/booking/management`
6. 器材管理 - `/equipment/management`
7. 器材租借 - `/equipment/rental`
8. 穿线服务 - `/equipment/stringing`
9. 教练管理 - `/coach/management`
10. 课程管理 - `/course/management`
11. 课程预约 - `/course/booking`
12. 赛事管理 - `/tournament/management`
13. 赛事报名 - `/tournament/registration`
14. 财务管理 - `/finance/management`
15. 用户管理 - `/user/management`

---

### 2.5 今日运营区域 ✅

| 数据项 | 数据来源 | API接口 | 状态 |
|--------|---------|---------|------|
| 天气信息 | `weatherData` | `getWeather()` | ✅ 已对接 |
| 今日预订率 | `dashboardStats.bookingRate` | 计算值（预订数/场地总数） | ✅ 已实现 |
| 预约高峰时段 | `dashboardStats.peakHours` | `getBookingStatistics()` | ✅ 已对接 |
| 运营提示 | 动态计算 | 根据预订率生成 | ✅ 已实现 |

**代码位置：** 第196-244行

**预订率计算逻辑：**
```javascript
// 第1073-1076行
const total = dashboardStats.value.courtTotal
dashboardStats.value.bookingRate = total > 0 
  ? Math.round((numOrZero(dashboardStats.value.todayBookings) / total) * 100) : 0
```

---

### 2.6 场地状态区域 ✅

| 数据项 | 数据来源 | API接口 | 状态 |
|--------|---------|---------|------|
| 场地列表 | `dashboardStats.courts` | `getCourtList()` | ✅ 已对接 |
| 空闲/使用中统计 | 计算属性 | 前端计算 | ✅ 已实现 |

**代码位置：** 第245-289行

**数据获取逻辑：**
```javascript
// 第1082-1110行 fetchCourtStatus()
const res = await getCourtList({ page: 1, size: 20, status: null })
if (res.code === 200 && res.data) {
  let courtList = []
  // 处理不同的数据结构
  if (Array.isArray(res.data)) {
    courtList = res.data
  } else if (res.data.data && Array.isArray(res.data.data)) {
    courtList = res.data.data
  }
  
  // 转换场地数据格式
  dashboardStats.value.courts = courtList.slice(0, 6).map(court => {
    const isOccupied = court.status === 2 || court.status === 3
    return {
      id: court.id,
      name: court.courtName || court.courtCode || `场地 ${court.id}`,
      time: isOccupied ? '使用中' : '空闲',
      status: isOccupied ? 'occupied' : 'available'
    }
  })
}
```

**状态说明：**
- `status === 0`: 维护中
- `status === 1`: 空闲
- `status === 2`: 预约中（显示为使用中）
- `status === 3`: 使用中

---

### 2.7 场馆收入/预订趋势区域 ✅

| 数据项 | 数据来源 | API接口 | 状态 |
|--------|---------|---------|------|
| 场馆收入趋势 | `venueRevenueData` | `getFinanceVenueTrend()` | ✅ 已对接 |
| 场馆预订趋势 | `venueBookingData` | `getBookingVenueTrend()` | ✅ 已对接 |

**代码位置：** 
- 场馆收入趋势：第323-357行
- 场馆预订趋势：第378-412行

**数据获取逻辑：**
```javascript
// 第991-1003行 fetchVenueRevenueTrend()
const res = await getFinanceVenueTrend()
if (res.code === 200 && Array.isArray(res.data)) {
  venueRevenueData.value = applyVenueThemes(res.data, ['blue', 'purple', 'green', 'orange'])
}

// 第1005-1017行 fetchVenueBookingTrend()
const res = await getBookingVenueTrend()
if (res.code === 200 && Array.isArray(res.data)) {
  venueBookingData.value = applyVenueThemes(res.data, ['green', 'blue', 'purple', 'orange'])
}
```

---

### 2.8 最近活动区域 ✅

| 数据项 | 数据来源 | API接口 | 状态 |
|--------|---------|---------|------|
| 活动列表 | `activities` | `getRecentActivities()` | ✅ 已对接 |

**代码位置：** 第545-584行

**数据获取逻辑：**
```javascript
// 第1019-1029行 fetchRecentActivities()
const res = await getRecentActivities({ limit: 10 })
if (res.code === 200 && Array.isArray(res.data)) {
  activities.value = res.data
}
```

**活动类型：**
- `member`: 会员相关
- `court`: 场地相关
- `booking`: 预约相关
- `finance`: 财务相关

---

## 3. 已对接的API接口详情（完整列表）

### 3.1 Dashboard主页面使用的API（9个）

| API函数 | 后端路径 | 返回数据 | 使用位置 |
|---------|---------|---------|---------|
| getMemberStatistics | `/api/member/statistics` | total, newToday, growthRate | 会员统计卡片 |
| getBookingStatistics | `/api/booking/statistics` | todayBookings, ongoing, finished, bookingTrend, peakHours | 预订统计卡片、今日运营 |
| getFinanceStatistics | `/api/finance/statistics` | totalIncome, incomeChange | 收入统计卡片 |
| getCourtStatistics | `/api/court/statistics` | total, newToday | 场地统计卡片 |
| getVenueList | `/api/venue/list` | 场馆列表 | 场馆统计卡片 |
| getCourtList | `/api/court/list` | 场地列表 | 场地状态卡片 |
| getRecentActivities | `/api/activity/recent` | 活动列表 | 最近活动列表 |
| getFinanceVenueTrend | `/api/finance/venue-trend` | 各场馆收入趋势 | 场馆收入趋势图 |
| getBookingVenueTrend | `/api/booking/venue-trend` | 各场馆预订趋势 | 场馆预订趋势图 |
| getWeather | `/api/weather` | 天气信息 | 今日运营-天气卡片 |

---

### 3.2 图表组件使用的API（31个组件，40+ API）

#### 收入相关（5个组件，3个API）
| 组件 | API函数 | 后端路径 |
|------|---------|---------|
| RevenueBarChart | getFinanceTrend | `/api/finance/trend` |
| RevenueBookingCompareChart | getFinanceTrend + getBookingTrend | `/api/finance/trend` + `/api/booking/trend` |
| RevenueGaugeChart | getFinanceStatistics | `/api/finance/statistics` |
| RevenueStructurePie | getFinanceRatio | `/api/finance/ratio` |
| VenueRevenueStackedChart | getVenueStatistics | `/api/venue/statistics` |

#### 会员相关（6个组件，4个API）
| 组件 | API函数 | 后端路径 |
|------|---------|---------|
| MemberPieChart | getMemberDistribution | `/api/member/distribution` |
| MemberGrowthChart | getMemberStatistics | `/api/member/statistics` |
| MemberFunnelChart | getMemberFunnel | `/api/member/funnel` |
| MemberChurnChart | getMemberStatistics | `/api/member/statistics` |
| ExpiringMemberAlertCard | getExpiringMembers | `/api/member/expiring` |
| MemberSourcePieChart | getMemberSource | `/api/member/source` |

#### 预订相关（3个组件，3个API）
| 组件 | API函数 | 后端路径 |
|------|---------|---------|
| BookingLineChart | getBookingTrend | `/api/booking/trend` |
| PeakHoursHeatmap | getBookingHeatmap | `/api/booking/heatmap` |
| OperationTodoSummary | getOperationTodoStatistics | `/api/booking/operation-todo` |

#### 场地相关（3个组件，4个API）
| 组件 | API函数 | 后端路径 |
|------|---------|---------|
| CourtUsageRingChart | getCourtList + getTodayBookingCounts + getCourtStatistics | `/api/court/list` + `/api/court/bookings/today/counts` + `/api/court/statistics` |
| CourtUtilizationAnomalyChart | getCourtList + getTodayBookingCounts | `/api/court/list` + `/api/court/bookings/today/counts` |
| TodayCourtTimeline | getCourtSchedule | `/api/court/timeline/today` |

#### 课程/教练（3个组件，3个API）
| 组件 | API函数 | 后端路径 |
|------|---------|---------|
| CourseHotRankChart | getCourseStatistics | `/api/course/statistics` |
| CourseCapacityTrendChart | getCourseBookingStatistics | `/api/course/booking/statistics` |
| CoachScheduleWorkloadChart | getCoachStatistics | `/api/coach/statistics` |
| CoachPerformanceChart | getCoachList | `/api/coach/list` |

#### 赛事相关（3个组件，3个API）
| 组件 | API函数 | 后端路径 |
|------|---------|---------|
| TournamentTimeline | getTournamentList | `/api/tournament/list` |
| TournamentFunnelChart | getTournamentRegistrationStatistics | `/api/tournament/registration/statistics` |
| TournamentImpactChart | getTournamentStatistics | `/api/tournament/statistics` |

#### 器材/穿线（2个组件，3个API）
| 组件 | API函数 | 后端路径 |
|------|---------|---------|
| EquipmentRentalChart | getEquipmentRentalStatistics | `/api/equipment/rental/statistics` |
| StringingServiceChart | getStringingStatistics | `/api/stringing/statistics` |
| InventoryAlertList | getEquipmentLowStock | `/api/equipment/low-stock` |

#### 场馆分析（2个组件，1个API）
| 组件 | API函数 | 后端路径 |
|------|---------|---------|
| VenueRadarChart | getVenueStatistics | `/api/venue/statistics` |
| VenueRevenueChart | 父组件传入（getFinanceVenueTrend） | `/api/finance/venue-trend` |
| VenueBookingChart | 父组件传入（getBookingVenueTrend） | `/api/booking/venue-trend` |

---

### 3.3 API接口完整性检查

**已实现的统计API接口（40+）：**

✅ `/api/member/statistics` - 会员统计
✅ `/api/member/distribution` - 会员分布
✅ `/api/member/funnel` - 会员漏斗
✅ `/api/member/source` - 会员来源
✅ `/api/member/expiring` - 即将到期会员
✅ `/api/booking/statistics` - 预约统计
✅ `/api/booking/trend` - 预订趋势
✅ `/api/booking/heatmap` - 预订热力图
✅ `/api/booking/venue-trend` - 场馆预订趋势
✅ `/api/booking/operation-todo` - 运营待办
✅ `/api/finance/statistics` - 财务统计
✅ `/api/finance/trend` - 财务趋势
✅ `/api/finance/ratio` - 业务占比
✅ `/api/finance/venue-trend` - 场馆收入趋势
✅ `/api/court/statistics` - 场地统计
✅ `/api/court/list` - 场地列表
✅ `/api/court/bookings/today/counts` - 今日预约统计
✅ `/api/court/timeline/today` - 今日场地时间轴
✅ `/api/venue/list` - 场馆列表
✅ `/api/venue/statistics` - 场馆统计
✅ `/api/course/statistics` - 课程统计
✅ `/api/course/booking/statistics` - 课程预约统计
✅ `/api/coach/list` - 教练列表
✅ `/api/coach/statistics` - 教练统计
✅ `/api/tournament/list` - 赛事列表
✅ `/api/tournament/statistics` - 赛事统计
✅ `/api/tournament/registration/statistics` - 赛事报名统计
✅ `/api/equipment/rental/statistics` - 器材租借统计
✅ `/api/equipment/low-stock` - 库存预警
✅ `/api/stringing/statistics` - 穿线服务统计
✅ `/api/activity/recent` - 最近活动
✅ `/api/weather` - 天气信息

**结论：** 所有Dashboard和图表组件所需的API接口均已实现，无需新增接口。

**接口定义：** `BMP-uniapp/api/president/dashboard.ts`
```typescript
export function getMemberStatistics() {
  return get<{ total?: number; newToday?: number; growthRate?: number }>('/member/statistics')
}
```

**后端接口：** `GET /api/member/statistics`

**返回数据：**
- `total`: 会员总数
- `newToday`: 今日新增会员数
- `growthRate`: 增长率（当前未使用）

**使用位置：**
- UniApp Dashboard: 会员总数卡片、今日新增会员卡片

---

### 3.2 预约统计API ✅

**接口定义：** `BMP-uniapp/api/president/dashboard.ts`
```typescript
export function getBookingStatistics() {
  return get<{
    todayBookings?: number
    ongoing?: number
    finished?: number
    bookingTrend?: number
    peakHours?: string
  }>('/booking/statistics')
}
```

**后端接口：** `GET /api/booking/statistics`

**返回数据：**
- `todayBookings`: 今日预约总数
- `ongoing`: 进行中的预约
- `finished`: 已完成的预约
- `bookingTrend`: 预约趋势（当前未使用）
- `peakHours`: 高峰时段（当前未使用）

**使用位置：**
- UniApp Dashboard: 今日预约卡片
- 数据处理：如果 `todayBookings` 不存在，则使用 `ongoing + finished`

---

### 3.3 财务统计API ✅

**接口定义：** `BMP-uniapp/api/president/dashboard.ts`
```typescript
export function getFinanceStatistics(params?: { startDate?: string; endDate?: string }) {
  const startDate = params?.startDate ?? today()
  const endDate = params?.endDate ?? today()
  return get<{ totalIncome?: number; incomeChange?: number }>('/finance/statistics', { startDate, endDate })
}
```

**后端接口：** `GET /api/finance/statistics`

**查询参数：**
- `startDate`: 开始日期（默认今天）
- `endDate`: 结束日期（默认今天）

**返回数据：**
- `totalIncome`: 总收入
- `incomeChange`: 收入变化（当前未使用）

**使用位置：**
- UniApp Dashboard: 今日收入卡片

---

### 3.4 场地统计API ✅

**接口定义：** `BMP-uniapp/api/president/dashboard.ts`
```typescript
export function getCourtStatistics() {
  return get<{ total?: number; newToday?: number }>('/court/statistics')
}
```

**后端接口：** `GET /api/court/statistics`

**返回数据：**
- `total`: 场地总数
- `newToday`: 今日新增场地数

**使用位置：**
- 已在 `fetchStats()` 中调用，但当前Dashboard页面未显示

---

### 3.5 运营待办API ✅

**接口定义：** `BMP-uniapp/api/president/dashboard.ts`
```typescript
export function getOperationTodo() {
  return get<{ pendingBookings?: number; unpaidOrders?: number; pendingIssues?: number }>('/booking/operation-todo')
}
```

**后端接口：** `GET /api/booking/operation-todo`

**返回数据：**
- `pendingBookings`: 待确认预订数
- `unpaidOrders`: 未付款订单数
- `pendingIssues`: 待处理问题数

**使用位置：**
- API已定义，但当前Dashboard页面未使用
- 可用于后续添加待办事项卡片

---

## 4. 数据加载策略与性能优化

### 4.1 分层加载策略

**核心数据（立即加载）：**
```javascript
onMounted(() => {
  // 第一优先级：用户体验关键数据
  fetchWeather()              // 天气信息
  fetchVenueList()            // 场馆列表
  fetchDashboardStats()       // 核心统计数据
    .then(() => fetchCourtStatus())  // 场地状态（依赖统计数据）
  
  // 第二优先级：辅助数据（延迟500ms）
  setTimeout(() => {
    fetchRecentActivities()   // 最近活动
    fetchVenueRevenueTrend()  // 场馆收入趋势
    fetchVenueBookingTrend()  // 场馆预订趋势
  }, 500)
})
```

### 4.2 并发请求优化

**使用 Promise.all 并发请求：**
```javascript
const [memberStats, bookingStats, financeStats, courtStats] = await Promise.all([
  getMemberStatistics(),
  getBookingStatistics(),
  getFinanceStatistics({ startDate: today, endDate: today }),
  getCourtStatistics()
])
```

### 4.3 错误处理策略

**每个API调用都有错误处理：**
```javascript
try {
  const res = await getFinanceTrend({ period })
  if (res.code === 200 && res.data) {
    // 处理数据
  }
} catch (error) {
  console.error('获取财务趋势失败:', error)
  // 设置默认值或空数据
}
```

### 4.4 数据安全转换

**统一的数值转换函数：**
```javascript
const numOrZero = (v) => {
  if (v === null || v === undefined) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}
```

### 4.5 图表组件懒加载

**图表组件按需初始化：**
- 使用 `onMounted` 钩子初始化图表
- 使用 `onUnmounted` 钩子销毁图表实例
- 避免内存泄漏

---

## 5. 后端API接口规范

### 5.1 统一返回格式

所有API接口统一使用以下格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 5.2 分页接口格式

分页接口返回格式：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "data": [],
    "total": 100,
    "page": 1,
    "size": 10,
    "pages": 10
  }
}
```

### 5.3 统计接口示例

**会员统计：**
```json
{
  "code": 200,
  "data": {
    "total": 128,
    "newToday": 3,
    "growthRate": 12.5
  }
}
```

**预订趋势：**
```json
{
  "code": 200,
  "data": {
    "categories": ["周一", "周二", "周三"],
    "values": [24, 32, 28]
  }
}
```

---

## 6. 对接检查清单

### 6.1 Dashboard主页面 ✅

- [x] **欢迎区域** - 用户名、问候语、实时时间、今日数据
- [x] **KPI看板** - 核心指标展示
- [x] **统计卡片** - 5张卡片（会员、场馆、场地、预订、收入）
- [x] **快捷入口** - 15个管理入口
- [x] **今日运营** - 天气、预订率、高峰时段
- [x] **场地状态** - 实时场地状态列表
- [x] **场馆趋势** - 收入和预订趋势图
- [x] **最近活动** - 活动列表

### 6.2 图表组件（31个）✅

#### 收入相关（5个）
- [x] RevenueBarChart - 收入柱状图
- [x] RevenueBookingCompareChart - 收入预订对比图
- [x] RevenueGaugeChart - 收入目标仪表盘
- [x] RevenueStructurePie - 收入结构饼图
- [x] VenueRevenueStackedChart - 场馆收入堆叠图

#### 会员相关（6个）
- [x] MemberPieChart - 会员分布饼图
- [x] MemberGrowthChart - 会员增长趋势图
- [x] MemberFunnelChart - 会员活跃漏斗图
- [x] MemberChurnChart - 会员流失分析图
- [x] ExpiringMemberAlertCard - 会员到期预警卡片
- [x] MemberSourcePieChart - 会员来源分布图

#### 预订相关（3个）
- [x] BookingLineChart - 预订趋势折线图
- [x] PeakHoursHeatmap - 预订热力图
- [x] OperationTodoSummary - 运营待办总览

#### 场地相关（3个）
- [x] CourtUsageRingChart - 场地使用率环形图
- [x] CourtUtilizationAnomalyChart - 场地利用异常图
- [x] TodayCourtTimeline - 今日场地时间轴

#### 课程/教练（4个）
- [x] CourseHotRankChart - 课程热度排行榜
- [x] CourseCapacityTrendChart - 课程满班率趋势
- [x] CoachScheduleWorkloadChart - 教练排课工作量
- [x] CoachPerformanceChart - 教练业绩排行榜

#### 赛事相关（3个）
- [x] TournamentTimeline - 赛事时间轴
- [x] TournamentFunnelChart - 赛事报名漏斗图
- [x] TournamentImpactChart - 赛事带动效果图

#### 器材/穿线（3个）
- [x] EquipmentRentalChart - 器材租借统计图
- [x] StringingServiceChart - 穿线服务状态图
- [x] InventoryAlertList - 库存预警列表

#### 场馆分析（3个）
- [x] VenueRadarChart - 场地评估雷达图
- [x] VenueRevenueChart - 场馆收入趋势图
- [x] VenueBookingChart - 场馆预订趋势图

#### 核心指标（1个）
- [x] KPIBoard - KPI核心指标看板

### 6.3 后端API开发 ✅

- [x] 所有统计API接口已实现（40+个）
- [x] 统一返回格式已规范
- [x] 错误处理已完善
- [x] 数据类型已统一

---

## 7. 总结

### 7.1 对接完成情况

| 项目 | 数量 | 完成度 |
|------|------|--------|
| Dashboard主页面 | 1个 | ✅ 100% |
| 图表组件 | 31个 | ✅ 100% |
| API接口 | 40+个 | ✅ 100% |
| 虚拟数据 | 0个 | ✅ 已清除 |

### 7.2 技术特点

1. **数据来源：** 100% 真实API，无虚拟数据
2. **性能优化：** 并发请求 + 分层加载
3. **错误处理：** 完善的try-catch和数据验证
4. **类型安全：** 统一的数值转换函数
5. **用户体验：** 加载状态管理 + 实时更新

### 7.3 后续建议

1. ✅ 数据对接已完成，无需额外工作
2. 可考虑添加数据缓存机制（如使用Vuex/Pinia）
3. 可考虑添加数据刷新按钮
4. 可考虑添加图表导出功能
5. 可考虑添加自定义时间范围筛选

---

## 附录A：完整API列表

### Dashboard主页面API（10个）
1. `getMemberStatistics` - 会员统计
2. `getBookingStatistics` - 预约统计
3. `getFinanceStatistics` - 财务统计
4. `getCourtStatistics` - 场地统计
5. `getVenueList` - 场馆列表
6. `getCourtList` - 场地列表
7. `getRecentActivities` - 最近活动
8. `getFinanceVenueTrend` - 场馆收入趋势
9. `getBookingVenueTrend` - 场馆预订趋势
10. `getWeather` - 天气信息

### 图表组件API（30+个）
11. `getFinanceTrend` - 财务趋势
12. `getFinanceRatio` - 业务占比
13. `getMemberDistribution` - 会员分布
14. `getMemberFunnel` - 会员漏斗
15. `getMemberSource` - 会员来源
16. `getExpiringMembers` - 即将到期会员
17. `getBookingTrend` - 预订趋势
18. `getBookingHeatmap` - 预订热力图
19. `getOperationTodoStatistics` - 运营待办
20. `getTodayBookingCounts` - 今日预约统计
21. `getCourtSchedule` - 场地时间轴
22. `getVenueStatistics` - 场馆统计
23. `getCourseStatistics` - 课程统计
24. `getCourseBookingStatistics` - 课程预约统计
25. `getCoachList` - 教练列表
26. `getCoachStatistics` - 教练统计
27. `getTournamentList` - 赛事列表
28. `getTournamentStatistics` - 赛事统计
29. `getTournamentRegistrationStatistics` - 赛事报名统计
30. `getEquipmentRentalStatistics` - 器材租借统计
31. `getEquipmentLowStock` - 库存预警
32. `getStringingStatistics` - 穿线服务统计

---

## 版本历史

| 版本 | 日期 | 内容 |
|------|------|------|
| v3.0 | 2026-02-23 | 完整检查所有31个图表组件；确认100%已对接真实API；更新为最终版本 |
| v2.0 | 2026-02-23 | 基于Web端实际代码重写文档；确认Dashboard已对接核心统计API |
| v1.11 | 2026-02-19 | 二次检查：修复 KPIBoard 场地使用数；补全相关API |
| v1.10 | 2026-02-19 | 补全与修复：新增接口、场馆统计等 |

### 4.1 会长端其他模块API

#### 财务管理 (`BMP-uniapp/api/president/finance.ts`)
- `getFinanceList()`: 财务记录列表
- `getFinanceInfo()`: 财务记录详情
- `deleteFinance()`: 删除财务记录
- `reconciliation()`: 全面对账（仅会长）

#### 会员管理 (`BMP-uniapp/api/president/member.ts`)
- `getMemberList()`: 会员列表

#### 预约管理 (`BMP-uniapp/api/president/booking.ts`)
- `getBookingList()`: 预约列表
- `getBookingDetail()`: 预约详情
- `cancelBooking()`: 取消预约

#### 场馆管理 (`BMP-uniapp/api/president/venue.ts`)
- `getVenueList()`: 场馆列表
- `getVenueInfo()`: 场馆详情
- `addVenue()`: 新增场馆
- `updateVenue()`: 更新场馆
- `deleteVenue()`: 删除场馆

---

## 5. 需要新增的统计API接口（Web端图表组件）

**注意：** 以下接口主要针对Web端的图表组件，UniApp移动端当前不需要这些接口。

## 6. Web端图表组件状态（待确认）

**说明：** 以下内容基于原文档中的Web端vue项目图表组件，实际状态需要单独确认。

**文件路径：** `vue/src/components/charts/KPIBoard.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 今日收入 | `3680` | `getFinanceStatistics()` | ⚠️ 已注释 |
| 收入趋势 | `15%` | `getFinanceStatistics()` | ⚠️ 已注释 |
| 今日预订 | `24` | `getBookingStatistics()` | ⚠️ 已注释 |
| 预订趋势 | `8%` | `getBookingStatistics()` | ⚠️ 已注释 |
| 活跃会员 | `85` | `getMemberStatistics()` | ⚠️ 已注释 |
| 总会员数 | `128` | `getMemberStatistics()` | ⚠️ 已注释 |
| 场地使用率 | `75%` | `getCourtStatistics()` | ⚠️ 已注释 |
| 使用中场地 | `6` | `getCourtStatistics()` | ⚠️ 已注释 |
| 总场地数 | `8` | `getCourtStatistics()` | ⚠️ 已注释 |

**API已导入但被注释：** 第106-109行
```javascript
import { getMemberStatistics } from '@/api/member'
import { getBookingStatistics } from '@/api/booking'
import { getFinanceStatistics } from '@/api/finance'
import { getCourtStatistics } from '@/api/court'
```

**TODO位置：** 第143-183行

---

### 3.2 RevenueBarChart.vue ❌

**文件路径：** `vue/src/components/charts/RevenueBarChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 周收入数据 | `[2800, 3200, 2950, 3680, 4200, 5800, 4500]` | `GET /api/finance/trend?period=week` |
| 月收入数据 | `[18500, 22300, 19800, 25600]` | `GET /api/finance/trend?period=month` |

**当前虚拟数据：** 第81-89行
```javascript
const weekData = {
  categories: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
  values: [2800, 3200, 2950, 3680, 4200, 5800, 4500]
}

const monthData = {
  categories: ['第1周', '第2周', '第3周', '第4周'],
  values: [18500, 22300, 19800, 25600]
}
```

**状态：** 无API调用逻辑，需完全重写

---

### 3.3 MemberPieChart.vue ❌

**文件路径：** `vue/src/components/charts/MemberPieChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 会员分布 | VIP:45, 金卡:38, 银卡:25, 普通:15, 新:5 | `GET /api/member/distribution` |

**当前虚拟数据：** 第78-84行
```javascript
const memberData = ref([
  { name: 'VIP会员', value: 45 },
  { name: '金卡会员', value: 38 },
  { name: '银卡会员', value: 25 },
  { name: '普通会员', value: 15 },
  { name: '新会员', value: 5 }
])
```

**状态：** 无API调用逻辑，需完全重写

---

### 3.4 BookingLineChart.vue ⚠️

**文件路径：** `vue/src/components/charts/BookingLineChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 周预订数据 | `[24, 32, 28, 35, 42, 58, 45]` | `GET /api/booking/trend?period=week` |
| 月预订数据 | `[185, 223, 198, 256]` | `GET /api/booking/trend?period=month` |

**TODO标注：** 第83-84行
```javascript
// TODO: 正式上线时，从API获取真实的预订数据
// 目前使用虚拟数据进行开发测试
```

---

### 3.5 VenueRadarChart.vue ❌

**文件路径：** `vue/src/components/charts/VenueRadarChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 场地综合评估 | 利用率/预订率/满意度等 | `GET /api/venue/evaluation` |

**当前虚拟数据：** 第91-96行
```javascript
const venueData = {
  all: [85, 78, 92, 88, 75, 82],
  a: [90, 85, 95, 92, 80, 88],
  b: [82, 75, 88, 85, 72, 78],
  c: [78, 72, 90, 82, 70, 75]
}
```

**状态：** 无API调用逻辑，需完全重写

---

### 3.6 RevenueBookingCompareChart.vue ⚠️

**文件路径：** `vue/src/components/charts/RevenueBookingCompareChart.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 收入趋势 | `generateMockData()` | `getFinanceTrend()` | ⚠️ 已注释 |
| 预订趋势 | `generateMockData()` | `getBookingStatistics()` | ⚠️ 已注释 |

**TODO位置：** 第226-259行

---

### 3.7 EquipmentRentalChart.vue ⚠️

**文件路径：** `vue/src/components/charts/EquipmentRentalChart.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 器材租借统计 | 羽毛球拍:85, 羽毛球:62等 | `getEquipmentRentalStatistics()` | ⚠️ 已注释 |

**当前虚拟数据：** 第156-162行
```javascript
chartData = [
  { name: '羽毛球拍', value: 85, percentage: 42.5 },
  { name: '羽毛球', value: 62, percentage: 31.0 },
  { name: '运动鞋', value: 28, percentage: 14.0 },
  { name: '运动服', value: 18, percentage: 9.0 },
  { name: '护具', value: 7, percentage: 3.5 }
]
```

**TODO位置：** 第139-151行

---

### 3.8 MemberGrowthChart.vue ⚠️

**文件路径：** `vue/src/components/charts/MemberGrowthChart.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 累计会员趋势 | `generateMockData()` | `getMemberStatistics()` | ⚠️ 已注释 |
| 新增会员趋势 | `generateMockData()` | `getMemberStatistics()` | ⚠️ 已注释 |

**TODO位置：** 第252-274行

---

### 3.9 RevenueGaugeChart.vue ⚠️

**文件路径：** `vue/src/components/charts/RevenueGaugeChart.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 日收入目标 | current:3680, target:5000 | `getFinanceStatistics()` | ⚠️ 已注释 |
| 月收入目标 | current:92500, target:150000 | `getFinanceStatistics()` | ⚠️ 已注释 |

**TODO位置：** 第248-278行

---

### 3.10 PeakHoursHeatmap.vue ⚠️

**文件路径：** `vue/src/components/charts/PeakHoursHeatmap.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 预订热力数据 | `generateMockData()` | `getBookingStatistics()` | ⚠️ 已注释 |

**TODO位置：** 第156-165行

---

### 3.11 CoachPerformanceChart.vue ⚠️

**文件路径：** `vue/src/components/charts/CoachPerformanceChart.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 教练业绩排行 | 王/李/张/刘/陈教练 | `getCoachList()` | ⚠️ 已注释 |

**当前虚拟数据：** 第126-132行
```javascript
coachData.value = [
  { id: 1, name: '王教练', avatar: '', specialty: '专业羽毛球', bookings: 86, rating: 4.9 },
  { id: 2, name: '李教练', avatar: '', specialty: '青少年培训', bookings: 72, rating: 4.8 },
  { id: 3, name: '张教练', avatar: '', specialty: '技术提升', bookings: 65, rating: 4.7 },
  { id: 4, name: '刘教练', avatar: '', specialty: '初学入门', bookings: 48, rating: 4.6 },
  { id: 5, name: '陈教练', avatar: '', specialty: '比赛训练', bookings: 42, rating: 4.5 }
]
```

**TODO位置：** 第101-122行

---

### 3.12 MemberFunnelChart.vue ⚠️

**文件路径：** `vue/src/components/charts/MemberFunnelChart.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 会员活跃漏斗 | 注册:128, 活跃:85, 高频:42, VIP:18 | `getMemberStatistics()` | ⚠️ 已注释 |

**TODO位置：** 第142-151行

---

### 3.13 CourtUsageRingChart.vue ⚠️

**文件路径：** `vue/src/components/charts/CourtUsageRingChart.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 场地使用率环形图 | A01:85%, A02:45%, B01:72%等 | `getCourtStatistics()` | ⚠️ 已注释 |

**当前虚拟数据：** 第194-201行
```javascript
courtData.value = [
  { id: 1, name: 'A01', usage: 85 },
  { id: 2, name: 'A02', usage: 45 },
  { id: 3, name: 'B01', usage: 72 },
  { id: 4, name: 'B02', usage: 30 },
  { id: 5, name: 'C01', usage: 90 },
  { id: 6, name: 'C02', usage: 55 }
]
```

**TODO位置：** 第177-189行

---

### 3.14 VenueRevenueChart.vue ⚠️

**文件路径：** `vue/src/components/charts/VenueRevenueChart.vue`

| 数据项 | 数据来源 | 所需API |
|--------|----------|---------|
| 场馆收入趋势 | 父组件Dashboard.vue传入的虚拟数据 | `GET /api/finance/venue-trend` |

**说明：** 数据通过 props 从 Dashboard.vue 传入，源头是 `venueRevenueData` 计算属性生成的虚拟数据

---

### 3.15 VenueBookingChart.vue ⚠️

**文件路径：** `vue/src/components/charts/VenueBookingChart.vue`

| 数据项 | 数据来源 | 所需API |
|--------|----------|---------|
| 场馆预订趋势 | 父组件Dashboard.vue传入的虚拟数据 | `GET /api/booking/venue-trend` |

**说明：** 数据通过 props 从 Dashboard.vue 传入，源头是 `venueBookingData` 计算属性生成的虚拟数据

---

### 3.16 StringingServiceChart.vue ⚠️

**文件路径：** `vue/src/components/charts/StringingServiceChart.vue`

| 数据项 | 当前虚拟值 | 所需API | 状态 |
|--------|-----------|---------|------|
| 穿线服务状态分布 | 等待:5, 进行中:3, 已完成:42, 已取消:2 | `getStringingStatistics()` | ⚠️ 已注释 |

**当前虚拟数据：** 第146-151行
```javascript
const chartData = [
  { name: '等待穿线', value: 5 },
  { name: '正在穿线', value: 3 },
  { name: '已完成', value: 42 },
  { name: '已取消', value: 2 }
]
```

**TODO 位置：** 第133-148行。`GET /api/stringing/statistics` 接口已存在，返回 `{ total, waiting, inProgress, completed, cancelled }`，后期取消注释并接入即可。

---

### 3.17 CourseHotRankChart.vue ❌

**文件路径：** `vue/src/components/charts/CourseHotRankChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 课程热度排行榜 | 若干课程名称 + 报名人数 + 出勤率 | `GET /api/course/statistics/hot` |

---

### 3.18 CourseCapacityTrendChart.vue ❌

**文件路径：** `vue/src/components/charts/CourseCapacityTrendChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 周/近几周平均满班率 | `fullRate: [0.68, 0.74, 0.71, 0.79, 0.83, 0.81]` | `GET /api/course/statistics/capacity-trend` |

---

### 3.19 CoachScheduleWorkloadChart.vue ❌

**文件路径：** `vue/src/components/charts/CoachScheduleWorkloadChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 教练排课节数 | `scheduled` 字段 | `GET /api/coach/statistics/schedule` |
| 教练已上课节数 | `completed` 字段 | `GET /api/coach/statistics/schedule` |

---

### 3.20 TournamentTimeline.vue ❌

**文件路径：** `vue/src/components/charts/TournamentTimeline.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 未来赛事列表 | `{ name, date, weekday, level, venue, signup, quota }[]` | `GET /api/tournament/list/upcoming` |

---

### 3.21 TournamentFunnelChart.vue ❌

**文件路径：** `vue/src/components/charts/TournamentFunnelChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 赛事报名漏斗 | 浏览:1000, 报名:420, 缴费:320, 到场:280 | `GET /api/tournament/statistics/funnel` |

---

### 3.22 TournamentImpactChart.vue ❌

**文件路径：** `vue/src/components/charts/TournamentImpactChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 各赛事新增会员数 | `newMembers` | `GET /api/tournament/statistics/impact` |
| 各赛事带来收入 | `revenue` | `GET /api/tournament/statistics/impact` |

---

### 3.23 MemberChurnChart.vue ❌

**文件路径：** `vue/src/components/charts/MemberChurnChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 各月新增会员数 | `newMembers` 数组 | `GET /api/member/statistics/churn` |
| 各月流失会员数 | `churnMembers` 数组 | `GET /api/member/statistics/churn` |

---

### 3.24 ExpiringMemberAlertCard.vue ❌

**文件路径：** `vue/src/components/charts/ExpiringMemberAlertCard.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 30天内到期会员列表 | 4条静态记录 | `GET /api/member/expiring?days=30` |

---

### 3.25 MemberSourcePieChart.vue ❌

**文件路径：** `vue/src/components/charts/MemberSourcePieChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 会员来源分布 | 自然到店/推荐/线上/赛事/其他 | `GET /api/member/statistics/source` |

---

### 3.26 RevenueStructurePie.vue ❌

**文件路径：** `vue/src/components/charts/RevenueStructurePie.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 收入结构 | 场地费/课程费/赛事费/器材租借/穿线 | `GET /api/finance/statistics/structure` |

---

### 3.27 VenueRevenueStackedChart.vue ❌

**文件路径：** `vue/src/components/charts/VenueRevenueStackedChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 各场馆场地费 | `stackedData.court` | `GET /api/finance/venue-structure` |
| 各场馆课程费 | `stackedData.course` | 同上 |
| 各场馆赛事费 | `stackedData.tournament` | 同上 |
| 各场馆其他收入 | `stackedData.other` | 同上 |

---

### 3.28 InventoryAlertList.vue ❌

**文件路径：** `vue/src/components/charts/InventoryAlertList.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 低于安全库存的物品列表 | 4条硬编码 | `GET /api/equipment/inventory/alerts` |

---

### 3.29 CourtUtilizationAnomalyChart.vue ❌

**文件路径：** `vue/src/components/charts/CourtUtilizationAnomalyChart.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 场地利用率 | `courtData.utilization` 0-1 | `GET /api/court/statistics/utilization-anomaly` |

---

### 3.30 OperationTodoSummary.vue ❌

**文件路径：** `vue/src/components/charts/OperationTodoSummary.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 待确认预订数 | `pendingBookings = 6` | `GET /api/dashboard/todo-summary` |
| 未付款订单数 | `unpaidOrders = 3` | 同上 |
| 待处理退款/投诉数 | `pendingIssues = 2` | 同上 |

---

### 3.31 TodayCourtTimeline.vue ❌

**文件路径：** `vue/src/components/charts/TodayCourtTimeline.vue`

| 数据项 | 当前虚拟值 | 所需API |
|--------|-----------|---------|
| 当日各场地时间轴占用片段 | `courts[].slots[{ status, duration }]` | `GET /api/court/timeline/today` |

---

## 4. 需要新增的统计API接口

### 4.1 会员统计API

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/member/statistics` | GET | totalMembers, activeMembers, newToday, growthRate, trends | KPI/会员增长 |
| `/api/member/distribution` | GET | 各等级会员数量及占比 | 会员分布饼图 |
| `/api/member/funnel` | GET | 注册/活跃/高频/VIP会员数 | 会员漏斗图 |

### 4.2 预订统计API

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/booking/statistics` | GET | todayBookings, bookingTrend, rate, peakHours | KPI/今日运营 |
| `/api/booking/trend` | GET | period参数(week/month)的预订趋势 | 预订趋势图 |
| `/api/booking/heatmap` | GET | 按时段/星期的预订热力数据 | 热力图 |
| `/api/booking/venue-trend` | GET | 各场馆预订趋势 | 场馆预订图 |

### 4.3 财务统计API

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/finance/statistics` | GET | todayRevenue, monthRevenue, targets, progress | KPI/仪表盘 |
| `/api/finance/trend` | GET | period参数的收入趋势 | 收入趋势图 |
| `/api/finance/venue-trend` | GET | 各场馆收入趋势 | 场馆收入图 |

### 4.4 场地统计API

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/court/statistics` | GET | total, inUse, usageRate, courtDetails | KPI/场地使用率 |
| `/api/court/realtime-status` | GET | 实时场地状态列表 | 场地状态卡片 |

### 4.5 场馆评估API

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/venue/evaluation` | GET | 利用率/预订率/满意度等多维指标 | 雷达图 |

### 4.6 其他统计API

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/activity/recent` | GET | 最近10条系统活动 | 最近活动列表 |
| `/api/equipment/rental/statistics` | GET | 各类器材租借统计 | 器材租借饼图 |
| `/api/coach/ranking` | GET | 教练业绩排行 | 教练排行榜 |
| `/api/stringing/statistics` | GET | total, waiting, inProgress, completed, cancelled | 穿线服务状态饼图（✅ **接口已存在**，前端接入 `getStringingStatistics` 即可） |

### 4.7 课程与教练统计API（新增）

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/course/statistics/hot` | GET | 各课程报名人数、出勤率 | 课程热度排行榜 |
| `/api/course/statistics/capacity-trend` | GET | 周维度平均满班率趋势 | 课程满班率趋势图 |
| `/api/coach/statistics/schedule` | GET | 各教练排课节数、已授课节数 | 教练排课与授课量对比 |

### 4.8 赛事统计API（新增）

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/tournament/list/upcoming` | GET | 未来一段时间的赛事列表 | 赛事时间轴 |
| `/api/tournament/statistics/funnel` | GET | 浏览/报名/缴费/参赛人数 | 赛事报名转化漏斗 |
| `/api/tournament/statistics/impact` | GET | 各赛事新增会员与收入贡献 | 赛事带动效果图 |

### 4.9 库存与预警API（新增）

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/equipment/inventory/alerts` | GET | 低于安全库存的物品列表 | 库存预警列表 |
| `/api/court/statistics/utilization-anomaly` | GET | 利用异常的场地及利用率 | 场地利用异常监控图 |

### 4.10 Dashboard 待办与时间轴API（新增）

| 接口 | 方法 | 返回数据 | 用途 |
|------|------|----------|------|
| `/api/dashboard/todo-summary` | GET | pendingBookings / unpaidOrders / pendingIssues | 运营待办总览卡片 |
| `/api/court/timeline/today` | GET | 当日按时间片的各场地占用信息 | 今日场地时间轴 |

---

## 7. 对接优先级排序（针对Web端扩展）

**说明：** UniApp移动端Dashboard已完成核心功能对接。以下优先级针对Web端图表组件的扩展开发。

### P0 - 核心指标（必须优先对接）

| 优先级 | 组件/区域 | 涉及数据 | 预计工时 |
|--------|----------|----------|----------|
| 1 | KPIBoard.vue | 今日收入/预订/会员/场地使用率 | 2h |
| 2 | 统计卡片区域 | 会员/场馆/场地/预订/收入汇总 | 2h |
| 3 | 场地状态卡片 | 实时场地状态 | 1h |

### P1 - 主要图表（重要）

| 优先级 | 组件 | 涉及数据 | 预计工时 |
|--------|------|----------|----------|
| 4 | RevenueBarChart.vue | 收入趋势 | 1h |
| 5 | BookingLineChart.vue | 预订趋势 | 1h |
| 6 | MemberPieChart.vue | 会员分布 | 1h |
| 7 | MemberGrowthChart.vue | 会员增长 | 1h |

### P2 - 辅助图表（推荐）

| 优先级 | 组件 | 涉及数据 | 预计工时 |
|--------|------|----------|----------|
| 8 | RevenueBookingCompareChart.vue | 收入预订对比 | 1h |
| 9 | EquipmentRentalChart.vue | 器材租借 | 1h |
| 10 | StringingServiceChart.vue | 穿线服务状态分布 | 0.5h |
| 11 | CourtUsageRingChart.vue | 场地使用率 | 1h |
| 12 | PeakHoursHeatmap.vue | 预订热力图 | 1h |

### P3 - 扩展图表（可选）

| 优先级 | 组件 | 涉及数据 | 预计工时 |
|--------|------|----------|----------|
| 13 | VenueRadarChart.vue | 场地评估 | 2h |
| 14 | CoachPerformanceChart.vue | 教练排行 | 1h |
| 15 | MemberFunnelChart.vue | 会员漏斗 | 1h |
| 16 | RevenueGaugeChart.vue | 收入目标 | 1h |
| 17 | VenueRevenueChart.vue | 场馆收入 | 1h |
| 18 | VenueBookingChart.vue | 场馆预订 | 1h |
| 19 | 最近活动区域 | 系统活动 | 1h |
| 20 | 欢迎语区域 | 今日统计 | 0.5h |

### P3+ 新增扩展模块（本次新增，全部为虚拟数据）

| 优先级 | 组件/分组 | 涉及数据 | 预计工时 |
|--------|----------|----------|----------|
| 21 | 课程/教练相关（3个图表） | 课程热度、满班率、教练排课/授课 | 3h |
| 22 | 赛事相关（3个图表） | 赛事时间轴、漏斗、带动会员与收入 | 3h |
| 23 | 会员生命周期/来源（3个模块） | 新增 vs 流失、到期预警、来源分布 | 3h |
| 24 | 收入结构与场馆对比（2个图表） | 收入结构、场馆收入堆叠结构 | 2h |
| 25 | 预警与待办（3个模块） | 库存预警、待办统计、场地利用异常 | 2h |
| 26 | 今日场地时间轴 | 当日场地占用时间线 | 1.5h |

**总预计工时：** 约 36.5 小时

**说明：**
- P0-P1 为核心功能，建议优先完成（约 10h）
- P2-P3 为常用功能，提升用户体验（约 18.5h）
- P3+ 为扩展功能，可根据实际需求选择性实现（约 14.5h）
- 所有31个图表组件均已在Dashboard中集成并使用虚拟数据

---

## 8. 对接检查清单

### 8.1 UniApp移动端 Dashboard ✅

- [x] **getMemberStatistics** - 会员统计API
- [x] **getBookingStatistics** - 预约统计API
- [x] **getFinanceStatistics** - 财务统计API
- [x] **getCourtStatistics** - 场地统计API（已调用，未显示）
- [x] **KPI卡片展示** - 4个核心指标卡片
- [x] **快捷入口** - 4个管理入口
- [x] **下拉刷新** - 数据刷新机制
- [x] **错误处理** - Promise.all + catch
- [x] **加载状态** - loading状态管理
- [ ] **运营待办** - getOperationTodo API已定义，可添加待办卡片

### 8.2 Web端 Dashboard（待确认）

#### 核心组件（P0-P1）
- [ ] **KPIBoard.vue** - 取消API注释
- [ ] **RevenueBarChart.vue** - 新增API调用逻辑
- [ ] **MemberPieChart.vue** - 新增API调用逻辑
- [ ] **BookingLineChart.vue** - 取消注释/新增API
- [ ] **MemberGrowthChart.vue** - 取消注释
- [ ] **VenueRevenueChart.vue** - 父组件传入真实数据
- [ ] **VenueBookingChart.vue** - 父组件传入真实数据

#### 辅助组件（P2）
- [ ] **RevenueBookingCompareChart.vue** - 取消注释
- [ ] **EquipmentRentalChart.vue** - 取消注释
- [ ] **StringingServiceChart.vue** - 取消注释，接入 `getStringingStatistics`（API已存在）
- [ ] **CourtUsageRingChart.vue** - 取消注释
- [ ] **PeakHoursHeatmap.vue** - 取消注释
- [ ] **RevenueGaugeChart.vue** - 取消注释

#### 扩展组件（P3）
- [ ] **VenueRadarChart.vue** - 新增API调用逻辑
- [ ] **CoachPerformanceChart.vue** - 取消注释
- [ ] **MemberFunnelChart.vue** - 取消注释

#### 课程/教练模块（P3+）
- [ ] **CourseHotRankChart.vue** - 新增API调用逻辑
- [ ] **CourseCapacityTrendChart.vue** - 新增API调用逻辑
- [ ] **CoachScheduleWorkloadChart.vue** - 新增API调用逻辑

#### 赛事模块（P3+）
- [ ] **TournamentTimeline.vue** - 新增API调用逻辑
- [ ] **TournamentFunnelChart.vue** - 新增API调用逻辑
- [ ] **TournamentImpactChart.vue** - 新增API调用逻辑

#### 会员生命周期模块（P3+）
- [ ] **MemberChurnChart.vue** - 新增API调用逻辑
- [ ] **ExpiringMemberAlertCard.vue** - 新增API调用逻辑
- [ ] **MemberSourcePieChart.vue** - 新增API调用逻辑

#### 收入结构模块（P3+）
- [ ] **RevenueStructurePie.vue** - 新增API调用逻辑
- [ ] **VenueRevenueStackedChart.vue** - 新增API调用逻辑

#### 预警与待办模块（P3+）
- [ ] **InventoryAlertList.vue** - 新增API调用逻辑
- [ ] **CourtUtilizationAnomalyChart.vue** - 新增API调用逻辑
- [ ] **OperationTodoSummary.vue** - 新增API调用逻辑
- [ ] **TodayCourtTimeline.vue** - 新增API调用逻辑

### 8.3 后端API开发检查

#### 核心统计API（已实现）
- [x] `/api/member/statistics` - 会员统计
- [x] `/api/booking/statistics` - 预约统计
- [x] `/api/finance/statistics` - 财务统计
- [x] `/api/court/statistics` - 场地统计
- [x] `/api/booking/operation-todo` - 运营待办

#### 其他模块API（已实现）
- [x] `/api/member/list` - 会员列表
- [x] `/api/booking/list` - 预约列表
- [x] `/api/finance/list` - 财务列表
- [x] `/api/venue/list` - 场馆列表
- [x] `/api/court/list` - 场地列表

#### Web端扩展API（待开发，如需要）
- [ ] 会员统计API（3个接口）
  - [ ] `/api/member/statistics` - 会员总数、活跃数、增长率
  - [ ] `/api/member/distribution` - 会员等级分布
  - [ ] `/api/member/funnel` - 会员活跃漏斗
- [ ] 预订统计API（4个接口）
  - [ ] `/api/booking/statistics` - 今日预订、趋势、预订率
  - [ ] `/api/booking/trend` - 周/月预订趋势
  - [ ] `/api/booking/heatmap` - 预订热力图数据
  - [ ] `/api/booking/venue-trend` - 各场馆预订趋势
- [ ] 财务统计API（3个接口）
  - [ ] `/api/finance/statistics` - 今日/月收入、目标、进度
  - [ ] `/api/finance/trend` - 周/月收入趋势
  - [ ] `/api/finance/venue-trend` - 各场馆收入趋势
- [ ] 场地统计API（2个接口）
  - [ ] `/api/court/statistics` - 场地总数、使用率
  - [ ] `/api/court/realtime-status` - 实时场地状态

#### 辅助统计API（P2-P3）
- [ ] 场馆评估API（1个接口）
  - [ ] `/api/venue/evaluation` - 场馆综合评估雷达图
- [ ] 其他统计API（3个接口）
  - [ ] `/api/activity/recent` - 最近系统活动
  - [ ] `/api/equipment/rental/statistics` - 器材租借统计
  - [ ] `/api/coach/ranking` - 教练业绩排行
- [x] **穿线服务统计API**（`/api/stringing/statistics` 已存在，仅需前端接入）

#### 扩展模块API（P3+）
- [ ] 课程与教练统计API（3个接口）
  - [ ] `/api/course/statistics/hot` - 课程热度排行
  - [ ] `/api/course/statistics/capacity-trend` - 课程满班率趋势
  - [ ] `/api/coach/statistics/schedule` - 教练排课工作量
- [ ] 赛事统计API（3个接口）
  - [ ] `/api/tournament/list/upcoming` - 未来赛事列表
  - [ ] `/api/tournament/statistics/funnel` - 赛事报名漏斗
  - [ ] `/api/tournament/statistics/impact` - 赛事带动效果
- [ ] 会员生命周期API（3个接口）
  - [ ] `/api/member/statistics/churn` - 会员流失分析
  - [ ] `/api/member/expiring` - 会员到期预警
  - [ ] `/api/member/statistics/source` - 会员来源分布
- [ ] 收入结构API（2个接口）
  - [ ] `/api/finance/statistics/structure` - 收入结构分布
  - [ ] `/api/finance/venue-structure` - 场馆收入结构堆叠
- [ ] 预警与待办API（3个接口）
  - [ ] `/api/equipment/inventory/alerts` - 库存预警列表
  - [ ] `/api/court/statistics/utilization-anomaly` - 场地利用异常
  - [ ] `/api/dashboard/todo-summary` - 运营待办总览
  - [ ] `/api/court/timeline/today` - 今日场地时间轴

**统计：** 共需开发约 30+ 个统计API接口（1个已存在）
- [ ] 场地统计API（2个接口）
- [ ] 场馆评估API（1个接口）
- [ ] 其他统计API（3个接口：activity、equipment/rental、coach/ranking）
- [x] **穿线服务统计API**（`GET /api/stringing/statistics` 已存在，仅需前端接入）

---

## 附录A：UniApp Dashboard 完整代码结构

### 数据获取流程
```typescript
// 1. 定义响应式数据
const stats = ref({
  todayBookings: 0,
  todayNewMembers: 0,
  todayRevenue: 0,
  memberTotal: 0,
  courtTotal: 0,
  bookingGrowth: 0,
  revenueGrowth: 0
})

// 2. 并发请求所有API
async function fetchStats() {
  loading.value = true
  const today = new Date().toISOString().split('T')[0]
  try {
    const [memberRes, bookingRes, financeRes, courtRes] = await Promise.all([
      getMemberStatistics().catch(() => ({})),
      getBookingStatistics().catch(() => ({})),
      getFinanceStatistics({ startDate: today, endDate: today }).catch(() => ({})),
      getCourtStatistics().catch(() => ({}))
    ])
    
    // 3. 数据处理和赋值
    stats.value.todayNewMembers = numOrZero(memberRes?.newToday)
    stats.value.memberTotal = numOrZero(memberRes?.total)
    stats.value.todayBookings = bookingRes?.todayBookings != null 
      ? numOrZero(bookingRes.todayBookings) 
      : numOrZero(bookingRes?.ongoing) + numOrZero(bookingRes?.finished)
    stats.value.todayRevenue = financeRes?.totalIncome != null 
      ? numOrZero(financeRes.totalIncome) : 0
    stats.value.courtTotal = numOrZero(courtRes?.total)
  } catch (e) {
    console.error('Dashboard fetch error', e)
  } finally {
    loading.value = false
  }
}

// 4. 页面加载和刷新
onMounted(() => {
  fetchStats()
})

onPullDownRefresh(() => {
  fetchStats().finally(() => uni.stopPullDownRefresh())
})
```

### 数据安全处理
```typescript
// 数值安全转换函数
function numOrZero(v: unknown): number {
  if (v === null || v === undefined) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

// 金额格式化函数
function formatNum(amount: unknown): string {
  const n = numOrZero(amount)
  return n.toLocaleString('zh-CN', { 
    minimumFractionDigits: 0, 
    maximumFractionDigits: 0 
  })
}
```

---

## 附录B：后端API响应格式

### 会员统计响应示例

```json
{
  "code": 200,
  "data": {
    "totalMembers": 128,
    "activeMembers": 85,
    "newToday": 3,
    "newThisWeek": 12,
    "newThisMonth": 45,
    "growthRate": 15.4,
    "trends": {
      "categories": ["周一", "周二", "周三", "周四", "周五", "周六", "周日"],
      "totalMembers": [98, 105, 112, 118, 126, 138, 150],
      "newMembers": [5, 7, 7, 6, 8, 12, 12]
    },
    "distribution": [
      { "name": "VIP会员", "value": 45, "percentage": 35.2 },
      { "name": "金卡会员", "value": 38, "percentage": 29.7 },
      { "name": "银卡会员", "value": 25, "percentage": 19.5 },
      { "name": "普通会员", "value": 15, "percentage": 11.7 },
      { "name": "新会员", "value": 5, "percentage": 3.9 }
    ],
    "funnel": [
      { "name": "注册会员", "value": 128, "rate": 100 },
      { "name": "活跃会员", "value": 85, "rate": 66.4 },
      { "name": "高频会员", "value": 42, "rate": 32.8 },
      { "name": "VIP会员", "value": 18, "rate": 14.1 }
    ]
  }
}
```

### 预订统计响应示例

```json
{
  "code": 200,
  "data": {
    "todayBookings": 24,
    "bookingTrend": 8,
    "bookingRate": 75,
    "peakHours": "18:00-20:00",
    "trends": {
      "week": {
        "categories": ["周一", "周二", "周三", "周四", "周五", "周六", "周日"],
        "values": [24, 32, 28, 35, 42, 58, 45]
      },
      "month": {
        "categories": ["第1周", "第2周", "第3周", "第4周"],
        "values": [185, 223, 198, 256]
      }
    },
    "heatmap": [
      { "hour": 0, "day": 0, "count": 5 },
      { "hour": 1, "day": 0, "count": 8 }
      // ...更多数据
    ]
  }
}
```

---

## 版本历史

| 版本 | 日期 | 内容 |
|------|------|------|
| v2.0 | 2026-02-23 | 基于UniApp移动端实际代码重写文档；确认Dashboard已对接4个核心统计API；更新文档结构以区分UniApp和Web端 |
| v1.11 | 2026-02-19 | 二次检查：修复 KPIBoard 场地使用数；补全 getCourtSchedule API；补全 GET /api/booking/operation-todo |
| v1.10 | 2026-02-19 | 补全与修复：新增接口、场馆统计、场地利用异常、会员流失图、课程相关 |
| v1.9 | 2026-02-19 | 二次检查：修复 KPIBoard 场地使用数（后端无 statusCounts，改为使用 inUse） |1.8 | 2026-02-19 | 完成剩余 3 个组件真实数据对接：新增 /api/booking/heatmap、/api/member/funnel；PeakHoursHeatmap、MemberFunnelChart、CourtUsageRingChart 均已接真实 API |
| v1.7 | 2026-02-19 | 全项目检查：Dashboard 主页面及 28/31 图表已对接真实 API；仅 3 个组件仍用虚拟数据（PeakHoursHeatmap、MemberFunnelChart、CourtUsageRingChart） |
| v1.5 | 2026-02-13 | 更新行号至当前代码实际位置；新增图表组件总览表（31个）；补充快捷入口区域说明；细化统计概览分类 |
| v1.4 | 2026-02-12 | 根据最新 Dashboard 结构补充课程/赛事/会员/预警等新增可视化模块的虚拟数据与API对接清单 |
| v1.3 | 2026-02-09 | 根据项目实际开发进度更新文档版本与日期 |
| v1.2 | 2026-01-25 | 更新状态为"暂缓"，优先级调整为P2，等待UniApp完成后处理 |
| v1.1 | 2026-01-25 | 补充穿线服务统计API已存在的说明 |
| v1.0 | 2026-01-24 | 初始版本，完成虚拟数据对接清单 |

---

**文档维护者：** BMP开发团队
**最后更新：** 2026-02-19
