<template>
  <div class="chars-scene">
    <div class="chars-canvas" ref="canvasRef" :class="{ 'chars-error': hasLoginError }">
      <!-- Purple character - back layer -->
      <div
        ref="purpleRef"
        class="char char-purple"
        :style="purpleStyle"
      >
        <div class="char-eyes" :style="purpleEyesStyle">
          <EyeBall
            :size="18"
            :pupil-size="7"
            :max-distance="5"
            eye-color="white"
            pupil-color="#2D2D2D"
            :is-blinking="isPurpleBlinking"
            :force-look-x="purpleForceLookX"
            :force-look-y="purpleForceLookY"
          />
          <EyeBall
            :size="18"
            :pupil-size="7"
            :max-distance="5"
            eye-color="white"
            pupil-color="#2D2D2D"
            :is-blinking="isPurpleBlinking"
            :force-look-x="purpleForceLookX"
            :force-look-y="purpleForceLookY"
          />
        </div>
      </div>

      <!-- Black character - middle layer -->
      <div
        ref="blackRef"
        class="char char-black"
        :style="blackStyle"
      >
        <div class="char-eyes" :style="blackEyesStyle">
          <EyeBall
            :size="16"
            :pupil-size="6"
            :max-distance="4"
            eye-color="white"
            pupil-color="#2D2D2D"
            :is-blinking="isBlackBlinking"
            :force-look-x="blackForceLookX"
            :force-look-y="blackForceLookY"
          />
          <EyeBall
            :size="16"
            :pupil-size="6"
            :max-distance="4"
            eye-color="white"
            pupil-color="#2D2D2D"
            :is-blinking="isBlackBlinking"
            :force-look-x="blackForceLookX"
            :force-look-y="blackForceLookY"
          />
        </div>
      </div>

      <!-- Orange character - front left -->
      <div
        ref="orangeRef"
        class="char char-orange"
        :style="orangeStyle"
      >
        <div class="char-eyes char-eyes-pupils" :style="orangeEyesStyle">
          <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :force-look-x="orangeForceLookX" :force-look-y="orangeForceLookY" />
          <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :force-look-x="orangeForceLookX" :force-look-y="orangeForceLookY" />
        </div>
      </div>

      <!-- Yellow character - front right -->
      <div
        ref="yellowRef"
        class="char char-yellow"
        :style="yellowStyle"
      >
        <div class="char-eyes char-eyes-pupils" :style="yellowEyesStyle">
          <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :force-look-x="yellowForceLookX" :force-look-y="yellowForceLookY" />
          <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :force-look-x="yellowForceLookX" :force-look-y="yellowForceLookY" />
        </div>
        <div class="char-mouth" :style="yellowMouthStyle"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import EyeBall from './EyeBall.vue'
import Pupil from './Pupil.vue'

const props = defineProps<{
  isTyping?: boolean
  showPassword?: boolean
  passwordLength?: number
  hasLoginError?: boolean
}>()

const canvasRef = ref<HTMLElement | null>(null)
const purpleRef = ref<HTMLElement | null>(null)
const blackRef = ref<HTMLElement | null>(null)
const orangeRef = ref<HTMLElement | null>(null)
const yellowRef = ref<HTMLElement | null>(null)

const mouseX = ref(0)
const mouseY = ref(0)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isPurplePeeking = ref(false)
const isLookingAtEachOther = ref(false)
let lookAtTimer: number | null = null
let purplePeekTimer: number | null = null

const passwordVisibleAndHasPassword = computed(() => props.passwordLength > 0 && props.showPassword)
const typingOrPasswordHidden = computed(() => props.isTyping || (props.passwordLength > 0 && !props.showPassword))
const hasLoginError = computed(() => !!props.hasLoginError)

function calcPosition(refEl: HTMLElement | null) {
  if (!refEl) return { faceX: 0, faceY: 0, bodySkew: 0 }
  const rect = refEl.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3
  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY
  const faceX = Math.max(-15, Math.min(15, deltaX / 20))
  const faceY = Math.max(-10, Math.min(10, deltaY / 30))
  const bodySkew = Math.max(-6, Math.min(6, -deltaX / 120))
  return { faceX, faceY, bodySkew }
}

const purplePos = computed(() => calcPosition(purpleRef.value))
const blackPos = computed(() => calcPosition(blackRef.value))
const orangePos = computed(() => calcPosition(orangeRef.value))
const yellowPos = computed(() => calcPosition(yellowRef.value))

const purpleStyle = computed(() => {
  const h = typingOrPasswordHidden.value ? 440 : 400
  let transform = ''
  if (passwordVisibleAndHasPassword.value) {
    transform = 'skewX(0deg)'
  } else if (typingOrPasswordHidden.value) {
    transform = `skewX(${(purplePos.value.bodySkew || 0) - 12}deg) translateX(40px)`
  } else {
    transform = `skewX(${purplePos.value.bodySkew || 0}deg)`
  }
  return {
    height: `${h}px`,
    transform,
  }
})

