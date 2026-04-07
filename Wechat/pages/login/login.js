// pages/login/login.js
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
    data: {
        activeTab: 'login',
        showPassword: false,
        loading: false,
        loginForm: { username: '', password: '' },
        registerForm: { username: '', password: '' }
    },

    onLoad() {
        const token = wx.getStorageSync('token')
        if (token) {
            wx.switchTab({ url: '/pages/index/index' })
        }
    },

    switchTab(e) {
        this.setData({ activeTab: e.currentTarget.dataset.tab })
    },

    onLoginUsernameInput(e) { this.setData({ 'loginForm.username': e.detail.value }) },
    onLoginPasswordInput(e) { this.setData({ 'loginForm.password': e.detail.value }) },
    onRegisterUsernameInput(e) { this.setData({ 'registerForm.username': e.detail.value }) },
    onRegisterPasswordInput(e) { this.setData({ 'registerForm.password': e.detail.value }) },

    async handleLogin() {
        const { username, password } = this.data.loginForm
        if (!username || !password) {
            return util.showError('请输入用户名和密码')
        }

        this.setData({ loading: true })
        try {
            const res = await api.login({ username, password })
            if (res.code === 200 && res.data) {
                wx.setStorageSync('token', res.data.token)
                wx.setStorageSync('username', res.data.username)
                wx.setStorageSync('avatar', res.data.avatar || '')
                util.showSuccess('登录成功')
                setTimeout(() => {
                    wx.switchTab({ url: '/pages/index/index' })
                }, 500)
            }
        } catch (e) {
            console.error('登录失败', e)
        } finally {
            this.setData({ loading: false })
        }
    },

    async handleRegister() {
        const { username, password } = this.data.registerForm
        if (!username || !password) {
            return util.showError('请填写注册信息')
        }

        this.setData({ loading: true })
        try {
            const res = await api.register({ username, password })
            if (res.code === 200) {
                util.showSuccess('注册成功，请登录')
                this.setData({
                    activeTab: 'login',
                    'loginForm.username': username
                })
            }
        } catch (e) {
            console.error('注册失败', e)
        } finally {
            this.setData({ loading: false })
        }
    }
})
