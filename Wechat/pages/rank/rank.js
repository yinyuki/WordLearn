// pages/rank/rank.js
const api = require('../../utils/api')

Page({
    data: {
        loading: true,
        rankList: [],
        defaultAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    },

    onLoad() { this.loadRank() },

    async loadRank() {
        this.setData({ loading: true })
        try {
            const res = await api.getLeaderboard()
            if (res.code === 200) {
                this.setData({ rankList: res.data || [] })
            }
        } catch (e) {
            console.error('加载排行榜失败', e)
        } finally {
            this.setData({ loading: false })
        }
    }
})
