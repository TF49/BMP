<template>
  <PresidentLayout :showTabBar="false" backgroundColor="#f9f9f9">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-bar-left">
          <view class="icon-round" @click="onBack">
            <uni-icons type="arrow-left" size="24" color="#ff6600" />
          </view>
          <text class="top-title">管理</text>
        </view>
        <view class="icon-round">
          <uni-icons type="search" size="22" color="#ff6600" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="content">
          <view class="hero-row">
            <view class="hero-text">
              <text class="hero-heading">{{ pageHeading }}</text>
              <text class="hero-sub">{{ form.venueSubtitle }}</text>
            </view>
            <view v-if="isEdit" class="status-pill">
              <text class="status-pill-text">{{ form.statusLabel }}</text>
            </view>
          </view>

          <view class="form-shell">
            <!-- 基础信息 -->
            <view class="section">
              <view class="section-head">
                <view class="section-ico">
                  <uni-icons type="info" size="18" color="#a33e00" />
                </view>
                <text class="section-title">基础信息</text>
              </view>
              <view class="field-grid">
                <view class="field">
                  <text class="field-label">场地编号</text>
                  <input class="field-input bold" v-model="form.courtNumber" placeholder="例如 01" />
                </view>
                <view class="field">
                  <text class="field-label">场地等级</text>
                  <picker mode="selector" :range="levelOptions" :value="form.levelIndex" @change="onLevelChange">
                    <view class="picker-display">
                      <text class="picker-text">{{ levelOptions[form.levelIndex] }}</text>
                      <uni-icons type="bottom" size="14" color="#5f5e5e" />
                    </view>
                  </picker>
                </view>
              </view>
            </view>

            <!-- 价格规则 -->
            <view class="section">
              <view class="section-head">
                <view class="section-ico">
                  <uni-icons type="wallet" size="18" color="#a33e00" />
                </view>
                <text class="section-title">价格规则</text>
              </view>
              <view class="price-layout">
                <view class="price-base">
                  <text class="field-label">基础价格</text>
                  <view class="price-row">
                    <text class="currency">$</text>
                    <input class="price-input" type="digit" v-model="form.basePrice" />
                  </view>
                  <text class="price-hint">每 60 分钟</text>
                </view>
                <view class="toggle-list">
                  <view class="toggle-row" :class="{ dim: !form.peakEnabled }">
                    <view class="toggle-info">
                      <text class="toggle-title">高峰时段加价</text>
                      <text class="toggle-sub">周一至周五 18:00 - 22:00</text>
                    </view>
                    <view class="toggle-right">
                      <text class="toggle-extra primary">+25%</text>
                      <view class="switch" :class="{ on: form.peakEnabled }" @click="form.peakEnabled = !form.peakEnabled">
                        <view class="switch-knob" />
                      </view>
                    </view>
                  </view>
                  <view class="toggle-row" :class="{ dim: !form.weekendEnabled }">
                    <view class="toggle-info">
                      <text class="toggle-title">周末附加费</text>
                      <text class="toggle-sub">周六、周日全天</text>
                    </view>
                    <view class="toggle-right">
                      <text class="toggle-extra muted">+$5.00</text>
                      <view class="switch" :class="{ on: form.weekendEnabled }" @click="form.weekendEnabled = !form.weekendEnabled">
                        <view class="switch-knob" />
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>

            <!-- 维护与状态 -->
            <view class="section">
              <view class="section-head">
                <view class="section-ico">
                  <uni-icons type="gear" size="18" color="#a33e00" />
                </view>
                <text class="section-title">维护与状态</text>
              </view>
              <view class="maint-grid">
                <view class="maint-card">
                  <view class="maint-card-head">
                    <text class="field-label">当前状况</text>
                    <view class="cond-badge">
                      <text class="cond-badge-text">{{ form.conditionLabel }}</text>
                    </view>
                  </view>
                  <view class="progress-track">
                    <view class="progress-fill" :style="{ width: form.conditionPct + '%' }" />
                  </view>
                  <text class="maint-note">{{ form.conditionNote }}</text>
                </view>
                <view class="maint-side">
                  <text class="field-label pad-x">计划停机时间</text>
                  <view class="downtime-field">
                    <uni-icons class="cal-ico" type="calendar" size="18" color="#5f5e5e" />
                    <input class="downtime-input" v-model="form.downtimeText" />
                  </view>
                  <view class="next-task">
                    <uni-icons type="flag" size="16" color="#a33e00" />
                    <text class="next-task-text">下次：{{ form.nextTask }}</text>
                  </view>
                </view>
              </view>
            </view>

            <view class="actions">
              <button class="btn-save" @click="onSubmit">
                <text class="btn-save-text">{{ isEdit ? '保存修改' : '确认添加' }}</text>
              </button>
              <button v-if="isEdit" class="btn-archive" @click="onArchive">
                <text class="btn-archive-text">归档场地</text>
              </button>
            </view>
          </view>

          <!-- 预览 -->
          <view class="preview-card">
            <view class="preview-img-wrap">
              <image class="preview-img" :src="preview.imageUrl" mode="aspectFill" />
              <view class="preview-img-overlay">
                <text class="preview-img-title">{{ previewTitle }}</text>
              </view>
            </view>
            <view class="preview-body">
              <view class="occ-row">
                <view class="avatar-stack">
                  <image
                    v-for="(u, i) in preview.avatars"
                    :key="i"
                    class="avatar"
                    :src="u"
                    mode="aspectFill"
                  />
                  <view class="avatar more">
                    <text class="more-text">+12</text>
                  </view>
                </view>
                <text class="occ-label">占用率: {{ preview.occupancyPct }}%</text>
              </view>
              <view class="divider" />
              <view class="meta-rows">
                <view class="meta-row">
                  <text class="meta-k">场馆</text>
                  <text class="meta-v">{{ preview.venueName }}</text>
                </view>
                <view class="meta-row">
                  <text class="meta-k">区域</text>
                  <text class="meta-v">{{ preview.areaLabel }}</text>
                </view>
                <view class="meta-row">
                  <text class="meta-k">累计预约</text>
                  <text class="meta-v">{{ preview.totalBookings }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 修改历史 -->
          <view v-if="isEdit" class="history-card">
            <text class="history-title">修改历史</text>
            <view class="timeline">
              <view v-for="(h, i) in history" :key="i" class="timeline-item" :class="{ faded: h.faded }">
                <view class="timeline-dot" :class="h.dotClass" />
                <view class="timeline-body">
                  <text class="timeline-main">{{ h.title }}</text>
                  <text class="timeline-sub">{{ h.meta }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="bottom-space" />
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'
import { computed, reactive, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const routeId = ref<string | undefined>()
const isEdit = computed(() => Boolean(routeId.value))

const levelOptions = ['普通级', 'VIP / 专业级', '青少年训练']

const form = reactive({
  courtNumber: '04',
  venueSubtitle: '中央羽毛球馆 • 西翼区域',
  statusLabel: '运营中',
  levelIndex: 1,
  basePrice: '45.00',
  peakEnabled: true,
  weekendEnabled: false,
  conditionLabel: '优良',
  conditionPct: 92,
  conditionNote: '人造地板 3 个月前已更换。保持定期的清洁计划。',
  downtimeText: '2023年10月12日 - 08:00 AM',
  nextTask: '更换灯具'
})

const preview = reactive({
  imageUrl:
    '/static/placeholders/hero.svg',
  avatars: [
    '/static/placeholders/hero.svg',
    '/static/placeholders/hero.svg'
  ],
  occupancyPct: 85,
  venueName: '中央场馆',
  areaLabel: '西翼 (B1)',
  totalBookings: '1,248'
})

const history = [
  { title: '价格已更新', meta: '10月01日 • 管理员 Smith', dotClass: 'dot-primary', faded: false },
  { title: '状态：维护中', meta: '9月24日 • 自动化系统', dotClass: 'dot-secondary', faded: false },
  { title: '场地等级变更为 VIP', meta: '8月15日 • 管理员 Smith', dotClass: 'dot-secondary', faded: true }
]

const pageHeading = computed(() => {
  if (isEdit.value) return `编辑 ${form.courtNumber || '—'} 号场地`
  return '新增场地'
})

const previewTitle = computed(() => {
  const n = form.courtNumber.trim()
  if (n) return `${n} 号场地预览`
  return '场地预览'
})

onLoad((options) => {
  routeId.value = options?.id
  if (!options?.id) {
    form.courtNumber = ''
    form.venueSubtitle = '请填写场馆、分区与场地信息'
    form.statusLabel = '运营中'
    form.levelIndex = 0
    form.basePrice = ''
    form.peakEnabled = false
    form.weekendEnabled = false
    form.conditionLabel = '优良'
    form.conditionPct = 100
    form.conditionNote = ''
    form.downtimeText = ''
    form.nextTask = ''
    preview.occupancyPct = 0
    preview.totalBookings = '0'
    preview.venueName = '—'
    preview.areaLabel = '—'
  }
})

function onLevelChange(e: { detail: { value: string } }) {
  form.levelIndex = Number(e.detail.value)
}

function onBack() {
  safeNavigateBack(PRESIDENT_PAGES.COURT_LIST)
}

function onSubmit() {
  uni.showToast({ title: isEdit.value ? '已保存修改' : '已添加', icon: 'success' })
  setTimeout(() => onBack(), 600)
}

function onArchive() {
  uni.showModal({
    title: '归档场地',
    content: '确定要归档该场地吗？归档后可能影响预约与排期。',
    success(res) {
      if (res.confirm) {
        uni.showToast({ title: '已归档（示例）', icon: 'none' })
        setTimeout(() => onBack(), 500)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  color: #1a1c1c;
}
.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}
.top-bar {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 28rpx 20rpx;
  background: #f9f9f9;
  position: sticky;
  top: 0;
  z-index: 30;
}
.top-bar-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
}
.icon-round {
  width: 72rpx;
  height: 72rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.icon-round:active {
  background: rgba(0, 0, 0, 0.05);
}
.top-title {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}
.scroll {
  flex: 1;
  height: 0;
}
.content {
  padding: 8rpx 28rpx 48rpx;
  max-width: 1600rpx;
  margin: 0 auto;
}

.hero-row {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
  margin-bottom: 32rpx;
}
.hero-text {
  flex: 1;
  min-width: 0;
}
.hero-heading {
  display: block;
  font-size: 56rpx;
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: -0.03em;
}
.hero-sub {
  display: block;
  margin-top: 16rpx;
  font-size: 26rpx;
  color: #5f5e5e;
}
.status-pill {
  flex-shrink: 0;
  padding: 12rpx 24rpx;
  border-radius: 9999px;
  background: #d0e4ff;
}
.status-pill-text {
  font-size: 22rpx;
  font-weight: 800;
  color: #00497b;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.form-shell {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx 36rpx;
  box-shadow: 0 4rpx 20rpx rgba(26, 28, 28, 0.06);
}
.section {
  margin-bottom: 56rpx;
}
.section:last-of-type {
  margin-bottom: 40rpx;
}
.section-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 32rpx;
}
.section-ico {
  width: 64rpx;
  height: 64rpx;
  border-radius: 9999px;
  background: rgba(255, 102, 0, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
}
.section-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.field-grid {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}
.field-label {
  display: block;
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 12rpx;
}
.field-label.pad-x {
  padding-left: 4rpx;
}
.field-input {
  width: 100%;
  padding: 28rpx 24rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
  font-size: 36rpx;
  color: #1a1c1c;
  border: none;
  box-sizing: border-box;
}
.field-input.bold {
  font-weight: 800;
}
.picker-display {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 24rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
}
.picker-text {
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.price-layout {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}
.price-base {
  background: #f3f3f3;
  border-radius: 16rpx;
  padding: 36rpx;
}
.price-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8rpx;
  margin-top: 16rpx;
}
.currency {
  font-size: 40rpx;
  font-weight: 800;
  color: #a33e00;
}
.price-input {
  flex: 1;
  font-size: 56rpx;
  font-weight: 900;
  color: #1a1c1c;
  background: transparent;
  border: none;
  padding: 0;
}
.price-hint {
  display: block;
  margin-top: 16rpx;
  font-size: 22rpx;
  color: #5f5e5e;
  opacity: 0.65;
}

.toggle-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.toggle-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx;
  background: #e8e8e8;
  border-radius: 16rpx;
  gap: 20rpx;
}
.toggle-row.dim {
  opacity: 0.72;
}
.toggle-title {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.toggle-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #5f5e5e;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.toggle-right {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
}
.toggle-extra {
  font-size: 28rpx;
  font-weight: 800;
}
.toggle-extra.primary {
  color: #a33e00;
}
.toggle-extra.muted {
  color: #5f5e5e;
}

.switch {
  width: 80rpx;
  height: 44rpx;
  border-radius: 9999px;
  background: #e2dfde;
  position: relative;
  transition: background 0.2s;
}
.switch.on {
  background: #ff6600;
}
.switch-knob {
  position: absolute;
  top: 6rpx;
  left: 6rpx;
  width: 32rpx;
  height: 32rpx;
  border-radius: 9999px;
  background: #ffffff;
  transition: transform 0.2s;
}
.switch.on .switch-knob {
  transform: translateX(36rpx);
}

.maint-grid {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}
.maint-card {
  background: #f3f3f3;
  border-radius: 16rpx;
  padding: 36rpx;
}
.maint-card-head {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.maint-card-head .field-label {
  margin-bottom: 0;
}
.cond-badge {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  background: rgba(0, 156, 252, 0.15);
}
.cond-badge-text {
  font-size: 20rpx;
  font-weight: 800;
  color: #003155;
}
.progress-track {
  height: 8rpx;
  background: #e8e8e8;
  border-radius: 9999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: #ff6600;
  border-radius: 9999px;
}
.maint-note {
  display: block;
  margin-top: 20rpx;
  font-size: 24rpx;
  color: #5f5e5e;
  line-height: 1.5;
}
.maint-side .field-label {
  margin-bottom: 12rpx;
}
.downtime-field {
  position: relative;
  background: #f3f3f3;
  border-radius: 16rpx;
}
.cal-ico {
  position: absolute;
  left: 24rpx;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.45;
  z-index: 1;
}
.downtime-input {
  width: 100%;
  padding: 28rpx 24rpx 28rpx 72rpx;
  font-size: 26rpx;
  font-weight: 700;
  background: transparent;
  border: none;
  box-sizing: border-box;
}
.next-task {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 10rpx;
  margin-top: 20rpx;
  padding-left: 4rpx;
}
.next-task-text {
  font-size: 20rpx;
  font-weight: 800;
  color: #a33e00;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding-top: 16rpx;
}
.btn-save {
  margin: 0;
  height: 100rpx;
  line-height: 100rpx;
  background: #ff6600;
  border-radius: 16rpx;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(255, 102, 0, 0.25);
}
.btn-save::after {
  border: none;
}
.btn-save-text {
  font-size: 28rpx;
  font-weight: 900;
  color: #561d00;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}
.btn-archive {
  margin: 0;
  height: 100rpx;
  line-height: 100rpx;
  background: #e8e8e8;
  border-radius: 16rpx;
  border: none;
}
.btn-archive::after {
  border: none;
}
.btn-archive-text {
  font-size: 28rpx;
  font-weight: 900;
  color: #1a1c1c;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.preview-card {
  margin-top: 40rpx;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(26, 28, 28, 0.06);
}
.preview-img-wrap {
  position: relative;
  height: 320rpx;
}
.preview-img {
  width: 100%;
  height: 100%;
}
.preview-img-overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  top: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.55), transparent);
  justify-content: flex-end;
  padding: 36rpx;
  display: flex;
  flex-direction: column;
}
.preview-img-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #ffffff;
}
.preview-body {
  padding: 36rpx;
}
.occ-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.avatar-stack {
  display: flex;
  flex-direction: row;
  align-items: center;
}
.avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 9999px;
  border: 4rpx solid #ffffff;
  margin-left: -20rpx;
}
.avatar:first-child {
  margin-left: 0;
}
.avatar.more {
  background: #5f5e5e;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -20rpx;
}
.more-text {
  font-size: 20rpx;
  font-weight: 800;
  color: #ffffff;
}
.occ-label {
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}
.divider {
  height: 1rpx;
  background: #e8e8e8;
  margin: 28rpx 0;
}
.meta-rows {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.meta-row {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}
.meta-k {
  font-size: 28rpx;
  color: #5f5e5e;
}
.meta-v {
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.history-card {
  margin-top: 40rpx;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 36rpx;
  box-shadow: 0 4rpx 20rpx rgba(26, 28, 28, 0.06);
}
.history-title {
  font-size: 20rpx;
  font-weight: 800;
  color: #5f5e5e;
  letter-spacing: 0.15em;
  text-transform: uppercase;
}
.timeline {
  margin-top: 28rpx;
  margin-left: 8rpx;
  padding-left: 28rpx;
  border-left: 4rpx solid #e8e8e8;
}
.timeline-item {
  position: relative;
  padding-bottom: 40rpx;
}
.timeline-item:last-child {
  padding-bottom: 0;
}
.timeline-item.faded {
  opacity: 0.6;
}
.timeline-dot {
  position: absolute;
  left: -22rpx;
  top: 6rpx;
  width: 20rpx;
  height: 20rpx;
  border-radius: 9999px;
  border: 4rpx solid #ffffff;
}
.dot-primary {
  background: #a33e00;
}
.dot-secondary {
  background: #5f5e5e;
}
.timeline-main {
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.timeline-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #5f5e5e;
}

.bottom-space {
  height: 48rpx;
}

@media screen and (min-width: 768px) {
  .field-grid {
    flex-direction: row;
    gap: 28rpx;
  }
  .field-grid .field {
    flex: 1;
  }
  .price-layout {
    flex-direction: row;
    align-items: stretch;
  }
  .price-base {
    flex: 1;
    min-width: 240rpx;
  }
  .toggle-list {
    flex: 2;
  }
  .maint-grid {
    flex-direction: row;
    align-items: flex-start;
  }
  .maint-card {
    flex: 1;
  }
  .maint-side {
    flex: 1;
  }
  .actions {
    flex-direction: row;
  }
  .actions .btn-save,
  .actions .btn-archive {
    flex: 1;
  }
}
</style>
