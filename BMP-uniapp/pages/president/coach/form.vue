<template>
  <PresidentLayout :showTabBar="false">
    <view class="coach-form-page">
      <view class="status-bar-placeholder" />

      <view class="header">
        <view class="header-left">
          <view class="hit" @click="onBack">
            <uni-icons type="arrow-left" size="22" color="#1a1c1c" />
          </view>
          <text class="header-title">{{ pageTitle }}</text>
        </view>
        <text class="save-top" @click="onSubmit">SAVE</text>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view v-if="pageLoading" class="loading">加载中…</view>
        <template v-else>
          <!-- 01 基本信息 -->
          <view class="sec-head">
            <text class="sec-label">01 基本信息 / BASIC INFO</text>
            <view class="sec-line" />
          </view>
          <view class="grid-basic">
            <view class="avatar-col">
              <view class="avatar-shell" @click="pickAvatar">
                <image class="avatar-img" :src="avatarDisplay" mode="aspectFill" />
                <view class="avatar-fab">
                  <uni-icons type="camera" size="18" color="#ffffff" />
                </view>
              </view>
              <text class="avatar-hint">更新头像</text>
            </view>
            <view class="fields-col">
              <view class="field">
                <text class="lab">姓名 / NAME</text>
                <input class="inp" v-model="form.coachName" placeholder="输入教练姓名" maxlength="50" />
              </view>
              <view class="row-2">
                <view class="field half">
                  <text class="lab muted">性别 / GENDER</text>
                  <picker mode="selector" :range="genderLabels" :value="genderIndex" @change="onGenderPick">
                    <view class="inp picker-row">
                      <text>{{ genderLabels[genderIndex] }}</text>
                      <uni-icons type="arrowdown" size="14" color="#71717a" />
                    </view>
                  </picker>
                </view>
                <view class="field half">
                  <text class="lab muted">联系电话 / PHONE</text>
                  <input class="inp" v-model="form.phone" type="number" placeholder="11 位手机号" maxlength="11" />
                </view>
              </view>
              <view class="field">
                <text class="lab muted">身份证号 / ID CARD</text>
                <input class="inp" v-model="form.idCard" placeholder="选填" maxlength="18" />
              </view>
            </view>
          </view>

          <!-- 02 职业背景 -->
          <view class="sec-head m-top">
            <text class="sec-label">02 职业背景 / PROFESSIONAL</text>
            <view class="sec-line" />
          </view>
          <view class="field">
            <text class="lab muted">教学专长 / SPECIALTIES</text>
            <view class="tags">
              <view class="tag" v-for="(t, i) in tags" :key="i">
                <text>{{ t }}</text>
                <uni-icons type="closeempty" size="14" color="#561d00" @click="removeTag(i)" />
              </view>
              <view v-if="!tagInputOpen" class="tag add" @click="tagInputOpen = true">
                <text>+ 添加标签</text>
              </view>
            </view>
            <view v-if="tagInputOpen" class="tag-input-row">
              <input class="inp flex1" v-model="tagDraft" placeholder="输入标签后确定" maxlength="30" />
              <view class="mini-btn" @click="confirmTag">确定</view>
              <view class="mini-btn ghost" @click="cancelTag">取消</view>
            </view>
          </view>
          <view class="row-2 venue-row">
            <view class="field half">
              <text class="lab muted">所属场馆 / VENUE</text>
              <picker
                mode="selector"
                :range="venueNames"
                :value="venuePickerValue"
                @change="onVenuePick"
                :disabled="venues.length === 0"
              >
                <view class="inp picker-row">
                  <text>{{ venueNames[venueIndex] || '请选择场馆' }}</text>
                  <uni-icons type="arrowdown" size="14" color="#71717a" />
                </view>
              </picker>
            </view>
            <view class="field half">
              <text class="lab muted">总学员数 / STUDENTS</text>
              <input class="inp ro" :value="String(totalStudentsDisplay)" disabled type="number" />
            </view>
          </view>
          <view class="field">
            <text class="lab muted">教学经验 / EXPERIENCE</text>
            <textarea
              class="area"
              v-model="form.experience"
              placeholder="描述教练的执教生涯、获得奖项等..."
              maxlength="500"
            />
          </view>

          <!-- 03 价格与状态 -->
          <view class="sec-head m-top">
            <text class="sec-label">03 价格与状态 / PRICING &amp; STATUS</text>
            <view class="sec-line" />
          </view>
          <view class="pricing-card">
            <view class="price-block">
              <text class="lab pri">时薪 (元/小时) / HOURLY PRICE</text>
              <view class="money-row">
                <text class="yen">¥</text>
                <input class="money-input" type="digit" v-model="form.hourlyPrice" />
              </view>
            </view>
            <view class="side-block">
              <view class="rate-box">
                <text class="lab muted center">评分 / RATING</text>
                <view class="rate-val">
                  <uni-icons type="star-filled" size="20" color="#a33e00" />
                  <text class="rate-num">{{ ratingDisplay }}</text>
                </view>
              </view>
              <view class="stat-box">
                <text class="lab muted center">执教状态 / STATUS</text>
                <view class="toggle-row">
                  <switch :checked="form.status === 1" color="#ff6600" @change="onStatusChange" />
                  <text class="stat-txt">{{ form.status === 1 ? '在职' : '停用' }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="submit-wrap">
            <view class="submit" :class="{ disabled: submitting }" @click="onSubmit">
              <text>保存教练资料 / SAVE COACH PROFILE</text>
            </view>
          </view>
        </template>
        <view class="spacer" />
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { resolveImageUrl } from '@/utils/resolveImageUrl'
import { addCoach, getCoachDetail, getCoachVenueOptions, updateCoach, type CoachDto, type CoachVenueOption } from '@/api/coach'

const HERO_AVATAR =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCTguqN8hnBjJEkaT5bq0Mkzq60LR0M--aNulfuRUi4AfqH1e4vxQ1sgK_KqLN2WYY1yeK5syOPON4FoYTPjAP4cm2PgGozl7Ycv3Co-H8Y8icgYWheBPAj504KPB03LxTLbQU5UwohQiMzJiJYpMJIfZ45LOi91vpbn408kVhadpP1K0UhicS4jGOmFZOUgTRdrm1xC3toUmVOx7eFnepKoOlSS7K-qU8dqXCPisZAC_xXI7JXSCIcwkSq2PSGa1I6k6I8PL5f4RpL'

const genderLabels = ['男 / MALE', '女 / FEMALE']
const genderValues = [1, 0]

const coachId = ref<number | null>(null)
const isEdit = computed(() => coachId.value != null && coachId.value > 0)
const pageTitle = computed(() => (isEdit.value ? '编辑教练' : '新增教练'))

const pageLoading = ref(false)
const submitting = ref(false)

const venues = ref<CoachVenueOption[]>([])
const venueIndex = ref(0)
const venueNames = computed(() => venues.value.map((v) => v.venueName))

const genderIndex = ref(0)

const tags = ref<string[]>([])
const tagInputOpen = ref(false)
const tagDraft = ref('')

const meta = reactive({
  rating: null as number | null,
  totalStudents: 0 as number,
  userId: null as number | null
})

const form = reactive({
  coachName: '',
  phone: '',
  idCard: '',
  experience: '',
  hourlyPrice: '',
  status: 1,
  avatar: '' as string
})

const avatarDisplay = computed(() => {
  if (form.avatar) return resolveImageUrl(form.avatar) || form.avatar
  return HERO_AVATAR
})

const totalStudentsDisplay = computed(() =>
  isEdit.value ? meta.totalStudents : 0
)

const ratingDisplay = computed(() =>
  meta.rating != null && !Number.isNaN(Number(meta.rating))
    ? Number(meta.rating).toFixed(1)
    : '—'
)

const venuePickerValue = computed(() =>
  venues.value.length === 0 ? 0 : Math.min(Math.max(0, venueIndex.value), venues.value.length - 1)
)

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.COACH_LIST)
}

