<template>
  <div class="bigscreen">
    <div class="bigscreen-bg" />
    <div class="bigscreen-content">
      <h1 class="bigscreen-title">运营看板</h1>
      <p class="bigscreen-time">{{ currentTime }}</p>
      <div class="bigscreen-cards">
        <div class="bigscreen-card">
          <span class="card-label">今日预订</span>
          <span class="card-value">{{ stats.todayBookings }}</span>
          <span class="card-unit">次</span>
        </div>
        <div class="bigscreen-card">
          <span class="card-label">今日收入</span>
          <span class="card-value">¥{{ formatNum(stats.todayRevenue) }}</span>
        </div>
        <div class="bigscreen-card">
          <span class="card-label">会员总数</span>
          <span class="card-value">{{ stats.memberTotal }}</span>
          <span class="card-unit">人</span>
        </div>
        <div class="bigscreen-card">
          <span class="card-label">场地总数</span>
          <span class="card-value">{{ stats.courtTotal }}</span>
          <span class="card-unit">块</span>
        </div>
        <div class="bigscreen-card">
          <span class="card-label">今日新增会员</span>
          <span class="card-value">{{ stats.todayNewMembers }}</span>
          <span class="card-unit">人</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getMemberStatistics } from '@/api/member'
import { getBookingStatistics } from '@/api/booking'
import { getFinanceStatistics } from '@/api/finance'
import { getCourtStatistics } from '@/api/court'

const currentTime = ref('')
const stats = ref({
  todayBookings: 0,
  todayRevenue: 0,
  memberTotal: 0,
  courtTotal: 0,
  todayNewMembers: 0
})

function formatNum(n: number | string) {
  if (n == null) return '0'
  const num = typeof n === 'number' ? n : parseFloat(String(n)) || 0
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

async function fetchStats() {
  try {
    const today = new Date().toISOString().split('T')[0]
    const [memberRes, bookingRes, financeRes, courtRes] = await Promise.all([
      getMemberStatistics({}),
      getBookingStatistics(),
      getFinanceStatistics({ startDate: today, endDate: today }),
      getCourtStatistics()
    ])
    if (memberRes?.data) {
      stats.value.memberTotal = memberRes.data.total ?? 0
      stats.value.todayNewMembers = memberRes.data.newToday ?? 0
    }
    if (bookingRes?.data) {
      const d = bookingRes.data
      stats.value.todayBookings = d.todayBookings != null ? d.todayBookings : ((d.ongoing ?? 0) + (d.finished ?? 0))
    }
    if (financeRes?.data) {
      const r = financeRes.data.totalIncome ?? 0
      stats.value.todayRevenue = typeof r === 'number' ? r : parseFloat(r) || 0
    }
    if (courtRes?.data) {
      stats.value.courtTotal = courtRes.data.total ?? 0
    }
  } catch (e) {
    console.error('大屏数据获取失败', e)
  }
}

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

const onRefresh = () => {
  fetchStats()
}

let timeInterval: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  window.addEventListener('bmp-dashboard-refresh', onRefresh)
  fetchStats()
})

onUnmounted(() => {
  window.removeEventListener('bmp-dashboard-refresh', onRefresh)
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.bigscreen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  overflow: auto;
}
.bigscreen-bg {
  position: absolute;
  inset: 0;
  opacity: 0.08;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.4'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}
.bigscreen-content {
  position: relative;
  padding: 48px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.bigscreen-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #f8fafc;
  margin: 0 0 8px 0;
  letter-spacing: 0.05em;
}
.bigscreen-time {
  font-size: 1.5rem;
  color: #94a3b8;
  margin: 0 0 48px 0;
}
.bigscreen-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  justify-content: center;
  max-width: 1200px;
}
.bigscreen-card {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  padding: 32px 40px;
  min-width: 200px;
  text-align: center;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.3);
}
.bigscreen-card .card-label {
  display: block;
  font-size: 1rem;
  color: #94a3b8;
  margin-bottom: 12px;
}
.bigscreen-card .card-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: #f8fafc;
}
.bigscreen-card .card-unit {
  font-size: 1rem;
  color: #94a3b8;
  margin-left: 4px;
}
</style>
