<template>
  <div class="glass-card inventory-card" v-loading="loading">
    <div class="card-header">
      <span class="card-title">库存预警</span>
      <span class="card-subtitle">羽毛球、球拍等低于安全库存的物品</span>
    </div>
    <div class="inventory-list">
      <template v-if="items && items.length > 0">
        <div
          v-for="item in items"
          :key="item.id"
          class="inventory-item"
        >
          <div class="inventory-main">
            <div class="name-row">
              <span class="name">{{ item.name }}</span>
              <span class="category">{{ item.category }}</span>
            </div>
            <div class="meta-row">
              <span>剩余：{{ item.stock ?? 0 }}</span>
              <span>安全值：{{ item.threshold ?? 0 }}</span>
            </div>
          </div>
          <span
            class="status-badge"
            :class="(item.stock ?? 0) <= (item.threshold ?? 0) / 2 ? 'danger' : 'warning'"
          >
            {{ (item.stock ?? 0) <= (item.threshold ?? 0) / 2 ? '严重预警' : '库存偏低' }}
          </span>
        </div>
      </template>
      <div v-else class="inventory-empty">
        <span>暂无库存预警</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEquipmentLowStock } from '@/api/equipment'

const items = ref([])
const loading = ref(false)
const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchLowStockItems = async () => {
  loading.value = true
  try {
    const res = await getEquipmentLowStock()
    if (res.code === 200 && Array.isArray(res.data)) {
      items.value = res.data.map(item => ({
        id: item.equipmentId || item.id,
        name: item.equipmentName || item.name || '未知物品',
        category: item.category || item.type || '其他',
        stock: parseNum(item.stock || item.quantity),
        threshold: parseNum(item.threshold || item.safetyStock || 0)
      }))
    } else {
      items.value = []
    }
  } catch (e) {
    console.error('获取库存预警数据失败:', e)
    items.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLowStockItems()
})
</script>

<style scoped>
.inventory-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 14px;
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

.inventory-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.inventory-empty {
  padding: 24px 16px;
  text-align: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px dashed #E2E8F0;
  font-size: 13px;
  color: #94A3B8;
}

.inventory-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border: 1px solid #E2E8F0;
  transition: all 0.25s ease;
}

.inventory-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.08);
  border-color: rgba(251, 191, 36, 0.6);
}

.inventory-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.name {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.category {
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.08);
  color: #1D4ED8;
}

.meta-row {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #94A3B8;
  display: flex;
  gap: 10px;
}

.status-badge {
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999px;
  flex-shrink: 0;
}

.status-badge.warning {
  background: rgba(245, 158, 11, 0.12);
  color: #B45309;
}

.status-badge.danger {
  background: rgba(239, 68, 68, 0.12);
  color: #B91C1C;
}
</style>

