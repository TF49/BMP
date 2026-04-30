<template>
  <div class="user-dashboard">
    <!-- 用户信息卡片 -->
    <div class="user-info-card">
        <div class="user-avatar-section">
        <el-avatar
          :size="80"
          :src="dashboardAvatarUrl || undefined"
          :icon="!dashboardAvatarUrl ? UserFilled : undefined"
          class="user-avatar"
        />
        <div class="user-badge">{{ userRoleLabel }}</div>
      </div>
      <div class="user-details">
        <h2 class="user-name">{{ userName }}</h2>
        <p class="user-greeting">{{ greeting }}，欢迎回来！</p>
        <div class="user-stats">
          <div class="stat-item">
            <span class="stat-label">账户余额</span>
            <span class="stat-value balance">¥{{ formatCurrency(balance) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">我的预约</span>
            <span class="stat-value">{{ bookingCount }}</span>
          </div>
        </div>
      </div>
      <div class="user-actions">
        <div class="user-actions-inner">
          <div class="vip-level-display" :class="vipLevelClass">
            <span class="vip-level-label">{{ vipLevelLabel }}</span>
          </div>
          <el-button type="primary" :icon="Money" @click="$router.push('/user/recharge')" class="action-btn">
            立即充值
          </el-button>
        </div>
      </div>
    </div>

    <!-- 快捷功能卡片 -->
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

    <!-- 我的预约 -->
    <div class="my-bookings">
      <div class="section-header">
        <h3 class="section-title">我的预约</h3>
        <el-button type="primary" link @click="$router.push('/user/booking')">查看全部</el-button>
      </div>
      <div v-if="recentBookings.length > 0" class="booking-list">
        <div
          v-for="booking in recentBookings"
          :key="booking.id"
          class="booking-card"
        >
          <div class="booking-info">
            <div class="booking-header">
              <span class="booking-no">{{ booking.bookingNo }}</span>
              <el-tag :type="getStatusType(booking.status)" size="small">
                {{ getStatusText(booking.status) }}
              </el-tag>
            </div>
            <div class="booking-details">
              <p class="booking-venue">{{ booking.venueName }} · {{ booking.courtName }}</p>
              <p class="booking-time">{{ formatTimeRange(booking.startTime, booking.endTime) }}</p>
              <p class="booking-amount">¥{{ formatCurrency(booking.orderAmount) }}</p>
            </div>
          </div>
          <div class="booking-actions">
            <el-button
              v-if="booking.status === 1"
              type="primary"
              size="small"
              @click="handlePay(booking)"
            >
              立即支付
            </el-button>
            <el-button
              v-if="booking.status === 2 || booking.status === 3"
              type="warning"
              size="small"
              @click="handleCancel(booking)"
            >
              取消预约
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无预约记录" :image-size="120" />
    </div>

    <!-- 订单统计 -->
    <div class="order-stats">
      <h3 class="section-title">订单统计</h3>
      <div class="stats-grid">
        <div class="stat-card stat-pending">
          <div class="stat-icon">
            <el-icon :size="24"><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ orderStats.pending }}</div>
            <div class="stat-label">待支付</div>
          </div>
        </div>
        <div class="stat-card stat-processing">
          <div class="stat-icon">
            <el-icon :size="24"><Loading /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ orderStats.processing }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </div>
        <div class="stat-card stat-finished">
          <div class="stat-icon">
            <el-icon :size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ orderStats.finished }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UserFilled,
  Money,
  DataLine,
  ShoppingBag,
  Tickets,
  Trophy,
  Clock,
  Loading,
  CircleCheck,
  Tools
} from '@element-plus/icons-vue'
import { getBookingList, getBookingStatistics, payMemberBooking, updateBookingStatus } from '@/api/booking'
import { getCurrentMember } from '@/api/member'
import { getUserInfo, resolveAvatarUrl } from '@/utils/auth'
import { getRoleName } from '@/utils/roleHelper'

const router = useRouter()

const userName = ref('用户')
const dashboardAvatarUrl = ref('') // 首页展示的头像，与设置页更新后同步
const balance = ref(0)
const bookingCount = ref(0)
const memberLevel = ref(0) // 会员等级 0-5，0 表示非会员/普通
const userRoleLabel = ref('普通用户') // 用户角色标签：普通用户/会员
const recentBookings = ref([])
const orderStats = ref({
  pending: 0,
  processing: 0,
  finished: 0
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})

const getMemberRoleLabel = (memberType, level) => {
  if (memberType === 'NORMAL' || !memberType) {
    return '普通用户'
  }
  const safeLevel = Number(level) || 0
  if (safeLevel >= 3) {
    return 'SVIP用户'
  }
  if (safeLevel > 0) {
    return `会员 Lv.${safeLevel}`
  }
  return '会员'
}

