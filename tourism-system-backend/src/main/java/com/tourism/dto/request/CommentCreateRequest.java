package com.tourism.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 发表评论请求DTO
 */
@Data
public class CommentCreateRequest {

    /**
     * 景点ID
     */
    @NotNull(message = "景点ID不能为空")
    private Integer spotId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不超过500字")
    private String content;

    /**
     * 评分(1-5星)
     */
    @NotNull(message = "评分不能为空")
    private Integer rating;
}
