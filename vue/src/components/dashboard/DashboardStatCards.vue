<template>
  <el-row :gutter="24">
    <el-col :xs="24" :sm="12" :lg="4" :xl="4">
      <div class="stat-card stat-card-members" :class="{ loading: statsLoading }">
        <div class="stat-icon-wrapper">
          <div class="stat-icon-bg">
            <el-icon :size="28"><User /></el-icon>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ nz(stats.memberTotal) }}</div>
          <div class="stat-label">会员总数</div>
          <div class="stat-trend" :class="nz(stats.memberGrowth) >= 0 ? 'up' : 'down'">
            <el-icon><component :is="nz(stats.memberGrowth) >= 0 ? ArrowUp : ArrowDown" /></el-icon> {{ nz(stats.memberGrowth) }}%
          </div>
        </div>
      </div>
    </el-col>
    <el-col :xs="24" :sm="12" :lg="5" :xl="5">
      <div class="stat-card stat-card-venues" :class="{ loading: statsLoading }">
        <div class="stat-icon-wrapper">
          <div class="stat-icon-bg">
            <el-icon :size="28"><OfficeBuilding /></el-icon>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ venueCount }}</div>
          <div class="stat-label">场馆数量</div>
          <div class="stat-trend" :class="nz(stats.venueGrowth) >= 0 ? 'up' : 'down'">
            <el-icon><component :is="nz(stats.venueGrowth) >= 0 ? ArrowUp : ArrowDown" /></el-icon> {{ nz(stats.venueGrowth) }}
          </div>
        </div>
      </div>
    </el-col>
    <el-col :xs="24" :sm="12" :lg="5" :xl="5">
      <div class="stat-card stat-card-courts" :class="{ loading: statsLoading }">
        <div class="stat-icon-wrapper">
          <div class="stat-icon-bg">
            <el-icon :size="28"><Grid /></el-icon>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ nz(stats.courtTotal) }}</div>
          <div class="stat-label">场地数量</div>
          <div class="stat-trend" :class="nz(stats.courtGrowth) >= 0 ? 'up' : 'down'">
            <el-icon><component :is="nz(stats.courtGrowth) >= 0 ? ArrowUp : ArrowDown" /></el-icon> {{ nz(stats.courtGrowth) }}
          </div>
        </div>
      </div>
    </el-col>
    <el-col :xs="24" :sm="12" :lg="5" :xl="5">
      <div class="stat-card stat-card-bookings" :class="{ loading: statsLoading }">
        <div class="stat-icon-wrapper">
          <div class="stat-icon-bg">
            <el-icon :size="28"><Calendar /></el-icon>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ nz(stats.todayBookings) }}</div>
          <div class="stat-label">今日预订</div>
          <div class="stat-trend" :class="nz(stats.bookingGrowth) >= 0 ? 'up' : 'down'">
            <el-icon><component :is="nz(stats.bookingGrowth) >= 0 ? ArrowUp : ArrowDown" /></el-icon> {{ Math.abs(nz(stats.bookingGrowth)) }}%
          </div>
        </div>
      </div>
    </el-col>
    <el-col :xs="24" :sm="12" :lg="5" :xl="5">
      <div class="stat-card stat-card-revenue" :class="{ loading: statsLoading }">
        <div class="stat-icon-wrapper">
          <div class="stat-icon-bg">
            <el-icon :size="28"><Money /></el-icon>
          </div>
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ formatCurrency(stats.todayRevenue) }}</div>
          <div class="stat-label">今日收入</div>
          <div class="stat-trend" :class="nz(stats.revenueGrowth) >= 0 ? 'up' : 'down'">
            <el-icon><component :is="nz(stats.revenueGrowth) >= 0 ? ArrowUp : ArrowDown" /></el-icon> {{ Math.abs(nz(stats.revenueGrowth)).toFixed(1) }}%
          </div>
        </div>
      </div>
    </el-col>
  </el-row>
</template>

<script setup>
import { User, Grid, Calendar, Money, ArrowUp, ArrowDown, OfficeBuilding } from '@element-plus/icons-vue'

const props = defineProps({
  stats: { type: Object, required: true },
  statsLoading: { type: Boolean, default: false },
  venueCount: { type: Number, default: 0 }
})

const nz = (v) => {
  if (v === null || v === undefined) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const formatCurrency = (amount) => {
  const n = nz(amount)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  animation: cardFadeIn 0.6s ease-out both;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s ease;
}

.stat-card:hover::before {
  left: 100%;
}

.stat-card:nth-child(1) { animation-delay: 0s; }
.stat-card:nth-child(2) { animation-delay: 0.1s; }
.stat-card:nth-child(3) { animation-delay: 0.2s; }
.stat-card:nth-child(4) { animation-delay: 0.3s; }

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.12);
  border-color: rgba(59, 130, 246, 0.3);
}

.stat-card.loading {
  opacity: 0.6;
  pointer-events: none;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-icon-wrapper {
  margin-right: 18px;
}

.stat-card .stat-icon-bg {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card:hover .stat-icon-bg {
  transform: scale(1.1) rotate(5deg);
}

.stat-card-members .stat-icon-bg {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  box-shadow: 0 4px 15px rgba(var(--color-primary-rgb, 37, 99, 235), 0.4);
}

.stat-card-venues .stat-icon-bg {
  background: linear-gradient(135deg, #8b5cf6 0%, #a855f7 100%);
  box-shadow: 0 4px 15px rgba(139, 92, 246, 0.4);
}

.stat-card-courts .stat-icon-bg {
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-warning) 100%);
  box-shadow: 0 4px 15px rgba(var(--color-accent-rgb, 236, 72, 153), 0.4);
}

.stat-card-bookings .stat-icon-bg {
  background: linear-gradient(135deg, var(--color-info) 0%, var(--color-primary-light) 100%);
  box-shadow: 0 4px 15px rgba(var(--color-info-rgb, 6, 182, 212), 0.4);
}

.stat-card-revenue .stat-icon-bg {
  background: linear-gradient(135deg, var(--color-success) 0%, var(--color-info) 100%);
  box-shadow: 0 4px 15px rgba(var(--color-success-rgb, 16, 185, 129), 0.4);
}

.stat-value {
  font-family: inherit;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  line-height: 1.2;
}

.stat-label {
  font-family: inherit;
  font-size: 14px;
  color: var(--color-text-secondary, #64748b);
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  margin-top: 8px;
  font-family: inherit;
}

.stat-trend.up {
  color: var(--color-success, #10b981);
}

.stat-trend.down {
  color: var(--color-danger, #ef4444);
}

@media (max-width: 1200px) {
  .stat-card {
    padding: 20px;
  }

  .stat-value {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .stat-icon-wrapper {
    margin-right: 0;
    margin-bottom: 14px;
  }

  .stat-value {
    font-size: 28px;
  }
}

@media (max-width: 480px) {
  .stat-card {
    padding: 18px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-icon-bg {
    width: 56px;
    height: 56px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .stat-card {
    animation: none !important;
    transition: none !important;
  }
}
</style>
