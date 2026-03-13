<template>
  <section class="page">
    <header class="head">
      <div class="kicker">方案与版本</div>
      <h1 class="title">从校园到商业运营，都有合适的配置</h1>
      <p class="sub">这里提供的是“官网展示用”的方案示例，你后续可以按实际业务改文案与功能点。</p>
    </header>

    <div class="plans">
      <article class="plan">
        <div class="plan__top">
          <div class="plan__name">校园版</div>
          <div class="plan__tag">轻量启动</div>
        </div>
        <div class="plan__price">
          <span class="plan__num">¥0</span>
          <span class="plan__unit">/试用</span>
        </div>
        <ul class="plan__list">
          <li>基础预约与核销</li>
          <li>会员与通知</li>
          <li>基础数据看板</li>
        </ul>
        <router-link class="btn" to="/login">去登录</router-link>
      </article>

      <article class="plan plan--hot">
        <div class="plan__glow" aria-hidden="true" />
        <div class="plan__top">
          <div class="plan__name">协会版</div>
          <div class="plan__tag plan__tag--hot">推荐</div>
        </div>
        <div class="plan__price">
          <span class="plan__num">¥399</span>
          <span class="plan__unit">/月</span>
        </div>
        <ul class="plan__list">
          <li>预约/器材/服务全流程</li>
          <li>课程/赛事运营工具</li>
          <li>财务与数据分析</li>
          <li>多角色权限管理</li>
        </ul>
        <router-link class="btn btn--primary" to="/login">立即使用</router-link>
      </article>

      <article class="plan">
        <div class="plan__top">
          <div class="plan__name">商业版</div>
          <div class="plan__tag">规模化运营</div>
        </div>
        <div class="plan__price">
          <span class="plan__num">定制</span>
          <span class="plan__unit">/按需</span>
        </div>
        <ul class="plan__list">
          <li>多场馆/多门店管理</li>
          <li>更细粒度的报表与审计</li>
          <li>运营自动化与扩展能力</li>
        </ul>
        <router-link class="btn" to="/site/help">联系与支持</router-link>
      </article>
    </div>

    <div class="faq">
      <div class="faq__head">
        <h2 class="faq__title">常见问题</h2>
        <p class="faq__desc">你也可以在“帮助”页面看到更多说明。</p>
      </div>

      <div class="faq__grid">
        <div class="qa">
          <div class="qa__q">是否支持普通用户与管理端分离？</div>
          <div class="qa__a">支持。项目里已有用户端与管理端路由组，并按角色做重定向与权限控制。</div>
        </div>
        <div class="qa">
          <div class="qa__q">官网页面会不会被强制跳到登录？</div>
          <div class="qa__a">不会。`/site*` 已加入未登录白名单，可直接访问。</div>
        </div>
        <div class="qa">
          <div class="qa__q">价格是不是固定？</div>
          <div class="qa__a">这里是展示用模板，你可以根据实际情况替换为真实方案与售卖方式。</div>
        </div>
        <div class="qa">
          <div class="qa__q">我只想要一个官网入口？</div>
          <div class="qa__a">未登录访问根路径 `/` 会自动跳到 `/site`，作为默认入口。</div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts"></script>

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

.plans {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.plan {
  position: relative;
  padding: 24px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
  box-shadow: var(--site-shadow);
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.plan--hot {
  border-color: rgba(14, 165, 233, 0.35);
  box-shadow: 0 0 0 1px rgba(14, 165, 233, 0.2), var(--site-shadow);
}

.plan__glow {
  position: absolute;
  inset: -2px;
  border-radius: calc(var(--site-radius-lg) + 2px);
  background: var(--site-gradient-soft);
  opacity: 0.5;
  z-index: 0;
}

.plan > * {
  position: relative;
  z-index: 1;
}

.plan__top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.plan__name {
  font-size: 17px;
  font-weight: 700;
  color: var(--site-text);
}

.plan__tag {
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--site-surface-hover);
  border: 1px solid var(--site-border);
  color: var(--site-text-secondary);
}

.plan__tag--hot {
  color: #fff;
  background: var(--site-gradient);
  border: 0;
}

.plan__price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.plan__num {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--site-text);
}

.plan__unit {
  font-size: 13px;
  color: var(--site-text-muted);
}

.plan__list {
  margin: 0;
  padding-left: 18px;
  font-size: 14px;
  line-height: 1.65;
  color: var(--site-text-secondary);
}

.btn {
  margin-top: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 20px;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  color: var(--site-text);
  background: var(--site-surface-hover);
  border: 1px solid var(--site-border);
  transition: opacity 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  cursor: pointer;
}

.btn:hover {
  opacity: 0.95;
  background: var(--site-border);
}

.btn:focus-visible {
  outline: 2px solid var(--site-accent);
  outline-offset: 2px;
}

.btn--primary {
  color: #fff;
  border: 0;
  background: var(--site-gradient);
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.35);
}

.btn--primary:hover {
  box-shadow: 0 8px 28px rgba(14, 165, 233, 0.4);
}

.faq {
  padding: 28px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
}

.faq__head {
  margin-bottom: 20px;
}

.faq__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 6px;
}

.faq__desc {
  font-size: 14px;
  color: var(--site-text-muted);
  margin: 0;
}

.faq__grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.qa {
  padding: 18px;
  background: var(--site-bg-elevated);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius);
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

@media (max-width: 1024px) {
  .plans {
    grid-template-columns: 1fr;
  }
  .faq__grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .title {
    font-size: 24px;
  }
}
</style>

