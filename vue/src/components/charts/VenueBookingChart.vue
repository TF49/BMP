<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon venue-booking-icon" :style="{ background: venueGradient }">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 21h18M3 10h18M5 6l7-3 7 3M4 10v11M20 10v11M8 14v3M12 14v3M16 14v3"/>
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">{{ venueName }}预订趋势</h3>
          <span class="chart-subtitle">{{ venueName }}近7天预订量分析</span>
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
      <div v-if="hasData" ref="chartRef" class="chart-container"></div>
      <div v-else class="chart-empty">
        <span class="empty-text">暂无预订数据</span>
      </div>
    </div>
    <div class="chart-footer">
      <div class="chart-stat">
        <span class="stat-label">总预订</span>
        <span class="stat-value">{{ totalBookings }}</span>
      </div>
      <div class="chart-stat">
        <span class="stat-label">日均</span>
        <span class="stat-value">{{ avgBookings }}</span>
      </div>
      <div class="chart-stat" :class="trendChange >= 0 ? 'trend-up' : 'trend-down'">
        <span class="stat-label">环比</span>
        <span class="stat-value">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="trend-icon">
            <path v-if="trendChange >= 0" d="M7 17l5-5 5 5M7 7l5 5 5-5"/>
            <path v-else d="M7 7l5 5 5-5M7 17l5-5 5 5"/>
          </svg>
          {{ trendChange >= 0 ? '+' : '' }}{{ trendChange.toFixed(1) }}%
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  venueName: {
    type: String,
    required: true
  },
  venueData: {
    type: Object,
    default: () => ({
      week: { categories: [], values: [] },
      month: { categories: [], values: [] }
    })
  },
  colorTheme: {
    type: String,
    default: 'green' // green, blue, purple, orange
  }
})

const chartRef = ref(null)
let chartInstance = null

// 根据主题获取渐变色
const getColorsByTheme = (theme) => {
  const themes = {
    green: {
      primary: '#10B981',
      secondary: '#34D399',
      tertiary: '#06B6D4',
      gradient: 'linear-gradient(135deg, #10B981 0%, #06B6D4 100%)'
    },
    blue: {
      primary: '#3B82F6',
      secondary: '#60A5FA',
      tertiary: '#06B6D4',
      gradient: 'linear-gradient(135deg, #3B82F6 0%, #06B6D4 100%)'
    },
    purple: {
      primary: '#8B5CF6',
      secondary: '#A78BFA',
      tertiary: '#EC4899',
      gradient: 'linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%)'
    },
    orange: {
      primary: '#F59E0B',
      secondary: '#FBBF24',
      tertiary: '#F97316',
      gradient: 'linear-gradient(135deg, #F59E0B 0%, #F97316 100%)'
    }
  }
  return themes[theme] || themes.green
}

const venueGradient = computed(() => getColorsByTheme(props.colorTheme).gradient)

// 获取CSS变量值的辅助函数
const getCSSVar = (varName, defaultValue) => {
  if (typeof window === 'undefined') return defaultValue
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || defaultValue
}

const activePeriod = ref('week')
const periods = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' }
]

const currentData = computed(() => props.venueData[activePeriod.value] || { categories: [], values: [] })
const hasData = computed(() => currentData.value.values && currentData.value.values.length > 0)
const totalBookings = computed(() => hasData.value ? currentData.value.values.reduce((a, b) => a + b, 0) : 0)
const avgBookings = computed(() => hasData.value ? Math.round(totalBookings.value / currentData.value.values.length) : 0)
const trendChange = computed(() => {
  if (!hasData.value) return 0
  const values = currentData.value.values
  if (values.length < 2) return 0
  const lastValue = values[values.length - 1]
  const prevValue = values[values.length - 2]
  return prevValue > 0 ? ((lastValue - prevValue) / prevValue) * 100 : 0
})

const getChartOption = () => {
  const colors = getColorsByTheme(props.colorTheme)
  const textPrimary = getCSSVar('--color-text-primary', '#1E293B')
  const textSecondary = getCSSVar('--color-text-secondary', '#64748B')
  const textMuted = getCSSVar('--color-text-muted', '#94A3B8')
  const border = getCSSVar('--color-border', '#E2E8F0')

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: colors.primary,
      borderWidth: 1,
      borderRadius: 12,
      padding: [14, 18],
      textStyle: {
        color: textPrimary,
        fontSize: 13,
        fontFamily: 'Open Sans, sans-serif'
      },
      axisPointer: {
        type: 'line',
        lineStyle: {
          color: colors.primary + '4D',
          width: 2,
          type: 'dashed'
        }
      },
      formatter: (params) => {
        const data = params[0]
        const trend = data.dataIndex > 0
          ? (data.value > currentData.value.values[data.dataIndex - 1] ? '↑' : '↓')
          : ''
        return `
          <div style="font-weight: 600; margin-bottom: 10px; color: ${textPrimary}; font-size: 14px;">${props.venueName} - ${data.name}</div>
          <div style="display: flex; align-items: center; gap: 10px; padding: 8px 0;">
            <span style="width: 12px; height: 12px; border-radius: 50%; background: linear-gradient(135deg, ${colors.primary}, ${colors.tertiary}); box-shadow: 0 2px 4px ${colors.primary}4D;"></span>
            <span style="color: ${textSecondary}; font-size: 13px;">预订量</span>
            <span style="font-weight: 700; color: ${textPrimary}; margin-left: auto; font-size: 15px;">${data.value} 次 ${trend}</span>
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
        color: textSecondary,
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
          color: border,
          type: 'dashed'
        }
      },
      axisLabel: {
        color: textMuted,
        fontSize: 11,
        fontFamily: 'Open Sans, sans-serif'
      }
    },
    series: [{
      type: 'line',
      name: '预订量',
      data: currentData.value.values,
      smooth: 0.5,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: {
        color: colors.primary,
        borderColor: '#fff',
        borderWidth: 3,
        shadowBlur: 10,
        shadowColor: colors.primary + '80',
        shadowOffsetY: 2
      },
      lineStyle: {
        width: 3,
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: colors.primary },
          { offset: 0.5, color: colors.secondary },
          { offset: 1, color: colors.tertiary }
        ]),
        shadowBlur: 12,
        shadowColor: colors.primary + '66',
        shadowOffsetY: 8,
        cap: 'round',
        join: 'round'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors.primary + '66' },
          { offset: 0.5, color: colors.primary + '33' },
          { offset: 1, color: colors.primary + '0D' }
        ])
      },
      emphasis: {
        focus: 'series',
        itemStyle: {
          shadowBlur: 20,
          shadowColor: colors.primary + '99',
          borderWidth: 4,
          symbolSize: 12
        },
        lineStyle: {
          width: 4
        }
      },
      label: {
        show: true,
        position: 'top',
        color: textSecondary,
        fontSize: 11,
        fontFamily: 'Open Sans, sans-serif',
        fontWeight: 600,
        formatter: '{c}',
        offset: [0, -8]
      },
      animationDuration: 1500,
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

const resizeChart = () => {
  chartInstance?.resize()
}

watch(activePeriod, () => {
  chartInstance?.setOption(getChartOption(), true)
})

watch(() => props.venueData, () => {
  chartInstance?.setOption(getChartOption(), true)
}, { deep: true })

onMounted(() => {
  initChart()
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

.venue-booking-icon {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
  height: 220px;
}

.chart-empty {
  width: 100%;
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px dashed #E2E8F0;
}

.empty-text {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  color: #94A3B8;
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
    height: 180px;
  }

  .chart-footer {
    flex-wrap: wrap;
    gap: 16px;
  }
}
</style>
