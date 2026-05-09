import { h } from 'vue'
import { ElMessageBox } from 'element-plus'
import BrandConfirmContent from '@/components/common/BrandConfirmContent.vue'

const DEFAULT_OPTIONS = {
  autofocus: false,
  closeOnClickModal: false,
  closeOnPressEscape: true,
  distinguishCancelAndClose: false,
  confirmButtonText: '确认',
  cancelButtonText: '取消'
}

export function openActionConfirm(options) {
  const {
    title = '确认操作',
    message,
    detail = '',
    eyebrow = '',
    entityLabel = '',
    entityValue = '',
    tone = 'warning',
    type,
    customClass = '',
    ...restOptions
  } = options

  return ElMessageBox.confirm(
    h(BrandConfirmContent, {
      eyebrow,
      message,
      detail,
      entityLabel,
      entityValue,
      tone
    }),
    title,
    {
      ...DEFAULT_OPTIONS,
      type: type ?? (tone === 'danger' ? 'error' : tone),
      customClass: ['brand-confirm-box', `brand-confirm-box--${tone}`, customClass].filter(Boolean).join(' '),
      ...restOptions
    }
  )
}
