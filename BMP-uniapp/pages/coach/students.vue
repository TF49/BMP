<template>
  <view class="coach-students-page">
    <scroll-view class="page-scroll" scroll-y :show-scrollbar="false">
      <view class="page-shell">
        <CoachTopBar
          :status-bar-height="statusBarHeight"
          :avatar="coach.avatar"
          brand="KINETIC LOGIC"
          action-icon="notification-filled"
          @action="handleNotice"
        />

        <view class="hero-head">
          <text class="hero-kicker">SESSION ATTENDANCE</text>
          <text class="hero-title">青少年基础训练班</text>
        </view>

        <view class="summary-card">
          <view class="summary-block">
            <text class="summary-label">DATE & TIME</text>
            <view class="summary-line">
              <uni-icons type="calendar" size="16" color="#a33e00" />
              <text>10月24日, 16:00 - 18:00</text>
            </view>
          </view>

          <view class="summary-divider" />

          <view class="summary-block">
            <text class="summary-label">COURT</text>
            <view class="summary-line">
              <uni-icons type="location" size="16" color="#a33e00" />
              <text>场地 3 & 4</text>
            </view>
          </view>
        </view>

        <view class="stats-grid">
          <view class="stat-card stat-card-primary">
            <text class="stat-label">TOTAL STUDENTS</text>
            <text class="stat-value stat-value-primary">12</text>
            <view class="stat-watermark">
              <uni-icons type="staff-filled" size="58" color="rgba(163,62,0,0.12)" />
            </view>
          </view>

          <view class="stat-card">
            <text class="stat-label">CHECKED IN</text>
            <text class="stat-value">8</text>
          </view>

          <view class="stat-card">
            <text class="stat-label">PENDING</text>
            <text class="stat-value stat-value-secondary">4</text>
          </view>

          <view class="stat-card stat-card-action" @tap="handleScanQr">
            <uni-icons type="scan" size="28" color="#a33e00" />
            <text class="scan-label">Scan QR</text>
          </view>
        </view>

        <view class="list-card">
          <view
            v-for="student in students"
            :key="student.id"
            class="student-item"
          >
            <view class="student-main">
              <view class="student-head">
                <view class="student-avatar-wrap" :class="{ initials: !student.avatar }">
                  <image
                    v-if="student.avatar"
                    class="student-avatar"
                    :src="student.avatar"
                    mode="aspectFill"
                  />
                  <text v-else class="student-initials">{{ student.initials }}</text>
                </view>

                <view class="student-copy">
                  <text class="student-name">{{ student.name }}</text>
                  <text class="student-id">ID: {{ student.memberId }}</text>
                </view>
              </view>

              <view class="student-level">{{ student.level }}</view>

              <view v-if="student.checkedIn" class="status-line status-line-success">
                <uni-icons type="checkbox-filled" size="16" color="#a33e00" />
                <text>Checked In</text>
              </view>

              <view v-else class="status-actions">
                <button class="checkin-btn" @tap.stop="handleCheckIn(student)">
                  <uni-icons type="fingerprint" size="16" color="#561d00" />
                  <text>Check In</text>
                </button>
              </view>
            </view>

            <view class="menu-btn" @tap="handleMore(student)">
              <uni-icons type="more-filled" size="18" color="#5f5e5e" />
            </view>
          </view>
        </view>

        <view class="bottom-space" />
      </view>
    </scroll-view>

    <CoachTabBar current="courses" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CoachTabBar from '@/components/coach/CoachTabBar.vue'
import CoachTopBar from '@/components/coach/CoachTopBar.vue'

type StudentItem = {
  id: number
  name: string
  memberId: string
  level: string
  checkedIn: boolean
  avatar?: string
  initials?: string
}

const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

const coach = ref({
  avatar: '/static/placeholders/avatar.svg'
})

const students = ref<StudentItem[]>([
  {
    id: 1,
    name: '林晓月 (Lin Xiaoyue)',
    memberId: 'KL-2023-042',
    level: 'U15 Foundation',
    checkedIn: true,
    avatar: '/static/placeholders/avatar.svg'
  },
  {
    id: 2,
    name: '张宇轩 (Zhang Yuxuan)',
    memberId: 'KL-2023-088',
    level: 'U15 Foundation',
    checkedIn: false,
    avatar: '/static/placeholders/avatar.svg'
  },
  {
    id: 3,
    name: '李思宇 (Li Siyu)',
    memberId: 'KL-2024-012',
    level: 'U12 Foundation',
    checkedIn: false,
    initials: '李'
  },
  {
    id: 4,
    name: '陈浩然 (Chen Haoran)',
    memberId: 'KL-2022-115',
    level: 'U15 Foundation',
    checkedIn: true,
    avatar: '/static/placeholders/avatar.svg'
  }
])

function handleNotice() {
  uni.showToast({
    title: '通知入口待接入',
    icon: 'none'
  })
}

function handleScanQr() {
  uni.showToast({
    title: '扫码签到待接入',
    icon: 'none'
  })
}

