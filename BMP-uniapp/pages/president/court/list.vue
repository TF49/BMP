<template>
	<view class="container">
		<!-- Top Navigation Bar -->
		<view class="nav-header">
			<view class="header-left">
				<image class="avatar" src="https://lh3.googleusercontent.com/aida-public/AB6AXuB9uisTFuxOrewEOBT1_CYDkCpzwp3sh5zaOfAMNpTzHr2kSmpfQ3dG09tSa6zJVWPvhoT3t5iuPY4aSiCqjxcx6eYZsDBrmvsQgdtqefjOV2U--rzRHp4E94BXveAq8b3Xtm_QW1N29JOffaaaVZU1m3wvhzujCI6NCRQffQoz_B4g-uEnbMMiAFAFv4TvrTOalkQpW36-ru3yeBrDtvo9EEis8whEt4fAEccEMmLeqeDcAdEVsJkp2_moehv4EFPsVniOwvmYHwUb" mode="aspectFill"></image>
				<text class="title">场地管理</text>
			</view>
			<view class="header-right">
				<view class="icon-btn">
					<uni-icons type="notification" size="24" color="#5f5e5e"></uni-icons>
				</view>
				<view class="icon-btn">
					<uni-icons type="search" size="24" color="#5f5e5e"></uni-icons>
				</view>
			</view>
		</view>

		<scroll-view scroll-y class="main-content">
			<!-- Hero Section -->
			<view class="hero-section">
				<view class="page-title-box">
					<text class="page-title">场地概览 <text class="dot">.</text></text>
				</view>

				<!-- Filters -->
				<view class="filters">
					<view class="filter-item">
						<text class="filter-label">所属场馆</text>
						<picker @change="onVenueChange" :value="venueIndex" :range="venues">
							<view class="picker-inner">
								<text>{{ venues[venueIndex] }}</text>
								<uni-icons type="bottom" size="14" color="#5f5e5e"></uni-icons>
							</view>
						</picker>
					</view>
					<view class="filter-item">
						<text class="filter-label">当前状态</text>
						<picker @change="onStatusChange" :value="statusIndex" :range="statuses">
							<view class="picker-inner">
								<text>{{ statuses[statusIndex] }}</text>
								<uni-icons type="settings" size="14" color="#5f5e5e"></uni-icons>
							</view>
						</picker>
					</view>
				</view>

				<!-- Add Button -->
				<button class="add-btn" @click="navigateToAdd">
					<uni-icons type="plus-filled" size="20" color="#561d00"></uni-icons>
					<text>添加场地</text>
				</button>
			</view>

			<!-- Court Grid -->
			<view class="court-grid">
				<view v-for="(court, index) in filteredCourts" :key="index" class="court-card shadow-sm">
					<view class="card-header">
						<view class="id-info">
							<text class="court-id">ID: {{ court.id }}</text>
							<text class="court-name">#{{ court.name }} 场地</text>
							<text class="court-location">{{ court.venue }} · {{ court.zone }}</text>
						</view>
						<view :class="['status-badge', court.statusClass]">
							<text>{{ court.statusName }}</text>
						</view>
					</view>

					<view class="card-footer">
						<view class="info-content">
							<block v-if="court.status === 'AVAILABLE'">
								<text class="small-label">计费标准</text>
								<view class="price-box">
									<text class="currency">¥</text>
									<text class="price">{{ court.price }}</text>
									<text class="unit">/小时</text>
								</view>
							</block>
							<block v-else-if="court.status === 'IN_USE'">
								<text class="small-label">计时时长</text>
								<view class="price-box">
									<text class="price">{{ court.duration }}</text>
									<text class="unit"> 分钟</text>
								</view>
							</block>
							<block v-else-if="court.status === 'BOOKED'">
								<text class="small-label">下次预约</text>
								<view class="price-box">
									<text class="time-range">{{ court.nextBooking }}</text>
								</view>
							</block>
							<block v-else-if="court.status === 'MAINTENANCE'">
								<text class="small-label">维护状态</text>
								<view class="price-box">
									<text class="maintenance-text">{{ court.maintenanceMsg }}</text>
								</view>
							</block>
						</view>

						<view class="action-btns">
							<view class="action-btn edit" @click="onEdit(court)">
								<uni-icons type="compose" size="20" color="#5f5e5e"></uni-icons>
							</view>
							<view class="action-btn delete" @click="onDelete(court)">
								<uni-icons type="trash" size="20" color="#5f5e5e"></uni-icons>
							</view>
						</view>
					</view>
				</view>

				<!-- Quick Add Placeholder Card -->
				<view class="add-card border-dashed" @click="navigateToAdd">
					<uni-icons type="plusempty" size="32" color="#5f5e5e"></uni-icons>
					<text class="add-text">点击快速添加场地</text>
				</view>
			</view>

			<!-- Bottom padding for tabbar -->
			<view class="safe-area-bottom"></view>
		</scroll-view>

		<!-- Bottom Tab Bar Mockup -->
		<view class="tab-bar">
			<view class="tab-item" @click="goTab('dashboard')">
				<uni-icons type="home" size="24" color="#5f5e5e"></uni-icons>
				<text class="tab-label">Dashboard</text>
			</view>
			<view class="tab-item" @click="goTab('venue')">
				<uni-icons type="shop" size="24" color="#5f5e5e"></uni-icons>
				<text class="tab-label">Venue</text>
			</view>
			<view class="tab-item active">
				<view class="active-bg">
					<uni-icons type="calendar" size="24" color="#561d00"></uni-icons>
					<text class="tab-label active-text">Court</text>
				</view>
			</view>
			<view class="tab-item" @click="goTab('profile')">
				<uni-icons type="person" size="24" color="#5f5e5e"></uni-icons>
				<text class="tab-label">Profile</text>
			</view>
		</view>

		<!-- Mobile FAB -->
		<view class="fab-btn" @click="navigateToAdd">
			<uni-icons type="plus" size="28" color="#ffffff"></uni-icons>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';

