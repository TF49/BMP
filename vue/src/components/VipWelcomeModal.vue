<template>
  <Teleport to="body">
    <Transition name="vip-welcome-fade">
      <div
        v-show="modelValue"
        class="vip-welcome-overlay"
        role="dialog"
        aria-modal="true"
        aria-labelledby="vip-welcome-title"
        @click.self="handleClose"
      >
        <div class="vip-welcome-backdrop" aria-hidden="true" />
        <div
          class="vip-welcome-card"
          :class="vipCardClass"
          @click.stop
        >
          <!-- 卡片内层高光与装饰（Galaxy 风格渐变层次） -->
          <div class="vip-welcome-card-shine" aria-hidden="true" />
          <div class="vip-welcome-card-dots" aria-hidden="true">
            <span class="dot dot-1" /><span class="dot dot-2" /><span class="dot dot-3" />
            <span class="dot dot-4" /><span class="dot dot-5" /><span class="dot dot-6" />
          </div>
          <!-- 顶部光效条 -->
          <div class="vip-welcome-glow" aria-hidden="true" />
          <!-- 装饰性图标区（Vue Bits 式微动效） -->
          <div class="vip-welcome-icon-wrap">
            <div class="vip-welcome-icon">
              <el-icon :size="52"><HomeFilled /></el-icon>
            </div>
          </div>
          <h2 id="vip-welcome-title" class="vip-welcome-title">
            欢迎回家
          </h2>
          <p class="vip-welcome-subtitle">
            <span class="vip-welcome-name">{{ userName }}</span>，尊贵的
            <span class="vip-welcome-level" :class="vipCardClass">{{ vipLevelLabel }}</span>
          </p>
          <p class="vip-welcome-desc">
            感谢您的支持，祝您使用愉快
          </p>
          <button
            type="button"
            class="vip-welcome-btn"
            @click="handleClose"
          >
            <span class="vip-welcome-btn-text">开始使用</span>
          </button>
          <button
            type="button"
            class="vip-welcome-close"
            aria-label="关闭"
            @click="handleClose"
          >
            <el-icon :size="20"><Close /></el-icon>
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { HomeFilled, Close } from '@element-plus/icons-vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    userName?: string
    vipLevelLabel?: string
    memberLevel?: number
  }>(),
  {
    userName: '会员',
    vipLevelLabel: 'VIP',
    memberLevel: 3
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'close'): void
}>()

const vipCardClass = computed(() => {
  const level = Math.min(5, Math.max(0, props.memberLevel ?? 0))
  return `vip-welcome-lv${level}`
})

const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.modelValue) handleClose()
}

watch(() => props.modelValue, (open) => {
  if (open) {
    document.addEventListener('keydown', onKeydown)
  } else {
    document.removeEventListener('keydown', onKeydown)
  }
})

onMounted(() => {
  if (props.modelValue) document.addEventListener('keydown', onKeydown)
})
onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
.vip-welcome-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.vip-welcome-backdrop {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.52);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  transition: opacity 0.35s ease;
}

/* 卡片容器：多层阴影 + 入场动画（Vue Bits 弹性节奏） */
.vip-welcome-card {
  position: relative;
  width: 100%;
  max-width: 440px;
  padding: 44px 36px 40px;
  border-radius: 32px;
  text-align: center;
  overflow: hidden;
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.12) inset,
    0 24px 48px rgba(0, 0, 0, 0.2),
    0 12px 24px rgba(0, 0, 0, 0.12);
  animation: vipCardEntrance 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

@media (prefers-reduced-motion: reduce) {
  .vip-welcome-card {
    animation: none;
  }
  .vip-welcome-card:hover .vip-welcome-icon {
    transform: none;
  }
  .vip-welcome-icon-wrap,
  .vip-welcome-title,
  .vip-welcome-subtitle,
  .vip-welcome-desc,
  .vip-welcome-btn {
    animation: none;
  }
  .vip-welcome-btn::before {
    transition: none;
  }
}

