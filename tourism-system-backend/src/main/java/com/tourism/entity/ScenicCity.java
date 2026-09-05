package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 景点城市实体类
 * 对应数据库表: scenic_city
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("scenic_city")
public class ScenicCity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 省份名称
     */
    private String province;

    /**
     * 城市名称
     */
    private String city;
}
