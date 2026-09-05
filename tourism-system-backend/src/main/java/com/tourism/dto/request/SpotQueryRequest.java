package com.tourism.dto.request;

import com.tourism.common.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 景点多条件分页查询请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpotQueryRequest extends PageDTO {

    /**
     * 省份
     */
    private String province;

    /**
     * 城市ID
     */
    private Integer cityId;

    /**
     * 景点类型
     */
    private String spotType;

    /**
     * 景点名称(模糊查询)
     */
    private String spotName;

    /**
     * 最低评分
     */
    private BigDecimal minScore;

    /**
     * 最高评分
     */
    private BigDecimal maxScore;

    /**
     * 最低门票价格
     */
    private BigDecimal minPrice;

    /**
     * 最高门票价格
     */
    private BigDecimal maxPrice;
}
