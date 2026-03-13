<template>
  <div class="character-wrapper" :class="[wrapperClass, { 'is-blinking': isBlinking }]">
    <!-- 左侧羽毛球吉祥物 -->
    <div class="character character-left" :class="stateClass">
      <svg viewBox="0 0 120 160" class="character-svg">
        <!-- 羽毛球主体 -->
        <g class="character-body">
          <ellipse cx="60" cy="40" rx="26" ry="16" />
          <ellipse cx="60" cy="36" rx="22" ry="12" class="character-cap" />
        </g>

        <!-- 羽毛，密码遮挡时作为“遮眼帘” -->
        <g class="character-feathers">
          <path d="M25 40 Q60 90 55 140" />
          <path d="M35 40 Q60 90 60 140" />
          <path d="M45 40 Q60 90 65 140" />
          <path d="M75 40 Q60 90 65 140" />
          <path d="M85 40 Q60 90 55 140" />
        </g>

        <!-- 眼睛 -->
        <g class="character-eyes">
          <g class="eye-group" :style="eyeTransform">
            <circle cx="48" cy="38" r="6" class="eye-white" />
            <circle cx="48" cy="38" r="3" class="eye-pupil" />
          </g>
          <g class="eye-group" :style="eyeTransform">
            <circle cx="72" cy="38" r="6" class="eye-white" />
            <circle cx="72" cy="38" r="3" class="eye-pupil" />
          </g>
        </g>

        <!-- 腮红 -->
        <g class="character-blush">
          <ellipse cx="42" cy="46" rx="6" ry="3" />
          <ellipse cx="78" cy="46" rx="6" ry="3" />
        </g>

        <!-- 底座 -->
        <g class="character-base">
          <ellipse cx="60" cy="148" rx="16" ry="6" />
        </g>
      </svg>
    </div>

    <!-- 右侧较小吉祥物，增强层次感 -->
    <div class="character character-right" :class="stateClass">
      <svg viewBox="0 0 120 160" class="character-svg small">
        <g class="character-body">
          <ellipse cx="60" cy="45" rx="20" ry="12" />
          <ellipse cx="60" cy="42" rx="17" ry="9" class="character-cap" />
        </g>
        <g class="character-eyes">
          <circle cx="50" cy="44" r="5" class="eye-white" />
          <circle cx="50" cy="44" r="2.5" class="eye-pupil" />
          <circle cx="70" cy="44" r="5" class="eye-white" />
          <circle cx="70" cy="44" r="2.5" class="eye-pupil" />
        </g>
        <g class="character-base">
          <ellipse cx="60" cy="150" rx="14" ry="5" />
        </g>
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps<{
  isPasswordFocused?: boolean
  isLoading?: boolean
  hasError?: boolean
  mousePosition?: { x: number; y: number } | null
  isPasswordVisible?: boolean
}>()

const wrapperClass = computed(() => ({
  'is-password-visible': !!props.isPasswordVisible,
  'is-password-focused': !!props.isPasswordFocused
}))

const stateClass = computed(() => {
  if (props.isLoading) return 'is-loading'
  if (props.hasError) return 'is-error'
  if (props.isPasswordFocused) return 'is-shy'
  return 'is-idle'
})

const eyeOffsetX = ref(0)
const eyeOffsetY = ref(0)
const isBlinking = ref(false)
let frameId: number | null = null
let pendingPos: { x: number; y: number } | null = null
let blinkTimer: number | null = null

const scheduleUpdate = () => {
  if (frameId != null) return
  frameId = requestAnimationFrame(() => {
    frameId = null
    if (!pendingPos) return

    // 将 [0,1] 范围的输入映射到 -1..1，再限制最大偏移
    const maxOffset = 4
    const nx = Math.max(-1, Math.min(1, (pendingPos.x - 0.5) * 2))
    const ny = Math.max(-1, Math.min(1, (pendingPos.y - 0.5) * 2))
    eyeOffsetX.value = nx * maxOffset
    eyeOffsetY.value = ny * maxOffset
  })
}

watch(
  () => props.mousePosition,
  (pos) => {
    if (!pos) {
      eyeOffsetX.value = 0
      eyeOffsetY.value = 0
      pendingPos = null
      return
    }
    pendingPos = pos
    scheduleUpdate()
  }
)

