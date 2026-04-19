<template>
  <view class="member-tabbar" :style="{ paddingBottom: safeAreaBottom + 'px' }">
    <view class="member-tabbar-inner">
      <view
        v-for="(item, index) in memberTabList"
        :key="item.pagePath"
        class="tab-item"
        :class="{ active: isTabActive(index) }"
        @tap="switchTab(item.pagePath, index)"
      >
        <uni-icons
          :type="item.icon"
          :size="22"
          :color="isTabActive(index) ? activeColor : inactiveColor"
        />
        <text class="tab-text">{{ item.text }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getSafeSystemInfo } from '@/utils/systemInfo'

const props = defineProps({
  /** 当前高亮下标：0 首页 … 4 我的；传 -1 表示不高亮（如场馆等不在主栏的页面） */
  current: {
    type: Number,
    default: 0
  }
})

const safeAreaBottom = ref(0)
const activeColor = '#ea580c'
const inactiveColor = '#a1a1aa'

/** 与课程中心 / 赛事中心原型一致：首页、预约、培训、赛事、我的 */
const memberTabList = [
  { pagePath: '/pages/index/index', text: '首页', icon: 'home' },
  { pagePath: '/pages/booking/list', text: '预约', icon: 'calendar' },
  { pagePath: '/pages/course/list', text: '培训', icon: 'compose' },
  { pagePath: '/pages/tournament/list', text: '赛事', icon: 'medal' },
  { pagePath: '/pages/profile/index', text: '我的', icon: 'person' }
] as const

function isTabActive(index: number) {
  return props.current >= 0 && props.current === index
}

function syncSafeArea() {
  try {
    const systemInfo = getSafeSystemInfo()
    safeAreaBottom.value = systemInfo.safeAreaInsets?.bottom ?? 0
  } catch {
    safeAreaBottom.value = 0
  }
}

onMounted(() => {
  syncSafeArea()
})

onShow(() => {
  syncSafeArea()
})

function switchTab(url: string, index: number) {
  if (props.current === index && props.current >= 0) return
  if (!url || url === '#') return
  uni.reLaunch({ url })
}
</script>

<style lang="scss" scoped>
/* 容器形态对齐 components/president/PresidentTabBar.vue */
.member-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  padding: 24rpx 16rpx 48rpx;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 48rpx 48rpx 0 0;
  box-shadow: 0 -8rpx 40rpx rgba(0, 0, 0, 0.05);
  box-sizing: border-box;
}

.member-tabbar-inner {
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
  padding: 16rpx 20rpx;
  border-radius: 32rpx;
  color: #a1a1aa;
  transition: all 0.3s ease-out;
  min-width: 0;

  .tab-text {
    font-size: 20rpx;
    font-weight: 500;
    letter-spacing: 2rpx;
    margin-top: 8rpx;
    max-width: 120rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &.active {
    background-color: #ffedd5;
    color: #ea580c;
    transform: scale(1.05);

    .tab-text {
      font-weight: bold;
      color: #ea580c;
    }
  }
}
</style>
