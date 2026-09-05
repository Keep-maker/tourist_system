package com.tourism.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 相似度网络图响应DTO
 * 直接对应ECharts graph关系图结构: 节点列表 + 边列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityGraphResponse {

    /**
     * 图节点列表(中心景点 + 相似景点)
     */
    private List<Node> nodes;

    /**
     * 图边列表(中心景点与各相似景点的连线, 粗细表示相似度)
     */
    private List<Edge> edges;

    /**
     * 图节点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        /**
         * 节点名称(景点名)
         */
        private String name;

        /**
         * 节点大小(中心节点更大)
         */
        private Integer symbolSize;

        /**
         * 分类(0=中心景点, 1=相似景点), 用于不同颜色
         */
        private Integer category;
    }

    /**
     * 图边
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        /**
         * 起点名称
         */
        private String source;

        /**
         * 终点名称
         */
        private String target;

        /**
         * 相似度(0~1, 决定连线粗细)
         */
        private Double value;
    }
}
