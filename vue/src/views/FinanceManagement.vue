<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <!-- 头部统计卡片 -->
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32"><Coin /></el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">财务管理</h2>
              <p class="page-subtitle">查看收支流水、统计分析和财务报表</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item stat-item-success" @click="handleStatClick(1)">
              <span class="stat-label">总收入</span>
              <span class="stat-value">¥{{ formatCurrency(statistics.totalIncome) }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">总支出</span>
              <span class="stat-value">¥{{ formatCurrency(statistics.totalExpense) }}</span>
            </div>
            <div class="stat-item stat-item-primary" @click="handleStatClick(null)">
              <span class="stat-label">净收入</span>
              <span class="stat-value">¥{{ formatCurrency(statistics.netIncome) }}</span>
            </div>
            <div class="stat-item" @click="handleStatClick(1)">
              <span class="stat-label">本月收入</span>
              <span class="stat-value">¥{{ formatCurrency(statistics.monthIncome) }}</span>
              <span class="stat-change" :class="statistics.incomeChange >= 0 ? 'up' : 'down'">
                {{ statistics.incomeChange >= 0 ? '+' : '' }}{{ (statistics.incomeChange || 0).toFixed(1) }}%
              </span>
            </div>
            <div class="stat-item" @click="handleStatClick(0)">
              <span class="stat-label">本月支出</span>
              <span class="stat-value">¥{{ formatCurrency(statistics.monthExpense) }}</span>
              <span class="stat-change" :class="statistics.expenseChange <= 0 ? 'up' : 'down'">
                {{ statistics.expenseChange >= 0 ? '+' : '' }}{{ (statistics.expenseChange || 0).toFixed(1) }}%
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="charts-row">
        <div class="glass-card chart-card">
          <h3 class="chart-title">收支趋势</h3>
          <div class="chart-wrapper">
            <div ref="trendChartRef" class="chart-container" v-loading="chartLoading"></div>
            <div v-if="!chartLoading && (!trendChartData || !trendChartData.dates || !Array.isArray(trendChartData.dates) || trendChartData.dates.length === 0 || (!trendChartData.incomes?.some(v => v > 0) && !trendChartData.expenses?.some(v => v > 0)))" class="chart-empty">
              <el-empty description="暂无数据" :image-size="80" />
            </div>
          </div>
        </div>
        <div class="glass-card chart-card chart-card-small">
          <h3 class="chart-title">业务收入占比</h3>
          <div class="chart-wrapper">
            <div ref="ratioChartRef" class="chart-container" v-loading="chartLoading"></div>
            <div v-if="!chartLoading && (!ratioChartData || !Array.isArray(ratioChartData) || ratioChartData.length === 0 || !ratioChartData.some(item => item.value > 0))" class="chart-empty">
              <el-empty description="暂无数据" :image-size="80" />
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索 -->
      <div class="glass-card search-card">
        <div class="search-header">
          <h3 class="search-title">搜索过滤</h3>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <div class="search-container">
            <div class="search-fields">
              <el-form-item label="场馆" class="search-item" v-if="userRole === 'PRESIDENT'">
                <el-select
                  v-model="searchForm.venueId"
                  placeholder="全部场馆"
                  clearable
                  class="search-select"
                >
                  <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="业务类型" class="search-item">
                <el-select v-model="searchForm.businessType" placeholder="全部类型" clearable class="search-select">
                  <el-option v-for="item in businessTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="收支类型" class="search-item">
                <el-select v-model="searchForm.incomeExpenseType" placeholder="全部" clearable class="search-select-small">
                  <el-option label="收入" :value="1" />
                  <el-option label="支出" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间范围" class="search-item">
                <el-date-picker
                  v-model="searchForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item label="关键字" class="search-item">
                <el-input v-model="searchForm.keyword" placeholder="单号/备注" clearable class="search-input" />
              </el-form-item>
            </div>
            <div class="search-buttons">
              <el-button type="primary" :icon="Search" class="search-btn bmp-uiv-btn bmp-uiv-btn-primary" @click="handleSearch">搜索</el-button>
              <el-button type="primary" :icon="Refresh" class="reset-btn bmp-uiv-btn bmp-uiv-btn-primary" @click="handleReset">重置</el-button>
            </div>
          </div>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="glass-card action-card">
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleAdd">新增财务记录</el-button>
      </div>

      <!-- 表格 -->
      <div class="glass-card table-card">
        <el-table
          :data="financeList"
          v-loading="loading"
          element-loading-text="加载中..."
          style="width: 100%"
          border
          stripe
          highlight-current-row
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '14px 0' }"
          :height="tableHeight"
          row-key="id"
          :virtual-scroll="financeList.length > 100"
        >
          <el-table-column prop="financeNo" label="财务单号" min-width="150" align="center" />
          <el-table-column prop="businessType" label="业务类型" min-width="100" align="center">
            <template #default="scope">
              <el-tag :type="getBusinessTypeTagType(scope.row.businessType)" size="small">
                {{ getBusinessTypeName(scope.row.businessType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="incomeExpenseType" label="收支类型" min-width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.incomeExpenseType === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.incomeExpenseType === 1 ? '收入' : '支出' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip align="center" />
          <el-table-column label="金额" min-width="120" align="center">
            <template #default="scope">
              <span :class="scope.row.incomeExpenseType === 1 ? 'price-income' : 'price-expense'">
                {{ scope.row.incomeExpenseType === 1 ? '+' : '-' }}¥{{ formatCurrency(scope.row.amount) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="支付方式" min-width="100" align="center">
            <template #default="scope">
              {{ getPaymentMethodName(scope.row.paymentMethod) }}
            </template>
          </el-table-column>
          <el-table-column prop="venueName" label="场馆" min-width="120" align="center" v-if="userRole === 'PRESIDENT'" />
          <el-table-column prop="operator" label="操作人" min-width="100" align="center" />
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip align="center" />
          <el-table-column prop="createTime" label="创建时间" min-width="160" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="220" fixed="right" align="center">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button type="primary" size="small" :icon="View" plain @click="handleView(scope.row)">查看</el-button>
                <el-button type="success" size="small" :icon="Edit" plain @click="handleEdit(scope.row)">编辑</el-button>
                <el-button v-if="userRole === 'PRESIDENT'" type="danger" size="small" :icon="Delete" plain @click="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="pagination"
          />
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
      class="modern-dialog"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top" class="modern-form">
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">财务信息</h4>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="业务类型" prop="businessType" class="form-item-enhanced modern-form-item">
                <el-select v-model="form.businessType" placeholder="请选择业务类型" class="form-input modern-form-input" style="width: 100%">
                  <el-option v-for="item in businessTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
                <span class="field-hint modern-field-hint">选择该财务记录关联的业务类型</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="收支类型" prop="incomeExpenseType" class="form-item-enhanced modern-form-item">
                <el-select v-model="form.incomeExpenseType" placeholder="请选择收支类型" class="form-input modern-form-input" style="width: 100%">
                  <el-option label="收入" :value="1" />
                  <el-option label="支出" :value="0" />
                </el-select>
                <span class="field-hint modern-field-hint">选择该记录是收入还是支出</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="金额" prop="amount" class="form-item-enhanced modern-form-item">
                <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="10" style="width: 100%" />
                <span class="field-hint modern-field-hint">请输入金额，支持两位小数</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="支付方式" prop="paymentMethod" class="form-item-enhanced modern-form-item">
                <el-select v-model="form.paymentMethod" placeholder="请选择支付方式" class="form-input modern-form-input" style="width: 100%">
                  <el-option label="现金" value="CASH" />
                  <el-option label="支付宝" value="ALIPAY" />
                  <el-option label="微信" value="WECHAT" />
                  <el-option label="余额" value="BALANCE" />
                  <el-option label="银行转账" value="BANK" />
                </el-select>
                <span class="field-hint modern-field-hint">选择该财务记录的支付方式</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="场馆" prop="venueId" class="form-item-enhanced modern-form-item" v-if="userRole === 'PRESIDENT'">
            <el-select v-model="form.venueId" placeholder="请选择场馆（可选）" clearable class="form-input modern-form-input" style="width: 100%">
              <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
            </el-select>
            <span class="field-hint modern-field-hint">可选，关联到特定场馆的财务记录</span>
          </el-form-item>
          <el-form-item label="备注" prop="remark" class="form-item-enhanced modern-form-item">
            <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" class="form-input modern-form-input" />
            <span class="field-hint modern-field-hint">可选，添加该财务记录的备注说明</span>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-cancel modern-btn-cancel">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="btn-submit modern-btn-submit">
            {{ isEdit ? '更新财务记录' : '新增财务记录' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看财务记录对话框 -->
    <el-dialog
      title="财务记录详情"
      v-model="viewDialogVisible"
      width="600px"
      @close="handleViewDialogClose"
      class="modern-dialog"
    >
      <el-descriptions :column="2" border :model="viewFormData" class="modern-descriptions">
        <el-descriptions-item label="财务单号" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ viewFormData.financeNo || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="业务类型" class="modern-descriptions-item">
          <el-tag :type="getBusinessTypeTagType(viewFormData.businessType)" size="small">
            {{ getBusinessTypeName(viewFormData.businessType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收支类型" class="modern-descriptions-item">
          <el-tag :type="viewFormData.incomeExpenseType === 1 ? 'success' : 'danger'" size="small">
            {{ viewFormData.incomeExpenseType === 1 ? '收入' : '支出' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="金额" class="modern-descriptions-item">
          <span :class="viewFormData.incomeExpenseType === 1 ? 'price-income' : 'price-expense'">
            {{ viewFormData.incomeExpenseType === 1 ? '+' : '-' }}¥{{ formatCurrency(viewFormData.amount) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ getPaymentMethodName(viewFormData.paymentMethod) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="场馆" class="modern-descriptions-item" v-if="userRole === 'PRESIDENT'">
          <span class="modern-descriptions-value">{{ viewFormData.venueName || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作人" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ viewFormData.operator || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ formatDateTime(viewFormData.createTime) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="备注" class="modern-descriptions-item" :span="2">
          <span class="modern-descriptions-value">{{ viewFormData.remark || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer modern-dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Coin, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getFinanceList,
  addFinance,
  updateFinance,
  deleteFinance,
  getFinanceStatistics,
  getFinanceTrend,
  getFinanceRatio,
  getFinanceVenues,
  getBusinessTypes
} from '@/api/finance'

// 用户角色
const userRole = computed(() => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.role || ''
  } catch {
    return ''
  }
})

// 数据状态
const loading = ref(false)
const submitLoading = ref(false)
const chartLoading = ref(false)
const financeList = ref([])
const trendChartData = ref(null)
const ratioChartData = ref(null)
const statistics = ref({
  totalIncome: 0,
  totalExpense: 0,
  netIncome: 0,
  monthIncome: 0,
  monthExpense: 0,
  incomeChange: 0,
  expenseChange: 0
})
const venueOptions = ref([])
const businessTypeOptions = ref([
  { value: 'BOOKING', label: '场地预约' },
  { value: 'COURSE', label: '课程预约' },
  { value: 'EQUIPMENT', label: '器材租借' },
  { value: 'TOURNAMENT', label: '赛事报名' },
  { value: 'STRINGING', label: '穿线服务' },
  { value: 'RECHARGE', label: '会员充值' },
  { value: 'OTHER', label: '其他收支' }
])

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  venueId: null,
  businessType: '',
  incomeExpenseType: null,
  dateRange: [],
  keyword: ''
})

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const viewDialogVisible = ref(false)
const viewFormData = ref({})
const isEdit = computed(() => !!form.id)
const form = reactive({
  id: null,
  businessType: 'OTHER',
  incomeExpenseType: 1,
  amount: null,
  paymentMethod: 'CASH',
  venueId: null,
  remark: ''
})

const formRules = {
  businessType: [{ required: true, message: '请选择业务类型', trigger: 'change' }],
  incomeExpenseType: [{ required: true, message: '请选择收支类型', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0.01', trigger: 'blur' }
  ]
}

// 图表
const trendChartRef = ref(null)
const ratioChartRef = ref(null)
let trendChart = null
let ratioChart = null

// 表格样式
const tableHeight = ref(400)
const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  fontWeight: '600',
  textAlign: 'center'
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadData(),
    loadStatistics(),
    loadVenues()
  ])
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  ratioChart?.dispose()
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      venueId: searchForm.venueId,
      businessType: searchForm.businessType,
      incomeExpenseType: searchForm.incomeExpenseType,
      startDate: searchForm.dateRange?.[0] || '',
      endDate: searchForm.dateRange?.[1] || '',
      keyword: searchForm.keyword
    }
    const res = await getFinanceList(params)
    if (res.code === 200) {
      financeList.value = res.data?.data ?? []
      pagination.total = res.data?.total ?? 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadStatistics() {
  try {
    const res = await getFinanceStatistics({
      venueId: searchForm.venueId,
      startDate: searchForm.dateRange?.[0] || '',
      endDate: searchForm.dateRange?.[1] || ''
    })
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadVenues() {
  if (userRole.value !== 'PRESIDENT') return
  try {
    const res = await getFinanceVenues()
    if (res.code === 200) {
      venueOptions.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

// 图表初始化
async function initCharts() {
  await nextTick()
  if (trendChartRef.value && ratioChartRef.value) {
    await Promise.all([
      loadTrendChart(),
      loadRatioChart()
    ])
  }
}

async function loadTrendChart() {
  if (!trendChartRef.value) return

  chartLoading.value = true
  try {
    const res = await getFinanceTrend({
      venueId: searchForm.venueId,
      startDate: searchForm.dateRange?.[0] || '',
      endDate: searchForm.dateRange?.[1] || ''
    })

    if (res.code === 200) {
      const data = res.data || { dates: [], incomes: [], expenses: [] }
      trendChartData.value = data

      if (!trendChart) {
        trendChart = echarts.init(trendChartRef.value)
      }

      // 如果有数据，渲染图表
      if (data && data.dates && Array.isArray(data.dates) && data.dates.length > 0 && 
          data.incomes && data.expenses && 
          (data.incomes.some(v => v > 0) || data.expenses.some(v => v > 0))) {
        trendChart.setOption({
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            backgroundColor: 'rgba(50, 50, 50, 0.9)',
            borderColor: 'transparent',
            textStyle: { color: '#fff' },
            formatter: function(params) {
              let result = params[0].name + '<br/>'
              params.forEach(item => {
                result += item.marker + item.seriesName + ': ¥' + item.value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + '<br/>'
              })
              return result
            }
          },
          legend: {
            data: ['收入', '支出'],
            top: 10,
            textStyle: {
              color: '#1E293B',
              fontSize: 12
            }
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '15%',
            top: '15%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: data.dates,
            axisLabel: {
              rotate: 45,
              fontSize: 11,
              color: '#64748B'
            },
            axisLine: {
              lineStyle: {
                color: '#E2E8F0'
              }
            }
          },
          yAxis: {
            type: 'value',
            axisLabel: {
              formatter: function(value) {
                if (value >= 10000) {
                  return '¥' + (value / 10000).toFixed(1) + '万'
                }
                return '¥' + value
              },
              color: '#64748B',
              fontSize: 11
            },
            axisLine: {
              lineStyle: {
                color: '#E2E8F0'
              }
            },
            splitLine: {
              lineStyle: {
                color: '#E2E8F0',
                type: 'dashed'
              }
            }
          },
          series: [
            {
              name: '收入',
              type: 'bar',
              data: data.incomes,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#10b981' },
                  { offset: 1, color: '#34d399' }
                ]),
                borderRadius: [4, 4, 0, 0]
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowColor: 'rgba(16, 185, 129, 0.5)'
                }
              },
              animationDelay: function(idx) {
                return idx * 100
              }
            },
            {
              name: '支出',
              type: 'bar',
              data: data.expenses,
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#ef4444' },
                  { offset: 1, color: '#f87171' }
                ]),
                borderRadius: [4, 4, 0, 0]
              },
              emphasis: {
                itemStyle: {
                  shadowBlur: 10,
                  shadowColor: 'rgba(239, 68, 68, 0.5)'
                }
              },
              animationDelay: function(idx) {
                return idx * 100 + 50
              }
            }
          ],
          animationEasing: 'elasticOut',
          animationDelayUpdate: function(idx) {
            return idx * 5
          }
        })
      } else {
        // 空数据时清空图表并隐藏
        if (trendChart) {
          trendChart.clear()
          trendChart.dispose()
          trendChart = null
        }
      }
    }
  } catch (e) {
    console.error(e)
    trendChartData.value = null
  } finally {
    chartLoading.value = false
  }
}

async function loadRatioChart() {
  if (!ratioChartRef.value) return

  chartLoading.value = true
  try {
    const res = await getFinanceRatio({
      venueId: searchForm.venueId,
      startDate: searchForm.dateRange?.[0] || '',
      endDate: searchForm.dateRange?.[1] || ''
    })

    if (res.code === 200) {
      const data = (res.data || []).map(item => ({
        name: item.name,
        value: item.amount
      }))
      ratioChartData.value = data

      if (!ratioChart) {
        ratioChart = echarts.init(ratioChartRef.value)
      }

      // 如果有数据，渲染图表
      if (data && data.length > 0) {
        // 定义颜色方案
        const colors = [
          '#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899',
          '#06B6D4', '#84CC16', '#F97316', '#6366F1'
        ]

        ratioChart.setOption({
          tooltip: {
            trigger: 'item',
            backgroundColor: 'rgba(50, 50, 50, 0.9)',
            borderColor: 'transparent',
            textStyle: { color: '#fff' },
            formatter: function(params) {
              return params.marker + params.name + '<br/>' +
                     '金额: ¥' + params.value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + '<br/>' +
                     '占比: ' + params.percent + '%'
            }
          },
          legend: {
            orient: 'vertical',
            right: 10,
            top: 'center',
            itemGap: 12,
            textStyle: {
              color: '#1E293B',
              fontSize: 12
            },
            formatter: function(name) {
              const item = data.find(d => d.name === name)
              if (item) {
                return name + '  ¥' + item.value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
              }
              return name
            }
          },
          color: colors,
          series: [
            {
              type: 'pie',
              radius: ['40%', '70%'],
              center: ['35%', '50%'],
              avoidLabelOverlap: true,
              itemStyle: {
                borderRadius: 8,
                borderColor: '#fff',
                borderWidth: 2
              },
              label: {
                show: true,
                position: 'outside',
                formatter: '{b}\n{d}%',
                fontSize: 11,
                color: '#1E293B'
              },
              labelLine: {
                show: true,
                length: 15,
                length2: 8,
                lineStyle: {
                  color: '#E2E8F0'
                }
              },
              emphasis: {
                label: {
                  show: true,
                  fontSize: 13,
                  fontWeight: 'bold'
                },
                itemStyle: {
                  shadowBlur: 15,
                  shadowOffsetX: 0,
                  shadowColor: 'rgba(0, 0, 0, 0.3)'
                },
                scale: true,
                scaleSize: 5
              },
              data: data,
              animationType: 'scale',
              animationEasing: 'elasticOut',
              animationDelay: function(idx) {
                return idx * 100
              }
            }
          ]
        })
      } else {
        // 空数据时清空图表并隐藏
        if (ratioChart) {
          ratioChart.clear()
          ratioChart.dispose()
          ratioChart = null
        }
      }
    }
  } catch (e) {
    console.error(e)
    ratioChartData.value = null
  } finally {
    chartLoading.value = false
  }
}

function handleResize() {
  trendChart?.resize()
  ratioChart?.resize()
}

// 搜索和分页
function handleSearch() {
  pagination.page = 1
  loadData()
  loadStatistics()
  loadTrendChart()
  loadRatioChart()
}

function handleReset() {
  searchForm.venueId = null
  searchForm.businessType = ''
  searchForm.incomeExpenseType = null
  searchForm.dateRange = []
  searchForm.keyword = ''
  handleSearch()
}

function handleStatClick(incomeExpenseType) {
  searchForm.incomeExpenseType = incomeExpenseType
  handleSearch()
}

function handlePageChange(page) {
  pagination.page = page
  loadData()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadData()
}

// 新增/编辑
function handleAdd() {
  dialogTitle.value = '新增财务记录'
  form.id = null
  form.businessType = 'OTHER'
  form.incomeExpenseType = 1
  form.amount = null
  form.paymentMethod = 'CASH'
  form.venueId = null
  form.remark = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑财务记录'
  form.id = row.id
  form.businessType = row.businessType
  form.incomeExpenseType = row.incomeExpenseType
  form.amount = row.amount
  form.paymentMethod = row.paymentMethod
  form.venueId = row.venueId
  form.remark = row.remark
  dialogVisible.value = true
}

function handleDialogClose() {
  formRef.value?.resetFields()
}

function handleView(row) {
  viewFormData.value = { ...row }
  viewDialogVisible.value = true
}

function handleViewDialogClose() {
  viewFormData.value = {}
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const data = { ...form }
    let res
    if (form.id) {
      res = await updateFinance(data)
    } else {
      res = await addFinance(data)
    }

    if (res.code === 200) {
      ElMessage.success(form.id ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadData()
      loadStatistics()
      loadTrendChart()
      loadRatioChart()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除这条财务记录吗？', '提示', {
      type: 'warning'
    })

    const res = await deleteFinance(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
      loadStatistics()
      loadTrendChart()
      loadRatioChart()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 工具函数
function formatCurrency(value) {
  if (!value && value !== 0) return '0.00'
  return Number(value).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function formatDateTime(datetime) {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

function getBusinessTypeName(type) {
  const map = {
    BOOKING: '场地预约',
    COURSE: '课程预约',
    EQUIPMENT: '器材租借',
    TOURNAMENT: '赛事报名',
    STRINGING: '穿线服务',
    RECHARGE: '会员充值',
    OTHER: '其他收支'
  }
  return map[type] || type
}

function getBusinessTypeTagType(type) {
  const map = {
    BOOKING: 'primary',
    COURSE: 'success',
    EQUIPMENT: 'warning',
    TOURNAMENT: 'danger',
    STRINGING: 'warning',
    RECHARGE: 'info',
    OTHER: ''
  }
  return map[type] || ''
}

function getPaymentMethodName(method) {
  const map = {
    CASH: '现金',
    ALIPAY: '支付宝',
    WECHAT: '微信',
    BALANCE: '余额',
    BANK: '银行转账'
  }
  return map[method] || method || '-'
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Sans:wght@400;500;600;700&display=swap');

.page-wrapper {
  padding: 20px;
  background-color: var(--color-background, #f0f2f5);
  height: calc(100vh - 84px);
  overflow-y: auto;
  position: relative;
}

.page-wrapper::-webkit-scrollbar {
  width: 8px;
}

.page-wrapper::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}

.page-wrapper::-webkit-scrollbar-thumb {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 4px;
}

.page-wrapper::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted, #94A3B8);
}

.page-bg {
  position: absolute;
  inset: 0;
  background: var(--color-background, #F8FAFC);
  z-index: 0;
  pointer-events: none;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: transparent;
}

.page-content {
  position: relative;
  z-index: 1;
  max-width: 1600px;
  margin: 0 auto;
}

.glass-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
  padding: 24px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.glass-card:hover {
  border-color: var(--color-border-hover, #CBD5E1);
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.page-header {
  padding: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.page-title {
  font-family: 'Fira Sans', sans-serif;
  font-size: 26px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 8px 0;
}

.page-subtitle {
  margin: 0;
  color: var(--color-text-secondary, #64748B);
}

.header-stats {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 12px 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  min-width: 120px;
  width: 120px;
  height: 80px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-sizing: border-box;
}

.stat-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.stat-item-success { background: linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%) !important; }
.stat-item-warning { background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%) !important; }
.stat-item-danger { background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 100%) !important; }
.stat-item-primary { background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%) !important; }

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary, #2563EB);
  line-height: 1.2;
}

.stat-change {
  display: block;
  font-size: 11px;
  margin-top: 2px;
  line-height: 1;
}

.stat-change.up { color: #10b981; }
.stat-change.down { color: #ef4444; }

/* 图表区域 */
.charts-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  flex: 2;
}

.chart-card-small {
  flex: 1;
  min-width: 350px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin: 0 0 16px 0;
}

.chart-wrapper {
  position: relative;
  height: 280px;
  min-height: 280px;
}

.chart-container {
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 1;
}

.chart-empty {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-card-bg, #FFFFFF);
  border-radius: 12px;
  z-index: 2;
}

/* 搜索区域 */
.search-card {
  padding: 24px;
}

.search-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.search-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  margin: 0;
}

.search-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
}

.search-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.search-item {
  margin-bottom: 0 !important;
}

.search-select {
  width: 180px;
}

.search-select-small {
  width: 140px;
}

.search-input {
  width: 200px;
}

.search-buttons {
  display: flex;
  gap: 10px;
}

/* 操作栏 */
.action-card {
  padding: 16px 24px;
}

/* 表格 */
.table-card {
  padding: 24px;
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

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}

.search-btn,
.reset-btn,
.action-card :deep(.el-button),
.operation-buttons :deep(.el-button) {
  transition: all 0.3s ease;
}

.search-btn:hover,
.reset-btn:hover,
.action-card :deep(.el-button:hover),
.operation-buttons :deep(.el-button:hover) {
  transform: translateY(-2px);
}

.search-btn:hover,
.action-card :deep(.el-button--primary:hover),
.operation-buttons :deep(.el-button--primary:hover) {
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.35);
}

.operation-buttons :deep(.el-button--success:hover) {
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.28);
}

.operation-buttons :deep(.el-button--danger:hover) {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.28);
}

.price-income {
  color: #10b981;
  font-weight: 600;
}

.price-expense {
  color: #ef4444;
  font-weight: 600;
}

/* 弹窗样式 */
.modern-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.form-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
}

/* 表单增强样式 */
.form-item-enhanced {
  margin-bottom: 20px !important;
}

.modern-form-input {
  width: 100%;
}

.modern-input-icon {
  color: var(--color-primary, #3B82F6);
  font-size: 16px;
}

.modern-field-hint {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-secondary, #64748b);
  font-style: italic;
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 20px 0 0 0;
}

.modern-btn-cancel {
  padding: 12px 28px;
  font-weight: 600;
  border-radius: 12px;
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
  background: var(--color-card-bg, #ffffff);
  color: var(--color-text-secondary, #64748b);
}

.modern-btn-submit {
  padding: 12px 32px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border: none;
  color: #ffffff;
}

.modern-descriptions {
  margin-top: 0;
}

.modern-descriptions-item {
  padding: 12px;
}

.modern-descriptions-value {
  font-weight: 500;
  color: var(--color-text-primary, #1e293b);
}

/* 按钮样式 */
.bmp-uiv-btn-primary {
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border: none;
}

.bmp-uiv-btn-primary:hover {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
}

/* 响应式 */
@media (max-width: 1200px) {
  .charts-row {
    flex-direction: column;
  }

  .chart-card-small {
    min-width: auto;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-stats {
    width: 100%;
    justify-content: space-between;
  }

  .search-container {
    flex-direction: column;
    align-items: stretch;
  }

  .search-buttons {
    justify-content: flex-end;
  }
}
</style>
