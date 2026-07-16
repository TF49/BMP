/**
 * ECharts 按需导入模块
 *
 * 只注册项目中实际用到的图表类型和组件，避免全量导入（896KB -> ~300KB）。
 * 所有图表组件请从此模块导入 echarts，而非直接 import * as echarts from '@/utils/echarts'。
 *
 * 若新增图表类型或用到新的 ECharts 组件，在此文件中追加对应注册。
 */

import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'

// 图表类型
import { BarChart } from 'echarts/charts'
import { LineChart } from 'echarts/charts'
import { PieChart } from 'echarts/charts'
import { FunnelChart } from 'echarts/charts'
import { HeatmapChart } from 'echarts/charts'
import { GaugeChart } from 'echarts/charts'
import { RadarChart } from 'echarts/charts'

// ECharts 组件
import {
  GridComponent,
  TooltipComponent,
  TitleComponent,
  LegendComponent,
  VisualMapComponent,
  RadarComponent,
  GraphicComponent,
  MarkLineComponent,
  MarkPointComponent,
  MarkAreaComponent,
  DataZoomComponent,
  AxisPointerComponent,
  CalendarComponent,
  TimelineComponent,
} from 'echarts/components'

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  FunnelChart,
  HeatmapChart,
  GaugeChart,
  RadarChart,
  GridComponent,
  TooltipComponent,
  TitleComponent,
  LegendComponent,
  VisualMapComponent,
  RadarComponent,
  GraphicComponent,
  MarkLineComponent,
  MarkPointComponent,
  MarkAreaComponent,
  DataZoomComponent,
  AxisPointerComponent,
  CalendarComponent,
  TimelineComponent,
])

export * from 'echarts/core'
export { use } from 'echarts/core'
