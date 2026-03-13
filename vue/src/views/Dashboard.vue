<template>
  <div class="dashboard">
    <div class="dashboard-background">
      <div class="gradient-overlay"></div>
      <div class="floating-badminton">
        <svg viewBox="0 0 100 100" class="badminton-icon" v-for="(item, index) in shuttlecocks" :key="index" :style="{ left: item.x + '%', top: item.y + '%', animationDelay: item.delay + 's', transform: 'rotate(' + item.rotate + 'deg)', color: 'var(--color-primary)' }">
          <ellipse cx="50" cy="20" rx="18" ry="8" fill="rgba(255,255,255,0.15)" opacity="0.9"/>
          <ellipse cx="50" cy="20" rx="15" ry="6" fill="currentColor" opacity="0.2"/>
          <path d="M32 20 Q50 60 50 90" stroke="rgba(255,255,255,0.4)" stroke-width="2" fill="none" stroke-linecap="round"/>
          <path d="M68 20 Q50 60 50 90" stroke="rgba(255,255,255,0.4)" stroke-width="2" fill="none" stroke-linecap="round"/>
          <circle cx="50" cy="95" r="3" fill="rgba(255,255,255,0.7)"/>
        </svg>
      </div>
    </div>

    <div class="dashboard-content">
      <!-- 欢迎语 -->
      <div class="welcome-section mb-24">
        <div class="welcome-background">
          <div class="welcome-gradient"></div>
          <div class="welcome-pattern"></div>
          <div class="floating-elements">
            <div class="float-circle circle-1"></div>
            <div class="float-circle circle-2"></div>
            <div class="float-circle circle-3"></div>
          </div>
        </div>
        <div class="welcome-main">
          <div class="welcome-left">
            <div class="welcome-icon-wrapper">
              <div class="welcome-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/>
                  <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
                  <circle cx="9" cy="9" r="1" fill="currentColor"/>
                  <circle cx="15" cy="9" r="1" fill="currentColor"/>
                </svg>
              </div>
            </div>
            <div class="welcome-content">
              <div class="welcome-greeting">
                <span class="greeting-emoji">{{ greetingEmoji }}</span>
                <h1 class="welcome-title">
                  <span class="greeting-text">{{ greeting }}</span>
                  <span class="user-name">{{ userName }}</span>
                </h1>
              </div>
              <p class="welcome-subtitle">
                <span class="subtitle-icon">✨</span>
                欢迎回到羽毛球场馆管理系统，祝您工作顺利！
              </p>
              <div class="welcome-tags">
                <span class="welcome-tag tag-primary">
                  <el-icon><Calendar /></el-icon>
                  今日预订 {{ numOrZero(dashboardStats.todayBookings) }} 次
                </span>
                <span class="welcome-tag tag-success">
                  <el-icon><User /></el-icon>
                  新增会员 {{ numOrZero(dashboardStats.todayNewMembers) }} 人
                </span>
              </div>
            </div>
          </div>
          <div class="welcome-right">
            <div class="date-card">
              <div class="date-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
              <div class="date-info">
                <span class="date-text">{{ currentDate }}</span>
                <span class="weekday-text">{{ currentWeekday }}</span>
              </div>
              <div class="time-display">{{ currentTime }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- KPI看板 -->
      <el-row :gutter="24" class="mb-24">
        <el-col :xs="24">
          <KPIBoard :stats="dashboardStats" :loading="statsLoading" />
        </el-col>
      </el-row>

      <!-- 第一行：统计卡片 -->
      <el-row :gutter="24" class="mb-24">
        <el-col :xs="24" :sm="12" :lg="4" :xl="4">
          <div class="stat-card stat-card-members" :class="{ 'loading': statsLoading }">
            <div class="stat-icon-wrapper">
              <div class="stat-icon-bg">
                <el-icon :size="28"><User /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ numOrZero(dashboardStats.memberTotal) }}</div>
              <div class="stat-label">会员总数</div>
              <div class="stat-trend" :class="numOrZero(dashboardStats.memberGrowth) >= 0 ? 'up' : 'down'">
                <el-icon><ArrowUp /></el-icon> {{ numOrZero(dashboardStats.memberGrowth) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="5" :xl="5">
          <div class="stat-card stat-card-venues" :class="{ 'loading': statsLoading }">
            <div class="stat-icon-wrapper">
              <div class="stat-icon-bg">
                <el-icon :size="28"><OfficeBuilding /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ venueCount }}</div>
              <div class="stat-label">场馆数量</div>
              <div class="stat-trend up">
                <el-icon><ArrowUp /></el-icon> {{ numOrZero(dashboardStats.venueGrowth) }}
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="5" :xl="5">
          <div class="stat-card stat-card-courts" :class="{ 'loading': statsLoading }">
            <div class="stat-icon-wrapper">
              <div class="stat-icon-bg">
                <el-icon :size="28"><Grid /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ numOrZero(dashboardStats.courtTotal) }}</div>
              <div class="stat-label">场地数量</div>
              <div class="stat-trend" :class="numOrZero(dashboardStats.courtGrowth) >= 0 ? 'up' : 'down'">
                <el-icon><ArrowUp /></el-icon> {{ numOrZero(dashboardStats.courtGrowth) }}
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="5" :xl="5">
          <div class="stat-card stat-card-bookings" :class="{ 'loading': statsLoading }">
            <div class="stat-icon-wrapper">
              <div class="stat-icon-bg">
                <el-icon :size="28"><Calendar /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ numOrZero(dashboardStats.todayBookings) }}</div>
              <div class="stat-label">今日预订</div>
              <div class="stat-trend" :class="numOrZero(dashboardStats.bookingGrowth) >= 0 ? 'up' : 'down'">
                <el-icon><ArrowUp /></el-icon> {{ Math.abs(numOrZero(dashboardStats.bookingGrowth)) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :lg="5" :xl="5">
          <div class="stat-card stat-card-revenue" :class="{ 'loading': statsLoading }">
            <div class="stat-icon-wrapper">
              <div class="stat-icon-bg">
                <el-icon :size="28"><Money /></el-icon>
              </div>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatCurrency(dashboardStats.todayRevenue) }}</div>
              <div class="stat-label">今日收入</div>
              <div class="stat-trend" :class="numOrZero(dashboardStats.revenueGrowth) >= 0 ? 'up' : 'down'">
                <el-icon><ArrowUp /></el-icon> {{ Math.abs(numOrZero(dashboardStats.revenueGrowth)).toFixed(1) }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 第二行：快捷入口 + 今日运营 + 场地状态 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="14">
          <div class="glass-card shortcut-card">
            <div class="card-header">
              <span class="card-title">快捷入口</span>
              <span class="card-subtitle">常用功能快速访问</span>
            </div>
            <div class="shortcut-grid">
              <div
                class="shortcut-item"
                v-for="shortcut in shortcuts"
                :key="shortcut.path"
                @click="navigateTo(shortcut.path)"
              >
                <div class="shortcut-icon" :style="{ background: shortcut.gradient }">
                  <el-icon :size="26"><component :is="shortcut.icon" /></el-icon>
                </div>
                <span class="shortcut-name">{{ shortcut.name }}</span>
                <span class="shortcut-desc">{{ shortcut.desc }}</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="5">
          <div class="glass-card operation-card">
            <div class="card-header">
              <span class="card-title">今日运营</span>
            </div>
            <div class="operation-stats">
              <div class="operation-item" :class="{ 'weather-loading': weatherData.loading }">
                <div class="operation-icon weather">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle v-if="weatherData.icon === 'sunny'" cx="12" cy="12" r="5"/>
                    <path :d="getWeatherSvgPath"/>
                  </svg>
                </div>
                <div class="operation-info">
                  <span class="operation-value">
                    <template v-if="weatherData.loading">加载中</template>
                    <template v-else>{{ weatherData.temp }}°C<span v-if="weatherData.city" class="weather-city">{{ weatherData.city }}</span></template>
                  </span>
                  <span class="operation-label">{{ weatherData.loading ? '正在获取天气…' : weatherData.advice }}</span>
                </div>
              </div>
              <div class="operation-item">
                <div class="operation-icon booking-rate">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 20V10M18 20V4M6 20v-4"/>
                  </svg>
                </div>
                <div class="operation-info">
                  <span class="operation-value">{{ numOrZero(dashboardStats.bookingRate) }}%</span>
                  <span class="operation-label">今日预订率</span>
                </div>
              </div>
              <div class="operation-item">
                <div class="operation-icon peak-time">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M12 6v6l4 2"/>
                  </svg>
                </div>
                <div class="operation-info">
                  <span class="operation-value">{{ dashboardStats.peakHours || '暂无' }}</span>
                  <span class="operation-label">预约高峰时段</span>
                </div>
              </div>
            </div>
            <div class="operation-tip" :class="{ 'tip-low': numOrZero(dashboardStats.bookingRate) === 0 }">
              <el-icon class="tip-icon"><InfoFilled /></el-icon>
              <span>{{ numOrZero(dashboardStats.bookingRate) === 0 ? '暂无预订数据，可引导用户预约场地' : (numOrZero(dashboardStats.bookingRate) >= 50 ? '今日场地预订率较高，建议用户提前预约' : '当前预订率一般，可适当推广预约') }}</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="5">
          <div class="glass-card court-status-card">
            <div class="card-header">
              <span class="card-title">场地状态</span>
              <el-link type="primary" :underline="false" class="view-all" @click="navigateTo('/court/management')">查看全部</el-link>
            </div>
            <div class="court-list">
              <template v-if="dashboardStats.courts && dashboardStats.courts.length > 0">
                <div class="court-item" v-for="court in dashboardStats.courts" :key="court.id">
                  <div class="court-info">
                    <span class="court-name">{{ court.name }}</span>
                    <span class="court-time">{{ court.time }}</span>
                  </div>
                  <span
                    class="court-status-badge"
                    :class="court.status === 'available' ? 'status-available' : 'status-occupied'"
                  >
                    <span class="status-dot"></span>
                    {{ court.status === 'available' ? '空闲' : '使用中' }}
                  </span>
                </div>
              </template>
              <div v-else class="court-empty">
                <span class="court-empty-text">暂无场地数据</span>
              </div>
            </div>
            <div class="court-summary">
              <div class="summary-item">
                <span class="summary-value available">{{ numOrZero(availableCount) }}</span>
                <span class="summary-label">空闲</span>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-item">
                <span class="summary-value occupied">{{ numOrZero(occupiedCount) }}</span>
                <span class="summary-label">使用中</span>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-item">
                <span class="summary-value total">{{ (dashboardStats.courts && dashboardStats.courts.length) || 0 }}</span>
                <span class="summary-label">总数</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 第三行：图表区域（收入 + 会员分布） -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="16">
          <RevenueBarChart class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="8">
          <MemberPieChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 第二.5行：各场馆收入趋势 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="24" class="mb-16">
          <div class="section-header">
            <h3 class="section-title">各场馆收入趋势</h3>
            <el-button
              v-if="venueRevenueData.length > 3"
              type="primary"
              link
              @click="showAllRevenueVenues = !showAllRevenueVenues"
            >
              {{ showAllRevenueVenues ? '收起' : `查看更多 (${venueRevenueData.length - 3})` }}
              <el-icon>
                <component :is="showAllRevenueVenues ? 'ArrowUp' : 'ArrowDown'" />
              </el-icon>
            </el-button>
          </div>
        </el-col>
        <template v-if="displayedRevenueVenues.length > 0">
          <el-col
            v-for="venue in displayedRevenueVenues"
            :key="venue.id"
            :xs="24"
            :lg="8"
          >
            <VenueRevenueChart
              :venue-name="venue.name"
              :venue-data="venue.data"
              :color-theme="venue.colorTheme"
            />
          </el-col>
        </template>
        <el-col v-else :xs="24" :lg="24">
          <div class="section-empty">
            <span class="section-empty-text">暂无场馆收入趋势数据</span>
          </div>
        </el-col>
      </el-row>

      <!-- 第三行：图表区域（预订趋势 + 场地雷达） -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="16">
          <BookingLineChart class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="8">
          <VenueRadarChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 第三.5行：各场馆预订趋势 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="24" class="mb-16">
          <div class="section-header">
            <h3 class="section-title">各场馆预订趋势</h3>
            <el-button
              v-if="venueBookingData.length > 3"
              type="primary"
              link
              @click="showAllBookingVenues = !showAllBookingVenues"
            >
              {{ showAllBookingVenues ? '收起' : `查看更多 (${venueBookingData.length - 3})` }}
              <el-icon>
                <component :is="showAllBookingVenues ? 'ArrowUp' : 'ArrowDown'" />
              </el-icon>
            </el-button>
          </div>
        </el-col>
        <template v-if="displayedBookingVenues.length > 0">
          <el-col
            v-for="venue in displayedBookingVenues"
            :key="'booking-' + venue.id"
            :xs="24"
            :lg="8"
          >
            <VenueBookingChart
              :venue-name="venue.name"
              :venue-data="venue.data"
              :color-theme="venue.colorTheme"
            />
          </el-col>
        </template>
        <el-col v-else :xs="24" :lg="24">
          <div class="section-empty">
            <span class="section-empty-text">暂无场馆预订趋势数据</span>
          </div>
        </el-col>
      </el-row>

      <!-- 新增：收入预订对比 + 器材租借 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="16">
          <RevenueBookingCompareChart class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="8">
          <EquipmentRentalChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 穿线服务状态分布 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="24">
          <StringingServiceChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 新增：会员增长 + 收入仪表盘 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="16">
          <MemberGrowthChart class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="8">
          <RevenueGaugeChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 新增：预订热力图 + 教练业绩排行 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="16">
          <PeakHoursHeatmap class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="8">
          <CoachPerformanceChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 新增：会员漏斗图 + 场地使用率 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="12">
          <MemberFunnelChart class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="12">
          <CourtUsageRingChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 新增：课程与教练分析（左右等高，减少空白） -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned charts-row-equal">
        <el-col :xs="24" :lg="16">
          <CourseHotRankChart class="chart-full-height chart-fill-col" />
        </el-col>
        <el-col :xs="24" :lg="8" class="chart-col-stack">
          <CourseCapacityTrendChart class="chart-full-height chart-stack-item" />
          <CoachScheduleWorkloadChart class="chart-full-height chart-stack-item" />
        </el-col>
      </el-row>

      <!-- 新增：赛事与会员生命周期（拆成两行 12+12，避免单侧多块堆叠造成空白） -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="12">
          <TournamentTimeline class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="12">
          <TournamentFunnelChart class="chart-full-height" />
        </el-col>
      </el-row>
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="12">
          <TournamentImpactChart class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="12">
          <MemberChurnChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 新增：收入结构与会员来源（拆成 12+12 与 整行，减少右侧堆叠空白） -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="12">
          <RevenueStructurePie class="chart-full-height" />
        </el-col>
        <el-col :xs="24" :lg="12">
          <VenueRevenueStackedChart class="chart-full-height" />
        </el-col>
      </el-row>
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="24">
          <MemberSourcePieChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 新增：预警与待办总览 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="8">
          <ExpiringMemberAlertCard />
        </el-col>
        <el-col :xs="24" :lg="8">
          <InventoryAlertList />
        </el-col>
        <el-col :xs="24" :lg="8">
          <OperationTodoSummary />
        </el-col>
      </el-row>

      <!-- 新增：今日场地时间轴 + 利用异常 -->
      <el-row :gutter="24" class="mb-24 charts-row-aligned">
        <el-col :xs="24" :lg="16">
          <TodayCourtTimeline />
        </el-col>
        <el-col :xs="24" :lg="8">
          <CourtUtilizationAnomalyChart class="chart-full-height" />
        </el-col>
      </el-row>

      <!-- 最后一行：最近活动 -->
      <el-row :gutter="24" class="charts-row-aligned">
        <el-col :xs="24" :lg="24">
          <div class="glass-card activity-card">
            <div class="card-header">
              <span class="card-title">最近活动</span>
              <el-link type="primary" :underline="false" class="view-all" @click="navigateTo('/booking/management')">查看更多</el-link>
            </div>
            <div class="activity-list">
              <template v-if="activities && activities.length > 0">
                <div
                  class="activity-item"
                  v-for="(activity, aindex) in activities"
                  :key="activity.id"
                  :style="{ animationDelay: (aindex * 0.08) + 's' }"
                >
                  <div class="activity-icon" :class="activity.type">
                    <el-icon v-if="activity.type === 'member'"><User /></el-icon>
                    <el-icon v-else-if="activity.type === 'court'"><Grid /></el-icon>
                    <el-icon v-else-if="activity.type === 'booking'"><Calendar /></el-icon>
                    <el-icon v-else><Money /></el-icon>
                  </div>
                  <div class="activity-content">
                    <p class="activity-desc">{{ activity.description }}</p>
                    <span class="activity-time">{{ activity.time }}</span>
                  </div>
                  <div class="activity-badge" :class="activity.type">
                    {{ getActivityTypeLabel(activity.type) }}
                  </div>
                </div>
              </template>
              <div v-else class="activity-empty">
                <span class="activity-empty-text">暂无最近活动</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Grid, Calendar, Money, ArrowUp, ArrowDown, InfoFilled, OfficeBuilding, DataLine, Document, Coin, Trophy, Tools, Setting, QuestionFilled } from '@element-plus/icons-vue'

