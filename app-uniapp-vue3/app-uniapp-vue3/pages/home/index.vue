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
      <view>
        <picker
          mode="date"
          fields="month"
          :value="month"
          @change="onMonthChange"
        >
          <view class="month-picker">
            <text class="month-label">{{ month.replace('-', '年') }}月</text>
            <text class="month-arrow">⌄</text>
          </view>
        </picker>
        <text class="title">账单</text>
      </view>
        <view class="actions">
          <button class="add-button" @click="goToCreate">记一笔</button>
          <button class="refresh-button" :disabled="loading" @click="refreshHome">
            刷新
          </button>
        </view>

    </view>
	<view class="summary-card">
	  <view class="summary-heading">
	    <text>本月收支</text>
	    <text>共 {{ monthlyStats.billCount }} 笔</text>
	  </view>

	  <view class="summary-totals">
	    <view>
	      <text class="summary-label">总支出</text>
	      <text class="summary-amount">
	        ¥{{ Number(monthlyStats.totalExpense).toFixed(2) }}
	      </text>
	    </view>

	    <view class="summary-income">
	      <text class="summary-label">总收入</text>
	      <text class="summary-amount">
	        ¥{{ Number(monthlyStats.totalIncome).toFixed(2) }}
	      </text>
	    </view>
	  </view>
	</view>
<view class="budget-summary">
  <view class="budget-summary-heading">
    <text>本月预算</text>
    <text>已使用 {{ Math.round(Number(monthlyBudget.totalUsageRatio) * 100) }}%</text>
  </view>

  <view class="budget-summary-amounts">
    <text>已支出 ¥{{ Number(monthlyBudget.totalUsedAmount).toFixed(2) }}</text>
    <text>剩余 ¥{{ Number(monthlyBudget.totalRemainingAmount).toFixed(2) }}</text>
  </view>

  <view class="budget-progress-track">
    <view
      class="budget-progress-fill"
      :style="{ width: budgetProgressWidth(monthlyBudget.totalUsageRatio) }"
    ></view>
  </view>

  <text class="budget-total">
    总预算 ¥{{ Number(monthlyBudget.totalBudgetAmount).toFixed(2) }}
  </text>
