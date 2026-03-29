<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon coach-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="7" r="4" />
            <path d="M5 21v-2a5 5 0 0 1 5-5h4a5 5 0 0 1 5 5v2" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">教练排课与授课量</h3>
          <span class="chart-subtitle">近期各教练课程负载对比</span>
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
import { getCoachStatistics } from '@/api/coach'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)

const getCSSVar = (varName, defaultValue) => {
  if (typeof window === 'undefined') return defaultValue
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || defaultValue
}

const themeColors = computed(() => ({
  primary: getCSSVar('--color-primary', '#3B82F6'),
  info: getCSSVar('--color-info', '#06B6D4'),
  success: getCSSVar('--color-success', '#10B981'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const coachData = ref([])

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchCoachWorkload = async () => {
  loading.value = true
  try {
    const res = await getCoachStatistics()
    if (res.code === 200 && Array.isArray(res.data?.coachWorkload || res.data)) {
      const workload = res.data?.coachWorkload || res.data
      if (workload.length > 0) {
        coachData.value = workload.map(item => ({
          name: item.coachName || item.name || '未知教练',
          scheduled: parseNum(item.scheduledCourses || item.scheduled),
          completed: parseNum(item.completedCourses || item.completed)
        }))
      } else {
        coachData.value = [
          { name: '教练1', scheduled: 0, completed: 0 },
          { name: '教练2', scheduled: 0, completed: 0 },
          { name: '教练3', scheduled: 0, completed: 0 },
          { name: '教练4', scheduled: 0, completed: 0 },
          { name: '教练5', scheduled: 0, completed: 0 }
        ]
      }
    } else {
      coachData.value = [
        { name: '教练1', scheduled: 0, completed: 0 },
        { name: '教练2', scheduled: 0, completed: 0 },
        { name: '教练3', scheduled: 0, completed: 0 },
        { name: '教练4', scheduled: 0, completed: 0 },
        { name: '教练5', scheduled: 0, completed: 0 }
      ]
    }
  } catch (e) {
    console.error('获取教练工作量数据失败:', e)
    coachData.value = [
      { name: '教练1', scheduled: 0, completed: 0 },
      { name: '教练2', scheduled: 0, completed: 0 },
      { name: '教练3', scheduled: 0, completed: 0 },
      { name: '教练4', scheduled: 0, completed: 0 },
      { name: '教练5', scheduled: 0, completed: 0 }
    ]
  } finally {
    loading.value = false
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
        const scheduled = params.find(p => p.seriesName === '排课节数')
        const completed = params.find(p => p.seriesName === '已上课节数')
        const rate = completed && scheduled ? (completed.value / scheduled.value) * 100 : 0
        return `
          <div style="margin-bottom:6px;font-weight:600;color:${colors.textPrimary};">${scheduled.axisValue}</div>
          <div style="font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>排课节数</span>
            <span style="font-weight:600;color:${colors.textPrimary};">${scheduled.value} 节</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>已上课节数</span>
            <span style="font-weight:600;color:${colors.success};">${completed.value} 节</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>执行率</span>
            <span style="font-weight:600;color:${colors.info};">${rate.toFixed(1)}%</span>
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
      bottom: '18%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: coachData.value.map(c => c.name),
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
        fontSize: 11
      }
    },
    series: [
      {
        name: '排课节数',
        type: 'bar',
        data: coachData.value.map(c => c.scheduled),
        barWidth: 14,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: colors.primary
        }
      },
      {
        name: '已上课节数',
        type: 'bar',
        data: coachData.value.map(c => c.completed),
        barWidth: 14,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
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
  fetchCoachWorkload().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

useDashboardChartRefresh(() => fetchCoachWorkload().then(() => updateChart()))

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

.coach-icon {
  background: linear-gradient(135deg, var(--color-info) 0%, var(--color-primary) 100%);
  box-shadow: 0 4px 10px color-mix(in srgb, var(--color-info) 30%, transparent);
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