const blackStyle = computed(() => {
  let transform = ''
  if (passwordVisibleAndHasPassword.value) {
    transform = 'skewX(0deg)'
  } else if (isLookingAtEachOther.value) {
    transform = `skewX(${(blackPos.value.bodySkew || 0) * 1.5 + 10}deg) translateX(20px)`
  } else if (typingOrPasswordHidden.value) {
    transform = `skewX(${(blackPos.value.bodySkew || 0) * 1.5}deg)`
  } else {
    transform = `skewX(${blackPos.value.bodySkew || 0}deg)`
  }
  return { transform }
})

const orangeStyle = computed(() => {
  const transform = passwordVisibleAndHasPassword.value ? 'skewX(0deg)' : `skewX(${orangePos.value.bodySkew || 0}deg)`
  return { transform }
})

const yellowStyle = computed(() => {
  const transform = passwordVisibleAndHasPassword.value ? 'skewX(0deg)' : `skewX(${yellowPos.value.bodySkew || 0}deg)`
  return { transform }
})

const purpleForceLookX = computed(() => {
  if (hasLoginError.value) return 0
  if (passwordVisibleAndHasPassword.value) return isPurplePeeking.value ? 4 : -4
  if (isLookingAtEachOther.value) return 3
  return undefined
})
const purpleForceLookY = computed(() => {
  if (hasLoginError.value) return 6
  if (passwordVisibleAndHasPassword.value) return isPurplePeeking.value ? 5 : -4
  if (isLookingAtEachOther.value) return 4
  return undefined
})

const blackForceLookX = computed(() => {
  if (hasLoginError.value) return 0
  if (passwordVisibleAndHasPassword.value) return -4
  if (isLookingAtEachOther.value) return 0
  return undefined
})
const blackForceLookY = computed(() => {
  if (hasLoginError.value) return 6
  if (passwordVisibleAndHasPassword.value) return -4
  if (isLookingAtEachOther.value) return -4
  return undefined
})

const orangeForceLookX = computed(() => {
  if (hasLoginError.value) return 0
  return passwordVisibleAndHasPassword.value ? -5 : undefined
})
const orangeForceLookY = computed(() => {
  if (hasLoginError.value) return 5
  return passwordVisibleAndHasPassword.value ? -4 : undefined
})
const yellowForceLookX = computed(() => {
  if (hasLoginError.value) return 0
  return passwordVisibleAndHasPassword.value ? -5 : undefined
})
const yellowForceLookY = computed(() => {
  if (hasLoginError.value) return 5
  return passwordVisibleAndHasPassword.value ? -4 : undefined
})

const purpleEyesStyle = computed(() => {
  let left = 45 + purplePos.value.faceX
  let top = 40 + purplePos.value.faceY
  if (passwordVisibleAndHasPassword.value) {
    left = 20
    top = 35
  } else if (isLookingAtEachOther.value) {
    left = 55
    top = 65
  }
  return { left: `${left}px`, top: `${top}px` }
})

const blackEyesStyle = computed(() => {
  let left = 26 + blackPos.value.faceX
  let top = 32 + blackPos.value.faceY
  if (passwordVisibleAndHasPassword.value) {
    left = 10
    top = 28
  } else if (isLookingAtEachOther.value) {
    left = 32
    top = 12
  }
  return { left: `${left}px`, top: `${top}px` }
})

const orangeEyesStyle = computed(() => {
  const left = passwordVisibleAndHasPassword.value ? 50 : 82 + (orangePos.value.faceX || 0)
  const top = passwordVisibleAndHasPassword.value ? 85 : 90 + (orangePos.value.faceY || 0)
  return { left: `${left}px`, top: `${top}px` }
})

const yellowEyesStyle = computed(() => {
  const left = passwordVisibleAndHasPassword.value ? 20 : 52 + (yellowPos.value.faceX || 0)
  const top = passwordVisibleAndHasPassword.value ? 35 : 40 + (yellowPos.value.faceY || 0)
  return { left: `${left}px`, top: `${top}px` }
})

const yellowMouthStyle = computed(() => {
  const left = passwordVisibleAndHasPassword.value ? 10 : 40 + (yellowPos.value.faceX || 0)
  const top = passwordVisibleAndHasPassword.value ? 88 : 88 + (yellowPos.value.faceY || 0)
  return {
    left: `${left}px`,
    top: `${top}px`,
    ...(hasLoginError.value ? { borderRadius: '0 0 9999px 9999px', height: '6px', top: '94px' } : {}),
  }
})

function scheduleBlink(setter: (v: boolean) => void, idRef: { current: number }) {
  idRef.current = window.setTimeout(() => {
    setter(true)
    setTimeout(() => {
      setter(false)
      scheduleBlink(setter, idRef)
    }, 150)
  }, 3000 + Math.random() * 4000)
}

