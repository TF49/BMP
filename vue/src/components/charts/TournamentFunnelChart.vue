<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon funnel-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 4h18l-7 8v6l-4 2v-8L3 4Z" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">赛事报名转化漏斗</h3>
          <span class="chart-subtitle">从浏览到实际参赛的转化情况</span>
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
import { getTournamentRegistrationStatistics } from '@/api/tournamentRegistration'
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
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  background: getCSSVar('--color-background', '#F8FAFC')
}))

const funnelData = ref([])

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchFunnelData = async () => {
  loading.value = true
  try {
    const res = await getTournamentRegistrationStatistics()
    if (res.code === 200 && res.data) {
      const data = res.data.funnel || res.data
      funnelData.value = [
        { name: '浏览赛事', value: parseNum(data.views || data.totalViews) },
        { name: '报名', value: parseNum(data.signups || data.totalSignups) },
        { name: '完成缴费', value: parseNum(data.paid || data.totalPaid) },
        { name: '到场签到', value: parseNum(data.attended || data.totalAttended) }
      ].filter(item => item.value > 0)
      if (funnelData.value.length === 0) {
        funnelData.value = []
      }
    }
  } catch (e) {
    console.error('获取赛事报名转化漏斗失败:', e)
    funnelData.value = []
  } finally {
    loading.value = false
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  const data = funnelData.value || []
  const topValue = data[0]?.value ?? 1

  if (data.length === 0) {
    return {
      tooltip: { show: false },
      series: [{ name: '报名转化', type: 'funnel', data: [], min: 0, max: 1 }]
    }
  }

  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.98)',
      borderColor: colors.secondary,
      borderWidth: 1,
      borderRadius: 10,
      padding: [12, 16],
      textStyle: {
        color: colors.textPrimary,
        fontSize: 12
      },
      formatter: params => {
        const d = params.data
        if (!d) return ''
        const total = topValue || 1
        const prevValue = params.dataIndex > 0 ? (data[params.dataIndex - 1]?.value ?? 1) : 1
        const stepRate = params.dataIndex === 0 ? 100 : (d.value / prevValue) * 100
        const overallRate = (d.value / total) * 100
        return `
          <div style="font-weight:600;margin-bottom:6px;color:${colors.textPrimary};">${d.name}</div>
          <div style="font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>人数</span>
            <span style="font-weight:600;color:${colors.textPrimary};">${d.value} 人</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>相对上一环节</span>
            <span style="font-weight:600;color:${colors.info};">${stepRate.toFixed(1)}%</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>整体转化率</span>
            <span style="font-weight:600;color:${colors.secondary};">${overallRate.toFixed(1)}%</span>
          </div>
        `
      }
    },
    series: [
      {
        name: '报名转化',
        type: 'funnel',
        left: '5%',
        right: '5%',
        top: 10,
        bottom: 10,
        width: '90%',
        min: 0,
        max: topValue,
        sort: 'descending',
        gap: 4,
        label: {
          show: true,
          position: 'inside',
          formatter: params => `${params.name}\n${topValue ? ((params.value / topValue) * 100).toFixed(1) : 0}%`,
          fontSize: 11
        },
        labelLine: {
          show: false
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2
        },
        data: data.map((item, index) => ({
          ...item,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
              { offset: 0, color: index === 0 ? colors.primary : colors.secondary },
              { offset: 1, color: colors.info }
            ])
          }
        }))
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
  if (chartInstance && funnelData.value.length > 0) {
    chartInstance.setOption(getChartOption())
  }
}

onMounted(() => {
  initChart()
  fetchFunnelData().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

useDashboardChartRefresh(() => fetchFunnelData().then(() => updateChart()))

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
  border-color: var(--color-secondary, #8B5CF6);
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

.funnel-icon {
  background: linear-gradient(135deg, var(--color-secondary) 0%, var(--color-primary) 100%);
  box-shadow: 0 4px 10px color-mix(in srgb, var(--color-secondary) 30%, transparent);
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

