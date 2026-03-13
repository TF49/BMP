<template>
  <div class="theme-selector">
    <el-dropdown trigger="click" placement="bottom-end" @command="handleThemeCommand">
      <!-- 触发按钮 -->
      <!-- 阻止本次点击继续冒泡到 document，避免“打开即关闭” -->
      <div class="theme-trigger" @click.stop>
        <div class="theme-icon">
          <span class="theme-emoji">{{ currentTheme.icon }}</span>
        </div>
        <span class="theme-name-mobile">{{ currentTheme.displayName }}</span>
      </div>

      <template #dropdown>
        <el-dropdown-menu class="theme-dropdown-menu">
          <!-- 类似头像菜单的标题行 -->
          <el-dropdown-item disabled class="theme-dropdown-header">
            切换主题
          </el-dropdown-item>

          <!-- 恢复默认 -->
          <el-dropdown-item command="__reset_default__" divided>
            <div class="theme-dropdown-item">
              <span class="theme-emoji-large">↺</span>
              <span class="theme-name">恢复默认主题</span>
            </div>
          </el-dropdown-item>

          <!-- 可滚动的主题列表 -->
          <div class="theme-dropdown-scroll">
            <el-dropdown-item
              v-for="theme in themes"
              :key="theme.name"
              :command="theme.name"
              :class="{ active: currentThemeName === theme.name }"
            >
              <div class="theme-dropdown-item">
                <span class="theme-emoji-large">{{ theme.icon }}</span>
                <span class="theme-name">{{ theme.displayName }}</span>
                <el-icon v-if="currentThemeName === theme.name" class="theme-check-icon"><Check /></el-icon>
              </div>
            </el-dropdown-item>
          </div>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { Check } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'
import { ElMessage } from 'element-plus'
import { defaultTheme } from '@/config/themes'

const { currentThemeName, currentTheme, themes, setTheme } = useTheme()

// 防刷屏：保持同一时刻只显示一条主题提示
const showThemeMessage = (type: 'success' | 'info', message: string) => {
  ElMessage.closeAll()
  ElMessage({
    type,
    message,
    duration: type === 'success' ? 2000 : 1500,
    grouping: true
  })
}

const handleThemeCommand = (themeName: string) => {
  if (themeName === '__reset_default__') {
    setTheme(defaultTheme)
    showThemeMessage('success', `已恢复默认主题「${currentTheme.value.displayName}」`)
    return
  }

  // 点击当前主题时也给出反馈（用户常以为“没反应”）
  if (currentThemeName.value === themeName) {
    showThemeMessage('info', `当前已是「${currentTheme.value.displayName}」`)
    return
  }

  setTheme(themeName)
  showThemeMessage('success', `主题已切换为「${currentTheme.value.displayName}」`)
}
</script>

<style scoped>
.theme-selector {
  position: relative;
}

/* 触发按钮 */
.theme-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
}

.theme-trigger:hover {
  background: var(--color-card-bg-hover, #FAFBFC);
  border-color: var(--color-border-hover, #CBD5E1);
  box-shadow: var(--shadow-hover, 0 4px 12px rgba(0, 0, 0, 0.08));
  transform: translateY(-2px);
}

.theme-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-secondary, #3B82F6) 100%);
  border-radius: 8px;
}

.theme-emoji {
  font-size: 18px;
  filter: grayscale(0);
}

.theme-name-mobile {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #1E293B);
}

/* Dropdown 菜单 */
.theme-dropdown-menu {
  min-width: 220px;
  padding: 6px;
}

.theme-dropdown-header {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
  cursor: default !important;
}

/* 让内容区滚动，体验接近用户菜单 */
.theme-dropdown-scroll {
  max-height: 280px;
  overflow-y: auto;
  padding: 4px 0;
}

.theme-dropdown-scroll::-webkit-scrollbar {
  width: 6px;
}

.theme-dropdown-scroll::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 6px;
}

.theme-dropdown-scroll::-webkit-scrollbar-thumb {
  background: var(--color-border, #CBD5E1);
  border-radius: 6px;
}

.theme-dropdown-scroll::-webkit-scrollbar-thumb:hover {
  background: var(--color-border-hover, #94A3B8);
}

.theme-dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.theme-check-icon {
  margin-left: auto;
  color: var(--el-color-primary, var(--color-primary, #2563EB));
}

:deep(.el-dropdown-menu__item.active) {
  background: var(--color-card-bg-hover, #EFF6FF);
  color: var(--el-color-primary, var(--color-primary, #2563EB));
}

/* 标题行禁用态样式优化（接近“头像菜单”顶部提示行） */
:deep(.el-dropdown-menu__item.is-disabled.theme-dropdown-header) {
  opacity: 1;
  height: 34px;
  line-height: 34px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  margin-bottom: 4px;
}

/* 响应式 */
@media (max-width: 768px) {
  .theme-name-mobile {
    display: none;
  }
}
</style>
