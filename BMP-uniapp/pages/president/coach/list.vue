<template>
  <PresidentLayout :showTabBar="true">
    <view class="coach-list-page">
      <view class="status-bar-placeholder"></view>
      
      <!-- TopAppBar Navigation -->
      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#1a1c1c"></uni-icons>
            </view>
            <text class="nav-title">教练管理</text>
          </view>
          <view class="nav-right">
            <view class="add-btn" @click="goAdd">
              <uni-icons type="plusempty" size="24" color="#ea580c" style="font-weight: bold;"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="main-content">
        <!-- Search and Filter Section -->
        <view class="filter-section">
          <view class="search-wrap">
            <text class="section-label">搜索教练</text>
            <view class="search-box">
              <uni-icons type="search" size="20" color="#a1a1aa" class="search-icon"></uni-icons>
              <input 
                class="search-input" 
                placeholder="输入教练姓名或特长..." 
                placeholder-class="input-placeholder"
                v-model="searchQuery"
              />
            </view>
          </view>

          <view class="tabs-wrap">
            <view 
              class="tab-item" 
              :class="{ active: currentTab === 0 }" 
              @click="currentTab = 0">全部</view>
            <view 
              class="tab-item" 
              :class="{ active: currentTab === 1 }" 
              @click="currentTab = 1">在职</view>
            <view 
              class="tab-item" 
              :class="{ active: currentTab === 2 }" 
              @click="currentTab = 2">休息中</view>
          </view>
        </view>

        <!-- Coach List -->
        <view class="coach-grid">
          <view class="coach-card group" v-for="(coach, index) in filteredList" :key="index" @click="goDetail(coach)">

            <view class="card-top">
              <view class="avatar-wrap">
                <image class="avatar" :src="coach.avatar" mode="aspectFill"></image>
                <view class="status-indicator" :class="coach.status === 'active' ? 'active' : 'resting'"></view>
              </view>
              
              <view class="info-wrap">
                <view class="name-row">
                  <text class="name">{{ coach.name }}</text>
                  <view class="rating-badge">
                    <uni-icons type="star-filled" size="12" color="#ea580c"></uni-icons>
                    <text class="rating-val">{{ coach.rating.toFixed(1) }}</text>
                  </view>
                </view>
                <text class="title">{{ coach.title }}</text>
                <view class="tags-row">
                  <text class="tag" v-for="tag in coach.tags" :key="tag">{{ tag }}</text>
                </view>
              </view>
            </view>

            <view class="card-bottom">
              <view class="price-side">
                <text class="label">价格</text>
                <view class="price-val">
                  <text class="currency">¥</text>
                  <text class="amount">{{ coach.price }}</text>
                  <text class="unit"> / 小时</text>
                </view>
              </view>
              <view class="activity-side">
                <text class="label">活跃度</text>
                <text class="activity-val">{{ coach.activity }}</text>
              </view>
            </view>
          </view>

          <!-- Add Placeholder -->
          <view class="add-placeholder" @click="goAdd">
            <uni-icons type="personadd" size="36" color="#a1a1aa"></uni-icons>
            <text class="add-text">添加新教练</text>
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
const searchQuery = ref('')

