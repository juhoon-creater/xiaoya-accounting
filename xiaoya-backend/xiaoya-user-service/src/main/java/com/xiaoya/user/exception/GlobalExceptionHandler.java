package com.xiaoya.user.exception;

import com.xiaoya.user.common.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 集中处理用户服务 Controller 抛出的异常，避免每个接口重复编写错误响应逻辑。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 触发的请求字段校验错误。
     *
     * @param exception Spring 包装的参数校验异常
     * @return HTTP 400 和第一个字段错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("请求参数不合法");

        ApiErrorResponse body = ApiErrorResponse.failure(message, "INVALID_PARAMETER");
        log.warn("用户请求字段校验失败: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 处理注册时发现用户名已存在的业务异常。
     *
     * @param exception 用户名重复异常
     * @return HTTP 409 和统一错误响应
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException exception) {
        ApiErrorResponse body = ApiErrorResponse.failure(
                exception.getMessage(),
                "USERNAME_ALREADY_EXISTS"
        );
        log.warn("用户注册失败：用户名已存在");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    /**
     * 处理用户名或密码错误的登录异常。
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception) {
        log.warn("用户登录失败：用户名或密码错误");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.failure(
                        exception.getMessage(),
                        "INVALID_CREDENTIALS"
                ));
    }
    /**
     * 处理未预期异常，避免把堆栈或敏感信息返回给客户端。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception) {
        // 详细异常只记录在服务端日志中；响应只返回固定错误信息。
        log.error("发生未预期异常", exception);

        ApiErrorResponse body = ApiErrorResponse.failure(
                "服务内部错误",
                "INTERNAL_ERROR"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
    /**
     * 处理已登录身份对应的用户记录不存在的情况。
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UserNotFoundException exception) {

        log.warn("查询当前用户失败：Token 对应的用户记录不存在");

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.failure(
                        exception.getMessage(),
                        "RESOURCE_NOT_FOUND"
                ));
    }
}