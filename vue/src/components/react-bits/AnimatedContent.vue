<template>
  <Transition name="rb-animated-content" appear>
    <div
      v-if="show"
      class="rb-animated-content"
      :style="rootStyle"
    >
      <slot />
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: true },
  duration: { type: Number, default: 240 },
  distance: { type: Number, default: 10 },
  scaleFrom: { type: Number, default: 0.98 }
})

const rootStyle = computed(() => ({
  '--rb-animated-duration': `${props.duration}ms`,
  '--rb-animated-distance': `${props.distance}px`,
  '--rb-animated-scale-from': props.scaleFrom
}))
</script>

<style scoped>
.rb-animated-content {
  width: 100%;
}

.rb-animated-content-enter-active,
.rb-animated-content-leave-active {
  transition:
    opacity var(--rb-animated-duration) ease,
    transform var(--rb-animated-duration) ease;
}

.rb-animated-content-enter-from,
.rb-animated-content-leave-to {
  opacity: 0;
  transform: translateY(var(--rb-animated-distance)) scale(var(--rb-animated-scale-from));
}
</style>
