<template>
  <view class="president-tabbar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
    <view class="president-tabbar-inner">
      <view
        v-for="(item, index) in tabList"
        :key="item.pagePath"
        class="tab-item"
        :class="{ active: isActive(item.pagePath) }"
        @click="switchTab(item.pagePath)"
      >
        <text class="tab-icon">{{ item.iconPath }}</text>
        <text class="tab-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { PRESIDENT_TAB_BAR_LIST } from '@/utils/presidentTabBar'
import { safeReLaunch } from '@/utils/safeRoute'
import { getSafeSystemInfo } from '@/utils/systemInfo'

const currentPath = ref('')
const safeAreaBottom = ref(0)
const tabList = PRESIDENT_TAB_BAR_LIST

function updateCurrentPath() {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as { route?: string }
  if (page?.route) {
    currentPath.value = page.route
  }
}

onMounted(() => {
  updateCurrentPath()
  try {
    const systemInfo = getSafeSystemInfo()
    safeAreaBottom.value = systemInfo.safeAreaInsets?.bottom ?? 0
  } catch {
    safeAreaBottom.value = 0
  }
})

onShow(() => {
  updateCurrentPath()
})

function isActive(pagePath: string): boolean {
  const path = pagePath.replace(/^\//, '')
  const cur = currentPath.value || ''
  if (cur === path) return true
  
  // Highlight for sub-pages
  if (path.includes('user/list')) return cur.includes('president/user')
  if (path.includes('venue/list')) return cur.includes('president/venue')
  if (path.includes('index/index')) return cur.includes('pages/index/index') || cur.includes('dashboard/index')
  
  return false
}

function switchTab(pagePath: string) {
  const url = '/' + pagePath
  safeReLaunch(url, '/pages/index/index')
}
</script>

<style lang="scss" scoped>
.president-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  padding: 24rpx 16rpx 48rpx;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 48rpx 48rpx 0 0;
  box-shadow: 0 -8rpx 40rpx rgba(0, 0, 0, 0.05);
}

.president-tabbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-around;
  width: 100%;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16rpx 24rpx;
  border-radius: 32rpx;
  color: #a1a1aa;
  transition: all 0.3s ease-out;

  .tab-icon {
    font-size: 40rpx;
    margin-bottom: 8rpx;
  }

  .tab-text {
    font-size: 20rpx;
    font-weight: 500;
    letter-spacing: 2rpx;
  }

  &.active {
    background-color: #ffedd5;
    color: #ea580c;
    transform: scale(1.05);

    .tab-text {
      font-weight: bold;
    }
  }
}
</style>
