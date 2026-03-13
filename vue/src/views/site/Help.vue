<template>
  <section class="page">
    <header class="head">
      <div class="kicker">帮助与反馈</div>
      <h1 class="title">常见问题与使用指引</h1>
      <p class="sub">
        官网页面无需登录即可访问；进入系统后按角色（会长/场馆/用户）自动分流到对应工作台。
      </p>
    </header>

    <div class="grid">
      <article class="panel">
        <h2 class="panel__title">快速开始</h2>
        <ol class="steps">
          <li>点击右上角 <strong>去登录</strong> 进入登录页（`/login`）。</li>
          <li>登录成功后，系统会根据角色自动跳转到管理端或用户端。</li>
          <li>如遇到权限不足，会提示并跳转到 403 页面。</li>
        </ol>
      </article>

      <article class="panel">
        <h2 class="panel__title">常见问题</h2>
        <div class="qa">
          <div class="qa__q">为什么我打开某些页面会回到登录？</div>
          <div class="qa__a">除 `/site*`、`/login`、`/register` 外，未登录访问会被路由守卫要求登录。</div>
        </div>
        <div class="qa">
          <div class="qa__q">普通用户能进管理端吗？</div>
          <div class="qa__a">不能。普通用户访问管理端路由会被重定向到用户端对应页面。</div>
        </div>
        <div class="qa">
          <div class="qa__q">我想修改官网内容（文案/价格/模块）</div>
          <div class="qa__a">直接编辑 `src/views/site/` 下对应页面即可，无需改后端。</div>
        </div>
      </article>
    </div>

    <div class="contact">
      <div class="contact__left">
        <div class="contact__title">需要进一步支持？</div>
        <div class="contact__desc">
          你可以通过以下方式联系我们，我们随时为您服务。
        </div>
        <div class="contact__info">
          <div class="contact__info-item">
            <strong>电话：</strong>
            <a :href="`tel:${contactInfo.phone}`">{{ contactInfo.phoneDisplay }}</a>
          </div>
          <div class="contact__info-item">
            <strong>邮箱：</strong>
            <a :href="`mailto:${contactInfo.email}`">{{ contactInfo.email }}</a>
          </div>
          <div class="contact__info-item">
            <strong>微信：</strong>
            <span>{{ contactInfo.wechat }}</span>
          </div>
          <div v-if="contactInfo.workingHours" class="contact__info-item">
            <strong>工作时间：</strong>
            <span>{{ contactInfo.workingHours }}</span>
          </div>
        </div>
      </div>
      <div class="contact__right">
        <router-link class="btn" to="/login">去登录</router-link>
        <router-link class="btn btn--ghost" to="/site">返回首页</router-link>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { contactInfo } from '@/config/contact'
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.head {
  padding: 0 0 8px;
}

.kicker {
  display: inline-block;
  padding: 8px 14px;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--site-accent);
  background: var(--site-accent-muted);
  border: 1px solid rgba(14, 165, 233, 0.25);
  border-radius: 999px;
}

.title {
  font-size: clamp(26px, 3.2vw, 32px);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--site-text);
  margin: 0 0 10px;
}

.sub {
  font-size: 16px;
  line-height: 1.65;
  color: var(--site-text-secondary);
  max-width: 60ch;
  margin: 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.panel {
  padding: 24px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
  box-shadow: var(--site-shadow);
}

.panel__title {
  font-size: 17px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 14px;
}

.steps {
  margin: 0;
  padding-left: 18px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--site-text-secondary);
}

.qa {
  margin-top: 14px;
  padding: 18px;
  background: var(--site-bg-elevated);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius);
}

.qa:first-of-type {
  margin-top: 0;
}

.qa__q {
  font-weight: 600;
  font-size: 14px;
  color: var(--site-text);
}

.qa__a {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--site-text-secondary);
}

.contact {
  padding: 28px 32px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.contact__title {
  font-weight: 700;
  font-size: 18px;
  color: var(--site-text);
  margin: 0 0 6px;
}

.contact__desc {
  font-size: 14px;
  color: var(--site-text-muted);
  line-height: 1.65;
  max-width: 60ch;
  margin: 0 0 16px;
}

.contact__info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.contact__info-item {
  font-size: 14px;
  color: var(--site-text-secondary);
}

.contact__info-item strong {
  color: var(--site-text);
  font-weight: 600;
  margin-right: 8px;
}

.contact__info-item a {
  color: var(--site-accent);
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.contact__info-item a:hover {
  opacity: 0.8;
  text-decoration: underline;
}

.contact__right {
  display: flex;
  gap: 12px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 20px;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: opacity 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  cursor: pointer;
}

.btn {
  color: #fff;
  background: var(--site-gradient);
  border: 0;
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.35);
}

.btn:hover {
  box-shadow: 0 8px 28px rgba(14, 165, 233, 0.4);
}

.btn:focus-visible {
  outline: 2px solid var(--site-accent);
  outline-offset: 2px;
}

.btn--ghost {
  color: var(--site-text);
  background: var(--site-surface-hover);
  border: 1px solid var(--site-border);
  box-shadow: none;
}

.btn--ghost:hover {
  background: var(--site-border);
}

@media (max-width: 1024px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .title {
    font-size: 24px;
  }
}
</style>

