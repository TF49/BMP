<template>
  <div class="coach-dashboard">
    <!-- 教练信息卡片（与用户端首页大卡片统一：渐变、圆角、头像+问候+数据） -->
    <div class="coach-info-card" :class="{ 'not-bound': notBound }">
      <div class="coach-avatar-section">
        <el-avatar
          :size="80"
          :src="coachAvatarUrl || undefined"
          :icon="!coachAvatarUrl ? UserFilled : undefined"
          class="coach-avatar"
        />
        <div class="coach-badge">{{ notBound ? '未绑定' : '教练' }}</div>
      </div>
      <div class="coach-details">
        <h2 class="coach-name">{{ displayName }}</h2>
        <p class="coach-greeting">{{ greeting }}，{{ notBound ? '请先绑定教练档案' : '欢迎回来！' }}</p>
        <div v-if="!notBound" class="coach-stats">
          <div class="stat-item">
            <span class="stat-label">今日课节</span>
            <span class="stat-value">{{ todayCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">本周课节</span>
            <span class="stat-value">{{ weekCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">累计学员数</span>
            <span class="stat-value">{{ coachInfo?.totalStudents ?? 0 }}</span>
          </div>
        </div>
      </div>
      <div class="coach-actions">
        <el-button
          type="primary"
          :icon="User"
          class="action-btn"
          @click="$router.push('/coach/profile')"
        >
          {{ notBound ? '去绑定' : '我的档案' }}
        </el-button>
      </div>
    </div>

    <!-- 快捷功能（与用户端首页一致：白底圆角卡片 + 图标 + 标题 + 描述） -->
    <div class="quick-actions">
      <h3 class="section-title">快捷功能</h3>
      <div class="action-grid">
        <div
          v-for="action in quickActions"
          :key="action.path"
          class="action-card"
          @click="$router.push(action.path)"
        >
          <div class="action-icon" :style="{ background: action.gradient }">
            <el-icon :size="28"><component :is="action.icon" /></el-icon>
          </div>
          <div class="action-content">
            <h4 class="action-title">{{ action.title }}</h4>
            <p class="action-desc">{{ action.desc }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据概览（与用户端订单统计风格一致） -->
    <div v-if="!notBound" class="stats-section">
      <h3 class="section-title">数据概览</h3>
      <div class="stats-grid">
        <div class="stat-card stat-today">
          <div class="stat-icon">
            <el-icon :size="24"><Calendar /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ todayCount }}</div>
            <div class="stat-label">今日课节</div>
          </div>
        </div>
        <div class="stat-card stat-week">
          <div class="stat-icon">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ weekCount }}</div>
            <div class="stat-label">本周课节</div>
          </div>
        </div>
        <div class="stat-card stat-students">
          <div class="stat-icon">
            <el-icon :size="24"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ coachInfo?.totalStudents ?? 0 }}</div>
            <div class="stat-label">累计学员</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { UserFilled, Document, Calendar, List, User } from '@element-plus/icons-vue'
import { getCurrentCoach } from '@/api/coach'
import { getMyCourses } from '@/api/course'

const coachInfo = ref(null)
const notBound = ref(false)
const todayCount = ref(0)
const weekCount = ref(0)

const displayName = computed(() => {
  if (coachInfo.value?.coachName) return coachInfo.value.coachName
  try {
    const u = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return u.username || '教练'
  } catch (_) {
    return '教练'
  }
})

const coachAvatarUrl = computed(() => {
  const url = coachInfo.value?.avatar
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = import.meta.env.VITE_APP_BASE_API || ''
  return base ? (base.replace(/\/$/, '') + url) : url
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 17) return '下午好'
  if (h < 19) return '傍晚好'
  return '晚上好'
})

const quickActions = [
  {
    title: '我的课程',
    desc: '查看我担任教练的课程',
    path: '/coach/courses',
    icon: Document,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '我的课表',
    desc: '查看课程排期与时间',
    path: '/coach/schedule',
    icon: Calendar,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    title: '预约明细',
    desc: '课程预约与签到管理',
    path: '/coach/bookings',
    icon: List,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  },
  {
    title: '我的档案',
    desc: '编辑个人信息与头像',
    path: '/coach/profile',
    icon: User,
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  }
]

