<template>
  <div class="login-container">
    <div class="login-background">
      <div class="gradient-overlay"></div>
      <div class="floating-badminton">
        <svg viewBox="0 0 100 100" class="badminton-icon" v-for="(item, index) in shuttlecocks" :key="index" :style="{ left: item.x + '%', top: item.y + '%', animationDelay: item.delay + 's', transform: 'rotate(' + item.rotate + 'deg)' }">
          <!-- 羽毛球头部 -->
          <ellipse cx="50" cy="20" rx="18" ry="8" fill="rgba(255,255,255,0.4)" opacity="0.9"/>
          <ellipse cx="50" cy="20" rx="15" ry="6" fill="rgba(59,130,246,0.3)"/>
          <!-- 羽毛部分 - 增强视觉效果 -->
          <path d="M32 20 Q50 60 50 90" stroke="rgba(255,255,255,0.7)" stroke-width="2.5" fill="none" stroke-linecap="round"/>
          <path d="M35 20 Q50 55 50 85" stroke="rgba(59,130,246,0.5)" stroke-width="2" fill="none" stroke-linecap="round"/>
          <path d="M40 20 Q50 50 50 75" stroke="rgba(255,255,255,0.5)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
          <path d="M45 20 Q50 45 50 65" stroke="rgba(249,115,22,0.4)" stroke-width="1" fill="none" stroke-linecap="round"/>
          <path d="M68 20 Q50 60 50 90" stroke="rgba(255,255,255,0.7)" stroke-width="2.5" fill="none" stroke-linecap="round"/>
          <path d="M65 20 Q50 55 50 85" stroke="rgba(59,130,246,0.5)" stroke-width="2" fill="none" stroke-linecap="round"/>
          <path d="M60 20 Q50 50 50 75" stroke="rgba(255,255,255,0.5)" stroke-width="1.5" fill="none" stroke-linecap="round"/>
          <path d="M55 20 Q50 45 50 65" stroke="rgba(249,115,22,0.4)" stroke-width="1" fill="none" stroke-linecap="round"/>
          <!-- 球头 -->
          <circle cx="50" cy="95" r="4" fill="rgba(255,255,255,0.9)"/>
          <circle cx="50" cy="95" r="2.5" fill="rgba(59,130,246,0.6)"/>
        </svg>
      </div>
      <div class="particles">
        <div class="particle" v-for="(particle, index) in particles" :key="'p'+index" :style="{ left: particle.x + '%', top: particle.y + '%', animationDelay: particle.delay + 's' }"></div>
      </div>
    </div>

    <div class="login-content">
      <!-- 左侧：动画角色区域（桌面端显示） -->
      <div class="login-left-chars">
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
          :show-password="charsSceneShowPassword"
          :password-length="charsScenePasswordLength"
          :has-login-error="charsSceneHasError"
        />
        <div class="chars-footer-spacer" aria-hidden="true"></div>
        <div class="chars-footer">
          <span class="chars-footer-copyright">{{ systemInfo }}</span>
        </div>
      </div>

      <!-- 右侧：登录表单 -->
      <div class="login-right-form">
        <div class="login-form-wrapper">
          <div class="login-mobile-brand">
            <span class="login-mobile-brand-text">羽擎</span>
            <span class="login-mobile-brand-slogan">让运动更智能</span>
          </div>
          <div class="login-header">
            <div class="welcome-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <h2>{{ currentForm === 'login' ? '欢迎回来' : '用户注册' }}</h2>
            <p>{{ currentForm === 'login' ? '请登录您的账号' : '请填写注册信息' }}</p>
            </div>

            <!-- 表单切换标签 -->
            <div class="form-tabs">
            <div 
              class="form-tab" 
              :class="{ 'active': currentForm === 'login' }"
              @click="switchForm('login')"
            >
              登录
            </div>
            <div 
              class="form-tab" 
              :class="{ 'active': currentForm === 'register' }"
              @click="switchForm('register')"
            >
              注册
            </div>
            </div>

            <!-- 登录表单 -->
            <el-form v-if="currentForm === 'login'" ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form" size="medium" autocomplete="off">
            <el-form-item prop="username" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.username, 'uiverse-has-value': loginForm.username }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                  <input
                    id="login-account"
                    v-model="loginForm.username"
                    type="text"
                    autocomplete="off"
                    placeholder=" "
                    class="uiverse-input"
                    aria-label="账号"
                    aria-required="true"
                    :readonly="usernameReadonly"
                    @focus="onLoginAccountFocus(); isTyping = true"
                    @blur="onLoginAccountBlur"
                  />
                  <label class="floating-label">账号</label>
                  <div class="input-progress"></div>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="password" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.password, 'uiverse-has-value': loginForm.password }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6z M18,20H6V10h12V20z M12,17c1.1,0,2-0.9,2-2s-0.9-2-2-2s-2,0.9-2,2S10.9,17,12,17z"/>
                    </svg>
                  </div>
                  <input
                    id="login-pwd"
                    v-model="loginForm.password"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="off"
                    placeholder=" "
                    class="uiverse-input"
                    aria-label="密码"
                    aria-required="true"
                    :readonly="passwordReadonly"
                    @keyup.enter="handleLogin"
                    @focus="onLoginPasswordFocus(); isTyping = true"
                    @blur="onLoginPasswordBlur"
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
                  v-model="loginForm.code"
                  auto-complete="off"
                  placeholder="请输入验证码"
                  class="custom-input"
                  aria-label="验证码"
                  aria-required="true"
                  @keyup.enter.native="handleLogin"
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

            <div class="login-options">
              <el-checkbox 
                v-model="loginForm.rememberMe" 
                class="remember-me"
                id="remember-me-checkbox"
              >
                <span class="checkbox-label">记住密码</span>
              </el-checkbox>
              <button 
                type="button"
                class="forgot-password" 
                @click="handleForgotPassword"
                aria-label="忘记密码"
              >
                忘记密码？
              </button>
            </div>

            <el-form-item class="login-button-item">
              <el-button
                :loading="loading"
                type="primary"
                class="login-button"
                @click.native.prevent="handleLogin"
                :disabled="loading"
                aria-label="登录按钮"
              >
                <span v-if="!loading" class="button-content">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="button-icon" aria-hidden="true">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                    <polyline points="10 17 15 12 10 7"/>
                    <line x1="15" y1="12" x2="3" y2="12"/>
                  </svg>
                  <span>立即登录</span>
                </span>
                <span v-else class="button-content">
                  <svg class="loading-spinner" viewBox="0 0 24 24" aria-hidden="true">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none" stroke-dasharray="31.4 31.4" stroke-linecap="round"/>
                  </svg>
                  <span>登录中...</span>
                </span>
              </el-button>
            </el-form-item>

            <div v-if="currentForm === 'login'" class="login-agreement">
              <span class="login-agreement-text">登录即表示同意</span>
              <button type="button" class="login-agreement-link" @click="handleUserAgreement">《用户协议》</button>
              <span class="login-agreement-text">和</span>
              <button type="button" class="login-agreement-link" @click="handlePrivacyPolicy">《隐私政策》</button>
            </div>
            </el-form>

            <!-- 注册表单 -->
            <el-form v-else-if="currentForm === 'register'" ref="registerForm" :model="registerForm" :rules="registerRules" class="login-form" size="medium">
            <el-form-item prop="username" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.registerUsername, 'uiverse-has-value': registerForm.username }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                  <input
                    v-model="registerForm.username"
                    type="text"
                    autocomplete="username"
                    placeholder=" "
                    class="uiverse-input"
                    @focus="inputFocused.registerUsername = true"
                    @blur="inputFocused.registerUsername = false"
                  />
                  <label class="floating-label">用户名</label>
                  <div class="input-progress"></div>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="idCard" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.idCard, 'uiverse-has-value': registerForm.idCard }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-7-2h5v-2h-5v2zm0-4h5v-2h-5v2zm-3-4h8V7H9v2z"/>
                    </svg>
                  </div>
                  <input
                    v-model="registerForm.idCard"
                    type="text"
                    autocomplete="off"
                    placeholder=" "
                    class="uiverse-input"
                    @focus="inputFocused.idCard = true"
                    @blur="inputFocused.idCard = false"
                  />
                  <label class="floating-label">身份证号</label>
                  <div class="input-progress"></div>
                </div>
              </div>
            </el-form-item>

            <el-form-item prop="password" class="form-item-custom">
              <div class="uiverse-input-container">
                <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': inputFocused.registerPassword, 'uiverse-has-value': registerForm.password }">
                  <div class="uiverse-input-icon">
                    <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6z M18,20H6V10h12V20z M12,17c1.1,0,2-0.9,2-2s-0.9-2-2-2s-2,0.9-2,2S10.9,17,12,17z"/>
                    </svg>
                  </div>
                  <input
                    v-model="registerForm.password"
                    :type="showRegisterPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    placeholder=" "
                    class="uiverse-input"
                    @focus="inputFocused.registerPassword = true"
                    @blur="inputFocused.registerPassword = false"
                  />
                  <label class="floating-label">密码</label>
                  <div class="input-progress"></div>
                  <button
                    type="button"
                    class="uiverse-password-toggle"
                    @click="showRegisterPassword = !showRegisterPassword"
                  >
                    <svg v-if="!showRegisterPassword" class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
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
                  <input
                    v-model="registerForm.confirmPassword"
                    :type="showRegisterPassword ? 'text' : 'password'"
                    autocomplete="new-password"
                    placeholder=" "
                    class="uiverse-input"
                    @focus="inputFocused.confirmPassword = true"
                    @blur="inputFocused.confirmPassword = false"
                  />
                  <label class="floating-label">确认密码</label>
                  <div class="input-progress"></div>
                  <button
                    type="button"
                    class="uiverse-password-toggle"
                    @click="showRegisterPassword = !showRegisterPassword"
                  >
                    <svg v-if="!showRegisterPassword" class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                    </svg>
                    <svg v-else class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zm2.76 9.54l2.17 2.17c-.43.51-.9.96-1.41 1.33-1.73 1.26-4.01 2.16-6.52 2.16-2.76 0-5-2.24-5-5 0-2.52.9-4.79 2.16-6.52.37-.51.82-.98 1.33-1.41l2.17 2.17C14.63 11.36 13.34 12 12 12c-1.66 0-3-1.34-3-3 0-.74.26-1.42.7-1.96L3 3l1.96-.7C5.58 1.26 8.3 1 12 1c3.89 0 7.29 1.26 10.04 3.34L20.33 6.43C17.58 4.26 14.96 3 12 3 6.48 3 2.39 6.92 1.59 12c.8 5.11 4.89 9 10.41 9 2.96 0 5.58-1.26 7.33-3.43l-3.17-3.17C14.42 16.74 13.14 17 12 17z"/>
                    </svg>
                  </button>
                </div>
              </div>
            </el-form-item>

            <!-- 注册时默认角色为普通用户，不允许选择其他角色 -->
            <!-- 协会会长和场馆管理者只能由管理员在用户管理页面创建 -->
            <el-form-item prop="role" class="form-item-custom" style="display: none;">
              <!-- 隐藏角色选择，默认设置为USER -->
            </el-form-item>

            <el-form-item class="login-button-item">
              <el-button
                :loading="loading"
                type="primary"
                class="login-button"
                @click.native.prevent="handleRegister"
                :disabled="loading"
                aria-label="注册按钮"
              >
                <span v-if="!loading" class="button-content">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="button-icon" aria-hidden="true">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="8.5" cy="7" r="4"/>
                    <line x1="20" y1="8" x2="20" y2="14"/>
                    <line x1="23" y1="11" x2="17" y2="11"/>
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
            </el-form>

            <div class="login-footer">
              <span v-if="currentForm === 'login' && register" class="register-link">
                还没有账号？<a href="javascript:void(0)" @click="switchForm('register')">立即注册</a>
              </span>
              <span v-else-if="currentForm === 'register'" class="register-link">
                已有账号？<a href="javascript:void(0)" @click="switchForm('login')">立即登录</a>
              </span>
            </div>
        </div>
      </div>
    </div>

    <!-- 忘记密码弹窗（与登录/注册同款 uiverse 风格） -->
    <el-dialog
      v-model="forgotPasswordVisible"
      width="420px"
      :close-on-click-modal="false"
      class="forgot-password-dialog"
      @closed="resetForgotForm"
    >
      <template #header>
        <div class="forgot-dialog-header">
          <div class="welcome-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6z M18,20H6V10h12V20z M12,17c1.1,0,2-0.9,2-2s-0.9-2-2-2s-2,0.9-2,2S10.9,17,12,17z"/>
            </svg>
          </div>
          <h2>找回密码</h2>
          <p>请验证身份后设置新密码</p>
        </div>
      </template>
      <div class="forgot-password-inner">
        <el-form ref="forgotFormRef" :model="forgotPasswordForm" :rules="forgotPasswordRules" label-width="0" size="medium" class="login-form">
          <el-form-item prop="username" class="form-item-custom">
            <div class="uiverse-input-container">
              <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': forgotFocused.username, 'uiverse-has-value': forgotPasswordForm.username }">
                <div class="uiverse-input-icon">
                  <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                </div>
                <input
                  v-model="forgotPasswordForm.username"
                  type="text"
                  autocomplete="off"
                  placeholder=" "
                  class="uiverse-input"
                  maxlength="50"
                  @focus="forgotFocused.username = true"
                  @blur="forgotFocused.username = false"
                />
                <label class="floating-label">用户名</label>
                <div class="input-progress"></div>
              </div>
            </div>
          </el-form-item>
          <el-form-item prop="idCard" class="form-item-custom">
            <div class="uiverse-input-container">
              <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': forgotFocused.idCard, 'uiverse-has-value': forgotPasswordForm.idCard }">
                <div class="uiverse-input-icon">
                  <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-7-2h5v-2h-5v2zm0-4h5v-2h-5v2zm-3-4h8V7H9v2z"/>
                  </svg>
                </div>
                <input
                  v-model="forgotPasswordForm.idCard"
                  type="text"
                  autocomplete="off"
                  placeholder=" "
                  class="uiverse-input"
                  maxlength="18"
                  @focus="forgotFocused.idCard = true"
                  @blur="forgotFocused.idCard = false"
                />
                <label class="floating-label">身份证号</label>
                <div class="input-progress"></div>
              </div>
            </div>
          </el-form-item>
          <el-form-item prop="newPassword" class="form-item-custom">
            <div class="uiverse-input-container">
              <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': forgotFocused.newPassword, 'uiverse-has-value': forgotPasswordForm.newPassword }">
                <div class="uiverse-input-icon">
                  <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6z M18,20H6V10h12V20z M12,17c1.1,0,2-0.9,2-2s-0.9-2-2-2s-2,0.9-2,2S10.9,17,12,17z"/>
                  </svg>
                </div>
                <input
                  v-model="forgotPasswordForm.newPassword"
                  :type="showForgotPassword ? 'text' : 'password'"
                  autocomplete="new-password"
                  placeholder=" "
                  class="uiverse-input"
                  @focus="forgotFocused.newPassword = true"
                  @blur="forgotFocused.newPassword = false"
                />
                <label class="floating-label">新密码（至少6位）</label>
                <div class="input-progress"></div>
                <button type="button" class="uiverse-password-toggle" @click="showForgotPassword = !showForgotPassword" aria-label="显示/隐藏密码">
                  <svg v-if="!showForgotPassword" class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                  </svg>
                  <svg v-else class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7z"/>
                  </svg>
                </button>
              </div>
            </div>
          </el-form-item>
          <el-form-item prop="confirmPassword" class="form-item-custom">
            <div class="uiverse-input-container">
              <div class="uiverse-input-wrapper" :class="{ 'uiverse-focused': forgotFocused.confirmPassword, 'uiverse-has-value': forgotPasswordForm.confirmPassword }">
                <div class="uiverse-input-icon">
                  <svg class="uiverse-uiverse-input-icon-svg" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z M9,6c0-1.66,1.34-3,3-3s3,1.34,3,3v2H9V6z M18,20H6V10h12V20z M12,17c1.1,0,2-0.9,2-2s-0.9-2-2-2s-2,0.9-2,2S10.9,17,12,17z"/>
                  </svg>
                </div>
                <input
                  v-model="forgotPasswordForm.confirmPassword"
                  :type="showForgotPassword ? 'text' : 'password'"
                  autocomplete="new-password"
                  placeholder=" "
                  class="uiverse-input"
                  @focus="forgotFocused.confirmPassword = true"
                  @blur="forgotFocused.confirmPassword = false"
                />
                <label class="floating-label">确认新密码</label>
                <div class="input-progress"></div>
                <button type="button" class="uiverse-password-toggle" @click="showForgotPassword = !showForgotPassword" aria-label="显示/隐藏密码">
                  <svg v-if="!showForgotPassword" class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                  </svg>
                  <svg v-else class="uiverse-toggle-icon" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7z"/>
                  </svg>
                </button>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="forgot-dialog-footer">
          <button type="button" class="forgot-btn-cancel" @click="forgotPasswordVisible = false">取消</button>
          <button
            type="button"
            class="login-button forgot-btn-submit"
            :disabled="loadingForgot"
            @click="handleSubmitForgotPassword"
          >
            <span v-if="!loadingForgot" class="button-content">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="button-icon" aria-hidden="true">
                <path d="M18,8h-1V6c0-2.76-2.24-5-5-5S7,3.24,7,6v2H6c-1.1,0-2,0.9-2,2v10c0,1.1,0.9,2,2,2h12c1.1,0,2-0.9,2-2V10C20,8.9,19.1,8,18,8z"/>
              </svg>
              <span>确认重置</span>
            </span>
            <span v-else class="button-content">
              <svg class="loading-spinner" viewBox="0 0 24 24" aria-hidden="true">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" fill="none" stroke-dasharray="31.4 31.4" stroke-linecap="round"/>
              </svg>
              <span>重置中...</span>
            </span>
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getCodeImg, login, getInfo, register, forgotPassword } from '@/api/login'
import Cookies from 'js-cookie'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'
import { setToken, setUserInfo, saveLoginData } from '@/utils/auth'
import AnimatedCharactersScene from '@/components/AnimatedCharactersScene.vue'

