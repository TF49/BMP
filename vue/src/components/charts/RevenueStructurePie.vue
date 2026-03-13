<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon revenue-structure-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12A9 9 0 1 1 12 3" />
            <path d="M22 12A10 10 0 1 1 12 2" />
            <path d="M12 8V12L15 15" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">收入结构</h3>
          <span class="chart-subtitle">场地 / 课程 / 赛事 / 器材 / 穿线</span>
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
import { getFinanceRatio } from '@/api/finance'

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

const revenueData = ref([])

// 业务类型英文 -> 图例/展示用中文
const businessTypeToChinese = {
  RECHARGE: '会员充值',
  COURSE: '课程',
  BOOKING: '场地预约',
  EQUIPMENT: '器材租借',
  TOURNAMENT: '赛事',
  STRINGING: '穿线',
  OTHER: '其他',
  场地: '场地',
  课程: '课程',
  赛事: '赛事',
  器材: '器材',
  穿线: '穿线'
}

const toDisplayName = (name) => {
  if (!name) return '其他'
  return businessTypeToChinese[name] || name
}

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchRevenueStructure = async () => {
  loading.value = true
  try {
    const res = await getFinanceRatio()
    if (res.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
      revenueData.value = res.data.map(item => {
        const rawName = item.businessType || item.type || item.name || '未知'
        return {
          name: toDisplayName(rawName),
          value: Math.round(parseNum(item.amount || item.revenue || item.value))
        }
      })
    } else {
      revenueData.value = []
    }
  } catch (e) {
    console.error('获取收入结构数据失败:', e)
    revenueData.value = []
  } finally {
    loading.value = false
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  const colorList = [
    colors.primary,
    colors.secondary,
    colors.info,
    colors.success,
    colors.warning
  ]
  const total = revenueData.value.reduce((sum, d) => sum + d.value, 0)
  const hasData = revenueData.value.length > 0
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
          <span>金额</span>
          <span style="font-weight:600;color:${colors.textPrimary};">¥${params.value.toLocaleString()}</span>
        </div>
        <div style="display:flex;justify-content:space-between;gap:12px;font-size:12px;color:${colors.textSecondary};margin-top:4px;">
          <span>占比</span>
          <span style="font-weight:600;color:${colors.secondary};">${params.percent.toFixed(1)}%</span>
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
        name: '收入结构',
        type: 'pie',
        radius: ['48%', '75%'],
        center: ['42%', '52%'],
        avoidLabelOverlap: true,
        label: {
          show: true,
          color: colors.textPrimary,
          fontSize: 11,
          formatter: (params) => {
            const pct = total ? ((params.value / total) * 100).toFixed(1) : 0
            const amount = params.value != null ? `¥${Number(params.value).toLocaleString()}` : '¥0'
            return `${params.name}\n${amount} (${pct}%)`
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
        data: revenueData.value.map((item, index) => {
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
  fetchRevenueStructure().then(() => updateChart())
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

.revenue-structure-icon {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  box-shadow: 0 4px 10px color-mix(in srgb, var(--color-primary) 30%, transparent);
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
  height: 230px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 210px;
  }
}
</style>

