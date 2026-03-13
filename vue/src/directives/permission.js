/**
 * Vue权限指令
 * 用于按钮级权限控制
 *
 * 使用方式：
 * v-permission="['PRESIDENT']"                    - 仅协会会长可见
 * v-permission="['PRESIDENT', 'VENUE_MANAGER']"   - 会长和场馆管理者可见
 * v-permission:module.action="'user.delete'"      - 检查用户模块的删除权限
 */

import { hasRole, hasPermission } from '@/utils/permission'

/**
 * 权限检查指令
 */
const permission = {
  mounted(el, binding) {
    const { value, arg, modifiers } = binding

    // 方式1: v-permission="['ROLE1', 'ROLE2']" - 直接传入角色数组
    if (Array.isArray(value)) {
      if (!hasRole(value)) {
        // 没有权限，移除元素
        el.parentNode && el.parentNode.removeChild(el)
      }
      return
    }

    // 方式2: v-permission="'module.action'" - 传入模块.操作格式
    if (typeof value === 'string' && value.includes('.')) {
      const [module, action] = value.split('.')
      if (!hasPermission(module, action)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
      return
    }

    // 方式3: v-permission:module="'action'" - 使用arg作为模块名
    if (arg && typeof value === 'string') {
      if (!hasPermission(arg, value)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
      return
    }

    console.warn('[v-permission] 无效的权限配置:', value)
  },

  updated(el, binding) {
    // 权限变更时重新检查（通常不会发生，但保留此逻辑）
    const { value, arg } = binding

    let hasAuth = false

    if (Array.isArray(value)) {
      hasAuth = hasRole(value)
    } else if (typeof value === 'string' && value.includes('.')) {
      const [module, action] = value.split('.')
      hasAuth = hasPermission(module, action)
    } else if (arg && typeof value === 'string') {
      hasAuth = hasPermission(arg, value)
    }

    if (!hasAuth) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

/**
 * 角色检查指令
 * v-role="'PRESIDENT'"         - 仅协会会长可见
 * v-role="['PRESIDENT', 'VENUE_MANAGER']" - 会长或场馆管理者可见
 */
const role = {
  mounted(el, binding) {
    const { value } = binding

    if (!hasRole(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * 安装权限指令
 */
export function setupPermissionDirective(app) {
  app.directive('permission', permission)
  app.directive('role', role)
}

export default permission
