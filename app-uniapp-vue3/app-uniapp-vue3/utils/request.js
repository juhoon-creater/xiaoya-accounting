const BASE_URL = 'http://localhost:8080'
const ANONYMOUS_URLS = ['/api/users/login', '/api/users/register']
let handlingUnauthorized = false

function handleUnauthorized() {
  if (handlingUnauthorized) {
    return
  }

  handlingUnauthorized = true
  uni.removeStorageSync('token')

  uni.showModal({
    title: '登录状态已失效',
    content: '请重新登录后继续使用',
    showCancel: false,
    success: () => {
      uni.reLaunch({
        url: '/pages/index/index',
        complete: () => {
          handlingUnauthorized = false
        }
      })
    }
  })
}
export function request({ url, method = 'GET', data, header = {} }) {
  const requestHeaders = {
    'Content-Type': 'application/json',
    ...header
  }

  const token = uni.getStorageSync('token')

  if (token && !ANONYMOUS_URLS.includes(url)) {
    requestHeaders.Authorization = `Bearer ${token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: requestHeaders,
      success: (response) => {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          resolve(response.data)
        } else {
				if (
					response.statusCode === 401 &&
					!ANONYMOUS_URLS.includes(url)
  ) {
    handleUnauthorized()
  }

  reject(response.data)
}
      },
      fail: (error) => {
        reject(error)
      }
    })
  })
}
