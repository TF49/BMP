/**
 * 页面过渡动画
 */

// 淡入淡出动画
export const fadeTransition = {
  enterActiveClass: 'fade-enter-active',
  leaveActiveClass: 'fade-leave-active',
  enterFromClass: 'fade-enter-from',
  leaveToClass: 'fade-leave-to'
}

// 滑动动画
export const slideTransition = {
  enterActiveClass: 'slide-enter-active',
  leaveActiveClass: 'slide-leave-active',
  enterFromClass: 'slide-enter-from',
  leaveToClass: 'slide-leave-to'
}

// 缩放动画
export const scaleTransition = {
  enterActiveClass: 'scale-enter-active',
  leaveActiveClass: 'scale-leave-active',
  enterFromClass: 'scale-enter-from',
  leaveToClass: 'scale-leave-to'
}

// 页面进入动画
export const pageEnterAnimation = {
  duration: 300,
  timingFunction: 'ease-out',
  delay: 0
}

// 页面离开动画
export const pageLeaveAnimation = {
  duration: 200,
  timingFunction: 'ease-in',
  delay: 0
}
