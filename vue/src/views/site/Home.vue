<template>
  <div class="site-home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero__content">
        <p class="hero__badge">
          <span class="hero__badge-dot" aria-hidden="true" />
          智能场馆运营平台
        </p>
        <h1 class="hero__title font-display">
          让羽毛球场馆运营
          <span class="hero__title-accent gradient-text">更高效、更清晰</span>
        </h1>
        <p class="hero__desc">
          场地预约、器材租借、课程赛事、会员与财务一体化管理，助力协会与场馆降本增效，数据可视、决策有据。
        </p>
        <div class="hero__cta">
          <router-link class="btn-primary hero-cta-primary" to="/login">
            立即登录
            <svg class="btn-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="5" y1="12" x2="19" y2="12"/>
              <polyline points="12 5 19 12 12 19"/>
            </svg>
          </router-link>
          <router-link class="btn-secondary" :to="{ path: '/site', hash: '#features' }">了解功能</router-link>
        </div>
        <div class="hero__stats" role="list" aria-label="产品亮点">
          <div class="hero__stat" role="listitem">
            <span class="hero__stat-value">全流程</span>
            <span class="hero__stat-label">预约到核销</span>
          </div>
          <div class="hero__stat" role="listitem">
            <span class="hero__stat-value">多角色</span>
            <span class="hero__stat-label">会长 / 场馆 / 用户</span>
          </div>
          <div class="hero__stat" role="listitem">
            <span class="hero__stat-value">可视化</span>
            <span class="hero__stat-label">数据与财务</span>
          </div>
        </div>
      </div>
      <div class="hero__visual" aria-hidden="true">
        <div class="hero__cardBorder">
          <div class="hero__card glass-card">
            <div class="card-shine" aria-hidden="true" />
            <div class="card-glow" aria-hidden="true" />
            <div class="hero__card-header">
              <div class="hero__card-header-left">
                <span class="hero__card-dot hero__card-dot--animated" />
                <span class="hero__card-title">运营概览</span>
              </div>
              <div class="hero__card-header-right">
                <span class="hero__card-live">
                  <span class="hero__card-live-dot" />
                  实时
                </span>
              </div>
            </div>
            <div class="hero__card-body">
              <div class="hero__card-utilization">
                <div class="hero__card-utilization-header">
                  <div class="hero__card-utilization-label">
                    <svg class="hero__card-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <rect x="3" y="3" width="18" height="18" rx="2" />
                      <path d="M3 9h18" />
                      <path d="M9 21V9" />
                    </svg>
                    <span>场地利用率</span>
                  </div>
                  <span class="hero__card-percent">76%</span>
                </div>
                <div class="hero__card-bar">
                  <span class="hero__card-fill hero__card-fill--animated" />
                  <span class="hero__card-bar-glow" aria-hidden="true" />
                </div>
                <div class="hero__card-bar-labels">
                  <span>0%</span>
                  <span>50%</span>
                  <span>100%</span>
                </div>
              </div>
              <div class="hero__card-divider" />
              <div class="hero__card-grid">
                <div class="hero__card-cell hero__card-cell--success">
                  <div class="hero__card-cell-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                      <polyline points="22 4 12 14.01 9 11.01" />
                    </svg>
                  </div>
                  <span class="hero__card-num hero__card-num--highlight">98.6%</span>
                  <span class="hero__card-tag">预约成功率</span>
                  <span class="hero__card-trend hero__card-trend--up">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <polyline points="18 15 12 9 6 15" />
                    </svg>
                    +2.3%
                  </span>
                </div>
                <div class="hero__card-cell hero__card-cell--info">
                  <div class="hero__card-cell-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="10" />
                      <path d="M12 6v6l4 2" />
                    </svg>
                  </div>
                  <span class="hero__card-num">24</span>
                  <span class="hero__card-tag">器材可用</span>
                  <span class="hero__card-trend hero__card-trend--neutral">
                    共32件
                  </span>
                </div>
                <div class="hero__card-cell hero__card-cell--accent">
                  <div class="hero__card-cell-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="12" y1="1" x2="12" y2="23" />
                      <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
                    </svg>
                  </div>
                  <span class="hero__card-num hero__card-num--accent">¥3,820</span>
                  <span class="hero__card-tag">当日收入</span>
                  <span class="hero__card-trend hero__card-trend--up">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <polyline points="18 15 12 9 6 15" />
                    </svg>
                    +12%
                  </span>
                </div>
              </div>
              <div class="hero__card-footer">
                <span class="hero__card-footer-text">数据更新于 5 秒前</span>
                <span class="hero__card-footer-dot" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 核心能力（概览） -->
    <section class="block">
      <div class="block__head">
        <h2 class="block__title">核心能力</h2>
        <p class="block__desc">预约、运营、财务、服务一体化，可追踪、可复盘、可持续。</p>
      </div>
      <div class="cards">
        <article class="card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="card__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <h3 class="card__title">智能预约与核销</h3>
          <p class="card__desc">按场地、时段、价格灵活配置，预约记录可追溯。</p>
        </article>
        <article class="card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="card__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
              <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
              <line x1="12" y1="22.08" x2="12" y2="12"/>
            </svg>
          </div>
          <h3 class="card__title">器材与服务闭环</h3>
          <p class="card__desc">球拍租借、穿线服务、库存与费用统一管理。</p>
        </article>
        <article class="card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="card__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/>
              <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
              <path d="M4 22h16"/>
              <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/>
              <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/>
              <path d="M18 2H6v7a6 6 0 0 0 12 0V2z"/>
            </svg>
          </div>
          <h3 class="card__title">赛事与课程运营</h3>
          <p class="card__desc">报名、名额、费用与通知流程化，运营更轻松。</p>
        </article>
        <article class="card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="card__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="20" x2="18" y2="10"/>
              <line x1="12" y1="20" x2="12" y2="4"/>
              <line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
          </div>
          <h3 class="card__title">财务与数据可视化</h3>
          <p class="card__desc">收入、订单、利用率一屏掌握，决策从“感觉”到“数据”。</p>
        </article>
      </div>
    </section>

    <!-- 使用场景 -->
    <section class="block landing-section">
      <div class="block__head">
        <p class="block__kicker">使用场景</p>
        <h2 class="block__title">适用于各类羽毛球场馆运营场景</h2>
        <p class="block__desc">从高校社团到商业连锁，BMP 为不同规模的场馆提供定制化解决方案。</p>
      </div>
      <div class="scenarios-grid">
        <article class="scenario-card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="scenario-card__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 10v6M2 10l10-5 10 5M2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h3 class="scenario-card__title">高校/社团</h3>
          <p class="scenario-card__desc">学生社团、高校体育场馆的轻量级管理方案</p>
          <div class="scenario-card__pain">
            <span class="scenario-card__pain-label">痛点</span>
            <ul class="scenario-card__pain-list">
              <li>预约信息混乱，经常冲突</li>
              <li>器材借用记录不清晰</li>
              <li>活动组织依赖人工统计</li>
            </ul>
          </div>
          <div class="scenario-card__solution">
            <span class="scenario-card__solution-label">BMP 解决方案</span>
            <ul class="scenario-card__solution-list">
              <li>在线预约，自动冲突检测</li>
              <li>器材库存实时可见</li>
              <li>活动报名与费用统一管理</li>
            </ul>
          </div>
          <div class="scenario-card__metric">
            <span class="scenario-card__metric-value">预约效率提升 60%</span>
            <span class="scenario-card__metric-desc">减少沟通成本，提升场地利用率</span>
          </div>
        </article>

        <article class="scenario-card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="scenario-card__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <h3 class="scenario-card__title">协会/俱乐部</h3>
          <p class="scenario-card__desc">羽毛球协会、俱乐部的专业运营管理平台</p>
          <div class="scenario-card__pain">
            <span class="scenario-card__pain-label">痛点</span>
            <ul class="scenario-card__pain-list">
              <li>多场馆数据分散，难以统一管理</li>
              <li>会员信息与财务数据脱节</li>
              <li>赛事组织流程繁琐</li>
            </ul>
          </div>
          <div class="scenario-card__solution">
            <span class="scenario-card__solution-label">BMP 解决方案</span>
            <ul class="scenario-card__solution-list">
              <li>多场馆数据集中管理</li>
              <li>会员与财务数据联动</li>
              <li>赛事报名、费用、通知一体化</li>
            </ul>
          </div>
          <div class="scenario-card__metric">
            <span class="scenario-card__metric-value">运营效率提升 80%</span>
            <span class="scenario-card__metric-desc">数据可视化，决策更科学</span>
          </div>
        </article>

        <article class="scenario-card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="scenario-card__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="21" x2="9" y2="9"/>
            </svg>
          </div>
          <h3 class="scenario-card__title">商业场馆</h3>
          <p class="scenario-card__desc">商业羽毛球场馆、连锁门店的规模化运营方案</p>
          <div class="scenario-card__pain">
            <span class="scenario-card__pain-label">痛点</span>
            <ul class="scenario-card__pain-list">
              <li>多门店数据无法统一分析</li>
              <li>收入统计依赖手工汇总</li>
              <li>高峰时段管理困难</li>
            </ul>
          </div>
          <div class="scenario-card__solution">
            <span class="scenario-card__solution-label">BMP 解决方案</span>
            <ul class="scenario-card__solution-list">
              <li>多门店数据统一看板</li>
              <li>财务数据自动汇总与分析</li>
              <li>智能预约与时段优化</li>
            </ul>
          </div>
          <div class="scenario-card__metric">
            <span class="scenario-card__metric-value">收入增长 25%</span>
            <span class="scenario-card__metric-desc">数据驱动运营，提升盈利能力</span>
          </div>
        </article>

        <article class="scenario-card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="scenario-card__icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h3 class="scenario-card__title">连锁门店</h3>
          <p class="scenario-card__desc">多门店连锁运营，统一管理各分店数据</p>
          <div class="scenario-card__pain">
            <span class="scenario-card__pain-label">痛点</span>
            <ul class="scenario-card__pain-list">
              <li>各门店数据孤立，无法对比</li>
              <li>总部难以实时掌握运营情况</li>
              <li>标准化运营流程难以落地</li>
            </ul>
          </div>
          <div class="scenario-card__solution">
            <span class="scenario-card__solution-label">BMP 解决方案</span>
            <ul class="scenario-card__solution-list">
              <li>多门店数据对比分析</li>
              <li>实时运营数据看板</li>
              <li>统一流程与标准化管理</li>
            </ul>
          </div>
          <div class="scenario-card__metric">
            <span class="scenario-card__metric-value">管理效率提升 90%</span>
            <span class="scenario-card__metric-desc">总部统一管控，分店独立运营</span>
          </div>
        </article>
      </div>
    </section>

    <!-- 功能（详细） -->
    <section id="features" class="block landing-section">
      <div class="block__head">
        <p class="block__kicker">产品功能</p>
        <h2 class="block__title">把每个模块做成可复用的“运营动作”</h2>
        <p class="block__desc">从预约到核销、从器材到服务、从赛事到财务，BMP 用统一的流程把复杂管理变简单。</p>
      </div>
      <div class="features-grid">
        <article class="feature-card glass-card">
          <div class="card-shine" aria-hidden="true" />
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
        <article class="feature-card glass-card">
          <div class="card-shine" aria-hidden="true" />
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
        <article class="feature-card glass-card">
          <div class="card-shine" aria-hidden="true" />
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
        <article class="feature-card glass-card">
          <div class="card-shine" aria-hidden="true" />
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
        <article class="feature-card glass-card">
          <div class="card-shine" aria-hidden="true" />
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
        <article class="feature-card glass-card">
          <div class="card-shine" aria-hidden="true" />
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

      <!-- 全部模块概览 -->
      <div class="gradient-border modules-overviewBorder">
        <div class="modules-overview glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="modules-overview__head">
            <h3 class="modules-overview__title font-display">全部模块</h3>
            <p class="modules-overview__desc">BMP 提供 20+ 个功能模块，覆盖场馆运营全流程</p>
          </div>
          <div class="modules-grid">
            <div 
              v-for="(module, index) in allModules" 
              :key="index" 
              class="module-item"
              :class="{ 'is-visible': visibleModules.has(index) }"
              :ref="el => { if (el) moduleRefs[index] = el }"
            >
              <span class="module-item__num">{{ String(index + 1).padStart(2, '0') }}</span>
              <div class="module-item__content">
                <span class="module-item__name">{{ module.name }}</span>
                <span class="module-item__desc">{{ module.desc }}</span>
              </div>
              <span v-if="module.adminRoute" class="module-item__tag module-item__tag--admin">管理端</span>
              <span v-if="module.userRoute" class="module-item__tag module-item__tag--user">用户端</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 产品截图 -->
    <section class="block landing-section">
      <div class="block__head">
        <p class="block__kicker">产品预览</p>
        <h2 class="block__title">直观的界面，流畅的体验</h2>
        <p class="block__desc">现代化的设计，让管理更高效，让使用更便捷。</p>
      </div>
      <div class="screenshots-grid">
        <article class="screenshot-card glass-card" @click="openScreenshot('dashboard')">
          <div class="card-shine" aria-hidden="true" />
          <div class="screenshot-card__image screenshot-card__image--enhanced">
            <div class="screenshot-placeholder screenshot-placeholder--dashboard">
              <div class="screenshot-mock">
                <div class="screenshot-mock__header">
                  <span></span><span></span><span></span>
                </div>
                <div class="screenshot-mock__content">
                  <div class="screenshot-mock__sidebar"></div>
                  <div class="screenshot-mock__main">
                    <div class="screenshot-mock__card"></div>
                    <div class="screenshot-mock__card"></div>
                    <div class="screenshot-mock__chart"></div>
                  </div>
                </div>
              </div>
              <span class="screenshot-placeholder__label">Dashboard 数据看板</span>
              <span class="screenshot-placeholder__cta">点击预览</span>
            </div>
          </div>
          <div class="screenshot-card__content">
            <h3 class="screenshot-card__title">数据看板</h3>
            <p class="screenshot-card__desc">一屏掌握关键指标：会员数、场地利用率、今日收入、预约情况等，数据可视化呈现。</p>
          </div>
        </article>

        <article class="screenshot-card glass-card" @click="openScreenshot('booking')">
          <div class="card-shine" aria-hidden="true" />
          <div class="screenshot-card__image screenshot-card__image--enhanced">
            <div class="screenshot-placeholder screenshot-placeholder--booking">
              <div class="screenshot-mock">
                <div class="screenshot-mock__header">
                  <span></span><span></span><span></span>
                </div>
                <div class="screenshot-mock__content">
                  <div class="screenshot-mock__calendar">
                    <div></div><div></div><div></div><div></div>
                    <div></div><div></div><div></div><div></div>
                  </div>
                </div>
              </div>
              <span class="screenshot-placeholder__label">预约管理界面</span>
              <span class="screenshot-placeholder__cta">点击预览</span>
            </div>
          </div>
          <div class="screenshot-card__content">
            <h3 class="screenshot-card__title">预约管理</h3>
            <p class="screenshot-card__desc">清晰的日历视图，实时显示场地占用情况，支持快速预约、修改和取消。</p>
          </div>
        </article>

        <article class="screenshot-card glass-card" @click="openScreenshot('finance')">
          <div class="card-shine" aria-hidden="true" />
          <div class="screenshot-card__image screenshot-card__image--enhanced">
            <div class="screenshot-placeholder screenshot-placeholder--finance">
              <div class="screenshot-mock">
                <div class="screenshot-mock__header">
                  <span></span><span></span><span></span>
                </div>
                <div class="screenshot-mock__content">
                  <div class="screenshot-mock__bars">
                    <div></div><div></div><div></div><div></div><div></div>
                  </div>
                </div>
              </div>
              <span class="screenshot-placeholder__label">财务统计报表</span>
              <span class="screenshot-placeholder__cta">点击预览</span>
            </div>
          </div>
          <div class="screenshot-card__content">
            <h3 class="screenshot-card__title">财务统计</h3>
            <p class="screenshot-card__desc">多维度财务分析，收入趋势、订单统计、各业务模块收入占比一目了然。</p>
          </div>
        </article>

        <article class="screenshot-card glass-card" @click="openScreenshot('member')">
          <div class="card-shine" aria-hidden="true" />
          <div class="screenshot-card__image screenshot-card__image--enhanced">
            <div class="screenshot-placeholder screenshot-placeholder--member">
              <div class="screenshot-mock">
                <div class="screenshot-mock__header">
                  <span></span><span></span><span></span>
                </div>
                <div class="screenshot-mock__content">
                  <div class="screenshot-mock__table">
                    <div></div><div></div><div></div>
                  </div>
                </div>
              </div>
              <span class="screenshot-placeholder__label">会员管理界面</span>
              <span class="screenshot-placeholder__cta">点击预览</span>
            </div>
          </div>
          <div class="screenshot-card__content">
            <h3 class="screenshot-card__title">会员管理</h3>
            <p class="screenshot-card__desc">完整的会员信息管理，支持充值、消费记录查询，会员等级与权益配置。</p>
          </div>
        </article>
      </div>
      <div class="screenshots-cta">
        <p class="screenshots-cta__text">想查看完整功能？</p>
        <router-link class="btn-primary" to="/login">立即体验</router-link>
      </div>
    </section>

    <!-- 方案与版本 -->
    <section id="pricing" class="block landing-section">
      <div class="block__head">
        <p class="block__kicker">方案与版本</p>
        <h2 class="block__title">从校园到商业运营，都有合适的配置</h2>
        <p class="block__desc">这里提供的是“官网展示用”的方案示例，可按实际业务改文案与功能点。</p>
      </div>
      <div class="plans">
        <article class="plan glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="plan__top">
            <span class="plan__name">校园版</span>
            <span class="plan__tag">轻量启动</span>
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
          <router-link class="btn-secondary" to="/login">去登录</router-link>
        </article>
        <article class="plan plan--hot glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="plan__glow" aria-hidden="true" />
          <div class="plan__top">
            <span class="plan__name">协会版</span>
            <span class="plan__tag plan__tag--hot">推荐</span>
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
          <router-link class="btn-primary" to="/login">立即使用</router-link>
        </article>
        <article class="plan glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="plan__top">
            <span class="plan__name">商业版</span>
            <span class="plan__tag">规模化运营</span>
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
          <router-link class="btn-secondary" :to="{ path: '/site', hash: '#help' }">联系与支持</router-link>
        </article>
      </div>
      <div class="faq">
        <h3 class="faq__title">常见问题</h3>
        <p class="faq__desc">你也可以在下方「帮助」区块看到更多说明。</p>
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
            <div class="qa__a">这里是展示用模板，可根据实际情况替换为真实方案与售卖方式。</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 客户评价 -->
    <section class="block landing-section">
      <div class="block__head">
        <p class="block__kicker">客户评价</p>
        <h2 class="block__title">来自用户的真实反馈</h2>
        <p class="block__desc">看看使用 BMP 的场馆管理者怎么说。</p>
      </div>
      <div class="testimonials-grid">
        <article class="testimonial-card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="testimonial-card__header">
            <div class="testimonial-card__avatar testimonial-card__avatar--gradient">
              <span>张</span>
            </div>
            <div class="testimonial-card__info">
              <div class="testimonial-card__name">
                张会长
                <span class="testimonial-card__verified" title="已认证用户">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                </span>
              </div>
              <div class="testimonial-card__role">某高校羽毛球协会会长</div>
              <div class="testimonial-card__date">2025年12月</div>
            </div>
            <div class="testimonial-card__rating">
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
            </div>
          </div>
          <div class="testimonial-card__content">
            <p class="testimonial-card__text">"使用 BMP 后，预约效率提升了 60%，再也不用人工记录和电话确认了。财务数据一目了然，协会运营更加规范。"</p>
          </div>
          <div class="testimonial-card__highlight">
            <span class="testimonial-card__highlight-label">关键收益</span>
            <span class="testimonial-card__highlight-value">预约效率提升 60%</span>
          </div>
        </article>

        <article class="testimonial-card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="testimonial-card__header">
            <div class="testimonial-card__avatar testimonial-card__avatar--gradient">
              <span>李</span>
            </div>
            <div class="testimonial-card__info">
              <div class="testimonial-card__name">
                李经理
                <span class="testimonial-card__verified" title="已认证用户">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                </span>
              </div>
              <div class="testimonial-card__role">商业羽毛球场馆运营经理</div>
              <div class="testimonial-card__date">2026年1月</div>
            </div>
            <div class="testimonial-card__rating">
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
            </div>
          </div>
          <div class="testimonial-card__content">
            <p class="testimonial-card__text">"多场馆管理非常方便，数据统一分析帮助我们优化运营策略。特别是财务统计功能，让我们的收入结构更加清晰。"</p>
          </div>
          <div class="testimonial-card__highlight">
            <span class="testimonial-card__highlight-label">关键收益</span>
            <span class="testimonial-card__highlight-value">运营效率提升 80%</span>
          </div>
        </article>

        <article class="testimonial-card glass-card">
          <div class="card-shine" aria-hidden="true" />
          <div class="testimonial-card__header">
            <div class="testimonial-card__avatar testimonial-card__avatar--gradient">
              <span>王</span>
            </div>
            <div class="testimonial-card__info">
              <div class="testimonial-card__name">
                王教练
                <span class="testimonial-card__verified" title="已认证用户">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
                  </svg>
                </span>
              </div>
              <div class="testimonial-card__role">羽毛球俱乐部教练</div>
              <div class="testimonial-card__date">2025年11月</div>
            </div>
            <div class="testimonial-card__rating">
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
              <span class="rating-star">★</span>
            </div>
          </div>
          <div class="testimonial-card__content">
            <p class="testimonial-card__text">"课程预约功能很实用，学员可以自主预约，减少了我的沟通成本。数据分析功能让我更好地了解课程受欢迎程度。"</p>
          </div>
          <div class="testimonial-card__highlight">
            <span class="testimonial-card__highlight-label">关键收益</span>
            <span class="testimonial-card__highlight-value">沟通成本降低 70%</span>
          </div>
        </article>
      </div>
    </section>

    <!-- 关于我们 -->
    <section id="about" class="block landing-section">
      <div class="block__head">
        <p class="block__kicker">关于我们</p>
        <h2 class="block__title">为羽毛球运营而生的管理平台</h2>
        <p class="block__desc">BMP 面向协会与场馆的日常运营：预约、器材、课程与赛事、会员与财务、数据分析。我们的目标是让“流程可视化、数据可追踪、决策可复盘”。</p>
      </div>
      <div class="bento">
        <article class="tile tile--wide">
          <h3 class="tile__title">核心理念</h3>
          <p class="tile__desc">运营的效率，来自清晰的边界与稳定的流程。我们将高频动作（预约、核销、借用、收费、通知）模块化，让管理员更少被打断，让用户更快完成目标。</p>
          <div class="chips" aria-label="关键词">
            <span class="chip">流程化</span>
            <span class="chip">多角色</span>
            <span class="chip">可追溯</span>
            <span class="chip">可视化</span>
          </div>
        </article>
        <article class="tile">
          <h3 class="tile__title">适用对象</h3>
          <ul class="tile__list">
            <li>高校/社团羽毛球场地</li>
            <li>协会/俱乐部</li>
            <li>商业场馆与连锁门店</li>
          </ul>
        </article>
        <article class="tile">
          <h3 class="tile__title">角色体系</h3>
          <ul class="tile__list">
            <li>协会会长：全局管理与配置</li>
            <li>场馆管理者：场馆运营与执行</li>
            <li>普通用户：预约、服务与活动参与</li>
          </ul>
        </article>
        <article class="tile tile--wide">
          <h3 class="tile__title">我们希望你得到什么</h3>
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
    </section>

    <!-- 帮助与反馈 -->
    <section id="help" class="block landing-section">
      <div class="block__head">
        <p class="block__kicker">帮助与反馈</p>
        <h2 class="block__title">常见问题与使用指引</h2>
        <p class="block__desc">官网页面无需登录即可访问；会员与教练使用同一登录页（/login），登录后按角色（会长/场馆/用户/教练）自动分流到对应工作台。</p>
      </div>
      <div class="help-grid">
        <article class="panel glass-card">
          <div class="card-shine" aria-hidden="true" />
          <h3 class="panel__title">快速开始</h3>
          <ol class="steps">
            <li>点击右上角 <strong>去登录</strong> 进入登录页（<code>/login</code>）。</li>
            <li>登录成功后，系统会根据角色自动跳转到管理端、用户端或教练端。</li>
            <li>如遇到权限不足，会提示并跳转到 403 页面。</li>
          </ol>
        </article>
        <article class="panel glass-card">
          <div class="card-shine" aria-hidden="true" />
          <h3 class="panel__title">常见问题</h3>
          <div class="faq-accordion">
            <div
              v-for="(item, idx) in faqItems"
              :key="item.q"
              class="faq-item"
              :class="{ 'faq-item--active': activeFaqIndex === idx }"
              @click="toggleFaq(idx)"
            >
              <div class="faq-item__header">
                <span class="faq-item__q">{{ item.q }}</span>
                <span class="faq-item__icon" aria-hidden="true">⌄</span>
              </div>
              <div class="faq-item__body" v-show="activeFaqIndex === idx">
                <p class="faq-item__a" v-html="item.a" />
              </div>
            </div>
          </div>
        </article>
      </div>
      <div class="contact-band">
        <div class="contact-band__text">
          <h3 class="contact-band__title">需要进一步支持？</h3>
          <p class="contact-band__desc">欢迎通过以下方式联系我们，或点击右下角「联系我们」悬浮按钮留言。</p>
          <div class="contact-band__info">
            <a :href="`tel:${contactInfo.phone}`" class="contact-band__link">{{ contactInfo.phoneDisplay }}</a>
            <a :href="`mailto:${contactInfo.email}`" class="contact-band__link">{{ contactInfo.email }}</a>
            <span class="contact-band__link contact-band__link--text">微信 {{ contactInfo.wechat }}</span>
            <span v-if="contactInfo.workingHours" class="contact-band__meta">{{ contactInfo.workingHours }}</span>
          </div>
        </div>
        <div class="contact-band__actions">
          <router-link class="btn-primary" to="/login">去登录</router-link>
          <router-link class="btn-secondary" to="/site">返回顶部</router-link>
        </div>
      </div>
    </section>

    <!-- 底部 CTA -->
    <section class="cta">
      <div class="cta__inner">
        <h2 class="cta__title">准备好提升运营效率了吗？</h2>
        <p class="cta__desc">登录系统，让重复工作交给 BMP 处理。</p>
        <div class="cta__actions">
          <router-link class="btn-primary" to="/login">去登录</router-link>
          <router-link class="btn-secondary" :to="{ path: '/site', hash: '#help' }">常见问题</router-link>
        </div>
      </div>
    </section>
  </div>

  <el-dialog
    v-model="screenshotDialogVisible"
    :title="screenshotDialog.title"
    width="60%"
    class="screenshot-dialog"
  >
    <div class="screenshot-dialog__content">
      <div class="screenshot-dialog__preview glass-card">
        <div class="screenshot-dialog__placeholder">
          <span class="screenshot-dialog__label">{{ screenshotDialog.title }}</span>
        </div>
      </div>
      <p class="screenshot-dialog__desc">
        {{ screenshotDialog.desc }}
      </p>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useIntersectionObserver } from '@/composables/useIntersectionObserver'
