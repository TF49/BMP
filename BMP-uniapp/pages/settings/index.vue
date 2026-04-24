<template>
  <MobileLayout>
    <view class="settings-page">
      <view class="topbar">
        <view class="topbar-inner">
          <view class="icon-btn" @click="handleBack">
            <uni-icons type="left" size="18" color="#ff6600" />
          </view>
          <view class="topbar-copy">
            <text class="topbar-title">设置中心</text>
            <text class="topbar-sub">SETTINGS HUB</text>
          </view>
          <view class="icon-btn ghost" @click="goToProfileInfo">
            <uni-icons type="person" size="18" color="#a33e00" />
          </view>
        </view>
      </view>

      <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
        <view class="hero-card">
          <view class="hero-glow" />
          <view class="hero-head">
            <image v-if="avatarUrl" class="hero-avatar-image" :src="avatarUrl" mode="aspectFill" />
            <view v-else class="hero-avatar">
              <uni-icons type="person-filled" size="30" color="#ffffff" />
            </view>
            <view class="hero-meta">
              <text class="hero-name">{{ userInfo.username || '未登录用户' }}</text>
              <text class="hero-role">{{ roleLabel }}</text>
            </view>
            <view class="hero-level">{{ levelBadge }}</view>
          </view>

          <view class="hero-stats">
            <view class="stat-chip">
              <text class="stat-label">Security</text>
              <text class="stat-value">{{ securityLabel }}</text>
            </view>
            <view class="stat-chip">
              <text class="stat-label">Theme</text>
              <text class="stat-value">{{ isDarkMode ? 'Dark' : 'Light' }}</text>
            </view>
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Account</text>
              <text class="section-title">账户与资料</text>
            </view>
          </view>

          <view
            v-for="item in accountMenus"
            :key="item.title"
            class="menu-row"
            @click="navigate(item.path)"
          >
            <view class="menu-left">
              <view class="menu-icon" :style="{ background: item.bgColor }">
                <uni-icons :type="item.icon" size="18" :color="item.iconColor" />
              </view>
              <view class="menu-copy">
                <text class="menu-title">{{ item.title }}</text>
                <text class="menu-desc">{{ item.desc }}</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#8e7164" />
          </view>
        </view>

        <view class="section-card">
          <view class="section-head">
            <view>
              <text class="section-kicker">Preferences</text>
              <text class="section-title">偏好与通知</text>
            </view>
          </view>

          <view
            v-for="item in preferenceMenus"
            :key="item.title"
            class="menu-row"
            @click="navigate(item.path)"
          >
            <view class="menu-left">
              <view class="menu-icon" :style="{ background: item.bgColor }">
                <uni-icons :type="item.icon" size="18" :color="item.iconColor" />
              </view>
              <view class="menu-copy">
                <text class="menu-title">{{ item.title }}</text>
                <text class="menu-desc">{{ item.desc }}</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#8e7164" />
          </view>

          <view class="theme-row">
            <view class="menu-left">
              <view class="menu-icon" style="background: rgba(0, 98, 161, 0.12);">
                <uni-icons type="eye" size="18" color="#0062a1" />
              </view>
              <view class="menu-copy">
                <text class="menu-title">主题模式</text>
                <text class="menu-desc">切换明亮 / 深色视觉模式</text>
              </view>
            </view>
            <switch :checked="isDarkMode" color="#ff6600" @change="toggleTheme" />
          </view>
        </view>

        <view class="section-card">
          <view class="section-head compact">
            <view>
              <text class="section-kicker">Support</text>
              <text class="section-title">支持与说明</text>
            </view>
          </view>

          <view
            v-for="item in supportMenus"
            :key="item.title"
            class="menu-row"
            @click="navigate(item.path)"
          >
            <view class="menu-left">
              <view class="menu-icon" :style="{ background: item.bgColor }">
                <uni-icons :type="item.icon" size="18" :color="item.iconColor" />
              </view>
              <view class="menu-copy">
                <text class="menu-title">{{ item.title }}</text>
                <text class="menu-desc">{{ item.desc }}</text>
              </view>
            </view>
            <uni-icons type="right" size="16" color="#8e7164" />
          </view>
        </view>

        <view class="logout-panel">
          <button class="logout-btn" @click="handleLogout">
            <uni-icons type="redo" size="18" color="#1a1c1c" />
            <text class="logout-text">退出登录</text>
          </button>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { safeReLaunch } from '@/utils/safeRoute'
import { useThemeStore } from '@/store/modules/theme'
import MobileLayout from '@/components/MobileLayout.vue'
import { getCurrentUser, getSettings } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'
import { getAvatarImage } from '@/utils/displayImage'

const userInfo = ref({
  username: '',
  role: '',
  avatar: '',
  phone: ''
})

const userStore = useUserStore()
const themeStore = useThemeStore()
const isDarkMode = ref(false)
const siteMessageEnabled = ref(true)

const accountMenus = [
  {
    title: '账户设置',
    desc: '查看资料摘要并前往统一资料页编辑',
    path: '/pages/settings/account',
    icon: 'person',
    bgColor: 'rgba(255, 102, 0, 0.12)',
    iconColor: '#ff6600'
  },
  {
    title: '安全设置',
    desc: '密码修改与真实登录保护开关',
    path: '/pages/settings/security',
    icon: 'locked',
    bgColor: 'rgba(0, 98, 161, 0.12)',
    iconColor: '#0062a1'
  },
  {
    title: '个人资料',
    desc: '维护头像、手机号、性别与个性签名',
    path: '/pages/profile/info',
    icon: 'compose',
    bgColor: 'rgba(163, 62, 0, 0.12)',
    iconColor: '#a33e00'
  }
] as const

