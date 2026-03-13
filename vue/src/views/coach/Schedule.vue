<template>
  <div class="coach-schedule">
    <h2 class="page-title">我的课表</h2>
    <p class="page-subtitle">按日期查看您的课程安排</p>

    <div class="toolbar">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="loadList"
      />
      <el-button type="primary" @click="loadList">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="courseDate" label="日期" width="120" />
      <el-table-column prop="courseName" label="课程名称" min-width="140" />
      <el-table-column prop="courtName" label="场地" width="100" />
      <el-table-column label="时间" width="140">
        <template #default="{ row }">{{ row.startTime || '-' }} - {{ row.endTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="人数" width="90">
        <template #default="{ row }">{{ row.currentStudents ?? 0 }} / {{ row.maxStudents ?? 0 }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'info'" size="small">
            {{ statusText(row.status) }}
          </el-tag>
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
import { getMyCourses } from '@/api/course'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const dateRange = ref([])

const statusText = (s) => {
  const m = { 0: '已取消', 1: '报名中', 2: '进行中', 3: '已结束' }
  return m[s] ?? '未知'
}

const loadList = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0] + ' 00:00:00'
      params.endTime = dateRange.value[1] + ' 23:59:59'
    }
    const res = await getMyCourses(params)
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

onMounted(() => {
  // 默认本周一至本周日
  const now = new Date()
  const day = now.getDay()
  const diff = day === 0 ? -6 : 1 - day
  const weekStart = new Date(now)
  weekStart.setDate(now.getDate() + diff)
  const weekEnd = new Date(weekStart)
  weekEnd.setDate(weekStart.getDate() + 6)
  dateRange.value = [weekStart.toISOString().slice(0, 10), weekEnd.toISOString().slice(0, 10)]
  loadList()
})
</script>

<style scoped>
.coach-schedule { max-width: 100%; }
.page-title { font-size: 22px; font-weight: 600; margin: 0 0 4px 0; }
.page-subtitle { color: var(--el-text-color-secondary); margin: 0 0 16px 0; font-size: 14px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; align-items: center; }
.pagination-wrap { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
