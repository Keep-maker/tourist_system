package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.dto.request.BehaviorRequest;
import com.tourism.dto.response.RecommendItemResponse;
import com.tourism.dto.response.SimilarityGraphResponse;
import com.tourism.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐Controller(景点推荐算法)
 * 所有接口需要token鉴权, 行为采集和猜你喜欢需要当前登录用户ID
 *
 * 提供接口:
 * - POST /api/recommend/behavior          上报用户行为(浏览/收藏)
 * - GET  /api/recommend/similar           基于内容的相似景点推荐
 * - GET  /api/recommend/guessYouLike      猜你喜欢(个性化混合推荐)
 * - GET  /api/recommend/hot               推荐热度榜
 * - GET  /api/recommend/similarityGraph   相似度网络图数据
 */
@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    /**
     * 上报用户行为(浏览/收藏)
     * 前端用户浏览景点详情或收藏时调用, 作为推荐算法数据源
     * @param adminId 当前登录用户ID(由拦截器注入)
     * @param request 行为请求(spotId + behaviorType)
     */
    @PostMapping("/behavior")
    public Result<Void> recordBehavior(@RequestAttribute("adminId") Integer adminId,
                                       @RequestBody BehaviorRequest request) {
        recommendService.recordBehavior(adminId, request.getSpotId(), request.getBehaviorType());
        return Result.success(null);
    }

    /**
     * 相似景点推荐(基于内容+协同过滤混合)
     * @param spotId 目标景点ID
     * @param topN 推荐数量(默认10)
     */
    @GetMapping("/similar")
    public Result<List<RecommendItemResponse>> similarSpots(
            @RequestParam Integer spotId,
            @RequestParam(defaultValue = "10") Integer topN) {
        List<RecommendItemResponse> result = recommendService.similarSpots(spotId, topN);
        return Result.success(result);
    }

    /**
     * 猜你喜欢(基于用户历史的个性化推荐, 新用户冷启动退化为热门)
     * @param adminId 当前登录用户ID(由拦截器注入)
     * @param topN 推荐数量(默认10)
     */
    @GetMapping("/guessYouLike")
    public Result<List<RecommendItemResponse>> guessYouLike(
            @RequestAttribute("adminId") Integer adminId,
            @RequestParam(defaultValue = "10") Integer topN) {
        List<RecommendItemResponse> result = recommendService.guessYouLike(adminId, topN);
        return Result.success(result);
    }

    /**
     * 推荐热度榜(被浏览/收藏最多的景点)
     * @param topN 排行数量(默认10)
     */
    @GetMapping("/hot")
    public Result<List<RecommendItemResponse>> hotRecommended(
            @RequestParam(defaultValue = "10") Integer topN) {
        List<RecommendItemResponse> result = recommendService.hotRecommended(topN);
        return Result.success(result);
    }

    /**
     * 相似度网络图数据(ECharts graph结构: 节点+边)
     * @param spotId 中心景点ID
     * @param topN 相似景点数量(默认10)
     */
    @GetMapping("/similarityGraph")
    public Result<SimilarityGraphResponse> similarityGraph(
            @RequestParam Integer spotId,
            @RequestParam(defaultValue = "10") Integer topN) {
        SimilarityGraphResponse result = recommendService.similarityGraph(spotId, topN);
        return Result.success(result);
    }

    /**
     * 查询当前用户的浏览历史(去重, 按最近浏览时间倒序)
     * @param adminId 当前登录用户ID(由拦截器注入)
     * @param topN 数量限制(默认20)
     */
    @GetMapping("/history")
    public Result<List<RecommendItemResponse>> browseHistory(
            @RequestAttribute("adminId") Integer adminId,
            @RequestParam(defaultValue = "20") Integer topN) {
        List<RecommendItemResponse> result = recommendService.browseHistory(adminId, topN);
        return Result.success(result);
    }
}
