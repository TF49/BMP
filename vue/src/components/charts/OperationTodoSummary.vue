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
        <div class="todo-label">{{ pendingBookingsLabel }}</div>
        <div class="todo-value">{{ pendingBookings }}</div>
        <div class="todo-desc">{{ pendingBookingsDesc }}</div>
      </div>
      <div class="todo-item danger">
        <div class="todo-label">{{ unpaidOrdersLabel }}</div>
        <div class="todo-value">{{ unpaidOrders }}</div>
        <div class="todo-desc">{{ unpaidOrdersDesc }}</div>
      </div>
      <div class="todo-item info">
        <div class="todo-label">{{ pendingRefundsLabel }}</div>
        <div class="todo-value">{{ pendingRefunds }}</div>
        <div class="todo-desc">{{ pendingRefundsDesc }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOperationTodoStatistics } from '@/api/booking'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const pendingBookings = ref(0)
const unpaidOrders = ref(0)
const pendingRefunds = ref(0)
const pendingBookingsLabel = ref('待支付预约')
const pendingBookingsDesc = ref('待支付的场地预约')
const unpaidOrdersLabel = ref('未付款订单')
const unpaidOrdersDesc = ref('待完成支付的全部业务订单')
const pendingRefundsLabel = ref('待退款订单')
const pendingRefundsDesc = ref('退款申请处理中订单')
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
      pendingRefunds.value = parseNum(res.data.pendingRefunds ?? res.data.pendingIssues ?? res.data.issues)
      pendingBookingsLabel.value = res.data.pendingBookingsLabel || '待支付预约'
      pendingBookingsDesc.value = res.data.pendingBookingsDescription || '待支付的场地预约'
      unpaidOrdersLabel.value = res.data.unpaidOrdersLabel || '未付款订单'
      unpaidOrdersDesc.value = res.data.unpaidOrdersDescription || '待完成支付的全部业务订单'
      pendingRefundsLabel.value = res.data.pendingRefundsLabel || '待退款订单'
      pendingRefundsDesc.value = res.data.pendingRefundsDescription || '退款申请处理中订单'
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

useDashboardChartRefresh(() => fetchTodoStatistics())
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
