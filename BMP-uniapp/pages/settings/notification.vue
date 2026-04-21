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
              <text class="hero-title">统一管理你的通知接收偏好</text>
              <text class="hero-subtitle">
                预订、支付、系统消息和免打扰时段都收纳在一套 Stitch 风格的设置界面里，减少旧式设置页的割裂感。
              </text>
            </view>

            <view class="hero-grid">
              <view class="hero-metric">
                <text class="metric-label">ACTIVE</text>
                <text class="metric-value">{{ enabledCount }}/{{ totalSwitches }}</text>
              </view>
              <view class="hero-metric">
                <text class="metric-label">QUIET HOURS</text>
                <text class="metric-value">{{ settings.dndStart }} - {{ settings.dndEnd }}</text>
              </view>
            </view>
          </view>

          <view v-for="section in notificationSections" :key="section.key" class="panel-card">
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
                <text class="panel-title">通知时段</text>
                <text class="panel-subtitle">为夜间休息预留一个安静窗口。</text>
              </view>
              <text class="panel-tag">DO NOT DISTURB</text>
            </view>

            <view class="time-grid">
              <picker mode="time" :value="settings.dndStart" @change="onDNDStartChange">
                <view class="time-box">
                  <text class="time-label">开始时间</text>
                  <text class="time-value">{{ settings.dndStart }}</text>
                </view>
              </picker>
              <picker mode="time" :value="settings.dndEnd" @change="onDNDEndChange">
                <view class="time-box">
                  <text class="time-label">结束时间</text>
                  <text class="time-value">{{ settings.dndEnd }}</text>
                </view>
              </picker>
            </view>
          </view>

          <view class="panel-card tips-card">
            <text class="tips-title">通知建议</text>
            <text class="tips-text">
              为了获得完整提醒体验，建议同时开启系统推送权限、应用内提示和声音提醒。你也可以在手机系统设置里进一步控制通知样式。
            </text>
          </view>
        </view>
      </scroll-view>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useUserStore } from '@/store/modules/user'
import MobileLayout from '@/components/MobileLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'

const STORAGE_KEY = 'notification_settings'

const settingsDefaults = {
  bookingSuccess: true,
  bookingChange: true,
  bookingReminder: true,
  paymentSuccess: true,
  balanceChange: true,
  systemMessage: true,
  eventPush: true,
  promotion: false,
  pushNotifications: true,
  soundAlert: true,
  vibrationAlert: true,
  dndStart: '22:00',
  dndEnd: '08:00'
}

type NotificationSettings = typeof settingsDefaults
type NotificationSettingKey = keyof NotificationSettings
type BooleanSettingKey = {
  [K in NotificationSettingKey]: NotificationSettings[K] extends boolean ? K : never
}[NotificationSettingKey]

type NotificationSection = {
  key: string
  title: string
  subtitle: string
  tag: string
  items: Array<{
    key: BooleanSettingKey
    title: string
    description: string
  }>
}

const settings = reactive<NotificationSettings>({ ...settingsDefaults })

const notificationSections: NotificationSection[] = [
  {
    key: 'booking',
    title: '预订通知',
    subtitle: '保障预约成功、变更和开场前提醒都能及时送达。',
    tag: 'BOOKING',
    items: [
      {
        key: 'bookingSuccess',
        title: '预约成功通知',
        description: '下单成功后立即收到确认提醒。'
      },
      {
        key: 'bookingChange',
        title: '预约变更通知',
        description: '时间、场地或状态变化时第一时间通知你。'
      },
      {
        key: 'bookingReminder',
        title: '预约提醒',
        description: '在开始前推送提醒，避免错过场地或课程。'
      }
    ]
  },
  {
    key: 'payment',
    title: '支付通知',
    subtitle: '聚焦订单支付、余额变更和资金相关提醒。',
    tag: 'PAYMENT',
    items: [
      {
        key: 'paymentSuccess',
        title: '支付成功通知',
        description: '支付完成后返回结果并同步到账提示。'
      },
      {
        key: 'balanceChange',
        title: '余额变动通知',
        description: '钱包充值、消费或退款时同步变更信息。'
      }
    ]
  },
  {
    key: 'system',
    title: '系统通知',
    subtitle: '控制平台消息、活动提醒和营销类推送强度。',
    tag: 'SYSTEM',
    items: [
      {
        key: 'systemMessage',
        title: '系统消息',
        description: '保留账户、服务和订单相关的重要站内消息。'
      },
      {
        key: 'eventPush',
        title: '活动推送',
        description: '接收赛事、课程和运营活动的上新提醒。'
      },
      {
        key: 'promotion',
        title: '优惠活动',
        description: '在折扣、满减和会员福利更新时收到通知。'
      }
    ]
  },
  {
    key: 'device',
    title: '提醒方式',
    subtitle: '决定消息是否以推送、声音或震动方式打扰你。',
    tag: 'DEVICE',
    items: [
      {
        key: 'pushNotifications',
        title: '接收推送通知',
        description: '允许应用在系统层级向你发送通知横幅。'
      },
      {
        key: 'soundAlert',
        title: '声音提醒',
        description: '收到关键通知时播放提示音。'
      },
      {
        key: 'vibrationAlert',
        title: '震动提醒',
        description: '收到通知时触发轻微震动反馈。'
      }
    ]
  }
]

const userStore = useUserStore()

const booleanKeys: BooleanSettingKey[] = [
  'bookingSuccess',
  'bookingChange',
  'bookingReminder',
  'paymentSuccess',
  'balanceChange',
  'systemMessage',
  'eventPush',
  'promotion',
  'pushNotifications',
  'soundAlert',
  'vibrationAlert'
]

const enabledCount = computed(() => booleanKeys.filter(key => settings[key]).length)
const totalSwitches = booleanKeys.length

function saveSettings() {
  uni.setStorageSync(STORAGE_KEY, { ...settings })
}

function loadSettings() {
  try {
    const savedSettings = uni.getStorageSync(STORAGE_KEY) as Partial<NotificationSettings> | ''
    if (!savedSettings || typeof savedSettings !== 'object') return
    Object.assign(settings, savedSettings)
  } catch (error) {
    console.error('加载通知设置失败:', error)
  }
}

function toggleSetting(key: BooleanSettingKey, event: any) {
  const nextValue = !!event?.detail?.value
  settings[key] = nextValue
  saveSettings()
}

function onDNDStartChange(event: { detail?: { value?: string } }) {
  settings.dndStart = event?.detail?.value || settings.dndStart
  saveSettings()
}

function onDNDEndChange(event: { detail?: { value?: string } }) {
  settings.dndEnd = event?.detail?.value || settings.dndEnd
  saveSettings()
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

.hero-metric,
.time-box {
  padding: 20rpx;
  border-radius: 24rpx;
  background: linear-gradient(180deg, #ffffff 0%, #fff7ed 100%);
  border: 1rpx solid rgba(251, 146, 60, 0.16);
  box-sizing: border-box;
}

.metric-label,
.time-label {
  display: block;
  margin-bottom: 12rpx;
  font-size: 18rpx;
  color: #c2410c;
  font-weight: 800;
}

.metric-value,
.time-value {
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

.setting-list {
  margin-top: 20rpx;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
  padding: 22rpx 0;
  border-bottom: 1rpx solid rgba(226, 232, 240, 0.82);
}

.setting-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
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

.time-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  margin-top: 24rpx;
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
