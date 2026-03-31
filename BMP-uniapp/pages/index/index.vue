<template>
  <view>
    <PresidentIndex v-if="isPresident" />
    <MemberIndex v-else />
    <CustomTabBar :current="0" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import MemberIndex from './components/MemberIndex.vue'
import PresidentIndex from './components/PresidentIndex.vue'
import { useUserStore } from '@/store/modules/user'
import { isPresidentRole } from '@/utils/roleCheck'

const userStore = useUserStore()
const isPresident = computed(() => isPresidentRole(userStore.userInfo?.role))
</script>

<style scoped>
/* No specific styles needed since they are componentized */
</style>