// 注册所有快捷入口使用的图标组件
const iconComponents = { User, Grid, Calendar, Money, OfficeBuilding, DataLine, Document, Coin, Trophy, Tools, Setting, QuestionFilled }
import { getVenueList } from '@/api/venue'
import { getWeather } from '@/api/weather'
import { getUserInfo } from '@/utils/auth'
import { getMemberStatistics } from '@/api/member'
import { getBookingStatistics, getBookingVenueTrend } from '@/api/booking'
import { getFinanceStatistics, getFinanceVenueTrend } from '@/api/finance'
import { getCourtStatistics, getCourtList } from '@/api/court'
import { getRecentActivities } from '@/api/activity'

// 导入图表组件
import RevenueBarChart from '@/components/charts/RevenueBarChart.vue'
import MemberPieChart from '@/components/charts/MemberPieChart.vue'
import VenueRadarChart from '@/components/charts/VenueRadarChart.vue'
import BookingLineChart from '@/components/charts/BookingLineChart.vue'
import VenueRevenueChart from '@/components/charts/VenueRevenueChart.vue'
import VenueBookingChart from '@/components/charts/VenueBookingChart.vue'

// 导入新增图表组件
import KPIBoard from '@/components/charts/KPIBoard.vue'
import RevenueBookingCompareChart from '@/components/charts/RevenueBookingCompareChart.vue'
import EquipmentRentalChart from '@/components/charts/EquipmentRentalChart.vue'
import MemberGrowthChart from '@/components/charts/MemberGrowthChart.vue'
import RevenueGaugeChart from '@/components/charts/RevenueGaugeChart.vue'

