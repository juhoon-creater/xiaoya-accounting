<template>
  <view class="page">
    <view class="heading">
      <text class="title">小芽记账</text>
      <text class="subtitle">登录后继续管理你的收支</text>
    </view>

    <view class="form">
      <view class="field">
        <text class="label">用户名</text>
        <input
          v-model="username"
          class="input"
          placeholder="请输入用户名"
          maxlength="50"
        />
      </view>

      <view class="field">
        <text class="label">密码</text>
        <input
          v-model="password"
          class="input"
          password
          placeholder="请输入密码"
          maxlength="64"
        />
      </view>

      <button
        class="login-button"
        :disabled="loading"
        @click="handleLogin"
      >
        {{ loading ? '登录中...' : '登录' }}
      </button>
	  <view class="register-entry">
	    <text>还没有账号？</text>
	    <text class="register-link" @click="goToRegister">去注册</text>
	  </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { request } from '../../utils/request.js'
import { onShow } from '@dcloudio/uni-app'

const username = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  const loginName = username.value.trim()

  if (!loginName || !password.value) {
    uni.showToast({
      title: '请输入用户名和密码',
      icon: 'none'
    })
    return
  }

  loading.value = true

  try {
    const response = await request({
      url: '/api/users/login',
      method: 'POST',
      data: {
        username: loginName,
        password: password.value
      }
    })

    if (
      response.code !== 1 ||
      !response.data ||
      !response.data.accessToken
    ) {
      uni.showToast({
        title: response.message || '登录失败',
        icon: 'none'
      })
      return
    }

    uni.setStorageSync('token', response.data.accessToken)
	const profileResponse = await request({
	  url: '/api/users/me',
	  method: 'GET'
	})

	if (profileResponse.code !== 1 || !profileResponse.data) {
	  uni.showToast({
	    title: '登录成功，但令牌验证失败',
	    icon: 'none'
	  })
	  return
	}

	console.log('当前登录用户：', profileResponse.data.username)

    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
	uni.redirectTo({
	  url: '/pages/home/index'
	})
  } catch (error) {
    uni.showToast({
      title: '登录请求失败，请检查服务连接',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
async function restoreSession() {
  const token = uni.getStorageSync('token')
  if (!token) {
    return
  }

  try {
    const response = await request({
      url: '/api/users/me',
      method: 'GET'
    })

    if (response.code === 1 && response.data) {
      uni.redirectTo({
        url: '/pages/home/index'
      })
    }
  } catch (error) {
    // 网络暂时不可用时保留 Token，让用户仍可尝试登录。
    console.warn('验证本地登录状态失败', error)
  }
}

onShow(() => {
  restoreSession()
})
function goToRegister() {
  uni.navigateTo({
    url: '/pages/register/index'
  })
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
  padding: 120rpx 48rpx 64rpx;
}

.heading {
  margin-bottom: 72rpx;
}

.title {
  display: block;
  color: #20352b;
  font-size: 56rpx;
  font-weight: 700;
}

.subtitle {
  display: block;
  margin-top: 18rpx;
  color: #69766e;
  font-size: 28rpx;
}

.form {
  width: 100%;
}

.field {
  margin-bottom: 36rpx;
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

.login-button {
  height: 96rpx;
  margin-top: 16rpx;
  border-radius: 8rpx;
  background: #287a50;
  color: #ffffff;
  font-size: 32rpx;
  line-height: 96rpx;
}

.login-button::after {
  border: none;
}
.register-entry {
  margin-top: 32rpx;
  text-align: center;
  color: #69766e;
  font-size: 26rpx;
}

.register-link {
  margin-left: 12rpx;
  color: #287a50;
}
</style>
