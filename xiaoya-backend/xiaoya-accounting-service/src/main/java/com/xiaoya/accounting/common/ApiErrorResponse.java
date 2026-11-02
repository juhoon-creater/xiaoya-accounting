package com.xiaoya.accounting.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一包装记账服务的接口失败响应。
 */
@Data
@AllArgsConstructor
public class ApiErrorResponse {

    /** 0 表示请求失败。 */
    private int code;

    /** 面向调用方的简要错误说明。 */
    private String msg;

    /** 稳定的错误标识，客户端逻辑应依据它判断错误类型。 */
    private String errorCode;

    /** 失败时固定为 null。 */
    private Object data;

    /**
     * 创建标准失败响应。
     */
    public static ApiErrorResponse failure(String msg, String errorCode) {
        return new ApiErrorResponse(0, msg, errorCode, null);
    }
}