</view>
    <view class="tabs">
      <button
        :class="['tab', { active: activeType === 'EXPENSE' }]"
        :disabled="loading"
        @click="selectType('EXPENSE')"
      >
        支出
      </button>
      <button
        :class="['tab', { active: activeType === 'INCOME' }]"
        :disabled="loading"
        @click="selectType('INCOME')"
      >
        收入
      </button>
    </view>
	<picker
	  mode="selector"
	  :range="categories"
	  range-key="name"
	  :value="categoryIndex"
	  @change="onCategoryChange"
	>
	  <view class="category-picker">
	    <text>{{ categories[categoryIndex]?.name || '选择分类' }}</text>
	    <text class="month-arrow">⌄</text>
	  </view>
	</picker>

    <view v-if="loading" class="state">正在加载账单...</view>

    <view v-else-if="errorText" class="state">
      <text>{{ errorText }}</text>
      <button class="retry-button" @click="loadBills">重试</button>
    </view>

    <view v-else-if="bills.length === 0" class="state">
      {{ activeType === 'EXPENSE' ? '本月暂无支出记录' : '本月暂无收入记录' }}
    </view>

    <view v-else class="bill-list">
      <view v-for="bill in bills" :key="bill.id" class="bill-row">
        <view class="bill-info">
          <text class="category">{{ bill.categoryName }}</text>
          <text class="remark">{{ bill.remark || '未填写备注' }}</text>
          <text class="date">{{ bill.transactionDate }}</text>
        </view>

        <view class="bill-right">
          <text
            :class="[
              'amount',
              bill.transactionType === 'EXPENSE' ? 'expense' : 'income'
            ]"
          >
            {{ bill.transactionType === 'EXPENSE' ? '-' : '+' }}¥{{ Number(bill.amount).toFixed(2) }}
          </text>

          <view class="bill-actions">
            <button class="edit-button" @click.stop="goToEdit(bill.id)">
              编辑
            </button>
            <button
              class="delete-button"
              @click.stop="confirmDeleteBill(bill)"
            >
              删除
            </button>
          </view>
        </view>
      </view>
    </view>
	<button
	  v-if="bills.length > 0 && bills.length < totalBills"
	  class="load-more-button"
	  :disabled="loadingMore"
	  @click="loadMoreBills"
	>
	  {{ loadingMore ? '加载中...' : '加载更多' }}
	</button>
	<BottomNav active-page="home" />
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
const activeType = ref('EXPENSE')
const bills = ref([])
const categories = ref([])
const selectedCategoryId = ref('')
const currentPage = ref(1)
const pageSize = 20
const totalBills = ref(0)
const loadingMore = ref(false)
const monthlyStats = ref({
  totalIncome: '0.00',
  totalExpense: '0.00',
  balance: '0.00',
  billCount: 0
})
const monthlyBudget = ref({
  totalBudgetAmount: '0.00',
  totalUsedAmount: '0.00',
  totalRemainingAmount: '0.00',
  totalUsageRatio: '0'
})
const loading = ref(false)
const errorText = ref('')
const categoryIndex = ref(0)
async function loadCategories() {
  try {
    const response = await request({
      url: `/api/categories?type=${activeType.value}`,
      method: 'GET'
    })

    if (response.code !== 1 || !Array.isArray(response.data)) {
      throw new Error(response.msg || '分类加载失败')
    }

    categories.value = [
      { id: '', name: '全部分类' },
      ...response.data
    ]
    categoryIndex.value = 0
    selectedCategoryId.value = ''
  } catch (error) {
    categories.value = [{ id: '', name: '全部分类' }]
    categoryIndex.value = 0
    selectedCategoryId.value = ''

    const message = error && (error.msg || error.message)
    uni.showToast({
      title: message || '分类加载失败',
      icon: 'none'
    })
  }
}

function onCategoryChange(event) {
  categoryIndex.value = Number(event.detail.value)
  selectedCategoryId.value = String(
    categories.value[categoryIndex.value]?.id || ''
  )
  loadBills()
}

async function loadBills() {
	currentPage.value = 1
  loading.value = true
  errorText.value = ''

  try {
    const categoryQuery = selectedCategoryId.value
      ? `&categoryId=${selectedCategoryId.value}`
      : ''

    const response = await request({
      url: `/api/bills?month=${month.value}&transactionType=${activeType.value}${categoryQuery}&page=${currentPage.value}&pageSize=${pageSize}`,
      method: 'GET'
    })
    if (response.code !== 1 || !response.data) {
      throw new Error(response.msg || '账单加载失败')
    }

    bills.value = response.data.rows || []
    totalBills.value = Number(response.data.total || 0)
  } catch (error) {
    errorText.value =
      (error && (error.msg || error.message)) || '账单加载失败，请检查服务连接'
  } finally {
    loading.value = false
  }
}
async function loadMoreBills() {
  if (loadingMore.value || bills.value.length >= totalBills.value) {
    return
  }

  loadingMore.value = true
  const nextPage = currentPage.value + 1

  try {
    const categoryQuery = selectedCategoryId.value
      ? `&categoryId=${selectedCategoryId.value}`
      : ''

    const response = await request({
      url: `/api/bills?month=${month.value}&transactionType=${activeType.value}${categoryQuery}&page=${nextPage}&pageSize=${pageSize}`,
      method: 'GET'
    })

    if (response.code !== 1 || !response.data) {
      throw new Error(response.msg || '加载更多账单失败')
    }

    bills.value = [...bills.value, ...(response.data.rows || [])]
    currentPage.value = nextPage
    totalBills.value = Number(response.data.total || 0)
  } catch (error) {
    const message = error && (error.msg || error.message)
    uni.showToast({
      title: message || '加载更多失败，请重试',
      icon: 'none'
    })
  } finally {
    loadingMore.value = false
  }
}
async function loadMonthlyStatistics() {
  try {
    const response = await request({
      url: `/api/statistics/monthly?month=${month.value}`,
      method: 'GET'
    })

    if (response.code !== 1 || !response.data) {
      throw new Error(response.msg || '月度统计加载失败')
    }

    monthlyStats.value = response.data
  } catch (error) {
    console.error('月度统计加载失败', error)
  }
}
async function loadMonthlyBudget() {
  try {
    const response = await request({
      url: `/api/budgets/monthly/${month.value}`,
      method: 'GET'
    })

    if (response.code !== 1 || !response.data) {
      throw new Error(response.msg || '预算加载失败')
    }

    monthlyBudget.value = response.data
  } catch (error) {
    console.error('首页预算加载失败', error)
  }
}

