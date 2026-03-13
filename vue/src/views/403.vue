<template>
  <div class="error-page">
    <el-result
      icon="warning"
      title="403"
      sub-title="抱歉，您没有权限访问此页面"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
</script>

<style scoped>
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: var(--color-background, #f0f2f5);
}
.error-page :deep(.el-result__title) {
  color: var(--color-text-primary, #1E293B);
}
.error-page :deep(.el-result__subtitle) {
  color: var(--color-text-secondary, #64748B);
}
</style>
