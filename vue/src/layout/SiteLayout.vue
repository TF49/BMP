<template>
  <div class="site-shell">
    <div class="scroll-progress" :style="{ width: scrollProgress + '%' }" />

    <header class="site-nav nav-blur">
      <div class="site-nav__inner">
        <router-link class="site-brand" to="/site" aria-label="返回官网首页">
          <span class="site-brand__mark" aria-hidden="true">
            <svg class="site-brand__svg" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
              <ellipse cx="20" cy="10" rx="8" ry="4" fill="currentColor" opacity="0.95" />
              <path d="M14 10q6 14 6 22" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" opacity="0.9" />
              <path d="M18 10q4 10 4 18" stroke="currentColor" stroke-width="1.2" fill="none" stroke-linecap="round" opacity="0.7" />
              <path d="M22 10q-4 10-4 18" stroke="currentColor" stroke-width="1.2" fill="none" stroke-linecap="round" opacity="0.7" />
              <path d="M26 10q-6 14-6 22" stroke="currentColor" stroke-width="1.5" fill="none" stroke-linecap="round" opacity="0.9" />
              <circle cx="20" cy="32" r="2.5" fill="currentColor" opacity="0.95" />
            </svg>
          </span>
          <span class="site-brand__text">
            <span class="site-brand__title">羽擎</span>
            <span class="site-brand__sub">BMP</span>
          </span>
        </router-link>

        <nav class="site-links" aria-label="官网导航">
          <router-link
            class="nav-link"
            :class="{ 'nav-link--active': isSiteHome && !hash }"
            to="/site"
          >首页</router-link>
          <router-link
            class="nav-link"
            :class="{ 'nav-link--active': hash === '#features' }"
            :to="{ path: '/site', hash: '#features' }"
          >功能</router-link>
          <router-link
            class="nav-link"
            :class="{ 'nav-link--active': hash === '#pricing' }"
            :to="{ path: '/site', hash: '#pricing' }"
          >方案</router-link>
          <router-link
            class="nav-link"
            :class="{ 'nav-link--active': hash === '#about' }"
            :to="{ path: '/site', hash: '#about' }"
          >关于</router-link>
          <router-link
            class="nav-link"
            :class="{ 'nav-link--active': hash === '#help' }"
            :to="{ path: '/site', hash: '#help' }"
          >帮助</router-link>
        </nav>

        <div class="site-actions">
          <ThemeSelector />
          <button class="btn-primary" type="button" @click="goLogin">去登录</button>
          <button
            class="site-burger"
            type="button"
            @click="drawerOpen = true"
            aria-label="打开导航菜单"
          >
            <svg class="burger-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>
      </div>
    </header>

    <main class="site-main">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="site-footer__inner">
        <div class="site-footer__brand">
          <span class="site-footer__title">羽擎</span>
          <span class="site-footer__tagline">更智能的预约、运营与数据管理</span>
        </div>
        <div class="site-footer__nav">
          <router-link class="site-footer__link" :to="{ path: '/site', hash: '#features' }">功能</router-link>
          <router-link class="site-footer__link" :to="{ path: '/site', hash: '#pricing' }">方案</router-link>
          <router-link class="site-footer__link" :to="{ path: '/site', hash: '#about' }">关于</router-link>
          <router-link class="site-footer__link" :to="{ path: '/site', hash: '#help' }">帮助</router-link>
        </div>
        <div class="site-footer__copy">
          © 2026 BMP · Badminton Management Platform
        </div>
      </div>
    </footer>

    <el-drawer
      v-model="drawerOpen"
      direction="rtl"
      size="320px"
      :with-header="false"
      class="site-drawer"
    >
      <div class="site-drawer__content">
        <div class="site-drawer__header">
          <span class="site-drawer__title">导航</span>
          <button type="button" class="site-drawer__close" @click="drawerOpen = false" aria-label="关闭菜单">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
              <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" />
            </svg>
          </button>
        </div>
        <nav class="site-drawer__links" role="navigation" aria-label="移动端导航">
          <router-link class="site-drawer__link" to="/site" @click="drawerOpen = false">首页</router-link>
          <router-link class="site-drawer__link" :to="{ path: '/site', hash: '#features' }" @click="drawerOpen = false">功能</router-link>
          <router-link class="site-drawer__link" :to="{ path: '/site', hash: '#pricing' }" @click="drawerOpen = false">方案</router-link>
          <router-link class="site-drawer__link" :to="{ path: '/site', hash: '#about' }" @click="drawerOpen = false">关于</router-link>
          <router-link class="site-drawer__link" :to="{ path: '/site', hash: '#help' }" @click="drawerOpen = false">帮助</router-link>
        </nav>
        <div class="site-drawer__actions">
          <div class="site-drawer__theme">
            <ThemeSelector />
          </div>
          <div class="site-drawer__cta">
            <el-button class="site-cta" type="primary" @click="goLoginFromDrawer">去登录</el-button>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 悬浮客服 -->
    <FloatingContact />

    <!-- 返回顶部按钮 -->
    <transition name="back-to-top">
      <button
        v-show="showBackToTop"
        class="back-to-top"
        @click="scrollToTop"
        aria-label="返回顶部"
        type="button"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="18 15 12 9 6 15"/>
        </svg>
      </button>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ThemeSelector from '@/components/ThemeSelector.vue'