// VIP 等级展示文案（与后端 MemberServiceImpl 等逻辑一致：1 银卡 2 金卡 3+ VIP）
const vipLevelLabel = computed(() => {
  const level = memberLevel.value
  if (level >= 3) return `VIP Lv.${level}`
  if (level === 2) return '金卡会员'
  if (level === 1) return '银卡会员'
  return '普通用户'
})

// VIP 等级样式类（每级独立颜色与视觉效果）
const vipLevelClass = computed(() => {
  const level = Math.min(5, Math.max(0, memberLevel.value))
  return `vip-lv${level}`
})

const iconComponents = {
  DataLine,
  ShoppingBag,
  Tickets,
  Trophy,
  Tools
}

const quickActions = [
  {
    title: '场地预订',
    desc: '预订羽毛球场地',
    path: '/user/booking',
    icon: DataLine,
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '器材租借',
    desc: '租借运动器材',
    path: '/user/equipment',
    icon: ShoppingBag,
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    title: '穿线服务',
    desc: '专业球拍穿线',
    path: '/user/stringing',
    icon: Tools,
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  },
  {
    title: '课程预约',
    desc: '预约培训课程',
    path: '/user/course',
    icon: Tickets,
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    title: '赛事报名',
    desc: '报名参加赛事',
    path: '/user/tournament',
    icon: Trophy,
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
]

const formatCurrency = (val) => {
  if (val === null || val === undefined) return '0.00'
  const num = Number(val) || 0
  return num.toFixed(2)
}

const formatTimeRange = (start, end) => {
  if (!start || !end) return '-'
  const startDate = new Date(start)
  const endDate = new Date(end)
  const dateStr = `${startDate.getMonth() + 1}月${startDate.getDate()}日`
  const startTime = `${String(startDate.getHours()).padStart(2, '0')}:${String(startDate.getMinutes()).padStart(2, '0')}`
  const endTime = `${String(endDate.getHours()).padStart(2, '0')}:${String(endDate.getMinutes()).padStart(2, '0')}`
  return `${dateStr} ${startTime}-${endTime}`
}

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: '' }
  return map[status] || 'info'
}

const loadUserInfo = async () => {
  try {
    const userInfo = getUserInfo()
    if (userInfo) {
      userName.value = userInfo.nickname || userInfo.username || '用户'
      // 根据用户角色设置标签：USER -> 普通用户，MEMBER -> 会员
      userRoleLabel.value = getRoleName(userInfo.role) || '普通用户'
      // 同步头像（设置页更换头像后会更新 localStorage，返回首页时需重新读取）
      dashboardAvatarUrl.value = resolveAvatarUrl(userInfo.avatar) || ''
    } else {
      dashboardAvatarUrl.value = ''
    }
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }
}

const loadBalance = async () => {
  try {
    const res = await getCurrentMember()
    if (res.code === 200 && res.data) {
      balance.value = res.data.balance != null ? Number(res.data.balance) : 0
      memberLevel.value = res.data.memberLevel != null ? Number(res.data.memberLevel) : 0
      userRoleLabel.value = getMemberRoleLabel(res.data.memberType, res.data.memberLevel)
    }
  } catch (e) {
    console.error('获取余额失败:', e)
  }
}

const loadBookings = async () => {
  try {
    // 预约列表（最近 5 条）
    const listRes = await getBookingList({ page: 1, size: 5 })
    if (listRes.code === 200 && listRes.data) {
      recentBookings.value = listRes.data.data || []
    }
    // 统计接口与后端一致：用户端仅统计本人预约，与管理端数据口径一致
    const statRes = await getBookingStatistics()
    if (statRes.code === 200 && statRes.data) {
      const s = statRes.data
      bookingCount.value = s.total ?? 0
      orderStats.value = {
        pending: s.pending ?? 0,
        processing: s.ongoing ?? 0,
        finished: s.finished ?? 0
      }
    }
  } catch (e) {
    console.error('获取预约列表/统计失败:', e)
  }
}

const handlePay = (booking) => {
  ElMessageBox.confirm(
    `确认使用余额支付该预约订单吗？\n订单金额：¥${formatCurrency(booking.orderAmount)}`,
    '预约支付确认',
    {
      type: 'warning',
      confirmButtonText: '确认支付',
      cancelButtonText: '稍后支付'
    }
  ).then(async () => {
    try {
      const res = await payMemberBooking({
        bookingId: booking.id,
        paymentMethod: 'BALANCE'
      })
      if (res.code === 200) {
        ElMessage.success('支付成功')
        await Promise.all([loadBalance(), loadBookings()])
      } else {
        ElMessage.error(res.message || '支付失败')
      }
    } catch (e) {
      console.error('预约支付失败:', e)
      ElMessage.error(e.response?.data?.message || e.message || '支付失败，请稍后重试')
    }
  }).catch(() => {})
}

