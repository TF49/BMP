<template>
  <div class="ambient-particle-field" aria-hidden="true">
    <div class="ambient-particle-field__glow ambient-particle-field__glow--blue"></div>
    <div class="ambient-particle-field__glow ambient-particle-field__glow--cyan"></div>
    <div class="ambient-particle-field__grid"></div>
    <span
      v-for="particle in particles"
      :key="particle.id"
      class="ambient-particle-field__particle"
      :style="particle.style"
    ></span>
  </div>
</template>

<script setup>
const particles = Array.from({ length: 18 }, (_, index) => {
  const left = 4 + ((index * 13) % 92)
  const top = 8 + ((index * 17) % 76)
  const size = 4 + (index % 4) * 2
  const delay = `${(index % 6) * 0.55}s`
  const duration = `${8 + (index % 5) * 1.8}s`

  return {
    id: `particle-${index}`,
    style: {
      left: `${left}%`,
      top: `${top}%`,
      width: `${size}px`,
      height: `${size}px`,
      animationDelay: delay,
      animationDuration: duration
    }
  }
})
</script>

<style scoped>
.ambient-particle-field {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.ambient-particle-field__glow {
  position: absolute;
  border-radius: 999px;
  filter: blur(48px);
  opacity: 0.45;
}

.ambient-particle-field__glow--blue {
  top: -38px;
  left: -10px;
  width: 220px;
  height: 140px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.24) 0%, rgba(59, 130, 246, 0) 75%);
}

.ambient-particle-field__glow--cyan {
  right: -24px;
  bottom: -50px;
  width: 220px;
  height: 160px;
  background: radial-gradient(circle, rgba(34, 211, 238, 0.2) 0%, rgba(34, 211, 238, 0) 72%);
}

.ambient-particle-field__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.56) 0%, rgba(0, 0, 0, 0.1) 100%);
}

.ambient-particle-field__particle {
  position: absolute;
  display: block;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.95) 0%, rgba(191, 219, 254, 0.65) 50%, rgba(191, 219, 254, 0) 100%);
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.18);
  animation: particleFloat ease-in-out infinite;
}

@keyframes particleFloat {
  0%,
  100% {
    transform: translate3d(0, 0, 0) scale(0.9);
    opacity: 0.15;
  }
  50% {
    transform: translate3d(0, -10px, 0) scale(1.15);
    opacity: 0.62;
  }
}

@media (prefers-reduced-motion: reduce) {
  .ambient-particle-field__particle,
  .ambient-particle-field__glow {
    animation: none;
  }
}
</style>
