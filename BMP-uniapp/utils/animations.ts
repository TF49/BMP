/**
 * 动画工具函数库
 * 提供常用的动画效果和粒子系统工具
 */

/**
 * 缓动函数
 */
export const easing = {
  linear: (t: number) => t,
  easeIn: (t: number) => t * t,
  easeOut: (t: number) => t * (2 - t),
  easeInOut: (t: number) => t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t,
  bounce: (t: number) => {
    if (t < 1 / 2.75) {
      return 7.5625 * t * t
    } else if (t < 2 / 2.75) {
      return 7.5625 * (t -= 1.5 / 2.75) * t + 0.75
    } else if (t < 2.5 / 2.75) {
      return 7.5625 * (t -= 2.25 / 2.75) * t + 0.9375
    } else {
      return 7.5625 * (t -= 2.625 / 2.75) * t + 0.984375
    }
  },
  elastic: (t: number) => {
    return t === 0 || t === 1
      ? t
      : -Math.pow(2, 10 * (t - 1)) * Math.sin((t - 1.1) * 5 * Math.PI)
  }
}

/**
 * 动画配置接口
 */
export interface AnimationConfig {
  duration?: number
  delay?: number
  easing?: (t: number) => number
  onComplete?: () => void
  onUpdate?: (progress: number) => void
}

/**
 * 创建动画函数
 */
export function animate(
  from: number,
  to: number,
  config: AnimationConfig = {}
): Promise<void> {
  const {
    duration = 300,
    delay = 0,
    easing: easingFn = easing.easeInOut,
    onComplete,
    onUpdate
  } = config

  return new Promise((resolve) => {
    const startTime = Date.now() + delay
    let animationFrameId: number

    const update = () => {
      const currentTime = Date.now()
      const elapsed = currentTime - startTime

      if (elapsed < 0) {
        animationFrameId = requestAnimationFrame(update)
        return
      }

      const progress = Math.min(elapsed / duration, 1)
      const easedProgress = easingFn(progress)
      const current = from + (to - from) * easedProgress

      if (onUpdate) {
        onUpdate(current)
      }

      if (progress < 1) {
        animationFrameId = requestAnimationFrame(update)
      } else {
        if (onComplete) {
          onComplete()
        }
        resolve()
      }
    }

    animationFrameId = requestAnimationFrame(update)
  })
}

/**
 * 粒子类
 */
export class Particle {
  x: number
  y: number
  vx: number
  vy: number
  radius: number
  color: string
  alpha: number
  life: number
  maxLife: number

  constructor(
    x: number,
    y: number,
    vx: number,
    vy: number,
    radius: number,
    color: string,
    life: number
  ) {
    this.x = x
    this.y = y
    this.vx = vx
    this.vy = vy
    this.radius = radius
    this.color = color
    this.alpha = 1
    this.life = life
    this.maxLife = life
  }

  update() {
    this.x += this.vx
    this.y += this.vy
    this.life--
    this.alpha = this.life / this.maxLife
  }

  draw(ctx: CanvasRenderingContext2D) {
    ctx.save()
    ctx.globalAlpha = this.alpha
    ctx.fillStyle = this.color
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2)
    ctx.fill()
    ctx.restore()
  }

  isDead(): boolean {
    return this.life <= 0
  }
}

/**
 * 粒子系统类
 */
export class ParticleSystem {
  particles: Particle[] = []
  canvas: HTMLCanvasElement | null = null
  ctx: CanvasRenderingContext2D | null = null
  animationId: number | null = null
  isRunning = false

  constructor(canvas: HTMLCanvasElement) {
    this.canvas = canvas
    this.ctx = canvas.getContext('2d')
    if (this.ctx) {
      this.ctx.imageSmoothingEnabled = true
    }
  }

  /**
   * 创建粒子
   */
  createParticle(
    x: number,
    y: number,
    config: {
      color?: string
      radius?: number
      speed?: number
      life?: number
      angle?: number
    } = {}
  ) {
    const {
      color = '#ffffff',
      radius = 2,
      speed = 1,
      life = 100,
      angle = Math.random() * Math.PI * 2
    } = config

    const vx = Math.cos(angle) * speed
    const vy = Math.sin(angle) * speed

    const particle = new Particle(x, y, vx, vy, radius, color, life)
    this.particles.push(particle)
  }

  /**
   * 创建爆炸效果
   */
  createExplosion(
    x: number,
    y: number,
    count: number = 20,
    color: string = '#3cc51f'
  ) {
    for (let i = 0; i < count; i++) {
      const angle = (Math.PI * 2 * i) / count + Math.random() * 0.5
      const speed = 1 + Math.random() * 2
      this.createParticle(x, y, {
        color,
        radius: 2 + Math.random() * 2,
        speed,
        life: 30 + Math.random() * 30,
        angle
      })
    }
  }

