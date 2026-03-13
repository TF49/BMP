<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon revenue-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 20V10M18 20V4M6 20v-4"/>
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">收入趋势</h3>
          <span class="chart-subtitle">近7天营业收入分析</span>
        </div>
      </div>
      <div class="chart-actions">
        <button
          v-for="period in periods"
          :key="period.value"
          :class="['period-btn', { active: activePeriod === period.value }]"
          @click="activePeriod = period.value"
        >
          {{ period.label }}
        </button>
      </div>
    </div>
    <div class="chart-body">
      <div ref="chartRef" class="chart-container"></div>
    </div>
    <div class="chart-footer">
      <div class="chart-stat">
        <span class="stat-label">总收入</span>
        <span class="stat-value">¥{{ (totalRevenue ?? 0).toLocaleString() }}</span>
      </div>
      <div class="chart-stat">
        <span class="stat-label">日均</span>
        <span class="stat-value">¥{{ (avgRevenue ?? 0).toLocaleString() }}</span>
      </div>
      <div class="chart-stat" :class="(trendChange ?? 0) >= 0 ? 'trend-up' : 'trend-down'">
        <span class="stat-label">环比</span>
        <span class="stat-value">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="trend-icon">
            <path d="M7 17l5-5 5 5M7 7l5 5 5-5"/>
          </svg>
          {{ (trendChange ?? 0) >= 0 ? '+' : '' }}{{ (trendChange ?? 0).toFixed(1) }}%
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'
import { getFinanceTrend } from '@/api/finance'

const chartRef = ref(null)
let chartInstance = null

// 获取CSS变量值的辅助函数
const getCSSVar = (varName, defaultValue) => {
  if (typeof window === 'undefined') return defaultValue
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || defaultValue
}

// 获取主题颜色
const themeColors = computed(() => ({
  primary: getCSSVar('--color-primary', '#3B82F6'),
  primaryLight: getCSSVar('--color-primary-light', '#60A5FA'),
  info: getCSSVar('--color-info', '#06B6D4'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  textMuted: getCSSVar('--color-text-muted', '#94A3B8'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const activePeriod = ref('week')
const periods = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' }
]

const weekData = ref({ categories: [], values: [] })
const monthData = ref({ categories: [], values: [] })

const currentData = computed(() => activePeriod.value === 'week' ? weekData.value : monthData.value)
const totalRevenue = computed(() => (currentData.value.values || []).reduce((a, b) => a + (Number(b) || 0), 0))
const avgRevenue = computed(() => {
  const len = (currentData.value.values || []).length
  return len > 0 ? Math.round(totalRevenue.value / len) : 0
})
const trendChange = computed(() => {
  const values = currentData.value.values || []
  if (values.length < 2) return 0
  const lastValue = Number(values[values.length - 1]) || 0
  const prevValue = Number(values[values.length - 2]) || 0
  return prevValue > 0 ? ((lastValue - prevValue) / prevValue) * 100 : 0
})

const toDateStr = (d) => d.toISOString().slice(0, 10)
const weekdayLabel = (dateStr) => {
  const labels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const d = new Date(dateStr + 'T00:00:00')
  return labels[d.getDay()]
}

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchWeek = async () => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 6)
  const res = await getFinanceTrend({ startDate: toDateStr(start), endDate: toDateStr(end) })
  if (res.code !== 200 || !res.data) return
  const dates = Array.isArray(res.data.dates) ? res.data.dates : []
  const incomes = Array.isArray(res.data.incomes) ? res.data.incomes : []
  weekData.value = {
    categories: dates.map(weekdayLabel),
    values: dates.map((_, i) => Math.round(parseNum(incomes[i])))
  }
}

const fetchMonth = async () => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 27)
  const res = await getFinanceTrend({ startDate: toDateStr(start), endDate: toDateStr(end) })
  if (res.code !== 200 || !res.data) return
  const dates = Array.isArray(res.data.dates) ? res.data.dates : []
  const incomes = Array.isArray(res.data.incomes) ? res.data.incomes : []
  const daily = dates.map((_, i) => parseNum(incomes[i]))
  const buckets = [0, 0, 0, 0]
  daily.forEach((val, idx) => {
    const b = Math.min(3, Math.floor(idx / 7))
    buckets[b] += val
  })
  monthData.value = {
    categories: ['第1周', '第2周', '第3周', '第4周'],
    values: buckets.map(v => Math.round(v))
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  return {
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255, 255, 255, 0.98)',
    borderColor: colors.primary,
    borderWidth: 1,
    borderRadius: 12,
    padding: [14, 18],
    textStyle: {
      color: colors.textPrimary,
      fontSize: 13,
      fontFamily: 'Open Sans, sans-serif'
    },
    axisPointer: {
      type: 'shadow',
      shadowStyle: {
        color: colors.primary + '26',
        shadowBlur: 10
      }
    },
    formatter: (params) => {
      const data = params[0]
      const trend = data.dataIndex > 0 
        ? (data.value > currentData.value.values[data.dataIndex - 1] ? '↑' : '↓')
        : ''
      return `
        <div style="font-weight: 600; margin-bottom: 10px; color: ${colors.textPrimary}; font-size: 14px;">${data.name}</div>
        <div style="display: flex; align-items: center; gap: 10px; padding: 8px 0;">
          <span style="width: 12px; height: 12px; border-radius: 50%; background: linear-gradient(135deg, ${colors.primary}, ${colors.info}); box-shadow: 0 2px 4px ${colors.primary}4D;"></span>
          <span style="color: ${colors.textSecondary}; font-size: 13px;">营业收入</span>
          <span style="font-weight: 700; color: ${colors.textPrimary}; margin-left: auto; font-size: 15px;">¥${data.value.toLocaleString()} ${trend}</span>
        </div>
      `
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '10%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: currentData.value.categories,
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    },
    axisLabel: {
      color: colors.textSecondary,
      fontSize: 12,
      fontFamily: 'Open Sans, sans-serif',
      margin: 12
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    },
    splitLine: {
      lineStyle: {
        color: colors.border,
        type: 'dashed'
      }
    },
    axisLabel: {
      color: colors.textMuted,
      fontSize: 11,
      fontFamily: 'Open Sans, sans-serif',
      formatter: (value) => value >= 1000 ? `${value / 1000}k` : value
    }
  },
  series: [{
    type: 'bar',
    name: '营业收入',
    data: currentData.value.values,
    barWidth: '55%',
    barMaxWidth: 45,
    itemStyle: {
      borderRadius: [10, 10, 0, 0],
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: colors.primary },
        { offset: 0.5, color: colors.primaryLight },
        { offset: 1, color: colors.info }
      ]),
      shadowBlur: 8,
      shadowColor: colors.primary + '40',
      shadowOffsetY: 4
    },
    emphasis: {
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors.primary },
          { offset: 0.5, color: colors.primaryLight },
          { offset: 1, color: colors.info }
        ]),
        shadowBlur: 25,
        shadowColor: colors.primary + '80',
        shadowOffsetY: 6
      },
      focus: 'series'
    },
    label: {
      show: true,
      position: 'top',
      color: colors.textSecondary,
      fontSize: 11,
      fontFamily: 'Open Sans, sans-serif',
      formatter: (params) => {
        return '¥' + (params.value >= 1000 ? (params.value / 1000).toFixed(1) + 'k' : params.value)
      },
      offset: [0, -8]
    },
    animationDuration: 1200,
    animationEasing: 'elasticOut',
    animationDelay: (idx) => idx * 80
  }]
  }
}

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption(getChartOption())
  }
}