// 导入第二批新增图表组件
import PeakHoursHeatmap from '@/components/charts/PeakHoursHeatmap.vue'
import CoachPerformanceChart from '@/components/charts/CoachPerformanceChart.vue'
import MemberFunnelChart from '@/components/charts/MemberFunnelChart.vue'
import CourtUsageRingChart from '@/components/charts/CourtUsageRingChart.vue'
import StringingServiceChart from '@/components/charts/StringingServiceChart.vue'

// 导入第三批新增可视化组件
import CourseHotRankChart from '@/components/charts/CourseHotRankChart.vue'
import CourseCapacityTrendChart from '@/components/charts/CourseCapacityTrendChart.vue'
import CoachScheduleWorkloadChart from '@/components/charts/CoachScheduleWorkloadChart.vue'
import TournamentTimeline from '@/components/charts/TournamentTimeline.vue'
import TournamentFunnelChart from '@/components/charts/TournamentFunnelChart.vue'
import TournamentImpactChart from '@/components/charts/TournamentImpactChart.vue'
import MemberChurnChart from '@/components/charts/MemberChurnChart.vue'
import ExpiringMemberAlertCard from '@/components/charts/ExpiringMemberAlertCard.vue'
import MemberSourcePieChart from '@/components/charts/MemberSourcePieChart.vue'
import RevenueStructurePie from '@/components/charts/RevenueStructurePie.vue'
import VenueRevenueStackedChart from '@/components/charts/VenueRevenueStackedChart.vue'
import InventoryAlertList from '@/components/charts/InventoryAlertList.vue'
import CourtUtilizationAnomalyChart from '@/components/charts/CourtUtilizationAnomalyChart.vue'
import OperationTodoSummary from '@/components/charts/OperationTodoSummary.vue'
import TodayCourtTimeline from '@/components/charts/TodayCourtTimeline.vue'

const router = useRouter()
const shuttlecocks = ref([])
const statsLoading = ref(false)

// Dashboard统计数据
const dashboardStats = ref({
  memberTotal: 0,
  memberGrowth: 0,
  venueTotal: 0,
  venueGrowth: 0,
  courtTotal: 0,
  courtGrowth: 0,
  todayBookings: 0,
  bookingGrowth: 0,
  todayRevenue: 0,
  revenueGrowth: 0,
  todayNewMembers: 0,
  bookingRate: 0,
  peakHours: '暂无',
  courts: [],
  activities: []
})

