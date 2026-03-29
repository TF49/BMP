<template>
  <div class="glass-card kpi-board">
    <div class="card-header">
      <span class="card-title">📊 核心业务指标</span>
      <el-link type="primary" :underline="false" class="view-all" @click="refreshData">
        <el-icon><Refresh /></el-icon>
        刷新数据
      </el-link>
    </div>
    <div class="kpi-grid">
      <!-- 今日收入 -->
      <div class="kpi-item kpi-revenue" @click="navigateTo('/finance/management')">
        <div class="kpi-icon-wrapper">
          <div class="kpi-icon">
            <el-icon :size="32"><Money /></el-icon>
          </div>
          <div class="kpi-pulse"></div>
        </div>
        <div class="kpi-content">
          <div class="kpi-label">今日收入</div>
          <div class="kpi-value" :class="{ 'loading': loading }">
            <span class="currency">¥</span>
            <span class="number">{{ formatNumber(kpiData.todayRevenue) }}</span>
          </div>
          <div class="kpi-trend" :class="(kpiData.revenueTrend ?? 0) > 0 ? 'up' : 'down'">
            <el-icon><component :is="(kpiData.revenueTrend ?? 0) > 0 ? 'Top' : 'Bottom'" /></el-icon>
            {{ Math.abs(kpiData.revenueTrend ?? 0) }}%
            <span class="trend-label">vs 昨日</span>
          </div>
        </div>
      </div>

      <!-- 今日预订 -->
      <div class="kpi-item kpi-booking" @click="navigateTo('/booking/management')">
        <div class="kpi-icon-wrapper">
          <div class="kpi-icon">
            <el-icon :size="32"><Calendar /></el-icon>
          </div>
          <div class="kpi-pulse"></div>
        </div>
        <div class="kpi-content">
          <div class="kpi-label">今日预订</div>
          <div class="kpi-value" :class="{ 'loading': loading }">
            <span class="number">{{ kpiData.todayBookings ?? 0 }}</span>
            <span class="unit">次</span>
          </div>
          <div class="kpi-trend" :class="(kpiData.bookingTrend ?? 0) > 0 ? 'up' : 'down'">
            <el-icon><component :is="(kpiData.bookingTrend ?? 0) > 0 ? 'Top' : 'Bottom'" /></el-icon>
            {{ Math.abs(kpiData.bookingTrend ?? 0) }}%
            <span class="trend-label">vs 昨日</span>
          </div>
        </div>
      </div>

      <!-- 在线会员 -->
      <div class="kpi-item kpi-member" @click="navigateTo('/member/management')">
        <div class="kpi-icon-wrapper">
          <div class="kpi-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="kpi-pulse"></div>
        </div>
        <div class="kpi-content">
          <div class="kpi-label">活跃会员</div>
          <div class="kpi-value" :class="{ 'loading': loading }">
            <span class="number">{{ kpiData.activeMembers ?? 0 }}</span>
            <span class="unit">人</span>
          </div>
          <div class="kpi-trend up">
            <el-icon><User /></el-icon>
            总会员 {{ kpiData.totalMembers ?? 0 }}
          </div>
        </div>
      </div>

      <!-- 场地使用率 -->
      <div class="kpi-item kpi-usage" @click="navigateTo('/court/management')">
        <div class="kpi-icon-wrapper">
          <div class="kpi-icon">
            <el-icon :size="32"><Grid /></el-icon>
          </div>
          <div class="kpi-pulse"></div>
        </div>
        <div class="kpi-content">
          <div class="kpi-label">场地使用率</div>
          <div class="kpi-value" :class="{ 'loading': loading }">
            <span class="number">{{ kpiData.courtUsageRate ?? 0 }}</span>
            <span class="unit">%</span>
          </div>
          <div class="kpi-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: (kpiData.courtUsageRate ?? 0) + '%' }"></div>
            </div>
            <span class="progress-text">{{ kpiData.courtsInUse ?? 0 }}/{{ kpiData.totalCourts ?? 0 }} 场地使用中</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Money, Calendar, User, Grid, Refresh, Top, Bottom } from '@element-plus/icons-vue'

