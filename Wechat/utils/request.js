// utils/request.js
// 网络请求封装

const config = require('./config')

// 请求基础配置
const request = (url, method, data) => {
    return new Promise((resolve, reject) => {
        const token = wx.getStorageSync('token')

        wx.request({
            url: config.baseUrl + '/api' + url,
            method: method,
            data: data,
            header: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            },
            success: (res) => {
                if (res.statusCode === 200) {
                    if (res.data.code === 401) {
                        // token过期，跳转登录
                        wx.removeStorageSync('token')
                        wx.reLaunch({ url: '/pages/login/login' })
                        reject(res.data)
                    } else if (res.data.code === 500 && res.data.msg === 'NOT_LOGIN') {
                        // 未登录状态，跳转登录
                        wx.removeStorageSync('token')
                        wx.reLaunch({ url: '/pages/login/login' })
                        reject(res.data)
                    } else if (res.data.code !== 200) {
                        wx.showToast({
                            title: res.data.msg || '请求失败',
                            icon: 'none'
                        })
                        reject(res.data)
                    } else {
                        resolve(res.data)
                    }
                } else {
                    wx.showToast({
                        title: '网络请求失败',
                        icon: 'none'
                    })
                    reject({ code: res.statusCode, msg: '网络请求失败' })
                }
            },
            fail: (err) => {
                wx.showToast({
                    title: '网络连接失败',
                    icon: 'none'
                })
                reject(err)
            }
        })
    })
}

// GET请求
const get = (url, params) => {
    return request(url, 'GET', params)
}

// POST请求
const post = (url, data) => {
    return request(url, 'POST', data)
}

// PUT请求
const put = (url, data) => {
    return request(url, 'PUT', data)
}

// DELETE请求
const del = (url) => {
    return request(url, 'DELETE')
}

module.exports = {
    get,
    post,
    put,
    del,
    request
}
