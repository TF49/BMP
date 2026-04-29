<template>
  <div class="user-app-wrapper">
    <!-- 顶部导航栏 -->
    <header class="user-header">
      <div class="header-content">
        <div class="header-left">
          <div class="logo-section" @click="$router.push('/user/dashboard')">
            <div class="logo-icon">
              <svg viewBox="0 0 100 100" class="badminton-logo">
                <defs>
                  <linearGradient id="userFeatherGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" style="stop-color:#ffffff;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#e0e0e0;stop-opacity:1" />
                  </linearGradient>
                  <linearGradient id="userCorkGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#3B82F6;stop-opacity:0.6" />
                    <stop offset="100%" style="stop-color:#8B5CF6;stop-opacity:0.3" />
                  </linearGradient>
                </defs>
                <ellipse cx="50" cy="28" rx="20" ry="10" fill="url(#userFeatherGrad)" />
                <ellipse cx="50" cy="28" rx="16" ry="7" fill="url(#userCorkGrad)" />
                <path d="M32 28 Q50 70 50 100" stroke="rgba(255,255,255,0.9)" stroke-width="2.5" fill="none" stroke-linecap="round" />
                <path d="M36 28 Q50 60 50 88" stroke="rgba(255,255,255,0.7)" stroke-width="2" fill="none" stroke-linecap="round" />
                <path d="M40 28 Q50 50 50 76" stroke="rgba(255,255,255,0.5)" stroke-width="1.5" fill="none" stroke-linecap="round" />
                <path d="M44 28 Q50 42 50 64" stroke="rgba(255,255,255,0.3)" stroke-width="1" fill="none" stroke-linecap="round" />
                <circle cx="50" cy="100" r="3" fill="#ffffff" />
              </svg>
            </div>
            <span class="logo-text">羽擎</span>
          </div>
          
          <!-- 顶部导航菜单 -->
          <nav class="top-nav">
            <router-link
              v-for="nav in navItems"
              :key="nav.path"
              :to="nav.path"
              class="nav-item"
              :class="{ active: $route.path.startsWith(nav.path) }"
            >
              <el-icon :size="18"><component :is="nav.icon" /></el-icon>
              <span>{{ nav.title }}</span>
            </router-link>
          </nav>
        </div>
        
        <div class="header-right">
          <!-- 通知铃铛：点击后打开与参考图一致的通知弹窗 -->
          <NotificationBell
            v-if="siteMessageEnabled"
            ref="notificationBellRef"
            @open-popup="openNotificationFromBell"
          />
          <!-- 主题选择器 -->
          <ThemeSelector />
          
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
      </div>
    </header>
    
    <!-- 主内容区 -->
    <main class="user-main">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    
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

    <!-- VIP 用户登录后欢迎回家弹窗（仅 VIP 且本次会话首次进入用户端时显示） -->
    <VipWelcomeModal
      v-model="vipWelcomeVisible"
      :user-name="vipWelcomeUserName"
      :vip-level-label="vipWelcomeLevelLabel"
      :member-level="vipWelcomeLevel"
      @close="onVipWelcomeClose"
    />

    <!-- 底部导航栏（移动端） -->
    <footer class="user-footer-mobile">
        <div
          v-for="nav in mobileNavItems"
          :key="nav.path"
          class="footer-nav-item"
          :class="{ active: $route.path.startsWith(nav.path) }"
          @click="$router.push(nav.path)"
        >
          <el-icon :size="20"><component :is="iconComponents[nav.icon]" /></el-icon>
          <span>{{ nav.title }}</span>
        </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { logout as authLogout, resolveAvatarUrl } from '@/utils/auth'
import ThemeSelector from '@/components/ThemeSelector.vue'
import NotificationBell from '@/components/NotificationBell.vue'
import NotificationPopup from '@/components/NotificationPopup.vue'
import VipWelcomeModal from '@/components/VipWelcomeModal.vue'
import { useNotificationPopup } from '@/composables/useNotificationPopup'
import { getCurrentMember } from '@/api/member'
import { useSiteMessageSetting } from '@/composables/useSiteMessageSetting'
import { useWebSocket } from '@/composables/useWebSocket'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

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
  const role = userInfo.value?.role
  return role === 'PRESIDENT' || role === 'VENUE_MANAGER'
})
const handleNotificationPublish = () => {
  notificationPopupVisible.value = false
  notificationBellRef.value?.openPublishDialog()
}

// VIP 欢迎弹窗：仅 memberLevel >= 3 且本次会话未显示过时弹出
const vipWelcomeVisible = ref(false)
const vipWelcomeUserName = ref('')
const vipWelcomeLevel = ref(0)
const vipWelcomeLevelLabel = ref('VIP')
function getVipLevelLabel(level: number): string {
  if (level >= 3) return `VIP Lv.${level}`
  if (level === 2) return '金卡会员'
  if (level === 1) return '银卡会员'
  return '普通用户'
}
function onVipWelcomeClose() {
  try {
    sessionStorage.setItem('vipWelcomeShown', '1')
  } catch (_) {}
}

