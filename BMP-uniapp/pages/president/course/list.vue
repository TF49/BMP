<template>
  <PresidentLayout :showTabBar="true">
    <view class="course-list-page">
      <view class="status-bar-placeholder"></view>
      
      <!-- TopAppBar Navigation -->
      <view class="nav-header">
        <view class="nav-row">
          <view class="nav-left" @click="goBack">
            <view class="back-btn">
              <uni-icons type="arrow-left" size="24" color="#1a1c1c"></uni-icons>
            </view>
            <text class="nav-title">课程管理</text>
          </view>
          <view class="nav-right">
            <view class="notification-btn">
              <uni-icons type="notification" size="24" color="#71717a"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <view class="main-content">
        <!-- Search and Filter Section -->
        <view class="filter-section">
          <view class="search-wrap">
            <view class="search-box">
              <uni-icons type="search" size="20" color="#a1a1aa" class="search-icon"></uni-icons>
              <input 
                class="search-input" 
                placeholder="搜索课程或教练..." 
                placeholder-class="input-placeholder"
                v-model="searchQuery"
              />
            </view>
          </view>

          <scroll-view scroll-x class="tabs-scroll" :show-scrollbar="false">
            <view class="tabs-wrap">
              <view 
                class="tab-item" 
                :class="{ active: currentTab === 0 }" 
                @click="currentTab = 0">全部</view>
              <view 
                class="tab-item" 
                :class="{ active: currentTab === 1 }" 
                @click="currentTab = 1">私教课</view>
              <view 
                class="tab-item" 
                :class="{ active: currentTab === 2 }" 
                @click="currentTab = 2">团课</view>
              <view 
                class="tab-item" 
                :class="{ active: currentTab === 3 }" 
                @click="currentTab = 3">入门班</view>
            </view>
          </scroll-view>
        </view>

        <!-- Course List -->
        <view class="course-grid">
          
          <!-- Course Card 1: 零基础新手入门班 -->
          <view class="course-card">
            <view class="card-img-wrap">
              <image class="card-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCaz7cdhkujT3alxalKpRLEmy8PuEUZQTbizYx78bO_Fv43w2CuDfFKloXnj_IJaax_1f07aoUHsZ-6biT6hlV11tgU6eBWekw43gYS-c8IFMwvejUdQmt6xTAN3zLfNCv1fv-er6er_fWada2ym-NhYJWQ3RAqHFuCZlPA_AM9nF9akG3Lak4dIgs60-hntxuPuHFQsGDgYtxFswG8XdSWtm_1f1M-0S-AJ-u6Dlhdk77XHg6aZgnyodcbv6i-AuaT3LAvzhOwdjXB" mode="aspectFill"></image>
              <view class="status-badge status-active">报名中</view>
            </view>
            <view class="card-content">
              <view class="title-row">
                <text class="title">零基础新手入门班</text>
                <text class="price">¥299</text>
              </view>
              <view class="coach-row">
                <uni-icons type="person" size="14" color="#a1a1aa"></uni-icons>
                <text class="coach-name">李教练 (Lee Zii Jia)</text>
              </view>
              
              <view class="details-box">
                <view class="time-row">
                  <view class="time-item">
                    <uni-icons type="calendar" size="12" color="#a1a1aa"></uni-icons>
                    <text class="time-text">10月24日 - 11月10日</text>
                  </view>
                  <view class="time-item">
                    <uni-icons type="info" size="12" color="#a1a1aa"></uni-icons>
                    <text class="time-text">19:00 - 21:00</text>
                  </view>
                </view>
                
                <view class="progress-wrap">
                  <view class="progress-bar">
                    <view class="progress-inner bg-primary" :style="{ width: '80%' }"></view>
                  </view>
                  <view class="progress-labels">
                    <text class="progress-label">当前报名</text>
                    <text class="progress-value text-primary">8/10 人</text>
                  </view>
                </view>
              </view>
              
              <view class="action-row">
                <button class="btn btn-secondary">编辑</button>
                <button class="btn btn-primary">查看详情</button>
              </view>
            </view>
          </view>
          
          <!-- Course Card 2: 高阶杀球专项私教 -->
          <view class="course-card">
            <view class="card-img-wrap">
              <image class="card-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCd7lY1jKQfOQ9NMWEMlhCkRIK85cBA2zNZSOxp19-XWaCpDw4JXhVyCB7uGZY7GjM_cUFrocjzkCa3aBPMAE5jkD2DWDkeAij04csPSf5bpOylWUqY1BSHglEVOYShbZqcleAcaOwwZJc3h50gAZ51R7hJP9RYTDBKle1caQ3HP_rBBwugwvAu23D2ui6zMq9iH278gGDSzuGn6eAB6s9S88T4d72HrYsEkAxLkq19nV33Y7pMJGkWEToYbQP2WRNGR2pQXKm-M3aE" mode="aspectFill"></image>
              <view class="status-badge status-progress">进行中</view>
            </view>
            <view class="card-content">
              <view class="title-row">
                <text class="title">高阶杀球专项私教</text>
                <text class="price text-primary">¥580</text>
              </view>
              <view class="coach-row">
                <uni-icons type="person" size="14" color="#a1a1aa"></uni-icons>
                <text class="coach-name">张教练 (Coach Zhang)</text>
              </view>
              
              <view class="details-box">
                <view class="time-row">
                  <view class="time-item">
                    <uni-icons type="calendar" size="12" color="#a1a1aa"></uni-icons>
                    <text class="time-text">每周三/五</text>
                  </view>
                  <view class="time-item">
                    <uni-icons type="info" size="12" color="#a1a1aa"></uni-icons>
                    <text class="time-text">14:00 - 16:00</text>
                  </view>
                </view>
                
                <view class="progress-wrap">
                  <view class="progress-bar">
                    <view class="progress-inner bg-tertiary" :style="{ width: '100%' }"></view>
                  </view>
                  <view class="progress-labels">
                    <text class="progress-label">当前报名</text>
                    <text class="progress-value text-tertiary">1/1 人</text>
                  </view>
                </view>
              </view>
              
              <view class="action-row">
                <button class="btn btn-secondary">编辑</button>
                <button class="btn btn-primary">查看详情</button>
              </view>
            </view>
          </view>
          
          <!-- Course Card 3: 暑期少年集训营 (Ended) -->
          <view class="course-card ended">
            <view class="card-img-wrap">
              <image class="card-img grayscale" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBuXUU1z6WL6qzQlxbHhKOqK9PkfpwvYCw5vcjq-pYkOJ154jnDt_NEarkoF9lRl0SxcPGR-aKFP0cXnT_PCWennXDRvmub3I3MPYafu8-iwZOnckK5PF8TzFXctR8pjkBG4yyaAJVIb9-GjDnhO1x7DjUnML5wLtKL9F9gRCkY0BpXelmlMO8ikPSKOmnvgOWxByMo-yE7gIX5v6V3xWGjJOqZYALvVkzuehshxCNCRrllgmvV6MtbGwd4Kybv8EtxqFVUlxLgQhox" mode="aspectFill"></image>
              <view class="status-badge status-ended">已结束</view>
            </view>
            <view class="card-content">
              <view class="title-row">
                <text class="title">暑期少年集训营</text>
                <text class="price text-secondary">¥1200</text>
              </view>
              <view class="coach-row">
                <uni-icons type="person" size="14" color="#a1a1aa"></uni-icons>
                <text class="coach-name">王教练 (Coach Wang)</text>
              </view>
              
              <view class="details-box">
                <view class="time-row">
                  <view class="time-item">
                    <uni-icons type="calendar" size="12" color="#a1a1aa"></uni-icons>
                    <text class="time-text">7月15日 - 8月30日</text>
                  </view>
                </view>
                
                <view class="progress-wrap">
                  <view class="progress-bar">
                    <view class="progress-inner bg-secondary" :style="{ width: '100%' }"></view>
                  </view>
                  <view class="progress-labels">
                    <text class="progress-label">最终人数</text>
                    <text class="progress-value">24/24 人</text>
                  </view>
                </view>
              </view>
              
              <view class="action-row">
                <button class="btn btn-secondary">数据统计</button>
                <button class="btn btn-primary dark">历史记录</button>
              </view>
            </view>
          </view>
          
          <!-- Course Card 4: 步伐专项：球场飞行艺术 -->
          <view class="course-card feature-card">
            <view class="card-img-wrap">
              <image class="card-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAKVAC6fWIwoAIJDadWI5ic0LEJ43-3u3_khK0-1PdQRRndx5wmIl3VU9nrgjxwpSqgbmXbwC9zLCsoElhlPdSTRUDjtjUqEGVFL6YpTn9kb_JdfBjOq-FkkG2h7qwl9iM_J7HvKrE2UEjkVP83JlNsXM2ZHB0eLvCkK64xSAuUAaYdF1yctiJTWkeHPOWTKdZ0QRmX05EEBLlrA8-feJ64pSpsYvz9RU5ut6CNhUTZxl7eWO4VWOdKeuuhyfv1Z6d6KBstvVmdaCWn" mode="aspectFill"></image>
              <view class="status-badge status-active">热抢中</view>
            </view>
            <view class="card-content pd-extra">
              <view class="course-type">精品团课</view>
              <view class="title-row mb-more">
                <text class="title text-lg">步伐专项：球场飞行艺术</text>
                <text class="price text-xl">¥399</text>
              </view>
              <view class="coach-row coach-feature">
                <view class="coach-avatar">C.L</view>
                <text class="coach-name font-dark">陈教练 (Chen Long)</text>
              </view>
              
              <view class="grid-details">
                <view class="grid-item">
                  <text class="grid-label">训练周期</text>
                  <text class="grid-val">11月01日 - 11月20日</text>
                </view>
                <view class="grid-item">
                  <text class="grid-label">训练时段</text>
                  <text class="grid-val">周末下午 15:00</text>
                </view>
              </view>
              
              <view class="enrollment-status">
                <view class="avatars">
                  <image class="avatar-small" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDP4I9QST4TCZBmcQGQ3l6MjU_AaZcxj9SPS9mBPjWY1UC5MQfDY8Q0lnVEKzFycX8UDS-apK76dn9U_VfUKlSa5-xvksfdmrvotfMtVxCTQe-XQz44Vis3QkXkwaijK9IZ2kku505LfHdRssG07tjeW21nGuis2iviaDRIlyBVwBSfBx-kSrftoaTvl2QJidaiEBVe_DFopuY3rJlJUPzAjxXqwj_Am5NOr1bGTDHk_v84QyxhgwWX5wroXViMwbZ4j9lyrENxG5rs" mode="aspectFill"></image>
                  <image class="avatar-small" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAnrO1yFljBCEY2VAF6wzc6ujt0ukXnrlo0d65qIbMA_DhXrajIJSL6NAM2sosE9Pl4SaMi6Tk77fPnSIsW8vGPRM-SLk-26zwv9snt_KP_6__qEBs7b9hpqLwhIMRPjjZ5RqxBGU3zByC6O-eRSJiQt6SvXq0ufxuoik19GM652SYoEUIJDy3ZWwjZalpf117JozqkdwvsAOZoJt028SkGL5gJxKVNYxic30xhM7rGUDFAlOoXYEuXCvelKxL45nj-kCjsEBlzp7SA" mode="aspectFill"></image>
                  <image class="avatar-small" src="https://lh3.googleusercontent.com/aida-public/AB6AXuCivsQ42mc6CCA7IxjovCx1Oal9f421sj_SQHIWyKHil5hpgKxWkBUtzd3dwkLw8LEBmEE0nqdN7j4SPtc-4SEY1Oix9AYv0RiDk0bHkA_hEjJQQS00P16-ZyswK-6k4zZUauErNvSMqQquyWrBOD7Zh3aYs8PpzX7nEh_K1YfULRnA7yECuuvzijIeAO8CNtojpWVt1CHzmXDITArC7Xl7PcKbq-IzMmDOkOSYIlMk3WUI2F4Y8KPt92Iyai9pFLGGr89FHVA5jovg" mode="aspectFill"></image>
                  <view class="avatar-more">+12</view>
                </view>
                <text class="remaining-spots">剩余 5 个名额</text>
              </view>
              
              <view class="action-row feature-actions">
                <button class="btn btn-action-primary">发布管理</button>
                <button class="btn btn-action-more">
                  <uni-icons type="more-filled" size="20" color="#1a1c1c"></uni-icons>
                </button>
              </view>
            </view>
          </view>
          
          <!-- Course Card 5: Stats Placeholder -->
          <view class="stats-card">
            <view class="stats-content">
              <view class="stats-top">
                <text class="stats-title">本月活跃课程</text>
                <text class="stats-number">12</text>
              </view>
              <view class="stats-bottom">
                <text class="stats-subtitle">营收环比增长</text>
                <view class="stats-growth">
                  <text class="growth-val">+24%</text>
                  <uni-icons type="arrow-up" size="20" color="#ffffff" class="growth-icon"></uni-icons>
                </view>
              </view>
            </view>
            <view class="stats-bg-decor"></view>
          </view>
          
        </view>
      </view>

      <!-- FAB: Post Course -->
      <view class="fab-btn" @click="goAdd">
        <uni-icons type="plusempty" size="28" color="#ffffff"></uni-icons>
      </view>
      
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const currentTab = ref(0)
const searchQuery = ref('')

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function goAdd() {
  // Navigate to add course page (placeholder for now)
  uni.showToast({ title: '发布课程功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.course-list-page {
  min-height: 100vh;
  background-color: #f9f9f9;
  padding-bottom: 240rpx;
  font-family: "Lexend", -apple-system, sans-serif;
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
  .notification-btn {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    &:active {
      background-color: #e4e4e7;
    }
  }
}

.main-content {
  padding: 24rpx 40rpx 48rpx;
  max-width: 1200rpx;
  margin: 0 auto;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  margin-bottom: 48rpx;
}

.search-wrap {
  .search-box {
    position: relative;
    background-color: #ffffff;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.02);
    
    .search-icon {
      position: absolute;
      left: 32rpx;
    }

    .search-input {
      flex: 1;
      height: 100rpx;
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

.tabs-scroll {
  width: 100%;
}

.tabs-wrap {
  display: flex;
  gap: 16rpx;
  padding-bottom: 8rpx;
  
  .tab-item {
    padding: 16rpx 48rpx;
    background-color: #ffffff;
    color: #5f5e5e;
    font-size: 28rpx;
    font-weight: 600;
    border-radius: 999rpx;
    white-space: nowrap;
    transition: all 0.2s ease;
    
    &.active {
      background-color: #ff6600;
      color: #ffffff;
    }
  }
}

.course-grid {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.course-card {
  background-color: #ffffff;
  border-radius: 32rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 40rpx rgba(0,0,0,0.02);
  border-bottom: 4rpx solid transparent;
  transition: all 0.3s;
  
  &.ended {
    opacity: 0.75;
    .grayscale {
      filter: grayscale(100%);
    }
  }
  
  .card-img-wrap {
    position: relative;
    height: 360rpx;
    width: 100%;
    
    .card-img {
      width: 100%;
      height: 100%;
    }
    
    .status-badge {
      position: absolute;
      top: 32rpx;
      right: 32rpx;
      padding: 8rpx 24rpx;
      border-radius: 999rpx;
      color: #ffffff;
      font-size: 20rpx;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      
      &.status-active { background-color: #a33e00; }
      &.status-progress { background-color: #0062a1; }
      &.status-ended { background-color: #5f5e5e; }
    }
  }
  
  .card-content {
    padding: 40rpx;
    
    &.pd-extra {
      padding: 48rpx;
    }
  }
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16rpx;
  
  .title {
    font-size: 36rpx;
    font-weight: 700;
    color: #1a1c1c;
    line-height: 1.2;
    flex: 1;
    padding-right: 24rpx;
    
    &.text-lg {
      font-size: 44rpx;
    }
  }
  
  .price {
    font-size: 36rpx;
    font-weight: 700;
    color: #a33e00;
    
    &.text-primary { color: #ea580c; }
    &.text-secondary { color: #5f5e5e; }
    &.text-xl { 
      font-size: 56rpx; 
      font-weight: 900;
    }
  }
}

.course-type {
  font-size: 20rpx;
  font-weight: 900;
  text-transform: uppercase;
  color: #a33e00;
  margin-bottom: 8rpx;
}

.coach-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 32rpx;
  
  .coach-name {
    font-size: 26rpx;
    color: #5f5e5e;
    
    &.font-dark {
      color: #1a1c1c;
      font-weight: 500;
    }
  }
}

.coach-feature {
  margin-bottom: 48rpx;
  
  .coach-avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background-color: #ffedd5;
    color: #a33e00;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24rpx;
    font-weight: 700;
  }
}

.details-box {
  margin-bottom: 48rpx;
  
  .time-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16rpx;
    
    .time-item {
      display: flex;
      align-items: center;
      gap: 8rpx;
      
      .time-text {
        font-size: 24rpx;
        color: #5f5e5e;
      }
    }
  }
  
  .progress-wrap {
    .progress-bar {
      width: 100%;
      height: 12rpx;
      background-color: #eeeeee;
      border-radius: 999rpx;
      overflow: hidden;
      margin-bottom: 16rpx;
      
      .progress-inner {
        height: 100%;
        border-radius: 999rpx;
        
        &.bg-primary { background-color: #a33e00; }
        &.bg-tertiary { background-color: #0062a1; }
        &.bg-secondary { background-color: #5f5e5e; }
      }
    }
    
    .progress-labels {
      display: flex;
      justify-content: space-between;
      text-transform: uppercase;
      font-size: 20rpx;
      font-weight: 700;
      letter-spacing: 0.05em;
      
      .progress-label {
        color: #5f5e5e;
      }
      
      .progress-value {
        color: #1a1c1c;
        
        &.text-primary { color: #a33e00; }
        &.text-tertiary { color: #0062a1; }
      }
    }
  }
}

.grid-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48rpx;
  margin-bottom: 48rpx;
  
  .grid-item {
    .grid-label {
      display: block;
      font-size: 20rpx;
      text-transform: uppercase;
      font-weight: 700;
      color: #a1a1aa;
      margin-bottom: 8rpx;
    }
    .grid-val {
      font-size: 28rpx;
      font-weight: 700;
      color: #1a1c1c;
    }
  }
}

.enrollment-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
  
  .avatars {
    display: flex;
    align-items: center;
    
    .avatar-small {
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      border: 4rpx solid #ffffff;
      margin-right: -16rpx;
    }
    
    .avatar-more {
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
      border: 4rpx solid #ffffff;
      background-color: #e8e8e8;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16rpx;
      font-weight: 700;
      z-index: 10;
    }
  }
  
  .remaining-spots {
    font-size: 24rpx;
    font-weight: 700;
    color: #a33e00;
  }
}

.action-row {
  display: flex;
  gap: 16rpx;
  
  .btn {
    flex: 1;
    padding: 24rpx 0;
    border-radius: 16rpx;
    font-size: 28rpx;
    font-weight: 500;
    text-align: center;
    line-height: 1;
    border: none;
    
    &::after { display: none; }
    
    &.btn-secondary {
      background-color: #e8e8e8;
      color: #1a1c1c;
    }
    
    &.btn-primary {
      background-color: #5f5e5e;
      color: #ffffff;
      
      &.dark {
        background-color: #1a1c1c;
      }
    }
  }
}

.feature-actions {
  .btn {
    font-weight: 700;
    border-radius: 24rpx;
    padding: 32rpx 0;
    
    &.btn-action-primary {
      background-color: #ff6600;
      color: #561d00;
      flex: 3;
    }
    &.btn-action-more {
      background-color: #e8e8e8;
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}

.stats-card {
  background-color: #a33e00;
  border-radius: 32rpx;
  padding: 64rpx;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 400rpx;
  
  .stats-content {
    position: relative;
    z-index: 10;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    flex: 1;
  }
  
  .stats-top {
    .stats-title {
      display: block;
      font-size: 28rpx;
      font-weight: 500;
      color: rgba(255,255,255,0.8);
      margin-bottom: 8rpx;
    }
    .stats-number {
      font-size: 100rpx;
      font-weight: 900;
      font-style: italic;
      letter-spacing: -0.05em;
      color: #ffffff;
      line-height: 1;
    }
  }
  
  .stats-bottom {
    margin-top: 64rpx;
    .stats-subtitle {
      display: block;
      font-size: 20rpx;
      text-transform: uppercase;
      font-weight: 700;
      letter-spacing: 0.2em;
      color: #ffffff;
      margin-bottom: 16rpx;
    }
    .stats-growth {
      display: flex;
      align-items: center;
      gap: 16rpx;
      .growth-val {
        font-size: 56rpx;
        font-weight: 700;
        color: #ffffff;
      }
      .growth-icon {
        margin-bottom: 8rpx;
      }
    }
  }
  
  .stats-bg-decor {
    position: absolute;
    bottom: -80rpx;
    right: -80rpx;
    width: 320rpx;
    height: 320rpx;
    background-color: rgba(255,255,255,0.05);
    border-radius: 50%;
    filter: blur(40px);
    z-index: 1;
  }
}

.fab-btn {
  position: fixed;
  bottom: 240rpx;
  right: 48rpx;
  width: 112rpx;
  height: 112rpx;
  background-color: #ff6600;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 48rpx rgba(255, 102, 0, 0.4);
  z-index: 1001; /* Above TabBar */
  transition: transform 0.2s ease;
  
  &:active {
    transform: scale(0.9);
  }
}
</style>
