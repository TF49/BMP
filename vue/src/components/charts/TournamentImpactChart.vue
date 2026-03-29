<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon impact-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 3h7l2 4h9v13H3V3Z" />
            <path d="M7 13l3 3 7-7" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">赛事带动效果</h3>
          <span class="chart-subtitle">赛事对新增会员与收入的贡献</span>
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
import { getTournamentStatistics } from '@/api/tournament'
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
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0')
}))

const impactData = ref([])

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchTournamentImpact = async () => {
  loading.value = true
  try {
    const res = await getTournamentStatistics()
    if (res.code === 200 && Array.isArray(res.data?.tournamentImpact)) {
      const impact = res.data.tournamentImpact
      impactData.value = impact.map(item => ({
        name: item.tournamentName || item.name || '未知赛事',
        newMembers: parseNum(item.newMembers != null ? item.newMembers : item.members),
        revenue: parseNum(item.revenue != null ? item.revenue : item.income)
      }))
    } else {
      impactData.value = []
    }
  } catch (e) {
    console.error('获取赛事影响数据失败:', e)
    impactData.value = []
  } finally {
    loading.value = false
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  const hasData = impactData.value.length > 0
  return {
    graphic: !hasData ? {
      type: 'text',
      left: 'center',
      top: 'middle',
      style: { text: loading.value ? '加载中...' : '暂无数据', fill: colors.textSecondary, fontSize: 14 }
    } : undefined,
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
        const mem = params.find(p => p.seriesName === '新增会员')
        const rev = params.find(p => p.seriesName === '赛事收入')
        const memVal = mem ? mem.value : 0
        const revVal = rev ? rev.value : 0
        return `
          <div style="margin-bottom:6px;font-weight:600;color:${colors.textPrimary};">${mem ? mem.axisValue : ''}</div>
          <div style="font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>新增会员</span>
            <span style="font-weight:600;color:${colors.success};">${memVal} 人</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>赛事收入</span>
            <span style="font-weight:600;color:${colors.textPrimary};">¥${Number(revVal).toLocaleString()}</span>
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
      top: '18%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: impactData.value.map(i => i.name),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: colors.textSecondary,
        fontSize: 11
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '新增会员',
        nameLocation: 'middle',
        nameGap: 50,
        nameTextStyle: {
          color: colors.textSecondary,
          fontSize: 12,
          fontWeight: 500
        },
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
      {
        type: 'value',
        name: '赛事收入',
        nameLocation: 'middle',
        nameGap: 50,
        nameTextStyle: {
          color: colors.textSecondary,
          fontSize: 12,
          fontWeight: 500
        },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: {
          color: colors.textSecondary,
          fontSize: 11,
          formatter: val => `¥${val / 1000}k`
        }
      }
    ],
    series: [
      {
        name: '新增会员',
        type: 'bar',
        data: impactData.value.map(i => i.newMembers),
        barWidth: 14,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: colors.primary
        }
      },
      {
        name: '赛事收入',
        type: 'line',
        yAxisIndex: 1,
        data: impactData.value.map(i => i.revenue),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: colors.success,
          borderColor: '#fff',
          borderWidth: 2
        },
        lineStyle: {
          width: 2,
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
  fetchTournamentImpact().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

useDashboardChartRefresh(() => fetchTournamentImpact().then(() => updateChart()))

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

.impact-icon {
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
  height: 220px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 200px;
  }
}
</style>

