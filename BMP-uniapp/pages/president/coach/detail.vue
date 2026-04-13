<template>
  <PresidentLayout :showTabBar="false">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">教练详情</text>
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="loading" class="state-wrap">
          <view class="spinner" />
          <text>加载教练详情中...</text>
        </view>

        <view v-else-if="errorText" class="state-wrap">
          <text>{{ errorText }}</text>
          <view class="retry-btn" @click="reloadDetail">重试</view>
        </view>

        <view v-else-if="!coach" class="state-wrap">
          <text>未找到教练信息</text>
        </view>

        <template v-else>
          <view class="hero-card">
            <image class="hero-avatar" :src="coach.avatar" mode="aspectFill" />
            <view class="hero-main">
              <view class="hero-row">
                <text class="name">{{ coach.name }}</text>
                <view class="status-pill" :class="coach.statusMeta.key">
                  {{ coach.statusMeta.label }}
                </view>
              </view>
              <text class="sub">{{ coach.venueName || '未绑定场馆' }}</text>
              <text class="sub">{{ coach.phone || '未填写联系电话' }}</text>
            </view>
          </view>

          <view class="grid">
            <view class="info-card">
              <text class="label">评分</text>
              <text class="value">{{ coach.ratingText }}</text>
            </view>
            <view class="info-card">
              <text class="label">课时费</text>
              <text class="value">¥{{ coach.priceText }}/小时</text>
            </view>
            <view class="info-card">
              <text class="label">累计学员</text>
              <text class="value">{{ coach.totalStudents }}</text>
            </view>
            <view class="info-card">
              <text class="label">性别</text>
              <text class="value">{{ coach.genderText }}</text>
            </view>
          </view>

          <view class="panel">
            <text class="panel-title">专长</text>
            <text class="panel-content">{{ coach.specialty || '未填写专长信息' }}</text>
          </view>

          <view class="panel">
            <text class="panel-title">执教经验</text>
            <text class="panel-content">{{ coach.experience || '未填写执教经验' }}</text>
          </view>

          <view class="panel">
            <text class="panel-title">身份证号</text>
            <text class="panel-content">{{ coach.idCard || '未填写身份证号' }}</text>
          </view>
        </template>

        <view class="bottom-space" />
      </scroll-view>

      <view v-if="coach" class="footer">
        <view class="footer-btn ghost" @click="goBack">返回列表</view>
        <view class="footer-btn primary" @click="goEdit">编辑教练</view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getCoachDetail, type CoachDto } from '@/api/coach'
import { getCoachStatusMeta } from '@/utils/presidentStatus'
import { resolveImageUrl } from '@/utils/resolveImageUrl'

type CoachDetailModel = {
  id: number
  name: string
  avatar: string
  venueName: string
  phone: string
  idCard: string
  specialty: string
  experience: string
  totalStudents: number
  ratingText: string
  priceText: string
  genderText: string
  statusMeta: ReturnType<typeof getCoachStatusMeta>
}

const coachId = ref(0)
const loading = ref(true)
const errorText = ref('')
const coach = ref<CoachDetailModel | null>(null)

function mapCoach(item: CoachDto): CoachDetailModel {
  return {
    id: Number(item.id || 0),
    name: item.coachName || '未命名教练',
    avatar: resolveImageUrl(item.avatar) || '/static/placeholders/avatar.svg',
    venueName: item.venueName || '',
    phone: item.phone || '',
    idCard: item.idCard || '',
    specialty: item.specialty || '',
    experience: item.experience || '',
    totalStudents: Number(item.totalStudents || 0),
    ratingText: item.rating != null ? Number(item.rating).toFixed(1) : '暂无',
    priceText: item.hourlyPrice != null ? Number(item.hourlyPrice).toFixed(2) : '0.00',
    genderText: item.gender === 1 ? '男' : item.gender === 0 ? '女' : '未知',
    statusMeta: getCoachStatusMeta(item.status)
  }
}

async function loadDetail() {
  if (!coachId.value) return
  loading.value = true
  errorText.value = ''
  coach.value = null

  try {
    const res = await getCoachDetail(coachId.value)
    coach.value = mapCoach(res)
  } catch (error) {
    errorText.value = error instanceof Error ? error.message : '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function reloadDetail() {
  loadDetail()
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.COACH_LIST)
}

function goEdit() {
  if (!coachId.value) return
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COACH_FORM}?id=${coachId.value}` })
}

onLoad((options?: Record<string, string | undefined>) => {
  const id = Number(options?.id || 0)
  if (!id) {
    errorText.value = '缺少教练参数'
    loading.value = false
    return
  }
  coachId.value = id
  loadDetail()
})
</script>

<style lang="scss" scoped>
.page { min-height: 100vh; background: #f8fafc; color: #111827; padding-bottom: 132rpx; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { padding: 16rpx 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.icon-btn { width: 72rpx; height: 72rpx; border-radius: 20rpx; background: #ffffff; display: flex; align-items: center; justify-content: center; }
.title { font-size: 38rpx; font-weight: 800; }
.scroll { height: calc(100vh - var(--status-bar-height) - 108rpx - 132rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.hero-card { display: flex; gap: 20rpx; padding: 24rpx; border-radius: 28rpx; background: #ffffff; box-shadow: 0 10rpx 28rpx rgba(15, 23, 42, 0.06); }
.hero-avatar { width: 160rpx; height: 160rpx; border-radius: 28rpx; background: #e5e7eb; flex-shrink: 0; }
.hero-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 10rpx; justify-content: center; }
.hero-row { display: flex; justify-content: space-between; gap: 12rpx; align-items: center; }
.name { font-size: 42rpx; font-weight: 900; }
.sub { font-size: 24rpx; color: #6b7280; }
.status-pill { padding: 10rpx 18rpx; border-radius: 9999px; font-size: 20rpx; font-weight: 800; }
.status-pill.active { background: #dcfce7; color: #166534; }
.status-pill.inactive { background: #fee2e2; color: #b91c1c; }
.status-pill.unknown { background: #e5e7eb; color: #4b5563; }
.grid { margin-top: 18rpx; display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; }
.info-card, .panel { background: #ffffff; border-radius: 24rpx; padding: 22rpx; box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.05); }
.label { display: block; font-size: 20rpx; color: #6b7280; font-weight: 700; margin-bottom: 10rpx; }
.value { font-size: 30rpx; font-weight: 800; }
.panel { margin-top: 18rpx; display: flex; flex-direction: column; gap: 12rpx; }
.panel-title { font-size: 24rpx; font-weight: 800; }
.panel-content { font-size: 24rpx; color: #4b5563; line-height: 1.7; }
.bottom-space { height: 32rpx; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; display: flex; gap: 12rpx; padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom)); background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(16px); }
.footer-btn { height: 88rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 800; }
.footer-btn.ghost { flex: 1; background: #e5e7eb; color: #111827; }
.footer-btn.primary { flex: 1; background: #ea580c; color: #ffffff; }
</style>
