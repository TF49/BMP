<template>
  <view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
    <view class="nav-bar-content">
      <view v-if="showBack" class="nav-bar-back" @click="handleBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <text class="nav-bar-title">{{ title }}</text>
      <view class="nav-bar-right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Props {
  title: string
  showBack?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showBack: true
})

const statusBarHeight = ref(0)

onMounted(() => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
})

const handleBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.navigateTo({
      url: '/pages/index/index'
    })
  }
}
</script>

<style lang="scss" scoped>
.nav-bar {
  background-color: #fff;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  
  .nav-bar-content {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    
    .nav-bar-back {
      position: absolute;
      left: 0;
      padding: 0 15px;
    }
    
    .nav-bar-title {
      font-size: 17px;
      font-weight: 500;
    }
    
    .nav-bar-right {
      position: absolute;
      right: 0;
      padding: 0 15px;
    }
  }
}
</style>
