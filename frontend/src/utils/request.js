import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200 && res.code !== 0) {
            ElMessage.error(res.msg || '系统错误')
            
            if (res.msg && res.msg.includes('过期') || res.msg.includes('未登录')) {
                localStorage.removeItem('token')
                router.push('/login')
            }
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    error => {
        ElMessage.error('网络请求失败')
        return Promise.reject(error)
    }
)

export default request