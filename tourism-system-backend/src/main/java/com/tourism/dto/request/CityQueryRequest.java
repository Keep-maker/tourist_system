package com.tourism.dto.request;

import com.tourism.common.PageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 城市分页查询请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CityQueryRequest extends PageDTO {

    /**
     * 省份(模糊查询)
     */
    private String province;

    /**
     * 城市(模糊查询)
     */
    private String city;
}
