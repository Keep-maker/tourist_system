package com.tourism.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应VO
 * 封装分页查询的返回结果
 * 
 * @param <T> 数据类型
 */
@Data
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    public PageVO() {
    }

    public PageVO(Long current, Long size, Long total, Long pages, List<T> records) {
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = pages;
        this.records = records;
    }

    /**
     * 从MyBatis-Plus的IPage转换
     */
    public static <T> PageVO<T> fromIPage(IPage<T> page) {
        return new PageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getPages(),
                page.getRecords()
        );
    }

    /**
     * 转换方法(用于类型转换)
     */
    public <R> PageVO<R> convert(java.util.function.Function<? super T, ? extends R> converter) {
        java.util.ArrayList<R> newRecords = new java.util.ArrayList<>();
        if (this.records != null) {
            for (T item : this.records) {
                newRecords.add(converter.apply(item));
            }
        }
        return new PageVO<>(this.current, this.size, this.total, this.pages, newRecords);
    }
}
