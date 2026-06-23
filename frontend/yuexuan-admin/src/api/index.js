import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：自动附加 JWT Bearer Token
api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('admin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：自动解包 {code:0, data:...} + 401 跳转登录
api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 0) return res.data
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(res)
  },
  error => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem('admin_token')
      window.location.hash = '#/login'
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default api
