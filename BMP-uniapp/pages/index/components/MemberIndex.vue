<template>
  <view class="home-page bg-surface text-on-surface min-h-screen pb-24">
    <scroll-view scroll-y class="main-content">
      <view class="w-full bg-[#f3f3f3] flex justify-between items-center px-6 py-4 pb-6" :style="{ paddingTop: (statusBarHeight || 44) + 'px' }">
        <view class="flex items-center gap-3">
          <view class="w-10 h-10 rounded-full overflow-hidden">
            <image class="w-full h-full object-cover" mode="aspectFill" :src="avatarUrl" />
          </view>
          <text class="font-lexend text-xl font-bold tracking-tight text-[#1a1a1a]">Kinetic Logic</text>
        </view>
        <view class="text-[#5f5e5e]" :style="{ marginRight: navBarMarginRight + 'px' }" @tap="navigateTo('/pages/notice/index')">
          <uni-icons type="chatbubble" size="20" color="#5f5e5e"></uni-icons>
        </view>
      </view>

      <view class="px-6 space-y-8 pb-24 pt-2">
        <view class="grid-section flex flex-col gap-4">
          <view class="bg-surface-container-lowest p-6 rounded-xl flex flex-col justify-between h-40 relative overflow-hidden group shadow-sm" @tap="navigateTo('/pages/recharge/index')">
            <view class="z-10">
              <text class="text-xs-10 font-bold tracking-wide uppercase text-secondary mb-1 block">账户余额</text>
              <text class="text-3xl font-extrabold text-on-surface block mt-1">¥{{ balanceText }}</text>
              <view class="inline-flex mt-2 px-3 py-1 bg-primary-container-alpha rounded-full">
                <text class="text-primary text-xs-10 font-bold">{{ memberTypeText }}</text>
              </view>
            </view>
            <view class="absolute-bottom-right opacity-5">
              <uni-icons type="wallet" size="52" color="rgba(0,0,0,0.12)"></uni-icons>
            </view>
            <view class="z-10 self-end">
              <view class="text-primary text-sm font-bold flex items-center gap-1">
                立即充值 <uni-icons type="right" size="14" color="#a33e00"></uni-icons>
              </view>
            </view>
          </view>

          <view class="gradient-cta p-6 rounded-xl text-white flex flex-col items-center justify-center text-center shadow-lg" @tap="navigateTo('/pages/booking/list')">
            <text class="text-xs-10 font-bold tracking-wide uppercase opacity-80 mb-2 block">预约提醒</text>
            <view class="flex flex-col items-center">
              <text class="text-lg font-bold block">{{ nextBookingTitle }}</text>
              <text class="text-2xl font-black block mt-1">{{ nextBookingTime }}</text>
            </view>
            <view class="mt-4 flex flex-row gap-2 items-center">
              <uni-icons type="location" size="14" color="#ffffff"></uni-icons>
              <text class="text-xs-10 font-medium">{{ nextBookingLocation }}</text>
            </view>
          </view>
        </view>

        <view class="mt-8">
          <view class="flex flex-row justify-between items-end mb-6">
            <text class="font-headline text-2xl font-bold tracking-tight text-on-surface">核心服务</text>
            <text class="text-primary text-xs-12 font-bold uppercase tracking-widest">Core Services</text>
          </view>
          <view class="grid-services gap-4">
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/venue/list')">
              <view class="w-12 h-12 rounded-lg bg-primary-alpha flex items-center justify-center text-primary">
                <uni-icons type="flag" size="24" color="#a33e00"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">场地预订</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Court Booking</text>
              </view>
            </view>
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/course/list')">
              <view class="w-12 h-12 rounded-lg bg-tertiary-fixed flex items-center justify-center text-tertiary">
                <uni-icons type="compose" size="24" color="#0062a1"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">教练课程</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Coach Courses</text>
              </view>
            </view>
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/tournament/list')">
              <view class="w-12 h-12 rounded-lg bg-orange-100 flex items-center justify-center text-orange-600">
                <uni-icons type="medal" size="24" color="#ea580c"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">赛事报名</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Tournaments</text>
              </view>
            </view>
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/equipment/list')">
              <view class="w-12 h-12 rounded-lg bg-slate-100 flex items-center justify-center text-slate-600">
                <uni-icons type="cart" size="24" color="#475569"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">器材租赁</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Equipment</text>
              </view>
            </view>
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/profile/index')">
              <view class="w-12 h-12 rounded-lg bg-purple-100 flex items-center justify-center text-purple-600">
                <uni-icons type="vip" size="24" color="#9333ea"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">会员中心</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Member Center</text>
              </view>
            </view>
            <view class="bg-surface-container-lowest aspect-square p-5 rounded-xl flex flex-col items-center justify-center gap-2 text-center hover-scale shadow-sm" @tap="navigateTo('/pages/stringing/list')">
              <view class="w-12 h-12 rounded-lg bg-emerald-100 flex items-center justify-center text-emerald-600">
                <uni-icons type="compose" size="24" color="#059669"></uni-icons>
              </view>
              <view>
                <text class="font-bold text-on-surface block">穿线服务</text>
                <text class="text-xs-10 text-secondary block mt-0-5">Stringing</text>
              </view>
            </view>
          </view>
        </view>

        <view class="mt-8 pb-8">
          <view class="flex flex-row justify-between items-end mb-6">
            <text class="font-headline text-2xl font-bold tracking-tight text-on-surface">快捷入口</text>
            <text class="text-primary text-xs-12 font-bold uppercase tracking-widest">Quick Access</text>
          </view>
          <view class="flex flex-col gap-4">
            <view class="relative h-64 rounded-xl overflow-hidden group shadow-md bg-gray-900 group-hover-scale" @tap="navigateTo('/pages/recharge/records')">
              <image class="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover-image" mode="aspectFill" src="/static/placeholders/hero.svg" />
              <view class="absolute inset-0 bg-gradient-to-t flex flex-col justify-end p-6">
                <text class="bg-primary px-2 py-1 text-xs-8 font-bold text-white rounded w-fit mb-2">账户流水</text>
                <text class="text-white text-xl font-bold block">查看充值记录与账户变化</text>
                <text class="text-white-70 text-xs-12 mt-1 block">用户端已接入真实充值记录与余额数据。</text>
              </view>
            </view>

            <view class="bg-surface-container-low p-4 rounded-xl flex flex-row items-center gap-4 shadow-sm" @tap="navigateTo('/pages/booking/list')">
              <view class="w-16 h-16 rounded-lg overflow-hidden flex-shrink-0 bg-gray-200">
                <image class="w-full h-full object-cover" mode="aspectFill" src="/static/placeholders/avatar.svg" />
              </view>
              <view class="flex flex-col flex-1">
                <text class="font-bold text-sm text-on-surface block">我的预约</text>
                <text class="text-secondary text-xs-10 mt-1 block">查看待支付、进行中和历史预约状态。</text>
              </view>
            </view>
            <view class="bg-surface-container-low p-4 rounded-xl flex flex-row items-center gap-4 shadow-sm" @tap="navigateTo('/pages/profile/member')">
              <view class="w-16 h-16 rounded-lg overflow-hidden flex-shrink-0 bg-gray-200">
                <image class="w-full h-full object-cover" mode="aspectFill" src="/static/placeholders/avatar.svg" />
              </view>
              <view class="flex flex-col flex-1">
                <text class="font-bold text-sm text-on-surface block">会员档案</text>
                <text class="text-secondary text-xs-10 mt-1 block">查看等级、累计充值和账户余额。</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="fixed right-6 bottom-10 w-14 h-14 gradient-cta text-white rounded-full flex items-center justify-center shadow-2xl transition-transform z-60" hover-class="scale-95" @tap="navigateTo('/pages/venue/list')">
      <uni-icons type="plusempty" size="24" color="#ffffff"></uni-icons>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getSafeSystemInfo } from '@/utils/systemInfo'
