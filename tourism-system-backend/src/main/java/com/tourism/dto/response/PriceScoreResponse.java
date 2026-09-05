package com.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 门票-评分散点数据响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceScoreResponse {

    /**
     * 门票价格
     */
    private BigDecimal price;

    /**
     * 评分
     */
    private BigDecimal score;

    /**
     * 景点名称(散点图tooltip显示)
     */
    private String spotName;
}
