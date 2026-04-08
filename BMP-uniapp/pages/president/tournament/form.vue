<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="t-form-page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-inner">
          <view class="top-left" @click="onBack">
            <view class="hit">
              <uni-icons type="arrow-left" size="22" color="#ea580c" />
            </view>
            <text class="title">配置赛事</text>
          </view>
          <image class="avatar" :src="adminAvatar" mode="aspectFill" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <!-- 基本信息 -->
        <view class="sec-head">
          <text class="sec-title">基本信息</text>
          <view class="sec-line" />
        </view>

        <view class="field">
          <text class="label orange">赛事名称</text>
          <input class="input" v-model="form.tournamentName" placeholder="例如：2024 夏季精英羽毛球赛" maxlength="100" />
        </view>

        <view class="field">
          <text class="label orange">赛制类型</text>
          <picker mode="selector" :range="typeOptions" :value="typeIndex" @change="onTypeChange">
            <view class="input picker">
              <text>{{ typeOptions[typeIndex] }}</text>
              <uni-icons type="arrowdown" size="14" color="#a1a1aa" />
            </view>
          </picker>
        </view>

        <!-- 场馆与后勤 -->
        <view class="sec-head">
          <text class="sec-title">场馆与后勤</text>
          <view class="sec-line" />
        </view>

        <view class="field venue-field">
          <text class="label orange">选择场馆</text>
          <view class="input icon">
            <uni-icons type="location" size="16" color="#a1a1aa" />
            <input
              class="inner"
              v-model="venueKeyword"
              placeholder="搜索体育中心或球馆..."
              @input="onVenueInput"
              @focus="venueFocused = true"
              @blur="onVenueBlur"
              @confirm="searchVenue"
            />
            <view class="venue-actions">
              <view v-if="venueName" class="clear-btn" @click.stop="clearVenue">
                <uni-icons type="closeempty" size="14" color="#71717a" />
              </view>
              <uni-icons type="arrowdown" size="14" color="#a1a1aa" />
            </view>
          </view>
          <view v-if="venueName" class="picked">
            <text>已选择：{{ venueName }}</text>
            <text class="picked-id">#{{ form.venueId }}</text>
          </view>

          <view v-if="venueFocused" class="venue-dropdown">
            <view v-if="venueLoading" class="venue-skeleton">
              <view class="sk-row" v-for="i in 5" :key="i">
                <view class="sk-main">
                  <view class="sk-line l1" />
                  <view class="sk-line l2" />
                </view>
                <view class="sk-id" />
              </view>
            </view>

            <view v-else-if="venueKeyword.trim() && venueOptions.length === 0" class="venue-empty">
              <text>没有找到相关场馆</text>
            </view>

            <scroll-view v-else class="venue-list" scroll-y :show-scrollbar="false">
              <view class="venue-item" v-for="v in venueOptions" :key="v.id" @mousedown.prevent @click="pickVenue(v)">
                <view class="v-main">
                  <text class="v-name">{{ v.venueName }}</text>
                  <text class="v-addr">{{ v.address || '' }}</text>
                </view>
                <text class="v-id">#{{ v.id }}</text>
              </view>
            </scroll-view>
          </view>
        </view>

        <view class="field">
          <text class="label orange">报名费</text>
          <view class="input icon">
            <text class="yen">¥</text>
            <input class="inner" type="digit" v-model="form.entryFee" placeholder="0.00" />
            <text class="cny">CNY</text>
          </view>
        </view>

        <!-- 日程安排 -->
        <view class="sec-head">
          <text class="sec-title">日程安排</text>
          <view class="sec-line" />
        </view>

        <view class="date-card">
          <view class="date-head">
            <uni-icons type="calendar" size="16" color="#ea580c" />
            <text>报名时间</text>
          </view>
          <view class="date-grid">
            <view class="date-item">
              <text class="dk">开始</text>
              <picker mode="date" :value="form.registrationStartDate" @change="(e: any) => (form.registrationStartDate = e.detail.value)">
                <view class="date-input">{{ form.registrationStartDate || 'mm/dd/yyyy' }}</view>
              </picker>
            </view>
            <view class="date-item">
              <text class="dk">截止</text>
              <picker mode="date" :value="form.registrationEndDate" @change="(e: any) => (form.registrationEndDate = e.detail.value)">
                <view class="date-input">{{ form.registrationEndDate || 'mm/dd/yyyy' }}</view>
              </picker>
            </view>
          </view>
        </view>

        <view class="date-card">
          <view class="date-head">
            <uni-icons type="medal" size="16" color="#ea580c" />
            <text>比赛日期</text>
          </view>
          <view class="date-grid">
            <view class="date-item">
              <text class="dk">开始</text>
              <picker mode="date" :value="form.tournamentStartDate" @change="(e: any) => (form.tournamentStartDate = e.detail.value)">
                <view class="date-input">{{ form.tournamentStartDate || 'mm/dd/yyyy' }}</view>
              </picker>
            </view>
            <view class="date-item">
              <text class="dk">结束</text>
              <picker mode="date" :value="form.tournamentEndDate" @change="(e: any) => (form.tournamentEndDate = e.detail.value)">
                <view class="date-input">{{ form.tournamentEndDate || 'mm/dd/yyyy' }}</view>
              </picker>
            </view>
          </view>
        </view>

        <view class="field">
          <text class="label">参赛席位（最大人数）</text>
          <input class="input" type="number" v-model="form.maxParticipants" placeholder="例如：150" />
        </view>

        <view class="bottom-space" />
      </scroll-view>

      <view class="footer">
        <view class="btn ghost" @click="saveDraft">保存草稿</view>
        <view class="btn primary" :class="{ disabled: submitting }" @click="submitCreate">
          {{ isEdit ? '保存修改' : '创建赛事' }}
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'
import { addTournament, getTournamentDetail, updateTournament } from '@/api/tournament'
import { useUserStore } from '@/store/modules/user'
import { getVenueList, type VenueItem as PresidentVenueItem } from '@/api/president/venue'