// 欢迎语相关数据
const userName = computed(() => {
  const userInfo = getUserInfo()
  // 优先使用 nickname（昵称），如果没有则使用 username（用户名），最后兜底显示"用户"
  return userInfo?.nickname || userInfo?.username || '用户'
})
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})

// 根据时间段返回对应的emoji
const greetingEmoji = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '🌙'
  if (hour < 9) return '🌅'
  if (hour < 12) return '☀️'
  if (hour < 14) return '🌞'
  if (hour < 17) return '🌤️'
  if (hour < 19) return '🌇'
  return '🌃'
})

// 当前时间（实时更新，初始即显示避免闪烁）
const currentTime = ref('00:00:00')
const updateTime = () => {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${hours}:${minutes}:${seconds}`
}

const currentDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}年${month}月${day}日`
})

const currentWeekday = computed(() => {
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekdays[new Date().getDay()]
})

// 导航函数
const navigateTo = (path) => {
  router.push(path)
}

// 活动类型标签
const getActivityTypeLabel = (type) => {
  const labels = {
    member: '会员',
    court: '场地',
    booking: '预约',
    finance: '财务'
  }
  return labels[type] || '其他'
}

const activities = ref([])

// 修正路由路径，确保与 router/index.js 中的定义一致
const shortcuts = ref([
  {
    name: '场馆管理',
    desc: '管理场馆信息',
    path: '/venue/management',
    icon: 'OfficeBuilding',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    name: '场地管理',
    desc: '管理场地资源',
    path: '/court/management',
    icon: 'Grid',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    name: '会员管理',
    desc: '管理会员信息',
    path: '/member/management',
    icon: 'User',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    name: '充值中心',
    desc: '会员充值服务',
    path: '/recharge/index',
    icon: 'Money',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  },
  {
    name: '场地预约',
    desc: '场地预约管理',
    path: '/booking/management',
    icon: 'DataLine',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  },
  {
    name: '器材管理',
    desc: '器材库存管理',
    path: '/equipment/management',
    icon: 'Document',
    gradient: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)'
  },
  {
    name: '器材租借',
    desc: '器材租借服务',
    path: '/equipment/rental',
    icon: 'Document',
    gradient: 'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)'
  },
  {
    name: '穿线服务',
    desc: '穿线服务管理',
    path: '/equipment/stringing',
    icon: 'Tools',
    gradient: 'linear-gradient(135deg, #96fbc4 0%, #f9f586 100%)'
  },
  {
    name: '教练管理',
    desc: '管理教练信息',
    path: '/coach-course/coach',
    icon: 'Trophy',
    gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  },
  {
    name: '课程管理',
    desc: '管理培训课程',
    path: '/coach-course/course',
    icon: 'Document',
    gradient: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)'
  },
  {
    name: '课程预约',
    desc: '预约培训课程',
    path: '/coach-course/course-booking',
    icon: 'Calendar',
    gradient: 'linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%)'
  },
  {
    name: '赛事管理',
    desc: '管理赛事活动',
    path: '/tournament/management',
    icon: 'Trophy',
    gradient: 'linear-gradient(135deg, #f6d365 0%, #fda085 100%)'
  },
  {
    name: '赛事报名',
    desc: '赛事报名管理',
    path: '/tournament/registration',
    icon: 'DataLine',
    gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
  },
  {
    name: '财务管理',
    desc: '收支财务统计',
    path: '/finance/management',
    icon: 'Coin',
    gradient: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)'
  },
  {
    name: '用户管理',
    desc: '系统用户管理',
    path: '/admin/user/management',
    icon: 'User',
    gradient: 'linear-gradient(135deg, #cfd9df 0%, #e2ebf0 100%)'
  },
  {
    name: '审计日志',
    desc: '财务审计日志',
    path: '/finance/audit-log',
    icon: 'Document',
    gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
  },
  {
    name: '个人中心',
    desc: '管理个人信息',
    path: '/profile',
    icon: 'User',
    gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)'
  },
  {
    name: '系统设置',
    desc: '系统配置管理',
    path: '/settings',
    icon: 'Setting',
    gradient: 'linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%)'
  },
  {
    name: '帮助反馈',
    desc: '帮助与反馈',
    path: '/help',
    icon: 'QuestionFilled',
    gradient: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)'
  },
])

const availableCount = computed(() => dashboardStats.value.courts.filter(c => c.status === 'available').length)
const occupiedCount = computed(() => dashboardStats.value.courts.filter(c => c.status === 'occupied').length)

// 天气数据
const weatherData = ref({
  temp: '--',
  text: '加载中...',
  icon: 'sunny',
  advice: '正在获取天气信息...',
  city: '',
  loading: true
})

// 获取天气数据
const fetchWeather = async () => {
  try {
    weatherData.value.loading = true
    const result = await getWeather()
    if (result.success && result.data) {
      weatherData.value = {
        temp: result.data.temp,
        text: result.data.text,
        icon: result.data.icon,
        advice: result.data.advice,
        city: result.data.city || '',
        loading: false
      }
    } else {
      // API失败时显示默认数据
      weatherData.value = {
        temp: '24',
        text: '晴',
        icon: 'sunny',
        advice: '天气适宜运动',
        city: '',
        loading: false
      }
    }
  } catch (error) {
    console.error('获取天气失败:', error)
    weatherData.value = {
      temp: '24',
      text: '晴',
      icon: 'sunny',
      advice: '天气适宜运动',
      city: '',
      loading: false
    }
  }
}

// 获取天气图标SVG路径
const getWeatherSvgPath = computed(() => {
  const iconType = weatherData.value.icon
  switch (iconType) {
    case 'sunny':
      return 'M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42'
    case 'cloudy':
      return 'M18 10h-1.26A8 8 0 109 20h9a5 5 0 000-10z'
    case 'rainy':
      return 'M16 13v8M8 13v8M12 15v8M20 16.58A5 5 0 0018 7h-1.26A8 8 0 104 15.25'
    case 'snowy':
      return 'M20 17.58A5 5 0 0018 8h-1.26A8 8 0 104 16.25M8 16h.01M8 20h.01M12 18h.01M12 22h.01M16 16h.01M16 20h.01'
    case 'foggy':
      return 'M3 10h18M3 14h18M3 18h18'
    default:
      return 'M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42'
  }
})

// 场馆列表（来自API）
const venueList = ref([])

// Dashboard 场馆趋势图数据（来自API）
const venueRevenueData = ref([])
const venueBookingData = ref([])

// 控制场馆趋势图展开/收起状态
const showAllRevenueVenues = ref(false)
const showAllBookingVenues = ref(false)

// 场馆总数（统一从 dashboardStats 获取，避免数据不同步）
const venueCount = computed(() => dashboardStats.value.venueTotal)

// 显示的收入趋势场馆列表（默认只显示前3个）
const displayedRevenueVenues = computed(() => {
  if (showAllRevenueVenues.value || venueRevenueData.value.length <= 3) {
    return venueRevenueData.value
  }
  return venueRevenueData.value.slice(0, 3)
})

// 显示的预订趋势场馆列表（默认只显示前3个）
const displayedBookingVenues = computed(() => {
  if (showAllBookingVenues.value || venueBookingData.value.length <= 3) {
    return venueBookingData.value
  }
  return venueBookingData.value.slice(0, 3)
})

