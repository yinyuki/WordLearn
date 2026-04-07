package com.example.word.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> handleSQLIntegrityException(SQLIntegrityConstraintViolationException ex) {
        log.error("数据库异常: {}", ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + " 已存在";
            return R.error(msg);
        }
        return R.error("数据库操作失败，请重试");
    }

    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception ex) {
        log.error("系统未知错误", ex);
        return R.error("系统繁忙，请稍后再试: " + ex.getMessage());
    }
}