import { contactInfo } from '@/config/contact'

// 全部模块数据（基于模块化测试文档）
const allModules = [
  { name: '认证', desc: '登录与注册', adminRoute: '/login', userRoute: '/login' },
  { name: '系统首页', desc: 'Dashboard 数据看板', adminRoute: '/dashboard', userRoute: '/user/dashboard' },
  { name: '用户管理', desc: '账号与权限管理', adminRoute: '/admin/user/management' },
  { name: '场馆管理', desc: '场馆信息配置', adminRoute: '/venue/management' },
  { name: '场地管理', desc: '场地与时段配置', adminRoute: '/court/management' },
  { name: '会员管理', desc: '会员信息与状态', adminRoute: '/member/management' },
  { name: '充值中心', desc: '账户充值', adminRoute: '/recharge/index', userRoute: '/user/recharge' },
  { name: '场地预约', desc: '预约与核销', adminRoute: '/booking/management', userRoute: '/user/booking' },
  { name: '器材管理', desc: '器材库存管理', adminRoute: '/equipment/management' },
  { name: '器材租借', desc: '租借与归还', adminRoute: '/equipment/rental', userRoute: '/user/equipment' },
  { name: '穿线服务', desc: '服务订单管理', adminRoute: '/equipment/stringing', userRoute: '/user/stringing' },
  { name: '教练管理', desc: '教练信息管理', adminRoute: '/coach-course/coach' },
  { name: '课程管理', desc: '课程排期管理', adminRoute: '/coach-course/course' },
  { name: '课程预约', desc: '课程预约与签到', adminRoute: '/coach-course/course-booking', userRoute: '/user/course' },
  { name: '赛事管理', desc: '赛事组织管理', adminRoute: '/tournament/management' },
  { name: '赛事报名', desc: '报名与费用', adminRoute: '/tournament/registration', userRoute: '/user/tournament' },
  { name: '财务管理', desc: '收入与流水', adminRoute: '/finance/management' },
  { name: '统计分析', desc: '数据可视化', adminRoute: '/dashboard' },
  { name: '用户端-搜索', desc: '全局搜索', userRoute: '/user/search' },
  { name: '用户端-设置', desc: '个人设置', userRoute: '/user/settings' },
  { name: '错误页', desc: '403/404 页面', adminRoute: '/403', userRoute: '/403' }
]

