<template>
  <div class="glass-card chart-card">
    <div class="chart-header">
      <div class="header-left">
        <span class="chart-title">💰 收入 & 预订趋势对比</span>
        <span class="chart-subtitle">分析收入与预订量的关联性</span>
      </div>
      <div class="header-right">
        <el-segmented v-model="timeRange" :options="timeOptions" @change="handleTimeChange" />
      </div>
    </div>
    <div ref="chartRef" class="chart-container" v-loading="loading"></div>
    <div class="chart-footer">
      <div class="legend-item">
        <span class="legend-dot" style="background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);"></span>
        <span class="legend-label">收入金额（元）</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot" style="background: linear-gradient(135deg, #10B981 0%, #059669 100%);"></span>
        <span class="legend-label">预订次数（次）</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot" style="background: #F59E0B;"></span>
        <span class="legend-label">单均价（元/次）</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getFinanceTrend } from '@/api/finance'
import { getBookingTrend } from '@/api/booking'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)
const timeRange = ref('week')

const timeOptions = [
  { label: '最近7天', value: 'week' },
  { label: '最近30天', value: 'month' }
]

const toDateStr = (d) => d.toISOString().slice(0, 10)
const weekdayLabel = (dateStr) => {
  const labels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const d = new Date(dateStr + 'T00:00:00')
  return labels[d.getDay()]
}

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

// 图表配置
const getChartOption = (data) => {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E2E8F0',
      borderWidth: 1,
      textStyle: {
        color: '#1E293B'
      },
      padding: [12, 16],
      formatter: (params) => {
        let result = `<div style="font-weight: 600; margin-bottom: 8px;">${params[0].axisValue}</div>`
        params.forEach(item => {
          const color = item.color.colorStops ? item.color.colorStops[0].color : item.color
          result += `
            <div style="display: flex; align-items: center; justify-content: space-between; margin: 4px 0;">
              <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${color}; margin-right: 8px;"></span>
              <span style="flex: 1;">${item.seriesName}:</span>
              <span style="font-weight: 600; margin-left: 16px;">${item.value}</span>
            </div>
          `
        })
        return result
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.categories,
      axisLine: {
        lineStyle: {
          color: '#E2E8F0'
        }
      },
      axisLabel: {
        color: '#64748B',
        fontSize: 12,
        fontFamily: 'Open Sans, sans-serif'
      },
      axisTick: {
        show: false
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '收入（元）',
        nameTextStyle: {
          color: '#64748B',
          fontSize: 12,
          fontFamily: 'Open Sans, sans-serif'
        },
        axisLine: {
          show: false
        },
        axisLabel: {
          color: '#64748B',
          fontSize: 12,
          formatter: '{value}'
        },
        splitLine: {
          lineStyle: {
            color: '#F1F5F9',
            type: 'dashed'
          }
        }
      },
      {
        type: 'value',
        name: '预订（次）',
        nameTextStyle: {
          color: '#64748B',
          fontSize: 12,
          fontFamily: 'Open Sans, sans-serif'
        },
        axisLine: {
          show: false
        },
        axisLabel: {
          color: '#64748B',
          fontSize: 12,
          formatter: '{value}'
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: '收入金额',
        type: 'bar',
        data: data.revenue,
        barWidth: '30%',
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#3B82F6' },
            { offset: 1, color: '#2563EB' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#60A5FA' },
              { offset: 1, color: '#3B82F6' }
            ])
          }
        }
      },
      {
        name: '预订次数',
        type: 'line',
        yAxisIndex: 1,
        data: data.bookings,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#10B981' },
            { offset: 1, color: '#059669' }
          ])
        },
        itemStyle: {
          color: '#10B981',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16, 185, 129, 0.3)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.05)' }
          ])
        }
      },
      {
        name: '单均价',
        type: 'line',
        data: data.avgPrice,
        smooth: true,
        symbol: 'diamond',
        symbolSize: 6,
        lineStyle: {
          width: 2,
          type: 'dashed',
          color: '#F59E0B'
        },
        itemStyle: {
          color: '#F59E0B'
        }
      }
    ],
    animationDuration: 1000,
    animationEasing: 'cubicOut'
  }
}

