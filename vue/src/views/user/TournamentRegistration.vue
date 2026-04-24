<template>
  <div class="user-tournament">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">赛事报名</h1>
      <p class="page-subtitle">报名参加精彩赛事，展现运动风采</p>
    </div>

    <!-- Tab切换：报名赛事 / 我的报名 -->
    <el-tabs v-model="activeTab" class="tournament-tabs">
      <el-tab-pane label="报名赛事" name="register">
        <!-- 步骤指示器 -->
        <div class="step-indicator" v-if="step > 0">
          <div class="step-indicator-container">
            <div 
              v-for="(stepItem, index) in steps"
              :key="index"
              class="step-item"
              :class="{ 
                active: step > index + 1,
                current: step === index + 1,
                completed: step > index + 1
              }"
            >
              <div class="step-number">
                <el-icon v-if="step > index + 1"><CircleCheck /></el-icon>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div class="step-label">{{ stepItem }}</div>
              <div class="step-line" v-if="index < steps.length - 1"></div>
            </div>
          </div>
        </div>

        <!-- 步骤1: 选择赛事 -->
        <div v-if="step === 1" class="tournament-step">
          <h3 class="step-title">选择赛事</h3>
          <!-- 赛事卡片网格 -->
          <div class="tournament-grid">
            <div
              v-for="tournament in tournamentList"
              :key="tournament.id"
              class="tournament-card"
              :class="{ 
                unavailable: tournament.status !== 1 || tournament.registrationStatus !== 1,
                selected: selectedTournament?.id === tournament.id
              }"
              @click="selectTournament(tournament)"
            >
              <div class="tournament-image">
                <el-icon :size="48"><Trophy /></el-icon>
              </div>
              <div class="tournament-info">
                <h4 class="tournament-name">{{ tournament.tournamentName }}</h4>
                <div class="tournament-meta">
                  <span class="tournament-date">{{ formatDate(tournament.startTime) }}</span>
                  <span class="tournament-venue">{{ tournament.venueName || '待定' }}</span>
                </div>
                <div class="tournament-details">
                  <div class="detail-item">
                    <span class="detail-label">报名费</span>
                    <span class="detail-value price">¥{{ formatCurrency(tournament.registrationFee || 0) }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label">状态</span>
                    <el-tag :type="getTournamentStatusType(tournament)" size="small">
                      {{ getTournamentStatusText(tournament) }}
                    </el-tag>
                  </div>
                </div>
                <p class="tournament-desc" v-if="tournament.description">{{ tournament.description }}</p>
              </div>
            </div>
          </div>
          <el-empty v-if="tournamentList.length === 0" description="暂无可报名赛事" :image-size="120" />
        </div>

        <!-- 步骤2: 填写信息 -->
        <div v-if="step === 2" class="tournament-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 1" text>返回选择赛事</el-button>
            <h3 class="step-title">填写报名信息</h3>
          </div>
          <div class="info-section">
            <div class="selected-info-card">
              <div class="info-item">
                <span class="info-label">当前会员</span>
                <span class="info-value">{{ currentMember?.memberName || '未获取到会员信息' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">赛事名称</span>
                <span class="info-value">{{ selectedTournament?.tournamentName }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">赛事时间</span>
                <span class="info-value">{{ formatTimeRange(selectedTournament?.startTime, selectedTournament?.endTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">报名费用</span>
                <span class="info-value price">¥{{ formatCurrency(selectedTournament?.registrationFee || 0) }}</span>
              </div>
            </div>
            <el-form :model="registrationForm" label-width="120px" class="info-form">
              <el-form-item v-if="isDoublesOrMixed(selectedTournament?.tournamentType)" label="搭档">
                <el-select v-model="registrationForm.partnerId" placeholder="请选择搭档会员" filterable clearable style="width: 100%">
                  <el-option
                    v-for="member in partnerOptions"
                    :key="member.id"
                    :label="`${member.memberName}${member.phone ? `（${member.phone}）` : ''}`"
                    :value="member.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="补充说明">
                <el-input
                  v-model="registrationForm.remark"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  placeholder="可选，填写给会长的补充说明"
                />
              </el-form-item>
            </el-form>
            <div class="step-actions">
              <el-button @click="step = 1" size="large">返回</el-button>
              <el-button type="primary" @click="step = 3" size="large">下一步</el-button>
            </div>
          </div>
        </div>

        <!-- 步骤3: 确认报名 -->
        <div v-if="step === 3" class="tournament-step">
          <div class="step-header">
            <el-button :icon="ArrowLeft" @click="step = 2" text>返回填写信息</el-button>
            <h3 class="step-title">确认报名信息</h3>
          </div>
          <div class="confirm-section">
            <div class="confirm-card">
              <h4 class="confirm-title">报名信息</h4>
              <div class="confirm-info">
                <div class="confirm-item">
                  <span class="confirm-label">当前会员</span>
                  <span class="confirm-value">{{ currentMember?.memberName || '-' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">赛事名称</span>
                  <span class="confirm-value">{{ selectedTournament?.tournamentName }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">赛事时间</span>
                  <span class="confirm-value">{{ formatTimeRange(selectedTournament?.startTime, selectedTournament?.endTime) }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">报名费用</span>
                  <span class="confirm-value price">¥{{ formatCurrency(selectedTournament?.registrationFee || 0) }}</span>
                </div>
                <div v-if="isDoublesOrMixed(selectedTournament?.tournamentType)" class="confirm-item">
                  <span class="confirm-label">搭档</span>
                  <span class="confirm-value">{{ getPartnerName(registrationForm.partnerId) }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">补充说明</span>
                  <span class="confirm-value">{{ registrationForm.remark || '无' }}</span>
                </div>
                <p v-if="isDoublesOrMixed(selectedTournament?.tournamentType)" class="confirm-fee-hint">双打/混双为一对总价（单人费×2），由您一次性支付，搭档不扣款。</p>
              </div>
              <div class="confirm-actions">
                <el-button @click="step = 2" size="large">返回修改</el-button>
                <el-button type="primary" :loading="submitting" @click="submitRegistration" size="large">确认报名</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的报名" name="my-registrations">
        <div class="my-registrations-section">
          <div class="filter-bar">
            <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 150px" @change="loadMyRegistrations">
              <el-option label="待支付" value="1" />
              <el-option label="已支付" value="2" />
              <el-option label="已参赛" value="3" />
              <el-option label="已取消" value="0" />
            </el-select>
          </div>

          <div v-if="myRegistrations.length > 0" class="registrations-list">
            <div
              v-for="registration in myRegistrations"
              :key="registration.id"
              class="registration-item-card"
            >
              <div class="registration-main">
                <div class="registration-header">
                  <span class="registration-no">{{ registration.registrationNo }}</span>
                  <el-tag :type="getRegistrationStatusType(registration.status)" size="small">
                    {{ getRegistrationStatusText(registration.status) }}
                  </el-tag>
                </div>
                <div class="registration-details">
                  <p class="registration-tournament">{{ registration.tournamentName }}</p>
                  <p class="registration-time">赛事时间：{{ formatTimeRange(registration.tournamentStartTime, registration.tournamentEndTime) }}</p>
                  <p class="registration-amount">报名费：¥{{ formatCurrency(registration.registrationFee ?? registration.entryFee) }}</p>
                </div>
              </div>
              <div class="registration-actions">
                <el-button
                  v-if="registration.status === 1"
                  type="primary"
                  size="small"
                  @click="handlePay(registration)"
                >
                  立即支付
                </el-button>
                <el-button
                  v-if="registration.status === 1 || registration.status === 2"
                  type="warning"
                  size="small"
                  @click="handleCancel(registration)"
                >
                  取消报名
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无报名记录" :image-size="120" />
        </div>
      </el-tab-pane>
    </el-tabs>

  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trophy, CircleCheck, ArrowLeft } from '@element-plus/icons-vue'
import { getTournamentList } from '@/api/tournament'
import { getCurrentMember } from '@/api/member'
import {
  getTournamentRegistrationList,
  addTournamentRegistration,
  updateTournamentRegistrationStatus,
  processMemberTournamentRegistrationPayment,
  getTournamentRegistrationMembers
} from '@/api/tournamentRegistration'

const activeTab = ref('register')
const step = ref(1) // 1-选择赛事, 2-填写信息, 3-确认报名

// 步骤配置
const steps = ['选择赛事', '填写信息', '确认报名']

const filterStatus = ref(null)
const tournamentList = ref([])
const myRegistrations = ref([])
const selectedTournament = ref(null)
const submitting = ref(false)
const currentBalance = ref(0)
const currentMember = ref(null)
const partnerOptions = ref([])

const registrationForm = ref({
  tournamentId: null,
  partnerId: null,
  remark: ''
})

const formatCurrency = (val) => {
  if (val === null || val === undefined) return '0.00'
  const num = Number(val) || 0
  return num.toFixed(2)
}

const formatDate = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return `${date.getMonth() + 1}月${date.getDate()}日`
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

const getTournamentStatusText = (tournament) => {
  if (tournament.status !== 1) return '已结束'
  if (tournament.registrationStatus === 1) return '报名中'
  if (tournament.registrationStatus === 2) return '报名已满'
  return '报名已截止'
}

const getTournamentStatusType = (tournament) => {
  if (tournament.status !== 1) return 'info'
  if (tournament.registrationStatus === 1) return 'success'
  return 'warning'
}

const getRegistrationStatusText = (status) => {
  const map = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '已参赛' }
  return map[status] || '未知'
}

const getRegistrationStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary' }
  return map[status] || 'info'
}

const isDoublesOrMixed = (type) => type === 'DOUBLE' || type === 'MIXED'

const loadTournaments = async () => {
  try {
    const res = await getTournamentList({ page: 1, size: 100 })
    if (res.code === 200) {
      const list = res.data?.data || res.data?.records || res.data || []
      // 兼容后端字段：tournamentStart/tournamentEnd/entryFee/tournamentType，双打/混双报名费×2
      const baseFee = (t) => {
        const n = Number(t.registrationFee ?? t.entryFee ?? 0)
        return Number.isNaN(n) || n < 0 ? 0 : n
      }
      tournamentList.value = (Array.isArray(list) ? list : []).map((t) => {
        let fee = baseFee(t)
        if (isDoublesOrMixed(t.tournamentType)) fee = Math.round(fee * 2 * 100) / 100
        return {
          ...t,
          startTime: t.startTime ?? t.tournamentStart,
          endTime: t.endTime ?? t.tournamentEnd,
          registrationFee: fee,
          registrationStatus: t.registrationStatus ?? (t.status === 1 && (t.currentParticipants ?? 0) < (t.maxParticipants ?? 0) ? 1 : 2)
        }
      })
    }
  } catch (e) {
    console.error('加载赛事列表失败:', e)
    ElMessage.error('加载赛事列表失败')
  }
}

const selectTournament = (tournament) => {
  if (tournament.status !== 1 || tournament.registrationStatus !== 1) {
    ElMessage.warning('该赛事暂不可报名')
    return
  }
  selectedTournament.value = tournament
  registrationForm.value = {
    tournamentId: tournament.id,
    partnerId: null,
    remark: ''
  }
  loadPartnerOptions()
  step.value = 2
}

const resetRegistrationForm = () => {
  selectedTournament.value = null
  registrationForm.value = {
    tournamentId: null,
    partnerId: null,
    remark: ''
  }
  partnerOptions.value = []
  step.value = 1
}

const submitRegistration = async () => {
  if (isDoublesOrMixed(selectedTournament.value?.tournamentType) && !registrationForm.value.partnerId) {
    ElMessage.warning('双打/混双赛事请选择搭档')
    step.value = 2
    return
  }

  submitting.value = true
  try {
    const res = await addTournamentRegistration({
      tournamentId: registrationForm.value.tournamentId,
      partnerId: registrationForm.value.partnerId,
      remark: registrationForm.value.remark
    })
    
    if (res.code === 200) {
      ElMessage.success('报名成功！')
      resetRegistrationForm()
      loadTournaments()
      activeTab.value = 'my-registrations'
      loadMyRegistrations()
    } else {
      ElMessage.error(res.message || '报名失败')
    }
  } catch (e) {
    console.error('提交报名失败:', e)
    ElMessage.error('报名失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const loadMyRegistrations = async () => {
  try {
    const params = { page: 1, size: 20 }
    if (filterStatus.value) {
      params.status = parseInt(filterStatus.value)
    }
    const res = await getTournamentRegistrationList(params)
    if (res.code === 200) {
      myRegistrations.value = res.data?.data || []
    }
  } catch (e) {
    console.error('加载报名列表失败:', e)
  }
}

const loadCurrentMemberInfo = async () => {
  try {
    const res = await getCurrentMember()
    if (res.code === 200 && res.data) {
      currentMember.value = res.data
      currentBalance.value = Number(res.data.balance) || 0
    }
  } catch (e) {
    console.error('获取当前会员余额失败:', e)
  }
}

const loadPartnerOptions = async () => {
  try {
    const res = await getTournamentRegistrationMembers('')
    if (res.code === 200) {
      const list = Array.isArray(res.data) ? res.data : []
      partnerOptions.value = list.filter((item) => item.id !== currentMember.value?.id)
    }
  } catch (e) {
    console.error('加载搭档列表失败:', e)
    partnerOptions.value = []
  }
}

const getPartnerName = (partnerId) => {
  if (!partnerId) return '未选择'
  return partnerOptions.value.find((item) => item.id === partnerId)?.memberName || `会员 #${partnerId}`
}

const handlePay = (registration) => {
  const fee = Number(registration.registrationFee ?? registration.entryFee ?? 0)
  ElMessageBox.confirm(
    `确认使用余额支付该赛事报名吗？\n报名费用：¥${formatCurrency(fee)}\n当前余额：¥${formatCurrency(currentBalance.value)}`,
    '赛事报名支付确认',
    {
      type: 'warning',
      confirmButtonText: '确认支付',
      cancelButtonText: '稍后支付'
    }
  ).then(async () => {
    try {
      const res = await processMemberTournamentRegistrationPayment(registration.id, 'BALANCE')
      if (res.code === 200) {
        ElMessage.success('支付成功')
        await Promise.all([loadCurrentMemberInfo(), loadMyRegistrations(), loadTournaments()])
      } else {
        ElMessage.error(res.message || '支付失败')
      }
    } catch (e) {
      console.error('赛事报名支付失败:', e)
      ElMessage.error(e.response?.data?.message || e.message || '支付失败，请稍后重试')
    }
  }).catch(() => {})
}

const handleCancel = async (registration) => {
  try {
    await ElMessageBox.confirm('确定要取消这个报名吗？', '提示', {
      type: 'warning'
    })
    
    const res = await updateTournamentRegistrationStatus(registration.id, 0)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadMyRegistrations()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消报名失败:', e)
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'my-registrations') {
    loadMyRegistrations()
  }
})

onMounted(() => {
  loadTournaments()
  loadCurrentMemberInfo()
  if (activeTab.value === 'my-registrations') {
    loadMyRegistrations()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-tournament {
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

/* 标题动画定义 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes titleShimmer {
  0% {
    background-position: -200% center;
  }
  100% {
    background-position: 200% center;
  }
}

@keyframes gradientShift {
  0%, 100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

.page-header {
  margin-bottom: 32px;
  padding: 24px 0;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(180deg, 
    var(--color-primary, #2563EB) 0%, 
    var(--color-secondary, #8B5CF6) 100%);
  border-radius: 2px;
  opacity: 0.6;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    var(--color-border, rgba(226, 232, 240, 0.5)) 50%, 
    transparent 100%);
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  font-family: 'Poppins', 'Inter', sans-serif;
  position: relative;
  padding-left: 16px;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards;
  opacity: 0;
  
  /* 渐变文字效果 */
  background: linear-gradient(135deg, 
    var(--color-text-primary, #1E293B) 0%, 
    var(--color-primary, #2563EB) 50%,
    var(--color-secondary, #8B5CF6) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  
  /* 文字阴影（作为后备） */
  text-shadow: 0 2px 8px rgba(37, 99, 235, 0.15);
  
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, 
    var(--color-primary, #2563EB) 0%, 
    var(--color-secondary, #8B5CF6) 100%);
  border-radius: 2px;
  opacity: 0.8;
}

.page-title:hover {
  transform: translateX(4px);
  background-position: 100% 50%;
}

.page-subtitle {
  font-size: 17px;
  margin: 0;
  padding-left: 16px;
  line-height: 1.6;
  letter-spacing: 0.3px;
  animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s forwards;
  opacity: 0;
  
  /* 渐变文字效果 */
  background: linear-gradient(135deg, 
    var(--color-text-secondary, #64748B) 0%, 
    var(--color-primary, #2563EB) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: var(--color-text-secondary, #64748B);
  
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.page-subtitle:hover {
  transform: translateX(4px);
  opacity: 1;
}

/* 深色模式适配 */
html.theme-dark-mode .page-title {
  background: linear-gradient(135deg, 
    var(--color-text-primary, #F1F5F9) 0%, 
    var(--color-primary, #60A5FA) 50%,
    var(--color-secondary, #A78BFA) 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 8px rgba(96, 165, 250, 0.2);
}

html.theme-dark-mode .page-subtitle {
  background: linear-gradient(135deg, 
    var(--color-text-secondary, #94A3B8) 0%, 
    var(--color-primary, #60A5FA) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 无障碍：减少动画 */
@media (prefers-reduced-motion: reduce) {
  .page-title,
  .page-subtitle {
    animation: none;
    opacity: 1;
  }
  
  .page-title:hover,
  .page-subtitle:hover {
    transform: none;
  }
}

.tournament-tabs {
  margin-top: 24px;
}

/* 步骤指示器 */
.step-indicator {
  margin-bottom: 32px;
  padding: 24px;
  background: var(--color-card-bg, #FFFFFF);
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.step-indicator-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  max-width: 800px;
  margin: 0 auto;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background, #F1F5F9);
  border: 3px solid var(--color-border, #E2E8F0);
  color: var(--color-text-secondary, #64748B);
  font-weight: 600;
  font-size: 16px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.step-item.current .step-number {
  background: var(--color-primary, #2563EB);
  border-color: var(--color-primary, #2563EB);
  color: #ffffff;
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
  animation: pulseStep 2s ease-in-out infinite;
}

.step-item.completed .step-number {
  background: var(--color-success, #10B981);
  border-color: var(--color-success, #10B981);
  color: #ffffff;
  transform: scale(1.05);
}

.step-item.completed .step-number .el-icon {
  font-size: 20px;
}

@keyframes pulseStep {
  0%, 100% {
    box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
  }
  50% {
    box-shadow: 0 4px 24px rgba(37, 99, 235, 0.6);
  }
}

.step-label {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
  transition: all 0.3s ease;
  text-align: center;
}

.step-item.current .step-label {
  color: var(--color-primary, #2563EB);
  font-weight: 600;
}

.step-item.completed .step-label {
  color: var(--color-success, #10B981);
}

.step-line {
  position: absolute;
  top: 20px;
  left: calc(50% + 20px);
  right: calc(-50% + 20px);
  height: 3px;
  background: var(--color-border, #E2E8F0);
  z-index: 0;
  transition: all 0.4s ease;
}

.step-item.completed + .step-item .step-line {
  background: var(--color-success, #10B981);
}

.step-item.current .step-line {
  background: linear-gradient(90deg, var(--color-success, #10B981) 0%, var(--color-border, #E2E8F0) 100%);
}

/* 步骤内容 */
.tournament-step {
  padding: 24px 0;
  animation: fadeInUp 0.4s ease-out;
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 24px 0;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.step-header .step-title {
  margin: 0;
}

.info-section,
.confirm-section {
  max-width: 800px;
  margin: 0 auto;
}

.selected-info-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  animation: slideIn 0.4s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
}

.info-value {
  font-size: 16px;
  color: var(--color-text-primary, #1E293B);
  font-weight: 600;
}

.info-value.price {
  color: var(--color-primary, #2563EB);
  font-size: 18px;
}

.info-form {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
}

.confirm-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-primary, #2563EB);
  border-radius: 16px;
  padding: 32px;
  animation: scaleIn 0.4s ease-out;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.confirm-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 24px 0;
  text-align: center;
}

.confirm-info {
  margin-bottom: 32px;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.confirm-item:last-child {
  border-bottom: none;
}

.confirm-fee-hint {
  margin: 12px 0 0;
  padding: 10px 12px;
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  background: var(--color-fill-secondary, #F1F5F9);
  border-radius: 8px;
}

.confirm-label {
  font-size: 15px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
}

.confirm-value {
  font-size: 16px;
  color: var(--color-text-primary, #1E293B);
  font-weight: 600;
}

.confirm-value.price {
  color: var(--color-primary, #2563EB);
  font-size: 20px;
}

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

.tournament-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.tournament-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 20px;
  padding: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.tournament-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.05), transparent);
  transition: left 0.5s ease;
}

.tournament-card:hover:not(.unavailable) {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(37, 99, 235, 0.2);
  border-color: var(--color-primary-light, #60A5FA);
}

.tournament-card.selected {
  border-color: var(--color-primary, #2563EB);
  background: linear-gradient(135deg, var(--color-card-bg, #FFFFFF) 0%, rgba(37, 99, 235, 0.05) 100%);
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.2);
  animation: selectedPulse 2s ease-in-out infinite;
}

@keyframes selectedPulse {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(37, 99, 235, 0.2);
  }
  50% {
    box-shadow: 0 4px 28px rgba(37, 99, 235, 0.3);
  }
}

.tournament-card:hover:not(.unavailable)::before {
  left: 100%;
}

.tournament-card.unavailable {
  opacity: 0.5;
  filter: grayscale(0.6);
  cursor: not-allowed;
}

.tournament-image {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  background: linear-gradient(135deg, #F59E0B 0%, #F97316 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  margin: 0 auto;
  box-shadow: 0 6px 20px rgba(245, 158, 11, 0.35);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.tournament-card:hover:not(.unavailable) .tournament-image {
  transform: scale(1.1) rotate(-5deg);
  box-shadow: 0 8px 28px rgba(245, 158, 11, 0.45);
}

.tournament-card.unavailable .tournament-image {
  background: linear-gradient(135deg, #94A3B8 0%, #64748B 100%);
  filter: grayscale(0.8);
}

.tournament-info {
  text-align: center;
}

.tournament-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 12px 0;
}

.tournament-meta {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
}

.tournament-details {
  display: flex;
  justify-content: space-around;
  gap: 16px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.detail-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.detail-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
}

.detail-value.price {
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
  font-size: 20px;
  font-weight: 700;
  transition: all 0.3s ease;
}

.tournament-card:hover:not(.unavailable) .detail-value.price {
  transform: scale(1.1);
}

.tournament-desc {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 12px 0 0 0;
  text-align: left;
  line-height: 1.5;
}

.tournament-actions {
  display: flex;
  justify-content: center;
}

.my-registrations-section {
  padding: 24px 0;
}

.filter-bar {
  margin-bottom: 20px;
}

.registrations-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.registration-item-card {
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

.registration-item-card::before {
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

.registration-item-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(37, 99, 235, 0.15);
  border-color: var(--color-primary-light, #60A5FA);
  transform: translateX(4px);
}

.registration-item-card:hover::before {
  transform: scaleY(1);
}

.registration-main {
  flex: 1;
}

.registration-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.registration-no {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Fira Code', monospace;
}

.registration-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.registration-tournament {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.registration-time {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
}

.registration-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  margin: 4px 0 0 0;
  font-family: 'Poppins', sans-serif;
}

.registration-actions {
  display: flex;
  gap: 8px;
}

.estimated-cost {
  display: flex;
  align-items: baseline;
}

.cost-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
}

/* 响应式 - 移动端优化 */
@media (max-width: 768px) {
  .tournament-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .tournament-card {
    padding: 20px;
  }
  
  .registration-item-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .registration-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  /* 移动端触摸优化 */
  .tournament-card,
  .registration-item-card {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
}
</style>
