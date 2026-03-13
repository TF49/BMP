<template>
  <div class="glass-card chart-card court-usage-card">
    <div class="chart-header">
      <div class="header-left">
        <span class="chart-title">🏟️ 场地使用率</span>
        <span class="chart-subtitle">各场地实时使用率对比</span>
      </div>
      <el-link type="primary" :underline="false" class="view-all" @click="navigateTo('/court/management')">
        管理场地
      </el-link>
    </div>
    <div class="court-rings" v-loading="loading">
      <div class="ring-item" v-for="court in filteredCourtData" :key="court.id">
        <div class="ring-chart" :ref="el => setChartRef(el, court.id)"></div>
        <div class="ring-info">
          <span class="court-name">{{ court.name }}</span>
          <div class="usage-info">
            <el-tag v-if="court.status === 0" type="warning" size="small" class="maintenance-tag">维护中</el-tag>
            <span class="usage-text" :class="getUsageClass(court.usage)">{{ court.usage }}%</span>
          </div>
        </div>
      </div>
    </div>
    <div class="usage-summary">
      <div class="summary-item" :class="{ active: isFilterActive(1) }" @click="handleFilterClick(1)">
        <div class="summary-icon available">
          <el-icon><Check /></el-icon>
        </div>
        <div class="summary-content">
          <span class="summary-value">{{ summaryData.available }}</span>
          <span class="summary-label">空闲场地</span>
        </div>
      </div>
      <div class="summary-item" :class="{ active: isFilterActive([2, 3]) }" @click="handleFilterClick([2, 3])">
        <div class="summary-icon occupied">
          <el-icon><Close /></el-icon>
        </div>
        <div class="summary-content">
          <span class="summary-value">{{ summaryData.occupied }}</span>
          <span class="summary-label">使用中</span>
        </div>
      </div>
      <div class="summary-item" :class="{ active: isFilterActive(0) }" @click="handleFilterClick(0)">
        <div class="summary-icon maintenance">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="summary-content">
          <span class="summary-value">{{ summaryData.maintenance }}</span>
          <span class="summary-label">维护中</span>
        </div>
      </div>
      <div class="summary-item" @click="handleFilterClick(null)">
        <div class="summary-icon total">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="summary-content">
          <span class="summary-value">{{ summaryData.avgUsage }}%</span>
          <span class="summary-label">平均使用率</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Check, Close, Grid, Warning } from '@element-plus/icons-vue'
import { getCourtList, getTodayBookingCounts, getCourtStatistics } from '@/api/court'

const router = useRouter()
const loading = ref(false)
const courtData = ref([])
const bookingCounts = ref({})
const chartRefs = ref({})
const chartInstances = ref({})
const filterStatus = ref(null) // null=全部, 0=维护中, 1=空闲, [2,3]=使用中

const summaryData = ref({
  available: 0,
  occupied: 0,
  maintenance: 0,
  avgUsage: 0
})

const navigateTo = (path) => {
  router.push(path)
}

const getLocalTodayDateStr = () => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

// 筛选后的场地数据
const filteredCourtData = computed(() => {
  if (filterStatus.value === null) return courtData.value
  if (Array.isArray(filterStatus.value)) {
    return courtData.value.filter(c => {
      const cnt = bookingCounts.value?.[c.id] != null ? Number(bookingCounts.value[c.id]) : 0
      return cnt > 0 && c.status !== 0
    })
  }
  return courtData.value.filter(c => c.status === filterStatus.value)
})

// 处理统计按钮点击
const handleFilterClick = (status) => {
  if (status === null) {
    // 平均使用率按钮不执行操作
    return
  }
  // 如果点击的是当前激活的按钮，则取消筛选
  if (isFilterActive(status)) {
    filterStatus.value = null
  } else {
    filterStatus.value = status
  }
}

// 检查按钮是否激活
const isFilterActive = (status) => {
  if (status === null) return false
  if (Array.isArray(status)) {
    if (!Array.isArray(filterStatus.value)) return false
    return status.length === filterStatus.value.length &&
           status.every(s => filterStatus.value.includes(s)) &&
           filterStatus.value.every(s => status.includes(s))
  }
  return filterStatus.value === status
}

// 设置图表引用
const setChartRef = (el, id) => {
  if (el) {
    chartRefs.value[id] = el
  }
}

// 获取使用率样式类
const getUsageClass = (usage) => {
  if (usage >= 80) return 'high'
  if (usage >= 50) return 'medium'
  return 'low'
}

// 获取使用率颜色
const getUsageColor = (usage) => {
  if (usage >= 80) {
    return {
      start: '#EF4444',
      end: '#DC2626'
    }
  }
  if (usage >= 50) {
    return {
      start: '#F59E0B',
      end: '#D97706'
    }
  }
  return {
    start: '#10B981',
    end: '#059669'
  }
}

// 获取环形图配置
const getRingOption = (usage) => {
  const colors = getUsageColor(usage)
  return {
    series: [
      {
        type: 'pie',
        radius: ['75%', '90%'],
        center: ['50%', '50%'],
        startAngle: 90,
        data: [
          {
            value: usage,
            name: '使用率',
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: colors.start },
                { offset: 1, color: colors.end }
              ]),
              borderRadius: 10
            }
          },
          {
            value: 100 - usage,
            name: '空闲',
            itemStyle: {
              color: '#F1F5F9'
            }
          }
        ],
        label: {
          show: false
        },
        labelLine: {
          show: false
        },
        emphasis: {
          scale: false
        },
        animation: true,
        animationType: 'scale',
        animationEasing: 'elasticOut'
      }
    ]
  }
}

