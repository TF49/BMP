<template>
  <div class="brand-confirm-content" :class="`brand-confirm-content--${tone}`">
    <div class="brand-confirm-content__icon-wrap">
      <div class="brand-confirm-content__icon">
        <el-icon>
          <component :is="iconComponent" />
        </el-icon>
      </div>
      <div class="brand-confirm-content__icon-halo" aria-hidden="true"></div>
    </div>
    <div class="brand-confirm-content__body">
      <p v-if="eyebrow" class="brand-confirm-content__eyebrow">{{ eyebrow }}</p>
      <p class="brand-confirm-content__message">{{ message }}</p>
      <div v-if="entityValue" class="brand-confirm-content__entity">
        <span v-if="entityLabel" class="brand-confirm-content__entity-label">{{ entityLabel }}</span>
        <span class="brand-confirm-content__entity-value">{{ entityValue }}</span>
      </div>
      <p v-if="detail" class="brand-confirm-content__detail">{{ detail }}</p>
      <PaymentPayCountdown
        v-if="paymentOrder"
        :order="paymentOrder"
        :countdown-state="countdownState"
        class="brand-confirm-content__countdown"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import PaymentPayCountdown from '@/components/payment/PaymentPayCountdown.vue'
import {
  CircleCheckFilled,
  DeleteFilled,
  InfoFilled,
  WarningFilled
} from '@element-plus/icons-vue'

const props = defineProps({
  eyebrow: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    required: true
  },
  detail: {
    type: String,
    default: ''
  },
  entityLabel: {
    type: String,
    default: ''
  },
  entityValue: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'warning'
  },
  paymentOrder: {
    type: Object,
    default: null
  },
  countdownState: {
    type: Object,
    default: null
  }
})

const iconComponent = computed(() => {
  switch (props.tone) {
    case 'danger':
      return DeleteFilled
    case 'success':
      return CircleCheckFilled
    case 'info':
      return InfoFilled
    case 'warning':
    default:
      return WarningFilled
  }
})

</script>

<style scoped>
.brand-confirm-content__countdown {
  margin-top: 12px;
}
</style>
