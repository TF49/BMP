<template>
  <div class="app-wrapper">
    <el-container>
      <el-aside
        :width="sidebarWidth"
        class="sidebar-container"
        @mouseenter="handleSidebarMouseEnter"
        @mouseleave="handleSidebarMouseLeave"
      >
        <div class="sidebar-logo" :class="{ collapse: isCollapse }">
          <div class="logo-icon">
            <svg viewBox="0 0 100 100" class="badminton-logo">
              <defs>
                <linearGradient id="featherGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" style="stop-color:#ffffff;stop-opacity:1" />
                  <stop offset="100%" style="stop-color:#e0e0e0;stop-opacity:1" />
                </linearGradient>
                <linearGradient id="corkGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#3B82F6;stop-opacity:0.6" />
                  <stop offset="100%" style="stop-color:#8B5CF6;stop-opacity:0.3" />
                </linearGradient>
              </defs>
              <ellipse cx="50" cy="28" rx="20" ry="10" fill="url(#featherGrad)" />
              <ellipse cx="50" cy="28" rx="16" ry="7" fill="url(#corkGrad)" />
              <path d="M32 28 Q50 70 50 100" stroke="rgba(255,255,255,0.9)" stroke-width="2.5" fill="none" stroke-linecap="round" />
              <path d="M36 28 Q50 60 50 88" stroke="rgba(255,255,255,0.7)" stroke-width="2" fill="none" stroke-linecap="round" />
              <path d="M40 28 Q50 50 50 76" stroke="rgba(255,255,255,0.5)" stroke-width="1.5" fill="none" stroke-linecap="round" />
              <path d="M44 28 Q50 42 50 64" stroke="rgba(255,255,255,0.3)" stroke-width="1" fill="none" stroke-linecap="round" />
              <circle cx="50" cy="100" r="3" fill="#ffffff" />
            </svg>
          </div>
          <transition name="fade">
            <span v-if="!isCollapse" class="logo-title">羽擎</span>
          </transition>
        </div>
        
        <el-menu
          :default-active="currentRoute"
          class="el-menu-vertical"
          :collapse="isCollapse"
          router
          background-color="transparent"
          text-color="rgba(255, 255, 255, 0.75)"
          active-text-color="#ffffff"
        >
          <template v-for="route in visibleRoutes" :key="route.path">
            <template v-if="route.path === '/'">
              <el-tooltip
                v-for="child in route.children"
                :key="child.path"
                :content="child.meta?.title"
                placement="right"
                :disabled="!isCollapse"
              >
                <el-menu-item
                  :index="`${route.path}${child.path}`"
                  class="menu-item"
                >
                  <el-icon :size="20">
                    <component :is="getIconComponent(child.meta?.icon)" />
                  </el-icon>
                  <template #title>{{ child.meta?.title }}</template>
                </el-menu-item>
              </el-tooltip>
            </template>
            <template v-else>
              <el-tooltip
                v-if="!route.children?.length"
                :content="route.meta?.title"
                placement="right"
                :disabled="!isCollapse"
              >
                <el-menu-item
                  :index="route.path"
                  class="menu-item"
                >
                  <el-icon :size="20">
                    <component :is="getIconComponent(route.meta?.icon)" />
                  </el-icon>
                  <template #title>{{ route.meta?.title }}</template>
                </el-menu-item>
              </el-tooltip>

              <el-sub-menu v-if="route.children?.length" :index="route.path">
                <template #title>
                  <el-icon :size="20">
                    <component :is="getIconComponent(route.meta?.icon)" />
                  </el-icon>
                  <span>{{ route.meta?.title }}</span>
                </template>
                <el-tooltip
                  v-for="child in route.children"
                  :key="child.path"
                  :content="child.meta?.title"
                  placement="right"
                  :disabled="!isCollapse"
                >
                  <el-menu-item
                    :index="`${route.path}/${child.path}`"
                    class="sub-menu-item"
                  >
                    <el-icon :size="18">
                      <component :is="getIconComponent(child.meta?.icon)" />
                    </el-icon>
                    <template #title>{{ child.meta?.title }}</template>
                  </el-menu-item>
                </el-tooltip>
              </el-sub-menu>
            </template>
          </template>
        </el-menu>
      </el-aside>
      
      <el-container class="main-wrapper">
        <el-header class="header-container">
          <div class="header-left">
            <button 
              class="menu-toggle" 
              @click="toggleCollapse"
              :aria-label="isCollapse ? '展开菜单' : '折叠菜单'"
            >
              <el-icon :size="20">
                <component :is="isCollapse ? 'Expand' : 'Fold'" />
              </el-icon>
            </button>
            <h2 class="header-title">羽擎</h2>
          </div>
          
          <div class="header-right">
            <!-- 待办角标（首屏来自接口，之后由 WebSocket 实时更新） -->
            <router-link v-if="pendingTodoCount > 0" to="/dashboard" class="header-todo-badge">
              <el-badge :value="pendingTodoCount" :max="99" type="danger">
                <span class="todo-label">待办</span>
              </el-badge>
            </router-link>
            <!-- 通知铃铛：点击后打开与参考图一致的通知弹窗 -->
            <NotificationBell
              v-if="siteMessageEnabled"
              ref="notificationBellRef"
              @open-popup="openNotificationFromBell"
            />
            <!-- 主题选择器 -->
            <ThemeSelector />

            <el-tag
              :type="userRole === 'PRESIDENT' ? 'danger' : userRole === 'VENUE_MANAGER' ? 'warning' : 'success'"
              size="small"
              effect="dark"
            >
              {{ userRole === 'PRESIDENT' ? '协会会长' : userRole === 'VENUE_MANAGER' ? '场馆管理者' : '普通用户' }}
            </el-tag>
            
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="32" :src="headerAvatarUrl" :icon="!headerAvatarUrl ? UserFilled : undefined" />
                <span class="username">{{ userInfo.username }}</span>
                <el-icon :size="12"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main class="main-container">
          <router-view v-slot="{ Component }">
            <transition name="fade-slide" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
        
        <el-footer class="footer-container">
          <p>© 2026 羽擎 · 用心管理每一份数据</p>
        </el-footer>
      </el-container>
    </el-container>

    <!-- 通知弹窗：每日首次登录自动弹出 + 点击铃铛打开，统一为参考图样式 -->
    <NotificationPopup
      v-if="siteMessageEnabled"
      v-model="notificationPopupVisible"
      :loading="notificationLoading"
      :list="notificationList"
      :can-publish="notificationCanPublish"
      footer-today-text="今日关闭"
      footer-close-text="关闭公告"
      @close="closeNotificationPopup"
      @close-today="closeNotificationPopup"
      @publish="handleNotificationPublish"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { RouteRecordNormalized } from 'vue-router'
