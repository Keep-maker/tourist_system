import request from '@/utils/request.js'

/**
 * 城市分页查询
 */
export function getCityPage(params) {
  return request({
    url: '/api/city/page',
    method: 'get',
    params
  })
}

/**
 * 获取所有省份
 */
export function getProvinces() {
  return request({
    url: '/api/city/provinces',
    method: 'get'
  })
}

/**
 * 按省份获取城市
 */
export function getCitiesByProvince(province) {
  return request({
    url: '/api/city/listByProvince',
    method: 'get',
    params: { province }
  })
}

/**
 * 新增城市
 */
export function createCity(data) {
  return request({
    url: '/api/city',
    method: 'post',
    data
  })
}

/**
 * 编辑城市
 */
export function updateCity(data) {
  return request({
    url: '/api/city',
    method: 'put',
    data
  })
}

/**
 * 删除城市
 */
export function deleteCity(id) {
  return request({
    url: `/api/city/${id}`,
    method: 'delete'
  })
}