// 获取场馆列表
const fetchVenueList = async () => {
  try {
    const res = await getVenueList({ page: 1, size: 100 })
    if (res.code === 200 && res.data) {
      // 处理不同的数据结构：VenueController 返回的是 { data: { data: [...], total, ... } }
      let venues = []
      if (Array.isArray(res.data)) {
        venues = res.data
      } else if (res.data.data && Array.isArray(res.data.data)) {
        venues = res.data.data
      } else if (res.data.records && Array.isArray(res.data.records)) {
        venues = res.data.records
      } else if (res.data.list && Array.isArray(res.data.list)) {
        venues = res.data.list
      }
      
      venueList.value = venues
      dashboardStats.value.venueTotal = venues.length

      // 当日新增场馆数量（作为增长展示）
      const todayStr = new Date().toISOString().slice(0, 10)
      dashboardStats.value.venueGrowth = venues.filter(v => {
        const ct = v?.createTime
        if (!ct) return false
        const d = typeof ct === 'string' ? ct.slice(0, 10) : ''
        return d === todayStr
      }).length
    } else {
      venueList.value = []
      dashboardStats.value.venueTotal = 0
      dashboardStats.value.venueGrowth = 0
    }
  } catch (error) {
    console.error('获取场馆列表失败:', error)
    venueList.value = []
    dashboardStats.value.venueTotal = 0
    dashboardStats.value.venueGrowth = 0
  }
}

const applyVenueThemes = (items, themes) => {
  return (items || []).map((item, index) => ({
    id: item.venueId ?? item.id ?? index,
    name: item.venueName ?? item.name ?? `场馆${index + 1}`,
    colorTheme: themes[index % themes.length],
    data: item.data ?? item.trends ?? item
  }))
}

// 获取各场馆收入趋势（真实数据）
const fetchVenueRevenueTrend = async () => {
  try {
    const res = await getFinanceVenueTrend()
    if (res.code === 200 && Array.isArray(res.data)) {
      venueRevenueData.value = applyVenueThemes(res.data, ['blue', 'purple', 'green', 'orange'])
    } else {
      venueRevenueData.value = []
    }
  } catch (error) {
    console.error('获取场馆收入趋势失败:', error)
    venueRevenueData.value = []
  }
}

// 获取各场馆预订趋势（真实数据）
const fetchVenueBookingTrend = async () => {
  try {
    const res = await getBookingVenueTrend()
    if (res.code === 200 && Array.isArray(res.data)) {
      venueBookingData.value = applyVenueThemes(res.data, ['green', 'blue', 'purple', 'orange'])
    } else {
      venueBookingData.value = []
    }
  } catch (error) {
    console.error('获取场馆预订趋势失败:', error)
    venueBookingData.value = []
  }
}

// 获取最近活动（真实数据）
const fetchRecentActivities = async () => {
  try {
    const res = await getRecentActivities({ limit: 10 })
    if (res.code === 200 && Array.isArray(res.data)) {
      activities.value = res.data
    } else {
      activities.value = []
    }
  } catch (error) {
    console.error('获取最近活动失败:', error)
    activities.value = []
  }
}

// 获取Dashboard统计数据
const fetchDashboardStats = async () => {
  try {
    statsLoading.value = true
    const today = new Date().toISOString().split('T')[0]

    // 并行请求所有统计接口
    const [memberStats, bookingStats, financeStats, courtStats] = await Promise.all([
      getMemberStatistics(),
      getBookingStatistics(),
      getFinanceStatistics({ startDate: today, endDate: today }),
      getCourtStatistics()
    ])

    // 处理会员数据
    if (memberStats.code === 200 && memberStats.data) {
      dashboardStats.value.memberTotal = numOrZero(memberStats.data.total)
      dashboardStats.value.todayNewMembers = numOrZero(memberStats.data.newToday)
      dashboardStats.value.memberGrowth = numOrZero(memberStats.data.growthRate)
    }

    // 处理预订数据
    if (bookingStats.code === 200 && bookingStats.data) {
      const d = bookingStats.data
      dashboardStats.value.todayBookings = d.todayBookings != null ? numOrZero(d.todayBookings) : (numOrZero(d.ongoing) + numOrZero(d.finished))
      dashboardStats.value.bookingGrowth = numOrZero(d.bookingTrend)
      dashboardStats.value.peakHours = (d.peakHours != null && d.peakHours !== '') ? d.peakHours : '暂无'
    }

    // 处理财务数据
    if (financeStats.code === 200 && financeStats.data) {
      const revenue = financeStats.data.totalIncome != null ? financeStats.data.totalIncome : 0
      dashboardStats.value.todayRevenue = numOrZero(revenue)
      dashboardStats.value.revenueGrowth = financeStats.data.incomeChange != null ? numOrZero(financeStats.data.incomeChange) : 0
    }

    // 处理场地数据
    if (courtStats.code === 200 && courtStats.data) {
      dashboardStats.value.courtTotal = numOrZero(courtStats.data.total)
      dashboardStats.value.courtGrowth = numOrZero(courtStats.data.newToday)
      const total = dashboardStats.value.courtTotal
      dashboardStats.value.bookingRate = total > 0 ? Math.round((numOrZero(dashboardStats.value.todayBookings) / total) * 100) : 0
    }
  } catch (error) {
    console.error('获取Dashboard统计数据失败:', error)
  } finally {
    statsLoading.value = false
  }
}

// 获取实时场地状态
const fetchCourtStatus = async () => {
  try {
    const res = await getCourtList({ page: 1, size: 20, status: null })
    if (res.code === 200 && res.data) {
      let courtList = []
      // 处理不同的数据结构：CourtController 返回的是 { data: { data: [...], total, ... } }
      if (Array.isArray(res.data)) {
        courtList = res.data
      } else if (res.data.data && Array.isArray(res.data.data)) {
        courtList = res.data.data
      } else if (res.data.records && Array.isArray(res.data.records)) {
        courtList = res.data.records
      } else if (res.data.list && Array.isArray(res.data.list)) {
        courtList = res.data.list
      }
      
      // 转换场地数据格式
      const today = new Date()
      dashboardStats.value.courts = courtList.slice(0, 6).map(court => {
        // 根据状态判断是否使用中
        const isOccupied = court.status === 2 || court.status === 3
        return {
          id: court.id,
          name: court.courtName || court.courtCode || `场地 ${court.id}`,
          time: isOccupied ? '使用中' : '空闲',
          status: isOccupied ? 'occupied' : 'available'
        }
      })
    }
  } catch (error) {
    console.error('获取场地状态失败:', error)
    dashboardStats.value.courts = []
  }
}

