package com.tourism.dto.request;

import lombok.Data;

/**
 * 个人资料更新请求DTO
 * 用户在个人中心修改自己的资料(不包含密码)
 */
@Data
public class ProfileUpdateRequest {

    /**
     * 用户账号(可改)
     */
    private String adminName;

    /**
     * 手机号(可改)
     */
    private String phone;

    /**
     * 头像URL(可改)
     */
    private String avatar;
}
