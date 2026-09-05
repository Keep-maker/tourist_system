import request from '@/utils/request.js'

/**
 * 景点分页查询
 */
export function getSpotPage(params) {
  return request({
    url: '/api/admin/spot/page',
    method: 'get',
    params
  })
}

/**
 * 景点详情
 */
export function getSpotDetail(id) {
  return request({
    url: `/api/admin/spot/${id}`,
    method: 'get'
  })
}

/**
 * 新增景点
 */
export function createSpot(data) {
  return request({
    url: '/api/admin/spot',
    method: 'post',
    data
  })
}

/**
 * 编辑景点
 */
export function updateSpot(data) {
  return request({
    url: '/api/admin/spot',
    method: 'put',
    data
  })
}

/**
 * 删除景点
 */
export function deleteSpot(id) {
  return request({
    url: `/api/admin/spot/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除景点
 */
export function batchDeleteSpot(ids) {
  return request({
    url: '/api/admin/spot/batch',
    method: 'post',
    data: { ids }
  })
}

/**
 * 导入景点Excel
 */
export function importSpot(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/admin/spot/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 导出景点Excel
 */
export function exportSpot(params) {
  return request({
    url: '/api/admin/spot/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
