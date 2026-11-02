package com.xiaoya.accounting.exception;

/**
 * 当前用户查询的账单不存在，或不属于当前用户时抛出。
 */
public class BillNotFoundException extends RuntimeException {

    public BillNotFoundException() {
        super("账单不存在或无权访问");
    }
}