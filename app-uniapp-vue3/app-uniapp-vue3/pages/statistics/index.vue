<template>
  <view class="page">
    <view class="brand-header">
      <view class="brand-icon">🌱</view>
      <view class="brand-copy">
        <text class="brand-name">小芽记账</text>
        <text class="brand-caption">让每一笔都有生长的方向</text>
      </view>
    </view>

    <view class="heading">
      <text class="title">统计分析</text>
      <picker
        mode="date"
        fields="month"
        :value="month"
        @change="onMonthChange"
      >
        <view class="month-picker">
          <text>{{ month.replace('-', '年') }}月</text>
          <text class="month-arrow">⌄</text>
        </view>
      </picker>
    </view>

    <view v-if="loading" class="state">正在加载统计数据...</view>
    <view v-else-if="errorText" class="state">{{ errorText }}</view>

    <template v-else>
      <view class="summary-card">
        <view class="summary-item">
          <text class="summary-label">总收入</text>
          <text class="summary-amount">¥{{ Number(stats.totalIncome).toFixed(2) }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">总支出</text>
          <text class="summary-amount">¥{{ Number(stats.totalExpense).toFixed(2) }}</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">结余</text>
          <text class="summary-amount">¥{{ Number(stats.balance).toFixed(2) }}</text>
        </view>
      </view>

      <view class="section-title">
        <text>支出分类</text>
        <text class="bill-count">{{ stats.billCount }} 笔账单</text>
      </view>

      <view v-if="stats.expenseByCategory.length" class="category-list">
        <view
          v-for="item in stats.expenseByCategory"
          :key="item.categoryId"
          class="category-row"
        >
          <view class="category-heading">
            <text class="category-name">{{ item.categoryName }}</text>
            <text class="category-amount">¥{{ Number(item.amount).toFixed(2) }}</text>
          </view>
          <view class="bar-track">
            <view
              class="bar-fill"
              :style="{ width: `${Number(item.ratio) * 100}%` }"
            ></view>
          </view>
          <text class="category-ratio">
            {{ Math.round(Number(item.ratio) * 100) }}%
          </text>
        </view>
      </view>

      <view v-else class="state">本月暂无支出分类数据</view>
    </template>
	<BottomNav active-page="statistics" />
  </view>
</template>

<script setup>
	import { ref } from 'vue'
	import { onShow } from '@dcloudio/uni-app'
	import { request } from '../../utils/request.js'
	import BottomNav from '../../components/BottomNav.vue'

	function getCurrentMonth() {
	  const now = new Date()
	  const monthNumber = String(now.getMonth() + 1).padStart(2, '0')
	  return `${now.getFullYear()}-${monthNumber}`
	}

	const month = ref(getCurrentMonth())
	const stats = ref({
	  totalIncome: '0.00',
	  totalExpense: '0.00',
	  balance: '0.00',
	  billCount: 0,
	  expenseByCategory: []
	})
	const loading = ref(false)
	const errorText = ref('')

async function loadStatistics() {
  loading.value = true
  errorText.value = ''

  try {
    const response = await request({
      url: `/api/statistics/monthly?month=${month.value}`,
      method: 'GET'
    })

    if (response.code !== 1 || !response.data) {
      throw new Error(response.msg || '统计数据加载失败')
    }

    stats.value = response.data
  } catch (error) {
    errorText.value =
      (error && (error.msg || error.message)) || '统计数据加载失败，请检查服务连接'
  } finally {
    loading.value = false
  }
}

function onMonthChange(event) {
  month.value = event.detail.value
  loadStatistics()
}

	onShow(() => {
	  loadStatistics()
	})

</script>

<style>
	page {
	  min-height: 100%;
	  background: #f7faf4;
	}

	.page {
	  min-height: 100vh;
	  box-sizing: border-box;
	  padding: 40rpx 36rpx 64rpx;
	}

	.brand-header {
	  display: flex;
	  align-items: center;
	  gap: 20rpx;
	  margin-bottom: 36rpx;
	}

	.brand-icon {
	  display: flex;
	  width: 88rpx;
	  height: 88rpx;
	  align-items: center;
	  justify-content: center;
	  border-radius: 28rpx;
	  background: #d9efcc;
	  font-size: 42rpx;
	}

	.brand-copy {
	  display: flex;
	  flex-direction: column;
	  gap: 6rpx;
	}

	.brand-name {
	  color: #315c42;
	  font-size: 34rpx;
	  font-weight: 700;
	}

	.brand-caption {
	  color: #8a968d;
	  font-size: 22rpx;
	}

	.heading,
	.section-title,
	.category-heading {
	  display: flex;
	  align-items: center;
	  justify-content: space-between;
	}

	.heading {
	  justify-content: flex-start;
	  gap: 24rpx;
	  margin-bottom: 28rpx;
	}

	.title {
	  color: #20352b;
	  font-size: 40rpx;
	  font-weight: 700;
	}

	.month-picker {
	  display: flex;
	  align-items: center;
	  gap: 10rpx;
	  color: #315c42;
	  font-size: 26rpx;
	}

	.month-arrow,
	.bill-count,
	.category-ratio {
	  color: #8a968d;
	  font-size: 22rpx;
	}

	.summary-card {
	  display: grid;
	  grid-template-columns: repeat(3, 1fr);
	  gap: 16rpx;
	  margin-bottom: 40rpx;
	  padding: 28rpx 20rpx;
	  border-radius: 20rpx;
	  background: #4f8d61;
	  color: #ffffff;
	}

	.summary-item {
	  display: flex;
	  min-width: 0;
	  flex-direction: column;
	  gap: 12rpx;
	}

	.summary-label {
	  color: rgba(255, 255, 255, 0.78);
	  font-size: 22rpx;
	}

	.summary-amount {
	  overflow-wrap: anywhere;
	  font-size: 26rpx;
	  font-weight: 700;
	}

	.section-title {
	  margin-bottom: 20rpx;
	  color: #315c42;
	  font-size: 30rpx;
	  font-weight: 600;
	}

	.category-list {
	  display: flex;
	  flex-direction: column;
	  gap: 16rpx;
	}

	.category-row {
	  padding: 24rpx;
	  border: 1px solid #e7efe5;
	  border-radius: 16rpx;
	  background: #ffffff;
	}

	.category-name {
	  color: #315c42;
	  font-size: 28rpx;
	  font-weight: 600;
	}

	.category-amount {
	  color: #526258;
	  font-size: 26rpx;
	}

	.bar-track {
	  height: 12rpx;
	  margin-top: 18rpx;
	  overflow: hidden;
	  border-radius: 6rpx;
	  background: #eef5ec;
	}

	.bar-fill {
	  height: 100%;
	  border-radius: 6rpx;
	  background: #a9d4ad;
	}

	.category-ratio {
	  display: block;
	  margin-top: 10rpx;
	  text-align: right;
	}

	.state {
	  padding: 64rpx 20rpx;
	  color: #8a968d;
	  text-align: center;
	  font-size: 26rpx;
	}

</style>
