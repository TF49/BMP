<template>
  <view class="president-tabbar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
    <view class="president-tabbar-inner glass-card">
      <view
        v-for="(item, index) in tabList"
        :key="item.pagePath"
        class="tab-item"
        :class="{ active: isActive(item.pagePath) }"
        @click="switchTab(item.pagePath)"
      >
        <image
          class="tab-icon"
          :src="isActive(item.pagePath) ? item.selectedIconPath : item.iconPath"
          mode="aspectFit"
        />
        <view class="tab-text-row">
          <uni-icons
            v-if="item.extraIconType"
            :type="item.extraIconType"
            size="16"
            :color="isActive(item.pagePath) ? '#3cc51f' : '#475569'"
          ></uni-icons>
          <text class="tab-text">{{ item.text }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { PRESIDENT_TAB_BAR_LIST } from '@/utils/presidentTabBar'
import { safeReLaunch } from '@/utils/safeRoute'

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
    const systemInfo = uni.getSystemInfoSync()
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
  // 子页面高亮对应 tab：user/detail、user/form 高亮「用户」；venue、finance 同理
  if (path.includes('/user/list')) return cur.startsWith('pages/president/user')
  if (path.includes('/venue/list')) return cur.startsWith('pages/president/venue')
  if (path.includes('/finance/list')) return cur.startsWith('pages/president/finance')
  return false
}

function switchTab(pagePath: string) {
  const url = '/' + pagePath
  safeReLaunch(url, '/pages/president/dashboard/index')
}
</script>

<style lang="scss" scoped>
.president-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  padding: 0 16rpx 0;
}

.president-tabbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 100rpx;
  border-radius: 24rpx;
  margin: 0 16rpx;
  background: var(--glass-bg, rgba(255, 255, 255, 0.85));
  backdrop-filter: blur(20rpx);
  -webkit-backdrop-filter: blur(20rpx);
  border: 1rpx solid var(--glass-border, rgba(226, 232, 240, 0.9));
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.06);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx 0;

  .tab-icon {
    /* tabbar 原点来自这里的 PNG（tab-*-active.png 等），去掉以保持更简洁的视觉 */
    display: none;
    width: 44rpx;
    height: 44rpx;
    margin-bottom: 4rpx;
  }

  .tab-text-row {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4rpx;
  }

  .tab-text {
    font-size: 20rpx;
    color: #475569;
    transition: color 0.2s;
  }

  &.active .tab-text {
    color: #3cc51f;
    font-weight: 500;
  }
}
</style>
