<template>
  <view class="coach-detail-page">
    <!-- Header -->
    <view class="nav-header">
      <view class="status-bar-placeholder"></view>
      <view class="nav-content">
        <view class="nav-left" @click="goBack">
          <uni-icons type="left" size="24" color="#a33e00"></uni-icons>
          <text class="nav-title">教练详情</text>
        </view>
      </view>
    </view>

    <scroll-view class="main-content" scroll-y :show-scrollbar="false">
      <!-- Hero Section -->
      <view class="hero-section">
        <image class="hero-bg" src="https://lh3.googleusercontent.com/aida-public/AB6AXuBRxzwTBjTJ2hUvfLvdZtYV2eSQMZaRYfBJZF4-I_SxLjriXlM05_K1sP5mLIsm9cuTo_JGfHkDeVjvuSUrr9NyJTe6SeQr5k21-v4g62s_B3byHvQGR-0uj9L3tMqbcsHS9U_Og54IO8i58LoAsElcyMBeNLD3gLiYaeud0uZdEyzDsF3_CofgVCTcVDGnjrZuTlLw99rq-pdGuE1fvXx48WlEUy1_bJpJBGgAVnZVorzwpfdWJJtbn0WWKmLH1rbGMe1Cr-8NacUD" mode="aspectFill"></image>
        <view class="hero-gradient"></view>
        <view class="hero-content">
          <view class="badge">ELITE COACH</view>
          <view class="name">陈伟杰 (Coach Chen)</view>
          <view class="rating-row">
            <view class="stars">
              <uni-icons type="star-filled" size="18" color="#ff6600" v-for="i in 4" :key="i"></uni-icons>
              <uni-icons type="starhalf" size="18" color="#ff6600"></uni-icons>
            </view>
            <text class="score">4.9</text>
            <text class="reviews">(128条评价)</text>
          </view>
        </view>
      </view>

      <!-- Stats section -->
      <view class="stats-section">
        <view class="stat-card">
          <text class="stat-label">执教年限</text>
          <text class="stat-value text-primary">15</text>
          <text class="stat-sub">YEARS EXP.</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">学员总数</text>
          <text class="stat-value text-primary">1.2k+</text>
          <text class="stat-sub">STUDENTS</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">学员满意度</text>
          <text class="stat-value text-primary">98%</text>
          <text class="stat-sub">SATISFACTION</text>
        </view>
        <view class="stat-card">
          <text class="stat-label">专业等级</text>
          <text class="stat-value text-primary">精英级</text>
          <text class="stat-sub">ELITE LEVEL</text>
        </view>
      </view>

      <!-- Professional Info & Bio -->
      <view class="info-section">
        <view class="section-title">
          <view class="title-bar"></view>
          <text>教练简介</text>
        </view>
        <view class="bio-text">
          作为前国家队资深队员，陈教练在羽毛球教学领域深耕15年。他擅长精准的技术拆解与战术分析，尤其在“高爆发进攻”与“步法协同”领域拥有独特的教学体系。他不仅关注技术的提升，更注重运动员心理素质的培养，已帮助数百名青少年及成人学员在地区级比赛中斩获佳绩。
        </view>

        <view class="section-title mt-8">
          <view class="title-bar"></view>
          <text>技术特长</text>
        </view>
        <view class="tags-container">
          <view class="tag">
            <uni-icons type="bolt" size="16" color="#5f5e5e"></uni-icons>
            <text>高级进攻战术</text>
          </view>
          <view class="tag">
            <uni-icons type="refresh" size="16" color="#5f5e5e"></uni-icons>
            <text>步法灵活性训练</text>
          </view>
          <view class="tag">
            <uni-icons type="heart" size="16" color="#5f5e5e"></uni-icons>
            <text>比赛心理调节</text>
          </view>
          <view class="tag">
            <uni-icons type="pyramid" size="16" color="#5f5e5e"></uni-icons>
            <text>专项体能强化</text>
          </view>
        </view>

        <view class="certifications-box">
          <view class="cert-title">执教资质</view>
          <view class="cert-item">
            <uni-icons type="checkbox-filled" size="20" color="#a33e00"></uni-icons>
            <text>国家一级羽毛球教练员证</text>
          </view>
          <view class="cert-item">
            <uni-icons type="checkbox-filled" size="20" color="#a33e00"></uni-icons>
            <text>BWF 世界羽联认证高级教练</text>
          </view>
          <view class="cert-item">
            <uni-icons type="checkbox-filled" size="20" color="#a33e00"></uni-icons>
            <text>专项体能康复师认证</text>
          </view>
        </view>
      </view>

      <!-- Available Courses -->
      <view class="courses-section">
        <view class="courses-header">
          <view class="section-title">
            <view class="title-bar"></view>
            <text>开设课程</text>
          </view>
          <text class="view-all">查看全部</text>
        </view>
        
        <view class="course-list">
          <!-- Course Card 1 -->
          <view class="course-card">
            <image class="course-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAkFBPKERZPfUdBLq6ytAcAI9S8zuXRlNFMbMJiemWrTPMc3_43hW0nX_tGXy4OSvo3Zn2HLrCJG7S5NfByYWENpuWMf8NwQOt7eGs_8OLIh05d1dmNAZ2osJoh4NPlSPZJDNY5eATUxRftgIphs-WkC4Qc46mi3hqeioxd8_zWEpm3XdQTeXSrwsx0p4wYYTzlJbry5oNQ4TFA-WpxUs6quSbvQUzfAHqt05KLqIRsWXOBvNQCSI4CRIPBvjY-4pft66XNXRrzRxlQ" mode="aspectFill"></image>
            <view class="course-info">
              <view class="course-level">ADVANCED / 进阶</view>
              <view class="course-title">暴风进攻：杀球专项强化班</view>
              <view class="course-meta">
                <view class="meta-item">
                  <uni-icons type="calendar" size="14" color="#5f5e5e"></uni-icons>
                  <text>12 课时</text>
                </view>
                <view class="meta-item">
                  <uni-icons type="staff" size="14" color="#5f5e5e"></uni-icons>
                  <text>4人班</text>
                </view>
              </view>
            </view>
          </view>

          <!-- Course Card 2 -->
          <view class="course-card">
            <image class="course-img" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDfnbrujmADJA-4E_b_aoH-ek1d_TL4irF9LfP_Jw6TbWNQxN2fjaU2XFpglUJT5RoNQxzDvJUE4t457l9QY7hdOD8Y4ZBXuxl4yHnqWVUWXa-ZGcaWVcA3Jqrc6nUBG0MI4R1_XQz34md5M_ROkY8LSdDbbeLc63tZffde9OBNYNVwbUMAzu2145QRYAykSjLzpFEJ4Yd2Gj8S2DrIzzdARxBgrGSoerizrxsQKnAL5Wx9zfzn6wXflzFuu-cYaoZT2ZwHW3kFLbE1" mode="aspectFill"></image>
            <view class="course-info">
              <view class="course-level">INTERMEDIATE / 中级</view>
              <view class="course-title">凌云步法：场上移动效率提升</view>
              <view class="course-meta">
                <view class="meta-item">
                  <uni-icons type="calendar" size="14" color="#5f5e5e"></uni-icons>
                  <text>8 课时</text>
                </view>
                <view class="meta-item">
                  <uni-icons type="person" size="14" color="#5f5e5e"></uni-icons>
                  <text>1对1</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="safe-bottom"></view>
    </scroll-view>

    <!-- Footer CTA -->
    <view class="footer-cta">
      <view class="price-box">
        <text class="price-label">单课起价</text>
        <view class="price-value">
          <text class="currency">¥</text>
          <text class="amount">480</text>
          <text class="unit">/ 小时</text>
        </view>
      </view>
      <view class="btn-book" @click="handleBook">
        <text>预约教练</text>
        <uni-icons type="arrow-right" size="20" color="#561d00"></uni-icons>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const goBack = () => {
  uni.navigateBack()
}

