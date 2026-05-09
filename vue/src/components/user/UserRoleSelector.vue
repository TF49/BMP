<template>
  <div class="user-role-selector">
    <div class="selector-shell">
      <AmbientParticleField />
      <div class="selector-orbit selector-orbit--left"></div>
      <div class="selector-orbit selector-orbit--right"></div>
    </div>

    <div class="selector-summary">
      <div class="selector-summary__label">当前角色</div>
      <div class="selector-summary__main">
        <div class="selector-summary__title">{{ activeOption?.title || '未选择角色' }}</div>
        <div class="selector-summary__desc">{{ activeOption?.hint || '请选择一个角色以继续配置账户权限。' }}</div>
      </div>
      <div v-if="activeOption?.badge" class="selector-summary__badge">
        {{ activeOption.badge }}
      </div>
    </div>

    <div class="role-grid" role="radiogroup" aria-label="用户角色">
      <button
        v-for="option in options"
        :key="option.value"
        type="button"
        class="role-card"
        :class="[
          `role-card--${option.tone || 'blue'}`,
          {
            'is-active': option.value === modelValue,
            'is-disabled': option.disabled
          }
        ]"
        :aria-pressed="option.value === modelValue"
        :disabled="option.disabled"
        @click="handleSelect(option.value)"
      >
        <div class="role-card__topline">
          <span class="role-card__badge">{{ option.badge }}</span>
          <span class="role-card__check" :class="{ 'is-active': option.value === modelValue }">
            <el-icon><CircleCheckFilled /></el-icon>
          </span>
        </div>

        <div class="role-card__icon" :class="`role-card__icon--${option.tone || 'blue'}`">
          <component :is="resolveIcon(option.icon)" />
        </div>

        <div class="role-card__body">
          <div class="role-card__title">{{ option.title }}</div>
          <div class="role-card__desc">{{ option.desc }}</div>
        </div>

        <div class="role-card__footer">
          <span v-if="option.disabled && option.disabledReason" class="role-card__warning">
            {{ option.disabledReason }}
          </span>
          <span v-else class="role-card__hint">
            {{ option.hint }}
          </span>
        </div>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  Avatar,
  CircleCheckFilled,
  Management,
  Trophy,
  User
} from '@element-plus/icons-vue'
import AmbientParticleField from '@/components/user/AmbientParticleField.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  options: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const iconMap = {
  trophy: Trophy,
  management: Management,
  avatar: Avatar,
  user: User
}

const activeOption = computed(() => {
  return props.options.find((option) => option.value === props.modelValue) || props.options[0] || null
})

const resolveIcon = (icon) => {
  return iconMap[icon] || User
}

const handleSelect = (value) => {
  if (value === props.modelValue) return
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped>
.user-role-selector {
  position: relative;
  width: 100%;
}

.selector-shell {
  position: absolute;
  inset: 0;
  border-radius: 24px;
  overflow: hidden;
  pointer-events: none;
}

.selector-orbit {
  position: absolute;
  border-radius: 999px;
  border: 1px solid rgba(59, 130, 246, 0.12);
  animation: orbitPulse 7s ease-in-out infinite;
}

.selector-orbit--left {
  top: 12px;
  left: 16px;
  width: 190px;
  height: 88px;
}

.selector-orbit--right {
  right: 8px;
  bottom: 24px;
  width: 210px;
  height: 110px;
  animation-delay: 1.8s;
}

.selector-summary {
  position: relative;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  padding: 16px 18px;
  margin-bottom: 16px;
  background:
    linear-gradient(135deg, rgba(239, 246, 255, 0.92) 0%, rgba(248, 250, 252, 0.92) 100%);
  border: 1px solid rgba(59, 130, 246, 0.14);
  border-radius: 14px;
}

.selector-summary__label {
  align-self: start;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(37, 99, 235, 0.08);
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.selector-summary__main {
  min-width: 0;
}

.selector-summary__title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.selector-summary__desc {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
}

.selector-summary__badge {
  padding: 7px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(148, 163, 184, 0.28);
  color: #334155;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  backdrop-filter: blur(10px);
}

.role-grid {
  position: relative;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.role-card {
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 222px;
  padding: 18px;
  text-align: left;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92) 0%, rgba(248, 250, 252, 0.96) 100%);
  border: 1px solid #dbe5f2;
  border-radius: 18px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.05);
  backdrop-filter: blur(12px);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease, background 0.24s ease;
  cursor: pointer;
  overflow: hidden;
}