// 统一：无数据时显示为 0，避免页面出现空白或 undefined
const numOrZero = (v) => {
  if (v === null || v === undefined) return 0
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

// 格式化金额（不带¥符号，因为模板中已有）
const formatCurrency = (amount) => {
  const n = numOrZero(amount)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

const initAnimations = () => {
  shuttlecocks.value = Array.from({ length: 5 }, () => ({
    x: Math.random() * 80 + 10,
    y: Math.random() * 60 + 20,
    delay: Math.random() * 5,
    rotate: Math.random() * 360,
  }))
}

// 时间更新定时器
let timeInterval = null

const onDashboardRefresh = () => {
  fetchDashboardStats()
}

onMounted(() => {
  initAnimations()
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  window.addEventListener('bmp-dashboard-refresh', onDashboardRefresh)

  // 核心数据：并行加载
  fetchWeather()
  fetchVenueList()
  fetchDashboardStats().then(() => fetchCourtStatus())

  // 辅助数据：延迟加载
  setTimeout(() => {
    fetchRecentActivities()
    fetchVenueRevenueTrend()
    fetchVenueBookingTrend()
  }, 500)
})

onUnmounted(() => {
  window.removeEventListener('bmp-dashboard-refresh', onDashboardRefresh)
  if (timeInterval) {
    clearInterval(timeInterval)
    timeInterval = null
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&family=Open+Sans:wght@300;400;500;600;700&display=swap');

.dashboard {
  padding: 24px;
  background: linear-gradient(135deg, var(--color-background) 0%, var(--color-card-bg-hover) 50%, var(--color-background) 100%);
  min-height: calc(100vh - 84px);
  overflow-y: auto;
  position: relative;
  background-attachment: fixed;
}

.dashboard::-webkit-scrollbar {
  width: 8px;
}

.dashboard::-webkit-scrollbar-track {
  background: #F1F5F9;
  border-radius: 4px;
}

.dashboard::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 4px;
}

.dashboard::-webkit-scrollbar-thumb:hover {
  background: #94A3B8;
}

.dashboard-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 300px;
  background: linear-gradient(180deg, color-mix(in srgb, var(--color-primary) 3%, transparent) 0%, transparent 100%);
}

.floating-badminton {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.badminton-icon {
  position: absolute;
  width: 40px;
  height: 40px;
  animation: float 15s ease-in-out infinite;
  opacity: 0.15;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-20px) rotate(5deg); }
  50% { transform: translateY(-10px) rotate(-5deg); }
  75% { transform: translateY(-30px) rotate(3deg); }
}

.dashboard-content {
  position: relative;
  z-index: 1;
  max-width: 1600px;
  margin: 0 auto;
}

.mb-24 {
  margin-bottom: 36px;
}

.mb-16 {
  margin-bottom: 16px;
}

/* 场馆趋势图区域标题样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 4px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 18px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 2px;
}

.section-empty {
  padding: 40px 24px;
  text-align: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 18px;
  border: 1px dashed #E2E8F0;
}

.section-empty-text {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  color: #94A3B8;
}

.operation-tip.tip-low {
  border-left-color: #94A3B8;
}

.operation-tip.tip-low .tip-icon {
  color: #94A3B8;
}

.operation-tip.tip-low span {
  color: #64748B;
}

/* ========== 欢迎语区域样式 ========== */
.welcome-section {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary, #8B5CF6) 50%, var(--color-accent, #EC4899) 100%);
  box-shadow:
    0 20px 60px rgba(var(--color-primary-rgb, 37, 99, 235), 0.3),
    0 8px 24px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  animation: welcomeFadeIn 0.8s ease-out;
}

@keyframes welcomeFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.welcome-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.welcome-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.welcome-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.08) 2px, transparent 2px),
    radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.06) 2px, transparent 2px);
  background-size: 60px 60px;
  animation: patternMove 20s linear infinite;
}

@keyframes patternMove {
  0% { background-position: 0 0, 30px 30px; }
  100% { background-position: 60px 60px, 90px 90px; }
}

.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.float-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
  animation: floatCircle 8s ease-in-out infinite;
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: -30px;
  right: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 80px;
  height: 80px;
  bottom: -20px;
  left: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 60px;
  height: 60px;
  top: 50%;
  right: 25%;
  animation-delay: 4s;
}

@keyframes floatCircle {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-20px) scale(1.1);
    opacity: 0.8;
  }
}

.welcome-main {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32px 40px;
  gap: 24px;
}

.welcome-left {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
}

.welcome-icon-wrapper {
  flex-shrink: 0;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: iconPulse 3s ease-in-out infinite;
}

.welcome-icon:hover {
  transform: scale(1.1) rotate(5deg);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

@keyframes iconPulse {
  0%, 100% {
    box-shadow:
      0 8px 32px rgba(0, 0, 0, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }
  50% {
    box-shadow:
      0 12px 40px rgba(0, 0, 0, 0.2),
      0 0 30px rgba(255, 255, 255, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }
}

.welcome-icon svg {
  width: 40px;
  height: 40px;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.welcome-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.welcome-greeting {
  display: flex;
  align-items: center;
  gap: 12px;
}

.greeting-emoji {
  font-size: 32px;
  animation: emojiWave 2s ease-in-out infinite;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

@keyframes emojiWave {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

.welcome-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin: 0;
  font-family: 'Poppins', sans-serif;
}

.greeting-text {
  font-size: 28px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.user-name {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  position: relative;
}

.user-name::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.8), rgba(255, 255, 255, 0.2));
  border-radius: 2px;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.4s ease;
}

.welcome-greeting:hover .user-name::after {
  transform: scaleX(1);
}

.welcome-subtitle {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-family: 'Open Sans', sans-serif;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

.subtitle-icon {
  font-size: 16px;
  animation: sparkle 2s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.2); }
}

.welcome-tags {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.welcome-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 20px;
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  font-weight: 500;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
  cursor: default;
}

.welcome-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.tag-primary {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff;
}

.tag-success {
  background: rgba(16, 185, 129, 0.3);
  color: #ffffff;
  border-color: rgba(16, 185, 129, 0.4);
}

.welcome-right {
  flex-shrink: 0;
}

.date-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 28px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(16px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s ease;
  min-width: 180px;
}

.date-card:hover {
  transform: translateY(-4px) scale(1.02);
  background: rgba(255, 255, 255, 0.2);
  box-shadow:
    0 16px 48px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.date-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14px;
  color: #ffffff;
  transition: all 0.3s ease;
}

.date-icon svg {
  width: 24px;
  height: 24px;
}

.date-card:hover .date-icon {
  transform: scale(1.1) rotate(5deg);
  background: rgba(255, 255, 255, 0.3);
}

.date-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.date-text {
  font-family: 'Poppins', sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
}

.weekday-text {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

.time-display {
  font-family: 'Poppins', monospace;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  letter-spacing: 2px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.7));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .welcome-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    padding: 28px 32px;
  }

  .welcome-right {
    width: 100%;
  }

  .date-card {
    flex-direction: row;
    justify-content: center;
    width: 100%;
  }

  .date-info {
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .welcome-section {
    border-radius: 18px;
  }

  .welcome-main {
    padding: 24px;
  }

  .welcome-left {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .welcome-icon {
    width: 64px;
    height: 64px;
    border-radius: 18px;
  }

  .welcome-icon svg {
    width: 32px;
    height: 32px;
  }

  .greeting-emoji {
    font-size: 24px;
  }

  .greeting-text {
    font-size: 22px;
  }

  .user-name {
    font-size: 26px;
  }

  .welcome-subtitle {
    font-size: 14px;
  }

  .welcome-tags {
    flex-wrap: wrap;
  }

  .welcome-tag {
    padding: 6px 12px;
    font-size: 12px;
  }

  .time-display {
    font-size: 22px;
  }

  .circle-1 {
    width: 80px;
    height: 80px;
  }

  .circle-2, .circle-3 {
    display: none;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    flex-direction: column;
    gap: 4px;
  }

  .greeting-text, .user-name {
    font-size: 20px;
  }

  .date-card {
    padding: 16px 20px;
    flex-direction: column;
  }

  .time-display {
    font-size: 20px;
  }
}

/* ========== 统一列内组件的间距 ========== */
.el-row .el-col {
  margin-bottom: 28px;
}

.el-row .el-col:last-child {
  margin-bottom: 0;
}

/* 修复响应式下列堆叠时的间距 */
@media (max-width: 1199px) {
  .el-row .el-col {
    margin-bottom: 32px;
  }

  .el-row .el-col:last-child {
    margin-bottom: 0;
  }
}

/* 增加图表组件之间的间距 */
.chart-full-height {
  margin-bottom: 0;
}

/* 统计卡片样式 */
.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: #FFFFFF;
  border-radius: 18px;
  border: 1px solid #E2E8F0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  animation: cardFadeIn 0.6s ease-out both;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s ease;
}

.stat-card:hover::before {
  left: 100%;
}

.stat-card:nth-child(1) { animation-delay: 0s; }
.stat-card:nth-child(2) { animation-delay: 0.1s; }
.stat-card:nth-child(3) { animation-delay: 0.2s; }
.stat-card:nth-child(4) { animation-delay: 0.3s; }

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.12);
  border-color: rgba(59, 130, 246, 0.3);
}

