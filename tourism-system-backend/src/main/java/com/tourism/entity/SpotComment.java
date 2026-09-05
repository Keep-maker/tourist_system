package com.tourism.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 景点评论实体类
 * 对应数据库表: spot_comment
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("spot_comment")
public class SpotComment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID, 自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 景点ID
     */
    private Integer spotId;

    /**
     * 评论用户ID
     */
    private Integer userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评分(1-5星)
     */
    private Integer rating;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;
}
