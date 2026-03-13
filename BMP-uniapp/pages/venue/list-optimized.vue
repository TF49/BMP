<template>
  <MobileLayout>
    <!-- 骨架屏加载状态 -->
    <Skeleton v-if="loading && items.length === 0" :count="5" />

    <!-- 列表内容 -->
    <scroll-view
      v-else
      class="list-container"
      scroll-y
      @scrolltolower="handleLoadMore"
      :enable-back-to-top="true"
    >
      <view
        v-for="item in items"
        :key="item.id"
        class="list-item animate-slide-up"
      >
        <image
          v-lazy-load="item.image"
          class="item-image"
          mode="aspectFill"
        />
        <view class="item-content">
          <text class="item-title">{{ item.name }}</text>
          <text class="item-desc">{{ item.description }}</text>
          <view class="item-footer">
            <text class="item-price">¥{{ item.price }}</text>
            <button class="item-btn" @click="handleItemClick(item)">
              查看详情
            </button>
          </view>
        </view>
      </view>

      <!-- 加载更多提示 -->
      <view v-if="!hasMore && items.length > 0" class="load-complete">
        <text>已加载全部内容</text>
      </view>

      <!-- 加载中提示 -->
      <view v-if="loading && items.length > 0" class="loading-more">
        <LoadingAnimation type="dots" text="加载中..." />
      </view>
    </scroll-view>

    <!-- 空状态 -->
    <view v-if="!loading && items.length === 0" class="empty-state">
      <text class="empty-icon">📭</text>
      <text class="empty-text">暂无数据</text>
    </view>
  </MobileLayout>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import MobileLayout from '@/components/MobileLayout.vue'
import Skeleton from '@/components/Skeleton.vue'
import LoadingAnimation from '@/components/LoadingAnimation.vue'
import { usePagination } from '@/composables/usePagination'
import { getVenueList } from '@/api/venue'

// 使用分页加载 Composable
const {
  items,
  state,
  loadData,
  loadMore
} = usePagination(
  (page, pageSize) => getVenueList({ page, pageSize }),
  {
    pageSize: 10,
    cacheKey: 'venue_list',
    cacheDuration: 5 * 60 * 1000
  }
)

const loading = state.value.loading
const hasMore = state.value.hasMore

// 初始化加载
onMounted(() => {
  loadData()
})

// 加载更多
const handleLoadMore = () => {
  loadMore()
}

// 点击项目
const handleItemClick = (item: any) => {
  uni.navigateTo({
    url: `/pages/venue/detail?id=${item.id}`
  })
}
</script>

<style lang="scss" scoped>
.list-container {
  padding: 20rpx;

  .list-item {
    background: white;
    border-radius: 12rpx;
    margin-bottom: 20rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
    display: flex;

    .item-image {
      width: 180rpx;
      height: 180rpx;
      object-fit: cover;
    }

    .item-content {
      flex: 1;
      padding: 20rpx;
      display: flex;
      flex-direction: column;
      justify-content: space-between;

      .item-title {
        font-size: 28rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 10rpx;
      }

      .item-desc {
        font-size: 24rpx;
        color: #999;
        margin-bottom: 15rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .item-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .item-price {
          font-size: 28rpx;
          color: #3cc51f;
          font-weight: bold;
        }

        .item-btn {
          background: #3cc51f;
          color: white;
          border: none;
          border-radius: 6rpx;
          padding: 10rpx 20rpx;
          font-size: 24rpx;
        }
      }
    }
  }

  .load-complete {
    text-align: center;
    padding: 30rpx;
    color: #999;
    font-size: 26rpx;
  }

  .loading-more {
    padding: 30rpx;
    text-align: center;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 20rpx;

  .empty-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}
</style>
