<template>
  <PresidentLayout :showTabBar="false">
    <view class="course-form-page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-left" @click="onBack">
          <view class="back-btn">
            <uni-icons type="arrow-left" size="22" color="#ea580c" />
          </view>
          <text class="nav-title">{{ isEdit ? '编辑课程' : '新建课程' }}</text>
        </view>
      </view>

      <scroll-view scroll-y class="content" :show-scrollbar="false">
        <view class="hero">
          <image class="hero-img" :src="heroImg" mode="aspectFill" />
          <view class="hero-mask">
            <text class="hero-kicker">Association Management</text>
            <text class="hero-title">COURSE CONFIGURATION</text>
          </view>
        </view>

        <view class="sec-head">
          <view class="bar" />
          <text>基础信息 / BASIC</text>
        </view>

        <view class="field">
          <text class="label">课程名称</text>
          <input class="input" v-model="form.courseName" placeholder="例如：精英羽球进阶训练营" maxlength="100" />
        </view>

        <view class="grid2">
          <view class="field">
            <text class="label">关联教练</text>
            <picker mode="selector" :range="coachLabels" :value="coachIndex" @change="onCoachChange">
              <view class="input picker">
                <text>{{ coachLabels[coachIndex] || '选择主讲教练' }}</text>
                <uni-icons type="arrowdown" size="14" color="#a1a1aa" />
              </view>
            </picker>
          </view>
          <view class="field">
            <text class="label">关联场地</text>
            <picker mode="selector" :range="courtLabels" :value="courtIndex" @change="onCourtChange">
              <view class="input picker">
                <text>{{ courtLabels[courtIndex] || '选择训练场馆' }}</text>
                <uni-icons type="location" size="14" color="#a1a1aa" />
              </view>
            </picker>
          </view>
        </view>

        <view class="sec-head">
          <view class="bar" />
          <text>排课设置 / LOGISTICS</text>
        </view>

        <view class="logistics-card">
          <view class="grid2">
            <view class="field">
              <text class="label">课程日期</text>
              <view class="input icon">
                <uni-icons type="calendar" size="16" color="#a33e00" />
                <picker mode="date" :value="form.courseDate" @change="(e: any) => (form.courseDate = e.detail.value)">
                  <view class="pick-text">{{ form.courseDate || 'mm/dd/yyyy' }}</view>
                </picker>
              </view>
            </view>
            <view class="field">
              <text class="label">时长（分钟）</text>
              <view class="input icon">
                <uni-icons type="timer" size="16" color="#a33e00" />
                <input class="inner" type="number" v-model="form.courseDuration" />
              </view>
            </view>
            <view class="field">
              <text class="label">开始时间</text>
              <view class="input icon">
                <uni-icons type="clock" size="16" color="#a33e00" />
                <picker mode="time" :value="form.startTime" @change="(e: any) => (form.startTime = e.detail.value)">
                  <view class="pick-text">{{ form.startTime || '09:00' }}</view>
                </picker>
              </view>
            </view>
            <view class="field">
              <text class="label">结束时间</text>
              <view class="input icon">
                <uni-icons type="redo" size="16" color="#a33e00" />
                <picker mode="time" :value="form.endTime" @change="(e: any) => (form.endTime = e.detail.value)">
                  <view class="pick-text">{{ form.endTime || '10:00' }}</view>
                </picker>
              </view>
            </view>
          </view>
        </view>

        <view class="sec-head">
          <view class="bar" />
          <text>定价与容量 / PRICING</text>
        </view>

        <view class="pricing-grid">
          <view class="price-card">
            <text class="k">课程单价（¥）</text>
            <view class="money-row">
              <text class="yen">¥</text>
              <input class="money-input" type="digit" v-model="form.coursePrice" placeholder="0.00" />
            </view>
          </view>
          <view class="price-card">
            <text class="k">学员上限</text>
            <view class="money-row">
              <input class="money-input" type="number" v-model="form.maxStudents" />
              <text class="unit">人</text>
            </view>
          </view>
        </view>

        <view class="spacer" />
      </scroll-view>

      <view class="footer">
        <view class="save-btn" :class="{ disabled: submitting }" @click="onSubmit">
          <uni-icons type="wallet" size="16" color="#ffffff" />
          <text>保存课程配置</text>
        </view>
        <text class="suite">KINETIC LOGIC MANAGEMENT SUITE V2.0</text>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { safeNavigateBack } from '@/utils/navigation'
import { addCourse, getCoachList, getCourseDetail, updateCourse } from '@/api/president/course'
import { getCourtList } from '@/api/court'

