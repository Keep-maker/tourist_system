package com.tourism.service.impl;

import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tourism.common.BusinessException;
import com.tourism.common.TokenUtil;
import com.tourism.dto.request.AdminCreateRequest;
import com.tourism.dto.request.AdminLoginRequest;
import com.tourism.dto.request.AdminUpdateRequest;
import com.tourism.dto.request.ChangePasswordRequest;
import com.tourism.dto.request.ProfileUpdateRequest;
import com.tourism.dto.request.RegisterRequest;
import com.tourism.dto.response.LoginResponse;
import com.tourism.entity.Admin;
import com.tourism.mapper.AdminMapper;
import com.tourism.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 管理员Service实现类
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    public LoginResponse login(AdminLoginRequest request) {
        // 根据账号和角色查询(用户端role=0，管理员端role=1)
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<Admin>()
                .eq(Admin::getAdminName, request.getAdminName());
        // 如果请求指定了role，则按role过滤(防止用户端账号登录管理员端)
        if (request.getRole() != null) {
            wrapper.eq(Admin::getRole, request.getRole());
        }
        Admin admin = adminMapper.selectOne(wrapper);
        if (admin == null) {
            throw new BusinessException("账号不存在或无权访问此端");
        }
        // MD5密码校验
        String md5Password = MD5.create().digestHex(request.getPassword());
        if (!admin.getPassword().equals(md5Password)) {
            throw new BusinessException("密码错误");
        }
        // 状态校验: 禁用用户(状态0)不允许登录
        if (admin.getStatus() != null && admin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用, 请联系管理员");
        }
        // 生成token
        String token = tokenUtil.generateToken(admin.getId());
        return new LoginResponse(token, admin.getId(), admin.getAdminName());
    }

    @Override
    public void logout(String token) {
        tokenUtil.removeToken(token);
    }

    @Override
    public Page<Admin> pageList(Long current, Long size, String keyword) {
        Page<Admin> page = new Page<>(current, size);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Admin::getAdminName, keyword);
        }
        wrapper.orderByDesc(Admin::getId);
        return adminMapper.selectPage(page, wrapper);
    }

    @Override
    public Admin getById(Integer id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        return admin;
    }

    @Override
    public void create(AdminCreateRequest request) {
        // 检查账号是否已存在
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getAdminName, request.getAdminName())
        );
        if (count > 0) {
            throw new BusinessException("账号已存在");
        }
        // 新增管理员(默认role=1管理员)
        Admin admin = new Admin();
        admin.setAdminName(request.getAdminName());
        admin.setPassword(MD5.create().digestHex(request.getPassword()));
        admin.setRole(1);
        adminMapper.insert(admin);
    }

    @Override
    public void update(AdminUpdateRequest request) {
        // 检查ID是否存在
        Admin admin = adminMapper.selectById(request.getId());
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        // 检查账号是否重复(排除自己)
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getAdminName, request.getAdminName())
                        .ne(Admin::getId, request.getId())
        );
        if (count > 0) {
            throw new BusinessException("账号已存在");
        }
        // 更新
        admin.setAdminName(request.getAdminName());
        if (StringUtils.hasText(request.getPassword())) {
            admin.setPassword(MD5.create().digestHex(request.getPassword()));
        }
        adminMapper.updateById(admin);
    }

    @Override
    public void delete(Integer id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }
        adminMapper.deleteById(id);
    }

    /**
     * 用户注册(普通用户role=0)
     * 校验账号唯一性后插入, 密码MD5加密
     */
    @Override
    public void register(RegisterRequest request) {
        // 检查账号是否已存在
        Long count = adminMapper.selectCount(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getAdminName, request.getAdminName())
        );
        if (count > 0) {
            throw new BusinessException("账号已存在");
        }
        Admin admin = new Admin();
        admin.setAdminName(request.getAdminName());
        admin.setPassword(MD5.create().digestHex(request.getPassword()));
        admin.setPhone(request.getPhone());
        // 注册的用户固定为普通用户(role=0)
        admin.setRole(0);
        adminMapper.insert(admin);
    }

    /**
     * 修改密码(需校验旧密码)
     */
    @Override
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        Admin admin = adminMapper.selectById(userId);
        if (admin == null) {
            throw new BusinessException("用户不存在");
        }
        // 校验旧密码
        String oldMd5 = MD5.create().digestHex(request.getOldPassword());
        if (!admin.getPassword().equals(oldMd5)) {
            throw new BusinessException("旧密码错误");
        }
        // 更新为新密码
        admin.setPassword(MD5.create().digestHex(request.getNewPassword()));
        adminMapper.updateById(admin);
    }

    /**
     * 修改个人资料(账号/手机/头像, 不含密码)
     */
    @Override
    public Admin updateProfile(Integer userId, ProfileUpdateRequest request) {
        Admin admin = adminMapper.selectById(userId);
        if (admin == null) {
            throw new BusinessException("用户不存在");
        }
        // 账号若修改需校验唯一性(排除自己)
        if (StringUtils.hasText(request.getAdminName())
                && !request.getAdminName().equals(admin.getAdminName())) {
            Long count = adminMapper.selectCount(
                    new LambdaQueryWrapper<Admin>()
                            .eq(Admin::getAdminName, request.getAdminName())
                            .ne(Admin::getId, userId)
            );
            if (count > 0) {
                throw new BusinessException("账号已存在");
            }
            admin.setAdminName(request.getAdminName());
        }
        // 手机号、头像直接更新(允许置空)
        admin.setPhone(request.getPhone());
        admin.setAvatar(request.getAvatar());
        adminMapper.updateById(admin);
        return admin;
    }

    /**
     * 分页查询普通用户列表(role=0)
     */
    @Override
    public Page<Admin> pageUsers(Long current, Long size, String keyword) {
        Page<Admin> page = new Page<>(current, size);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<Admin>()
                .eq(Admin::getRole, 0); // 只查普通用户
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Admin::getAdminName, keyword);
        }
        wrapper.orderByDesc(Admin::getId);
        return adminMapper.selectPage(page, wrapper);
    }

    /**
     * 启用/禁用用户(切换状态)
     */
    @Override
    public Integer toggleUserStatus(Integer userId) {
        Admin admin = adminMapper.selectById(userId);
        if (admin == null) {
            throw new BusinessException("用户不存在");
        }
        // 不允许禁用管理员账号
        if (admin.getRole() != null && admin.getRole() == 1) {
            throw new BusinessException("不允许禁用管理员账号");
        }
        // 切换状态: 1->0 或 0->1
        int newStatus = (admin.getStatus() == null || admin.getStatus() == 0) ? 1 : 0;
        admin.setStatus(newStatus);
        adminMapper.updateById(admin);
        return newStatus;
    }

    /**
     * 管理员重置用户密码(无需旧密码)
     */
    @Override
    public void resetPassword(Integer userId, String newPassword) {
        Admin admin = adminMapper.selectById(userId);
        if (admin == null) {
            throw new BusinessException("用户不存在");
        }
        admin.setPassword(MD5.create().digestHex(newPassword));
        adminMapper.updateById(admin);
    }
}
