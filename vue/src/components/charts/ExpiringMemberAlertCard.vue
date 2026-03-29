<template>
  <div class="glass-card alert-card" v-loading="loading">
    <div class="card-header">
      <span class="card-title">即将到期会员</span>
      <span class="card-subtitle">未来30天内需要重点维护的会员</span>
    </div>
    <div class="alert-summary">
      <div class="summary-main">
        <div class="summary-value">{{ totalExpiring ?? 0 }}</div>
        <div class="summary-label">30天内即将到期</div>
      </div>
      <div class="summary-badges">
        <span class="badge high">7天内 {{ within7Days ?? 0 }} 人</span>
        <span class="badge medium">15天内 {{ within15Days ?? 0 }} 人</span>
      </div>
    </div>
    <div class="alert-list">
      <template v-if="expiringMembers && expiringMembers.length > 0">
        <div
          v-for="member in expiringMembers"
          :key="member.id"
          class="alert-item"
        >
          <div class="alert-avatar">
            <span>{{ (member.name || '会').charAt(0) }}</span>
          </div>
          <div class="alert-info">
            <div class="alert-name-row">
              <span class="alert-name">{{ member.name || '未知会员' }}</span>
              <span class="alert-level">{{ member.level || '—' }}</span>
            </div>
            <div class="alert-meta">
              <span>到期：{{ member.expireDate || '—' }}</span>
              <span>剩余：{{ (member.daysLeft ?? 0) }}天</span>
            </div>
          </div>
          <span
            class="alert-tag"
            :class="(member.daysLeft ?? 0) <= 7 ? 'high' : (member.daysLeft ?? 0) <= 15 ? 'medium' : 'low'"
          >
            {{ (member.daysLeft ?? 0) <= 7 ? '紧急' : (member.daysLeft ?? 0) <= 15 ? '提醒' : '关注' }}
          </span>
        </div>
      </template>
      <div v-else class="alert-list-empty">
        <span>暂无即将到期会员</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { getExpiringMembers } from '@/api/member'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const expiringMembers = ref([])
const loading = ref(false)

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const fetchExpiringMembers = async () => {
  loading.value = true
  try {
    const res = await getExpiringMembers({ days: 30 })
    if (res.code === 200 && Array.isArray(res.data)) {
      expiringMembers.value = res.data.map(item => ({
        id: item.memberId || item.id,
        name: item.memberName || item.name || '未知会员',
        level: item.memberLevel || item.level || '普通会员',
        daysLeft: parseNum(item.daysLeft || item.remainingDays),
        expireDate: item.expireDate || item.expiryDate || ''
      }))
    } else {
      expiringMembers.value = []
    }
  } catch (e) {
    console.error('获取即将到期会员数据失败:', e)
    expiringMembers.value = []
  } finally {
    loading.value = false
  }
}

const totalExpiring = computed(() => expiringMembers.value.length)
const within7Days = computed(() => expiringMembers.value.filter(m => m.daysLeft <= 7).length)
const within15Days = computed(() => expiringMembers.value.filter(m => m.daysLeft <= 15).length)

onMounted(() => {
  fetchExpiringMembers()
})

useDashboardChartRefresh(() => fetchExpiringMembers())
</script>

<style scoped>
.alert-card {
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

.alert-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, #EEF2FF 0%, #E0F2FE 100%);
  border-radius: 14px;
  margin-bottom: 14px;
}

.summary-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.summary-value {
  font-family: 'Poppins', sans-serif;
  font-size: 26px;
  font-weight: 700;
  color: #1D4ED8;
}

.summary-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #475569;
}

.summary-badges {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.badge {
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999px;
}

.badge.high {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.badge.medium {
  background: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.alert-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.alert-list-empty {
  padding: 24px 16px;
  text-align: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px dashed #E2E8F0;
  font-size: 13px;
  color: #94A3B8;
}

.alert-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border: 1px solid #E2E8F0;
  transition: all 0.25s ease;
}

.alert-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.08);
  border-color: rgba(59, 130, 246, 0.35);
}

.alert-avatar {
  width: 32px;
  height: 32px;
  border-radius: 999px;
  background: linear-gradient(135deg, #3B82F6 0%, #6366F1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #FFFFFF;
  flex-shrink: 0;
}

.alert-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.alert-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.alert-name {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.alert-level {
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.08);
  color: #1D4ED8;
}

.alert-meta {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #94A3B8;
  display: flex;
  gap: 10px;
}

.alert-tag {
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  padding: 4px 10px;
  border-radius: 999px;
  flex-shrink: 0;
}

.alert-tag.high {
  background: rgba(239, 68, 68, 0.1);
  color: #B91C1C;
}

.alert-tag.medium {
  background: rgba(245, 158, 11, 0.1);
  color: #B45309;
}

.alert-tag.low {
  background: rgba(16, 185, 129, 0.08);
  color: #059669;
}

@media (max-width: 768px) {
  .alert-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .summary-badges {
    flex-direction: row;
  }
}
</style>

