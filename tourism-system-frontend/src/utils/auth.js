/**
 * Token本地存储工具
 */

const TOKEN_KEY = 'tourism_token'
const ADMIN_INFO_KEY = 'tourism_admin_info'

/**
 * 存储token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 获取token
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 删除token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ADMIN_INFO_KEY)
}

/**
 * 存储管理员信息
 */
export function setAdminInfo(info) {
  localStorage.setItem(ADMIN_INFO_KEY, JSON.stringify(info))
}

/**
 * 获取管理员信息
 */
export function getAdminInfo() {
  const info = localStorage.getItem(ADMIN_INFO_KEY)
  if (info) {
    return JSON.parse(info)
  }
  return null
}

/**
 * 检查是否已登录
 */
export function isLoggedIn() {
  return !!getToken()
}