const handleBook = () => {
  uni.showToast({
    title: '预约成功',
    icon: 'success'
  })
}
</script>

<style lang="scss" scoped>
.coach-detail-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f9f9f9;
}

.status-bar-placeholder {
  height: var(--status-bar-height);
  background-color: #f4f4f5;
}

.nav-header {
  background-color: #f4f4f5;
  position: relative;
  z-index: 100;
}

.nav-content {
  display: flex;
  align-items: center;
  padding: 0 32rpx;
  height: 88rpx;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  
  .nav-title {
    font-size: 36rpx;
    font-weight: 900;
    color: #1a1c1c;
  }
}

.main-content {
  flex: 1;
  overflow-y: auto;
}

.hero-section {
  position: relative;
  width: 100%;
  height: 1060rpx;
  overflow: hidden;
  
  .hero-bg {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .hero-gradient {
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, rgba(249,249,249,0) 0%, rgba(249,249,249,1) 100%);
  }
  
  .hero-content {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    padding: 64rpx 48rpx;
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    
    .badge {
      background-color: #ff6600;
      color: #561d00;
      width: fit-content;
      padding: 8rpx 24rpx;
      border-radius: 999px;
      font-size: 24rpx;
      font-weight: 700;
      letter-spacing: 2rpx;
    }
    
    .name {
      font-size: 64rpx;
      font-weight: 700;
      letter-spacing: -2rpx;
      color: #1a1c1c;
      margin-top: 8rpx;
    }
    
    .rating-row {
      display: flex;
      align-items: center;
      gap: 16rpx;
      margin-top: 8rpx;
      
      .stars {
        display: flex;
        color: #ff6600;
      }
      
      .score {
        font-weight: 700;
        font-size: 32rpx;
      }
      
      .reviews {
        color: #5f5e5e;
        font-size: 26rpx;
        margin-left: 16rpx;
      }
    }
  }
}

