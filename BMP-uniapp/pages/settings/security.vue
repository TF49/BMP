<template>
  <view class="page">
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="header-inner">
        <view class="nav-left" @tap="handleBack">
          <view class="icon-btn">
            <uni-icons type="left" size="22" color="#ff6600" />
          </view>
          <text class="nav-title">安全设置</text>
        </view>
      </view>
    </view>

    <scroll-view
      scroll-y
      class="main-scroll"
      :style="{ paddingTop: headerOffset + 'px' }"
      :show-scrollbar="false"
    >
      <view class="content">
        <view class="hero-card">
          <text class="hero-label">账户安全等级</text>
          <view class="hero-main">
            <view class="hero-icon" :class="securityLevel">
              <uni-icons :type="securityIconType" size="26" :color="securityColor" />
            </view>
            <view class="hero-copy">
              <text class="hero-title">{{ securityLevelText }}</text>
              <text class="hero-sub">建议补齐手机号和登录保护项</text>
            </view>
          </view>
          <view class="hero-progress">
            <view class="progress-track">
              <view class="progress-fill" :style="{ width: `${securityProgress}%` }" />
            </view>
            <text class="progress-text">{{ securityProgress }}%</text>
          </view>
        </view>

        <view class="section-card">
          <text class="section-title">账户安全</text>

          <view class="list-row" @tap="handleChangePassword">
            <view class="list-left">
              <view class="list-icon amber">
                <uni-icons type="locked" size="20" color="#a33e00" />
              </view>
              <view>
                <text class="list-title">修改密码</text>
                <text class="list-desc">定期更换密码更安全</text>
              </view>
            </view>
            <view class="list-right">
              <text class="status-text verified">已设置</text>
              <uni-icons type="right" size="18" color="#94a3b8" />
            </view>
          </view>

          <view class="divider" />

          <view class="list-row" @tap="handleBindPhone">
            <view class="list-left">
              <view class="list-icon blue">
                <uni-icons type="phone" size="20" color="#0062a1" />
              </view>
              <view>
                <text class="list-title">绑定手机</text>
                <text class="list-desc">{{ userInfo.phone || '绑定手机号可找回密码' }}</text>
              </view>
            </view>
            <view class="list-right">
              <text class="status-text" :class="{ verified: !!userInfo.phone }">
                {{ userInfo.phone ? '已绑定' : '未绑定' }}
              </text>
              <uni-icons type="right" size="18" color="#94a3b8" />
            </view>
          </view>
        </view>

        <view class="section-card">
          <text class="section-title">高级安全</text>

          <view class="switch-row">
            <view class="list-left">
              <view class="list-icon green">
                <uni-icons type="person-filled" size="20" color="#16a34a" />
              </view>
              <view>
                <text class="list-title">指纹 / 面容登录</text>
                <text class="list-desc">使用生物识别快速登录</text>
              </view>
            </view>
            <switch :checked="settings.biometricLogin" @change="toggleSetting('biometricLogin')" color="#ff6600" />
          </view>

          <view class="divider" />

          <view class="switch-row">
            <view class="list-left">
              <view class="list-icon violet">
                <uni-icons type="hand-up" size="20" color="#7c3aed" />
              </view>
              <view>
                <text class="list-title">手势密码</text>
                <text class="list-desc">设置手势密码保护应用</text>
              </view>
            </view>
            <switch :checked="settings.gesturePassword" @change="toggleSetting('gesturePassword')" color="#ff6600" />
          </view>

          <view class="divider" />

          <view class="switch-row">
            <view class="list-left">
              <view class="list-icon slate">
                <uni-icons type="locked" size="20" color="#475569" />
              </view>
              <view>
                <text class="list-title">自动锁定</text>
                <text class="list-desc">离开应用后自动锁定</text>
              </view>
            </view>
            <switch :checked="settings.autoLock" @change="toggleSetting('autoLock')" color="#ff6600" />
          </view>
        </view>

        <view class="section-card">
          <text class="section-title">登录记录</text>
          <view v-for="(record, index) in loginRecords" :key="index" class="record-row">
            <view class="list-left">
              <view class="list-icon light">
                <uni-icons :type="record.iconType" size="20" :color="record.isCurrent ? '#16a34a' : '#64748b'" />
              </view>
              <view>
                <text class="list-title">{{ record.device }}</text>
                <text class="list-desc">{{ record.time }}</text>
              </view>
            </view>
            <text v-if="record.isCurrent" class="current-tag">当前设备</text>
          </view>
        </view>

        <view class="tips-card">
          <text class="tips-title">安全建议</text>
          <view class="tips-list">
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>定期更换密码，使用强密码</text>
            </view>
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>不要在公共设备上保持登录状态</text>
            </view>
            <view class="tip-item">
              <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
              <text>绑定手机号以便找回账户</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCurrentUser } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { getSafeSystemInfo } from '@/utils/systemInfo'

const userStore = useUserStore()

const statusBarHeight = ref(44)
const headerOffset = computed(() => statusBarHeight.value + 56)

const userInfo = reactive({
  phone: ''
})

const settings = reactive({
  biometricLogin: false,
  gesturePassword: false,
  autoLock: true
})