function onGenderPick(e: { detail: { value: string } }) {
  genderIndex.value = Number(e.detail.value)
}

function onVenuePick(e: { detail: { value: string } }) {
  venueIndex.value = Number(e.detail.value)
}

function onStatusChange(e: { detail: { value: boolean } }) {
  form.status = e.detail.value ? 1 : 0
}

function pickAvatar() {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      form.avatar = res.tempFilePaths?.[0] || ''
    }
  })
}

function removeTag(i: number) {
  tags.value.splice(i, 1)
}

function confirmTag() {
  const t = tagDraft.value.trim()
  if (!t) {
    uni.showToast({ title: '请输入标签', icon: 'none' })
    return
  }
  if (tags.value.includes(t)) {
    uni.showToast({ title: '标签已存在', icon: 'none' })
    return
  }
  const next = [...tags.value, t].join('、')
  if (next.length > 200) {
    uni.showToast({ title: '专长总长度不能超过 200 字', icon: 'none' })
    return
  }
  tags.value.push(t)
  tagDraft.value = ''
  tagInputOpen.value = false
}

function cancelTag() {
  tagDraft.value = ''
  tagInputOpen.value = false
}

function parseSpecialtyToTags(s?: string) {
  if (!s || !s.trim()) return []
  return s
    .split(/[、,，;；]+/)
    .map((x) => x.trim())
    .filter(Boolean)
}

function specialtyFromTags() {
  return tags.value.join('、')
}

