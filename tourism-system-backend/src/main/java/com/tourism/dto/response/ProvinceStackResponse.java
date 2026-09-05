package com.tourism.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 等级×省份堆叠柱状图响应DTO
 * 直接对应ECharts堆叠柱状图结构: x轴省份 + 多条series(每个等级一条)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceStackResponse {

    /**
     * x轴省份名列表(已按景点总数降序, 取TOP12)
     */
    private List<String> provinces;

    /**
     * 每个等级一条series, data顺序与provinces对齐
     */
    private List<SeriesItem> series;

    /**
     * 单条series(一个等级的各省数据)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeriesItem {
        /**
         * 等级名(如"5A景区")
         */
        private String name;

        /**
         * 各省份数量, 顺序与provinces一致
         */
        private List<Long> data;
    }
}
