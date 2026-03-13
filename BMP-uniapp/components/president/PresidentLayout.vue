<template>
  <view class="president-layout" :class="[backgroundColor, className]">
    <!-- 渐变背景 -->
    <view class="president-layout-bg">
      <view class="president-layout-gradient"></view>
    </view>
    <!-- 主要内容区域 -->
    <view class="president-layout-content">
      <slot></slot>
    </view>
    <!-- 会长端 TabBar（仅 tabBar 页显示） -->
    <PresidentTabBar v-if="showTabBar" />
  </view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import PresidentTabBar from './PresidentTabBar.vue'
import { useUserStore } from '@/store/modules/user'
import { isPresidentRole } from '@/utils/roleCheck'
import { safeReLaunch } from '@/utils/safeRoute'

interface Props {
  showTabBar?: boolean
  className?: string
  backgroundColor?: string
}

withDefaults(defineProps<Props>(), {
  showTabBar: true,
  className: '',
  backgroundColor: 'bg-president'
})

onMounted(() => {
  const userStore = useUserStore()
  if (!isPresidentRole(userStore.userRole)) {
    safeReLaunch('/pages/index/index', '/pages/index/index')
  }
})
</script>

<style lang="scss" scoped>
.president-layout {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

.president-layout-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  pointer-events: none;
}

.president-layout-gradient {
  width: 100%;
  height: 100%;
  background: linear-gradient(165deg, #f0fdf4 0%, #ecfdf5 20%, #f8fafc 50%, #f1f5f9 100%);
}

.president-layout-content {
  flex: 1;
  overflow-y: auto;
  position: relative;
  z-index: 1;
  padding-bottom: calc(env(safe-area-inset-bottom) + 56px);
}

.bg-president {
  /* 使用渐变背景 */
}
</style>
