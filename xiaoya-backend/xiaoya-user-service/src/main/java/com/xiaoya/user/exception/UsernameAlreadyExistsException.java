package com.xiaoya.user.exception;

/**
 * 用户名已被注册时抛出的业务异常。
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException() {
        super("用户名已被使用");
    }
}