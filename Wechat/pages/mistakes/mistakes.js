// pages/mistakes/mistakes.js
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
    data: { loading: true, mistakes: [] },

    onShow() { this.loadMistakes() },

    async loadMistakes() {
        this.setData({ loading: true })
        try {
            const res = await api.getMistakes()
            if (res.code === 200) {
                this.setData({ mistakes: res.data || [] })
            }
        } catch (e) {
            console.error('加载错题失败', e)
        } finally {
            this.setData({ loading: false })
        }
    },

    async deleteMistake(e) {
        const id = e.currentTarget.dataset.id
        const confirm = await util.showConfirm('确定删除此错题？')
        if (confirm) {
            try {
                await api.deleteMistake(id)
                util.showSuccess('已删除')
                this.loadMistakes()
            } catch (e) { util.showError('删除失败') }
        }
    },

    markLearned(e) {
        const id = e.currentTarget.dataset.id
        this.deleteMistake(e)
    }
})
