package com.tourism.service;

import com.tourism.dto.response.RecommendItemResponse;
import com.tourism.dto.response.SimilarityGraphResponse;

import java.util.List;

/**
 * 推荐Service接口
 * 提供基于内容的推荐、协同过滤、混合推荐等算法能力
 */
public interface RecommendService {

    /**
     * 记录用户行为(浏览/收藏), 作为推荐算法数据源
     * @param userId 用户ID
     * @param spotId 景点ID
     * @param behaviorType 行为类型(1=浏览, 2=收藏)
     */
    void recordBehavior(Integer userId, Integer spotId, Integer behaviorType);

    /**
     * 基于内容的相似景点推荐
     * 计算目标景点与全量景点的特征相似度, 返回TOP N
     * @param spotId 目标景点ID
     * @param topN 推荐数量
     * @return 相似景点列表(按相似度降序)
     */
    List<RecommendItemResponse> similarSpots(Integer spotId, Integer topN);

    /**
     * 猜你喜欢(混合推荐)
     * 基于用户历史行为, 融合协同过滤与内容推荐
     * @param userId 用户ID
     * @param topN 推荐数量
     * @return 推荐景点列表(新用户冷启动时退化为热门推荐)
     */
    List<RecommendItemResponse> guessYouLike(Integer userId, Integer topN);

    /**
     * 推荐热度榜(被浏览/收藏最多的景点)
     * @param topN 排行数量
     * @return 热门景点列表(按行为次数降序)
     */
    List<RecommendItemResponse> hotRecommended(Integer topN);

    /**
     * 查询用户浏览历史(去重, 按最近浏览时间倒序)
     * @param userId 用户ID
     * @param topN 数量限制(默认20)
     * @return 浏览过的景点列表(已去重, 每条景点保留最近一次浏览时间)
     */
    List<RecommendItemResponse> browseHistory(Integer userId, Integer topN);

    /**
     * 相似度网络图数据(中心景点 + TOP N相似景点及其连线)
     * @param spotId 中心景点ID
     * @param topN 相似景点数量
     * @return ECharts graph结构(节点+边)
     */
    SimilarityGraphResponse similarityGraph(Integer spotId, Integer topN);
}
