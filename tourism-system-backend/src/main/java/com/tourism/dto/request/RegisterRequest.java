package com.tourism.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户注册请求DTO
 * 普通用户自助注册(role=0)
 */
@Data
public class RegisterRequest {

    /**
     * 用户账号
     */
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 20, message = "账号长度3-20个字符")
    private String adminName;

    /**
     * 密码(明文, 后端MD5加密存储)
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20个字符")
    private String password;

    /**
     * 手机号(选填)
     */
    private String phone;
}
