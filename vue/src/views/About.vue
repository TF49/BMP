<template>
  <div class="about-page">
    <header class="about-header">
      <h1 class="page-title">关于我们</h1>
    </header>
    <el-card class="about-card" shadow="hover">
      <h2>羽毛球管理系统（BMP）</h2>
      <p class="version">版本：1.0</p>
      <p class="intro">
        本系统为羽毛球场馆与协会提供场地预约、器材租借、课程预约、赛事报名、穿线服务、会员与财务管理等功能，
        支持协会会长、场馆管理员与普通用户多种角色，便于统一运营与会员服务。
      </p>
      <p class="contact">如有合作或建议，请通过「帮助与反馈」与我们联系。</p>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped lang="scss">
.about-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 0 32px;
}
.about-header {
  margin-bottom: 16px;
}
.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #111827);
}
.about-card {
  border-radius: 16px;
  padding: 24px;
  h2 {
    margin: 0 0 8px 0;
    font-size: 18px;
  }
  .version {
    margin: 0 0 16px 0;
    font-size: 13px;
    color: var(--color-text-secondary, #6b7280);
  }
  .intro, .contact {
    margin: 0 0 12px 0;
    font-size: 14px;
    line-height: 1.6;
    color: var(--color-text-secondary, #4b5563);
  }
}
</style>