const heroImg =
  '/static/placeholders/hero.svg'

const isEdit = ref(false)
const courseId = ref<number | null>(null)
const submitting = ref(false)

const coaches = ref<any[]>([])
const courts = ref<any[]>([])
const coachIndex = ref(0)
const courtIndex = ref(0)

const form = reactive({
  courseName: '',
  coachId: 0,
  courtId: 0,
  courseDate: '',
  courseDuration: '60',
  startTime: '09:00',
  endTime: '10:00',
  coursePrice: '0.00',
  maxStudents: '10'
})

const coachLabels = computed(() => ['选择主讲教练', ...coaches.value.map((c) => c.coachName || c.name || `教练#${c.id}`)])
const courtLabels = computed(() => ['选择训练场馆', ...courts.value.map((c) => c.courtName || c.name || `场地#${c.id}`)])

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURSE_LIST)
}

function onCoachChange(e: { detail: { value: string } }) {
  coachIndex.value = Number(e.detail.value)
  if (coachIndex.value <= 0) {
    form.coachId = 0
    return
  }
  const c = coaches.value[coachIndex.value - 1]
  form.coachId = Number(c?.id || 0)
}

function onCourtChange(e: { detail: { value: string } }) {
  courtIndex.value = Number(e.detail.value)
  if (courtIndex.value <= 0) {
    form.courtId = 0
    return
  }
  const c = courts.value[courtIndex.value - 1]
  form.courtId = Number(c?.id || 0)
}

function combineDateTime(date: string, time: string) {
  return `${date} ${time}:00`
}

function validate() {
  if (!form.courseName.trim()) return '请填写课程名称'
  if (!form.coachId) return '请选择主讲教练'
  if (!form.courseDate) return '请选择课程日期'
  const dur = Number(form.courseDuration)
  if (!Number.isFinite(dur) || dur < 1) return '时长需大于 0'
  const price = Number(form.coursePrice)
  if (!Number.isFinite(price) || price < 0) return '课程单价无效'
  const max = Number(form.maxStudents)
  if (!Number.isFinite(max) || max < 1) return '学员上限至少为 1'
  return ''
}

async function loadOptions() {
  try {
    const [coachRes, courtRes] = await Promise.all([
      getCoachList({ status: 1, page: 1, size: 100 }) as any,
      getCourtList({ page: 1, size: 100 }) as any
    ])
    const coachData = Array.isArray(coachRes) ? coachRes : coachRes?.data || []
    const courtData = courtRes?.data || []
    coaches.value = coachData
    courts.value = courtData
  } catch {
    coaches.value = []
    courts.value = []
  }
}

