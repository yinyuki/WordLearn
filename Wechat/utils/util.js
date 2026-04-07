// utils/util.js
// 工具函数

// 显示成功提示
const showSuccess = (title) => {
    wx.showToast({ title, icon: 'success' })
}

// 显示错误提示
const showError = (title) => {
    wx.showToast({ title, icon: 'none' })
}

// 显示加载中
const showLoading = (title = '加载中...') => {
    wx.showLoading({ title, mask: true })
}

// 隐藏加载
const hideLoading = () => {
    wx.hideLoading()
}

// 确认弹窗
const showConfirm = (content, title = '提示') => {
    return new Promise((resolve) => {
        wx.showModal({
            title,
            content,
            success: (res) => {
                resolve(res.confirm)
            }
        })
    })
}

// 格式化日期
const formatDate = (date, format = 'YYYY-MM-DD') => {
    if (!date) return ''
    const d = new Date(date)
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')

    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds)
}

// 格式化时间为相对时间
const formatRelativeTime = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date

    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 30) return `${days}天前`
    return formatDate(date)
}

// 截断文本
const truncate = (text, length) => {
    if (!text) return ''
    return text.length > length ? text.substring(0, length) + '...' : text
}

// 显示信息提示
const showInfo = (title) => {
    wx.showToast({ title, icon: 'none' })
}

module.exports = {
    showSuccess,
    showError,
    showInfo,
    showLoading,
    hideLoading,
    showConfirm,
    formatDate,
    formatRelativeTime,
    truncate
}
