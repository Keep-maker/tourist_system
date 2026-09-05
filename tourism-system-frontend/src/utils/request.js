import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth.js'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: '/',
  timeout: 30000
})

// 请求拦截器：自动携带token
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应
service.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果返回的是文件流，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 处理业务状态码
    if (res.code !== 200) {
      // 401未登录，跳转登录页（已在登录页时静默处理，不弹窗）
      if (res.code === 401) {
        removeToken()
        if (router.currentRoute.value.path !== '/login') {
          ElMessage.error(res.msg || '未登录或登录已过期')
          router.push('/login')
        }
        return Promise.reject(new Error(res.msg || '未授权'))
      }
      // 400业务错误
      if (res.code === 400) {
        ElMessage.error(res.msg || '业务错误')
        return Promise.reject(new Error(res.msg || '业务错误'))
      }
      // 500系统异常
      ElMessage.error(res.msg || '系统异常')
      return Promise.reject(new Error(res.msg || '系统异常'))
    }
    
    return res.data
  },
  (error) => {
    if (error.response) {
      // HTTP状态码处理
      if (error.response.status === 401) {
        removeToken()
        // 已在登录页时静默处理，不弹窗
        if (router.currentRoute.value.path !== '/login') {
          ElMessage.error('未登录或登录已过期')
          router.push('/login')
        }
      } else {
        ElMessage.error(error.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接异常，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default service
