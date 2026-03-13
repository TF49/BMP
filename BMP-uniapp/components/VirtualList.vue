<template>
  <view class="virtual-list">
    <view
      v-for="(item, index) in visibleItems"
      :key="item.id || index"
      class="virtual-list-item"
      :style="{ transform: `translateY(${getItemOffset(item)}px)` }"
    >
      <slot :item="item" :index="getItemIndex(item)" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface Props {
  items: any[]
  itemHeight: number
  bufferSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  bufferSize: 5
})

const scrollTop = ref(0)
const containerHeight = ref(0)

// 计算可见的项目
const visibleItems = computed(() => {
  const startIndex = Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.bufferSize)
  const endIndex = Math.min(
    props.items.length,
    Math.ceil((scrollTop.value + containerHeight.value) / props.itemHeight) + props.bufferSize
  )

  return props.items.slice(startIndex, endIndex)
})

// 获取项目的偏移量
const getItemOffset = (item: any) => {
  const index = props.items.indexOf(item)
  return index * props.itemHeight
}

// 获取项目的实际索引
const getItemIndex = (item: any) => {
  return props.items.indexOf(item)
}

// 处理滚动事件
const handleScroll = (e: any) => {
  scrollTop.value = e.detail.scrollTop
}

onMounted(() => {
  // 获取容器高度
  const query = uni.createSelectorQuery()
  query.select('.virtual-list').boundingClientRect((rect: any) => {
    if (rect) {
      containerHeight.value = rect.height
    }
  }).exec()
})
</script>

<style lang="scss" scoped>
.virtual-list {
  height: 100%;
  overflow-y: auto;
  position: relative;

  .virtual-list-item {
    position: absolute;
    left: 0;
    right: 0;
    width: 100%;
    will-change: transform;
  }
}
</style>
