<template>
  <MobileLayout className="privacy-shell">
    <view class="privacy-page">
      <scroll-view scroll-y class="page-scroll" :show-scrollbar="false">
        <view class="content">
          <view class="hero-card">
            <view class="hero-top">
              <view class="hero-brand" @tap="handleBack">
                <uni-icons type="left" size="18" color="#a33e00" />
                <text class="hero-brand-text">KINETIC LOGIC</text>
              </view>
              <text class="hero-side-label">DATA CONTROL</text>
            </view>

            <view class="hero-copy">
              <text class="hero-eyebrow">PRIVACY & ACCOUNT</text>
              <text class="hero-title">这里改成真实可执行的数据与账户管理</text>
              <text class="hero-subtitle">
                不再保留本地假保存的隐私开关，当前仅提供缓存管理、账号注销和边界说明。
              </text>
            </view>

            <view class="hero-grid">
              <view class="hero-metric">
                <text class="metric-label">CACHE</text>
                <text class="metric-value">{{ cacheSize }}</text>
              </view>
              <view class="hero-metric">
                <text class="metric-label">ACCOUNT</text>
                <text class="metric-value">可执行注销确认</text>
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">数据管理</text>
                <text class="panel-subtitle">清理本地缓存时会保留登录态、用户信息和主题设置。</text>
              </view>
              <text class="panel-tag">DATA</text>
            </view>

            <view class="action-list">
              <view class="action-row" @tap="handleClearCache">
                <view class="action-copy">
                  <text class="action-title">清除缓存</text>
                  <text class="action-desc">当前缓存 {{ cacheSize }}，用于清理临时数据和页面缓存。</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">账户处理</text>
                <text class="panel-subtitle">注销会删除当前账户与相关设置，请谨慎操作。</text>
              </view>
              <text class="panel-tag">ACCOUNT</text>
            </view>

            <view class="field-card">
              <text class="field-label">确认密码</text>
              <input
                v-model="password"
                class="field-input"
                type="password"
                password
                placeholder="输入当前登录密码以确认注销"
              />
            </view>

            <button class="danger-btn" :disabled="submitting" @tap="handleDeleteAccount">
              <text class="danger-top">Delete Account</text>
              <text class="danger-bottom">{{ submitting ? '注销中...' : '确认注销账户' }}</text>
            </button>
          </view>

          <view class="panel-card tips-card">
            <text class="tips-title">当前边界说明</text>
            <view class="tips-list">
              <view class="tip-item">
                <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
                <text>隐私政策、用户协议、数据导出等内容暂未提供独立真实页面，因此不再保留假入口。</text>
              </view>
              <view class="tip-item">
                <uni-icons type="checkbox-filled" size="16" color="#16a34a" />
                <text>如需维护手机号、性别等资料，请前往账户设置或个人资料页。</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { deleteAccount } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'
import { useThemeStore } from '@/store/modules/theme'
import { safeReLaunch } from '@/utils/safeRoute'

const userStore = useUserStore()
const themeStore = useThemeStore()
const cacheSize = ref('0 KB')
const password = ref('')
const submitting = ref(false)

function calculateCacheSize() {
  try {
    const info = uni.getStorageInfoSync()
    const sizeKB = Number(info.currentSize || 0)
    if (sizeKB > 1024) {
      cacheSize.value = `${(sizeKB / 1024).toFixed(2)} MB`
      return
    }
    cacheSize.value = `${sizeKB} KB`
  } catch (error) {
    cacheSize.value = '未知'
  }
}

function handleClearCache() {
  uni.showModal({
    title: '确认清除',
    content: '确定要清除缓存数据吗？当前登录态、用户信息和主题设置会被保留。',
    success: (res) => {
      if (!res.confirm) return
      try {
        const token = uni.getStorageSync('token')
        const refreshToken = uni.getStorageSync('refreshToken')
        const userInfo = uni.getStorageSync('userInfo')
        const themeMode = themeStore.mode
        const themePrimaryColor = uni.getStorageSync('theme_primary_color')

        uni.clearStorageSync()

        if (token) uni.setStorageSync('token', token)
        if (refreshToken) uni.setStorageSync('refreshToken', refreshToken)
        if (userInfo) uni.setStorageSync('userInfo', userInfo)
        if (themeMode) uni.setStorageSync('theme_mode', themeMode)
        if (themePrimaryColor) uni.setStorageSync('theme_primary_color', themePrimaryColor)

        uni.showToast({
          title: '缓存已清除',
          icon: 'success'
        })
        calculateCacheSize()
      } catch (error) {
        uni.showToast({
          title: '清除失败',
          icon: 'none'
        })
      }
    }
  })
}

function clearSessionAndRedirect() {
  userStore.logout()
  safeReLaunch('/pages/login/login', '/pages/login/login')
}

