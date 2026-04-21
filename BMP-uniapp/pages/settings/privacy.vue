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
              <text class="hero-side-label">PRIVACY MODE</text>
            </view>

            <view class="hero-copy">
              <text class="hero-eyebrow">DATA & VISIBILITY</text>
              <text class="hero-title">把个人可见性与数据权限收拢到一个地方</text>
              <text class="hero-subtitle">
                统一管理个人资料展示、推荐权限、社交入口与数据处理选项，让设置页从旧业务表单升级成 Stitch 风格的偏好中心。
              </text>
            </view>

            <view class="hero-grid">
              <view class="hero-metric">
                <text class="metric-label">VISIBLE</text>
                <text class="metric-value">{{ visibleCount }}/{{ visibleTotal }}</text>
              </view>
              <view class="hero-metric">
                <text class="metric-label">CACHE</text>
                <text class="metric-value">{{ cacheSize }}</text>
              </view>
            </view>
          </view>

          <view v-for="section in privacySections" :key="section.key" class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">{{ section.title }}</text>
                <text class="panel-subtitle">{{ section.subtitle }}</text>
              </view>
              <text class="panel-tag">{{ section.tag }}</text>
            </view>

            <view class="setting-list">
              <view
                v-for="item in section.items"
                :key="item.key"
                class="setting-row"
              >
                <view class="setting-copy">
                  <text class="setting-title">{{ item.title }}</text>
                  <text class="setting-desc">{{ item.description }}</text>
                </view>
                <switch
                  :checked="settings[item.key]"
                  color="#ea580c"
                  @change="toggleSetting(item.key, $event)"
                />
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">数据管理</text>
                <text class="panel-subtitle">处理缓存、导出数据与账号注销等操作。</text>
              </view>
              <text class="panel-tag">DATA CONTROL</text>
            </view>

            <view class="action-list">
              <view class="action-row" @tap="handleClearCache">
                <view class="action-copy">
                  <text class="action-title">清除缓存</text>
                  <text class="action-desc">当前缓存 {{ cacheSize }}，保留登录态和关键账户信息。</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>

              <view class="action-row" @tap="handleExportData">
                <view class="action-copy">
                  <text class="action-title">导出个人数据</text>
                  <text class="action-desc">下载你的个人资料、记录和偏好数据副本。</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>

              <view class="action-row danger-row" @tap="handleDeleteAccount">
                <view class="action-copy">
                  <text class="action-title danger-text">注销账户</text>
                  <text class="action-desc">永久删除账户和相关数据，该操作不可撤销。</text>
                </view>
                <uni-icons type="right" size="16" color="#f97316" />
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">隐私文件</text>
                <text class="panel-subtitle">查看平台隐私说明与用户协议内容。</text>
              </view>
              <text class="panel-tag">POLICY</text>
            </view>

            <view class="action-list">
              <view class="action-row" @tap="handleViewPrivacyPolicy">
                <view class="action-copy">
                  <text class="action-title">查看隐私政策</text>
                  <text class="action-desc">了解平台如何收集、使用和保护你的数据。</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>

              <view class="action-row" @tap="handleViewUserAgreement">
                <view class="action-copy">
                  <text class="action-title">查看用户协议</text>
                  <text class="action-desc">阅读使用本产品前需要了解的条款说明。</text>
                </view>
                <uni-icons type="right" size="16" color="#94a3b8" />
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'

const STORAGE_KEY = 'privacy_settings'

const settingsDefaults = {
  showRealName: false,
  showPhone: false,
  showBookingHistory: true,
  allowLocation: true,
  personalizedRecommendation: true,
  dataAnalytics: true,
  allowSearch: true,
  allowMessage: true
}

type PrivacySettings = typeof settingsDefaults
type PrivacySettingKey = keyof PrivacySettings

type PrivacySection = {
  key: string
  title: string
  subtitle: string
  tag: string
  items: Array<{
    key: PrivacySettingKey
    title: string
    description: string
  }>
}

const settings = reactive<PrivacySettings>({ ...settingsDefaults })
const cacheSize = ref('0 KB')
const userStore = useUserStore()

