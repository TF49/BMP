<template>
  <div class="coach-bookings">
    <div class="page-hero">
      <div class="hero-copy">
        <h2 class="page-title">预约明细</h2>
        <p class="page-subtitle">查看课程预约、签到到课、登记缺席或取消</p>
      </div>
      <div class="hero-actions">
        <div class="hero-tip">
          <span class="hero-tip-label">教练权限</span>
          <span class="hero-tip-value">可签到 / 完成 / 取消</span>
        </div>
      </div>
    </div>

    <div class="overview-grid">
      <div class="overview-card">
        <div class="overview-label">待签到</div>
        <div class="overview-value">{{ bookingSummary.ready }}</div>
        <div class="overview-desc">已支付待处理的预约</div>
      </div>
      <div class="overview-card">
        <div class="overview-label">进行中</div>
        <div class="overview-value">{{ bookingSummary.ongoing }}</div>
        <div class="overview-desc">正在上课的预约</div>
      </div>
      <div class="overview-card">
        <div class="overview-label">已完成</div>
        <div class="overview-value">{{ bookingSummary.finished }}</div>
        <div class="overview-desc">已完成的课节记录</div>
      </div>
    </div>

    <div class="toolbar-card">
      <div class="toolbar-header">
        <div>
          <div class="toolbar-title">工作筛选</div>
          <div class="toolbar-subtitle">先按课程、会员或状态聚焦，再处理签到与异常</div>
        </div>
      </div>
      <div class="toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="预约单号 / 课程名 / 会员名"
          clearable
          class="toolbar-input"
          @keyup.enter="applySearch"
          @clear="applySearch"
        />
        <el-select v-model="searchStatus" placeholder="全部状态" clearable class="toolbar-select" @change="applySearch">
          <el-option label="待支付" :value="1" />
          <el-option label="已支付" :value="2" />
          <el-option label="进行中" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="0" />
        </el-select>
        <el-button type="primary" @click="applySearch">查询</el-button>
      </div>
    </div>

    <div v-if="hasVisibleFilters" class="filter-banner">
      <div class="filter-banner-text">
        <span v-if="activeCourseFilterId">
          当前仅显示课程
          <span class="filter-banner-name">{{ activeCourseFilterName || `#${activeCourseFilterId}` }}</span>
          的预约记录
        </span>
        <span v-if="searchStatus !== null">
          <template v-if="activeCourseFilterId">，</template>
          当前聚焦状态
          <span class="filter-banner-name">{{ formatStatusText(searchStatus, BOOKING_STATUS_TEXT_MAP) }}</span>
        </span>
      </div>
      <el-button link type="primary" @click="clearRouteFilters">清除筛选</el-button>
    </div>

    <div v-if="loadFailed" class="error-state">
      <div class="error-title">预约数据加载失败</div>
      <div class="error-description">{{ errorMessage }}</div>
      <el-button type="primary" @click="loadList">重试</el-button>
    </div>

    <template v-else>
      <div v-if="list.length" v-loading="loading" class="booking-list">
        <article v-for="row in list" :key="row.id" class="booking-card">
          <div class="booking-card-top">
            <div class="booking-main">
              <div class="booking-name-row">
                <div class="booking-member">{{ row.memberName || '-' }}</div>
                <span class="booking-divider">/</span>
                <div class="booking-course">{{ row.courseName || '-' }}</div>
              </div>
              <div class="booking-time">{{ formatCourseTime(row) }}</div>
            </div>
            <div class="booking-tag-group">
              <el-tag :type="paymentStatusType(row.paymentStatus)" size="small">
                {{ paymentStatusText(row.paymentStatus) }}
              </el-tag>
              <el-tag :type="formatStatusType(row.status, BOOKING_STATUS_TYPE_MAP)" size="small">
                {{ formatStatusText(row.status, BOOKING_STATUS_TEXT_MAP) }}
              </el-tag>
            </div>
          </div>

          <div class="booking-meta-grid">
            <div class="meta-block">
              <span class="meta-label">预约单号</span>
              <span class="meta-value">{{ row.bookingNo || '-' }}</span>
            </div>
            <div class="meta-block">
              <span class="meta-label">场地</span>
              <span class="meta-value">{{ row.courtName || '未安排场地' }}</span>
            </div>
            <div class="meta-block">
              <span class="meta-label">金额</span>
              <span class="meta-value">¥{{ formatAmount(row.orderAmount) }}</span>
            </div>
            <div class="meta-block">
              <span class="meta-label">备注</span>
              <span class="meta-value">{{ row.remark || '-' }}</span>
            </div>
          </div>

          <div class="booking-card-actions">
            <el-button link type="primary" @click="openDetail(row.id)">查看详情</el-button>
            <el-button v-if="row.memberId" link type="primary" @click="openMemberHistory(row.memberId)">学员历史</el-button>
            <el-button v-if="canStart(row)" link type="success" @click="openStatusDialog(row, 3)">签到上课</el-button>
            <el-button v-if="canComplete(row)" link type="success" @click="openStatusDialog(row, 4)">完成课程</el-button>
            <el-button v-if="canCancel(row)" link type="danger" @click="openStatusDialog(row, 0)">缺席/取消</el-button>
          </div>
        </article>
      </div>
      <div v-else-if="!loading" class="empty-wrap">
        <el-empty description="当前筛选条件下暂无预约记录" :image-size="120" />
      </div>
    </template>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="handleSizeChange"
      />
    </div>

    <el-dialog v-model="detailVisible" title="预约详情" width="760px" destroy-on-close>
      <div v-loading="detailLoading">
        <template v-if="detail">
          <el-descriptions :column="2" border class="detail-descriptions">
            <el-descriptions-item label="预约单号">{{ detail.bookingNo }}</el-descriptions-item>
            <el-descriptions-item label="会员">{{ detail.memberName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="课程">{{ detail.courseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="教练">{{ detail.coachName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="日期">{{ detail.courseDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="场地">{{ detail.courtName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="时间">{{ formatCourseTime(detail) }}</el-descriptions-item>
            <el-descriptions-item label="金额">¥{{ formatAmount(detail.orderAmount) }}</el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="paymentStatusType(detail.paymentStatus)" size="small">
                {{ paymentStatusText(detail.paymentStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="formatStatusType(detail.status, BOOKING_STATUS_TYPE_MAP)" size="small">
                {{ formatStatusText(detail.status, BOOKING_STATUS_TEXT_MAP) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(detail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.remark || '无' }}</el-descriptions-item>
          </el-descriptions>

          <div class="member-info-card">
            <div class="member-info-header">
              <div>
                <div class="member-info-title">学员信息</div>
                <div class="member-info-subtitle">查看本次预约关联学员的基础资料与历史记录</div>
              </div>
              <el-button v-if="detail.memberId" link type="primary" @click="openMemberHistory(detail.memberId)">查看历史记录</el-button>
            </div>

            <div v-loading="memberPreviewLoading">
              <template v-if="memberPreview">
                <div class="member-info-grid">
                  <div class="member-info-item">
                    <span class="member-info-label">姓名</span>
                    <span class="member-info-value">{{ memberPreview.memberName || '-' }}</span>
                  </div>
                  <div class="member-info-item">
                    <span class="member-info-label">手机号</span>
                    <span class="member-info-value">{{ memberPreview.phone || '-' }}</span>
                  </div>
                  <div class="member-info-item">
                    <span class="member-info-label">会员类型</span>
                    <span class="member-info-value">{{ memberTypeText(memberPreview.memberType) }}</span>
                  </div>
                  <div class="member-info-item">
                    <span class="member-info-label">状态</span>
                    <span class="member-info-value">{{ memberStatusText(memberPreview.status) }}</span>
                  </div>
                </div>
              </template>
              <el-alert
                v-else-if="memberPreviewLoadFailed"
                type="warning"
                :closable="false"
                show-icon
                :title="memberPreviewErrorMessage"
              />
              <el-empty v-else description="暂无学员信息" />
            </div>
          </div>

          <el-alert
            v-if="showRefundHint(detail)"
            type="warning"
            :closable="false"
            show-icon
            class="detail-hint"
            title="该预约已取消，但仍为已支付状态，请联系管理员处理退款。"
          />

          <div class="detail-actions">
            <el-button v-if="canStart(detail)" type="success" @click="openStatusDialog(detail, 3)">签到上课</el-button>
            <el-button v-if="canComplete(detail)" type="primary" @click="openStatusDialog(detail, 4)">完成课程</el-button>
            <el-button v-if="canCancel(detail)" type="danger" plain @click="openStatusDialog(detail, 0)">缺席/取消</el-button>
          </div>
        </template>
        <el-empty v-else-if="detailLoadFailed" description="预约详情加载失败" />
        <el-empty v-else description="暂无预约详情" />
      </div>
    </el-dialog>

    <el-dialog
      v-model="statusVisible"
      :title="statusDialogTitle"
      width="460px"
      destroy-on-close
      @closed="resetStatusDialog"
    >
      <div class="status-dialog-body">
        <div class="status-dialog-summary">
          <div class="summary-label">当前预约</div>
          <div class="summary-value">{{ currentActionRow?.bookingNo || '-' }}</div>
          <div class="summary-sub">{{ currentActionRow?.memberName || '-' }} · {{ currentActionRow?.courseName || '-' }}</div>
        </div>

        <el-form label-position="top">
          <el-form-item :label="statusRemarkLabel">
            <el-input
              v-model="statusRemark"
              type="textarea"
              :rows="3"
              :placeholder="statusRemarkPlaceholder"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusSubmitting" @click="submitStatusUpdate">确认</el-button>
      </template>
    </el-dialog>

    <MemberHistoryDrawer
      v-model="memberHistoryVisible"
      :member-loading="memberLoading"
      :member-load-failed="memberLoadFailed"
      :member-error-message="memberErrorMessage"
      :member="memberInfo"
      :history-loading="memberHistoryLoading"
      :history-load-failed="memberHistoryLoadFailed"
      :history-error-message="memberHistoryErrorMessage"
      :history="memberHistoryList"
      :history-page="memberHistoryPage"
      :history-size="memberHistorySize"
      :history-total="memberHistoryTotal"
      @reload-member="reloadMemberInfo"
      @reload-history="reloadMemberHistory"
      @history-page-change="handleMemberHistoryPageChange"
      @history-size-change="handleMemberHistorySizeChange"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getBookingDetailForCoach,
  getBookingsForCoach,
  updateBookingStatusForCoach
} from '@/api/courseBooking'
import { getMemberConsumeList, getMemberInfo } from '@/api/member'
import MemberHistoryDrawer from './components/MemberHistoryDrawer.vue'
import {
  BOOKING_STATUS_TEXT_MAP,
  BOOKING_STATUS_TYPE_MAP,
  COACH_UNBOUND_MESSAGE,
  MEMBER_STATUS_TEXT_MAP,
  MEMBER_TYPE_TEXT_MAP,
  formatAmount,
  formatCourseTime,
  formatDateTime,
  formatStatusText,
  formatStatusType,
  isCoachUnboundError
} from './coachViewUtils'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const searchKeyword = ref('')
const searchStatus = ref(null)
const activeCourseFilterId = ref(null)
const activeCourseFilterName = ref('')
const routeStatusFilter = ref(null)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const detailLoadFailed = ref(false)

const memberPreviewLoading = ref(false)
const memberPreview = ref(null)
const memberPreviewLoadFailed = ref(false)
const memberPreviewErrorMessage = ref('请稍后重试')

const statusVisible = ref(false)
const statusSubmitting = ref(false)
const currentActionRow = ref(null)
const targetStatus = ref(null)
const statusRemark = ref('')

const memberHistoryVisible = ref(false)
const activeMemberId = ref(null)
const memberLoading = ref(false)
const memberLoadFailed = ref(false)
const memberErrorMessage = ref('请稍后重试')
const memberInfo = ref(null)
const memberHistoryLoading = ref(false)
const memberHistoryLoadFailed = ref(false)
const memberHistoryErrorMessage = ref('请稍后重试')
const memberHistoryList = ref([])
const memberHistoryTotal = ref(0)
const memberHistoryPage = ref(1)
const memberHistorySize = ref(10)
const loadFailed = ref(false)
const errorMessage = ref('请稍后重试')

const hasVisibleFilters = computed(() => activeCourseFilterId.value != null || searchStatus.value !== null)

const bookingSummary = computed(() => {
  const data = list.value || []
  return {
    ready: data.filter((item) => Number(item.status) === 2).length,
    ongoing: data.filter((item) => Number(item.status) === 3).length,
    finished: data.filter((item) => Number(item.status) === 4).length
  }
})

const paymentStatusText = (status) => {
  const map = { 0: '未支付', 1: '已支付', 2: '已退款' }
  return map[status] ?? '未知'
}

const paymentStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] ?? 'info'
}

const memberTypeText = (value) => formatStatusText(value, MEMBER_TYPE_TEXT_MAP)
const memberStatusText = (value) => formatStatusText(value, MEMBER_STATUS_TEXT_MAP)

const canStart = (row) => Number(row?.status) === 2
const canComplete = (row) => Number(row?.status) === 3
const canCancel = (row) => {
  const status = Number(row?.status)
  return status === 2 || status === 3
}

const showRefundHint = (row) => Number(row?.status) === 0 && Number(row?.paymentStatus) === 1

const statusDialogTitle = computed(() => {
  const map = { 0: '登记缺席 / 取消预约', 3: '签到上课', 4: '完成课程' }
  return map[targetStatus.value] || '更新状态'
})

const statusRemarkLabel = computed(() => targetStatus.value === 0 ? '原因备注' : '课后备注')
const statusRemarkPlaceholder = computed(() => {
  if (targetStatus.value === 0) return '可填写缺席、取消或异常结束原因'
  if (targetStatus.value === 4) return '可填写上课情况、课后说明'
  return '可选，填写签到说明'
})

const loadList = async () => {
  loading.value = true
  loadFailed.value = false
  errorMessage.value = '请稍后重试'
  try {
    const res = await getBookingsForCoach({
      page: page.value,
      size: size.value,
      courseId: activeCourseFilterId.value ?? undefined,
      status: searchStatus.value ?? undefined,
      keyword: searchKeyword.value?.trim() || undefined
    })
    if (res?.code === 200 && res?.data) {
      list.value = res.data.data ?? []
      total.value = res.data.total ?? 0
    } else {
      throw new Error(res?.message || '未获取到预约数据')
    }
  } catch (error) {
    list.value = []
    total.value = 0
    loadFailed.value = true
    errorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    loading.value = false
  }
}

const applyRouteFilters = () => {
  const courseId = route.query.courseId
  const parsedCourseId = courseId != null && courseId !== '' ? Number(courseId) : null
  activeCourseFilterId.value = Number.isFinite(parsedCourseId) ? parsedCourseId : null
  activeCourseFilterName.value = typeof route.query.courseName === 'string' ? route.query.courseName : ''

  const queryStatus = route.query.status
  const parsedStatus = queryStatus != null && queryStatus !== '' ? Number(queryStatus) : null
  routeStatusFilter.value = Number.isInteger(parsedStatus) ? parsedStatus : null

  if (routeStatusFilter.value !== null) {
    searchStatus.value = routeStatusFilter.value
  }
}

const clearRouteFilters = async () => {
  activeCourseFilterId.value = null
  activeCourseFilterName.value = ''
  routeStatusFilter.value = null
  searchStatus.value = null
  page.value = 1
  await router.replace({
    path: route.path,
    query: {
      ...route.query,
      courseId: undefined,
      courseName: undefined,
      status: undefined
    }
  })
}

const applySearch = () => {
  if (routeStatusFilter.value !== null && searchStatus.value !== routeStatusFilter.value) {
    routeStatusFilter.value = null
  }
  page.value = 1
  loadList()
}

const handleSizeChange = () => {
  page.value = 1
  loadList()
}

const loadMemberPreview = async (memberId) => {
  if (!memberId) {
    memberPreview.value = null
    return
  }
  memberPreviewLoading.value = true
  memberPreviewLoadFailed.value = false
  memberPreviewErrorMessage.value = '请稍后重试'
  try {
    const res = await getMemberInfo(memberId)
    if (res?.code === 200) {
      memberPreview.value = res.data
      return
    }
    throw new Error(res?.message || '学员信息获取失败')
  } catch (error) {
    memberPreview.value = null
    memberPreviewLoadFailed.value = true
    memberPreviewErrorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    memberPreviewLoading.value = false
  }
}

const openDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  detailLoadFailed.value = false
  memberPreview.value = null
  memberPreviewLoadFailed.value = false
  try {
    const res = await getBookingDetailForCoach(id)
    if (res?.code === 200) {
      detail.value = res.data
      await loadMemberPreview(res.data?.memberId)
    } else {
      detailLoadFailed.value = true
    }
  } catch (error) {
    detailLoadFailed.value = true
    if (isCoachUnboundError(error)) {
      memberPreviewLoadFailed.value = true
      memberPreviewErrorMessage.value = COACH_UNBOUND_MESSAGE
    }
  } finally {
    detailLoading.value = false
  }
}

const openStatusDialog = (row, status) => {
  currentActionRow.value = row
  targetStatus.value = status
  statusRemark.value = row?.remark || ''
  statusVisible.value = true
}

const resetStatusDialog = () => {
  currentActionRow.value = null
  targetStatus.value = null
  statusRemark.value = ''
}

const syncRowAfterStatusUpdate = async (id) => {
  await loadList()
  if (detailVisible.value && detail.value?.id === id) {
    await openDetail(id)
  }
}

const submitStatusUpdate = async () => {
  if (!currentActionRow.value?.id || targetStatus.value == null) return
  statusSubmitting.value = true
  try {
    const res = await updateBookingStatusForCoach({
      id: currentActionRow.value.id,
      status: targetStatus.value,
      remark: statusRemark.value?.trim() || undefined
    })
    if (res?.code === 200) {
      const successMessage = targetStatus.value === 0
        ? '已登记缺席/取消；若订单已支付，请联系管理员处理退款'
        : '状态已更新'
      ElMessage.success(successMessage)
      statusVisible.value = false
      await syncRowAfterStatusUpdate(currentActionRow.value.id)
    } else {
      ElMessage.error(res?.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error(isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '更新失败'))
  } finally {
    statusSubmitting.value = false
  }
}

const loadMemberInfo = async () => {
  if (!activeMemberId.value) return
  memberLoading.value = true
  memberLoadFailed.value = false
  memberErrorMessage.value = '请稍后重试'
  try {
    const res = await getMemberInfo(activeMemberId.value)
    if (res?.code === 200) {
      memberInfo.value = res.data
      return
    }
    throw new Error(res?.message || '学员信息获取失败')
  } catch (error) {
    memberInfo.value = null
    memberLoadFailed.value = true
    memberErrorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    memberLoading.value = false
  }
}

const loadMemberHistory = async () => {
  if (!activeMemberId.value) return
  memberHistoryLoading.value = true
  memberHistoryLoadFailed.value = false
  memberHistoryErrorMessage.value = '请稍后重试'
  try {
    const res = await getMemberConsumeList(activeMemberId.value, {
      page: memberHistoryPage.value,
      size: memberHistorySize.value
    })
    if (res?.code === 200 && res?.data) {
      memberHistoryList.value = res.data.data ?? []
      memberHistoryTotal.value = res.data.total ?? 0
      return
    }
    throw new Error(res?.message || '历史记录获取失败')
  } catch (error) {
    memberHistoryList.value = []
    memberHistoryTotal.value = 0
    memberHistoryLoadFailed.value = true
    memberHistoryErrorMessage.value = isCoachUnboundError(error) ? COACH_UNBOUND_MESSAGE : (error?.message || '请稍后重试')
  } finally {
    memberHistoryLoading.value = false
  }
}

const openMemberHistory = async (memberId) => {
  if (!memberId) {
    ElMessage.error('学员ID缺失，无法查看历史记录')
    return
  }
  activeMemberId.value = memberId
  memberHistoryVisible.value = true
  memberHistoryPage.value = 1
  await Promise.all([loadMemberInfo(), loadMemberHistory()])
}

const reloadMemberInfo = async () => {
  await loadMemberInfo()
}

const reloadMemberHistory = async () => {
  await loadMemberHistory()
}

const handleMemberHistoryPageChange = (nextPage) => {
  memberHistoryPage.value = nextPage
  loadMemberHistory()
}

const handleMemberHistorySizeChange = (nextSize) => {
  memberHistorySize.value = nextSize
  memberHistoryPage.value = 1
  loadMemberHistory()
}

watch(
  () => [route.query.courseId, route.query.courseName, route.query.status],
  () => {
    applyRouteFilters()
    page.value = 1
    loadList()
  }
)

onMounted(() => {
  applyRouteFilters()
  loadList()
})
</script>

<style scoped>
.coach-bookings {
  max-width: 100%;
}

.page-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.hero-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.page-subtitle {
  color: var(--el-text-color-secondary);
  margin: 0;
  font-size: 14px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.overview-card {
  padding: 18px;
  border-radius: 18px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.overview-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
  border-color: color-mix(in srgb, var(--color-primary, #2563EB) 18%, var(--color-border, #e2e8f0));
}

.overview-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.overview-value {
  margin-top: 10px;
  font-size: 30px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.overview-desc {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.hero-tip {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  padding: 10px 14px;
  border-radius: 12px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
  min-width: 140px;
  transition: all 0.3s ease;
}

.hero-tip:hover {
  transform: translateY(-2px);
  border-color: var(--color-primary-light, var(--el-color-primary-light-5, #60A5FA));
  box-shadow: 0 8px 20px color-mix(in srgb, var(--color-primary, #2563EB) 10%, transparent);
}

.hero-tip-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.hero-tip-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.toolbar-card {
  padding: 16px;
  border-radius: 16px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: var(--shadow, 0 1px 3px rgba(0, 0, 0, 0.05));
  margin-bottom: 16px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.toolbar-card:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--color-primary, #2563EB) 18%, var(--color-border, #e2e8f0));
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
}

.toolbar-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.toolbar-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.filter-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 14px;
  margin-bottom: 16px;
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-5);
  transition: all 0.3s ease;
}

.filter-banner:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px color-mix(in srgb, var(--color-primary, #2563EB) 10%, transparent);
}

.filter-banner-text {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.filter-banner-name {
  font-weight: 600;
  color: var(--el-color-primary);
}

.toolbar-input {
  width: 260px;
}

.toolbar-select {
  width: 140px;
}

.booking-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
}

.booking-card {
  padding: 18px;
  border-radius: 18px;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.booking-card:hover {
  transform: translateY(-4px);
  border-color: color-mix(in srgb, var(--color-primary, #2563EB) 18%, var(--color-border, #e2e8f0));
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.10), 0 4px 12px color-mix(in srgb, var(--color-primary, #2563EB) 10%, transparent);
}

.booking-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.booking-main {
  min-width: 0;
}

.booking-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.booking-member,
.booking-course {
  font-size: 17px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.booking-divider {
  color: var(--el-text-color-secondary);
}

.booking-time {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-primary, var(--el-color-primary, #2563EB));
  font-weight: 600;
}

.booking-tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.booking-meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.meta-block {
  padding: 12px 14px;
  border-radius: 14px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
}

.meta-label {
  display: block;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.meta-value {
  display: block;
  margin-top: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.booking-card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 16px;
}

.empty-wrap {
  min-height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-card-bg, #fff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: 16px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-descriptions {
  margin-bottom: 20px;
}

.member-info-card {
  margin-bottom: 20px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-light);
  transition: all 0.3s ease;
}

.member-info-card:hover {
  transform: translateY(-2px);
  border-color: var(--color-primary-light, var(--el-color-primary-light-5, #60A5FA));
  box-shadow: 0 8px 20px color-mix(in srgb, var(--color-primary, #2563EB) 10%, transparent);
}

.member-info-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.member-info-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.member-info-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.member-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.member-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.member-info-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.member-info-value {
  font-size: 14px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.detail-hint {
  margin-bottom: 20px;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.status-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-dialog-summary {
  padding: 14px 16px;
  border-radius: 12px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
  transition: all 0.3s ease;
}

.status-dialog-summary:hover {
  transform: translateY(-2px);
  border-color: var(--color-primary-light, var(--el-color-primary-light-5, #60A5FA));
  box-shadow: 0 8px 20px color-mix(in srgb, var(--color-primary, #2563EB) 10%, transparent);
}

.summary-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 6px;
}

.summary-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.summary-sub {
  margin-top: 6px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

@media (max-width: 900px) {
  .page-hero,
  .member-info-header {
    flex-direction: column;
  }

  .overview-grid,
  .booking-meta-grid {
    grid-template-columns: 1fr;
  }

  .hero-tip {
    align-items: flex-start;
  }

  .toolbar-input,
  .toolbar-select {
    width: 100%;
  }

  .filter-banner {
    flex-direction: column;
    align-items: flex-start;
  }

  .member-info-grid {
    grid-template-columns: 1fr;
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
