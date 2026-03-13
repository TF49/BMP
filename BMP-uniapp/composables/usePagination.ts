/**
 * 分页加载工具
 * 用于列表页面的分页加载和缓存管理
 */

import { ref, computed } from 'vue'

export interface PaginationOptions {
  pageSize?: number
  cacheKey?: string
  cacheDuration?: number // 缓存时长（毫秒）
}

export interface PaginationState {
  page: number
  pageSize: number
  total: number
  hasMore: boolean
  loading: boolean
  refreshing: boolean
}

/**
 * 分页加载 Composable
 */
export function usePagination(
  fetchFn: (page: number, pageSize: number) => Promise<any[]>,
  options: PaginationOptions = {}
) {
  const {
    pageSize = 10,
    cacheKey = '',
    cacheDuration = 5 * 60 * 1000 // 默认5分钟
  } = options

  const state = ref<PaginationState>({
    page: 1,
    pageSize,
    total: 0,
    hasMore: true,
    loading: false,
    refreshing: false
  })

  const items = ref<any[]>([])
  const cacheTime = ref<number>(0)

  // 检查缓存是否有效
  const isCacheValid = computed(() => {
    if (!cacheKey) return false
    const now = Date.now()
    return now - cacheTime.value < cacheDuration
  })

  // 加载数据
  const loadData = async (isRefresh = false) => {
    if (isRefresh) {
      state.value.refreshing = true
      state.value.page = 1
      items.value = []
    } else {
      if (!state.value.hasMore || state.value.loading) return
      state.value.loading = true
    }

    try {
      const data = await fetchFn(state.value.page, state.value.pageSize)

      if (isRefresh) {
        items.value = data
      } else {
        items.value.push(...data)
      }

      // 更新分页状态
      state.value.total = data.length
      state.value.hasMore = data.length === state.value.pageSize
      state.value.page++

      // 更新缓存时间
      if (cacheKey) {
        cacheTime.value = Date.now()
      }
    } catch (error) {
      console.error('加载数据失败:', error)
      throw error
    } finally {
      state.value.loading = false
      state.value.refreshing = false
    }
  }

  // 加载更多
  const loadMore = () => {
    if (state.value.hasMore && !state.value.loading) {
      loadData(false)
    }
  }

  // 刷新数据
  const refresh = () => {
    loadData(true)
  }

  // 重置分页
  const reset = () => {
    state.value.page = 1
    state.value.hasMore = true
    items.value = []
    cacheTime.value = 0
  }

  return {
    items,
    state,
    isCacheValid,
    loadData,
    loadMore,
    refresh,
    reset
  }
}

/**
 * 数据缓存管理
 */
export function useDataCache(key: string, duration = 5 * 60 * 1000) {
  const cacheKey = `cache_${key}`
  const timeKey = `cache_time_${key}`

  const setCache = (data: any) => {
    try {
      uni.setStorageSync(cacheKey, JSON.stringify(data))
      uni.setStorageSync(timeKey, Date.now().toString())
    } catch (error) {
      console.error('缓存设置失败:', error)
    }
  }

  const getCache = () => {
    try {
      const data = uni.getStorageSync(cacheKey)
      const time = uni.getStorageSync(timeKey)

      if (!data || !time) return null

      const now = Date.now()
      const cacheTime = parseInt(time)

      if (now - cacheTime > duration) {
        removeCache()
        return null
      }

      return JSON.parse(data)
    } catch (error) {
      console.error('缓存获取失败:', error)
      return null
    }
  }

  const removeCache = () => {
    try {
      uni.removeStorageSync(cacheKey)
      uni.removeStorageSync(timeKey)
    } catch (error) {
      console.error('缓存删除失败:', error)
    }
  }

  return {
    setCache,
    getCache,
    removeCache
  }
}