const privacySections: PrivacySection[] = [
  {
    key: 'identity',
    title: '个人信息可见性',
    subtitle: '决定他人能从你的资料里看到哪些身份信息。',
    tag: 'IDENTITY',
    items: [
      {
        key: 'showRealName',
        title: '展示真实姓名',
        description: '其他用户可以看到你的真实姓名信息。'
      },
      {
        key: 'showPhone',
        title: '展示手机号',
        description: '其他用户可以在资料页中查看你的手机号。'
      },
      {
        key: 'showBookingHistory',
        title: '展示预约记录',
        description: '允许他人查看你的公开预约和活动参与记录。'
      }
    ]
  },
  {
    key: 'data',
    title: '位置与数据',
    subtitle: '控制定位能力、个性化推荐和产品分析权限。',
    tag: 'DATA',
    items: [
      {
        key: 'allowLocation',
        title: '允许获取位置',
        description: '用于推荐附近的场馆、活动和服务。'
      },
      {
        key: 'personalizedRecommendation',
        title: '个性化推荐',
        description: '根据使用习惯推荐课程、赛事和器材内容。'
      },
      {
        key: 'dataAnalytics',
        title: '数据分析',
        description: '帮助我们理解使用行为并持续优化体验。'
      }
    ]
  },
  {
    key: 'social',
    title: '社交设置',
    subtitle: '决定你是否能被搜索以及是否接收来自他人的消息。',
    tag: 'SOCIAL',
    items: [
      {
        key: 'allowSearch',
        title: '允许被搜索',
        description: '其他用户可以通过昵称或资料搜索到你。'
      },
      {
        key: 'allowMessage',
        title: '接收私信',
        description: '允许其他用户向你发送站内私信和互动消息。'
      }
    ]
  }
]

const visibleKeys: PrivacySettingKey[] = ['showRealName', 'showPhone', 'showBookingHistory', 'allowSearch', 'allowMessage']
const visibleCount = computed(() => visibleKeys.filter(key => settings[key]).length)
const visibleTotal = visibleKeys.length

function saveSettings() {
  uni.setStorageSync(STORAGE_KEY, { ...settings })
}

function loadSettings() {
  try {
    const savedSettings = uni.getStorageSync(STORAGE_KEY) as Partial<PrivacySettings> | ''
    if (!savedSettings || typeof savedSettings !== 'object') return
    Object.assign(settings, savedSettings)
  } catch (error) {
    console.error('加载隐私设置失败:', error)
  }
}

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

function toggleSetting(key: PrivacySettingKey, event: any) {
  settings[key] = !!event?.detail?.value
  saveSettings()
}

function handleClearCache() {
  uni.showModal({
    title: '确认清除',
    content: '确定要清除缓存数据吗？登录状态和重要账户信息会被保留。',
    success: (res) => {
      if (!res.confirm) return
      try {
        const token = uni.getStorageSync('token')
        const userInfo = uni.getStorageSync('userInfo')
        const privacySettings = uni.getStorageSync(STORAGE_KEY)
        const notificationSettings = uni.getStorageSync('notification_settings')

        uni.clearStorageSync()

        if (token) uni.setStorageSync('token', token)
        if (userInfo) uni.setStorageSync('userInfo', userInfo)
        if (privacySettings) uni.setStorageSync(STORAGE_KEY, privacySettings)
        if (notificationSettings) uni.setStorageSync('notification_settings', notificationSettings)

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

function handleExportData() {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

function handleDeleteAccount() {
  uni.showModal({
    title: '确认注销',
    content: '注销后你的账户数据将被永久删除，且无法恢复。确定要继续吗？',
    confirmColor: '#f97316',
    success: (res) => {
      if (!res.confirm) return
      uni.showModal({
        title: '再次确认',
        content: '请再次确认你要注销账户，此操作不可撤销。',
        confirmColor: '#f97316',
        success: (res2) => {
          if (!res2.confirm) return
          uni.showToast({
            title: '功能开发中',
            icon: 'none'
          })
        }
      })
    }
  })
}

function handleViewPrivacyPolicy() {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

function handleViewUserAgreement() {
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
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

  loadSettings()
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

.setting-list,
.action-list {
  margin-top: 20rpx;
}

.setting-row,
.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.82);
}

.setting-row:last-child,
.action-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.setting-copy,
.action-copy {
  flex: 1;
}

.setting-title,
.action-title {
  display: block;
  font-size: 28rpx;
  color: #0f172a;
  font-weight: 800;
}

.setting-desc,
.action-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #94a3b8;
}

.danger-row {
  border-bottom: none;
}

.danger-text {
  color: #c2410c;
}
</style>
