<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon member-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <path d="M12 6v6l4 2"/>
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">会员分布</h3>
          <span class="chart-subtitle">按会员等级分类统计</span>
        </div>
      </div>
    </div>
    <div class="chart-body">
      <div ref="chartRef" class="chart-container"></div>
      <div class="center-stat">
        <div class="center-value">{{ totalMembers ?? 0 }}</div>
        <div class="center-label">总会员</div>
      </div>
      <div v-if="!(memberData && memberData.length)" class="chart-empty-hint">
        <span>暂无会员分布数据</span>
      </div>
    </div>
    <div class="chart-legend">
      <div
        v-for="(item, index) in memberData"
        :key="item.name"
        class="legend-item"
        :class="{ active: activeIndex === index }"
        @mouseenter="handleLegendHover(index)"
        @mouseleave="handleLegendLeave"
      >
        <span class="legend-dot" :style="{ background: colors[index] }"></span>
        <span class="legend-name">{{ item.name }}</span>
        <span class="legend-value">{{ (item.value ?? 0) }}人</span>
        <span class="legend-percent">{{ totalMembers ? (((item.value ?? 0) / totalMembers) * 100).toFixed(1) : '0.0' }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getMemberDistribution } from '@/api/member'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const chartRef = ref(null)
let chartInstance = null
const activeIndex = ref(-1)

// 获取CSS变量值的辅助函数
const getCSSVar = (varName, defaultValue) => {
  if (typeof window === 'undefined') return defaultValue
  return getComputedStyle(document.documentElement).getPropertyValue(varName).trim() || defaultValue
}

// 获取主题颜色
const themeColors = computed(() => ({
  primary: getCSSVar('--color-primary', '#3B82F6'),
  secondary: getCSSVar('--color-secondary', '#8B5CF6'),
  info: getCSSVar('--color-info', '#06B6D4'),
  success: getCSSVar('--color-success', '#10B981'),
  warning: getCSSVar('--color-warning', '#F59E0B'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  background: getCSSVar('--color-background', '#F8FAFC'),
  cardBgHover: getCSSVar('--color-card-bg-hover', '#EEF2FF')
}))

// 使用主题颜色生成饼图颜色数组
const colors = computed(() => [
  themeColors.value.primary,
  themeColors.value.secondary,
  themeColors.value.info,
  themeColors.value.success,
  themeColors.value.warning
])

const memberData = ref([])

const totalMembers = computed(() => (memberData.value || []).reduce((a, b) => a + (b.value ?? 0), 0))

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchDistribution = async () => {
  try {
    const res = await getMemberDistribution()
    if (res.code === 200 && Array.isArray(res.data)) {
      memberData.value = res.data.map((x) => ({
        name: x.name,
        value: Math.round(parseNum(x.value))
      }))
    } else {
      memberData.value = []
    }
  } catch (e) {
    console.error('获取会员分布失败:', e)
    memberData.value = []
  }
}

const getChartOption = () => {
  const themeColor = themeColors.value
  return {
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(255, 255, 255, 0.98)',
    borderColor: themeColor.secondary,
    borderWidth: 1,
    borderRadius: 12,
    padding: [16, 20],
    textStyle: {
      color: themeColor.textPrimary,
      fontSize: 13,
      fontFamily: 'Open Sans, sans-serif'
    },
    formatter: (params) => `
      <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
        <span style="width: 14px; height: 14px; border-radius: 50%; background: ${params.color}; box-shadow: 0 2px 4px ${params.color}4D;"></span>
        <span style="font-weight: 700; color: ${themeColor.textPrimary}; font-size: 14px;">${params.name}</span>
      </div>
      <div style="padding: 12px; background: linear-gradient(135deg, ${themeColor.background} 0%, ${themeColor.cardBgHover} 100%); border-radius: 8px; border-left: 3px solid ${params.color};">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
          <span style="color: ${themeColor.textSecondary}; font-size: 12px;">会员人数</span>
          <span style="font-weight: 700; color: ${themeColor.textPrimary}; font-size: 16px;">${params.value}人</span>
        </div>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="color: ${themeColor.textSecondary}; font-size: 12px;">占比</span>
          <span style="font-weight: 700; color: ${params.color}; font-size: 16px;">${params.percent.toFixed(1)}%</span>
        </div>
      </div>
    `
  },
  series: [{
    name: '会员分布',
    type: 'pie',
    radius: ['58%', '82%'],
    center: ['50%', '50%'],
    avoidLabelOverlap: false,
    itemStyle: {
      borderRadius: 10,
      borderColor: '#fff',
      borderWidth: 4,
      shadowBlur: 8,
      shadowColor: 'rgba(0, 0, 0, 0.1)'
    },
    label: {
      show: false
    },
    emphasis: {
      scale: true,
      scaleSize: 12,
      itemStyle: {
        shadowBlur: 30,
        shadowOffsetX: 0,
        shadowOffsetY: 4,
        shadowColor: themeColor.secondary + '66',
        borderWidth: 5
      },
      label: {
        show: true,
        fontSize: 14,
        fontWeight: 'bold',
        color: themeColor.textPrimary
      }
    },
    labelLine: {
      show: false
    },
    data: memberData.value.map((item, index) => {
      const colorValue = colors.value[index % colors.value.length]
      return {
      ...item,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: colorValue },
          { offset: 0.5, color: adjustColor(colorValue, 15) },
          { offset: 1, color: adjustColor(colorValue, 25) }
        ]),
        shadowBlur: 6,
        shadowColor: colorValue + '33'
      }
    }}),
    animationType: 'expansion',
    animationDuration: 1500,
    animationEasing: 'elasticOut',
    animationDelay: (idx) => idx * 150
  }]
  }
}

