// pages/learn/learn.js
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
    data: {
        loading: true,
        words: [],
        currentIndex: 0,
        currentWord: null,
        showMeaning: false,
        isFinished: false
    },

    onLoad() {
        this.loadTask()
    },

    async loadTask() {
        this.setData({ loading: true })
        try {
            // 先尝试从存储获取传递的学习列表
            const learningListStr = wx.getStorageSync('learningList')
            if (learningListStr) {
                const words = JSON.parse(learningListStr)
                wx.removeStorageSync('learningList') // 使用后清除
                if (words && words.length > 0) {
                    this.setData({
                        words,
                        currentWord: words[0],
                        currentIndex: 0,
                        isFinished: false
                    })
                    return
                }
            }

            // 如果没有传递列表，则获取每日任务
            const dailyLimit = wx.getStorageSync('dailyLimit') || 20
            const bookId = wx.getStorageSync('selectedBookId') || 1
            const res = await api.getDailyTask({ limit: dailyLimit, bookId: bookId })
            
            if (res.code === 200 && res.data) {
                // 合并新词和复习词
                const newWords = res.data.newWords || []
                const reviews = res.data.reviews || []
                const words = [...reviews, ...newWords]
                
                if (words.length > 0) {
                    this.setData({
                        words,
                        currentWord: words[0],
                        currentIndex: 0,
                        isFinished: false
                    })
                } else {
                    this.setData({
                        words: [],
                        currentWord: null,
                        isFinished: true
                    })
                }
            }
        } catch (e) {
            console.error('加载任务失败', e)
            util.showError('加载任务失败')
        } finally {
            this.setData({ loading: false })
        }
    },

    toggleMeaning() {
        this.setData({ showMeaning: true })
    },

    async markRight() {
        await this.submitResult(true)
    },

    async markWrong() {
        await this.submitResult(false)
    },

    async submitResult(remembered) {
        const { currentWord } = this.data
        if (!currentWord) return

        try {
            const bookId = wx.getStorageSync('selectedBookId') || 1
            await api.submitResult({
                wordId: currentWord.id,
                remembered: remembered,
                bookId: bookId
            })
            this.nextWord()
        } catch (e) {
            console.error('提交失败', e)
            this.nextWord()
        }
    },

    nextWord() {
        const { currentIndex, words } = this.data
        const nextIndex = currentIndex + 1

        if (nextIndex >= words.length) {
            this.setData({ 
                currentWord: null, 
                showMeaning: false,
                isFinished: true
            })
            util.showSuccess('学习完成！')
        } else {
            this.setData({
                currentIndex: nextIndex,
                currentWord: words[nextIndex],
                showMeaning: false
            })
        }
    },

    goBack() {
        wx.navigateBack()
    }
})