const loadCoach = async () => {
  try {
    const res = await getCurrentCoach()
    if (res?.code === 200 && res?.data) {
      coachInfo.value = res.data
      notBound.value = false
    } else {
      coachInfo.value = null
      notBound.value = true
    }
  } catch (_) {
    notBound.value = true
  }
}

const loadCounts = async () => {
  if (!coachInfo.value) return
  const now = new Date()
  const today = now.toISOString().slice(0, 10)
  const weekStart = new Date(now)
  weekStart.setDate(now.getDate() - now.getDay() + (now.getDay() === 0 ? -6 : 1))
  const weekStartStr = weekStart.toISOString().slice(0, 10)
  const weekEnd = new Date(weekStart)
  weekEnd.setDate(weekStart.getDate() + 6)
  const weekEndStr = weekEnd.toISOString().slice(0, 10)
  try {
    const [todayRes, weekRes] = await Promise.all([
      getMyCourses({ startTime: today + ' 00:00:00', endTime: today + ' 23:59:59', page: 1, size: 1 }),
      getMyCourses({ startTime: weekStartStr + ' 00:00:00', endTime: weekEndStr + ' 23:59:59', page: 1, size: 1 })
    ])
    todayCount.value = todayRes?.data?.total ?? 0
    weekCount.value = weekRes?.data?.total ?? 0
  } catch (_) {
    todayCount.value = 0
    weekCount.value = 0
  }
}

onMounted(async () => {
  await loadCoach()
  await loadCounts()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.coach-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@media (prefers-reduced-motion: reduce) {
  * { animation-duration: 0.01ms !important; transition-duration: 0.01ms !important; }
}

/* 教练信息大卡片 - 与用户端 user-info-card 统一 */
.coach-info-card {
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-secondary, #8B5CF6) 100%);
  border-radius: 24px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  color: #ffffff;
  box-shadow: 0 12px 40px rgba(37, 99, 235, 0.3), 0 4px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.coach-info-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  pointer-events: none;
}

.coach-info-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 16px 48px rgba(37, 99, 235, 0.4), 0 6px 16px rgba(0, 0, 0, 0.15);
}

.coach-info-card.not-bound {
  background: linear-gradient(135deg, #94a3b8 0%, #64748b 100%);
  box-shadow: 0 12px 40px rgba(100, 116, 139, 0.25);
}

.coach-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.coach-avatar {
  border: 4px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.coach-badge {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
}

.coach-details {
  flex: 1;
}

.coach-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  font-family: 'Poppins', 'Inter', sans-serif;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.coach-greeting {
  font-size: 16px;
  margin: 0 0 16px 0;
  opacity: 0.9;
}

.coach-stats {
  display: flex;
  gap: 32px;
}

.coach-stats .stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coach-stats .stat-label {
  font-size: 13px;
  opacity: 0.8;
}

.coach-stats .stat-value {
  font-size: 24px;
  font-weight: 700;
  font-family: 'Poppins', sans-serif;
}

.coach-actions {
  position: relative;
  z-index: 1;
}

.coach-actions .action-btn {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  border: 2px solid rgba(255, 255, 255, 0.4);
  color: #ffffff;
  font-weight: 600;
  padding: 12px 24px;
  border-radius: 14px;
  transition: all 0.3s ease;
}

.coach-actions .action-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
}

/* 快捷功能 - 与用户端一致 */
.quick-actions {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 16px 0;
  font-family: 'Poppins', 'Inter', sans-serif;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

@media (min-width: 1024px) {
  .action-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.action-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 18px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.action-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15), 0 4px 12px rgba(37, 99, 235, 0.2);
  border-color: var(--color-primary-light, #60A5FA);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  transition: all 0.4s ease;
  position: relative;
  z-index: 1;
}

.action-card:hover .action-icon {
  transform: scale(1.15) rotate(5deg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 4px 0;
}

.action-desc {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

/* 数据概览 - 与用户端订单统计风格一致 */
.stats-section {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 18px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--color-primary-light, #60A5FA);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}

.stat-today .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-week .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-students .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-content .stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Poppins', sans-serif;
  margin-bottom: 4px;
}

.stat-content .stat-label {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
}

@media (max-width: 768px) {
  .coach-info-card {
    flex-direction: column;
    text-align: center;
    padding: 24px 16px;
  }
  .coach-stats {
    justify-content: center;
    flex-wrap: wrap;
    gap: 20px;
  }
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