const adjustColor = (color, amount) => {
  if (!color || typeof color !== 'string') {
    return 'rgb(59, 130, 246)' // fallback to primary blue
  }
  const hex = color.replace('#', '')
  const r = Math.min(255, parseInt(hex.slice(0, 2), 16) + amount)
  const g = Math.min(255, parseInt(hex.slice(2, 4), 16) + amount)
  const b = Math.min(255, parseInt(hex.slice(4, 6), 16) + amount)
  return `rgb(${r}, ${g}, ${b})`
}

const handleLegendHover = (index) => {
  activeIndex.value = index
  chartInstance?.dispatchAction({
    type: 'highlight',
    seriesIndex: 0,
    dataIndex: index
  })
}

const handleLegendLeave = () => {
  activeIndex.value = -1
  chartInstance?.dispatchAction({
    type: 'downplay',
    seriesIndex: 0
  })
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

onMounted(() => {
  initChart()
  fetchDistribution().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

useDashboardChartRefresh(() => fetchDistribution().then(() => updateChart()))

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
  animation: chartFadeIn 0.6s ease-out 0.1s both;
  display: flex;
  flex-direction: column;
}

.chart-card:hover {
  box-shadow: var(--color-shadow-hover);
  border-color: var(--color-secondary);
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

.member-icon {
  background: linear-gradient(135deg, var(--color-secondary) 0%, var(--color-primary) 100%);
  box-shadow: 0 4px 12px color-mix(in srgb, var(--color-secondary) 30%, transparent);
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

.chart-body {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 220px;
}

.center-stat {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
}

.chart-empty-hint {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
  pointer-events: none;
}

.center-value {
  font-family: 'Poppins', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.center-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: var(--color-text-muted, #94A3B8);
  margin-top: 4px;
}

.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border, #E2E8F0);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: var(--color-background, #F8FAFC);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.legend-item:hover,
.legend-item.active {
  background: #EEF2FF;
  transform: translateX(4px);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-name {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  color: var(--color-text-primary, #1E293B);
  flex: 1;
}

.legend-value {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.legend-percent {
  font-family: 'Poppins', sans-serif;
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
  min-width: 45px;
  text-align: right;
}

@media (max-width: 768px) {
  .chart-container {
    height: 180px;
  }

  .center-value {
    font-size: 26px;
  }

  .legend-item {
    padding: 8px 12px;
  }
}
</style>
