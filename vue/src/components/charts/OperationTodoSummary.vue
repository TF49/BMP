<template>
  <div class="glass-card todo-card" v-loading="loading">
    <div class="card-header">
      <span class="card-title">运营待办总览</span>
      <span class="card-subtitle">需要尽快处理的关键事项</span>
    </div>
    <div v-if="hasError" class="todo-error">
      <span>数据加载失败，请稍后重试</span>
    </div>
    <div v-else class="todo-grid">
      <div class="todo-item warning">
        <div class="todo-label">待确认预订</div>
        <div class="todo-value">{{ pendingBookings }}</div>
        <div class="todo-desc">尽快确认，避免资源浪费</div>
      </div>
      <div class="todo-item danger">
        <div class="todo-label">未付款订单</div>
        <div class="todo-value">{{ unpaidOrders }}</div>
        <div class="todo-desc">提醒用户完成支付</div>
      </div>
      <div class="todo-item info">
        <div class="todo-label">待处理退款/投诉</div>
        <div class="todo-value">{{ pendingIssues }}</div>
        <div class="todo-desc">保障用户体验与口碑</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOperationTodoStatistics } from '@/api/booking'

const pendingBookings = ref(0)
const unpaidOrders = ref(0)
const pendingIssues = ref(0)
const loading = ref(false)
const hasError = ref(false)

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchTodoStatistics = async () => {
  loading.value = true
  hasError.value = false
  try {
    const res = await getOperationTodoStatistics()
    if (res.code === 200 && res.data) {
      pendingBookings.value = parseNum(res.data.pendingBookings || res.data.pending)
      unpaidOrders.value = parseNum(res.data.unpaidOrders || res.data.unpaid)
      pendingIssues.value = parseNum(res.data.pendingIssues || res.data.issues)
    }
  } catch (e) {
    console.error('获取运营待办统计失败:', e)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTodoStatistics()
})
</script>

<style scoped>
.todo-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 16px;
}

.card-title {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
}

.card-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #94A3B8;
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.todo-item {
  border-radius: 14px;
  padding: 12px 12px 10px;
  color: #0F172A;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.todo-item.warning {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.12) 0%, rgba(252, 211, 77, 0.4) 100%);
}

.todo-item.danger {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(252, 165, 165, 0.5) 100%);
}

.todo-item.info {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.12) 0%, rgba(191, 219, 254, 0.6) 100%);
}

.todo-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  font-weight: 600;
}

.todo-value {
  font-family: 'Poppins', sans-serif;
  font-size: 26px;
  font-weight: 700;
}

.todo-desc {
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  color: #0F172A;
}

@media (max-width: 768px) {
  .todo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.todo-error {
  padding: 24px 16px;
  text-align: center;
  background: linear-gradient(135deg, #FEF2F2 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px dashed #FECACA;
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #B91C1C;
}
</style>

