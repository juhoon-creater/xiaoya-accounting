package com.xiaoya.accounting.exception;

import com.xiaoya.accounting.common.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;


/**
 * 集中处理记账服务的异常，并转换为统一 HTTP 错误响应。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 将非法参数异常转换为接口文档约定的 400 响应。
     */
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(
            InvalidParameterException exception) {
        ApiErrorResponse body = ApiErrorResponse.failure(
                exception.getMessage(),
                "INVALID_PARAMETER"
        );
        log.warn("业务参数校验失败: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    /**
     * 将请求 DTO 的字段校验错误转换为统一的 400 响应。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            BindException exception) {
        // 取第一个字段的校验提示；没有具体字段错误时使用通用提示。
        var fieldError = exception.getFieldError();
        String message = (fieldError == null)
                ? "请求参数不合法"
                : fieldError.getDefaultMessage();

        ApiErrorResponse body = ApiErrorResponse.failure(
                message,
                "INVALID_PARAMETER"
        );
        log.warn("请求字段校验失败: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    /**
     * 将 JSON 格式或字段类型转换错误转换为统一的 400 响应。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception) {

        ApiErrorResponse body = ApiErrorResponse.failure(
                "请求 JSON 格式或字段类型不正确",
                "INVALID_PARAMETER"
        );
        log.warn("请求 JSON 解析失败");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    /**
     * 处理未预期异常，只向调用方返回通用错误信息。
     */
    /**
     * 处理当前用户无法访问的账单详情。
     */
    @ExceptionHandler(BillNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBillNotFound(
            BillNotFoundException exception) {

        log.warn("账单详情不存在或不属于当前用户");

        ApiErrorResponse body = ApiErrorResponse.failure(
                exception.getMessage(),
                "BILL_NOT_FOUND"
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception) {

        // 详细异常只留在服务端日志中，不返回给 App。
        log.error("记账服务发生未预期异常", exception);

        ApiErrorResponse body = ApiErrorResponse.failure(
                "服务内部错误",
                "INTERNAL_ERROR"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}