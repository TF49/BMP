<template>
  <div class="skeleton-loader">
    <!-- 卡片骨架 -->
    <div v-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-header">
        <div class="skeleton-avatar skeleton-shimmer"></div>
        <div class="skeleton-header-content">
          <div class="skeleton-title skeleton-shimmer"></div>
          <div class="skeleton-subtitle skeleton-shimmer"></div>
        </div>
      </div>
      <div class="skeleton-body">
        <div class="skeleton-line skeleton-shimmer" v-for="i in lines" :key="i" :style="{ width: getLineWidth(i) }"></div>
      </div>
    </div>

    <!-- 列表骨架 -->
    <div v-else-if="type === 'list'" class="skeleton-list">
      <div class="skeleton-list-item" v-for="i in rows" :key="i">
        <div class="skeleton-icon skeleton-shimmer"></div>
        <div class="skeleton-item-content">
          <div class="skeleton-title skeleton-shimmer"></div>
          <div class="skeleton-subtitle skeleton-shimmer"></div>
        </div>
      </div>
    </div>

    <!-- 表格骨架 -->
    <div v-else-if="type === 'table'" class="skeleton-table">
      <div class="skeleton-table-row" v-for="i in rows" :key="i">
        <div class="skeleton-table-cell skeleton-shimmer" v-for="j in columns" :key="j"></div>
      </div>
    </div>

    <!-- 默认骨架 -->
    <div v-else class="skeleton-default">
      <div class="skeleton-line skeleton-shimmer" v-for="i in lines" :key="i" :style="{ width: getLineWidth(i) }"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'default', // card, list, table, default
    validator: (value) => ['card', 'list', 'table', 'default'].includes(value)
  },
  rows: {
    type: Number,
    default: 3
  },
  columns: {
    type: Number,
    default: 4
  },
  lines: {
    type: Number,
    default: 3
  }
})

const getLineWidth = (index) => {
  const widths = ['100%', '80%', '90%', '70%', '85%']
  return widths[index % widths.length]
}
</script>

<style scoped lang="scss">
.skeleton-loader {
  width: 100%;
}

.skeleton-shimmer {
  background: linear-gradient(
    90deg,
    #f0f0f0 25%,
    #e0e0e0 50%,
    #f0f0f0 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

// 卡片骨架
.skeleton-card {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.skeleton-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.skeleton-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.skeleton-header-content {
  flex: 1;
}

.skeleton-title {
  height: 16px;
  margin-bottom: 8px;
  border-radius: 4px;
}

.skeleton-subtitle {
  height: 12px;
  width: 60%;
  border-radius: 4px;
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-line {
  height: 14px;
  border-radius: 4px;
}

// 列表骨架
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.skeleton-list-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}

.skeleton-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.skeleton-item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

// 表格骨架
.skeleton-table {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-table-row {
  display: flex;
  gap: 12px;
}

.skeleton-table-cell {
  flex: 1;
  height: 40px;
  border-radius: 4px;
}

// 默认骨架
.skeleton-default {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
}
</style>
