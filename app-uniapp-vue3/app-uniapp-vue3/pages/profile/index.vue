<template>
  <view class="page">
    <view class="brand-header">
      <view class="brand-icon">🌱</view>
      <view class="brand-copy">
        <text class="brand-name">小芽记账</text>
        <text class="brand-caption">让每一笔都有生长的方向</text>
      </view>
    </view>

    <text class="title">我的</text>

    <view v-if="loading" class="state">正在加载账号信息...</view>

    <view v-else-if="errorText" class="state">
      <text>{{ errorText }}</text>
      <button class="retry-button" @click="loadProfile">重试</button>
    </view>

    <template v-else>
      <view class="profile-card">
        <view class="avatar">芽</view>
        <view class="user-info">
          <text class="username">{{ profile.username }}</text>
          <text class="user-id">用户 ID：{{ profile.id }}</text>
        </view>
      </view>

      <button
        class="logout-button"
        :disabled="loggingOut"
        @click="confirmLogout"
      >
        {{ loggingOut ? '正在退出...' : '退出登录' }}
      </button>
    </template>

    <BottomNav active-page="profile" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '../../utils/request.js'
import BottomNav from '../../components/BottomNav.vue'

const profile = ref({})
const loading = ref(false)
const loggingOut = ref(false)
const errorText = ref('')

async function loadProfile() {
  loading.value = true
  errorText.value = ''

  try {
    const response = await request({
      url: '/api/users/me',
      method: 'GET'
    })

    if (response.code !== 1 || !response.data) {
      throw new Error(response.msg || '账号信息加载失败')
    }

    profile.value = response.data
  } catch (error) {
    errorText.value =
      (error && (error.msg || error.message)) || '账号信息加载失败，请重试'
  } finally {
    loading.value = false
  }
}

function confirmLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定要退出当前账号吗？',
    success: async (result) => {
      if (!result.confirm || loggingOut.value) {
        return
      }

      loggingOut.value = true

      try {
        const response = await request({
          url: '/api/users/logout',
          method: 'POST'
        })

        if (response.code !== 1) {
          throw new Error(response.msg || '退出登录失败')
        }

        uni.removeStorageSync('token')
        uni.redirectTo({
          url: '/pages/index/index'
        })
      } catch (error) {
        const message = error && (error.msg || error.message)
        uni.showToast({
          title: message || '退出登录失败，请重试',
          icon: 'none'
        })
      } finally {
        loggingOut.value = false
      }
    }
  })
}

onShow(() => {
  loadProfile()
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
  padding: 40rpx 36rpx 160rpx;
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 36rpx;
}

.brand-icon,
.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28rpx;
  background: #d9efcc;
  color: #417553;
  font-weight: 700;
}

.brand-icon {
  width: 88rpx;
  height: 88rpx;
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

.title {
  display: block;
  margin-bottom: 24rpx;
  color: #315c42;
  font-size: 40rpx;
  font-weight: 700;
}

.profile-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx 24rpx;
  border: 1px solid #e7efe5;
  border-radius: 16rpx;
  background: #ffffff;
}

.avatar {
  width: 88rpx;
  height: 88rpx;
  flex-shrink: 0;
  font-size: 36rpx;
}

.user-info {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 10rpx;
}

.username {
  color: #315c42;
  font-size: 32rpx;
  font-weight: 600;
}

.user-id {
  color: #8a968d;
  font-size: 24rpx;
}

.logout-button,
.retry-button {
  margin: 32rpx 0 0;
  border: 1px solid #ead3d0;
  border-radius: 8rpx;
  background: #ffffff;
  color: #c94f45;
  font-size: 28rpx;
  line-height: 84rpx;
}

.retry-button {
  border-color: #d7e3d8;
  color: #417553;
}

.logout-button::after,
.retry-button::after {
  border: none;
}

.state {
  padding: 64rpx 20rpx;
  color: #8a968d;
  text-align: center;
  font-size: 26rpx;
}
</style>
