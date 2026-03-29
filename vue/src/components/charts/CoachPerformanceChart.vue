<template>
  <div class="glass-card chart-card coach-performance-card">
    <div class="chart-header">
      <div class="header-left">
        <span class="chart-title">🏆 教练业绩排行</span>
        <span class="chart-subtitle">课程预约量与评分排名</span>
      </div>
      <el-link type="primary" :underline="false" class="view-all" @click="navigateTo('/coach-course/coach')">
        查看全部
      </el-link>
    </div>
    <div class="coach-list" v-loading="loading">
      <template v-if="coachData && coachData.length > 0">
        <div
          class="coach-item"
          v-for="(coach, index) in coachData"
          :key="coach.id"
          :class="{ 'top-three': index < 3 }"
        >
          <div class="rank-badge" :class="getRankClass(index)">
            <span v-if="index < 3">{{ getRankIcon(index) }}</span>
            <span v-else>{{ index + 1 }}</span>
          </div>
          <div class="coach-avatar">
            <img v-if="coach.avatar" :src="resolveCoachAvatarUrl(coach.avatar)" :alt="coach.name" />
            <div v-else class="avatar-placeholder">{{ (coach.name || '教').charAt(0) }}</div>
          </div>
          <div class="coach-info">
            <div class="coach-name">{{ coach.name || '未知教练' }}</div>
            <div class="coach-specialty">{{ coach.specialty || '—' }}</div>
          </div>
        <div class="coach-stats">
          <div class="stat-item">
            <span class="stat-value">{{ coach.bookings ?? 0 }}</span>
            <span class="stat-label">预约</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value rating">
              <el-icon><StarFilled /></el-icon>
              {{ coach.rating ?? 0 }}
            </span>
            <span class="stat-label">评分</span>
          </div>
        </div>
        <div class="progress-bar-wrapper">
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: getProgressWidth(coach.bookings ?? 0) + '%' }"
              :class="getRankClass(index)"
            ></div>
          </div>
        </div>
        </div>
      </template>
      <div v-else class="coach-list-empty">
        <el-empty description="暂无教练业绩数据" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { StarFilled } from '@element-plus/icons-vue'
import { getCoachList } from '@/api/coach'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const router = useRouter()
const loading = ref(false)
const coachData = ref([])
const maxBookings = ref(100)

const navigateTo = (path) => {
  router.push(path)
}

// 获取排名样式类
const getRankClass = (index) => {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}

// 获取排名图标
const getRankIcon = (index) => {
  if (index === 0) return '🥇'
  if (index === 1) return '🥈'
  if (index === 2) return '🥉'
  return ''
}

// 计算进度条宽度
const getProgressWidth = (bookings) => {
  return Math.min((bookings / maxBookings.value) * 100, 100)
}

// 解析教练头像 URL（与教练管理页一致，支持相对路径）
const resolveCoachAvatarUrl = (url) => {
  if (!url || typeof url !== 'string') return ''
  const t = url.trim()
  if (t.startsWith('http://') || t.startsWith('https://')) return t
  if (t.startsWith('/')) return t
  return '/' + t
}

// 获取教练数据（真实 API 数据，无模拟数据fallback）
const fetchCoachData = async () => {
  loading.value = true
  try {
    const res = await getCoachList({ page: 1, size: 10, status: 1 })
    if (res.code === 200 && res.data) {
      const list = res.data.data || res.data.records || res.data.list || []
      if (Array.isArray(list) && list.length > 0) {
        const mapped = list.map(coach => ({
          id: coach.id,
          name: coach.coachName || coach.name,
          avatar: coach.avatar || '',
          specialty: coach.specialty || '羽毛球教练',
          bookings: coach.totalStudents ?? 0,
          rating: coach.rating != null ? Number(coach.rating) : 0
        }))
        coachData.value = mapped
          .sort((a, b) => b.bookings - a.bookings)
          .slice(0, 5)
        maxBookings.value = Math.max(...coachData.value.map(c => c.bookings), 1)
      } else {
        coachData.value = []
      }
    } else {
      coachData.value = []
    }
  } catch (error) {
    console.error('获取教练数据失败:', error)
    coachData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCoachData()
})

useDashboardChartRefresh(() => fetchCoachData())
</script>

<style scoped>
.coach-performance-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.chart-title {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
}

.chart-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #94A3B8;
}

.coach-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coach-list-empty {
  padding: 32px 16px;
  text-align: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px dashed #E2E8F0;
  font-size: 13px;
  color: #94A3B8;
}

.coach-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.coach-item:hover {
  transform: translateX(6px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-color: rgba(99, 102, 241, 0.3);
}

.coach-item.top-three {
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border-color: #FCD34D;
}

.coach-item.top-three:hover {
  box-shadow: 0 4px 16px rgba(251, 191, 36, 0.3);
}

.rank-badge {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Poppins', sans-serif;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  background: #F1F5F9;
  color: #64748B;
}

.rank-badge.gold {
  background: linear-gradient(135deg, #FCD34D 0%, #F59E0B 100%);
  color: #fff;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
}

.rank-badge.silver {
  background: linear-gradient(135deg, #E2E8F0 0%, #94A3B8 100%);
  color: #fff;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(148, 163, 184, 0.4);
}

.rank-badge.bronze {
  background: linear-gradient(135deg, #FDBA74 0%, #EA580C 100%);
  color: #fff;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(234, 88, 12, 0.4);
}

.coach-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid #E2E8F0;
}

.coach-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
}

.coach-info {
  flex: 1;
  min-width: 0;
}

.coach-name {
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 2px;
}

.coach-specialty {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #64748B;
}

.coach-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-value {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
}

.stat-value.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #F59E0B;
}

.stat-value.rating .el-icon {
  font-size: 14px;
}

.stat-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  color: #94A3B8;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: #E2E8F0;
}

.progress-bar-wrapper {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
}

.progress-bar {
  width: 100%;
  height: 100%;
  background: #F1F5F9;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6 0%, #2563EB 100%);
  border-radius: 0 2px 2px 0;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.progress-fill.gold {
  background: linear-gradient(90deg, #FCD34D 0%, #F59E0B 100%);
}

.progress-fill.silver {
  background: linear-gradient(90deg, #CBD5E1 0%, #94A3B8 100%);
}

.progress-fill.bronze {
  background: linear-gradient(90deg, #FDBA74 0%, #EA580C 100%);
}

@media (max-width: 768px) {
  .coach-item {
    flex-wrap: wrap;
    gap: 12px;
    padding: 14px;
  }

  .coach-stats {
    width: 100%;
    justify-content: flex-end;
    margin-top: 8px;
  }

  .coach-avatar {
    width: 40px;
    height: 40px;
  }

  .rank-badge {
    width: 32px;
    height: 32px;
  }
}
</style>
