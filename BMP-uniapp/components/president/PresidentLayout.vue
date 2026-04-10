<template>
  <view :class="rootClassList">
    <view class="president-layout-bg">
      <view class="president-layout-gradient" :style="gradientStyle" />
    </view>
    <view class="president-layout-content">
      <slot></slot>
    </view>
    <PresidentTabBar v-if="showTabBar" />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import PresidentTabBar from './PresidentTabBar.vue'
import { useUserStore } from '@/store/modules/user'
import { isPresidentRole } from '@/utils/roleCheck'
import { safeReLaunch } from '@/utils/safeRoute'

const props = withDefaults(
  defineProps<{
    showTabBar?: boolean
    className?: string
    backgroundColor?: string
  }>(),
  {
    showTabBar: true,
    className: '',
    backgroundColor: 'bg-president'
  }
)

function isCssBackgroundLiteral(s: string): boolean {
  const t = s.trim()
  if (!t) return false
  if (/^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/.test(t)) return true
  if (/^rgb(a)?\(/i.test(t)) return true
  if (/^linear-gradient\(/i.test(t)) return true
  return false
}

const gradientStyle = computed(() => {
  if (isCssBackgroundLiteral(props.backgroundColor)) {
    return { background: props.backgroundColor.trim() }
  }
  return {}
})

const rootClassList = computed(() => {
  const list = ['president-layout']
  if (props.className) list.push(props.className)
  if (!isCssBackgroundLiteral(props.backgroundColor) && props.backgroundColor) {
    list.push(props.backgroundColor)
  }
  return list
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
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

.president-layout-gradient {
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at top left, rgba(251, 146, 60, 0.18), transparent 36%),
    linear-gradient(165deg, #fff7ef 0%, #ffedd5 24%, #faf6f1 58%, #f7f4ef 100%);
}

.president-layout-content {
  flex: 1;
  overflow-y: auto;
  position: relative;
  z-index: 1;
  padding-bottom: calc(env(safe-area-inset-bottom) + 128rpx);
}

.bg-president {
  /* semantic marker */
}
</style>
