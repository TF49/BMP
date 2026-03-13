<template>
  <view class="animated-background" :class="{ 'galaxy-bg': true }">
    <canvas
      v-if="useCanvas"
      :canvas-id="canvasId"
      :id="canvasId"
      class="background-canvas"
      type="2d"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
    ></canvas>
    <view v-if="!useCanvas || stars.length > 0" class="css-stars">
      <view
        v-for="(star, index) in stars"
        :key="index"
        class="star"
        :style="star.style"
      ></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useThemeStore } from '@/store/modules/theme'
import { createStarfield, ParticleSystem } from '@/utils/animations'

interface Props {
  useCanvas?: boolean
  starCount?: number
  interactive?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  useCanvas: true,
  starCount: 100,
  interactive: true
})

const themeStore = useThemeStore()
const canvasId = `galaxy-canvas-${Date.now()}`
const stars = ref<Array<{ style: Record<string, string> }>>([])
let particleSystem: ParticleSystem | null = null
let canvas: any = null
let ctx: any = null
let animationId: number | null = null
let touchX = 0
let touchY = 0

// 创建CSS星星（备用方案，用于不支持Canvas的环境）
const createCSSStars = () => {
  const starList = []
  for (let i = 0; i < props.starCount; i++) {
    const x = Math.random() * 100
    const y = Math.random() * 100
    const size = Math.random() * 3 + 1
    const delay = Math.random() * 3
    const duration = 2 + Math.random() * 3

    starList.push({
      style: {
        left: `${x}%`,
        top: `${y}%`,
        width: `${size}px`,
        height: `${size}px`,
        animationDelay: `${delay}s`,
        animationDuration: `${duration}s`
      }
    })
  }
  stars.value = starList
}

// 初始化Canvas
const initCanvas = () => {
  // #ifdef MP-WEIXIN
  // 微信小程序使用Canvas 2D
  try {
    const query = uni.createSelectorQuery()
    query
      .select(`#${canvasId}`)
      .fields({ node: true, size: true })
      .exec((res) => {
        if (res && res[0] && res[0].node) {
          canvas = res[0].node
          ctx = canvas.getContext('2d')
          const dpr = uni.getSystemInfoSync().pixelRatio || 1
          canvas.width = res[0].width * dpr
          canvas.height = res[0].height * dpr
          if (ctx) {
            ctx.scale(dpr, dpr)
            particleSystem = createStarfield(canvas, {
              starCount: props.starCount,
              starColor: themeStore.colors.primary,
              speed: 0.3
            })
            particleSystem.start()
          }
        } else {
          // 如果Canvas 2D不支持，回退到CSS动画
          createCSSStars()
        }
      })
  } catch (error) {
    console.warn('Canvas初始化失败，使用CSS动画:', error)
    createCSSStars()
  }
  // #endif

  // #ifdef H5
  // H5环境
  try {
    const canvasEl = document.getElementById(canvasId) as HTMLCanvasElement
    if (canvasEl) {
      canvas = canvasEl
      ctx = canvas.getContext('2d')
      const resizeCanvas = () => {
        if (canvas) {
          canvas.width = canvas.offsetWidth
          canvas.height = canvas.offsetHeight
        }
      }
      resizeCanvas()
      window.addEventListener('resize', resizeCanvas)

      if (ctx) {
        particleSystem = createStarfield(canvas, {
          starCount: props.starCount,
          starColor: themeStore.colors.primary,
          speed: 0.3
        })
        particleSystem.start()
      }
    }
  } catch (error) {
    console.warn('Canvas初始化失败，使用CSS动画:', error)
    createCSSStars()
  }
  // #endif

  // #ifndef MP-WEIXIN || H5
  // 其他平台使用CSS动画
  createCSSStars()
  // #endif
}

// 触摸事件处理（交互式粒子效果）
const handleTouchStart = (e: any) => {
  if (!props.interactive || !particleSystem) return
  const touch = e.touches[0]
  touchX = touch.x
  touchY = touch.y
  createInteractiveParticles(touchX, touchY)
}

const handleTouchMove = (e: any) => {
  if (!props.interactive || !particleSystem) return
  const touch = e.touches[0]
  touchX = touch.x
  touchY = touch.y
  createInteractiveParticles(touchX, touchY)
}

const handleTouchEnd = () => {
  // 触摸结束
}

// 创建交互式粒子
const createInteractiveParticles = (x: number, y: number) => {
  if (!particleSystem) return

  // 创建少量粒子从触摸点扩散
  for (let i = 0; i < 5; i++) {
    const angle = (Math.PI * 2 * i) / 5 + Math.random() * 0.5
    particleSystem.createParticle(x, y, {
      color: themeStore.colors.primary,
      radius: 2,
      speed: 2 + Math.random() * 2,
      life: 20 + Math.random() * 20,
      angle
    })
  }
}

onMounted(() => {
  if (props.useCanvas) {
    // 延迟初始化，确保DOM已渲染
    setTimeout(() => {
      initCanvas()
    }, 100)
  } else {
    createCSSStars()
  }
})

onUnmounted(() => {
  if (particleSystem) {
    particleSystem.stop()
    particleSystem.clear()
  }
  if (animationId !== null) {
    cancelAnimationFrame(animationId)
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/theme.scss';

.animated-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.background-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

.css-stars {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.star {
  position: absolute;
  background: var(--galaxy-star-color);
  border-radius: 50%;
  animation: twinkle 3s ease-in-out infinite;
  box-shadow: 0 0 6rpx var(--galaxy-star-color);
}

@keyframes twinkle {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}
</style>