.role-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(125deg, rgba(255, 255, 255, 0) 24%, rgba(255, 255, 255, 0.72) 48%, rgba(255, 255, 255, 0) 72%);
  transform: translateX(-120%);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.role-card::after {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: 17px;
  border: 1px solid transparent;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.55), rgba(255, 255, 255, 0)) padding-box,
    linear-gradient(135deg, rgba(255, 255, 255, 0.65), rgba(148, 163, 184, 0.02)) border-box;
  -webkit-mask:
    linear-gradient(#fff 0 0) padding-box,
    linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
          mask-composite: exclude;
  pointer-events: none;
}

.role-card:hover:not(.is-disabled) {
  transform: translateY(-3px);
  box-shadow: 0 14px 26px rgba(37, 99, 235, 0.12);
}

.role-card:hover:not(.is-disabled)::before {
  opacity: 1;
  animation: sheenMove 1.6s ease;
}

.role-card.is-active {
  border-color: rgba(37, 99, 235, 0.45);
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 18px 30px rgba(37, 99, 235, 0.16);
}

.role-card.is-disabled {
  cursor: not-allowed;
  opacity: 0.68;
  box-shadow: none;
}

.role-card__topline {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.role-card__badge {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 12px;
  font-weight: 700;
}

.role-card__check {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  background: #f8fafc;
  border: 1px solid #dbe5f2;
  color: transparent;
  transition: all 0.24s ease;
}

.role-card__check.is-active {
  color: #2563eb;
  background: rgba(37, 99, 235, 0.1);
  border-color: rgba(37, 99, 235, 0.22);
}

.role-card__icon {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  margin-top: 22px;
  border-radius: 18px;
  font-size: 24px;
  box-shadow: 0 12px 22px rgba(148, 163, 184, 0.14);
}

.role-card__icon--red {
  background: linear-gradient(135deg, #fee2e2 0%, #fff1f2 100%);
  color: #dc2626;
  border: 1px solid #fecaca;
}

.role-card__icon--amber {
  background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%);
  color: #d97706;
  border: 1px solid #fde68a;
}

.role-card__icon--teal {
  background: linear-gradient(135deg, #ccfbf1 0%, #f0fdfa 100%);
  color: #0f766e;
  border: 1px solid #99f6e4;
}

.role-card__icon--blue {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  color: #2563eb;
  border: 1px solid #bfdbfe;
}

.role-card__body {
  position: relative;
  z-index: 1;
  margin-top: 16px;
}

.role-card__title {
  font-size: 20px;
  font-weight: 800;
  line-height: 1.2;
  color: #0f172a;
}

.role-card__desc {
  margin-top: 8px;
  min-height: 42px;
  font-size: 13px;
  line-height: 1.6;
  color: #64748b;
}

.role-card__footer {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding-top: 14px;
}

.role-card__hint,
.role-card__warning {
  display: block;
  font-size: 12px;
  line-height: 1.6;
}

.role-card__hint {
  color: #475569;
}

.role-card__warning {
  color: #dc2626;
  font-weight: 700;
}

.role-card--red.is-active {
  border-color: rgba(220, 38, 38, 0.36);
  box-shadow: 0 18px 30px rgba(220, 38, 38, 0.12);
}

.role-card--amber.is-active {
  border-color: rgba(217, 119, 6, 0.36);
  box-shadow: 0 18px 30px rgba(217, 119, 6, 0.12);
}

.role-card--teal.is-active {
  border-color: rgba(13, 148, 136, 0.36);
  box-shadow: 0 18px 30px rgba(13, 148, 136, 0.12);
}

.role-card--blue.is-active {
  border-color: rgba(37, 99, 235, 0.36);
  box-shadow: 0 18px 30px rgba(37, 99, 235, 0.12);
}

@keyframes orbitPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.38;
  }
  50% {
    transform: scale(1.04);
    opacity: 0.72;
  }
}

@keyframes sheenMove {
  0% {
    transform: translateX(-120%);
  }
  100% {
    transform: translateX(120%);
  }
}

@media (max-width: 1280px) {
  .role-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .selector-summary {
    grid-template-columns: 1fr;
  }

  .selector-summary__badge {
    justify-self: start;
  }

  .role-grid {
    grid-template-columns: 1fr;
  }

  .role-card {
    min-height: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .selector-orbit,
  .role-card,
  .role-card::before {
    animation: none !important;
    transition: none !important;
  }
}
</style>
