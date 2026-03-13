<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon anomaly-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 19h18" />
            <path d="M6 16l3-7 4 5 3-8 2 5" />
            <circle cx="19" cy="7" r="2" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">场地利用异常监控</h3>
          <span class="chart-subtitle">过高或过低利用率的场地</span>
        </div>
      </div>
    </div>
    <div class="chart-body" v-loading="loading">
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getCourtList, getTodayBookingCounts } from '@/api/court'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)

const getCSSVar = (varName, defaultValue) => {
  if (typeof window === 'undefined') return defaultValue
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || defaultValue
}

const themeColors = computed(() => ({
  primary: getCSSVar('--color-primary', '#3B82F6'),
  success: getCSSVar('--color-success', '#10B981'),
  danger: getCSSVar('--color-danger', '#EF4444'),
  warning: getCSSVar('--color-warning', '#F59E0B'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const courtData = ref([])
let cachedCourts = null

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

// 拉取所有场地（分页直到取完，使用缓存）
const fetchAllCourts = async () => {
  if (cachedCourts) return cachedCourts

  const pageSize = 100
  let page = 1
  let list = []
  let hasMore = true
  while (hasMore) {
    const listRes = await getCourtList({ page, size: pageSize, status: null })
    let chunk = []
    if (listRes.code === 200 && listRes.data) {
      const raw = listRes.data
      if (Array.isArray(raw)) chunk = raw
      else if (raw.records && Array.isArray(raw.records)) chunk = raw.records
      else if (raw.data && Array.isArray(raw.data)) chunk = raw.data
      else if (raw.list && Array.isArray(raw.list)) chunk = raw.list
    }
    list = list.concat(chunk)
    hasMore = chunk.length >= pageSize
    page += 1
  }
  cachedCourts = list
  return list
}

// 使用场地列表 + 今日预约数计算利用率（与 CourtUsageRingChart 一致）
const fetchCourtUtilization = async () => {
  try {
    const list = await fetchAllCourts()
    if (list.length === 0) {
      courtData.value = []
      return
    }
    const courtIds = list.map(c => c.id)
    const today = new Date().toISOString().split('T')[0]
    let countMap = {}
    try {
      const countRes = await getTodayBookingCounts(courtIds, today)
      if (countRes.code === 200 && countRes.data && typeof countRes.data === 'object') {
        countMap = countRes.data
      }
    } catch (e) {
      console.warn('获取今日预约数失败:', e)
    }
    courtData.value = list.map(court => {
      const count = countMap[court.id] != null ? Number(countMap[court.id]) : 0
      const usage = Math.min(100, Math.round(count * 10))
      return {
        name: court.courtName || court.courtCode || `场地${court.id}`,
        utilization: usage / 100
      }
    })
  } catch (e) {
    console.error('获取场地利用率数据失败:', e)
    courtData.value = []
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255,255,255,0.98)',
      borderColor: colors.primary,
      borderWidth: 1,
      borderRadius: 10,
      padding: [10, 14],
      textStyle: {
        color: colors.textPrimary,
        fontSize: 12
      },
      formatter: params => {
        const data = params[0]
        const value = courtData.value[data.dataIndex]
        const percent = (value.utilization * 100).toFixed(1)
        const label =
          value.utilization < 0.3 ? '利用偏低' :
          value.utilization > 0.85 ? '利用过高' :
          '正常'
        return `
          <div style="font-weight:600;margin-bottom:6px;color:${colors.textPrimary};">场地 ${value.name}</div>
          <div style="display:flex;justify-content:space-between;gap:12px;font-size:12px;color:${colors.textSecondary};">
            <span>利用率</span>
            <span style="font-weight:600;color:${colors.textPrimary};">${percent}%</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>状态</span>
            <span style="font-weight:600;color:${
              value.utilization < 0.3 ? colors.warning :
              value.utilization > 0.85 ? colors.danger :
              colors.success
            };">${label}</span>
          </div>
        `
      }
    },
    grid: {
      left: '4%',
      right: '4%',
      bottom: '6%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: courtData.value.map(c => c.name),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: colors.textSecondary,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 1,
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: {
        lineStyle: {
          color: colors.border,
          type: 'dashed'
        }
      },
      axisLabel: {
        color: colors.textSecondary,
        fontSize: 11,
        formatter: val => `${Math.round(val * 100)}%`
      }
    },
    series: [
      {
        name: '利用率',
        type: 'bar',
        data: courtData.value.map(c => c.utilization),
        barWidth: 18,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: params => {
            const v = courtData.value[params.dataIndex].utilization
            if (v < 0.3) return colors.warning
            if (v > 0.85) return colors.danger
            return colors.success
          }
        }
      }
    ]
  }
}

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption(getChartOption())
  }
}

const resizeChart = () => {
  chartInstance?.resize()
}

const updateChart = () => {
  if (chartInstance) {
    chartInstance.setOption(getChartOption())
  }
}

onMounted(() => {
  initChart()
  fetchCourtUtilization().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 20px 20px 22px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.08);
  border-color: var(--color-primary-light, #93C5FD);
  transform: translateY(-2px);
}

.chart-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.chart-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.chart-icon svg {
  width: 20px;
  height: 20px;
}

.anomaly-icon {
  background: linear-gradient(135deg, var(--color-warning) 0%, var(--color-danger) 100%);
  box-shadow: 0 4px 10px color-mix(in srgb, var(--color-danger) 30%, transparent);
}

.chart-title-content {
  display: flex;
  flex-direction: column;
}

.chart-title {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.chart-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
  margin-top: 2px;
}

.chart-body {
  padding-top: 4px;
}

.chart-container {
  width: 100%;
  height: 210px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 190px;
  }
}
</style>

