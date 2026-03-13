<template>
  <div class="coach-app-wrapper">
    <!-- 顶部导航栏（与用户端统一：玻璃态、浅色、圆角） -->
    <header class="coach-header">
      <div class="header-content">
        <div class="header-left">
          <div class="logo-section" @click="$router.push('/coach/dashboard')">
            <div class="logo-icon">
              <svg viewBox="0 0 100 100" class="badminton-logo">
                <defs>
                  <linearGradient id="coachFeatherGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" style="stop-color:#ffffff;stop-opacity:1" />
                    <stop offset="100%" style="stop-color:#e0e0e0;stop-opacity:1" />
                  </linearGradient>
                  <linearGradient id="coachCorkGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" style="stop-color:#3B82F6;stop-opacity:0.6" />
                    <stop offset="100%" style="stop-color:#8B5CF6;stop-opacity:0.3" />
                  </linearGradient>
                </defs>
                <ellipse cx="50" cy="28" rx="20" ry="10" fill="url(#coachFeatherGrad)" />
                <ellipse cx="50" cy="28" rx="16" ry="7" fill="url(#coachCorkGrad)" />
                <path d="M32 28 Q50 70 50 100" stroke="rgba(255,255,255,0.9)" stroke-width="2.5" fill="none" stroke-linecap="round" />
                <path d="M36 28 Q50 60 50 88" stroke="rgba(255,255,255,0.7)" stroke-width="2" fill="none" stroke-linecap="round" />
                <path d="M40 28 Q50 50 50 76" stroke="rgba(255,255,255,0.5)" stroke-width="1.5" fill="none" stroke-linecap="round" />
                <path d="M44 28 Q50 42 50 64" stroke="rgba(255,255,255,0.3)" stroke-width="1" fill="none" stroke-linecap="round" />
                <circle cx="50" cy="100" r="3" fill="#ffffff" />
              </svg>
            </div>
            <span class="logo-text">羽擎</span>
            <span class="logo-badge">教练</span>
          </div>

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
          <ThemeSelector />
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="coachAvatarUrl" :icon="!coachAvatarUrl ? UserFilled : undefined" />
              <span class="username">{{ displayName }}</span>
              <el-icon :size="12"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  我的档案
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

    <!-- 未绑定教练档案时显示顶部提示（与用户端风格一致：柔和提示条） -->
    <div v-if="showUnboundTip" class="unbound-banner">
      <el-icon><WarningFilled /></el-icon>
      <span>您尚未绑定教练档案，请联系管理员在「教练管理」中为您的账号关联教练档案。</span>
    </div>

    <main class="coach-main">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, ArrowDown, User, SwitchButton, Odometer, Document, Calendar, List, Postcard, WarningFilled } from '@element-plus/icons-vue'
import ThemeSelector from '@/components/ThemeSelector.vue'
import { getCurrentCoach } from '@/api/coach'
import { getToken, removeToken, removeRefreshToken } from '@/utils/auth'

const router = useRouter()
const coachInfo = ref(null)

const navItems = [
  { path: '/coach/dashboard', title: '工作台', icon: Odometer },
  { path: '/coach/courses', title: '我的课程', icon: Document },
  { path: '/coach/schedule', title: '我的课表', icon: Calendar },
  { path: '/coach/bookings', title: '预约明细', icon: List },
  { path: '/coach/profile', title: '我的档案', icon: Postcard }
]

const displayName = computed(() => {
  if (coachInfo.value?.coachName) return coachInfo.value.coachName
  try {
    const u = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return u.username || '教练'
  } catch (_) {
    return '教练'
  }
})

const coachAvatarUrl = computed(() => {
  const url = coachInfo.value?.avatar
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = import.meta.env.VITE_APP_BASE_API || ''
  return base ? (base.replace(/\/$/, '') + url) : url
})

const coachInfoLoaded = ref(false)
const showUnboundTip = computed(() => {
  return getToken() && coachInfo.value === null && coachInfoLoaded.value
})

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/coach/profile').catch(() => {})
  } else if (command === 'logout') {
    removeToken()
    removeRefreshToken()
    localStorage.removeItem('userInfo')
    router.push('/login').catch(() => {})
  }
}

const loadCoachInfo = async () => {
  if (!getToken()) return
  coachInfoLoaded.value = false
  try {
    const res = await getCurrentCoach()
    if (res?.code === 200 && res?.data) {
      coachInfo.value = res.data
    } else {
      coachInfo.value = null
    }
  } catch (_) {
    coachInfo.value = null
  } finally {
    coachInfoLoaded.value = true
  }
}

onMounted(() => {
  loadCoachInfo()
  window.addEventListener('storage', loadCoachInfo)
})

onUnmounted(() => {
  window.removeEventListener('storage', loadCoachInfo)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.coach-app-wrapper {
  min-height: 100vh;
  background: var(--color-background, #F8FAFC);
  display: flex;
  flex-direction: column;
  transition: background-color 0.3s ease;
}

.coach-header {
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

html.theme-dark-mode .coach-header {
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

.logo-section:hover {
  transform: translateY(-2px) scale(1.02);
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
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Poppins', 'Inter', 'Fira Sans', sans-serif;
  letter-spacing: 0.3px;
}

.logo-badge {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary, #2563EB);
  background: rgba(37, 99, 235, 0.1);
  padding: 2px 8px;
  border-radius: 8px;
  border: 1px solid rgba(37, 99, 235, 0.25);
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
}

.nav-item:hover {
  background: var(--color-background, #F1F5F9);
  color: var(--color-primary, #2563EB);
  transform: translateY(-1px);
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
  box-shadow: 0 -2px 8px rgba(37, 99, 235, 0.4);
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
}

.user-info:hover {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, var(--color-background, #F1F5F9) 100%);
  border-color: var(--color-primary-light, #60A5FA);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.25), 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px) scale(1.02);
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, #1E293B);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unbound-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  border-bottom: 1px solid rgba(245, 158, 11, 0.3);
  color: #92400E;
  font-size: 14px;
  font-weight: 500;
}

.unbound-banner .el-icon {
  font-size: 18px;
}

.coach-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
  overflow-y: auto;
  scrollbar-gutter: stable;
}

.coach-main::-webkit-scrollbar {
  width: 8px;
}

.coach-main::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}

.coach-main::-webkit-scrollbar-thumb {
  background: var(--color-border, #CBD5E1);
  border-radius: 4px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

@media (max-width: 1024px) {
  .header-content { padding: 0 16px; }
  .top-nav { display: none; }
  .logo-text { font-size: 16px; }
  .coach-main { padding: 20px 16px; }
}

@media (max-width: 768px) {
  .header-content { padding: 0 12px; height: 56px; }
  .header-left { gap: 16px; }
  .logo-badge { display: none; }
  .username { display: none; }
  .coach-main { padding: 16px; }
}
</style>