import {
  Odometer,
  DataLine,
  ShoppingBag,
  Tickets,
  Trophy,
  Money,
  User,
  Setting,
  SwitchButton,
  UserFilled,
  ArrowDown,
  Tools
} from '@element-plus/icons-vue'

// 图标组件映射
const iconComponents = {
  Odometer,
  DataLine,
  ShoppingBag,
  Tickets,
  Trophy,
  Money,
  Tools
}

interface UserInfo {
  username: string
  role: string
  avatar?: string
}

const router = useRouter()
const userInfo = ref<UserInfo>({ username: '', role: 'USER' })

const { connect: connectWebSocket, setOnOrderStatus } = useWebSocket(false)

// 顶部导航菜单项
const navItems = computed(() => [
  { path: '/user/dashboard', title: '首页', icon: Odometer },
  { path: '/user/booking', title: '场地预订', icon: DataLine },
  { path: '/user/equipment', title: '器材租借', icon: ShoppingBag },
  { path: '/user/stringing', title: '穿线服务', icon: Tools },
  { path: '/user/course', title: '课程预约', icon: Tickets },
  { path: '/user/tournament', title: '赛事报名', icon: Trophy }
])

// 移动端底部导航（简化版）
const mobileNavItems = computed(() => [
  { path: '/user/dashboard', title: '首页', icon: Odometer },
  { path: '/user/booking', title: '预订', icon: DataLine },
  { path: '/user/recharge', title: '充值', icon: Money },
  { path: '/user/equipment', title: '器材', icon: ShoppingBag }
])

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
      router.push('/user/profile').catch(err => console.error('导航到个人中心失败:', err))
      break
    case 'settings':
      router.push('/user/settings').catch(err => console.error('导航到设置失败:', err))
      break
    case 'logout':
      authLogout()
      router.push('/login').catch(err => console.error('导航到登录页失败:', err))
      break
  }
}

onMounted(async () => {
  handleStorageChange()
  setOnOrderStatus((payload) => {
    const title = payload.title || '预约'
    ElMessage.success(`${title}状态已更新：${payload.statusText}`)
  })
  if (getToken() && (userInfo.value.role === 'USER' || userInfo.value.role === 'MEMBER')) {
    connectWebSocket()
  }
  window.addEventListener('storage', handleStorageChange)
  window.addEventListener('userInfoUpdated', handleStorageChange)
  await loadSiteMessageSetting()
  if (siteMessageEnabled.value) {
    checkNotificationPopup()
  }
  // VIP 用户：登录后首次进入用户端时弹出欢迎回家（每次登录会清除标记，故每次登录最多弹一次）
  try {
    if (sessionStorage.getItem('vipWelcomeShown')) return
    const res = await getCurrentMember()
    if (res?.code !== 200 || !res?.data) return
    const data = res.data as { memberLevel?: number; member_level?: number }
    const level = Number(data.memberLevel ?? data.member_level ?? 0)
    if (level < 3) return
    vipWelcomeUserName.value = (userInfo.value as { nickname?: string })?.nickname || userInfo.value?.username || '会员'
    vipWelcomeLevel.value = level
    vipWelcomeLevelLabel.value = getVipLevelLabel(level)
    // 稍作延迟再显示，确保页面已渲染且位于其他弹窗之上
    setTimeout(() => {
      vipWelcomeVisible.value = true
    }, 400)
  } catch (_) {
    // 非会员或接口失败时静默跳过
  }
})

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange)
  window.removeEventListener('userInfoUpdated', handleStorageChange)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-app-wrapper {
  min-height: 100vh;
  background: var(--color-background, #F8FAFC);
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s ease;
}

/* 顶部导航栏 - 玻璃态效果 */
.user-header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--color-border, rgba(226, 232, 240, 0.5));
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 暗色主题适配 */
html.theme-dark-mode .user-header {
  background: rgba(30, 41, 59, 0.8);
  border-bottom-color: rgba(51, 65, 85, 0.5);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
  flex: 1;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  padding: 4px 8px;
  border-radius: 12px;
}

.logo-section::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary, #2563EB), var(--color-secondary, #8B5CF6));
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.logo-section:hover {
  transform: translateY(-2px) scale(1.02);
}

.logo-section:hover::before {
  opacity: 0.1;
}

.logo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badminton-logo {
  width: 32px;
  height: 32px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: logoFloat 3s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-3px) rotate(2deg);
  }
}

.logo-section:hover .badminton-logo {
  filter: drop-shadow(0 4px 12px rgba(37, 99, 235, 0.4));
  transform: scale(1.1) rotate(5deg);
  animation: none;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Poppins', 'Inter', 'Fira Sans', sans-serif;
  letter-spacing: 0.3px;
}