import FloatingContact from '@/components/FloatingContact.vue'

const router = useRouter()
const route = useRoute()
const drawerOpen = ref(false)
const scrollProgress = ref(0)
const showBackToTop = ref(false)

const isSiteHome = computed(() => (route.path || '').replace(/\/$/, '') === '/site')
const hash = computed(() => route.hash || '')

const goLogin = () => {
  router.push('/login').catch(() => {})
}

const goLoginFromDrawer = () => {
  drawerOpen.value = false
  goLogin()
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const updateScrollProgress = () => {
  const doc = document.documentElement
  const scrollTop = window.scrollY || doc.scrollTop || 0
  const scrollHeight = doc.scrollHeight - doc.clientHeight
  const progress = scrollHeight > 0 ? (scrollTop / scrollHeight) * 100 : 0
  scrollProgress.value = Math.max(0, Math.min(100, progress))

  // 显示/隐藏返回顶部按钮（滚动超过 300px 时显示）
  showBackToTop.value = scrollTop > 300
}

onMounted(() => {
  updateScrollProgress()
  window.addEventListener('scroll', updateScrollProgress, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateScrollProgress)
})
</script>

<style scoped>
.site-nav {
  position: fixed;
  top: 0;
  z-index: 50;
  left: 0;
  right: 0;
  padding: 14px 16px;
}

.site-nav__inner {
  max-width: var(--site-container);
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  height: var(--site-nav-height);
  padding: 0 20px;
  border: 1px solid var(--site-border);
  border-radius: var(--site-radius-lg);
  box-shadow: var(--site-shadow);
}

.site-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--site-text);
  cursor: pointer;
}

