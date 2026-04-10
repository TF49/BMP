<template>
  <div class="member-management">
    <div class="member-management-background">
      <div class="gradient-overlay"></div>
    </div>

    <div class="member-management-content">
      <!-- 页面标题 -->
      <div class="page-header glass-card">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32">
                <UserFilled />
              </el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">会员管理</h2>
              <p class="page-subtitle">管理会员信息、类型、等级与状态，支持查看消费记录</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总会员数</span>
              <span class="stat-value">{{ statistics.total || 0 }}</span>
            </div>
            <div class="stat-item stat-item-success" @click="handleStatClick(1)">
              <span class="stat-label">正常</span>
              <span class="stat-value">{{ statistics.normal || 0 }}</span>
            </div>
            <div class="stat-item stat-item-warning" @click="handleStatClick(2)">
              <span class="stat-label">到期</span>
              <span class="stat-value">{{ statistics.expired || 0 }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">冻结</span>
              <span class="stat-value">{{ statistics.frozen || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="glass-card search-card">
        <div class="search-header">
          <h3 class="search-title">搜索过滤</h3>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <div class="search-container">
            <div class="search-fields">
              <el-form-item label="会员姓名" class="search-item">
                <el-input v-model="searchForm.memberName" placeholder="请输入会员姓名" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="手机号" class="search-item">
                <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="会员编号" class="search-item">
                <el-input v-model="searchForm.memberId" placeholder="MB000001 或数字ID" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="会员类型" class="search-item">
                <el-select v-model="searchForm.memberType" placeholder="请选择类型" clearable class="search-select">
                  <el-option v-for="item in memberTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.status" placeholder="请选择状态" clearable class="search-select">
                  <el-option label="正常" :value="1" />
                  <el-option label="冻结" :value="0" />
                  <el-option label="到期" :value="2" />
                </el-select>
              </el-form-item>
            </div>
            <div class="search-buttons">
              <el-button type="primary" @click="handleSearch" :icon="Search" class="search-btn bmp-uiv-btn bmp-uiv-btn-primary">
                搜索
              </el-button>
              <el-button type="primary" @click="handleReset" :icon="Refresh" class="reset-btn bmp-uiv-btn bmp-uiv-btn-primary">重置</el-button>
            </div>
          </div>
        </el-form>
      </div>

      <!-- 操作栏 -->
      <div class="glass-card action-card" v-if="isAdmin">
        <el-button type="primary" @click="handleAdd" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary">
          添加会员
        </el-button>
      </div>

      <!-- 会员列表表格 -->
      <div class="glass-card table-card">
        <el-table
          :data="memberList"
          v-loading="loading"
          element-loading-text="加载中..."
          style="width: 100%"
          stripe
          border
          highlight-current-row
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          row-key="id"
          :height="tableHeight"
          :virtual-scroll="memberList.length > 100"
        >
          <el-table-column
            label="序号"
            type="index"
            min-width="80"
            align="center"
            :index="(index) => (pagination.page - 1) * pagination.size + index + 1"
          />
          <el-table-column prop="id" label="会员编号" min-width="120" align="center">
            <template #default="scope">
              <el-tag size="small" type="info">{{ formatMemberNo(scope.row.id) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="memberName" label="姓名" min-width="140" align="center" />
          <el-table-column prop="phone" label="手机号" min-width="140" align="center" />
          <el-table-column prop="memberType" label="类型" min-width="110" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.memberType === 'MEMBER' ? 'success' : 'info'" size="small">
                {{ scope.row.memberType === 'MEMBER' ? '会员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="memberLevel" label="等级" min-width="90" align="center">
            <template #default="scope">
              <span v-if="scope.row.memberType === 'MEMBER' && scope.row.memberLevel">Lv{{ scope.row.memberLevel }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="110" align="center">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="余额/积分" min-width="120" align="center">
            <template #default="scope">
              <span>{{ formatCurrency(scope.row.balance) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="registerTime" label="注册时间" min-width="170" align="center">
            <template #default="scope">
              {{ formatDateTime(scope.row.registerTime || scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="expireTime" label="到期时间" min-width="170" align="center">
            <template #default="scope">
              {{ scope.row.expireTime ? formatDateTime(scope.row.expireTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="260" fixed="right" align="center">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button type="primary" size="small" @click="handleView(scope.row)" :icon="View" plain>
                  查看
                </el-button>
                <el-button v-if="isAdmin" type="success" size="small" @click="handleEdit(scope.row)" :icon="Edit" plain>
                  编辑
                </el-button>
                <el-button type="warning" size="small" @click="handleRecharge(scope.row)" plain>
                  充值
                </el-button>
                <el-button v-if="isAdmin" type="danger" size="small" @click="handleDelete(scope.row)" :icon="Delete" plain>
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="pagination"
          />
        </div>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :close-on-click-modal="false"
      class="member-dialog modern-dialog"
      @close="handleDialogClose"
      width="700px"
    >
      <el-form
        ref="memberFormRef"
        :model="memberForm"
        :rules="memberFormRules"
        label-width="auto"
        label-position="top"
        class="member-form modern-form"
      >
        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">基本信息</h4>
          <el-form-item label="会员姓名" prop="memberName" class="form-item-enhanced modern-form-item">
            <el-input
              v-model="memberForm.memberName"
              placeholder="请输入会员姓名（最多50字符）"
              clearable
              maxlength="50"
              show-word-limit
              class="form-input modern-form-input"
            >
              <template #prefix>
                <el-icon class="input-icon modern-input-icon">
                  <User />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="手机号" prop="phone" class="form-item-enhanced modern-form-item">
            <el-input
              v-model="memberForm.phone"
              placeholder="请输入手机号"
              clearable
              maxlength="20"
              class="form-input modern-form-input"
            >
              <template #prefix>
                <el-icon class="input-icon modern-input-icon">
                  <Phone />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="会员编号" class="form-item-enhanced modern-form-item">
            <el-input :model-value="formatMemberNo(memberForm.id) || '提交后自动生成'" disabled class="form-input modern-form-input" />
          </el-form-item>

          <el-form-item label="身份证号" prop="idCard" class="form-item-enhanced modern-form-item">
            <el-input v-model="memberForm.idCard" placeholder="可选，18位身份证号" clearable maxlength="18" class="form-input modern-form-input" />
          </el-form-item>
        </div>

        <div class="form-section modern-form-section">
          <h4 class="section-title modern-section-title">会员属性</h4>
          <el-form-item label="会员类型" prop="memberType" class="form-item-enhanced modern-form-item">
            <el-select v-model="memberForm.memberType" placeholder="请选择会员类型" class="form-input modern-form-input" @change="handleMemberTypeChange">
              <el-option v-for="item in memberTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>

          <el-form-item label="会员等级" prop="memberLevel" class="form-item-enhanced modern-form-item">
            <el-input-number
              v-model="memberForm.memberLevel"
              :min="1"
              :max="5"
              :step="1"
              :disabled="memberForm.memberType !== 'MEMBER'"
              controls-position="right"
              style="width: 100%;"
            />
            <span class="field-hint modern-field-hint">仅会员（MEMBER）需要等级，范围 1-5</span>
          </el-form-item>

          <el-form-item label="会员状态" prop="status" class="form-item-enhanced modern-form-item">
            <div class="status-selector modern-status-selector">
              <el-radio-group v-model="memberForm.status" class="radio-group-enhanced modern-radio-group">
                <el-radio :label="1" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon enabled modern-status-icon">
                      <SuccessFilled />
                    </el-icon>
                    正常
                  </span>
                </el-radio>
                <el-radio :label="2" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon warning modern-status-icon">
                      <Warning />
                    </el-icon>
                    到期
                  </span>
                </el-radio>
                <el-radio :label="0" class="radio-item modern-radio-item">
                  <span class="radio-label modern-radio-label">
                    <el-icon class="status-icon disabled modern-status-icon">
                      <CircleCloseFilled />
                    </el-icon>
                    冻结
                  </span>
                </el-radio>
              </el-radio-group>
            </div>
            <span class="field-hint modern-field-hint">冻结将限制登录/预约，状态变更会同步列表和统计</span>
          </el-form-item>

          <el-form-item label="到期时间" prop="expireTime" class="form-item-enhanced modern-form-item">
            <el-date-picker
              v-model="memberForm.expireTime"
              type="date"
              placeholder="选择到期日期（普通用户可不填）"
              style="width: 100%;"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>

          <el-form-item label="账户余额/积分" prop="balance" class="form-item-enhanced modern-form-item">
            <el-input-number
              v-model="memberForm.balance"
              :min="0"
              :step="0.01"
              controls-position="right"
              style="width: 100%;"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button @click="dialogVisible = false" class="btn-cancel modern-btn-cancel">
            取消
          </el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="btn-submit modern-btn-submit">
            {{ isEdit ? '更新会员' : '创建会员' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 查看会员对话框 -->
    <el-dialog
      title="会员详情"
      v-model="viewDialogVisible"
      width="780px"
      @close="handleViewDialogClose"
      class="modern-dialog"
    >
      <el-tabs v-model="viewActiveTab" class="member-view-tabs">
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border :model="viewFormData" class="modern-descriptions">
            <el-descriptions-item label="会员编号" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">{{ formatMemberNo(viewFormData.id) }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="会员姓名" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.memberName || '-' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="手机号" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.phone || '-' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="类型" class="modern-descriptions-item">
              <template #default>
                <el-tag :type="viewFormData.memberType === 'MEMBER' ? 'success' : 'info'" size="small">
                  {{ viewFormData.memberType === 'MEMBER' ? '会员' : '普通用户' }}
                </el-tag>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="等级" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">
                  <span v-if="viewFormData.memberType === 'MEMBER' && viewFormData.memberLevel">Lv{{ viewFormData.memberLevel }}</span>
                  <span v-else>-</span>
                </span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="状态" class="modern-descriptions-item">
              <template #default>
                <el-tag :type="getStatusType(viewFormData.status)" size="small">
                  {{ getStatusText(viewFormData.status) }}
                </el-tag>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="身份证号" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.idCard || '-' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">{{ formatDateTime(viewFormData.registerTime || viewFormData.createTime) }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="到期时间" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">{{ viewFormData.expireTime ? formatDateTime(viewFormData.expireTime) : '-' }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="余额/积分" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">{{ formatCurrency(viewFormData.balance) }}</span>
              </template>
            </el-descriptions-item>
            <el-descriptions-item label="累计消费" class="modern-descriptions-item">
              <template #default>
                <span class="modern-descriptions-value">{{ formatCurrency(viewFormData.totalConsumption) }}</span>
              </template>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="消费记录" name="consume">
          <div class="consume-records-container">
            <el-table
              :data="consumeRecords"
              v-loading="consumeLoading"
              element-loading-text="加载中..."
              size="small"
              border
              stripe
              :header-cell-style="tableHeaderStyle"
            >
              <el-table-column prop="createTime" label="时间" min-width="160" align="center">
                <template #default="scope">
                  {{ formatDateTime(scope.row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="businessType" label="业务类型" min-width="120" align="center">
                <template #default="scope">
                  {{ businessTypeLabelMap[scope.row.businessType] || scope.row.businessType }}
                </template>
              </el-table-column>
              <el-table-column prop="description" label="消费描述" min-width="180" align="center" show-overflow-tooltip />
              <el-table-column prop="incomeExpenseType" label="收支" min-width="80" align="center">
                <template #default="scope">
                  <el-tag :type="scope.row.incomeExpenseType === 1 ? 'success' : 'danger'" size="small">
                    {{ scope.row.incomeExpenseType === 1 ? '收入' : '支出' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="amount" label="金额" min-width="100" align="center">
                <template #default="scope">
                  <span :class="scope.row.incomeExpenseType === 1 ? 'price-income' : 'price-expense'">
                    {{ scope.row.incomeExpenseType === 1 ? '+' : '-' }}{{ formatCurrency(scope.row.amount) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" min-width="120" align="center">
                <template #default="scope">
                  {{ paymentMethodLabelMap[scope.row.paymentMethod] || scope.row.paymentMethod }}
                </template>
              </el-table-column>
              <el-table-column prop="remark" label="备注" min-width="160" align="center" show-overflow-tooltip />
            </el-table>
            <div class="pagination-wrapper" style="margin-top: 12px;">
              <el-pagination
                v-model:current-page="consumePagination.page"
                v-model:page-size="consumePagination.size"
                :total="consumePagination.total"
                :page-sizes="[5, 10, 20]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleConsumeSizeChange"
                @current-change="handleConsumePageChange"
                class="pagination"
                small
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <span class="dialog-footer modern-dialog-footer">
          <el-button @click="viewDialogVisible = false" class="modern-btn-cancel">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 管理员充值对话框 -->
    <el-dialog v-model="rechargeDialogVisible" title="会员充值" width="420px">
      <el-form label-width="90px">
        <el-form-item label="会员编号">
          <el-tag type="info">{{ formatMemberNo(rechargeForm.memberId) }}</el-tag>
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeForm.amount" :min="1" :step="50" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="rechargeForm.method" placeholder="请选择支付方式">
            <el-option v-for="item in rechargeMethods" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.remark" type="textarea" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="rechargeSubmitting" :disabled="rechargeSubmitting" @click="submitRecharge">确认充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  View,
  User,
  Phone,
  UserFilled,
  SuccessFilled,
  Warning,
  CircleCloseFilled
} from '@element-plus/icons-vue'
import {
  getMemberList,
  addMember,
  updateMember,
  deleteMember,
  getMemberInfo,
  getMemberStatistics,
  getMemberConsumeList
} from '@/api/member'
import { adminRecharge } from '@/api/recharge'

// 搜索表单
const searchForm = reactive({
  memberName: '',
  phone: '',
  memberId: '',
  memberType: null,
  status: null
})

// 会员列表
const memberList = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 统计数据
const statistics = reactive({
  total: 0,
  normal: 0,
  frozen: 0,
  expired: 0
})

// 表格高度
const tableHeight = ref(600)

// 对话框
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const viewActiveTab = ref('info')
const dialogTitle = ref('添加会员')
const isEdit = ref(false)
const submitLoading = ref(false)
const memberFormRef = ref(null)

// 消费记录
const consumeRecords = ref([])
const consumeLoading = ref(false)
const consumePagination = reactive({
  page: 1,
  size: 5,
  total: 0
})

// 枚举
const memberTypeOptions = [
  { label: '普通用户', value: 'NORMAL' },
  { label: '会员', value: 'MEMBER' }
]

// 消费记录：业务类型、支付方式中文映射
const businessTypeLabelMap = {
  BOOKING: '场地预约',
  COURSE: '课程预约',
  EQUIPMENT: '器材租借',
  TOURNAMENT: '赛事报名',
  STRINGING: '穿线服务',
  RECHARGE: '会员充值',
  OTHER: '其他'
}
const paymentMethodLabelMap = {
  CASH: '现金',
  ALIPAY: '支付宝',
  WECHAT: '微信',
  BALANCE: '余额'
}

// 获取用户角色和权限
const { userRole, isAdmin } = useAuth()

// 表单数据
const memberForm = reactive({
  id: null,
  memberName: '',
  phone: '',
  memberType: 'MEMBER',
  memberLevel: 1,
  status: 1,
  idCard: '',
  expireTime: '',
  balance: 0
})

// 充值弹窗
const rechargeDialogVisible = ref(false)
const rechargeSubmitting = ref(false)
const rechargeForm = reactive({
  memberId: null,
  amount: 200,
  method: 'ALIPAY',
  remark: ''
})
const rechargeMethods = [
  { label: '支付宝', value: 'ALIPAY' },
  { label: '微信', value: 'WECHAT' },
  { label: '现金', value: 'CASH' },
  { label: '银行卡', value: 'BANK' }
]

// 详情表单
const viewFormData = reactive({
  id: null,
  memberName: '',
  phone: '',
  memberType: 'NORMAL',
  memberLevel: null,
  status: 1,
  idCard: '',
  registerTime: '',
  expireTime: '',
  balance: 0,
  totalConsumption: 0,
  createTime: ''
})

// 校验规则
const memberFormRules = {
  memberName: [
    { required: true, message: '请输入会员姓名', trigger: 'blur' },
    { max: 50, message: '会员姓名不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    {
      required: true,
      validator: (rule, value, callback) => {
        if (!value || value.trim() === '') {
          callback(new Error('请输入手机号'))
          return
        }
        const mobilePattern = /^1[3-9]\d{9}$/
        if (mobilePattern.test(value)) {
          callback()
        } else {
          callback(new Error('请输入正确的手机号'))
        }
      },
      trigger: 'blur'
    }
  ],
  memberType: [{ required: true, message: '请选择会员类型', trigger: 'change' }],
  memberLevel: [
    {
      validator: (rule, value, callback) => {
        if (memberForm.memberType !== 'MEMBER') {
          callback()
          return
        }
        if (value === null || value === undefined || value === '') {
          callback(new Error('会员等级为必填'))
          return
        }
        if (value < 1 || value > 5) {
          callback(new Error('会员等级需在 1-5 范围内'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  idCard: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback()
          return
        }
        const idCardPattern = /^\d{15}$|^\d{17}[\dXx]$/
        if (!idCardPattern.test(value)) {
          callback(new Error('请输入正确的身份证号'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

// 状态文案/类型
const getStatusText = (status) => {
  const map = {
    0: '冻结',
    1: '正常',
    2: '到期'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return map[status] || 'info'
}

const formatMemberNo = (id) => {
  if (!id) return ''
  return `MB${String(id).padStart(6, '0')}`
}

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0.00'
  const num = Number(value) || 0
  return num.toFixed(2)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return '-'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 加载列表
const loadMemberList = async () => {
  loading.value = true
  try {
    const params = {
      memberName: searchForm.memberName || null,
      phone: searchForm.phone || null,
      memberId: parseMemberId(searchForm.memberId),
      memberType: searchForm.memberType,
      status: searchForm.status,
      page: pagination.page,
      size: pagination.size
    }
    const res = await getMemberList(params)
    if (res.code === 200) {
      memberList.value = res.data?.data || []
      pagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取会员列表失败')
    }
  } catch (e) {
    console.error('加载会员列表失败:', e)
    ElMessage.error('加载会员列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  if (userRole.value !== 'PRESIDENT') {
    Object.assign(statistics, {
      total: 0,
      normal: 0,
      frozen: 0,
      expired: 0
    })
    return
  }

  try {
    const res = await getMemberStatistics()
    if (res.code === 200) {
      Object.assign(statistics, res.data || {})
    }
  } catch (e) {
    console.error('加载会员统计失败:', e)
  }
}

const parseMemberId = (input) => {
  if (!input) return null
  const trimmed = input.trim().toUpperCase()
  if (trimmed.startsWith('MB')) {
    const numPart = trimmed.replace('MB', '')
    const num = Number(numPart)
    return Number.isNaN(num) ? null : num
  }
  const num = Number(trimmed)
  return Number.isNaN(num) ? null : num
}

// 统计卡片过滤
const handleStatClick = (status) => {
  searchForm.status = status
  pagination.page = 1
  loadMemberList()
}

const handleSearch = () => {
  pagination.page = 1
  loadMemberList()
}

const handleReset = () => {
  searchForm.memberName = ''
  searchForm.phone = ''
  searchForm.memberId = ''
  searchForm.memberType = null
  searchForm.status = null
  pagination.page = 1
  loadMemberList()
}

const handleAdd = () => {
  dialogTitle.value = '添加会员'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleRecharge = (row) => {
  rechargeForm.memberId = row.id
  rechargeForm.amount = 200
  rechargeForm.method = 'ALIPAY'
  rechargeForm.remark = ''
  rechargeDialogVisible.value = true
}

const submitRecharge = async () => {
  // 防止重复提交：如果正在提交，直接返回
  if (rechargeSubmitting.value) {
    return
  }
  
  if (!rechargeForm.memberId || !rechargeForm.amount || rechargeForm.amount <= 0) {
    ElMessage.warning('请填写有效的充值金额')
    return
  }
  
  rechargeSubmitting.value = true
  try {
    const res = await adminRecharge({
      memberId: rechargeForm.memberId,
      amount: rechargeForm.amount,
      method: rechargeForm.method,
      remark: rechargeForm.remark
    })
    if (res.code === 200) {
      ElMessage.success(res.data?.message || '充值成功')
      rechargeDialogVisible.value = false
      // 立即更新列表中该行：类型/等级/累计充值/余额（便于界面即时反映累计充值升级）
      const row = rechargeForm.memberId ? memberList.value.find((m) => m.id === rechargeForm.memberId) : null
      if (row) {
        if (res.data?.rechargeRecord?.isUpgraded === 1) {
          row.memberType = 'MEMBER'
          row.memberLevel = 1
        }
        if (res.data?.memberLevel != null) row.memberLevel = res.data.memberLevel
        if (res.data?.totalRecharge != null) row.totalRecharge = res.data.totalRecharge
        row.balance = (Number(row.balance) || 0) + Number(rechargeForm.amount || 0)
      }
      loadMemberList()
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
    rechargeSubmitting.value = false
  }
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑会员'
  isEdit.value = true
  try {
    const res = await getMemberInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      Object.assign(memberForm, {
        id: data.id,
        memberName: data.memberName || '',
        phone: data.phone || '',
        memberType: data.memberType || 'NORMAL',
        memberLevel: data.memberType === 'MEMBER' ? (data.memberLevel || 1) : null,
        status: data.status !== undefined ? data.status : 1,
        idCard: data.idCard || '',
        expireTime: data.expireTime || '',
        balance: data.balance !== undefined && data.balance !== null ? data.balance : 0
      })
    }
    dialogVisible.value = true
  } catch (e) {
    console.error('加载会员详情失败:', e)
    ElMessage.error('加载会员详情失败，请稍后重试')
  }
}

const handleView = async (row) => {
  viewActiveTab.value = 'info'
  consumePagination.page = 1
  try {
    const res = await getMemberInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      Object.assign(viewFormData, {
        id: data.id,
        memberName: data.memberName || '',
        phone: data.phone || '',
        memberType: data.memberType || 'NORMAL',
        memberLevel: data.memberLevel || null,
        status: data.status !== undefined ? data.status : 1,
        idCard: data.idCard || '',
        registerTime: data.registerTime || data.createTime || '',
        expireTime: data.expireTime || '',
        balance: data.balance || 0,
        totalConsumption: data.totalConsumption || 0,
        createTime: data.createTime || ''
      })
      viewDialogVisible.value = true
      await loadConsumeRecords(row.id)
    } else {
      ElMessage.error(res.message || '获取会员详情失败')
    }
  } catch (e) {
    console.error('获取会员详情失败:', e)
    ElMessage.error('获取会员详情失败，请稍后重试')
  }
}

const loadConsumeRecords = async (memberId) => {
  consumeLoading.value = true
  try {
    const res = await getMemberConsumeList(memberId, {
      page: consumePagination.page,
      size: consumePagination.size
    })
    if (res.code === 200) {
      consumeRecords.value = res.data?.data || []
      consumePagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取消费记录失败')
    }
  } catch (e) {
    console.error('获取消费记录失败:', e)
    // 后端未实现时保持空表
    consumeRecords.value = []
    consumePagination.total = 0
  } finally {
    consumeLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除会员 "${row.memberName}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteMember(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadMemberList()
        loadStatistics()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (e) {
      console.error('删除会员失败:', e)
      ElMessage.error('删除失败，请稍后重试')
    }
  }).catch(() => { })
}

const handleSubmit = async () => {
  if (!memberFormRef.value) return
  await memberFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload = {
        ...memberForm,
        memberLevel: memberForm.memberType === 'MEMBER' ? memberForm.memberLevel : null
      }
      const res = isEdit.value ? await updateMember(payload) : await addMember(payload)
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadMemberList()
        loadStatistics()
      } else {
        ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'))
      }
    } catch (e) {
      console.error('提交失败:', e)
      ElMessage.error('操作失败，请稍后重试')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleMemberTypeChange = (val) => {
  if (val !== 'MEMBER') {
    memberForm.memberLevel = null
  } else if (!memberForm.memberLevel) {
    memberForm.memberLevel = 1
  }
}

const handleConsumeSizeChange = (size) => {
  consumePagination.size = size
  consumePagination.page = 1
  if (viewFormData.id) {
    loadConsumeRecords(viewFormData.id)
  }
}

const handleConsumePageChange = (page) => {
  consumePagination.page = page
  if (viewFormData.id) {
    loadConsumeRecords(viewFormData.id)
  }
}

const resetForm = () => {
  Object.assign(memberForm, {
    id: null,
    memberName: '',
    phone: '',
    memberType: 'MEMBER',
    memberLevel: 1,
    status: 1,
    idCard: '',
    expireTime: '',
    balance: 0
  })
  if (memberFormRef.value) {
    memberFormRef.value.resetFields()
  }
}

const handleDialogClose = () => {
  resetForm()
}

const handleViewDialogClose = () => {
  Object.assign(viewFormData, {
    id: null,
    memberName: '',
    phone: '',
    memberType: 'NORMAL',
    memberLevel: null,
    status: 1,
    idCard: '',
    registerTime: '',
    expireTime: '',
    balance: 0,
    totalConsumption: 0,
    createTime: ''
  })
  consumeRecords.value = []
  consumePagination.page = 1
  consumePagination.total = 0
  viewActiveTab.value = 'info'
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadMemberList()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadMemberList()
}

const calculateTableHeight = () => {
  const windowHeight = window.innerHeight
  tableHeight.value = Math.max(400, windowHeight - 400)
}

onMounted(() => {
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
  loadMemberList()
  loadStatistics()
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateTableHeight)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Fira+Code:wght@400;500;600;700&family=Fira+Sans:wght@300;400;500;600;700&display=swap');

.member-management {
  padding: 20px;
  background-color: var(--color-background, #f0f2f5);
  height: calc(100vh - 84px);
  overflow-y: auto;
  position: relative;
}

.member-management::-webkit-scrollbar {
  width: 8px;
}

.member-management::-webkit-scrollbar-track {
  background: var(--color-background, #F1F5F9);
  border-radius: 4px;
}

.member-management::-webkit-scrollbar-thumb {
  background: var(--color-border-hover, #CBD5E1);
  border-radius: 4px;
}

.member-management::-webkit-scrollbar-thumb:hover {
  background: var(--color-text-muted, #94A3B8);
}

.member-management-background {
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

.member-management-content {
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
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.page-header {
  padding: 28px;
  animation: fadeInDown 0.6s ease-out;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.header-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 12px;
  transition: all 0.3s ease;
  color: #FFFFFF;
}

.header-icon:hover {
  transform: scale(1.1) rotate(5deg);
}

.header-text {
  flex: 1;
}

.page-title {
  font-family: 'Fira Sans', sans-serif;
  font-size: 26px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0 0 8px 0;
  animation: slideInLeft 0.6s ease-out 0.1s both;
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.page-subtitle {
  font-family: 'Fira Sans', sans-serif;
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin: 0;
  animation: slideInLeft 0.6s ease-out 0.2s both;
}

.header-stats {
  /**
   * 统计卡片等宽优化：使用固定宽度确保所有按钮大小一致
   */
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: nowrap;
}

.stat-item {
  /**
   * 固定宽度方案：确保所有按钮视觉上完全一致
   */
  width: 80px;
  min-width: 80px;
  max-width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 14px 8px;
  background: var(--gradient-members, linear-gradient(135deg, #DBEAFE 0%, #EFF6FF 100%));
  border-radius: 14px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.stat-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.stat-item-success {
  background: linear-gradient(135deg, #D1FAE5 0%, #ECFDF5 100%) !important;
}
.stat-item-success:hover {
  box-shadow: 0 8px 16px rgba(16, 185, 129, 0.2) !important;
}
.stat-item-success .stat-value {
  color: var(--color-success, #10b981);
}

.stat-item-warning {
  background: linear-gradient(135deg, #FEF3C7 0%, #FFFBEB 100%) !important;
}
.stat-item-warning:hover {
  box-shadow: 0 8px 16px rgba(245, 158, 11, 0.2) !important;
}
.stat-item-warning .stat-value {
  color: var(--color-warning, #f59e0b);
}

.stat-item-danger {
  background: linear-gradient(135deg, #FEE2E2 0%, #FEF2F2 100%) !important;
}
.stat-item-danger:hover {
  box-shadow: 0 8px 16px rgba(239, 68, 68, 0.2) !important;
}
.stat-item-danger .stat-value {
  color: var(--color-danger, #ef4444);
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
  font-weight: 500;
  text-align: center;
  line-height: 1.4;
  word-break: keep-all;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary, #3b82f6);
  font-family: 'Fira Code', monospace;
}

.search-card {
  padding: 24px;
  animation: fadeInUp 0.6s ease-out 0.1s both;
}

.search-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.search-title {
  font-family: 'Fira Sans', sans-serif;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary, #1E293B);
  margin: 0;
}

.search-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
}

.search-fields {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: flex-end;
  flex: 1;
}

.search-item {
  margin-bottom: 0 !important;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 150px;
}

.search-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-shrink: 0;
}

/**
 * 按钮悬停上浮效果（与场地管理页面保持一致的交互感）
 * 说明：仅影响视觉表现，不改变业务逻辑
 */
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

/* 不同类型按钮增加更贴近语义的阴影色 */
.search-btn:hover,
.action-card :deep(.el-button--primary:hover),
.operation-buttons :deep(.el-button--primary:hover) {
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.35);
}

.operation-buttons :deep(.el-button--success:hover) {
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.28);
}

.operation-buttons :deep(.el-button--warning:hover) {
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.28);
}

.operation-buttons :deep(.el-button--danger:hover) {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.28);
}

.action-card {
  padding: 16px 24px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.table-card {
  padding: 24px;
  animation: fadeInUp 0.6s ease-out 0.3s both;
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

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
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

.member-dialog :deep(.el-dialog) {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
}

.member-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  padding: 20px 24px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 16px 16px 0 0;
}

.member-dialog :deep(.el-dialog__title) {
  color: #ffffff;
  font-family: 'Fira Sans', sans-serif;
  font-size: 18px;
  font-weight: 600;
}

.modern-form-section {
  margin-bottom: 28px;
  padding: 28px;
  background: linear-gradient(135deg, var(--color-card-bg, #ffffff) 0%, var(--color-background, rgba(248, 250, 252, 0.95)) 100%);
  border-radius: 16px;
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
}

.modern-section-title {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  display: flex;
  align-items: center;
  gap: 12px;
}

.modern-section-title::before {
  content: '';
  width: 5px;
  height: 24px;
  background: linear-gradient(180deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border-radius: 3px;
}

.modern-form-item {
  margin-bottom: 24px !important;
}

.modern-form-input {
  width: 100%;
}

.modern-input-icon {
  color: var(--color-primary, #3B82F6);
  font-size: 18px;
}

.modern-field-hint {
  display: block;
  margin-top: 10px;
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
  font-style: italic;
}

.modern-status-selector {
  display: flex;
  gap: 32px;
  padding: 16px;
  background: var(--color-background, rgba(248, 250, 252, 0.9));
  border-radius: 12px;
}

.modern-radio-group {
  display: flex;
  gap: 32px;
  width: 100%;
}

.modern-radio-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 24px;
  border-radius: 12px;
  background: var(--color-card-bg, #ffffff);
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
  cursor: pointer;
  transition: all 0.3s ease;
}

.modern-radio-item:hover {
  border-color: var(--color-primary, rgba(59, 130, 246, 0.4));
}

.modern-radio-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.modern-status-icon {
  font-size: 22px;
}
.modern-status-icon.enabled {
  color: var(--color-success, #10b981);
}
.modern-status-icon.warning {
  color: var(--color-warning, #f59e0b);
}
.modern-status-icon.disabled {
  color: var(--color-danger, #ef4444);
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 24px 0 0 0;
}

.modern-btn-cancel {
  padding: 14px 32px;
  font-weight: 600;
  border-radius: 12px;
  border: 2px solid var(--color-border, rgba(226, 232, 240, 0.8));
  background: var(--color-card-bg, #ffffff);
  color: var(--color-text-secondary, #64748b);
}

.modern-btn-submit {
  padding: 14px 36px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  border: none;
  color: #ffffff;
}

.modern-descriptions {
  border-radius: 16px;
  overflow: hidden;
}

.modern-descriptions-item {
  padding: 20px 24px;
}

.modern-descriptions-value {
  font-size: 15px;
  color: var(--color-text-primary, #1e293b);
  font-weight: 500;
}

.member-view-tabs {
  margin-top: 10px;
}

.consume-records-container {
  padding: 8px 0;
}

.price-income {
  color: #10b981;
  font-weight: 600;
}

.price-expense {
  color: #ef4444;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .header-stats {
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .stat-item {
    width: 72px;
    min-width: 72px;
    max-width: 72px;
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

  .stat-item {
    width: 68px;
    min-width: 68px;
    max-width: 68px;
  }

  .search-container {
    flex-direction: column;
    align-items: stretch;
  }

  .search-fields {
    flex-direction: column;
  }

  .search-input,
  .search-select {
    width: 100%;
  }

  .operation-buttons {
    flex-direction: column;
  }
}

/* 按钮悬停上浮效果（与场馆管理页面保持一致的交互感）
 * 说明：仅影响视觉表现，不改变业务逻辑
 */
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

/* 不同类型按钮增加更贴近语义的阴影色 */
.search-btn:hover,
.action-card :deep(.el-button--primary:hover),
.operation-buttons :deep(.el-button--primary:hover) {
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.35);
}

.operation-buttons :deep(.el-button--success:hover) {
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.28);
}

.operation-buttons :deep(.el-button--warning:hover) {
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.28);
}

.operation-buttons :deep(.el-button--danger:hover) {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.28);
}

</style>
