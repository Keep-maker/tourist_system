package com.tourism.controller;

import com.tourism.common.Result;
import com.tourism.dto.response.RecommendItemResponse;
import com.tourism.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏Controller
 * 所有接口需要token鉴权
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 收藏/取消收藏(切换状态)
     * POST /api/favorite/toggle?spotId=xxx
     * @return {favorited: true/false}
     */
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleFavorite(
            @RequestAttribute("adminId") Integer adminId,
            @RequestParam Integer spotId) {
        boolean favorited = favoriteService.toggleFavorite(adminId, spotId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        return Result.success(favorited ? "收藏成功" : "已取消收藏", data);
    }

    /**
     * 查询是否已收藏某景点
     * GET /api/favorite/check?spotId=xxx
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkFavorited(
            @RequestAttribute("adminId") Integer adminId,
            @RequestParam Integer spotId) {
        boolean favorited = favoriteService.isFavorited(adminId, spotId);
        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        return Result.success(data);
    }

    /**
     * 查询当前用户的收藏列表(个人中心"我的收藏")
     * GET /api/favorite/my
     */
    @GetMapping("/my")
    public Result<List<RecommendItemResponse>> listMyFavorites(
            @RequestAttribute("adminId") Integer adminId) {
        List<RecommendItemResponse> list = favoriteService.listUserFavorites(adminId);
        return Result.success(list);
    }

    /**
     * 从收藏夹移除指定景点
     * DELETE /api/favorite/remove?spotId=xxx
     */
    @DeleteMapping("/remove")
    public Result<Void> removeFavorite(
            @RequestAttribute("adminId") Integer adminId,
            @RequestParam Integer spotId) {
        favoriteService.removeFavorite(adminId, spotId);
        return Result.success(null);
    }
}
