<template>
  <view class="president-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="president-navbar-inner glass-card">
      <view v-if="showBack" class="navbar-back" @click="handleBack">
        <uni-icons type="left" size="22" color="#1E293B"></uni-icons>
      </view>
      <text class="navbar-title">{{ title }}</text>
      <view class="navbar-right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeReLaunch } from '@/utils/safeRoute'
import { safeNavigateBack } from '@/utils/navigation'

interface Props {
  title: string
  showBack?: boolean
}

withDefaults(defineProps<Props>(), {
  showBack: true
})

const statusBarHeight = ref(0)

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
})

function handleBack() {
  const pages = getCurrentPages()
  if (pages.length <= 1) {
    safeReLaunch(PRESIDENT_PAGES.DASHBOARD, PRESIDENT_PAGES.DASHBOARD)
    return
  }
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}
</script>

<style lang="scss" scoped>
.president-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 0 24rpx 0;
}

.president-navbar-inner {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin: 16rpx 0;
  border-radius: 24rpx;
  background: var(--glass-bg, rgba(255, 255, 255, 0.85));
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  border: 1rpx solid var(--glass-border, rgba(226, 232, 240, 0.9));
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.navbar-back {
  position: absolute;
  left: 24rpx;
  padding: 12rpx;
}

.navbar-title {
  font-size: 34rpx;
  font-weight: 600;
  color: var(--color-text, #1E293B);
}

.navbar-right {
  position: absolute;
  right: 24rpx;
}
</style>
