<template>
  <view class="bottom-nav">
    <view
      v-for="item in items"
      :key="item.key"
      class="nav-item"
      @tap="handleNavigate(item.key)"
    >
      <view class="nav-icon" :class="{ 'nav-icon-active': current === item.key }">
        <uni-icons :type="item.icon" size="22" :color="current === item.key ? '#ffffff' : '#5f5e5e'" />
      </view>
      <text class="nav-label" :class="{ 'nav-label-active': current === item.key }">{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
type CoachTabKey = 'home' | 'schedule' | 'courses' | 'bookings' | 'profile'

const props = defineProps<{
  current: CoachTabKey
}>()

const items: Array<{ key: Exclude<CoachTabKey, 'schedule'>; label: string; icon: string; url: string }> = [
  { key: 'home', label: '首页', icon: 'home-filled', url: '/pages/coach/index' },
  { key: 'courses', label: '课程', icon: 'compose', url: '/pages/coach/courses' },
  { key: 'bookings', label: '预约', icon: 'list', url: '/pages/coach/bookings' },
  { key: 'profile', label: '我的', icon: 'person', url: '/pages/coach/profile' }
]

function handleNavigate(key: CoachTabKey) {
  if (props.current === key) return
  const target = items.find((item) => item.key === key)
  if (!target) return

  uni.reLaunch({
    url: target.url
  })
}
</script>

<style lang="scss" scoped>
.bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 50;
  background: #ffffff;
  box-shadow: 0 -8rpx 30rpx rgba(26, 28, 28, 0.06);
  border-radius: 34rpx 34rpx 0 0;
  padding: 18rpx 20rpx calc(28rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
}

.nav-item {
  min-width: 80rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.nav-icon {
  width: 68rpx;
  height: 68rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-icon-active {
  background: #ff6600;
  box-shadow: 0 10rpx 26rpx rgba(255, 102, 0, 0.25);
}

.nav-label {
  font-size: 18rpx;
  color: #5f5e5e;
}

.nav-label-active {
  color: #ff6600;
  font-weight: 800;
}
</style>