// FAQ 数据（NeuroFlow 风格手风琴）
const faqItems = [
  {
    q: '为什么我打开某些页面会回到登录？',
    a: '除 <code>/site*</code>、<code>/login</code>、<code>/register</code> 外，未登录访问会被路由守卫要求登录。'
  },
  {
    q: '普通用户能进管理端吗？',
    a: '不能。普通用户访问管理端路由会被重定向到用户端对应页面。'
  },
  {
    q: '我想修改官网内容（文案/价格/模块）',
    a: '直接编辑 <code>src/views/site/</code> 下对应页面即可，无需改后端。'
  }
]

const activeFaqIndex = ref<number | null>(0)

const toggleFaq = (index: number) => {
  activeFaqIndex.value = activeFaqIndex.value === index ? null : index
}

// 产品截图预览（Element Plus 弹窗）
type ScreenshotKey = 'dashboard' | 'booking' | 'finance' | 'member'

const screenshotDialogVisible = ref(false)
const screenshotDialog = ref<{ title: string; desc: string }>({
  title: '',
  desc: ''
})

const screenshotMap: Record<ScreenshotKey, { title: string; desc: string }> = {
  dashboard: {
    title: '数据看板',
    desc: '一屏掌握会员总数、场地利用率、今日收入与预约情况，可用于日常例会或运营复盘。'
  },
  booking: {
    title: '预约管理',
    desc: '以日历视图管理场地预约，支持快速查看空闲时段、修改预约与处理冲突。'
  },
  finance: {
    title: '财务统计',
    desc: '汇总各业务模块的收入构成，支持按时间、模块、场馆查看收入趋势与占比。'
  },
  member: {
    title: '会员管理',
    desc: '集中管理会员资料、充值与消费记录，可结合会员等级做精细化运营。'
  }
}

