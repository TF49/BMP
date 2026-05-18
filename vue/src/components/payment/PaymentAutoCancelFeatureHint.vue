<template>
  <el-alert
    v-if="configLoaded && !autoCancelEnabled"
    type="info"
    :closable="false"
    show-icon
    class="payment-auto-cancel-feature-hint"
    title="支付超时自动取消功能未开启，订单将不会自动取消"
  />
</template>

<script setup>
defineProps({
  configLoaded: {
    type: Boolean,
    default: false
  },
  autoCancelEnabled: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.payment-auto-cancel-feature-hint {
  margin-bottom: 16px;
}
</style>
