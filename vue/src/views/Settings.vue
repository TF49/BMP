<template>
  <div class="admin-settings-wrapper">
    <user-settings />
  </div>
</template>

<script setup lang="ts">
import UserSettings from './user/Settings.vue'
</script>

<style scoped lang="scss">
.admin-settings-wrapper {
  max-width: 960px;
  margin: 0 auto;
}
</style>