function validate(): string {
  if (!form.coachName.trim()) return '请填写教练姓名'
  const phone = form.phone.replace(/\D/g, '')
  if (phone && !/^1[3-9]\d{9}$/.test(phone)) return '请输入正确的手机号'
  if (form.idCard.trim()) {
    const id = form.idCard.trim().toUpperCase()
    const ok15 = /^[1-9]\d{7}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}$/.test(id)
    const ok18 = /^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}[0-9X]$/.test(id)
    if (!ok15 && !ok18) return '身份证号格式不正确'
  }
  if (!venues.value.length) return '暂无可选场馆，请稍后重试'
  if (venueIndex.value < 0 || venueIndex.value >= venues.value.length) return '请选择所属场馆'
  const price = Number(form.hourlyPrice)
  if (form.hourlyPrice === '' || Number.isNaN(price) || price < 0) return '请填写有效的时薪'
  if (form.experience.length > 500) return '教学经验不能超过 500 字'
  const spec = specialtyFromTags()
  if (spec.length > 200) return '教学专长总长度不能超过 200 字'
  return ''
}

async function onSubmit() {
  if (submitting.value || pageLoading.value) return
  const err = validate()
  if (err) {
    uni.showToast({ title: err, icon: 'none' })
    return
  }
  submitting.value = true
  try {
    const phone = form.phone.replace(/\D/g, '')
    const venueId = venues.value[venueIndex.value]?.id
    const specialty = specialtyFromTags() || undefined
    const idCardVal = form.idCard.trim() || undefined

    if (isEdit.value && coachId.value) {
      const payload: CoachDto = {
        id: coachId.value,
        coachName: form.coachName.trim(),
        gender: genderValues[genderIndex.value],
        phone: phone || undefined,
        idCard: idCardVal,
        specialty,
        experience: form.experience.trim() || undefined,
        hourlyPrice: Number(form.hourlyPrice),
        rating: meta.rating != null ? Number(meta.rating) : undefined,
        totalStudents: meta.totalStudents,
        status: form.status,
        avatar: form.avatar || undefined,
        venueId,
        userId: meta.userId
      }
      await updateCoach(payload)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      const payload: CoachDto = {
        coachName: form.coachName.trim(),
        gender: genderValues[genderIndex.value],
        phone: phone || undefined,
        idCard: idCardVal,
        specialty,
        experience: form.experience.trim() || undefined,
        hourlyPrice: Number(form.hourlyPrice),
        status: form.status,
        avatar: form.avatar || undefined,
        venueId
      }
      await addCoach(payload)
      uni.showToast({ title: '新增成功', icon: 'success' })
    }
    setTimeout(() => onBack(), 450)
  } finally {
    submitting.value = false
  }
}

async function loadVenues() {
  const list = await getCoachVenueOptions()
  venues.value = Array.isArray(list) ? list : []
}

async function loadCoach(id: number) {
  const c = await getCoachDetail(id)
  form.coachName = c.coachName || ''
  form.phone = c.phone || ''
  form.idCard = c.idCard || ''
  form.experience = c.experience || ''
  form.hourlyPrice = c.hourlyPrice != null ? String(c.hourlyPrice) : ''
  form.status = c.status ?? 1
  form.avatar = c.avatar || ''
  meta.rating = c.rating != null ? Number(c.rating) : null
  meta.totalStudents = c.totalStudents ?? 0
  meta.userId = c.userId != null ? Number(c.userId) : null

  const g = c.gender
  const idx = genderValues.findIndex((v) => v === g)
  genderIndex.value = idx >= 0 ? idx : 0

  tags.value = parseSpecialtyToTags(c.specialty)

  if (c.venueId != null && venues.value.length) {
    const vi = venues.value.findIndex((v) => v.id === c.venueId)
    venueIndex.value = vi >= 0 ? vi : 0
  }
}

onLoad(async (q?: Record<string, string | undefined>) => {
  const raw = q?.id
  const n = raw ? parseInt(String(raw), 10) : NaN
  if (Number.isFinite(n) && n > 0) coachId.value = n

  pageLoading.value = true
  try {
    await loadVenues()
    if (coachId.value) {
      await loadCoach(coachId.value)
    } else {
      genderIndex.value = 0
      venueIndex.value = venues.value.length ? 0 : -1
      tags.value = []
      form.experience = ''
      form.hourlyPrice = ''
      form.coachName = ''
      form.phone = ''
      form.idCard = ''
      form.status = 1
      meta.rating = null
      meta.totalStudents = 0
      meta.userId = null
    }
  } catch {
    venues.value = []
  } finally {
    pageLoading.value = false
  }
})
</script>

<style lang="scss" scoped>
.coach-form-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
  font-family: 'Lexend', -apple-system, sans-serif;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}
