<template>
  <div class="register-container">
    <div class="register-background">
      <div class="gradient-overlay"></div>
      <div class="floating-badminton">
        <svg viewBox="0 0 100 100" class="badminton-icon" v-for="(item, index) in shuttlecocks" :key="index" :style="{ left: item.x + '%', top: item.y + '%', animationDelay: item.delay + 's', transform: 'rotate(' + item.rotate + 'deg)' }">
          <ellipse cx="50" cy="20" rx="18" ry="8" fill="rgba(255,255,255,0.4)" opacity="0.9"/>
          <ellipse cx="50" cy="20" rx="15" ry="6" fill="rgba(59,130,246,0.3)"/>
          <path d="M32 20 Q50 60 50 90" stroke="rgba(255,255,255,0.7)" stroke-width="2.5" fill="none" stroke-linecap="round"/>
          <path d="M35 20 Q50 55 50 85" stroke="rgba(59,130,246,0.5)" stroke-width="2" fill="none" stroke-linecap="round"/>
          <path d="M40 20 Q50 50 50 75" stroke="rgba(255,255,255,0.5)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
          <path d="M45 20 Q50 45 50 65" stroke="rgba(249,115,22,0.4)" stroke-width="1" fill="none" stroke-linecap="round"/>
          <path d="M68 20 Q50 60 50 90" stroke="rgba(255,255,255,0.7)" stroke-width="2.5" fill="none" stroke-linecap="round"/>
          <path d="M65 20 Q50 55 50 85" stroke="rgba(59,130,246,0.5)" stroke-width="2" fill="none" stroke-linecap="round"/>
          <path d="M60 20 Q50 50 50 75" stroke="rgba(255,255,255,0.5)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
          <path d="M55 20 Q50 45 50 65" stroke="rgba(249,115,22,0.4)" stroke-width="1" fill="none" stroke-linecap="round"/>
          <circle cx="50" cy="95" r="4" fill="rgba(255,255,255,0.9)"/>
          <circle cx="50" cy="95" r="2.5" fill="rgba(59,130,246,0.6)"/>
        </svg>
      </div>
      <div class="particles">
        <div class="particle" v-for="(particle, index) in particles" :key="'p'+index" :style="{ left: particle.x + '%', top: particle.y + '%', animationDelay: particle.delay + 's' }"></div>
      </div>
    </div>

    <div class="register-content">
      <!-- 左侧：与登录页一致的人物动画区域（桌面端显示） -->
      <div class="register-left-chars">
        <div class="chars-brand">
          <h1 class="chars-brand-text">羽擎</h1>
          <p class="chars-brand-tagline">让运动更智能</p>
        </div>
        <div class="chars-features">
          <div class="chars-feature-item">
            <span class="chars-feature-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
            </span>
            <span>场地预约</span>
          </div>
          <div class="chars-feature-item">
            <span class="chars-feature-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
            </span>
            <span>课程与教练</span>
          </div>
          <div class="chars-feature-item">
            <span class="chars-feature-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            </span>
            <span>赛事报名</span>
          </div>
          <div class="chars-feature-item">
            <span class="chars-feature-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
            </span>
            <span>数据看板</span>
          </div>
        </div>
        <AnimatedCharactersScene
          :is-typing="charsSceneIsTyping"
          :show-password="showPassword"
          :password-length="registerForm.password ? registerForm.password.length : 0"
          :has-login-error="hasRegisterError"
        />
        <div class="chars-footer-spacer" aria-hidden="true"></div>
        <div class="chars-footer">
          <span class="chars-footer-copyright">{{ systemInfo }}</span>
        </div>
      </div>

      <div class="register-right">
        <div class="register-card">
          <div class="register-mobile-brand">
            <span class="register-mobile-brand-text">羽擎</span>
          </div>
          <div class="register-header">
            <div class="welcome-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
            </div>
            <h2>创建账号</h2>
            <p>注册成为羽毛球管理系统新用户</p>
          </div>

          <el-form ref="registerForm" :model="registerForm" :rules="registerRules" class="register-form" size="medium">
            <el-form-item prop="username" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.username, 'uiverse-has-value': registerForm.username }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                  <el-input
                    id="username-input"
                    v-model="registerForm.username"
                    type="text"
                    auto-complete="username"
                    placeholder=" "
                    class="uiverse-input-el"
                    aria-label="账号"
                    aria-required="true"
                    @focus="inputFocused.username = true"
                    @blur="inputFocused.username = false"
                  />
                  <label class="floating-label">账号</label>
                  <div class="input-progress"></div>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="email" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.email, 'uiverse-has-value': registerForm.email }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/>
                    </svg>
                  </div>
                  <el-input
                    id="email-input"
                    v-model="registerForm.email"
                    type="email"
                    auto-complete="email"
                    placeholder=" "
                    class="uiverse-input-el"
                    aria-label="邮箱"
                    aria-required="true"
                    @focus="inputFocused.email = true"
                    @blur="inputFocused.email = false"
                  />
                  <label class="floating-label">邮箱</label>
                  <div class="input-progress"></div>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="phone" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.phone, 'uiverse-has-value': registerForm.phone }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/>
                    </svg>
                  </div>
                  <el-input
                    id="phone-input"
                    v-model="registerForm.phone"
                    type="tel"
                    auto-complete="tel"
                    placeholder=" "
                    class="uiverse-input-el"
                    aria-label="手机号"
                    aria-required="true"
                    @focus="inputFocused.phone = true"
                    @blur="inputFocused.phone = false"
                  />
                  <label class="floating-label">手机号</label>
                  <div class="input-progress"></div>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="password" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.password, 'uiverse-has-value': registerForm.password }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6z M18,20H6V10h12V20z M12,17c1.1,0,2-0.9,2-2s-0.9-2-2-2s-2,0.9-2,2S10.9,17,12,17z"/>
                    </svg>
                  </div>
                  <el-input
                    id="password-input"
                    v-model="registerForm.password"
                    :type="showPassword ? 'text' : 'password'"
                    auto-complete="new-password"
                    placeholder=" "
                    class="uiverse-input-el"
                    aria-label="密码"
                    aria-required="true"
                    @focus="inputFocused.password = true"
                    @blur="inputFocused.password = false"
                  />
                  <label class="floating-label">密码</label>
                  <div class="input-progress"></div>
                  <button
                    type="button"
                    class="uiverse-password-toggle"
                    @click="showPassword = !showPassword"
                    :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                    tabindex="0"
                  >
                    <svg v-if="!showPassword" class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                    </svg>
                    <svg v-else class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zm2.76 9.54l2.17 2.17c-.43.51-.9.96-1.41 1.33-1.73 1.26-4.01 2.16-6.52 2.16-2.76 0-5-2.24-5-5 0-2.52.9-4.79 2.16-6.52.37-.51.82-.98 1.33-1.41l2.17 2.17C14.63 11.36 13.34 12 12 12c-1.66 0-3-1.34-3-3 0-.74.26-1.42.7-1.96L3 3l1.96-.7C5.58 1.26 8.3 1 12 1c3.89 0 7.29 1.26 10.04 3.34L20.33 6.43C17.58 4.26 14.96 3 12 3 6.48 3 2.39 6.92 1.59 12c.8 5.11 4.89 9 10.41 9 2.96 0 5.58-1.26 7.33-3.43l-3.17-3.17C14.42 16.74 13.14 17 12 17z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="confirmPassword" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.confirmPassword, 'uiverse-has-value': registerForm.confirmPassword }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6z M18,20H6V10h12V20z M12,17c1.1,0,2-0.9,2-2s-0.9-2-2-2s-2,0.9-2,2S10.9,17,12,17z"/>
                    </svg>
                  </div>
                  <el-input
                    id="confirm-password-input"
                    v-model="registerForm.confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    auto-complete="new-password"
                    placeholder=" "
                    class="uiverse-input-el"
                    aria-label="确认密码"
                    aria-required="true"
                    @focus="inputFocused.confirmPassword = true"
                    @blur="inputFocused.confirmPassword = false"
                    @keyup.enter.native="handleRegister"
                  />
                  <label class="floating-label">确认密码</label>
                  <div class="input-progress"></div>
                  <button
                    type="button"
                    class="uiverse-password-toggle"
                    @click="showConfirmPassword = !showConfirmPassword"
                    :aria-label="showConfirmPassword ? '隐藏密码' : '显示密码'"
                    tabindex="0"
                  >
                    <svg v-if="!showConfirmPassword" class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                    </svg>
                    <svg v-else class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zm2.76 9.54l2.17 2.17c-.43.51-.9.96-1.41 1.33-1.73 1.26-4.01 2.16-6.52 2.16-2.76 0-5-2.24-5-5 0-2.52.9-4.79 2.16-6.52.37-.51.82-.98 1.33-1.41l2.17 2.17C14.63 11.36 13.34 12 12 12c-1.66 0-3-1.34-3-3 0-.74.26-1.42.7-1.96L3 3l1.96-.7C5.58 1.26 8.3 1 12 1c3.89 0 7.29 1.26 10.04 3.34L20.33 6.43C17.58 4.26 14.96 3 12 3 6.48 3 2.39 6.92 1.59 12c.8 5.11 4.89 9 10.41 9 2.96 0 5.58-1.26 7.33-3.43l-3.17-3.17C14.42 16.74 13.14 17 12 17z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="code" v-if="captchaEnabled">
              <label for="code-input" class="input-label">验证码</label>
              <div class="input-wrapper code-input-wrapper">
                <span class="input-icon" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 9.9-1"/>
                  </svg>
                </span>
                <el-input
                  id="code-input"
                  v-model="registerForm.code"
                  auto-complete="off"
                  placeholder="请输入验证码"
                  class="custom-input"
                  aria-label="验证码"
                  aria-required="true"
                  @keyup.enter.native="handleRegister"
                />
                <button 
                  type="button"
                  class="code-image" 
                  @click="getCode"
                  aria-label="刷新验证码"
                  title="点击刷新验证码"
                >
                  <img :src="codeUrl" alt="验证码图片" v-if="codeUrl"/>
                  <span class="code-refresh-hint" v-if="codeUrl">点击刷新</span>
                </button>
              </div>
            </el-form-item>

            <el-form-item class="register-button-item">
              <el-button
                :loading="loading"
                type="primary"
                class="register-button"
                @click.native.prevent="handleRegister"
                :disabled="loading"
                aria-label="注册按钮"
              >
                <span v-if="!loading" class="button-content">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="button-icon" aria-hidden="true">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                  <span>立即注册</span>
                </span>
                <span v-else class="button-content">
                  <svg class="loading-spinner" viewBox="0 0 24 24" aria-hidden="true">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none" stroke-dasharray="31.4 31.4" stroke-linecap="round"/>
                  </svg>
                  <span>注册中...</span>
                </span>
              </el-button>
            </el-form-item>

            <div class="register-footer">
              <span class="login-link">
                已有账号？<router-link :to="'/login'">立即登录</router-link>
              </span>
            </div>
          </el-form>
        </div>

        <div class="system-info">
          <span>{{ systemInfo }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getCodeImg, register } from '@/api/login'
