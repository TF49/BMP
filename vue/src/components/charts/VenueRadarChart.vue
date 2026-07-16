<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon venue-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 2 7 12 12 22 7 12 2"/>
            <polyline points="2 17 12 22 22 17"/>
            <polyline points="2 12 12 17 22 12"/>
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">场馆经营六项</h3>
          <span class="chart-subtitle">真实业务统计口径</span>
        </div>
      </div>
      <div class="venue-selector">
        <select v-model="selectedVenue" class="venue-select">
          <option v-for="venue in venues" :key="venue.value" :value="venue.value">
            {{ venue.label }}
          </option>
        </select>
      </div>
    </div>
    <div class="chart-body">
      <div ref="chartRef" class="chart-container"></div>
    </div>
    <div class="chart-metrics">
      <div
        v-for="(metric, index) in currentMetrics"
        :key="metric.name"
        class="metric-item"
        :style="{ '--metric-color': metricColors[index] }"
      >
        <div class="metric-header">
          <span class="metric-name">{{ metric.name }}</span>
          <span class="metric-value">{{ formatMetricValue(metric) }}</span>
        </div>
        <div class="metric-bar">
          <div class="metric-fill" :style="{ width: metric.percent + '%', background: metricColors[index] }"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from '@/utils/echarts'
import { getVenueStatistics } from '@/api/venue'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const chartRef = ref(null)
let chartInstance = null

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
  accent: getCSSVar('--color-accent', '#EC4899'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const selectedVenue = ref('all')
const venues = ref([{ label: '全部场馆', value: 'all' }])
const metricColors = computed(() => [
  themeColors.value.primary,
  themeColors.value.secondary,
  themeColors.value.info,
  themeColors.value.success,
  themeColors.value.warning,
  themeColors.value.accent
])

const indicators = [
  { name: '场地数', formatter: 'count' },
  { name: '今日预订', formatter: 'count' },
  { name: '今日收入', formatter: 'currency' },
  { name: '课程数', formatter: 'count' },
  { name: '赛事数', formatter: 'count' },
  { name: '使用中场地', formatter: 'count' }
]

const venueData = ref({ all: [0, 0, 0, 0, 0, 0] })
const indicatorMax = ref([1, 1, 1, 1, 1, 1])

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchVenueRadarData = async () => {
  try {
    const res = await getVenueStatistics()
    const metricsList = res.code === 200 && Array.isArray(res.data?.venueMetrics)
      ? res.data.venueMetrics
      : []

    venues.value = [
      { label: '全部场馆', value: 'all' },
      ...metricsList.map(item => ({
        label: item.venueName || item.name || '未知场馆',
        value: String(item.venueId || item.id)
      }))
    ]

    const allData = { all: [0, 0, 0, 0, 0, 0] }
    metricsList.forEach(item => {
      const values = [
        parseNum(item.courtCount ?? item.court ?? item.repeatRate ?? item.repeat),
        parseNum(item.todayBookings ?? item.booking ?? item.bookingRate),
        parseNum(item.todayRevenue ?? item.revenue ?? item.revenueContribution),
        parseNum(item.courseCount ?? item.course ?? item.satisfactionRate ?? item.satisfaction),
        parseNum(item.tournamentCount ?? item.tournament ?? item.turnoverRate ?? item.turnover),
        parseNum(item.courtsInUse ?? item.utilization ?? item.utilizationRate)
      ]
      allData[String(item.venueId || item.id)] = values
      values.forEach((value, index) => {
        allData.all[index] += value
      })
    })

    venueData.value = allData
    indicatorMax.value = indicators.map((_, index) => {
      const maxValue = Math.max(...Object.values(allData).map(metricList => parseNum(metricList[index])), 0)
      return maxValue > 0 ? Math.ceil(maxValue * 1.2) : 1
    })
  } catch (e) {
    console.error('获取场馆雷达图数据失败:', e)
    venueData.value = { all: [0, 0, 0, 0, 0, 0] }
    indicatorMax.value = [1, 1, 1, 1, 1, 1]
  }
}

const currentMetrics = computed(() => {
  const values = venueData.value[selectedVenue.value] || [0, 0, 0, 0, 0, 0]
  return indicators.map((indicator, index) => ({
    name: indicator.name,
    formatter: indicator.formatter,
    value: parseNum(values[index]),
    percent: indicatorMax.value[index] > 0 ? Math.min((parseNum(values[index]) / indicatorMax.value[index]) * 100, 100) : 0
  }))
})

const formatMetricValue = (metric) => {
  if (metric.formatter === 'currency') {
    return `¥${parseNum(metric.value).toLocaleString()}`
  }
  return parseNum(metric.value).toLocaleString()
}

const getChartOption = () => {
  const colors = themeColors.value
  const primaryRgb = hexToRgb(colors.primary)
  return {
    tooltip: {
      trigger: 'item',
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
      formatter: params => {
        const values = Array.isArray(params.value) ? params.value : []
        const lines = indicators.map((indicator, index) => {
          const rawValue = parseNum(values[index])
          const display = indicator.formatter === 'currency'
            ? `¥${rawValue.toLocaleString()}`
            : rawValue.toLocaleString()
          return `${indicator.name}：${display}`
        })
        return [params.name, ...lines].join('<br/>')
      }
    },
    radar: {
      indicator: indicators.map((indicator, index) => ({
        name: indicator.name,
        max: indicatorMax.value[index] || 1
      })),
      center: ['50%', '50%'],
      radius: '65%',
      startAngle: 90,
      splitNumber: 4,
      shape: 'polygon',
      axisName: {
        color: colors.textSecondary,
        fontSize: 12,
        fontFamily: 'Open Sans, sans-serif'
      },
      splitArea: {
        areaStyle: {
          color: [
            `rgba(${primaryRgb.r}, ${primaryRgb.g}, ${primaryRgb.b}, 0.02)`,
            `rgba(${primaryRgb.r}, ${primaryRgb.g}, ${primaryRgb.b}, 0.04)`,
            `rgba(${primaryRgb.r}, ${primaryRgb.g}, ${primaryRgb.b}, 0.06)`,
            `rgba(${primaryRgb.r}, ${primaryRgb.g}, ${primaryRgb.b}, 0.08)`
          ]
        }
      },
      axisLine: {
        lineStyle: {
          color: colors.border
        }
      },
      splitLine: {
        lineStyle: {
          color: colors.border
        }
      }
    },
    series: [{
      name: '场馆经营指标',
      type: 'radar',
      data: [{
        value: venueData.value[selectedVenue.value] || [0, 0, 0, 0, 0, 0],
        name: venues.value.find(v => v.value === selectedVenue.value)?.label || '全部场馆',
        symbol: 'circle',
        symbolSize: 10,
        lineStyle: {
          width: 3.5,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: colors.primary },
            { offset: 0.5, color: colors.secondary },
            { offset: 1, color: colors.info }
          ]),
          shadowBlur: 8,
          shadowColor: colors.info + '4D'
        },
        areaStyle: {
          color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
            { offset: 0, color: colors.primary + '73' },
            { offset: 0.5, color: colors.secondary + '40' },
            { offset: 1, color: colors.info + '1F' }
          ])
        },
        itemStyle: {
          color: colors.primary,
          borderColor: '#fff',
          borderWidth: 3,
          shadowBlur: 12,
          shadowColor: colors.primary + '99',
          shadowOffsetY: 2
        }
      }],
      animationDuration: 1500,
      animationEasing: 'elasticOut',
      animationDelay: 200
    }]
  }
}

