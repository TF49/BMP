<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon source-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <path d="M12 2v20" />
            <path d="M2 12h20" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">会员来源渠道</h3>
          <span class="chart-subtitle">新会员主要来自哪些渠道</span>
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
import { getMemberSource } from '@/api/member'
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
  secondary: getCSSVar('--color-secondary', '#8B5CF6'),
  info: getCSSVar('--color-info', '#06B6D4'),
  success: getCSSVar('--color-success', '#10B981'),
  warning: getCSSVar('--color-warning', '#F59E0B'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  background: getCSSVar('--color-background', '#F8FAFC')
}))

const sourceData = ref([])

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchSourceData = async () => {
  loading.value = true
  try {
    const res = await getMemberSource()
    if (res.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
      sourceData.value = res.data.map(item => ({
        name: item.source || item.name || '未知',
        value: parseNum(item.count || item.value)
      }))
    } else {
      sourceData.value = []
    }
  } catch (e) {
    console.error('获取会员来源数据失败:', e)
    sourceData.value = []
  } finally {
    loading.value = false
  }
}

// 高对比度配色，避免相邻扇区颜色相近
const SOURCE_CHART_COLORS = ['#2563EB', '#EA580C', '#059669', '#7C3AED', '#DC2626', '#CA8A04']

const getChartOption = () => {
  const colors = themeColors.value
  const colorList = SOURCE_CHART_COLORS
  const total = sourceData.value.reduce((sum, d) => sum + d.value, 0)
  const hasData = sourceData.value.length > 0
  return {
    graphic: !hasData ? {
      type: 'text',
      left: 'center',
      top: 'middle',
      style: { text: loading.value ? '加载中...' : '暂无数据', fill: colors.textSecondary, fontSize: 14 }
    } : undefined,
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.98)',
      borderColor: colors.primary,
      borderWidth: 1,
      borderRadius: 10,
      padding: [12, 16],
      textStyle: {
        color: colors.textPrimary,
        fontSize: 12
      },
      formatter: params => `
        <div style="font-weight:600;margin-bottom:6px;color:${colors.textPrimary};">${params.name}</div>
        <div style="display:flex;justify-content:space-between;gap:12px;font-size:12px;color:${colors.textSecondary};">
          <span>人数</span>
          <span style="font-weight:600;color:${colors.textPrimary};">${params.value} 人</span>
        </div>
        <div style="display:flex;justify-content:space-between;gap:12px;font-size:12px;color:${colors.textSecondary};margin-top:4px;">
          <span>占比</span>
          <span style="font-weight:600;color:${colors.secondary};">${params.percent != null ? params.percent.toFixed(1) : 0}%</span>
        </div>
      `
    },
    legend: {
      orient: 'vertical',
      right: 0,
      top: 'center',
      textStyle: {
        color: colors.textSecondary,
        fontSize: 11
      }
    },
    series: [
      {
        name: '会员来源',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          color: colors.textPrimary,
          fontSize: 12,
          formatter: (params) => {
            const pct = total ? ((params.value / total) * 100).toFixed(1) : 0
            return `${params.name}\n${params.value}人 (${pct}%)`
          }
        },
        labelLine: {
          show: true,
          length: 8,
          length2: 6,
          lineStyle: { color: colors.border }
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 3
        },
        data: sourceData.value.map((item, index) => {
          const colorValue = colorList[index % colorList.length]
          return {
            ...item,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
                { offset: 0, color: colorValue },
                { offset: 1, color: colorValue + 'B3' }
              ])
            }
          }
        })
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
  fetchSourceData().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

useDashboardChartRefresh(() => fetchSourceData().then(() => updateChart()))

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

.source-icon {
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
  height: 220px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 200px;
  }
}
</style>