import defaultSettings from '@/settings'
import AnimatedCharactersScene from '@/components/AnimatedCharactersScene.vue'

export default {
  name: 'Register',
  components: {
    AnimatedCharactersScene
  },
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入您的账号'))
      } else if (!/^[a-zA-Z0-9_]{4,20}$/.test(value)) {
        callback(new Error('账号必须为4-20位字母、数字或下划线'))
      } else {
        callback()
      }
    }

    const validateEmail = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入您的邮箱'))
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error('请输入有效的邮箱地址'))
      } else {
        callback()
      }
    }

    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入您的手机号'))
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error('请输入有效的手机号'))
      } else {
        callback()
      }
    }

    const validatePassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入您的密码'))
      } else if (value.length < 6 || value.length > 20) {
        callback(new Error('密码长度必须为6-20位'))
      } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
        callback(new Error('密码必须包含大小写字母和数字'))
      } else {
        callback()
      }
    }

    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }

    return {
      title: process.env.VUE_APP_TITLE,
      systemInfo: defaultSettings.footerContent,
      codeUrl: '',
      registerForm: {
        username: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: '',
        code: '',
        uuid: ''
      },
      registerRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        email: [{ required: true, trigger: 'blur', validator: validateEmail }],
        phone: [{ required: true, trigger: 'blur', validator: validatePhone }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        confirmPassword: [{ required: true, trigger: 'blur', validator: validateConfirmPassword }],
        code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
      },
      loading: false,
      captchaEnabled: true,
      redirect: undefined,
      shuttlecocks: [],
      particles: [],
      showPassword: false,
      showConfirmPassword: false,
      hasRegisterError: false,
      inputFocused: {
        username: false,
        email: false,
        phone: false,
        password: false,
        confirmPassword: false
      }
    }
  },
  computed: {
    charsSceneIsTyping() {
      return this.inputFocused.username || this.inputFocused.email ||
        this.inputFocused.phone || this.inputFocused.password ||
        this.inputFocused.confirmPassword
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.initAnimations()
    this.getCode()
  },
  methods: {
    initAnimations() {
      this.shuttlecocks = []
      for (let i = 0; i < 8; i++) {
        this.shuttlecocks.push({
          x: Math.random() * 80 + 10,
          y: Math.random() * 60 + 20,
          delay: Math.random() * 5,
          rotate: Math.random() * 360
        })
      }

      this.particles = []
      for (let i = 0; i < 20; i++) {
        this.particles.push({
          x: Math.random() * 100,
          y: Math.random() * 100,
          delay: Math.random() * 10
        })
      }
    },
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled
        if (this.captchaEnabled) {
          this.codeUrl = 'data:image/gif;base64,' + res.img
          this.registerForm.uuid = res.uuid
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.hasRegisterError = false
          register(this.registerForm)
            .then(response => {
              if (response.code === 200) {
                this.$message.success('注册成功，请登录！')
                setTimeout(() => {
                  this.$router.push({ path: '/login' }).catch(()=>{})
                }, 1000)
              } else {
                this.hasRegisterError = true
                this.$message.error(response.message || '注册失败')
                this.loading = false
                if (this.captchaEnabled) {
                  this.getCode()
                }
              }
            })
            .catch(error => {
              console.error('注册失败:', error)
              this.hasRegisterError = true
              this.$message.error(error.message || '注册失败，请稍后重试')
              this.loading = false
              if (this.captchaEnabled) {
                this.getCode()
              }
            })
        } else {
          this.$message.warning('请填写完整的注册信息')
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  
  @media (prefers-reduced-motion: reduce) {
    *,
    *::before,
    *::after {
      animation-duration: 0.01ms !important;
      animation-iteration-count: 1 !important;
      transition-duration: 0.01ms !important;
    }
  }
}

.register-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--color-background, #0A1628) 0%, var(--color-primary-dark, #1E3A8A) 30%, var(--color-primary, #3B82F6) 60%, var(--color-background, #0F172A) 100%);
  z-index: 0;
  animation: backgroundShift 15s ease-in-out infinite;
}

@keyframes backgroundShift {
  0%, 100% {
    background: linear-gradient(135deg, var(--color-background, #0A1628) 0%, var(--color-primary-dark, #1E3A8A) 30%, var(--color-primary, #3B82F6) 60%, var(--color-background, #0F172A) 100%);
  }
  50% {
    background: linear-gradient(135deg, var(--color-primary-dark, #1E3A8A) 0%, var(--color-primary, #3B82F6) 30%, var(--color-primary-light, #60A5FA) 60%, var(--color-background, #0A1628) 100%);
  }
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(ellipse at 20% 30%, rgba(59, 130, 246, 0.25) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(249, 115, 22, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 50%, rgba(96, 165, 250, 0.15) 0%, transparent 70%),
    radial-gradient(ellipse at 10% 80%, rgba(22, 163, 74, 0.1) 0%, transparent 40%);
  animation: overlayPulse 8s ease-in-out infinite;
}

@keyframes overlayPulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.8;
  }
}

.floating-badminton {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;

  .badminton-icon {
    position: absolute;
    width: 70px;
    height: 70px;
    opacity: 0;
    filter: drop-shadow(0 4px 12px rgba(59, 130, 246, 0.4));
    animation: floatShuttlecock 10s ease-in-out infinite;

    @for $i from 1 through 8 {
      &:nth-child(#{$i}) {
        animation-delay: #{$i * 0.6}s;
        animation-duration: #{8 + $i * 0.5}s;
      }
    }
  }
}

@keyframes floatShuttlecock {
  0% {
    opacity: 0;
    transform: translateY(100vh) translateX(0) rotate(0deg) scale(0.8);
  }
  10% {
    opacity: 0.7;
    transform: translateY(80vh) translateX(20px) rotate(45deg) scale(1);
  }
  30% {
    opacity: 0.8;
    transform: translateY(50vh) translateX(-15px) rotate(90deg) scale(1.1);
  }
  50% {
    opacity: 0.6;
    transform: translateY(20vh) translateX(30px) rotate(180deg) scale(1);
  }
  70% {
    opacity: 0.7;
    transform: translateY(10vh) translateX(-20px) rotate(225deg) scale(0.9);
  }
  90% {
    opacity: 0.5;
    transform: translateY(-10vh) translateX(10px) rotate(270deg) scale(0.8);
  }
  100% {
    opacity: 0;
    transform: translateY(-20vh) translateX(0) rotate(360deg) scale(0.6);
  }
}

.particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  overflow: hidden;

  .particle {
    position: absolute;
    width: 8px;
    height: 8px;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 50%;
    animation: particleFloat 12s ease-in-out infinite;
    box-shadow: 0 0 12px rgba(96, 165, 250, 0.6);
    opacity: 0;
    
    @for $i from 1 through 20 {
      &:nth-child(#{$i}) {
        animation-delay: #{$i * 0.6}s;
        animation-duration: 16s;
        left: #{$i * 5%};
        width: #{(4 + $i) * 0.5}px;
        height: #{(4 + $i) * 0.5}px;
      }
    }
  }
}

@keyframes particleFloat {
  0%, 100% {
    opacity: 0;
    transform: translateY(100vh) translateX(0) scale(0.5);
  }
  10% {
    opacity: 0.8;
    transform: translateY(80vh) translateX(20px) scale(1);
  }
  30% {
    opacity: 0.6;
    transform: translateY(50vh) translateX(-20px) scale(1.2);
  }
  50% {
    opacity: 0.7;
    transform: translateY(25vh) translateX(25px) scale(1);
  }
  70% {
    opacity: 0.5;
    transform: translateY(5vh) translateX(-15px) scale(0.8);
  }
  90% {
    opacity: 0.3;
    transform: translateY(-10vh) translateX(10px) scale(0.6);
  }
}

.register-content {
  display: grid;
  grid-template-columns: 1fr;
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 1;

  @media (min-width: 1024px) {
    grid-template-columns: 1fr 1fr;
  }
}

.register-left-chars {
  display: none;
  flex-direction: column;
  justify-content: space-between;
  padding: 48px;
  color: #fff;
  position: relative;

  @media (min-width: 1024px) {
    display: flex;
  }
}

.chars-brand {
  position: relative;
  z-index: 20;
  text-align: center;
}

.chars-brand-text {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.chars-brand-tagline {
  font-size: 15px;
  font-weight: 500;
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 2px;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.1);
}

.chars-features {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  position: relative;
  z-index: 20;
  margin-top: 28px;
}

.chars-feature-item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.92);
  background: rgba(255, 255, 255, 0.08);
  border-radius: 14px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: background 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease, transform 0.2s ease;
}

.chars-feature-item:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(96, 165, 250, 0.35);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.18);
  transform: translateY(-1px);
}