const hexToRgb = (hex) => {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : { r: 59, g: 130, b: 246 }
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
    chartInstance.setOption(getChartOption(), true)
  }
}

watch(selectedVenue, () => {
  updateChart()
})

onMounted(() => {
  initChart()
  fetchVenueRadarData().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

useDashboardChartRefresh(() => fetchVenueRadarData().then(() => updateChart()))

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
  animation: chartFadeIn 0.6s ease-out 0.2s both;
  display: flex;
  flex-direction: column;
}

.chart-card:hover {
  box-shadow: var(--color-shadow-hover);
  border-color: var(--color-info);
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

.venue-icon {
  background: linear-gradient(135deg, var(--color-info) 0%, var(--color-primary) 100%);
  box-shadow: 0 4px 12px color-mix(in srgb, var(--color-info) 30%, transparent);
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

.venue-selector {
  position: relative;
}

.venue-select {
  appearance: none;
  padding: 10px 36px 10px 14px;
  background: var(--color-background, #F8FAFC);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 10px;
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: var(--color-text-primary, #1E293B);
  cursor: pointer;
  transition: all 0.25s ease;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
}

.venue-select:hover {
  border-color: var(--color-primary, #3B82F6);
}

.venue-select:focus {
  outline: none;
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.chart-body {
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 280px;
}

.chart-metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border, #E2E8F0);
}

.metric-item {
  padding: 12px 14px;
  background: var(--color-background, #F8FAFC);
  border-radius: 10px;
  transition: all 0.25s ease;
}

.metric-item:hover {
  background: #EEF2FF;
  transform: translateY(-2px);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.metric-name {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.metric-value {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.metric-bar {
  height: 6px;
  background: #E2E8F0;
  border-radius: 3px;
  overflow: hidden;
}

.metric-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .chart-container {
    height: 240px;
  }

  .chart-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
