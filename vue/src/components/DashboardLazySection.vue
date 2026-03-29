<template>
  <div ref="rootRef" class="dashboard-lazy-section">
    <div
      v-if="!showContent"
      class="dashboard-lazy-placeholder"
      :style="{ minHeight: minHeight }"
      aria-hidden="true"
    >
      <el-skeleton :rows="skeletonRows" animated />
      <span class="dashboard-lazy-hint">{{ hint }}</span>
    </div>
    <div v-else class="dashboard-lazy-inner">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  /** 占位最小高度，减轻布局跳动 */
  minHeight: { type: String, default: '480px' },
  skeletonRows: { type: Number, default: 8 },
  hint: { type: String, default: '图表区域将在进入视口时加载' },
  /** 提前量：距视口多少像素开始加载 */
  rootMargin: { type: String, default: '280px 0px' }
})

const rootRef = ref(null)
const showContent = ref(false)

let observer = null

onMounted(() => {
  if (typeof IntersectionObserver === 'undefined') {
    showContent.value = true
    return
  }
  observer = new IntersectionObserver(
    (entries) => {
      const hit = entries.some((e) => e.isIntersecting)
      if (hit) {
        showContent.value = true
        observer?.disconnect()
        observer = null
      }
    },
    { root: null, rootMargin: props.rootMargin, threshold: 0.01 }
  )
  if (rootRef.value) {
    observer.observe(rootRef.value)
  }
})

onUnmounted(() => {
  observer?.disconnect()
})
</script>

<style scoped>
.dashboard-lazy-section {
  width: 100%;
}

.dashboard-lazy-placeholder {
  padding: 24px;
  border-radius: 16px;
  border: 1px dashed var(--el-border-color-lighter, #e4e7ed);
  background: var(--el-fill-color-blank, #fafafa);
  box-sizing: border-box;
}

.dashboard-lazy-hint {
  display: block;
  margin-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
}

.dashboard-lazy-inner {
  width: 100%;
}
</style>
