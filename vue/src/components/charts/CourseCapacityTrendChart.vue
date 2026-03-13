<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon capacity-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M4 19h16M5 16l3-7 4 5 3-8 4 10" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">课程满班率趋势</h3>
          <span class="chart-subtitle">最近几周课程整体上座情况</span>
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
import { getCourseBookingStatistics } from '@/api/courseBooking'

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
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const trendData = ref({
  categories: [],
  fullRate: []
})

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchCapacityData = async () => {
  loading.value = true
  try {
    const res = await getCourseBookingStatistics()
    if (res.code === 200 && res.data) {
      if (Array.isArray(res.data.weeklyCapacity) && res.data.weeklyCapacity.length > 0) {
        trendData.value.categories = res.data.weeklyCapacity.map(item => item.week || item.label || '')
        trendData.value.fullRate = res.data.weeklyCapacity.map(item => parseNum(item.rate || item.capacityRate))
      } else {
        trendData.value = {
          categories: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周', '第7周', '第8周', '第9周', '第10周', '第11周', '第12周'],
          fullRate: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        }
      }
    } else {
      trendData.value = {
        categories: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周', '第7周', '第8周', '第9周', '第10周', '第11周', '第12周'],
        fullRate: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
      }
    }
  } catch (e) {
    console.error('获取课程满班率趋势失败:', e)
    trendData.value = {
      categories: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周', '第7周', '第8周', '第9周', '第10周', '第11周', '第12周'],
      fullRate: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    }
  } finally {
    loading.value = false
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  return {
    tooltip: {
      trigger: 'axis',
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
        return `
          <div style="margin-bottom:6px;font-weight:600;color:${colors.textPrimary};">${data.name}</div>
          <div style="font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>平均满班率</span>
            <span style="font-weight:600;color:${colors.success};">${(data.value * 100).toFixed(1)}%</span>
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
      data: trendData.value.categories,
      boundaryGap: false,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: colors.textSecondary,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      min: 0.4,
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
        type: 'line',
        name: '满班率',
        data: trendData.value.fullRate,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: colors.primary
        },
        itemStyle: {
          color: colors.success,
          borderColor: '#fff',
          borderWidth: 2,
          shadowBlur: 6,
          shadowColor: colors.success + '40'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors.primary + '33' },
            { offset: 1, color: 'rgba(255,255,255,0)' }
          ])
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut'
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
  fetchCapacityData().then(() => updateChart())
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

.capacity-icon {
  background: linear-gradient(135deg, var(--color-success) 0%, var(--color-primary) 100%);
  box-shadow: 0 4px 10px color-mix(in srgb, var(--color-success) 30%, transparent);
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
  height: 200px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 180px;
  }
}
</style>