import { logout as authLogout, resolveAvatarUrl, setUserInfo as persistUserInfo } from '@/utils/auth'
import { getInfo } from '@/api/login'
import ThemeSelector from '@/components/ThemeSelector.vue'
import NotificationBell from '@/components/NotificationBell.vue'
import NotificationPopup from '@/components/NotificationPopup.vue'
import { useNotificationPopup } from '@/composables/useNotificationPopup'
import { useSiteMessageSetting } from '@/composables/useSiteMessageSetting'
import { useWebSocket } from '@/composables/useWebSocket'
import { getToken } from '@/utils/auth'
import { getOperationTodoStatistics } from '@/api/booking'

const { siteMessageEnabled, loadFromApi: loadSiteMessageSetting } = useSiteMessageSetting()
const {
  popupVisible: notificationPopupVisible,
  notifications: notificationList,
  loading: notificationLoading,
  closePopup: closeNotificationPopup,
  checkAndShow: checkNotificationPopup,
  openFromBell: openNotificationFromBell
} = useNotificationPopup()

const notificationBellRef = ref<InstanceType<typeof NotificationBell> | null>(null)
const notificationCanPublish = computed(() => {
  return userRole.value === 'PRESIDENT' || userRole.value === 'VENUE_MANAGER'
})
const handleNotificationPublish = () => {
  notificationPopupVisible.value = false
  notificationBellRef.value?.openPublishDialog()
}

