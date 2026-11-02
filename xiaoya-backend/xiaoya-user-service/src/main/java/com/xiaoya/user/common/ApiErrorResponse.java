package com.xiaoya.user.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一的接口失败响应结构。
 */
@Data
@AllArgsConstructor
public class ApiErrorResponse {

    /** 0 表示请求失败。 */
    private int code;

    /** 提供给调用方查看的简要错误说明。 */
    private String msg;

    /** 稳定的错误标识，前端应根据它判断错误类型。 */
    private String errorCode;

    /** 失败时没有业务数据，固定为 null。 */
    private Object data;

    /**
     * 创建标准失败响应。
     *
     * @param msg 错误说明
     * @param errorCode 稳定的错误标识
     * @return 包装后的失败响应
     */
    public static ApiErrorResponse failure(String msg, String errorCode) {
        return new ApiErrorResponse(0, msg, errorCode, null);
    }
}