const venues = ['全部场馆', '中心体育馆', '奥体中心', '动力羽毛球馆'];
const venueIndex = ref(0);

const statuses = ['所有状态', '空闲', '使用中', '预约中', '维护中'];
const statusIndex = ref(0);

const courts = ref([
	{
		id: 'CRT-001',
		name: '01',
		venue: '中心体育馆',
		zone: 'A区',
		status: 'AVAILABLE',
		statusName: '空闲',
		statusClass: 'status-available',
		price: 80
	},
	{
		id: 'CRT-002',
		name: '02',
		venue: '中心体育馆',
		zone: 'A区',
		status: 'IN_USE',
		statusName: '使用中',
		statusClass: 'status-inuse',
		duration: 45
	},
	{
		id: 'CRT-003',
		name: '03',
		venue: '中心体育馆',
		zone: 'B区',
		status: 'BOOKED',
		statusName: '预约中',
		statusClass: 'status-booked',
		nextBooking: '14:00 - 16:00'
	},
	{
		id: 'CRT-004',
		name: '04',
		venue: '奥体中心',
		zone: '训练场',
		status: 'MAINTENANCE',
		statusName: '维护中',
		statusClass: 'status-maintenance',
		maintenanceMsg: '地板修整中'
	},
	{
		id: 'CRT-005',
		name: '05',
		venue: '奥体中心',
		zone: 'VIP区',
		status: 'AVAILABLE',
		statusName: '空闲',
		statusClass: 'status-available',
		price: 120
	}
]);

const filteredCourts = computed(() => {
	let list = courts.value;
	if (venueIndex.value > 0) {
		list = list.filter(c => c.venue === venues[venueIndex.value]);
	}
	if (statusIndex.value > 0) {
		const targetStatusName = statuses[statusIndex.value];
		list = list.filter(c => c.statusName === targetStatusName);
	}
	return list;
});

const onVenueChange = (e) => {
	venueIndex.value = e.detail.value;
};