const loginRecords = ref([
  { device: '当前设备 - 微信小程序', time: '刚刚', iconType: 'phone', isCurrent: true },
  { device: 'iPhone 14 Pro', time: '2026-04-20 15:30', iconType: 'phone', isCurrent: false },
  { device: 'Windows PC', time: '2026-04-18 09:15', iconType: 'contact', isCurrent: false }
])

const securityProgress = computed(() => {
  let progress = 0
  progress += 30
  if (userInfo.phone) progress += 25
  if (settings.biometricLogin || settings.gesturePassword) progress += 20
  return Math.min(progress, 100)
})

const securityLevel = computed(() => {
  if (securityProgress.value >= 80) return 'high'
  if (securityProgress.value >= 50) return 'medium'
  return 'low'
})

const securityLevelText = computed(() => {
  if (securityProgress.value >= 80) return '安全'
  if (securityProgress.value >= 50) return '中等'
  return '较低'
})

const securityIconType = computed(() => {
  if (securityProgress.value >= 80) return 'checkbox-filled'
  if (securityProgress.value >= 50) return 'info'
  return 'help'
})

const securityColor = computed(() => {
  if (securityProgress.value >= 80) return '#16a34a'
  if (securityProgress.value >= 50) return '#f59e0b'
  return '#ef4444'
})

function saveSettings() {
  uni.setStorageSync('security_settings', settings)
}

function loadSettings() {
  try {
    const saved = uni.getStorageSync('security_settings')
    if (saved) Object.assign(settings, saved)
  } catch (error) {
    console.error('加载安全设置失败:', error)
  }
}

function toggleSetting(key: keyof typeof settings) {
  settings[key] = !settings[key]
  saveSettings()

  if ((key === 'biometricLogin' || key === 'gesturePassword') && settings[key]) {
    uni.showToast({ title: '功能开发中', icon: 'none' })
    settings[key] = false
    saveSettings()
  }
}

async function loadUserInfo() {
  try {
    const user = await getCurrentUser()
    userInfo.phone = user.phone || ''
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

function handleChangePassword() {
  uni.navigateTo({ url: '/pages/settings/change-password' })
}

function handleBindPhone() {
  uni.showToast({ title: '手机号绑定功能开发中', icon: 'none' })
}

function handleBack() {
  safeNavigateBack('/pages/settings/index')
}

onLoad(async () => {
  const sys = getSafeSystemInfo()
  statusBarHeight.value = sys.statusBarHeight || 44

  if (!userStore.isLoggedIn) {
    uni.redirectTo({ url: '/pages/login/login' })
    return
  }

  loadSettings()
  await loadUserInfo()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f9f9f9;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  background: rgba(249, 249, 249, 0.92);
  backdrop-filter: blur(16px);
}

.header-inner {
  min-height: 112rpx;
  padding: 12rpx 28rpx 20rpx;
  display: flex;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.icon-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-title {
  font-size: 38rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.main-scroll {
  height: 100vh;
}

.content {
  padding: 20rpx 18rpx 80rpx;
}

.hero-card,
.section-card,
.tips-card {
  background: #ffffff;
  border-radius: 28rpx;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05);
}

.hero-card {
  padding: 30rpx 28rpx;
}

.hero-label,
.section-title,
.tips-title {
  font-size: 20rpx;
  font-weight: 800;
  color: #6b7280;
  letter-spacing: 1rpx;
}

.hero-main {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.hero-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-icon.high { background: #dcfce7; }
.hero-icon.medium { background: #fef3c7; }
.hero-icon.low { background: #fee2e2; }

.hero-copy {
  flex: 1;
}

.hero-title {
  display: block;
  font-size: 40rpx;
  font-weight: 900;
  color: #111111;
}

.hero-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.5;
  color: #5f5e5e;
}

.hero-progress {
  margin-top: 26rpx;
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.progress-track {
  flex: 1;
  height: 12rpx;
  border-radius: 9999rpx;
  background: #e5e7eb;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 9999rpx;
  background: linear-gradient(90deg, #a33e00 0%, #ff6600 100%);
}

.progress-text {
  font-size: 24rpx;
  font-weight: 900;
  color: #a33e00;
}

.section-card,
.tips-card {
  margin-top: 20rpx;
  padding: 28rpx 26rpx;
}

.section-title,
.tips-title {
  display: block;
  font-size: 30rpx;
  letter-spacing: 0;
  color: #111111;
}

.list-row,
.switch-row,
.record-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 24rpx 0;
}

.list-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}

.list-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.list-icon.amber { background: #fff3ec; }
.list-icon.blue { background: #e0f2fe; }
.list-icon.rose { background: #fff1f2; }
.list-icon.green { background: #dcfce7; }
.list-icon.violet { background: #f3e8ff; }
.list-icon.slate { background: #e2e8f0; }
.list-icon.light { background: #f8fafc; }

.list-title {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #111111;
}

.list-desc {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  line-height: 1.5;
  color: #6b7280;
}

.list-right {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-shrink: 0;
}

.status-text {
  font-size: 22rpx;
  color: #94a3b8;
  font-weight: 700;
}

.status-text.verified {
  color: #16a34a;
}

.divider {
  height: 1rpx;
  background: #f1f5f9;
}

.current-tag {
  min-width: 108rpx;
  height: 42rpx;
  padding: 0 14rpx;
  border-radius: 9999rpx;
  background: #dcfce7;
  color: #16a34a;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18rpx;
  font-weight: 800;
}

.tips-list {
  margin-top: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #475569;
}
</style>