.chars-feature-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  min-width: 28px;
  min-height: 28px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.9) 0%, rgba(249, 115, 22, 0.85) 100%);
  border-radius: 10px;
  color: #fff;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.35);
}

.chars-feature-icon svg {
  width: 14px;
  height: 14px;
}

.chars-footer-spacer {
  flex-shrink: 0;
  height: 40px;
}

.chars-footer {
  position: absolute;
  bottom: 20px;
  left: 48px;
  right: 48px;
  z-index: 20;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.chars-footer-copyright {
  letter-spacing: 0.3px;
}

.brand-showcase {
  text-align: center;
  max-width: 520px;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-container {
  margin-bottom: 28px;
  animation: logoFloat 4s ease-in-out infinite;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-12px);
  }
}

.logo-icon {
  width: 110px;
  height: 110px;
  margin: 0 auto;
  filter: drop-shadow(0 8px 32px rgba(59, 130, 246, 0.4));
  
  .badminton-logo {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 4px 16px rgba(59, 130, 246, 0.3));
  }
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #FFFFFF;
  margin-bottom: 12px;
  letter-spacing: 2px;
  text-shadow: 0 4px 20px rgba(59, 130, 246, 0.4);
}

.brand-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 36px;
  letter-spacing: 3px;
  text-transform: uppercase;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  transition: background-color 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
  cursor: default;

  &:hover {
    background: rgba(255, 255, 255, 0.14);
    border-color: rgba(59, 130, 246, 0.5);
    box-shadow: 0 4px 20px rgba(59, 130, 246, 0.25);
  }
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  min-width: 26px;
  min-height: 26px;
  background: linear-gradient(135deg, #3B82F6, #1D4ED8);
  border-radius: 50%;
  color: #FFFFFF;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
  flex-shrink: 0;
  transition: box-shadow 0.25s ease;
}

