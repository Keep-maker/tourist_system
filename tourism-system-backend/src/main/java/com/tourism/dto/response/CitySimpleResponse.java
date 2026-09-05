package com.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 城市简单响应DTO(下拉选择用)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitySimpleResponse {

    /**
     * 城市ID
     */
    private Integer id;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市名称
     */
    private String city;
}
