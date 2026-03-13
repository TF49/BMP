<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon venue-revenue-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="10" width="4" height="10" rx="1" />
            <rect x="10" y="6" width="4" height="14" rx="1" />
            <rect x="17" y="3" width="4" height="17" rx="1" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">场馆收入结构对比</h3>
          <span class="chart-subtitle">不同场馆在各业务线的收入占比</span>
        </div>
      </div>
    </div>
    <div class="chart-body">
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getVenueStatistics } from '@/api/venue'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)

const getCSSVar = (varName, defaultValue) => {
  if (typeof window === 'undefined') return defaultValue
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || defaultValue
}

const themeColors = computed(() => ({
  primary: getCSSVar('--color-primary', '#3B82F6'),
  secondary: getCSSVar('--color-secondary', '#8B5CF6'),
  info: getCSSVar('--color-info', '#06B6D4'),
  success: getCSSVar('--color-success', '#10B981'),
  warning: getCSSVar('--color-warning', '#F59E0B'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const venues = ref([])

const stackedData = ref({
  court: [],
  course: [],
  tournament: [],
  other: []
})

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchVenueRevenueStructure = async () => {
  loading.value = true
  try {
    const res = await getVenueStatistics()
    const revenueList = (res.code === 200 && Array.isArray(res.data?.venueRevenue || res.data))
      ? (res.data?.venueRevenue || res.data)
      : []

    venues.value = revenueList.map(v => v.venueName || v.name || '未知场馆')

    stackedData.value = {
      court: revenueList.map(v => parseNum(v.courtRevenue || v.court)),
      course: revenueList.map(v => parseNum(v.courseRevenue || v.course)),
      tournament: revenueList.map(v => parseNum(v.tournamentRevenue || v.tournament)),
      other: revenueList.map(v => parseNum(v.otherRevenue || v.other))
    }
  } catch (e) {
    console.error('获取场馆收入结构数据失败:', e)
    venues.value = []
    stackedData.value = { court: [], course: [], tournament: [], other: [] }
  } finally {
    loading.value = false
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  const venueList = venues.value.length > 0 ? venues.value : ['暂无']
  const courtData = venues.value.length > 0 ? stackedData.value.court : [0]
  const courseData = venues.value.length > 0 ? stackedData.value.course : [0]
  const tournamentData = venues.value.length > 0 ? stackedData.value.tournament : [0]
  const otherData = venues.value.length > 0 ? stackedData.value.other : [0]
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
        const total = params.reduce((sum, p) => sum + p.value, 0)
        const name = params[0].axisValue
        const rows = params.map(p => {
          const percent = total ? ((p.value / total) * 100).toFixed(1) : 0
          return `
            <div style="display:flex;justify-content:space-between;gap:12px;margin-top:2px;">
              <span style="color:${colors.textSecondary};font-size:12px;">${p.seriesName}</span>
              <span style="font-weight:600;color:${colors.textPrimary};font-size:12px;">¥${p.value.toLocaleString()} (${percent}%)</span>
            </div>
          `
        }).join('')
        return `
          <div style="font-weight:600;margin-bottom:6px;color:${colors.textPrimary};">${name}</div>
          <div>${rows}</div>
          <div style="margin-top:6px;border-top:1px solid ${colors.border};padding-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>总收入</span>
            <span style="font-weight:600;color:${colors.primary};">¥${total.toLocaleString()}</span>
          </div>
        `
      }
    },
    legend: {
      bottom: 0,
      left: 'center',
      textStyle: {
        color: colors.textSecondary,
        fontSize: 11
      }
    },
    grid: {
      left: '4%',
      right: '4%',
      bottom: '20%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: venueList,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: colors.textSecondary,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
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
        formatter: val => `¥${val / 1000}k`
      }
    },
    series: [
      {
        name: '场地费',
        type: 'bar',
        stack: 'total',
        data: courtData,
        barWidth: 22,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: colors.primary
        }
      },
      {
        name: '课程费',
        type: 'bar',
        stack: 'total',
        data: courseData,
        barWidth: 22,
        itemStyle: {
          color: colors.secondary
        }
      },
      {
        name: '赛事费',
        type: 'bar',
        stack: 'total',
        data: tournamentData,
        barWidth: 22,
        itemStyle: {
          color: colors.info
        }
      },
      {
        name: '其他收入',
        type: 'bar',
        stack: 'total',
        data: otherData,
        barWidth: 22,
        itemStyle: {
          color: colors.success
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
  fetchVenueRevenueStructure().then(() => updateChart())
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

.venue-revenue-icon {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-info) 100%);
  box-shadow: 0 4px 10px color-mix(in srgb, var(--color-primary) 30%, transparent);
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
  height: 230px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 210px;
  }
}
</style>