async function onSubmit() {
  if (submitting.value) return
  const err = validate()
  if (err) {
    uni.showToast({ title: err, icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const payload = {
      id: courseId.value ?? undefined,
      courseName: form.courseName.trim(),
      coachId: Number(form.coachId),
      courtId: form.courtId ? Number(form.courtId) : undefined,
      coursePrice: Number(form.coursePrice),
      courseDuration: Number(form.courseDuration),
      courseDate: form.courseDate,
      startTime: form.startTime,
      endTime: form.endTime,
      maxStudents: Number(form.maxStudents),
      status: 1
    }
    if (isEdit.value) {
      await updateCourse(payload)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      await addCourse(payload)
      uni.showToast({ title: '创建成功', icon: 'success' })
    }
    setTimeout(() => onBack(), 500)
  } catch {
    // request 已统一 toast
  } finally {
    submitting.value = false
  }
}

onLoad(async (q?: Record<string, string | undefined>) => {
  await loadOptions()
  const raw = q?.id
  const id = raw ? parseInt(String(raw), 10) : NaN
  if (!Number.isFinite(id) || id <= 0) return

  isEdit.value = true
  courseId.value = id
  try {
    const info = await getCourseDetail(id)
    form.courseName = info.courseName || ''
    form.coachId = Number(info.coachId || 0)
    form.courtId = Number(info.courtId || 0)
    form.courseDate = (info.courseDate || '').slice(0, 10)
    form.courseDuration = String(info.courseDuration ?? 60)
    form.startTime = (info.startTime || '09:00').slice(0, 5)
    form.endTime = (info.endTime || '10:00').slice(0, 5)
    form.coursePrice = String(info.coursePrice ?? '0.00')
    form.maxStudents = String(info.maxStudents ?? 10)

    const ci = coaches.value.findIndex((c) => Number(c.id) === form.coachId)
    coachIndex.value = ci >= 0 ? ci + 1 : 0
    const ct = courts.value.findIndex((c) => Number(c.id) === form.courtId)
    courtIndex.value = ct >= 0 ? ct + 1 : 0
  } catch {
    // ignore
  }
})
</script>

<style lang="scss" scoped>
.course-form-page { min-height: 100vh; background: #f9f9f9; color: #1a1c1c; padding-bottom: 180rpx; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.nav-header { position: sticky; top: 0; z-index: 40; background: rgba(248,250,252,.92); backdrop-filter: blur(12px); padding: 12rpx 16rpx; }
.nav-left { display: flex; align-items: center; gap: 12rpx; min-height: 72rpx; }
.back-btn { width: 56rpx; height: 56rpx; border-radius: 9999px; display: flex; align-items: center; justify-content: center; background: #fff; }
.nav-title { font-size: 32rpx; color: #ea580c; font-weight: 900; }

.content { height: calc(100vh - var(--status-bar-height) - 260rpx); padding: 20rpx 24rpx 0; box-sizing: border-box; }
.hero { position: relative; height: 300rpx; border-radius: 26rpx; overflow: hidden; margin-bottom: 22rpx; }
.hero-img { width: 100%; height: 100%; }
.hero-mask { position: absolute; inset: 0; background: linear-gradient(180deg, rgba(0,0,0,.12), rgba(0,0,0,.62)); display: flex; flex-direction: column; justify-content: flex-end; padding: 22rpx; }
.hero-kicker { color: #fb923c; font-size: 14rpx; font-weight: 900; letter-spacing: .18em; text-transform: uppercase; }
.hero-title { margin-top: 6rpx; color: #fff; font-size: 44rpx; font-weight: 900; letter-spacing: -0.03em; }

.sec-head { display: flex; align-items: center; gap: 10rpx; margin: 20rpx 0 14rpx; }
.bar { width: 6rpx; height: 34rpx; border-radius: 99rpx; background: #a33e00; }
.sec-head text { font-size: 20rpx; font-weight: 900; color: #5f5e5e; letter-spacing: .12em; text-transform: uppercase; }

.field { display: flex; flex-direction: column; gap: 8rpx; margin-bottom: 12rpx; }
.label { font-size: 16rpx; font-weight: 900; color: #71717a; letter-spacing: .1em; text-transform: uppercase; }
.input { min-height: 86rpx; background: #fff; border-radius: 16rpx; padding: 0 14rpx; font-size: 25rpx; font-weight: 700; display: flex; align-items: center; }
.input.picker { justify-content: space-between; }
.grid2 { display: grid; grid-template-columns: 1fr; gap: 10rpx; }
@media screen and (min-width: 768px) { .grid2 { grid-template-columns: 1fr 1fr; } }

.logistics-card { background: #f3f3f3; border-radius: 22rpx; padding: 14rpx; }
.input.icon { gap: 10rpx; }
.inner { flex: 1; font-size: 25rpx; font-weight: 700; }
.pick-text { font-size: 25rpx; font-weight: 700; color: #1a1c1c; }

.pricing-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10rpx; }
.price-card { background: #fff; border-radius: 22rpx; padding: 16rpx; box-shadow: 0 4rpx 12rpx rgba(2,6,23,.04); }
.k { font-size: 14rpx; color: #71717a; font-weight: 900; letter-spacing: .1em; text-transform: uppercase; }
.money-row { margin-top: 8rpx; display: flex; align-items: baseline; gap: 6rpx; }
.yen { color: #a33e00; font-weight: 900; font-size: 24rpx; }
.money-input { flex: 1; font-size: 46rpx; font-weight: 900; letter-spacing: -0.03em; }
.unit { color: #a1a1aa; font-size: 22rpx; font-weight: 700; }
.spacer { height: 30rpx; }

.footer {
  position: fixed; left: 0; right: 0; bottom: 0; z-index: 60;
  padding: 22rpx 24rpx calc(22rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, rgba(249,249,249,0), rgba(249,249,249,.95) 40%, rgba(249,249,249,1));
}
.save-btn {
  height: 96rpx; border-radius: 20rpx; background: linear-gradient(90deg, #a33e00, #ff6600);
  color: #fff; display: flex; align-items: center; justify-content: center; gap: 10rpx;
  font-size: 30rpx; font-weight: 900; box-shadow: 0 16rpx 40rpx rgba(234,88,12,.25);
}
.save-btn.disabled { opacity: .6; pointer-events: none; }
.suite { display: block; text-align: center; margin-top: 12rpx; font-size: 14rpx; letter-spacing: .18em; color: #a1a1aa; font-weight: 700; }
</style>
