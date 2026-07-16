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
import * as echarts from '@/utils/echarts'
import { getCourtDailyUtilization } from '@/api/court'
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
  success: getCSSVar('--color-success', '#10B981'),
  danger: getCSSVar('--color-danger', '#EF4444'),
  warning: getCSSVar('--color-warning', '#F59E0B'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const courtData = ref([])

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const getLocalTodayDateStr = () => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const fetchCourtUtilization = async () => {
  try {
    const res = await getCourtDailyUtilization(getLocalTodayDateStr())
    const list = res.code === 200 && Array.isArray(res.data) ? res.data : []
    if (list.length === 0) {
      courtData.value = []
      return
    }
    courtData.value = list.map(court => {
      const usage = Number(court.utilizationRate || 0)
      return {
        name: court.courtName || `场地${court.courtId}`,
        utilization: usage,
        status: court.status || 'normal'
      }
    })
  } catch (e) {
    console.error('获取场地利用率数据失败:', e)
    courtData.value = []
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  const getStatusMeta = value => {
    if (value.status === 'maintenance') {
      return {
        label: '维护中',
        color: colors.textSecondary
      }
    }
    if (value.utilization < 0.3) {
      return {
        label: '利用偏低',
        color: colors.warning
      }
    }
    if (value.utilization > 0.85) {
      return {
        label: '利用过高',
        color: colors.danger
      }
    }
    return {
      label: '正常',
      color: colors.success
    }
  }
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
        const statusMeta = getStatusMeta(value)
        return `
          <div style="font-weight:600;margin-bottom:6px;color:${colors.textPrimary};">场地 ${value.name}</div>
          <div style="display:flex;justify-content:space-between;gap:12px;font-size:12px;color:${colors.textSecondary};">
            <span>利用率</span>
            <span style="font-weight:600;color:${colors.textPrimary};">${percent}%</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>状态</span>
            <span style="font-weight:600;color:${statusMeta.color};">${statusMeta.label}</span>
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
            const value = courtData.value[params.dataIndex]
            return getStatusMeta(value).color
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

useDashboardChartRefresh(() => fetchCourtUtilization().then(() => updateChart()))

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