const userStore = useUserStore()
const adminAvatar = computed(() => userStore.userInfo?.avatar || '/static/placeholders/avatar.svg')

const isEdit = ref(false)
const id = ref<number | null>(null)
const submitting = ref(false)

const typeOptions = ['单败淘汰制', '循环赛制', '双败淘汰制', '瑞士轮制']
const typeIndex = ref(0)

const venueKeyword = ref('')
const venueName = ref('')
const venueFocused = ref(false)
const venueOptions = ref<PresidentVenueItem[]>([])
const venueLoading = ref(false)
let venueTimer: number | undefined

const form = reactive({
  tournamentName: '',
  tournamentType: typeOptions[0],
  venueId: 0,
  entryFee: '0.00',
  maxParticipants: '150',
  registrationStartDate: '',
  registrationEndDate: '',
  tournamentStartDate: '',
  tournamentEndDate: ''
})

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.TOURNAMENT_LIST)
}

function onTypeChange(e: { detail: { value: string } }) {
  typeIndex.value = Number(e.detail.value)
  form.tournamentType = typeOptions[typeIndex.value]
}

function onVenueInput() {
  venueFocused.value = true
  if (venueTimer) clearTimeout(venueTimer)
  venueTimer = setTimeout(() => {
    searchVenue()
  }, 260) as unknown as number
}

function onVenueBlur() {
  // 给点击候选项留时间
  setTimeout(() => {
    venueFocused.value = false
  }, 150)
}

async function searchVenue() {
  const kw = venueKeyword.value.trim()
  if (!kw) {
    venueOptions.value = []
    return
  }
  try {
    venueLoading.value = true
    const res = await getVenueList({ venueName: kw, page: 1, size: 10, status: 1 })
    venueOptions.value = res.data || []
  } catch {
    venueOptions.value = []
  } finally {
    venueLoading.value = false
  }
}

function pickVenue(v: PresidentVenueItem) {
  form.venueId = Number(v.id)
  venueName.value = v.venueName
  venueKeyword.value = v.venueName
  venueOptions.value = []
  venueFocused.value = false
}

function clearVenue() {
  form.venueId = 0
  venueName.value = ''
  venueKeyword.value = ''
  venueOptions.value = []
  venueFocused.value = false
}

function toDateTime(date: string, endOfDay = false) {
  if (!date) return ''
  return `${date} ${endOfDay ? '23:59:59' : '00:00:00'}`
}

