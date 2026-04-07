// utils/api.js
// API接口定义

const { get, post, put, del } = require('./request')

// ==================== 1. 基础认证 ====================
const login = (data) => post('/auth/login', data)
const register = (data) => post('/auth/register', data)
const getUserInfo = () => get('/auth/info')

// ==================== 2. 个人中心设置 ====================
const updatePassword = (data) => put('/auth/password', data)
const updateUsername = (data) => put('/auth/username', data)
const updateAvatar = (data) => put('/auth/avatar', data)
const resetAvatar = () => del('/auth/avatar')

// ==================== 3. 单词管理 ====================
const getWordList = (params) => get('/words', params)
const addWord = (data) => post('/words', data)
const updateWord = (data) => put('/words', data)
const deleteWord = (id) => del(`/words/${id}`)

// ==================== 4. 学习核心逻辑 ====================
const getDailyTask = (params) => get('/learn/task', params)
const submitResult = (data) => post('/learn/submit', data)
const getLearningStats = (params) => get('/learn/stats', params)

// ==================== 5. 单词书管理 ====================
const getBookList = () => get('/books')
const getBooks = getBookList
const addBook = (data) => post('/books', data)
const deleteBook = (id) => del(`/books/${id}`)
const updateBook = (data) => put('/books', data)
const getBookWords = (bookId) => get(`/books/${bookId}/words`)

// ==================== 6. 数据统计 ====================
const getStats = (params) => get('/stats', params)

// ==================== 7. 社区功能 ====================
const getPosts = () => get('/extension/posts')
const createPost = (data) => post('/extension/posts', data)
const deletePost = (id) => del(`/extension/posts/${id}`)

const getPostComments = (id) => get(`/extension/posts/${id}/comments`)
const addPostComment = (id, data) => post(`/extension/posts/${id}/comments`, data)
const deletePostComment = (commentId) => del(`/extension/posts/comments/${commentId}`)

const likePost = (id) => post(`/extension/posts/${id}/like`)
const unlikePost = (id) => post(`/extension/posts/${id}/unlike`)

// ==================== 8. 错题本与排行榜 ====================
const getMistakes = () => get('/extension/mistakes')
const deleteMistake = (id) => del(`/extension/mistakes/${id}`)
const getLeaderboard = () => get('/extension/rank')

// ==================== 9. 拓展学习 ====================
const getLearningResources = (params) => get('/extension/learning', params)
const getLearningResourceById = (id) => get(`/extension/learning/${id}`)
const addLearningResource = (data) => post('/extension/learning', data)
const updateLearningResource = (data) => put('/extension/learning', data)
const deleteLearningResource = (id) => del(`/extension/learning/${id}`)
const pinLearningResource = (id) => post(`/extension/learning/pin/${id}`)
const updateResourceOrder = (ids) => post('/extension/learning/update-sort', ids)

module.exports = {
    // 认证
    login, register, getUserInfo,
    // 个人中心
    updatePassword, updateUsername, updateAvatar, resetAvatar,
    // 单词管理
    getWordList, addWord, updateWord, deleteWord,
    // 学习
    getDailyTask, submitResult, getLearningStats,
    // 词书
    getBookList, getBooks, addBook, deleteBook, updateBook, getBookWords,
    // 统计
    getStats,
    // 社区
    getPosts, createPost, deletePost,
    getPostComments, addPostComment, deletePostComment,
    likePost, unlikePost,
    // 错题本与排行榜
    getMistakes, deleteMistake, getLeaderboard,
    // 拓展学习
    getLearningResources, getLearningResourceById,
    addLearningResource, updateLearningResource, deleteLearningResource,
    pinLearningResource, updateResourceOrder
}
