package com.tourism.dto.request;

import lombok.Data;

/**
 * 编辑管理员请求DTO
 */
@Data
public class AdminUpdateRequest {

    /**
     * 管理员ID
     */
    private Integer id;

    /**
     * 管理员账号
     */
    private String adminName;

    /**
     * 新密码(为空则不修改)
     */
    private String password;
}
