// pages/extension/extension.js
const api = require('../../utils/api')
const config = require('../../utils/config')

Page({
    data: {
        loading: false,
        resources: [],
        articleResources: [],
        audioResources: [],
        videoResources: [],
        filterLanguage: '',
        filterType: '',
        detailVisible: false,
        currentResource: null,
        baseUrl: config.baseUrl || 'http://localhost:8082'
    },

    onLoad() { this.loadResources() },
    onShow() { this.loadResources() },

    async loadResources() {
        this.setData({ loading: true })
        try {
            const params = {}
            if (this.data.filterLanguage) params.language = this.data.filterLanguage
            if (this.data.filterType) params.type = this.data.filterType

            const res = await api.getLearningResources(params)
            if (res.code === 200) {
                const resources = res.data || []
                this.setData({
                    resources,
                    articleResources: resources.filter(r => r.type === 'ARTICLE'),
                    audioResources: resources.filter(r => r.type === 'AUDIO'),
                    videoResources: resources.filter(r => r.type === 'VIDEO')
                })
            }
        } catch (error) {
            console.error('加载资源失败:', error)
        } finally {
            this.setData({ loading: false })
        }
    },

    setLanguage(e) {
        this.setData({ filterLanguage: e.currentTarget.dataset.value })
        this.loadResources()
    },

    setType(e) {
        this.setData({ filterType: e.currentTarget.dataset.value })
        this.loadResources()
    },

    openDetail(e) {
        this.setData({
            detailVisible: true,
            currentResource: e.currentTarget.dataset.item
        })
    },

    closeDetail() {
        this.setData({ detailVisible: false, currentResource: null })
    }
})
