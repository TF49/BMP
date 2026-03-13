<template>
  <div class="admin-profile-wrapper">
    <user-profile />
  </div>
</template>

<script setup lang="ts">
import UserProfile from './user/Profile.vue'
</script>

<style scoped lang="scss">
.admin-profile-wrapper {
  max-width: 960px;
  margin: 0 auto;
}
</style>