const coaches = ref([
  {
    id: 1,
    name: '张立明',
    avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBPA3GHUJavioGZPQrcNH2rJ7GX8BHoGQM684sF4Zu6bpyzMgTs_lecSqypfsP6TR_JzwBCp1amyVrDmQxfBYQMS80Ye6PKMDMW8w9Distdq-ecUQn40ZZ0hvzNZmELfxhy5f6Y4gFmKQ47auJ2nwqiAheR8pPWc80S7te6XOoiLpjg1Gsr9zaTu-TGWwBXYbNNrTp6U0t1W3FHlNVKVMnXqRNtNHMrL9-qjWNTjPhSD7p9WhFIks58TdGjVXtDgSFl83e8Zb6V2l-1',
    status: 'active',
    rating: 4.9,
    title: '国家一级运动员',
    tags: ['进攻专家', '10年教龄'],
    price: 180,
    activity: '累计 350+ 学员'
  },
  {
    id: 2,
    name: '李小雅',
    avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuBmogNMXK0enTWBupOqtA9MpT6HNw7dxxqmzZyOn9Gy_PocUBZLuWx9WTqCDrFs4rwJtmJMUmy4iPUWqmAI7duRa0eA_yhnvBN7CylhCkmomHnGBL99iQm9Ktykijq04FQzfkTyzmAL45nxU_4gXpytDzudNQDiN7xzdlcWlmhiLLUqhMqcrjef53Tj818gMgGqjtS6IWaHk92zIA6beSVxg4tQ6DEqPCzDuBQ_8LFE0jY1xQ0ZjFEyu7umEMF-7wU2vg0hCyQXRx01',
    status: 'resting',
    rating: 4.8,
    title: '青少年培训专家',
    tags: ['基础稳固', '耐心温和'],
    price: 120,
    activity: '累计 200+ 学员'
  },
  {
    id: 3,
    name: '王志强',
    avatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuA0hGBXqY_DSGK4IX9jewjiG0g9m8Sv_PY-S1tY3r5QZ_j8elN2eyOXu1ntdzETnjDmQrSIQf6MyyK8hNT4K_QBbqndTGEoYG5L3vyvulreYzhJtl5fE0MgVqM8lWTQwdG0zAccfF8vZs5xYjqQQoJd9MPXiarL8ucHK3oqWfPyvxib3f2p97bKCdK2Ba2i0D0Bc19oduFnfuxYUp4RbU5W8i-uDIjCR9QomQ0H4GTxpMZ38dLsVWLfRx1OwcLWZBEWEPpWCeknqmho',
    status: 'active',
    rating: 5.0,
    title: '前省队主教练',
    tags: ['战术大师', '职业指导'],
    price: 300,
    activity: '累计 120+ 职业球员'
  }
])

const filteredList = computed(() => {
  return coaches.value.filter(c => {
    if (currentTab.value === 1 && c.status !== 'active') return false
    if (currentTab.value === 2 && c.status !== 'resting') return false
    
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase()
      if (!c.name.includes(q) && !c.tags.some(t => t.includes(q)) && !c.title.includes(q)) {
        return false
      }
    }
    
    return true
  })
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function goAdd() {
  uni.navigateTo({ url: PRESIDENT_PAGES.COACH_FORM })
}

function goDetail(coach: any) {
  if (!coach || !coach.id) return
  uni.navigateTo({
    url: `${PRESIDENT_PAGES.COACH_DETAIL}?id=${encodeURIComponent(String(coach.id))}`
  })
}
</script>

<style lang="scss" scoped>
.coach-list-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding-bottom: 240rpx; /* Leave space for bottom bar */
  font-family: "Lexend", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f8fafc;
}

.nav-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #f8fafc;
  padding: 32rpx 48rpx;
  transition: all 0.3s ease;
}

.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 32rpx;

  .back-btn {
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

  .nav-title {
    font-size: 44rpx;
    font-weight: 800;
    color: #18181b;
    letter-spacing: -0.025em;
  }
}

.nav-right {
  .add-btn {
    width: 80rpx;
    height: 80rpx;
    background-color: #ffedd5;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.2s;
    
    &:active {
      transform: scale(0.95);
    }
  }
}

.main-content {
  padding: 24rpx 48rpx 48rpx;
  max-width: 1200rpx;
  margin: 0 auto;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 48rpx;
  margin-bottom: 64rpx;
  
  @media (min-width: 768px) {
    flex-direction: row;
    align-items: flex-end;
  }
}

.search-wrap {
  flex: 1;

  .section-label {
    display: block;
    font-size: 20rpx;
    font-weight: 600;
    color: #71717a;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    margin-bottom: 16rpx;
  }

  .search-box {
    position: relative;
    background-color: #ffffff;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    box-shadow: 0 2rpx 4rpx rgba(0,0,0,0.02);
    transition: all 0.2s ease;
    
    &:focus-within {
      box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
      border-bottom: 4rpx solid #ea580c;
    }

    .search-icon {
      position: absolute;
      left: 32rpx;
    }

    .search-input {
      flex: 1;
      height: 110rpx;
      padding: 0 32rpx 0 96rpx;
      font-size: 28rpx;
      color: #1a1c1c;
      background: transparent;
    }

    .input-placeholder {
      color: #a1a1aa;
    }
  }
}

.tabs-wrap {
  display: flex;
  background-color: #f3f3f3;
  padding: 8rpx;
  border-radius: 24rpx;
  
  .tab-item {
    flex: 1;
    padding: 24rpx 32rpx;
    text-align: center;
    border-radius: 16rpx;
    font-size: 28rpx;
    font-weight: 600;
    color: #71717a;
    transition: all 0.3s ease;
    white-space: nowrap;
    
    &.active {
      background-color: #ffffff;
      color: #ea580c;
      box-shadow: 0 2rpx 4rpx rgba(0,0,0,0.04);
    }
  }
}