export default {
  name: 'Login',
  components: {
    AnimatedCharactersScene
  },
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      systemInfo: defaultSettings.footerContent,
      codeUrl: '',
      loginForm: {
        username: '',
        password: '',
        rememberMe: false,
        code: '',
        uuid: ''
      },
      usernameReadonly: true,
      passwordReadonly: true,
      loginRules: {
        username: [
          { required: true, trigger: 'blur', message: '请输入您的账号' }
        ],
        password: [
          { required: true, trigger: 'blur', message: '请输入您的密码' }
        ],
        code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
      },
      loading: false,
      captchaEnabled: true,
      register: true,
      currentForm: 'login', // 'login' 或 'register'
      redirect: undefined,
      shuttlecocks: [],
      particles: [],
      showPassword: false,
      showRegisterPassword: false,
      inputFocused: {
        username: false,
        password: false,
        registerUsername: false,
        idCard: false,
        registerPassword: false,
        confirmPassword: false
      },
      hasLoginError: false,
      isTyping: false,
      forgotPasswordVisible: false,
      forgotPasswordForm: {
        username: '',
        idCard: '',
        newPassword: '',
        confirmPassword: ''
      },
      forgotPasswordRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        idCard: [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入有效的身份证号', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value !== this.forgotPasswordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
        ]
      },
      loadingForgot: false,
      forgotFocused: {
        username: false,
        idCard: false,
        newPassword: false,
        confirmPassword: false
      },
      showForgotPassword: false,
      registerForm: {
        username: '',
        idCard: '',
        password: '',
        confirmPassword: '',
        role: 'USER' // 注册时固定为普通用户，协会会长和场馆管理者只能由管理员创建
      },
      registerRules: {
        username: [
          { required: true, trigger: 'blur', message: '请输入用户名' }
        ],
        idCard: [
          { required: true, trigger: 'blur', message: '请输入身份证号' },
          { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, trigger: 'blur', message: '请输入有效的身份证号' }
        ],
        password: [
          { required: true, trigger: 'blur', message: '请输入密码' },
          { min: 6, max: 20, trigger: 'blur', message: '密码长度在 6 到 20 个字符' }
        ],
        confirmPassword: [
          { required: true, trigger: 'blur', message: '请确认密码' },
          { validator: (rule, value, callback) => {
            if (value !== this.registerForm.password) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          }, trigger: 'blur' }
        ],
        role: [
          { required: true, trigger: 'change', message: '请选择角色' }
        ]
      }
    }
  },
  computed: {
    charsSceneIsTyping() {
      if (this.currentForm === 'login') {
        return this.inputFocused.username || this.inputFocused.password
      }
      return this.inputFocused.registerUsername || this.inputFocused.idCard ||
        this.inputFocused.registerPassword || this.inputFocused.confirmPassword
    },
    charsSceneShowPassword() {
      return this.currentForm === 'login' ? this.showPassword : this.showRegisterPassword
    },
    charsScenePasswordLength() {
      if (this.currentForm === 'login') {
        return this.loginForm.password ? this.loginForm.password.length : 0
      }
      return this.registerForm.password ? this.registerForm.password.length : 0
    },
    charsSceneHasError() {
      return this.currentForm === 'login' && this.hasLoginError
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
    this.getCookie()
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
          this.loginForm.uuid = res.uuid
        }
      })
    },
    getCookie() {
      const username = Cookies.get('username')
      const password = Cookies.get('password')
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    onLoginAccountFocus() {
      this.usernameReadonly = false
      this.inputFocused.username = true
    },
    onLoginPasswordFocus() {
      this.passwordReadonly = false
      this.inputFocused.password = true
    },
    onLoginAccountBlur() {
      this.inputFocused.username = false
      this.$nextTick(() => {
        this.isTyping = this.inputFocused.username || this.inputFocused.password
      })
    },
    onLoginPasswordBlur() {
      this.inputFocused.password = false
      this.$nextTick(() => {
        this.isTyping = this.inputFocused.username || this.inputFocused.password
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          this.hasLoginError = false
          if (this.loginForm.rememberMe) {
            Cookies.set('username', this.loginForm.username, { expires: 30 })
            Cookies.set('password', encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove('username')
            Cookies.remove('password')
            Cookies.remove('rememberMe')
          }
          
          // 直接调用登录 API
          login(this.loginForm)
            .then(async response => {
              if (response.code === 200) {
                this.hasLoginError = false
                // 保存登录信息（包含AccessToken、RefreshToken、用户信息）
                saveLoginData(response.data)
                // 显示成功消息
                this.$message.success('登录成功，欢迎使用羽毛球管理系统！')
                // 按角色跳转：USER/MEMBER 去用户端，COACH 去教练端，PRESIDENT/ADMIN/VENUE_MANAGER 去管理端
                const role = (response.data.user && response.data.user.role) || (response.data.role)
                const isUserSide = role === 'USER' || role === 'MEMBER'
                const isCoach = role === 'COACH'
                let targetPath = this.redirect
                if (!targetPath) {
                  if (isUserSide) targetPath = '/user/dashboard'
                  else if (isCoach) targetPath = '/coach/dashboard'
                  else targetPath = '/dashboard'
                }
                // 用户端登录时清除 VIP 欢迎弹窗“已显示”标记，以便本次登录后能弹出欢迎回家
                if (isUserSide) {
                  try { sessionStorage.removeItem('vipWelcomeShown') } catch (e) {}
                }

                // 先完成页面跳转，避免被额外接口阻塞；跳转后再异步刷新用户信息
                this.$router.push({ path: targetPath }).catch(()=>{})

                getInfo()
                  .then(infoRes => {
                    if (infoRes && infoRes.code === 200 && infoRes.data) {
                      setUserInfo(infoRes.data)
                      window.dispatchEvent(new Event('userInfoUpdated'))
                    }
                  })
                  .catch(e => {
                    console.warn('登录后拉取用户信息失败，使用登录返回数据:', e)
                  })
              } else {
                this.hasLoginError = true
                this.$message.error(response.message || '登录失败')
                this.loading = false
                if (this.captchaEnabled) {
                  this.getCode()
                }
              }
            })
            .catch(error => {
              console.error('登录失败:', error)
              this.$message.error(error.message || '登录失败，请检查用户名和密码')
              this.hasLoginError = true
              this.loading = false
              if (this.captchaEnabled) {
                this.getCode()
              }
            })
        } else {
          // 表单验证失败时的反馈
          this.$message.warning('请填写完整的登录信息')
        }
      })
    },
    handleForgotPassword() {
      this.forgotPasswordForm.username = this.loginForm.username || ''
      this.forgotPasswordVisible = true
    },
    handleSubmitForgotPassword() {
      this.$refs.forgotFormRef.validate(valid => {
        if (valid) {
          this.loadingForgot = true
          forgotPassword(this.forgotPasswordForm)
            .then(response => {
              if (response.code === 200) {
                this.$message.success('密码重置成功，请使用新密码登录')
                this.forgotPasswordVisible = false
              } else {
                this.$message.error(response.message || '重置失败')
              }
            })
            .catch(error => {
              const msg = error.response?.data?.message || error.message || '重置失败，请稍后重试'
              this.$message.error(msg)
            })
            .finally(() => {
              this.loadingForgot = false
            })
        }
      })
    },
    resetForgotForm() {
      this.forgotPasswordForm = {
        username: '',
        idCard: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.forgotFocused = { username: false, idCard: false, newPassword: false, confirmPassword: false }
      this.showForgotPassword = false
      this.$refs.forgotFormRef?.resetFields()
    },
    switchForm(formName) {
      this.currentForm = formName
      if (formName === 'register') {
        // 切换到注册表单时，清空注册表单
        this.registerForm = {
          username: '',
          idCard: '',
          password: '',
          confirmPassword: '',
          role: 'USER'
        }
      } else if (formName === 'login') {
        // 切回登录时重置 readonly，避免浏览器对重新挂载的输入框自动填充
        this.usernameReadonly = true
        this.passwordReadonly = true
      }
    },
    handleRegister() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          this.loading = true
          
          register(this.registerForm)
            .then(response => {
              if (response.code === 200) {
                this.$message.success('注册成功，请登录')
                // 切换到登录表单
                this.currentForm = 'login'
                // 清空注册表单
                this.registerForm = {
                  username: '',
                  idCard: '',
                  password: '',
                  confirmPassword: '',
                  role: 'USER'
                }
              } else {
                this.$message.error(response.message || '注册失败')
              }
            })
            .catch(error => {
              console.error('注册失败:', error)
              const errorMessage = error.response?.data?.message || error.message || '注册失败，请稍后重试'
              this.$message.error(errorMessage)
            })
            .finally(() => {
              this.loading = false
            })
        } else {
          this.$message.warning('请填写完整的注册信息')
        }
      })
    },
    handleUserAgreement() {
      this.$message.info('用户协议')
      // 可后续替换为实际协议页路由： this.$router.push('/agreement')
    },
    handlePrivacyPolicy() {
      this.$message.info('隐私政策')
      // 可后续替换为实际隐私政策页路由： this.$router.push('/privacy')
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login-container {
  width: 100%;
  min-width: 0;
  height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  box-sizing: border-box;

  // 减少动画（尊重用户的偏好设置）
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

.login-background {
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
    background: radial-gradient(circle, rgba(59, 130, 246, 0.6) 0%, rgba(249, 115, 22, 0.4) 50%, transparent 100%);
    border-radius: 50%;
    box-shadow: 0 0 10px rgba(59, 130, 246, 0.5);
    animation: particleFloat 20s linear infinite;
    
    &::before {
      content: '';
      position: absolute;
      width: 100%;
      height: 100%;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      animation: particleGlow 2s ease-in-out infinite;
    }
  }
}

@keyframes particleFloat {
  0% {
    transform: translateY(100vh) translateX(0) scale(0);
    opacity: 0;
  }
  5% {
    opacity: 1;
    transform: translateY(90vh) translateX(10px) scale(0.5);
  }
  50% {
    opacity: 0.8;
    transform: translateY(30vh) translateX(-20px) scale(1);
  }
  95% {
    opacity: 0.6;
    transform: translateY(-10vh) translateX(15px) scale(1.2);
  }
  100% {
    transform: translateY(-20vh) translateX(0) scale(0);
    opacity: 0;
  }
}

@keyframes particleGlow {
  0%, 100% {
    transform: scale(1);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.5);
    opacity: 0.6;
  }
}

