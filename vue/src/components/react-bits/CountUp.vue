<template>
  <span class="rb-count-up">{{ displayValue }}</span>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps({
  value: { type: [Number, String], default: 0 },
  duration: { type: Number, default: 320 },
  decimals: { type: Number, default: 0 },
  padLength: { type: Number, default: 0 }
})

const animatedValue = ref(0)
let frameId = null

const parseNumber = (value) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}

const formatValue = (value) => {
  const fixed = Number(value).toFixed(props.decimals)
  if (props.padLength <= 0) return fixed

  const [integerPart, decimalPart] = fixed.split('.')
  const paddedInteger = integerPart.padStart(props.padLength, '0')
  return decimalPart == null ? paddedInteger : `${paddedInteger}.${decimalPart}`
}

const stopAnimation = () => {
  if (frameId != null) {
    cancelAnimationFrame(frameId)
    frameId = null
  }
}

const animateTo = (nextValue) => {
  stopAnimation()

  const startValue = animatedValue.value
  const targetValue = parseNumber(nextValue)
  if (startValue === targetValue || props.duration <= 0) {
    animatedValue.value = targetValue
    return
  }

  const startTime = performance.now()
  const step = (now) => {
    const progress = Math.min(1, (now - startTime) / props.duration)
    const eased = 1 - Math.pow(1 - progress, 3)
    animatedValue.value = startValue + (targetValue - startValue) * eased

    if (progress < 1) {
      frameId = requestAnimationFrame(step)
    } else {
      animatedValue.value = targetValue
      frameId = null
    }
  }

  frameId = requestAnimationFrame(step)
}

watch(
  () => props.value,
  (nextValue) => {
    animateTo(nextValue)
  },
  { immediate: true }
)

onBeforeUnmount(stopAnimation)

const displayValue = computed(() => formatValue(animatedValue.value))
</script>

<style scoped>
.rb-count-up {
  display: inline-block;
  min-width: 1ch;
  font-variant-numeric: tabular-nums;
}
</style>
