<template>
  <view class="bottom-nav">
    <view
      v-for="item in items"
      :key="item.key"
      :class="['nav-item', { active: activePage === item.key }]"
      @click="openPage(item)"
    >
      <text class="nav-icon">{{ item.icon }}</text>
      <text class="nav-label">{{ item.label }}</text>
      <view v-if="activePage === item.key" class="active-dot"></view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  activePage: {
    type: String,
    required: true
  }
})

const navigating = ref(false)
const items = [
  { key: 'home', label: '账单', icon: '⌂', url: '/pages/home/index' },
  { key: 'statistics', label: '统计', icon: '▥', url: '/pages/statistics/index' },
  { key: 'budget', label: '预算', icon: '◌', url: '/pages/budget/index' },
  { key: 'profile', label: '我的', icon: '●', url: '/pages/profile/index' }
]

function openPage(item) {
  if (props.activePage === item.key || navigating.value) {
    return
  }

  navigating.value = true
  uni.redirectTo({
    url: item.url,
    complete: () => {
      navigating.value = false
    }
  })
}
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  display: flex;
  justify-content: space-around;
  padding: 14rpx 20rpx calc(12rpx + env(safe-area-inset-bottom));
  border-top: 1px solid #e7efe5;
  background: rgba(255, 255, 255, 0.97);
}

.nav-item {
  position: relative;
  display: flex;
  min-width: 120rpx;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  color: #8a968d;
}

.nav-item.active {
  color: #417553;
  font-weight: 600;
}

.nav-icon {
  font-size: 38rpx;
  line-height: 1.2;
}

.nav-label {
  font-size: 22rpx;
}

.active-dot {
  position: absolute;
  bottom: -8rpx;
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #f0b783;
}
</style>
