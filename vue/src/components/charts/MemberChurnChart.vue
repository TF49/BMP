<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon churn-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="8" r="4" />
            <path d="M4 21a8 8 0 0 1 16 0" />
            <path d="M3 4h6M5 2l-2 2 2 2" />
            <path d="M21 4h-6M19 2l2 2-2 2" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">会员新增与流失</h3>
          <span class="chart-subtitle">净增长 = 新增 - 流失</span>
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
import { getMemberStatistics } from '@/api/member'
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
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const churnData = ref([])

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchChurnData = async () => {
  loading.value = true
  try {
    const res = await getMemberStatistics({ period: 'month' })
    if (res.code === 200 && Array.isArray(res.data?.monthlyChurn) && res.data.monthlyChurn.length > 0) {
      churnData.value = res.data.monthlyChurn.map(item => ({
        month: item.month || item.label || '',
        newMembers: parseNum(item.newMembers || item.new),
        churnMembers: parseNum(item.churnMembers || item.churn)
      }))
    } else {
      churnData.value = []
    }
  } catch (e) {
    console.error('获取会员流失数据失败:', e)
    churnData.value = []
  } finally {
    loading.value = false
  }
}

const defaultMonths = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']

const getChartOption = () => {
  const colors = themeColors.value
  const list = churnData.value.length > 0 ? churnData.value : defaultMonths.map(m => ({ month: m, newMembers: 0, churnMembers: 0 }))
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
        const add = params.find(p => p.seriesName === '新增')
        const loss = params.find(p => p.seriesName === '流失')
        const net = (add?.value || 0) - (loss?.value || 0)
        return `
          <div style="margin-bottom:6px;font-weight:600;color:${colors.textPrimary};">${add.axisValue}</div>
          <div style="font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>新增会员</span>
            <span style="font-weight:600;color:${colors.success};">${add.value} 人</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>流失会员</span>
            <span style="font-weight:600;color:${colors.danger};">${loss.value} 人</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>净增长</span>
            <span style="font-weight:600;color:${net >= 0 ? colors.success : colors.danger};">${net} 人</span>
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
      data: list.map(i => i.month),
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
        name: '新增',
        type: 'bar',
        data: list.map(i => i.newMembers),
        barWidth: 16,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: colors.success
        }
      },
      {
        name: '流失',
        type: 'bar',
        data: list.map(i => i.churnMembers),
        barWidth: 16,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: colors.danger
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
  fetchChurnData().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

useDashboardChartRefresh(() => fetchChurnData().then(() => updateChart()))

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

.churn-icon {
  background: linear-gradient(135deg, var(--color-danger) 0%, var(--color-primary) 100%);
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

