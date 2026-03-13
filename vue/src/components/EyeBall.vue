<template>
  <div
    ref="eyeRef"
    class="eye-ball"
    :style="eyeStyle"
  >
    <div
      v-if="!isBlinking"
      class="eye-pupil"
      :style="pupilStyle"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    size?: number
    pupilSize?: number
    maxDistance?: number
    eyeColor?: string
    pupilColor?: string
    isBlinking?: boolean
    forceLookX?: number
    forceLookY?: number
  }>(),
  {
    size: 48,
    pupilSize: 16,
    maxDistance: 10,
    eyeColor: 'white',
    pupilColor: 'black',
    isBlinking: false,
  }
)

const eyeRef = ref<HTMLElement | null>(null)
const mouseX = ref(0)
const mouseY = ref(0)
const pupilPos = ref({ x: 0, y: 0 })
let rafId: number | null = null

function calcPupilPosition() {
  if (!eyeRef.value) return { x: 0, y: 0 }
  if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
    return { x: props.forceLookX, y: props.forceLookY }
  }
  const rect = eyeRef.value.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy
  const dist = Math.min(Math.sqrt(dx * dx + dy * dy), props.maxDistance ?? 10)
  const angle = Math.atan2(dy, dx)
  return {
    x: Math.cos(angle) * dist,
    y: Math.sin(angle) * dist,
  }
}

function updatePupil() {
  if (rafId != null) return
  rafId = requestAnimationFrame(() => {
    pupilPos.value = calcPupilPosition()
    rafId = null
  })
}

const eyeStyle = computed(() => ({
  width: `${props.size}px`,
  height: props.isBlinking ? '2px' : `${props.size}px`,
  backgroundColor: props.eyeColor,
}))

const pupilStyle = computed(() => ({
  width: `${props.pupilSize}px`,
  height: `${props.pupilSize}px`,
  backgroundColor: props.pupilColor,
  transform: `translate(${pupilPos.value.x}px, ${pupilPos.value.y}px)`,
}))

watch([() => props.forceLookX, () => props.forceLookY], () => {
  pupilPos.value = calcPupilPosition()
})

onMounted(() => {
  const handle = (e: MouseEvent) => {
    mouseX.value = e.clientX
    mouseY.value = e.clientY
    updatePupil()
  }
  window.addEventListener('mousemove', handle)
  onBeforeUnmount(() => {
    window.removeEventListener('mousemove', handle)
    if (rafId) cancelAnimationFrame(rafId)
  })
})
</script>

<style scoped>
.eye-ball {
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.15s ease;
}

.eye-pupil {
  border-radius: 9999px;
  transition: transform 0.1s ease-out;
}
</style>