.stat-card.loading {
  opacity: 0.6;
  pointer-events: none;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-icon-wrapper {
  margin-right: 18px;
}

.stat-card .stat-icon-bg {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card:hover .stat-icon-bg {
  transform: scale(1.1) rotate(5deg);
}

.stat-card-members .stat-icon-bg {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  box-shadow: 0 4px 15px rgba(var(--color-primary-rgb, 37, 99, 235), 0.4);
}

.stat-card-venues .stat-icon-bg {
  background: linear-gradient(135deg, #8B5CF6 0%, #A855F7 100%);
  box-shadow: 0 4px 15px rgba(139, 92, 246, 0.4);
}

.stat-card-courts .stat-icon-bg {
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-warning) 100%);
  box-shadow: 0 4px 15px rgba(var(--color-accent-rgb, 236, 72, 153), 0.4);
}

.stat-card-bookings .stat-icon-bg {
  background: linear-gradient(135deg, var(--color-info) 0%, var(--color-primary-light) 100%);
  box-shadow: 0 4px 15px rgba(var(--color-info-rgb, 6, 182, 212), 0.4);
}

.stat-card-revenue .stat-icon-bg {
  background: linear-gradient(135deg, var(--color-success) 0%, var(--color-info) 100%);
  box-shadow: 0 4px 15px rgba(var(--color-success-rgb, 16, 185, 129), 0.4);
}

.stat-value {
  font-family: 'Poppins', sans-serif;
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary, #1E293B);
  line-height: 1.2;
}

.stat-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  color: var(--color-text-secondary, #64748B);
  margin-top: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  margin-top: 8px;
  font-family: 'Open Sans', sans-serif;
}

.stat-trend.up {
  color: var(--color-success, #10B981);
}

.stat-trend.down {
  color: var(--color-danger, #EF4444);
}

/* 玻璃卡片样式 */
.glass-card {
  background: var(--color-card-bg, #FFFFFF);
  border: 1px solid var(--color-border, #E2E8F0);
  border-radius: 18px;
  padding: 24px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--color-shadow, 0 2px 8px rgba(0, 0, 0, 0.06));
  position: relative;
  overflow: hidden;
}

.glass-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-secondary), var(--color-accent));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.glass-card:hover {
  box-shadow: var(--color-shadow-hover, 0 12px 32px rgba(0, 0, 0, 0.1));
  transform: translateY(-6px) scale(1.02);
  border-color: var(--color-border-hover, rgba(59, 130, 246, 0.2));
}

.glass-card:hover::after {
  opacity: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
}

.card-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #94A3B8;
}

.view-all {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
}

/* 快捷入口样式 */
.shortcut-card {
  height: 100%;
}

.shortcut-card .card-header {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 4px;
}

.shortcut-grid::-webkit-scrollbar {
  width: 6px;
}

.shortcut-grid::-webkit-scrollbar-track {
  background: #F1F5F9;
  border-radius: 3px;
}

.shortcut-grid::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 3px;
}

.shortcut-grid::-webkit-scrollbar-thumb:hover {
  background: #94A3B8;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 22px 18px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #E2E8F0;
  position: relative;
  overflow: hidden;
}

.shortcut-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(139, 92, 246, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.shortcut-item:hover {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EEF2FF) 0%, var(--color-background, #F5F3FF) 100%);
  border-color: var(--color-primary-light, #C7D2FE);
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 28px rgba(var(--color-primary-rgb, 37, 99, 235), 0.2);
}

.shortcut-item:hover::before {
  opacity: 1;
}

.shortcut-item:hover .shortcut-icon {
  transform: scale(1.15) rotate(5deg);
  box-shadow: 0 8px 20px rgba(var(--color-primary-rgb, 37, 99, 235), 0.3);
}

.shortcut-icon {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  margin-bottom: 14px;
  color: white;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 1;
}

.shortcut-name {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  text-align: center;
  margin-bottom: 4px;
}

.shortcut-desc {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #94A3B8;
  text-align: center;
}

/* 场地状态卡片样式 */
.court-status-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.court-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.court-empty {
  padding: 24px 18px;
  text-align: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  border: 1px dashed #E2E8F0;
}

.court-empty-text {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #94A3B8;
}

.court-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #E2E8F0;
  cursor: pointer;
  position: relative;
}

.court-item::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%) translateX(10px);
  width: 0;
  height: 0;
  border-left: 6px solid var(--color-primary);
  border-top: 4px solid transparent;
  border-bottom: 4px solid transparent;
  opacity: 0;
  transition: all 0.3s ease;
}

.court-item:hover {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EEF2FF) 0%, var(--color-background, #F5F3FF) 100%);
  transform: translateX(6px);
  box-shadow: 0 4px 12px rgba(var(--color-primary-rgb, 37, 99, 235), 0.12);
  border-color: var(--color-primary-light, rgba(59, 130, 246, 0.2));
}

.court-item:hover::after {
  opacity: 1;
  transform: translateY(-50%) translateX(0);
}

