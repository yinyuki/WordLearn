// pages/index/index.js
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
    data: {
        userInfo: { username: '', avatar: '' },
        defaultAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        today: '',
        dailyLimit: 20,
        currentBookId: 1,
        taskInfo: {
            reviews: [],
            newWords: [],
            todayWords: []
        }
    },

    onShow() {
        // 检查登录状态
        const token = wx.getStorageSync('token')
        if (!token) {
            wx.reLaunch({ url: '/pages/login/login' })
            return
        }
        this.initData()
        this.loadDailyTask()
    },

    initData() {
        const username = wx.getStorageSync('username') || 'User'
        const avatar = wx.getStorageSync('avatar') || ''
        const dailyLimit = wx.getStorageSync('dailyLimit') || 20
        const currentBookId = wx.getStorageSync('selectedBookId') || 1
        const now = new Date()
        const today = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`

        this.setData({
            userInfo: { username, avatar },
            dailyLimit,
            currentBookId,
            today
        })
    },

    async loadDailyTask() {
        try {
            const res = await api.getDailyTask({
                limit: this.data.dailyLimit,
                bookId: this.data.currentBookId
            })
            if (res.code === 200 && res.data) {
                this.setData({
                    taskInfo: {
                        reviews: res.data.reviews || [],
                        newWords: res.data.newWords || [],
                        todayWords: res.data.todayWords || []
                    }
                })
            }
        } catch (e) {
            console.error('加载任务失败', e)
        }
    },

    goLearn(e) {
        const type = e.currentTarget.dataset.type
        const list = this.data.taskInfo[type]
        if (list && list.length > 0) {
            wx.setStorageSync('learningList', JSON.stringify(list))
            wx.navigateTo({ url: '/pages/learn/learn' })
        } else {
            util.showError('暂无可学习的单词')
        }
    },

    goStats() { wx.navigateTo({ url: '/pages/stats/stats' }) },
    goMistakes() { wx.navigateTo({ url: '/pages/mistakes/mistakes' }) },
    goRank() { wx.navigateTo({ url: '/pages/rank/rank' }) },
    goExtension() { wx.navigateTo({ url: '/pages/extension/extension' }) }
})