.stats-section {
  padding: 0 48rpx;
  margin-top: -32rpx;
  position: relative;
  z-index: 10;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24rpx;
  
  .stat-card {
    background-color: #ffffff;
    padding: 32rpx;
    border-radius: 24rpx;
    box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.04);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    
    .stat-label {
      color: #5f5e5e;
      font-size: 20rpx;
      font-weight: 700;
      letter-spacing: 2rpx;
      margin-bottom: 8rpx;
    }
    
    .stat-value {
      font-size: 48rpx;
      font-weight: 900;
      &.text-primary { color: #a33e00; }
    }
    
    .stat-sub {
      font-size: 20rpx;
      color: #5a4136;
      margin-top: 8rpx;
    }
  }
}

.info-section {
  padding: 64rpx 48rpx 0;
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 16rpx;
    margin-bottom: 32rpx;
    
    .title-bar {
      width: 8rpx;
      height: 36rpx;
      background-color: #ff6600;
      border-radius: 999px;
    }
    
    text {
      font-size: 40rpx;
      font-weight: 700;
      color: #1a1c1c;
      letter-spacing: -1rpx;
    }
  }
  
  .mt-8 { margin-top: 64rpx; }
  
  .bio-text {
    color: #5f5e5e;
    line-height: 1.6;
    font-size: 30rpx;
  }
  
  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    
    .tag {
      background-color: #e2dfde;
      color: #636262;
      padding: 16rpx 28rpx;
      border-radius: 16rpx;
      font-weight: 500;
      display: flex;
      align-items: center;
      gap: 12rpx;
      font-size: 26rpx;
    }
  }
  
  .certifications-box {
    background-color: #ffdbcd;
    padding: 36rpx;
    border-radius: 32rpx;
    margin-top: 48rpx;
    display: flex;
    flex-direction: column;
    gap: 24rpx;
    
    .cert-title {
      color: #7c2e00;
      font-weight: 700;
      font-size: 32rpx;
      margin-bottom: 8rpx;
    }
    
    .cert-item {
      display: flex;
      align-items: flex-start;
      gap: 16rpx;
      
      text {
        color: #7c2e00;
        font-size: 26rpx;
        line-height: 1.4;
      }
    }
  }
}

.courses-section {
  padding: 80rpx 48rpx 0;
  
  .courses-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 48rpx;
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 16rpx;
      
      .title-bar {
        width: 8rpx;
        height: 36rpx;
        background-color: #ff6600;
        border-radius: 999px;
      }
      
      text {
        font-size: 40rpx;
        font-weight: 700;
        color: #1a1c1c;
        letter-spacing: -1rpx;
      }
    }
    
    .view-all {
      color: #a33e00;
      font-weight: 700;
      font-size: 24rpx;
      text-transform: uppercase;
      letter-spacing: 2rpx;
      padding-bottom: 4rpx;
    }
  }
  
  .course-list {
    display: flex;
    flex-direction: column;
    gap: 32rpx;
  }
  
  .course-card {
    background-color: #ffffff;
    padding: 16rpx;
    border-radius: 32rpx;
    display: flex;
    gap: 32rpx;
    box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.02);
    
    .course-img {
      width: 200rpx;
      height: 200rpx;
      border-radius: 20rpx;
      flex-shrink: 0;
    }
    
    .course-info {
      display: flex;
      flex-direction: column;
      justify-content: center;
      padding-right: 16rpx;
      
      .course-level {
        color: #a33e00;
        font-weight: 700;
        font-size: 20rpx;
        letter-spacing: -1rpx;
        margin-bottom: 12rpx;
      }
      
      .course-title {
        font-weight: 700;
        font-size: 32rpx;
        color: #1a1c1c;
        line-height: 1.3;
      }
      
      .course-meta {
        display: flex;
        align-items: center;
        gap: 24rpx;
        margin-top: 24rpx;
        
        .meta-item {
          display: flex;
          align-items: center;
          gap: 8rpx;
          color: #5f5e5e;
          font-size: 24rpx;
        }
      }
    }
  }
}

.safe-bottom {
  height: 200rpx;
}

.footer-cta {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  z-index: 50;
  padding: 32rpx 48rpx;
  padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid rgba(0,0,0,0.05);
  
  .price-box {
    display: flex;
    flex-direction: column;
    
    .price-label {
      font-size: 20rpx;
      font-weight: 700;
      color: #5f5e5e;
      letter-spacing: 2rpx;
    }
    
    .price-value {
      display: flex;
      align-items: baseline;
      gap: 4rpx;
      margin-top: 4rpx;
      
      .currency {
        font-size: 32rpx;
        font-weight: 900;
      }
      
      .amount {
        font-size: 48rpx;
        font-weight: 900;
      }
      
      .unit {
        font-size: 24rpx;
        color: #5f5e5e;
        margin-left: 8rpx;
      }
    }
  }
  
  .btn-book {
    background-color: #ff6600;
    color: #561d00;
    padding: 32rpx 64rpx;
    border-radius: 24rpx;
    font-weight: 700;
    font-size: 32rpx;
    display: flex;
    align-items: center;
    gap: 16rpx;
    box-shadow: 0 16rpx 32rpx rgba(255, 102, 0, 0.2);
    transition: all 0.2s;
    
    &:active {
      transform: scale(0.96);
    }
  }
}
</style>
