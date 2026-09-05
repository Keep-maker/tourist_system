import request from '@/utils/request.js'

/**
 * 发表评论
 * @param data { spotId, content, rating(1-5) }
 */
export function addComment(data) {
  return request({
    url: '/api/comment',
    method: 'post',
    data
  })
}

/**
 * 查询某景点的评论列表
 * @param params { spotId, current, size }
 */
export function getCommentsBySpot(spotId, params) {
  return request({
    url: `/api/comment/spot/${spotId}`,
    method: 'get',
    params
  })
}

/**
 * 查询我的评论(个人中心)
 * @param params { current, size }
 */
export function getMyComments(params) {
  return request({
    url: '/api/comment/my',
    method: 'get',
    params
  })
}

/**
 * 删除评论
 */
export function deleteComment(commentId) {
  return request({
    url: `/api/comment/${commentId}`,
    method: 'delete'
  })
}

/**
 * 管理员查询全部评论(支持按景点/用户筛选)
 * @param params { spotId, userId, current, size }
 */
export function getAllComments(params) {
  return request({
    url: '/api/comment/all',
    method: 'get',
    params
  })
}

/**
 * 管理员删除任意评论(无需作者校验)
 */
export function adminDeleteComment(commentId) {
  return request({
    url: `/api/comment/admin/${commentId}`,
    method: 'delete'
  })
}
