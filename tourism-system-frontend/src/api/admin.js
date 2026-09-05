import request from '@/utils/request.js'

/**
 * 管理员登录
 */
export function login(data) {
  return request({
    url: '/api/admin/login',
    method: 'post',
    data
  })
}

/**
 * 管理员登出
 */
export function logout() {
  return request({
    url: '/api/admin/logout',
    method: 'post'
  })
}

/**
 * 管理员分页列表
 */
export function getAdminPage(params) {
  return request({
    url: '/api/admin/page',
    method: 'get',
    params
  })
}

/**
 * 新增管理员
 */
export function createAdmin(data) {
  return request({
    url: '/api/admin',
    method: 'post',
    data
  })
}

/**
 * 编辑管理员
 */
export function updateAdmin(data) {
  return request({
    url: '/api/admin',
    method: 'put',
    data
  })
}

/**
 * 删除管理员
 */
export function deleteAdmin(id) {
  return request({
    url: `/api/admin/${id}`,
    method: 'delete'
  })
}

/**
 * 用户注册(无需登录)
 */
export function register(data) {
  return request({
    url: '/api/admin/register',
    method: 'post',
    data
  })
}

/**
 * 修改密码(需校验旧密码)
 */
export function changePassword(data) {
  return request({
    url: '/api/admin/password',
    method: 'put',
    data
  })
}

/**
 * 获取当前登录用户资料
 */
export function getProfile() {
  return request({
    url: '/api/admin/profile',
    method: 'get'
  })
}

/**
 * 修改个人资料(账号/手机/头像)
 */
export function updateProfile(data) {
  return request({
    url: '/api/admin/profile',
    method: 'put',
    data
  })
}

/**
 * 分页查询普通用户列表(role=0)
 */
export function getUserPage(params) {
  return request({
    url: '/api/admin/users',
    method: 'get',
    params
  })
}

/**
 * 启用/禁用用户(切换状态)
 */
export function toggleUserStatus(userId) {
  return request({
    url: `/api/admin/users/${userId}/status`,
    method: 'put'
  })
}

/**
 * 管理员重置用户密码
 */
export function resetUserPassword(userId, newPassword) {
  return request({
    url: `/api/admin/users/${userId}/password`,
    method: 'put',
    data: { newPassword }
  })
}