.login-content {
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

.login-left-chars {
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

.login-right-form {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);

  @media (max-width: 1023px) {
    background: linear-gradient(180deg, rgba(59, 130, 246, 0.06) 0%, rgba(255, 255, 255, 0.98) 12%, rgba(255, 255, 255, 0.98) 100%);
  }

  @media (min-width: 1024px) {
    background: #fff;
    box-shadow: -8px 0 32px rgba(0, 0, 0, 0.08);
  }
}

.login-form-wrapper {
  width: 100%;
  max-width: 420px;

  @media (max-width: 1023px) {
    background: #fff;
    border-radius: 20px;
    box-shadow: 0 8px 32px rgba(15, 23, 42, 0.12);
    padding: 28px 24px 24px;
    margin: 0 auto;
  }
}

.login-mobile-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  text-align: center;

  @media (min-width: 1024px) {
    display: none;
  }
}

.login-mobile-brand-text {
  font-size: 18px;
  font-weight: 600;
  color: #0f172a;
}

.login-mobile-brand-slogan {
  font-size: 13px;
  font-weight: 400;
  color: #64748b;
  margin-top: 4px;
  letter-spacing: 0.5px;
}

.brand-showcase {
  max-width: 720px;
  width: 100%;
  text-align: center;
  margin: 0 auto;
}

