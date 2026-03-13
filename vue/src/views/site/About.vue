<template>
  <section class="page">
    <header class="head">
      <div class="kicker">关于我们</div>
      <h1 class="title">为羽毛球运营而生的管理平台</h1>
      <p class="sub">
        BMP 面向协会与场馆的日常运营：预约、器材、课程与赛事、会员与财务、数据分析。
        我们的目标是让“流程可视化、数据可追踪、决策可复盘”。 
      </p>
    </header>

    <div class="bento">
      <article class="tile tile--wide">
        <h2 class="tile__title">核心理念</h2>
        <p class="tile__desc">
          运营的效率，来自清晰的边界与稳定的流程。我们将高频动作（预约、核销、借用、收费、通知）
          模块化，让管理员更少被打断，让用户更快完成目标。
        </p>
        <div class="chips" aria-label="关键词">
          <span class="chip">流程化</span>
          <span class="chip">多角色</span>
          <span class="chip">可追溯</span>
          <span class="chip">可视化</span>
        </div>
      </article>

      <article class="tile">
        <h2 class="tile__title">适用对象</h2>
        <ul class="tile__list">
          <li>高校/社团羽毛球场地</li>
          <li>协会/俱乐部</li>
          <li>商业场馆与连锁门店</li>
        </ul>
      </article>

      <article class="tile">
        <h2 class="tile__title">角色体系</h2>
        <ul class="tile__list">
          <li>协会会长：全局管理与配置</li>
          <li>场馆管理者：场馆运营与执行</li>
          <li>普通用户：预约、服务与活动参与</li>
        </ul>
      </article>

      <article class="tile tile--wide">
        <h2 class="tile__title">我们希望你得到什么</h2>
        <div class="valueGrid">
          <div class="value">
            <div class="value__t">更少沟通成本</div>
            <div class="value__d">状态清晰、规则明确，减少反复确认。</div>
          </div>
          <div class="value">
            <div class="value__t">更稳定的收入结构</div>
            <div class="value__d">预约、课程、赛事、服务形成组合拳。</div>
          </div>
          <div class="value">
            <div class="value__t">更可控的运营节奏</div>
            <div class="value__d">用数据识别高峰与瓶颈，持续优化。</div>
          </div>
          <div class="value">
            <div class="value__t">更好的用户体验</div>
            <div class="value__d">步骤更少、反馈更快、结果可预期。</div>
          </div>
        </div>
      </article>
    </div>

    <div class="cta">
      <div class="cta__text">
        <div class="cta__title">想直接体验系统？</div>
        <div class="cta__desc">点击右上角“去登录”，或使用下面按钮进入。</div>
      </div>
      <div class="cta__actions">
        <router-link class="btn" to="/login">去登录</router-link>
        <router-link class="btn btn--ghost" to="/site/features">查看功能</router-link>
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

.bento {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.tile {
  padding: 24px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
  box-shadow: var(--site-shadow);
}

.tile--wide {
  grid-column: span 2;
  border-color: rgba(14, 165, 233, 0.2);
}

.tile__title {
  font-size: 17px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 10px;
}

.tile__desc {
  font-size: 14px;
  line-height: 1.65;
  color: var(--site-text-secondary);
  margin: 0;
}

.chips {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--site-surface-hover);
  border: 1px solid var(--site-border);
  color: var(--site-text-secondary);
}

.tile__list {
  margin: 0;
  padding-left: 18px;
  font-size: 14px;
  line-height: 1.65;
  color: var(--site-text-secondary);
}

.valueGrid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.value {
  padding: 18px;
  background: var(--site-bg-elevated);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius);
}

.value__t {
  font-weight: 600;
  font-size: 14px;
  color: var(--site-text);
}

.value__d {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--site-text-secondary);
}

.cta {
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

.cta__title {
  font-weight: 700;
  font-size: 18px;
  color: var(--site-text);
  margin: 0 0 6px;
}

.cta__desc {
  font-size: 14px;
  color: var(--site-text-muted);
  margin: 0;
}

.cta__actions {
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
  .bento {
    grid-template-columns: 1fr;
  }
  .tile--wide {
    grid-column: auto;
  }
}

@media (max-width: 640px) {
  .title {
    font-size: 24px;
  }
  .valueGrid {
    grid-template-columns: 1fr;
  }
}
</style>