function schedulePurplePeek() {
  if (!passwordVisibleAndHasPassword.value) return null
  const delay = 2000 + Math.random() * 3000
  return window.setTimeout(() => {
    isPurplePeeking.value = true
    setTimeout(() => {
      isPurplePeeking.value = false
    }, 800)
  }, delay)
}

const purpleBlinkIdRef = { current: 0 }
const blackBlinkIdRef = { current: 0 }
let mouseHandler: ((e: MouseEvent) => void) | null = null

onMounted(() => {
  mouseHandler = (e: MouseEvent) => {
    mouseX.value = e.clientX
    mouseY.value = e.clientY
  }
  window.addEventListener('mousemove', mouseHandler)

  scheduleBlink((v) => { isPurpleBlinking.value = v }, purpleBlinkIdRef)
  scheduleBlink((v) => { isBlackBlinking.value = v }, blackBlinkIdRef)

  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (!prefersReduced && passwordVisibleAndHasPassword.value) {
    purplePeekTimer = schedulePurplePeek()
  }
})

onBeforeUnmount(() => {
  if (mouseHandler) window.removeEventListener('mousemove', mouseHandler)
  clearTimeout(purpleBlinkIdRef.current)
  clearTimeout(blackBlinkIdRef.current)
  if (purplePeekTimer) clearTimeout(purplePeekTimer)
  if (lookAtTimer) clearTimeout(lookAtTimer)
})

watch(() => props.isTyping, (typing) => {
  if (typing) {
    isLookingAtEachOther.value = true
    if (lookAtTimer) clearTimeout(lookAtTimer)
    lookAtTimer = window.setTimeout(() => {
      isLookingAtEachOther.value = false
      lookAtTimer = null
    }, 800)
  } else {
    isLookingAtEachOther.value = false
  }
})

watch([() => props.passwordLength, () => props.showPassword], () => {
  if (purplePeekTimer) {
    clearTimeout(purplePeekTimer)
    purplePeekTimer = null
  }
  if (passwordVisibleAndHasPassword.value && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    purplePeekTimer = schedulePurplePeek()
  } else {
    isPurplePeeking.value = false
  }
})
</script>

<style scoped>
.chars-scene {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-height: 500px;
}

.chars-canvas {
  position: relative;
  width: 550px;
  height: 400px;
}

.chars-canvas.chars-error {
  animation: charsShake 0.5s ease-in-out;
}

@media (prefers-reduced-motion: reduce) {
  .chars-canvas.chars-error {
    animation: none;
  }
}

@keyframes charsShake {
  0%, 100% { transform: translateX(0); }
  15% { transform: translateX(-8px); }
  30% { transform: translateX(8px); }
  45% { transform: translateX(-6px); }
  60% { transform: translateX(6px); }
  75% { transform: translateX(-3px); }
  90% { transform: translateX(3px); }
}

.char {
  position: absolute;
  bottom: 0;
  transition: all 0.7s ease-in-out;
  transform-origin: bottom center;
}

.char-purple {
  left: 70px;
  width: 180px;
  background-color: #6C3FF5;
  border-radius: 10px 10px 0 0;
  z-index: 1;
}

.char-black {
  left: 240px;
  width: 120px;
  height: 310px;
  background-color: #2D2D2D;
  border-radius: 8px 8px 0 0;
  z-index: 2;
}

.char-orange {
  left: 0;
  width: 240px;
  height: 200px;
  background-color: #FF9B6B;
  border-radius: 120px 120px 0 0;
  z-index: 3;
}

.char-yellow {
  left: 310px;
  width: 140px;
  height: 230px;
  background-color: #E8D754;
  border-radius: 70px 70px 0 0;
  z-index: 4;
}

.char-eyes {
  position: absolute;
  display: flex;
  gap: 32px;
  transition: all 0.7s ease-in-out;
}

.char-black .char-eyes {
  gap: 24px;
}

.char-eyes-pupils {
  gap: 32px;
  transition: all 0.2s ease-out;
}

.char-yellow .char-eyes-pupils {
  gap: 24px;
}

.char-mouth {
  position: absolute;
  width: 80px;
  height: 4px;
  background-color: #2D2D2D;
  border-radius: 9999px;
  transition: all 0.2s ease-out;
}

@media (max-width: 1024px) {
  .chars-canvas {
    width: 420px;
    height: 320px;
  }

  .char-purple {
    left: 50px;
    width: 130px;
  }

  .char-black {
    left: 170px;
    width: 90px;
    height: 240px;
  }

  .char-orange {
    width: 180px;
    height: 150px;
    border-radius: 90px 90px 0 0;
  }

  .char-yellow {
    left: 230px;
    width: 110px;
    height: 180px;
    border-radius: 55px 55px 0 0;
  }
}
</style>
