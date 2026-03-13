<template>
  <div class="glass-card chart-card member-funnel-card">
    <div class="chart-header">
      <div class="header-left">
        <span class="chart-title">📈 会员活跃漏斗</span>
        <span class="chart-subtitle">会员活跃度转化分析</span>
      </div>
      <el-link type="primary" :underline="false" class="view-all" @click="navigateTo('/member/management')">
        查看详情
      </el-link>
    </div>
    <div ref="chartRef" class="chart-container" v-loading="loading"></div>
    <div class="funnel-stats">
      <div class="funnel-stat-item" v-for="(item, index) in funnelStats" :key="index">
        <div class="stat-color" :style="{ background: item.color }"></div>
        <div class="stat-content">
          <span class="stat-name">{{ item.name }}</span>
          <span class="stat-value">{{ item.value }} 人</span>
        </div>
        <span class="stat-rate" v-if="item.rate">{{ item.rate }}%</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getMemberFunnel } from '@/api/member'

const router = useRouter()
const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)
const funnelStats = ref([])

const navigateTo = (path) => {
  router.push(path)
}

// 颜色配置
const colorScheme = [
  { start: '#8B5CF6', end: '#7C3AED' },
  { start: '#3B82F6', end: '#2563EB' },
  { start: '#10B981', end: '#059669' },
  { start: '#F59E0B', end: '#D97706' }
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
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${params.color};"></span>
            <span>会员数: <strong>${params.value}</strong> 人</span>
          </div>
          <div style="margin-top: 4px; color: #64748B; font-size: 12px;">
            占比: ${params.percent}%
          </div>
        `
      }
    },
    series: [
      {
        name: '会员漏斗',
        type: 'funnel',
        left: '10%',
        top: 60,
        bottom: 60,
        width: '80%',
        min: 0,
        max: 100,
        minSize: '0%',
        maxSize: '100%',
        sort: 'descending',
        gap: 4,
        label: {
          show: true,
          position: 'inside',
          formatter: '{b}\n{c} 人',
          fontSize: 13,
          fontWeight: 600,
          fontFamily: 'Poppins, sans-serif',
          color: '#fff',
          lineHeight: 20
        },
        labelLine: {
          show: false
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2,
          borderRadius: 6
        },
        emphasis: {
          label: {
            fontSize: 15
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
        data: data.map((item, index) => ({
          value: item.value,
          name: item.name,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: colorScheme[index % colorScheme.length].start },
              { offset: 1, color: colorScheme[index % colorScheme.length].end }
            ])
          }
        }))
      }
    ],
    animation: true,
    animationDuration: 1000,
    animationEasing: 'cubicOut'
  }
}

// 获取图表数据（真实 API：/api/member/funnel 返回 [{ name, value, rate }]）
const fetchChartData = async () => {
  loading.value = true
  try {
    let funnelData = []
    const res = await getMemberFunnel()
    if (res.code === 200 && Array.isArray(res.data)) {
      funnelData = res.data.map(item => ({
        name: item.name || '',
        value: item.value != null ? Number(item.value) : 0,
        rate: item.rate != null ? Number(item.rate) : 0
      }))
    }
    if (funnelData.length === 0) {
      funnelData = [
        { name: '注册会员', value: 0, rate: 100 },
        { name: '活跃会员', value: 0, rate: 0 },
        { name: '高频会员', value: 0, rate: 0 },
        { name: 'VIP会员', value: 0, rate: 0 }
      ]
    }

    funnelStats.value = funnelData.map((item, index) => ({
      name: item.name,
      value: item.value,
      rate: index > 0 ? item.rate : null,
      color: `linear-gradient(135deg, ${colorScheme[index % colorScheme.length].start} 0%, ${colorScheme[index % colorScheme.length].end} 100%)`
    }))

    if (chartInstance) {
      chartInstance.setOption(getChartOption(funnelData))
    }
  } catch (error) {
    console.error('获取会员漏斗数据失败:', error)
    funnelStats.value = []
    if (chartInstance) {
      chartInstance.setOption(getChartOption([]))
    }
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
.member-funnel-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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
  min-height: 280px;
  width: 100%;
}

.funnel-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #E2E8F0;
}

.funnel-stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.funnel-stat-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: rgba(99, 102, 241, 0.3);
}

.stat-color {
  width: 8px;
  height: 36px;
  border-radius: 4px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.stat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.stat-name {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #64748B;
}

.stat-value {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
}

.stat-rate {
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #10B981;
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .funnel-stats {
    grid-template-columns: 1fr;
  }

  .chart-container {
    min-height: 240px;
  }
}
</style>
