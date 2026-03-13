/**
 * 会长端专用状态
 * - 当前页面路径（供自定义 TabBar 高亮）
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const usePresidentStore = defineStore('president', () => {
  const currentPath = ref('')

  function setCurrentPath(path: string) {
    currentPath.value = path
  }

  return {
    currentPath,
    setCurrentPath
  }
})
