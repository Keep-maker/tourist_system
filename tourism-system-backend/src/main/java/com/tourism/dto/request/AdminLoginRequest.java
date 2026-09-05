package com.tourism.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理员登录请求DTO
 */
@Data
public class AdminLoginRequest {

    /**
     * 管理员账号
     */
    @NotBlank(message = "账号不能为空")
    private String adminName;

    /**
     * 密码(明文，后端MD5校验)
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 角色：1=管理员端登录，0=用户端登录
     */
    private Integer role;
}