function validate() {
  if (!form.tournamentName.trim()) return '请填写赛事名称'
  if (!form.tournamentType.trim()) return '请选择赛制类型'
  if (!form.venueId) return '请选择场馆'
  if (!form.registrationStartDate || !form.registrationEndDate) return '请选择报名时间'
  if (!form.tournamentStartDate || !form.tournamentEndDate) return '请选择比赛日期'
  const max = Number(form.maxParticipants)
  if (!Number.isFinite(max) || max < 2) return '参赛席位最少为 2'
  const fee = Number(form.entryFee)
  if (!Number.isFinite(fee) || fee < 0) return '报名费不能为空且不能为负数'
  return ''
}

async function saveDraft() {
  uni.showToast({ title: '草稿已保存（前端占位）', icon: 'none' })
}

async function submitCreate() {
  if (submitting.value) return
  const err = validate()
  if (err) {
    uni.showToast({ title: err, icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const payload = {
      id: id.value ?? undefined,
      tournamentName: form.tournamentName.trim(),
      tournamentType: form.tournamentType.trim(),
      venueId: Number(form.venueId),
      maxParticipants: Number(form.maxParticipants),
      registrationStart: toDateTime(form.registrationStartDate, false),
      registrationEnd: toDateTime(form.registrationEndDate, true),
      tournamentStart: toDateTime(form.tournamentStartDate, false),
      tournamentEnd: toDateTime(form.tournamentEndDate, true),
      entryFee: Number(form.entryFee),
      status: isEdit.value ? undefined : 1
    }

    if (isEdit.value) {
      await updateTournament(payload)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } else {
      const res = await addTournament(payload)
      uni.showToast({ title: '创建成功', icon: 'success' })
      if (res?.id) {
        setTimeout(() => uni.navigateTo({ url: `${PRESIDENT_PAGES.TOURNAMENT_DETAIL}?tournamentId=${res.id}` }), 400)
        return
      }
    }
    setTimeout(() => onBack(), 500)
  } catch {
    // request 已统一toast
  } finally {
    submitting.value = false
  }
}

onLoad(async (q?: Record<string, string | undefined>) => {
  const raw = q?.id
  const n = raw ? parseInt(String(raw), 10) : NaN
  if (Number.isFinite(n) && n > 0) {
    isEdit.value = true
    id.value = n
    try {
      const info = await getTournamentDetail(n)
      form.tournamentName = info.tournamentName || ''
      form.tournamentType = info.tournamentType || typeOptions[0]
      const idx = typeOptions.findIndex((x) => x === form.tournamentType)
      typeIndex.value = idx >= 0 ? idx : 0
      form.venueId = Number(info.venueId || 0)
      venueName.value = info.venueName || ''
      venueKeyword.value = venueName.value
      form.entryFee = String(info.entryFee ?? '0.00')
      form.maxParticipants = String(info.maxParticipants ?? '150')
      form.registrationStartDate = (info.registrationStart || '').slice(0, 10)
      form.registrationEndDate = (info.registrationEnd || '').slice(0, 10)
      form.tournamentStartDate = (info.tournamentStart || '').slice(0, 10)
      form.tournamentEndDate = (info.tournamentEnd || '').slice(0, 10)
    } catch {
      // ignore
    }
  }
})
</script>

<style lang="scss" scoped>
.t-form-page { min-height: 100vh; background: #f9f9f9; color: #1a1c1c; padding-bottom: 140rpx; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { position: sticky; top: 0; z-index: 50; background: rgba(248,250,252,.92); backdrop-filter: blur(14px); }
.top-inner { height: 96rpx; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; }
.top-left { display: flex; align-items: center; gap: 12rpx; }
.hit { width: 56rpx; height: 56rpx; border-radius: 9999px; display: flex; align-items: center; justify-content: center; background: #fff; }
.title { font-size: 34rpx; font-weight: 900; }
.avatar { width: 56rpx; height: 56rpx; border-radius: 9999px; background: #ffedd5; }
.scroll { height: calc(100vh - var(--status-bar-height) - 200rpx); padding: 18rpx 24rpx 0; box-sizing: border-box; }
.sec-head { display: flex; align-items: center; gap: 12rpx; margin: 16rpx 0 16rpx; }
.sec-title { font-size: 22rpx; font-weight: 800; color: #71717a; letter-spacing: .12em; text-transform: uppercase; }
.sec-line { height: 2rpx; flex: 1; background: rgba(255,102,0,.18); }
.field { display: flex; flex-direction: column; gap: 10rpx; margin-bottom: 14rpx; }
.label { font-size: 18rpx; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; color: #71717a; margin-left: 6rpx; }
.label.orange { color: #ea580c; }
.input { background: #fff; border-radius: 16rpx; min-height: 96rpx; padding: 0 18rpx; display: flex; align-items: center; font-size: 26rpx; font-weight: 700; }
.input.picker { justify-content: space-between; }
.input.icon { gap: 10rpx; }
.inner { flex: 1; font-size: 26rpx; font-weight: 700; }
.yen { color: #a1a1aa; font-weight: 900; }
.cny { font-size: 16rpx; color: #a1a1aa; font-weight: 900; }
.picked { margin-top: 8rpx; background: #f3f3f3; padding: 10rpx 14rpx; border-radius: 14rpx; display: flex; justify-content: space-between; color: #5f5e5e; font-weight: 700; }
.picked-id { color: #a33e00; font-weight: 900; }

.venue-field { position: relative; }
.venue-dropdown {
  margin-top: 10rpx;
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 12rpx 28rpx rgba(2, 6, 23, 0.10);
  overflow: hidden;
  border: 1rpx solid rgba(0, 0, 0, 0.04);
}

.venue-actions { display: flex; align-items: center; gap: 10rpx; }
.clear-btn {
  width: 34rpx;
  height: 34rpx;
  border-radius: 9999px;
  background: #f3f3f3;
  display: flex;
  align-items: center;
  justify-content: center;
}

.venue-list {
  max-height: 360rpx;
}

.venue-item {
  padding: 16rpx 14rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  &:active { background: #f8fafc; }
}

.v-main { min-width: 0; display: flex; flex-direction: column; gap: 4rpx; }
.v-name { font-size: 24rpx; font-weight: 900; color: #1a1c1c; }
.v-addr { font-size: 18rpx; color: #71717a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 520rpx; }
.v-id { font-size: 18rpx; color: #a33e00; font-weight: 900; }

.venue-empty {
  padding: 22rpx 16rpx;
  text-align: center;
  color: #a1a1aa;
  font-size: 22rpx;
  font-weight: 700;
}

.venue-skeleton { padding: 12rpx 12rpx; }
.sk-row { padding: 14rpx 8rpx; display: flex; align-items: center; justify-content: space-between; }
.sk-main { display: flex; flex-direction: column; gap: 10rpx; }
.sk-line { background: #f3f3f3; border-radius: 9999px; overflow: hidden; }
.sk-line.l1 { width: 360rpx; height: 18rpx; }
.sk-line.l2 { width: 520rpx; height: 14rpx; }
.sk-id { width: 80rpx; height: 18rpx; background: #f3f3f3; border-radius: 9999px; }
.date-card { background: #fff; border-radius: 22rpx; padding: 20rpx; margin-bottom: 12rpx; box-shadow: 0 6rpx 20rpx rgba(2,6,23,.04); }
.date-head { display: flex; align-items: center; gap: 8rpx; font-size: 16rpx; font-weight: 900; color: #a1a1aa; letter-spacing: .12em; text-transform: uppercase; margin-bottom: 12rpx; }
.date-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12rpx; }
.date-item { display: flex; flex-direction: column; gap: 8rpx; }
.dk { font-size: 14rpx; color: #a1a1aa; font-weight: 900; letter-spacing: .12em; text-transform: uppercase; }
.date-input { background: #f3f3f3; border-radius: 12rpx; padding: 16rpx 12rpx; font-weight: 800; color: #1a1c1c; }
.bottom-space { height: 40rpx; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; z-index: 60; padding: 24rpx 24rpx calc(24rpx + env(safe-area-inset-bottom)); background: rgba(255,255,255,.72); backdrop-filter: blur(20px); display: flex; gap: 12rpx; }
.btn { height: 96rpx; border-radius: 18rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; font-weight: 900; }
.btn.ghost { flex: 1; background: #e5e7eb; color: #1a1c1c; }
.btn.primary { flex: 1.5; background: #ff6600; color: #561d00; box-shadow: 0 16rpx 40rpx rgba(234,88,12,.22); }
.btn.primary.disabled { opacity: .6; pointer-events: none; }
</style>

