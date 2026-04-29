<template>
  <view>
    <PresidentIndex v-if="isManagement" />
    <MemberIndex v-else />
    <CustomTabBar v-if="!isManagement" :current="0" />
    <PresidentTabBar v-if="isManagement" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CustomTabBar from '@/components/CustomTabBar/CustomTabBar.vue'
import PresidentTabBar from '@/components/president/PresidentTabBar.vue'
import MemberIndex from './components/MemberIndex.vue'
import PresidentIndex from './components/PresidentIndex.vue'
import { useUserStore } from '@/store/modules/user'
import { isManagementRole } from '@/utils/roleCheck'

const userStore = useUserStore()
const isManagement = computed(() => isManagementRole(userStore.userInfo?.role))
</script>

<style scoped>
/* No specific styles needed since they are componentized */
</style>
