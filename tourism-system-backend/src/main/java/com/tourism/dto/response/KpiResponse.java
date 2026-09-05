package com.tourism.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 大屏KPI概览响应DTO
 * 所有指标均在筛选条件范围内统计
 */
@Data
public class KpiResponse {

    /**
     * 景点总数(满足筛选条件)
     */
    private Long totalCount;

    /**
     * 平均门票价格
     */
    private BigDecimal avgPrice;

    /**
     * 平均评分(含0分未评景点)
     */
    private BigDecimal avgScore;

    /**
     * 总销量
     */
    private BigDecimal totalSales;

    /**
     * 免费景点数
     */
    private Long freeCount;

    /**
     * 5A景点数
     */
    private Long level5ACount;

    /**
     * 4A及以上景点数(4A + 5A)
     */
    private Long level4APlusCount;
}
