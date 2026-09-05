package com.tourism.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.Result;
import com.tourism.dto.request.AdminCreateRequest;
import com.tourism.dto.request.AdminLoginRequest;
import com.tourism.dto.request.AdminUpdateRequest;
import com.tourism.dto.request.ChangePasswordRequest;
import com.tourism.dto.request.ProfileUpdateRequest;
import com.tourism.dto.request.RegisterRequest;
import com.tourism.dto.response.LoginResponse;
import com.tourism.entity.Admin;
import com.tourism.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 管理员Controller
 * 登录接口不需要token，其余接口需要鉴权
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登录(无需token)
     * POST /api/admin/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        LoginResponse response = adminService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 管理员登出
     * POST /api/admin/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("token");
        adminService.logout(token);
        return Result.success();
    }

    /**
     * 分页查询管理员列表
     * GET /api/admin/page
     */
    @GetMapping("/page")
    public Result<Page<Admin>> pageList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        Page<Admin> page = adminService.pageList(current, size, keyword);
        return Result.success(page);
    }

    /**
     * 根据ID查询管理员
     * GET /api/admin/{id}
     */
    @GetMapping("/{id}")
    public Result<Admin> getById(@PathVariable Integer id) {
        Admin admin = adminService.getById(id);
        return Result.success(admin);
    }

    /**
     * 新增管理员
     * POST /api/admin
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody AdminCreateRequest request) {
        adminService.create(request);
        return Result.success("新增成功", null);
    }

    /**
     * 编辑管理员
     * PUT /api/admin
     */
    @PutMapping
    public Result<Void> update(@RequestBody AdminUpdateRequest request) {
        adminService.update(request);
        return Result.success("修改成功", null);
    }

    /**
     * 删除管理员
     * DELETE /api/admin/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        adminService.delete(id);
        return Result.success("删除成功", null);
    }

    /**
     * 用户注册(无需token)
     * POST /api/admin/register
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        adminService.register(request);
        return Result.success("注册成功", null);
    }

    /**
     * 修改密码(需登录, 校验旧密码)
     * PUT /api/admin/password
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestAttribute("adminId") Integer adminId,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        adminService.changePassword(adminId, request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 修改个人资料(账号/手机/头像)
     * PUT /api/admin/profile
     */
    @PutMapping("/profile")
    public Result<Admin> updateProfile(@RequestAttribute("adminId") Integer adminId,
                                       @RequestBody ProfileUpdateRequest request) {
        Admin admin = adminService.updateProfile(adminId, request);
        return Result.success("资料修改成功", admin);
    }

    /**
     * 获取当前登录用户信息(个人中心展示)
     * GET /api/admin/profile
     */
    @GetMapping("/profile")
    public Result<Admin> getProfile(@RequestAttribute("adminId") Integer adminId) {
        Admin admin = adminService.getById(adminId);
        return Result.success(admin);
    }

    // ==================== 用户管理(管理员端) ====================

    /**
     * 分页查询普通用户列表(role=0)
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public Result<Page<Admin>> pageUsers(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {
        Page<Admin> page = adminService.pageUsers(current, size, keyword);
        return Result.success(page);
    }

    /**
     * 启用/禁用用户(切换状态)
     * PUT /api/admin/users/{userId}/status
     */
    @PutMapping("/users/{userId}/status")
    public Result<Map<String, Object>> toggleUserStatus(@PathVariable Integer userId) {
        Integer status = adminService.toggleUserStatus(userId);
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("status", status);
        return Result.success(status == 1 ? "已启用" : "已禁用", data);
    }

    /**
     * 管理员重置用户密码
     * PUT /api/admin/users/{userId}/password
     */
    @PutMapping("/users/{userId}/password")
    public Result<Void> resetPassword(@PathVariable Integer userId,
                                      @RequestBody java.util.Map<String, String> body) {
        String newPassword = body.get("newPassword");
        adminService.resetPassword(userId, newPassword);
        return Result.success("密码已重置", null);
    }
}