import {
  Odometer,
  User,
  Document,
  Setting,
  DataLine,
  Expand,
  Fold,
  ArrowDown,
  SwitchButton,
  UserFilled,
  OfficeBuilding,
  Grid,
  Money
} from '@element-plus/icons-vue'

interface UserInfo {
  username: string
  role: string
  venueId?: number | null
  avatar?: string
}

interface RouteMeta {
  title?: string
  icon?: string
  hidden?: boolean
}

interface RouteItem {
  path: string
  meta?: RouteMeta
  children?: RouteItem[]
}

const router = useRouter()
const route = useRoute()

const isCollapse = ref<boolean>(false)
const isHovering = ref<boolean>(false)
const userInfo = ref<UserInfo>({ username: '', role: 'USER' })

const currentRoute = computed<string>(() => route.path)

// 用于侧边栏与权限：后端可能返回 ADMIN，与前端 PRESIDENT 等价，统一视为管理角色
const userRole = computed<string>(() => {
  const r = userInfo.value.role || 'USER'
  return r === 'ADMIN' ? 'PRESIDENT' : r
})

const { connect: connectWebSocket, connected: wsConnected, todoCount: wsTodoCount } = useWebSocket(true)
const initialTodoCount = ref<Record<string, number>>({})
const pendingTodoCount = computed(() => {
  const fromWs = wsTodoCount.value?.pendingBookings ?? wsTodoCount.value?.pending
  if (fromWs !== undefined && fromWs !== null) return Number(fromWs)
  return Number(initialTodoCount.value?.pendingBookings ?? initialTodoCount.value?.pending ?? 0)
})

const sidebarWidth = computed<string>(() => {
  if (isCollapse.value && !isHovering.value) {
    return '64px'
  }
  return '200px'
})

const visibleRoutes = computed<RouteItem[]>(() => {
  const allRoutes = router.options.routes || []
  const currentRole = userRole.value

  return allRoutes
    .filter((r: RouteRecordNormalized | any) => {
      // 过滤登录页和隐藏的路由
      if (r.path === '/login' || r.path === '/register' || r.meta?.hidden) {
        return false
      }
      // 过滤“仅做重定向”的根路由（如 / -> /site），避免侧栏出现空分组
      if (r.path === '/' && r.redirect && !r.component) {
        return false
      }
      // 检查路由权限（PRESIDENT 与 ADMIN 等价，上面 userRole 已统一）
      if (r.meta?.roles) {
        return r.meta.roles.includes(currentRole)
      }
      return true
    })
    .map((r: RouteRecordNormalized | any): RouteItem => {
      // 过滤子路由
      const filteredChildren = r.children?.filter((c: any) => {
        if (c.meta?.hidden) {
          return false
        }
        // 检查子路由权限
        if (c.meta?.roles) {
          return c.meta.roles.includes(currentRole)
        }
        return true
      }) || []
      
      return {
        path: r.path,
        meta: r.meta,
        children: filteredChildren
      }
    })
    .filter((r: RouteItem) => {
      // 如果路由有子路由，但所有子路由都被过滤掉了，则隐藏该路由
      if (r.children && r.children.length === 0 && r.path !== '/') {
        return false
      }
      return true
    })
})

const iconComponents: Record<string, any> = {
  'Odometer': Odometer,
  'User': User,
  'Setting': Setting,
  'DataLine': DataLine,
  'Document': Document,
  'OfficeBuilding': OfficeBuilding,
  'Grid': Grid,
  'Money': Money
}

const getIconComponent = (iconName?: string): any => {
  if (!iconName) return Document
  return iconComponents[iconName] || Document
}

const toggleCollapse = (): void => {
  isCollapse.value = !isCollapse.value
}

const handleSidebarMouseEnter = (): void => {
  if (isCollapse.value) {
    isHovering.value = true
  }
}

const handleSidebarMouseLeave = (): void => {
  isHovering.value = false
}

const handleStorageChange = (): void => {
  try {
    const stored = localStorage.getItem('userInfo')
    if (stored && stored !== 'null') {
      const parsed = JSON.parse(stored) as UserInfo
      if (parsed?.username && parsed?.role) {
        userInfo.value = parsed
      }
    }
  } catch (e) {
    console.warn('获取用户信息失败:', e)
  }
}

