<template>
  <PresidentLayout :showTabBar="false">
    <view class="stock-page">
      <view class="status-bar-placeholder" />

      <view class="top-bar">
        <view class="top-left">
          <view class="menu-btn">
            <uni-icons type="bars" size="20" color="#5f5e5e" />
          </view>
          <text class="top-title">库存管理</text>
        </view>
        <view class="top-right">
          <view class="icon-btn">
            <uni-icons type="notification" size="18" color="#ea580c" />
          </view>
          <image class="avatar" :src="presidentAvatar" mode="aspectFill" />
        </view>
      </view>

      <scroll-view scroll-y class="scroll" :show-scrollbar="false">
        <view class="bento-grid">
          <view class="card total">
            <text class="k">资产总量</text>
            <text class="v">1,248 <text class="unit">项目</text></text>
            <text class="tag">+12% 较上月</text>
            <view class="watermark"><uni-icons type="shop" size="88" color="#000000" /></view>
          </view>
          <view class="card warning">
            <text class="k white">低库存预警</text>
            <text class="v white">04</text>
            <text class="tag white-soft">立即处理</text>
          </view>
          <view class="card flow">
            <text class="k">今日流水</text>
            <text class="v">82</text>
            <text class="sub">入库/出库</text>
          </view>
        </view>

        <view class="panel">
          <view class="tabs">
            <view class="tab active">库存项 (Active)</view>
            <view class="tab">入库记录</view>
            <view class="tab">出库日志</view>
          </view>

          <view class="thead">
            <text class="c1">器材项目</text>
            <text class="c2">器材分类</text>
            <text class="c3">补货分发</text>
          </view>

          <view class="row" v-for="item in items" :key="item.id">
            <view class="item-left">
              <image class="item-img" :src="item.image" mode="aspectFill" />
              <view class="item-main">
                <text class="name">{{ item.name }}</text>
                <text class="sku">SKU: {{ item.sku }}</text>
              </view>
            </view>
            <text class="type">{{ item.type }}</text>
            <view class="stepper">
              <view class="op" @click="decrease(item)">-</view>
              <input class="count" type="number" v-model="item.delta" />
              <view class="op plus" @click="increase(item)">+</view>
            </view>
          </view>

          <view class="panel-footer">
            <text>最后更新：今天, 14:42 操作人：Marcus V.</text>
            <view class="actions">
              <view class="btn ghost" @click="onCancel">取消</view>
              <view class="btn primary" @click="onSubmit">确认提交</view>
            </view>
          </view>
        </view>

        <view class="log-wrap">
          <view class="log-head">
            <view>
              <text class="log-title">近期变动日志 (Recent Log)</text>
              <text class="log-sub">历史出库及入库记录。</text>
            </view>
            <view class="log-link">查看全部日志 <uni-icons type="right" size="14" color="#a33e00" /></view>
          </view>
          <view class="log-grid">
            <view class="log-card" v-for="log in logs" :key="log.id">
              <view class="log-left">
                <view class="log-icon" :class="log.kind === 'in' ? 'in' : 'out'">
                  <uni-icons :type="log.kind === 'in' ? 'download' : 'upload'" size="16" :color="log.kind === 'in' ? '#0062a1' : '#a33e00'" />
                </view>
                <view>
                  <text class="log-name">{{ log.title }}</text>
                  <text class="log-meta">{{ log.meta }}</text>
                </view>
              </view>
              <view class="log-right">
                <text :class="log.kind === 'in' ? 'in-t' : 'out-t'">{{ log.qty }}</text>
                <text class="qty">Qty</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { useUserStore } from '@/store/modules/user'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const userStore = useUserStore()
const presidentAvatar = computed(() => userStore.userInfo?.avatar || '/static/placeholders/avatar.svg')