.header {
  position: sticky;
  top: 0;
  z-index: 40;
  background: #f9f9f9;
  padding: 16rpx 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.hit {
  width: 56rpx;
  height: 56rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e8e8e8;
}
.header-title {
  font-size: 34rpx;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.save-top {
  color: #ff6600;
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 0.2em;
}
.scroll {
  height: calc(100vh - var(--status-bar-height) - 100rpx);
  padding: 24rpx 28rpx 48rpx;
  box-sizing: border-box;
}
.loading {
  text-align: center;
  padding: 80rpx;
  color: #71717a;
  font-weight: 600;
}
.sec-head {
  display: flex;
  align-items: flex-end;
  gap: 12rpx;
  margin-bottom: 20rpx;
}
.sec-head.m-top {
  margin-top: 40rpx;
}
.sec-label {
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  white-space: nowrap;
}
.sec-line {
  flex: 1;
  height: 2rpx;
  background: rgba(227, 191, 177, 0.35);
}
.grid-basic {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
@media (min-width: 768px) {
  .grid-basic {
    flex-direction: row;
    align-items: flex-start;
  }
}
.avatar-col {
  align-items: center;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.avatar-shell {
  position: relative;
  width: 240rpx;
  height: 240rpx;
  border-radius: 24rpx;
  overflow: hidden;
  border: 4rpx dashed rgba(142, 113, 100, 0.45);
  background: #e8e8e8;
}
.avatar-img {
  width: 100%;
  height: 100%;
}
.avatar-fab {
  position: absolute;
  right: 10rpx;
  bottom: 10rpx;
  width: 52rpx;
  height: 52rpx;
  border-radius: 9999px;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-hint {
  margin-top: 12rpx;
  font-size: 18rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}
.fields-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.field.half {
  flex: 1;
  min-width: 0;
}
.lab {
  font-size: 18rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}
.lab.muted {
  color: #5f5e5e;
}
.lab.pri {
  color: #a33e00;
}
.lab.center {
  text-align: center;
  display: block;
}
.inp {
  background: #ffffff;
  border-radius: 12rpx;
  padding: 20rpx 22rpx;
  font-size: 26rpx;
  font-weight: 600;
}
.inp.ro {
  background: #f3f3f3;
  color: #5a4136;
  font-weight: 800;
  opacity: 1;
}
.picker-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.row-2 {
  display: flex;
  gap: 16rpx;
}
.venue-row {
  margin-top: 12rpx;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 8rpx;
}
.tag {
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: #ff6600;
  color: #561d00;
  padding: 10rpx 20rpx;
  border-radius: 9999px;
  font-size: 22rpx;
  font-weight: 800;
}
.tag.add {
  background: #e8e8e8;
  color: #5a4136;
  border: 2rpx dashed rgba(142, 113, 100, 0.45);
}
.tag-input-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-top: 12rpx;
}
.flex1 {
  flex: 1;
}
.mini-btn {
  padding: 14rpx 20rpx;
  border-radius: 12rpx;
  background: #a33e00;
  color: #ffffff;
  font-size: 22rpx;
  font-weight: 800;
}
.mini-btn.ghost {
  background: #e5e5e5;
  color: #1a1c1c;
}
.area {
  background: #ffffff;
  border-radius: 12rpx;
  padding: 20rpx 22rpx;
  min-height: 160rpx;
  font-size: 26rpx;
  line-height: 1.5;
}
.pricing-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.price-block {
  width: 100%;
}
.money-row {
  margin-top: 8rpx;
  position: relative;
  display: flex;
  align-items: center;
  background: #f3f3f3;
  border-radius: 16rpx;
  padding: 20rpx 24rpx 20rpx 56rpx;
}
.yen {
  position: absolute;
  left: 22rpx;
  font-weight: 900;
  color: #a33e00;
  font-size: 28rpx;
}
.money-input {
  flex: 1;
  font-size: 44rpx;
  font-weight: 900;
}
.side-block {
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 20rpx;
}
.rate-box,
.stat-box {
  flex: 1;
}
.rate-val {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  margin-top: 8rpx;
}
.rate-num {
  font-size: 40rpx;
  font-weight: 900;
  color: #a33e00;
}
.toggle-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  margin-top: 8rpx;
}
.stat-txt {
  font-size: 24rpx;
  font-weight: 800;
  text-transform: uppercase;
}
.submit-wrap {
  padding-top: 36rpx;
}
.submit {
  width: 100%;
  padding: 32rpx;
  border-radius: 16rpx;
  background: linear-gradient(90deg, #a33e00, #ff6600);
  color: #561d00;
  font-size: 30rpx;
  font-weight: 900;
  text-align: center;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  box-shadow: 0 16rpx 40rpx rgba(163, 62, 0, 0.22);
}
.submit.disabled {
  opacity: 0.6;
}
.spacer {
  height: 40rpx;
}
</style>
