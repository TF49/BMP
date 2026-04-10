<template>
  <PresidentLayout :showTabBar="false" className="president-equipment-rental-form-layout">
    <view class="page">
      <view class="status-bar-placeholder" />

      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="nav-icon-btn">
              <uni-icons type="arrow-left" size="22" color="#1a1c1c" />
            </view>
            <text class="nav-title">手动录入租借</text>
          </view>
          <image class="nav-avatar" :src="operatorAvatar" mode="aspectFill" />
        </view>
      </view>

      <scroll-view scroll-y class="main-scroll" :show-scrollbar="false" enable-flex>
        <view class="scroll-inner">
          <!-- 会员检索 -->
          <view class="card section-card">
            <view class="section-head">
              <text class="section-label">会员检索</text>
              <uni-icons type="search" size="16" color="#a33e00" />
            </view>
            <input
              v-model="memberKeyword"
              class="search-input"
              type="text"
              placeholder="输入会员ID、手机或姓名"
              placeholder-class="ph"
            />
            <view class="member-pick">
              <image class="mem-avatar" :src="currentMember.avatar" mode="aspectFill" />
              <view class="mem-info">
                <text class="mem-name">{{ currentMember.name }}</text>
                <text class="mem-bal">余额: ¥{{ currentMember.balance }}</text>
              </view>
              <text class="mem-switch" @click="switchMember">切换</text>
            </view>
          </view>

          <!-- 器材选择 -->
          <view class="card section-card">
            <view class="section-head">
              <text class="section-label">器材选择</text>
              <uni-icons type="shop" size="16" color="#a33e00" />
            </view>
            <view class="equip-grid">
              <view
                v-for="eq in equipments"
                :key="eq.id"
                class="equip-cell"
                :class="{ selected: selectedEquipId === eq.id }"
                @click="selectedEquipId = eq.id"
              >
                <uni-icons :type="eq.icon" size="28" :color="selectedEquipId === eq.id ? '#a33e00' : '#5f5e5e'" />
                <text class="equip-name" :class="{ on: selectedEquipId === eq.id }">{{ eq.name }}</text>
                <text class="equip-price" :class="{ on: selectedEquipId === eq.id }">¥{{ eq.price }}/小时</text>
              </view>
            </view>
          </view>

          <!-- 数量 + 租金预估 -->
          <view class="row-2">
            <view class="card half-card">
              <text class="mini-label">数量</text>
              <view class="stepper">
                <view class="step-btn" @click="decQty">
                  <text class="step-sym">−</text>
                </view>
                <text class="qty-val">{{ quantity }}</text>
                <view class="step-btn" @click="incQty">
                  <text class="step-sym">+</text>
                </view>
              </view>
            </view>
            <view class="card half-card highlight-b">
              <text class="mini-label">租金预估 (¥)</text>
              <text class="estimate-val">{{ rentEstimate }}</text>
            </view>
          </view>

          <!-- 日期 -->
          <view class="card section-card">
            <text class="mini-label mb">租借日期与时段</text>
            <picker mode="date" :value="rentDate" @change="onDateChange">
              <view class="date-row">
                <uni-icons type="calendar" size="20" color="#5f5e5e" />
                <text class="date-text">{{ displayDate }}</text>
                <uni-icons type="calendar" size="20" color="#5f5e5e" />
              </view>
            </picker>
          </view>

          <!-- 支付方式 -->
          <view class="card section-card">
            <text class="mini-label mb">支付方式</text>
            <view class="pay-grid">
              <view
                v-for="p in payOptions"
                :key="p.key"
                class="pay-cell"
                :class="{ selected: payMethod === p.key }"
                @click="payMethod = p.key"
              >
                <uni-icons :type="p.icon" size="18" :color="payMethod === p.key ? '#a33e00' : '#5f5e5e'" />
                <text class="pay-lab" :class="{ on: payMethod === p.key }">{{ p.label }}</text>
              </view>
            </view>
          </view>

          <!-- 备注 -->
          <view class="card section-card">
            <text class="mini-label mb">备注说明</text>
            <textarea
              v-model="remarks"
              class="remarks"
              placeholder="输入其他备注信息..."
              placeholder-class="ph"
              :maxlength="500"
            />
          </view>

          <!-- 订单总额 -->
          <view class="card total-card">
            <uni-icons type="list" size="140" color="#a33e00" class="total-watermark" />
            <view class="total-row">
              <view>
                <text class="total-k">订单总额</text>
                <text class="total-num">¥{{ orderTotal }}</text>
              </view>
              <text class="total-sub">{{ quantity }}件 x 1小时</text>
            </view>
          </view>

          <view class="scroll-pad" />
        </view>
      </scroll-view>

      <view class="footer-actions">
        <button class="btn-cancel" @click="goBack">取消</button>
        <button class="btn-submit" @click="onConfirm">确认租借</button>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

