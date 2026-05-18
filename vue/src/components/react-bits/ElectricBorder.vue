<template>
  <div
    ref="containerRef"
    class="rb-electric-border"
    :class="className"
    :style="containerStyle"
  >
    <div class="rb-eb-canvas-container" aria-hidden="true">
      <canvas ref="canvasRef" class="rb-eb-canvas" />
    </div>
    <div class="rb-eb-layers" aria-hidden="true">
      <div class="rb-eb-glow-1" />
      <div class="rb-eb-glow-2" />
      <div class="rb-eb-background-glow" />
    </div>
    <div class="rb-eb-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  color: { type: String, default: '#f97316' },
  speed: { type: Number, default: 1 },
  chaos: { type: Number, default: 0.12 },
  borderRadius: { type: Number, default: 12 },
  className: { type: String, default: '' }
})

const canvasRef = ref(null)
const containerRef = ref(null)
let animationId = null
let resizeObserver = null
let time = 0
let lastFrameTime = 0

const containerStyle = computed(() => ({
  '--rb-electric-color': props.color,
  borderRadius: `${props.borderRadius}px`
}))

function random(x) {
  return (Math.sin(x * 12.9898) * 43758.5453) % 1
}

function noise2D(x, y) {
  const i = Math.floor(x)
  const j = Math.floor(y)
  const fx = x - i
  const fy = y - j
  const a = random(i + j * 57)
  const b = random(i + 1 + j * 57)
  const c = random(i + (j + 1) * 57)
  const d = random(i + 1 + (j + 1) * 57)
  const ux = fx * fx * (3 - 2 * fx)
  const uy = fy * fy * (3 - 2 * fy)
  return a * (1 - ux) * (1 - uy) + b * ux * (1 - uy) + c * (1 - ux) * uy + d * ux * uy
}

function octavedNoise(x, octaves, lacunarity, gain, amplitude, frequency, t, seed, baseFlatness) {
  let y = 0
  let amp = amplitude
  let freq = frequency
  for (let i = 0; i < octaves; i++) {
    let octaveAmp = amp
    if (i === 0) octaveAmp *= baseFlatness
    y += octaveAmp * noise2D(freq * x + seed * 100, t * freq * 0.3)
    freq *= lacunarity
    amp *= gain
  }
  return y
}

function getCornerPoint(cx, cy, radius, startAngle, arcLength, progress) {
  const angle = startAngle + progress * arcLength
  return { x: cx + radius * Math.cos(angle), y: cy + radius * Math.sin(angle) }
}

function getRoundedRectPoint(t, left, top, width, height, radius) {
  const straightWidth = width - 2 * radius
  const straightHeight = height - 2 * radius
  const cornerArc = (Math.PI * radius) / 2
  const totalPerimeter = 2 * straightWidth + 2 * straightHeight + 4 * cornerArc
  const distance = t * totalPerimeter
  let accumulated = 0

  if (distance <= accumulated + straightWidth) {
    const progress = (distance - accumulated) / straightWidth
    return { x: left + radius + progress * straightWidth, y: top }
  }
  accumulated += straightWidth

  if (distance <= accumulated + cornerArc) {
    const progress = (distance - accumulated) / cornerArc
    return getCornerPoint(left + width - radius, top + radius, radius, -Math.PI / 2, Math.PI / 2, progress)
  }
  accumulated += cornerArc

  if (distance <= accumulated + straightHeight) {
    const progress = (distance - accumulated) / straightHeight
    return { x: left + width, y: top + radius + progress * straightHeight }
  }
  accumulated += straightHeight

  if (distance <= accumulated + cornerArc) {
    const progress = (distance - accumulated) / cornerArc
    return getCornerPoint(left + width - radius, top + height - radius, radius, 0, Math.PI / 2, progress)
  }
  accumulated += cornerArc

  if (distance <= accumulated + straightWidth) {
    const progress = (distance - accumulated) / straightWidth
    return { x: left + width - radius - progress * straightWidth, y: top + height }
  }
  accumulated += straightWidth

  if (distance <= accumulated + cornerArc) {
    const progress = (distance - accumulated) / cornerArc
    return getCornerPoint(left + radius, top + height - radius, radius, Math.PI / 2, Math.PI / 2, progress)
  }
  accumulated += cornerArc

  if (distance <= accumulated + straightHeight) {
    const progress = (distance - accumulated) / straightHeight
    return { x: left, y: top + height - radius - progress * straightHeight }
  }
  accumulated += straightHeight

  const progress = (distance - accumulated) / cornerArc
  return getCornerPoint(left + radius, top + radius, radius, Math.PI, Math.PI / 2, progress)
}

