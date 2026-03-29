<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon course-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="14" rx="2" />
            <path d="M7 8h10M7 12h6" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">课程热度排行榜</h3>
          <span class="chart-subtitle">按报名人数排序的热门课程</span>
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
import { getCourseStatistics } from '@/api/course'
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
  info: getCSSVar('--color-info', '#06B6D4'),
  textPrimary: getCSSVar('--color-text-primary', '#1E293B'),
  textSecondary: getCSSVar('--color-text-secondary', '#64748B'),
  border: getCSSVar('--color-border', '#E2E8F0'),
  background: getCSSVar('--color-background', '#F8FAFC')
}))

const courseData = ref([])

// 获取课程统计数据
const fetchCourseData = async () => {
  loading.value = true
  try {
    const res = await getCourseStatistics()
    if (res.code === 200 && res.data) {
      const rawList = res.data.hotCourses || []
      // 按课程名称聚合：同一名称的多条（如后端未按名称汇总时）合并为一条，与课程管理维度一致
      const byName = new Map()
      for (const item of rawList) {
        const name = item.courseName || item.name || '未命名课程'
        const signup = Number(item.signupCount) ?? Number(item.bookingCount) ?? 0
        if (signup <= 0) continue
        const rate = item.attendanceRate != null ? Number(item.attendanceRate) : 0
        if (byName.has(name)) {
          const prev = byName.get(name)
          const totalSignup = prev.signupCount + signup
          prev.attendanceRate = (prev.attendanceRate * prev.signupCount + rate * signup) / totalSignup
          prev.signupCount = totalSignup
        } else {
          byName.set(name, { name, signupCount: signup, attendanceRate: rate })
        }
      }
      courseData.value = [...byName.values()]
        .sort((a, b) => b.signupCount - a.signupCount)
        .slice(0, 5)
    } else {
      courseData.value = []
    }
  } catch (error) {
    console.error('获取课程热度数据失败:', error)
    courseData.value = []
  } finally {
    loading.value = false
  }
}

const getChartOption = () => {
  const colors = themeColors.value
  const categories = courseData.value.map(item => item.name)
  const values = courseData.value.map(item => item.signupCount)
  const hasData = categories.length > 0

  return {
    // 有数据时显式清空 graphic，避免上次的「暂无数据」文案残留在图表上
    graphic: hasData ? [] : [{
      type: 'text',
      left: 'center',
      top: 'middle',
      style: { text: '暂无数据', fill: colors.textSecondary, fontSize: 14 }
    }],
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
        const data = params[0]
        const course = courseData.value[data.dataIndex] || {}
        const rate = (course.attendanceRate ?? 0) * 100
        return `
          <div style="margin-bottom:6px;font-weight:600;color:${colors.textPrimary};">${course.name || '—'}</div>
          <div style="font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>报名人数</span>
            <span style="font-weight:600;color:${colors.textPrimary};">${course.signupCount ?? 0} 人</span>
          </div>
          <div style="margin-top:4px;font-size:12px;color:${colors.textSecondary};display:flex;justify-content:space-between;gap:12px;">
            <span>平均出勤率</span>
            <span style="font-weight:600;color:${colors.info};">${rate.toFixed(1)}%</span>
          </div>
        `
      }
    },
    grid: {
      left: '2%',
      right: '2%',
      bottom: '2%',
      top: '4%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
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
        fontSize: 12
      }
    },
    yAxis: {
      type: 'category',
      data: categories,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: colors.textSecondary,
        fontSize: 13
      }
    },
    series: [
      {
        name: '报名人数',
        type: 'bar',
        data: values,
        barWidth: 12,
        itemStyle: {
          borderRadius: [0, 8, 8, 0],
          color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
            { offset: 0, color: colors.primary },
            { offset: 1, color: colors.info }
          ]),
          shadowBlur: 6,
          shadowColor: colors.primary + '40',
          shadowOffsetY: 2
        },
        label: {
          show: true,
          position: 'right',
          color: colors.textSecondary,
          fontSize: 12,
          formatter: val => `${val.value}人`
        },
        animationDuration: 1000,
        animationEasing: 'cubicOut'
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
    const option = getChartOption()
    // 有数据时替换 graphic，避免「暂无数据」残留
    const hasData = (option.series?.[0]?.data?.length ?? 0) > 0
    chartInstance.setOption(option, hasData ? { replaceMerge: ['graphic'] } : undefined)
  }
}

onMounted(() => {
  initChart()
  fetchCourseData().then(() => updateChart())
  window.addEventListener('resize', resizeChart)
})

useDashboardChartRefresh(() => fetchCourseData().then(() => updateChart()))

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
  padding: 20px 24px;
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.chart-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.chart-icon svg {
  width: 22px;
  height: 22px;
}

.course-icon {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-info) 100%);
  box-shadow: 0 4px 12px color-mix(in srgb, var(--color-primary) 30%, transparent);
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
  padding-top: 4px;
}

.chart-container {
  width: 100%;
  height: 240px;
}

@media (max-width: 768px) {
  .chart-container {
    height: 220px;
  }
}
</style>