.coach-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 48rpx;
  
  @media (min-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
}

.coach-card {
  position: relative;
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 48rpx;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  overflow: hidden;
  
  /* Active state for uniapp */
  &:active {
    transform: scale(0.98);
  }
  
  @media (hover: hover) {
    &:hover {
      transform: translateY(-4rpx);
      box-shadow: 0 12rpx 40rpx rgba(0,0,0,0.04);
    }
    
    &:hover .card-actions {
       opacity: 1;
    }
  }

  .card-actions {
    position: absolute;
    top: 32rpx;
    right: 32rpx;
    display: flex;
    gap: 16rpx;
    opacity: 1; // Uniapp doesn't handle hover great consistently on mobile, default visible
    transition: opacity 0.3s ease;
  }

  .action-btn {
    width: 64rpx;
    height: 64rpx;
    background-color: #f4f4f5;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:active {
      background-color: #e4e4e7;
    }
    
    &.delete:active {
      background-color: #fee2e2;
    }
  }
}

.card-top {
  display: flex;
  gap: 40rpx;
  align-items: flex-start;
  margin-bottom: 48rpx;
}

.avatar-wrap {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  border: 8rpx solid #fff7ed;
  flex-shrink: 0;
  
  .avatar {
    width: 100%;
    height: 100%;
    border-radius: 24rpx;
  }

  .status-indicator {
    position: absolute;
    bottom: -8rpx;
    right: -8rpx;
    width: 48rpx;
    height: 48rpx;
    border-radius: 50%;
    border: 8rpx solid #ffffff;
    
    &.active {
      background-color: #22c55e;
    }
    
    &.resting {
      background-color: #d4d4d8;
    }
  }
}

.info-wrap {
  flex: 1;
  min-width: 0;
  
  .name-row {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8rpx;
    
    .name {
      font-size: 40rpx;
      font-weight: 700;
      color: #1a1c1c;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .rating-badge {
      display: flex;
      align-items: center;
      gap: 8rpx;
      background-color: #fff7ed;
      padding: 4rpx 16rpx;
      border-radius: 12rpx;
      flex-shrink: 0;
      
      .rating-val {
        font-size: 24rpx;
        font-weight: 700;
        color: #ea580c;
      }
    }
  }

  .title {
    display: block;
    font-size: 28rpx;
    font-weight: 500;
    color: #71717a;
    margin-bottom: 24rpx;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .tags-row {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    
    .tag {
      font-size: 20rpx;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      background-color: #e2dfde;
      color: #636262;
      padding: 8rpx 16rpx;
      border-radius: 99px;
    }
  }
}

.card-bottom {
  border-top: 2rpx solid #f8fafc;
  padding-top: 48rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .label {
    display: block;
    font-size: 20rpx;
    font-weight: 700;
    color: #a1a1aa;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    margin-bottom: 8rpx;
  }

  .price-side {
    .price-val {
      display: flex;
      align-items: baseline;
      
      .currency {
        font-size: 28rpx;
        font-weight: 700;
        color: #ea580c;
      }
      
      .amount {
        font-size: 40rpx;
        font-weight: 700;
        color: #ea580c;
        margin: 0 4rpx;
      }
      
      .unit {
        font-size: 24rpx;
        color: #a1a1aa;
      }
    }
  }

  .activity-side {
    text-align: right;
    
    .activity-val {
      font-size: 28rpx;
      font-weight: 700;
      color: #1a1c1c;
    }
  }
}

.add-placeholder {
  background-color: #f3f3f3;
  border: 4rpx dashed #e4e4e7;
  border-radius: 24rpx;
  padding: 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 280rpx;
  transition: all 0.2s ease;
  cursor: pointer;
  
  &:active {
    background-color: #f4f4f5;
  }
  
  @media (hover: hover) {
    &:hover {
      border-color: #fdba74;
      .add-text { color: #fb923c; }
    }
  }

  .add-text {
    margin-top: 16rpx;
    font-size: 28rpx;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    color: #a1a1aa;
    transition: color 0.2s;
  }
}

.fab-btn {
  position: fixed;
  bottom: 240rpx;
  right: 48rpx;
  width: 120rpx;
  height: 120rpx;
  background-color: #ea580c;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(234, 88, 12, 0.4);
  z-index: 1001; /* Above TabBar */
  transition: transform 0.2s ease;
  
  &:active {
    transform: scale(0.9);
  }
}
</style>
