<template>
  <div class="agent-composer">
    <el-input
      v-model="text"
      type="textarea"
      :rows="2"
      :autosize="{ minRows: 2, maxRows: 5 }"
      :maxlength="maxLength"
      :disabled="disabled"
      resize="none"
      :placeholder="placeholder"
      @keydown="onKeydown"
    />
    <div class="agent-composer__footer">
      <span class="agent-composer__count">{{ text.length }} / {{ maxLength }}</span>
      <el-button
        type="primary"
        :disabled="disabled || !canSend"
        :loading="loading"
        @click="onSend"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  // 发送中：禁用输入与按钮，避免并发请求
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  maxLength: {
    type: Number,
    default: 2000
  },
  placeholder: {
    type: String,
    default: '输入你的经营分析问题，Enter 发送，Shift+Enter 换行'
  }
})

const emit = defineEmits(['send'])

const text = ref('')

const canSend = computed(() => text.value.trim().length > 0 && !props.loading)

function onSend() {
  const value = text.value.trim()
  if (!value || props.loading || props.disabled) return
  emit('send', value)
  text.value = ''
}

function onKeydown(event) {
  // Enter 发送；Shift+Enter 换行
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    onSend()
  }
}

/** 供父组件在快捷提问时填充并直接发送。 */
function submitText(value) {
  const v = String(value || '').trim()
  if (!v || props.loading || props.disabled) return
  emit('send', v.slice(0, props.maxLength))
}

defineExpose({ submitText })
</script>

<style scoped>
.agent-composer {
  border-top: 1px solid var(--el-border-color-light, #e4e7ed);
  padding: 12px 16px;
  background: #fff;
}

.agent-composer__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.agent-composer__count {
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
}
</style>
