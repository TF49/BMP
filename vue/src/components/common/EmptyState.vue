<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="iconSize">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="empty-text">
      <p class="empty-title">{{ title }}</p>
      <p v-if="description" class="empty-description">{{ description }}</p>
    </div>
    <div v-if="showAction && actionText" class="empty-action">
      <el-button type="primary" @click="$emit('action')">
        {{ actionText }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Box, Document, Search, DataLine } from '@element-plus/icons-vue'

const props = defineProps({
  type: {
    type: String,
    default: 'default', // default, search, data, document
    validator: (value) => ['default', 'search', 'data', 'document'].includes(value)
  },
  title: {
    type: String,
    default: '暂无数据'
  },
  description: {
    type: String,
    default: ''
  },
  actionText: {
    type: String,
    default: ''
  },
  showAction: {
    type: Boolean,
    default: false
  },
  iconSize: {
    type: Number,
    default: 80
  }
})

const icon = computed(() => {
  const iconMap = {
    default: Box,
    search: Search,
    data: DataLine,
    document: Document
  }
  return iconMap[props.type] || Box
})

defineEmits(['action'])
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  color: #c0c4cc;
  margin-bottom: 20px;
}

.empty-text {
  margin-bottom: 24px;
}

.empty-title {
  font-size: 16px;
  color: #909399;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.empty-description {
  font-size: 14px;
  color: #c0c4cc;
  margin: 0;
}

.empty-action {
  margin-top: 8px;
}
</style>