.feature-icon svg {
  width: 12px;
  height: 12px;
}

.feature-item span:last-child {
  color: rgba(255, 255, 255, 0.95);
  font-size: 0.95rem;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.register-right {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 32px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);

  @media (min-width: 1024px) {
    width: auto;
    background: #fff;
    box-shadow: -8px 0 32px rgba(0, 0, 0, 0.08);
  }
}

.register-card {
  width: 100%;
  max-width: 440px;
  background: var(--color-card-bg, rgba(255, 255, 255, 0.95));
  border-radius: 24px;
  padding: 44px 40px;
  box-shadow: 
    0 25px 50px -12px rgba(0, 0, 0, 0.4),
    0 0 0 1px var(--color-border, rgba(255, 255, 255, 0.1)),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  animation: cardFadeIn 0.6s ease-out;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.register-mobile-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;

  @media (min-width: 1024px) {
    display: none;
  }
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.welcome-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, var(--color-primary, #3B82F6), var(--color-primary-dark, #1D4ED8));
  border-radius: 18px;
  margin-bottom: 18px;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4);
  
  svg {
    width: 32px;
    height: 32px;
    color: #FFFFFF;
  }
}

.register-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-text-primary, #0F172A);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.register-header p {
  font-size: 0.95rem;
  color: var(--color-text-secondary, #64748B);
  letter-spacing: 0.3px;
}

