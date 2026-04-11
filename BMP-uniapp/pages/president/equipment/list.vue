<template>
  <PresidentLayout :showTabBar="true">
    <view class="equipment-list-page">
      <view class="status-bar-placeholder"></view>

      <!-- TopAppBar（与赛事管理一致） -->
      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#ff6600"></uni-icons>
            </view>
            <view class="brand-wrap">
              <text class="brand-name">Kinetic Logic</text>
            </view>
          </view>
          <view class="nav-right">
            <view class="icon-btn" @click.stop="handleSearch">
              <uni-icons type="search" size="22" color="#71717a"></uni-icons>
            </view>
            <view class="icon-btn" @click.stop="handleSettings">
              <uni-icons type="gear" size="22" color="#71717a"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="main-content">
        <view class="hero-section">
          <view class="hero-text">
            <text class="hero-title">器材管理</text>
            <text class="hero-subtitle">器材监控中心</text>
          </view>
        </view>

        <!-- Dashboard Inventory Overview (Bento Grid) -->
        <view class="overview-section">
          <view class="bento-card regular">
            <text class="bento-label">总器材数</text>
            <view class="bento-bottom">
              <text class="bento-value text-default">1,248</text>
              <uni-icons type="shop" size="30" color="#a33e00" class="bento-icon opacity-low"></uni-icons>
            </view>
          </view>
          
          <view class="bento-card bordered-tertiary">
            <text class="bento-label">当前已租借</text>
            <view class="bento-bottom">
              <text class="bento-value text-tertiary">312</text>
              <uni-icons type="cart" size="30" color="#009cfc" class="bento-icon opacity-low"></uni-icons>
            </view>
          </view>
          
          <view class="bento-card highlight-orange">
            <text class="bento-label highlight-text">需要补货</text>
            <view class="bento-bottom">
              <text class="bento-value text-primary">14</text>
              <view class="warning-wrap">
                <text class="warning-text">LOWER LIMIT REACHED</text>
                <uni-icons type="info" size="24" color="#a33e00"></uni-icons>
              </view>
            </view>
          </view>
        </view>

        <!-- Navigation & Filter Section -->
        <view class="filter-section">
          <view class="tabs-wrap">
            <view class="tab-item" :class="{ active: currentTab === 0 }" @click="currentTab = 0">球拍</view>
            <view class="tab-item" :class="{ active: currentTab === 1 }" @click="currentTab = 1">羽毛球</view>
            <view class="tab-item" :class="{ active: currentTab === 2 }" @click="currentTab = 2">球线</view>
            <view class="tab-item" :class="{ active: currentTab === 3 }" @click="currentTab = 3">其他</view>
          </view>
        </view>

        <!-- Equipment Grid -->
        <view class="equipment-grid">
          <view class="equipment-card group" v-for="(item, index) in filteredEquipments" :key="index" @click="goDetail(item)">
            <view class="card-image-wrap">
              <image class="equipment-image" :src="item.image" mode="aspectFit"></image>
              <view class="status-badge" :class="item.status === '库存充足' ? 'success' : 'warning'">
                {{ item.status }}
              </view>
            </view>

            <view class="card-info">
              <view class="title-row">
                <text class="title">{{ item.name }}</text>
                <text class="sku">{{ item.sku }}</text>
              </view>

              <view class="stock-grid">
                <view class="stock-item">
                  <text class="stock-label">总库存</text>
                  <text class="stock-value">{{ item.total }}</text>
                </view>
                <view class="stock-item">
                  <text class="stock-label" :class="{ 'text-primary': item.status !== '库存充足' }">剩余可用</text>
                  <text class="stock-value" :class="{ 'text-primary': item.status !== '库存充足' }">{{ item.available }}</text>
                </view>
              </view>

              <view class="card-action-row">
                <view class="price-wrap">
                  <text class="currency">¥</text>
                  <text class="price">{{ item.price }}</text>
                  <text class="unit">/天</text>
                </view>
                <view class="action-btns">
                  <view class="edit-btn" @click.stop="handleEdit(item)">
                    <uni-icons type="compose" size="18" color="#52525b"></uni-icons>
                  </view>
                  <button class="primary-btn" :class="{ 'urgent': item.status !== '库存充足' }" @click.stop="handleRestock(item)">
                    {{ item.status === '库存充足' ? '入库' : '紧急补货' }}
                  </button>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- FAB -->
      <view class="fab-btn" @click="goAdd">
        <uni-icons type="plusempty" size="30" color="#ffffff" class="fab-bold"></uni-icons>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const currentTab = ref(0)

