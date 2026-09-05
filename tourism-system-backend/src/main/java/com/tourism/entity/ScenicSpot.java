package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 景点实体类
 * 对应数据库表: scenic_spot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("scenic_spot")
public class ScenicSpot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属城市ID(外键关联 scenic_city.id)
     */
    private Integer cityId;

    /**
     * 区县
     */
    private String district;

    /**
     * 景点名称
     */
    private String spotName;

    /**
     * 景点评分(0-5分)
     */
    private BigDecimal score;

    /**
     * 门票价格
     */
    private BigDecimal ticketPrice;

    /**
     * 销量(用于高销量排行)
     */
    private BigDecimal salesVolume;

    /**
     * 坐标字符串(如"121.728112,31.059636")
     */
    private String coordinate;

    /**
     * 景点介绍
     */
    private String spotIntro;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 景点类型(休闲类、小众特色类、城市地标类、自然类、文化类)
     */
    private String spotType;

    /**
     * 星级
     */
    private Integer starLevel;
}
