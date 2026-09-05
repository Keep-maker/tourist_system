package com.tourism.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统异常，不向前端暴露堆栈信息
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 处理参数校验异常(@Valid注解)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidException(MethodArgumentNotValidException e) {
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        String message = errors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error("参数校验失败: " + message);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String message = error != null ? error.getField() + ": " + error.getDefaultMessage() : "参数绑定失败";
        return Result.error(message);
    }

    /**
     * 处理请求参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingParamException(MissingServletRequestParameterException e) {
        return Result.error("缺少必要参数: " + e.getParameterName());
    }

    /**
     * 处理系统异常(兜底)
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        // 记录堆栈到日志，但不向前端暴露
        log.error("系统异常", e);
        return Result.systemError("系统繁忙，请稍后重试");
    }
}