.logo-container {
  margin-bottom: 30px;
}

.logo-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
  animation: logoFloat 4s ease-in-out infinite;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 140px;
    height: 140px;
    background: radial-gradient(circle, rgba(59, 130, 246, 0.3) 0%, transparent 70%);
    border-radius: 50%;
    animation: logoGlow 3s ease-in-out infinite;
    z-index: -1;
  }

  .badminton-logo {
    width: 100%;
    height: 100%;
    filter: 
      drop-shadow(0 10px 30px rgba(59, 130, 246, 0.5))
      drop-shadow(0 4px 12px rgba(249, 115, 22, 0.3));
    transition: filter 0.25s ease;
  }

  &:hover .badminton-logo {
    filter: 
      drop-shadow(0 12px 36px rgba(59, 130, 246, 0.6))
      drop-shadow(0 6px 16px rgba(249, 115, 22, 0.35));
  }
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-12px) rotate(-2deg);
  }
  50% {
    transform: translateY(-8px) rotate(0deg);
  }
  75% {
    transform: translateY(-12px) rotate(2deg);
  }
}

@keyframes logoGlow {
  0%, 100% {
    opacity: 0.5;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.8;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

.brand-title {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #60A5FA 50%, #F97316 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
  text-shadow: 0 4px 20px rgba(59, 130, 246, 0.3);
  animation: titleGradient 5s ease-in-out infinite;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.2));
}