function budgetProgressWidth(ratio) {
  const percent = Math.min(100, Math.max(0, Number(ratio || 0) * 100))
  return `${percent}%`
}
function refreshHome() {
  loadBills()
  loadMonthlyStatistics()
  loadMonthlyBudget()
}

async function selectType(type) {
  if (activeType.value === type || loading.value) {
    return
  }

  activeType.value = type
  await loadCategories()
  await loadBills()
}
function goToCreate() {
  uni.navigateTo({
    url: '/pages/bills/create'
  })
}
function goToEdit(billId) {
  uni.navigateTo({
    url: `/pages/bills/create?billId=${billId}`
  })
}
function confirmDeleteBill(bill) {
  uni.showModal({
    title: '删除账单',
    content: `确定删除这笔“${bill.categoryName}”账单吗？删除后无法恢复。`,
    confirmText: '删除',
    confirmColor: '#c94f45',
    success: async (result) => {
      if (!result.confirm) {
        return
      }

      try {
        const response = await request({
          url: `/api/bills/${bill.id}`,
          method: 'DELETE'
        })

        if (response.code !== 1) {
          throw new Error(response.msg || '删除账单失败')
        }

        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })

        loadBills()
        loadMonthlyStatistics()
		loadMonthlyBudget()
      } catch (error) {
        const message = error && (error.msg || error.message)
        uni.showToast({
          title: message || '删除账单失败',
          icon: 'none'
        })
      }
    }
  })
}
function onMonthChange(event) {
  month.value = event.detail.value
  loadBills()
  loadMonthlyStatistics()
  loadMonthlyBudget()
}
onShow(async () => {
  await loadCategories()
  await loadBills()
  await loadMonthlyStatistics()
  await loadMonthlyBudget()
})
</script>

<style>
page {
  min-height: 100%;
  background: #f4f7f4;
}

.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 40rpx 36rpx 64rpx;
}

.heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 36rpx;
}

.month {
  display: block;
  margin-bottom: 10rpx;
  color: #69766e;
  font-size: 26rpx;
}

.title {
  display: block;
  color: #20352b;
  font-size: 48rpx;
  font-weight: 700;
}

.refresh-button,
.retry-button {
  margin: 0;
  padding: 0 24rpx;
  border: 1px solid #cbd8ce;
  border-radius: 8rpx;
  background: #ffffff;
  color: #287a50;
  font-size: 26rpx;
  line-height: 72rpx;
}

.refresh-button::after,
.retry-button::after,
.tab::after {
  border: none;
}

.tabs {
  display: flex;
  gap: 12rpx;
  margin-bottom: 28rpx;
}

.tab {
  flex: 1;
  height: 76rpx;
  margin: 0;
  border: 1px solid #d7dfd8;
  border-radius: 8rpx;
  background: #ffffff;
  color: #526258;
  font-size: 28rpx;
  line-height: 76rpx;
}

.tab.active {
  border-color: #287a50;
  background: #e5f0e8;
  color: #216744;
  font-weight: 600;
}

.bill-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.bill-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
  padding: 28rpx 24rpx;
  border: 1px solid #e7efe5;
  border-radius: 16rpx;
  background: #ffffff;
}

.bill-info {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 8rpx;
}

.category {
  color: #20352b;
  font-size: 30rpx;
  font-weight: 600;
}

.remark,
.date {
  color: #69766e;
  font-size: 24rpx;
}