function handleDeleteAccount() {
  const currentPassword = password.value.trim()
  if (!currentPassword) {
    uni.showToast({
      title: '请输入当前密码',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认注销',
    content: '账户注销后将不可恢复，确定继续吗？',
    confirmColor: '#f97316',
    success: async (res) => {
      if (!res.confirm) return

      try {
        submitting.value = true
        uni.showLoading({ title: '注销中...' })
        await deleteAccount({ password: currentPassword })
        uni.hideLoading()
        uni.showToast({
          title: '账户已注销',
          icon: 'success'
        })
        password.value = ''
        setTimeout(() => {
          clearSessionAndRedirect()
        }, 600)
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: error instanceof Error ? error.message : '注销失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        submitting.value = false
      }
    }
  })
}

function handleBack() {
  safeNavigateBack('/pages/settings/index')
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }

  calculateCacheSize()
})
</script>

<style lang="scss" scoped>
.privacy-shell {
  background:
    radial-gradient(circle at top left, rgba(255, 170, 112, 0.28), transparent 28%),
    radial-gradient(circle at top right, rgba(251, 146, 60, 0.16), transparent 22%),
    linear-gradient(180deg, #fff7ed 0%, #f8fafc 36%, #eef2ff 100%);
}

.privacy-page {
  min-height: 100vh;
}

.page-scroll {
  height: 100vh;
}

.content {
  padding: 30rpx 24rpx 48rpx;
}

.hero-card,
.panel-card {
  border-radius: 36rpx;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24rpx 60rpx rgba(15, 23, 42, 0.08);
}

.hero-card {
  position: relative;
  overflow: hidden;
  padding: 32rpx;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 247, 237, 0.98));
}

.hero-card::before {
  content: '';
  position: absolute;
  right: -84rpx;
  top: -80rpx;
  width: 250rpx;
  height: 250rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.22), rgba(251, 146, 60, 0));
}

.hero-top,
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-top {
  position: relative;
  z-index: 1;
  margin-bottom: 36rpx;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 14rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(255, 237, 213, 0.86);
}

.hero-brand-text,
.hero-side-label,
.hero-eyebrow,
.panel-tag,
.metric-label {
  letter-spacing: 0.18em;
}

.hero-brand-text {
  font-size: 20rpx;
  color: #9a3412;
  font-weight: 800;
}

.hero-side-label {
  font-size: 20rpx;
  color: #94a3b8;
  font-weight: 700;
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.hero-eyebrow {
  display: block;
  margin-bottom: 16rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.hero-title {
  display: block;
  font-size: 54rpx;
  line-height: 1.16;
  color: #0f172a;
  font-weight: 800;
}

.hero-subtitle {
  display: block;
  margin-top: 18rpx;
  font-size: 24rpx;
  line-height: 1.65;
  color: #64748b;
}

.hero-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 24rpx;
}

.hero-metric {
  padding: 20rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #ffffff 0%, #fff7ed 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.16);
  box-sizing: border-box;
}

.metric-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.metric-value {
  display: block;
  font-size: 26rpx;
  color: #0f172a;
  font-weight: 800;
  line-height: 1.45;
}

.panel-card {
  margin-top: 24rpx;
  padding: 28rpx;
}

.panel-title {
  display: block;
  font-size: 32rpx;
  color: #0f172a;
  font-weight: 800;
}

.panel-subtitle {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #64748b;
}

.panel-tag {
  font-size: 18rpx;
  color: #94a3b8;
  font-weight: 700;
}

.action-list {
  margin-top: 20rpx;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 22rpx 0;
}

.action-copy {
  flex: 1;
}

.action-title,
.field-label {
  display: block;
  font-size: 28rpx;
  color: #0f172a;
  font-weight: 800;
}

.action-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #94a3b8;
}

.field-card {
  margin-top: 22rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #fffaf5 0%, #ffffff 100%);
  border: 1rpx solid rgba(255, 102, 0, 0.08);
}

.field-input {
  width: 100%;
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #1f2937;
}

.danger-btn {
  width: 100%;
  min-height: 104rpx;
  margin-top: 24rpx;
  border: none;
  border-radius: 28rpx;
  background: linear-gradient(135deg, #f97316 0%, #ea580c 55%, #9a3412 100%);
  box-shadow: 0 22rpx 38rpx rgba(249, 115, 22, 0.24);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.danger-btn::after {
  border: none;
}

.danger-btn[disabled] {
  opacity: 0.7;
}

.danger-top,
.danger-bottom {
  color: #ffffff;
}

.danger-top {
  font-size: 20rpx;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.danger-bottom {
  font-size: 30rpx;
  font-weight: 700;
}

.tips-card {
  background:
    linear-gradient(145deg, rgba(255, 247, 237, 0.96), rgba(255, 255, 255, 0.98));
}

.tips-title {
  display: block;
  font-size: 30rpx;
  color: #0f172a;
  font-weight: 800;
}

.tips-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}
</style>
