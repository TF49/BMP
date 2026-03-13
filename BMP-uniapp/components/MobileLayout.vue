<template>
  <view class="mobile-layout" :class="[backgroundColor, className]">
    <!-- 主要内容区域 -->
    <view class="mobile-layout-content">
      <slot></slot>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  showTabBar?: boolean
  className?: string
  backgroundColor?: string
}

withDefaults(defineProps<Props>(), {
  showTabBar: true,
  className: '',
  backgroundColor: 'bg-background'
})
</script>

<style lang="scss" scoped>
.mobile-layout {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-background, #F8FAFC);
}

.mobile-layout-content {
  flex: 1;
  overflow-y: auto;
}

.bg-background {
  background-color: #F8FAFC;
}

.bg-white {
  background-color: #ffffff;
}

.bg-muted {
  background-color: #f1f5f9;
}
</style>
