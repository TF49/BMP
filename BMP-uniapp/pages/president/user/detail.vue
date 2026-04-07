<template>
  <PresidentLayout :showTabBar="false">
    <view class="detail-content">
      <view v-if="loading" class="loading-wrap">加载中...</view>
      <view v-else-if="detail" class="detail-card glass-card">
        <view class="detail-row">
          <text class="label">用户名</text>
          <text class="value">{{ detail.username }}</text>
        </view>
        <view class="detail-row">
          <text class="label">角色</text>
          <text class="value">{{ roleLabel(detail.role) }}</text>
        </view>
        <view class="detail-row">
          <text class="label">状态</text>
          <text class="value">{{ detail.status === 1 ? '启用' : '禁用' }}</text>
        </view>
        <view class="detail-row" v-if="detail.idCard">
          <text class="label">身份证号</text>
          <text class="value">{{ detail.idCard }}</text>
        </view>
        <view class="detail-row" v-if="detail.createTime">
          <text class="label">创建时间</text>
          <text class="value">{{ detail.createTime }}</text>
        </view>
        <view class="btn-row">
          <view class="btn-edit" @click="goEdit">编辑</view>
        </view>
      </view>
      <view v-else class="empty">用户不存在</view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { getUserInfo, type UserDetail } from '@/api/president/user'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const id = computed(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  return page?.options?.id ? Number(page.options.id) : 0
})
const loading = ref(true)
const detail = ref<UserDetail | null>(null)

const roleLabels: Record<string, string> = {
  PRESIDENT: '协会会长',
  VENUE_MANAGER: '场馆管理者',
  USER: '普通用户'
}
function roleLabel(role: string) {
  return roleLabels[role] || role
}

async function load() {
  if (!id.value) return
  loading.value = true
  try {
    const res = await getUserInfo(id.value)
    detail.value = (res as UserDetail) || null
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
}

function goEdit() {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.USER_FORM}?id=${id.value}` })
}

onMounted(() => {
  load()
})
</script>

<style lang="scss" scoped>
.detail-content {
  padding: 24rpx;
}
.detail-card {
  padding: 32rpx;
  border-radius: 24rpx;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid var(--color-border, #E2E8F0);
}
.detail-row:last-of-type { border-bottom: none; }
.label { font-size: 28rpx; color: var(--color-text-secondary, #475569); }
.value { font-size: 28rpx; color: var(--color-text, #1E293B); }
.btn-row { margin-top: 40rpx; }
.btn-edit {
  display: inline-block;
  padding: 24rpx 48rpx;
  background: linear-gradient(135deg, #3cc51f, #4ade80);
  color: #fff;
  border-radius: 16rpx;
  font-size: 28rpx;
}
.loading-wrap, .empty {
  padding: 80rpx;
  text-align: center;
  color: var(--color-text-secondary, #475569);
}
</style>
