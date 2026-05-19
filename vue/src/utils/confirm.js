import { h, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BrandConfirmContent from '@/components/common/BrandConfirmContent.vue'
import {
  buildPaymentCountdownOptions,
  getPaymentAutoCancelInfo
} from '@/composables/usePaymentAutoCancel'

const DEFAULT_OPTIONS = {
  autofocus: false,
  closeOnClickModal: false,
  closeOnPressEscape: true,
  distinguishCancelAndClose: false,
  confirmButtonText: '确认',
  cancelButtonText: '取消'
}

let paymentConfirmInstanceSeq = 0

function buildBrandConfirmProps(options) {
  const {
    message,
    detail = '',
    eyebrow = '',
    entityLabel = '',
    entityValue = '',
    tone = 'warning',
    paymentOrder = null,
    paymentAutoCancel = null
  } = options

  return {
    eyebrow,
    message,
    detail,
    entityLabel,
    entityValue,
    tone,
    paymentOrder,
    countdownState: paymentAutoCancel
  }
}

function getPaymentConfirmBoxEl(instanceId) {
  return document.querySelector(`.brand-confirm-box--payment-${instanceId}`)
}

function updatePaymentMessageBoxUi(paymentOrder, paymentAutoCancel, confirmButtonText, instanceId) {
  const boxEl = getPaymentConfirmBoxEl(instanceId)
  if (!boxEl) return

  const countdownOptions = buildPaymentCountdownOptions(paymentAutoCancel)
  const info = getPaymentAutoCancelInfo(paymentOrder, countdownOptions)
  const primaryBtn = boxEl.querySelector('.el-message-box__btns .el-button--primary')
  if (!primaryBtn) return
  const hasConfigFallback = Boolean(paymentAutoCancel?.configLoadError?.value)

  if (info.expired && !hasConfigFallback) {
    boxEl.classList.add('brand-confirm-box--payment-expired')
    primaryBtn.textContent = '已超时，请关闭'
    primaryBtn.disabled = true
    primaryBtn.classList.add('is-disabled')
    primaryBtn.setAttribute('aria-disabled', 'true')
    return
  }

  boxEl.classList.remove('brand-confirm-box--payment-expired')
  primaryBtn.textContent = confirmButtonText
  primaryBtn.disabled = false
  primaryBtn.classList.remove('is-disabled')
  primaryBtn.removeAttribute('aria-disabled')
}

function closePaymentMessageBoxOnExpire(paymentAutoCancel) {
  ElMessage.warning('订单已超时，系统正在自动取消，请稍后刷新')
  if (typeof paymentAutoCancel?.triggerRefreshOnExpire === 'function') {
    void paymentAutoCancel.triggerRefreshOnExpire()
  }
  ElMessageBox.close()
}

function createPaymentBeforeClose(paymentOrder, paymentAutoCancel) {
  return (action, _instance, done) => {
    if (action !== 'confirm') {
      done()
      return
    }
    if (paymentAutoCancel?.configLoadError?.value) {
      done()
      return
    }
    const info = getPaymentAutoCancelInfo(
      paymentOrder,
      buildPaymentCountdownOptions(paymentAutoCancel)
    )
    if (info.expired) {
      closePaymentMessageBoxOnExpire(paymentAutoCancel)
      return
    }
    done()
  }
}

export function openActionConfirm(options) {
  const {
    title = '确认操作',
    message,
    tone = 'warning',
    paymentOrder = null,
    paymentAutoCancel = null,
    type,
    customClass = '',
    confirmButtonText: confirmButtonTextOption,
    ...restOptions
  } = options

  const isPaymentConfirm = Boolean(paymentOrder && paymentAutoCancel)
  const brandProps = buildBrandConfirmProps(options)
  const confirmButtonText = confirmButtonTextOption ?? DEFAULT_OPTIONS.confirmButtonText
  const instanceId = isPaymentConfirm ? `p${++paymentConfirmInstanceSeq}` : ''

  const boxOptions = {
    ...DEFAULT_OPTIONS,
    type: type ?? (tone === 'danger' ? 'error' : tone),
    customClass: ['brand-confirm-box', `brand-confirm-box--${tone}`, customClass]
      .filter(Boolean)
      .concat(isPaymentConfirm ? ['brand-confirm-box--payment', `brand-confirm-box--payment-${instanceId}`] : [])
      .join(' '),
    confirmButtonText,
    ...restOptions
  }

  if (isPaymentConfirm) {
    boxOptions.beforeClose = createPaymentBeforeClose(paymentOrder, paymentAutoCancel)
  }

  const promise = ElMessageBox.confirm(
    isPaymentConfirm
      ? () => h(BrandConfirmContent, brandProps)
      : h(BrandConfirmContent, brandProps),
    title,
    boxOptions
  )

  if (isPaymentConfirm && paymentAutoCancel?.countdownNowMs) {
    let expiredHandled = false
    const stopWatch = watch(
      () => paymentAutoCancel.countdownNowMs.value,
      () => {
        void nextTick(() => {
          updatePaymentMessageBoxUi(paymentOrder, paymentAutoCancel, confirmButtonText, instanceId)
          if (paymentAutoCancel?.configLoadError?.value) {
            return
          }
          const info = getPaymentAutoCancelInfo(
            paymentOrder,
            buildPaymentCountdownOptions(paymentAutoCancel)
          )
          if (info.expired && !expiredHandled) {
            expiredHandled = true
            closePaymentMessageBoxOnExpire(paymentAutoCancel)
          }
        })
      },
      { immediate: true, flush: 'post' }
    )
    promise.finally(() => stopWatch())
  }

  return promise
}
