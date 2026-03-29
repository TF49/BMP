<template>
  <div class="welcome-section">
    <div class="welcome-background">
      <div class="welcome-gradient"></div>
      <div class="welcome-pattern"></div>
      <div class="floating-elements">
        <div class="float-circle circle-1"></div>
        <div class="float-circle circle-2"></div>
        <div class="float-circle circle-3"></div>
      </div>
    </div>
    <div class="welcome-main">
      <div class="welcome-left">
        <div class="welcome-icon-wrapper">
          <div class="welcome-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/>
              <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
              <circle cx="9" cy="9" r="1" fill="currentColor"/>
              <circle cx="15" cy="9" r="1" fill="currentColor"/>
            </svg>
          </div>
        </div>
        <div class="welcome-content">
          <div class="welcome-greeting">
            <span class="greeting-emoji">{{ greetingEmoji }}</span>
            <h1 class="welcome-title">
              <span class="greeting-text">{{ greeting }}</span>
              <span class="user-name">{{ userName }}</span>
            </h1>
          </div>
          <p class="welcome-subtitle">
            <span class="subtitle-icon">✨</span>
            欢迎回到羽擎，祝您工作顺利！
          </p>
          <div class="welcome-tags">
            <span class="welcome-tag tag-primary">
              <el-icon><Calendar /></el-icon>
              今日预订 {{ todayBookings }} 次
            </span>
            <span class="welcome-tag tag-success">
              <el-icon><User /></el-icon>
              新增会员 {{ todayNewMembers }} 人
            </span>
          </div>
        </div>
      </div>
      <div class="welcome-right">
        <div class="date-card">
          <div class="date-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <div class="date-info">
            <span class="date-text">{{ currentDate }}</span>
            <span class="weekday-text">{{ currentWeekday }}</span>
          </div>
          <div class="time-display">{{ currentTime }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { User, Calendar } from '@element-plus/icons-vue'

defineProps({
  greetingEmoji: { type: String, default: '' },
  greeting: { type: String, default: '' },
  userName: { type: String, default: '' },
  currentDate: { type: String, default: '' },
  currentWeekday: { type: String, default: '' },
  currentTime: { type: String, default: '' },
  todayBookings: { type: Number, default: 0 },
  todayNewMembers: { type: Number, default: 0 }
})
</script>

<style scoped>
.welcome-section {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary, #8B5CF6) 50%, var(--color-accent, #EC4899) 100%);
  box-shadow:
    0 20px 60px rgba(var(--color-primary-rgb, 37, 99, 235), 0.3),
    0 8px 24px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  animation: welcomeFadeIn 0.8s ease-out;
}

@keyframes welcomeFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.welcome-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.welcome-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.welcome-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.08) 2px, transparent 2px),
    radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.06) 2px, transparent 2px);
  background-size: 60px 60px;
  animation: patternMove 20s linear infinite;
}

@keyframes patternMove {
  0% { background-position: 0 0, 30px 30px; }
  100% { background-position: 60px 60px, 90px 90px; }
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.float-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
  animation: floatCircle 8s ease-in-out infinite;
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: -30px;
  right: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 80px;
  height: 80px;
  bottom: -20px;
  left: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 60px;
  height: 60px;
  top: 50%;
  right: 25%;
  animation-delay: 4s;
}

@keyframes floatCircle {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-20px) scale(1.1);
    opacity: 0.8;
  }
}

.welcome-main {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px 40px;
  gap: 24px;
}

.welcome-left {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}

.welcome-icon-wrapper {
  flex-shrink: 0;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: iconPulse 3s ease-in-out infinite;
}

.welcome-icon:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

@keyframes iconPulse {
  0%, 100% {
    box-shadow:
      0 8px 32px rgba(0, 0, 0, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }
  50% {
    box-shadow:
      0 12px 40px rgba(0, 0, 0, 0.2),
      0 0 30px rgba(255, 255, 255, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }
}

.welcome-icon svg {
  width: 40px;
  height: 40px;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.welcome-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.welcome-greeting {
  display: flex;
  align-items: center;
  gap: 12px;
}

.greeting-emoji {
  font-size: 32px;
  animation: emojiWave 2s ease-in-out infinite;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

@keyframes emojiWave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

.welcome-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin: 0;
  font-family: inherit;
}

.greeting-text {
  font-size: 28px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.user-name {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  position: relative;
}

.user-name::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.8), rgba(255, 255, 255, 0.2));
  border-radius: 2px;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s ease;
}

.welcome-greeting:hover .user-name::after {
  transform: scaleX(1);
}

.welcome-subtitle {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-family: inherit;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

.subtitle-icon {
  font-size: 16px;
  animation: sparkle 2s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.2); }
}

.welcome-tags {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.welcome-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-family: inherit;
  font-size: 13px;
  font-weight: 500;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  cursor: default;
}

.welcome-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.tag-primary {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.tag-success {
  background: rgba(16, 185, 129, 0.3);
  color: #ffffff;
  border-color: rgba(16, 185, 129, 0.4);
}

.welcome-right {
  flex-shrink: 0;
}

.date-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 28px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s ease;
  min-width: 180px;
}

.date-card:hover {
  transform: translateY(-4px) scale(1.02);
  background: rgba(255, 255, 255, 0.2);
  box-shadow:
    0 16px 48px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.date-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  color: #ffffff;
  transition: all 0.3s ease;
}

.date-icon svg {
  width: 24px;
  height: 24px;
}

.date-card:hover .date-icon {
  transform: scale(1.1) rotate(5deg);
  background: rgba(255, 255, 255, 0.3);
}

.date-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.date-text {
  font-family: inherit;
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

.weekday-text {
  font-family: inherit;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

.time-display {
  font-family: ui-monospace, 'Cascadia Code', Consolas, monospace;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  letter-spacing: 2px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.7));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

@media (max-width: 1024px) {
  .welcome-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    padding: 28px 32px;
  }

  .welcome-right {
    width: 100%;
  }

  .date-card {
    flex-direction: row;
    justify-content: center;
    width: 100%;
  }

  .date-info {
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .welcome-section {
    border-radius: 18px;
  }

  .welcome-main {
    padding: 24px;
  }

  .welcome-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .welcome-icon {
    width: 64px;
    height: 64px;
    border-radius: 18px;
  }

  .welcome-icon svg {
    width: 32px;
    height: 32px;
  }

  .greeting-emoji {
    font-size: 24px;
  }

  .greeting-text {
    font-size: 22px;
  }

  .user-name {
    font-size: 26px;
  }

  .welcome-subtitle {
    font-size: 14px;
  }

  .welcome-tags {
    flex-wrap: wrap;
  }

  .welcome-tag {
    padding: 6px 12px;
    font-size: 12px;
  }

  .time-display {
    font-size: 22px;
  }

  .circle-1 {
    width: 80px;
    height: 80px;
  }

  .circle-2, .circle-3 {
    display: none;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    flex-direction: column;
    gap: 4px;
  }

  .greeting-text, .user-name {
    font-size: 20px;
  }

  .date-card {
    padding: 16px 20px;
    flex-direction: column;
  }

  .time-display {
    font-size: 20px;
  }
}
</style>