const equipments = ref([
  {
    id: 1,
    name: 'YONEX 天斧 100ZZ',
    sku: 'EQ-AX100-001',
    image: '/static/placeholders/hero.svg',
    status: '库存充足',
    total: 48,
    available: 32,
    price: 80,
    category: 0 // 球拍
  },
  {
    id: 2,
    name: 'VICTOR 龙牙之刃 II',
    sku: 'EQ-TKRY-II',
    image: '/static/placeholders/hero.svg',
    status: '低库存预警',
    total: 12,
    available: 3,
    price: 75,
    category: 0 // 球拍
  },
  {
    id: 3,
    name: 'LI-NING 战戟 8000',
    sku: 'EQ-LN-HAL-8K',
    image: '/static/placeholders/hero.svg',
    status: '库存充足',
    total: 24,
    available: 18,
    price: 90,
    category: 0 // 球拍
  },
  {
    id: 4,
    name: 'YONEX 疾光 800 Pro',
    sku: 'EQ-NF-800P',
    image: '/static/placeholders/hero.svg',
    status: '库存充足',
    total: 15,
    available: 9,
    price: 65,
    category: 0 // 球拍
  }
])

const filteredEquipments = computed(() => {
  return equipments.value.filter(e => e.category === currentTab.value)
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function handleSearch() {
  uni.showToast({ title: '搜索功能开发中', icon: 'none' })
}

function handleSettings() {
  uni.showToast({ title: '设置功能开发中', icon: 'none' })
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.EQUIPMENT_FORM })
}

function handleEdit(item: any) {
  goDetail(item)
}

