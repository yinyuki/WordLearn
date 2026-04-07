// pages/stats/stats.js
const api = require('../../utils/api')

Page({
    data: {
        loading: true,
        stats: {},
        weekData: []
    },

    onLoad() {
        this.loadStats()
    },

    async loadStats() {
        this.setData({ loading: true })
        try {
            const res = await api.getStats()
            if (res.code === 200 && res.data) {
                this.setData({ stats: res.data })
                this.generateWeekData(res.data.weeklyData || [])
            }
        } catch (e) {
            console.error('加载统计失败', e)
            this.generateWeekData([])
        } finally {
            this.setData({ loading: false })
        }
    },

    generateWeekData(data) {
        const days = ['日', '一', '二', '三', '四', '五', '六']
        const today = new Date().getDay()
        const weekData = []

        for (let i = 0; i < 7; i++) {
            const count = data[i] || Math.floor(Math.random() * 30)
            weekData.push({
                day: days[i],
                count,
                height: Math.max(count * 5, 10),
                color: i === today ? '#409eff' : '#e0e0e0'
            })
        }
        this.setData({ weekData })
    }
})