const preferenceMenus = [
  {
    title: '通知设置',
    desc: '仅保留与网页端同步的站内消息开关',
    path: '/pages/settings/notification',
    icon: 'chatbubble',
    bgColor: 'rgba(255, 181, 150, 0.28)',
    iconColor: '#a33e00'
  },
  {
    title: '隐私设置',
    desc: '数据清理、账号注销与当前边界说明',
    path: '/pages/settings/privacy',
    icon: 'eye-slash',
    bgColor: 'rgba(93, 95, 94, 0.12)',
    iconColor: '#5f5e5e'
  }
] as const

const supportMenus = [
  {
    title: '关于我们',
    desc: '了解产品信息与联系方式',
    path: '/pages/settings/about',
    icon: 'info',
    bgColor: 'rgba(255, 102, 0, 0.12)',
    iconColor: '#ff6600'
  },
  {
    title: '帮助与反馈',
    desc: '查看帮助说明与客服联系方式',
    path: '/pages/settings/help-user',
    icon: 'help',
    bgColor: 'rgba(0, 98, 161, 0.12)',
    iconColor: '#0062a1'
  }
] as const

const roleLabel = computed(() => userInfo.value.role || '普通用户')
const avatarUrl = computed(() => getAvatarImage(userInfo.value.avatar))
const levelBadge = computed(() => {
  if (userInfo.value.phone) return '已完善'
  return '待完善'
})

const securityLabel = computed(() => {
  if (userInfo.value.phone && siteMessageEnabled.value) return '已对齐'
  if (userInfo.value.phone) return '基础保护'
  return '待完善'
})

const loadUserInfo = async () => {
  try {
    const [user, settings] = await Promise.all([getCurrentUser(), getSettings()])
    userInfo.value = {
      username: user.nickname || user.username || '',
      role: user.role || '普通用户',
      avatar: user.avatar || '',
      phone: user.phone || ''
    }
    siteMessageEnabled.value = settings.siteMessage ?? true
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const toggleTheme = (e: any) => {
  themeStore.setMode(e.detail.value ? 'dark' : 'light')
  isDarkMode.value = e.detail.value
}

const navigate = (path: string) => {
  uni.navigateTo({ url: path })
}

const goToProfileInfo = () => {
  navigate('/pages/profile/info')
}

const handleLogout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        safeReLaunch('/pages/login/login', '/pages/login/login')
      }
    }
  })
}

const handleBack = () => {
  safeNavigateBack()
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  isDarkMode.value = themeStore.isDark
  await loadUserInfo()
})
</script>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top, rgba(255, 102, 0, 0.14), transparent 28%),
    #f9f9f9;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(249, 249, 249, 0.82);
  backdrop-filter: blur(20rpx);
}

.topbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.96);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.06);

  &.ghost {
    background: rgba(255, 241, 234, 0.9);
  }
}

.topbar-copy {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.topbar-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.topbar-sub {
  font-size: 18rpx;
  font-weight: 800;
  letter-spacing: 3rpx;
  color: #8e7164;
}

.page-scroll {
  height: calc(100vh - 196rpx);
  padding: 0 24rpx 40rpx;
  box-sizing: border-box;
}

.hero-card {
  position: relative;
  margin-top: 12rpx;
  padding: 34rpx 28rpx;
  border-radius: 36rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  box-shadow: 0 18rpx 40rpx rgba(163, 62, 0, 0.2);
}

.hero-glow {
  position: absolute;
  right: -80rpx;
  top: -80rpx;
  width: 260rpx;
  height: 260rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.14);
  filter: blur(36rpx);
}

.hero-head,
.hero-stats {
  position: relative;
  z-index: 1;
}

.hero-head {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.hero-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.32);
}

.hero-avatar-image {
  width: 96rpx;
  height: 96rpx;
  border-radius: 999rpx;
  border: 2rpx solid rgba(255, 255, 255, 0.32);
}

.hero-meta {
  flex: 1;
  min-width: 0;
}

.hero-name {
  display: block;
  font-size: 36rpx;
  font-weight: 900;
  color: #ffffff;
}

.hero-role {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.86);
}

.hero-level {
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.16);
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 800;
  white-space: nowrap;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 28rpx;
}

.stat-chip {
  padding: 18rpx 20rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(10rpx);
}

.stat-label {
  display: block;
  font-size: 18rpx;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.stat-value {
  display: block;
  margin-top: 8rpx;
  font-size: 28rpx;
  font-weight: 900;
  color: #ffffff;
}

.section-card {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 14rpx 30rpx rgba(26, 28, 28, 0.05);
}

.section-head {
  margin-bottom: 16rpx;

  &.compact {
    margin-bottom: 12rpx;
  }
}

.section-kicker {
  display: block;
  font-size: 18rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 3rpx;
  text-transform: uppercase;
}

.section-title {
  display: block;
  margin-top: 8rpx;
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.menu-row,
.theme-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding: 22rpx 0;
  border-bottom: 2rpx solid #f4efec;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 18rpx;
  min-width: 0;
  flex: 1;
}

.menu-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.menu-copy {
  flex: 1;
  min-width: 0;
}

.menu-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.menu-desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 1.55;
  color: #6b625c;
}

.logout-panel {
  margin-top: 28rpx;
  padding-bottom: 32rpx;
}

.logout-btn {
  width: 100%;
  height: 96rpx;
  border: none;
  border-radius: 28rpx;
  background: #e2e2e2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  box-shadow: 0 10rpx 22rpx rgba(26, 28, 28, 0.04);
}

.logout-text {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}
</style>
