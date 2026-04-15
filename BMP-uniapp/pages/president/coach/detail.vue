<template>
  <PresidentLayout :showTabBar="false">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left" @click="goBack">
          <view class="icon-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="title">Kinetic Logic</text>
        </view>
        <view class="top-right">
          <uni-icons type="gear" size="20" color="#ea580c" />
          <image v-if="coach?.avatar" class="top-avatar" :src="coach.avatar" mode="aspectFill" />
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
          <view class="hero-wrap">
            <image class="hero-cover" :src="coach.avatar" mode="aspectFill" />
            <view class="hero-mask" />
            <view class="hero-content">
              <view class="elite-pill">Elite Coach</view>
              <view class="hero-row">
                <text class="name">{{ coach.name }}</text>
                <view class="status-pill" :class="coach.statusMeta.key">
                  {{ coach.statusMeta.label }}
                </view>
              </view>
              <view class="rating-row">
                <view class="star-group">
                  <uni-icons type="star-filled" size="14" color="#ff7a00" />
                  <uni-icons type="star-filled" size="14" color="#ff7a00" />
                  <uni-icons type="star-filled" size="14" color="#ff7a00" />
                  <uni-icons type="star-filled" size="14" color="#ff7a00" />
                  <uni-icons type="star-half" size="14" color="#ff7a00" />
                </view>
                <text class="rating-text">{{ coach.ratingText }}</text>
                <text class="rating-count">({{ coach.totalStudents }}名学员)</text>
              </view>
            </view>
          </view>

          <view class="grid stat-grid">
            <view class="info-card">
              <text class="label">教练评分</text>
              <text class="value">{{ coach.ratingText }}</text>
              <text class="desc">RATING</text>
            </view>
            <view class="info-card">
              <text class="label">课时费用</text>
              <text class="value card-money">¥{{ coach.priceText }}</text>
              <text class="desc">PRICE / HOUR</text>
            </view>
            <view class="info-card">
              <text class="label">学员满意度</text>
              <text class="value">{{ coach.ratingText === '暂无' ? '暂无' : '98%' }}</text>
              <text class="desc">SATISFACTION</text>
            </view>
            <view class="info-card">
              <text class="label">专业等级</text>
              <text class="value">{{ coach.genderText }}</text>
              <text class="desc">LEVEL</text>
            </view>
          </view>

          <view class="panel">
            <view class="section-title">
              <view class="section-line" />
              <text class="panel-title">教练简介</text>
            </view>
            <text class="panel-content">{{ coach.experience || '未填写执教经验' }}</text>
          </view>

          <view class="panel">
            <view class="section-title">
              <view class="section-line" />
              <text class="panel-title">技术特长</text>
            </view>
            <view v-if="specialtyTags.length" class="chip-wrap">
              <text v-for="(tag, idx) in specialtyTags" :key="`${coach.id}-${idx}`" class="chip">{{ tag }}</text>
            </view>
            <text v-else class="panel-content">{{ coach.specialty || '未填写专长信息' }}</text>
          </view>

          <view class="panel warm">
            <text class="warm-title">执教资质</text>
            <view class="meta-list warm-list">
              <view class="meta-item">
                <view class="qual-item">
                  <uni-icons type="checkmarkempty" size="14" color="#a33e00" />
                  <text class="meta-key">所属场馆：{{ coach.venueName || '未绑定场馆' }}</text>
                </view>
              </view>
              <view class="meta-item">
                <view class="qual-item">
                  <uni-icons type="checkmarkempty" size="14" color="#a33e00" />
                  <text class="meta-key">联系电话：{{ coach.phone || '未填写联系电话' }}</text>
                </view>
              </view>
              <view class="meta-item">
                <view class="qual-item">
                  <uni-icons type="checkmarkempty" size="14" color="#a33e00" />
                  <text class="meta-key">身份证号：{{ coach.idCard || '未填写身份证号' }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="panel">
            <view class="section-title">
              <view class="section-line" />
              <text class="panel-title">开设课程</text>
              <text class="view-all">查看全部</text>
            </view>
            <view class="course-card">
              <image class="course-cover" :src="coach.avatar" mode="aspectFill" />
              <view class="course-main">
                <text class="course-level">ADVANCED / 进阶</text>
                <text class="course-name">{{ coach.specialty || '专项课程待配置' }}</text>
                <view class="course-meta">
                  <text>12课时</text>
                  <text>4人班</text>
                </view>
              </view>
            </view>
            <view class="course-card">
              <image class="course-cover" :src="coach.avatar" mode="aspectFill" />
              <view class="course-main">
                <text class="course-level">INTERMEDIATE / 中级</text>
                <text class="course-name">{{ coach.specialty || '场上移动效率提升' }}</text>
                <view class="course-meta">
                  <text>8课时</text>
                  <text>1对1</text>
                </view>
              </view>
            </view>
          </view>
        </template>

        <view class="bottom-space" />
      </scroll-view>

      <view v-if="coach" class="footer">
        <view class="footer-price">
          <text class="price-label">单课起价</text>
          <text class="price-value">¥{{ coach.priceText }}<text class="price-unit">/小时</text></text>
        </view>
        <view class="footer-btn ghost" @click="goBack">返回列表</view>
        <view class="footer-btn primary" @click="goEdit">编辑教练</view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { getCoachDetail, type CoachDto } from '@/api/president/coach'
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
const specialtyTags = computed(() => {
  if (!coach.value?.specialty) return []
  return coach.value.specialty
    .split(/[、,，;；/\s]+/)
    .map(v => v.trim())
    .filter(Boolean)
    .slice(0, 6)
})

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
.page { min-height: 100vh; background: #f9f9f9; color: #1f2937; padding-bottom: 176rpx; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f9f9f9; }
.top-bar { padding: 14rpx 24rpx; background: #f9f9f9; display: flex; align-items: center; justify-content: space-between; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.top-right { min-width: 128rpx; display: flex; justify-content: flex-end; align-items: center; gap: 10rpx; }
.top-avatar { width: 44rpx; height: 44rpx; border-radius: 9999px; border: 2rpx solid #fff; }
.icon-btn { width: 56rpx; height: 56rpx; border-radius: 9999rpx; background: transparent; display: flex; align-items: center; justify-content: center; }
.title { font-size: 40rpx; font-weight: 900; letter-spacing: .1rpx; }
.scroll { height: calc(100vh - var(--status-bar-height) - 96rpx - 176rpx); padding: 0 24rpx; box-sizing: border-box; }
.state-wrap { min-height: 420rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16rpx; color: #6b7280; }
.spinner { width: 44rpx; height: 44rpx; border: 4rpx solid #e5e7eb; border-top-color: #ea580c; border-radius: 9999px; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.retry-btn { display: inline-flex; align-items: center; justify-content: center; height: 72rpx; padding: 0 32rpx; border-radius: 16rpx; background: #ea580c; color: #ffffff; font-weight: 700; }
.hero-wrap { margin-top: 4rpx; border-radius: 0 0 32rpx 32rpx; position: relative; overflow: hidden; height: 560rpx; background: #e5e7eb; }
.hero-cover { width: 100%; height: 100%; }
.hero-mask { position: absolute; left: 0; right: 0; bottom: 0; height: 64%; background: linear-gradient(to top, rgba(249, 249, 249, 1), rgba(249, 249, 249, 0)); }
.hero-content { position: absolute; left: 24rpx; right: 24rpx; bottom: 20rpx; display: flex; flex-direction: column; gap: 10rpx; }
.elite-pill { align-self: flex-start; background: #ff7a00; color: #fff; font-size: 17rpx; padding: 8rpx 18rpx; border-radius: 999rpx; font-weight: 800; letter-spacing: 1rpx; text-transform: uppercase; }
.hero-row { display: flex; justify-content: space-between; gap: 12rpx; align-items: center; }
.name { font-size: 54rpx; font-weight: 900; color: #111827; flex: 1; }
.rating-row { display: flex; align-items: center; gap: 6rpx; }
.star-group { display: flex; align-items: center; }
.rating-text { font-size: 27rpx; color: #111827; font-weight: 800; margin-left: 2rpx; }
.rating-count { font-size: 20rpx; color: #6b7280; }
.status-pill { padding: 10rpx 18rpx; border-radius: 9999px; font-size: 20rpx; font-weight: 800; backdrop-filter: blur(2px); }
.status-pill.active { background: #dcfce7; color: #166534; }
.status-pill.inactive { background: #fee2e2; color: #b91c1c; }
.status-pill.unknown { background: #e5e7eb; color: #4b5563; }
.grid { margin-top: 18rpx; display: grid; grid-template-columns: repeat(2, 1fr); gap: 14rpx; }
.stat-grid { margin-top: 16rpx; }
.info-card, .panel { background: #ffffff; border-radius: 22rpx; padding: 24rpx; box-shadow: 0 8rpx 20rpx rgba(15, 23, 42, 0.04); }
.info-card { background: #ffffff; box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04); padding: 22rpx 20rpx; border-radius: 20rpx; align-items: center; text-align: center; }
.label { display: block; font-size: 17rpx; color: #6b7280; font-weight: 700; margin-bottom: 8rpx; letter-spacing: 1rpx; text-transform: uppercase; }
.value { display: block; font-size: 44rpx; font-weight: 900; color: #a33e00; line-height: 1.12; }
.card-money { font-size: 42rpx; }
.desc { display: block; margin-top: 8rpx; font-size: 17rpx; color: #8b5e3c; font-weight: 700; letter-spacing: 1rpx; text-transform: uppercase; }
.panel { margin-top: 18rpx; display: flex; flex-direction: column; gap: 12rpx; }
.section-title { display: flex; align-items: center; gap: 10rpx; }
.section-line { width: 6rpx; height: 26rpx; border-radius: 99rpx; background: #ff7a00; }
.panel-title { font-size: 32rpx; font-weight: 900; color: #111827; }
.panel-content { font-size: 25rpx; color: #4b5563; line-height: 1.75; }
.chip-wrap { display: flex; flex-wrap: wrap; gap: 10rpx; }
.chip { background: #eceff3; color: #475569; border-radius: 10rpx; padding: 8rpx 14rpx; font-size: 21rpx; font-weight: 700; }
.meta-list { display: flex; flex-direction: column; gap: 14rpx; }
.meta-item { display: flex; align-items: flex-start; justify-content: space-between; gap: 20rpx; }
.meta-key { font-size: 24rpx; color: #64748b; }
.meta-val { font-size: 24rpx; color: #0f172a; font-weight: 700; text-align: right; flex: 1; }
.warm { background: #ffe8db; }
.warm-title { font-size: 28rpx; font-weight: 800; color: #7c2e00; }
.warm-list .meta-key { color: #7c2e00; }
.qual-item { display: flex; align-items: center; gap: 8rpx; }
.view-all { margin-left: auto; color: #c2410c; font-size: 20rpx; font-weight: 700; }
.course-card { display: flex; gap: 14rpx; background: #fff; border-radius: 18rpx; padding: 10rpx; box-shadow: inset 0 0 0 1rpx #f1f5f9; margin-top: 10rpx; }
.course-cover { width: 140rpx; height: 100rpx; border-radius: 12rpx; }
.course-main { display: flex; flex-direction: column; justify-content: center; gap: 8rpx; min-width: 0; }
.course-level { color: #c2410c; font-size: 16rpx; font-weight: 700; letter-spacing: .8rpx; }
.course-name { font-size: 24rpx; color: #0f172a; font-weight: 800; }
.course-meta { display: flex; gap: 16rpx; color: #64748b; font-size: 20rpx; }
.bottom-space { height: 42rpx; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; display: flex; align-items: center; gap: 12rpx; padding: 18rpx 24rpx calc(18rpx + env(safe-area-inset-bottom)); background: rgba(255, 255, 255, 0.96); backdrop-filter: blur(16px); border-top: 1rpx solid #f1f5f9; }
.footer-price { flex: 1.05; display: flex; flex-direction: column; gap: 4rpx; }
.price-label { font-size: 18rpx; color: #94a3b8; letter-spacing: 1rpx; text-transform: uppercase; font-weight: 700; }
.price-value { font-size: 40rpx; font-weight: 900; color: #0f172a; line-height: 1; }
.price-unit { font-size: 20rpx; color: #6b7280; margin-left: 6rpx; font-weight: 600; }
.footer-btn { height: 88rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 800; }
.footer-btn.ghost { flex: 1; background: #eef2f7; color: #111827; }
.footer-btn.primary { flex: 1; background: linear-gradient(90deg, #ff7a00, #ff5a00); color: #ffffff; box-shadow: 0 8rpx 20rpx rgba(255, 106, 0, 0.25); }
</style>
