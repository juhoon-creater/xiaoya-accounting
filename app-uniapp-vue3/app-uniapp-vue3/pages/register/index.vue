<template>
  <view class="page">
    <view class="heading">
      <text class="title">创建账号</text>
      <text class="subtitle">注册后即可开始记账</text>
    </view>

    <view class="form">
      <view class="field">
        <text class="label">用户名</text>
        <input
          v-model="username"
          class="input"
          placeholder="3 到 50 个字符"
          maxlength="50"
        />
      </view>

      <view class="field">
        <text class="label">密码</text>
        <input
          v-model="password"
          class="input"
          password
          placeholder="8 到 72 个字符"
          maxlength="72"
        />
      </view>

      <view class="field">
        <text class="label">确认密码</text>
        <input
          v-model="confirmPassword"
          class="input"
          password
          placeholder="再次输入密码"
          maxlength="72"
        />
      </view>

      <button
        class="register-button"
        :disabled="loading"
        @click="handleRegister"
      >
        {{ loading ? '注册中...' : '注册' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { request } from '../../utils/request.js'

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)

async function handleRegister() {
  const registerName = username.value.trim()

  if (registerName.length < 3 || registerName.length > 50) {
    uni.showToast({
      title: '用户名长度需为 3 到 50 个字符',
      icon: 'none'
    })
    return
  }

  if (password.value.length < 8 || password.value.length > 72) {
    uni.showToast({
      title: '密码长度需为 8 到 72 个字符',
      icon: 'none'
    })
    return
  }

  if (password.value !== confirmPassword.value) {
    uni.showToast({
      title: '两次输入的密码不一致',
      icon: 'none'
    })
    return
  }

  loading.value = true

  try {
    const response = await request({
      url: '/api/users/register',
      method: 'POST',
      data: {
        username: registerName,
        password: password.value
      }
    })

    if (response.code !== 1 || !response.data) {
      uni.showToast({
        title: response.msg || '注册失败',
        icon: 'none'
      })
      return
    }

    uni.showModal({
      title: '注册成功',
      content: '账号已创建，请登录',
      showCancel: false,
      success: () => {
        uni.redirectTo({
          url: '/pages/index/index'
        })
      }
    })
  } catch (error) {
    const message = error && (error.msg || error.message)

    uni.showToast({
      title: message || '注册请求失败，请检查服务连接',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
</script>

<style>
page {
  min-height: 100%;
  background: #f4f7f4;
}

.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 100rpx 48rpx 64rpx;
}

.heading {
  margin-bottom: 64rpx;
}

.title {
  display: block;
  color: #20352b;
  font-size: 52rpx;
  font-weight: 700;
}

.subtitle {
  display: block;
  margin-top: 18rpx;
  color: #69766e;
  font-size: 28rpx;
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

.input {
  box-sizing: border-box;
  width: 100%;
  height: 96rpx;
  padding: 0 24rpx;
  border: 1px solid #d7dfd8;
  border-radius: 8rpx;
  background: #ffffff;
  color: #20352b;
  font-size: 30rpx;
}

.register-button {
  height: 96rpx;
  margin-top: 16rpx;
  border-radius: 8rpx;
  background: #287a50;
  color: #ffffff;
  font-size: 32rpx;
  line-height: 96rpx;
}

.register-button::after {
  border: none;
}
</style>
