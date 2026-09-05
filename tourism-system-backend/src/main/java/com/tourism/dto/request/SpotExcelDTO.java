package com.tourism.dto.request;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 景点Excel读写DTO
 * 字段与新数据集CSV列名一致(number/name/score/level/address/comments/price/sales/province/region)
 * 同时用于导入(按列名匹配)和导出(英文表头)
 */
@Data
public class SpotExcelDTO {

    /**
     * 序号(导入时忽略,导出时填景点ID)
     */
    @ExcelProperty("number")
    private Integer number;

    /**
     * 景点名称
     */
    @ExcelProperty("name")
    private String name;

    /**
     * 评分(0-1区间,导入时×5换算到0-5)
     */
    @ExcelProperty("score")
    private BigDecimal score;

    /**
     * 景区等级(如5A景区/4A景区;导入时存为spot_type,并提取数字存star_level)
     */
    @ExcelProperty("level")
    private String level;

    /**
     * 详细地址
     */
    @ExcelProperty("address")
    private String address;

    /**
     * 景点一句话介绍(导入时存为spot_intro)
     */
    @ExcelProperty("comments")
    private String comments;

    /**
     * 门票价格(导入时存为ticket_price)
     */
    @ExcelProperty("price")
    private BigDecimal price;

    /**
     * 销量(导入时存为sales_volume)
     */
    @ExcelProperty("sales")
    private BigDecimal sales;

    /**
     * 省份(全称,如"北京市")
     */
    @ExcelProperty("province")
    private String province;

    /**
     * 区域(格式"省·市·区",导入时拆分:第1段省、第2段市、第3段区县)
     */
    @ExcelProperty("region")
    private String region;
}
