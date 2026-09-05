package com.tourism.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 景点详情响应DTO(带城市信息)
 */
@Data
public class SpotDetailResponse {

    /**
     * 景点ID
     */
    private Integer id;

    /**
     * 所属城市ID
     */
    private Integer cityId;

    /**
     * 省份名称(关联查询)
     */
    private String province;

    /**
     * 城市名称(关联查询)
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 景点名称
     */
    private String spotName;

    /**
     * 景点类型
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
     * 销量
     */
    private BigDecimal salesVolume;

    /**
     * 景点介绍
     */
    private String spotIntro;

    /**
     * 地址
     */
    private String address;

    /**
     * 星级
     */
    private Integer starLevel;
}
