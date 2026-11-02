package com.xiaoya.user.exception;

/**
 * 用户名或密码错误时抛出的业务异常。
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("用户名或密码错误");
    }
}
