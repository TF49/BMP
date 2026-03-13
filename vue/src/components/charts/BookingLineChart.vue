<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon booking-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 3v18h18"/>
            <path d="M18 17V9"/>
            <path d="M13 17V5"/>
            <path d="M8 17v-3"/>
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">预订趋势</h3>
          <span class="chart-subtitle">场地预订量变化趋势</span>
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
        <span class="stat-label">总预订</span>
        <span class="stat-value">{{ totalBookings ?? 0 }}</span>
      </div>
      <div class="chart-stat">
        <span class="stat-label">日均</span>
        <span class="stat-value">{{ avgBookings ?? 0 }}</span>
      </div>
      <div class="chart-stat" :class="(trendChange ?? 0) >= 0 ? 'trend-up' : 'trend-down'">
        <span class="stat-label">环比</span>
        <span class="stat-value">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="trend-icon">
            <path v-if="(trendChange ?? 0) >= 0" d="M7 17l5-5 5 5M7 7l5 5 5-5"/>
            <path v-else d="M7 7l5 5 5-5M7 17l5-5 5 5"/>
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
import { getBookingTrend } from '@/api/booking'

const chartRef = ref(null)
let chartInstance = null

// 获取CSS变量值的辅助函数
const getCSSVar = (varName, defaultValue) => {
  if (typeof window === 'undefined') return defaultValue
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || defaultValue
}

// 获取主题颜色
const themeColors = computed(() => ({
  success: getCSSVar('--color-success', '#10B981'),
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
const vals = (v) => v && Array.isArray(v) ? v : []
const totalBookings = computed(() => vals(currentData.value.values).reduce((a, b) => a + (Number(b) || 0), 0))
const avgBookings = computed(() => {
  const arr = vals(currentData.value.values)
  return arr.length > 0 ? Math.round(totalBookings.value / arr.length) : 0
})
const trendChange = computed(() => {
  const values = vals(currentData.value.values)
  if (values.length < 2) return 0
  const lastValue = Number(values[values.length - 1]) || 0
  const prevValue = Number(values[values.length - 2]) || 0
  return prevValue > 0 ? ((lastValue - prevValue) / prevValue) * 100 : 0
})

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchTrend = async (period) => {
  const res = await getBookingTrend({ period })
  if (res.code !== 200 || !res.data) return null
  if (res.data.categories && res.data.values) {
    return {
      categories: res.data.categories,
      values: (res.data.values || []).map(v => Math.round(parseNum(v)))
    }
  }
  return null
}

const getChartOption = () => {
  const colors = themeColors.value
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: colors.border,
      borderWidth: 1,
      borderRadius: 12,
      padding: [14, 18],
      textStyle: {
        color: colors.textPrimary,
        fontSize: 13,
        fontFamily: 'Open Sans, sans-serif'
      },
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: colors.success + '4D',
          width: 2,
          type: 'dashed'
        }
      },
      formatter: (params) => {
        const data = params[0]
        const arr = vals(currentData.value.values)
        const trend = data.dataIndex > 0 && arr[data.dataIndex - 1] != null
          ? (data.value > arr[data.dataIndex - 1] ? '↑' : '↓')
          : ''
        return `
          <div style="font-weight: 600; color: ${colors.textPrimary}; margin-bottom: 8px;">${data.name}</div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="width: 10px; height: 10px; border-radius: 50%; background: ${colors.success};"></span>
            <span style="color: ${colors.textSecondary};">预订量</span>
            <span style="font-weight: 600; color: ${colors.textPrimary}; margin-left: auto;">${data.value} 次 ${trend}</span>
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
      boundaryGap: false,
      data: currentData.value.categories,
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: colors.textSecondary,
        fontSize: 11,
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
        fontFamily: 'Open Sans, sans-serif'
      }
    },
    series: [{
      name: '预订量',
      type: 'line',
      smooth: 0.5,
      symbol: 'circle',
      symbolSize: 10,
      sampling: 'average',
      itemStyle: {
        color: colors.success,
        borderColor: '#fff',
        borderWidth: 4,
        shadowBlur: 12,
        shadowColor: colors.success + '80',
        shadowOffsetY: 2
      },
      lineStyle: {
        width: 4,
        color: colors.success,
        shadowBlur: 15,
        shadowColor: colors.success + '66',
        shadowOffsetY: 10,
        cap: 'round',
        join: 'round'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors.success + '66' },
          { offset: 0.5, color: colors.success + '33' },
          { offset: 1, color: colors.success + '0D' }
        ])
      },
      emphasis: {
        focus: 'series',
        itemStyle: {
          shadowBlur: 20,
          shadowColor: colors.success + '99',
          borderWidth: 5,
          symbolSize: 14
        },
        lineStyle: {
          width: 5
        }
      },
      label: {
        show: true,
        position: 'top',
        color: colors.success,
        fontSize: 11,
        fontFamily: 'Open Sans, sans-serif',
        fontWeight: 600,
        formatter: '{c}',
        offset: [0, -10]
      },
      data: currentData.value.values,
      animationDuration: 1800,
      animationEasing: 'cubicOut',
      animationDelay: (idx) => idx * 60
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
  Promise.all([fetchTrend('week'), fetchTrend('month')])
    .then(([week, month]) => {
      if (week) weekData.value = week
      if (month) monthData.value = month
      updateChart()
    })
    .catch((e) => console.error('获取预订趋势失败:', e))
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
  animation: chartFadeIn 0.6s ease-out 0.3s both;
  display: flex;
  flex-direction: column;
}

.chart-card:hover {
  box-shadow: var(--color-shadow-hover);
  border-color: var(--color-success);
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
  margin-bottom: 16px;
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

.booking-icon {
  background: linear-gradient(135deg, var(--color-success) 0%, var(--color-info) 100%);
  box-shadow: 0 4px 12px color-mix(in srgb, var(--color-success) 30%, transparent);
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
  color: var(--color-success);
  background: color-mix(in srgb, var(--color-success) 8%, transparent);
}

.period-btn.active {
  background: var(--color-card-bg);
  color: var(--color-success);
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
  color: var(--color-success, #10B981);
}

.chart-stat.trend-down .stat-value {
  color: var(--color-danger, #EF4444);
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
