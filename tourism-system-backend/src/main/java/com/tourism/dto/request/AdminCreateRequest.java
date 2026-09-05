package com.tourism.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 新增管理员请求DTO
 */
@Data
public class AdminCreateRequest {

    /**
     * 管理员账号
     */
    @NotBlank(message = "账号不能为空")
    private String adminName;

    /**
     * 密码(明文，后端MD5加密存储)
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
