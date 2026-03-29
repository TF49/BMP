<template>
  <div class="glass-card chart-card member-growth-card">
    <div class="chart-header">
      <div class="header-left">
        <span class="chart-title">👥 会员增长趋势</span>
        <span class="chart-subtitle">累计会员数与新增会员分析</span>
      </div>
      <div class="header-right">
        <el-segmented v-model="timeRange" :options="timeOptions" @change="handleTimeChange" />
      </div>
    </div>
    <div ref="chartRef" class="chart-container" v-loading="loading"></div>
    <div class="summary-cards">
      <div class="summary-card">
        <div class="summary-icon" style="background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);">
          <el-icon :size="20"><User /></el-icon>
        </div>
        <div class="summary-info">
          <div class="summary-value">{{ summaryData.totalMembers }}</div>
          <div class="summary-label">累计会员</div>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon" style="background: linear-gradient(135deg, #10B981 0%, #059669 100%);">
          <el-icon :size="20"><Plus /></el-icon>
        </div>
        <div class="summary-info">
          <div class="summary-value">+{{ summaryData.newMembers }}</div>
          <div class="summary-label">本{{ timeRange === 'week' ? '周' : '月' }}新增</div>
        </div>
      </div>
      <div class="summary-card">
        <div class="summary-icon" style="background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);">
          <el-icon :size="20"><TrendCharts /></el-icon>
        </div>
        <div class="summary-info">
          <div class="summary-value">{{ summaryData.growthRate }}%</div>
          <div class="summary-label">增长率</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { User, Plus, TrendCharts } from '@element-plus/icons-vue'
import { getMemberStatistics } from '@/api/member'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)
const timeRange = ref('week')

const timeOptions = [
  { label: '最近7天', value: 'week' },
  { label: '最近30天', value: 'month' }
]

const summaryData = ref({
  totalMembers: 0,
  newMembers: 0,
  growthRate: 0
})

// 图表配置
const getChartOption = (data) => {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
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
              <span style="font-weight: 600; margin-left: 16px;">${item.value} 人</span>
            </div>
          `
        })
        return result
      }
    },
    legend: {
      data: ['累计会员', '新增会员'],
      bottom: 0,
      textStyle: {
        color: '#64748B',
        fontFamily: 'Open Sans, sans-serif',
        fontSize: 12
      },
      itemWidth: 20,
      itemHeight: 12,
      itemGap: 24
    },
    grid: {
      left: '3%',
      right: '5%',
      bottom: '12%',
      top: '12%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: true,
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
        name: '累计会员',
        nameTextStyle: {
          color: '#8B5CF6',
          fontSize: 12,
          fontFamily: 'Open Sans, sans-serif'
        },
        position: 'left',
        axisLine: {
          show: true,
          lineStyle: {
            color: '#8B5CF6'
          }
        },
        axisLabel: {
          color: '#64748B',
          fontSize: 12
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
        name: '新增会员',
        nameTextStyle: {
          color: '#10B981',
          fontSize: 12,
          fontFamily: 'Open Sans, sans-serif'
        },
        position: 'right',
        min: 0,
        axisLine: {
          show: true,
          lineStyle: {
            color: '#10B981'
          }
        },
        axisLabel: {
          color: '#64748B',
          fontSize: 12
        },
        splitLine: {
          show: false
        }
      }
    ],
    series: [
      {
        name: '累计会员',
        type: 'line',
        yAxisIndex: 0,
        data: data.totalMembers,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {
          width: 3,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#8B5CF6' },
            { offset: 1, color: '#7C3AED' }
          ])
        },
        itemStyle: {
          color: '#8B5CF6',
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(139, 92, 246, 0.3)' },
            { offset: 1, color: 'rgba(139, 92, 246, 0.05)' }
          ])
        }
      },
      {
        name: '新增会员',
        type: 'bar',
        yAxisIndex: 1,
        data: data.newMembers,
        barWidth: '35%',
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#10B981' },
            { offset: 1, color: '#059669' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#34D399' },
              { offset: 1, color: '#10B981' }
            ])
          }
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
    const res = await getMemberStatistics({ period: timeRange.value })
    const trends = res?.code === 200 && res.data ? res.data.trends : null
    const chartData = trends && Array.isArray(trends.categories)
      ? {
          categories: trends.categories || [],
          totalMembers: trends.totalMembers || [],
          newMembers: trends.newMembers || []
        }
      : { categories: [], totalMembers: [], newMembers: [] }

    summaryData.value = {
      totalMembers: res?.code === 200 && res.data ? (res.data.totalMembers ?? res.data.total ?? 0) : 0,
      newMembers: res?.code === 200 && res.data ? (res.data.newThisMonth ?? res.data.newThisWeek ?? 0) : 0,
      growthRate: res?.code === 200 && res.data ? (Number(res.data.growthRate || 0).toFixed(1)) : 0
    }

    // 更新图表
    if (chartInstance) {
      chartInstance.setOption(getChartOption(chartData))
    }
  } catch (error) {
    console.error('获取会员增长数据失败:', error)
    summaryData.value = { totalMembers: 0, newMembers: 0, growthRate: 0 }
    chartInstance?.setOption(getChartOption({ categories: [], totalMembers: [], newMembers: [] }))
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

useDashboardChartRefresh(() => fetchChartData())

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
.member-growth-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: rgba(99, 102, 241, 0.3);
}

.summary-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.summary-info {
  flex: 1;
  min-width: 0;
}

.summary-value {
  font-family: 'Poppins', sans-serif;
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  line-height: 1.2;
  margin-bottom: 2px;
}

.summary-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #64748B;
}

@media (max-width: 1024px) {
  .summary-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-container {
    min-height: 250px;
  }

  .summary-card {
    padding: 14px;
  }

  .summary-icon {
    width: 42px;
    height: 42px;
  }

  .summary-value {
    font-size: 20px;
  }
}
</style>