const items = reactive([
  { id: 1, name: 'Yonex Astrox 88D Pro', sku: 'KL-YN-88D-01', type: '球拍', delta: '0', image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBepTMwPZQnCR9c8DUN9DoRK58eOe3F65Gt-AiElEkjdWwzGaBiCmvGfSC-JsxJ7aCvnwLoGPyVf1F-2A4KBekX0vALR64SJ-UlYULpeJnSUOeJRUTm4AOWm6Rb6Za-QHXMOUg1EqHDYUgpkKm0ELidcBcU-Qw0jbze_oPitwzLVT8jlStEVM1FLGnckr80F-S-_phwjK6W0oBfh0xtaEY0LHw_yu7nEdPCTnPPb58gsiiM0ANJHQRR8tH4wzevuSMkqQy06wJG7xCR' },
  { id: 2, name: 'Aerosensa 50 (Dozen)', sku: 'KL-YN-AS50', type: '耗材', delta: '12', image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCsxyaL0Lp0-rIA3f0IpqAS7vEFDLARPEBI6PB7lxonsrKOMo8dEZxmPrGOuxKtLYLBInbxKnzezVn8U3Hk0UUnAQKQT79d7UOflh_56l_f6O2SZMVW9RSLMYgJoIh0bEzuozr8kIottulmOMTaANPt8I8OA286FkohBNkFOYGqsLfpB-_gw5FBvAsoeUmYsgdwoxI8qHh9lHZ9pKfQjomPX_n06TQRvzZJfNTcgmew8NR122p1LRo6evhyxY7Xx7Efqt7kN17ZfYRJ' },
  { id: 3, name: 'BG80 Power Roll (200m)', sku: 'KL-YN-BG80P', type: '球线', delta: '0', image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCGDJ708fVvXT_3IDdMZR3kWTNFCs85Ln2xPwobyMospAzXqKfE6i_cLk0ZO_7cPT4ULFVYM7BHrGAL2CM4_fXYlIJPlJ7GWQIfTe_xCGLMpheSwLAD7vbI2zuxhAA_rSfX8vuHYcO2IyyUih8_Xjk1M4AjuUZ0BJFQ8jUALRnI0Pbbt67C08OmaPAXZKSdTnj53LxqNkVIOM29-Jc7ZtI3hW_0WXoeZ5R82sS8-jnLVXUDRwvM-3HwbG2dfFsNUJbS3wPOuEYgpE1M' }
])

const logs = [
  { id: 1, kind: 'in', title: 'Inbound: Yonex String Roll', meta: 'Batch #8821 • 2 hours ago', qty: '+5' },
  { id: 2, kind: 'out', title: 'Outbound: Shuttlecock Boxes', meta: 'Match Ref: #M92 • 5 hours ago', qty: '-12' }
]

function increase(item: { delta: string }) {
  item.delta = String((parseInt(item.delta || '0', 10) || 0) + 1)
}
function decrease(item: { delta: string }) {
  const next = (parseInt(item.delta || '0', 10) || 0) - 1
  item.delta = String(Math.max(0, next))
}
function onCancel() {
  safeNavigateBack(PRESIDENT_PAGES.EQUIPMENT_LIST)
}
function onSubmit() {
  uni.showToast({ title: '库存变更已提交（演示）', icon: 'success' })
}
</script>

<style lang="scss" scoped>
.stock-page { min-height: 100vh; background: #f9f9f9; color: #1a1c1c; }
.status-bar-placeholder { height: var(--status-bar-height); background: #f8fafc; }
.top-bar { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 24rpx; }
.top-left,.top-right { display: flex; align-items: center; gap: 12rpx; }
.menu-btn,.icon-btn { width: 46rpx; height: 46rpx; border-radius: 9999px; display: flex; align-items: center; justify-content: center; }
.top-title { font-size: 36rpx; font-weight: 900; }
.avatar { width: 52rpx; height: 52rpx; border-radius: 9999px; }
.scroll { height: calc(100vh - var(--status-bar-height) - 84rpx); padding: 0 24rpx 24rpx; box-sizing: border-box; }
.bento-grid { display: grid; grid-template-columns: 1fr; gap: 12rpx; }
.card { border-radius: 18rpx; padding: 20rpx; background: #fff; position: relative; overflow: hidden; }
.k { color: #71717a; font-size: 18rpx; font-weight: 800; letter-spacing: .08em; text-transform: uppercase; }
.k.white { color: rgba(255,255,255,.85); }
.v { display: block; margin-top: 8rpx; font-size: 58rpx; font-weight: 900; letter-spacing: -0.03em; }
.v.white { color: #fff; }
.unit { font-size: 20rpx; color: #a1a1aa; font-weight: 600; }
.tag { display: inline-block; margin-top: 14rpx; background: rgba(163,62,0,.1); color: #a33e00; font-size: 16rpx; font-weight: 800; padding: 6rpx 10rpx; border-radius: 9999px; }
.tag.white-soft { background: rgba(255,255,255,.22); color: #fff; }
.watermark { position: absolute; right: -8rpx; bottom: -8rpx; opacity: .06; }
.warning { background: #ff6600; }
.sub { display: block; margin-top: 10rpx; color: #0062a1; font-size: 18rpx; font-weight: 800; letter-spacing: .08em; text-transform: uppercase; }

.panel { margin-top: 18rpx; background: #fff; border-radius: 18rpx; overflow: hidden; }
.tabs { display: flex; align-items: center; background: #f3f3f3; overflow-x: auto; }
.tab { flex-shrink: 0; padding: 18rpx 20rpx; color: #71717a; font-size: 22rpx; font-weight: 700; }
.tab.active { color: #a33e00; border-bottom: 3rpx solid #a33e00; }
.thead { display: grid; grid-template-columns: 2fr .8fr 1fr; padding: 16rpx 18rpx; color: #71717a; font-size: 16rpx; font-weight: 800; border-bottom: 1rpx solid #f1f5f9; }
.row { display: grid; grid-template-columns: 2fr .8fr 1fr; align-items: center; padding: 14rpx 18rpx; border-bottom: 1rpx solid #f1f5f9; }
.item-left { display: flex; align-items: center; gap: 10rpx; min-width: 0; }
.item-img { width: 56rpx; height: 56rpx; border-radius: 12rpx; background: #e5e7eb; }
.item-main { min-width: 0; display: flex; flex-direction: column; }
.name { font-size: 22rpx; font-weight: 800; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sku { font-size: 16rpx; color: #a1a1aa; margin-top: 3rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.type { justify-self: center; font-size: 18rpx; color: #5f5e5e; background: #f3f3f3; padding: 4rpx 12rpx; border-radius: 9999px; }
.stepper { justify-self: end; display: inline-flex; align-items: center; background: #f3f3f3; border-radius: 10rpx; padding: 4rpx; gap: 4rpx; }
.op { width: 34rpx; height: 34rpx; border-radius: 8rpx; background: #fff; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 900; color: #a33e00; }
.op.plus { background: #ff6600; color: #fff; }
.count { width: 52rpx; text-align: center; font-size: 20rpx; font-weight: 800; }
.panel-footer { padding: 14rpx 18rpx; display: flex; flex-direction: column; gap: 10rpx; background: #f3f3f3; color: #71717a; font-size: 16rpx; }
.actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10rpx; }
.btn { height: 66rpx; border-radius: 12rpx; display: flex; align-items: center; justify-content: center; font-size: 24rpx; font-weight: 800; }
.btn.ghost { background: #e5e7eb; color: #1f2937; }
.btn.primary { background: #ff6600; color: #561d00; }

.log-wrap { margin-top: 18rpx; }
.log-head { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 10rpx; }
.log-title { display: block; font-size: 34rpx; font-weight: 900; }
.log-sub { display: block; font-size: 16rpx; color: #71717a; margin-top: 2rpx; }
.log-link { display: flex; align-items: center; gap: 2rpx; color: #a33e00; font-size: 18rpx; font-weight: 800; }
.log-grid { display: grid; grid-template-columns: 1fr; gap: 10rpx; }
.log-card { background: #fff; border-radius: 14rpx; padding: 14rpx; display: flex; align-items: center; justify-content: space-between; }
.log-left { display: flex; align-items: center; gap: 10rpx; min-width: 0; }
.log-icon { width: 42rpx; height: 42rpx; border-radius: 9999px; display: flex; align-items: center; justify-content: center; }
.log-icon.in { background: rgba(0,98,161,.12); }
.log-icon.out { background: rgba(163,62,0,.12); }
.log-name { display: block; font-size: 24rpx; font-weight: 800; }
.log-meta { display: block; font-size: 16rpx; color: #a1a1aa; margin-top: 2rpx; }
.log-right { text-align: right; }
.in-t { color: #0062a1; font-size: 28rpx; font-weight: 900; }
.out-t { color: #a33e00; font-size: 28rpx; font-weight: 900; }
.qty { display: block; font-size: 14rpx; color: #a1a1aa; margin-top: 2rpx; }
</style>
