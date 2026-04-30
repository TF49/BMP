<template>
  <el-drawer
    :model-value="modelValue"
    title="学员信息"
    size="520px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div class="member-history-body">
      <div v-loading="memberLoading" class="member-profile-card">
        <template v-if="member">
          <div class="member-header">
            <div>
              <div class="member-name">{{ member.memberName || '-' }}</div>
              <div class="member-meta">
                <span>{{ member.phone || '未登记手机号' }}</span>
                <span>{{ memberTypeText }}</span>
                <span>{{ memberStatusText }}</span>
              </div>
            </div>
          </div>
        </template>
        <div v-else-if="memberLoadFailed" class="error-state">
          <div class="error-title">学员信息加载失败</div>
          <div class="error-description">{{ memberErrorMessage }}</div>
          <el-button type="primary" @click="emit('reloadMember')">重试</el-button>
        </div>
      </div>

      <div class="history-section">
        <div class="section-header">
          <div>
            <h3 class="section-title">历史记录</h3>
            <p class="section-subtitle">只读查看该学员的消费与业务记录</p>
          </div>
        </div>

        <div v-if="historyLoadFailed" class="error-state">
          <div class="error-title">历史记录加载失败</div>
          <div class="error-description">{{ historyErrorMessage }}</div>
          <el-button type="primary" @click="emit('reloadHistory')">重试</el-button>
        </div>

        <template v-else-if="history.length">
          <div class="history-list">
            <article v-for="record in history" :key="record.id" class="history-card">
              <div class="history-title-row">
                <div class="history-title">{{ record.description || '业务记录' }}</div>
                <el-tag size="small" :type="record.amount >= 0 ? 'success' : 'info'">
                  {{ record.amount >= 0 ? '支出' : '退款' }}
                </el-tag>
              </div>
              <div class="history-meta">
                <span>金额：{{ amountLabel(record.amount) }}</span>
                <span>支付：{{ paymentStatusText(record.paymentStatus) }}</span>
                <span>{{ formatDateTime(record.createTime) }}</span>
              </div>
              <div class="history-remark">{{ record.remark || '无备注' }}</div>
            </article>
          </div>
        </template>
        <el-empty v-else-if="historyLoading" description="正在加载历史记录" />
        <el-empty v-else description="暂无历史记录" />

        <div v-if="historyTotal > 0" class="pagination-wrap">
          <el-pagination
            :current-page="historyPage"
            :page-size="historySize"
            :total="historyTotal"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @current-change="emit('historyPageChange', $event)"
            @size-change="emit('historySizeChange', $event)"
          />
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'
import {
  MEMBER_STATUS_TEXT_MAP,
  MEMBER_TYPE_TEXT_MAP,
  formatDateTime,
  formatStatusText
} from '../coachViewUtils'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  memberLoading: { type: Boolean, default: false },
  memberLoadFailed: { type: Boolean, default: false },
  memberErrorMessage: { type: String, default: '请稍后重试' },
  member: { type: Object, default: null },
  historyLoading: { type: Boolean, default: false },
  historyLoadFailed: { type: Boolean, default: false },
  historyErrorMessage: { type: String, default: '请稍后重试' },
  history: { type: Array, default: () => [] },
  historyPage: { type: Number, default: 1 },
  historySize: { type: Number, default: 10 },
  historyTotal: { type: Number, default: 0 }
})

const emit = defineEmits([
  'update:modelValue',
  'reloadMember',
  'reloadHistory',
  'historyPageChange',
  'historySizeChange'
])

const memberTypeText = computed(() => {
  return formatStatusText(props.member?.memberType, MEMBER_TYPE_TEXT_MAP)
})

const memberStatusText = computed(() => {
  return formatStatusText(props.member?.status, MEMBER_STATUS_TEXT_MAP)
})

const amountLabel = (value) => {
  const amount = Number(value ?? 0)
  return `${amount < 0 ? '-' : ''}¥${Math.abs(amount).toFixed(2)}`
}

const paymentStatusText = (status) => {
  const map = { 0: '未支付', 1: '已支付', 2: '已退款' }
  return map[status] ?? '未知'
}
</script>

<style scoped>
.member-history-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.member-profile-card {
  min-height: 96px;
}

.member-header {
  padding: 18px;
  border-radius: 16px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-light);
}

.member-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.member-meta {
  margin-top: 8px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.history-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.section-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-card {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-blank, #fff);
}

.history-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.history-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.history-meta {
  margin-top: 8px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.history-remark {
  margin-top: 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.pagination-wrap {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.error-state {
  padding: 24px;
  border-radius: 16px;
  background: #fff7f7;
  border: 1px solid #f5c2c7;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-start;
}

.error-title {
  font-size: 16px;
  font-weight: 600;
  color: #c2410c;
}

.error-description {
  font-size: 14px;
  color: #7c2d12;
}

@media (max-width: 768px) {
  .history-title-row,
  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
