<template>
  <view class="brand-logo" :class="{ 'brand-logo--floating': floating }" :style="brandStyle">
    <view class="brand-logo-avatar">
      <image class="brand-logo-avatar-image" src="/static/placeholders/avatar.svg" mode="aspectFill" />
    </view>
    <text class="brand-logo-text">Kinetic Logic</text>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getSafeSystemInfo } from '@/utils/systemInfo'

interface Props {
  width?: string
  floating?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  width: '208rpx',
  floating: false
})

const statusBarHeight = ref(0)

onMounted(() => {
  statusBarHeight.value = getSafeSystemInfo().statusBarHeight || 0
})

const brandStyle = computed(() => ({
  '--brand-width': props.width,
  top: props.floating ? `${statusBarHeight.value + 12}px` : undefined
}))
</script>

<style scoped lang="scss">
.brand-logo {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
  width: var(--brand-width);
}

.brand-logo--floating {
  position: fixed;
  left: 24rpx;
  z-index: 1200;
}

.brand-logo-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 999rpx;
  overflow: hidden;
  flex-shrink: 0;
}

.brand-logo-avatar-image {
  width: 100%;
  height: 100%;
  display: block;
}

.brand-logo-text {
  flex: 1;
  min-width: 0;
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #1a1a1a;
  white-space: nowrap;
}
</style>