const onStatusChange = (e) => {
	statusIndex.value = e.detail.value;
};

const navigateToAdd = () => {
	uni.navigateTo({
		url: '/pages/president/court/form'
	});
};

const onEdit = (court) => {
	uni.navigateTo({
		url: `/pages/president/court/form?id=${court.id}`
	});
};

const onDelete = (court) => {
	uni.showModal({
		title: '提示',
		content: `确定要删除 ${court.id} 号场地吗？`,
		success: (res) => {
			if (res.confirm) {
				const idx = courts.value.findIndex(c => c.id === court.id);
				if (idx > -1) courts.value.splice(idx, 1);
			}
		}
	});
};

const goTab = (tab) => {
	let url = '';
	switch(tab) {
		case 'dashboard': url = '/pages/president/dashboard/index'; break;
		case 'venue': url = '/pages/president/venue/list'; break;
		case 'profile': url = '/pages/president/profile/index'; break;
	}
	if (url) uni.reLaunch({ url });
};
</script>

<style lang="scss" scoped>
.container {
	min-height: 100vh;
	background-color: #f9f9f9;
	display: flex;
	flex-direction: column;
}

.nav-header {
	position: sticky;
	top: 0;
	z-index: 99;
	background-color: #f9f9f9;
	padding: 20rpx 40rpx;
	padding-top: calc(var(--status-bar-height) + 20rpx);
	display: flex;
	justify-content: space-between;
	align-items: center;

	.header-left {
		display: flex;
		align-items: center;
		gap: 20rpx;

		.avatar {
			width: 80rpx;
			height: 80rpx;
			border-radius: 40rpx;
			background-color: #eee;
		}

		.title {
			font-family: 'Lexend', sans-serif;
			font-weight: 700;
			font-size: 40rpx;
			text-transform: uppercase;
			letter-spacing: 2rpx;
			color: #ff6600;
		}
	}

	.header-right {
		display: flex;
		align-items: center;
		gap: 10rpx;

		.icon-btn {
			padding: 16rpx;
			border-radius: 50%;
			&:active {
				background-color: #eee;
				transform: scale(0.95);
			}
		}
	}
}

.main-content {
	flex: 1;
	padding: 0 40rpx;
}

.hero-section {
	margin-top: 40rpx;
	margin-bottom: 60rpx;

	.page-title-box {
		margin-bottom: 30rpx;
		.page-title {
			font-size: 64rpx;
			font-weight: 800;
			color: #1a1c1c;
			letter-spacing: -2rpx;
			.dot {
				color: #ff6600;
			}
		}
	}

	.filters {
		display: flex;
		gap: 24rpx;
		margin-bottom: 40rpx;

		.filter-item {
			flex: 1;
			
			.filter-label {
				display: block;
				font-size: 20rpx;
				font-weight: 700;
				text-transform: uppercase;
				letter-spacing: 4rpx;
				color: #5f5e5e;
				margin-bottom: 10rpx;
				margin-left: 4rpx;
			}

			.picker-inner {
				background-color: #ffffff;
				height: 90rpx;
				border-radius: 16rpx;
				padding: 0 30rpx;
				display: flex;
				justify-content: space-between;
				align-items: center;
				font-size: 28rpx;
				font-weight: 500;
			}
		}
	}

	.add-btn {
		background-color: #ffdbcd;
		color: #561d00;
		height: 100rpx;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		font-weight: 700;
		font-size: 30rpx;
		box-shadow: 0 12rpx 24rpx -8rpx rgba(255, 102, 0, 0.3);
		border: none;
		
		&:active {
			transform: scale(0.98);
		}
	}
}

.court-grid {
	display: flex;
	flex-direction: column;
	gap: 30rpx;
	padding-bottom: 240rpx;
}