.court-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.court-name {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.court-time {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #94A3B8;
}

.court-status-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  font-weight: 500;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-available {
  background: color-mix(in srgb, var(--color-success) 10%, transparent);
  color: var(--color-success);
}

.status-available .status-dot {
  background: var(--color-success);
}

.status-occupied {
  background: color-mix(in srgb, var(--color-danger) 10%, transparent);
  color: var(--color-danger);
}

.status-occupied .status-dot {
  background: var(--color-danger);
}

.court-summary {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  padding: 16px;
  background: linear-gradient(135deg, #F8FAFC 0%, #EEF2FF 100%);
  border-radius: 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.summary-value {
  font-family: 'Poppins', sans-serif;
  font-size: 24px;
  font-weight: 700;
}

.summary-value.available {
  color: var(--color-success);
}

.summary-value.occupied {
  color: var(--color-danger);
}

.summary-value.total {
  color: var(--color-primary);
}

.summary-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #64748B;
}

.summary-divider {
  width: 1px;
  height: 40px;
  background: #E2E8F0;
}

/* 活动卡片样式 */
.activity-card {
  height: 100%;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-empty {
  padding: 32px 18px;
  text-align: center;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 14px;
  border: 1px dashed #E2E8F0;
}

.activity-empty-text {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  color: #94A3B8;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 14px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: activitySlideIn 0.6s ease-out both;
  border: 1px solid #E2E8F0;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.activity-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--color-primary), var(--color-secondary));
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.activity-item:hover::before {
  transform: scaleY(1);
}

@keyframes activitySlideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.activity-item:hover {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EEF2FF) 0%, var(--color-background, #F5F3FF) 100%);
  transform: translateX(8px) scale(1.01);
  box-shadow: 0 4px 16px rgba(var(--color-primary-rgb, 37, 99, 235), 0.15);
  border-color: var(--color-primary-light, rgba(99, 102, 241, 0.2));
}

.activity-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.activity-icon.member {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.activity-icon.court {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.activity-icon.booking {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.activity-icon.finance {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-desc {
  font-family: 'Open Sans', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-time {
  font-family: 'Open Sans', sans-serif;
  font-size: 12px;
  color: #94A3B8;
}

.activity-badge {
  padding: 4px 10px;
  border-radius: 6px;
  font-family: 'Open Sans', sans-serif;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.activity-badge.member {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.activity-badge.court {
  background: rgba(240, 147, 251, 0.1);
  color: #f093fb;
}

.activity-badge.booking {
  background: rgba(79, 172, 254, 0.1);
  color: #4facfe;
}

.activity-badge.finance {
  background: rgba(67, 233, 123, 0.1);
  color: #22c55e;
}

/* 运营数据卡片样式 */
.operation-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.operation-stats {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.operation-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px;
  background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 100%);
  border-radius: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #E2E8F0;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.operation-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, var(--color-warning), var(--color-accent));
  transform: scaleY(0);
  transition: transform 0.3s ease;
}

.operation-item:hover {
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EEF2FF) 0%, var(--color-background, #F5F3FF) 100%);
  transform: translateX(6px) scale(1.02);
  box-shadow: 0 4px 16px rgba(var(--color-primary-rgb, 37, 99, 235), 0.12);
  border-color: var(--color-primary-light, rgba(59, 130, 246, 0.2));
}

.operation-item:hover::before {
  transform: scaleY(1);
}

.operation-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  flex-shrink: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.operation-item:hover .operation-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.operation-icon svg {
  width: 24px;
  height: 24px;
}

.operation-icon.weather {
  background: linear-gradient(135deg, #FEF3C7 0%, #FCD34D 100%);
  color: #F59E0B;
}

.operation-icon.booking-rate {
  background: linear-gradient(135deg, #DBEAFE 0%, #60A5FA 100%);
  color: #2563EB;
}

.operation-icon.peak-time {
  background: linear-gradient(135deg, #FCE7F3 0%, #F472B6 100%);
  color: #EC4899;
}

/* 天气加载动画 */
.weather-loading .operation-icon {
  animation: weatherPulse 1.5s ease-in-out infinite;
}

@keyframes weatherPulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.weather-city {
  font-size: 12px;
  font-weight: 400;
  color: #94A3B8;
  margin-left: 8px;
}

.operation-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.operation-value {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
}

.operation-label {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: #64748B;
}

.operation-tip {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px;
  background: linear-gradient(135deg, var(--color-card-bg-hover, #EEF2FF) 0%, var(--color-background, #E0E7FF) 100%);
  border-radius: 10px;
  border-left: 3px solid var(--color-primary);
}

.tip-icon {
  color: var(--color-primary);
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 1px;
}

.operation-tip span {
  font-family: 'Open Sans', sans-serif;
  font-size: 13px;
  color: var(--color-primary-dark);
  line-height: 1.5;
}

/* 响应式布局 */
@media (max-width: 1400px) {
  .dashboard-content {
    max-width: 1400px;
  }
}

@media (max-width: 1200px) {
  .shortcut-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-card {
    padding: 20px;
  }
  
  .stat-value {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .stat-card {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .stat-icon-wrapper {
    margin-right: 0;
    margin-bottom: 14px;
  }

  .stat-value {
    font-size: 28px;
  }

  .shortcut-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .shortcut-item {
    padding: 18px 14px;
  }

  .shortcut-icon {
    width: 52px;
    height: 52px;
  }

  .court-summary {
    gap: 16px;
    padding: 14px;
  }

  .summary-value {
    font-size: 20px;
  }
  
  .glass-card {
    padding: 20px;
  }
  
  .activity-item {
    padding: 16px;
  }
  
  .operation-item {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .shortcut-grid {
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  .stat-card {
    padding: 18px;
  }

  .stat-value {
    font-size: 24px;
  }
  
  .stat-icon-bg {
    width: 56px;
    height: 56px;
  }
  
  .glass-card {
    padding: 16px;
  }
  
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

/* 减少动画对性能敏感用户的影响 */
@media (prefers-reduced-motion: reduce) {
  .stat-card,
  .activity-item,
  .shortcut-item,
  .court-item,
  .operation-item {
    animation: none !important;
    transition: none !important;
  }
}

/* 图表行对齐样式 - 统一网格、减少空白 */
.charts-row-aligned {
  display: flex !important;
  flex-wrap: wrap;
  align-items: stretch !important;
}

.charts-row-aligned > .el-col {
  display: flex !important;
  flex-direction: column;
  margin-bottom: 0;
}

/* 同行等高：单块列拉伸以匹配多块列，消除单侧大面积空白 */
.charts-row-equal.charts-row-aligned {
  align-items: stretch !important;
}

.charts-row-equal > .el-col {
  display: flex !important;
  flex-direction: column;
}

.chart-fill-col {
  flex: 1;
  min-height: 320px;
  display: flex !important;
  flex-direction: column !important;
}

.chart-fill-col :deep(.chart-card),
.chart-fill-col :deep(.glass-card) {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 300px;
}

.chart-col-stack {
  display: flex !important;
  flex-direction: column !important;
  gap: 24px;
}

.chart-stack-item {
  flex-shrink: 0;
}

/* 让图表卡片高度一致 */
.charts-row-aligned :deep(.chart-card),
.charts-row-aligned :deep(.glass-card),
.charts-row-aligned .glass-card {
  display: flex;
  flex-direction: column;
  height: auto;
}

/* 确保活动列表和运营卡片内容填充 */
.charts-row-aligned .activity-card .activity-list,
.charts-row-aligned .operation-card .operation-stats {
  flex: 1;
}

.charts-row-aligned .shortcut-card .shortcut-grid {
  flex: 1;
  align-content: start;
}

.charts-row-aligned .court-status-card .court-list {
  flex: 1;
}

@media (max-width: 1199px) {
  .charts-row-aligned {
    display: block;
  }

  .charts-row-aligned > .el-col {
    display: block;
    margin-bottom: 32px;
  }

  .charts-row-aligned > .el-col:last-child {
    margin-bottom: 0;
  }
}
</style>
