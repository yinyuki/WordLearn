// pages/profile/profile.js
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
    data: {
        userInfo: { username: '', avatar: '' },
        defaultAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        dailyLimit: 20,
        usernameForm: { newUsername: '', password: '' },
        pwdForm: { oldPassword: '', newPassword: '', confirmPassword: '' }
    },

    onShow() {
        this.initData()
    },

    initData() {
        const username = wx.getStorageSync('username') || 'User'
        const avatar = wx.getStorageSync('avatar') || ''
        const dailyLimit = wx.getStorageSync('dailyLimit') || 20
        this.setData({ userInfo: { username, avatar }, dailyLimit })
    },

    changeAvatar() {
        wx.chooseImage({
            count: 1,
            sizeType: ['compressed'],
            success: (res) => {
                const tempFilePath = res.tempFilePaths[0]
                wx.getFileSystemManager().readFile({
                    filePath: tempFilePath,
                    encoding: 'base64',
                    success: async (data) => {
                        const base64Str = 'data:image/jpeg;base64,' + data.data
                        try {
                            await api.updateAvatar({ avatar: base64Str })
                            wx.setStorageSync('avatar', base64Str)
                            this.setData({ 'userInfo.avatar': base64Str })
                            util.showSuccess('头像更新成功')
                        } catch (e) {
                            util.showError('上传失败')
                        }
                    }
                })
            }
        })
    },

    increaseLimit() {
        const newVal = Math.min(this.data.dailyLimit + 5, 500)
        this.setData({ dailyLimit: newVal })
        wx.setStorageSync('dailyLimit', newVal)
    },

    decreaseLimit() {
        const newVal = Math.max(this.data.dailyLimit - 5, 5)
        this.setData({ dailyLimit: newVal })
        wx.setStorageSync('dailyLimit', newVal)
    },

    onNewUsernameInput(e) { this.setData({ 'usernameForm.newUsername': e.detail.value }) },
    onUsernamePwdInput(e) { this.setData({ 'usernameForm.password': e.detail.value }) },
    onOldPwdInput(e) { this.setData({ 'pwdForm.oldPassword': e.detail.value }) },
    onNewPwdInput(e) { this.setData({ 'pwdForm.newPassword': e.detail.value }) },
    onConfirmPwdInput(e) { this.setData({ 'pwdForm.confirmPassword': e.detail.value }) },

    async updateUsername() {
        const { newUsername, password } = this.data.usernameForm
        if (!newUsername || !password) return util.showError('请填写完整')
        try {
            const res = await api.updateUsername({ username: newUsername, password })
            if (res.code === 200) {
                util.showSuccess('修改成功，请重新登录')
                setTimeout(() => this.logout(), 1500)
            }
        } catch (e) { util.showError('修改失败') }
    },

    async updatePassword() {
        const { oldPassword, newPassword, confirmPassword } = this.data.pwdForm
        if (!oldPassword || !newPassword) return util.showError('请填写完整')
        if (newPassword.length < 6) return util.showError('新密码至少6位')
        if (newPassword !== confirmPassword) return util.showError('两次密码不一致')
        try {
            const res = await api.updatePassword({ oldPassword, newPassword })
            if (res.code === 200) {
                util.showSuccess('修改成功，请重新登录')
                setTimeout(() => this.logout(), 1500)
            }
        } catch (e) { util.showError('修改失败') }
    },

    async handleLogout() {
        const confirm = await util.showConfirm('确定要退出登录吗？')
        if (confirm) this.logout()
    },

    logout() {
        const app = getApp()
        app.clearLoginInfo()
        wx.reLaunch({ url: '/pages/login/login' })
    },

    goLearn() { wx.navigateTo({ url: '/pages/learn/learn' }) },
    goStats() { wx.navigateTo({ url: '/pages/stats/stats' }) },
    goMistakes() { wx.navigateTo({ url: '/pages/mistakes/mistakes' }) },
    goRank() { wx.navigateTo({ url: '/pages/rank/rank' }) },
    goExtension() { wx.navigateTo({ url: '/pages/extension/extension' }) }
})