const openScreenshot = (key: ScreenshotKey) => {
  const data = screenshotMap[key]
  screenshotDialog.value = { ...data }
  screenshotDialogVisible.value = true
}

// 滚动动画相关
const moduleRefs = ref<(Element | null)[]>([])
const visibleModules = ref<Set<number>>(new Set())

onMounted(() => {
  // 为各个 section 添加滚动动画
  const sections = document.querySelectorAll('.landing-section, .block')
  sections.forEach((section) => {
    useIntersectionObserver(
      section,
      (isIntersecting) => {
        if (isIntersecting) {
          section.classList.add('is-visible')
        }
      },
      { threshold: 0.1, once: true }
    )
  })

  // 为模块项添加滚动动画（使用 nextTick 确保 DOM 已渲染）
  nextTick(() => {
    moduleRefs.value.forEach((el, index) => {
      if (el) {
        useIntersectionObserver(
          el,
          (isIntersecting) => {
            if (isIntersecting) {
              visibleModules.value.add(index)
            }
          },
          { threshold: 0.1, once: true }
        )
      }
    })
  })
})
</script>

<style scoped>
.site-home {
  display: flex;
  flex-direction: column;
  gap: 80px;
}

/* 一级区块（Hero、定价、CTA）- 120px 间距 */
.site-home > .hero,
.site-home > section:nth-child(6), /* 方案与版本 */
.site-home > section:last-child /* 底部CTA */ {
  margin-bottom: 40px;
}

