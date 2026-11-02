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
      <text class="title">预算管理</text>
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

    <view v-if="loading" class="state">正在加载预算...</view>
    <view v-else-if="errorText" class="state">
      <text>{{ errorText }}</text>
      <button class="retry-button" @click="loadBudget">重试</button>
    </view>

    <template v-else>
      <view class="summary-card">
        <text class="summary-label">本月总预算</text>
        <text class="total-amount">
          ¥{{ Number(budget.totalBudgetAmount).toFixed(2) }}
        </text>

        <view class="usage-info">
          <text>已支出 ¥{{ Number(budget.totalUsedAmount).toFixed(2) }}</text>
          <text>剩余 ¥{{ Number(budget.totalRemainingAmount).toFixed(2) }}</text>
        </view>

        <view class="bar-track">
          <view
            class="bar-fill"
            :style="{ width: progressWidth(budget.totalUsageRatio) }"
          ></view>
        </view>
        <text class="usage-ratio">
          已使用 {{ Math.round(Number(budget.totalUsageRatio) * 100) }}%
        </text>
      </view>

      <view class="section-title">分类预算</view>

      <view class="category-list">
        <view
          v-for="item in budget.categories"
          :key="item.categoryId"
          class="category-row"
        >
          <view class="category-heading">
            <text class="category-name">{{ item.categoryName }}</text>
            <text class="category-amount">
              ¥{{ Number(item.usedAmount).toFixed(2) }}
              / ¥{{ Number(item.budgetAmount).toFixed(2) }}
            </text>
          </view>

          <view class="bar-track category-track">
            <view
              class="bar-fill"
              :style="{ width: progressWidth(item.usageRatio) }"
            ></view>
          </view>
		  <view class="budget-editor">
		    <input
		      v-model="draftAmounts[item.categoryId]"
		      type="digit"
		      placeholder="输入分类预算"
		      class="budget-input"
		    />
		    <button
		      class="save-button"
		      :disabled="savingCategoryId === item.categoryId"
		      @click="saveCategoryBudget(item)"
		    >
		      {{ savingCategoryId === item.categoryId ? '保存中' : '保存' }}
		    </button>
			<button
			  v-if="Number(item.budgetAmount) > 0"
			  class="clear-button"
			  :disabled="savingCategoryId === item.categoryId"
			  @click="confirmClearBudget(item)"
			>
			  清除
			</button>
		  </view>

          <text class="remaining">
            剩余 ¥{{ Number(item.remainingAmount).toFixed(2) }}
          </text>
        </view>
      </view>
    </template>
	<BottomNav active-page="budget" />
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
	const loading = ref(false)
	const errorText = ref('')
	const budget = ref({
	  totalBudgetAmount: '0.00',
	  totalUsedAmount: '0.00',
	  totalRemainingAmount: '0.00',
	  totalUsageRatio: '0',
	  categories: []
	})
	const draftAmounts = ref({})
	const savingCategoryId = ref(null)

	async function loadBudget() {
	  loading.value = true
	  errorText.value = ''

	  try {
	    const response = await request({
	      url: `/api/budgets/monthly/${month.value}`,
	      method: 'GET'
	    })

	    if (response.code !== 1 || !response.data) {
	      throw new Error(response.msg || '预算加载失败')
	    }

	    budget.value = response.data
	    draftAmounts.value = Object.fromEntries(
	      response.data.categories.map(item => [
	        item.categoryId,
	        Number(item.budgetAmount) > 0 ? String(item.budgetAmount) : ''
	      ])
	    )
	  } catch (error) {
	    errorText.value =
	      (error && (error.msg || error.message)) || '预算加载失败，请检查服务连接'
	  } finally {
	    loading.value = false
	  }
	}
	async function saveCategoryBudget(item) {
	  const amountText = String(draftAmounts.value[item.categoryId] || '').trim()

	  if (!/^\d+(\.\d{1,2})?$/.test(amountText) || Number(amountText) <= 0) {
	    uni.showToast({
	      title: '请输入大于 0 且最多两位小数的金额',
	      icon: 'none'
	    })
	    return
	  }

	  savingCategoryId.value = item.categoryId

	  try {
	    const response = await request({
	      url: `/api/budgets/monthly/${month.value}/categories/${item.categoryId}`,
	      method: 'PUT',
	      data: {
	        amount: Number(amountText).toFixed(2)
	      }
	    })

	    if (response.code !== 1 || !response.data) {
	      throw new Error(response.msg || '预算保存失败')
	    }

	    budget.value = response.data
	    draftAmounts.value = Object.fromEntries(
	      response.data.categories.map(category => [
	        category.categoryId,
	        Number(category.budgetAmount) > 0
	          ? String(category.budgetAmount)
	          : ''
	      ])
	    )

	    uni.showToast({
	      title: '预算已保存',
	      icon: 'success'
	    })
	  } catch (error) {
	    uni.showToast({
	      title: (error && (error.msg || error.message)) || '预算保存失败',
	      icon: 'none'
	    })
	  } finally {
	    savingCategoryId.value = null
	  }
	}
	function confirmClearBudget(item) {
	  uni.showModal({
	    title: '清除分类预算',
	    content: `确定清除“${item.categoryName}”的预算设置吗？账单记录不会删除。`,
	    confirmText: '清除',
	    success: async (result) => {
	      if (!result.confirm || savingCategoryId.value !== null) {
	        return
	      }

	      savingCategoryId.value = item.categoryId

	      try {
	        const response = await request({
	          url: `/api/budgets/monthly/${month.value}/categories/${item.categoryId}`,
	          method: 'DELETE'
	        })

	        if (response.code !== 1) {
	          throw new Error(response.msg || '清除预算失败')
	        }

	        await loadBudget()
	        uni.showToast({
	          title: '预算已清除',
	          icon: 'success'
	        })
	      } catch (error) {
	        const message = error && (error.msg || error.message)
	        uni.showToast({
	          title: message || '清除预算失败',
	          icon: 'none'
	        })
	      } finally {
	        savingCategoryId.value = null
	      }
	    }
	  })
	}

	function onMonthChange(event) {
	  month.value = event.detail.value
	  loadBudget()
	}

	function progressWidth(ratio) {
	  const percent = Math.min(100, Math.max(0, Number(ratio || 0) * 100))
	  return `${percent}%`
	}

	onShow(() => {
	  loadBudget()
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
	.category-heading,
	.usage-info {
	  display: flex;
	  align-items: center;
	  justify-content: space-between;
	}

	.heading {
	  justify-content: flex-start;
	  gap: 24rpx;
	  margin-bottom: 28rpx;
	}

	.title,
	.section-title {
	  color: #315c42;
	  font-size: 34rpx;
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
	.usage-ratio,
	.remaining {
	  color: #8a968d;
	  font-size: 22rpx;
	}

	.summary-card {
	  margin-bottom: 36rpx;
	  padding: 32rpx;
	  border-radius: 20rpx;
	  background: #4f8d61;
	  color: #ffffff;
	}

	.summary-label {
	  color: rgba(255, 255, 255, 0.78);
	  font-size: 24rpx;
	}

	.total-amount {
	  display: block;
	  margin-top: 8rpx;
	  font-size: 48rpx;
	  font-weight: 700;
	}

	.usage-info {
	  gap: 12rpx;
	  margin-top: 28rpx;
	  color: rgba(255, 255, 255, 0.85);
	  font-size: 22rpx;
	}

	.bar-track {
	  height: 12rpx;
	  margin-top: 18rpx;
	  overflow: hidden;
	  border-radius: 6rpx;
	  background: rgba(255, 255, 255, 0.25);
	}

	.bar-fill {
	  height: 100%;
	  border-radius: 6rpx;
	  background: #ffe29c;
	}

	.usage-ratio {
	  display: block;
	  margin-top: 10rpx;
	  color: rgba(255, 255, 255, 0.8);
	  text-align: right;
	}

	.section-title {
	  margin-bottom: 20rpx;
	  font-size: 30rpx;
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
	  font-size: 23rpx;
	}

	.category-track {
	  background: #eef5ec;
	}

	.category-track .bar-fill {
	  background: #a9d4ad;
	}

	.remaining {
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

	.retry-button {
	  display: block;
	  margin: 24rpx auto 0;
	}
	.budget-editor {
	  display: flex;
	  align-items: center;
	  gap: 12rpx;
	  margin-top: 20rpx;
	}

	.budget-input {
	  flex: 1;
	  min-width: 0;
	  height: 68rpx;
	  box-sizing: border-box;
	  padding: 0 16rpx;
	  border: 1px solid #d7e3d8;
	  border-radius: 8rpx;
	  background: #ffffff;
	  color: #315c42;
	  font-size: 24rpx;
	}

	.save-button {
	  flex-shrink: 0;
	  margin: 0;
	  padding: 0 22rpx;
	  border-radius: 8rpx;
	  background: #4f8d61;
	  color: #ffffff;
	  font-size: 24rpx;
	  line-height: 68rpx;
	}

	.save-button::after {
	  border: none;
	}
	.clear-button {
	  flex-shrink: 0;
	  margin: 0;
	  padding: 0 20rpx;
	  border: 1px solid #ead3d0;
	  border-radius: 8rpx;
	  background: #ffffff;
	  color: #c94f45;
	  font-size: 24rpx;
	  line-height: 68rpx;
	}

	.clear-button::after {
	  border: none;
	}

</style>