function handleRestock(item: any) {
  if (!item?.id) {
    uni.showToast({ title: '器材信息不完整', icon: 'none' })
    return
  }
  uni.navigateTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_STOCK}?id=${item.id}` })
}

function goDetail(item: any) {
  if (!item?.id) {
    uni.showToast({ title: '器材信息不完整', icon: 'none' })
    return
  }
  const q = [
    `id=${item.id}`,
    `equipmentName=${encodeURIComponent(item.name || '')}`,
    `equipmentCode=${encodeURIComponent(item.sku || '')}`,
    `equipmentType=${encodeURIComponent(currentTab.value === 0 ? '专业级' : '标准级')}`,
    `brand=${encodeURIComponent('YONEX')}`,
    `price=${item.price || 0}`,
    `rentalPrice=${item.price || 0}`,
    `totalQuantity=${item.total || 0}`,
    `availableQuantity=${item.available || 0}`,
    `description=${encodeURIComponent(item.status || '')}`
  ].join('&')
  uni.navigateTo({ url: `${PRESIDENT_PAGES.EQUIPMENT_DETAIL}?${q}` })
}
</script>

<style lang="scss" scoped>
.equipment-list-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding-bottom: 240rpx;
  font-family: "Lexend", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #1a1c1c;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f8fafc;
}

.nav-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: rgba(248, 250, 252, 0.85);
  backdrop-filter: blur(12px);
  padding: 24rpx 48rpx;
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.04);
}

.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 24rpx;
  min-width: 0;

  .back-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    &:active {
      background-color: #e4e4e7;
    }
  }

  .brand-name {
    font-size: 36rpx;
    font-weight: 800;
    color: #ea580c;
    letter-spacing: -0.04em;
    text-transform: uppercase;
  }
}

.nav-right {
  display: flex;
  gap: 8rpx;
  flex-shrink: 0;

  .icon-btn {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;

    &:active {
      background-color: #e4e4e7;
    }
  }
}

.main-content {
  padding: 48rpx 48rpx 48rpx;
  max-width: 1400rpx;
  margin: 0 auto;
}

/* Hero（与赛事管理一致） */
.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 64rpx;
  gap: 32rpx;
  flex-wrap: wrap;
}

.hero-text {
  display: flex;
  flex-direction: column;

  .hero-title {
    font-size: 72rpx;
    font-weight: 900;
    color: #18181b;
    letter-spacing: -0.04em;
    line-height: 1;
  }

  .hero-subtitle {
    font-size: 22rpx;
    font-weight: 600;
    color: #71717a;
    letter-spacing: 0.15em;
    margin-top: 8rpx;
    text-transform: uppercase;
  }
}

/* Overview Section */
.overview-section {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32rpx;
  margin-bottom: 64rpx;

  @media (min-width: 768px) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.bento-card {
  background-color: #ffffff;
  padding: 32rpx;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 220rpx;
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.98);
  }

  &.bordered-tertiary {
    border-left: 8rpx solid #009cfc;
  }

  &.highlight-orange {
    background-color: #fff7ed;
  }

  .bento-label {
    font-size: 20rpx;
    font-weight: 700;
    color: #71717a;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    
    &.highlight-text {
      color: #9a3412;
    }
  }

  .bento-bottom {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
  }

  .bento-value {
    font-size: 64rpx;
    font-weight: 700;
    line-height: 1;

    &.text-default {
      color: #1a1c1c;
    }
    
    &.text-tertiary {
      color: #009cfc;
    }

    &.text-primary {
      color: #a33e00;
    }
  }

  .bento-icon {
    &.opacity-low {
      opacity: 0.2;
    }
  }

  .warning-wrap {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    
    .warning-text {
      font-size: 16rpx;
      font-weight: 700;
      color: #a33e00;
      margin-bottom: 4rpx;
    }
  }
}

/* Filter Section（分类标签；主标题已在 Hero 区） */
.filter-section {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  margin-bottom: 48rpx;

  .tabs-wrap {
    display: flex;
    background-color: #f3f3f3;
    padding: 8rpx;
    border-radius: 24rpx;
    gap: 8rpx;
    
    .tab-item {
      padding: 16rpx 40rpx;
      border-radius: 16rpx;
      font-size: 28rpx;
      font-weight: 600;
      color: #71717a;
      transition: all 0.3s ease;
      
      &.active {
        background-color: #ffffff;
        color: #a33e00;
        box-shadow: 0 2rpx 4rpx rgba(0,0,0,0.05);
      }
    }
  }
}

/* Equipment Grid */
.equipment-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32rpx;
  
  @media (min-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (min-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }

  @media (min-width: 1280px) {
    grid-template-columns: repeat(4, 1fr);
  }
}

.equipment-card {
  background-color: #ffffff;
  border-radius: 24rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .card-image-wrap {
    position: relative;
    height: 380rpx; /* Tweak height for better proportion */
    width: 100%;
    background-color: #e8e8e8;
    padding: 32rpx;
    box-sizing: border-box;

    .equipment-image {
      width: 100%;
      height: 100%;
      transition: transform 0.5s ease;
    }

    .status-badge {
      position: absolute;
      top: 24rpx;
      left: 24rpx;
      padding: 8rpx 24rpx;
      border-radius: 999rpx;
      font-size: 18rpx;
      font-weight: 700;
      letter-spacing: 0.1em;

      &.success {
        background-color: #dcfce7;
        color: #15803d;
      }

      &.warning {
        background-color: #ffedd5;
        color: #ea580c;
      }
    }
  }

  @media (hover: hover) {
    &:hover .equipment-image {
      transform: scale(1.1);
    }
  }

  .card-info {
    padding: 32rpx;
    display: flex;
    flex-direction: column;
    flex: 1;

    .title-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24rpx;
      
      .title {
        font-size: 32rpx;
        font-weight: 700;
        line-height: 1.2;
        color: #1a1c1c;
        flex: 1;
        padding-right: 16rpx;
      }

      .sku {
        font-size: 16rpx;
        color: #a1a1aa;
        font-family: monospace;
        letter-spacing: -0.05em;
        white-space: nowrap;
      }
    }

    .stock-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 24rpx;
      margin-bottom: 40rpx;

      .stock-item {
        display: flex;
        flex-direction: column;

        .stock-label {
          font-size: 16rpx;
          font-weight: 700;
          color: #a1a1aa;
          text-transform: uppercase;
          letter-spacing: 0.1em;
          margin-bottom: 4rpx;

          &.text-primary {
            color: #a33e00;
          }
        }

        .stock-value {
          font-size: 32rpx;
          font-weight: 700;
          color: #1a1c1c;

          &.text-primary {
            color: #a33e00;
          }
        }
      }
    }

    .card-action-row {
      margin-top: auto;
      padding-top: 32rpx;
      border-top: 2rpx solid #eeeeee;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .price-wrap {
        color: #a33e00;
        font-weight: 700;
        
        .currency { font-size: 28rpx; }
        .price { font-size: 36rpx; }
        .unit { 
          font-size: 20rpx; 
          font-weight: 400; 
          color: #a1a1aa;
          margin-left: 4rpx;
        }
      }

      .action-btns {
        display: flex;
        gap: 16rpx;

        .edit-btn {
          width: 64rpx;
          height: 64rpx;
          border-radius: 16rpx;
          background-color: #f3f3f3;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: background-color 0.2s;

          &:active {
            background-color: #e8e8e8;
          }
        }

        .primary-btn {
          margin: 0;
          line-height: inherit;
          font-size: 24rpx;
          font-weight: 700;
          color: #ffffff;
          background-color: #18181b;
          border-radius: 16rpx;
          padding: 0 32rpx;
          height: 64rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: transform 0.2s;

          &:active {
            transform: scale(0.95);
          }

          &.urgent {
            background-color: #ff6600;
            color: #ffffff;
            box-shadow: 0 2rpx 8rpx rgba(255, 102, 0, 0.2);
          }
          
          &::after {
            border: none;
          }
        }
      }
    }
  }
}

.fab-btn {
  position: fixed;
  bottom: 240rpx;
  right: 48rpx;
  width: 120rpx;
  height: 120rpx;
  background-color: #ff6600;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(255, 102, 0, 0.4);
  z-index: 1001;
  transition: transform 0.2s ease;
  
  &:active {
    transform: scale(0.9);
  }
}
</style>
