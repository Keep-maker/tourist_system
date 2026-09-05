import request from '@/utils/request.js'

/**
 * 上报用户行为(浏览/收藏), 作为推荐算法数据源
 * @param data { spotId, behaviorType(1浏览/2收藏) }
 */
export function recordBehavior(data) {
  return request({
    url: '/api/recommend/behavior',
    method: 'post',
    data
  })
}

/**
 * 相似景点推荐(基于内容+协同过滤混合)
 * @param params { spotId, topN }
 */
export function getSimilarSpots(params) {
  return request({
    url: '/api/recommend/similar',
    method: 'get',
    params
  })
}

/**
 * 猜你喜欢(基于用户历史的个性化推荐)
 * @param params { topN }
 */
export function getGuessYouLike(params) {
  return request({
    url: '/api/recommend/guessYouLike',
    method: 'get',
    params
  })
}

/**
 * 推荐热度榜(被浏览/收藏最多的景点)
 * @param params { topN }
 */
export function getHotRecommended(params) {
  return request({
    url: '/api/recommend/hot',
    method: 'get',
    params
  })
}

/**
 * 相似度网络图数据(ECharts graph结构)
 * @param params { spotId, topN }
 */
export function getSimilarityGraph(params) {
  return request({
    url: '/api/recommend/similarityGraph',
    method: 'get',
    params
  })
}

/**
 * 查询当前用户的浏览历史(去重, 按最近浏览时间倒序)
 * @param params { topN }
 */
export function getBrowseHistory(params) {
  return request({
    url: '/api/recommend/history',
    method: 'get',
    params
  })
}
