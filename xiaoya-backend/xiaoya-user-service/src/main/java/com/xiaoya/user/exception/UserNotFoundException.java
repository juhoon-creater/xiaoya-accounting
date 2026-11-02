package com.xiaoya.user.exception;

/**
 * 当前登录用户在数据库中不存在时抛出。
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("用户不存在");
    }
}
