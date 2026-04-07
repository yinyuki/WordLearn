// pages/books/books.js
const api = require('../../utils/api')
const util = require('../../utils/util')

Page({
    data: {
        loading: true,
        books: []
    },

    onShow() {
        this.loadBooks()
    },

    async loadBooks() {
        this.setData({ loading: true })
        try {
            const res = await api.getBookList()
            if (res.code === 200) {
                this.setData({ books: res.data || [] })
            }
        } catch (e) {
            console.error('加载词书失败', e)
        } finally {
            this.setData({ loading: false })
        }
    },

    selectBook(e) {
        const book = e.currentTarget.dataset.book
        wx.setStorageSync('selectedBookId', book.id)
        wx.setStorageSync('currentBookName', book.bookName)

        const books = this.data.books.map(b => ({
            ...b,
            isSelected: b.id === book.id
        }))
        this.setData({ books })
        util.showSuccess('已选择: ' + book.bookName)
    }
})
