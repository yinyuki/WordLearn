// app.js
App({
  globalData: {
    userInfo: null,
    token: null
  },

  onLaunch() {
    // 初始化时检查登录状态
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
      this.globalData.userInfo = {
        username: wx.getStorageSync('username'),
        avatar: wx.getStorageSync('avatar')
      }
    }
  },

  // 检查登录状态
  checkLogin() {
    return !!this.globalData.token
  },

  // 清除登录信息
  clearLoginInfo() {
    this.globalData.token = null
    this.globalData.userInfo = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('username')
    wx.removeStorageSync('avatar')
  }
})
