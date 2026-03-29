<template>
  <div class="glass-card timeline-card">
    <div class="card-header">
      <span class="card-title">今日场地时间轴</span>
      <span class="card-subtitle">0-24点各场地占用情况</span>
    </div>
    <div class="timeline-legend">
      <span class="legend-block busy">已预订</span>
      <span class="legend-block free">空闲</span>
    </div>
    <div class="timeline-list" v-loading="loading">
      <template v-if="courts && courts.length > 0">
        <div
          v-for="court in courts"
          :key="court.id"
          class="timeline-row"
        >
          <div class="timeline-label">{{ court.name }}</div>
          <div class="timeline-bar">
            <div
              v-for="(slot, index) in (court.slots || [])"
              :key="index"
              class="timeline-slot"
              :class="slot.status"
              :style="{ flexGrow: slot.duration ?? 1 }"
            ></div>
          </div>
        </div>
      </template>
      <div v-else class="timeline-empty">
        <span class="timeline-empty-text">暂无今日场地安排</span>
      </div>
    </div>
    <div class="timeline-scale">
      <span>0:00</span>
      <span>6:00</span>
      <span>12:00</span>
      <span>18:00</span>
      <span>24:00</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCourtSchedule } from '@/api/court'
import { formatLocalDate } from '@/utils/localDate'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const courts = ref([])
const loading = ref(false)

const fetchTodaySchedule = async () => {
  loading.value = true
  try {
    const res = await getCourtSchedule({ date: formatLocalDate() })
    if (res.code === 200 && Array.isArray(res.data)) {
      courts.value = res.data.map(court => ({
        id: court.courtId || court.id,
        name: court.courtName || court.name || '未知场地',
        slots: court.timeSlots || court.slots || []
      }))
    } else {
      courts.value = []
    }
  } catch (e) {
    console.error('获取今日场地时间轴数据失败:', e)
    courts.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTodaySchedule()
})

useDashboardChartRefresh(() => fetchTodaySchedule())
</script>

<style scoped>
.timeline-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
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

.timeline-empty {
  padding: 32px 16px;
  text-align: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px dashed #E2E8F0;
}

.timeline-empty-text {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #94A3B8;
}

.timeline-legend {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  color: #64748B;
}

.legend-block {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.legend-block::before {
  content: '';
  width: 12px;
  height: 6px;
  border-radius: 999px;
}

.legend-block.busy::before {
  background: linear-gradient(135deg, #3B82F6, #6366F1);
}

.legend-block.free::before {
  background: #E5E7EB;
}

.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timeline-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.timeline-label {
  width: 40px;
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #64748B;
}

.timeline-bar {
  flex: 1;
  display: flex;
  height: 16px;
  border-radius: 999px;
  overflow: hidden;
  background: #E5E7EB;
}

.timeline-slot {
  height: 100%;
}

.timeline-slot.busy {
  background: linear-gradient(135deg, #3B82F6, #6366F1);
}

.timeline-slot.free {
  background: transparent;
}

.timeline-scale {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  font-family: 'Open Sans', sans-serif;
  font-size: 10px;
  color: #94A3B8;
}
</style>

