<template>
  <div class="glass-card chart-card equipment-rental-card">
    <div class="chart-header">
      <div class="header-left">
        <span class="chart-title">🏸 器材租借统计</span>
        <span class="chart-subtitle">各类器材租借占比分析</span>
      </div>
      <el-link type="primary" :underline="false" class="view-all" @click="navigateTo('/equipment/rental')">
        查看详情
      </el-link>
    </div>
    <div class="chart-body">
      <div ref="chartRef" class="chart-container" v-loading="loading"></div>
      <div class="stats-panel">
        <div class="stat-item" v-for="(item, index) in statsData" :key="index">
          <div class="stat-color" :style="{ background: item.color }"></div>
          <div class="stat-info">
            <div class="stat-name">{{ item.name }}</div>
            <div class="stat-value">{{ item.value }} 次</div>
          </div>
          <div class="stat-percentage">{{ item.percentage }}%</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getEquipmentRentalStatistics } from '@/api/equipmentRental'

const router = useRouter()
const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)
const statsData = ref([])

const navigateTo = (path) => {
  router.push(path)
}

// 颜色方案
const colorScheme = [
  { start: '#667eea', end: '#764ba2' },
  { start: '#f093fb', end: '#f5576c' },
  { start: '#4facfe', end: '#00f2fe' },
  { start: '#43e97b', end: '#38f9d7' },
  { start: '#fa709a', end: '#fee140' },
  { start: '#a18cd1', end: '#fbc2eb' }
]

// 图表配置
const getChartOption = (data) => {
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#E2E8F0',
      borderWidth: 1,
      textStyle: {
        color: '#1E293B'
      },
      padding: [12, 16],
      formatter: (params) => {
        return `
          <div style="font-weight: 600; margin-bottom: 8px;">${params.name}</div>
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 24px;">
            <span>租借次数:</span>
            <span style="font-weight: 600; color: ${params.color};">${params.value} 次</span>
          </div>
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 24px; margin-top: 4px;">
            <span>占比:</span>
            <span style="font-weight: 600; color: ${params.color};">${params.percent}%</span>
          </div>
        `
      }
    },
    legend: {
      show: false
    },
    series: [
      {
        name: '器材租借',
        type: 'pie',
        radius: ['45%', '75%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            fontFamily: 'Poppins, sans-serif'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: {
          show: false
        },
        data: data.map((item, index) => ({
          value: item.value,
          name: item.name,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 1, [
              { offset: 0, color: colorScheme[index % colorScheme.length].start },
              { offset: 1, color: colorScheme[index % colorScheme.length].end }
            ])
          }
        })),
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: (idx) => idx * 100
      }
    ]
  }
}

// 获取图表数据
const fetchChartData = async () => {
  loading.value = true
  try {
    let chartData = []
    const res = await getEquipmentRentalStatistics()
    if (res.code === 200 && res.data && Array.isArray(res.data.categoryStats)) {
      chartData = res.data.categoryStats.map(item => ({
        name: item.category || item.name,
        value: item.count || item.value || 0,
        percentage: item.percentage || 0
      }))
    }

    // 计算总数和百分比
    const total = chartData.reduce((sum, item) => sum + item.value, 0)
    statsData.value = chartData.map((item, index) => ({
      name: item.name,
      value: item.value,
      percentage: total > 0 ? ((item.value / total) * 100).toFixed(1) : 0,
      color: `linear-gradient(135deg, ${colorScheme[index % colorScheme.length].start} 0%, ${colorScheme[index % colorScheme.length].end} 100%)`
    }))

    // 更新图表
    if (chartInstance) {
      chartInstance.setOption(getChartOption(chartData))
    }
  } catch (error) {
    console.error('获取器材租借数据失败:', error)
    statsData.value = []
    if (chartInstance) chartInstance.setOption(getChartOption([]))
  } finally {
    loading.value = false
  }
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
.equipment-rental-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.chart-body {
  flex: 1;
  display: flex;
  gap: 24px;
  align-items: center;
}

.chart-container {
  flex: 1;
  min-height: 300px;
  max-height: 350px;
}

.stats-panel {
  flex-shrink: 0;
  width: 180px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(99, 102, 241, 0.3);
}

.stat-color {
  width: 8px;
  height: 40px;
  border-radius: 4px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-name {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #1E293B;
  font-weight: 600;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value {
  font-family: 'Poppins', sans-serif;
  font-size: 12px;
  color: #64748B;
}

.stat-percentage {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-primary);
  flex-shrink: 0;
}

@media (max-width: 1024px) {
  .chart-body {
    flex-direction: column;
  }

  .stats-panel {
    width: 100%;
  }

  .chart-container {
    min-height: 250px;
  }
}

@media (max-width: 768px) {
  .chart-container {
    min-height: 220px;
  }

  .stats-panel {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .stat-item {
    padding: 10px;
  }
}

@media (max-width: 480px) {
  .stats-panel {
    grid-template-columns: 1fr;
  }
}
</style>
