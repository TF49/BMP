<template>
  <PresidentLayout :showTabBar="true">
    <view class="user-list-page">
      <PresidentNavBar title="用户管理" :showBack="false" />
      <view class="list-content">
        <!-- 搜索栏 -->
        <view class="search-bar glass-card">
          <view class="search-row">
            <input
              v-model="searchForm.username"
              class="search-input"
              placeholder="用户名"
              @confirm="onSearch"
            />
            <picker
              mode="selector"
              :range="roleOptions"
              range-key="label"
              :value="roleIndex"
              @change="onRoleChange"
            >
              <view class="picker">{{ roleOptions[roleIndex]?.label || '角色' }}</view>
            </picker>
            <view class="btn-search" @click="onSearch">搜索</view>
          </view>
        </view>

        <!-- 添加按钮 -->
        <view class="action-bar">
          <view class="btn-add glass-card" @click="goAdd">添加用户</view>
        </view>

        <!-- 列表 -->
        <view v-if="loading" class="loading-wrap">
          <uni-icons type="spinner-cycle" size="32" color="#3cc51f" class="spin"></uni-icons>
        </view>
        <view v-else-if="list.length === 0" class="empty">暂无用户</view>
        <scroll-view
          v-else
          class="list-scroll"
          scroll-y
          :lower-threshold="80"
          @scrolltolower="loadMore"
        >
          <view
            v-for="item in list"
            :key="item.id"
            class="list-item glass-card"
            @click="goDetail(item.id)"
          >
            <view class="item-row">
              <text class="item-name">{{ item.username }}</text>
              <text class="item-role" :class="item.role">{{ roleLabel(item.role) }}</text>
            </view>
            <view class="item-row">
              <text class="item-meta">状态: {{ item.status === 1 ? '启用' : '禁用' }}</text>
            </view>
          </view>
          <view v-if="hasMore && list.length > 0" class="load-more">加载更多...</view>
        </scroll-view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import PresidentNavBar from '@/components/president/PresidentNavBar.vue'
import { getUserList, type UserListItem } from '@/api/president/user'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { parsePagedList } from '@/utils/parsePagedList'

const loading = ref(false)
const list = ref<UserListItem[]>([])
const total = ref(0)
const page = ref(1)
const size = 20
const searchForm = ref({ username: '', role: '' as string })
const roleOptions = [
  { label: '全部', value: '' },
  { label: '协会会长', value: 'PRESIDENT' },
  { label: '场馆管理者', value: 'VENUE_MANAGER' },
  { label: '普通用户', value: 'USER' }
]
const roleIndex = ref(0)

const hasMore = computed(() => list.value.length < total.value)

function roleLabel(role: string) {
  const r = roleOptions.find(o => o.value === role)
  return r ? r.label : role
}

function onRoleChange(e: any) {
  const i = Number(e.detail?.value ?? 0)
  roleIndex.value = i
  searchForm.value.role = roleOptions[i]?.value ?? ''
}

async function loadList(append = false) {
  if (loading.value && !append) return
  if (!append) {
    page.value = 1
    list.value = []
  }
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size,
      username: searchForm.value.username || undefined,
      role: searchForm.value.role || undefined
    })
    const parsed = parsePagedList<UserListItem>(res)
    total.value = parsed.total
    if (append) list.value = list.value.concat(parsed.list)
    else list.value = parsed.list
    page.value += 1
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function onSearch() {
  loadList()
}

function loadMore() {
  if (hasMore.value && !loading.value) loadList(true)
}

function goDetail(id: number) {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.USER_DETAIL}?id=${id}` })
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.USER_FORM })
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.user-list-page {
  min-height: 100vh;
}
.list-content {
  padding: 24rpx;
  padding-top: 120rpx;
}
.search-bar {
  padding: 24rpx;
  margin-bottom: 24rpx;
  border-radius: 20rpx;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.search-input {
  flex: 1;
  height: 64rpx;
  padding: 0 20rpx;
  background: rgba(255,255,255,0.6);
  border-radius: 12rpx;
  font-size: 28rpx;
}
.picker {
  padding: 0 24rpx;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 28rpx;
  color: var(--color-text-secondary, #475569);
}
.btn-search {
  padding: 0 28rpx;
  height: 64rpx;
  line-height: 64rpx;
  background: linear-gradient(135deg, #3cc51f, #4ade80);
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
}
.action-bar {
  margin-bottom: 24rpx;
}
.btn-add {
  display: inline-block;
  padding: 20rpx 36rpx;
  background: var(--glass-bg);
  border-radius: 16rpx;
  color: var(--color-primary, #3cc51f);
  font-size: 28rpx;
}
.list-scroll {
  height: calc(100vh - 320rpx);
}
.list-item {
  padding: 28rpx;
  margin-bottom: 20rpx;
  border-radius: 20rpx;
}
.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}
.item-name { font-size: 32rpx; font-weight: 600; color: var(--color-text, #1E293B); }
.item-role { font-size: 24rpx; color: var(--color-text-secondary, #475569); }
.item-role.PRESIDENT { color: #3cc51f; }
.item-meta { font-size: 24rpx; color: var(--color-text-secondary, #475569); }
.loading-wrap, .empty {
  padding: 80rpx;
  text-align: center;
  color: var(--color-text-secondary, #475569);
}
.load-more { padding: 24rpx; text-align: center; font-size: 24rpx; color: #475569; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