@keyframes titleGradient {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.brand-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 40px;
  letter-spacing: 4px;
  text-transform: uppercase;
  font-weight: 500;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  animation: subtitleFade 2s ease-in-out infinite;
}

@keyframes subtitleFade {
  0%, 100% {
    opacity: 0.7;
  }
  50% {
    opacity: 0.9;
  }
}

.feature-list {
  .feature-item {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    margin-bottom: 16px;
    font-size: 16px;
    color: rgba(255, 255, 255, 0.85);
    padding: 14px 24px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 30px;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: background-color 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
    cursor: default;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
      transition: left 0.5s ease;
    }

    &:hover {
      background: rgba(255, 255, 255, 0.15);
      border-color: rgba(59, 130, 246, 0.3);
      box-shadow: 
        0 4px 16px rgba(59, 130, 246, 0.2),
        inset 0 1px 0 rgba(255, 255, 255, 0.1);

      &::before {
        left: 100%;
      }
    }

    .feature-icon {
      width: 28px;
      height: 28px;
      min-width: 28px;
      min-height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #3B82F6 0%, #F97316 100%);
      border-radius: 50%;
      color: #ffffff;
      box-shadow: 
        0 4px 12px rgba(59, 130, 246, 0.4),
        inset 0 1px 0 rgba(255, 255, 255, 0.2);
      transition: box-shadow 0.25s ease, opacity 0.25s ease;
    }

    .feature-icon svg {
      width: 14px;
      height: 14px;
    }

    &:hover .feature-icon {
      box-shadow: 
        0 6px 16px rgba(249, 115, 22, 0.5),
        inset 0 1px 0 rgba(255, 255, 255, 0.3);
    }
  }
}

/* 右侧表单区域内的输入框使用浅色主题 */
.login-right-form .uiverse-input-wrapper {
  background: #f8fafc;
  border-color: #e2e8f0;
}

