package com.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * Token
     */
    private String token;

    /**
     * 管理员ID
     */
    private Integer adminId;

    /**
     * 管理员账号
     */
    private String adminName;
}
