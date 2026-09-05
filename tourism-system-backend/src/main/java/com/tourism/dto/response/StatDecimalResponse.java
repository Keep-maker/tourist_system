package com.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 带小数的统计项响应DTO(用于平均值类图表, 如各省平均门票价格)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatDecimalResponse {

    /**
     * 名称(省份名/区间名)
     */
    private String name;

    /**
     * 数值(平均值, 保留2位小数)
     */
    private BigDecimal value;
}
