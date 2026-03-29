<template>
  <div class="glass-card chart-card revenue-gauge-card">
    <div class="chart-header">
      <div class="header-left">
        <span class="chart-title">🎯 收入目标达成</span>
        <span class="chart-subtitle">实时追踪收入目标完成情况</span>
      </div>
      <el-segmented v-model="targetType" :options="targetOptions" @change="handleTargetChange" />
    </div>
    <div ref="chartRef" class="chart-container" v-loading="loading"></div>
    <div class="stats-grid">
      <div class="stat-block">
        <div class="stat-label">{{ targetType === 'day' ? '今日' : '本月' }}收入</div>
        <div class="stat-value primary">¥{{ formatNumber(statsData.current) }}</div>
      </div>
      <div class="stat-block">
        <div class="stat-label">{{ targetType === 'day' ? '今日' : '本月' }}目标</div>
        <div class="stat-value">¥{{ formatNumber(statsData.target) }}</div>
      </div>
      <div class="stat-block">
        <div class="stat-label">还需完成</div>
        <div class="stat-value" :class="statsData.remaining > 0 ? 'warning' : 'success'">
          ¥{{ formatNumber(Math.abs(statsData.remaining)) }}
        </div>
      </div>
      <div class="stat-block">
        <div class="stat-label">完成进度</div>
        <div class="stat-value" :class="statsData.progress >= 100 ? 'success' : 'primary'">
          {{ statsData.progress }}%
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getFinanceStatistics } from '@/api/finance'
import { formatLocalDate } from '@/utils/localDate'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)
const targetType = ref('day')

const targetOptions = [
  { label: '今日目标', value: 'day' },
  { label: '月度目标', value: 'month' }
]

const statsData = ref({
  current: 0,
  target: 0,
  remaining: 0,
  progress: 0
})