// 初始化单个环形图
const initRingChart = (id, usage) => {
  nextTick(() => {
    const el = chartRefs.value[id]
    if (el) {
      if (chartInstances.value[id]) {
        chartInstances.value[id].dispose()
      }
      chartInstances.value[id] = echarts.init(el)
      chartInstances.value[id].setOption(getRingOption(usage))
    }
  })
}

// 获取场地数据：列表用 list API（多拉一些以支持筛选展示），统计用 statistics API 与场地管理一致
const fetchCourtData = async () => {
  loading.value = true
  try {
    const [listRes, statsRes] = await Promise.all([
      getCourtList({ page: 1, size: 100, status: null }),
      getCourtStatistics()
    ])

    let list = []
    if (listRes.code === 200 && listRes.data) {
      if (Array.isArray(listRes.data)) list = listRes.data
      else if (listRes.data.data && Array.isArray(listRes.data.data)) list = listRes.data.data
      else if (listRes.data.records && Array.isArray(listRes.data.records)) list = listRes.data.records
      else if (listRes.data.list && Array.isArray(listRes.data.list)) list = listRes.data.list
    }
    const courts = list
    if (courts.length === 0) {
      courtData.value = []
      summaryData.value = { available: 0, occupied: 0, maintenance: 0, avgUsage: 0 }
      await nextTick()
      return
    }

    const courtIds = courts.map(c => c.id)
    const today = getLocalTodayDateStr()
    let countMap = {}
    try {
      const countRes = await getTodayBookingCounts(courtIds, today)
      if (countRes.code === 200 && countRes.data && typeof countRes.data === 'object') {
        countMap = countRes.data
      }
    } catch (e) {
      console.warn('获取今日预约数失败:', e)
    }

    bookingCounts.value = countMap

    courtData.value = courts.map(court => {
      const count = countMap[court.id] != null ? Number(countMap[court.id]) : 0
      const usage = Math.min(100, Math.round(count * 10))
      return {
        id: court.id,
        name: court.courtName || court.courtCode || `场地${court.id}`,
        usage,
        status: court.status
      }
    })

    const stats = (statsRes.code === 200 && statsRes.data) ? statsRes.data : {}
    const available = Number(stats.available) || 0
    const maintenance = Number(stats.maintenance) || 0
    const occupiedCourtsByBookings = courtIds.reduce((acc, id) => {
      const c = countMap[id] != null ? Number(countMap[id]) : 0
      return acc + (c > 0 ? 1 : 0)
    }, 0)
    const totalUsage = courtData.value.reduce((sum, c) => sum + c.usage, 0)
    summaryData.value = {
      available,
      occupied: occupiedCourtsByBookings,
      maintenance,
      avgUsage: courtData.value.length > 0 ? Math.round(totalUsage / courtData.value.length) : 0
    }

    await nextTick()
    filteredCourtData.value.forEach(court => {
      initRingChart(court.id, court.usage)
    })
  } catch (error) {
    console.error('获取场地数据失败:', error)
    courtData.value = []
    bookingCounts.value = {}
    summaryData.value = { available: 0, occupied: 0, maintenance: 0, avgUsage: 0 }
  } finally {
    loading.value = false
  }
}

// 窗口大小调整
const handleResize = () => {
  Object.values(chartInstances.value).forEach(instance => {
    instance?.resize()
  })
}

// 监听筛选变化，重新初始化图表
const updateCharts = () => {
  nextTick(() => {
    filteredCourtData.value.forEach(court => {
      initRingChart(court.id, court.usage)
    })
    // 清理不再显示的图表
    Object.keys(chartInstances.value).forEach(id => {
      if (!filteredCourtData.value.find(c => c.id.toString() === id)) {
        chartInstances.value[id]?.dispose()
        delete chartInstances.value[id]
      }
    })
  })
}

// 监听筛选数据变化，自动更新图表
watch(filteredCourtData, () => {
  updateCharts()
}, { deep: true })

onMounted(() => {
  fetchCourtData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  Object.values(chartInstances.value).forEach(instance => {
    instance?.dispose()
  })
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.court-usage-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-title {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
}

.chart-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #94A3B8;
}

.court-rings {
  display: flex;
  flex-wrap: nowrap;
  gap: 16px;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 8px;
  scrollbar-width: thin;
}

.court-rings::-webkit-scrollbar {
  height: 6px;
}

.court-rings::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.court-rings::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.ring-item {
  flex: 0 0 auto;
  min-width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.ring-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: rgba(99, 102, 241, 0.3);
}

.ring-chart {
  width: 80px;
  height: 80px;
}

.ring-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
}

.usage-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.maintenance-tag {
  margin-bottom: 2px;
}

.court-name {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.usage-text {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 700;
}

.usage-text.high {
  color: #EF4444;
}

.usage-text.medium {
  color: #F59E0B;
}

.usage-text.low {
  color: #10B981;
}

.usage-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #EEF2FF 0%, #F8FAFC 100%);
  border-radius: 14px;
  border: 1px solid #E2E8F0;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.summary-item:hover {
  background: rgba(99, 102, 241, 0.08);
  transform: translateY(-2px);
}

.summary-item.active {
  background: rgba(99, 102, 241, 0.12);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}

.summary-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.summary-icon.available {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
}

.summary-icon.occupied {
  background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
}

.summary-icon.maintenance {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
}

.summary-icon.total {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.summary-value {
  font-family: 'Poppins', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: #1E293B;
}

.summary-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #64748B;
}

@media (max-width: 768px) {
  .ring-item {
    min-width: 100px;
  }

  .usage-summary {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 14px;
  }

  .ring-chart {
    width: 70px;
    height: 70px;
  }
}

@media (max-width: 480px) {
  .ring-item {
    min-width: 90px;
    padding: 12px;
  }

  .ring-chart {
    width: 60px;
    height: 60px;
  }

  .usage-text {
    font-size: 16px;
  }
}
</style>
