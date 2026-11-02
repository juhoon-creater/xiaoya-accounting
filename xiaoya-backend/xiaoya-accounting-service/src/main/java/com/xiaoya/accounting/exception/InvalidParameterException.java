package com.xiaoya.accounting.exception;

/**
 * 请求参数不符合接口约定时抛出的业务异常。
 */
public class InvalidParameterException extends RuntimeException {

    public InvalidParameterException(String message) {
        super(message);
    }
}
