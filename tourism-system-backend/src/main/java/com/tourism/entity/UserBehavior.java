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
 * 用户行为实体类
 * 对应数据库表: user_behavior
 * 记录用户对景点的浏览/收藏行为, 作为推荐算法(协同过滤)的数据源
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_behavior")
public class UserBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID, 自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID(关联 admin.id)
     */
    private Integer userId;

    /**
     * 景点ID(关联 scenic_spot.id)
     */
    private Integer spotId;

    /**
     * 行为类型: 1=浏览, 2=收藏
     */
    private Integer behaviorType;

    /**
     * 行为发生时间
     */
    private LocalDateTime createTime;
}
