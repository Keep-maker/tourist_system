package com.tourism.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 推荐景点响应DTO
 * 在景点基本信息上增加"相似度/推荐分"字段, 用于推荐列表排序与展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendItemResponse {

    /**
     * 景点ID
     */
    private Integer id;

    /**
     * 景点名称
     */
    private String spotName;

    /**
     * 景点等级(5A景区/4A景区/3A景区/未评级)
     */
    private String spotType;

    /**
     * 评分
     */
    private BigDecimal score;

    /**
     * 门票价格
     */
    private BigDecimal ticketPrice;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 景点介绍
     */
    private String spotIntro;

    /**
     * 推荐分/相似度(0~1, 越大越相似, 用于排序)
     */
    private Double similarity;
}
