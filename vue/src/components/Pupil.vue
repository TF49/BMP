<template>
  <div
    ref="pupilRef"
    class="pupil"
    :style="pupilStyle"
  />
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    size?: number
    maxDistance?: number
    pupilColor?: string
    forceLookX?: number
    forceLookY?: number
  }>(),
  {
    size: 12,
    maxDistance: 5,
    pupilColor: 'black',
  }
)

const pupilRef = ref<HTMLElement | null>(null)
const mouseX = ref(0)
const mouseY = ref(0)
const pos = ref({ x: 0, y: 0 })
let rafId: number | null = null

function calcPosition() {
  if (!pupilRef.value) return { x: 0, y: 0 }
  if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
    return { x: props.forceLookX, y: props.forceLookY }
  }
  const rect = pupilRef.value.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy
  const dist = Math.min(Math.sqrt(dx * dx + dy * dy), props.maxDistance ?? 5)
  const angle = Math.atan2(dy, dx)
  return {
    x: Math.cos(angle) * dist,
    y: Math.sin(angle) * dist,
  }
}

function update() {
  if (rafId != null) return
  rafId = requestAnimationFrame(() => {
    pos.value = calcPosition()
    rafId = null
  })
}

const pupilStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
  backgroundColor: props.pupilColor,
  transform: `translate(${pos.value.x}px, ${pos.value.y}px)`,
}))

watch([() => props.forceLookX, () => props.forceLookY], () => {
  pos.value = calcPosition()
})

onMounted(() => {
  const handle = (e: MouseEvent) => {
    mouseX.value = e.clientX
    mouseY.value = e.clientY
    update()
  }
  window.addEventListener('mousemove', handle)
  onBeforeUnmount(() => {
    window.removeEventListener('mousemove', handle)
    if (rafId) cancelAnimationFrame(rafId)
  })
})
</script>

<style scoped>
.pupil {
  border-radius: 9999px;
  transition: transform 0.1s ease-out;
}
</style>