/* 二级区块（核心能力、使用场景、产品功能）- 100px 间距 */
.site-home > section:nth-child(2), /* 核心能力 */
.site-home > section:nth-child(3), /* 使用场景 */
.site-home > section:nth-child(4) /* 产品功能 */ {
  margin-bottom: 20px;
}

/* 偶数区块背景分隔 */
.site-home > section:nth-child(even) {
  padding: 60px 0;
  margin-left: -16px;
  margin-right: -16px;
  padding-left: 16px;
  padding-right: 16px;
  background: var(--site-bg-elevated);
  border-top: 1px solid var(--site-border);
  border-bottom: 1px solid var(--site-border);
}

/* 锚点区块：避免被固定导航遮挡 */
.landing-section {
  scroll-margin-top: calc(var(--site-nav-height) + 24px);
}

/* ----- Hero ----- */
.hero {
  position: relative;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 48px;
  align-items: center;
  padding: 24px 0;
}

.hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  margin-bottom: 16px;
  font-size: 13px;
  font-weight: 600;
  color: var(--site-accent);
  background: var(--site-accent-muted);
  border: 1px solid rgba(14, 165, 233, 0.25);
  border-radius: 999px;
}

.hero__badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: var(--site-accent);
  box-shadow: 0 0 16px color-mix(in srgb, var(--site-accent) 55%, transparent);
  animation: heroPulse 1.8s ease-in-out infinite;
}

@keyframes heroPulse {
  0%, 100% { transform: scale(1); opacity: 0.7; }
  50% { transform: scale(1.35); opacity: 1; }
}

.hero__title {
  font-size: clamp(32px, 4.2vw, 48px);
  font-weight: 800;
  line-height: 1.12;
  letter-spacing: -0.03em;
  color: var(--site-text);
  margin: 0 0 16px;
  animation: heroTitleFadeIn 1s ease-out;
}

@keyframes heroTitleFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero__title-accent {
  display: block;
  font-weight: 800;
  animation: heroAccentGlow 3s ease-in-out infinite;
}

@keyframes heroAccentGlow {
  0%, 100% {
    filter: drop-shadow(0 0 8px color-mix(in srgb, var(--site-accent) 30%, transparent));
  }
  50% {
    filter: drop-shadow(0 0 16px color-mix(in srgb, var(--site-accent) 50%, transparent));
  }
}

.hero__desc {
  font-size: 17px;
  line-height: 1.65;
  color: var(--site-text-secondary);
  max-width: 42ch;
  margin: 0 0 28px;
}

.hero__cta {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 40px;
  animation: heroTitleFadeIn 1s ease-out 0.3s both;
}

/* Hero CTA 主按钮增强 */
.hero-cta-primary {
  padding: 0 32px;
  min-height: 52px;
  font-size: 16px;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.hero-cta-primary::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: inherit;
  background: var(--site-gradient);
  opacity: 0;
  z-index: -1;
  animation: ctaPulseGlow 2.5s ease-in-out infinite;
}

@keyframes ctaPulseGlow {
  0%, 100% {
    opacity: 0;
    transform: scale(1);
  }
  50% {
    opacity: 0.4;
    transform: scale(1.05);
  }
}

.btn-arrow {
  width: 18px;
  height: 18px;
  transition: transform 0.3s ease;
}

.hero-cta-primary:hover .btn-arrow {
  transform: translateX(4px);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  padding: 0 24px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 15px;
  text-decoration: none;
  transition: opacity 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, color 0.2s ease;
  cursor: pointer;
  border: 1px solid transparent;
}

.btn:focus-visible {
  outline: 2px solid var(--site-accent);
  outline-offset: 2px;
}

.btn--primary {
  background: var(--site-gradient);
  color: #fff;
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.35);
  transition: transform 0.2s ease, opacity 0.2s ease, box-shadow 0.2s ease;
}

.btn--primary:hover {
  opacity: 0.95;
  transform: scale(1.02);
  box-shadow: 0 8px 28px rgba(14, 165, 233, 0.4);
}

.btn--primary:active {
  transform: scale(0.98);
}

.btn--secondary {
  background: var(--site-surface);
  color: var(--site-text);
  border-color: var(--site-border);
}

.btn--secondary:hover {
  background: var(--site-surface-hover);
  border-color: var(--site-border-strong);
}

.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 52px;
  padding: 0 30px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 15px;
  text-decoration: none;
  border: 1px solid transparent;
  cursor: pointer;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    background 0.22s ease,
    color 0.22s ease,
    filter 0.22s ease;
}

.btn-primary {
  position: relative;
  overflow: hidden;
  color: #fff;
  background: var(--site-gradient);
  box-shadow: 0 10px 28px rgba(14, 165, 233, 0.45);
}

.btn-primary::before {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.32), rgba(255, 255, 255, 0.02));
  opacity: 0.9;
}

.btn-primary span,
.btn-primary .btn-arrow {
  position: relative;
  z-index: 1;
}

.btn-primary:hover {
  transform: translateY(-1px) scale(1.02);
  box-shadow: 0 16px 40px rgba(14, 165, 233, 0.55);
  filter: brightness(1.03);
}

.btn-primary:active {
  transform: translateY(0);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.45);
  filter: brightness(0.98);
}

.btn-secondary {
  color: var(--site-accent);
  background: rgba(255, 255, 255, 0.9);
  border-color: color-mix(in srgb, var(--site-accent) 16%, transparent);
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.12);
  backdrop-filter: blur(12px);
}

.btn-secondary:hover {
  background: #ffffff;
  box-shadow: 0 10px 32px rgba(15, 23, 42, 0.16);
}

.hero__stats {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.hero__stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hero__stat-value {
  font-weight: 700;
  font-size: 15px;
  color: var(--site-text);
}

.hero__stat-label {
  font-size: 13px;
  color: var(--site-text-muted);
}

.hero__visual {
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero__cardBorder {
  width: 100%;
  max-width: 400px;
  padding: 10px;
  border-radius: 26px;
  position: relative;
  box-shadow:
    0 18px 45px rgba(15, 23, 42, 0.16),
    0 26px 60px rgba(15, 23, 42, 0.25);
}

.hero__cardBorder::before,
.hero__cardBorder::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  z-index: -1;
}

.hero__cardBorder::before {
  background: linear-gradient(120deg, #f97316, #fb923c);
  transform: translateY(-8px);
}

.hero__cardBorder::after {
  background: linear-gradient(120deg, #2563eb, #1d4ed8);
  transform: translateY(10px);
}

.hero__card {
  width: 100%;
  padding: 22px 24px 20px;
  border-radius: 22px;
  background: radial-gradient(circle at top left, rgba(255, 255, 255, 0.96), rgba(255, 250, 245, 0.98));
  backdrop-filter: blur(18px);
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.14);
}

/* --- Card Glow Effect --- */
.card-glow {
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  background: conic-gradient(
    from 180deg,
    rgba(37, 99, 235, 0.15),
    rgba(249, 115, 22, 0.15),
    rgba(16, 185, 129, 0.1),
    rgba(37, 99, 235, 0.15)
  );
  z-index: -1;
  opacity: 0;
  transition: opacity 0.4s ease-out;
  filter: blur(8px);
}

.hero__card:hover .card-glow {
  opacity: 1;
}

/* --- Card Header --- */
.hero__card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}

.hero__card-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.hero__card-header-right {
  display: flex;
  align-items: center;
}

.hero__card-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--site-text);
  letter-spacing: -0.01em;
}

