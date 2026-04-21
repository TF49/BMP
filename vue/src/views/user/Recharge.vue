<template>
  <div class="user-recharge">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">账户充值</h1>
      <p class="page-subtitle">快速充值，享受便捷服务</p>
    </div>

    <div class="recharge-content">
      <!-- 左侧：充值表单和套餐 -->
      <div class="recharge-left">
        <!-- 充值套餐卡片 -->
        <div class="package-section">
          <h3 class="section-title">充值套餐</h3>
          <div class="package-grid">
            <div
              v-for="pkg in packages"
              :key="pkg.amount"
              class="package-card"
              :class="{ selected: form.amount === pkg.amount }"
              @click="selectPackage(pkg)"
            >
              <div class="package-amount">¥{{ pkg.amount }}</div>
              <div class="package-bonus" v-if="pkg.bonus">
                <span class="bonus-text">送¥{{ pkg.bonus }}</span>
              </div>
              <div class="package-desc" v-if="pkg.desc">{{ pkg.desc }}</div>
            </div>
          </div>
        </div>

        <!-- 充值表单 -->
        <div class="recharge-form-section">
          <h3 class="section-title">充值信息</h3>
          <el-form :model="form" label-width="100px" class="recharge-form">
            <el-form-item label="充值金额">
              <el-input-number
                v-model="form.amount"
                :min="1"
                :step="50"
                :precision="2"
                style="width: 100%"
                size="large"
              />
            </el-form-item>
            <el-form-item label="支付方式">
              <el-radio-group v-model="form.method" size="large">
                <el-radio-button label="ALIPAY">
                  <el-icon><Wallet /></el-icon>
                  支付宝
                </el-radio-button>
                <el-radio-button label="WECHAT">
                  <el-icon><Wallet /></el-icon>
                  微信
                </el-radio-button>
                <el-radio-button label="BALANCE">
                  <el-icon><Money /></el-icon>
                  余额
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="submitting"
                :disabled="submitting || !form.amount || form.amount <= 0"
                @click="handleRecharge"
                size="large"
                class="submit-btn"
              >
                立即充值 ¥{{ formatCurrency(form.amount) }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>

      <!-- 右侧：统计和记录 -->
      <div class="recharge-right">
        <!-- 统计卡片 -->
        <div class="stats-card">
          <h3 class="section-title">充值统计</h3>
          <div class="stats-content">
            <div class="stat-item">
              <div class="stat-icon primary">
                <el-icon :size="24"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">累计充值</div>
                <div class="stat-value">¥{{ formatCurrency(totalRechargeAmount) }}</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon success">
                <el-icon :size="24"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">充值次数</div>
                <div class="stat-value">{{ totalRechargeCount }}</div>
              </div>
            </div>
            <div class="stat-item" v-if="lastRechargeTime">
              <div class="stat-icon info">
                <el-icon :size="24"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-label">最近充值</div>
                <div class="stat-value">¥{{ formatCurrency(lastRechargeAmount) }}</div>
                <div class="stat-time">{{ formatDateTime(lastRechargeTime) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近充值记录 -->
        <div class="records-card">
          <div class="card-header">
            <h3 class="section-title">最近充值</h3>
            <el-button type="primary" link @click="showAllRecords = true">查看全部</el-button>
          </div>
          <div v-if="recentRecords.length > 0" class="records-list">
            <div
              v-for="record in recentRecords"
              :key="record.id"
              class="record-item"
            >
              <div class="record-main">
                <div class="record-amount">¥{{ formatCurrency(record.rechargeAmount) }}</div>
                <div class="record-method">{{ methodText(record.rechargeMethod) }}</div>
              </div>
                <div class="record-time">{{ formatDateTime(record.rechargeTime || record.createTime) }}</div>
            </div>
          </div>
          <el-empty v-else description="暂无充值记录" :image-size="100" />
        </div>
      </div>
    </div>

    <!-- 全部记录弹窗 -->
    <el-dialog
      v-model="showAllRecords"
      title="充值记录"
      width="800px"
      class="records-dialog"
    >
      <el-table
        :data="allRecords"
        v-loading="loading"
        stripe
        border
        :header-cell-style="tableHeaderStyle"
        :cell-style="tableCellStyle"
        style="width: 100%"
      >
        <el-table-column label="时间" min-width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.rechargeTime || scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="rechargeAmount" label="金额" min-width="120" align="center">
          <template #default="scope">
            <span class="price-text">¥{{ formatCurrency(scope.row.rechargeAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rechargeMethod" label="方式" min-width="100" align="center">
          <template #default="scope">
            {{ methodText(scope.row.rechargeMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="isLevelUpgraded" label="等级升级" min-width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isLevelUpgraded === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.isLevelUpgraded === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" align="center" />
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchRecords"
          @current-change="fetchRecords"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Money, Wallet, Document, Clock } from '@element-plus/icons-vue'
import { userRecharge, getRechargeRecords } from '@/api/recharge'
import { getCurrentMember } from '@/api/member'

const form = ref({
  amount: 200,
  method: 'ALIPAY'
})

const submitting = ref(false)
const loading = ref(false)
const showAllRecords = ref(false)
const recentRecords = ref([])
const allRecords = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
// 与会员表一致：累计充值金额（来自 /api/member/current），充值次数来自 /api/recharge/records 的 total
const memberTotalRecharge = ref(0)
const totalRecordCount = ref(0)

// 充值套餐
const packages = [
  { amount: 100, bonus: 0, desc: '基础套餐' },
  { amount: 200, bonus: 10, desc: '推荐套餐' },
  { amount: 500, bonus: 50, desc: '超值套餐' },
  { amount: 1000, bonus: 150, desc: '豪华套餐' }
]

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

const tableCellStyle = {
  textAlign: 'center'
}

const methodText = (v) => {
  const map = {
    ALIPAY: '支付宝',
    WECHAT: '微信',
    CASH: '现金',
    BANK: '银行卡',
    BALANCE: '余额'
  }
  return map[v] || v
}

const formatCurrency = (val) => {
  if (val === null || val === undefined) return '0.00'
  const num = Number(val) || 0
  return num.toFixed(2)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return '-'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${mi}`
}

// 累计充值金额与会员表一致（管理端与用户端数据一致）
const totalRechargeAmount = computed(() => Number(memberTotalRecharge.value) || 0)
const totalRechargeCount = computed(() => totalRecordCount.value)

const lastRechargeAmount = computed(() => {
  if (allRecords.value.length > 0) {
    return Number(allRecords.value[0].rechargeAmount) || 0
  }
  return 0
})

const lastRechargeTime = computed(() => {
  if (allRecords.value.length > 0) {
    return allRecords.value[0].rechargeTime
  }
  return null
})

const selectPackage = (pkg) => {
  form.value.amount = pkg.amount
}

const loadMemberTotalRecharge = async () => {
  try {
    const res = await getCurrentMember()
    const member = res?.data || null
    if (res.code === 200 && member) {
      memberTotalRecharge.value = member.totalRecharge != null ? Number(member.totalRecharge) : 0
    }
  } catch (e) {
    console.error('获取会员信息失败:', e)
  }
}

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getRechargeRecords({ page: page.value, size: size.value })
    if (res.code === 200) {
      const data = res.data?.data || []
      if (showAllRecords.value) {
        allRecords.value = data
      } else {
        recentRecords.value = data.slice(0, 5)
      }
      total.value = res.data?.total || 0
      totalRecordCount.value = res.data?.total ?? 0
    }
  } catch (e) {
    console.error('获取充值记录失败:', e)
  } finally {
    loading.value = false
  }
}

const loadAllRecords = async () => {
  try {
    const res = await getRechargeRecords({ page: 1, size: 1000 })
    if (res.code === 200) {
      const data = res.data?.data || []
      allRecords.value = data.sort((a, b) => {
        const timeA = new Date(a.rechargeTime || a.createTime || 0).getTime()
        const timeB = new Date(b.rechargeTime || b.createTime || 0).getTime()
        return timeB - timeA
      })
      totalRecordCount.value = res.data?.total ?? data.length
    }
  } catch (e) {
    console.error('获取全部充值记录失败:', e)
  }
}

const handleRecharge = async () => {
  // 防止重复提交：如果正在提交，直接返回
  if (submitting.value) {
    return
  }
  
  if (!form.value.amount || form.value.amount <= 0) {
    ElMessage.warning('请输入充值金额')
    return
  }
  
  submitting.value = true
  try {
    const res = await userRecharge({
      amount: form.value.amount,
      method: form.value.method
    })
    if (res.code === 200) {
      ElMessage.success(res.data?.message || '充值成功！')
      form.value.amount = 200
      loadMemberTotalRecharge()
      fetchRecords()
      loadAllRecords()
    } else {
      // 检查是否是乐观锁冲突错误
      const errorMsg = res.message || '充值失败'
      if (errorMsg.includes('数据已被其他操作修改')) {
        ElMessage.warning('检测到并发操作，请稍后重试或刷新页面后重试')
      } else {
        ElMessage.error(errorMsg)
      }
    }
  } catch (e) {
    console.error('充值失败:', e)
    // 检查是否是乐观锁冲突错误
    const errorMsg = e.message || '充值失败，请稍后重试'
    if (errorMsg.includes('数据已被其他操作修改')) {
      ElMessage.warning('检测到并发操作，请稍后重试或刷新页面后重试')
    } else {
      ElMessage.error(errorMsg)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadMemberTotalRecharge()
  fetchRecords()
  loadAllRecords()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.user-recharge {
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

.recharge-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 16px 0;
}

/* 充值套餐 */
.package-section {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.package-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.package-card {
  background: linear-gradient(135deg, var(--color-background, #F8FAFC) 0%, var(--color-card-bg, #FFFFFF) 100%);
  border: 2px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.package-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--color-primary, #2563EB), var(--color-secondary, #8B5CF6));
  transform: scaleX(0);
  transition: transform 0.4s ease;
}

.package-card::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(37, 99, 235, 0.1);
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
}

.package-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(37, 99, 235, 0.2);
  border-color: var(--color-primary-light, #60A5FA);
}

.package-card:hover::before {
  transform: scaleX(1);
}

.package-card:hover::after {
  width: 200px;
  height: 200px;
}

.package-card.selected {
  border-color: var(--color-primary, #2563EB);
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, rgba(37, 99, 235, 0.05) 100%);
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.3), inset 0 0 0 2px rgba(37, 99, 235, 0.1);
  transform: scale(1.05);
  animation: selectedGlow 2s ease-in-out infinite;
}

@keyframes selectedGlow {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(37, 99, 235, 0.3), inset 0 0 0 2px rgba(37, 99, 235, 0.1);
  }
  50% {
    box-shadow: 0 8px 28px rgba(37, 99, 235, 0.4), inset 0 0 0 2px rgba(37, 99, 235, 0.15);
  }
}

.package-card.selected::before {
  transform: scaleX(1);
  height: 4px;
}

.package-amount {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-secondary, #8B5CF6) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-family: 'Poppins', sans-serif;
  margin-bottom: 8px;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.package-card:hover .package-amount {
  transform: scale(1.1);
}

.package-card.selected .package-amount {
  animation: amountPulse 1.5s ease-in-out infinite;
}

@keyframes amountPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.package-bonus {
  margin-bottom: 8px;
}

.bonus-text {
  display: inline-block;
  background: linear-gradient(135deg, #FEF3C7 0%, #FCD34D 100%);
  color: #F59E0B;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
  position: relative;
  z-index: 1;
  animation: bonusShake 2s ease-in-out infinite;
}

@keyframes bonusShake {
  0%, 100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(-2deg);
  }
  75% {
    transform: rotate(2deg);
  }
}

.package-card:hover .bonus-text {
  transform: scale(1.1) rotate(0deg);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
}

.package-desc {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
}

/* 充值表单 */
.recharge-form-section {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
}

.recharge-form {
  max-width: 500px;
}

.recharge-form :deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.recharge-form :deep(.el-radio-button) {
  flex: 1;
}

.recharge-form :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 16px;
  border-radius: 12px;
  border: 2px solid var(--color-border, #E2E8F0);
  background: var(--color-card-bg, #FFFFFF);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
}

.recharge-form :deep(.el-radio-button__inner:hover) {
  border-color: var(--color-primary-light, #60A5FA);
  background: var(--color-background, #F1F5F9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
}

.recharge-form :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  border-color: var(--color-primary, #2563EB);
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EFF6FF) 0%, rgba(37, 99, 235, 0.08) 100%);
  color: var(--color-primary, #2563EB);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.2);
  transform: scale(1.02);
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--color-primary, #2563EB) 0%, var(--color-secondary, #8B5CF6) 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.submit-btn::before {
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

.submit-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.4);
}

.submit-btn:hover::before {
  width: 300px;
  height: 300px;
}

.submit-btn:active {
  transform: translateY(-1px);
}

/* 统计卡片 */
.stats-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--color-background, #F8FAFC);
  border-radius: 14px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.stat-item::before {
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

.stat-item:hover {
  background: var(--color-card-bg-hover, #EFF6FF);
  transform: translateX(6px);
  border-color: var(--color-primary-light, #60A5FA);
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.15);
}

.stat-item:hover::before {
  transform: scaleY(1);
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-item:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon.primary {
  background: linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%);
  color: var(--color-primary, #2563EB);
}

.stat-icon.success {
  background: linear-gradient(135deg, #D1FAE5 0%, #ECFDF5 100%);
  color: var(--color-success, #10B981);
}

.stat-icon.info {
  background: linear-gradient(135deg, #E0F2FE 0%, #F0F9FF 100%);
  color: var(--color-info, #06B6D4);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary, #1E293B);
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
}

.stat-item:hover .stat-value {
  transform: scale(1.05);
  color: var(--color-primary, #2563EB);
}

.stat-time {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
  margin-top: 4px;
}

/* 记录卡片 */
.records-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--color-background, #F8FAFC);
  border-radius: 12px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;
}

.record-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--color-primary, #2563EB);
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.record-item:hover {
  background: var(--color-card-bg-hover, #EFF6FF);
  border-color: var(--color-primary-light, #60A5FA);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.1);
}

.record-item:hover::before {
  transform: scaleY(1);
}

.record-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-danger, #EF4444);
  font-family: 'Poppins', sans-serif;
}

.record-method {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.record-time {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
}

.price-text {
  color: var(--color-danger, #EF4444);
  font-weight: 600;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 表格文字居中 */
.records-dialog :deep(.el-table) {
  text-align: center;
}

.records-dialog :deep(.el-table th),
.records-dialog :deep(.el-table td) {
  text-align: center !important;
}

.records-dialog :deep(.el-table .cell) {
  text-align: center !important;
  justify-content: center;
}

/* 响应式 - 移动端优化 */
@media (max-width: 1024px) {
  .recharge-content {
    grid-template-columns: 1fr;
  }
  
  .recharge-right {
    order: -1;
  }
}

@media (max-width: 768px) {
  .package-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .package-card {
    padding: 16px;
  }
  
  .package-amount {
    font-size: 24px;
  }
  
  .recharge-form :deep(.el-radio-group) {
    gap: 8px;
  }
  
  .recharge-form :deep(.el-radio-button__inner) {
    padding: 12px;
    font-size: 14px;
  }
  
  /* 移动端触摸优化 */
  .package-card,
  .stat-item,
  .record-item {
    -webkit-tap-highlight-color: rgba(37, 99, 235, 0.1);
    touch-action: manipulation;
  }
}

@media (max-width: 480px) {
  .package-grid {
    grid-template-columns: 1fr;
  }
  
  .package-amount {
    font-size: 20px;
  }
}
</style>
