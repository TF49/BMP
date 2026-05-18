<template>
  <span
    class="rb-shiny-text"
    :class="[{ 'rb-shiny-text--disabled': disabled }, className]"
    :style="rootStyle"
  >
    <slot>{{ text }}</slot>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  text: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  speed: { type: Number, default: 2 },
  className: { type: String, default: '' },
  color: { type: String, default: '#b45309' },
  shineColor: { type: String, default: '#ffffff' },
  spread: { type: Number, default: 120 }
})

const rootStyle = computed(() => ({
  '--rb-shiny-color': props.color,
  '--rb-shiny-shine': props.shineColor,
  '--rb-shiny-spread': `${props.spread}deg`,
  '--rb-shiny-duration': `${props.speed}s`
}))
</script>

<style scoped>
@media (prefers-reduced-motion: reduce) {
  .rb-shiny-text {
    animation: none;
    -webkit-text-fill-color: currentColor;
    background: none;
    color: var(--rb-shiny-color);
  }
}

.rb-shiny-text {
  display: inline-block;
  background-image: linear-gradient(
    var(--rb-shiny-spread),
    var(--rb-shiny-color) 0%,
    var(--rb-shiny-color) 35%,
    var(--rb-shiny-shine) 50%,
    var(--rb-shiny-color) 65%,
    var(--rb-shiny-color) 100%
  );
  background-size: 200% auto;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: rb-shiny-sweep var(--rb-shiny-duration) linear infinite;
}

.rb-shiny-text--disabled {
  animation: none;
  -webkit-text-fill-color: currentColor;
  background: none;
  color: var(--rb-shiny-color);
}

@keyframes rb-shiny-sweep {
  to {
    background-position: -200% center;
  }
}
</style>