import { useCurrentMember } from '@/composables/useCurrentMember'
import { getBookingList } from '@/api/booking'
import { getAvatarImage } from '@/utils/displayImage'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
const { currentMember, fetchCurrentMember } = useCurrentMember()

const statusBarHeight = ref(44)
const navBarMarginRight = ref(0)
const nextBooking = ref<any | null>(null)

const avatarUrl = computed(() => getAvatarImage(userStore.userInfo?.avatar))
const balanceText = computed(() => Number(currentMember.value?.balance || 0).toFixed(2))
const memberTypeText = computed(() => {
  const typeMap: Record<string, string> = {
    NORMAL: '普通会员',
    MEMBER: '正式会员',
    VIP: 'VIP 会员',
    GOLD: '黄金会员',
    PLATINUM: '白金会员',
    DIAMOND: '钻石会员'
  }
  return typeMap[currentMember.value?.memberType || 'NORMAL'] || '普通会员'
})

const nextBookingTitle = computed(() => nextBooking.value?.courtName || '暂无待进行预约')
const nextBookingTime = computed(() => {
  if (!nextBooking.value?.startTime || !nextBooking.value?.endTime) {
    return '去预约一片场地'
  }
  return `${String(nextBooking.value.startTime).slice(0, 5)} - ${String(nextBooking.value.endTime).slice(0, 5)}`
})
const nextBookingLocation = computed(() => nextBooking.value?.venueName || '点击查看全部预约')