.hero__card-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--site-accent);
}

.hero__card-dot--animated {
  animation: cardDotPulse 2s ease-in-out infinite;
  box-shadow: 0 0 10px color-mix(in srgb, var(--site-accent) 50%, transparent);
}

@keyframes cardDotPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.6; }
}

.hero__card-live {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 999px;
}

.hero__card-live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  animation: liveDotPulse 1.5s ease-in-out infinite;
  box-shadow: 0 0 6px rgba(16, 185, 129, 0.5);
}

@keyframes liveDotPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

/* --- Card Body --- */
.hero__card-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* --- Utilization Section --- */
.hero__card-utilization {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hero__card-utilization-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero__card-utilization-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--site-text-secondary);
}

.hero__card-icon {
  width: 16px;
  height: 16px;
  color: var(--site-text-muted);
  flex-shrink: 0;
}

.hero__card-bar {
  position: relative;
  height: 10px;
  border-radius: 999px;
  background: var(--site-bg-elevated);
  overflow: hidden;
}

.hero__card-fill {
  display: block;
  width: 76%;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb, #3b82f6, #f97316);
  position: relative;
}

.hero__card-fill--animated {
  width: 0;
  animation: barFillIn 1.8s cubic-bezier(0.22, 1, 0.36, 1) 0.5s forwards;
}

.hero__card-bar-glow {
  position: absolute;
  top: 0;
  right: 0;
  width: 20px;
  height: 100%;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 999px;
  filter: blur(4px);
  opacity: 0;
  animation: barGlowPulse 2s ease-in-out 2.3s infinite;
}

@keyframes barFillIn {
  from { width: 0; }
  to { width: 76%; }
}

@keyframes barGlowPulse {
  0%, 100% { opacity: 0; }
  50% { opacity: 0.8; }
}

.hero__card-bar-labels {
  display: flex;
  justify-content: space-between;
  font-size: 10px;
  color: var(--site-text-muted);
  opacity: 0.6;
  margin-top: 2px;
}

.hero__card-percent {
  font-size: 20px;
  font-weight: 800;
  line-height: 1;
  color: var(--site-accent);
  letter-spacing: -0.02em;
}

/* --- Divider --- */
.hero__card-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(226, 232, 240, 0.6), transparent);
}

/* --- Stats Grid --- */
.hero__card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  align-items: stretch;
}

.hero__card-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 80px;
  padding: 14px 8px 12px;
  background: var(--site-bg-elevated);
  border-radius: 14px;
  text-align: center;
  border: 1px solid transparent;
  transition: all 0.25s ease-out;
  cursor: default;
  position: relative;
  overflow: hidden;
}

.hero__card-cell::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: radial-gradient(circle at 50% 0%, currentColor, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s ease-out;
}

.hero__card-cell:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.hero__card-cell:hover::before {
  opacity: 0.04;
}

.hero__card-cell--success { color: #10b981; }
.hero__card-cell--info { color: #3b82f6; }
.hero__card-cell--accent { color: #f97316; }

.hero__card-cell--success:hover { border-color: rgba(16, 185, 129, 0.2); }
.hero__card-cell--info:hover { border-color: rgba(59, 130, 246, 0.2); }
.hero__card-cell--accent:hover { border-color: rgba(249, 115, 22, 0.2); }

.hero__card-cell-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-bottom: 2px;
}

.hero__card-cell-icon svg {
  width: 16px;
  height: 16px;
}

.hero__card-cell--success .hero__card-cell-icon {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.hero__card-cell--info .hero__card-cell-icon {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.hero__card-cell--accent .hero__card-cell-icon {
  background: rgba(249, 115, 22, 0.1);
  color: #f97316;
}

.hero__card-num {
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 17px;
  line-height: 1.2;
  color: var(--site-text);
  letter-spacing: -0.02em;
}

.hero__card-num--highlight {
  color: #10b981;
}

.hero__card-num--accent {
  color: #f97316;
}

.hero__card-tag {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 500;
  line-height: 1.4;
  color: var(--site-text-muted);
}

/* --- Trend Indicators --- */
.hero__card-trend {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 6px;
  margin-top: 2px;
}

.hero__card-trend svg {
  width: 10px;
  height: 10px;
}

.hero__card-trend--up {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.hero__card-trend--neutral {
  color: var(--site-text-muted);
  background: var(--site-bg-elevated);
  font-weight: 500;
}

/* --- Card Footer --- */
.hero__card-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding-top: 12px;
  border-top: 1px solid rgba(226, 232, 240, 0.4);
}

.hero__card-footer-text {
  font-size: 10px;
  color: var(--site-text-muted);
  opacity: 0.7;
}

.hero__card-footer-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #10b981;
  animation: footerDotPulse 2s ease-in-out infinite;
}

@keyframes footerDotPulse {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* --- Reduced Motion --- */
@media (prefers-reduced-motion: reduce) {
  .hero__card-dot--animated,
  .hero__card-live-dot,
  .hero__card-fill--animated,
  .hero__card-bar-glow,
  .hero__card-footer-dot {
    animation: none;
  }
  .hero__card-fill--animated {
    width: 76%;
  }
  .hero__card-cell {
    transition: none;
  }
}

/* Hero 卡片入场动画 */
.hero__visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: heroCardFloat 1s ease-out 0.2s both;
}

.hero__visual::before {
  content: '';
  position: absolute;
  inset: -40px -32px -32px;
  border-radius: 36px;
  background:
    radial-gradient(circle at top left, rgba(254, 215, 170, 0.95), transparent 55%),
    radial-gradient(circle at bottom right, rgba(191, 219, 254, 0.9), transparent 55%);
  opacity: 0.9;
  filter: blur(16px);
  z-index: -2;
}

@keyframes heroCardFloat {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Hero 描述入场 */
.hero__desc {
  font-size: 17px;
  line-height: 1.65;
  color: var(--site-text-secondary);
  max-width: 42ch;
  margin: 0 0 28px;
  animation: heroTitleFadeIn 1s ease-out 0.15s both;
}

/* Hero stats 入场 */
.hero__stats {
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
  animation: heroTitleFadeIn 1s ease-out 0.45s both;
}

.hero__stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
  border-left: 2px solid var(--site-border);
  padding-left: 16px;
}

.hero__stat:first-child {
  border-left: 2px solid var(--site-accent);
}

/* ----- 通用区块 ----- */
.block {
  display: flex;
  flex-direction: column;
  gap: 32px;
  opacity: 1;
  transform: translateY(0);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.block.is-visible,
.landing-section.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.block__head {
  margin-bottom: 0;
}

.block__kicker {
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

.block__title {
  font-size: clamp(28px, 3.5vw, 36px);
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--site-text);
  margin: 0 0 10px;
}

/* 关键区块标题渐变效果 */
#features .block__title,
#pricing .block__title,
#about .block__title {
  background: var(--site-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  background-size: 200% 200%;
  animation: gradientShift 8s ease infinite;
}

.block__desc {
  font-size: 16px;
  line-height: 1.65;
  color: var(--site-text-secondary);
  max-width: 60ch;
  margin: 0;
}

/* 核心能力卡片 */
.cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.card {
  padding: 24px;
  cursor: pointer;
}

.card:hover {
  /* hover 交给全局 glass-card，避免叠加 */
}

.card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--site-accent-muted);
  color: var(--site-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.card__icon svg {
  width: 22px;
  height: 22px;
}

.card__title {
  font-size: 16px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 8px;
}

.card__desc {
  font-size: 14px;
  line-height: 1.6;
  color: var(--site-text-secondary);
  margin: 0;
}

/* 使用场景网格 */
.scenarios-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.scenario-card {
  padding: 28px;
  cursor: pointer;
}

.scenario-card:hover {
  /* hover 交给全局 glass-card */
}

.scenario-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--site-accent-muted);
  color: var(--site-accent);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.scenario-card__icon svg {
  width: 24px;
  height: 24px;
}

.scenario-card__title {
  font-size: 20px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 8px;
}

.scenario-card__desc {
  font-size: 14px;
  line-height: 1.6;
  color: var(--site-text-secondary);
  margin: 0 0 20px;
}

.scenario-card__pain,
.scenario-card__solution {
  margin-bottom: 16px;
}

.scenario-card__pain-label,
.scenario-card__solution-label {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  margin-bottom: 10px;
}

.scenario-card__pain-label {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.scenario-card__solution-label {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.scenario-card__pain-list,
.scenario-card__solution-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--site-text-secondary);
}

.scenario-card__pain-list li,
.scenario-card__solution-list li {
  margin-bottom: 6px;
}

.scenario-card__metric {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--site-border);
  text-align: center;
}

.scenario-card__metric-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--site-accent);
  margin-bottom: 4px;
}