@keyframes vipCardEntrance {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(24px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 内层高光（Galaxy 风格径向渐变，增强玻璃感） */
.vip-welcome-card-shine {
  position: absolute;
  inset: 0;
  pointer-events: none;
  border-radius: inherit;
  background: radial-gradient(
    ellipse 100% 80% at 50% -20%,
    rgba(255, 255, 255, 0.25) 0%,
    transparent 55%
  );
}

/* 装饰点（Galaxy/NotificationPopup 风格，增加层次） */
.vip-welcome-card-dots {
  position: absolute;
  inset: 0;
  pointer-events: none;
  border-radius: inherit;
}
.vip-welcome-card-dots .dot {
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
}
.vip-welcome-card-dots .dot-1 { top: 20%; left: 12%; }
.vip-welcome-card-dots .dot-2 { top: 28%; right: 16%; }
.vip-welcome-card-dots .dot-3 { bottom: 32%; left: 14%; }
.vip-welcome-card-dots .dot-4 { bottom: 24%; right: 12%; }
.vip-welcome-card-dots .dot-5 { top: 50%; left: 8%; }
.vip-welcome-card-dots .dot-6 { top: 48%; right: 10%; }

/* 顶部光效条 */
.vip-welcome-glow {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 64%;
  height: 5px;
  border-radius: 0 0 6px 6px;
  opacity: 0.95;
}

/* 图标区：略大 + 轻微呼吸光（Vue Bits 式微动效） */
.vip-welcome-icon-wrap {
  margin-bottom: 22px;
  position: relative;
  z-index: 1;
  animation: vipStagger 0.5s ease-out 0.1s both;
}
@keyframes vipStagger {
  from {
    opacity: 0;
    transform: scale(0.85) translateY(8px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.vip-welcome-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  border: 2px solid rgba(255, 255, 255, 0.4);
  color: rgba(255, 255, 255, 0.98);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.1) inset,
    0 10px 36px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.vip-welcome-card:hover .vip-welcome-icon {
  transform: scale(1.03);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.15) inset, 0 12px 40px rgba(0, 0, 0, 0.22);
}

/* 标题：渐变 + 轻微字阴影，层次更清晰 */
.vip-welcome-title {
  margin: 0 0 14px 0;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 0.04em;
  background: linear-gradient(180deg, #fff 0%, rgba(255, 255, 255, 0.92) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.25;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.15));
  position: relative;
  z-index: 1;
  animation: vipStagger 0.45s ease-out 0.18s both;
}

.vip-welcome-subtitle {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.92);
  line-height: 1.5;
  position: relative;
  z-index: 1;
  animation: vipStagger 0.45s ease-out 0.24s both;
}

.vip-welcome-name {
  font-weight: 700;
  color: #fff;
}

.vip-welcome-level {
  font-weight: 700;
  margin-left: 4px;
}

.vip-welcome-desc {
  margin: 0 0 30px 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.55;
  position: relative;
  z-index: 1;
  animation: vipStagger 0.45s ease-out 0.3s both;
}

/* 主按钮：Galaxy 风格 hover 光泽扫过 + 无布局位移 */
.vip-welcome-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 180px;
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.28);
  border: 2px solid rgba(255, 255, 255, 0.52);
  border-radius: 18px;
  cursor: pointer;
  transition: color 0.25s ease, background 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.18);
  overflow: hidden;
  z-index: 1;
  animation: vipStagger 0.45s ease-out 0.36s both;
}

.vip-welcome-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.25),
    transparent
  );
  transition: left 0.45s ease;
}

.vip-welcome-btn:hover {
  background: rgba(255, 255, 255, 0.38);
  border-color: rgba(255, 255, 255, 0.75);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.22);
}
.vip-welcome-btn:hover::before {
  left: 100%;
}

.vip-welcome-btn:active {
  transform: scale(0.98);
}

.vip-welcome-btn-text {
  position: relative;
  z-index: 1;
}

.vip-welcome-close {
  position: absolute;
  top: 18px;
  right: 18px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.85);
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
  z-index: 2;
}

.vip-welcome-close:hover {
  color: #fff;
  background: rgba(255, 255, 255, 0.28);
  border-color: rgba(255, 255, 255, 0.4);
  transform: scale(1.05);
}

.vip-welcome-close:active {
  transform: scale(0.96);
}