async function loadDashboardData() {
  try {
    const member = await fetchCurrentMember(true)
    const result = await getBookingList({
      memberId: member.id,
      page: 1,
      size: 20
    })
    const list = Array.isArray(result.data) ? result.data : []
    nextBooking.value = list.find((item) => [1, 2, 3].includes(Number(item.status))) || null
  } catch (error) {
    console.error('加载首页数据失败:', error)
    nextBooking.value = null
  }
}

onMounted(() => {
  const systemInfo = getSafeSystemInfo()
  statusBarHeight.value = systemInfo.statusBarHeight || 44

  // #ifdef MP
  try {
    const menuInfo = uni.getMenuButtonBoundingClientRect()
    if (menuInfo) {
      navBarMarginRight.value = Math.max(0, systemInfo.windowWidth - menuInfo.left - 4)
    }
  } catch (error) {
    console.error(error)
  }
  // #endif

  void loadDashboardData()
})

const navigateTo = (url: string) => {
  uni.navigateTo({ url })
}
</script>

<style lang="scss" scoped>
.home-page {
  background-color: #f9f9f9;
}

.text-on-surface { color: #1a1c1c; }
.bg-surface { background-color: #f9f9f9; }
.bg-surface-container-lowest { background-color: #ffffff; }
.bg-surface-container-low { background-color: #f3f3f3; }
.text-secondary { color: #5f5e5e; }
.text-primary { color: #a33e00; }
.bg-primary { background-color: #a33e00; }
.bg-primary-alpha { background-color: rgba(255, 102, 0, 0.1); }
.bg-primary-container-alpha { background-color: rgba(255, 102, 0, 0.1); }
.text-tertiary { color: #0062a1; }
.bg-tertiary-fixed { background-color: #d0e4ff; }
.text-orange-600 { color: #ea580c; }
.bg-orange-100 { background-color: #ffedd5; }
.text-slate-600 { color: #475569; }
.bg-slate-100 { background-color: #f1f5f9; }
.text-purple-600 { color: #9333ea; }
.bg-purple-100 { background-color: #f3e8ff; }
.text-emerald-600 { color: #059669; }
.bg-emerald-100 { background-color: #d1fae5; }
.text-white { color: #ffffff; }
.text-white-70 { color: rgba(255, 255, 255, 0.7); }

.gradient-cta {
  background: linear-gradient(45deg, #a33e00, #ff6600);
}

.bg-gradient-to-t {
  background: linear-gradient(to top, rgba(0,0,0,0.8), rgba(0,0,0,0.4), transparent);
}

.shadow-sm { box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); }
.shadow-md { box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); }
.shadow-2xl { box-shadow: 0 25px 50px -12px rgba(163, 62, 0, 0.25); }

.main-content { height: 100vh; }
.grid-services { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.flex { display: flex; }
.flex-col { flex-direction: column; }
.flex-row { flex-direction: row; }
.flex-1 { flex: 1; }
.items-center { align-items: center; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.justify-end { justify-content: flex-end; }
.items-end { align-items: flex-end; }
.self-end { align-self: flex-end; }
.flex-shrink-0 { flex-shrink: 0; }
.inline-flex { display: inline-flex; }
.block { display: block; }
.gap-1 { gap: 4rpx; }
.gap-2 { gap: 8rpx; }
.gap-3 { gap: 12rpx; }
.gap-4 { gap: 16rpx; }
.px-6 { padding-left: 24rpx; padding-right: 24rpx; }
.py-4 { padding-top: 16rpx; padding-bottom: 16rpx; }
.px-3 { padding-left: 12rpx; padding-right: 12rpx; }
.py-1 { padding-top: 4rpx; padding-bottom: 4rpx; }
.px-2 { padding-left: 8rpx; padding-right: 8rpx; }
.p-6 { padding: 24rpx; }
.p-5 { padding: 20rpx; }
.p-4 { padding: 16rpx; }
.pt-2 { padding-top: 8rpx; }
.pb-24 { padding-bottom: 96rpx; }
.pb-8 { padding-bottom: 32rpx; }
.mt-8 { margin-top: 32rpx; }
.mb-6 { margin-bottom: 24rpx; }
.mb-1 { margin-bottom: 4rpx; }
.mt-2 { margin-top: 8rpx; }
.mt-4 { margin-top: 16rpx; }
.mt-1 { margin-top: 4rpx; }
.mt-0-5 { margin-top: 2rpx; }
.w-full { width: 100%; }
.h-full { height: 100%; }
.w-10 { width: 40px; }
.h-10 { height: 40px; }
.w-12 { width: 48px; }
.h-12 { height: 48px; }
.w-14 { width: 56px; }
.h-14 { height: 56px; }
.w-16 { width: 64px; }
.h-16 { height: 64px; }
.h-40 { height: 160px; }
.h-64 { height: 256px; }
.min-h-screen { min-height: 100vh; }
.rounded-full { border-radius: 9999px; }
.rounded-xl { border-radius: 24rpx; }
.rounded-lg { border-radius: 16rpx; }
.overflow-hidden { overflow: hidden; }
.fixed { position: fixed; }
.absolute { position: absolute; }
.relative { position: relative; }
.z-10 { z-index: 10; }
.z-60 { z-index: 200; }
.right-6 { right: 24rpx; }
.bottom-10 { bottom: 40rpx; }
.inset-0 { top: 0; right: 0; bottom: 0; left: 0; }
.absolute-bottom-right { position: absolute; right: -16rpx; bottom: -16rpx; }
.font-bold { font-weight: bold; }
.font-extrabold { font-weight: 800; }
.font-black { font-weight: 900; }
.font-medium { font-weight: 500; }
.text-3xl { font-size: 30px; line-height: 36px; }
.text-2xl { font-size: 24px; line-height: 32px; }
.text-xl { font-size: 20px; line-height: 28px; }
.text-lg { font-size: 18px; line-height: 28px; }
.text-sm { font-size: 14px; line-height: 20px; }
.text-xs-12 { font-size: 12px; }
.text-xs-10 { font-size: 10px; }
.text-xs-8 { font-size: 8px; }
.tracking-tight { letter-spacing: -0.025em; }
.tracking-wide { letter-spacing: 0.05em; }
.tracking-widest { letter-spacing: 0.1em; }
.uppercase { text-transform: uppercase; }
.text-center { text-align: center; }
.opacity-5 { opacity: 0.05; }
.opacity-80 { opacity: 0.8; }
.hover-scale { transition: transform 0.3s; &:active { transform: scale(0.98); } }
.group-hover-image { transition: transform 0.7s; }
.group-hover-scale { &:active .group-hover-image { transform: scale(1.08); } }
.aspect-square { aspect-ratio: 1 / 1; }
.object-cover { object-fit: cover; }
</style>
