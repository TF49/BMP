<template>
  <view
    class="rental-card"
    :class="[{ 'is-returned': status === 'returned' }, `stripe-${status}`]"
    @click="emit('click')"
  >
    <view class="stripe" />
    <view class="inner">
      <view class="col member">
        <image v-if="avatarUrl" class="avatar-img" :src="avatarUrl" mode="aspectFill" />
        <view v-else class="avatar-initials" :class="`avatar-tone-${status}`">
          <text class="initials-text">{{ initials }}</text>
        </view>
        <view class="member-meta">
          <text class="member-name">{{ memberName }}</text>
          <text class="member-id">ID: {{ memberId }}</text>
        </view>
      </view>

      <view class="col equip">
        <text class="equip-name">{{ equipmentName }}</text>
        <text class="equip-tag">标签: {{ tag }}</text>
      </view>

      <view class="col period">
        <text class="period-range">{{ dateRange }}</text>
        <text class="period-sub" :class="subTextTone === 'error' ? 'text-error' : 'text-muted'">{{ subText }}</text>
      </view>

      <view class="col actions">
        <view class="status-pill" :class="`pill-${status}`">
          <text class="pill-text">{{ statusLabel }}</text>
        </view>
        <view class="more-hit" @click.stop="emit('more')">
          <uni-icons type="more-filled" size="22" color="#5f5e5e" />
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export type RentalListStatus = 'overdue' | 'on_rent' | 'returned'

const props = defineProps<{
  memberName: string
  memberId: string
  initials: string
  avatarUrl?: string
  equipmentName: string
  tag: string
  dateRange: string
  subText: string
  subTextTone?: 'error' | 'muted'
  status: RentalListStatus
}>()

const emit = defineEmits<{
  (e: 'click'): void
  (e: 'more'): void
}>()

const statusLabel = computed(() => {
  if (props.status === 'overdue') return '已逾期'
  if (props.status === 'on_rent') return '租借中'
  return '已归还'
})
</script>

<style lang="scss" scoped>
.rental-card {
  position: relative;
  background: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(26, 28, 28, 0.05);
}
.rental-card.is-returned {
  opacity: 0.82;
}
.stripe {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 6rpx;
}
.stripe-overdue .stripe {
  background: #ba1a1a;
}
.stripe-on_rent .stripe {
  background: #ff6600;
}
.stripe-returned .stripe {
  background: #e2dfde;
}
.inner {
  padding: 40rpx 36rpx 40rpx 28rpx;
  display: flex;
  flex-direction: column;
  gap: 28rpx;
}
.col {
  width: 100%;
}
.member {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24rpx;
}
.avatar-img {
  width: 96rpx;
  height: 96rpx;
  border-radius: 9999px;
}
.avatar-initials {
  width: 96rpx;
  height: 96rpx;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-tone-overdue {
  background: #e2dfde;
}
.avatar-tone-on_rent {
  background: rgba(255, 102, 0, 0.15);
}
.avatar-tone-returned {
  background: #e2e2e2;
}
.initials-text {
  font-size: 28rpx;
  font-weight: 800;
  color: #5f5e5e;
}
.stripe-on_rent .initials-text {
  color: #a33e00;
}
.member-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.member-name {
  font-size: 30rpx;
  font-weight: 800;
  color: #1a1c1c;
}
.member-id {
  font-size: 22rpx;
  color: #5f5e5e;
}
.equip-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
}
.equip-tag {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #5f5e5e;
}
.period-range {
  font-size: 28rpx;
  color: #1a1c1c;
}
.period-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
}
.text-error {
  color: #ba1a1a;
  font-weight: 700;
  font-style: italic;
}
.text-muted {
  color: #5f5e5e;
}
.actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}
.status-pill {
  padding: 10rpx 22rpx;
  border-radius: 9999px;
}
.pill-overdue {
  background: #ffdad6;
}
.pill-overdue .pill-text {
  color: #93000a;
}
.pill-on_rent {
  background: rgba(255, 102, 0, 0.12);
}
.pill-on_rent .pill-text {
  color: #a33e00;
}
.pill-returned {
  background: #e2dfde;
}
.pill-returned .pill-text {
  color: #636262;
}
.pill-text {
  font-size: 20rpx;
  font-weight: 800;
  letter-spacing: 0.08em;
}
.more-hit {
  padding: 8rpx;
}
</style>