type PayMethod = 'cash' | 'alipay' | 'wechat' | 'balance'

interface MemberPick {
  id: string
  name: string
  balance: string
  avatar: string
}

const operatorAvatar =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuC6UTWC-_KSKkFEO-vnzDrMug4JnqbxBd3pPmTrdUMQbRAVUlnXoll9E__4cN7qKEbJbtl7JjaSN32teg6TR45Ce1_WbALsuA8URhLdWzIYaCNflyzMix3YQ-nbzG3MyvwBjE5a2Zpr1FzMEYun7p7caBNSyMlE5GK6i7QhXVI2tkJobMtDll7CEGZgXPs-mb1Xz_1gycCoKo-RRC7jzIIE_yx10NbIVpGYyhz-yz9gONNaXMPpaRDMTjAdgr54KqDXrEc7JuggJ3ex'

const members: MemberPick[] = [
  {
    id: 'm1',
    name: '陈伟 (高级会员)',
    balance: '1,240.50',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuCprtdCCd4KabyWMUPQ5ycCtmyJIVSnMauKsPpDxLu6uzOspp67lE1xzQ-ClXj021BLkXBHkRR7FdwOmQM7fb1Ftc3bYNLu89nx7jw2NDoHcMRJxxWXH1EGZIuZTMIeMfRVhvYGsfcC9HOiEucY33ek5tIBO9IenosmPPwI52U0beVp5kPntUGoHULVxqGQ99keADyQM2XuLmtW1BLadhzR-y-TPp9tO_sLn8tN11VrwLGkRC5lzr6iObcPTaX6LdP0OBDT_K87b86o'
  },
  {
    id: 'm2',
    name: '林悦 (普通会员)',
    balance: '320.00',
    avatar: '/static/placeholders/avatar.svg'
  }
]

const memberKeyword = ref('')
const memberIndex = ref(0)
const currentMember = computed(() => members[memberIndex.value % members.length])

const equipments = [
  { id: 'racket', name: '专业球拍', price: 45, icon: 'shop' as const },
  { id: 'shoes', name: '专业球鞋', price: 20, icon: 'cart' as const }
]
const selectedEquipId = ref('racket')
const quantity = ref(1)
const rentDate = ref('2023-11-24')
const payMethod = ref<PayMethod>('balance')
const remarks = ref('')

const payOptions = [
  { key: 'cash' as const, label: '现金', icon: 'wallet' as const },
  { key: 'alipay' as const, label: '支付宝', icon: 'image' as const },
  { key: 'wechat' as const, label: '微信支付', icon: 'chatbubble' as const },
  { key: 'balance' as const, label: '余额支付', icon: 'contact' as const }
]

const hourlyRate = computed(() => {
  const eq = equipments.find((e) => e.id === selectedEquipId.value)
  return eq?.price ?? 45
})

const rentEstimate = computed(() => {
  const v = quantity.value * hourlyRate.value
  return v.toFixed(2)
})

const orderTotal = computed(() => rentEstimate.value)

const displayDate = computed(() => {
  const [y, m, d] = rentDate.value.split('-')
  if (!y) return rentDate.value
  return `${m}/${d}/${y}`
})

function onDateChange(e: { detail: { value: string } }) {
  rentDate.value = e.detail.value
}

function decQty() {
  if (quantity.value > 1) quantity.value -= 1
}

function incQty() {
  if (quantity.value < 99) quantity.value += 1
}

function switchMember() {
  memberIndex.value = (memberIndex.value + 1) % members.length
}

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_RENTAL_LIST)
}

function onConfirm() {
  // TODO: POST 手动录入租借
  uni.showToast({
    title: '租借已创建（示例）',
    icon: 'success'
  })
}
</script>

<style lang="scss" scoped>
.president-equipment-rental-form-layout {
  :deep(.president-layout-content) {
    padding-bottom: 0;
    display: flex;
    flex-direction: column;
    min-height: 100vh;
  }
}

.page {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f9f9f9;
  font-family: Lexend, 'PingFang SC', system-ui, sans-serif;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background: #f9f9f9;
}

.nav-header {
  flex-shrink: 0;
  background: #f9f9f9;
  padding: 20rpx 32rpx 16rpx;
  z-index: 50;
}

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.nav-icon-btn {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background: #e8e8e8;
  }
}

.nav-title {
  font-size: 36rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: -0.02em;
}