// 格式化数字（千分位）
const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  return Math.round(num).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 图表配置
const getChartOption = (progress) => {
  // 根据进度确定颜色
  const getColor = (percent) => {
    if (percent >= 100) {
      return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
        { offset: 0, color: '#10B981' },
        { offset: 1, color: '#059669' }
      ])
    } else if (percent >= 70) {
      return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
        { offset: 0, color: '#3B82F6' },
        { offset: 1, color: '#2563EB' }
      ])
    } else if (percent >= 40) {
      return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
        { offset: 0, color: '#F59E0B' },
        { offset: 1, color: '#D97706' }
      ])
    } else {
      return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
        { offset: 0, color: '#EF4444' },
        { offset: 1, color: '#DC2626' }
      ])
    }
  }

  return {
    series: [
      {
        type: 'gauge',
        radius: '85%',
        center: ['50%', '55%'],
        startAngle: 200,
        endAngle: -20,
        min: 0,
        max: 100,
        splitNumber: 10,
        itemStyle: {
          color: getColor(progress)
        },
        progress: {
          show: true,
          width: 24,
          roundCap: true
        },
        pointer: {
          show: true,
          length: '60%',
          width: 5,
          offsetCenter: [0, '-10%'],
          itemStyle: {
            color: '#1E293B',
            shadowColor: 'rgba(0, 0, 0, 0.3)',
            shadowBlur: 10,
            shadowOffsetY: 2
          }
        },
        axisLine: {
          lineStyle: {
            width: 24,
            color: [[1, '#E2E8F0']]
          },
          roundCap: true
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          show: false
        },
        anchor: {
          show: true,
          showAbove: true,
          size: 14,
          itemStyle: {
            borderWidth: 6,
            borderColor: '#1E293B',
            shadowColor: 'rgba(0, 0, 0, 0.2)',
            shadowBlur: 8
          }
        },
        title: {
          show: false
        },
        detail: {
          valueAnimation: true,
          fontSize: 42,
          fontWeight: 'bold',
          fontFamily: 'Poppins, sans-serif',
          color: '#1E293B',
          offsetCenter: [0, '35%'],
          formatter: (value) => {
            return `{value|${value}}{unit|%}`
          },
          rich: {
            value: {
              fontSize: 42,
              fontWeight: 700,
              color: '#1E293B'
            },
            unit: {
              fontSize: 20,
              fontWeight: 500,
              color: '#64748B',
              padding: [0, 0, 0, 4]
            }
          }
        },
        data: [
          {
            value: progress,
            name: ''
          }
        ]
      },
      // 外圈装饰
      {
        type: 'gauge',
        radius: '78%',
        center: ['50%', '55%'],
        startAngle: 200,
        endAngle: -20,
        min: 0,
        max: 100,
        splitNumber: 10,
        itemStyle: {
          color: 'transparent'
        },
        progress: {
          show: false
        },
        pointer: {
          show: false
        },
        axisLine: {
          lineStyle: {
            width: 2,
            color: [[1, '#CBD5E1']]
          },
          roundCap: true
        },
        axisTick: {
          show: true,
          distance: -28,
          length: 4,
          lineStyle: {
            width: 2,
            color: '#CBD5E1'
          }
        },
        splitLine: {
          show: true,
          distance: -32,
          length: 8,
          lineStyle: {
            width: 2,
            color: '#CBD5E1'
          }
        },
        axisLabel: {
          show: false
        },
        detail: {
          show: false
        },
        data: []
      }
    ],
    animation: true,
    animationDuration: 1500,
    animationEasing: 'cubicOut'
  }
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const today = formatLocalDate()
    const yesterdayDate = new Date()
    yesterdayDate.setDate(yesterdayDate.getDate() - 1)
    const yesterday = formatLocalDate(yesterdayDate)

    if (targetType.value === 'day') {
      const [todayRes, yesterdayRes] = await Promise.all([
        getFinanceStatistics({ startDate: today, endDate: today }),
        getFinanceStatistics({ startDate: yesterday, endDate: yesterday })
      ])

      const current = todayRes?.code === 200 && todayRes.data ? Number(todayRes.data.totalIncome || 0) : 0
      const target = yesterdayRes?.code === 200 && yesterdayRes.data ? Number(yesterdayRes.data.totalIncome || 0) : 0

      const safeTarget = target > 0 ? target : (current > 0 ? current : 0)
      const progress = safeTarget > 0 ? Math.min(Math.round((current / safeTarget) * 100), 100) : 0

      statsData.value = {
        current,
        target: safeTarget,
        remaining: safeTarget - current,
        progress
      }
    } else {
      const res = await getFinanceStatistics()
      const current = res?.code === 200 && res.data ? Number(res.data.monthIncome || 0) : 0
      const target = res?.code === 200 && res.data ? Number(res.data.lastMonthIncome || 0) : 0

      const safeTarget = target > 0 ? target : (current > 0 ? current : 0)
      const progress = safeTarget > 0 ? Math.min(Math.round((current / safeTarget) * 100), 100) : 0

      statsData.value = {
        current,
        target: safeTarget,
        remaining: safeTarget - current,
        progress
      }
    }

    // 更新图表
    if (chartInstance) {
      chartInstance.setOption(getChartOption(statsData.value.progress))
    }
  } catch (error) {
    console.error('获取收入目标数据失败:', error)
    statsData.value = { current: 0, target: 0, remaining: 0, progress: 0 }
    if (chartInstance) {
      chartInstance.setOption(getChartOption(statsData.value.progress))
    }
  } finally {
    loading.value = false
  }
}

// 目标类型切换
const handleTargetChange = () => {
  fetchData()
}

// 初始化图表
const initChart = async () => {
  await nextTick()
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    await fetchData()

    // 响应式
    window.addEventListener('resize', () => {
      chartInstance?.resize()
    })
  }
}

onMounted(() => {
  initChart()
})

useDashboardChartRefresh(() => fetchData())

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    window.removeEventListener('resize', () => {
      chartInstance?.resize()
    })
  }
})
</script>

<style scoped>
.revenue-gauge-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FAFC 100%);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-title {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
}

.chart-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #94A3B8;
}

.chart-container {
  flex: 1;
  min-height: 300px;
  width: 100%;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 16px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

.stat-block {
  text-align: center;
  padding: 14px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-block:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(99, 102, 241, 0.3);
}

.stat-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #64748B;
  margin-bottom: 8px;
}

.stat-value {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: #1E293B;
}

.stat-value.primary {
  color: #3B82F6;
}

.stat-value.success {
  color: #10B981;
}

.stat-value.warning {
  color: #F59E0B;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-container {
    min-height: 260px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .stat-block {
    padding: 12px;
  }

  .stat-value {
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