const handleCancel = async (booking) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      type: 'warning'
    })
    
    const res = await updateBookingStatus(booking.id, 0)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadBookings()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消预约失败:', e)
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}

onMounted(() => {
  loadUserInfo()
  loadBalance()
  loadBookings()
})

// 从设置/个人中心返回首页时重新拉取用户信息，使刚更换的头像能立即显示
onActivated(() => {
  loadUserInfo()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 减少动画（尊重用户的偏好设置） */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 用户信息卡片 - 增强渐变和动画 */
.user-info-card {
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
  animation: cardEntrance 0.6s ease-out;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.user-info-card::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: shimmer 8s ease-in-out infinite;
  pointer-events: none;
}

.user-info-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(ellipse at 20% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  opacity: 0.6;
  pointer-events: none;
}

@keyframes cardEntrance {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes shimmer {
  0%, 100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(180deg);
  }
}

.user-info-card:hover {
  transform: translateY(-4px) scale(1.01);
  box-shadow: 0 16px 48px rgba(37, 99, 235, 0.4), 0 6px 16px rgba(0, 0, 0, 0.15);
}

.user-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  border: 4px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2), 0 0 0 4px rgba(255, 255, 255, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.user-info-card:hover .user-avatar {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3), 0 0 0 6px rgba(255, 255, 255, 0.15);
}

.user-badge {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.user-info-card:hover .user-badge {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  font-family: 'Poppins', 'Inter', sans-serif;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.user-greeting {
  font-size: 16px;
  margin: 0 0 16px 0;
  opacity: 0.9;
}

.user-stats {
  display: flex;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.8;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  font-family: 'Poppins', sans-serif;
}

.stat-value.balance {
  font-size: 32px;
  color: #FFD700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3), 0 0 20px rgba(255, 215, 0, 0.4);
  animation: pulseGlow 2s ease-in-out infinite;
  position: relative;
  z-index: 1;
}

@keyframes pulseGlow {
  0%, 100% {
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3), 0 0 20px rgba(255, 215, 0, 0.4);
  }
  50% {
    text-shadow: 0 2px 12px rgba(0, 0, 0, 0.4), 0 0 30px rgba(255, 215, 0, 0.6);
  }
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-actions-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  position: relative;
  z-index: 1;
}

/* VIP 等级徽章 - 加大尺寸与视觉权重 */
.vip-level-display {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  padding: 10px 26px;
  border-radius: 28px;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.02em;
  backdrop-filter: blur(12px);
  border: 2px solid;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

/* Lv.0 普通用户 - 柔和白/灰 */
.vip-level-display.vip-lv0 {
  color: rgba(255, 255, 255, 0.95);
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

/* Lv.1 银卡 - 铜色 */
.vip-level-display.vip-lv1 {
  color: #fff8e7;
  background: linear-gradient(135deg, rgba(184, 115, 51, 0.5) 0%, rgba(139, 90, 43, 0.6) 100%);
  border-color: rgba(205, 127, 50, 0.8);
  text-shadow: 0 0 12px rgba(205, 127, 50, 0.6), 0 2px 4px rgba(0, 0, 0, 0.3);
  box-shadow: 0 4px 20px rgba(184, 115, 51, 0.35);
}

/* Lv.2 金卡 - 银色 */
.vip-level-display.vip-lv2 {
  color: #f0f4f8;
  background: linear-gradient(135deg, rgba(192, 192, 192, 0.45) 0%, rgba(169, 169, 169, 0.5) 100%);
  border-color: rgba(220, 220, 220, 0.9);
  text-shadow: 0 0 14px rgba(255, 255, 255, 0.5), 0 2px 4px rgba(0, 0, 0, 0.25);
  box-shadow: 0 4px 20px rgba(192, 192, 192, 0.4);
}

/* Lv.3 VIP - 金色 */
.vip-level-display.vip-lv3 {
  color: #ffd700;
  background: linear-gradient(135deg, rgba(230, 184, 0, 0.5) 0%, rgba(218, 165, 32, 0.6) 100%);
  border-color: rgba(255, 215, 0, 0.85);
  text-shadow: 0 0 16px rgba(255, 215, 0, 0.7), 0 2px 4px rgba(0, 0, 0, 0.3);
  box-shadow: 0 4px 24px rgba(255, 215, 0, 0.4);
}

/* Lv.4 VIP - 亮金/琥珀 */
.vip-level-display.vip-lv4 {
  color: #ffe44d;
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.55) 0%, rgba(255, 215, 0, 0.6) 100%);
  border-color: rgba(255, 228, 77, 0.9);
  text-shadow: 0 0 20px rgba(255, 228, 77, 0.8), 0 2px 4px rgba(0, 0, 0, 0.25);
  box-shadow: 0 6px 28px rgba(255, 193, 7, 0.5);
}

/* Lv.5 VIP - 尊贵金 + 强发光 */
.vip-level-display.vip-lv5 {
  color: #fffacd;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.6) 0%, rgba(255, 193, 7, 0.7) 50%, rgba(255, 215, 0, 0.6) 100%);
  border-color: rgba(255, 250, 205, 0.95);
  text-shadow: 0 0 24px rgba(255, 250, 205, 0.9), 0 0 40px rgba(255, 215, 0, 0.5), 0 2px 4px rgba(0, 0, 0, 0.2);
  box-shadow: 0 6px 32px rgba(255, 215, 0, 0.55), 0 0 40px rgba(255, 215, 0, 0.25);
  animation: vipGlow 2.5s ease-in-out infinite;
}

@keyframes vipGlow {
  0%, 100% {
    box-shadow: 0 6px 32px rgba(255, 215, 0, 0.55), 0 0 40px rgba(255, 215, 0, 0.25);
  }
  50% {
    box-shadow: 0 8px 36px rgba(255, 215, 0, 0.7), 0 0 56px rgba(255, 215, 0, 0.35);
  }
}

.action-btn {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(12px);
  border: 2px solid rgba(255, 255, 255, 0.4);
  color: #ffffff;
  font-weight: 600;
  padding: 12px 24px;
  border-radius: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 255, 255, 0.6);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
}

.action-btn:hover::before {
  width: 200px;
  height: 200px;
}

.action-btn:active {
  transform: translateY(-1px) scale(1.02);
}

/* 快捷功能 */
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

/* 确保在宽屏幕上5个卡片在同一行显示 */
@media (min-width: 1024px) {
  .action-grid {
    grid-template-columns: repeat(5, 1fr);
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

.action-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.05), transparent);
  transition: left 0.5s ease;
}