.register-form {
  :deep(.el-form-item) {
    margin-bottom: 22px;
    background: transparent;
  }
  
  :deep(.el-form-item__error) {
    font-size: 0.8rem;
    padding-top: 4px;
    color: #EF4444;
  }
}

.form-item-custom {
  :deep(.el-form-item__content) {
    display: block;
    width: 100%;
  }
}

.uiverse-input-container {
  width: 100%;
  position: relative;
}

.uiverse-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  background: var(--color-background, #F8FAFC);
  border-radius: 12px;
  border: 2px solid var(--color-border, #E2E8F0);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    border-color: var(--color-border-hover, #CBD5E1);
    background: var(--color-card-bg, #FFFFFF);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
  
  &.uiverse-focused {
    border-color: var(--color-primary, #3B82F6);
    background: var(--color-card-bg, #FFFFFF);
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1), 0 4px 12px rgba(0, 0, 0, 0.08);
    
    .floating-label {
      color: var(--color-primary, #3B82F6);
      font-weight: 600;
    }
    
    .input-progress {
      width: 100%;
      opacity: 1;
    }
    
    .uiverse-input-icon svg {
      color: var(--color-primary, #3B82F6);
    }
  }
  
  &.uiverse-has-value {
    .floating-label {
      top: 0;
      left: 48px;
      font-size: 0.75rem;
      color: var(--color-primary, #3B82F6);
      font-weight: 600;
      transform: translateY(-50%);
      background: linear-gradient(180deg, var(--color-card-bg, #FFFFFF) 50%, transparent 50%);
      padding: 0 6px;
    }
  }
}

.uiverse-input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  
  svg {
    width: 22px;
    height: 22px;
    color: var(--color-text-muted, #94A3B8);
    transition: color 0.3s ease;
  }
}

.uiverse-input-el {
  flex: 1;
  border: none !important;
  outline: none !important;
  background: transparent !important;
  box-shadow: none !important;
  font-size: 0.95rem;
  color: #1E293B;
  height: 48px;
  padding: 0 12px 0 0;
  
  :deep(.el-input__inner) {
    border: none !important;
    outline: none !important;
    background: transparent !important;
    box-shadow: none !important;
    color: var(--color-text-primary, #1E293B);
    font-size: 0.95rem;
    height: 48px;
    padding: 0;
    
    &::placeholder {
      color: transparent;
    }
  }
  
  :deep(.el-input__prefix),
  :deep(.el-input__suffix) {
    display: none;
  }
}

.floating-label {
  position: absolute;
  left: 48px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-muted, #94A3B8);
  font-size: 0.95rem;
  pointer-events: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent;
  padding: 0 4px;
}

.uiverse-password-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--color-text-muted, #94A3B8);
  transition: all 0.2s ease;
  flex-shrink: 0;
  margin-right: 4px;
  border-radius: 8px;
  
  &:hover {
    color: var(--color-primary, #3B82F6);
    background: var(--color-card-bg-hover, rgba(59, 130, 246, 0.08));
  }
  
  &:focus {
    outline: none;
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
  }
  
  .uiverse-toggle-icon {
    width: 20px;
    height: 20px;
  }
}

.input-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--color-primary, #3B82F6), var(--color-primary-light, #60A5FA));
  transition: width 0.3s ease;
  opacity: 0;
}

.input-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #475569;
  margin-bottom: 8px;
  padding-left: 4px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
  background: var(--color-background, #F8FAFC);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  
  &:hover {
    border-color: var(--color-border-hover, #CBD5E1);
    background: var(--color-card-bg, #FFFFFF);
  }
  
  &:focus-within {
    border-color: var(--color-primary, #3B82F6);
    background: var(--color-card-bg, #FFFFFF);
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
    
    .input-icon svg {
      color: var(--color-primary, #3B82F6);
    }
  }
  
  .input-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    flex-shrink: 0;
    
    svg {
      width: 22px;
      height: 22px;
      color: var(--color-text-muted, #94A3B8);
      transition: color 0.3s ease;
    }
  }
  
  .custom-input {
    flex: 1;
    border: none !important;
    outline: none !important;
    background: transparent !important;
    box-shadow: none !important;
    font-size: 0.95rem;
    color: #1E293B;
    height: 44px;
    padding: 0 12px;
    
    :deep(.el-input__inner) {
      border: none !important;
      outline: none !important;
      background: transparent !important;
      box-shadow: none !important;
      color: var(--color-text-primary, #1E293B);
      font-size: 0.95rem;
      height: 44px;
      padding: 0;
      
      &::placeholder {
        color: var(--color-text-muted, #94A3B8);
      }
    }
    
    :deep(.el-input__prefix),
    :deep(.el-input__suffix) {
      display: none;
    }
  }
  
  .code-image {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 44px;
    background: var(--color-background, #F1F5F9);
    border: none;
    cursor: pointer;
    padding: 0 12px;
    margin: 2px;
    border-radius: 8px;
    transition: all 0.2s ease;
    
    img {
      height: 38px;
      border-radius: 6px;
    }
    
    .code-refresh-hint {
      font-size: 0.75rem;
      color: var(--color-text-secondary, #64748B);
      margin-left: 8px;
    }
    
    &:hover {
      background: var(--color-border, #E2E8F0);
      
      .code-refresh-hint {
        color: var(--color-primary, #3B82F6);
      }
    }
    
    &:focus {
      outline: none;
      box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
    }
  }
}

.register-button-item {
  :deep(.el-form-item__content) {
    display: block;
    width: 100%;
    margin-left: 0 !important;
  }
}

.register-button {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, var(--color-primary, #3B82F6) 0%, var(--color-primary-dark, #1D4ED8) 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  font-size: 1rem !important;
  font-weight: 600 !important;
  color: #FFFFFF !important;
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  box-shadow: 0 4px 14px rgba(59, 130, 246, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  &:hover:not(:disabled) {
    background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-primary-dark, #1E40AF) 100%) !important;
    box-shadow: 0 6px 20px rgba(59, 130, 246, 0.5);
    transform: translateY(-2px);
  }
  
  &:active:not(:disabled) {
    transform: translateY(0);
  }
  
  &:focus-visible {
    outline: 2px solid rgba(255, 255, 255, 0.6);
    outline-offset: 2px;
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.3), 0 4px 14px rgba(59, 130, 246, 0.4);
  }
  
  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .button-icon {
    width: 20px;
    height: 20px;
  }
}

.loading-spinner {
  width: 20px;
  height: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.register-footer {
  margin-top: 24px;
  text-align: center;
}

.login-link {
  font-size: 0.9rem;
  color: var(--color-text-secondary, #64748B);
  
  a {
    color: var(--color-primary, #3B82F6);
    text-decoration: none;
    font-weight: 600;
    cursor: pointer;
    transition: color 0.2s ease;
    
    &:hover {
      color: var(--color-primary-dark, #1D4ED8);
      text-decoration: underline;
    }
    
    &:focus-visible {
      outline: 2px solid rgba(59, 130, 246, 0.5);
      outline-offset: 2px;
      border-radius: 4px;
    }
  }
}

.system-info {
  margin-top: 24px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  
  span {
    font-size: 0.8rem;
    color: rgba(255, 255, 255, 0.5);
    letter-spacing: 0.5px;
  }
}

@media (max-width: 1024px) {
  .register-left {
    display: none;
  }
  
  .register-right {
    width: 100%;
    border-left: none;
    background: transparent;
    backdrop-filter: none;
  }
  
  .register-card {
    max-width: 420px;
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 
      0 25px 50px -12px rgba(0, 0, 0, 0.3),
      0 0 0 1px rgba(255, 255, 255, 0.1);
  }
}

@media (max-width: 640px) {
  .register-card {
    padding: 32px 24px;
    margin: 16px;
  }
  
  .brand-title {
    font-size: 1.75rem;
  }
  
  .feature-list {
    display: none;
  }
  
  .register-header h2 {
    font-size: 1.5rem;
  }
}

:global(.el-message) {
  font-size: 0.9rem;
}

:global(.el-message--success) {
  --el-message-bg-color: #F0FDF4;
  --el-message-border-color: #BBF7D0;
  --el-message-text-color: #166534;
}

:global(.el-message--error) {
  --el-message-bg-color: #FEF2F2;
  --el-message-border-color: #FECACA;
  --el-message-text-color: #991B1B;
}

:global(.el-message--warning) {
  --el-message-bg-color: #FFFBEB;
  --el-message-border-color: #FEF3C7;
  --el-message-text-color: #92400E;
}
</style>