.site-brand__mark {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: var(--site-gradient);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.site-brand__svg {
  width: 24px;
  height: 24px;
}

.site-brand__text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.site-brand__title {
  font-weight: 700;
  font-size: 15px;
  letter-spacing: -0.02em;
}

.site-brand__sub {
  font-size: 11px;
  color: var(--site-text-secondary);
  font-weight: 500;
  letter-spacing: 0.05em;
}

.site-links {
  display: flex;
  align-items: center;
  gap: 4px;
}

.site-link {
  text-decoration: none;
  color: var(--site-text-secondary);
  font-weight: 500;
  font-size: 14px;
  padding: 10px 16px;
  border-radius: 10px;
  transition: color 0.2s ease, background 0.2s ease;
}

.site-link:hover {
  color: var(--site-text);
  background: var(--site-surface-hover);
}

.site-link.router-link-exact-active,
.site-link.site-link--active {
  color: var(--site-accent);
  background: var(--site-accent-muted);
}

.site-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.site-burger {
  display: none;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  border: 1px solid var(--site-border);
  background: var(--site-surface);
  color: var(--site-text);
  cursor: pointer;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.site-burger:hover {
  background: var(--site-surface-hover);
}

.burger-svg {
  width: 20px;
  height: 20px;
}

.site-main {
  max-width: var(--site-container);
  margin: 0 auto;
  padding: calc(var(--site-nav-height) + 36px) 16px 64px;
  position: relative;
  z-index: 1;
}

.site-footer {
  padding: 48px 16px 32px;
  border-top: 1px solid var(--site-border);
  background: var(--site-bg-elevated);
  position: relative;
  z-index: 1;
}

.site-footer__inner {
  max-width: var(--site-container);
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 24px;
  align-items: start;
}

.site-footer__brand {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.site-footer__title {
  font-weight: 700;
  font-size: 15px;
  color: var(--site-text);
}

.site-footer__tagline {
  font-size: 13px;
  color: var(--site-text-muted);
}

.site-footer__nav {
  display: flex;
  gap: 24px;
}

.site-footer__link {
  font-size: 13px;
  color: var(--site-text-secondary);
  text-decoration: none;
  transition: color 0.2s ease;
}

.site-footer__link:hover {
  color: var(--site-accent);
}

.site-footer__copy {
  font-size: 12px;
  color: var(--site-text-muted);
  text-align: right;
}

@media (max-width: 900px) {
  .site-links {
    display: none;
  }
  .site-burger {
    display: inline-flex;
  }
  .site-footer__inner {
    grid-template-columns: 1fr;
    text-align: center;
  }
  .site-footer__nav {
    justify-content: center;
  }
  .site-footer__copy {
    text-align: center;
  }
}

:deep(.site-drawer .el-drawer__body) {
  padding: 0;
}

.site-drawer__content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 24px;
  background: var(--site-bg-elevated);
}

.site-drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.site-drawer__title {
  font-weight: 700;
  font-size: 16px;
  color: var(--site-text);
}

.site-drawer__close {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid var(--site-border);
  background: var(--site-surface);
  color: var(--site-text);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.site-drawer__close:hover {
  background: var(--site-surface-hover);
}

.site-drawer__links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.site-drawer__link {
  padding: 14px 16px;
  border-radius: 12px;
  font-weight: 500;
  font-size: 15px;
  color: var(--site-text);
  text-decoration: none;
  background: var(--site-surface);
  border: 1px solid var(--site-border);
  transition: background 0.2s ease, color 0.2s ease;
}

.site-drawer__link:hover {
  background: var(--site-surface-hover);
}

.site-drawer__link.router-link-exact-active {
  color: var(--site-accent);
  background: var(--site-accent-muted);
  border-color: rgba(14, 165, 233, 0.3);
}

.site-drawer__actions {
  margin-top: auto;
  padding-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.site-drawer__theme {
  display: flex;
  justify-content: center;
}

.site-drawer__cta {
  width: 100%;
}

/* 返回顶部按钮 */
.back-to-top {
  position: fixed;
  bottom: 32px;
  right: 108px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 1px solid var(--site-border);
  background: var(--site-surface);
  backdrop-filter: blur(12px);
  color: var(--site-text);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.12);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 99;
}

.back-to-top:hover {
  background: var(--site-accent);
  color: #fff;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(14, 165, 233, 0.35);
}

.back-to-top svg {
  width: 20px;
  height: 20px;
}

/* 返回顶部按钮动画 */
.back-to-top-enter-active,
.back-to-top-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.back-to-top-enter-from,
.back-to-top-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.8);
}

@media (max-width: 768px) {
  .back-to-top {
    bottom: 108px;
    right: 24px;
    width: 44px;
    height: 44px;
  }
}
</style>