.scenario-card__metric-desc {
  font-size: 12px;
  color: var(--site-text-muted);
}

/* 产品截图网格 */
.screenshots-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.screenshot-card {
  border-radius: var(--site-radius-lg);
  overflow: hidden;
  cursor: pointer;
}

.screenshot-card:hover {
  /* hover 交给全局 glass-card */
}

.screenshot-card__image {
  width: 100%;
  aspect-ratio: 16 / 10;
  background: var(--site-bg-elevated);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.screenshot-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--site-text-muted);
  width: 100%;
  height: 100%;
  position: relative;
}

.screenshot-placeholder svg {
  width: 64px;
  height: 64px;
  opacity: 0.5;
}

.screenshot-placeholder span {
  font-size: 14px;
  font-weight: 500;
  color: var(--site-text-secondary);
}

.screenshot-placeholder__label {
  font-size: 13px !important;
  font-weight: 600 !important;
  color: var(--site-text) !important;
}

.screenshot-placeholder__cta {
  font-size: 12px !important;
  color: var(--site-accent) !important;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.screenshot-card:hover .screenshot-placeholder__cta {
  opacity: 1;
}

.screenshot-card__image--enhanced {
  background: linear-gradient(135deg, var(--site-bg-elevated) 0%, var(--site-surface) 100%);
  overflow: hidden;
}

/* 截图模拟 UI */
.screenshot-mock {
  width: 85%;
  border-radius: 8px;
  overflow: hidden;
  background: var(--site-bg);
  border: 1px solid var(--site-border);
  margin-bottom: 8px;
}

.screenshot-mock__header {
  display: flex;
  gap: 6px;
  padding: 8px 10px;
  background: var(--site-bg-elevated);
  border-bottom: 1px solid var(--site-border);
}

.screenshot-mock__header span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--site-border-strong);
}

.screenshot-mock__header span:first-child { background: #ef4444; }
.screenshot-mock__header span:nth-child(2) { background: #fbbf24; }
.screenshot-mock__header span:nth-child(3) { background: #10b981; }

.screenshot-mock__content {
  padding: 10px;
  min-height: 80px;
}

/* Dashboard 模拟 */
.screenshot-mock__sidebar {
  width: 25%;
  height: 70px;
  background: var(--site-surface);
  border-radius: 4px;
  float: left;
  margin-right: 8px;
}

.screenshot-mock__main {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  overflow: hidden;
}

.screenshot-mock__card {
  flex: 1;
  min-width: 40%;
  height: 28px;
  background: var(--site-surface);
  border-radius: 4px;
}

.screenshot-mock__chart {
  width: 100%;
  height: 30px;
  background: linear-gradient(90deg, var(--site-accent-muted), var(--site-surface));
  border-radius: 4px;
}

/* 日历模拟 */
.screenshot-mock__calendar {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
}

.screenshot-mock__calendar div {
  height: 16px;
  border-radius: 3px;
  background: var(--site-surface);
}

.screenshot-mock__calendar div:nth-child(2),
.screenshot-mock__calendar div:nth-child(5) {
  background: var(--site-accent-muted);
}

/* 柱状图模拟 */
.screenshot-mock__bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 70px;
  padding-top: 10px;
}

.screenshot-mock__bars div {
  flex: 1;
  border-radius: 3px 3px 0 0;
  background: var(--site-accent-muted);
}

.screenshot-mock__bars div:nth-child(1) { height: 40%; }
.screenshot-mock__bars div:nth-child(2) { height: 65%; background: var(--site-accent); opacity: 0.6; }
.screenshot-mock__bars div:nth-child(3) { height: 85%; }
.screenshot-mock__bars div:nth-child(4) { height: 55%; background: var(--site-accent); opacity: 0.6; }
.screenshot-mock__bars div:nth-child(5) { height: 70%; }

/* 表格模拟 */
.screenshot-mock__table {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.screenshot-mock__table div {
  height: 18px;
  border-radius: 3px;
  background: var(--site-surface);
}

.screenshot-mock__table div:first-child {
  background: var(--site-accent-muted);
}

.screenshot-card__content {
  padding: 20px;
}

.screenshot-card__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 8px;
}

.screenshot-card__desc {
  font-size: 14px;
  line-height: 1.6;
  color: var(--site-text-secondary);
  margin: 0;
}

.screenshots-cta {
  margin-top: 32px;
  padding: 24px;
  border-radius: var(--site-radius-lg);
  text-align: center;
}

.screenshots-cta__text {
  font-size: 16px;
  color: var(--site-text-secondary);
  margin: 0 0 16px;
}

/* 客户评价网格 */
.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.testimonial-card {
  padding: 24px;
  border-radius: var(--site-radius-lg);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.testimonial-card:hover {
  /* hover 交给全局 glass-card */
}

.testimonial-card__header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.testimonial-card__avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--site-gradient);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
  flex-shrink: 0;
}

.testimonial-card__avatar--gradient {
  background: linear-gradient(135deg, #0ea5e9 0%, #8b5cf6 100%);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
}

.testimonial-card__info {
  flex: 1;
}

.testimonial-card__name {
  font-size: 15px;
  font-weight: 700;
  color: var(--site-text);
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.testimonial-card__verified {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #10b981;
  color: #fff;
}

.testimonial-card__verified svg {
  width: 10px;
  height: 10px;
}

.testimonial-card__role {
  font-size: 12px;
  color: var(--site-text-muted);
}

.testimonial-card__date {
  font-size: 11px;
  color: var(--site-text-muted);
  margin-top: 2px;
  opacity: 0.8;
}

.testimonial-card__rating {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}

.rating-star {
  color: #fbbf24;
  font-size: 14px;
  line-height: 1;
  animation: starTwinkle 2s ease-in-out infinite;
}

.rating-star:nth-child(1) { animation-delay: 0s; }
.rating-star:nth-child(2) { animation-delay: 0.1s; }
.rating-star:nth-child(3) { animation-delay: 0.2s; }
.rating-star:nth-child(4) { animation-delay: 0.3s; }
.rating-star:nth-child(5) { animation-delay: 0.4s; }

@keyframes starTwinkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(0.9); }
}

.testimonial-card__content {
  flex: 1;
}

.testimonial-card__text {
  font-size: 14px;
  line-height: 1.7;
  color: var(--site-text-secondary);
  margin: 0;
  font-style: italic;
}

.testimonial-card__highlight {
  padding-top: 16px;
  border-top: 1px solid var(--site-border);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.testimonial-card__highlight-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--site-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.testimonial-card__highlight-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--site-accent);
}

/* 功能网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* 全部模块概览 */
.modules-overview {
  padding: 32px;
  border-radius: var(--site-radius-lg);
}

.modules-overviewBorder {
  margin-top: 48px;
  padding: 10px;
  border-radius: calc(var(--site-radius-lg) + 6px);
}

