package com.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统计项响应DTO(用于柱状图/饼图)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatResponse {

    /**
     * 名称(省份名/类型名)
     */
    private String name;

    /**
     * 数量/值
     */
    private Long value;
}