.top-nav {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 10px;
  color: var(--color-text-secondary, #64748B);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: var(--color-primary, #2563EB);
  opacity: 0.1;
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
  z-index: 0;
}

.nav-item:hover {
  background: var(--color-background, #F1F5F9);
  color: var(--color-primary, #2563EB);
  transform: translateY(-1px);
}

.nav-item:hover::before {
  width: 100px;
  height: 100px;
}

.nav-item > * {
  position: relative;
  z-index: 1;
}

.nav-item.active {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, rgba(37, 99, 235, 0.08) 100%);
  color: var(--color-primary, #2563EB);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.15);
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--color-primary, #2563EB), transparent);
  border-radius: 3px 3px 0 0;
  animation: slideIn 0.3s ease-out;
  box-shadow: 0 -2px 8px rgba(37, 99, 235, 0.4);
}

@keyframes slideIn {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 60%;
    opacity: 1;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
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
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.1), transparent);
  transition: left 0.5s ease;
}

.user-info:hover {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, var(--color-background, #F1F5F9) 100%);
  border-color: var(--color-primary-light, #60A5FA);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.25), 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px) scale(1.02);
}

.user-info:hover::before {
  left: 100%;
}

.user-info:active {
  transform: translateY(0) scale(0.98);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Open Sans', 'Inter', sans-serif;
}

/* 主内容区 */
.user-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  overflow-y: auto;
  scrollbar-gutter: stable; /* 预留滚动条空间，避免右侧内容被挤压、左右留白不对称 */
}

.user-main::-webkit-scrollbar {
  width: 8px;
}

.user-main::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}

.user-main::-webkit-scrollbar-thumb {
  background: var(--color-border, #CBD5E1);
  border-radius: 4px;
}

.user-main::-webkit-scrollbar-thumb:hover {
  background: var(--color-border-hover, #94A3B8);
}

/* 移动端底部导航 - 玻璃态效果 */
.user-footer-mobile {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-top: 1px solid var(--color-border, rgba(226, 232, 240, 0.5));
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.08), 0 -1px 3px rgba(0, 0, 0, 0.04);
  z-index: 1000;
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom));
}

html.theme-dark-mode .user-footer-mobile {
  background: rgba(30, 41, 59, 0.9);
  border-top-color: rgba(51, 65, 85, 0.5);
}

.footer-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px;
  color: var(--color-text-secondary, #64748B);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex: 1;
  position: relative;
  border-radius: 12px;
  margin: 0 4px;
}

.footer-nav-item::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: var(--color-primary, #2563EB);
  opacity: 0.1;
  transform: translate(-50%, -50%);
  transition: width 0.3s ease, height 0.3s ease;
}

.footer-nav-item:hover {
  color: var(--color-primary, #2563EB);
  transform: translateY(-2px);
}

.footer-nav-item:hover::before {
  width: 60px;
  height: 60px;
}

.footer-nav-item:active {
  transform: translateY(0) scale(0.95);
}

.footer-nav-item.active {
  color: var(--color-primary, #2563EB);
  font-weight: 600;
}

.footer-nav-item.active::after {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40%;
  height: 3px;
  background: var(--color-primary, #2563EB);
  border-radius: 0 0 3px 3px;
  animation: slideDown 0.3s ease-out;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.4);
}

@keyframes slideDown {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 40%;
    opacity: 1;
  }
}

.footer-nav-item .el-icon {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.footer-nav-item:hover .el-icon,
.footer-nav-item.active .el-icon {
  transform: scale(1.15) translateY(-2px);
}

/* 过渡动画 - 增强效果 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.98);
}

/* 减少动画（尊重用户的偏好设置） */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 响应式设计 - 优化移动端体验 */
@media (max-width: 1024px) {
  .header-content {
    padding: 0 16px;
  }
  
  .top-nav {
    display: none;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .user-main {
    padding: 20px 16px;
  }
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 12px;
    height: 56px;
  }
  
  .header-left {
    gap: 16px;
  }
  
  .logo-text {
    display: none;
  }
  
  .username {
    display: none;
  }
  
  .user-info {
    padding: 6px 10px;
  }
  
  .user-main {
    padding: 16px;
    padding-bottom: 80px; /* 为底部导航留出空间 */
  }
  
  .user-footer-mobile {
    display: flex;
  }
  
  /* 移动端触摸优化 */
  .footer-nav-item {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
  
  .nav-item,
  .user-info {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
}

@media (max-width: 480px) {
  .user-main {
    padding: 12px;
    padding-bottom: 80px;
  }
  
  .header-content {
    padding: 0 8px;
  }
  
  .user-info {
    padding: 4px 8px;
  }
}

</style>