const updateChart = () => {
  chartInstance?.setOption(getChartOption(), true)
}

const resizeChart = () => {
  chartInstance?.resize()
}

watch(activePeriod, () => {
  updateChart()
})

onMounted(() => {
  initChart()
  Promise.all([fetchWeek(), fetchMonth()])
    .then(() => updateChart())
    .catch((e) => console.error('获取收入趋势失败:', e))
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
  padding: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  animation: chartFadeIn 0.6s ease-out both;
  display: flex;
  flex-direction: column;
}

.chart-card:hover {
  box-shadow: var(--color-shadow-hover);
  border-color: var(--color-primary-light);
  transform: translateY(-2px);
}

@keyframes chartFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title-wrapper {
  display: flex;
  align-items: center;
  gap: 14px;
}

.chart-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.chart-icon svg {
  width: 22px;
  height: 22px;
}

.revenue-icon {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-info) 100%);
  box-shadow: 0 4px 12px color-mix(in srgb, var(--color-primary) 30%, transparent);
}

.chart-title-content {
  display: flex;
  flex-direction: column;
}

.chart-title {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.chart-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: var(--color-text-muted, #94A3B8);
  margin-top: 2px;
}

.chart-actions {
  display: flex;
  gap: 6px;
  background: var(--color-background, #F8FAFC);
  padding: 4px;
  border-radius: 10px;
}

.period-btn {
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary, #64748B);
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.period-btn:hover {
  color: var(--color-primary, #3B82F6);
  background: rgba(59, 130, 246, 0.08);
}

.period-btn.active {
  background: var(--color-card-bg);
  color: var(--color-primary);
  box-shadow: var(--color-shadow);
}

.chart-body {
  padding: 10px 0;
}

.chart-container {
  width: 100%;
  height: 260px;
}

.chart-footer {
  display: flex;
  gap: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border, #E2E8F0);
}

.chart-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-stat .stat-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
}

.chart-stat .stat-value {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  display: flex;
  align-items: center;
  gap: 4px;
}

.chart-stat.trend-up .stat-value {
  color: var(--color-success);
}

.trend-icon {
  width: 14px;
  height: 14px;
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .chart-container {
    height: 220px;
  }

  .chart-footer {
    flex-wrap: wrap;
    gap: 16px;
  }
}
</style>
