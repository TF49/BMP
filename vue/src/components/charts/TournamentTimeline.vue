<template>
  <div class="chart-card glass-card">
    <div class="chart-header">
      <div class="chart-title-wrapper">
        <div class="chart-icon tournament-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M7 4h10v4a5 5 0 0 1-10 0V4Z" />
            <path d="M5 4h2v4a5 5 0 0 1-2-4Z" />
            <path d="M17 4h2a5 5 0 0 1-2 4V4Z" />
            <path d="M12 13v7" />
            <path d="M8 21h8" />
          </svg>
        </div>
        <div class="chart-title-content">
          <h3 class="chart-title">赛事时间轴</h3>
          <span class="chart-subtitle">未来一段时间的主要赛事安排</span>
        </div>
      </div>
    </div>
    <div class="timeline-body" v-loading="loading">
      <div
        v-for="item in tournaments"
        :key="item.id"
        class="timeline-item"
      >
        <div class="timeline-date">
          <div class="date-main">{{ item.date }}</div>
          <div class="date-sub">{{ item.weekday }}</div>
        </div>
        <div class="timeline-line">
          <div class="dot" :class="item.level"></div>
          <div class="line"></div>
        </div>
        <div class="timeline-content">
          <div class="title-row">
            <span class="name">{{ item.name }}</span>
            <span class="tag" :class="item.level">{{ item.levelLabel }}</span>
          </div>
          <div class="meta">
            <span>{{ item.venue }}</span>
            <span>报名：{{ item.signup }}/{{ item.quota }}</span>
          </div>
        </div>
      </div>
      <!-- 空状态占位项 -->
      <div v-if="tournaments.length === 0" class="timeline-item timeline-empty">
        <div class="timeline-date">
          <div class="date-main">--</div>
          <div class="date-sub">--</div>
        </div>
        <div class="timeline-line">
          <div class="dot club"></div>
          <div class="line"></div>
        </div>
        <div class="timeline-content timeline-empty-content">
          <div class="title-row">
            <span class="name">暂无赛事安排</span>
          </div>
          <div class="meta">
            <span>暂无即将开始的赛事</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTournamentList } from '@/api/tournament'
import { useDashboardChartRefresh } from '@/composables/useDashboardChartRefresh'

const tournaments = ref([])
const loading = ref(false)

const parseNum = (v) => {
  if (v == null) return 0
  if (typeof v === 'number') return v
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

const formatDate = (dateStr) => {
  if (!dateStr) return { date: '', weekday: '' }
  const d = new Date(dateStr)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return {
    date: `${month}-${day}`,
    weekday: weekdays[d.getDay()]
  }
}

const getLevelClass = (status) => {
  // 根据赛事状态或级别返回样式类
  const statusMap = {
    1: 'club',      // 协会赛
    2: 'city',      // 城市赛
    3: 'youth',     // 青少年
    4: 'fun'        // 活动
  }
  return statusMap[status] || 'club'
}

const getLevelLabel = (status) => {
  const labelMap = {
    1: '协会赛',
    2: '城市赛',
    3: '青少年',
    4: '活动'
  }
  return labelMap[status] || '赛事'
}

const fetchTournaments = async () => {
  loading.value = true
  try {
    const res = await getTournamentList({
      pageNum: 1,
      pageSize: 4,
      status: 1
    })
    if (res.code === 200 && Array.isArray(res.data?.list || res.data)) {
      const list = res.data?.list || res.data
      tournaments.value = list.slice(0, 4).map((item, index) => {
        const dateInfo = formatDate(item.startDate || item.date)
        return {
          id: item.id || index,
          name: item.name || item.tournamentName || '未命名赛事',
          date: dateInfo.date,
          weekday: dateInfo.weekday,
          level: getLevelClass(item.level || item.type || 1),
          levelLabel: getLevelLabel(item.level || item.type || 1),
          venue: item.venueName || item.venue || '待定',
          signup: parseNum(item.currentSignup || item.registrationCount),
          quota: parseNum(item.maxSignup || item.maxParticipants || 0)
        }
      })
    }
  } catch (e) {
    console.error('获取赛事时间轴失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTournaments()
})

useDashboardChartRefresh(() => fetchTournaments())
</script>

<style scoped>
.chart-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 22px 22px 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.08);
  border-color: var(--color-primary-light, #93C5FD);
  transform: translateY(-2px);
}

.chart-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.chart-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chart-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.chart-icon svg {
  width: 22px;
  height: 22px;
}

.tournament-icon {
  background: linear-gradient(135deg, var(--color-warning) 0%, var(--color-primary) 100%);
  box-shadow: 0 4px 10px color-mix(in srgb, var(--color-warning) 30%, transparent);
}

.chart-title-content {
  display: flex;
  flex-direction: column;
}

.chart-title {
  font-family: 'Poppins', sans-serif;
  font-size: 17px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.chart-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
  margin-top: 2px;
}

.timeline-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-item {
  display: grid;
  grid-template-columns: auto 32px 1fr;
  gap: 10px;
  align-items: flex-start;
}

.timeline-date {
  text-align: right;
  min-width: 60px;
}

.date-main {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.date-sub {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: var(--color-text-muted);
}

.timeline-line {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  border: 2px solid #fff;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15);
}

.dot.club {
  background: var(--color-primary);
}

.dot.city {
  background: var(--color-warning);
}

.dot.youth {
  background: var(--color-success);
}

.dot.fun {
  background: var(--color-info);
}

.line {
  flex: 1;
  width: 2px;
  background: linear-gradient(to bottom, var(--color-border), transparent);
}

.timeline-content {
  padding: 10px 12px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 10px;
  border: 1px solid var(--color-border, #E2E8F0);
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
  gap: 6px;
}

.name {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.tag {
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
}

.tag.club {
  background: rgba(59, 130, 246, 0.08);
  color: var(--color-primary);
}

.tag.city {
  background: rgba(245, 158, 11, 0.1);
  color: var(--color-warning);
}

.tag.youth {
  background: rgba(16, 185, 129, 0.1);
  color: var(--color-success);
}

.tag.fun {
  background: rgba(6, 182, 212, 0.1);
  color: var(--color-info);
}

.meta {
  margin-top: 2px;
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: var(--color-text-muted);
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.timeline-empty .timeline-date {
  opacity: 0.5;
}

.timeline-empty-content {
  opacity: 0.7;
}

.timeline-empty-content .name {
  color: var(--color-text-muted);
  font-style: italic;
}

@media (max-width: 768px) {
  .timeline-item {
    grid-template-columns: auto 28px 1fr;
  }

  .timeline-content {
    padding: 8px 10px;
  }

  .name {
    font-size: 13px;
  }
}
</style>