const headerAvatarUrl = computed(() => resolveAvatarUrl(userInfo.value?.avatar))

const handleCommand = (command: string): void => {
  switch (command) {
    case 'profile':
      router.push('/profile').catch(err => console.error('导航到个人中心失败:', err))
      break
    case 'settings':
      router.push('/settings').catch(err => console.error('导航到设置失败:', err))
      break
    case 'logout':
      authLogout()
      router.push('/login').catch(err => console.error('导航到登录页失败:', err))
      break
  }
}

onMounted(async () => {
  handleStorageChange()
  // 有 token 时从后端拉取最新用户信息，确保 role 正确（避免本地 userInfo 缺失或与后端不一致导致无菜单）
  if (getToken()) {
    try {
      const res: any = await getInfo()
      if (res?.code === 200 && res?.data) {
        const u = res.data
        if (u?.username != null) {
          userInfo.value = {
            username: u.username,
            role: u.role || userInfo.value.role || 'USER',
            venueId: u.venueId,
            avatar: u.avatar
          }
          persistUserInfo(userInfo.value)
          window.dispatchEvent(new Event('userInfoUpdated'))
        }
      }
    } catch (e) {
      console.warn('获取用户信息失败，使用本地缓存:', e)
    }
  }
  window.addEventListener('storage', handleStorageChange)
  window.addEventListener('userInfoUpdated', handleStorageChange)
  if (getToken() && (userInfo.value.role === 'PRESIDENT' || userInfo.value.role === 'VENUE_MANAGER' || userInfo.value.role === 'ADMIN')) {
    connectWebSocket()
    getOperationTodoStatistics().then((res: any) => {
      if (res?.data && typeof res.data === 'object') initialTodoCount.value = res.data
    }).catch(() => {})
  }
  await loadSiteMessageSetting()
  if (siteMessageEnabled.value) {
    checkNotificationPopup()
  }
})

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
  window.removeEventListener('userInfoUpdated', handleStorageChange)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

* {
  box-sizing: border-box;
}

.app-wrapper {
  height: 100vh;
  width: 100%;
  background: var(--color-background, #F8FAFC);
  overflow: hidden;
  position: relative;
  transition: background-color 0.3s ease;
}

.app-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: transparent;
  pointer-events: none;
  z-index: 0;
}

.main-wrapper {
  flex: 1;
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  margin-left: 0;
}

.main-wrapper.menu-collapsed {
  margin-left: 64px;
}

