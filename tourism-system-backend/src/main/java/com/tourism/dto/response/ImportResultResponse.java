package com.tourism.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入结果响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {

    /**
     * 总读取行数
     */
    private Integer total;

    /**
     * 成功入库行数
     */
    private Integer success;

    /**
     * 被过滤丢弃行数
     */
    private Integer fail;
}