.amount {
  flex-shrink: 0;
  font-size: 30rpx;
  font-weight: 600;
}

.expense {
  color: #b34d43;
}

.income {
  color: #287a50;
}

.state {
  padding: 80rpx 20rpx;
  color: #69766e;
  text-align: center;
  font-size: 28rpx;
}

.retry-button {
  display: block;
  margin: 28rpx auto 0;
}
.actions {
  display: flex;
  gap: 12rpx;
}

.add-button {
  margin: 0;
  padding: 0 22rpx;
  border-radius: 8rpx;
  background: #287a50;
  color: #ffffff;
  font-size: 26rpx;
  line-height: 72rpx;
}

.add-button::after {
  border: none;
}
.month-picker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border: 1px solid #d5e1d9;
  border-radius: 6px;
  background: #fff;
  color: #315b49;
  font-size: 14px;
}

.month-arrow {
  color: #668575;
  font-size: 12px;
}
.summary-card {
  margin-bottom: 28rpx;
  padding: 32rpx;
  border-radius: 24rpx;
  background: #4f8d61;
  color: #ffffff;
}

.summary-heading {
  display: flex;
  justify-content: space-between;
  color: rgba(255, 255, 255, 0.8);
  font-size: 24rpx;
}

.summary-totals {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24rpx;
  margin-top: 32rpx;
}

.summary-totals > view {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.summary-income {
  padding-left: 24rpx;
  border-left: 1px solid rgba(255, 255, 255, 0.25);
}

.summary-label {
  color: rgba(255, 255, 255, 0.75);
  font-size: 24rpx;
}

.summary-amount {
  font-size: 40rpx;
  font-weight: 700;
}
.brand-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 32rpx;
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
.edit-button {
  flex-shrink: 0;
  margin: 0;
  padding: 0 16rpx;
  border: 1px solid #d7e3d8;
  border-radius: 8rpx;
  background: #ffffff;
  color: #417553;
  font-size: 22rpx;
  line-height: 56rpx;
}

.edit-button::after {
  border: none;
}
.delete-button {
  flex-shrink: 0;
  margin: 0;
  padding: 0 16rpx;
  border: 1px solid #ead3d0;
  border-radius: 8rpx;
  background: #ffffff;
  color: #c94f45;
  font-size: 22rpx;
  line-height: 56rpx;
}

.delete-button::after {
  border: none;
}
.bill-right {
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
  align-items: flex-end;
  gap: 16rpx;
}

.bill-actions {
  display: flex;
  gap: 12rpx;
}
.load-more-button {
  display: block;
  margin: 24rpx auto 0;
  padding: 0 28rpx;
  border: 1px solid #d7e3d8;
  border-radius: 8rpx;
  background: #ffffff;
  color: #417553;
  font-size: 24rpx;
  line-height: 68rpx;
}

.load-more-button::after {
  border: none;
}
.category-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72rpx;
  margin-bottom: 24rpx;
  padding: 0 20rpx;
  border: 1px solid #d7dfd8;
  border-radius: 8rpx;
  background: #ffffff;
  color: #315c42;
  font-size: 26rpx;
}
.budget-summary {
  margin-bottom: 28rpx;
  padding: 24rpx 28rpx;
  border: 1px solid #e7efe5;
  border-radius: 16rpx;
  background: #ffffff;
  color: #315c42;
}

.budget-summary-heading,
.budget-summary-amounts {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
}

.budget-summary-heading {
  font-size: 26rpx;
  font-weight: 600;
}

.budget-summary-amounts,
.budget-total {
  color: #69766e;
  font-size: 22rpx;
}

.budget-summary-amounts {
  margin-top: 16rpx;
}

.budget-progress-track {
  height: 12rpx;
  margin-top: 16rpx;
  overflow: hidden;
  border-radius: 6rpx;
  background: #eef5ec;
}

.budget-progress-fill {
  height: 100%;
  border-radius: 6rpx;
  background: #a9d4ad;
}

.budget-total {
  display: block;
  margin-top: 10rpx;
  text-align: right;
}
</style>
