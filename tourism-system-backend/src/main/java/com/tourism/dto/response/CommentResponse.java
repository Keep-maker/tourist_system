package com.tourism.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论响应DTO
 * 补充用户名/景点名等关联信息, 避免前端多次查询
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 景点ID
     */
    private Integer spotId;

    /**
     * 景点名称
     */
    private String spotName;

    /**
     * 评论用户ID
     */
    private Integer userId;

    /**
     * 评论用户名
     */
    private String userName;

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
