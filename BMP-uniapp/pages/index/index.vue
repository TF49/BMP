<template>
  <view>
    <PresidentIndex v-if="isPresident" />
    <MemberIndex v-else />
    <!-- 会长端底部栏；会员端首页不设 TabBar，与课程/赛事中心一致，由主栏页面承载 -->
    <PresidentTabBar v-if="isPresident" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PresidentTabBar from '@/components/president/PresidentTabBar.vue'
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