.login-right-form .uiverse-input-wrapper:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.login-right-form .uiverse-input-wrapper.uiverse-focused {
  background: #fff;
  border-color: var(--color-primary, #3B82F6);
}

.login-right-form .uiverse-input {
  color: #0f172a;
}

.login-right-form .floating-label {
  color: #64748b;
}

.login-right-form .uiverse-input-wrapper.uiverse-focused .floating-label,
.login-right-form .uiverse-input-wrapper.uiverse-has-value .floating-label {
  color: var(--color-primary, #3B82F6);
  background: #fff;
}

.login-right-form .login-header h2 {
  color: #0f172a;
}

.login-right-form .login-header p {
  color: #64748b;
}

.login-right-form .form-tab {
  color: #64748b;
}

.login-right-form .form-tab.active {
  color: #fff;
}

.login-right-form .uiverse-input:-webkit-autofill,
.login-right-form .uiverse-input:-webkit-autofill:hover,
.login-right-form .uiverse-input:-webkit-autofill:focus,
.login-right-form .uiverse-input:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 30px #f8fafc inset !important;
  box-shadow: 0 0 0 30px #f8fafc inset !important;
  -webkit-text-fill-color: #0f172a !important;
}

/* 验证码与 uiverse 输入框统一：浅色主题、56px 高、14px 圆角 */
.login-right-form .code-input-wrapper .input-wrapper .custom-input :deep(.el-input__inner) {
  height: 56px !important;
  min-height: 56px;
  border-radius: 14px !important;
  background: #f8fafc !important;
  background-color: #f8fafc !important;
  border: 2px solid #e2e8f0 !important;
  color: #0f172a !important;
  padding-left: 48px;
  padding-right: 44px;
}

.login-right-form .code-input-wrapper .input-wrapper .custom-input :deep(.el-input__inner):focus {
  border-color: var(--color-primary, #3B82F6) !important;
  background: #fff !important;
  background-color: #fff !important;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15);
}

.login-right-form .code-input-wrapper .input-wrapper .input-icon {
  color: #64748b;
}

.login-right-form .code-input-wrapper .input-wrapper:focus-within .input-icon {
  color: var(--color-primary, #3B82F6);
}

.login-right-form .code-input-wrapper .code-image {
  height: 56px;
  border-radius: 14px;
  border-color: #e2e8f0;
  background: #f8fafc;
}

@keyframes cardEntrance {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .welcome-icon {
    width: 72px;
    height: 72px;
    margin: 0 auto 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, var(--color-primary, #3B82F6) 0%, var(--color-primary-light, #60A5FA) 50%, var(--color-accent, #F97316) 100%);
    background-size: 200% 200%;
    border-radius: 20px;
    color: #ffffff;
    box-shadow: 
      0 8px 24px rgba(59, 130, 246, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    animation: iconPulse 3s ease-in-out infinite;
    transition: box-shadow 0.25s ease;

    &:hover {
      box-shadow: 
        0 12px 32px rgba(59, 130, 246, 0.5),
        inset 0 1px 0 rgba(255, 255, 255, 0.3);
    }

    &:focus-visible {
      outline: 2px solid rgba(255, 255, 255, 0.5);
      outline-offset: 2px;
    }

    svg {
      width: 36px;
      height: 36px;
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
    }
  }

@keyframes iconPulse {
  0%, 100% {
    background-position: 0% 50%;
    box-shadow: 
      0 8px 24px rgba(59, 130, 246, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }
  50% {
    background-position: 100% 50%;
    box-shadow: 
      0 10px 28px rgba(249, 115, 22, 0.4),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }
}

  h2 {
    font-size: 32px;
    font-weight: 700;
    color: var(--color-text-primary, #ffffff);
    margin-bottom: 8px;
    text-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
    letter-spacing: -0.5px;
  }

  p {
    font-size: 15px;
    color: var(--color-text-secondary, rgba(255, 255, 255, 0.7));
    font-weight: 400;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 28px;
  }
}

.form-item-custom {
  margin-bottom: 28px;
}

/* Uiverse 输入框样式 */
.uiverse-input-container {
  position: relative;
  margin-bottom: 8px;
  width: 100%;
}

.uiverse-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #1a2744;
  border: 2px solid var(--color-border, rgba(255, 255, 255, 0.25));
  border-radius: 16px;
  padding: 16px 20px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  width: 100%;
  min-width: 100%;
  box-sizing: border-box;
  min-height: 56px;
  margin: 0 auto;
}

.uiverse-input-wrapper:hover {
  border-color: var(--color-primary-light, rgba(96, 165, 250, 0.5));
  background: #1e2d4a;
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.2);
}

.uiverse-input-wrapper.uiverse-focused {
  border-color: var(--color-primary-light, #60A5FA);
  background: #223352;
  box-shadow: 0 0 0 4px rgba(96, 165, 250, 0.2), 0 8px 25px rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}

.uiverse-input-wrapper.uiverse-has-error {
  border-color: #EF4444;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.2);
}

.uiverse-input-wrapper.has-success {
  border-color: #10B981;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.2);
}

.uiverse-input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  margin-right: 12px;
  color: var(--color-text-muted, rgba(255, 255, 255, 0.6));
  transition: color 0.3s ease;
}

.uiverse-input-wrapper.uiverse-focused .uiverse-input-icon {
  color: var(--color-primary-light, #60A5FA);
}

.uiverse-input-wrapper.has-success .uiverse-input-icon {
  color: #10B981;
}

.uiverse-input-wrapper.uiverse-has-error .uiverse-input-icon {
  color: #EF4444;
}

.uiverse-uiverse-input-icon-svg {
  width: 20px;
  height: 20px;
}

.uiverse-input-success-icon {
  width: 18px;
  height: 18px;
  color: #10B981;
}

.uiverse-input-loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid #60A5FA;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.uiverse-input {
  flex: 1;
  width: 100%;
  padding: 8px 0;
  border: none;
  outline: none;
  font-size: 16px;
  font-weight: 500;
  color: #ffffff;
  background: transparent;
  transition: all 0.3s ease;
  line-height: 1.5;

  &::placeholder {
    color: transparent;
  }

  &::selection {
    background: rgba(96, 165, 250, 0.4);
    color: #ffffff;
  }

  /* 覆盖浏览器 autofill：实色、不用毛玻璃（参考 CMS 的实色做法） */
  &:-webkit-autofill,
  &:-webkit-autofill:hover,
  &:-webkit-autofill:focus,
  &:-webkit-autofill:active {
    -webkit-box-shadow: 0 0 0 30px #1a2744 inset !important;
    box-shadow: 0 0 0 30px #1a2744 inset !important;
    -webkit-text-fill-color: #ffffff !important;
    caret-color: #ffffff;
    transition: background-color 5000s ease-in-out 0s;
  }
}

/* 密码输入框需要右侧 padding 为切换按钮留空间 */
.uiverse-input-wrapper:has(.uiverse-password-toggle) .uiverse-input {
  padding-right: 40px;
}

.floating-label {
  position: absolute;
  left: 56px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: var(--color-text-muted, rgba(255, 255, 255, 0.6));
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
  background: transparent;
  padding: 0 4px;
  z-index: 1;
}

.uiverse-input-wrapper.uiverse-focused .floating-label,
.uiverse-input-wrapper.uiverse-has-value .floating-label {
  top: -8px;
  left: 12px;
  font-size: 12px;
  color: var(--color-primary-light, #60A5FA);
  background: #0f172a;
  font-weight: 500;
  padding: 0 6px;
  border-radius: 4px;
}

.uiverse-input-wrapper.uiverse-has-error .floating-label {
  color: #EF4444;
}

.uiverse-input-wrapper.has-success .floating-label {
  color: #10B981;
}

.input-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, #60A5FA, #3B82F6);
  transition: width 0.3s ease;
  border-radius: 0 0 14px 14px;
}

.uiverse-input-wrapper.uiverse-focused .input-progress,
.uiverse-input-wrapper.uiverse-has-value .input-progress {
  width: 100%;
}

.uiverse-password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: var(--color-text-muted, rgba(255, 255, 255, 0.6));
  transition: color 0.2s ease, background-color 0.2s ease;
  border-radius: 4px;
  z-index: 2;
}

.uiverse-password-toggle:hover {
  color: var(--color-primary-light, #60A5FA);
  background: var(--color-card-bg-hover, rgba(96, 165, 250, 0.1));
}

.uiverse-password-toggle:focus-visible {
  outline: 2px solid rgba(96, 165, 250, 0.5);
  outline-offset: 2px;
}

.uiverse-toggle-icon {
  width: 20px;
  height: 20px;
}

.uiverse-input-feedback {
  margin-top: 6px;
  padding: 0 4px;
}

.uiverse-error-text {
  font-size: 12px;
  color: #EF4444;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

.uiverse-error-text::before {
  content: '!';
  width: 16px;
  height: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  line-height: 1;
  font-weight: 800;
  color: #7F1D1D;
  background: rgba(239, 68, 68, 0.18);
  border: 1px solid rgba(239, 68, 68, 0.35);
  border-radius: 999px;
}

.input-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 8px;
  letter-spacing: 0.2px;
  transition: color 0.2s ease;
  position: relative;
  padding-left: 0;
  text-shadow: none;
}

// 当输入框聚焦时，标签样式
.el-form-item:has(.input-wrapper.is-focused) .input-label,
.el-form-item:has(.input-wrapper:focus-within) .input-label {
  color: #60A5FA;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;

  .input-icon {
    position: absolute;
    left: 16px;
    width: 20px;
    height: 20px;
    color: rgba(255, 255, 255, 0.5);
    z-index: 2;
    transition: color 0.2s ease;
    pointer-events: none;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  &.is-focused .input-icon,
  &:focus-within .input-icon {
    color: #60A5FA;
  }

  // 密码显示/隐藏按钮
  .password-toggle {
    position: absolute;
    right: 12px;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    z-index: 2;
    transition: all 0.2s ease;
    outline: none;
    color: rgba(255, 255, 255, 0.6);

    .eye-icon {
      width: 18px;
      height: 18px;
      transition: color 0.2s ease;
    }

    &:hover {
      color: #60A5FA;
      background: rgba(96, 165, 250, 0.1);
    }

    &:active {
      background: rgba(96, 165, 250, 0.15);
    }

    &:focus-visible {
      outline: 2px solid rgba(96, 165, 250, 0.5);
      outline-offset: 2px;
    }
  }

  .custom-input {
    width: 100%;

    :deep(.el-input__inner) {
      height: 52px;
      padding-left: 48px;
      padding-right: 44px;
      border: 1.5px solid rgba(255, 255, 255, 0.25) !important;
      border-radius: 12px;
      font-size: 15px;
      font-weight: 400;
      color: #ffffff !important;
      background: rgba(255, 255, 255, 0.1) !important;
      background-color: rgba(255, 255, 255, 0.1) !important;
      transition: all 0.2s ease;
      outline: none;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

      &::placeholder {
        color: var(--color-text-muted, rgba(255, 255, 255, 0.5));
        font-weight: 400;
      }

    &:focus {
      border-color: var(--color-primary-light, #60A5FA) !important;
      background: var(--color-card-bg-hover, rgba(255, 255, 255, 0.15)) !important;
      background-color: var(--color-card-bg-hover, rgba(255, 255, 255, 0.15)) !important;
      box-shadow:
        0 0 0 3px rgba(96, 165, 250, 0.2),
        0 6px 16px rgba(59, 130, 246, 0.3);

      &::placeholder {
        opacity: 0.3;
      }
    }

      &:hover:not(:focus) {
        border-color: rgba(255, 255, 255, 0.35) !important;
        background: rgba(255, 255, 255, 0.12) !important;
        background-color: rgba(255, 255, 255, 0.12) !important;
      }

      &:focus-visible {
        outline: none;
      }

      &:not(:placeholder-shown) {
        border-color: rgba(255, 255, 255, 0.3) !important;
        background: rgba(255, 255, 255, 0.12) !important;
        background-color: rgba(255, 255, 255, 0.12) !important;
      }

      &::selection {
        background: rgba(96, 165, 250, 0.4);
        color: #ffffff;
      }
    }

    // 密码输入框特殊样式 - 为显示/隐藏按钮留出空间
    &:has(.password-toggle) :deep(.el-input__inner) {
      padding-right: 50px;
    }

    // 错误状态
    &.is-error :deep(.el-input__inner) {
      border-color: #EF4444 !important;
      background: rgba(239, 68, 68, 0.2) !important;
      background-color: rgba(239, 68, 68, 0.2) !important;
      box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2);
    }
  }
}

.code-input-wrapper {
  .custom-input {
    width: 55%;
  }

  .code-image {
    width: 35%;
    height: 56px;
    margin-left: 10%;
    border-radius: 14px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid var(--color-border, rgba(255, 255, 255, 0.2));
    background: var(--color-card-bg, rgba(255, 255, 255, 0.1));
    backdrop-filter: blur(10px);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    outline: none;
    display: flex;
    align-items: center;
    justify-content: center;

    .code-refresh-hint {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 10px;
      color: rgba(255, 255, 255, 0.8);
      opacity: 0;
      transition: opacity 0.3s ease;
      pointer-events: none;
      font-weight: 600;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
      z-index: 2;
    }

    &:hover {
      border-color: var(--color-primary, #3B82F6);
      transform: scale(1.05);
      box-shadow: 
        0 4px 12px rgba(59, 130, 246, 0.3),
        inset 0 1px 0 rgba(255, 255, 255, 0.1);

      .code-refresh-hint {
        opacity: 1;
      }

      img {
        opacity: 0.5;
        filter: blur(2px);
      }
    }

    &:focus-visible {
      outline: 3px solid rgba(59, 130, 246, 0.5);
      outline-offset: 2px;
    }

    &:active {
      transform: scale(1.02);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: all 0.3s ease;
    }
  }
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  .remember-me {
    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background-color: var(--color-primary, #3B82F6);
      border-color: var(--color-primary, #3B82F6);
    }

    :deep(.el-checkbox__label) {
      color: var(--color-text-secondary, rgba(255, 255, 255, 0.8));
      font-size: 14px;
      font-weight: 400;
      cursor: pointer;
      user-select: none;
    }

    :deep(.el-checkbox__inner) {
      border-color: var(--color-border, rgba(255, 255, 255, 0.3));
      background-color: var(--color-card-bg, rgba(255, 255, 255, 0.08));
      transition: all 0.2s ease;
    }

    :deep(.el-checkbox__input:hover .el-checkbox__inner) {
      border-color: var(--color-primary, rgba(59, 130, 246, 0.5));
    }
  }

  .forgot-password {
    font-size: 14px;
    color: var(--color-text-secondary, rgba(255, 255, 255, 0.7));
    text-decoration: none;
    font-weight: 400;
    transition: color 0.2s ease;
    position: relative;
    background: none;
    border: none;
    padding: 0;
    cursor: pointer;
    outline: none;

    &:hover {
      color: var(--color-primary-light, #60A5FA);
    }

    &:focus-visible {
      outline: 2px solid rgba(96, 165, 250, 0.6);
      outline-offset: 2px;
      border-radius: 4px;
    }
  }
}

.login-button-item {
  margin-bottom: 0;
}

.login-button {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary, #3B82F6) 0%, var(--color-primary-light, #60A5FA) 50%, var(--color-accent, #F97316) 100%);
  background-size: 200% 200%;
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  position: relative;
  overflow: hidden;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  .button-content {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .button-icon {
    width: 20px;
    height: 20px;
    transition: transform 0.2s ease;
  }

  &:hover:not(:disabled) .button-icon {
    transform: translateX(2px);
  }

  &:focus-visible {
    outline: 2px solid rgba(255, 255, 255, 0.5);
    outline-offset: 2px;
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .loading-spinner {
    width: 20px;
    height: 20px;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 表单错误状态增强
.login-form {
  :deep(.el-form-item.is-error) {
    .input-label {
      color: rgba(239, 68, 68, 0.9);
      animation: shake 0.4s ease-in-out;
    }

    .input-icon {
      color: rgba(239, 68, 68, 0.8);
    }
  }

  :deep(.el-form-item__error) {
    color: rgba(239, 68, 68, 0.9);
    font-size: 12px;
    font-weight: 500;
    margin-top: 6px;
    padding-left: 4px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
    animation: fadeInDown 0.3s ease-out;
  }
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-4px);
  }
  75% {
    transform: translateX(4px);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-agreement {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  font-size: 13px;
  color: #64748b;
}

.login-agreement-text {
  color: #64748b;
}

.login-agreement-link {
  font-size: 13px;
  color: var(--color-primary, #3B82F6);
  font-weight: 500;
  text-decoration: underline;
  background: none;
  border: none;
  padding: 0 2px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.login-agreement-link:hover {
  color: var(--color-primary-light, #60A5FA);
}

.login-agreement-link:focus-visible {
  outline: 2px solid rgba(59, 130, 246, 0.5);
  outline-offset: 2px;
  border-radius: 2px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;

  .register-link {
    font-size: 14px;
    color: var(--color-text-secondary, rgba(255, 255, 255, 0.7));

    a {
      color: var(--color-primary-light, #60A5FA);
      text-decoration: none;
      font-weight: 500;
      cursor: pointer;
      transition: color 0.2s ease;

      &:hover {
        color: var(--color-primary, #3B82F6);
        text-decoration: underline;
      }

      &:focus-visible {
        outline: 2px solid rgba(96, 165, 250, 0.6);
        outline-offset: 2px;
        border-radius: 4px;
      }
    }
  }
}

/* 表单切换标签样式 */
.form-tabs {
  display: flex;
  margin-bottom: 24px;
  background: var(--color-card-bg, rgba(255, 255, 255, 0.1));
  border-radius: 12px;
  padding: 4px;
  gap: 4px;
}

.form-tab {
  flex: 1;
  padding: 12px 24px;
  text-align: center;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-muted, rgba(255, 255, 255, 0.7));
  cursor: pointer;
  transition: color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
  transition-duration: 180ms;
  border-radius: 8px;
  background: transparent;
  border: none;

  &:hover {
    color: var(--color-text-primary, rgba(255, 255, 255, 0.9));
    background: var(--color-card-bg-hover, rgba(255, 255, 255, 0.1));
  }

  &:focus-visible {
    outline: 2px solid rgba(96, 165, 250, 0.6);
    outline-offset: 2px;
  }

  &.active {
    color: #ffffff;
    background: linear-gradient(135deg, var(--color-primary, #3B82F6) 0%, var(--color-primary-light, #60A5FA) 50%, var(--color-accent, #F97316) 100%);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  }
}

/* 权限选择器样式 */
.permission-selector {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.permission-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary, rgba(255, 255, 255, 0.9));
  margin-bottom: 16px;
  text-align: center;
}

.permission-options {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

.permission-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 14px 24px;
  border: 2px solid var(--color-border, rgba(255, 255, 255, 0.3));
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--color-card-bg, rgba(255, 255, 255, 0.08));
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover {
    border-color: var(--color-primary-light, #60A5FA);
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.2);
    background: var(--color-card-bg-hover, rgba(255, 255, 255, 0.12));
  }

  &.active {
    border-color: var(--color-primary-light, #60A5FA);
    background: linear-gradient(135deg, var(--color-primary, #3B82F6) 0%, var(--color-primary-light, #60A5FA) 50%, var(--color-accent, #F97316) 100%);
    box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
    color: #fff;
  }
}

.permission-radio {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  margin-right: 12px;
  position: relative;
  transition: all 0.3s ease;
  background-color: transparent;
}

.permission-option:hover .permission-radio {
  border-color: #60A5FA;
  transform: scale(1.1);
}

.permission-option.active .permission-radio {
  border-color: #fff;
  background-color: #fff;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.2);
}

.permission-option.active .permission-radio::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 10px;
  height: 10px;
  background-color: #3B82F6;
  border-radius: 50%;
  animation: radioPulse 0.3s ease;
}

@keyframes radioPulse {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.3);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
}

.permission-text {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary, rgba(255, 255, 255, 0.9));
  transition: all 0.3s ease;
}

.permission-option.active .permission-text {
  color: #fff;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .permission-options {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }
  
  .permission-option {
    width: 100%;
    max-width: 300px;
    justify-content: center;
  }
}

.system-info {
  margin-top: 18px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 400;
  text-align: center;
  letter-spacing: 0.5px;
}

.login-brand-strip {
  padding: 0 16px 28px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

@media (max-width: 768px) {
  .brand-subtitle {
    font-size: 14px;
    letter-spacing: 2px;
  }

  .feature-list .feature-item {
    padding: 10px 18px;
    font-size: 14px;
  }
}
</style>

<style rel="stylesheet/scss" lang="scss">
/* 忘记密码弹窗：与登录/注册页一致的 uiverse 风格（弹窗 teleported 到 body，需全局样式） */
.forgot-password-dialog {
  .el-dialog {
    border-radius: 20px;
    overflow: hidden;
    box-shadow: -8px 0 32px rgba(0, 0, 0, 0.08), 0 8px 32px rgba(0, 0, 0, 0.12);
  }
  .el-dialog__header {
    padding: 0;
    margin: 0;
  }
  .el-dialog__body {
    padding: 0 28px 24px;
    background: #fff;
  }
  .el-dialog__footer {
    padding: 0 28px 24px 28px;
    background: #fff;
  }
  .forgot-dialog-header {
    text-align: center;
    padding: 28px 28px 24px;
    background: #fff;

    .welcome-icon {
      width: 72px;
      height: 72px;
      margin: 0 auto 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, var(--color-primary, #3B82F6) 0%, var(--color-primary-light, #60A5FA) 50%, var(--color-accent, #F97316) 100%);
      background-size: 200% 200%;
      border-radius: 20px;
      color: #ffffff;
      box-shadow: 0 8px 24px rgba(59, 130, 246, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.2);

      svg {
        width: 36px;
        height: 36px;
      }
    }
    h2 {
      font-size: 24px;
      font-weight: 700;
      color: #0f172a;
      margin: 0 0 8px;
    }
    p {
      font-size: 14px;
      color: #64748b;
      margin: 0;
    }
  }
  .forgot-password-inner {
    .uiverse-input-wrapper {
      background: #f8fafc;
      border-color: #e2e8f0;
    }
    .uiverse-input-wrapper:hover {
      background: #f1f5f9;
      border-color: #cbd5e1;
    }
    .uiverse-input-wrapper.uiverse-focused {
      background: #fff;
      border-color: var(--color-primary, #3B82F6);
    }
    .uiverse-input {
      color: #0f172a;
    }
    .floating-label {
      color: #64748b;
    }
    .uiverse-input-wrapper.uiverse-focused .floating-label,
    .uiverse-input-wrapper.uiverse-has-value .floating-label {
      color: var(--color-primary, #3B82F6);
      background: #fff;
    }
    .uiverse-input-icon {
      color: #64748b;
    }
    .uiverse-input-wrapper.uiverse-focused .uiverse-input-icon {
      color: var(--color-primary, #3B82F6);
    }
    .uiverse-password-toggle {
      color: #64748b;
    }
    .uiverse-password-toggle:hover {
      color: var(--color-primary, #3B82F6);
    }
    .uiverse-input:-webkit-autofill,
    .uiverse-input:-webkit-autofill:hover,
    .uiverse-input:-webkit-autofill:focus,
    .uiverse-input:-webkit-autofill:active {
      -webkit-box-shadow: 0 0 0 30px #f8fafc inset !important;
      box-shadow: 0 0 0 30px #f8fafc inset !important;
      -webkit-text-fill-color: #0f172a !important;
    }
  }
  .forgot-dialog-footer {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12px;
  }
  .forgot-btn-cancel {
    padding: 12px 24px;
    font-size: 15px;
    font-weight: 500;
    color: #64748b;
    background: #f1f5f9;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  .forgot-btn-cancel:hover {
    background: #e2e8f0;
    border-color: #cbd5e1;
    color: #0f172a;
  }
  .forgot-btn-submit {
    flex: 0 0 auto;
    min-width: 140px;
    height: 48px;
  }
}
</style>
