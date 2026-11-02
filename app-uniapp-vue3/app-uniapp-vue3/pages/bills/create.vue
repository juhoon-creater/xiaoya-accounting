<template>
  <view class="page">
    <view class="type-switch">
      <button
        :class="['type-button', { active: transactionType === 'EXPENSE' }]"
        :disabled="loadingCategories || submitting"
        @click="selectType('EXPENSE')"
      >
        支出
      </button>
      <button
        :class="['type-button', { active: transactionType === 'INCOME' }]"
        :disabled="loadingCategories || submitting"
        @click="selectType('INCOME')"
      >
        收入
      </button>
    </view>

    <view class="field">
      <text class="label">金额</text>
      <input
        v-model="amount"
        class="input amount-input"
        type="digit"
        maxlength="13"
        placeholder="0.00"
      />
    </view>

    <view class="field">
      <text class="label">分类</text>
      <picker
        v-if="categories.length"
        mode="selector"
        :range="categories"
        range-key="name"
        :value="categoryIndex"
        @change="onCategoryChange"
      >
        <view class="picker-field">{{ categories[categoryIndex].name }}　›</view>
      </picker>
      <view v-else class="picker-field">
        {{ loadingCategories ? '加载分类中...' : '暂无可用分类' }}
      </view>
    </view>

    <view class="field">
      <text class="label">日期</text>
      <picker
        mode="date"
        :value="transactionDate"
        @change="onDateChange"
      >
        <view class="picker-field">{{ transactionDate }}　›</view>
      </picker>
    </view>

    <view class="field">
      <text class="label">备注</text>
      <textarea
        v-model="remark"
        class="remark-input"
        maxlength="500"
        placeholder="添加备注（选填）"
      />
    </view>

    <button
      class="submit-button"
      :disabled="submitting || loadingCategories || !categories.length"
      @click="saveBill"
    >
      {{ submitting ? '保存中...' : (billId ? '保存修改' : '保存账单') }}
    </button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { request } from '../../utils/request.js'

function getToday() {
  const today = new Date()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${today.getFullYear()}-${month}-${day}`
}

const transactionType = ref('EXPENSE')
const categories = ref([])
const categoryIndex = ref(0)
const amount = ref('')
const transactionDate = ref(getToday())
const remark = ref('')
const loadingCategories = ref(false)
const submitting = ref(false)
const billId = ref('')

async function loadCategories() {
  loadingCategories.value = true

  try {
    const response = await request({
      url: `/api/categories?type=${transactionType.value}`,
      method: 'GET'
    })

    if (response.code !== 1 || !Array.isArray(response.data)) {
      throw new Error(response.msg || '分类加载失败')
    }

    categories.value = response.data
    categoryIndex.value = 0
  } catch (error) {
    categories.value = []
    const message = error && (error.msg || error.message)
    uni.showToast({
      title: message || '分类加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loadingCategories.value = false
  }
}

async function loadBillForEdit() {
  try {
    const response = await request({
      url: `/api/bills/${billId.value}`,
      method: 'GET'
    })

    if (response.code !== 1 || !response.data) {
      throw new Error(response.msg || '账单详情加载失败')
    }

    const bill = response.data
    transactionType.value = bill.transactionType

    await loadCategories()

    amount.value = String(bill.amount)
    transactionDate.value = bill.transactionDate
    remark.value = bill.remark || ''

    const matchedIndex = categories.value.findIndex(
      category => Number(category.id) === Number(bill.categoryId)
    )
    categoryIndex.value = matchedIndex >= 0 ? matchedIndex : 0
  } catch (error) {
    const message = error && (error.msg || error.message)
    uni.showToast({
      title: message || '账单详情加载失败',
      icon: 'none'
    })
  }
}
function selectType(type) {
  if (transactionType.value === type || submitting.value) {
    return
  }

  transactionType.value = type
  categories.value = []
  loadCategories()
}

function onCategoryChange(event) {
  categoryIndex.value = Number(event.detail.value)
}

function onDateChange(event) {
  transactionDate.value = event.detail.value
}

async function saveBill() {
  const amountText = amount.value.trim()

  if (!/^\d{1,10}(\.\d{1,2})?$/.test(amountText) || Number(amountText) <= 0) {
    uni.showToast({
      title: '金额须大于 0，最多保留两位小数',
      icon: 'none'
    })
    return
  }

  const selectedCategory = categories.value[categoryIndex.value]
  if (!selectedCategory) {
    uni.showToast({
      title: '请选择分类',
      icon: 'none'
    })
    return
  }

  submitting.value = true
  const isEditMode = Boolean(billId.value)

  try {
    const response = await request({
      url: isEditMode ? `/api/bills/${billId.value}` : '/api/bills',
      method: isEditMode ? 'PUT' : 'POST',
      data: {
        transactionType: transactionType.value,
        categoryId: selectedCategory.id,
        amount: amountText,
        transactionDate: transactionDate.value,
        remark: remark.value.trim() || null
      }
    })

    if (response.code !== 1 || !response.data) {
      throw new Error(response.msg || '保存账单失败')
    }

    uni.showToast({
      title: isEditMode ? '修改成功' : '保存成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 500)
  } catch (error) {
    const message = error && (error.msg || error.message)
    uni.showToast({
      title: message || '保存失败，请检查服务连接',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

onLoad(async (options) => {
  if (options && options.billId) {
    billId.value = String(options.billId)
    await loadBillForEdit()
  } else {
    await loadCategories()
  }
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
  padding: 36rpx;
}

.type-switch {
  display: flex;
  gap: 12rpx;
  margin-bottom: 40rpx;
}

.type-button {
  flex: 1;
  height: 80rpx;
  margin: 0;
  border: 1px solid #d7dfd8;
  border-radius: 8rpx;
  background: #ffffff;
  color: #526258;
  font-size: 28rpx;
  line-height: 80rpx;
}

.type-button.active {
  border-color: #287a50;
  background: #e5f0e8;
  color: #216744;
  font-weight: 600;
}

.type-button::after,
.submit-button::after {
  border: none;
}

.field {
  margin-bottom: 32rpx;
}

.label {
  display: block;
  margin-bottom: 14rpx;
  color: #33443a;
  font-size: 28rpx;
}

.input,
.picker-field,
.remark-input {
  box-sizing: border-box;
  width: 100%;
  border: 1px solid #d7dfd8;
  border-radius: 8rpx;
  background: #ffffff;
  color: #20352b;
  font-size: 30rpx;
}

.input,
.picker-field {
  height: 92rpx;
  padding: 0 24rpx;
}

.amount-input {
  font-size: 36rpx;
}

.picker-field {
  display: flex;
  align-items: center;
}

.remark-input {
  min-height: 180rpx;
  padding: 20rpx 24rpx;
}

.submit-button {
  height: 96rpx;
  margin-top: 20rpx;
  border-radius: 8rpx;
  background: #287a50;
  color: #ffffff;
  font-size: 32rpx;
  line-height: 96rpx;
}
</style>
