<template>
  <PresidentLayout :showTabBar="false">
    <view class="recon-content">
      <view class="status-bar-placeholder" />
      <view class="recon-nav" @click="onBack">
        <uni-icons type="arrow-left" size="24" color="#ff6600" />
        <text class="recon-nav-title">财务对账</text>
      </view>
      <view class="tip glass-card">
        <text>对账将汇总各业务模块财务数据，仅协会会长可操作。</text>
      </view>
      <view class="btn-wrap">
        <view class="btn-run" :class="{ loading: running }" @click="runRecon">
          {{ running ? '对账中...' : '执行全面对账' }}
        </view>
      </view>
      <view v-if="result" class="result-card glass-card">
        <text class="result-title">对账结果</text>
        <text class="result-text">{{ JSON.stringify(result, null, 2) }}</text>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { reconciliation } from '@/api/president/finance'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.FINANCE_LIST)
}

const running = ref(false)
const result = ref<Record<string, unknown> | null>(null)

async function runRecon() {
  if (running.value) return
  running.value = true
  result.value = null
  try {
    const res = await reconciliation()
    result.value = (res as Record<string, unknown>) || null
    uni.showToast({ title: '对账完成', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e?.message || '对账失败', icon: 'none' })
  } finally {
    running.value = false
  }
}
</script>

<style lang="scss" scoped>
.recon-content { padding: 24rpx; }
.status-bar-placeholder { height: var(--status-bar-height); }
.recon-nav { display: flex; align-items: center; gap: 12rpx; margin-bottom: 20rpx; }
.recon-nav-title { font-size: 32rpx; font-weight: 800; color: #1e293b; }
.tip { padding: 24rpx; margin-bottom: 32rpx; border-radius: 20rpx; font-size: 28rpx; color: #475569; }
.btn-wrap { margin-bottom: 32rpx; }
.btn-run {
  width: 100%; padding: 28rpx; text-align: center; background: linear-gradient(135deg, #c2410c, #ea580c);
  color: #fff; border-radius: 16rpx; font-size: 30rpx;
}
.btn-run.loading { opacity: 0.8; }
.result-card { padding: 32rpx; border-radius: 24rpx; }
.result-title { display: block; font-size: 30rpx; font-weight: 600; margin-bottom: 16rpx; }
.result-text { font-size: 24rpx; color: #475569; word-break: break-all; white-space: pre-wrap; }
</style>