.action-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15), 0 4px 12px rgba(37, 99, 235, 0.2);
  border-color: var(--color-primary-light, #60A5FA);
}

.action-card:hover::before {
  left: 100%;
}

.action-card:active {
  transform: translateY(-3px) scale(1.01);
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
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.action-card:hover .action-icon {
  transform: scale(1.15) rotate(5deg);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.action-card .action-icon .el-icon {
  transition: transform 0.3s ease;
}

.action-card:hover .action-icon .el-icon {
  transform: scale(1.1);
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

/* 我的预约 */
.my-bookings {
  margin-bottom: 32px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.booking-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.booking-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--color-primary, #2563EB);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.booking-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(37, 99, 235, 0.15);
  border-color: var(--color-primary-light, #60A5FA);
  transform: translateX(4px);
}

.booking-card:hover::before {
  transform: scaleY(1);
}

.booking-info {
  flex: 1;
}

.booking-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.booking-no {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Code', monospace;
}

.booking-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.booking-venue {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.booking-time {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

.booking-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  margin: 4px 0 0 0;
  font-family: 'Poppins', sans-serif;
}

/* 订单统计 */
.order-stats {
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

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.03) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: var(--color-primary-light, #60A5FA);
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
  animation: iconPulse 1.5s ease-in-out infinite;
}

@keyframes iconPulse {
  0%, 100% {
    transform: scale(1.1) rotate(5deg);
  }
  50% {
    transform: scale(1.15) rotate(5deg);
  }
}

.stat-pending .stat-icon {
  background: linear-gradient(135deg, #FEF3C7 0%, #FCD34D 100%);
  color: #F59E0B;
}

.stat-processing .stat-icon {
  background: linear-gradient(135deg, #DBEAFE 0%, #60A5FA 100%);
  color: #2563EB;
}

.stat-finished .stat-icon {
  background: linear-gradient(135deg, #D1FAE5 0%, #34D399 100%);
  color: #10B981;
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
  transition: all 0.3s ease;
  display: inline-block;
}

.stat-card:hover .stat-content .stat-value {
  transform: scale(1.05);
  color: var(--color-primary, #2563EB);
}

.stat-content .stat-label {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
}

/* 响应式 - 移动端优化 */
@media (max-width: 768px) {
  .user-info-card {
    flex-direction: column;
    text-align: center;
    padding: 24px 16px;
  }
  
  .user-stats {
    justify-content: center;
    flex-wrap: wrap;
    gap: 20px;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .action-card {
    padding: 20px;
  }
  
  .booking-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .booking-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  /* 移动端触摸优化 */
  .action-card,
  .booking-card,
  .stat-card {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
}

@media (max-width: 480px) {
  .user-info-card {
    padding: 20px 12px;
  }
  
  .user-name {
    font-size: 24px;
  }
  
  .stat-value.balance {
    font-size: 28px;
  }
  
  .action-icon {
    width: 48px;
    height: 48px;
  }
}
</style>