.modules-overview__head {
  margin-bottom: 24px;
  text-align: center;
}

.modules-overview__title {
  font-size: 20px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 8px;
}

.modules-overview__desc {
  font-size: 14px;
  color: var(--site-text-secondary);
  margin: 0;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.module-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: var(--site-bg-elevated);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius);
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
  opacity: 1;
  transform: translateY(0);
}

.module-item.is-visible,
.module-item[data-visible="true"] {
  opacity: 1;
  transform: translateY(0);
}

.module-item:hover {
  background: var(--site-surface-hover);
  border-color: var(--site-border-strong);
  transform: translateY(-2px);
  box-shadow: var(--site-shadow);
}

.module-item__num {
  font-weight: 700;
  font-size: 13px;
  color: var(--site-accent);
  min-width: 28px;
  text-align: center;
}

.module-item__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.module-item__name {
  font-weight: 600;
  font-size: 14px;
  color: var(--site-text);
}

.module-item__desc {
  font-size: 12px;
  color: var(--site-text-muted);
}

.module-item__tag {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 6px;
  white-space: nowrap;
}

.module-item__tag--admin {
  background: var(--site-accent-muted);
  color: var(--site-accent);
}

.module-item__tag--user {
  background: rgba(139, 92, 246, 0.15);
  color: #8b5cf6;
}

.feature-card {
  display: flex;
  gap: 16px;
  padding: 24px;
  border-radius: var(--site-radius-lg);
  cursor: pointer;
}

.feature-card.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.feature-card:hover {
  /* hover 交给全局 glass-card */
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

/* 方案 */
.plans {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.plan {
  position: relative;
  padding: 24px;
  border-radius: var(--site-radius-lg);
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.plan--hot {
  border-color: color-mix(in srgb, var(--site-accent) 35%, var(--site-border));
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

.plan__btn {
  margin-top: 4px;
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
  color: var(--site-text);
  background: var(--site-surface-hover);
  border: 1px solid var(--site-border);
}

.plan__btn:hover {
  background: var(--site-border);
}

.plan__btn--primary {
  color: #fff;
  border: 0;
  background: var(--site-gradient);
  box-shadow: 0 4px 20px rgba(14, 165, 233, 0.35);
}

.plan__btn--primary:hover {
  box-shadow: 0 8px 28px rgba(14, 165, 233, 0.4);
}

.faq {
  padding: 28px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
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
  margin: 0 0 16px;
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

.qa__a code {
  font-size: 13px;
  padding: 2px 6px;
  background: var(--site-surface-hover);
  border-radius: 6px;
}

/* 关于 bento */
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

/* 帮助 */
.help-grid {
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

.steps code {
  font-size: 13px;
  padding: 2px 6px;
  background: var(--site-surface-hover);
  border-radius: 6px;
}

.panel .qa {
  margin-top: 14px;
}

.panel .qa:first-of-type {
  margin-top: 0;
}

.contact-band {
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

.contact-band__title {
  font-size: 18px;
  font-weight: 700;
  color: var(--site-text);
  margin: 0 0 6px;
}

.contact-band__desc {
  font-size: 14px;
  color: var(--site-text-muted);
  line-height: 1.65;
  max-width: 60ch;
  margin: 0;
}

.contact-band__info {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 20px;
  margin-top: 14px;
}

.contact-band__link {
  font-size: 14px;
  font-weight: 500;
  color: var(--site-accent);
  text-decoration: none;
  transition: color 0.2s ease;
}

.contact-band__link:hover {
  color: var(--site-text);
}

.contact-band__link--text {
  color: var(--site-text-secondary);
  cursor: default;
}

.contact-band__link--text:hover {
  color: var(--site-text-secondary);
}

.contact-band__meta {
  font-size: 13px;
  color: var(--site-text-muted);
}

.contact-band__actions {
  display: flex;
  gap: 12px;
}

/* ----- 底部 CTA ----- */
.cta {
  padding: 48px 32px;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
  box-shadow: var(--site-shadow);
}

.cta__inner {
  max-width: 640px;
  margin: 0 auto;
  text-align: center;
}

.cta__title {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--site-text);
  margin: 0 0 10px;
}

.cta__desc {
  font-size: 16px;
  color: var(--site-text-secondary);
  margin: 0 0 24px;
}

.cta__actions {
  display: flex;
  gap: 14px;
  justify-content: center;
  flex-wrap: wrap;
}

@media (max-width: 1024px) {
  .hero {
    grid-template-columns: 1fr;
    text-align: center;
    gap: 32px;
  }
  .hero__content {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .hero__desc {
    margin-left: auto;
    margin-right: auto;
  }
  .hero__cta {
    justify-content: center;
  }
  .hero__stats {
    justify-content: center;
  }
  .hero__visual {
    order: -1;
  }
  .cards {
    grid-template-columns: repeat(2, 1fr);
  }
  .scenarios-grid {
    grid-template-columns: 1fr;
  }
  .screenshots-grid {
    grid-template-columns: 1fr;
  }
  .testimonials-grid {
    grid-template-columns: 1fr;
  }
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .plans {
    grid-template-columns: 1fr;
  }
  .faq__grid {
    grid-template-columns: 1fr;
  }
  .bento {
    grid-template-columns: 1fr;
  }
  .tile--wide {
    grid-column: auto;
  }
  .help-grid {
    grid-template-columns: 1fr;
  }
  .contact-band {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }

  /* 偶数区块背景在平板上取消 */
  .site-home > section:nth-child(even) {
    margin-left: 0;
    margin-right: 0;
    padding-left: 0;
    padding-right: 0;
  }
}

@media (max-width: 640px) {
  .site-home {
    gap: 56px;
  }

  /* Hero 区域移动端优化 */
  .hero {
    padding: 16px 0;
  }
  .hero__title {
    font-size: 28px;
    line-height: 1.2;
  }
  .hero__desc {
    font-size: 15px;
  }
  .hero__badge {
    font-size: 12px;
    padding: 6px 12px;
  }
  .hero__stats {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  .hero__stat {
    border-left: none;
    border-top: 2px solid var(--site-border);
    padding-left: 0;
    padding-top: 12px;
    text-align: center;
  }
  .hero__stat:first-child {
    border-top-color: var(--site-accent);
  }

  /* CTA 按钮移动端 */
  .hero-cta-primary,
  .btn-primary {
    width: 100%;
    justify-content: center;
  }
  .btn-secondary {
    width: 100%;
    justify-content: center;
  }

  /* 卡片网格移动端 */
  .cards {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .features-grid {
    grid-template-columns: 1fr;
  }

  /* 标题字号调整 */
  .block__title {
    font-size: 24px;
  }
  .block__desc {
    font-size: 14px;
  }

  /* 其他网格 */
  .valueGrid {
    grid-template-columns: 1fr;
  }

  /* 运营概览卡片移动端 */
  .hero__card {
    padding: 16px;
  }
  .hero__card-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  /* 截图卡片移动端 */
  .screenshot-card__content {
    padding: 16px;
  }

  /* 评价卡片移动端 */
  .testimonial-card {
    padding: 20px;
  }
  .testimonial-card__header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .testimonial-card__rating {
    align-self: flex-start;
  }

  /* 偶数区块背景在手机上完全取消 */
  .site-home > section:nth-child(even) {
    padding: 0;
    background: transparent;
    border: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .hero__card-fill,
  .hero__title,
  .hero__title-accent,
  .hero__cta,
  .hero__desc,
  .hero__stats,
  .hero__visual,
  .rating-star,
  .hero__card-dot--animated,
  .hero__card-live,
  .hero-cta-primary::after,
  .btn-arrow {
    animation: none !important;
  }

  .hero__card-fill--animated {
    width: 76% !important;
  }

  .block,
  .landing-section,
  .feature-card,
  .module-item {
    opacity: 1 !important;
    transform: none !important;
    transition: none !important;
  }
  
  .card:hover,
  .feature-card:hover,
  .module-item:hover {
    transform: none !important;
  }
  
  .btn--primary:hover {
    transform: none !important;
  }
}
</style>
