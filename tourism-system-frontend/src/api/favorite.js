import request from '@/utils/request.js'

/**
 * 收藏/取消收藏(切换状态)
 * @param params { spotId }
 * @returns { favorited: true/false }
 */
export function toggleFavorite(spotId) {
  return request({
    url: '/api/favorite/toggle',
    method: 'post',
    params: { spotId }
  })
}

/**
 * 查询是否已收藏某景点
 * @param params { spotId }
 * @returns { favorited: true/false }
 */
export function checkFavorite(spotId) {
  return request({
    url: '/api/favorite/check',
    method: 'get',
    params: { spotId }
  })
}

/**
 * 查询我的收藏列表
 */
export function getMyFavorites() {
  return request({
    url: '/api/favorite/my',
    method: 'get'
  })
}

/**
 * 从收藏夹移除指定景点
 * @param spotId 景点ID
 */
export function removeFavorite(spotId) {
  return request({
    url: '/api/favorite/remove',
    method: 'delete',
    params: { spotId }
  })
}