.nav-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  border: 4rpx solid #ff6600;
}

.main-scroll {
  flex: 1;
  height: 0;
  min-height: 200rpx;
}

.scroll-inner {
  padding: 16rpx 32rpx 32rpx;
  padding-bottom: 200rpx;
}

.card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 36rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-label {
  font-size: 22rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.search-input {
  width: 100%;
  height: 88rpx;
  padding: 0 28rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1a1c1c;
  box-sizing: border-box;
}

.ph {
  color: rgba(95, 94, 94, 0.45);
  font-size: 26rpx;
}

.member-pick {
  margin-top: 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx;
  background: #e8e8e8;
  border-radius: 16rpx;
  border-left: 8rpx solid #ff6600;
}

.mem-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #ddd;
}

.mem-info {
  flex: 1;
  min-width: 0;
}

.mem-name {
  display: block;
  font-size: 28rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.mem-bal {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: #5f5e5e;
}

.mem-switch {
  font-size: 22rpx;
  font-weight: 800;
  color: #a33e00;
  text-transform: uppercase;
}

.equip-grid {
  display: flex;
  gap: 24rpx;
}

.equip-cell {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28rpx 16rpx;
  border-radius: 16rpx;
  background: #f3f3f3;
  border: 4rpx solid transparent;
}

.equip-cell.selected {
  background: rgba(255, 102, 0, 0.1);
  border-color: #ff6600;
}

.equip-name {
  margin-top: 12rpx;
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.equip-name.on {
  color: #1a1c1c;
}

.equip-price {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #5f5e5e;
}

.equip-price.on {
  color: #a33e00;
  font-weight: 700;
}

.row-2 {
  display: flex;
  gap: 24rpx;
  margin-bottom: 24rpx;
}

.half-card {
  flex: 1;
  margin-bottom: 0;
}

.highlight-b {
  border-bottom: 6rpx solid #ff6600;
}

.mini-label {
  display: block;
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 16rpx;
}

.mini-label.mb {
  margin-bottom: 20rpx;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.step-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  &:active {
    background: #ff6600;
  }
}

.step-sym {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1c1c;
  line-height: 1;
}

.step-btn:active .step-sym {
  color: #fff;
}

.qty-val {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
  min-width: 48rpx;
  text-align: center;
}

.estimate-val {
  font-size: 40rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.date-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
}

.date-text {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1c1c;
}

.pay-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.pay-cell {
  width: calc(50% - 8rpx);
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 16rpx;
  border-radius: 16rpx;
  background: #f3f3f3;
  border: 4rpx solid transparent;
}

.pay-cell.selected {
  background: rgba(255, 102, 0, 0.1);
  border-color: #ff6600;
}

.pay-lab {
  font-size: 22rpx;
  font-weight: 800;
  color: #1a1c1c;
}

.pay-lab.on {
  color: #a33e00;
}

.remarks {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  background: #f3f3f3;
  border-radius: 16rpx;
  font-size: 26rpx;
  color: #1a1c1c;
  box-sizing: border-box;
}

.total-card {
  position: relative;
  overflow: hidden;
  background: #e8e8e8;
  border: 4rpx solid #ff6600;
}

.total-watermark {
  position: absolute;
  right: -20rpx;
  top: -20rpx;
  opacity: 0.06;
  pointer-events: none;
}

.total-row {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.total-k {
  display: block;
  font-size: 20rpx;
  font-weight: 700;
  color: #5f5e5e;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.total-num {
  display: block;
  margin-top: 8rpx;
  font-size: 48rpx;
  font-weight: 900;
  color: #1a1c1c;
}

.total-sub {
  font-size: 20rpx;
  font-style: italic;
  color: #5a4136;
}

.scroll-pad {
  height: 24rpx;
}

.footer-actions {
  flex-shrink: 0;
  display: flex;
  gap: 24rpx;
  padding: 20rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  background: rgba(249, 249, 249, 0.95);
  backdrop-filter: blur(12px);
  box-shadow: 0 -8rpx 24rpx rgba(0, 0, 0, 0.04);
}

.btn-cancel {
  flex: 1;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 16rpx;
  background: #e2e2e2;
  font-size: 26rpx;
  font-weight: 800;
  color: #1a1c1c;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  border: none;
  margin: 0;
  padding: 0;
  &::after {
    border: none;
  }
}

.btn-submit {
  flex: 1.4;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #a33e00 0%, #ff6600 100%);
  font-size: 26rpx;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  border: none;
  margin: 0;
  padding: 0;
  box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.22);
  &::after {
    border: none;
  }
}
</style>
