package com.tourism.dto.request;

import lombok.Data;

/**
 * 行为上报请求DTO
 * 前端用户浏览/收藏景点时上报, 作为推荐算法数据源
 */
@Data
public class BehaviorRequest {

    /**
     * 景点ID
     */
    private Integer spotId;

    /**
     * 行为类型: 1=浏览, 2=收藏
     */
    private Integer behaviorType;
}