function startAnimation() {
  const canvas = canvasRef.value
  const container = containerRef.value
  if (!canvas || !container) return

  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const octaves = 10
  const lacunarity = 1.6
  const gain = 0.7
  const amplitude = props.chaos
  const frequency = 10
  const baseFlatness = 0
  const displacement = 60
  const borderOffset = 60

  const updateSize = () => {
    const rect = container.getBoundingClientRect()
    const width = rect.width + borderOffset * 2
    const height = rect.height + borderOffset * 2
    const dpr = Math.min(window.devicePixelRatio || 1, 2)
    canvas.width = width * dpr
    canvas.height = height * dpr
    canvas.style.width = `${width}px`
    canvas.style.height = `${height}px`
    return { width, height, dpr }
  }

  let { width, height, dpr } = updateSize()

  const draw = (currentTime) => {
    if (!canvas || !ctx) return
    const delta = (currentTime - lastFrameTime) / 1000
    time += delta * props.speed
    lastFrameTime = currentTime

    dpr = Math.min(window.devicePixelRatio || 1, 2)
    ctx.setTransform(1, 0, 0, 1, 0, 0)
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.scale(dpr, dpr)
    ctx.strokeStyle = props.color
    ctx.lineWidth = 1
    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'

    const scale = displacement
    const left = borderOffset
    const top = borderOffset
    const borderWidth = width - 2 * borderOffset
    const borderHeight = height - 2 * borderOffset
    const maxRadius = Math.min(borderWidth, borderHeight) / 2
    const radius = Math.min(props.borderRadius, maxRadius)
    const approximatePerimeter = 2 * (borderWidth + borderHeight) + 2 * Math.PI * radius
    const sampleCount = Math.floor(approximatePerimeter / 2)

    ctx.beginPath()
    for (let i = 0; i <= sampleCount; i++) {
      const progress = i / sampleCount
      const point = getRoundedRectPoint(progress, left, top, borderWidth, borderHeight, radius)
      const xNoise = octavedNoise(progress * 8, octaves, lacunarity, gain, amplitude, frequency, time, 0, baseFlatness)
      const yNoise = octavedNoise(progress * 8, octaves, lacunarity, gain, amplitude, frequency, time, 1, baseFlatness)
      const x = point.x + xNoise * scale
      const y = point.y + yNoise * scale
      if (i === 0) ctx.moveTo(x, y)
      else ctx.lineTo(x, y)
    }
    ctx.closePath()
    ctx.stroke()

    animationId = requestAnimationFrame(draw)
  }

  resizeObserver = new ResizeObserver(() => {
    const size = updateSize()
    width = size.width
    height = size.height
  })
  resizeObserver.observe(container)
  lastFrameTime = performance.now()
  animationId = requestAnimationFrame(draw)
}

function stopAnimation() {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
}

onMounted(() => {
  startAnimation()
})

onBeforeUnmount(stopAnimation)

watch(() => [props.color, props.speed, props.chaos, props.borderRadius], () => {
  stopAnimation()
  startAnimation()
})
</script>

<style scoped>
.rb-electric-border {
  --rb-electric-light: color-mix(in srgb, var(--rb-electric-color) 75%, white);
  position: relative;
  border-radius: inherit;
  overflow: visible;
  isolation: isolate;
}

.rb-eb-canvas-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 2;
}

.rb-eb-canvas {
  display: block;
}

.rb-eb-content {
  position: relative;
  border-radius: inherit;
  z-index: 1;
}

.rb-eb-layers {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  z-index: 0;
}

.rb-eb-glow-1,
.rb-eb-glow-2,
.rb-eb-background-glow {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  box-sizing: border-box;
}

.rb-eb-glow-1 {
  border: 2px solid color-mix(in srgb, var(--rb-electric-color) 60%, transparent);
  filter: blur(1px);
}

.rb-eb-glow-2 {
  border: 2px solid var(--rb-electric-light);
  filter: blur(4px);
}

.rb-eb-background-glow {
  z-index: -1;
  transform: scale(1.08);
  filter: blur(24px);
  opacity: 0.35;
  background: linear-gradient(-30deg, var(--rb-electric-light), transparent, var(--rb-electric-color));
}
</style>