/* 按等级区分卡片底色与光效（与 Dashboard VIP 徽章一致） */
.vip-welcome-card.vip-welcome-lv0 {
  background: linear-gradient(160deg, rgba(71, 85, 105, 0.95) 0%, rgba(51, 65, 85, 0.98) 100%);
}
.vip-welcome-lv0 .vip-welcome-glow { background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent); }

.vip-welcome-card.vip-welcome-lv1 {
  background: linear-gradient(160deg, rgba(139, 90, 43, 0.92) 0%, rgba(101, 67, 33, 0.96) 100%);
}
.vip-welcome-lv1 .vip-welcome-glow { background: linear-gradient(90deg, transparent, rgba(205, 127, 50, 0.9), transparent); }
.vip-welcome-lv1 .vip-welcome-level { color: #e8c98a; }

.vip-welcome-card.vip-welcome-lv2 {
  background: linear-gradient(160deg, rgba(119, 136, 153, 0.92) 0%, rgba(89, 102, 116, 0.96) 100%);
}
.vip-welcome-lv2 .vip-welcome-glow { background: linear-gradient(90deg, transparent, rgba(192, 192, 192, 0.9), transparent); }
.vip-welcome-lv2 .vip-welcome-level { color: #f0f4f8; }

.vip-welcome-card.vip-welcome-lv3 {
  background: linear-gradient(160deg, rgba(180, 140, 20, 0.9) 0%, rgba(140, 100, 10, 0.95) 100%);
}
.vip-welcome-lv3 .vip-welcome-glow { background: linear-gradient(90deg, transparent, rgba(255, 215, 0, 0.95), transparent); }
.vip-welcome-lv3 .vip-welcome-level { color: #ffd700; text-shadow: 0 0 20px rgba(255, 215, 0, 0.6); }

.vip-welcome-card.vip-welcome-lv4 {
  background: linear-gradient(160deg, rgba(218, 165, 32, 0.9) 0%, rgba(184, 134, 11, 0.95) 100%);
}
.vip-welcome-lv4 .vip-welcome-glow { background: linear-gradient(90deg, transparent, rgba(255, 193, 7, 0.95), transparent); }
.vip-welcome-lv4 .vip-welcome-level { color: #ffe44d; text-shadow: 0 0 24px rgba(255, 228, 77, 0.7); }

.vip-welcome-card.vip-welcome-lv5 {
  background: linear-gradient(160deg, rgba(255, 215, 0, 0.88) 0%, rgba(218, 165, 32, 0.94) 100%);
  box-shadow:
    0 0 0 1px rgba(255, 255, 255, 0.14) inset,
    0 28px 56px rgba(0, 0, 0, 0.26),
    0 0 80px rgba(255, 215, 0, 0.22);
}
.vip-welcome-lv5 .vip-welcome-card-shine {
  background: radial-gradient(
    ellipse 100% 80% at 50% -20%,
    rgba(255, 255, 255, 0.35) 0%,
    transparent 50%
  );
}
.vip-welcome-lv5 .vip-welcome-glow { background: linear-gradient(90deg, transparent, rgba(255, 250, 205, 0.95), transparent); }
.vip-welcome-lv5 .vip-welcome-level { color: #fffacd; text-shadow: 0 0 28px rgba(255, 250, 205, 0.9); }
</style>

<style>
/* 全局过渡：overlay 淡入淡出（Vue Bits 式节奏） */
.vip-welcome-fade-enter-active {
  transition: opacity 0.35s ease;
}
.vip-welcome-fade-leave-active {
  transition: opacity 0.28s ease;
}
.vip-welcome-fade-enter-active .vip-welcome-card,
.vip-welcome-fade-leave-active .vip-welcome-card {
  transition: transform 0.35s ease, opacity 0.35s ease;
}
.vip-welcome-fade-enter-from,
.vip-welcome-fade-leave-to {
  opacity: 0;
}
.vip-welcome-fade-enter-from .vip-welcome-card,
.vip-welcome-fade-leave-to .vip-welcome-card {
  opacity: 0;
  transform: scale(0.94) translateY(20px);
}
</style>
