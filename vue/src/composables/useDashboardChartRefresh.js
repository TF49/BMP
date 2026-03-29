import { onMounted, onUnmounted } from 'vue'

export const DASHBOARD_CHARTS_REFRESH = 'bmp-dashboard-charts-refresh'

/**
 * 监听 Dashboard 全局刷新（与 KPI「刷新」、`bmp-dashboard-refresh` 联动时由父页派发）。
 * @param {() => void | Promise<void>} fetchFn 重新拉取图表数据
 */
export function useDashboardChartRefresh(fetchFn) {
  const handler = () => {
    try {
      const r = fetchFn()
      if (r && typeof r.then === 'function') {
        r.catch(() => {})
      }
    } catch (_) {
      /* ignore */
    }
  }
  onMounted(() => {
    window.addEventListener(DASHBOARD_CHARTS_REFRESH, handler)
  })
  onUnmounted(() => {
    window.removeEventListener(DASHBOARD_CHARTS_REFRESH, handler)
  })
}
