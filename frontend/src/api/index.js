import request from '../utils/request'

// ==================== 1. 基础认证 ====================
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const getUserInfo = () => request.get('/auth/info')

// ==================== 2. 个人中心设置 ====================
export const updatePassword = (data) => request.put('/auth/password', data)
export const updateUsername = (data) => request.put('/auth/username', data)
export const updateAvatar = (data) => request.put('/auth/avatar', data)
export const resetAvatar = () => request.delete('/auth/avatar')

// ==================== 3. 单词管理 ====================
export const getWordList = (params) => request.get('/words', { params })
export const addWord = (data) => request.post('/words', data)
export const updateWord = (data) => request.put('/words', data)
export const deleteWord = (id) => request.delete(`/words/${id}`)

// ==================== 4. 学习核心逻辑 ====================
export const getDailyTask = (params) => request.get('/learn/task', { params })
export const submitResult = (data) => request.post('/learn/submit', data)
export const getLearningProgress = () => request.get('/learn/progress')

// ==================== 5. 单词书管理 ====================
export const getBookList = () => request.get('/books')
export const getBooks = getBookList
export const addBook = (data) => request.post('/books', data)
export const deleteBook = (id) => request.delete(`/books/${id}`)
export const updateBook = (data) => request.put('/books', data)
export const getBookWords = (bookId) => request.get(`/books/${bookId}/words`)

// ==================== 6. 数据统计 ====================
export const getStats = (params) => request.get('/stats', { params })

// ==================== 7. 社区功能 ====================
export const getPosts = () => request.get('/extension/posts')
export const createPost = (data) => request.post('/extension/posts', data)
export const deletePost = (id) => request.delete(`/extension/posts/${id}`)

export const getPostComments = (id) => request.get(`/extension/posts/${id}/comments`)
export const addPostComment = (id, data) => request.post(`/extension/posts/${id}/comments`, data)
export const deletePostComment = (commentId) => request.delete(`/extension/posts/comments/${commentId}`)

export const likePost = (id) => request.post(`/extension/posts/${id}/like`)
export const unlikePost = (id) => request.post(`/extension/posts/${id}/unlike`)

// ==================== 8. 错题本与排行榜 ====================
export const getMistakes = () => request.get('/extension/mistakes')
export const deleteMistake = (id) => request.delete(`/extension/mistakes/${id}`)
export const getLeaderboard = () => request.get('/extension/rank')

// ==================== 9. 拓展学习 ====================
export const getLearningResources = (params) => request.get('/extension/learning', { params })
export const getLearningResourceById = (id) => request.get(`/extension/learning/${id}`)
export const addLearningResource = (data) => request.post('/extension/learning', data)
export const updateLearningResource = (data) => request.put('/extension/learning', data)
export const deleteLearningResource = (id) => request.delete(`/extension/learning/${id}`)
export const pinLearningResource = (id) => request.post(`/extension/learning/pin/${id}`)
export const updateResourceOrder = (ids) => request.post('/extension/learning/update-sort', ids)