onMounted(() => {
  if (typeof window !== 'undefined' && window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    eyeOffsetX.value = 0
    eyeOffsetY.value = 0
  }

   // 随机眨眼
  const scheduleBlink = () => {
    if (typeof window === 'undefined') return
    const delay = 2000 + Math.random() * 3000
    blinkTimer = window.setTimeout(() => {
      isBlinking.value = true
      setTimeout(() => {
        isBlinking.value = false
        scheduleBlink()
      }, 150)
    }, delay)
  }

  if (!(window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches)) {
    scheduleBlink()
  }
})

onBeforeUnmount(() => {
  if (frameId != null) {
    cancelAnimationFrame(frameId)
    frameId = null
  }
  if (blinkTimer != null && typeof window !== 'undefined') {
    window.clearTimeout(blinkTimer)
    blinkTimer = null
  }
})

const eyeTransform = computed(() => {
  let transform = ''
  if (props.hasError) {
    transform += 'translateY(3px) '
  } else if (props.isPasswordFocused) {
    transform += 'translateY(2px) '
  }
  if (eyeOffsetX.value || eyeOffsetY.value) {
    transform += `translate(${eyeOffsetX.value}px, ${eyeOffsetY.value}px)`
  }
  if (!transform) {
    return ''
  }
  return `transform: ${transform};`
})
</script>

<style scoped>
.character-wrapper {
  position: absolute;
  inset: -10px;
  pointer-events: none;
}

.character {
  position: absolute;
  bottom: -10px;
  filter: drop-shadow(0 12px 30px rgba(15, 23, 42, 0.5));
  opacity: 0.9;
  animation: floatIdle 4s ease-in-out infinite;
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.character-left {
  left: -60px;
}

.character-right {
  right: -60px;
  animation-delay: 0.8s;
}

.character-svg {
  width: 140px;
  height: auto;
}

.character-svg.small {
  width: 110px;
}

.character-body {
  fill: #ffffff;
  stroke: #e5e7eb;
  stroke-width: 2;
}

.character-cap {
  fill: rgba(148, 163, 184, 0.18);
}

.character-feathers path {
  stroke: rgba(255, 255, 255, 0.8);
  stroke-width: 2;
  stroke-linecap: round;
}

.character-feathers {
  transform-origin: 60px 40px;
  transition: transform 0.25s ease-out;
}

.eye-white {
  fill: #ffffff;
}

.eye-pupil {
  fill: var(--color-primary);
}

.character-blush ellipse {
  fill: var(--color-secondary);
  opacity: 0.35;
}

.character-base ellipse {
  fill: rgba(15, 23, 42, 0.55);
}

.eye-group {
  transition: transform 0.16s ease-out;
  transform-origin: center;
}

.character-wrapper.is-blinking .eye-group {
  transform: scaleY(0.15) translateY(3px);
}

.character-wrapper.is-password-focused .eye-group,
.character-wrapper.is-password-focused .character-eyes {
  opacity: 0;
}

@keyframes floatIdle {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.character.is-loading {
  animation: floatLoading 0.8s ease-in-out infinite;
}

@keyframes floatLoading {
  0%,
  100% {
    transform: translateY(0) rotate(-2deg);
  }
  50% {
    transform: translateY(-6px) rotate(2deg);
  }
}

.character.is-error {
  animation: floatError 0.4s ease-in-out 0s 3;
}

@keyframes floatError {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-6px);
  }
  75% {
    transform: translateX(6px);
  }
}

.character.is-shy .character-eyes {
  opacity: 0.3;
}

.character-wrapper.is-password-focused .character-feathers {
  transform: translateY(8px) rotate(6deg);
}

.character-wrapper.is-password-visible .character-feathers {
  transform: translateY(0) rotate(0);
}

.character-wrapper {
  will-change: transform;
}

@media (max-width: 1024px) {
  .character-left {
    left: -40px;
  }

  .character-right {
    right: -40px;
  }

  .character-svg {
    width: 120px;
  }
}

@media (max-width: 768px) {
  .character-right {
    display: none;
  }

  .character-left {
    left: -20px;
    bottom: -6px;
  }

  .character-svg {
    width: 110px;
  }
}
</style>