.court-card {
	background-color: #ffffff;
	padding: 40rpx;
	border-radius: 24rpx;
	min-height: 360rpx;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.shadow-sm {
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;

	.id-info {
		display: flex;
		flex-direction: column;
		
		.court-id {
			font-size: 20rpx;
			font-weight: 800;
			color: #5f5e5e;
			letter-spacing: 4rpx;
			text-transform: uppercase;
		}
		
		.court-name {
			font-size: 48rpx;
			font-weight: 900;
			margin-top: 4rpx;
		}
		
		.court-location {
			font-size: 24rpx;
			color: #5f5e5e;
			margin-top: 4rpx;
		}
	}

	.status-badge {
		padding: 8rpx 24rpx;
		border-radius: 100rpx;
		font-size: 20rpx;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: -1rpx;
	}
}

.status-available { background-color: #dcfce7; color: #166534; }
.status-inuse { background-color: #ffdbcd; color: #7c2e00; }
.status-booked { background-color: #d0e4ff; color: #003155; }
.status-maintenance { background-color: #e2dfde; color: #636262; }

.card-footer {
	margin-top: 60rpx;
	display: flex;
	justify-content: space-between;
	align-items: flex-end;

	.info-content {
		display: flex;
		flex-direction: column;
		
		.small-label {
			font-size: 20rpx;
			font-weight: 700;
			color: #5f5e5e;
			text-transform: uppercase;
			letter-spacing: 4rpx;
		}
		
		.price-box {
			margin-top: 4rpx;
			display: flex;
			align-items: baseline;
			
			.currency { font-size: 32rpx; font-weight: 700; color: #ff6600; margin-right: 4rpx; }
			.price { font-size: 48rpx; font-weight: 700; }
			.unit { font-size: 24rpx; color: #5f5e5e; margin-left: 4rpx; }
			.time-range { font-size: 40rpx; font-weight: 700; }
			.maintenance-text { font-size: 36rpx; font-weight: 700; color: #5f5e5e; }
		}
	}

	.action-btns {
		display: flex;
		gap: 16rpx;
		
		.action-btn {
			width: 80rpx;
			height: 80rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			border-radius: 16rpx;
			background-color: #eeeeee;
			
			&.delete:active {
				background-color: #ffdad6;
				color: #ba1a1a;
			}
			
			&:active {
				background-color: #e2e2e2;
			}
		}
	}
}

.add-card {
	background-color: #f9f9f9;
	border: 4rpx dashed #e3bfb1;
	border-radius: 24rpx;
	min-height: 360rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	
	&:active {
		border-color: #ff6600;
		.add-text { color: #ff6600; }
	}

	.add-text {
		font-size: 24rpx;
		font-weight: 700;
		text-transform: uppercase;
		letter-spacing: 4rpx;
		color: #5f5e5e;
		margin-top: 20rpx;
	}
}

.tab-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	height: 120rpx;
	background-color: #ffffff;
	display: flex;
	justify-content: space-around;
	align-items: center;
	padding-bottom: env(safe-area-inset-bottom);
	border-top: 1rpx solid #f3f3f3;
	box-shadow: 0 -4rpx 24rpx rgba(26, 28, 28, 0.06);
	z-index: 100;

	.tab-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		flex: 1;
		
		.tab-label {
			font-size: 20rpx;
			font-weight: 500;
			text-transform: uppercase;
			letter-spacing: 1rpx;
			margin-top: 4rpx;
			color: #5f5e5e;
		}
		
		&.active {
			.active-bg {
				background-color: #ff6600;
				padding: 10rpx 30rpx;
				border-radius: 16rpx;
				display: flex;
				flex-direction: column;
				align-items: center;
			}
			.tab-label { color: #561d00; }
		}
	}
}

.fab-btn {
	position: fixed;
	right: 40rpx;
	bottom: 160rpx;
	width: 110rpx;
	height: 110rpx;
	background-color: #ff6600;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 12rpx 36rpx rgba(255, 102, 0, 0.4);
	z-index: 90;
	
	&:active {
		transform: scale(0.9);
	}
}

.safe-area-bottom {
	height: 120rpx;
	padding-bottom: env(safe-area-inset-bottom);
}
</style>
