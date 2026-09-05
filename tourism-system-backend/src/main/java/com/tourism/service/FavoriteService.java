package com.tourism.service;

import com.tourism.dto.response.RecommendItemResponse;

import java.util.List;

/**
 * 收藏Service接口
 * 复用 user_behavior 表(behavior_type=2 表示收藏)
 */
public interface FavoriteService {

    /**
     * 收藏/取消收藏(切换状态)
     * @param userId 用户ID
     * @param spotId 景点ID
     * @return true=已收藏, false=已取消
     */
    boolean toggleFavorite(Integer userId, Integer spotId);

    /**
     * 查询用户是否已收藏某景点
     * @param userId 用户ID
     * @param spotId 景点ID
     * @return true=已收藏
     */
    boolean isFavorited(Integer userId, Integer spotId);

    /**
     * 查询用户收藏的景点列表(个人中心"我的收藏")
     * @param userId 用户ID
     * @return 收藏景点列表
     */
    List<RecommendItemResponse> listUserFavorites(Integer userId);

    /**
     * 从收藏夹移除指定景点
     * @param userId 用户ID
     * @param spotId 景点ID
     */
    void removeFavorite(Integer userId, Integer spotId);
}
