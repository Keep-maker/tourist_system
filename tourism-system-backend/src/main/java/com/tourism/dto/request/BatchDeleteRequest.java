package com.tourism.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 批量删除请求DTO
 */
@Data
public class BatchDeleteRequest {

    /**
     * ID列表
     */
    private List<Integer> ids;
}
