package com.tourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tourism.dto.response.RecommendItemResponse;
import com.tourism.entity.ScenicCity;
import com.tourism.entity.ScenicSpot;
import com.tourism.entity.UserBehavior;
import com.tourism.mapper.ScenicCityMapper;
import com.tourism.mapper.ScenicSpotMapper;
import com.tourism.mapper.UserBehaviorMapper;
import com.tourism.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 收藏Service实现类
 * 复用 user_behavior 表, behavior_type=2 表示收藏
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Autowired
    private ScenicCityMapper scenicCityMapper;

    /**
     * 收藏行为类型常量
     */
    private static final int BEHAVIOR_FAVORITE = 2;

    /**
     * 收藏/取消收藏(切换状态)
     */
    @Override
    public boolean toggleFavorite(Integer userId, Integer spotId) {
        // 查是否已收藏
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getSpotId, spotId)
                .eq(UserBehavior::getBehaviorType, BEHAVIOR_FAVORITE);
        UserBehavior exist = userBehaviorMapper.selectOne(wrapper);

        if (exist != null) {
            // 已收藏 -> 取消收藏
            userBehaviorMapper.deleteById(exist.getId());
            return false;
        } else {
            // 未收藏 -> 添加收藏
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setSpotId(spotId);
            behavior.setBehaviorType(BEHAVIOR_FAVORITE);
            userBehaviorMapper.insert(behavior);
            return true;
        }
    }

    /**
     * 查询是否已收藏
     */
    @Override
    public boolean isFavorited(Integer userId, Integer spotId) {
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getSpotId, spotId)
                .eq(UserBehavior::getBehaviorType, BEHAVIOR_FAVORITE);
        Long count = userBehaviorMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    /**
     * 查询用户收藏列表(复用 RecommendItemResponse 结构)
     */
    @Override
    public List<RecommendItemResponse> listUserFavorites(Integer userId) {
        // 查用户收藏的景点ID列表(按收藏时间倒序)
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getBehaviorType, BEHAVIOR_FAVORITE)
                .orderByDesc(UserBehavior::getCreateTime);
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(wrapper);
        if (behaviors.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询景点信息(避免N+1)
        List<Integer> spotIds = behaviors.stream().map(UserBehavior::getSpotId).collect(Collectors.toList());
        List<ScenicSpot> spots = scenicSpotMapper.selectBatchIds(spotIds);
        Map<Integer, ScenicSpot> spotMap = spots.stream()
                .collect(Collectors.toMap(ScenicSpot::getId, s -> s));

        // 加载城市映射补充省份/城市
        Map<Integer, String[]> cityMap = new HashMap<>();
        for (ScenicCity city : scenicCityMapper.selectList(null)) {
            cityMap.put(city.getId(), new String[]{city.getProvince(), city.getCity()});
        }

        // 按收藏顺序组装结果
        List<RecommendItemResponse> result = new ArrayList<>();
        for (UserBehavior b : behaviors) {
            ScenicSpot spot = spotMap.get(b.getSpotId());
            if (spot == null) {
                continue; // 景点已被删除
            }
            result.add(toRecommendItem(spot, cityMap));
        }
        return result;
    }

    /**
     * ScenicSpot -> RecommendItemResponse 转换(similarity不适用, 置0)
     */
    private RecommendItemResponse toRecommendItem(ScenicSpot spot, Map<Integer, String[]> cityMap) {
        RecommendItemResponse item = new RecommendItemResponse();
        item.setId(spot.getId());
        item.setSpotName(spot.getSpotName());
        item.setSpotType(spot.getSpotType());
        item.setScore(spot.getScore());
        item.setTicketPrice(spot.getTicketPrice());
        String[] info = cityMap.get(spot.getCityId());
        item.setProvince(info == null ? "" : info[0]);
        item.setCity(info == null ? "" : info[1]);
        item.setAddress(spot.getAddress());
        item.setSpotIntro(spot.getSpotIntro());
        item.setSimilarity(0.0);
        return item;
    }

    /**
     * 从收藏夹移除
     */
    @Override
    public void removeFavorite(Integer userId, Integer spotId) {
        LambdaQueryWrapper<UserBehavior> wrapper = new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getSpotId, spotId)
                .eq(UserBehavior::getBehaviorType, BEHAVIOR_FAVORITE);
        userBehaviorMapper.delete(wrapper);
    }
}
