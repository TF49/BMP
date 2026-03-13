<template>
  <div class="page-wrapper">
    <div class="page-bg"><div class="gradient-overlay"></div></div>

    <div class="page-content">
      <div class="glass-card page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <el-icon :size="32"><Tools /></el-icon>
            </div>
            <div class="header-text">
              <h2 class="page-title">穿线服务管理</h2>
              <p class="page-subtitle">管理穿线服务申请、状态更新与完成，快速查看服务进度</p>
            </div>
          </div>
          <div class="header-stats">
            <div class="stat-item" @click="handleStatClick(null)">
              <span class="stat-label">总服务数</span>
              <span class="stat-value">{{ statistics.total || 0 }}</span>
            </div>
            <div class="stat-item stat-item-warning" @click="handleStatClick(1)">
              <span class="stat-label">等待穿线</span>
              <span class="stat-value">{{ statistics.waiting || 0 }}</span>
            </div>
            <div class="stat-item stat-item-primary" @click="handleStatClick(2)">
              <span class="stat-label">正在穿线</span>
              <span class="stat-value">{{ statistics.inProgress || 0 }}</span>
            </div>
            <div class="stat-item stat-item-success" @click="handleStatClick(3)">
              <span class="stat-label">已完成</span>
              <span class="stat-value">{{ statistics.completed || 0 }}</span>
            </div>
            <div class="stat-item stat-item-danger" @click="handleStatClick(0)">
              <span class="stat-label">已取消</span>
              <span class="stat-value">{{ statistics.cancelled || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="glass-card search-card">
        <div class="search-header">
          <h3 class="search-title">搜索过滤</h3>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <div class="search-container">
            <div class="search-fields">
              <el-form-item label="服务单号" class="search-item">
                <el-input v-model="searchForm.serviceNo" placeholder="输入服务单号" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="会员" class="search-item">
                <el-input v-model="searchForm.memberKeyword" placeholder="姓名/手机号" clearable class="search-input" />
              </el-form-item>
              <el-form-item label="场馆" class="search-item">
                <el-select
                  v-model="searchForm.venueId"
                  placeholder="全部场馆"
                  :clearable="!isVenueManager"
                  :disabled="isVenueManager"
                  class="search-select"
                >
                  <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.status" placeholder="全部状态" clearable class="search-select-small">
                  <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="时间" class="search-item">
                <el-date-picker
                  v-model="searchForm.timeRange"
                  type="datetimerange"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  start-placeholder="开始"
                  end-placeholder="结束"
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

      <div class="glass-card action-card" v-if="isAdmin">
        <el-button type="primary" :icon="Plus" class="bmp-uiv-btn bmp-uiv-btn-primary" @click="handleAdd">添加穿线</el-button>
      </div>

      <div class="glass-card table-card">
        <el-table
          :data="serviceList"
          v-loading="loading"
          border
          stripe
          :height="tableHeight"
          :header-cell-style="tableHeaderStyle"
          :cell-style="{ textAlign: 'center', padding: '12px 0' }"
          row-key="id"
        >
          <el-table-column prop="serviceNo" label="服务单号" min-width="140" />
          <el-table-column prop="memberName" label="会员" min-width="120">
            <template #default="scope">
              {{ scope.row.memberName || scope.row.userName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="venueName" label="场馆" min-width="120" />
          <el-table-column label="球拍信息" min-width="180">
            <template #default="scope">
              <div style="text-align: left;">
                <div>{{ scope.row.racketBrand }} {{ scope.row.racketModel }}</div>
                <div style="font-size: 12px; color: var(--color-text-secondary, #64748B);">
                  {{ scope.row.hasBreakage ? '有断裂' : '无断裂' }} / {{ scope.row.hasCollapse ? '有塌陷' : '无塌陷' }}
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="线材" min-width="120">
            <template #default="scope">
              {{ scope.row.stringName || scope.row.stringEquipmentName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="pound" label="磅数" min-width="80">
            <template #default="scope">
              {{ scope.row.pound }}磅
            </template>
          </el-table-column>
          <el-table-column label="穿线法" min-width="100">
            <template #default="scope">
              {{ getStringingMethodText(scope.row.stringingMethod) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="110">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="价格" min-width="100">
            <template #default="scope">
              <span class="price-text">¥{{ formatCurrency(scope.row.servicePrice) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentMethod" label="支付方式" min-width="110">
            <template #default="scope">
              <span>{{ getPaymentMethodText(scope.row.paymentMethod) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentStatus" label="支付状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)" size="small">
                {{ getPaymentStatusText(scope.row.paymentStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="380" fixed="right">
            <template #default="scope">
              <div class="operation-buttons">
                <el-button type="primary" size="small" plain @click="handleView(scope.row)">查看</el-button>
                <!-- 待支付显示支付，已支付显示退款（同一位置互斥，与赛事报名/场地预约一致） -->
                <el-button
                  v-if="isAdmin && scope.row.paymentStatus === 0 && scope.row.memberId && scope.row.servicePrice != null && Number(scope.row.servicePrice) > 0"
                  type="warning"
                  size="small"
                  plain
                  @click="openPay(scope.row)"
                >
                  支付
                </el-button>
                <el-button
                  v-else-if="isAdmin && scope.row.paymentStatus === 1"
                  type="info"
                  size="small"
                  plain
                  @click="handleRefund(scope.row)"
                >
                  退款
                </el-button>
                <el-button
                  v-if="scope.row.status === 1 && isAdmin"
                  type="success"
                  size="small"
                  plain
                  @click="handleStartStringing(scope.row)"
                >
                  开始穿线
                </el-button>
                <el-button
                  v-if="scope.row.status === 2 && isAdmin"
                  type="primary"
                  size="small"
                  plain
                  @click="handleCompleteStringing(scope.row)"
                >
                  完成穿线
                </el-button>
                <el-button
                  v-if="(scope.row.status === 1 || scope.row.status === 2) && isAdmin"
                  type="warning"
                  size="small"
                  plain
                  @click="handleCancelService(scope.row)"
                >
                  取消
                </el-button>
                <el-button v-if="isAdmin" type="success" size="small" :icon="Edit" plain @click="handleEdit(scope.row)">编辑</el-button>
                <el-button v-if="isAdmin" type="danger" size="small" :icon="Delete" plain @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="服务详情"
      width="700px"
      :close-on-click-modal="false"
      class="modern-dialog"
    >
      <div v-if="currentService" class="detail-content">
        <div class="detail-section">
          <h4 class="detail-title">基本信息</h4>
          <div class="detail-item">
            <span class="detail-label">服务单号：</span>
            <span class="detail-value">{{ currentService.serviceNo }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">会员：</span>
            <span class="detail-value">{{ currentService.memberName || currentService.userName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">场馆：</span>
            <span class="detail-value">{{ currentService.venueName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态：</span>
            <el-tag :type="getStatusType(currentService.status)" size="small">
              {{ getStatusText(currentService.status) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-section">
          <h4 class="detail-title">球拍信息</h4>
          <div class="detail-item">
            <span class="detail-label">品牌：</span>
            <span class="detail-value">{{ currentService.racketBrand }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">型号：</span>
            <span class="detail-value">{{ currentService.racketModel }}</span>
          </div>
          <div class="detail-item" v-if="currentService.racketDescription">
            <span class="detail-label">描述：</span>
            <span class="detail-value">{{ currentService.racketDescription }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">断裂/塌陷：</span>
            <span class="detail-value">
              {{ currentService.hasBreakage ? '有断裂' : '无断裂' }} / {{ currentService.hasCollapse ? '有塌陷' : '无塌陷' }}
            </span>
          </div>
        </div>
        <div class="detail-section">
          <h4 class="detail-title">线材信息</h4>
          <div class="detail-item">
            <span class="detail-label">线材：</span>
            <span class="detail-value">{{ currentService.stringName || currentService.stringEquipmentName || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">是否自带：</span>
            <span class="detail-value">{{ currentService.isOwnString === 1 ? '是' : '否' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">磅数：</span>
            <span class="detail-value">{{ currentService.pound }}磅</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">穿线法：</span>
            <span class="detail-value">{{ getStringingMethodText(currentService.stringingMethod) }}</span>
          </div>
        </div>
        <div class="detail-section">
          <h4 class="detail-title">服务信息</h4>
          <div class="detail-item">
            <span class="detail-label">服务价格：</span>
            <span class="detail-value price">¥{{ formatCurrency(currentService.servicePrice) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">支付方式：</span>
            <span class="detail-value">{{ getPaymentMethodText(currentService.paymentMethod) || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">支付状态：</span>
            <el-tag :type="getPaymentStatusType(currentService.paymentStatus)" size="small">
              {{ getPaymentStatusText(currentService.paymentStatus) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">创建时间：</span>
            <span class="detail-value">{{ formatDateTime(currentService.createTime) }}</span>
          </div>
          <div class="detail-item" v-if="currentService.startTime">
            <span class="detail-label">开始时间：</span>
            <span class="detail-value">{{ formatDateTime(currentService.startTime) }}</span>
          </div>
          <div class="detail-item" v-if="currentService.remark">
            <span class="detail-label">备注：</span>
            <span class="detail-value">{{ currentService.remark }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button class="modern-btn-cancel" @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editDialogTitle"
      width="680px"
      :close-on-click-modal="false"
      class="modern-dialog"
      @close="handleEditDialogClose"
    >
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-position="top" class="modern-form">
        <div class="modern-form-section">
          <h4 class="modern-section-title">服务信息</h4>
          <el-form-item label="服务单号" class="modern-form-item">
            <el-input :model-value="editForm.serviceNo || '提交后自动生成'" disabled />
          </el-form-item>
          <el-form-item label="场馆" prop="venueId" class="modern-form-item">
            <el-select v-model="editForm.venueId" placeholder="选择场馆" filterable style="width: 100%" :disabled="isVenueManager">
              <el-option v-for="item in venueOptions" :key="item.id" :label="item.venueName" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="会员" prop="memberId" class="modern-form-item">
            <el-input
              v-model="memberKeyword"
              placeholder="输入姓名/手机号以搜索"
              @input="loadMembers"
              clearable
            />
            <el-select
              v-model="editForm.memberId"
              placeholder="选择会员"
              filterable
              style="width: 100%; margin-top: 8px"
              @change="handleMemberChange"
            >
              <el-option v-for="item in memberOptions" :key="item.id" :label="item.memberName" :value="item.id">
                <span>{{ item.memberName }}</span>
                <span style="margin-left: 8px; color: var(--color-text-secondary, #64748B)">{{ item.phone }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="球拍品牌" prop="racketBrand" class="modern-form-item">
            <el-input v-model="editForm.racketBrand" placeholder="请输入球拍品牌" />
          </el-form-item>
          <el-form-item label="球拍型号" prop="racketModel" class="modern-form-item">
            <el-input v-model="editForm.racketModel" placeholder="请输入球拍型号" />
          </el-form-item>
          <el-form-item label="球拍描述" class="modern-form-item">
            <el-input v-model="editForm.racketDescription" type="textarea" :rows="3" placeholder="可选" />
          </el-form-item>
          <el-form-item label="是否自带线材" class="modern-form-item">
            <el-switch v-model="editForm.isOwnString" :active-value="1" :inactive-value="0" @change="handleOwnStringChange" />
          </el-form-item>
          <el-form-item v-if="!editForm.isOwnString" label="选择线材" class="modern-form-item">
            <el-select v-model="editForm.stringId" placeholder="选择线材" filterable style="width: 100%" @change="handleStringIdChange">
              <el-option
                v-for="item in stringOptions"
                :key="item.id"
                :label="`${item.equipmentName} (¥${formatCurrency(item.price)})`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item v-if="editForm.isOwnString" label="线材名称" class="modern-form-item">
            <el-input v-model="editForm.stringName" placeholder="请输入线材名称" />
          </el-form-item>
          <el-form-item label="磅数" prop="pound" class="modern-form-item">
            <el-input-number
              v-model="editForm.pound"
              :min="0"
              :max="50"
              :precision="1"
              :step="0.5"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="穿线法" prop="stringingMethod" class="modern-form-item">
            <el-radio-group v-model="editForm.stringingMethod">
              <el-radio label="TWO_SECTION">两节</el-radio>
              <el-radio label="FOUR_SECTION">四节</el-radio>
              <el-radio label="AUTO">视球拍而定</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="是否存在断裂" class="modern-form-item">
            <el-switch v-model="editForm.hasBreakage" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="是否存在塌陷" class="modern-form-item">
            <el-switch v-model="editForm.hasCollapse" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="状态" prop="status" class="modern-form-item">
            <el-select v-model="editForm.status" placeholder="选择状态">
              <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="服务价格" prop="servicePrice" class="modern-form-item">
            <el-input-number v-model="editForm.servicePrice" :min="0" :precision="2" :step="10" style="width: 100%" />
          </el-form-item>
          <el-form-item label="支付方式" class="modern-form-item">
            <!-- 创建和编辑时仅展示，实际选择仍通过支付弹窗处理 -->
            <el-select v-model="editForm.paymentMethod" placeholder="创建后在支付弹窗中选择" disabled>
              <el-option label="余额" value="BALANCE" />
              <el-option label="现金" value="CASH" />
              <el-option label="微信" value="WECHAT" />
              <el-option label="支付宝" value="ALIPAY" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" class="modern-form-item">
            <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="可选" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer-enhanced modern-dialog-footer">
          <el-button class="modern-btn-cancel" @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" class="modern-btn-submit" :loading="submitLoading" @click="handleEditSubmit">
            {{ isEdit ? '更新服务' : '添加穿线' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 支付弹窗（与预约管理/器材租借逻辑一致） -->
    <el-dialog v-model="payDialogVisible" title="穿线服务支付" width="420px">
      <el-form label-width="100px">
        <el-form-item label="服务单号">
          <el-tag type="info">{{ currentPay?.serviceNo || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input-number v-model="payForm.amount" :min="0" :precision="2" :step="10" disabled />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="payForm.method" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in paymentMethodOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="submitPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete, Tools } from '@element-plus/icons-vue'
import { useAuth } from '@/composables/useAuth'
import {
  getStringingList,
  getStringingInfo,
  addStringingService,
  updateStringingService,
  deleteStringingService,
  updateStringingStatus,
  getStringingStatistics,
  getStringOptions,
  calculateStringingPrice,
  processStringingPayment,
  processStringingRefund
} from '@/api/stringing'
import { getVenueList } from '@/api/venue'
import { getEquipmentRentalMembers } from '@/api/equipmentRental'

const { isAdmin, isVenueManager, isPresident } = useAuth()

const searchForm = reactive({
  serviceNo: '',
  memberKeyword: '',
  venueId: null,
  status: null,
  timeRange: []
})

const serviceList = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })
const statistics = reactive({ total: 0, waiting: 0, inProgress: 0, completed: 0, cancelled: 0 })
const venueOptions = ref([])
const stringOptions = ref([])
const tableHeight = ref(600)

const detailDialogVisible = ref(false)
const editDialogVisible = ref(false)
const editDialogTitle = ref('编辑服务')
const isEdit = ref(true)
const memberKeyword = ref('')
const memberOptions = ref([])
const currentService = ref(null)
const editFormRef = ref(null)
const editForm = reactive({
  id: null,
  serviceNo: '',
  venueId: null,
  memberId: null,
  racketBrand: '',
  racketModel: '',
  racketDescription: '',
  stringId: null,
  stringName: '',
  isOwnString: 0,
  pound: null,
  stringingMethod: '',
  hasBreakage: 0,
  hasCollapse: 0,
  status: 1,
  servicePrice: 0,
  paymentMethod: null,
  remark: ''
})
const editFormRules = {
  venueId: [{ required: true, message: '请选择场馆', trigger: 'change' }],
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  racketBrand: [{ required: true, message: '请输入球拍品牌', trigger: 'blur' }],
  racketModel: [{ required: true, message: '请输入球拍型号', trigger: 'blur' }],
  pound: [{ required: true, message: '请输入磅数', trigger: 'blur' }],
  stringingMethod: [{ required: true, message: '请选择穿线法', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  servicePrice: [{ validator: (rule, value, cb) => { if (isEdit.value && (value == null || value === '')) cb(new Error('请输入服务价格')); else cb(); }, trigger: 'blur' }]
}
const submitLoading = ref(false)

// 支付弹窗（与器材租借/预约管理一致）
const payDialogVisible = ref(false)
const payLoading = ref(false)
const currentPay = ref(null)
const payForm = reactive({ amount: 0, method: 'BALANCE' })
const paymentMethodOptions = [
  { label: '余额', value: 'BALANCE' },
  { label: '现金', value: 'CASH' },
  { label: '微信', value: 'WECHAT' },
  { label: '支付宝', value: 'ALIPAY' }
]

const statusOptions = [
  { label: '已取消', value: 0 },
  { label: '等待穿线', value: 1 },
  { label: '正在穿线', value: 2 },
  { label: '已完成', value: 3 }
]

const tableHeaderStyle = {
  background: 'var(--color-card-bg-hover, #EFF6FF)',
  color: 'var(--color-text-primary, #1E293B)',
  borderBottom: '1px solid var(--color-border, #E2E8F0)',
  fontWeight: '600',
  textAlign: 'center'
}

const currentVenueId = computed(() => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (userInfoStr && userInfoStr !== 'null') {
      const userInfo = JSON.parse(userInfoStr)
      return userInfo?.venueId ?? null
    }
  } catch (e) {
    console.error('获取当前用户场馆ID失败:', e)
  }
  return null
})

const getStatusText = (status) => {
  const map = { 0: '已取消', 1: '等待穿线', 2: '正在穿线', 3: '已完成' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }
  return map[status] || 'info'
}

const getStringingMethodText = (method) => {
  const map = { 'TWO_SECTION': '两节', 'FOUR_SECTION': '四节', 'AUTO': '视球拍而定' }
  return map[method] || method
}

const getPaymentMethodText = (method) => {
  const map = { 'CASH': '现金', 'ALIPAY': '支付宝', 'WECHAT': '微信', 'BALANCE': '余额' }
  return map[method] || method
}

const getPaymentStatusText = (status) => {
  const map = { 0: '未支付', 1: '已支付', 2: '已退款' }
  return map[status] || '未知'
}

const getPaymentStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || 'info'
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
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const mi = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${mi}:${s}`
}

const loadVenueOptions = async () => {
  try {
    const res = await getVenueList({ page: 1, size: 1000 })
    if (res.code === 200) {
      venueOptions.value = res.data?.data || res.data || []
      // 如果是场馆管理者，自动填充自己场馆
      if (isVenueManager.value && currentVenueId.value) {
        searchForm.venueId = currentVenueId.value
      }
    }
  } catch (e) {
    console.error('加载场馆列表失败:', e)
  }
}

const loadStringOptions = async () => {
  try {
    const res = await getStringOptions()
    if (res.code === 200) {
      stringOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载线材列表失败:', e)
  }
}

/** 根据「是否自带线材」与「选择线材」刷新服务价格（自带=20，不自带=线材价格） */
const refreshServicePriceFromRule = async () => {
  if (editForm.isOwnString === 1) {
    editForm.servicePrice = 20
    return
  }
  if (editForm.stringId) {
    try {
      const res = await calculateStringingPrice(editForm.stringId, 0)
      if (res.code === 200 && res.data?.price != null) {
        editForm.servicePrice = Number(res.data.price)
      } else {
        editForm.servicePrice = 0
      }
    } catch (e) {
      console.error('计算服务价格失败:', e)
      editForm.servicePrice = 0
    }
  } else {
    editForm.servicePrice = 0
  }
}

const handleOwnStringChange = () => {
  if (editForm.isOwnString === 1) {
    editForm.servicePrice = 20
  } else {
    refreshServicePriceFromRule()
  }
}

const handleStringIdChange = () => {
  if (editForm.isOwnString === 0) {
    refreshServicePriceFromRule()
  }
}

// 与器材租借一致：按关键字搜索会员，结果填入下拉（使用同一会员接口）
const loadMembers = async () => {
  if (!memberKeyword.value) {
    memberOptions.value = []
    return
  }
  try {
    const res = await getEquipmentRentalMembers(memberKeyword.value)
    if (res.code === 200) {
      memberOptions.value = res.data || []
    }
  } catch (e) {
    console.error('加载会员列表失败:', e)
  }
}

// 与器材租借一致：选择会员后同步关键字为当前会员姓名
const handleMemberChange = (memberId) => {
  const target = memberOptions.value.find((m) => m.id === memberId)
  if (target) {
    memberKeyword.value = target.memberName
  }
}

const loadStatistics = async () => {
  try {
    const res = await getStringingStatistics()
    if (res.code === 200) {
      Object.assign(statistics, res.data || {})
    }
  } catch (e) {
    console.error('加载统计失败:', e)
  }
}

const loadList = async () => {
  loading.value = true
  try {
    // 合并服务单号和会员关键词为keyword参数（后端支持匹配服务单号、会员姓名、会员手机号、场馆名称）
    const keyword = searchForm.serviceNo || searchForm.memberKeyword || null
    const params = {
      keyword: keyword,
      status: searchForm.status,
      venueId: searchForm.venueId || undefined,
      page: pagination.page,
      size: pagination.size
    }
    // 时间范围：传 createTimeStart / createTimeEnd
    if (searchForm.timeRange && Array.isArray(searchForm.timeRange) && searchForm.timeRange.length === 2) {
      params.createTimeStart = searchForm.timeRange[0]
      params.createTimeEnd = searchForm.timeRange[1]
    }
    const res = await getStringingList(params)
    if (res.code === 200) {
      serviceList.value = res.data?.data || []
      pagination.total = res.data?.total || 0
    } else {
      ElMessage.error(res.message || '获取服务列表失败')
      serviceList.value = []
    }
  } catch (e) {
    console.error('加载服务列表失败:', e)
    ElMessage.error('加载服务列表失败，请稍后重试')
    serviceList.value = []
  } finally {
    loading.value = false
  }
}

const handleStatClick = (status) => {
  searchForm.status = status
  pagination.page = 1
  loadList()
}

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  Object.assign(searchForm, {
    serviceNo: '',
    memberKeyword: '',
    venueId: isVenueManager.value && currentVenueId.value ? currentVenueId.value : null,
    status: null,
    timeRange: []
  })
  pagination.page = 1
  loadList()
}

const resetForm = () => {
  Object.assign(editForm, {
    id: null,
    serviceNo: '',
    venueId: isVenueManager.value && currentVenueId.value ? currentVenueId.value : null,
    memberId: null,
    racketBrand: '',
    racketModel: '',
    racketDescription: '',
    stringId: null,
    stringName: '',
    isOwnString: 0,
    pound: null,
    stringingMethod: '',
    hasBreakage: 0,
    hasCollapse: 0,
    status: 1,
    servicePrice: 0,
    paymentMethod: null,
    remark: ''
  })
  memberKeyword.value = ''
  memberOptions.value = []
  if (editFormRef.value) editFormRef.value.resetFields()
}

const handleAdd = () => {
  editDialogTitle.value = '添加穿线服务'
  isEdit.value = false
  resetForm()
  editDialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const res = await getStringingInfo(row.id)
    if (res.code === 200) {
      currentService.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取服务详情失败')
    }
  } catch (e) {
    console.error('获取服务详情失败:', e)
    ElMessage.error('获取服务详情失败，请稍后重试')
  }
}

// 打开支付弹窗（与预约管理/器材租借一致）
const openPay = (row) => {
  currentPay.value = row
  payForm.amount = Number(row.servicePrice) || 0
  payForm.method = 'BALANCE'
  payDialogVisible.value = true
}

const submitPay = async () => {
  if (!currentPay.value) return
  payLoading.value = true
  try {
    const res = await processStringingPayment(currentPay.value.id, payForm.method)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (e) {
    console.error('支付失败:', e)
    ElMessage.error(e.response?.data?.message || e.message || '支付失败，请稍后重试')
  } finally {
    payLoading.value = false
  }
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm(`确定对穿线服务 ${row.serviceNo} 进行退款吗？`, '退款确认', {
      type: 'warning',
      confirmButtonText: '确认退款',
      cancelButtonText: '取消'
    })
    const res = await processStringingRefund(row.id)
    if (res.code === 200) {
      ElMessage.success('退款成功')
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '退款失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('退款失败:', e)
      ElMessage.error(e.response?.data?.message || e.message || '退款失败')
    }
  }
}

const handleStartStringing = async (row) => {
  try {
    await ElMessageBox.confirm('确定要开始穿线吗？', '提示', { type: 'warning' })
    const res = await updateStringingStatus(row.id, 2)
    if (res.code === 200) {
      ElMessage.success('状态已更新为"正在穿线"')
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '更新状态失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('更新状态失败:', e)
      // 提取错误信息：优先使用 response.data.message，其次使用 e.message
      const errorMessage = e.response?.data?.message || e.message || '更新状态失败，请稍后重试'
      ElMessage.error(errorMessage)
    }
  }
}

const handleCompleteStringing = async (row) => {
  try {
    await ElMessageBox.confirm('确定要完成穿线吗？', '提示', { type: 'warning' })
    const res = await updateStringingStatus(row.id, 3)
    if (res.code === 200) {
      ElMessage.success('状态已更新为"已完成"')
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '更新状态失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('更新状态失败:', e)
      // 提取错误信息：优先使用 response.data.message，其次使用 e.message
      const errorMessage = e.response?.data?.message || e.message || '更新状态失败，请稍后重试'
      ElMessage.error(errorMessage)
    }
  }
}

const handleCancelService = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消这个服务吗？', '提示', { type: 'warning' })
    const res = await updateStringingStatus(row.id, 0)
    if (res.code === 200) {
      ElMessage.success('服务已取消')
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('取消失败:', e)
      ElMessage.error('取消失败，请稍后重试')
    }
  }
}

const handleEdit = async (row) => {
  editDialogTitle.value = '编辑服务'
  isEdit.value = true
  try {
    const res = await getStringingInfo(row.id)
    if (res.code === 200) {
      const data = res.data || {}
      Object.assign(editForm, {
        id: data.id,
        serviceNo: data.serviceNo,
        venueId: data.venueId,
        memberId: data.memberId || null,
        racketBrand: data.racketBrand,
        racketModel: data.racketModel,
        racketDescription: data.racketDescription || '',
        stringId: data.stringId,
        stringName: data.stringName || '',
        isOwnString: data.isOwnString || 0,
        pound: data.pound,
        stringingMethod: data.stringingMethod,
        hasBreakage: data.hasBreakage || 0,
        hasCollapse: data.hasCollapse || 0,
        status: data.status,
        servicePrice: data.servicePrice,
        paymentMethod: data.paymentMethod || null,
        remark: data.remark || ''
      })
      // 与预约管理一致：确保下拉中立即有当前会员选项，避免只显示数字 ID 或空白
      if (data.memberId && data.memberName) {
        memberOptions.value = [
          {
            id: data.memberId,
            memberName: data.memberName,
            phone: data.memberPhone || data.phone || ''
          }
        ]
        memberKeyword.value = data.memberName
      } else {
        memberKeyword.value = ''
        memberOptions.value = []
      }
      editDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取服务详情失败')
    }
  } catch (e) {
    console.error('加载详情失败:', e)
    ElMessage.error('加载详情失败，请稍后重试')
  }
}

const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      let res
      if (isEdit.value) {
        res = await updateStringingService(editForm)
      } else {
        const payload = { ...editForm }
        delete payload.id
        delete payload.serviceNo
        if (payload.servicePrice == null || payload.servicePrice === 0) delete payload.servicePrice
        res = await addStringingService(payload)
      }
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        editDialogVisible.value = false
        loadList()
        loadStatistics()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (e) {
      console.error(isEdit.value ? '更新失败' : '添加失败', e)
      ElMessage.error(isEdit.value ? '更新失败，请稍后重试' : '添加失败，请稍后重试')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除服务 ${row.serviceNo} 吗？`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        const res = await deleteStringingService(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadList()
          loadStatistics()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (e) {
        console.error('删除失败:', e)
        ElMessage.error('删除失败，请稍后重试')
      }
    })
    .catch(() => {})
}

const handleEditDialogClose = () => {
  resetForm()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadList()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadList()
}

const calculateTableHeight = () => {
  tableHeight.value = Math.max(420, window.innerHeight - 430)
}

onMounted(() => {
  calculateTableHeight()
  window.addEventListener('resize', calculateTableHeight)
  loadVenueOptions()
  loadStringOptions()
  loadStatistics()
  loadList()
})

onUnmounted(() => {
  window.removeEventListener('resize', calculateTableHeight)
})
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

.page-bg {
  position: absolute;
  inset: 0;
  background: var(--color-background, #F8FAFC);
  z-index: 0;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
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
  margin-bottom: 20px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
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
  gap: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary-light, #3B82F6) 0%, var(--color-primary, #2563EB) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.page-title {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 600;
}

.page-subtitle {
  margin: 0;
  color: var(--color-text-secondary, #64748B);
}

.header-stats {
  display: flex;
  gap: 10px;
  align-items: center;
}

.stat-item {
  width: 78px;
  min-width: 78px;
  max-width: 78px;
  padding: 12px 8px;
  border-radius: 14px;
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-item:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18), 0 4px 12px rgba(37, 99, 235, 0.18);
}

.stat-item-primary {
  background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%) !important;
}

.stat-item-success {
  background: linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%) !important;
}

.stat-item-warning {
  background: linear-gradient(135deg, #fef3c7 0%, #fffbeb 100%) !important;
}

.stat-item-danger {
  background: linear-gradient(135deg, #fee2e2 0%, #fef2f2 100%) !important;
}

.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary, #64748B);
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-primary, #2563EB);
}

.search-card {
  padding: 20px 24px;
}

.search-header {
  margin-bottom: 12px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
  padding-bottom: 10px;
}

.search-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.search-container {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-end;
}

.search-fields {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  align-items: flex-end;
}

.search-item {
  margin-bottom: 0 !important;
}

.search-input {
  width: 200px;
}

.search-select {
  width: 160px;
}

.search-select-small {
  width: 130px;
}

.search-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.action-card {
  padding: 14px 20px;
}

.table-card {
  padding: 20px;
}

.price-text {
  color: var(--color-danger, #ef4444);
  font-weight: 600;
}

.operation-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  padding-top: 12px;
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

.detail-content {
  padding: 0;
}

.detail-section {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--color-border, #E2E8F0);
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: var(--color-text-primary, #1E293B);
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.detail-label {
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  min-width: 100px;
}

.detail-value {
  font-size: 14px;
  color: var(--color-text-primary, #1E293B);
  font-weight: 500;
}

.detail-value.price {
  font-size: 18px;
  color: var(--color-primary, #2563EB);
  font-weight: 700;
}

.modern-form-section {
  margin-bottom: 18px;
  padding: 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--color-card-bg, #ffffff) 0%, var(--color-background, #f8fafc) 100%);
  border: 1px solid var(--color-border, rgba(226, 232, 240, 0.8));
}

.modern-section-title {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 700;
}

.modern-form-item {
  margin-bottom: 14px !important;
}

.dialog-footer-enhanced {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1024px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  .search-container {
    flex-direction: column;
    align-items: stretch;
  }
  .search-input,
  .search-select,
  .search-select-small {
    width: 100%;
  }
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

.operation-buttons :deep(.el-button--warning:hover) {
  box-shadow: 0 6px 16px rgba(245, 158, 11, 0.28);
}

.operation-buttons :deep(.el-button--danger:hover) {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.28);
}
</style>
