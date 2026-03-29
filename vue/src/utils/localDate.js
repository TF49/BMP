/**
 * 本地日历日期 yyyy-MM-dd。
 * 避免使用 Date#toISOString() 取日期（UTC），在东八区等会在夜间与「本地今天」差一天。
 */
export function formatLocalDate(date = new Date()) {
  const d = date instanceof Date ? date : new Date(date)
  if (Number.isNaN(d.getTime())) return formatLocalDate(new Date())
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** Date 格式化为本地 yyyy-MM-dd（与 formatLocalDate 相同，便于语义化命名） */
export function dateToLocalYmd(d) {
  return formatLocalDate(d)
}
