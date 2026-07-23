<template>
  <div class="agent-chart">
    <div v-if="isEmpty" class="agent-chart__empty">
      <el-empty :description="emptyText" :image-size="80" />
    </div>
    <div v-else ref="chartRef" class="agent-chart__canvas"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from '@/utils/echarts'

const props = defineProps({
  // 受控图表结构：{ chartType, title, categories[], yCategories[], series[], empty, emptyText }
  chartData: {
    type: Object,
    required: true
  }
})

const chartRef = ref(null)
let chartInstance = null
let resizeObserver = null

const isEmpty = computed(() => {
  const d = props.chartData || {}
  return d.empty === true || !Array.isArray(d.series) || d.series.length === 0
})

const emptyText = computed(() => props.chartData?.emptyText || '当前暂无相关经营数据')

/** 将受控结构映射为 ECharts option。前端不生成任意脚本，仅消费后端数据。 */
function buildOption(data) {
  const type = (data.chartType || 'bar').toLowerCase()
  const categories = Array.isArray(data.categories) ? data.categories : []
  const series = Array.isArray(data.series) ? data.series : []
  const baseTitle = data.title
    ? { text: data.title, left: 'center', textStyle: { fontSize: 14 } }
    : undefined

  if (type === 'pie') {
    const first = series[0] || { data: [] }
    const pieData = categories.length
      ? categories.map((name, i) => ({ name, value: first.data?.[i] ?? 0 }))
      : (first.data || []).map((v, i) => ({ name: `项${i + 1}`, value: v }))
    return {
      title: baseTitle,
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [
        {
          name: first.name || data.title || '构成',
          type: 'pie',
          radius: ['40%', '68%'],
          center: ['50%', '46%'],
          data: pieData,
          label: { formatter: '{b}\n{d}%' }
        }
      ]
    }
  }

  if (type === 'heatmap') {
    const yCategories = Array.isArray(data.yCategories) ? data.yCategories : []
    const first = series[0] || { data: [] }
    const points = Array.isArray(first.data) ? first.data : []
    const values = points
      .map((p) => (Array.isArray(p) ? Number(p[2]) : Number(p)))
      .filter((v) => !Number.isNaN(v))
    const maxVal = values.length ? Math.max(...values) : 1
    return {
      title: baseTitle,
      tooltip: { position: 'top' },
      grid: { top: baseTitle ? 40 : 20, bottom: 60, left: 70, right: 20 },
      xAxis: { type: 'category', data: categories, splitArea: { show: true } },
      yAxis: { type: 'category', data: yCategories, splitArea: { show: true } },
      visualMap: {
        min: 0,
        max: maxVal,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: 0
      },
      series: [
        {
          name: first.name || '利用率',
          type: 'heatmap',
          data: points,
          emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.4)' } }
        }
      ]
    }
  }

  // line / bar（未知类型退化为 bar）
  const echartType = type === 'line' ? 'line' : 'bar'
  return {
    title: baseTitle,
    tooltip: { trigger: 'axis' },
    legend: series.length > 1 ? { bottom: 0, type: 'scroll' } : undefined,
    grid: { top: baseTitle ? 48 : 24, bottom: series.length > 1 ? 48 : 32, left: 56, right: 24 },
    xAxis: { type: 'category', data: categories, boundaryGap: echartType === 'bar' },
    yAxis: { type: 'value' },
    series: series.map((s) => ({
      name: s.name,
      type: echartType,
      smooth: echartType === 'line',
      data: Array.isArray(s.data) ? s.data : []
    }))
  }
}

function renderChart() {
  if (isEmpty.value || !chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(buildOption(props.chartData), true)
  chartInstance.resize()
}

onMounted(async () => {
  await nextTick()
  renderChart()
  if (chartRef.value && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => {
      chartInstance && chartInstance.resize()
    })
    resizeObserver.observe(chartRef.value)
  }
})

watch(
  () => props.chartData,
  async () => {
    await nextTick()
    renderChart()
  },
  { deep: true }
)

onBeforeUnmount(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.agent-chart {
  width: 100%;
  margin: 12px 0 4px;
}

.agent-chart__canvas {
  width: 100%;
  height: 320px;
}

.agent-chart__empty {
  display: flex;
  justify-content: center;
  padding: 12px 0;
}
</style>
