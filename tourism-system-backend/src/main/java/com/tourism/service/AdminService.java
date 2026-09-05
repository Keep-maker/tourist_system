package com.tourism.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.dto.request.AdminCreateRequest;
import com.tourism.dto.request.AdminLoginRequest;
import com.tourism.dto.request.AdminUpdateRequest;
import com.tourism.dto.request.ChangePasswordRequest;
import com.tourism.dto.request.ProfileUpdateRequest;
import com.tourism.dto.request.RegisterRequest;
import com.tourism.dto.response.LoginResponse;
import com.tourism.entity.Admin;

/**
 * 管理员Service接口
 */
public interface AdminService {

    /**
     * 管理员登录
     * @param request 登录请求
     * @return 登录响应(含token)
     */
    LoginResponse login(AdminLoginRequest request);

    /**
     * 管理员登出
     * @param token token字符串
     */
    void logout(String token);

    /**
     * 分页查询管理员列表
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 关键字(账号模糊查询)
     * @return 分页结果
     */
    Page<Admin> pageList(Long current, Long size, String keyword);

    /**
     * 根据ID查询管理员
     * @param id 管理员ID
     * @return 管理员信息
     */
    Admin getById(Integer id);

    /**
     * 新增管理员
     * @param request 新增请求
     */
    void create(AdminCreateRequest request);

    /**
     * 编辑管理员
     * @param request 编辑请求
     */
    void update(AdminUpdateRequest request);

    /**
     * 删除管理员
     * @param id 管理员ID
     */
    void delete(Integer id);

    /**
     * 用户注册(普通用户role=0)
     * @param request 注册请求
     */
    void register(RegisterRequest request);

    /**
     * 修改密码(需校验旧密码)
     * @param userId 当前用户ID
     * @param request 改密请求
     */
    void changePassword(Integer userId, ChangePasswordRequest request);

    /**
     * 修改个人资料(账号/手机/头像, 不含密码)
     * @param userId 当前用户ID
     * @param request 资料更新请求
     * @return 更新后的用户信息
     */
    Admin updateProfile(Integer userId, ProfileUpdateRequest request);

    /**
     * 分页查询普通用户列表(role=0)
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 账号关键字模糊查询
     * @return 用户分页
     */
    Page<Admin> pageUsers(Long current, Long size, String keyword);

    /**
     * 启用/禁用用户(切换状态)
     * @param userId 用户ID
     * @return 更新后的状态(1=启用, 0=禁用)
     */
    Integer toggleUserStatus(Integer userId);

    /**
     * 管理员重置用户密码(无需旧密码)
     * @param userId 用户ID
     * @param newPassword 新密码(明文, 后端MD5加密)
     */
    void resetPassword(Integer userId, String newPassword);
}