const router = useRouter()

const props = defineProps({
  stats: {
    type: Object,
    default: () => ({
      memberTotal: 0,
      memberGrowth: 0,
      activeMembers: 0,
      todayBookings: 0,
      bookingGrowth: 0,
      todayRevenue: 0,
      revenueGrowth: 0,
      courtTotal: 0,
      courtGrowth: 0,
      courtsInUse: 0,
      courtUsageRate: 0
    })
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh'])

// 计算 KPI 数据（与 Dashboard 统计接口字段一致，见 /api/member/statistics、/api/court/statistics）
const kpiData = computed(() => ({
  todayRevenue: props.stats.todayRevenue || 0,
  revenueTrend: props.stats.revenueGrowth || 0,
  todayBookings: props.stats.todayBookings || 0,
  bookingTrend: props.stats.bookingGrowth || 0,
  activeMembers: props.stats.activeMembers != null ? props.stats.activeMembers : props.stats.memberTotal || 0,
  totalMembers: props.stats.memberTotal || 0,
  courtUsageRate: props.stats.courtUsageRate != null ? props.stats.courtUsageRate : 0,
  courtsInUse: props.stats.courtsInUse != null ? props.stats.courtsInUse : 0,
  totalCourts: props.stats.courtTotal || 0
}))

// 格式化数字（千分位）
const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 导航
const navigateTo = (path) => {
  router.push(path)
}

// 刷新数据
const refreshData = () => {
  emit('refresh')
}
</script>

<style scoped>
.kpi-board {
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FAFC 100%);
  border: 1px solid #E2E8F0;
  position: relative;
  overflow: hidden;
}

.kpi-board::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-secondary), var(--color-accent));
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-top: 8px;
}

.kpi-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px 24px;
  background: linear-gradient(135deg, #FFFFFF 0%, #F8FAFC 100%);
  border-radius: 16px;
  border: 2px solid transparent;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.kpi-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.03) 0%, rgba(139, 92, 246, 0.03) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.kpi-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  border-color: rgba(99, 102, 241, 0.3);
}

.kpi-item:hover::before {
  opacity: 1;
}

.kpi-item:hover .kpi-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

.kpi-icon-wrapper {
  position: relative;
  flex-shrink: 0;
}

.kpi-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 2;
}

.kpi-item:hover .kpi-icon {
  transform: scale(1.1) rotate(-5deg);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.2);
}

.kpi-revenue .kpi-icon {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
}

.kpi-booking .kpi-icon {
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
}

.kpi-member .kpi-icon {
  background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%);
}

.kpi-usage .kpi-icon {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
}

.kpi-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: inherit;
  opacity: 0;
  z-index: 1;
}

@keyframes pulse {
  0% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(1.5);
  }
}

.kpi-content {
  flex: 1;
  min-width: 0;
}

.kpi-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  color: #64748B;
  margin-bottom: 8px;
  font-weight: 500;
}

.kpi-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 8px;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
}

.kpi-value.loading {
  opacity: 0.6;
  animation: shimmer 1.5s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.kpi-value .currency {
  font-size: 24px;
  font-weight: 600;
  color: #10B981;
}

.kpi-value .number {
  font-size: 36px;
  font-weight: 700;
  color: #1E293B;
  letter-spacing: -0.5px;
}

.kpi-value .unit {
  font-size: 16px;
  font-weight: 500;
  color: #64748B;
  margin-left: 4px;
}

.kpi-trend {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  font-weight: 600;
}

.kpi-trend.up {
  background: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.kpi-trend.down {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.trend-label {
  font-size: 11px;
  font-weight: 400;
  opacity: 0.8;
  margin-left: 4px;
}

.kpi-progress {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #E2E8F0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #F59E0B 0%, #D97706 100%);
  border-radius: 3px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 10px rgba(245, 158, 11, 0.5);
}

.progress-text {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #64748B;
}

/* 响应式 */
@media (max-width: 1400px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .kpi-item {
    padding: 20px;
  }

  .kpi-icon {
    width: 64px;
    height: 64px;
  }

  .kpi-value .number {
    font-size: 28px;
  }
}
</style>