  /**
   * 更新所有粒子
   */
  update() {
    for (let i = this.particles.length - 1; i >= 0; i--) {
      const particle = this.particles[i]
      particle.update()

      if (particle.isDead()) {
        this.particles.splice(i, 1)
      }
    }
  }

  /**
   * 绘制所有粒子
   */
  draw() {
    if (!this.ctx || !this.canvas) return

    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)

    for (const particle of this.particles) {
      particle.draw(this.ctx)
    }
  }

  /**
   * 动画循环
   */
  animate() {
    if (!this.isRunning) return

    this.update()
    this.draw()
    this.animationId = requestAnimationFrame(() => this.animate())
  }

  /**
   * 启动粒子系统
   */
  start() {
    if (this.isRunning) return
    this.isRunning = true
    this.animate()
  }

  /**
   * 停止粒子系统
   */
  stop() {
    this.isRunning = false
    if (this.animationId !== null) {
      cancelAnimationFrame(this.animationId)
      this.animationId = null
    }
  }

  /**
   * 清除所有粒子
   */
  clear() {
    this.particles = []
    if (this.ctx && this.canvas) {
      this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)
    }
  }
}

/**
 * 创建星空粒子系统
 */
export function createStarfield(
  canvas: HTMLCanvasElement,
  config: {
    starCount?: number
    starColor?: string
    speed?: number
    twinkleSpeed?: number
  } = {}
): ParticleSystem {
  const {
    starCount = 100,
    starColor = '#ffffff',
    speed = 0.5,
    twinkleSpeed = 0.02
  } = config

  const system = new ParticleSystem(canvas)
  const width = canvas.width
  const height = canvas.height

  // 创建星星
  for (let i = 0; i < starCount; i++) {
    const x = Math.random() * width
    const y = Math.random() * height
    const radius = Math.random() * 2 + 0.5
    const angle = Math.random() * Math.PI * 2

    const star = new Particle(
      x,
      y,
      Math.cos(angle) * speed,
      Math.sin(angle) * speed,
      radius,
      starColor,
      Infinity // 星星不消失
    )

    // 添加闪烁效果
    let twinklePhase = Math.random() * Math.PI * 2
    const originalAlpha = 0.3 + Math.random() * 0.7

    const originalUpdate = star.update.bind(star)
    star.update = function () {
      originalUpdate()
      twinklePhase += twinkleSpeed
      this.alpha = originalAlpha + Math.sin(twinklePhase) * 0.3
    }

    system.particles.push(star)
  }

  return system
}

/**
 * 震动动画
 */
export function shake(
  element: HTMLElement | null,
  intensity: number = 10,
  duration: number = 500
): Promise<void> {
  if (!element) return Promise.resolve()

  const originalTransform = element.style.transform
  const startTime = Date.now()

  return new Promise((resolve) => {
    const animate = () => {
      const elapsed = Date.now() - startTime
      const progress = elapsed / duration

      if (progress < 1) {
        const shakeX = (Math.random() - 0.5) * intensity * (1 - progress)
        const shakeY = (Math.random() - 0.5) * intensity * (1 - progress)
        element.style.transform = `translate(${shakeX}px, ${shakeY}px)`
        requestAnimationFrame(animate)
      } else {
        element.style.transform = originalTransform
        resolve()
      }
    }

    animate()
  })
}

/**
 * 波纹效果
 */
export function createRipple(
  x: number,
  y: number,
  element: HTMLElement
): void {
  const ripple = document.createElement('div')
  ripple.style.position = 'absolute'
  ripple.style.borderRadius = '50%'
  ripple.style.background = 'rgba(255, 255, 255, 0.6)'
  ripple.style.transform = 'translate(-50%, -50%) scale(0)'
  ripple.style.pointerEvents = 'none'
  ripple.style.left = `${x}px`
  ripple.style.top = `${y}px`
  ripple.style.width = '20px'
  ripple.style.height = '20px'
  ripple.style.transition = 'transform 0.6s ease-out, opacity 0.6s ease-out'

  element.style.position = 'relative'
  element.style.overflow = 'hidden'
  element.appendChild(ripple)

  requestAnimationFrame(() => {
    ripple.style.transform = 'translate(-50%, -50%) scale(10)'
    ripple.style.opacity = '0'
  })

  setTimeout(() => {
    ripple.remove()
  }, 600)
}