function handleCheckIn(student: StudentItem) {
  student.checkedIn = true
  uni.showToast({
    title: `${student.name.split(' ')[0]} 已签到`,
    icon: 'success'
  })
}

function handleMore(student: StudentItem) {
  uni.showActionSheet({
    itemList: ['查看资料', '标记缺勤'],
    success: () => {
      uni.showToast({
        title: `操作 ${student.name.split(' ')[0]}`,
        icon: 'none'
      })
    }
  })
}

</script>

<style lang="scss" scoped>
.coach-students-page {
  min-height: 100vh;
  background: #f9f9f9;
  color: #1a1c1c;
}

.page-scroll {
  min-height: 100vh;
}

.page-shell {
  padding: 0 18rpx;
}

.hero-head {
  margin-top: 46rpx;
  padding: 0 10rpx;
}

.hero-kicker {
  display: block;
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 3rpx;
  color: #5f5e5e;
}

.hero-title {
  display: block;
  margin-top: 14rpx;
  font-size: 66rpx;
  line-height: 1.08;
  font-weight: 900;
  letter-spacing: -2rpx;
}

.summary-card {
  margin-top: 28rpx;
  border-radius: 28rpx;
  background: #ffffff;
  padding: 28rpx 30rpx;
  display: grid;
  grid-template-columns: 1fr 2rpx 1fr;
  gap: 22rpx;
  box-shadow: 0 10rpx 28rpx rgba(26, 28, 28, 0.05);
}

.summary-block {
  min-width: 0;
}

.summary-label {
  display: block;
  font-size: 18px;
  letter-spacing: 3rpx;
  color: #474746;
}

.summary-line {
  margin-top: 16rpx;
  display: flex;
  align-items: flex-start;
  gap: 10rpx;
  font-size: 22px;
  line-height: 1.45;
  font-weight: 900;
}

.summary-divider {
  width: 2rpx;
  background: #e2e2e2;
}

.stats-grid {
  margin-top: 28rpx;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.stat-card {
  min-height: 172rpx;
  border-radius: 24rpx;
  background: #ffffff;
  padding: 24rpx 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(26, 28, 28, 0.04);
  position: relative;
  overflow: hidden;
}

.stat-card-primary {
  background: rgba(255, 219, 205, 0.22);
  border: 2rpx solid rgba(163, 62, 0, 0.18);
}

.stat-label {
  display: block;
  font-size: 17px;
  letter-spacing: 3rpx;
  color: #474746;
}

.stat-value {
  display: block;
  margin-top: 24rpx;
  font-size: 76rpx;
  line-height: 1;
  font-weight: 900;
}

.stat-value-primary {
  color: #a33e00;
}

.stat-value-secondary {
  color: #5f5e5e;
}

.stat-watermark {
  position: absolute;
  right: 12rpx;
  bottom: 10rpx;
}

.stat-card-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
}

.scan-label {
  font-size: 20px;
  font-weight: 700;
}

.list-card {
  margin-top: 28rpx;
  border-radius: 28rpx;
  background: #ffffff;
  padding: 18rpx 0;
  box-shadow: 0 12rpx 34rpx rgba(26, 28, 28, 0.06);
}

.student-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 26rpx 24rpx;
}

.student-main {
  flex: 1;
  min-width: 0;
}

.student-head {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.student-avatar-wrap {
  width: 76rpx;
  height: 76rpx;
  border-radius: 999rpx;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f3f3;
  border: 2rpx solid rgba(163, 62, 0, 0.18);
}

.student-avatar-wrap.initials {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #bfe2ff;
  border: none;
}

.student-avatar {
  width: 100%;
  height: 100%;
}

.student-initials {
  font-size: 34rpx;
  font-weight: 900;
  color: #00497b;
}

.student-copy {
  flex: 1;
  min-width: 0;
}

.student-name {
  display: block;
  font-size: 22px;
  line-height: 1.2;
  font-weight: 900;
}

.student-id {
  display: block;
  margin-top: 10rpx;
  font-size: 18px;
  color: #5f5e5e;
}

.student-level {
  display: inline-flex;
  align-items: center;
  min-height: 42rpx;
  margin-top: 22rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: #e2dfde;
  color: #636262;
  font-size: 16px;
}

.status-line {
  margin-top: 46rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 18px;
  font-weight: 900;
}

.status-line-success {
  color: #a33e00;
}

.status-actions {
  margin-top: 34rpx;
}

.checkin-btn {
  margin: 0;
  min-width: 250rpx;
  height: 74rpx;
  padding: 0 28rpx;
  border: none;
  border-radius: 999rpx;
  background: #ff6600;
  color: #561d00;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: 18px;
  font-weight: 900;
  box-shadow: 0 8rpx 20rpx rgba(255, 102, 0, 0.2);
}

.menu-btn {
  width: 74rpx;
  height: 74rpx;
  border-radius: 999rpx;
  background: #e2e2e2;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bottom-space {
  height: 170rpx;
}

</style>
