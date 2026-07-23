<template>
  <div class="agent-action-confirm">
    <div v-if="title" class="agent-action-confirm__title">{{ title }}</div>
    <div v-if="description" class="agent-action-confirm__desc">{{ description }}</div>
    <div class="agent-action-confirm__buttons">
      <el-button type="primary" :loading="loading" :disabled="disabled" @click="onConfirm">
        {{ confirmText }}
      </el-button>
      <el-button :disabled="loading || disabled" @click="onReject">
        {{ rejectText }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  // 单个待执行动作：{ actionId, title, description, confirmText, rejectText, ... }
  action: {
    type: Object,
    required: true
  },
  // 该动作是否处理中（提交确认/拒绝时禁用）
  loading: {
    type: Boolean,
    default: false
  },
  // 是否已被处理（一次性消费后禁用）
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['confirm', 'reject'])

const title = computed(() => props.action?.title || '请确认以下操作')
const description = computed(() => props.action?.description || '')
const confirmText = computed(() => props.action?.confirmText || '确认')
const rejectText = computed(() => props.action?.rejectText || '取消')

function onConfirm() {
  emit('confirm', props.action)
}

function onReject() {
  emit('reject', props.action)
}
</script>

<style scoped>
.agent-action-confirm {
  margin-top: 12px;
  padding: 12px 14px;
  border: 1px solid var(--el-border-color-light, #e4e7ed);
  border-radius: 8px;
  background: var(--el-fill-color-lighter, #fafafa);
}

.agent-action-confirm__title {
  font-weight: 600;
  margin-bottom: 4px;
}

.agent-action-confirm__desc {
  color: var(--el-text-color-regular, #606266);
  font-size: 13px;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.agent-action-confirm__buttons {
  display: flex;
  gap: 8px;
}
</style>
