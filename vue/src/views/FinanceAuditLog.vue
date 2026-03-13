<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <!-- 头部 -->
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">财务审计日志</h2>
              <p class="page-subtitle">查看财务记录的操作审计日志</p>
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
              <el-form-item label="财务单号" class="search-item">
                <el-input v-model="searchForm.financeNo" placeholder="请输入财务单号" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="操作类型" class="search-item">
                <el-select v-model="searchForm.operationType" placeholder="全部类型" clearable class="search-select">
                  <el-option label="创建" value="CREATE" />
                  <el-option label="修改" value="UPDATE" />
                  <el-option label="删除" value="DELETE" />
                  <el-option label="对账" value="RECONCILE" />
                </el-select>
              </el-form-item>
              <el-form-item label="操作人" class="search-item">
                <el-input v-model="searchForm.operator" placeholder="请输入操作人" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="时间范围" class="search-item">
                <el-date-picker
                  v-model="searchForm.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
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
        <el-button
          type="primary"
          :icon="Download"
          class="bmp-uiv-btn bmp-uiv-btn-primary"
          @click="handleExport"
          :loading="exportLoading"
          :disabled="exportDialogVisible"
        >
          导出Excel
        </el-button>
      </div>

      <!-- 表格 -->
      <div class="glass-card table-card">
        <el-table
          :data="auditLogList"
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
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="financeNo" label="财务单号" min-width="150" align="center" />
          <el-table-column prop="operationType" label="操作类型" min-width="100" align="center">
            <template #default="scope">
              <el-tag :type="getOperationTypeTagType(scope.row.operationType)" size="small">
                {{ getOperationTypeName(scope.row.operationType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" min-width="100" align="center" />
          <el-table-column prop="operationTime" label="操作时间" min-width="160" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.operationTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="changeSummary" label="变更摘要" min-width="200" show-overflow-tooltip align="center" />
          <el-table-column prop="ipAddress" label="IP地址" min-width="120" align="center" />
          <el-table-column label="操作" min-width="120" fixed="right" align="center">
            <template #default="scope">
              <el-button type="primary" size="small" :icon="View" plain @click="handleView(scope.row)">查看</el-button>
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

    <!-- 查看详情对话框 -->
    <el-dialog
      title="审计日志详情"
      v-model="viewDialogVisible"
      width="700px"
      @close="handleViewDialogClose"
      class="modern-dialog"
    >
      <el-descriptions :column="2" border :model="viewFormData" class="modern-descriptions">
        <el-descriptions-item label="ID" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ viewFormData.id || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="财务单号" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ viewFormData.financeNo || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型" class="modern-descriptions-item">
          <el-tag :type="getOperationTypeTagType(viewFormData.operationType)" size="small">
            {{ getOperationTypeName(viewFormData.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作人" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ viewFormData.operator || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ formatDateTime(viewFormData.operationTime) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="IP地址" class="modern-descriptions-item">
          <span class="modern-descriptions-value">{{ viewFormData.ipAddress || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="变更摘要" class="modern-descriptions-item" :span="2">
          <span class="modern-descriptions-value">{{ viewFormData.changeSummary || '-' }}</span>
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

    <!-- 导出进度对话框 -->
    <el-dialog
      title="导出进度"
      v-model="exportDialogVisible"
      width="400px"
      :close-on-click-modal="false"
      class="modern-dialog"
    >
      <div class="export-progress">
        <el-progress :percentage="exportProgress" :status="exportStatus" />
        <p class="export-message">{{ exportMessage }}</p>
      </div>
      <template #footer>
        <el-button v-if="exportStatus === 'success'" type="primary" @click="handleDownloadExport">下载文件</el-button>
        <el-button v-if="exportStatus === 'success' || exportStatus === 'exception'" @click="exportDialogVisible = false">关闭</el-button>
        <el-button v-else @click="handleCloseExportDialog">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, View, Document } from '@element-plus/icons-vue'
import axios from 'axios'
import {
  getAuditLogList,
  getAuditLogDetail,
  createAuditLogExport,
  getExportTaskStatus
} from '@/api/finance'

// 数据状态
const loading = ref(false)
const exportLoading = ref(false)
const auditLogList = ref([])
const exportDialogVisible = ref(false)
const exportProgress = ref(0)
const exportStatus = ref('')
const exportMessage = ref('')
const currentTaskId = ref('')
let exportPollTimer = null

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  financeNo: '',
  operationType: '',
  operator: '',
  dateRange: []
})

// 查看详情
const viewDialogVisible = ref(false)
const viewFormData = ref({})

// 表格样式
const tableHeight = ref(400)
const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  fontWeight: '600',
  textAlign: 'center'
}

// 初始化
onMounted(() => {
  loadData()
})

// 关闭导出对话框时清除轮询
watch(exportDialogVisible, (visible) => {
  if (!visible && exportPollTimer) {
    clearInterval(exportPollTimer)
    exportPollTimer = null
  }
})

onBeforeUnmount(() => {
  if (exportPollTimer) {
    clearInterval(exportPollTimer)
    exportPollTimer = null
  }
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.size,
      financeId: null,
      financeNo: searchForm.financeNo || null,
      operationType: searchForm.operationType || null,
      operator: searchForm.operator || null,
      startTime: searchForm.dateRange?.[0] || null,
      endTime: searchForm.dateRange?.[1] || null
    }
    const res = await getAuditLogList(params)
    if (res.code === 200) {
      // MyBatis-Plus Page 序列化为 records + total
      auditLogList.value = res.data?.records ?? res.data?.data ?? []
      pagination.total = res.data?.total ?? 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 搜索和分页
function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchForm.financeNo = ''
  searchForm.operationType = ''
  searchForm.operator = ''
  searchForm.dateRange = []
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

// 导出功能
async function handleExport() {
  exportLoading.value = true
  try {
    const params = {
      financeId: null,
      financeNo: searchForm.financeNo || null,
      operationType: searchForm.operationType || null,
      operator: searchForm.operator || null,
      startTime: searchForm.dateRange?.[0] || null,
      endTime: searchForm.dateRange?.[1] || null
    }
    const res = await createAuditLogExport(params)
    if (res.code === 200) {
      currentTaskId.value = res.data.taskId
      exportDialogVisible.value = true
      exportProgress.value = 0
      exportStatus.value = ''
      exportMessage.value = '正在创建导出任务...'
      startPollingExportStatus()
    } else {
      ElMessage.error(res.message || '创建导出任务失败')
    }
  } catch (e) {
    ElMessage.error('创建导出任务失败')
  } finally {
    exportLoading.value = false
  }
}

// 轮询导出任务状态
function startPollingExportStatus() {
  if (exportPollTimer) {
    clearInterval(exportPollTimer)
  }
  
  exportPollTimer = setInterval(async () => {
    try {
      const res = await getExportTaskStatus(currentTaskId.value)
      if (res.code === 200) {
        const status = res.data.status
        if (status === 'PROCESSING') {
          exportProgress.value = 50
          exportStatus.value = ''
          exportMessage.value = '正在生成Excel文件...'
        } else if (status === 'COMPLETED') {
          exportProgress.value = 100
          exportStatus.value = 'success'
          exportMessage.value = '导出完成！'
          clearInterval(exportPollTimer)
          exportPollTimer = null
        } else if (status === 'FAILED') {
          exportProgress.value = 0
          exportStatus.value = 'exception'
          exportMessage.value = res.data.errorMessage || '导出失败'
          clearInterval(exportPollTimer)
          exportPollTimer = null
        }
      }
    } catch (e) {
      console.error('查询导出状态失败:', e)
      clearInterval(exportPollTimer)
      exportPollTimer = null
      exportStatus.value = 'exception'
      exportMessage.value = '查询任务状态失败，请关闭后重试'
    }
  }, 2000)
}

// 下载导出文件
async function handleDownloadExport() {
  try {
    const token = localStorage.getItem('token')
    const baseURL = process.env.NODE_ENV === 'production' ? '/api' : ''
    const response = await axios({
      baseURL,
      url: `/api/finance/audit/export/download/${currentTaskId.value}`,
      method: 'get',
      responseType: 'blob',
      timeout: 60000,
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })

    const contentType = response.headers['content-type'] || ''
    if (contentType.indexOf('application/json') !== -1) {
      const text = await response.data.text()
      let msg = '下载失败'
      try {
        const json = JSON.parse(text)
        msg = json.message || msg
      } catch (_) {}
      ElMessage.error(msg)
      return
    }

    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `财务审计日志_${new Date().getTime()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('文件下载成功')
    exportDialogVisible.value = false
  } catch (e) {
    let msg = '下载文件失败'
    if (e.response?.data && typeof e.response.data.text === 'function') {
      try {
        const text = await e.response.data.text()
        const parsed = text.startsWith('{') ? JSON.parse(text) : null
        msg = parsed?.message || text || msg
      } catch (_) {
        msg = e.message || msg
      }
    } else {
      msg = e.message || msg
    }
    ElMessage.error(msg)
  }
}

// 关闭导出对话框（取消导出）
function handleCloseExportDialog() {
  if (exportPollTimer) {
    clearInterval(exportPollTimer)
    exportPollTimer = null
  }
  exportDialogVisible.value = false
}

// 查看详情
function handleView(row) {
  viewFormData.value = { ...row }
  viewDialogVisible.value = true
}

function handleViewDialogClose() {
  viewFormData.value = {}
}

// 工具函数
function formatDateTime(datetime) {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

function getOperationTypeName(type) {
  const map = {
    CREATE: '创建',
    UPDATE: '修改',
    DELETE: '删除',
    RECONCILE: '对账'
  }
  return map[type] || type
}

function getOperationTypeTagType(type) {
  const map = {
    CREATE: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    RECONCILE: 'info'
  }
  return map[type] || ''
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
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.page-header {
  padding: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.search-input {
  width: 200px;
}

.search-buttons {
  display: flex;
  gap: 10px;
}

.action-card {
  padding: 16px 24px;
}

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

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

.export-progress {
  padding: 20px 0;
}

.export-message {
  margin-top: 16px;
  text-align: center;
  color: var(--color-text-secondary, #64748B);
}

.modern-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
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

.bmp-uiv-btn-primary {
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border: none;
}

.bmp-uiv-btn-primary:hover {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
}
</style>
