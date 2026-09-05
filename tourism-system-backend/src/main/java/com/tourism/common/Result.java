package com.tourism.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果封装类
 * 标准JSON格式: {code, msg, data}
 * 
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * 200:成功  401:未登录  400:业务错误  500:系统异常
     */
    private Integer code;

    /**
     * 消息提示
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    /**
     * 成功无数据
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功有数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功自定义消息
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    /**
     * 未登录
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(401, "未登录或登录已过期", null);
    }

    /**
     * 业务错误
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(400, msg, null);
    }

    /**
     * 系统异常
     */
    public static <T> Result<T> systemError(String msg) {
        return new Result<>(500, msg, null);
    }

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
