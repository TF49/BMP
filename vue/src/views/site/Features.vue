<template>
  <section class="site-page">
    <header class="section__head">
      <p class="section__kicker">产品功能</p>
      <h1 class="section__title">把每个模块做成可复用的“运营动作”</h1>
      <p class="section__desc">
        从预约到核销、从器材到服务、从赛事到财务，BMP 用统一的流程把复杂管理变简单。
      </p>
    </header>

    <div class="features-grid">
      <article class="feature-card">
        <div class="feature-card__icon" aria-hidden="true">场</div>
        <div class="feature-card__body">
          <h3 class="feature-card__title">场地预约管理</h3>
          <ul class="feature-card__list">
            <li>按场馆/场地/时段配置价格与开放规则</li>
            <li>预约记录可追溯，减少沟通成本</li>
            <li>对高峰时段进行引导与限制</li>
          </ul>
        </div>
      </article>

      <article class="feature-card">
        <div class="feature-card__icon feature-card__icon--alt" aria-hidden="true">器</div>
        <div class="feature-card__body">
          <h3 class="feature-card__title">器材租借与库存</h3>
          <ul class="feature-card__list">
            <li>租借状态实时可见，避免丢漏与重复借用</li>
            <li>费用与订单绑定，账目清晰</li>
            <li>器材维护与可用性更可控</li>
          </ul>
        </div>
      </article>

      <article class="feature-card">
        <div class="feature-card__icon" aria-hidden="true">线</div>
        <div class="feature-card__body">
          <h3 class="feature-card__title">穿线服务管理</h3>
          <ul class="feature-card__list">
            <li>服务进度可跟踪，减少询问</li>
            <li>费用标准化，交付更稳定</li>
            <li>与会员/订单打通，便于复购</li>
          </ul>
        </div>
      </article>

      <article class="feature-card">
        <div class="feature-card__icon feature-card__icon--alt" aria-hidden="true">课</div>
        <div class="feature-card__body">
          <h3 class="feature-card__title">教练与课程预约</h3>
          <ul class="feature-card__list">
            <li>课程排期统一管理，名额自动控制</li>
            <li>用户预约体验更顺畅</li>
            <li>支持课程与场地联动的运营打法</li>
          </ul>
        </div>
      </article>

      <article class="feature-card">
        <div class="feature-card__icon" aria-hidden="true">赛</div>
        <div class="feature-card__body">
          <h3 class="feature-card__title">赛事组织与报名</h3>
          <ul class="feature-card__list">
            <li>报名、费用、通知流程化</li>
            <li>活动运营更专业，减少人工登记</li>
            <li>沉淀活动数据，便于复盘</li>
          </ul>
        </div>
      </article>

      <article class="feature-card">
        <div class="feature-card__icon feature-card__icon--alt" aria-hidden="true">数</div>
        <div class="feature-card__body">
          <h3 class="feature-card__title">财务与数据分析</h3>
          <ul class="feature-card__list">
            <li>收入/订单/利用率等关键指标可视化</li>
            <li>支持不同角色看到不同视图</li>
            <li>从“经验管理”升级为“数据管理”</li>
          </ul>
        </div>
      </article>
    </div>

    <div class="cta-band">
      <div class="cta-band__text">
        <h2 class="cta-band__title">右上角「去登录」随时可用</h2>
        <p class="cta-band__desc">也可在此进入系统，开始体验。</p>
      </div>
      <div class="cta-band__actions">
        <router-link class="btn-primary" to="/login">去登录</router-link>
        <router-link class="btn-secondary" to="/site/help">帮助与反馈</router-link>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts"></script>

<style scoped>
.site-page {
  display: flex;
  flex-direction: column;
  gap: 48px;
}

.section__head {
  margin-bottom: 8px;
}

.section__kicker {
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

.section__title {
  font-size: clamp(26px, 3.2vw, 32px);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--site-text);
  margin: 0 0 10px;
}

.section__desc {
  font-size: 16px;
  line-height: 1.65;
  color: var(--site-text-secondary);
  max-width: 60ch;
  margin: 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.feature-card {
  display: flex;
  gap: 16px;
  padding: 24px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
  transition: background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
  background: var(--site-surface-hover);
  border-color: var(--site-border-strong);
  box-shadow: var(--site-shadow);
}

.feature-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 15px;
  color: var(--site-accent);
  background: var(--site-accent-muted);
  flex-shrink: 0;
}

.feature-card__icon--alt {
  color: var(--site-text);
  background: var(--site-surface-hover);
}

.feature-card__title {
  font-size: 16px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 10px;
}

.feature-card__list {
  padding-left: 18px;
  margin: 0;
  font-size: 14px;
  line-height: 1.65;
  color: var(--site-text-secondary);
}

.cta-band {
  padding: 28px 32px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.cta-band__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 4px;
}

.cta-band__desc {
  font-size: 14px;
  color: var(--site-text-muted);
  margin: 0;
}

.cta-band__actions {
  display: flex;
  gap: 12px;
}

.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 20px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  transition: opacity 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
  cursor: pointer;
}

.btn-primary {
  background: var(--site-gradient);
  color: #fff;
  border: 0;
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.35);
}

.btn-primary:hover {
  opacity: 0.95;
  box-shadow: 0 8px 28px rgba(14, 165, 233, 0.4);
}

.btn-secondary {
  background: var(--site-surface-hover);
  color: var(--site-text);
  border: 1px solid var(--site-border);
}

.btn-secondary:hover {
  background: var(--site-border);
}

@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .section__title {
    font-size: 24px;
  }
  .features-grid {
    grid-template-columns: 1fr;
  }
  .cta-band {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }
}
</style>

