<template>
  <div class="recharge-page">
    <div class="recharge-page-background">
      <div class="gradient-overlay"></div>
    </div>

    <div class="recharge-page-content">
      <!-- 顶部卡片区域：充值表单和统计信息 -->
      <div class="top-cards-container">
        <div class="glass-card recharge-form-card">
          <div class="card-header">
            <div class="title">为会员充值</div>
          </div>
          <el-form :model="form" label-width="90px" class="recharge-form">
            <el-form-item label="选择会员" required>
              <el-select
                v-model="form.memberId"
                filterable
                remote
                placeholder="输入姓名或手机号搜索会员"
                :remote-method="searchMembers"
                :loading="memberSearchLoading"
                clearable
                style="width: 100%"
                value-key="id"
                @clear="memberOptions = []"
              >
                <el-option
                  v-for="m in memberOptions"
                  :key="m.id"
                  :label="`${m.memberName || '-'} ${m.phone ? '(' + m.phone + ')' : ''}`"
                  :value="m.id"
                />
              </el-select>
              <div class="form-tip">请先搜索并选择要充值的会员，再填写金额并充值</div>
            </el-form-item>
            <el-form-item label="充值金额">
              <el-input-number v-model="form.amount" :min="1" :step="50" />
            </el-form-item>
            <el-form-item label="支付方式">
              <el-radio-group v-model="form.method">
                <el-radio label="ALIPAY">支付宝</el-radio>
                <el-radio label="WECHAT">微信</el-radio>
                <el-radio label="CASH">现金</el-radio>
                <el-radio label="BANK">银行卡</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="选填" clearable maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" :disabled="!form.memberId" @click="handleRecharge" size="large" class="bmp-uiv-btn bmp-uiv-btn-primary">立即充值</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="glass-card stats-card">
          <div class="card-header">
            <div class="title">充值统计</div>
            <el-icon class="stats-icon"><Wallet /></el-icon>
          </div>
          <div class="stats-content">
            <div class="stat-item stat-item-primary">
              <div class="stat-label">累计充值</div>
              <div class="stat-value">{{ formatCurrency(totalRechargeAmount) }}</div>
            </div>
            <div class="stat-item stat-item-success">
              <div class="stat-label">充值次数</div>
              <div class="stat-value">{{ totalRechargeCount }}</div>
            </div>
            <div class="stat-item stat-item-info">
              <div class="stat-label">最近充值</div>
              <div class="stat-value">{{ formatCurrency(lastRechargeAmount) }}</div>
              <div class="stat-desc" v-if="lastRechargeTime">{{ formatDateTime(lastRechargeTime) }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="glass-card table-card">
        <div class="title">充值记录</div>

        <!-- 筛选表单 -->
        <div class="filter-form">
          <el-form :inline="true" :model="filterForm" class="filter-form-content">
            <el-form-item label="会员搜索">
              <el-input
                v-model="filterForm.memberKeyword"
                placeholder="输入姓名或手机号"
                clearable
                style="width: 200px"
                @keyup.enter="handleFilter"
              />
            </el-form-item>
            <el-form-item label="时间段">
              <el-date-picker
                v-model="filterForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 280px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleFilter" class="bmp-uiv-btn bmp-uiv-btn-primary">搜索</el-button>
              <el-button @click="handleResetFilter" class="bmp-uiv-btn">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

      <el-table
        :data="records"
        v-loading="loading"
        element-loading-text="加载中..."
        style="width: 100%"
        stripe
        border
        highlight-current-row
        :header-cell-style="tableHeaderStyle"
        :cell-style="{ textAlign: 'center', padding: '12px 0' }"
        row-key="id"
      >
        <el-table-column prop="rechargeNo" label="充值单号" min-width="150" align="center" />
        <el-table-column prop="rechargeTime" label="时间" min-width="180" align="center" />
        <el-table-column prop="rechargeAmount" label="金额(元)" min-width="120" align="center">
          <template #default="scope">
            {{ Number(scope.row.rechargeAmount).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="rechargeMethod" label="方式" min-width="140" align="center">
          <template #default="scope">
            {{ methodText(scope.row.rechargeMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="operatorType" label="类型" min-width="120" align="center">
          <template #default="scope">
            {{ scope.row.operatorType === 'ADMIN' ? '管理员充值' : '自助充值' }}
          </template>
        </el-table-column>
        <el-table-column prop="isUpgraded" label="是否升级" min-width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isUpgraded === 1 ? 'success' : 'info'">
              {{ scope.row.isUpgraded === 1 ? '已升级' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isLevelUpgraded" label="等级升级" min-width="120" align="center">
          <template #default="scope">
            <!-- 兼容旧数据：未执行升级脚本时 isLevelUpgraded 可能为 null，显示为否 -->
            <el-tag :type="scope.row.isLevelUpgraded === 1 ? 'success' : 'info'">
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
          class="pagination"
        />
      </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { adminRecharge, getRechargeRecords } from '@/api/recharge'
import { getMemberList } from '@/api/member'
import { ElMessage } from 'element-plus'
import { Wallet } from '@element-plus/icons-vue'

const form = ref({
  memberId: null,
  amount: 200,
  method: 'ALIPAY',
  remark: ''
})

const memberOptions = ref([])
const memberSearchLoading = ref(false)

// 筛选表单
const filterForm = ref({
  memberKeyword: '',
  dateRange: []
})

const loading = ref(false)
const submitting = ref(false)
const records = ref([])
const allRecords = ref([]) // 存储所有充值记录用于统计
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 表格样式
const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
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

// 计算总充值金额
const totalRechargeAmount = computed(() => {
  return allRecords.value.reduce((sum, record) => {
    return sum + (Number(record.rechargeAmount) || 0)
  }, 0)
})

// 计算充值次数
const totalRechargeCount = computed(() => {
  return allRecords.value.length
})

// 最近充值金额
const lastRechargeAmount = computed(() => {
  if (allRecords.value.length > 0) {
    return Number(allRecords.value[0].rechargeAmount) || 0
  }
  return 0
})

// 最近充值时间
const lastRechargeTime = computed(() => {
  if (allRecords.value.length > 0) {
    return allRecords.value[0].rechargeTime
  }
  return null
})

// 格式化金额
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0.00'
  const num = Number(value) || 0
  return num.toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return '-'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const fetchRecords = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      page: page.value,
      size: size.value
    }

    // 添加筛选参数
    if (filterForm.value.memberKeyword) {
      params.memberKeyword = filterForm.value.memberKeyword
    }
    if (filterForm.value.dateRange && filterForm.value.dateRange.length === 2) {
      params.startTime = filterForm.value.dateRange[0]
      params.endTime = filterForm.value.dateRange[1]
    }

    const res = await getRechargeRecords(params)
    // 后端返回结构：{code: 200, message: "success", data: {data: [...], total: 100, ...}}
    // 所以需要使用 res.data.data 和 res.data.total
    const responseData = res?.data || {}
    records.value = Array.isArray(responseData.data) ? responseData.data : []
    total.value = responseData.total || 0
    
    // 获取所有充值记录用于统计（不限制分页）
    if (total.value > 0) {
      try {
        const allRes = await getRechargeRecords({ page: 1, size: total.value })
        const allResponseData = allRes?.data || {}
        let allDataList = Array.isArray(allResponseData.data) ? allResponseData.data : []
        // 按时间倒序排序，确保最新的记录在前面
        allDataList.sort((a, b) => {
          const timeA = new Date(a.rechargeTime || a.createTime || 0).getTime()
          const timeB = new Date(b.rechargeTime || b.createTime || 0).getTime()
          return timeB - timeA
        })
        allRecords.value = allDataList
      } catch (e) {
        console.error('获取全部充值记录失败:', e)
        // 如果获取全部记录失败，使用当前页数据
        allRecords.value = records.value
      }
    } else {
      allRecords.value = []
    }
  } catch (e) {
    console.error(e)
    records.value = []  // 出错时重置为空数组
    allRecords.value = []
  } finally {
    loading.value = false
  }
}

const searchMembers = async (query) => {
  if (!query || !String(query).trim()) {
    memberOptions.value = []
    return
  }
  memberSearchLoading.value = true
  try {
    const keyword = String(query).trim()
    const isLikelyPhone = /^\d[\d\s-]*\d$|^\d+$/.test(keyword)
    const params = { page: 1, size: 20 }
    if (isLikelyPhone) {
      params.phone = keyword
    } else {
      params.memberName = keyword
    }
    const res = await getMemberList(params)
    if (res?.code === 200) {
      const list = res.data?.data || []
      memberOptions.value = list
      if (list.length === 0) {
        ElMessage.warning('未找到匹配的会员，请检查姓名或手机号')
      }
    } else {
      memberOptions.value = []
    }
  } catch (e) {
    console.error('搜索会员失败:', e)
    memberOptions.value = []
  } finally {
    memberSearchLoading.value = false
  }
}

const handleRecharge = async () => {
  if (!form.value.memberId) {
    ElMessage.warning('请先选择要充值的会员')
    return
  }
  if (!form.value.amount || form.value.amount <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  submitting.value = true
  try {
    const res = await adminRecharge({
      memberId: form.value.memberId,
      amount: form.value.amount,
      method: form.value.method,
      remark: form.value.remark || undefined
    })
    if (res?.code === 200) {
      ElMessage.success(res.data?.message || '充值成功')
      form.value.remark = ''
      await fetchRecords()
    } else {
      ElMessage.error(res?.message || '充值失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.message || '充值失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 筛选方法
const handleFilter = () => {
  page.value = 1 // 重置到第一页
  fetchRecords()
}

// 重置筛选
const handleResetFilter = () => {
  filterForm.value.memberKeyword = ''
  filterForm.value.dateRange = []
  page.value = 1
  fetchRecords()
}

onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600;700&family=Fira+Sans:wght@300;400;500;600;700&display=swap');

.recharge-page {
  padding: 20px;
  background-color: var(--color-background, #f0f2f5);
  height: calc(100vh - 84px);
  overflow-y: auto;
  position: relative;
}
.recharge-page::-webkit-scrollbar {
  width: 8px;
}
.recharge-page::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}
.recharge-page::-webkit-scrollbar-thumb {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 4px;
}
.recharge-page::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted, #94A3B8);
}
.recharge-page-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--color-background, #F8FAFC);
  z-index: 0;
  pointer-events: none;
}
.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: transparent;
}
.recharge-page-content {
  position: relative;
  z-index: 1;
  padding: 0;
  max-width: 1600px;
  margin: 0 auto;
}
.glass-card {
  background: var(--color-card-bg, #FFFFFF);
  backdrop-filter: none;
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
}
.glass-card:hover {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-border-hover, #CBD5E1);
  transform: translateY(-6px) scale(1.02);
  box-shadow: var(--shadow-hover, 0 4px 12px rgba(0, 0, 0, 0.08));
}
.top-cards-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.recharge-form-card {
  padding: 24px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.stats-card {
  padding: 24px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-icon {
  font-size: 24px;
  color: var(--color-primary, #3B82F6);
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  padding: 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-item-primary {
  background: linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%);
}

.stat-item-primary:hover {
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.2);
  transform: translateY(-4px);
}

.stat-item-success {
  background: linear-gradient(135deg, #D1FAE5 0%, #ECFDF5 100%);
}

.stat-item-success:hover {
  box-shadow: 0 8px 16px rgba(16, 185, 129, 0.2);
  transform: translateY(-4px);
}

.stat-item-info {
  background: linear-gradient(135deg, #E0E7FF 0%, #EEF2FF 100%);
}

.stat-item-info:hover {
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.2);
  transform: translateY(-4px);
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-primary, #3b82f6);
  font-family: 'Fira Code', monospace;
  margin-bottom: 4px;
}

.stat-item-success .stat-value {
  color: var(--color-success, #10b981);
}

.stat-item-info .stat-value {
  color: var(--color-info, #6366f1);
}

.stat-desc {
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
  margin-top: 4px;
}

.table-card {
  padding: 24px;
  animation: fadeInUp 0.6s ease-out 0.3s both;
}

.filter-form {
  margin: 20px 0;
  padding: 16px;
  background: var(--color-background, #F8FAFC);
  border-radius: 8px;
  border: 1px solid var(--color-border, #E2E8F0);
}

.filter-form-content {
  margin: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-form :deep(.el-form-item__label) {
  color: var(--color-text-primary, #1E293B);
  font-weight: 500;
}

.filter-form :deep(.el-input__wrapper) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  transition: all 0.3s ease;
}

.filter-form :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary, #3B82F6);
}

.filter-form :deep(.el-input__wrapper:focus-within) {
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.filter-form :deep(.el-date-editor) {
  background: var(--color-card-bg, #FFFFFF);
}

.filter-form :deep(.el-date-editor .el-range-separator) {
  color: var(--color-text-secondary, #64748B);
}

.filter-form :deep(.el-date-editor .el-range-input) {
  background: transparent;
  color: var(--color-text-primary, #1E293B);
}
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
.table-card :deep(.el-table) {
  background: transparent;
  color: var(--color-text-primary, #1E293B);
}
.table-card :deep(.el-table th) {
  background: var(--color-card-bg-hover, #EFF6FF) !important;
  color: var(--color-text-primary, #1E293B) !important;
  border-bottom: 1px solid var(--color-border, #E2E8F0) !important;
  font-weight: 600;
  padding: 14px 0 !important;
}
.table-card :deep(.el-table td) {
  border-bottom: 1px solid var(--color-border, #E2E8F0) !important;
  padding: 14px 0 !important;
  color: var(--color-text-primary, #1E293B);
}
.table-card :deep(.el-table tr:hover > td) {
  background: var(--color-background, #F8FAFC) !important;
}
.title {
  font-family: 'Fira Sans', sans-serif;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-primary, #1E293B);
  font-size: 18px;
}
.recharge-form {
  max-width: 420px;
}

.form-tip {
  font-size: 12px;
  color: var(--color-text-muted, #94A3B8);
  margin-top: 6px;
  line-height: 1.4;
}
.recharge-form :deep(.el-form-item__label) {
  color: var(--color-text-primary, #1E293B);
}
.recharge-form :deep(.el-input-number) {
  background: var(--color-background, #F8FAFC);
  border-color: var(--color-border, #E2E8F0);
}
.recharge-form :deep(.el-radio__label) {
  color: var(--color-text-primary, #1E293B);
}
.recharge-form :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: var(--color-primary, #3B82F6);
  border-color: var(--color-primary, #3B82F6);
}
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid var(--color-border, #E2E8F0);
}
.pagination :deep(.el-pagination) {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.pagination :deep(.el-pagination__total),
.pagination :deep(.el-pagination__jump),
.pagination :deep(.btn-prev),
.pagination :deep(.btn-next),
.pagination :deep(.el-pager li) {
  color: var(--color-text-primary, #1E293B);
  transition: all 0.3s ease;
  font-family: 'Fira Sans', sans-serif;
}

.pagination :deep(.el-pagination__total) {
  color: var(--color-text-secondary, #64748B);
  font-size: 13px;
  font-weight: 500;
}

.pagination :deep(.el-pagination__jump) {
  color: var(--color-text-secondary, #64748B);
  font-size: 13px;
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 6px;
  padding: 6px 10px;
  min-width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 14px;
}

.pagination :deep(.btn-prev:hover),
.pagination :deep(.btn-next:hover) {
  background: var(--color-background, #F8FAFC);
  border-color: var(--color-primary, #3B82F6);
  color: var(--color-primary, #3B82F6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.pagination :deep(.btn-prev.disabled),
.pagination :deep(.btn-next.disabled) {
  background: var(--color-background, #F1F5F9);
  border-color: var(--color-border, #E2E8F0);
  color: var(--color-text-muted, #CBD5E1);
  cursor: not-allowed;
}

.pagination :deep(.el-pager li) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #DCDFE6);
  border-radius: 6px;
  padding: 4px 10px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
  color: var(--color-text-primary, #666666);
}

.pagination :deep(.el-pager li:hover) {
  background: var(--color-card-bg-hover, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  color: var(--color-primary, #3B82F6);
  transform: scale(1.05);
}

.pagination :deep(.el-pager li.is-active) {
  background: var(--color-primary, #3B82F6);
  border-color: var(--color-primary, #3B82F6);
  color: #ffffff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transform: scale(1.08);
}

.pagination :deep(.el-select__wrapper) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #DCDFE6);
  border-radius: 6px;
  transition: all 0.3s ease;
  padding: 4px 8px;
}

.pagination :deep(.el-select__wrapper:hover) {
  background: var(--color-background, #F8FAFC);
  border-color: var(--color-primary, #3B82F6);
}

.pagination :deep(.el-select__wrapper:focus-within) {
  background: var(--color-card-bg, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.pagination :deep(.el-select__placeholder) {
  color: var(--color-text-muted, #94A3B8);
  font-size: 13px;
}

.pagination :deep(.el-input__wrapper) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 6px;
  transition: all 0.3s ease;
}

.pagination :deep(.el-input__wrapper:hover) {
  background: var(--color-background, #F8FAFC);
  border-color: var(--color-primary, #3B82F6);
}

.pagination :deep(.el-input__wrapper:focus-within) {
  background: var(--color-card-bg, #FFFFFF);
  border-color: var(--color-primary, #3B82F6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.pagination :deep(.el-input__inner) {
  color: var(--color-text-primary, #1E293B);
  font-size: 13px;
  text-align: center;
}

.pagination :deep(.el-input__inner::placeholder) {
  color: var(--color-text-muted, #94A3B8);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .top-cards-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .recharge-page-content {
    padding: 0;
  }
  
  .top-cards-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .recharge-form-card,
  .stats-card {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .table-card {
    padding: 16px;
  }
}
</style>
