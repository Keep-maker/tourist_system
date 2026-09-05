package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理员实体类
 * 对应数据库表: admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("admin")
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 管理员账号
     */
    private String adminName;

    /**
     * 密码(MD5加密存储)
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色：1=管理员，0=普通用户
     */
    private Integer role;

    /**
     * 状态: 1=启用, 0=禁用(仅普通用户使用, 禁用后无法登录)
     */
    private Integer status;
}
