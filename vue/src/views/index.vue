<template>
  <div class="dashboard-container">
    <div class="dashboard-content">
      <h1>欢迎使用羽擎</h1>
      <p>登录成功，正在跳转到首页...</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Index',
  mounted() {
    setTimeout(() => {
      this.$router.push('/')
    }, 2000)
  }
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-background, #0f2027) 0%, var(--color-primary-dark, #203a43) 50%, var(--color-primary, #2c5364) 100%);
}
.dashboard-content {
  text-align: center;
  color: var(--color-text-primary, #ffffff);
}
.dashboard-content h1 {
  font-size: 32px;
  margin-bottom: 16px;
  color: var(--color-text-primary, #ffffff);
}
.dashboard-content p {
  font-size: 16px;
  opacity: 0.8;
  color: var(--color-text-secondary, #ffffff);
}
</style>