.sidebar-container {
  background: var(--color-card-bg, #FFFFFF);
  -webkit-backdrop-filter: none;
  backdrop-filter: none;
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1), background-color 0.3s ease;
  box-shadow: var(--shadow, 1px 0 3px rgba(0, 0, 0, 0.08));
  position: relative;
  z-index: 1001;
  border-right: 1px solid var(--color-border, #E2E8F0);
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.sidebar-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: transparent;
  pointer-events: none;
  transition: background-color 0.3s ease;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: var(--color-card-bg, #FFFFFF);
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  min-height: 64px;
  transition: all 0.28s ease;
}

.sidebar-logo.collapse {
  justify-content: center;
  padding: 16px 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.badminton-logo {
  width: 36px;
  height: 36px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.logo-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin-left: 12px;
  white-space: nowrap;
  font-family: 'Poppins', 'Inter', 'Fira Sans', sans-serif;
  letter-spacing: 0.3px;
}

.el-menu-vertical {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 0;
  overflow-y: auto;
  overflow-x: hidden;
  height: calc(100% - 64px);
}

.el-menu-vertical:not(.el-menu--collapse) .el-sub-menu__title span,
.el-menu-vertical:not(.el-menu--collapse) .el-menu-item span {
  transition: opacity 0.2s ease;
}

.el-menu-vertical.el-menu--collapse .el-menu-item {
  margin: 6px 8px;
  padding: 0 !important;
  justify-content: center;
  border-radius: 10px;
  min-height: 44px;
  position: relative;
}

.el-menu-vertical.el-menu--collapse .el-menu-item .el-icon {
  margin: 0;
  font-size: 20px;
  color: var(--color-text-secondary, #64748B);
  transition: color 0.3s ease, transform 0.3s ease;
}

.el-menu-vertical.el-menu--collapse .el-menu-item:hover .el-icon {
  color: var(--color-primary, #2563EB) !important;
  transform: scale(1.1);
}

.el-menu-vertical.el-menu--collapse .el-menu-item.is-active {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, rgba(59, 130, 246, 0.1) 100%) !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

.el-menu-vertical.el-menu--collapse .el-menu-item.is-active .el-icon {
  color: var(--color-primary, #2563EB) !important;
}

.el-menu-vertical.el-menu--collapse .el-menu-item.is-active::after {
  display: none;
}

.el-menu-vertical.el-menu--collapse .el-sub-menu {
  text-align: center;
}

.el-menu-vertical.el-menu--collapse .el-sub-menu__title {
  justify-content: center;
  padding: 0 !important;
  margin: 6px 8px;
  border-radius: 10px;
}

.el-menu-vertical.el-menu--collapse .el-sub-menu__title .el-icon {
  margin: 0;
  font-size: 20px;
}

.el-menu-vertical.el-menu--collapse .el-sub-menu__title span {
  display: none;
}

.el-menu-vertical :deep(.el-menu-item),
.el-menu-vertical :deep(.el-sub-menu__title) {
  color: var(--color-text-secondary, #64748B) !important;
}

.el-menu-vertical :deep(.el-menu-item.is-active),
.el-menu-vertical :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--color-primary, #2563EB) !important;
  background-color: var(--color-card-bg-hover, #EFF6FF) !important;
}

.el-menu-vertical::-webkit-scrollbar {
  width: 8px;
}

.el-menu-vertical::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.el-menu-vertical::-webkit-scrollbar-thumb {
  background: var(--color-border, #CBD5E1);
  border-radius: 4px;
  transition: background 0.3s ease;
}

.el-menu-vertical::-webkit-scrollbar-thumb:hover {
  background: var(--color-border-hover, #94A3B8);
}

.menu-item {
  margin: 4px 12px;
  padding: 0 12px !important;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent !important;
  color: var(--color-text-secondary, #64748B) !important;
  position: relative;
  overflow: hidden;
}

.menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.1), transparent);
  transition: left 0.5s ease;
}

.menu-item:hover {
  background: var(--color-background, #F1F5F9) !important;
  transform: translateX(4px);
  color: var(--color-primary, #2563EB) !important;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);
}

.menu-item:hover::before {
  left: 100%;
}

.menu-item:focus-visible {
  outline: 2px solid var(--color-primary, #2563EB);
  outline-offset: 2px;
}

.menu-item.is-active {
  background: linear-gradient(90deg, var(--color-card-bg-hover, #EFF6FF) 0%, rgba(59, 130, 246, 0.08) 100%) !important;
  border-left: 3px solid var(--color-primary, #2563EB);
  padding-left: 9px !important;
  color: var(--color-primary, #2563EB) !important;
  box-shadow: inset 4px 0 12px rgba(59, 130, 246, 0.2), 0 2px 8px rgba(59, 130, 246, 0.1);
  font-weight: 600;
}

.menu-item.is-active::after {
  content: '';
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  background: var(--color-primary, #2563EB);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.6);
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: translateY(-50%) scale(1);
  }
  50% {
    opacity: 0.7;
    transform: translateY(-50%) scale(1.2);
  }
}

.menu-item .el-icon {
  margin-right: 10px;
}

.sub-menu-item {
  margin: 2px 12px;
  padding: 0 12px !important;
  border-radius: 6px;
  height: 40px;
  line-height: 40px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent !important;
  color: var(--color-text-secondary, #64748B) !important;
  position: relative;
}

.sub-menu-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 60%;
  background: var(--color-primary, #2563EB);
  border-radius: 0 4px 4px 0;
  transition: width 0.3s ease;
}

.sub-menu-item:hover {
  background: var(--color-background, #F1F5F9) !important;
  transform: translateX(4px);
  color: var(--color-primary, #2563EB) !important;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.12);
}

.sub-menu-item:hover::before {
  width: 3px;
}

.sub-menu-item.is-active {
  background: var(--color-card-bg-hover, #EFF6FF) !important;
  color: var(--color-primary, #2563EB) !important;
  font-weight: 500;
}

.sub-menu-item.is-active::before {
  width: 3px;
}

.sub-menu-item .el-icon {
  margin-right: 8px;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-card-bg, #FFFFFF);
  -webkit-backdrop-filter: none;
  backdrop-filter: none;
  padding: 0 24px;
  height: 60px;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.08));
  position: relative;
  z-index: 1000;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  transition: background-color 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menu-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: var(--color-background, #F1F5F9);
  color: var(--color-text-secondary, #64748B);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.menu-toggle::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.15);
  transform: translate(-50%, -50%);
  transition: width 0.4s, height 0.4s;
}

.menu-toggle:hover {
  background: var(--color-border, #E2E8F0);
  color: var(--color-primary, #2563EB);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
  transform: scale(1.05);
}

.menu-toggle:hover::before {
  width: 100%;
  height: 100%;
}

.menu-toggle:active {
  transform: scale(0.95);
}

.menu-toggle:focus-visible {
  outline: 2px solid var(--color-primary, #2563EB);
  outline-offset: 2px;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
  font-family: 'Poppins', 'Inter', 'Fira Sans', sans-serif;
  letter-spacing: 0.2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-todo-badge {
  text-decoration: none;
  color: var(--color-text-secondary, #64748B);
  font-size: 13px;
}
.header-todo-badge .todo-label {
  padding: 4px 10px;
  border-radius: 8px;
  background: var(--color-background, #F1F5F9);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border-radius: 24px;
  background: linear-gradient(135deg, var(--color-background, #F1F5F9) 0%, var(--color-card-bg, #FFFFFF) 100%);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid var(--color-border, #E2E8F0);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.user-info::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.1), transparent);
  transition: left 0.5s ease;
}

.user-info:hover {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, var(--color-background, #F1F5F9) 100%);
  border-color: var(--color-primary-light, #60A5FA);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  transform: translateY(-2px);
}

.user-info:hover::before {
  left: 100%;
}

.user-info:active {
  transform: translateY(0);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Open Sans', 'Inter', sans-serif;
}

.main-container {
  background: transparent;
  padding: 24px;
  overflow-y: auto;
  position: relative;
  z-index: 1;
}

.main-container::-webkit-scrollbar {
  width: 8px;
}

.main-container::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}

.main-container::-webkit-scrollbar-thumb {
  background: var(--color-border, #CBD5E1);
  border-radius: 4px;
}

.main-container::-webkit-scrollbar-thumb:hover {
  background: var(--color-border-hover, #94A3B8);
}

.footer-container {
  background: var(--color-card-bg, #FFFFFF);
  -webkit-backdrop-filter: none;
  backdrop-filter: none;
  padding: 16px 24px;
  border-top: 1px solid var(--color-border, #E2E8F0);
  transition: background-color 0.3s ease;
}

.footer-container p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  text-align: center;
  font-family: 'Open Sans', 'Inter', sans-serif;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 1024px) {
  .sidebar-container {
    width: 64px !important;
  }
  
  .sidebar-logo {
    padding: 16px 0;
    justify-content: center;
  }
  
  .logo-title {
    display: none;
  }
  
  .header-title {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .header-title {
    font-size: 15px;
    max-width: 150px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .username {
    display: none;
  }

  .user-info {
    padding: 8px 12px;
    min-height: 44px;
    gap: 8px;
  }

  .user-info :deep(.el-avatar) {
    width: 40px !important;
    height: 40px !important;
  }

  .user-info > .el-icon {
    font-size: 16px !important;
  }

  .header-right .el-tag {
    padding: 6px 10px;
    font-size: 12px;
  }

  .main-container {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .header-title {
    font-size: 14px;
    max-width: 120px;
  }
  
  .menu-toggle {
    width: 32px;
    height: 32px;
  }
  
  .main-container {
    padding: 12px;
  }
  
  .user-info {
    padding: 6px 8px;
  }
}

</style>