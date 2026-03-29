/** @type {Map<string, Promise<any>>} */
const pending = new Map()

/**
 * 同一 key 并发只发起一次请求，多个调用方共享同一 Promise。
 * 用于 Dashboard 多图表同时挂载时的只读 GET 去重。
 */
export function withDedupe(key, fetcher) {
  if (pending.has(key)) {
    return pending.get(key)
  }
  const p = Promise.resolve()
    .then(() => fetcher())
    .finally(() => {
      pending.delete(key)
    })
  pending.set(key, p)
  return p
}
