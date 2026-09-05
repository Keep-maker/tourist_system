package com.tourism.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求DTO
 * 所有需要分页查询的请求都使用此DTO作为基类
 */
@Data
public class PageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认1
     */
    private Long current = 1L;

    /**
     * 每页大小，默认10
     */
    private Long size = 10L;

    /**
     * 获取偏移量(用于MyBatis的limit计算)
     */
    public Long getOffset() {
        return (current - 1) * size;
    }
}