// 获取图表数据
const fetchChartData = async () => {
  loading.value = true
  try {
    let chartData = { categories: [], revenue: [], bookings: [], avgPrice: [] }

    if (timeRange.value === 'week') {
      const end = new Date()
      const start = new Date()
      start.setDate(end.getDate() - 6)

      const [financeRes, bookingRes] = await Promise.all([
        getFinanceTrend({ startDate: toDateStr(start), endDate: toDateStr(end) }),
        getBookingTrend({ period: 'week' })
      ])

      // finance: fill 7 days
      const dateToIncome = new Map()
      if (financeRes.code === 200 && financeRes.data) {
        const dates = Array.isArray(financeRes.data.dates) ? financeRes.data.dates : []
        const incomes = Array.isArray(financeRes.data.incomes) ? financeRes.data.incomes : []
        dates.forEach((d, i) => dateToIncome.set(d, Math.round(parseNum(incomes[i]))))
      }

      const categories = []
      const revenue = []
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        const ds = toDateStr(d)
        categories.push(weekdayLabel(ds))
        revenue.push(dateToIncome.get(ds) || 0)
      }

      const bookings = bookingRes.code === 200 && bookingRes.data && Array.isArray(bookingRes.data.values)
        ? bookingRes.data.values.map(v => Math.round(parseNum(v)))
        : new Array(revenue.length).fill(0)

      const avgPrice = revenue.map((r, i) => {
        const b = bookings[i] || 0
        return b > 0 ? Math.round(r / b) : 0
      })

      chartData = { categories, revenue, bookings, avgPrice }
    } else {
      const end = new Date()
      const start = new Date()
      start.setDate(end.getDate() - 27)

      const [financeRes, bookingRes] = await Promise.all([
        getFinanceTrend({ startDate: toDateStr(start), endDate: toDateStr(end) }),
        getBookingTrend({ period: 'month' })
      ])

      // finance: bucket 4 weeks
      const dates = financeRes.code === 200 && financeRes.data && Array.isArray(financeRes.data.dates) ? financeRes.data.dates : []
      const incomes = financeRes.code === 200 && financeRes.data && Array.isArray(financeRes.data.incomes) ? financeRes.data.incomes : []
      const daily = dates.map((_, i) => parseNum(incomes[i]))
      const revenueBuckets = [0, 0, 0, 0]
      daily.forEach((val, idx) => {
        const b = Math.min(3, Math.floor(idx / 7))
        revenueBuckets[b] += val
      })

      const categories = ['第1周', '第2周', '第3周', '第4周']
      const revenue = revenueBuckets.map(v => Math.round(v))
      const bookings = bookingRes.code === 200 && bookingRes.data && Array.isArray(bookingRes.data.values)
        ? bookingRes.data.values.map(v => Math.round(parseNum(v)))
        : new Array(revenue.length).fill(0)
      const avgPrice = revenue.map((r, i) => {
        const b = bookings[i] || 0
        return b > 0 ? Math.round(r / b) : 0
      })

      chartData = { categories, revenue, bookings, avgPrice }
    }

    // 更新图表
    if (chartInstance && chartData) {
      chartInstance.setOption(getChartOption(chartData))
    }
  } catch (error) {
    console.error('获取图表数据失败:', error)
    chartInstance?.setOption(getChartOption({ categories: [], revenue: [], bookings: [], avgPrice: [] }))
  } finally {
    loading.value = false
  }
}

// 时间范围切换
const handleTimeChange = () => {
  fetchChartData()
}

// 初始化图表
const initChart = async () => {
  await nextTick()
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    await fetchChartData()

    // 响应式
    window.addEventListener('resize', () => {
      chartInstance?.resize()
    })
  }
}

onMounted(() => {
  initChart()
})

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
.chart-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
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

.header-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chart-container {
  flex: 1;
  min-height: 350px;
  width: 100%;
}

.chart-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid #E2E8F0;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.legend-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #64748B;
  font-weight: 500;
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-container {
    min-height: 280px;
  }

  .chart-footer {
    gap: 16px;
  }
}
</style>
