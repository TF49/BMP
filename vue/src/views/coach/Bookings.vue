<template>
  <div class="coach-bookings">
    <h2 class="page-title">预约明细</h2>
    <p class="page-subtitle">您所教课程的预约记录（仅查看）</p>

    <div class="toolbar">
      <el-input
        v-model="searchKeyword"
        placeholder="预约单号/课程名/会员名"
        clearable
        style="width: 200px"
        @keyup.enter="loadList"
      />
      <el-select v-model="searchStatus" placeholder="状态" clearable style="width: 120px" @change="loadList">
        <el-option label="待支付" :value="1" />
        <el-option label="已支付" :value="2" />
        <el-option label="进行中" :value="3" />
        <el-option label="已完成" :value="4" />
        <el-option label="已取消" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadList">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="bookingNo" label="预约单号" width="140" />
      <el-table-column prop="courseName" label="课程" min-width="120" />
      <el-table-column prop="memberName" label="会员" width="100" />
      <el-table-column prop="courseDate" label="日期" width="110" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ (row.orderAmount ?? 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="loadList"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBookingsForCoach } from '@/api/courseBooking'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const searchKeyword = ref('')
const searchStatus = ref(null)

const statusText = (s) => {
  const m = { 0: '已取消', 1: '待支付', 2: '已支付', 3: '进行中', 4: '已完成' }
  return m[s] ?? '未知'
}
const statusType = (s) => {
  const m = { 0: 'info', 1: 'warning', 2: 'success', 3: 'primary', 4: '' }
  return m[s] ?? 'info'
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await getBookingsForCoach({
      page: page.value,
      size: size.value,
      status: searchStatus.value ?? undefined,
      keyword: searchKeyword.value?.trim() || undefined
    })
    if (res?.code === 200 && res?.data) {
      list.value = res.data.data ?? []
      total.value = res.data.total ?? 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch (_) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => loadList())
</script>

<style scoped>
.coach-bookings { max-width: 100%; }
.page-title { font-size: 22px; font-weight: 600; margin: 0 0 4px 0; }
.page-subtitle { color: var(--el-text-color-secondary); margin: 0 0 16px 0; font-size: 14px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
