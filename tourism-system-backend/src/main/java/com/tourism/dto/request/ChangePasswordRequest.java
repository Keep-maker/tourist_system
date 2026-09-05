package com.tourism.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 修改密码请求DTO
 * 需校验旧密码, 防止越权修改
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码(明文, 后端MD5校验)
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码(明文, 后端MD5加密存储)
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
