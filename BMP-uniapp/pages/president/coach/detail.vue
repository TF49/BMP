<template>
  <PresidentLayout :showTabBar="false">
    <view class="coach-detail-page">
      <!-- Header -->
      <view class="nav-header">
        <view class="status-bar-placeholder"></view>
        <view class="nav-content">
          <view class="nav-left" @click="goBack">
            <uni-icons type="arrow-left" size="24" color="#a33e00"></uni-icons>
            <text class="nav-title">教练详情</text>
          </view>
        </view>
      </view>

      <scroll-view class="main-content" scroll-y :show-scrollbar="false">
        <!-- Hero Section -->
        <view class="hero-section">
          <image class="hero-bg" :src="(coach && coach.avatar) || '/static/placeholders/hero.svg'" mode="aspectFill"></image>
          <view class="hero-gradient"></view>
          <view class="hero-content">
            <view class="badge">
              {{ coach && coach.status === 'active' ? 'ELITE COACH' : 'COACH' }}
            </view>
            <view class="name">{{ (coach && coach.name) || '-' }}</view>
            <view class="rating-row">
              <view class="stars">
                <uni-icons type="star-filled" size="18" color="#ff6600" v-for="i in 4" :key="i"></uni-icons>
                <uni-icons type="starhalf" size="18" color="#ff6600"></uni-icons>
              </view>
              <text class="score">
                {{ coach && coach.rating != null ? Number(coach.rating).toFixed(1) : '-' }}
              </text>
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
            作为前国家队资深队员，教练在羽毛球教学领域深耕多年。他擅长精准的技术拆解与战术分析，
            并结合学员实际情况制定训练方案，帮助学员稳步提升对抗与步法协同能力。
          </view>

          <view class="section-title mt-8">
            <view class="title-bar"></view>
            <text>技术特长</text>
          </view>
          <view class="tags-container">
            <view class="tag" v-for="tag in (coach && coach.tags) || []" :key="tag">
              <uni-icons type="bolt" size="16" color="#5f5e5e"></uni-icons>
              <text>{{ tag }}</text>
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
              <image class="course-img" src="/static/placeholders/hero.svg" mode="aspectFill"></image>
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
              <image class="course-img" src="/static/placeholders/hero.svg" mode="aspectFill"></image>
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

      <!-- Bottom Action Bar (aligned with venue detail) -->
      <view class="bottom-nav">
        <view class="bottom-nav-glass"></view>
        <view class="bottom-nav-content">
          <view class="secondary-actions">
            <view class="action-item delete-action" @click="handleDelete">
              <uni-icons type="trash" size="18" color="#94a3b8"></uni-icons>
              <text class="action-label">DELETE</text>
            </view>
            <view class="action-item edit-action" @click="handleEdit">
              <uni-icons type="compose" size="18" color="#94a3b8"></uni-icons>
              <text class="action-label">EDIT</text>
            </view>
            <view class="action-item" @click="handleShare">
              <uni-icons type="paperplane" size="18" color="#94a3b8"></uni-icons>
              <text class="action-label">SHARE</text>
            </view>
          </view>
          <view class="primary-cta" @click="handleBook">
            <view class="price-box">
              <text class="price-label">单课起价</text>
              <view class="price-value">
                <text class="currency">¥</text>
                <text class="amount">{{ coach && coach.price != null ? coach.price : 0 }}</text>
                <text class="unit">/ 小时</text>
              </view>
            </view>
            <view class="primary-cta-button">
              <text class="cta-text">预约教练</text>
              <uni-icons type="right" size="16" color="#ffffff"></uni-icons>
            </view>
          </view>
        </view>
      </view>
    </view>
  </PresidentLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import PresidentLayout from '@/components/president/PresidentLayout.vue'
import { safeNavigateBack } from '@/utils/navigation'
import { PRESIDENT_PAGES } from '@/utils/presidentRouter'

const coachId = ref<number>(1)

// 会长端教练列表（当前为原型数据）。详情页根据路由参数 id 渲染对应卡片数据。
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

const coach = computed(() => {
  return coaches.value.find(c => c.id === coachId.value) || coaches.value[0]
})

onLoad((options: any) => {
  const rawId = options && options.id
  const id = Number(rawId)
  if (!Number.isNaN(id)) coachId.value = id
})

function goBack() {
  safeNavigateBack(PRESIDENT_PAGES.DASHBOARD)
}

function handleBook() {
  uni.showToast({ title: '预约功能开发中', icon: 'none' })
}

function handleEdit() {
  uni.navigateTo({ url: `${PRESIDENT_PAGES.COACH_FORM}?id=${coachId.value}` })
}

function handleDelete() {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除教练「${coach.value?.name || ''}」吗？`,
    confirmColor: '#ba1a1a',
    success: (res) => {
      if (!res.confirm) return
      uni.showToast({ title: '已删除（原型行为）', icon: 'success' })
    }
  })
}

function handleShare() {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
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
    background: linear-gradient(180deg, rgba(249, 249, 249, 0) 0%, rgba(249, 249, 249, 1) 100%);
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
    box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.04);
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
      &.text-primary {
        color: #a33e00;
      }
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

  .mt-8 {
    margin-top: 64rpx;
  }

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
    box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.02);

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

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 50;
  padding-bottom: env(safe-area-inset-bottom);

  .bottom-nav-glass {
    position: absolute;
    inset: 0;
    background-color: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(20px);
    border-top-left-radius: 40rpx;
    border-top-right-radius: 40rpx;
    box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.05);
  }

  .bottom-nav-content {
    position: relative;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 32rpx 16rpx;
    column-gap: 32rpx;
  }

  .secondary-actions {
    display: flex;
    gap: 20rpx;
    flex-shrink: 0;
  }

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
    padding: 12rpx;
    transition: all 0.2s;

    &:active {
      background-color: #f1f5f9;
      border-radius: 12rpx;
    }
  }

  .action-label {
    font-size: 20rpx;
    font-weight: 700;
    margin-top: 4rpx;
    letter-spacing: 1rpx;
  }

  .primary-cta {
    display: flex;
    align-items: center;
    gap: 20rpx;
    background: #ffffff;
    padding: 16rpx 20rpx;
    border-radius: 999rpx;
    box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.08);
    max-width: 60%;
  }

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
        font-size: 26rpx;
        font-weight: 900;
      }

      .amount {
        font-size: 36rpx;
        font-weight: 900;
      }

      .unit {
        font-size: 22rpx;
        color: #5f5e5e;
        margin-left: 8rpx;
      }
    }
  }

  .primary-cta-button {
    background: linear-gradient(45deg, #a33e00 0%, #ff6600 100%);
    padding: 18rpx 32rpx;
    border-radius: 999rpx;
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  .cta-text {
    font-size: 26rpx;
    font-weight: 700;
    color: #ffffff;
  }
}
</style>

