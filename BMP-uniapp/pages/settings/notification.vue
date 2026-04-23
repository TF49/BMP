<template>
  <MobileLayout className="notification-shell">
    <view class="notification-page">
      <scroll-view scroll-y class="page-scroll" :show-scrollbar="false">
        <view class="content">
          <view class="hero-card">
            <view class="hero-top">
              <view class="hero-brand" @tap="handleBack">
                <uni-icons type="left" size="18" color="#a33e00" />
                <text class="hero-brand-text">KINETIC LOGIC</text>
              </view>
              <text class="hero-side-label">NOTIFY CONTROL</text>
            </view>

            <view class="hero-copy">
              <text class="hero-eyebrow">ALERT PREFERENCES</text>
              <text class="hero-title">只保留当前真实可保存的通知能力</text>
              <text class="hero-subtitle">
                目前小程序与网页端统一使用后端 `siteMessage` 开关，控制重要通知是否在站内展示。
              </text>
            </view>

            <view class="hero-grid">
              <view class="hero-metric">
                <text class="metric-label">SYNC</text>
                <text class="metric-value">与网页端设置同步</text>
              </view>
              <view class="hero-metric">
                <text class="metric-label">STATUS</text>
                <text class="metric-value">{{ settings.siteMessage ? '已开启' : '已关闭' }}</text>
              </view>
            </view>
          </view>

          <view class="panel-card">
            <view class="panel-head">
              <view>
                <text class="panel-title">站内消息</text>
                <text class="panel-subtitle">控制通知铃铛、站内提醒和登录后的重要消息展示。</text>
              </view>
              <text class="panel-tag">REAL CAPABILITY</text>
            </view>

            <view class="setting-row">
              <view class="setting-copy">
                <text class="setting-title">接收站内消息</text>
                <text class="setting-desc">保存到后端后，网页端与小程序端会共享同一状态。</text>
              </view>
              <switch
                :checked="settings.siteMessage"
                :disabled="loading || saving"
                color="#ea580c"
                @change="handleToggle"
              />
            </view>
          </view>

          <view class="panel-card tips-card">
            <text class="tips-title">说明</text>
            <text class="tips-text">
              推送通知、免打扰、声音和震动提醒属于移动端扩展能力，当前后端未提供对应保存字段，因此这里不再保留假保存开关。
            </text>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { getSettings, updateSettings } from '@/api/auth'
import { safeNavigateBack } from '@/utils/navigation'

const userStore = useUserStore()
const loading = ref(true)
const saving = ref(false)
const settings = reactive({
  siteMessage: true
})

async function loadSettings() {
  loading.value = true
  try {
    const data = await getSettings()
    settings.siteMessage = data.siteMessage ?? true
  } catch (error) {
    console.error('加载通知设置失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleToggle(event: any) {
  const nextValue = !!event?.detail?.value
  const previousValue = settings.siteMessage
  settings.siteMessage = nextValue
  saving.value = true

  try {
    await updateSettings({ siteMessage: nextValue })
    uni.showToast({
      title: '设置已保存',
      icon: 'success'
    })
  } catch (error) {
    settings.siteMessage = previousValue
    uni.showToast({
      title: error instanceof Error ? error.message : '保存失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    saving.value = false
  }
}

function handleBack() {
  safeNavigateBack('/pages/settings/index')
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    uni.redirectTo({
      url: '/pages/login/login'
    })
    return
  }

  await loadSettings()
})
</script>

<style lang="scss" scoped>
.notification-shell {
  background:
    radial-gradient(circle at top left, rgba(255, 170, 112, 0.28), transparent 28%),
    radial-gradient(circle at top right, rgba(251, 146, 60, 0.16), transparent 22%),
    linear-gradient(180deg, #fff7ed 0%, #f8fafc 36%, #eef2ff 100%);
}

.notification-page {
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

.setting-row {
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.setting-copy {
  flex: 1;
}

.setting-title {
  display: block;
  font-size: 28rpx;
  color: #0f172a;
  font-weight: 800;
}

.setting-desc {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #94a3b8;
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

.tips-text {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.7;
  color: #64748b;
}
</style>
