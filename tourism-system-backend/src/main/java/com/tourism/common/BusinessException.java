package com.tourism.common;

/**
 * 业务异常类
 * 用于抛出业务逻辑相关的异常，由GlobalExceptionHandler统一处理
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
