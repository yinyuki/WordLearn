// pages/community/community.js
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
    data: {
        loading: true,
        posts: [],
        defaultAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        showPost: false,
        postContent: ''
    },

    onShow() { this.loadPosts() },

    async loadPosts() {
        this.setData({ loading: true })
        try {
            const res = await api.getPosts()
            if (res.code === 200) {
                this.setData({ posts: res.data || [] })
            }
        } catch (e) {
            console.error('加载帖子失败', e)
        } finally {
            this.setData({ loading: false })
        }
    },

    showPostDialog() { this.setData({ showPost: true, postContent: '' }) },
    hidePostDialog() { this.setData({ showPost: false }) },
    onPostInput(e) { this.setData({ postContent: e.detail.value }) },

    async submitPost() {
        const { postContent } = this.data
        if (!postContent.trim()) return util.showError('请输入内容')
        try {
            const res = await api.createPost({ content: postContent })
            if (res.code === 200) {
                util.showSuccess('发布成功')
                this.hidePostDialog()
                this.loadPosts()
            }
        } catch (e) { util.showError('发布失败') }
    },

    async toggleLike(e) {
        const id = e.currentTarget.dataset.id
        const post = this.data.posts.find(p => p.id === id)
        if (!post) return
        try {
            if (post.isLiked) {
                await api.unlikePost(id)
            } else {
                await api.likePost(id)
            }
            this.loadPosts()
        } catch (e) { console.error('操作失败', e) }
    },

    // 阻止事件冒泡
    stopPropagation() { },

    // 显示评论（暂不实现）
    showComments(e) {
        util.showInfo('评论功能开发中')
    }
})
