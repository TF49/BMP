/**
 * useTheme Composable
 * 主题管理逻辑，负责主题切换、CSS 变量更新和持久化
 */

import { ref, watch } from 'vue'
import { TinyColor } from '@ctrl/tinycolor'
import { getTheme, getThemeList, defaultTheme, type Theme } from '@/config/themes'

const STORAGE_KEY = 'bmp-theme'

// 全局响应式主题状态
const currentThemeName = ref<string>(defaultTheme)
const currentTheme = ref<Theme>(getTheme(defaultTheme))

export function useTheme() {
  const setElColorVariants = (root: HTMLElement, baseVar: string, baseColor: string) => {
    // Element Plus 约定：light-3/5/7/8/9 为与白色混合后的浅色，dark-2 为与黑色混合后的深色
    // 参考：Element Plus 主题色变量命名习惯（保持视觉与内置主题一致）
    const base = new TinyColor(baseColor)
    if (!base.isValid) return

    const mixWithWhite = (amount: number) => base.mix('#ffffff', amount).toHexString()
    const mixWithBlack = (amount: number) => base.mix('#000000', amount).toHexString()

    root.style.setProperty(`${baseVar}-light-3`, mixWithWhite(30))
    root.style.setProperty(`${baseVar}-light-5`, mixWithWhite(50))
    root.style.setProperty(`${baseVar}-light-7`, mixWithWhite(70))
    root.style.setProperty(`${baseVar}-light-8`, mixWithWhite(80))
    root.style.setProperty(`${baseVar}-light-9`, mixWithWhite(90))
    root.style.setProperty(`${baseVar}-dark-2`, mixWithBlack(20))
  }

  /**
   * 应用主题到 DOM（更新 CSS 变量）
   */
  const applyTheme = (theme: Theme) => {
    const root = document.documentElement

    // 基础颜色
    root.style.setProperty('--color-primary', theme.primary)
    root.style.setProperty('--color-primary-light', theme.primaryLight)
    root.style.setProperty('--color-primary-dark', theme.primaryDark)
    root.style.setProperty('--color-secondary', theme.secondary)
    root.style.setProperty('--color-accent', theme.accent)
    root.style.setProperty('--color-success', theme.success)
    root.style.setProperty('--color-warning', theme.warning)
    root.style.setProperty('--color-danger', theme.danger)
    root.style.setProperty('--color-info', theme.info)

    // 同步 Element Plus 主题变量（否则多数组件看起来“没变色”）
    root.style.setProperty('--el-color-primary', theme.primary)
    root.style.setProperty('--el-color-success', theme.success)
    root.style.setProperty('--el-color-warning', theme.warning)
    root.style.setProperty('--el-color-danger', theme.danger)
    root.style.setProperty('--el-color-info', theme.info)

    // 衍生色：确保 hover/active/浅底等状态与主题一致
    setElColorVariants(root, '--el-color-primary', theme.primary)
    setElColorVariants(root, '--el-color-success', theme.success)
    setElColorVariants(root, '--el-color-warning', theme.warning)
    setElColorVariants(root, '--el-color-danger', theme.danger)
    setElColorVariants(root, '--el-color-info', theme.info)

    // 背景颜色
    root.style.setProperty('--color-background', theme.background)
    root.style.setProperty('--color-card-bg', theme.cardBg)
    root.style.setProperty('--color-card-bg-hover', theme.cardBgHover)

    // 文字颜色
    root.style.setProperty('--color-text-primary', theme.textPrimary)
    root.style.setProperty('--color-text-secondary', theme.textSecondary)
    root.style.setProperty('--color-text-muted', theme.textMuted)

    // 边框颜色
    root.style.setProperty('--color-border', theme.border)
    root.style.setProperty('--color-border-hover', theme.borderHover)

    // 阴影
    root.style.setProperty('--shadow', theme.shadow)
    root.style.setProperty('--shadow-hover', theme.shadowHover)

    // 统计卡片渐变
    root.style.setProperty('--gradient-members', theme.gradients.members)
    root.style.setProperty('--gradient-courts', theme.gradients.courts)
    root.style.setProperty('--gradient-bookings', theme.gradients.bookings)
    root.style.setProperty('--gradient-revenue', theme.gradients.revenue)

    // 更新 body 背景色
    document.body.style.backgroundColor = theme.background

    // 添加主题类名到 html 元素（用于特殊样式）
    root.className = `theme-${theme.name}`
  }

  /**
   * 切换主题
   */
  const setTheme = (themeName: string) => {
    const theme = getTheme(themeName)
    currentThemeName.value = themeName
    currentTheme.value = theme
    applyTheme(theme)

    // 保存到 localStorage
    try {
      localStorage.setItem(STORAGE_KEY, themeName)
    } catch (error) {
      console.warn('无法保存主题设置到 localStorage:', error)
    }
  }

  /**
   * 多标签页同步：监听 localStorage 变更
   */
  const handleStorageThemeSync = (event: StorageEvent) => {
    if (event.key !== STORAGE_KEY) return
    const next = event.newValue
    if (!next) return
    if (next === currentThemeName.value) return
    setTheme(next)
  }

  /**
   * 从 localStorage 加载主题
   */
  const loadTheme = () => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) {
        setTheme(saved)
      } else {
        // 首次访问，应用默认主题
        applyTheme(currentTheme.value)
      }
    } catch (error) {
      console.warn('无法从 localStorage 加载主题:', error)
      applyTheme(currentTheme.value)
    }

    // 监听跨 tab 的主题同步
    try {
      window.removeEventListener('storage', handleStorageThemeSync)
      window.addEventListener('storage', handleStorageThemeSync)
    } catch {
      // ignore
    }
  }

  /**
   * 获取所有可用主题
   */
  const themes = getThemeList()

  /**
   * 切换到下一个主题（用于快捷键等场景）
   */
  const toggleTheme = () => {
    const currentIndex = themes.findIndex(t => t.name === currentThemeName.value)
    const nextIndex = (currentIndex + 1) % themes.length
    setTheme(themes[nextIndex].name)
  }

  // 监听主题变化（用于调试）
  watch(currentThemeName, (newTheme) => {
    console.log(`[Theme] 主题已切换为: ${newTheme}`)
  })

  return {
    currentThemeName,
    currentTheme,
    themes,
    setTheme,
    loadTheme,
    toggleTheme,
    applyTheme
  }
}
