<template>
  <div class="glass-card chart-card peak-hours-card">
    <div class="chart-header">
      <div class="header-left">
        <span class="chart-title">🔥 预订热力时段</span>
        <span class="chart-subtitle">各时段场地预订热度分布</span>
      </div>
      <el-segmented v-model="viewType" :options="viewOptions" @change="handleViewChange" />
    </div>
    <div ref="chartRef" class="chart-container" v-loading="loading"></div>
    <div class="heatmap-legend">
      <span class="legend-label">冷门</span>
      <div class="legend-gradient"></div>
      <span class="legend-label">热门</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getBookingHeatmap } from '@/api/booking'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)
const viewType = ref('week')

const viewOptions = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' }
]

// 时段标签
const hours = [
  '08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00',
  '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00'
]

// 星期标签
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 图表配置
const getChartOption = (data) => {
  return {
    tooltip: {
      position: 'top',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E2E8F0',
      borderWidth: 1,
      textStyle: {
        color: '#1E293B'
      },
      padding: [12, 16],
      formatter: (params) => {
        const day = weekDays[params.value[1]]
        const hour = hours[params.value[0]]
        const count = params.value[2]
        return `
          <div style="font-weight: 600; margin-bottom: 8px;">${day} ${hour}</div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="display: inline-block; width: 10px; height: 10px; border-radius: 2px; background: ${params.color};"></span>
            <span>预订次数: <strong>${count}</strong> 次</span>
          </div>
        `
      }
    },
    grid: {
      left: '8%',
      right: '10%',
      top: '10%',
      bottom: '15%'
    },
    xAxis: {
      type: 'category',
      data: hours,
      splitArea: {
        show: true
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#64748B',
        fontSize: 11,
        fontFamily: 'Open Sans, sans-serif',
        interval: 1,
        rotate: 45
      }
    },
    yAxis: {
      type: 'category',
      data: weekDays,
      splitArea: {
        show: true
      },
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#64748B',
        fontSize: 12,
        fontFamily: 'Open Sans, sans-serif'
      }
    },
    visualMap: {
      min: 0,
      max: 20,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: '0%',
      show: false,
      inRange: {
        color: ['#E0F2FE', '#7DD3FC', '#38BDF8', '#0EA5E9', '#0284C7', '#0369A1', '#075985']
      }
    },
    series: [{
      name: '预订热度',
      type: 'heatmap',
      data: data,
      label: {
        show: false
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      itemStyle: {
        borderColor: '#fff',
        borderWidth: 2,
        borderRadius: 4
      }
    }],
    animation: true,
    animationDuration: 1000,
    animationEasing: 'cubicOut'
  }
}

// 获取图表数据（真实 API：heatmapData 中 day=0-6 周一至周日，hour=8-21，转为 [hourIndex, dayIndex, count]）
const fetchChartData = async () => {
  loading.value = true
  try {
    let heatmapData = []
    const res = await getBookingHeatmap({ period: viewType.value })
    if (res.code === 200 && res.data && Array.isArray(res.data.heatmapData)) {
      heatmapData = res.data.heatmapData.map(item => {
        const hour = item.hour != null ? Number(item.hour) : 8
        const day = item.day != null ? Number(item.day) : 0
        const count = item.count != null ? Number(item.count) : 0
        const hourIndex = Math.max(0, Math.min(13, hour - 8))
        return [hourIndex, day, count]
      })
    }
    if (chartInstance) {
      chartInstance.setOption(getChartOption(heatmapData))
    }
  } catch (error) {
    console.error('获取热力图数据失败:', error)
    if (chartInstance) {
      chartInstance.setOption(getChartOption([]))
    }
  } finally {
    loading.value = false
  }
}

// 视图切换
const handleViewChange = () => {
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
.peak-hours-card {
  height: 100%;
  display: flex;
  flex-direction: column;
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
  min-height: 320px;
  width: 100%;
}

.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E2E8F0;
}

.legend-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #64748B;
}

.legend-gradient {
  width: 120px;
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #E0F2FE 0%, #38BDF8 50%, #075985 100%);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-container {
    min-height: 280px;
  }
}
</style>
