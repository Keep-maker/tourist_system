package com.tourism.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 新增/编辑城市请求DTO
 */
@Data
public class CityCreateRequest {

    /**
     * 城市ID(编辑时必填)
     */
    private Integer id;

    /**
     * 省份
     */
    @NotBlank(message = "省份不能为空")
    private String province;

    /**
     * 城市名称
     */
    @NotBlank(message = "城市名称不能为空")
    private String city;
}
