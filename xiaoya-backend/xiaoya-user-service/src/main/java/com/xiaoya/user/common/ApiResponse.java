package com.xiaoya.user.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一的接口成功响应结构。
 *
 * @param <T> 接口业务数据的类型
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {

    /** 1 表示请求成功。 */
    private int code;

    /** 简要响应信息。 */
    private String msg;

    /** 接口实际返回的数据。 */
    private T data;

    /**
     * 创建标准成功响应。
     *
     * @param data 接口返回的数据
     * @param <T> 业务数据类型
     * @return 包装后的成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(1, "success", data);
    }
}