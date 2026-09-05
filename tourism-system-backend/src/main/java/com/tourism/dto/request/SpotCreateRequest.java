package com.tourism.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 新增/编辑景点请求DTO
 */
@Data
public class SpotCreateRequest {

    /**
     * 景点ID(编辑时必填)
     */
    private Integer id;

    /**
     * 所属城市ID
     */
    private Integer cityId;

    /**
     * 区县
     */
    private String district;

    /**
     * 景点名称
     */
    @NotBlank(message = "景点名称不能为空")
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
