export interface ParsedPagedList<T> {
  list: T[]
  total: number
}

/**
 * 兼容多种分页返回结构：
 * - Result 包裹：{ code, data: { data: [], total } }
 * - 直接返回：{ data: [], total }
 * - 直接返回：{ list: [], total }
 * - 直接返回数组：[]
 */
export function parsePagedList<T = any>(res: any): ParsedPagedList<T> {
  const root = res?.data ?? res

  // 直接数组
  if (Array.isArray(root)) {
    return { list: root as T[], total: root.length }
  }

  // root.data 可能是数组/对象
  const d = root?.data
  const l = root?.list

  const list = (Array.isArray(d) ? d : Array.isArray(l) ? l : Array.isArray(d?.data) ? d.data : Array.isArray(d?.list) ? d.list : []) as T[]

  const total = Number(
    res?.total ??
      root?.total ??
      d?.total ??
      d?.count ??
      root?.count ??
      list.length
  )

  return {
    list,
    total: Number.isFinite(total) ? total : list.length
  }
}
