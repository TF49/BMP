<template>
  <view class="pagination">
    <view class="nav-btn" :class="{ disabled: modelValue <= 1 }" @click="goPrev">
      <uni-icons type="left" size="22" :color="modelValue <= 1 ? '#c8c6c5' : '#5f5e5e'" />
    </view>
    <view class="pages">
      <view
        v-for="p in pages"
        :key="p"
        class="page-btn"
        :class="{ active: p === modelValue }"
        @click="emit('update:modelValue', p)"
      >
        <text class="page-num" :class="{ 'num-active': p === modelValue }">{{ p }}</text>
      </view>
    </view>
    <view class="nav-btn" :class="{ disabled: modelValue >= totalPages }" @click="goNext">
      <uni-icons type="right" size="22" :color="modelValue >= totalPages ? '#c8c6c5' : '#5f5e5e'" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: number
  totalPages: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: number): void
}>()

const pages = computed(() =>
  Array.from({ length: Math.max(1, props.totalPages) }, (_, i) => i + 1)
)

function goPrev() {
  if (props.modelValue <= 1) return
  emit('update:modelValue', props.modelValue - 1)
}

function goNext() {
  if (props.modelValue >= props.totalPages) return
  emit('update:modelValue', props.modelValue + 1)
}
</script>

<style lang="scss" scoped>
.pagination {
  margin-top: 48rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 28rpx;
}
.nav-btn {
  padding: 12rpx;
  border-radius: 9999px;
}
.nav-btn:active:not(.disabled) {
  background: #e8e8e8;
}
.nav-btn.disabled {
  opacity: 0.5;
}
.pages {
  display: flex;
  flex-direction: row;
  gap: 16rpx;
}
.page-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(26, 28, 28, 0.06);
}
.page-btn.active {
  background: #ff6600;
}
.page-num {
  font-size: 28rpx;
  font-weight: 800;
  color: #5f5e5e;
}
.num-active {
  color: #ffffff;
}
</style>
